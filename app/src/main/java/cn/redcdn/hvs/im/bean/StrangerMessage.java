package cn.redcdn.hvs.im.bean;

/**
 * Desc  陌生人信息
 * Created by caiguo on 2017/2/25.
 */

public class StrangerMessage {

    private int id;
    private String strangerNubeNumber;
    private String strangerHead;
    private String strangerName;
    private int msgDirection;//区别发送、接收  0|发送   1|接收
    private String msgContent;
    private String time;
    private int isRead;//0:未读 1：已读
    public static final int SEND = 0; //发送
    public static final int RECIEVE = 1; //接收
    public static final int HAS_READ = 1; //已读
    public static final int NOT_READ = 0; //未读
    public StrangerMessage() {}


    public StrangerMessage(int id, String strangerNubeNumber, String strangerHead,
                           String strangerName, int msgDirection, String msgContent, String time, int isRead) {
        this.id = id;
        this.strangerNubeNumber = strangerNubeNumber;
        this.strangerHead = strangerHead;
        this.strangerName = strangerName;
        this.msgDirection = msgDirection;
        this.msgContent = msgContent;
        this.time = time;
        this.isRead = isRead;
    }


    public StrangerMessage(String strangerNubeNumber, String strangerHead,
                           String strangerName, int msgDirection, String msgContent, String time, int isRead) {
        this.strangerNubeNumber = strangerNubeNumber;
        this.strangerHead = strangerHead;
        this.strangerName = strangerName;
        this.msgDirection = msgDirection;
        this.msgContent = msgContent;
        this.time = time;
        this.isRead = isRead;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getStrangerNubeNumber() {
        return strangerNubeNumber;
    }


    public void setStrangerNubeNumber(String strangerNubeNumber) {
        this.strangerNubeNumber = strangerNubeNumber;
    }


    public String getStrangerHead() {
        return strangerHead;
    }


    public void setStrangerHead(String strangerHead) {
        this.strangerHead = strangerHead;
    }


    public String getStrangerName() {
        return strangerName;
    }


    public void setStrangerName(String strangerName) {
        this.strangerName = strangerName;
    }


    public int getMsgDirection() {
        return msgDirection;
    }


    public void setMsgDirection(int msgDirection) {
        this.msgDirection = msgDirection;
    }


    public String getMsgContent() {
        return msgContent;
    }


    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }


    public String getTime() {
        return time;
    }


    public void setTime(String time) {
        this.time = time;
    }


    public int getIsRead() {
        return isRead;
    }


    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }


    @Override public String toString() {
        return "StrangerMessage{" +
            "id=" + id +
            ", strangerNubeNumber='" + strangerNubeNumber + '\'' +
            ", strangerHead='" + strangerHead + '\'' +
            ", strangerName='" + strangerName + '\'' +
            ", msgDirection=" + msgDirection +
            ", msgContent='" + msgContent + '\'' +
            ", time='" + time + '\'' +
            ", isRead=" + isRead +
            '}';
    }
}
