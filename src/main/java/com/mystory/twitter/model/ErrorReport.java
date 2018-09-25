package com.mystory.twitter.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "ErrorReport")
@EqualsAndHashCode
public class ErrorReport {
    @Id
    @NonNull
    private String subjectID;
    private String errorUrl;
    private Date reportTime;
    private String description;
    private String reportBy;

}
