package cn.redcdn.hvs.im.bean;

/**
 * Created by guoyx on 2017/2/25.
 */

import android.text.TextUtils;

public class WebpageBean {

    private String srcUrl;
    private String title;
    private String description;
    private String imgUrl;

    private String headerStr;
    private String footerStr;


    public String getSrcUrl() {
        return srcUrl;
    }


    public void setSrcUrl(String srcUrl) {
        this.srcUrl = srcUrl;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public String getImgUrl() {
        return imgUrl;
    }


    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }


    public boolean isValid() {
        if (!TextUtils.isEmpty(srcUrl)
            && (!TextUtils.isEmpty(title)
            || !TextUtils.isEmpty(description) || !TextUtils
            .isEmpty(imgUrl))) {
            return true;
        }
        return false;
    }


    public String getHeaderStr() {
        return headerStr;
    }


    public void setHeaderStr(String headerStr) {
        this.headerStr = headerStr;
    }


    public String getFooterStr() {
        return footerStr;
    }


    public void setFooterStr(String footerStr) {
        this.footerStr = footerStr;
    }
}
