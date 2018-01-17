package cn.redcdn.hvs.meeting.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.meeting.Holder.MeetingListViewHolder;
import cn.redcdn.hvs.meeting.util.DateUtil;
import cn.redcdn.jmeetingsdk.MeetingItem;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Administrator on 2017/2/21 0021.
 */
public class MeetingListAdapter extends RecyclerView.Adapter<MeetingListViewHolder> implements View.OnClickListener {
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    private List<MeetingItem> mDatas;
    private Context mContext;


    public static interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, MeetingItem meetingItem);
    }


    public MeetingListAdapter(List<MeetingItem> mDatas, Context context) {
        this.mDatas = mDatas;
        this.mContext = context;
    }


    @Override
    public MeetingListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home, parent, false);
        MeetingListViewHolder vh = new MeetingListViewHolder(view);
        view.setOnClickListener(this);
        return vh;
    }


    @Override
    public void onBindViewHolder(MeetingListViewHolder holder, int position) {

        holder.meetingId.setText(mDatas.get(position).meetingId);
        holder.itemView.setTag(mDatas.get(position));
        //判断创建者是不是自己
        if (!AccountManager.getInstance(mContext).getAccountInfo().getNube().equals(mDatas.get(position).creatorId)) {
            holder.creator_from.setText(mDatas.get(position).creatorName);
        } else {
            holder.fromtext.setVisibility(View.INVISIBLE);
            holder.creator_from.setVisibility(View.INVISIBLE);
        }
        //meetingType   1|召开的会诊   2|预约的会诊
        if (mDatas.get(position).meetingType == 1) {
            if (mDatas.get(position).creatorId.equals(AccountManager.getInstance(mContext).getNube())) {
                holder.meetingTopic.setText(mContext.getString(R.string.my_consult_meeting_txt));
            } else {
                if (mDatas.get(position).topic.equals("")||holder.meetingTopic==null) {
                    holder.meetingTopic.setText(mDatas.get(position).creatorName + mContext.getString(R.string.my_consultation));
                } else {
                    holder.meetingTopic.setText(mDatas.get(position).topic);
                }
            }
            if (mDatas.get(position).hasMeetingPwd == 1) {
                holder.hasPassWord.setVisibility(View.VISIBLE);
            }else {
                holder.hasPassWord.setVisibility(View.INVISIBLE);
            }
            holder.fromtext.setVisibility(View.INVISIBLE);
            holder.creator_from.setVisibility(View.INVISIBLE);
            holder.meetingType.setVisibility(View.INVISIBLE);
            holder.creatTime.setTextColor(Color.parseColor("#2d2d2d"));
            holder.meetingTopic.setTextColor(Color.parseColor("#2d2d2d"));
        } else if (mDatas.get(position).meetingType == 2) {//预约的会议   判断是否为当天的会议显示  会议列表中的颜色
            if (mDatas.get(position).creatorId.equals(AccountManager.getInstance(mContext).getNube())) {
                holder.fromtext.setVisibility(View.INVISIBLE);
                holder.creator_from.setVisibility(View.INVISIBLE);
            } else {
                holder.fromtext.setVisibility(View.VISIBLE);
                holder.creator_from.setVisibility(View.VISIBLE);
            }
            if (mDatas.get(position).hasMeetingPwd == 1) {
                holder.hasPassWord.setVisibility(View.VISIBLE);
            }else {
                holder.hasPassWord.setVisibility(View.INVISIBLE);
            }
            holder.meetingType.setVisibility(View.VISIBLE);
            holder.creator_from.setText(mDatas.get(position).creatorName);
            if (mDatas.get(position).topic.equals("")) {
                holder.meetingTopic.setText(mDatas.get(position).creatorName + mContext.getString(R.string.my_consultation));
            } else {
                holder.meetingTopic.setText(mDatas.get(position).topic);
            }
            if (isToday(Long.valueOf(mDatas.get(position).createTime) * 1000)) {
                holder.creatTime.setTextColor(Color.parseColor("#2d2d2d"));
                holder.meetingTopic.setTextColor(Color.parseColor("#2d2d2d"));
            } else {
                holder.creatTime.setTextColor(Color.parseColor("#c7c7cd"));
                holder.meetingTopic.setTextColor(Color.parseColor("#c7c7cd"));
            }
        }
        holder.creatTime.setText(getDispTimestamp(Long.valueOf(mDatas.get(position).createTime) * 1000));

    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }


    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, (MeetingItem) v.getTag());
        }
    }


    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }


    private String getDispTimestamp(long dbTime) {
        String dateStr = DateUtil.formatMs2String(dbTime, "yyyy/MM/dd HH:mm");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dbTime);
        Calendar nowCal = Calendar.getInstance();
        // 此处优先级：跨年？年-月-日，当天？时-分，跨月？月-日，昨天？昨天，else:月-日
        if (cal.get(Calendar.YEAR) != nowCal.get(Calendar.YEAR)) {
            // 跨年了，此处应显示月、日
            return dateStr.substring(2, 10);
        } else {
            // realDateIntervalDay函数，求的是日期间隔，可能之前，可能之后
            int dayInterval = DateUtil.realDateIntervalDay(cal.getTime(),
                nowCal.getTime());
            if (dayInterval == 0) {
                // 当天（14:11）
                return dateStr.substring(11, 16);
            } else {
                //非当天，显示“月-日”
                return dateStr.substring(5, 10);
            }
        }
    }


    private boolean isToday(long dbTime) {
        String dateStr = DateUtil.formatMs2String(dbTime, "yyyy/MM/dd HH:mm");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dbTime);
        Calendar nowCal = Calendar.getInstance();
        // 此处优先级：跨年？年-月-日，当天？时-分，跨月？月-日，昨天？昨天，else:月-日
        if (cal.get(Calendar.YEAR) != nowCal.get(Calendar.YEAR)) {
            // 跨年了，此处应显示月、日
            return false;
        } else {
            // realDateIntervalDay函数，求的是日期间隔，可能之前，可能之后
            int dayInterval = DateUtil.realDateIntervalDay(cal.getTime(),
                nowCal.getTime());
            if (dayInterval == 0) {
                // 当天（14:11）
                return true;
            } else {
                //非当天，显示“月-日”
                return false;
            }
        }
    }
}
