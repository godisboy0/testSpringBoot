package com.mystory.twitter.controller;

import com.mystory.twitter.model.OathUser;
import com.mystory.twitter.model.SysUser;
import com.mystory.twitter.utils.FuncMenu;
import com.mystory.twitter.utils.FuncMenuFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/func")
public class FuncMenuController {

    List<FuncMenu> userMenu  = FuncMenuFactory.getGeneralFuncs();
    List<FuncMenu> adminMenu = FuncMenuFactory.getAdminFuncs();

    private boolean isAdmin(SysUser sysUser){
        for(OathUser user:sysUser.getRoles()){
            if (user.getRole().equals("admin"))
                return true;
        }
        return false;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin','user')")
    public ModelAndView showFunc(ModelAndView modelAndView, HttpSession session) {
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication auth = ctx.getAuthentication();
        SysUser user = (SysUser) auth.getPrincipal();
        List<FuncMenu> funcs;
        if (isAdmin(user)){
            funcs = adminMenu;
        }else{
            funcs = userMenu;
        }
        modelAndView.addObject("functions", funcs);
        modelAndView.setViewName("func");
        return modelAndView;
    }

}
