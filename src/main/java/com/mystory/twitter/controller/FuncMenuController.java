package com.mystory.twitter.controller;

import com.mystory.twitter.utils.FuncMenu;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/func")
public class FuncMenuController {

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

}
