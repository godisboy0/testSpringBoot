package com.mystory.twitter.controller;

import com.google.gson.Gson;
import com.mystory.twitter.Engine.UserInfoManipulator;
import com.mystory.twitter.model.UserInfo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/userInfo")
@Api(description = "插入用户数据")
public class UserInfoController {
    @Autowired
    UserInfoManipulator userInfoManipulator;
    Gson gson = new Gson();

    @PostMapping("/insertOne")
    public String updateUserInfo(@RequestParam(value = "screenName") String screenName,
                                 @RequestParam(value = "keywords") String keyWords,
                                 @RequestParam(value = "startDate")
                                 @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                 @RequestParam(value = "finishDate", required = false)
                                 @DateTimeFormat(pattern = "yyyy-MM-dd") Date finishDate) {
        UserInfo userInfo = new UserInfo();
        userInfo.setKeyWords(gson.toJson(new HashSet<>(Arrays.asList(keyWords.split("[;；]")))));
        userInfo.setStartTime(startDate);
        userInfo.setFinishTime(finishDate);
        return userInfoManipulator.set(userInfo);
    }

    /**
     * 通过一个screenName返回该用户的已设定信息，如为空则返回所有人的信息
     *
     * @param screenName
     * @return
     */
    @GetMapping("/findUser")
    public List<UserInfo> getUserInfo(@RequestParam(value = "screenName") String screenName) {
        return userInfoManipulator.get(screenName);
    }

    @PostMapping("/batchUpdate")
    public String batchUpdateDateAndKeywords(@RequestParam(value = "startDate")
                                             @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                             @RequestParam(value = "finishDate", required = false)
                                             @DateTimeFormat(pattern = "yyyy-MM-dd") Date finishDate,
                                             @RequestParam(value = "Keywords") String keyWords,
                                             @RequestParam(value = "ScreenNames", required = false) String screenNames) {
        return userInfoManipulator.batchSet(screenNames, startDate, finishDate, keyWords);
    }

    @GetMapping("/status")
    public String updateStatus(@ModelAttribute("message") String message) {
        return message;
    }

    @PostMapping("/deleteOne")
    public String deleteOne(@RequestParam("screenName") String screenName,
                            final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", userInfoManipulator.deleteOne(screenName));
        return "redirect:status";
    }

}
