package com.mystory.twitter.repository;

import com.mystory.twitter.model.OathUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OathUserRepo extends CrudRepository<OathUser, String> {
    @Query(value = "select user_name from oath_user",nativeQuery = true)
    List<String> findUserName();
    OathUser findByUserName(String userName);
}
