package com.mystory.twitter.controller;

import com.mystory.twitter.Engine.TwitterContentServer;
import com.mystory.twitter.model.FrontTwitterContent;
import com.mystory.twitter.utils.FuncMenu;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
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
        modelAndView.setViewName("getone");
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        return modelAndView;
    }

    @PostMapping("/getOne")
    public ModelAndView postToGetOne(@RequestParam(value = "sname") String screenNames,
                                     @RequestParam(value = "startTime",required = false)Date startTime,
                                     @RequestParam(value = "finishTime",required = false)Date finishTime,
                                     @RequestParam(value = "narrowMatch") Boolean narrowMatch,
                                     ModelAndView modelAndView, HttpSession httpSession){
        List<FrontTwitterContent> frontTwitterContents =twitterContentServer.
                getFrontTwitterContent(screenNames,startTime,finishTime,narrowMatch);
        return modelAndView;
    }

}
