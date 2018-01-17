package cn.redcdn.hvs.meeting.Holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.redcdn.hvs.R;

/**
 * Created by Administrator on 2017/3/3 0003.
 */

public class MeetingListViewHolder extends RecyclerView.ViewHolder {
    public TextView meetingTopic, meetingId, creatTime ,fromtext,creator_from;
    public ImageView meetingType,hasPassWord;


    public MeetingListViewHolder(View view) {
        super(view);
        meetingTopic = (TextView) view.findViewById(R.id.meetinglist_item_topic);
        meetingId = (TextView) view.findViewById(R.id.meetinglist_item_meetingId);
        creatTime = (TextView) view.findViewById(R.id.meetinglist_item_time);
        meetingType = (ImageView) view.findViewById(R.id.meeting_type_icon);
        fromtext = (TextView) view.findViewById(R.id.item_from);
        creator_from = (TextView) view.findViewById(R.id.meeting_item_creator_from);
        hasPassWord= (ImageView) view.findViewById(R.id.meetinghaspasswordicon);
    }
}
