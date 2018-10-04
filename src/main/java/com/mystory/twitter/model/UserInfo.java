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
    private String keyWords;            //用GSON转化一个Set<String>
    private Long lastGotID;
    private Long firstGotID;
    private Date startTime;
    private Date finishTime;
    private Date lastFetchTime;         //当点击爬取按钮时，上次爬取时间太近的人将不予再次爬取
    private Boolean startTimeChanged;   //用于减少爬取量
    private Boolean keywordChanged;     //按ID筛选时用，当keyword更改过时不进行ID筛选

}
