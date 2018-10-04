package com.mystory.twitter.utils;

import lombok.Getter;

import java.util.*;
import java.util.regex.*;

public class KeyWordFilter {

    private final Set<String> keywords;
    @Getter
    private Set<String> matchedKeywords = new HashSet<>();
    private Map<String, String> keyword2Pattern = new HashMap<>();

    public KeyWordFilter(Set<String> keywords) {
        this.keywords = keywords;
    }

    public KeyWordFilter reset() {
        matchedKeywords.clear();
        return this;
    }

    private String getPattern(String keyword) {
        if (keyword2Pattern.get(keyword) == null) {
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
            List<String> tmps = Arrays.asList(keyword.split("\\."));
            String pattern = new String();
            for (int i = 0; i != tmps.size() - 1; ++i) {
                pattern += tmps.get(i) + "\\.";
            }
            pattern += tmps.get(tmps.size() - 1);
            keyword2Pattern.put(keyword, prefix + pattern + suffix);
        }
        return keyword2Pattern.get(keyword);

    }

    public Boolean matched(String content) {
        boolean flag = false;
        for (String keyword : keywords) {
            //此处应该对keywords的内容进行限制和扩展，比如.应该扩展为\.(在java中为\\.)，通过下面的函数完成
            String pattern = getPattern(keyword);
            if (pattern.isEmpty()) {
                continue;
            }
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(content);
            if (m.matches()) {
                matchedKeywords.add(keyword);
                flag = true;
            }
        }
        return flag;
    }

}
