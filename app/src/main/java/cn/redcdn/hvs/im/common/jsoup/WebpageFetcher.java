package cn.redcdn.hvs.im.common.jsoup;

import android.text.TextUtils;


import com.butel.connectevent.utils.LogUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import cn.redcdn.hvs.im.bean.WebpageBean;
import cn.redcdn.log.CustomLog;

public class WebpageFetcher {

    /**
     * QQ新闻链接
     */
    private final static String URL_PREFIX_QQ_NEWS = "http://view.inews.qq.com";
    /**
     * 微信公众号链接
     */
    private final static String URL_PREFIX_WX = "http://mp.weixin.qq.com";
    /**
     * 头条链接
     */
    private static final String URL_PREFIX_TOUTIAO = "http://toutiao.com/";
    /**
     * 头条APP链接
     */
    private static final String URL_PREFIX_TOUTIAO_APP = "http://app.toutiao.com/";

    // 1、http或者https开头；
    // 2、不以.gif（不区分大小写）结尾；
    private static final String JSOUP_SELECTOR_IMG = "img[src~=^(http[s]{0,1})://.+$(?<!\\.(?i)gif)], img[alt_src~=^(http[s]{0,1})://.+$(?<!\\.(?i)gif)]";

    public static WebpageBean fetchWebpage(String url) {
        LogUtil.d("处理链接：" + url);
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        try {
            if (url.startsWith(URL_PREFIX_QQ_NEWS)) {
                return parsingQQUrl(url);
            } else if (url.startsWith(URL_PREFIX_WX)) {
                return parsingWXUrl(url);
            }

            Document linkDoc = Jsoup.parse(new URL(url), 5000);
            WebpageBean bean = new WebpageBean();
            StringBuilder sb = new StringBuilder();

            Element titleEl = linkDoc.select("head > title").first();
            Element descEl = linkDoc.select("head > meta[name=\"description\"]").first();

//            Elements elements = linkDoc.select("div.activity_banner").select("p");
//            Element descE2 = linkDoc.select("div > div > div > p").first();

            if (titleEl != null) {
                String title = titleEl.text();
                sb.append("\n").append("title:" + title);
                bean.setTitle(title);
            }
            if (descEl != null) {
                String desc = descEl.attr("content");
                sb.append("\n").append("desc:" + desc);
                bean.setDescription(desc);
            }else{
//                String desc = elements.get(1).text();
//                sb.append("\n").append("desc:" + desc);
//                bean.setDescription(desc);
                CustomLog.e("WebpageFetcher","descEl is null");
            }
            Elements imgEls = null;
            boolean hasImageUrl = false;
            if (url.startsWith(URL_PREFIX_TOUTIAO)) {
                imgEls = linkDoc.body().select("div.article-content");
                if (imgEls != null && imgEls.size() > 0) {
                	imgEls = imgEls.first().select(JSOUP_SELECTOR_IMG);
                }else{
                	imgEls = linkDoc.body().select(JSOUP_SELECTOR_IMG);
                }
            	
            } else if (url.startsWith(URL_PREFIX_TOUTIAO_APP)) {
                    imgEls = linkDoc.head().select("link[rel=\"shortcut icon\"]");
                    if (imgEls != null && imgEls.size() > 0) {
                        Element imgEl = imgEls.first();
                        String imgUrl = imgEl.absUrl("href");
                        sb.append("\n").append("shortcut icon imgUrl:" + imgUrl);
                        bean.setImgUrl(imgUrl);
                        hasImageUrl = true;
                    }
            }else{
                imgEls = linkDoc.body().select(JSOUP_SELECTOR_IMG);
                }
            
            if (!hasImageUrl && imgEls != null && imgEls.size() > 0) {
                Element imgEl = imgEls.first();
                String imgUrl = imgEl.hasAttr("src") ? imgEl.absUrl("src") : imgEl.absUrl("alt_src");
                sb.append("\n").append("imgUrl:" + imgUrl);
                bean.setImgUrl(imgUrl);
            }else{
            	if(imgEls==null||imgEls.size()==0){
                imgEls = linkDoc.head().select("link[rel=\"shortcut icon\"]");
            		
                if (imgEls != null && imgEls.size() > 0) {
                    Element imgEl = imgEls.first();
                    String imgUrl = imgEl.absUrl("href");
                    sb.append("\n").append("shortcut icon imgUrl:" + imgUrl);
                    bean.setImgUrl(imgUrl);
                    hasImageUrl = true;
                }
            }
            }

            LogUtil.d("链接解析结果：" + sb.toString());

            return bean;

        } catch (Exception e) {
            LogUtil.e("Exception", e);
            return null;
        }
    }

