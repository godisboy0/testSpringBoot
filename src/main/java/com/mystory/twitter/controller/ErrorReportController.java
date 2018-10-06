package com.mystory.twitter.controller;

import com.mystory.twitter.model.ErrorReport;
import com.mystory.twitter.model.OathUser;
import com.mystory.twitter.repository.ErrorReportRepo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/func")
@Api(description = "错误报告")
public class ErrorReportController {
    @Autowired
    ErrorReportRepo errorReportRepo;

    @GetMapping("/errorReport")
    @PreAuthorize("hasAnyRole('admin', 'user')")
    public ModelAndView reportErrorPage(ModelAndView modelAndView) {
        modelAndView.setViewName("errorReport");
        return modelAndView;
    }

    @PostMapping("/errorReport")
    @PreAuthorize("hasAnyRole('admin', 'user')")
    public ModelAndView reportError(@RequestParam(value = "errorUrl") String errorUrl,
                                    @RequestParam(value = "description") String description,
                                    HttpSession session,
                                    ModelAndView modelAndView) {
        try {
            ErrorReport errorReport = new ErrorReport();
            errorReport.setSubjectID(UUID.randomUUID().toString());
            errorReport.setErrorUrl(errorUrl);
            errorReport.setDescription(description);
            errorReport.setReportTime(new Date());
            if (session.getAttribute("User") != null)
                errorReport.setReportBy(((OathUser) session.getAttribute("User")).getUsername());
            errorReportRepo.save(errorReport);
            modelAndView.addObject("reportStatus","报告成功");
        } catch (Exception e) {
            modelAndView.addObject("reportStatus","报告失败");
        }
        modelAndView.setViewName("errorReport");
        return modelAndView;
    }

}
