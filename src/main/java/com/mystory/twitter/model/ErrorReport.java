package com.mystory.twitter.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

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
    private LocalDateTime reportTime;
    private String description;

}
