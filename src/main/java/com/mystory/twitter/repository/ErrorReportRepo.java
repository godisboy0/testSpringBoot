package com.mystory.twitter.repository;

import com.mystory.twitter.model.ErrorReport;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorReportRepo extends CrudRepository<ErrorReport,String> {
}
