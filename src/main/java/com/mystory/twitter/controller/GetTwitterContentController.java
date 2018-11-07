package com.mystory.twitter.controller;


import com.mystory.twitter.Engine.TwitterContentServer;
import com.mystory.twitter.model.FrontTwitterContent;
import javassist.bytecode.ByteArray;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/func")
public class GetTwitterContentController {
    @Autowired
    private TwitterContentServer twitterContentServer;

    @GetMapping("/getOne")
    @PreAuthorize("hasRole('admin') or hasRole('user')")
    public ModelAndView getOne(ModelAndView modelAndView, HttpSession session) {
        modelAndView.setViewName("getOne");
        modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
        return modelAndView;
    }

    @PostMapping("/getOne")
    @PreAuthorize("hasRole('admin') or hasRole('user')")
    public ModelAndView postToGetOne(@RequestParam(value = "sname") String screenNames,
                                     @RequestParam(value = "startTime", required = false)
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
                                     @RequestParam(value = "finishTime", required = false)
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date finishTime,
                                     @RequestParam(value = "narrowMatch", required = false) boolean narrowMatch,
                                     ModelAndView modelAndView) {
        if (finishTime == null) {
            finishTime = new Date(2018, 0, 1);
        }
        if (startTime == null) {
            startTime = new Date(0, 0, 1);
        }
        if (startTime.compareTo(finishTime) >= 0) {
            modelAndView.addObject("error", "哥，仔细点……你这开始日期都比结束日期还晚了");
        } else {
            List<FrontTwitterContent> frontTwitterContents = twitterContentServer.
                getFrontTwitterContent(screenNames, startTime, finishTime, narrowMatch);
            modelAndView.addObject("twitterContents", frontTwitterContents);
            modelAndView.addObject("screenNames", twitterContentServer.getAllScreenNames());
            modelAndView.addObject("getNum", frontTwitterContents.size());
        }
        modelAndView.setViewName("getOne");
        return modelAndView;
    }

    @GetMapping("/downloadExcel")
    @PreAuthorize("hasRole('admin') or hasRole('user')")
    public void downLoadFile(HttpServletResponse response) throws Exception {

        String fileName = "twitter-content" + LocalDate.now().toString() + ".xlsx";

        Workbook workbook = twitterContentServer.getExcelForDownload();

        try {
            response.reset();
            response.setHeader("content-disposition", "attachment;filename=" + fileName);
            response.setContentType("APPLICATION/msexcel");
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
            response.getOutputStream().close();
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            response.reset();
//            response.reset();
//            fileName = URLEncoder.encode(fileName, "UTF-8");
//            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
//            response.setContentType("application/octet-stream; charset=utf-8");
//            workbook.write(response.getOutputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

}
