package com.mystory.twitter.controller;

import io.swagger.annotations.Api;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/hello")
@Api(description = "使用说明")
public class HelloController {

    @GetMapping("/doc")
    @PreAuthorize("hasRole('admin') or hasRole('user')")
    public ModelAndView sayHello(ModelAndView modelAndView) {
        modelAndView.setViewName("guide");
        return modelAndView;
    }
}
