package com.mystory.twitter.controller;

import com.mystory.twitter.Engine.TwitterContentServer;
import com.mystory.twitter.model.FrontTwitterContent;
import com.mystory.twitter.utils.FuncMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/func")
public class FuncMenuController {
    @Autowired
    private TwitterContentServer twitterContentServer;

    @GetMapping
    public ModelAndView showFunc(ModelAndView modelAndView, HttpSession session) {
        if (session.getAttribute("functions") == null) {
            return new ModelAndView("/error");
        }
        String funcString = (String) session.getAttribute("functions");
        List<FuncMenu> funcs;
        if (funcString.equals("adminMenu")) {
            funcs = FuncMenuFactory.getAdminFuncs();
        } else if (funcString.equals("generalMenu")) {
            funcs = FuncMenuFactory.getAdminFuncs();
        } else {
            return new ModelAndView("/error");
        }
        modelAndView.addObject("functions", funcs);
        modelAndView.setViewName("func");
        return modelAndView;
    }

    @GetMapping("/getOne")
    public ModelAndView getOne(ModelAndView modelAndView, HttpSession session) {
        modelAndView.setViewName("/getOne");
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
        List<FrontTwitterContent> frontTwitterContents = twitterContentServer.
                getFrontTwitterContent(screenNames, startTime, finishTime, narrowMatch);
        modelAndView.addObject("twitterContents",frontTwitterContents);
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        modelAndView.setViewName("/getOne");
        return modelAndView;
    }

}
