package com.mystory.twitter.model;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class FrontTwitterContent {
    private String screenName;
    private Date tweetTime;
    private String matchKeyword;
    private String tweetUrl;
    private String tweetContent;
    private String matchPlace;
    private List<Map<String,String>> quotedTweets;
    private List<String> narrowMatchUrls;
    private List<String> wideMatchUrls;
    private List<String> missedUrls;
}
