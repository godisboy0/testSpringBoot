package com.mystory.twitter.controller;

import com.mystory.twitter.Engine.ClimbTwitter;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/func")
@Api(description = "正式开始爬取，只有一个用户有权限更新")
public class ClimbController {
    @Autowired
    ClimbTwitter climbTwitter;

    /**
     * 爬取所有用户的所有设定信息。如果关键词没有修改，是自动增量爬取的，已爬取过的信息不会再次爬取。
     * @return
     */
    @GetMapping("/fetchNow")
    public ModelAndView fetchNowPage(ModelAndView modelAndView){
        modelAndView.setViewName("fetchNow");
        return modelAndView;
    }

    @PostMapping("/fetchNow")
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
