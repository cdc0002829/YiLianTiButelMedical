package cn.redcdn.hvs.im.bean;

import java.io.Serializable;

import cn.redcdn.hvs.util.StringUtil;


/**
 * Desc  主要用于发现好友
 * Created by wangkai on 2017/2/25.
 */

public class NubeFriendBean implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String nickname; // 别名

    private String number; // 安全号码  只有在平板绑定了安全号才有此号码

    private String nubeNumber; // Nube号码

    private String headUrl; // 头像

    private String name;

    private String fullPym = "fullPym"; // 全拼

    private String contactUserId = "contactUserId"; // Nube号码ID

    private String uid;
    //sunjian add 20131023 start
    private String mobile;  //手机号码  PC端和手机上才会存在

    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    //sunjian add 20131023 end
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNumber() {
        if (!StringUtil.isEmpty(number) && StringUtil.isNumeric(number)){
            return number;
        } else if (!StringUtil.isEmpty(mobile)) {
            return mobile;
        } else {
            return "";
        }

    }

    //liujc add  2014-12-25 根据性别显示不同头像
    private String sex; // 性别
    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }
    public void setNumber(String number) {
        this.number = number;
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
    public String getFullPym() {
        return fullPym;
    }
    public void setFullPym(String fullPym) {
        this.fullPym = fullPym;
    }
    public String getContactUserId() {
        return contactUserId;
    }
    public void setContactUserId(String contactUserId) {
        this.contactUserId = contactUserId;
    }
}
