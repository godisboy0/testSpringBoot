package com.mystory.twitter.model;

public interface MatchPlace {
    public static Integer originTweet = 1;
    public static Integer originURL = 2;
    public static Integer quotedTweet = 4;
    public static Integer quotedUrl = 8;
    public static Integer mayMissed = 16;
    public static Integer narrowMatched = 32;
}
