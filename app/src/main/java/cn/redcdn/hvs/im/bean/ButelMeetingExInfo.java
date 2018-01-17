package cn.redcdn.hvs.im.bean;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

public class ButelMeetingExInfo {
    /**
     * 会议邀请者的id,即视讯号
     */
    String inviterId = "";
    /**
     * 会议邀请者的name,即昵称
     */
    String inviterName ="";
    /**
     * 会议邀请者的头像链接
     */
    String inviterHeadUrl ="";
    /**
     * 会议邀请的房间号
     */
    String meetingRoom ="";
    /**
     * 会议邀请的链接
     */
    String meetingUrl ="";
    /**
     * 是否希望对方弹屏（会议邀请）
     */
    boolean showMeeting = true;


    public String getInviterId() {
        return inviterId;
    }
    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }
    public String getInviterName() {
        return inviterName;
    }
    public void setInviterName(String inviterName) {
        this.inviterName = inviterName;
    }
    public String getInviterHeadUrl() {
        return inviterHeadUrl;
    }
    public void setInviterHeadUrl(String inviterHeadUrl) {
        this.inviterHeadUrl = inviterHeadUrl;
    }
    public String getMeetingRoom() {
        return meetingRoom;
    }
    public void setMeetingRoom(String meetingRoom) {
        this.meetingRoom = meetingRoom;
    }
    public String getMeetingUrl() {
        return meetingUrl;
    }
    public void setMeetingUrl(String meetingUrl) {
        this.meetingUrl = meetingUrl;
    }
    public boolean isShowMeeting() {
        return showMeeting;
    }
    public void setShowMeeting(boolean showMeeting) {
        this.showMeeting = showMeeting;
    }
}
