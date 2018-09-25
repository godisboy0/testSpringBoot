package com.mystory.twitter.Engine;

import com.google.gson.Gson;
import com.mystory.twitter.model.MatchPlace;
import com.mystory.twitter.model.TwitterContent;
import com.mystory.twitter.model.UserInfo;
import com.mystory.twitter.repository.ErrorReportRepo;
import com.mystory.twitter.repository.TwitterContentRepo;
import com.mystory.twitter.repository.UserInfoRepo;
import com.mystory.twitter.utils.KeyWordFilter;
import com.mystory.twitter.utils.KeywordFilterPool;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import lombok.val;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j
@Component
public class ClimbTwitter {
    @Value("${blocked}")
    private Boolean blocked;
    @Value("${proxy.type}")
    private String proxyType;
    @Value("${proxy.host}")
    private String proxyhost;
    @Value("${proxy.port}")
    private Integer proxyport;
    @Value("${proxy.user}")
    private String proxyuser;
    @Value("${proxy.password}")
    private String proxypassword;
    @Autowired
    UserInfoRepo userInfoRepo;
    @Autowired
    TwitterContentRepo twitterContentRepo;
    @Autowired
    ErrorReportRepo errorReportRepo;
    @Autowired
    Twitter twitter;
    private HttpClient httpClient = null;
    private RequestConfig proxyConfig = null;
    private Gson gson = new Gson();
    private Map<String, String> cachedUrls = new HashMap<>();    //用于getUrlContent,已经查询过URL直接返回结果，不需要再拉取。

    public ClimbTwitter() {
        httpClient = HttpClients.createDefault();
    }

    /**
     * 根据配置文件中的值，确定是否设置网络代理
     */
    private void initNetwork() {
        if (blocked) {
            HttpHost proxy = new HttpHost(proxyhost, proxyport);
            proxyConfig = RequestConfig.custom().setProxy(proxy).build();
        }
    }

