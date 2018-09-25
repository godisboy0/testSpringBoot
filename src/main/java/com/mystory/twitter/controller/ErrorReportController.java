package com.mystory.twitter.controller;


import com.google.gson.Gson;
import com.mystory.twitter.model.ErrorReport;
import com.mystory.twitter.repository.ErrorReportRepo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/errorReport")
@Api(description = "错误报告")
public class ErrorReportController {
    @Autowired
    ErrorReportRepo errorReportRepo;

    @PostMapping("/report")
    public String reportError(@RequestBody String json) {
        ErrorReport errorReport = new Gson().fromJson(json,ErrorReport.class);
        errorReport.setSubjectID(UUID.randomUUID().toString());
        errorReportRepo.save(errorReport);
        return "已上报问题";
    }

}
