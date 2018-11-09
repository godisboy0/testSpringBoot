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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
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
    private Workbook thisWorkBook;

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

    @SuppressWarnings("unchecked")
    public List<FrontTwitterContent> getFrontTwitterContent(String screenNames, Date startTime, Date finishTime, Boolean narrowMatch) {
        log.info("read stored matched tweet for " + screenNames);
        List<FrontTwitterContent> ret = new ArrayList<>();
        List<TwitterContent> twitterContents = new ArrayList<>();
        List<String> names = Arrays.stream(screenNames.split("[;；]")).map(String::trim).filter(s -> !Strings.isNullOrEmpty(s)).filter(s -> !s.contains(" ")).distinct().collect(Collectors.toList());
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

        this.thisWorkBook = getWorkBook(ret);

        return ret;
    }

    public Workbook getFullExcelForDownload() {

        String screenNames = userInfoRepo.findAll().stream().map(UserInfo::getScreenName).collect(Collectors.joining(";"));
        Date finishTime = new Date(2018, 0, 1);
        Date startTime = new Date(0, 0, 1);

        List<FrontTwitterContent> twitterContents = getFrontTwitterContent(
            screenNames, startTime, finishTime, false);

        return getWorkBook(twitterContents);
    }

    public Workbook getThisExcelForDownload() {
        return this.thisWorkBook;
    }

    private Workbook getWorkBook(List<FrontTwitterContent> twitterContents) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFCellStyle mainStyle = workbook.createCellStyle();
        mainStyle.setAlignment(HorizontalAlignment.CENTER);
        mainStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        mainStyle.setWrapText(true);

        XSSFCellStyle backGroundCellStyle = workbook.createCellStyle();
        backGroundCellStyle.setFillForegroundColor(XSSFColor.toXSSFColor(new XSSFColor(new java.awt.Color(255, 255, 0), new DefaultIndexedColorMap())));
        backGroundCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        backGroundCellStyle.setAlignment(HorizontalAlignment.CENTER);
        backGroundCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        backGroundCellStyle.setWrapText(true);
        Sheet sheet = workbook.createSheet();
        Row row = sheet.createRow(0);
        row.setHeightInPoints(88);

        XSSFDataFormat format = workbook.createDataFormat();
        XSSFCellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(format.getFormat("yyyy年MM月dd日"));

        for (String titleName : Lists.newArrayList("screenName", "发推时间", "匹配的关键字",
            "推特内容", "推特地址", "引用推文", "窄匹配链接", "宽匹配链接", "未解析成功的链接", "匹配位置")) {
            Cell cell = row.createCell(row.getLastCellNum() < 0 ? 0 : row.getLastCellNum());
            cell.setCellValue(titleName);
            cell.setCellStyle(mainStyle);
        }
        int index = 1;
        Set<FrontTwitterContent> processed = new HashSet<>();
        for (FrontTwitterContent twitterContent : twitterContents) {
            if (processed.contains(twitterContent))
                continue;
            if (twitterContent.getMatchPlace().contains("原始推文")) {
                writeToSheet(sheet, twitterContent, dateStyle, backGroundCellStyle, mainStyle, index);
                index++;
                processed.add(twitterContent);
            }
        }

        for (FrontTwitterContent twitterContent : twitterContents) {
            if (processed.contains(twitterContent))
                continue;
            if (!twitterContent.getMatchKeyword().equals("[]") && twitterContent.getNarrowMatchUrls() != null) {
                writeToSheet(sheet, twitterContent, dateStyle, backGroundCellStyle, mainStyle, index);
                index++;
                processed.add(twitterContent);
            }
        }

        for (FrontTwitterContent twitterContent : twitterContents) {
            if (processed.contains(twitterContent))
                continue;
            if (!twitterContent.getMatchKeyword().equals("[]")) {
                writeToSheet(sheet, twitterContent, dateStyle, backGroundCellStyle, mainStyle, index);
                index++;
                processed.add(twitterContent);
            }
        }

        for (FrontTwitterContent twitterContent : twitterContents) {
            if (processed.contains(twitterContent))
                continue;
            if (twitterContent.getMatchKeyword().equals("[]")) {
                writeToSheet(sheet, twitterContent, dateStyle, backGroundCellStyle, mainStyle, index);
                index++;
            }
        }

        for (int i = 0; i != 10; ++i) {
            sheet.setColumnWidth(i, 21 * 256);
        }
        return workbook;
    }

    private void writeToSheet(Sheet sheet, FrontTwitterContent twitterContent,
                              XSSFCellStyle dateStyle, XSSFCellStyle backGroundCellStyle,
                              XSSFCellStyle mainCellStyle, int index) {

        Row row = sheet.createRow(index);
        row.setHeightInPoints(88);
        row.createCell(0).setCellValue(twitterContent.getScreenName());
        Cell dateCell = row.createCell(row.getLastCellNum());
        dateCell.setCellValue(twitterContent.getTweetTime());
        dateCell.setCellStyle(dateStyle);
        row.createCell(row.getLastCellNum()).setCellValue(twitterContent.getMatchKeyword());
        row.createCell(row.getLastCellNum()).setCellValue(twitterContent.getTweetContent());
        row.createCell(row.getLastCellNum()).setCellValue(twitterContent.getTweetUrl());
        StringBuilder sb = new StringBuilder();
        if (twitterContent.getQuotedTweets() != null) {
            for (Map<String, String> quotedTweet : twitterContent.getQuotedTweets()) {
                sb.append(quotedTweet.get("tweetContent")).append("tweetUrl").append("  ;\n");
            }
        }
        row.createCell(row.getLastCellNum()).setCellValue(sb.length() == 0 ? " " : sb.toString());
        String narrowMatchedUrlString = twitterContent.getNarrowMatchUrls() != null ? String.join("  ;\n", twitterContent.getNarrowMatchUrls()) : " ";

        row.createCell(row.getLastCellNum()).setCellValue(narrowMatchedUrlString);
        String wideMatchUrlString = twitterContent.getWideMatchUrls() != null ? String.join("  ;\n", twitterContent.getWideMatchUrls()) : " ";
        row.createCell(row.getLastCellNum()).setCellValue(wideMatchUrlString);

        String missedUrlString = twitterContent.getMissedUrls() != null ? String.join("  ;\n", twitterContent.getMissedUrls()) : " ";
        row.createCell(row.getLastCellNum()).setCellValue(missedUrlString);

        for (int i = 0; i != 9; ++i) {
            row.getCell(i).setCellStyle(mainCellStyle);
        }

        if (twitterContent.getMatchPlace().contains("原始推文")) {
            row.getCell(3).setCellStyle(backGroundCellStyle);
        }
        if (twitterContent.getMatchPlace().contains("推文链接")) {
            if (twitterContent.getNarrowMatchUrls() != null)
                row.getCell(6).setCellStyle(backGroundCellStyle);
            if (twitterContent.getWideMatchUrls() != null)
                row.getCell(7).setCellStyle(backGroundCellStyle);
        }
        if (twitterContent.getMatchPlace().contains("被引推文")) {
            row.getCell(5).setCellStyle(backGroundCellStyle);
        }
        if (twitterContent.getMatchPlace().contains("无法解析部分外链")) {
            row.getCell(8).setCellStyle(backGroundCellStyle);
        }
        String matchPlace = twitterContent.getMatchPlace();
        if (matchPlace != null && matchPlace.startsWith("[")) {
            matchPlace = matchPlace.replace("[", "");
            matchPlace = matchPlace.replace("]", "");
        }
        row.createCell(row.getLastCellNum()).setCellValue(matchPlace);
        row.getCell(9).setCellStyle(mainCellStyle);
    }

}
