package cn.redcdn.hvs.head.javabean;

/**
 * Created by Administrator on 2017/7/27.
 */

public class OfficialAccountsBean {
    private String picUrl;
    private String information;
    private String OffaccId;

    public OfficialAccountsBean(String picUrl, String information, String offaccId) {
        this.picUrl = picUrl;
        this.information = information;
        OffaccId = offaccId;
    }

    public String getOffaccId() {

        return OffaccId;
    }

    public void setOffaccId(String offaccId) {
        OffaccId = offaccId;
    }


    public OfficialAccountsBean(String picUrl, String information) {
        this.picUrl = picUrl;
        this.information = information;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }
}
