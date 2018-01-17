package cn.redcdn.hvs.im.adapter;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CursorAdapter;
import android.widget.TextView;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.bean.ShowNameUtil.NameElement;
import cn.redcdn.hvs.im.bean.ThreadsTempBean;
import cn.redcdn.hvs.im.bean.ThreadsTempTable;
import cn.redcdn.hvs.im.column.ThreadsTable;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.NetPhoneDaoImpl;
import cn.redcdn.hvs.im.preference.DaoPreference;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.view.SharePressableImageView;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.log.CustomLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.butel.connectevent.utils.LogUtil;
import java.util.Map;

public class ShareCursorAdapter extends CursorAdapter {

    private static final String TAG = "ConversationListCursorAdapter";
    private Context mContext;
    // Load ImageData
    private ViewHolder viewHolder;
    private LayoutInflater layoutInflater = null;
    // 屏幕宽度
    private int mScreentWidth;
    // 会话列表数据
    private Cursor threadsCursor = null;
    // 联系人名称
    // private Map<String, String> nubeNamesMap = null;
    // 刷新界面进度监听器
    private UIChangeListener uiChangeListener = null;
    // 数据变更监听
    private ThreadsContentObserver threadsObserver = null;
    private NoticesContentObserver noticesObserver = null;
    // 增加对群、群成员 数据库变更监听
    private GroupContentObserver groupObserver = null;
    private GroupMemberContentObserver groupMemberObserver = null;
    // 官方帐号（唯一）
    private String butelNubeNum;
    //服务号
    private String adminNubeNum;

    // 自身视讯号
    private String selfNubeNumber = "";

    private GroupDao groupDao;

    private ShareCallBack callback;

    public ShareCursorAdapter(Context context, Cursor c, int mScreenWidth) {
        super(context, c);
        this.mContext = context;
        this.layoutInflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater == null) {
            CustomLog.d(TAG, "layoutInflater  null");
        }
        this.mScreentWidth = mScreenWidth;

        groupDao = new GroupDao(context);

