package com.mystory.twitter.model;


import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "TwitterContent")
@EqualsAndHashCode
public class TwitterContent {

    @Id
    @NonNull
    private String subjectID;                   //enrich 生成一个UUID
    private Long tweetID;                       //enrich tweet的ID
    private String screenName;                  //enrich
    private String tweetUrl;                    //enrich 这个tweet的URL
    private String narrowMatchedUrls;           //       narrow匹配keyword的URL地址，如无则为null，是gson转化为json的一个列表（不包含quoted）
    private String wideMatchedUrls;             //       wide匹配keyword的URL地址
    private String missedUrls;                  //       未获取到内容的URL的地址（不包含quoted）
    @Column(length = 1000)
    private String tweetContent;                //enrich
    private String quotedTweetSubjectID;        //       如果在引用的Status中发现了keywordMatch，这个用来记录那个结果的Id，否则为null
    private Integer foundPlace;                 //enrich 关键字在哪里找到的，有四种可能，1.在推文中；2.在链接中；3.在引用的推文中；4.在引用的推文的链接中
    private Date tweetTime;                     //enrich
    private Date recordTime;                    //enrich
    private String matchedKeyword;              //       match的keyword，如无则为null，所以一个gson转化为json的set
    private Boolean isQuoted;                   //       是否是引用的推文，引用的推文暂不会记录matchedKeyword,因为其被存储入数据库是因为其他推文引用了它。
    private Boolean urlNarrowMatch;             //       如果为flase，说明在对url匹配时对网页全部源代码的字符进行匹配（有可能匹配到广告、其他链接等）
                                                //       如果为true，则只对<p></p>之间的数据进行匹配。包括<p xxx> </p>这样的。如果narrow匹配上了，直接命中
    private Boolean mayMissedSomeUrl;           //       表明在解析urlEntity时出现错误，返回了空字符串或null。这样的未匹配上也会记录。
//    private String RelatedUsers;
//    private String linkUrl;

}
