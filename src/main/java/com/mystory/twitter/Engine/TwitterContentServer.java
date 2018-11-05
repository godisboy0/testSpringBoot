package com.mystory.twitter.Engine;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mystory.twitter.model.FrontTwitterContent;
import com.mystory.twitter.model.MatchPlace;
import com.mystory.twitter.model.TwitterContent;
import com.mystory.twitter.model.UserInfo;
import com.mystory.twitter.repository.TwitterContentRepo;
import com.mystory.twitter.repository.UserInfoRepo;
import lombok.extern.log4j.Log4j;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Log4j
@Service
public class TwitterContentServer {
    @Autowired
    UserInfoRepo userInfoRepo;
    @Autowired
    TwitterContentRepo twitterContentRepo;
    private Gson gson = new Gson();

    public String getAllScreenNames() {
        StringBuilder stringBuilder = new StringBuilder();
        for (UserInfo userInfo : userInfoRepo.findAll()) {
            stringBuilder.append(userInfo.getScreenName()).append("; ");
        }
        return stringBuilder.toString();
    }

    private String getPlaceString(Integer places) {
        StringBuilder stringBuilder = new StringBuilder();
        if ((places & MatchPlace.originTweet) != 0) {
            stringBuilder.append("原始推文;");
        }
        if (((places & MatchPlace.originURL) | (places & MatchPlace.quotedUrl)) != 0) {
            stringBuilder.append("推文链接;");
        }
        if ((places & MatchPlace.quotedTweet) != 0) {
            stringBuilder.append("被引推文;");
        }
        if ((places & MatchPlace.mayMissed) != 0) {
            stringBuilder.append("无法解析部分外链;");
        }
        return stringBuilder.toString();
    }

    public List<FrontTwitterContent> getFrontTwitterContent(String screenNames, Date startTime, Date finishTime, Boolean narrowMatch) {
        log.info("read stored matched tweet for " + screenNames);
        List<FrontTwitterContent> ret = new ArrayList<>();
        List<TwitterContent> twitterContents = new ArrayList<>();
        List<String> names = Arrays.asList(screenNames.split("[;；]")).stream().map(String::trim).filter(s -> !Strings.isNullOrEmpty(s)).filter(s -> !s.contains(" ")).distinct().collect(Collectors.toList());
        for (String name : names) {
            twitterContents.addAll(twitterContentRepo.
                findByScreenNameAndIsQuotedAndUrlNarrowMatchAndTweetTimeGreaterThanAndTweetTimeLessThan(
                    name, false, narrowMatch, startTime, finishTime
                ));
        }
        if (!narrowMatch) {
            for (String name : names) {
                twitterContents.addAll(twitterContentRepo.
                    findByScreenNameAndIsQuotedAndUrlNarrowMatchAndTweetTimeGreaterThanAndTweetTimeLessThan(
                        name, false, !narrowMatch, startTime, finishTime
                    ));
            }
        }
        for (TwitterContent twitterContent : twitterContents) {
            FrontTwitterContent frontTwitterContent = new FrontTwitterContent();
            frontTwitterContent.setScreenName(twitterContent.getScreenName());
            frontTwitterContent.setTweetTime(twitterContent.getTweetTime());
            frontTwitterContent.setTweetContent(twitterContent.getTweetContent());
            frontTwitterContent.setTweetUrl(twitterContent.getTweetUrl());
            frontTwitterContent.setMatchKeyword(twitterContent.getMatchedKeyword());
            frontTwitterContent.setMatchPlace(getPlaceString(twitterContent.getFoundPlace()));
            frontTwitterContent.setNarrowMatchUrls(gson.fromJson(twitterContent.getNarrowMatchedUrls(), ArrayList.class));
            frontTwitterContent.setWideMatchUrls(gson.fromJson(twitterContent.getWideMatchedUrls(), ArrayList.class));
            frontTwitterContent.setMissedUrls(gson.fromJson(twitterContent.getMissedUrls(), ArrayList.class));
            frontTwitterContent.setQuotedTweets(null);
            List<Map<String, String>> quotedTweets = new ArrayList<>();
            while (twitterContent.getQuotedTweetSubjectID() != null) {
                HashMap<String, String> quotedTweet = new HashMap<>();
                twitterContent = twitterContentRepo.findOne(twitterContent.getQuotedTweetSubjectID());
                quotedTweet.put("tweetContent", twitterContent.getTweetContent());
                quotedTweet.put("tweetUrl", twitterContent.getTweetUrl());
                quotedTweets.add(quotedTweet);
            }
            if (!quotedTweets.isEmpty())
                frontTwitterContent.setQuotedTweets(quotedTweets);
            ret.add(frontTwitterContent);
        }

        return ret;
    }

