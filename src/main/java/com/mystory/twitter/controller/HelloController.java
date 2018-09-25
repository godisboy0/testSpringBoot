package com.mystory.twitter.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
@Api(description = "使用说明")
public class HelloController {

    @GetMapping("/")
    public String sayHello() {
        return "Hello Twitter";
    }
}
