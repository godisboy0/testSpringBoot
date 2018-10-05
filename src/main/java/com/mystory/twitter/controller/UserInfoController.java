package com.mystory.twitter.controller;

import com.mystory.twitter.Engine.TwitterContentServer;
import com.mystory.twitter.Engine.UserInfoManipulator;
import com.mystory.twitter.model.UserInfo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/root")
@Api(description = "插入用户数据")
public class UserInfoController {
    @Autowired
    private UserInfoManipulator userInfoManipulator;
    @Autowired
    private TwitterContentServer twitterContentServer;

    @GetMapping("/insertUser")
    @PreAuthorize("hasRole('admin')")
    public ModelAndView getInsertOnePage(ModelAndView modelAndView) {
        modelAndView.setViewName("insertUserInfo");
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        return modelAndView;
    }

    @PostMapping("/insertUser")
    @PreAuthorize("hasRole('admin')")
    public ModelAndView batchUpdateDateAndKeywords(@RequestParam(value = "sname") String screenNames,
                                                   @RequestParam(value = "keywords") String keyWords,
                                                   @RequestParam(value = "startTime")
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                   @RequestParam(value = "finishTime", required = false)
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd") Date finishDate,
                                                   ModelAndView modelAndView) {
        modelAndView.setViewName("insertUserInfo");
        modelAndView.addObject("insertStatus", userInfoManipulator.batchSet(screenNames, startDate, finishDate, keyWords));
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        return modelAndView;
    }

    @GetMapping("/deleteUser")
    @PreAuthorize("hasRole('admin')")
    public ModelAndView deleteOnePage(ModelAndView modelAndView) {
        modelAndView.setViewName("deleteUserInfo");
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        return modelAndView;
    }

    @PostMapping("/deleteUser")
    @PreAuthorize("hasRole('admin')")
    public ModelAndView deleteOne(@RequestParam(value = "sname") String screenNames, ModelAndView modelAndView) {

        modelAndView.setViewName("deleteUserInfo");
        modelAndView.addObject("deleteStatus", userInfoManipulator.batchDelete(screenNames));
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        return modelAndView;
    }

    @GetMapping("/findUserInfo")
    @PreAuthorize("hasRole('admin')")
    public ModelAndView getUserInfoPage(ModelAndView modelAndView) {
        modelAndView.setViewName("findUserInfo");
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        return modelAndView;
    }

    @PostMapping("/findUserInfo")
    @PreAuthorize("hasRole('admin')")
    public ModelAndView getUserInfo(@RequestParam(value = "sname") String screenNames,ModelAndView modelAndView){
        modelAndView.setViewName("findUserInfo");
        modelAndView.addObject("userInfos",userInfoManipulator.get(screenNames));
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        return modelAndView;
    }

}
