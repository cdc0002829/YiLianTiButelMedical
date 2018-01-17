package cn.redcdn.hvs.im.adapter;

import android.widget.BaseAdapter;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.bean.ShowNameUtil.NameElement;
import cn.redcdn.hvs.im.column.NubeFriendColumn;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.view.SharePressableImageView;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/3/1.
 */

public class SelectGroupMemberAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<GroupMemberBean> memberBeanMap = new ArrayList<GroupMemberBean>();
    private Context context;
    private ViewHolder viewHolder;
    // 屏幕宽度
    private int mScreentWidth;

    private String mySelfNubeNum = "";
    private static final String TAG = "SelectGroupMemberAdapter";
    private SelectCallBack callback;

    public SelectGroupMemberAdapter(Context contxt,
                                    ArrayList<GroupMemberBean> memberBeanMap,
                                    int mScreentWidth) {

        this.memberBeanMap = memberBeanMap;
        this.context = contxt;
        this.mScreentWidth = mScreentWidth;
        this.mySelfNubeNum = AccountManager.getInstance(contxt).getNube();
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (memberBeanMap == null) {
            return 0;
        } else {
            return memberBeanMap.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return memberBeanMap.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void appendPageData(
            ArrayList<GroupMemberBean> mberBeanMap) {
        if (memberBeanMap == null) {
            memberBeanMap = new ArrayList<GroupMemberBean>();
        }
        memberBeanMap = mberBeanMap;
        notifyDataSetChanged();
    }

    public void setCallBack(SelectCallBack callback){
        this.callback = callback;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CustomLog.d(TAG,"getView position=" + position+"nube号："
                + memberBeanMap.get(position).getNubeNum()
                + memberBeanMap.get(position).getHeadUrl());
        Log.d("chencj",
                "getView position=" + position+"位置："
                        + memberBeanMap.get(position).getNubeNum()
                        + memberBeanMap.get(position).getHeadUrl() + "性别："
                        + memberBeanMap.get(position).getGender());
        if (convertView == null) {
            convertView = layoutInflater.inflate(
                    R.layout.select_groupmember_item, null);
            viewHolder = createViewLine(convertView);
            LayoutParams lp = viewHolder.content.getLayoutParams();
            lp.width = mScreentWidth;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (TextUtils.isEmpty(memberBeanMap.get(position).getHeadUrl())) {
            viewHolder.contactIcon.shareImageview.setImageResource(IMCommonUtil.getHeadIdBySex(memberBeanMap.get(position).getGender() + ""));
        } else {
            Glide.with(convertView.getContext())
                    .load(memberBeanMap.get(position).getHeadUrl())
                    .placeholder(IMCommonUtil.getHeadIdBySex(memberBeanMap.get(position).getGender() + ""))
                    .error(IMCommonUtil.getHeadIdBySex(memberBeanMap.get(position).getGender() + "")).centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).crossFade()
                    .into(viewHolder.contactIcon.shareImageview);
        }
        if (memberBeanMap.get(position).getGender() == NubeFriendColumn.SEX_FEMALE) {
//            viewHolder.nameTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0,
//                    R.drawable.sex_female, 0);
        } else if (memberBeanMap.get(position).getGender() == NubeFriendColumn.SEX_FEMALE) {

//            viewHolder.nameTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0,
//                    R.drawable.sex_male, 0);
        }

        NameElement element = ShowNameUtil.getNameElement(
                memberBeanMap.get(position).getName(),
                memberBeanMap.get(position).getNickName(),
                memberBeanMap.get(position).getPhoneNum(),
                memberBeanMap.get(position).getNubeNum());
        final String MName = ShowNameUtil.getShowName(element);
        viewHolder.nameTxt.setText(MName);
        viewHolder.item_layout.setTag(position);
        viewHolder.contactIcon.pressableTextview.setTag(position);
        viewHolder.item_layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CustomLog.d(TAG,"OnClickListener点击选择的联系：");
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                final int pos = (Integer) v.getTag();
                callback.selectMember(memberBeanMap.get(pos).getNubeNum(),
                        MName);

//                Intent intent = new Intent(context, ChatActivity.class);
//                intent.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
//                        ChatActivity.VALUE_NOTICE_FRAME_TYPE_NUBE);
//                intent.putExtra(ChatActivity.KEY_CONVERSATION_NUBES,
//                        memberBeanMap.get(pos).getNubeNum());
//                intent.putExtra(ChatActivity.KEY_CONVERSATION_SHORTNAME,
//                        memberBeanMap.get(pos).getNickName());
//                context.startActivity(intent);
            }
        });
        viewHolder.contactIcon.pressableTextview.setVisibility(View.VISIBLE);
        viewHolder.contactIcon.pressableTextview
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (CommonUtil.isFastDoubleClick()) {
                            return;
                        }
//                        final int pos = (Integer) v.getTag();
//                        Intent intent = new Intent(context, ButelContactDetailActivity.class);
//                        intent.putExtra(ButelContactDetailActivity.KEY_NUBE_NUMBER,memberBeanMap.get(pos).getNubeNum());
//                        context.startActivity(intent);
                    }
                });
        return convertView;
    }

    public ViewHolder createViewLine(View parent) {

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.item_layout = (RelativeLayout) parent
                .findViewById(R.id.memeber_Layout);
        viewHolder.contactIcon = (SharePressableImageView) parent
                .findViewById(R.id.member_contact);
        viewHolder.nameTxt = (TextView) parent.findViewById(R.id.groupmember);
        viewHolder.content = (View) parent.findViewById(R.id.memeber_Layout);
        return viewHolder;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        CustomLog.d(TAG,"notifyDataSetChanged");
    }

    private class ViewHolder {
        RelativeLayout item_layout;
        View content;
        SharePressableImageView contactIcon;
        TextView nameTxt;
    }

    public interface SelectCallBack {
        public void selectMember(String nubeNumder, String showName);
    }
}
