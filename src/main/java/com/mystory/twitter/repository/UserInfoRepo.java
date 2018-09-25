package com.mystory.twitter.repository;

import com.mystory.twitter.model.UserInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface UserInfoRepo extends CrudRepository<UserInfo,String> {
    @Override
    ArrayList<UserInfo> findAll();
    UserInfo findByScreenName(String screenName);
}