    public Workbook getExcelForDownload() {
        Date startTime = new Date(0, 0, 1);
        Date finishTime = new Date(2018, 0, 1);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row row = sheet.createRow(0);
        for (String sheetName : Lists.newArrayList("screenName", "发推时间", "匹配的关键字",
            "推特内容", "推特地址", "匹配位置", "引用推文", "窄匹配链接", "宽匹配链接", "未解析成功的链接")) {
            row.createCell(row.getLastCellNum()).setCellValue(sheetName);
        }
        List<FrontTwitterContent> twitterContents = getFrontTwitterContent(
            userInfoRepo.findAll().stream().map(UserInfo::getScreenName).collect(Collectors.joining(",")),
            startTime, finishTime, false);
        for (FrontTwitterContent twitterContent : twitterContents) {
            row = sheet.createRow(sheet.getLastRowNum());
            row.createCell(row.getLastCellNum()).setCellValue(twitterContent.getScreenName());
            row.createCell(row.getLastCellNum()).setCellValue(twitterContent.getTweetTime());
            row.createCell(row.getLastCellNum()).setCellValue(twitterContent.getMatchKeyword());
            row.createCell(row.getLastCellNum()).setCellValue(twitterContent.getTweetContent());
            row.createCell(row.getLastCellNum()).setCellValue(twitterContent.getTweetUrl());
            row.createCell(row.getLastCellNum()).setCellValue(twitterContent.getMatchPlace());
            StringBuilder sb = new StringBuilder();
            for (Map<String, String> quotedTweet : twitterContent.getQuotedTweets()) {
                sb.append(quotedTweet.get("tweetContent")).append("tweetUrl").append("  ;\n");
            }
            row.createCell(row.getLastCellNum()).setCellValue(sb.toString());
            row.createCell(row.getLastCellNum()).setCellValue(twitterContent.getNarrowMatchUrls().stream().collect(Collectors.joining("  ;\n")));
            row.createCell(row.getLastCellNum()).setCellValue(twitterContent.getWideMatchUrls().stream().collect(Collectors.joining("  ;\n")));
            row.createCell(row.getLastCellNum()).setCellValue(twitterContent.getMissedUrls().stream().collect(Collectors.joining("  ;\n")));
            XSSFCellStyle xssfCellStyle = ((XSSFWorkbook) workbook).createCellStyle();
            xssfCellStyle.setFillBackgroundColor(XSSFColor.toXSSFColor(HSSFColor.HSSFColorPredefined.LIGHT_YELLOW.getColor()));
            if (twitterContent.getMatchPlace().contains("原始推文")) {
                row.getCell(3).setCellStyle(xssfCellStyle);
            }
            if (twitterContent.getMatchPlace().contains("推文链接")) {
                row.getCell(7).setCellStyle(xssfCellStyle);
                row.getCell(8).setCellStyle(xssfCellStyle);
            }
            if (twitterContent.getMatchPlace().contains("被引推文")) {
                row.getCell(6).setCellStyle(xssfCellStyle);
            }
            if (twitterContent.getMatchPlace().contains("无法解析部分外链")) {
                row.getCell(9).setCellStyle(xssfCellStyle);
            }
        }
        return workbook;
    }

}
