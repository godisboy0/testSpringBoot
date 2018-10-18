package com.mystory.twitter.Engine;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.mystory.twitter.model.FrontTwitterContent;
import com.mystory.twitter.model.MatchPlace;
import com.mystory.twitter.model.TwitterContent;
import com.mystory.twitter.model.UserInfo;
import com.mystory.twitter.repository.TwitterContentRepo;
import com.mystory.twitter.repository.UserInfoRepo;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Log4j
@Service
public class TwitterContentServer {
    @Autowired
    UserInfoRepo userInfoRepo;
    @Autowired
    TwitterContentRepo twitterContentRepo;
    private Gson gson = new Gson();

    public String getAllScreenNames() {
        StringBuilder stringBuilder = new StringBuilder();
        for (UserInfo userInfo : userInfoRepo.findAll()) {
            stringBuilder.append(userInfo.getScreenName()).append("; ");
        }
        return stringBuilder.toString();
    }

    private String getPlaceString(Integer places) {
        StringBuilder stringBuilder = new StringBuilder();
        if ((places & MatchPlace.originTweet) != 0) {
            stringBuilder.append("原始推文;");
        }
        if (((places & MatchPlace.originURL) | (places & MatchPlace.quotedUrl)) != 0) {
            stringBuilder.append("推文链接;");
        }
        if ((places & MatchPlace.quotedTweet) != 0) {
            stringBuilder.append("被引推文;");
        }
        if ((places & MatchPlace.mayMissed) != 0) {
            stringBuilder.append("无法解析部分外链;");
        }
        return stringBuilder.toString();
    }

    public List<FrontTwitterContent> getFrontTwitterContent(String screenNames, Date startTime, Date finishTime, Boolean narrowMatch) {
        log.info("read stored matched tweet for " + screenNames);
        List<FrontTwitterContent> ret = new ArrayList<>();
        List<TwitterContent> twitterContents = new ArrayList<>();
        List<String> names = Arrays.asList(screenNames.split("[;；]")).stream().map(String::trim).filter(s -> !Strings.isNullOrEmpty(s)).filter(s -> !s.contains(" ")).distinct().collect(Collectors.toList());
        for (String name : names) {
            twitterContents.addAll(twitterContentRepo.
                findByScreenNameAndIsQuotedAndUrlNarrowMatchAndTweetTimeGreaterThanAndTweetTimeLessThan(
                    name, false, narrowMatch, startTime, finishTime
                ));
        }
        if (!narrowMatch) {
            for (String name : names) {
                twitterContents.addAll(twitterContentRepo.
                    findByScreenNameAndIsQuotedAndUrlNarrowMatchAndTweetTimeGreaterThanAndTweetTimeLessThan(
                        name, false, !narrowMatch, startTime, finishTime
                    ));
            }
        }
        for (TwitterContent twitterContent : twitterContents) {
            FrontTwitterContent frontTwitterContent = new FrontTwitterContent();
            frontTwitterContent.setScreenName(twitterContent.getScreenName());
            frontTwitterContent.setTweetTime(twitterContent.getTweetTime());
            frontTwitterContent.setTweetContent(twitterContent.getTweetContent());
            frontTwitterContent.setTweetUrl(twitterContent.getTweetUrl());
            frontTwitterContent.setMatchKeyword(twitterContent.getMatchedKeyword());
            frontTwitterContent.setMatchPlace(getPlaceString(twitterContent.getFoundPlace()));
            frontTwitterContent.setNarrowMatchUrls(gson.fromJson(twitterContent.getNarrowMatchedUrls(), ArrayList.class));
            frontTwitterContent.setWideMatchUrls(gson.fromJson(twitterContent.getWideMatchedUrls(), ArrayList.class));
            frontTwitterContent.setMissedUrls(gson.fromJson(twitterContent.getMissedUrls(), ArrayList.class));
            frontTwitterContent.setQuotedTweets(null);
            List<Map<String, String>> quotedTweets = new ArrayList<>();
            while (twitterContent.getQuotedTweetSubjectID() != null) {
                HashMap<String, String> quotedTweet = new HashMap<>();
                twitterContent = twitterContentRepo.findOne(twitterContent.getQuotedTweetSubjectID());
                quotedTweet.put("tweetContent", twitterContent.getTweetContent());
                quotedTweet.put("tweetUrl", twitterContent.getTweetUrl());
                quotedTweets.add(quotedTweet);
            }
            if (!quotedTweets.isEmpty())
                frontTwitterContent.setQuotedTweets(quotedTweets);
            ret.add(frontTwitterContent);
        }

        return ret;
    }

}
