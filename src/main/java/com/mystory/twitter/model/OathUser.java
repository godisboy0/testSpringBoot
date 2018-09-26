package com.mystory.twitter.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Getter
@Setter
@Table(name = "OathUser")
@EqualsAndHashCode
public class OathUser {
    @Id
    @NonNull
    @Size(min = 2, max = 20, message = "用户名必须是2到20位")
    @Pattern(regexp = "[a-zA-Z0-9_]+", message = "只允许大小写字母、数字和下划线")
    private String userName;

    @Column(length = 50)
    private String password;        //加密后的密码
    private Boolean isSuperUser = false;
}
