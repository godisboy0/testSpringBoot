package com.mystory.twitter.Engine;

import com.google.gson.Gson;
import com.mystory.twitter.model.MatchPlace;
import com.mystory.twitter.model.TwitterContent;
import com.mystory.twitter.model.UserInfo;
import com.mystory.twitter.model.vo.AnalysisResult;
import com.mystory.twitter.repository.ErrorReportRepo;
import com.mystory.twitter.repository.TwitterContentRepo;
import com.mystory.twitter.repository.UserInfoRepo;
import com.mystory.twitter.utils.KeyWordFilter;
import com.mystory.twitter.utils.KeywordFilterPool;
import lombok.Getter;
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
    private HttpClient httpClient = HttpClients.createDefault();
    private RequestConfig proxyConfig = null;
    private Gson gson = new Gson();
    private Map<String, String> cachedUrls = new HashMap<>();    //用于getUrlContent,已经查询过URL直接返回结果，不需要再拉取。
    private Random random = new Random();
    private LinkedList<Date> urlDate = new LinkedList<>();
    private LinkedList<Date> twitterDate = new LinkedList<>();

    @Getter
    private List<AnalysisResult> succeed = new ArrayList<>();
    @Getter
    private List<AnalysisResult> failed = new ArrayList<>();
    @Getter
    private List<String> nearlyFetchedUser = new ArrayList<>();

    /**
     * 根据配置文件中的值，确定是否设置网络代理
     */
    private void initNetwork() {
        RequestConfig.Builder configBuilder = RequestConfig.custom().setConnectTimeout(10000).setConnectionRequestTimeout(10000).setSocketTimeout(10000);
        if (blocked) {
            HttpHost proxy = new HttpHost(proxyhost, proxyport);
            configBuilder = configBuilder.setProxy(proxy).setProxy(proxy);
        }
        proxyConfig = configBuilder.build();
    }

    private void sleep(long sleepTime) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
            log.warn("Sleep Interrupted in rate Control");
        }
    }

    private void rateControl(String from) {
        long sleepTime = 0;
        long rate  = 0;         //一分钟最多爬几个
        LinkedList<Date> queue = null;
        if (from.equals("url")){
            queue = urlDate;
            rate = 5;   //url最快一分钟爬5个
        }else if (from.equals("twitter")){
            queue = twitterDate;
            rate = 1;   //推文最快1分钟爬一个
        } else return;

        Date nowDate = new Date();
        if (queue.size() == rate && (nowDate.getTime() - queue.peek().getTime() < 1000 * 60)) {
            sleepTime = 1000 * 60 - (nowDate.getTime() - queue.peek().getTime());
            sleep(sleepTime);   //先睡到一分钟整再说啦
            urlDate.pop();
        }
        sleepTime = random.nextInt(30000);
        sleep(sleepTime);
        queue.push(nowDate);

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
                httpGet.setConfig(proxyConfig);
                httpGet.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
                HttpResponse response = httpClient.execute(httpGet);
                //这里可以解析header的"content-type"字段（可能有各种大小写区别），寻找charset字段来确定具体的charset，不过目前就默认Utf-8了
                //response.getHeaders("content-type")
                HttpEntity entity = response.getEntity();
                String entityString = EntityUtils.toString(entity, "UTF-8");
                cachedUrls.put(url, entityString);
                //rateControl("url");
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
        Pattern p = Pattern.compile("<p(.*?)</p>", Pattern.CASE_INSENSITIVE);
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
    private Integer analysisUrlEntity(Status status, TwitterContent twitterContent, KeyWordFilter filter) {
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

    private Integer analysisStatus(Status status, TwitterContent twitterContent, KeyWordFilter filter, String uuid, Boolean isQuoted) {
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
        if (((foundPlace & MatchPlace.quotedTweet) | (foundPlace & MatchPlace.originTweet) | (foundPlace & MatchPlace.narrowMatched)) != 0) {
            twitterContent.setUrlNarrowMatch(true);
        } else {
            twitterContent.setUrlNarrowMatch(false);
        }

        if (foundPlace != 0 || isQuoted) {
            twitterContentRepo.save(twitterContent);
        }
        return foundPlace;
    }

    public String analysis(String screenName) {
        succeed.clear();
        failed.clear();
        nearlyFetchedUser.clear();
        initNetwork();
        List<UserInfo> users;
        if (screenName == null)
            users = userInfoRepo.findAll();
        else {
            users = new ArrayList<>();
            try {
                users.add(userInfoRepo.findByScreenName(screenName));
                if (users.isEmpty()) {
                    return "User Not Found";
                }
            } catch (Exception e) {
                return "User Not Found";
            }
        }
        for (val user : users) {
            int allMatched = 0;
            if ( user.getLastFetchTime() != null && new Date().getTime() - user.getLastFetchTime().getTime() < 1000 * 60 * 60 * 12) {
                nearlyFetchedUser.add(user.getScreenName());
                continue;
            }
            ArrayList<Status> statuses = new ArrayList<>();
            int pageNo = 1;
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
                    failed.add(new AnalysisResult(user.getScreenName(), 0, false));
                    break;
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
            //user.setLastFetchTime(new Date());
            succeed.add(new AnalysisResult(user.getScreenName(), allMatched, true));
            userInfoRepo.save(user);
            //rateControl("twitter");
        }
        return "更新完毕";
    }

    private void enrichTwitterContent(@NonNull TwitterContent twitterContent, @NonNull Status status) {

        twitterContent.setTweetID(status.getId());
        twitterContent.setTweetContent(status.getText());
        twitterContent.setRecordTime(new Date());
        twitterContent.setTweetTime(status.getCreatedAt());
        twitterContent.setScreenName(status.getUser().getScreenName());
        twitterContent.setTweetUrl("https://twitter.com/" + status.getUser().getScreenName() + "/status/" + status.getId());
    }

}
