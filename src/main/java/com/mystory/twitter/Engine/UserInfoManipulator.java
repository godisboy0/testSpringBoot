package com.mystory.twitter.Engine;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.mystory.twitter.model.UserInfo;
import com.mystory.twitter.repository.UserInfoRepo;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Log4j
@Component
public class UserInfoManipulator {
    @Autowired
    UserInfoRepo userInfoRepo;
    private Gson gson = new Gson();

    private final List<String> errorMessages = Arrays.asList("screenName或keywords不能为空",
        "结束日期比开始日期还早？", "未知原因创建用户失败，maybe数据库挂了", "开始时间为空或格式不对，标准格式为2018-9-20",
        "要删除的用户不存在");
    private final List<String> successMessages = Arrays.asList("已创建或更新用户", "已删除用户");

    private String set(String screenName, String keyWords, Date startDate, Date finishDate) {
        if (Strings.isNullOrEmpty(keyWords)) {
            return errorMessages.get(0);
        }
        List<String> keyWordList = Arrays.asList(keyWords.split("[;；]"));
        keyWordList.stream().map(String::trim).filter(s -> !Strings.isNullOrEmpty(s)).distinct().collect(Collectors.toList());
        if (keyWordList.isEmpty()) {
            return errorMessages.get(0);
        }
        finishDate = finishDate == null ? new Date(2018, 0, 1) : finishDate;
        if (startDate == null) {
            return errorMessages.get(3);
        }
        if (startDate.compareTo(finishDate) >= 0) {
            return errorMessages.get(1);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setScreenName(screenName);
        userInfo.setKeyWords(gson.toJson(new HashSet<>(keyWordList)));
        userInfo.setStartTime(startDate);
        userInfo.setFinishTime(finishDate);
        try {
            UserInfo oldUserInfo = userInfoRepo.findByScreenName(userInfo.getScreenName());
            if (oldUserInfo == null) {
                userInfo.setKeywordChanged(true);
                userInfo.setStartTimeChanged(true);
                userInfo.setLastGotID(0L);
                userInfo.setFirstGotID(Long.MAX_VALUE);
                userInfo.setLastFetchTime(new Date(2018 - 1990, 2, 2));
            } else if (!oldUserInfo.getKeyWords().equals(userInfo.getKeyWords()) || !oldUserInfo.getStartTime().equals(userInfo.getStartTime())) {
                userInfo.setStartTimeChanged(true);
                userInfo.setKeywordChanged(true);
                userInfo.setLastGotID(0L);
                userInfo.setFirstGotID(Long.MAX_VALUE);
                userInfo.setLastFetchTime(new Date(2018 - 1990, 2, 2));
            }
            userInfoRepo.save(userInfo);
            return successMessages.get(0);
        } catch (Exception e) {
            log.error(e.getMessage());
            return errorMessages.get(2);
        }
    }

    public List<UserInfo> get(String screenNames) {
        List<UserInfo> userInfos = new ArrayList<>();
        if (!Strings.isNullOrEmpty(screenNames)) {
            List<String> screenNameList = new ArrayList<String>(Arrays.asList(screenNames.split("[;；]")));
            screenNameList = screenNameList.stream().map(String::trim).filter(s -> !Strings.isNullOrEmpty(s)).distinct().collect(Collectors.toList());
            for (String screenName : screenNameList) {
                UserInfo userInfo = userInfoRepo.findByScreenName(screenName);
                if (userInfo != null)
                    userInfos.add(userInfo);
            }
        } else {
            userInfos.addAll(userInfoRepo.findAll());
        }
        return userInfos;
    }

    /**
     * 为指定用户批量设置起始时间、结束时间和关键词.
     *
     * @param usersString    指定的用户，如为null，则更新数据库中所有数据，如果有名字，则只更新那些名字的数据库,如提供的名字不存在，则直接创建这个用户
     * @param keyWordsString 以分号隔开
     * @return 相关指示信息
     */
    public String batchSet(String usersString, Date startDate, Date finishDate, String keyWordsString) {

        log.info("插入用户数据for " + usersString);
        List<String> users = new ArrayList<>(Arrays.asList(usersString.split("[;；]")));
        users = users.stream().map(String::trim).filter(s -> !Strings.isNullOrEmpty(s)).filter(s -> !s.contains(" ")).distinct().collect(Collectors.toList());
        List<String> failNames = new ArrayList<>();
        List<String> successNames = new ArrayList<>();

        for (String screenName : users) {
            if (Strings.isNullOrEmpty(screenName))
                continue;
            if (!set(screenName, keyWordsString, startDate, finishDate).equals(successMessages.get(0))) {
                failNames.add(screenName);
            } else {
                successNames.add(screenName);
            }
        }
        StringBuilder stringBuilder = new StringBuilder("成功插入或更新以下用户：\n");
        for (String name : successNames) {
            stringBuilder.append(name).append("\n");
        }
        stringBuilder.append("以下用户信息更新失败：\n");
        for (String name : failNames) {
            stringBuilder.append(name).append("\n");
        }
        return stringBuilder.toString();
    }

    private String deleteOne(String screenName) {
        UserInfo userInfo = userInfoRepo.findByScreenName(screenName);
        if (userInfo == null) {
            return errorMessages.get(4);
        } else {
            userInfoRepo.delete(screenName);
            return successMessages.get(1);
        }
    }

    public String batchDelete(String screenNames) {
        List<String> successNames = new ArrayList<>();
        List<String> failNames = new ArrayList<>();
        for (String screenName : new HashSet<>(Arrays.asList(screenNames.split("[;；]")))) {
            if (deleteOne(screenName).equals(successMessages.get(1))) {
                successNames.add(screenName);
            } else {
                failNames.add(screenName);
            }
        }
        StringBuilder stringBuilder = new StringBuilder("成功删除以下用户：\n");
        for (String name : successNames) {
            stringBuilder.append(name).append("\n");
        }
        stringBuilder.append("以下用户删除失败：\n");
        for (String name : failNames) {
            stringBuilder.append(name).append("\n");
        }
        return stringBuilder.toString();
    }

}
