package com.mystory.twitter.interceptor;

import com.mystory.twitter.Engine.Oath;
import com.mystory.twitter.model.OathUser;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OathInceptor extends HandlerInterceptorAdapter {
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        return Oath.isUser((OathUser) request.getSession().getAttribute("user"));
    }
}
