package cn.redcdn.hvs.im.manager;

/**
 * Created by guoyx on 2017/2/25.
 */

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.im.bean.CollectionEntity;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.bean.WebpageBean;
import cn.redcdn.hvs.im.common.jsoup.WebpageFetcher;
import cn.redcdn.hvs.im.dao.CollectionDao;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.util.smileUtil.EmojiconTextView;
import cn.redcdn.hvs.im.view.LinkPressMovementMethod;
import cn.redcdn.hvs.im.view.TouchableSpan;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import com.butel.connectevent.utils.LogUtil;
import com.butel.connectevent.utils.NetWorkUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @ClassName: HtmlParseManager.java
 * @Description: 解析链接管理器
 */
public class HtmlParseManager {
    private static HtmlParseManager htmlParseManager;
    private CollectionDao mCollectionDao;
    private NoticesDao mNoticesDao;

    public static HtmlParseManager getInstance() {
        if (null == htmlParseManager) {
            htmlParseManager = new HtmlParseManager();
        }
        return htmlParseManager;
    }

    public HtmlParseManager() {
        mCollectionDao = new CollectionDao(MedicalApplication.getContext());
        mNoticesDao = new NoticesDao(MedicalApplication.getContext());
    }

    private Map<String, String> htmlParseMapNotices = new ConcurrentHashMap<String, String>();
    private static final int TYPE2NOTICES = 0;
    private Map<String, String> htmlParseMapCollection = new HashMap<String, String>();
    private static final int TYPE2COLLECTION = 1;

    public boolean parseHtmlToCollectionThread(final String uuid,
                                               final List<String> urls, String text) {
        return parseHtmlThread(uuid, urls, TYPE2COLLECTION, text);
    }

    public boolean parseHtmlToNoticesThread(final String uuid,
                                            final List<String> urls, String text) {
        return parseHtmlThread(uuid, urls, TYPE2NOTICES, text);
    }

