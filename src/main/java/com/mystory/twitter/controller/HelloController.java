package com.mystory.twitter.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
@Api(description = "使用说明")
public class HelloController {

    final String guide = "<p>1.其实没什么好介绍的，功能真的很简单啊，普通用户只能读取已经爬好的功能，而管理员用户可以设置需要爬取的账号、时间段，以及新增和删除" +
            "用户</p><p>2.遇到任何和预想的不一致的行为，都可以通过报告发现的行为异常这个页面来报告，说清楚到底在什么页面发生了什么问题。我有空会来看和修复的，如果很" +
            "着急……那也没办法啊。</p>";

    @GetMapping("/doc")
    public String sayHello() {
        return guide;
    }
}
