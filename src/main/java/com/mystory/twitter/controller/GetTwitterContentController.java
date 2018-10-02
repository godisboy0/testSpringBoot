package com.mystory.twitter.controller;


import com.mystory.twitter.Engine.TwitterContentServer;
import com.mystory.twitter.model.FrontTwitterContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/func")
public class GetTwitterContentController {
    @Autowired
    private TwitterContentServer twitterContentServer;

    @GetMapping("/getOne")
    public ModelAndView getOne(ModelAndView modelAndView, HttpSession session) {
        modelAndView.setViewName("getOne");
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        return modelAndView;
    }

    @PostMapping("/getOne")
    public ModelAndView postToGetOne(@RequestParam(value = "sname") String screenNames,
                                     @RequestParam(value = "startTime", required = false)
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
                                     @RequestParam(value = "finishTime", required = false)
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date finishTime,
                                     @RequestParam(value = "narrowMatch") Boolean narrowMatch,
                                     ModelAndView modelAndView, HttpSession httpSession) {
        if (finishTime == null) {
            finishTime = new Date(2018, 0, 1);
        }
        if (startTime == null) {
            startTime = new Date(0, 0, 1);
        }
        if (startTime.compareTo(finishTime) >= 0) {
            modelAndView.addObject("error", "哥，仔细点……你这开始日期都比结束日期还晚了");
        } else {
            List<FrontTwitterContent> frontTwitterContents = twitterContentServer.
                    getFrontTwitterContent(screenNames, startTime, finishTime, narrowMatch);
            modelAndView.addObject("twitterContents", frontTwitterContents);
            modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        }
        modelAndView.setViewName("getOne");
        return modelAndView;
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
