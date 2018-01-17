package cn.redcdn.hvs.meeting.bean;

import java.io.Serializable;

/**
 * <dl>
 * <dt>BookMeetingExInfo.java</dt>
 * <dd>Description:预约会议信息</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 北京红云融通技术有限公司</dd>
 * <dd>CreateDate: 2016-3-7 14:36:34</dd>
 * </dl>
 * 
 * @author niuben
 */
public class BookMeetingExInfo implements Serializable {

	private static final long serialVersionUID = -1070466256750675710L;
	public static final String BOOK_MEETING_INFO="meetingInfo";//info 
	
	public static final String BOOK_NUBE="bookNube";//预约者nube(String)
	public static final String BOOK_NAME="bookName";//预约者名称(String)
	public static final String MEETING_ROOM="meetingRoom";//88888888(String)
	public static final String MEETING_TIME="meetingTime";//时间(long ms级别)
	public static final String MEETING_URL="meetingUrl";//链接(String)
	public static final String MEETING_THEME="meetingTheme";//主题(String)
	
	/**
	 * 会议预约者的id,即视讯号
	 */
	private String bookNube = "";
	/**
	 * 会议预约者的name,即昵称
	 */
	private String bookName ="";
	/**
	 * 会议预约的房间号
	 */
	private String meetingRoom ="";
	/**
	 * 会议预约的链接
	 */
	private String meetingUrl ="";
	
	/**
	 * 会议预约的时间 ms
	 */
	private long meetingTime=0;
	/**
	 * 会议预约主题
	 */
	private String meetingTheme="";
	
	public String getBookName() {
		return bookName;
	}
	public void setBookName(String bookName) {
		this.bookName = bookName;
	}
	
	public String getBookNube() {
		return bookNube;
	}
	public void setBookNube(String bookNube) {
		this.bookNube = bookNube;
	}
	
	public String getMeetingRoom() {
		return meetingRoom;
	}
	public void setMeetingRoom(String meetingRoom) {
		this.meetingRoom = meetingRoom;
	}
	
	public String getMeetingTheme() {
		return meetingTheme;
	}
	public void setMeetingTheme(String meetingTheme) {
		this.meetingTheme = meetingTheme;
	}
	
	public long getMeetingTime() {
		return meetingTime;
	}
	public void setMeetingTime(long meetingTime) {
		this.meetingTime = meetingTime;
	}
	
	public String getMeetingUrl() {
		return meetingUrl;
	}
	public void setMeetingUrl(String meetingUrl) {
		this.meetingUrl = meetingUrl;
	}
}
