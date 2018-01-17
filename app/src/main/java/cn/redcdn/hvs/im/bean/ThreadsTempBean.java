package cn.redcdn.hvs.im.bean;

/**
 * Created by guoyx on 2017/2/25.
 */

public class ThreadsTempBean {

    public String getThreadsId() {
        return threadsId;
    }

    public void setThreadsId(String threadsId) {
        this.threadsId = threadsId;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(String recipientIds) {
        this.recipientIds = recipientIds;
    }

    public String getNoticesId() {
        return noticesId;
    }

    public void setNoticesId(String noticesId) {
        this.noticesId = noticesId;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(long receivedTime) {
        this.receivedTime = receivedTime;
    }

    public long getSendTime(){
        return sendTime;
    }

    public void setSendTime(long sendTime){
        this.sendTime = sendTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(int type) {
        this.noticeType = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getIsNews() {
        return isNews;
    }

    public void setIsNews(int isNews) {
        this.isNews = isNews;
    }

    String threadsId;
    long lastTime;
    int threadType;//会话类型 1：单聊；2：群聊 add at 15/6/17
    public int getThreadType() {
        return threadType;
    }

    public void setThreadType(int threadType) {
        this.threadType = threadType;
    }

    String recipientIds;
    //如果群消息不是本机发送，需要显示发送者姓名
    String name;//该消息成员本地备注名
    String nickName;//该成员在本群的昵称
    String phoneNum;//该成员手机号
    String nubeNumber;//该成员nube号

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getNubeNumber() {
        return nubeNumber;
    }

    public void setNubeNumber(String nubeNumber) {
        this.nubeNumber = nubeNumber;
    }

    String noticesId;
    long receivedTime;
    long sendTime;
    int status;
    int noticeType;//消息内容类型：文字、语音、视频、名片...
    String body;
    int isNews;
    String extInfo;
    String headUrl;
    String sender;
    String gHeadUrl;
    String gName;
    String top;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(String extInfo) {
        this.extInfo = extInfo;
    }

    public String getgHeadUrl() {
        return gHeadUrl;
    }

    public void setgHeadUrl(String gHeadUrl) {
        this.gHeadUrl = gHeadUrl;
    }

    public String getgName() {
        return gName;
    }

    public void setgName(String gName) {
        this.gName = gName;
    }

    public void setTop(String top) {this.top = top;}
    public String getTop() {return top;}

    String doNotDisturb;

    public void setDoNotDisturb(String doNotDisturb){
        this.doNotDisturb = doNotDisturb;
    }

    public String getDoNotDisturb(){return doNotDisturb;}
}
