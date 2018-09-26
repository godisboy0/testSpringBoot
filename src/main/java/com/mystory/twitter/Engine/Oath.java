package com.mystory.twitter.Engine;

import com.google.common.base.Strings;
import com.mystory.twitter.model.OathUser;
import com.mystory.twitter.repository.OathUserRepo;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Component
@Log4j
public class Oath {
    @Autowired
    OathUserRepo oathUserRepo;

    public Boolean isSuperuser(OathUser user) {
        OathUser oathUser = oathUserRepo.findByUserName(user.getUserName());
        if (oathUser == null) {
            return false;
        } else if (encryPassword(user.getPassword()).equals(oathUser.getPassword())  && oathUser.getIsSuperUser()) {
            return true;
        } else return false;
    }

    public Boolean isUser(OathUser user) {
        OathUser oathUser = oathUserRepo.findByUserName(user.getUserName());
        if (oathUser == null) {
            return false;
        } else if (encryPassword(user.getPassword()).equals(oathUser.getPassword())) {
            return true;
        } else return false;
    }

    private String encryPassword(String password) {
        String encryedPassword = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            byte[] inputData = password.getBytes();
            messageDigest.update(inputData);
            encryedPassword = new BigInteger(messageDigest.digest()).toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("用户密码加密失败");
        }
        return encryedPassword;
    }

    public String addUser(String userName, String password) {
        List<String> existedUsers = oathUserRepo.findUserName();
        if (existedUsers.contains(userName)) {
            OathUser oathUser = oathUserRepo.findByUserName(userName);
            String encryedPassword = encryPassword(password);
            if (Strings.isNullOrEmpty(encryedPassword)) {
                return "为用户>>>" + userName + "<<<修改密码失败";
            }
            oathUser.setPassword(encryedPassword);
            oathUserRepo.save(oathUser);
            log.info("用户>>>" + userName + "<<<的密码已经被修改为>>>" + password + "<<<");
            return "用户>>>" + userName + "<<<的密码已经被修改为>>>" + password + "<<<";
        } else {
            OathUser oathUser = new OathUser();
            oathUser.setUserName(userName);
            String encryedPassword = encryPassword(password);
            if (Strings.isNullOrEmpty(encryedPassword)) {
                return "添加用户>>>" + userName + "<<<失败";
            }
            oathUser.setPassword(encryedPassword);
            oathUserRepo.save(oathUser);
            log.info("添加用户>>>" + userName + "<<<成功");
            return "添加用户>>>" + userName + "<<<成功";
        }
    }

    public String deleteUser(String userName) {

        OathUser oathUser = oathUserRepo.findByUserName(userName);
        if (oathUser == null) {
            return "请求删除的用户>>>" + userName + "<<<不存在";
        } else if (oathUser.getIsSuperUser()) {
            return "超级用户" + userName + "无法被删除";
        } else {
            oathUserRepo.delete(userName);
            log.info("已删除用户>>>" + userName + "<<<");
            return "已删除用户>>>" + userName + "<<<";
        }

    }
}
