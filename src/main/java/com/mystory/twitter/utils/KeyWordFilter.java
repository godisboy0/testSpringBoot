package com.mystory.twitter.utils;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.*;

public class KeyWordFilter {

    private final Set<String> keywords;
    @Getter
    private Set<String> matchedKeywords = new HashSet<>();

    public KeyWordFilter(Set<String> keywords) {
        this.keywords = keywords;
    }

    public KeyWordFilter reset() {
        matchedKeywords.clear();
        return this;
    }

    public Boolean matched(String content) {
        boolean flag = false;
        for (String keyword : keywords) {
            //此处应该对keywords的内容进行限制和扩展，比如.应该扩展为\.(在java中为\\.)
            String prefix, suffix;
            if (keyword.startsWith("?") || keyword.startsWith("？")) {
                prefix = ".*";
                suffix = ".*";
                keyword = keyword.replace("?", "");
                keyword = keyword.replace("？", "");
            } else {
                prefix = ".*\\b";
                suffix = "\\b.*";
            }
            if(keyword.isEmpty()){
                continue;
            }
            List<String> tmps = Arrays.asList(keyword.split("\\."));
            for (String tmp : tmps) {
                keyword = tmp + "\\.";
            }
            Pattern p = Pattern.compile(prefix + keyword + suffix, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(content);
            if (m.matches()) {
                matchedKeywords.add(keyword);
                flag = true;
            }
        }
        return flag;
    }

}
