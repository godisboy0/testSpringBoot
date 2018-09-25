package com.mystory.twitter.Engine;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.mystory.twitter.model.UserInfo;
import com.mystory.twitter.repository.UserInfoRepo;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserInfoManipulator {
    @Autowired
    UserInfoRepo userInfoRepo;
    private Gson gson = new Gson();

    private final String Description = "设置用户名（例如https://twitter.com/realDonaldTrump中的\"realDonaldTrump\"）、" +
            "需要筛选的关键词(每个一行)、需要筛选的时间段等";

    public String set(UserInfo userInfo) {
        if (isNullorDeepEmpty(gson.fromJson(userInfo.getKeyWords(), ArrayList.class))) {
            return "keyword不能为空值";
        }
        try {
            UserInfo oldUserInfo = userInfoRepo.findByScreenName(userInfo.getScreenName());
            if (oldUserInfo == null) {
                userInfo.setKeywordChanged(true);
                userInfo.setLastGotID(0L);
                userInfo.setFirstGotID(Long.MAX_VALUE);
            } else if (!oldUserInfo.getKeyWords().equals(userInfo.getKeyWords())) {
                userInfo.setKeywordChanged(true);
            } else userInfo.setKeywordChanged(false);
            userInfoRepo.save(userInfo);
            return "已创建或更新用户";
        } catch (Exception e) {
            return "创建或更新用户失败";
        }
    }

    public List<UserInfo> get(String screenName) {
        List<UserInfo> userInfos = new ArrayList<>();
        if (!Strings.isNullOrEmpty(screenName)) {
            userInfos.add(userInfoRepo.findByScreenName(screenName));
        } else {
            userInfos.addAll(userInfoRepo.findAll());
        }
        return userInfos;
    }

    private Boolean isNullorDeepEmpty(List<String> list) {
        if (list == null || list.isEmpty())
            return true;
        for (String element : list) {
            if (!Strings.isNullOrEmpty(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 为指定用户批量设置起始时间、结束时间和关键词.
     *
     * @param usersString    指定的用户，如为null，则更新数据库中所有数据，如果有名字，则只更新那些名字的数据库,如提供的名字不存在，则直接创建这个用户
     * @param keyWordsString 以分号隔开
     * @return 相关指示信息
     */
    public String batchSet(String usersString, Date startDate, Date finishDate, String keyWordsString) {

        List<String> users = new ArrayList<String>(Arrays.asList(usersString.split("[;；]")));
        List<String> keyWords = new ArrayList<String>(Arrays.asList(keyWordsString.split("[;；]")));

        if (isNullorDeepEmpty(keyWords)) {
            return "过滤关键词不能为空";
        }
        while (keyWords.remove("")) ;
        for (String screenName : users) {
            if (Strings.isNullOrEmpty(screenName)) {
                continue;
            }
            UserInfo singleUser = userInfoRepo.findByScreenName(screenName);
            if (singleUser == null) {
                singleUser.setScreenName(screenName);
                set(singleUser);
            }
        }
        List<UserInfo> userInfos = userInfoRepo.findAll();
        for (val userInfo : userInfos) {
            userInfo.setStartTime(startDate);
            userInfo.setFinishTime(finishDate);
            userInfo.setKeyWords(gson.toJson(keyWords));
        }
        userInfoRepo.save(userInfos);
        return "已为" + userInfos.size() + "名用户更新起始时间为：" + startDate.toString() +
                ";\n更新结束时间为：" + finishDate.toString() + ";\n更新监控关键词为：" + keyWords;
    }

    public String deleteOne(String screenName) {
        UserInfo userInfo = userInfoRepo.findByScreenName(screenName);
        if (userInfo == null) {
            return "未设置此推特账户";
        } else {
            userInfoRepo.delete(screenName);
            return "已删除推特账户>>>" + screenName + "<<<该用户的数据将不会继续爬取，已爬取的数据仍可获得";
        }
    }
}
