package com.mystory.twitter.repository;

import com.mystory.twitter.model.TwitterContent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TwitterContentRepo extends CrudRepository<TwitterContent, String> {
    List<TwitterContent> findAll();

    List<TwitterContent> findByScreenNameAndIsQuotedAndTweetTimeGreaterThanAndTweetTimeLessThan(
            String screenName, Boolean isQuoted, Date startDate, Date finishDate);

    List<TwitterContent> findByScreenNameAndIsQuotedAndUrlNarrowMatchAndTweetTimeGreaterThanAndTweetTimeLessThan(
            String screenName, Boolean isQuoted, Boolean narrowMatched, Date startDate, Date finishDate
    );

}
