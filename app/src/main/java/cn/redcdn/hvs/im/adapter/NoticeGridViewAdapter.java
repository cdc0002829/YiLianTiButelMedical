package cn.redcdn.hvs.im.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.activity.ChatInputFragment;
import cn.redcdn.hvs.im.util.ButelOvell;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.LogUtil;

import static cn.redcdn.hvs.util.CommonUtil.getString;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class NoticeGridViewAdapter extends BaseAdapter{

    private final String TAG = "NoticeGridViewAdapter";
    private LayoutInflater mInflater;
    public ImageView imageView;
    public TextView textView;
    public String string[];
    private ComentNoticeListener1 comentNoticeListener1 = null;
    private String nubeNumber = "";
    private boolean isSelectPic = true;
    private boolean isSelectVedio = true;
    private boolean isSelectCard = true;
    private boolean isSelectAudioCall = true;
    private boolean isSelectVedioCall = true;
    private boolean isSelectMeeting = true;


    public NoticeGridViewAdapter(String nube) {
        CustomLog.d(TAG,nube);
        nubeNumber = nube;
//		mContext = context;
        if (nube.length() < 12) {
            string = MedicalApplication.getContext().getResources()
                    .getStringArray(R.array.title_name);
        } else {
            string = MedicalApplication.getContext().getResources()
                    .getStringArray(R.array.title_name_mutil);
        }
        mInflater = (LayoutInflater) MedicalApplication.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(String nube) {

        CustomLog.d(TAG,"setData nube:" + nube);
        if (!TextUtils.isEmpty(nube) && nube.length() < 12) {
            string = MedicalApplication.getContext().getResources()
                    .getStringArray(R.array.title_name);
        } else {
            string = MedicalApplication.getContext().getResources()
                    .getStringArray(R.array.title_name_mutil);
        }
        isSelectPic = true;
        isSelectVedio = true;
        isSelectCard = true;
        isSelectMeeting = true;
        nubeNumber = nube;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return string.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.gridview_item, null);
        imageView = (ImageView) convertView.findViewById(R.id.attachment_gridview);
        textView = (TextView) convertView.findViewById(R.id.gridview_textview);
        textView.setText(string[position]);
        try {
            if (MedicalApplication.getContext().getString(R.string.photo).equals(string[position])) {
                if (nubeNumber.length() > 12
                    || ButelOvell.hasSendPicturesAbility(ButelOvell
                    .getNubeOvell(nubeNumber))) {
                    imageView.setImageResource(R.drawable.select_pic_btn0);
                } else {
                    imageView.setImageResource(R.drawable.select_pic_disabled);
                    textView.setTextColor(Color.parseColor("#d3d4d6"));
                    isSelectPic = false;
                }
            }else if(MedicalApplication.getContext().getString(R.string.contact_card_btn).equals(string[position])){
                if (nubeNumber.length() > 12
                        || ButelOvell.hasSendVedioAbility(ButelOvell
                        .getNubeOvell(nubeNumber))) {
                    imageView
                            .setImageResource(R.drawable.select_notice_vedio_call);
                } else {
                    isSelectVedio = false;
                    textView.setTextColor(Color.parseColor("#d3d4d6"));
                    imageView
                            .setImageResource(R.drawable.select_vedio_call_disabled);
                }
            }else if (MedicalApplication.getContext().getString(R.string.shoot).equals(string[position])) {
                if (nubeNumber.length() > 12
                    || ButelOvell.hasSendVedioAbility(ButelOvell
                    .getNubeOvell(nubeNumber))) {
                    imageView
                        .setImageResource(R.drawable.select_vedio_btn1);
                } else {
                    isSelectVedio = false;
                    textView.setTextColor(Color.parseColor("#d3d4d6"));
//                    imageView
//                        .setImageResource(R.drawable.select_vedio_disabled);
                }
            } else if(MedicalApplication.getContext().getString(R.string.my_collection).equals(string[position])){
                if (nubeNumber.length() > 12
                        || ButelOvell.hasSendVedioAbility(ButelOvell
                        .getNubeOvell(nubeNumber))) {
                    imageView
                            .setImageResource(R.drawable.select_collection_item);
                }
            }
//            if ("名片".equals(string[position])) {
//                if (nubeNumber.length() > 12
//                    || ButelOvell.hasSendCardAbility(ButelOvell
//                    .getNubeOvell(nubeNumber))) {
//                    imageView
//                        .setImageResource(R.drawable.select_vcard_btn2);
//                } else {
//                    isSelectCard = false;
//                    textView.setTextColor(Color.parseColor("#d3d4d6"));
//                    imageView
//                        .setImageResource(R.drawable.select_vcard_disabled);
//                }
//            }
            else if (MedicalApplication.getContext().getString(R.string.recording_video_littleVedio).equals(string[position])) {
                if (nubeNumber.length() > 12
                        || ButelOvell.hasSendCardAbility(ButelOvell
                        .getNubeOvell(nubeNumber))) {
                    imageView
                            .setImageResource(R.drawable.select_small_vedio_btn1);
                } else {
                    isSelectCard = false;
                    textView.setTextColor(Color.parseColor("#d3d4d6"));
                    imageView
                            .setImageResource(R.drawable.select_small_vedio_disabled);
                }

            } else if (MedicalApplication.getContext().getString(R.string.chat_vcard_title).equals(string[position])) {
                if (nubeNumber.length() > 12
                    || ButelOvell.hasSendCardAbility(ButelOvell
                    .getNubeOvell(nubeNumber))) {
                    imageView
                        .setImageResource(R.drawable.select_vcard_btn2);
                } else {
                    isSelectCard = false;
                    textView.setTextColor(Color.parseColor("#d3d4d6"));
                    imageView
                        .setImageResource(R.drawable.select_vcard_disabled);
                }
            }

//            else if ("视频电话".equals(string[position])) {
//                if (!ButelOvell.hasCallAbility(ButelOvell
//                    .getNubeOvell(nubeNumber))) {
//                    isSelectVedioCall = false;
//                    textView.setTextColor(Color.parseColor("#d3d4d6"));
//                    imageView
//                        .setImageResource(R.drawable.select_vedio_call_disabled);
//                } else {
//                    imageView
//                        .setImageResource(R.drawable.select_notice_vedio_call);
//                }
//
//            }
//            else {
//                if (nubeNumber.length() > 12
//                    || ButelOvell.hasMeetingAbility(ButelOvell
//                    .getNubeOvell(nubeNumber))) {
//                    imageView
//                        .setImageResource(R.drawable.select_notice_meeting);
//                } else {
//                    isSelectMeeting = false;
//                    textView.setTextColor(Color.parseColor("#d3d4d6"));
//                    imageView
//                        .setImageResource(R.drawable.select_meeting_disabled);
//                }
//            }
        } catch (OutOfMemoryError e) {
            LogUtil.e("OutOfMemoryError", e);
        }
        imageView.setTag(position);
        initOnclick();
        return convertView;
    }

    public void initOnclick() {
        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                int pos = (Integer) v.getTag();
                if (MedicalApplication.getContext().getString(R.string.photo).equals(string[pos])) {
                    if (isSelectPic) {
                        comentNoticeListener1
                            .sendType(ChatInputFragment.MSG_TYPE_PIC);
                    }
                } else if (MedicalApplication.getContext().getString(R.string.contact_card_btn).equals(string[pos])) {
                    comentNoticeListener1
                        .sendType(ChatInputFragment.MSG_TYPE_MEETING);
                }else if (MedicalApplication.getContext().getString(R.string.shoot).equals(string[pos])) {
                    if (!TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())){
                        CustomToast.show(MedicalApplication.getContext(), getString(R.string.is_video_wait_try), CustomToast.LENGTH_SHORT);
                        return;
                    }
                    if (isSelectVedio) {
                        comentNoticeListener1
                            .sendType(ChatInputFragment.MSG_TYPE_CAMERA);
                    }
                } else if (MedicalApplication.getContext().getString(R.string.my_collection).equals(string[pos])) {
                    if (isSelectCard) {
                        comentNoticeListener1
                            .sendType(ChatInputFragment.MSG_TYPE_COLLECTION);
                    }

                } else if (MedicalApplication.getContext().getString(R.string.recording_video_littleVedio).equals(string[pos])) {
                    if (!TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())){
                        CustomToast.show(MedicalApplication.getContext(), getString(R.string.is_video_wait_try), CustomToast.LENGTH_SHORT);
                        return;
                    }
                    if (isSelectAudioCall) {
                        comentNoticeListener1
                            .sendType(ChatInputFragment.MSG_TYPE_VIDEO);
                    }

                } else if (MedicalApplication.getContext().getString(R.string.chat_vcard_title).equals(string[pos])) {
                    if (isSelectVedioCall) {
                        comentNoticeListener1
                            .sendType(ChatInputFragment.MSG_TYPE_VCARD);
                    }
                }

            }
        });
    }


    public interface ComentNoticeListener1 {
        public void sendType(int type);
    }
    public void setComentNoticeListener1(
            ComentNoticeListener1 comentNoticeListener) {
        comentNoticeListener1 = comentNoticeListener;
    }
}
