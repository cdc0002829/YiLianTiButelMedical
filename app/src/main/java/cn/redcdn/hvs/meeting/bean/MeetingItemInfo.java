package cn.redcdn.hvs.meeting.bean;

import java.io.Serializable;

import cn.redcdn.jmeetingsdk.MeetingInfo;
import cn.redcdn.jmeetingsdk.MeetingItem;

public class MeetingItemInfo implements Serializable,Comparable<MeetingItemInfo>{
	
	public String mMeetingTime;  //attention:不是毫秒级别的，每次使用时需要自乘1000
	public String mMeetingNumber;
	public String mCreatorName;
	public String mCreatorId;
	public int mMeetType; //1:实时会议 2:预约会议
	public String mTopic;
	
	
	private final static String FORMAT = "MeetingItemInfo: meetingTime=%s,meetingNumber=%s,creatorName=%s,creatorId=%s,meetType=%s,topic=%s";
	
	public MeetingItemInfo(MeetingItem item){
		if(null != item){
			mMeetingTime = item.createTime;
			mMeetingNumber = item.meetingId;
			mCreatorName = item.creatorName;
			mCreatorId = item.creatorId;
			mMeetType = item.meetingType;
			mTopic = item.topic;
		}
	}
	
	public MeetingItemInfo(MeetingInfo info){
		if(null != info){
			mMeetingNumber = info.meetingId;
			mCreatorId = info.creatorId;
		}
	}

	@Override
	public String toString() {
		return String.format(FORMAT, mMeetingTime,mMeetingNumber,mCreatorName,mCreatorId,mMeetType,mTopic);
	}

	@Override
	public int compareTo(MeetingItemInfo another) {
		if(this.mMeetType == another.mMeetType){
			if(this.mMeetType == 2){
				//预约会议，升序排列
				return Long.valueOf(this.mMeetingTime).compareTo(Long.valueOf(another.mMeetingTime));
			}else{
				//即时会议，降序排列
				return Long.valueOf(another.mMeetingTime).compareTo(Long.valueOf(this.mMeetingTime));
				
			}
		}else{
			return this.mMeetType>another.mMeetType? 1:-1;
		}
	}
}