    /**
     * @param url
     * @return WebpageBean
     * @throws MalformedURLException
     * @throws IOException
     * @description 解析手机新闻客户端的链接
     * @author niuben
     */
    private static WebpageBean parsingQQUrl(String url) throws IOException {
        WebpageBean bean = new WebpageBean();

        Document linkDoc = Jsoup.parse(new URL(url), 5000);
        Elements contentEIs = linkDoc.select("div#content");
        String title = contentEIs.select("p.title").text();

        String description = contentEIs.select("div.summary").text();
        if (TextUtils.isEmpty(description)) {
            description = contentEIs.select("p.text").text();
        }

        Elements imgEls = contentEIs.select(JSOUP_SELECTOR_IMG);
        String imgUrl = null;
        if (imgEls != null && imgEls.size() > 0) {
            Element imgEl = imgEls.first();
            imgUrl = imgEl.hasAttr("src") ? imgEl.absUrl("src") : imgEl.absUrl("alt_src");
            LogUtil.d("imgUrl:" + imgUrl);
        }

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(description) && TextUtils.isEmpty(imgUrl)) {
            LogUtil.d("解析失败");
            return null;
        }
        bean.setTitle(TextUtils.isEmpty(title) ? "腾讯新闻" : title);
        bean.setDescription(description);
        bean.setImgUrl(imgUrl);

        LogUtil.d("解析QQ链接成功：title:" + bean.getTitle() + "|description:" + bean.getDescription() + "|imgUrl:" + bean.getImgUrl());
        return bean;
    }


    private static WebpageBean parsingWXUrl(String url) throws IOException {
        WebpageBean bean = new WebpageBean();

        Document linkDoc = Jsoup.parse(new URL(url), 5000);

        Elements scriptEls = linkDoc.body().select("script[type=\"text/javascript\"]");
        if (scriptEls != null && scriptEls.size() > 0) {
            for (int i = 0; i < scriptEls.size(); i++) {
                String scriptStr = scriptEls.get(i).html();
                if (!TextUtils.isEmpty(scriptStr)) {
                    if (scriptStr.contains("var msg_title")) {
                        String tempStr = scriptStr.substring(scriptStr.indexOf("var msg_title"));
                        String titleStr = tempStr.substring(tempStr.indexOf("\"") + 1, tempStr.indexOf("\";"));
                        LogUtil.d("title:" + titleStr);
                        bean.setTitle(titleStr);
                    }
                    if (scriptStr.contains("var msg_desc")) {
                        String tempStr = scriptStr.substring(scriptStr.indexOf("var msg_desc"));
                        String descStr = tempStr.substring(tempStr.indexOf("\"") + 1, tempStr.indexOf("\";"));
                        LogUtil.d("desc:" + descStr);
                        bean.setDescription(descStr);
                    }
                    if (scriptStr.contains("var msg_cdn_url")) {
                        String tempStr = scriptStr.substring(scriptStr.indexOf("var msg_cdn_url "));
                        String imgUrl = tempStr.substring(tempStr.indexOf("\"") + 1, tempStr.indexOf("\";"));
                        LogUtil.d("imgUrl:" + imgUrl);
                        bean.setImgUrl(imgUrl);
                    }
                    if (!TextUtils.isEmpty(bean.getTitle()) &&
                            !TextUtils.isEmpty(bean.getDescription()) &&
                            !TextUtils.isEmpty(bean.getImgUrl())) {
                        LogUtil.d("解析成功");
                        break;
                    }
                }
            }
        }

//        Element titleEl = linkDoc.select("head > title").first();
//        Element descEl = linkDoc.select("head > meta[name=\"description\"]").first();
//
//        if (titleEl != null) {
//            String title = titleEl.text();
//            bean.setTitle(title);
//        }
//        if (descEl != null) {
//            String desc = descEl.attr("content");
//            bean.setDescription(desc);
//        }
//
//        Elements imgEls = linkDoc.body().select("div.rich_media_content").select("img[data-src~=^(http[s]{0,1})://.+$(?<!\\.(?i)gif)]");
//        String imgUrl = null;
//        if (imgEls != null && imgEls.size() > 0) {
//            Element imgEl = imgEls.first();
//            imgUrl = imgEl.absUrl("data-src");
//            LogUtil.d("imgUrl:" + imgUrl);
//        }
//        bean.setImgUrl(imgUrl);
//
//        if (TextUtils.isEmpty(bean.getTitle()) && TextUtils.isEmpty(bean.getDescription()) && TextUtils.isEmpty(imgUrl)) {
//            LogUtil.d("解析失败");
//            return null;
//        }
//
//        LogUtil.d("解析微信链接成功：title:" + bean.getTitle() + "|description:" + bean.getDescription() + "|imgUrl:" + bean.getImgUrl());
        return bean;
    }

}
