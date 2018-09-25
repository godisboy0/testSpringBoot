package com.mystory.twitter.controller;

import com.mystory.twitter.Engine.Oath;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/User")
@Api(description = "新增系统用户")
public class AddOathUserController {
    @Autowired
    Oath oath;

    @GetMapping("/status")
    public String afterUpdate(@ModelAttribute("message") String message) {
        return message; //可以以后修改为一个带返回窗口的页面
    }

    @PostMapping("/new")
    public String addUser(@RequestParam("userName") String userName,
                          @RequestParam("password") String password,
                          final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", oath.addUser(userName, password));
        return "redirect:/User/status";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam("userName") String userName,
                             final RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", oath.deleteUser(userName));
        return "redirect:/User/status";
    }
}
