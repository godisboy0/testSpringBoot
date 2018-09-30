package com.mystory.twitter.controller;

import com.google.gson.Gson;
import com.mystory.twitter.Engine.Oath;
import com.mystory.twitter.model.OathUser;
import com.mystory.twitter.utils.FuncMenu;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

class FuncMenuFactory {
    private static FuncMenu getTwitterContentFunc = new FuncMenu("按推特账户和时间获取已爬取的数据", "/func/getOne");
    private static FuncMenu getUserGuideFunc = new FuncMenu("查看用户指南", "/hello/doc");
    private static FuncMenu lookUpUserInfoFunc = new FuncMenu("查看已经设置的爬取账号信息", "/func/insert");
    private static FuncMenu batchSetUserInfoFunc = new FuncMenu("批量设置需要爬取的推特账号", "/func/insertUser");
    private static FuncMenu deleteUserInfoFunc = new FuncMenu("批量删除需要爬取的推特账号", "/func/deleteUser");
    private static FuncMenu errorReport = new FuncMenu("报告发现的行为异常", "/errorReport");
    private static FuncMenu addOathUser = new FuncMenu("新添加用户", "/addUser/new");
    private static FuncMenu deleteOathUser = new FuncMenu("删除用户", "/addUser/delete");
    private static List<FuncMenu> generalFuncs = null;
    private static List<FuncMenu> adminFuncs = null;

    public static List<FuncMenu> getGeneralFuncs() {
        if (generalFuncs == null) {
            generalFuncs = new ArrayList<>();
            generalFuncs.add(getTwitterContentFunc);
            generalFuncs.add(lookUpUserInfoFunc);
            generalFuncs.add(getUserGuideFunc);
            generalFuncs.add(errorReport);
        }
        return generalFuncs;
    }

    public static List<FuncMenu> getAdminFuncs() {
        if (adminFuncs == null) {
            if (generalFuncs == null) {
                getGeneralFuncs();
            }
            adminFuncs = new ArrayList<>(generalFuncs);
            adminFuncs.add(batchSetUserInfoFunc);
            adminFuncs.add(deleteUserInfoFunc);
            adminFuncs.add(addOathUser);
            adminFuncs.add(deleteOathUser);
        }
        return adminFuncs;
    }
}


@RestController
@RequestMapping("/")
@Api(description = "登陆")
@Log4j
public class LoginController {
    @Autowired
    Oath oath;

    @GetMapping
    public ModelAndView getIndex(ModelAndView modelAndView) {
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @GetMapping("login")
    public ModelAndView login(ModelAndView modelAndView, HttpSession session) {
        modelAndView.setViewName("login");
        return modelAndView;
    }

    //原来参数的顺序都不能乱，@Valid OathUser oathUser后必须紧跟BindingResult bindingResult
    @PostMapping("login")
    public ModelAndView login(HttpSession session, ModelAndView modelAndView, @Valid OathUser oathUser,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("error", bindingResult.getFieldError().getDefaultMessage());
            modelAndView.setViewName("login");
            return modelAndView;
        }
        if (oath.isSuperuser(oathUser)) {
            log.info("超级用户：" + oathUser.getUserName() + "登陆成功");
            oathUser.setIsSuperUser(true);
            session.setAttribute("User", oathUser);
            List<FuncMenu> adminMenu = FuncMenuFactory.getAdminFuncs();
            modelAndView.setViewName("redirect:/func");
            session.setAttribute("functions", "adminMenu");
            return modelAndView;
        } else if (oath.isUser(oathUser)) {
            log.info("普通用户：" + oathUser.getUserName() + "登陆成功");
            oathUser.setIsSuperUser(false);
            session.setAttribute("User", oathUser);
            List<FuncMenu> generalMenu = FuncMenuFactory.getGeneralFuncs();
            modelAndView.setViewName("redirect:/func");
            session.setAttribute("functions", "generalMenu");
            return modelAndView;
        } else {
            modelAndView.addObject("error", "用户名或密码错误");
            modelAndView.setViewName("login");
            return modelAndView;
        }
    }

}
