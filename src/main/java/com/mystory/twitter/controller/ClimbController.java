package com.mystory.twitter.controller;

import com.mystory.twitter.Engine.ClimbTwitter;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/climb")
@Api(description = "正式开始爬取，只有一个用户有权限更新")
public class ClimbController {
    @Autowired
    ClimbTwitter climbTwitter;

    /**
     * 爬取所有用户的所有设定信息。如果关键词没有修改，是自动增量爬取的，已爬取过的信息不会再次爬取。
     * @return
     */
    @GetMapping("/batchProcess")
    public String batchProcess(){
        return climbTwitter.analysis(null);
    }

    @GetMapping("/singleUser")
    public String singleUser(@RequestParam(value = "screenName") String screenName){
        return climbTwitter.analysis(screenName);
    }

}
