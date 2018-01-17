package cn.redcdn.hvs.meeting.bean;

public class MeetHisBean {

	private int id;
	private String meetid;
	private long createTime;
	private int meetStatus;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMeetID() {
		return meetid;
	}

	public void setMeetID(String keyStr) {
		this.meetid = keyStr;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	public int getMeetStatus(){
		return meetStatus;
	}
	
	public void setMeetStatus(int status){
		this.meetStatus = status;
	}
}
