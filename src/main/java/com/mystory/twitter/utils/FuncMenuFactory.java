package com.mystory.twitter.utils;

import java.util.ArrayList;
import java.util.List;

public class FuncMenuFactory {
    private static FuncMenu getTwitterContentFunc = new FuncMenu("按推特账户和时间获取已爬取的数据", "/func/getOne");
    private static FuncMenu getUserGuideFunc = new FuncMenu("查看用户指南", "/hello/doc");
    private static FuncMenu lookUpUserInfoFunc = new FuncMenu("查看已经设置的爬取账号信息", "/func/findUserInfo");
    private static FuncMenu batchSetUserInfoFunc = new FuncMenu("批量设置需要爬取的推特账号", "/root/insertUser");
    private static FuncMenu deleteUserInfoFunc = new FuncMenu("批量删除需要爬取的推特账号", "/root/deleteUser");
    private static FuncMenu errorReport = new FuncMenu("报告发现的行为异常", "/func/errorReport");
    private static FuncMenu startClimb = new FuncMenu("开始爬取","/root/fetchNow");
    private static List<FuncMenu> generalFuncs = null;
    private static List<FuncMenu> adminFuncs = null;

    public static List<FuncMenu> getGeneralFuncs() {
        if (generalFuncs == null) {
            generalFuncs = new ArrayList<>();
            generalFuncs.add(getTwitterContentFunc);
            generalFuncs.add(lookUpUserInfoFunc);
            generalFuncs.add(getUserGuideFunc);
            generalFuncs.add(errorReport);
        }
        return generalFuncs;
    }

    public static List<FuncMenu> getAdminFuncs() {
        if (adminFuncs == null) {
            if (generalFuncs == null) {
                getGeneralFuncs();
            }
            adminFuncs = new ArrayList<>(generalFuncs);
            adminFuncs.add(batchSetUserInfoFunc);
            adminFuncs.add(deleteUserInfoFunc);
            adminFuncs.add(startClimb);
        }
        return adminFuncs;
    }
}