    /**
     * parseHtmlThread 解析Html中title、description，获得首个图片的连接；
     * 将获得的结果，保存到数据库中对应uuid记录的body字段中（JSON对象） 在调用前需要判断网络是否可用；当无网络时，请不要使用该方法
     *
     * @param uuid
     *            消息的uuid
     * @param urls
     *            从分享的文本内容中提取的出的网页链接地址
     * @param text
     *            源文字
     */
    private boolean parseHtmlThread(final String uuid, final List<String> urls,
                                    int type, final String text) {
        LogUtil.d("parseHtmlThread");
        // 参数检查
        if (TextUtils.isEmpty(uuid) || urls == null) {
            LogUtil.d("参数检查 非法");
            return false;
        }

        if (!NetWorkUtil.isNetworkConnected(MedicalApplication.getContext())) {
            LogUtil.d("无网络连接，本次不做解析");
            return false;
        }

        if (type == TYPE2COLLECTION) {
            if (htmlParseMapCollection.containsKey(uuid)) {
                LogUtil.d("已处于parseHtml处理中");
                return false;
            }

            htmlParseMapCollection.put(uuid, "true");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    saveHtmlData2CollectionBody(uuid, getPageBeanList(urls, text));
                    htmlParseMapCollection.remove(uuid);
                }
            }).start();

        } else if (type == TYPE2NOTICES) {
            if (htmlParseMapNotices.containsKey(uuid)) {
                LogUtil.d("已处于parseHtml处理中 ");
                return false;
            }

            htmlParseMapNotices.put(uuid, "true");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    saveHtmlData2NoticesBody(uuid, getPageBeanList(urls, text));
                    htmlParseMapNotices.remove(uuid);
                }
            }).start();

        }

        return true;
    }

    private List<WebpageBean> getPageBeanList(List<String> urls, String text) {
        // 逐个解析链接
        List<WebpageBean> pageBeanList = new ArrayList<WebpageBean>();
        if (urls.size() > 0) {
            int length = urls.size();
            String url = null;
            String headerStr = null;
            String leftStr = text;
            for (int i = 0; i < length; i++) {
                url = urls.get(i);
                int index = leftStr.indexOf(url);
                if (index < 0) {
                    // if (i == length - 1) {
                    // // 如果最后一个链接，在原文里面不存在，则将前一个解析结果的footerStr设置为leftStr
                    // }
                    // 链接是从原文里面找出来的，此处index不会小于0
                    continue;
                }
                // 链接前文字
                headerStr = leftStr.substring(0, index);
                // 余下待处理文字
                leftStr = leftStr.substring(index + url.length());

                WebpageBean pageBean = WebpageFetcher.fetchWebpage(url);
                if (pageBean != null) {
                    pageBean.setSrcUrl(url);
                } else {
                    pageBean = new WebpageBean();
                    pageBean.setSrcUrl(url);
                }
                pageBean.setHeaderStr(headerStr);

                if (i == length - 1) {
                    // 最后一个链接
                    pageBean.setFooterStr(leftStr);
                }

                pageBeanList.add(pageBean);
            }
        }
        return pageBeanList;
    }

    public List<WebpageBean> convertWebpageBean(JSONArray pageData) {
        List<WebpageBean> beanList = null;
        WebpageBean bean = null;
        try {
            JSONObject obj = null;
            if (pageData != null) {
                beanList = new ArrayList<WebpageBean>();
                int length = pageData.length();
                for (int i = 0; i < length; i++) {
                    obj = pageData.getJSONObject(i);
                    bean = new WebpageBean();
                    bean.setSrcUrl(obj.optString("htmlStr"));
                    bean.setTitle(obj.optString("titleStr"));
                    bean.setDescription(obj.optString("desStr"));
                    bean.setImgUrl(obj.optString("imgUrl"));
                    bean.setHeaderStr(obj.optString("headStr"));
                    bean.setFooterStr(obj.optString("footStr"));
                    beanList.add(bean);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return beanList;
    }

    private void saveHtmlData2NoticesBody(String uuid,
                                          List<WebpageBean> pageBeanList) {
        if (TextUtils.isEmpty(uuid)) {
            return;
        }
        NoticesBean entity = mNoticesDao.getNoticeById(uuid);
        if (entity != null) {
            String body = entity.getBody();

            try {
                JSONArray bodyArray = new JSONArray(body);
                if (bodyArray != null && bodyArray.length() > 0) {
                    JSONObject firstItem = bodyArray.getJSONObject(0);
                    JSONObject obj = null;
                    WebpageBean item = null;
                    JSONArray pageData = new JSONArray();
                    if (pageBeanList != null) {
                        int length = pageBeanList.size();
                        for (int i = 0; i < length; i++) {
                            obj = new JSONObject();
                            item = pageBeanList.get(i);
                            obj.put("htmlStr", item.getSrcUrl());
                            obj.put("titleStr", item.getTitle());
                            obj.put("desStr", item.getDescription());
                            obj.put("imgUrl", item.getImgUrl());
                            obj.put("headStr", item.getHeaderStr());
                            obj.put("footStr", item.getFooterStr());

                            pageData.put(obj);
                        }
                        firstItem.put("webData", pageData);
                        // }else{
                        // firstItem.put("pageData", pageData);
                    }
                    LogUtil.d("webData" + pageData.toString());
                    bodyArray.put(0, firstItem);
                    mNoticesDao.updateNotice(uuid, bodyArray.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveHtmlData2CollectionBody(String uuid,
                                             List<WebpageBean> pageBeanList) {
        if (TextUtils.isEmpty(uuid)) {
            return;
        }
        CollectionEntity entity = mCollectionDao.getCollectionEntityById(uuid);
        if (entity != null) {
            String body = entity.getBody();

            try {
                JSONArray bodyArray = new JSONArray(body);
                if (bodyArray != null && bodyArray.length() > 0) {
                    JSONObject firstItem = bodyArray.getJSONObject(0);
                    JSONObject obj = null;
                    WebpageBean item = null;
                    JSONArray pageData = new JSONArray();
                    if (pageBeanList != null) {
                        int length = pageBeanList.size();
                        for (int i = 0; i < length; i++) {
                            obj = new JSONObject();
                            item = pageBeanList.get(i);
                            //                            obj.put("srcUrl", item.getSrcUrl());
                            //                            obj.put("title", item.getTitle());
                            //                            obj.put("description", item.getDescription());
                            //                            obj.put("imgUrl", item.getImgUrl());
                            obj.put("htmlStr", item.getSrcUrl());
                            obj.put("titleStr", item.getTitle());
                            obj.put("desStr", item.getDescription());
                            obj.put("imgUrl", item.getImgUrl());
                            obj.put("headStr", item.getHeaderStr());
                            obj.put("footStr", item.getFooterStr());

                            pageData.put(obj);
                        }
                        firstItem.put("webData", pageData);
                        // }else{
                        // firstItem.put("pageData", pageData);
                    }
                    LogUtil.d("webData" + pageData.toString());
                    bodyArray.put(0, firstItem);
                    mCollectionDao.upDateBodyById(uuid, bodyArray.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 修改显示规则，点击回调
     *
     * @param textView
     * @return
     */
    public void changeShowRules(final TextView textView, OnClickBack listener) {
        LogUtil.begin("");
        Spannable sp = (Spannable) textView.getText();
        URLSpan[] urlList = sp.getSpans(0, textView.getText().length(),
                URLSpan.class);
        SpannableStringBuilder style = new SpannableStringBuilder(
                textView.getText());
        style.clearSpans();// should clear old spans
        for (URLSpan url : urlList) {
            MyURLSpan myURLSpan = new MyURLSpan(
                    MedicalApplication.getContext(), url.getURL(),
                    url.getURL(), listener);
            myURLSpan.setPressedBgColor(0xff9ac57a);
            style.setSpan(myURLSpan, sp.getSpanStart(url), sp.getSpanEnd(url),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(style);
        textView.setMovementMethod(new LinkPressMovementMethod());
        LogUtil.end("");
    }

    public void changeShowRules(EmojiconTextView textView, OnClickBack listener) {
        LogUtil.begin("");
        Spannable sp = (Spannable) textView.getText();
        URLSpan[] urlList = sp.getSpans(0, textView.getText().length(),
                URLSpan.class);
        SpannableStringBuilder style = new SpannableStringBuilder(
                textView.getText());
        style.clearSpans();// should clear old spans
        for (URLSpan url : urlList) {
            MyURLSpan myURLSpan = new MyURLSpan(
                    MedicalApplication.getContext(), url.getURL(),
                    url.getURL(), listener);
            myURLSpan.setPressedBgColor(0xff9ac57a);
            style.setSpan(myURLSpan, sp.getSpanStart(url), sp.getSpanEnd(url),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(style);
        textView.setMovementMethod(new LinkPressMovementMethod());
        LogUtil.end("");
    }

    public List<String> getUrl(URLSpan[] spans) {
        List<String> urls = new ArrayList<String>();
        if (spans != null) {
            for (URLSpan item : spans) {
                String url = item.getURL();
                LogUtil.d(url);
                if (url.startsWith(MedicalMeetingManage.JMEETING_INVITE_URL)) {
                    LogUtil.d("url=" + url + "不解析");
                } else {
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    /**
     * 正则表达式获取文字中链接
     *
     * @param text
     * @return
     */
    public List<String> getUrls(String text) {
        LogUtil.d("获取文字链接:" + text);
        List<String> urls = new ArrayList<String>();
        // 匹配的条件选项为结束为空格(半角和全角)、换行符、字符串的结尾或者遇到其他格式的文本
        String regexp = "((http[s]{0,1}|ftp)://[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)|(www.[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)"; // 结束条件
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            LogUtil.d("获取到链接：" + matcher.group());
            urls.add(matcher.group());
        }
        return urls;
    }

    class MyURLSpan extends TouchableSpan {
        private OnClickBack mListener;
        private String mUrl;

        MyURLSpan(Context context, String str, String link, OnClickBack listener) {
            super(context, str, link);
            this.mUrl = link;
            mListener = listener;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(ds.linkColor);
            ds.setUnderlineText(true);
        }

        @Override
        public void onClick(View v) {
            mListener.OnClick(mUrl);
        }
    }

    public interface OnClickBack {
        void OnClick(String mUrl);
    }
}