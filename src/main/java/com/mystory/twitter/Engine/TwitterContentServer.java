package com.mystory.twitter.Engine;

import com.mystory.twitter.model.FrontTwitterContent;
import com.mystory.twitter.model.UserInfo;
import com.mystory.twitter.repository.UserInfoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TwitterContentServer {
    @Autowired
    UserInfoRepo userInfoRepo;

    private Set<String> ret = null;

    public Set<String> getAllScreenNames() {
        ret = new HashSet<>();
        for (UserInfo userInfo : userInfoRepo.findAll()) {
            ret.add(userInfo.getScreenName());
        }
        return ret;
    }

    public List<FrontTwitterContent> getFrontTwitterContent(String screenNames, Date startTime, Date finishTime, Boolean narrowMatch) {
        List<FrontTwitterContent> ret = new ArrayList<>();
        //todo


        return ret;
    }

}
