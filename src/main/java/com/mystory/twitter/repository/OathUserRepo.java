package com.mystory.twitter.repository;

import com.mystory.twitter.model.OathUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OathUserRepo extends CrudRepository<OathUser, String> {
}
