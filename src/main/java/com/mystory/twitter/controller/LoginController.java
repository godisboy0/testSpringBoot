package com.mystory.twitter.controller;


import com.mystory.twitter.Engine.Oath;
import com.mystory.twitter.model.OathUser;
import com.mystory.twitter.utils.FuncMenu;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

class FuncMenuFactory {
    private static FuncMenu getTwitterContentFunc = new FuncMenu("按推特账户和时间获取已爬取的数据", "/getResult/getOne");
    private static FuncMenu getUserGuideFunc = new FuncMenu("查看用户指南", "/hello/");
    private static List<FuncMenu> generalFuncs = null;
    private static List<FuncMenu> adminFuncs = null;

    public static List<FuncMenu> getGeneralFuncs() {
        if (generalFuncs == null) {
            generalFuncs = new ArrayList<>();
            generalFuncs.add(getTwitterContentFunc);
            //generalFuncs.add(getUserGuideFunc);
        }
        return generalFuncs;
    }

    public static List<FuncMenu> getAdminFuncs() {
        if (adminFuncs == null) {
            if (generalFuncs == null) {
                getGeneralFuncs();
            }
            adminFuncs = new ArrayList<>(generalFuncs);
            adminFuncs.add(getUserGuideFunc);
        }
        return adminFuncs;
    }
}


@RestController
@RequestMapping("/")
@Api(description = "登陆")
@Log4j
public class LoginController {

    @GetMapping("/")
    public ModelAndView getIndex(ModelAndView modelAndView){
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView login(ModelAndView modelAndView) {
        modelAndView.setViewName("login");
        return modelAndView;
    }

    //原来参数的顺序都不能乱，@Valid OathUser oathUser后必须紧跟BindingResult bindingResult
    @PostMapping("/login")
    public ModelAndView login(HttpSession session, ModelAndView modelAndView, @Valid OathUser oathUser, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("error", bindingResult.getFieldError().getDefaultMessage());
            modelAndView.setViewName("login");
            return modelAndView;
        }
        if (Oath.isSuperuser(oathUser)) {
            log.info("超级用户：" + oathUser.getUserName() + "登陆成功");
            oathUser.setIsSuperUser(true);
            session.setAttribute("User", oathUser);
            List<FuncMenu> adminMenu = FuncMenuFactory.getAdminFuncs();
            modelAndView.addObject("functions", adminMenu);
            modelAndView.setViewName("func");
            return modelAndView;
        } else if (Oath.isUser(oathUser)) {
            log.info("普通用户：" + oathUser.getUserName() + "登陆成功");
            oathUser.setIsSuperUser(false);
            session.setAttribute("User", oathUser);
            List<FuncMenu> generalMenu = FuncMenuFactory.getGeneralFuncs();
            modelAndView.addObject("functions", generalMenu);
            modelAndView.setViewName("func");
            return modelAndView;
        } else {
            modelAndView.setViewName("login");
            return modelAndView;
        }
    }

}
