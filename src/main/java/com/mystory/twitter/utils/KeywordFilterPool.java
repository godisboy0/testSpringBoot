package com.mystory.twitter.utils;

import java.util.HashMap;
import java.util.Set;

public class KeywordFilterPool {
    private static HashMap<Set<String>,KeyWordFilter> cachedFilter = new HashMap<>();
    public static KeyWordFilter getFilter(Set<String> keywords){
        if (cachedFilter.get(keywords) == null){
            cachedFilter.put(keywords, new KeyWordFilter(keywords));
        }
        return cachedFilter.get(keywords).reset();
    }
}
