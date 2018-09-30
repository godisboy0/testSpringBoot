package com.mystory.twitter.controller;

import com.mystory.twitter.Engine.TwitterContentServer;
import com.mystory.twitter.Engine.UserInfoManipulator;
import com.mystory.twitter.model.UserInfo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/func")
@Api(description = "插入用户数据")
public class UserInfoController {
    @Autowired
    UserInfoManipulator userInfoManipulator;
    @Autowired
    private TwitterContentServer twitterContentServer;

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

    @GetMapping("/insertUser")
    public ModelAndView getInsertOnePage(ModelAndView modelAndView) {
        modelAndView.setViewName("insertUserInfo");
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        return modelAndView;
    }

    @PostMapping("/insertUser")
    public ModelAndView batchUpdateDateAndKeywords(@RequestParam(value = "sname") String screenNames,
                                                   @RequestParam(value = "keywords", required = false) String keyWords,
                                                   @RequestParam(value = "startTime", required = false)
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                   @RequestParam(value = "finishTime", required = false)
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd") Date finishDate,
                                                   ModelAndView modelAndView) {
        modelAndView.setViewName("insertUserInfo");
        modelAndView.addObject("insertStatus", userInfoManipulator.batchSet(screenNames, startDate, finishDate, keyWords));
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        return modelAndView;
    }

    @GetMapping("/status")
    public String updateStatus(@ModelAttribute("message") String message) {
        return message;
    }

    @GetMapping("/deleteUser")
    public ModelAndView deleteOnePage(ModelAndView modelAndView) {
        modelAndView.setViewName("deleteUserInfo");
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        return modelAndView;
    }

    @PostMapping("/deleteUser")
    public ModelAndView deleteOne(@RequestParam(value = "sname") String screenNames, ModelAndView modelAndView) {

        modelAndView.setViewName("deleteUserInfo");
        modelAndView.addObject("deleteStatus", userInfoManipulator.batchDelete(screenNames));
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        return modelAndView;
    }

}
