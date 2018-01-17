package cn.redcdn.hvs.im.bean;

import java.io.Serializable;


/**
 * @ClassName: CallRecordBean
 * @author majj
 * @date 2013-12-23 上午10:50:37
 *
 */
public class CallRecordBean implements Serializable {

    private static final long serialVersionUID = -1238320866827592956L;

    private String contactUserId = "";
    private String nubeNumber = "";
    private String displayNumber;
    private String number = "";
    private String name = "";
    private String callDir = "";// 通话方向，0 ：来电未接，1：来电已接，2：去电
    private String callType = "";// 通话方式，0 ：ip电话 ，1：视频电话
    private String callTime = "";
    private String headUrl = "";
    private String lastTime = "";
    private String dataType = "0";//0:通话记录类型   1:nube好友类型  2:客服数据 3:PSTN电话记录
    private String isNew="0";//0：已读  1未读
    private String savedname="";// 从数据库中读取时，保存‘name’的值

    public CallRecordBean() {
        super();
    }

    public CallRecordBean(String contactUserId, String nubeNumber,
                          String number, String name, String callDir, String callType,
                          String callTime, String headUrl,String lastTime,String isNew) {
        super();
        this.contactUserId = contactUserId;
        this.nubeNumber = nubeNumber;
        this.number = number;
        if (name==null){
            this.name=nubeNumber;
        }else{
            this.name = name;
        }
        this.callDir = callDir;
        this.callType = callType;
        this.callTime = callTime;
        this.headUrl = headUrl;
        this.isNew=isNew;
    }

    public String getIsNew() {
        return this.isNew;
    }

    public void setIsNew(String isNew) {
        this.isNew=isNew;
    }


    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }


    public String getContactUserId() {
        return contactUserId;
    }

    public void setContactUserId(String contactUserId) {
        this.contactUserId = contactUserId;
    }

    public String getNubeNumber() {
        return nubeNumber;
    }

    public void setNubeNumber(String nubeNumber) {
        this.nubeNumber = nubeNumber;
    }

    public String getDisplayNumber(){
        if(null == displayNumber){
            displayNumber = ShowNameUtil.getShowNumber(ShowNameUtil.getNameElement("","",number,nubeNumber));
        }
        return displayNumber;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCallDir() {
        return callDir;
    }

    public void setCallDir(String callDir) {
        this.callDir = callDir;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }
    private String sex="1";//1：男     2：女

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }


    public String getSavedName() {
        return savedname;
    }

    public void setSavedName(String savedname) {
        this.savedname = savedname;
    }

    @Override
    public String toString() {
        return "contactUserId = " + contactUserId + ";name = " + name
            + ";nubeNumber = " + nubeNumber + ";sex = " + sex + ";number = " + number
            + ";callDir = " + callDir + ";calltype = " + callType
            + ";callTime = " + callTime + ";headUrl = " + headUrl
            + "; dataType = "+dataType;
    }

}
