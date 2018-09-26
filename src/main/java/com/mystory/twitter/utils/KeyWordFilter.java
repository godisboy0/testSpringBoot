package com.mystory.twitter.utils;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.*;

public class KeyWordFilter {

    private final Set<String> keywords;
    @Getter
    private Set<String> matchedKeywords = new HashSet<>();

    public KeyWordFilter(Set<String> keywords){
        this.keywords = keywords;
    }

    public KeyWordFilter reset(){
        matchedKeywords.clear();
        return this;
    }

    public Boolean matched(String content) {
        boolean flag = false;
        for (String keyword : keywords) {
            Pattern p = Pattern.compile(".*" + keyword + ".*",Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(content);
            if (m.matches()){
                matchedKeywords.add(keyword);
                flag = true;
            }
        }
        return flag;
    }

}
