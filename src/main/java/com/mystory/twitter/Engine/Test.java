package com.mystory.twitter.Engine;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.mystory.twitter.model.UserInfo;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    private static String whole = "</li>\n" +
            "\t\t\t\t\t\t\t<li class=\"share_more\">\n" +
            "\t\t\t\t\t\t\t\t<a href=\"javascript:;\" target=\"_self\"><i title=\"更多\"></i><span></span>更多</a>\n" +
            "\t\t\t\t\t\t\t</li>\n" +
            "\t\t\t\t\t\t\t<ul class=\"more\">\n" +
            "\t\t\t\t\t\t\t\t<span class=\"tri\"></span>\n" +
            "\t\t\t\t\t\t\t\t<li class=\"share_lofter js_share\" data-type=\"lofter\">\n" +
            "\t\t\t\t\t\t\t\t\t<a href=\"javascript:;\" target=\"_self\" title=\"分享到Lofter\"><i></i><span></span></a>\n" +
            "\t\t\t\t\t\t\t\t</li>\n" +
            "\t\t\t\t\t\t\t\t<li class=\"share_rr js_share\" data-type=\"rr\">\n" +
            "\t\t\t\t\t\t\t\t\t<a href=\"javascript:;\" target=\"_self\" title=\"分享到人人网\"><i></i><span></span></a>\n" +
            "\t\t\t\t\t\t\t\t</li>\n" +
            "\t\t\t\t\t\t\t\t<li class=\"share_youdao js_share\" data-type=\"youdao\">\n" +
            "\t\t\t\t\t\t\t\t\t<a href=\"javascript:;\" target=\"_self\" title=\"收藏到有道云笔记\"><i></i><span></span></a>\n" +
            "\t\t\t\t\t\t\t\t</li>\n" +
            "\t\t\t\t\t\t\t</ul>\n" +
            "\t\t\t\t\t\t</ul>\n" +
            "<div class=\"js_qrcode_wrap hidden\" id=\"js_qrcode_top\">\n" +
            "                                <div class=\"js_qrcode_arr\"></div>\n" +
            "                                <a href=\"javascript:;\" target=\"_self\" class=\"js_qrcode_close\" data-type=\"top\" title=\"关闭\"></a>\n" +
            "                                <div class=\"js_qrcode_img js_share_qrcode\"></div>\n" +
            "                                <p>用微信扫码二维码</p>\n" +
            "                                <p>分享至好友和朋友圈</p>\n" +
            "                            </div>\n" +
            "\t\t\t\t\t</div>\n" +
            "\t\t\t\t\t\t\t\t\t\t<div class=\"post_size_ctrl\">\n" +
            "\t\t\t\t\t\t<span class=\"post_size_t\">T</span>\n" +
            "\t\t\t\t\t\t<a class=\"post_size_large\" href=\"javascript:;\" target=\"_self\">+</a>\n" +
            "\t\t\t\t\t\t<a class=\"post_size_small\" href=\"javascript:;\" target=\"_self\">-</a>\n" +
            "\t\t\t\t\t</div>\n" +
            "            </div>\n" +
            "                                                            <div class=\"post_text\" id=\"endText\" style=\"border-top:1px solid #ddd;\">\n" +
            "                                <p class=\"otitle\">\n" +
            "                    （原标题：湖南一高校新生发布辱国言论被网友举报，校方：取消入学资格）\n" +
            "                </p>\n" +
            "                                <p class=\"f_center\"><img alt=\"大一新生称一辈子都不可能爱国 被取消入学资格\" src=\"http://cms-bucket.nosdn.127.net/2018/09/23/0a0b9405079c45dea18f7058031726fc.png?imageView&amp;thumbnail=550x0\" style=\"margin: 0px auto; display: block;\" />湖南城市学院官网通报截图</p><p>9月22日，湖南城市学院在官网首页“通知公告”栏目发布了对“贵州省省草王英俊发布辱国言论”事件的处理消息：该校决定取消涉事学生王栋的入学资格。</p><p>据湖南城市学院党委宣传部介绍，王栋，男，汉族，2000年8月13日出生，湖南祁东县人，现为学校2018级土木工程学院土木工程专业大一新生。9月19日凌晨，该生在网络微博以“贵州省省草王英俊”网名，在网络上发布“爱国是不可能爱国的，老子一辈子都不可能爱国”、“都他妈大学生了还爱国，我看你就是蠢货”等辱国言论，遭网友举报。益阳市有关部门和学校进行核查，查实了网名“贵州省省草王英俊”为该校土木专业新生王栋，王栋对所发错误微博言论供认不讳。又查，2018年9月9日入学以来，多次在学生宿舍发表辱国言论，且不听同学劝阻，并对同室同学爱国言论冷嘲热讽。</p><p>湖南城市学院党委宣传部表示，鉴于王栋散布辱国等极其错误言论，影响极坏。根据《国家招生考试规定》及《湖南省2018年普通高等学校招生工作实施办法》有关条款，《普通高等学校学生管理规定》第三章第九条和《湖南城市学院学籍管理规定》（湘城院发〔2017〕91号）第一章第二条之规定，经学校校长办公会研究，决定取消王栋入学资格。</p><p>湖南城市学院党委宣传部最后表态道，学校将深入贯彻全国教育大会精神，坚决反对损害党和国家声誉的言行，全面贯彻党的教育方针，切实加强思想政治工作，坚持立德树人，在全校师生员工中深入开展社会主义核心价值观教育，引导全校师生坚定“四个自信”，增强“四个意识”，爱党爱国爱人民，为培养担当民族复兴大任的一代新人而奋力。</p><p>澎湃新闻查询《湖南城市学院学籍管理规定》（湘城院发〔2017〕91号）发现，其第一章第二条为：新生入学后三个月内，由各二级学院审阅本院学生档案，学生工作部（处）组织有关部门对其进行政治思想品德、心理和身体健康复查，复查全部合格者准予注册，正式取得湖南城市学院学生学籍。不符合招生条件者，应根据情况予以处理，直至取消入学资格。凡属弄虚作假、徇私舞弊被录取者，不论何时发现，一经查实，由教务处、招生就业处审核，报校长办公会审定，取消其入学资格或取消其学籍，退回父母或抚养人所在地；情节恶劣的，转请有关部门查究。</p><p>湖南城市学院是一所由湖南省人民政府主办的全日制普通本科院校，学校位于湖南省益阳市。此前，9月19日，该校官网发布声明称，针对网友反映的网名为“贵州省省草王英俊”的微博用户在微博空间发表错误言论一事，该校高度重视，迅速成立工作小组进行调查。经查，该微博用户为该校土木工程学院大一新生。目前，学校正积极配合相关部门开展调查，并将根据调查情况严格依法依规处理。</p><p class=\"f_center\"><img alt=\"大一新生称一辈子都不可能爱国 被取消入学资格\" src=\"http://cms-bucket.nosdn.127.net/2018/09/23/ef272c626cea4806a692ea9b88515419.jpeg?imageView&amp;thumbnail=550x0\" style=\"margin: 0px auto; display: block;\" /></p><p class=\"f_center\"><img alt=\"大一新生称一辈子都不可能爱国 被取消入学资格\" src=\"http://cms-bucket.nosdn.127.net/2018/09/23/d401a137be6d4cf88732eef9a2080ccd.jpeg?imageView&amp;thumbnail=550x0\" style=\"margin: 0px auto; display: block;\" /></p><p class=\"f_center\"><img alt=\"大一新生称一辈子都不可能爱国 被取消入学资格\" src=\"http://cms-bucket.nosdn.127.net/2018/09/23/6cfdea43fec8452a8df64b05f119f1e4.jpeg?imageView&amp;thumbnail=550x0\" style=\"margin: 0px auto; display: block;\" /></p><p class=\"f_center\"><img alt=\"大一新生称一辈子都不可能爱国 被取消入学资格\" src=\"http://cms-bucket.nosdn.127.net/2018/09/23/e32dcf6404cf4208892fd9387550d348.jpeg?imageView&amp;thumbnail=550x0\" style=\"margin: 0px auto; display: block;\" />“贵州省省草王英俊”发表的不当言论截图</p><p><!-- AD200x300_2 -->\n" +
            "<div class=\"gg200x300\">\n" +
            "<div class=\"at_item right_ad_item\" adType=\"rightAd\" requestUrl=\"https://nex.163.com/q?app=7BE0FC82&c=news&l=133&site=netease&affiliate=news&cat=article&type=logo300x250&location=12\"></div>\n" +
            "<a href=\"javascript:;\" target=\"_self\" class=\"ad_hover_href\"></a>\n" +
            "</div>\n" +
            "                <p></p>\n" +
            "                                                <div class=\"ep-source cDGray\">\n" +
            "                    <span class=\"left\"><a href=\"https://news.163.com/\"><img src=\"http://static.ws.126.net/cnews/css13/img/end_news.png\" alt=\"赵亚萍\" width=\"13\" height=\"12\" class=\"icon\"></a> 本文来源：澎湃新闻  </span>\n" +
            "                    <!--赵亚萍_NN9005--><span class=\"ep-editor\">责任编辑：赵亚萍_NN9005</span>                </div>\n" +
            "                                \t\t\t\t\t\t\t\t            </div>\n" +
            "                                <div class=\"post_btmshare\">\n" +
            "\t\t\t\t\t<span class=\"post_topshare_title\">分享到：</span>\n" +
            "\t\t\t\t\t<ul class=\"post_share\">\n" +
            "\t\t\t\t\t\t<li class=\"share_yixin js_share\" data-type=\"yixin\">\n" +
            "\t\t\t\t\t\t\t<a href=\"javascript:;\" target=\"_self\"><i title=\"分享到易信\"></i><span></span>易信</a>\n" +
            "\t\t\t\t\t\t</li>\n" +
            "\t\t\t\t\t\t<li class=\"share_weixin js_weixin\" data-type=\"btm\">\n" +
            "\t\t\t\t\t\t\t<a href=\"javascript:;\" target=\"_self\"><i title=\"分享到微信\"></i><span></span>微信</a>\n" +
            "\t\t\t\t\t\t</li>\n" +
            "\t\t\t\t\t\t<li class=\"share_qzone js_share\" data-type=\"qzone\">\n" +
            "\t\t\t\t\t\t\t<a href=\"javascript:;\" target=\"_self\"><i title=\"分享到QQ空间\"></i><span></span>QQ空间</a>\n" +
            "\t\t\t\t\t\t</li>\n" +
            "\t\t\t\t\t\t<li class=\"share_weibo js_share\" data-type=\"weibo\">\n" +
            "\t\t\t\t\t\t\t<a href=\"javascript:;\" target=\"_self\"><i title=\"分享到新浪微博\"></i><span></span>微博</a>\n" +
            "\t\t\t\t\t\t</li>\n" +
            "\t\t\t\t\t\t<li class=\"share_more\">\n" +
            "\t\t\t\t\t\t\t<a href=\"javascript:;\" target=\"_self\"><i title=\"更多\"></i><span></span>更多</a>\n" +
            "\t\t\t\t\t\t</li>\n" +
            "\t\t\t\t\t\t<ul class=\"more\">\n" +
            "\t\t\t\t\t\t\t<span class=\"tri\"></span>";

    private static String getNarrowUrlContent(String wholeUrlContent) {
        String ret = new String();
        Pattern p = Pattern.compile("<p(.*?)</p>");
        Matcher matcher = p.matcher(wholeUrlContent);
        while (matcher.find()) {
            ret += matcher.group(1);
        }
        return ret;
    }

    public static String generateGsonOfUserinfo(){
        Gson gson = new Gson();
        UserInfo userInfo = new UserInfo();
        userInfo.setScreenName("realDonaldTrump");
        userInfo.setLastGotID(null);
        userInfo.setFirstGotID(null);
        userInfo.setKeywordChanged(true);
        ArrayList<String> keywords = new ArrayList<>();
        keywords.add("Las Vegas");
        keywords.add("North Korea");
        userInfo.setKeyWords(gson.toJson(keywords));
        userInfo.setStartTime(new Date(2018 - 1900,8,20));
        userInfo.setFinishTime(new Date());
        String stringUserInfo = gson.toJson(userInfo);
        return stringUserInfo;
    }

    public static String[] testRegixSplit(String text){
        return text.split("[;；]");
    }

    public static String testProxy(String url) throws Exception{
        String proxyhost = "127.0.0.1";
        Integer proxyport = 1080;
        HttpHost proxy = new HttpHost(proxyhost, proxyport);
        RequestConfig proxyConfig = RequestConfig.custom().setProxy(proxy).
                setConnectTimeout(10000).setConnectionRequestTimeout(10000).setSocketTimeout(10000).build();
        HttpClient httpClient = HttpClients.createDefault();
        URI uri = new URIBuilder(url).build();
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setConfig(proxyConfig);
        HttpResponse response = httpClient.execute(httpGet);
        String entityString = EntityUtils.toString(response.getEntity(), "UTF-8");
        return entityString;
    }

    public static void main(String args[]) throws Exception{
        String content = testProxy("https://www.google.com");
        System.out.println(content);
    }
}