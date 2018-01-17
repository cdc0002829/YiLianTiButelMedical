package cn.redcdn.hvs.im.adapter; /**
 * <dl>
 * <dt>GroupChatMembersAdapter.java</dt>
 * <dd>Description:聊天成员适配器</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 北京红云融通技术有限公司</dd>
 * <dd>CreateDate: 2015-6-11 上午11:36:34</dd>
 * </dl>
 *
 * @author niuben
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.view.SharePressableImageView;
import cn.redcdn.log.CustomLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.butel.connectevent.utils.LogUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class GroupChatMembersIconAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private OnGroupChatMembersIconListener mListener;

    private String loginNubeNumber;
    private ArrayList<GroupMemberBean> mBeanList = new ArrayList<GroupMemberBean>();
    private boolean isSingle;
    private boolean isManger;//当前转户是否是本群群主
    private Context mContext;

    private static final String TAG = "GroupChatMembersIconAdapter";


    // /**
    //  *
    //  * @param _context
    //  * @param _imageFetcher
    //  * @param _chatType 聊天记录
    //  * @param _idNumber 单聊是Nube，群聊是gId
    //  * @param _listener
    //  */
    public GroupChatMembersIconAdapter(Context _context, OnGroupChatMembersIconListener _listener) {
        this.mContext = _context;
        this.inflater = LayoutInflater.from(_context);
        this.canRemoved = false;
        this.loginNubeNumber = AccountManager.getInstance(_context).getAccountInfo().nube;
        this.mListener = _listener;
    }


    private boolean canRemoved;//可移除标志


    public void setCanRemoved(boolean _canRemoved) {
        if (this.canRemoved != _canRemoved) {//不等时，才刷新页面，避免多次刷新
            this.canRemoved = _canRemoved;
            notifyDataSetChanged();
        }
    }


    // /**
    //  * @param _groupMemberList
    //  */
    public void setSingleData(LinkedHashMap<String, GroupMemberBean> _groupMemberMap) {
        LogUtil.d("单聊");
        mBeanList.clear();
        mBeanList.addAll(_groupMemberMap.values());
        isSingle = true;
        notifyDataSetChanged();
    }


    public void setGroupData(LinkedHashMap<String, GroupMemberBean> _groupMemberMap, String mgrNube) {
        mBeanList.clear();
        if (TextUtils.isEmpty(mgrNube)) {//无群主
            LogUtil.d("群人数" + _groupMemberMap.size() + "|当前群无群主");
            Iterator<Entry<String, GroupMemberBean>> iter = _groupMemberMap.entrySet().iterator();
            while (iter.hasNext()) {
                mBeanList.add(iter.next().getValue());
            }
            isManger = false;
        } else if (_groupMemberMap.containsKey(mgrNube)) {
            CustomLog.d(TAG, "群人数" + _groupMemberMap.size() + "|确保群主在第一个位置");
            mBeanList.add(_groupMemberMap.get(mgrNube));
            Iterator<Entry<String, GroupMemberBean>> iter = _groupMemberMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, GroupMemberBean> iter1 = iter.next();
                if (!(iter1.getKey()).equals(mgrNube)) {
                    mBeanList.add(iter1.getValue());
                }
            }
            if (loginNubeNumber.equals(mgrNube)) {
                isManger = true;
            } else {
                isManger = false;
            }
            CustomLog.d(TAG, "isManger=" + isManger);
        } else {                //一种特殊情况，显示当前群有群主，但是群成员里没有群主数据，故无法显示群成员头像
            CustomLog.d(TAG, "问题群主 id ：" + mgrNube);
            CustomLog.d(TAG, "显示当前群有群主，但是群成员里没有群主数据，故无法显示群成员头像");
            Iterator<Entry<String, GroupMemberBean>> iter = _groupMemberMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, GroupMemberBean> iter1 = iter.next();
                if (!(iter1.getKey()).equals(mgrNube)) {
                    mBeanList.add(iter1.getValue());
                }
            }
            if (loginNubeNumber.equals(mgrNube)) {
                isManger = true;
            } else {
                isManger = false;
            }

        }
        isSingle = false;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        int size = 0;
        if (isSingle) {
            size = 2;
        } else {
            if (mBeanList != null && (mBeanList.size() > 0)) {
                if (isManger) {// 当前账号是群主
                    if (mBeanList.size() == 1) {
                        size = mBeanList.size() + 1;// (+人，-人)
                    } else {
                        size = mBeanList.size() + 2;// (+人，-人)
                    }
                } else {
                    size = mBeanList.size() + 1;// (+人)
                }
            }
        }
        return size;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.multi_grid_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.photo_relativelayout = (RelativeLayout) convertView.findViewById(
                R.id.photo_relativelayout);
            viewHolder.photo = (SharePressableImageView) convertView.findViewById(
                R.id.group_chat_member_photo);
            viewHolder.name = (TextView) convertView.findViewById(R.id.group_chat_member_name);
            viewHolder.canRemovedTag = (ImageView) convertView.findViewById(
                R.id.group_chat_member_can_removed_tag);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position < mBeanList.size()) {
            showNormalMembers(position, viewHolder);
        } else {//+-人
            showAddOrRemoveIcon(position, viewHolder);
        }

        return convertView;
    }


    private void showAddOrRemoveIcon(final int _position, ViewHolder _viewHolder) {
        if (canRemoved) {
            _viewHolder.photo_relativelayout.setVisibility(View.GONE);
            _viewHolder.name.setVisibility(View.GONE);
        } else {
            _viewHolder.photo_relativelayout.setVisibility(View.VISIBLE);
            _viewHolder.canRemovedTag.setVisibility(View.INVISIBLE);
            _viewHolder.name.setVisibility(View.VISIBLE);
            if (_position == mBeanList.size()) {//添加
                Glide.with(this.mContext)
                    .load(R.drawable.group_add)
                    .placeholder(R.drawable.group_add)
                    .into(_viewHolder.photo.shareImageview);
                _viewHolder.name.setText("");
            } else if (_position == mBeanList.size() + 1) {//移除
                Glide.with(this.mContext)
                    .load(R.drawable.group_dele)
                    .placeholder(R.drawable.group_dele)
                    .into(_viewHolder.photo.shareImageview);
                _viewHolder.name.setText("");
            }

            _viewHolder.photo.pressableTextview.setVisibility(View.VISIBLE);
            _viewHolder.photo.pressableTextview.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (_position == mBeanList.size()) {//添加
                        mListener.onAddMember();
                    } else if (_position == mBeanList.size() + 1) {//可删除标志
                        canRemoved = true;
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }


    private void showNormalMembers(final int _position, ViewHolder _viewHolder) {
        //清空标志
        _viewHolder.photo_relativelayout.setVisibility(View.VISIBLE);
        _viewHolder.name.setVisibility(View.VISIBLE);
        //删除标志(群主没有删除标志)
        if (canRemoved && (_position != 0)) {
            _viewHolder.canRemovedTag.setVisibility(View.VISIBLE);
        } else {
            _viewHolder.canRemovedTag.setVisibility(View.INVISIBLE);
        }

        if (isCustomService(_position)) {
            _viewHolder.name.setText(R.string.video_custom_service);
        } else {
            _viewHolder.name.setText(mBeanList.get(_position).getDispName());
        }

        if (!TextUtils.isEmpty(mBeanList.get(_position).getHeadUrl())) {
            if (isCustomService(_position)) {
                _viewHolder.photo.shareImageview.setImageResource(R.drawable.contact_customservice);
            } else {
                Glide.with(this.mContext)
                    .load(mBeanList.get(_position).getHeadUrl())
                    .placeholder(
                        IMCommonUtil.getHeadIdBySex(mBeanList.get(_position).getGender() + ""))
                    .error(IMCommonUtil.getHeadIdBySex(mBeanList.get(_position).getGender() + ""))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .crossFade()
                    .into(_viewHolder.photo.shareImageview);
            }
        } else {
            // 设置默认头像
            Glide.with(this.mContext)
                .load(IMCommonUtil.getHeadIdBySex(mBeanList.get(_position).getGender() + ""))
                .placeholder(IMCommonUtil.getHeadIdBySex(mBeanList.get(_position).getGender() + ""))
                .into(_viewHolder.photo.shareImageview);
        }
        // 头像点击，进入个人名片页面
        _viewHolder.photo.pressableTextview.setVisibility(View.VISIBLE);
        _viewHolder.photo.pressableTextview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canRemoved) {
                    if (_position != 0) {
                        mListener.onRemovedMember(mBeanList.get(_position).getNubeNum());
                    }
                } else {
                    if (!loginNubeNumber.equals(mBeanList.get(_position).getNubeNum())) {
                        mListener.onJumoToContactDetail(mBeanList.get(_position).getNubeNum());
                    }
                }
            }
        });
        _viewHolder.canRemovedTag.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canRemoved) {
                    if (_position != 0) {
                        mListener.onRemovedMember(mBeanList.get(_position).getNubeNum());
                    }
                } else {
                    if (!loginNubeNumber.equals(mBeanList.get(_position).getNubeNum())) {
                        mListener.onJumoToContactDetail(mBeanList.get(_position).getNubeNum());
                    }
                }
            }
        });
    }


    /**
     * 如果当前成员是视频客服，显示默认名称
     *
     * @param _position GridView 位置
     */
    private boolean isCustomService(int _position) {
        return mBeanList.get(_position)
            .getHeadUrl()
            .equals("CustomService");
    }


    static class ViewHolder {
        public RelativeLayout photo_relativelayout;
        public SharePressableImageView photo;
        public TextView name;
        public ImageView canRemovedTag;
    }


    public interface OnGroupChatMembersIconListener {
        /**
         * @Title: onAddMember
         * @Description: 添加联系人
         */
        public void onAddMember();

        /**
         * @param nubeNumber 视讯号
         * @Title: onRemovedMember
         * @Description: 移除联系人
         */
        public void onRemovedMember(String nubeNumber);

        /**
         * @param nubeNumber 视讯号
         * @Title: onJumoToContactDetail
         * @Description: 跳转到联系人详情页面
         */
        public void onJumoToContactDetail(String nubeNumber);

    }
}