        if (threadsObserver == null) {

            threadsObserver = new ThreadsContentObserver();
            context.getContentResolver().registerContentObserver(
                ProviderConstant.NETPHONE_THREADS_URI, true,
                threadsObserver);
        }
        if (noticesObserver == null) {
            noticesObserver = new NoticesContentObserver();
            context.getContentResolver()
                .registerContentObserver(
                    ProviderConstant.NETPHONE_NOTICE_URI, true,
                    noticesObserver);
        }
        if (groupObserver == null) {
            groupObserver = new GroupContentObserver();
            context.getContentResolver().registerContentObserver(
                ProviderConstant.NETPHONE_GROUP_URI, true, groupObserver);
        }
        if (groupMemberObserver == null) {
            groupMemberObserver = new GroupMemberContentObserver();
            context.getContentResolver().registerContentObserver(
                ProviderConstant.NETPHONE_GROUP_MEMBER_URI, true,
                groupMemberObserver);
        }
        butelNubeNum = MedicalApplication.getPreference().getKeyValue(
            DaoPreference.PrefType.KEY_BUTEL_PUBLIC_NO, "");
        adminNubeNum = SettingData.getInstance().adminNubeNum;
    }

    public void changeCursor(Cursor newCursor) {
        Cursor oldCursor = this.threadsCursor;
        this.threadsCursor = newCursor;
        // this.nubeNamesMap = nubeNamesMap;
        // this.gid_gName=gid_gNames;
        // this.gid_memberNubeNames=gid_memberNubeNames;
        this.notifyDataSetChanged();
        if (oldCursor != null) {
            oldCursor.close();
        }
    }

    public void setUIChangeListener(UIChangeListener uiChangeListener) {
        this.uiChangeListener = uiChangeListener;
    }

    public interface UIChangeListener {
        public void onRefreshProgress();
    }

    @Override
    public int getCount() {
        int count = this.threadsCursor != null ? this.threadsCursor.getCount()
                                               : 0;
        return count;
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

        CustomLog.d(TAG, "getView position=" + position);

        threadsCursor.moveToPosition(position);

        // 注意：ThreadsTempBean中的name、nickName、nube、phone，只有name是从本地nube表中查询的；
        // 其他三项是从群组成员表中查询的。--主要原因：当群中最后一条消息不是本机发送时，需要显示发送者
        // --add on 2015/6/29
        final ThreadsTempBean bean = ThreadsTempTable.pureCursor(threadsCursor);

        if (convertView == null) {
            CustomLog.d(TAG, "view == null:" + position);
            convertView = layoutInflater.inflate(
                R.layout.share_convs_list_item, parent, false);
            viewHolder = createViewLine(convertView);
            LayoutParams lp = viewHolder.content.getLayoutParams();
            lp.width = mScreentWidth;
            convertView.setTag(viewHolder);
        } else {
            Object viewTag = convertView.getTag();
            viewHolder = (ViewHolder) viewTag;
        }

        final String threadsId = bean.getThreadsId();

        // 显示name
        // final int type;// 消息类型：单聊、群发
        final String reciever = bean.getRecipientIds();

        // 如果是官方消息，则设置该行高为0，使其不显示出来
        if (reciever.equals(butelNubeNum)||reciever.equals(adminNubeNum)) {
            viewHolder.content.setVisibility(View.GONE);
            viewHolder.divider.setVisibility(View.GONE);
        } else {
            viewHolder.content.setVisibility(View.VISIBLE);
            viewHolder.divider.setVisibility(View.VISIBLE);

        }
        if (position == getCount()-1) {
            viewHolder.line_bottom.setVisibility(View.VISIBLE);
            viewHolder.divider.setVisibility(View.GONE);
        } else {
            viewHolder.line_bottom.setVisibility(View.GONE);
            viewHolder.divider.setVisibility(View.VISIBLE);
        }

        // 会话类型：根据ThreadTable新增字段type判断：1-单聊；2：群聊，没有群发的概念了
        final int threadType = bean.getThreadType();

        // 头像不可点击
        viewHolder.contactIcon.pressableTextview.setVisibility(View.INVISIBLE);

        // 新需求：没有群发，增加了群聊,根据threadTable的type字段判断类型，群聊中RecipientIds为gid(群号)
        // add at 15/6/17
        if (ThreadsTable.TYPE_GROUP_CHAT == threadType) {
            // TODO:显示群聊名称：gName 或者 默认群名
            // 群聊时，reciver是gid

            String groupName = groupDao.getGroupNameByGid(bean
                .getRecipientIds());
            viewHolder.nameTxt.setText(groupName);

            // TODO:群聊时，类型与原来群发概念不同
            // type = ChatActivity.VALUE_CONVERSATION_TYPE_MULTI;
        } else {
            // 单人
            // 产品要求按照ShowNameUtil中的显示规则显示名字--add on 2015/6/29
            NameElement element = ShowNameUtil.getNameElement(reciever);
            String singleName = ShowNameUtil.getShowName(element);

            // 单聊的时候需要给chatActivity传递name--add 15/6/23
            viewHolder.nameTxt.setText(singleName);
            // type = ChatActivity.VALUE_CONVERSATION_TYPE_SINGLE;
        }

        viewHolder.content.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }

                if (reciever.equals(butelNubeNum)||reciever.equals(adminNubeNum)) {
                    // TODO:官方消息需要排除
                }
                // TODO:确认发送
                if (callback != null) {
                    String showName = "";
                    String headUrl = "";
                    if (ThreadsTable.TYPE_SINGLE_CHAT == threadType) {

                        NameElement element = ShowNameUtil
                            .getNameElement(reciever);
                        showName = ShowNameUtil.getShowName(element);
                        headUrl = bean.getHeadUrl();
                    }else {
                        showName = groupDao.getGroupNameByGid(bean
                                .getRecipientIds());
                        headUrl = bean.getgHeadUrl();
                    }

                    callback.sharePic(bean.getThreadsId(), reciever, showName,
                            headUrl, threadType);
                }
            }
        });

        if (reciever.equals(butelNubeNum)) {
            // 官方头像
            viewHolder.contactIcon.shareImageview
                .setImageResource(R.drawable.on_public_icon);
            String butelName = mContext.getResources().getString(
                R.string.str_butel_name);
            viewHolder.nameTxt.setText(butelName);
        } else {
            // 头像url
            String headerUrl = "";
            int headId= IMCommonUtil.getHeadIdBySex(new NetPhoneDaoImpl(mContext).getSexByNumber(reciever));
            if (ThreadsTable.TYPE_SINGLE_CHAT == threadType) {
                headId = R.drawable.head;
                headerUrl = bean.getHeadUrl();
            }else {
                headId=R.drawable.group_icon;
                headerUrl = bean.getgHeadUrl();
            }
            Glide.with(mContext)
                .load(headerUrl)
                .placeholder(headId)
                .error(headId).centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).crossFade()
                .into(viewHolder.contactIcon.shareImageview);
        }
        return convertView;
    }

    @Override
    public void bindView(View arg0, Context arg1, Cursor arg2) {

    }

    @Override
    public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
        return null;
    }

    public ViewHolder createViewLine(View parent) {
        LogUtil.begin("");
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.contactIcon = (SharePressableImageView) parent
            .findViewById(R.id.contact);
        viewHolder.nameTxt = (TextView) parent.findViewById(R.id.receiver);
        viewHolder.content = (View) parent.findViewById(R.id.reLayout);
        viewHolder.divider = parent.findViewById(R.id.divider);
        viewHolder.line_bottom = parent.findViewById(R.id.line_bottom);
        LogUtil.d("createContentLine  end");
        LogUtil.end("");

        return viewHolder;
    }

    private class ThreadsContentObserver extends ContentObserver {

        public ThreadsContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            LogUtil.d("t_threads 动态消息数据库数据发生变更");

            // 刷新界面显示
            if (uiChangeListener != null) {
                uiChangeListener.onRefreshProgress();
            }
        }
    }

    private class GroupMemberContentObserver extends ContentObserver {

        public GroupMemberContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            LogUtil.d("t_multi_chat_users 群成员数据库数据发生变更");

            // 刷新界面显示
            if (uiChangeListener != null) {
                uiChangeListener.onRefreshProgress();
            }
        }
    }

    private class GroupContentObserver extends ContentObserver {

        public GroupContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            LogUtil.d("t_multi_chat_groups 群聊数据库数据发生变更");

            // 刷新界面显示
            if (uiChangeListener != null) {
                uiChangeListener.onRefreshProgress();
            }
        }
    }

    private class NoticesContentObserver extends ContentObserver {

        public NoticesContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            LogUtil.d("t_notices 动态消息数据库数据发生变更");

            // 刷新界面显示
            if (uiChangeListener != null) {
                uiChangeListener.onRefreshProgress();
            }
        }
    }

    private class ViewHolder {
        View content;
        SharePressableImageView contactIcon;
        TextView nameTxt;
        View divider;
        View line_bottom;

    }

    private String getName(Map<String, String> nubeNamesMap, String nubeNum) {
        String name = "";
        if (nubeNamesMap != null) {
            name = nubeNamesMap.get(nubeNum);
        }

//        // add by zzwang : 添加弱化视讯号需求
//        if (TextUtils.isEmpty(name)) {
//            name = QueryPhoneNumberHelper.getPhoneNumberByNubeNumer(nubeNum,
//                false, null);
//            if (TextUtils.isEmpty(name)) {
//                return nubeNum;
//            }
//        }
        return name;
    }

    public void onDestoryView() {
        if (noticesObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(
                noticesObserver);
        }
        if (threadsCursor != null) {
            mContext.getContentResolver().unregisterContentObserver(
                threadsObserver);
        }
        if (groupObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(
                groupObserver);
        }
        if (groupMemberObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(
                groupMemberObserver);
        }
        if (threadsCursor != null) {
            try {
                threadsCursor.close();
                threadsCursor = null;
            } catch (Exception e) {
                LogUtil.e("noticeCursor.close()", e);
            }

        }
    }

    public void setShareCallBack(ShareCallBack callback) {
        this.callback = callback;
    }

    public interface ShareCallBack {
        public void sharePic(String threadId, String receiver, String receName,
                             String headUrl, int type);
    }

    /**
     * 是否是本端发送的消息
     */
    public boolean isSendNotice(String noticeSender) {
        if (selfNubeNumber.equals(noticeSender)) {
            return true;
        } else {
            return false;
        }
    }

    public void setSelfNubeNumber(String nubeNumber) {
        this.selfNubeNumber = nubeNumber;
    }

}
