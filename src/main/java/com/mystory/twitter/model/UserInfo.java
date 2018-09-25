package com.mystory.twitter.model;


import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "UserInfo")
@EqualsAndHashCode
public class UserInfo {

    @Id
    @NonNull
    private String screenName;
    private String keyWords;        //用GSON转化一个Set<String>
    private Long lastGotID;
    private Long firstGotID;
    private Date startTime;
    private Date finishTime;
    private Boolean keywordChanged; //按ID筛选时用，当keyword更改过时不进行ID筛选

}
