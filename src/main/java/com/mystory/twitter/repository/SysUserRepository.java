package com.mystory.twitter.repository;

import com.mystory.twitter.model.SysUser;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SysUserRepository extends CrudRepository<SysUser,String> {
    List<SysUser> findAll();
    SysUser findByUsername(String username);
}
