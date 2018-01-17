package cn.redcdn.hvs.im.bean;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

public class ButelVcardBean {

    /**
     * butel名片的昵称
     */
    private String nickname="";
    /**
     * butel名片的视频号
     */
    private String nubeNumber="";
    /**
     * butel名片的头像链接
     */
    private String headUrl="";
    /**
     * butel名片的userid
     */
    private String userId="";
    /**
     * butel名片的电话号码
     */
    private String phoneNumber="";
    /**
     * butel名片的性别
     */
    private String sex="";

    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getNubeNumber() {
        return nubeNumber;
    }
    public void setNubeNumber(String nubeNumber) {
        this.nubeNumber = nubeNumber;
    }
    public String getHeadUrl() {
        return headUrl;
    }
    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }
}
