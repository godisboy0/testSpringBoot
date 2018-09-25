package com.mystory.twitter.controller;


import com.mystory.twitter.model.TwitterContent;
import com.mystory.twitter.repository.TwitterContentRepo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/getResult")
@Api(description = "查找结果")
public class GetTwitterContentController {
    @Autowired
    TwitterContentRepo twitterContentRepo;

    @GetMapping("/getOne")
    public List<TwitterContent> getByScreenName(@RequestParam(value = "screenName") String screenName,
                                                @RequestParam(value = "startDate", required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                @RequestParam(value = "finishDate", required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd") Date finishDate) {
        startDate = startDate == null ? new Date(2008 - 1900, 0, 1) : startDate;
        finishDate = finishDate == null ? new Date(2018, 0, 1) : finishDate;
        List<TwitterContent> twitterContents = twitterContentRepo.
                findByScreenNameAndIsQuotedAndTweetTimeGreaterThanAndTweetTimeLessThan(screenName, false, startDate, finishDate);
        return twitterContents;
    }

//    /**
//     * 返回所有已收集到的结果，这样做无逻辑，暂不支持。
//     * @return
//     */
//    @GetMapping("/getAll")
//    public List<TwitterContent> getAll(){
//        return twitterContentRepo.findAll();
//    }

}
