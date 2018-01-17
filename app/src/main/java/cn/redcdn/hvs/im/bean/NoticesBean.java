package cn.redcdn.hvs.im.bean;

import java.io.Serializable;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class NoticesBean implements Serializable {

    private static final long serialVersionUID = -9032469266033690971L;
    /**
     * 系统生成唯一ID，uuid
     */
    private String id="";
    /**
     * 消息类型：见Filetask中的定义
     */
    private int type =-1;
    /**
     * 0:否；1:是
     */
    private int isNew = 0;
    /**
     * 0:否；1:是
     */
    private int isRead = 0;
    /**
     *
     */
    private String body ="";
    /**
     * 发送时间
     */
    private long sendTime=0L;
    /**
     * Nube号
     */
    private String sender ="";
    /**
     * Nube号 可以有多个
     */
    private String reciever ="";
    /**
     * 0：准备 1：正在进行 2：发送成功 3：失败
     */
    private int status =-1;
    /**
     * 接收时间
     */
    private long receivedTime=0L;
    /**
     * 简单的标题
     */
    private String title="";

    /**
     * 消息的ID：如果是发送的记录，该值和id项一致，在插入记录时产生
     * 如果是接收到的记录，该值是从发送的信息中带来的（extInfo中id）,如果没有则和id项一致
     */
    private String msgId="";

    /**
     * 扩展信息：含文本部分、ID及回复的消息记录
     */
    private String extInfo="";
    /**
     * 发送失败的回复msgid
     */
    private String failReplyId="";
    /**
     * 会话id
     */
    private String threadsId="";
    /**
     * 头像URL
     */
    private String headUrl = "";
    /**
     * 成员名称
     */
    private String memberName = "";
    /**
     * 成员昵称
     */
    private String mNickName ="";
    /**
     * 成员电话号码
     */
    private String mPhone = "";

    private String serverId = "";     //消息对应的 serverId
    private String reserverStr1 = ""; //预留字段

    public String getReserverStr1() {
        return reserverStr1;
    }

    public void setReserverStr1(String reserverStr1) {
        this.reserverStr1 = reserverStr1;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * 性别
     */
    private String sex="";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getIsNew() {
        return isNew;
    }

    public void setIsNew(int isNew) {
        this.isNew = isNew;
    }

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(long receivedTime) {
        this.receivedTime = receivedTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(String extInfo) {
        this.extInfo = extInfo;
    }

    public String getFailReplyId() {
        return failReplyId;
    }

    public void setFailReplyId(String failReplyId) {
        this.failReplyId = failReplyId;
    }

    public String getThreadsId() {
        return threadsId;
    }

    public void setThreadsId(String threadsId) {
        this.threadsId = threadsId;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getmNickName() {
        return mNickName;
    }

    public void setmNickName(String mNickName) {
        this.mNickName = mNickName;
    }

    public String getmPhone() {
        return mPhone;
    }

    public void setmPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
