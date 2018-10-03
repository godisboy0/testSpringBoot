package com.mystory.twitter.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResult {
    private String screenName;
    private Integer updatedNum;
    private Boolean succeed;
}