    private void rateControl() {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            log.warn("Sleep Interrupted in rate Control");
        }
    }

    /**
     * 从url中解析文本，进行匹配。因为无法提取到正文，因此可能匹配到并不相关的东西，以后再做筛选。
     *
     * @param url
     * @return 如get到文本，为文本，如果get不到文本，为空字符串
     */
    private String getUrlContent(String url) {
        try {
            if (cachedUrls.get(url) == null) {
                URI uri = new URIBuilder(url).build();
                HttpGet httpGet = new HttpGet(uri);
                if (blocked) {
                    httpGet.setConfig(proxyConfig);
                }
                httpGet.setHeader("User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
                HttpResponse response = httpClient.execute(httpGet);
                //这里可以解析header的"content-type"字段（可能有各种大小写区别），寻找charset字段来确定具体的charset，不过目前就默认Utf-8了
                //response.getHeaders("content-type")
                HttpEntity entity = response.getEntity();
                String entityString = EntityUtils.toString(entity, "UTF-8");
                ;
                cachedUrls.put(url, entityString);
            }
            return cachedUrls.get(url);
        } catch (Exception e) {
            log.error(url + " Can't be enriched " + e.getMessage());
            return "";
        }
    }

    /**
     * 抽取整个urlContent中被"< p>< /p>"括起来的部分。
     *
     * @param wholeUrlContent
     * @return
     */
    private String getNarrowUrlContent(String wholeUrlContent) {
        String ret = new String();
        Pattern p = Pattern.compile("<p(.*?)</p>");
        Matcher matcher = p.matcher(wholeUrlContent);
        while (matcher.find()) {
            ret += matcher.group(1);
        }
        return ret;
    }

    /**
     * 对一个status中引用的urlEntity进行筛选。
     *
     * @param status
     * @param twitterContent
     * @param filter
     * @return foundPlace，有可能包含MatchPlace.mayMissed，MatchPlace.narrowMatched，MatchPlace.originURL的任意组合
     */
    public Integer analysisUrlEntity(Status status, TwitterContent twitterContent, KeyWordFilter filter) {
        Integer foundPlace = 0;
        ArrayList<String> narrowMatchedUrls = new ArrayList<>();
        ArrayList<String> wideMatchedUrls = new ArrayList<>();
        ArrayList<String> missedUrls = new ArrayList<>();
        for (val urlEntity : status.getURLEntities()) {
            String urlContent = getUrlContent(urlEntity.getExpandedURL());
            if (urlContent == "") {
                foundPlace |= MatchPlace.mayMissed;
                twitterContent.setMayMissedSomeUrl(true);
                missedUrls.add(urlEntity.getExpandedURL());
            } else {
                String narrowContent = getNarrowUrlContent(urlContent);
                if (filter.matched(narrowContent)) {
                    foundPlace |= MatchPlace.narrowMatched | MatchPlace.originURL;
                    twitterContent.setUrlNarrowMatch(true);
                    narrowMatchedUrls.add(urlEntity.getExpandedURL());
                } else if (filter.matched(urlContent)) {
                    foundPlace |= MatchPlace.originURL;
                    wideMatchedUrls.add(urlEntity.getExpandedURL());
                }
            }
        }
        if (!narrowMatchedUrls.isEmpty()) {
            twitterContent.setNarrowMatchedUrls(gson.toJson(narrowMatchedUrls));
        } else {
            twitterContent.setNarrowMatchedUrls(null);
        }
        if (!wideMatchedUrls.isEmpty()) {
            twitterContent.setWideMatchedUrls(gson.toJson(wideMatchedUrls));
        } else {
            twitterContent.setWideMatchedUrls(null);
        }
        if (!missedUrls.isEmpty()) {
            twitterContent.setMissedUrls(gson.toJson(missedUrls));
        } else {
            twitterContent.setMissedUrls(null);
        }
        return foundPlace;
    }

    public Integer analysisStatus(Status status, TwitterContent twitterContent, KeyWordFilter filter, String uuid, Boolean isQuoted) {
        Integer foundPlace = 0;
        twitterContent.setSubjectID(uuid);
        twitterContent.setIsQuoted(isQuoted);
        enrichTwitterContent(twitterContent, status);
        if (filter.matched(status.getText())) {
            foundPlace |= MatchPlace.originTweet;
        }
        foundPlace |= analysisUrlEntity(status, twitterContent, filter);
        String quotedUUID = UUID.randomUUID().toString();
        Integer quotedFoundPlace = 0;
        if (status.getQuotedStatus() != null) {
            TwitterContent quotedTwitterContent = new TwitterContent();
            quotedFoundPlace = analysisStatus(status.getQuotedStatus(), quotedTwitterContent, filter, quotedUUID, true);
            if (foundPlace != 0) {
                twitterContent.setQuotedTweetSubjectID(quotedUUID);
                if ((quotedFoundPlace & MatchPlace.originTweet) != 0) {
                    foundPlace |= MatchPlace.quotedTweet;
                }
                if ((quotedFoundPlace & MatchPlace.originURL) != 0) {
                    foundPlace |= MatchPlace.quotedUrl;
                }
            } else twitterContent.setQuotedTweetSubjectID(null);
            foundPlace |= quotedFoundPlace;
        }
        twitterContent.setFoundPlace(foundPlace);
        twitterContent.setMatchedKeyword(gson.toJson(filter.getMatchedKeywords()));
        if (foundPlace != 0) {
            twitterContentRepo.save(twitterContent);
        }
        return foundPlace;
    }

    public String analysis(String screenName) {
        initNetwork();
        List<UserInfo> users;
        if (screenName == null)
            users = userInfoRepo.findAll();
        else {
            users = new ArrayList<>();
            try {
                users.add(userInfoRepo.findByScreenName(screenName));
                if (users.isEmpty()){
                    return "User Not Found";
                }
            } catch (Exception e){
                return "User Not Found";
            }
        }
        Integer allMatched = 0;
        for (val user : users) {
            ArrayList<Status> statuses = new ArrayList<>();
            Integer pageNo = 1;
            Long startId = user.getFirstGotID();
            Long finishID = user.getLastGotID();
            outer:
            while (true) {    //跳出条件只有时间
                try {
                    Paging page = new Paging(pageNo, 200);
                    val grapedStatus = twitter.getUserTimeline(user.getScreenName(), page);
                    for (Status status : grapedStatus) {
                        if (status.getCreatedAt().after(user.getStartTime())
                                && status.getCreatedAt().before(user.getFinishTime())) {
                            if (user.getKeywordChanged()) {
                                //当keyword发生变化时，所有的推文都需要匹配
                                statuses.add(status);
                            } else if (status.getId() < startId || status.getId() > finishID) {
                                //当keyword没有变化时，已经有的ID就可以不再匹配重复了
                                statuses.add(status);
                            }
                        }
                        if (status.getCreatedAt().before(user.getStartTime())) {
                            user.setKeywordChanged(false);
                            break outer;
                        }   //有一个数据的时间已经晚于筛选的结束时间了，直接跳出整个循环。
                    }
                    user.setKeywordChanged(false);
                    pageNo++;
                } catch (Exception e) {
                    log.error(String.format("Can't get Twitter for User %s", user.getScreenName()));
                }
            }
            //现在我们获得了这个user的符合时间段筛选条件的Status列表
            //现在从KeywordFilterPool中获得一个Filter，进行匹配
            for (Status status : statuses) {
                KeyWordFilter filter = KeywordFilterPool.getFilter(gson.fromJson(user.getKeyWords(), HashSet.class));
                TwitterContent twitterContent = new TwitterContent();
                Integer foundPlace = analysisStatus(status, twitterContent, filter, UUID.randomUUID().toString(), false);
                if (foundPlace != 0) {
                    ++allMatched;
                    if (twitterContent.getTweetID() < startId) {
                        startId = twitterContent.getTweetID();
                    }
                    if (twitterContent.getTweetID() > finishID) {
                        finishID = twitterContent.getTweetID();
                    }
                }
            }
            user.setFirstGotID(startId);
            user.setLastGotID(finishID);
            userInfoRepo.save(user);
        }
        return "finished analysis and " + allMatched + " matched tweets are found";
    }

    private void enrichTwitterContent(@NonNull TwitterContent twitterContent,@NonNull Status status) {

        twitterContent.setTweetID(status.getId());
        twitterContent.setTweetContent(status.getText());
        twitterContent.setRecordTime(new Date());
        twitterContent.setTweetTime(status.getCreatedAt());
        twitterContent.setScreenName(status.getUser().getScreenName());
        twitterContent.setTweetUrl("https://twitter.com/" + status.getUser().getScreenName() + "/status/" + status.getId());
    }

}
