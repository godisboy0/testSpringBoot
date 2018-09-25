package com.mystory.twitter.utils;

import com.mystory.twitter.Engine.ClimbTwitter;
import com.mystory.twitter.Engine.UserInfoManipulator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineFactory {
    @Value("${blocked}")
    private Boolean blocked;
//    @Bean
//    public ClimbTwitter getClimbTwitter() {
//        ClimbTwitter climbTwitter = new ClimbTwitter();
//        return climbTwitter;
//    }

//    @Bean
//    public UserInfoManipulator getSetUserInfo() {
//        return new UserInfoManipulator();
//    }
}
