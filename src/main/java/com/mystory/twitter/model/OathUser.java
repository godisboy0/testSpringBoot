package com.mystory.twitter.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "OathUser")
@EqualsAndHashCode
public class OathUser {

    @Id
    @GeneratedValue
    private Integer id;
    private String username;
    private String role;
}
