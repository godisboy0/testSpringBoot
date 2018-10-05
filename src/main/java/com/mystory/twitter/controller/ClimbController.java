package com.mystory.twitter.controller;

import com.mystory.twitter.Engine.ClimbTwitter;
import com.mystory.twitter.Engine.TwitterContentServer;
import com.mystory.twitter.Engine.UserInfoManipulator;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/root")
@Api(description = "正式开始爬取，只有一个用户有权限更新")
public class ClimbController {
    @Autowired
    ClimbTwitter climbTwitter;
    @Autowired
    private TwitterContentServer twitterContentServer;

    /**
     * 爬取所有用户的所有设定信息。如果关键词没有修改，是自动增量爬取的，已爬取过的信息不会再次爬取。
     * @return
     */
    @GetMapping("/fetchNow")
    @PreAuthorize("hasRole('admin')")
    public ModelAndView fetchNowPage(ModelAndView modelAndView){
        modelAndView.addObject("screenNames",twitterContentServer.getAllScreenNames());
        modelAndView.setViewName("fetchNow");
        return modelAndView;
    }

    @PostMapping("/fetchNow")
    @PreAuthorize("hasRole('admin')")
    public ModelAndView FetchNow(ModelAndView modelAndView){
        climbTwitter.analysis(null);
        modelAndView.addObject("succeed",climbTwitter.getSucceed());
        modelAndView.addObject("failed",climbTwitter.getFailed());
        modelAndView.addObject("nearlyUpdated",climbTwitter.getNearlyFetchedUser());
        modelAndView.addObject("fetched",true);
        modelAndView.setViewName("fetchNow");
        return modelAndView;
    }

}
