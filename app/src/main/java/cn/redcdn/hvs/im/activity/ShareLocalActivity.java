package cn.redcdn.hvs.im.activity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.butel.connectevent.utils.LogUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.redcdn.datacenter.collectcenter.DataBodyInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.UrlConstant;
import cn.redcdn.hvs.im.adapter.ShareCursorAdapter;
import cn.redcdn.hvs.im.agent.AppP2PAgentManager;
import cn.redcdn.hvs.im.asyncTask.QueryNoticeAsyncTask;
import cn.redcdn.hvs.im.bean.ButelPAVExInfo;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.column.ThreadsTable;
import cn.redcdn.hvs.im.common.CommonWaitDialog;
import cn.redcdn.hvs.im.common.ThreadPoolManger;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.NetPhoneDaoImpl;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.manager.FileManager;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.hvs.im.manager.GroupChatInterfaceManager;
import cn.redcdn.hvs.im.preference.DaoPreference;
import cn.redcdn.hvs.im.util.ButelOvell;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.util.MediaFile;
import cn.redcdn.hvs.im.view.CommonDialog;
import cn.redcdn.hvs.im.view.SharePressableImageView;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

import static android.graphics.Bitmap.createBitmap;

/**
 * <dl>
 * <dt>NewFriendActivity.java</dt>
 * <dd>Description:本地照片分享界面</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2015-8-20 下午2:26:44</dd>
 * <dd>modify:消息转发使用该页面--2015-8-24</dd>
 * <dd>modify:修改消息转发界面跳转，回到当前聊天页面--2015-9-9</dd>
 * </dl>
 *
 * @author wxy
 */
public class ShareLocalActivity extends BaseActivity {

    public final static int BACK_FROM_SELECT_LINK = 1;

    /** 消息转发 */
    private static boolean mIsForward = false;
    public static final String KEY_ACTION_FORWARD = "is_forward";
    public static final String MSG_ID = "msg_id";
    public static final String FORWARD_TYPE = "forward_type";
    //TODO:从消息页面批量操作转发时，传入的msgId为多个id,用逗号分割；
    // 在使用时需要重新拆解。
    private static String msgId = null;

    private RelativeLayout shareFromLinks;
    private ListView convsList;
    // 动态消息列表适配器
    private ShareCursorAdapter adapter = null;
    // 自身视讯号
    private String selfNubeNumber = "";

    // 分享类型（图片或视频）
    private int shareType = 0;
    // 图片
    public static final int CHOOSER_TYPE_IMAGE = 1;
    // 视频
    public static final int CHOOSER_TYPE_VIDEO = 2;
    // 同时包含 图片、视频
    public static final int CHOOSER_TYPE_ALL = 3;

    public static final String SHARE_TYPE = "share_type";

    // 分享文件本地路径
    private List<String> mListPhotoPath = null;

    private SharePressableImageView contacts;

    private NoticesDao noticeDao = null;
    private GroupDao groupDao = null;
    // 存放群成员人数
    private int groupMemberSize = 0;
    private String groupName = "";

    /** 建群时，选择联系人键值对 <nube,name> */
    private Map<String, String> receiverNameMap = new HashMap<String, String>();
    /** 收件人nube号 */
    private ArrayList<String> receiverNumberLst = new ArrayList<String>();
    private GroupChatInterfaceManager groupChatInterfaceManager;
    // 关闭本页面标志位--如果是:从联系人列表-->选择联系人页面-->返回“选择页面”，在onStop()里不销毁本页面
    private boolean finishFlag = true;

    // 不同的转发入口标记，0表示消息页面的转发，1表示收藏页面的转发  2:文章类型转发
    private int shareFlag = 0;
    public static final String KEY_COLLECTION_FORWARD = "key_collection_forward";
    // 标记收藏功能，图片转发的具体位置
    public static final String KEY_COLLECTION_FORWARD_POS = "key_collection_forward_pos";
    private int pos = -1;

    public static final String KEY_COLLECTION_ITEM_INFO = "key_collection_item_info";
    private DataBodyInfo collectItemInfo = null;
    private DataBodyInfo articleInfo = null;

    private int forwradType = 0;//转发类型 0：逐条转发  1：合并转发

    private String chatPicVideoPath="";//从聊天消息转发的图片或视频的路径
    private int chatForwardType=-1;//转发的消息类型  现定义：-2位 逐条、合并消息的转发
    Intent chatForwardIntent;//聊天信息转发的intent
    private CommonDialog conDlg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_local_image);

        noticeDao = new NoticesDao(this);
        groupDao = new GroupDao(this);

        shareFromLinks = (RelativeLayout) findViewById(R.id.select_from_list);
        shareFromLinks.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO:极会议版本-转发消息时对用户设备做了限制：
                // a,转发文字/视频，只转发给手机用户，过滤掉M1/N8J/X1/N7/N8用户
                // b,转发图片/名片，只转发给手机/N7/N8用户，过滤掉M1/N8J/X1用户

                // 跳转到“选择联系人”
                // 置标志位：false，不销毁 选择 页，在选择联系人页面可以通过backBtn键返回
                finishFlag = false;

                Intent i = new Intent(ShareLocalActivity.this,
                    SelectLinkManActivity.class);
                // 区分：a.转发；b.分享
                if (mIsForward) {
                    i.putExtra(SelectLinkManActivity.ACTIVTY_PURPOSE,
                        SelectLinkManActivity.MSG_FORWARD);
                    // 转发时，被过滤的设备不显示在列表中
                    i.putExtra(MSG_ID, msgId);
                } else {
                    i.putExtra(SelectLinkManActivity.ACTIVTY_PURPOSE,
                        SelectLinkManActivity.SHARE_PIC);
                    // 本地分享，设备不支持时，选中联系人时，弹出Toast:该设备不支持发送图片/视频。
                    i.putExtra(SHARE_TYPE, getShareType());
                }
                i.putExtra(SelectLinkManActivity.ACTIVITY_FLAG,
                    SelectLinkManActivity.AVITVITY_START_FOR_RESULT);
                i.putExtra(SelectLinkManActivity.KEY_IS_SIGNAL_SELECT, true);
                i.putStringArrayListExtra(
                    SelectLinkManActivity.KEY_SELECTED_NUBENUMBERS,
                    new ArrayList<String>());
                i.putExtra(KEY_COLLECTION_FORWARD, shareFlag);
                i.putExtras(getIntent().getExtras());
                i.putExtra("chatForwardType",chatForwardType);
                i.putExtra(FORWARD_TYPE,forwradType);
                if(shareFlag == 1){
                    i.putExtra("collectItemInfo",collectItemInfo);
                }else if(shareFlag == 2){
                    i.putExtra("articleTitle",articleInfo.getTitle());
                }


                startActivityForResult(i, BACK_FROM_SELECT_LINK);

            }
        });
        contacts = (SharePressableImageView) findViewById(R.id.contacts);
//        contacts.pressableTextview.setVisibility(View.INVISIBLE);
        contacts.shareImageview.setImageResource(R.drawable.select_icon_normal);
        convsList = (ListView) findViewById(R.id.conversation_list);
        convsList.setDivider(null);
        convsList.setDividerHeight(0);

        getTitleBar().setTitle(getString(R.string.select_linkMan));

        String ownNumber = MedicalApplication.getPreference().getKeyValue(
            DaoPreference.PrefType.LOGIN_NUBENUMBER, "");
        String token = AccountManager.getInstance(getApplicationContext())
            .getAccountInfo().accessToken;


        // 接收数据等
        initParams();

        initControl();

        AppP2PAgentManager.getInstance().setImMsgResultListener(new AppP2PAgentManager.ImMsgResultInterface() {
            @Override
            public void onSuccess(String uuid) {
                CustomToast.show(ShareLocalActivity.this,getString(R.string.toast_sent),CustomToast.LENGTH_SHORT);
                finish();
            }

            @Override
            public void onFailed(String uuid) {
                CustomToast.show(ShareLocalActivity.this,getString(R.string.net_error_try_again),CustomToast.LENGTH_SHORT);
                finish();
            }

            @Override
            public void onFinalResult(boolean isSuccess, String uuid) {
                CustomLog.d(TAG,"发送结果:" + isSuccess + "msgUUID:" + uuid);
            }
        });

    }

    private void initControl() {
        selfNubeNumber = MedicalApplication.getPreference().getKeyValue(
            DaoPreference.PrefType.LOGIN_NUBENUMBER, "");

        int mScreenWidth = IMCommonUtil.getDeviceSize(this).x;
        adapter = new ShareCursorAdapter(this, null, mScreenWidth);
        // 初始化，清空cursor
        adapter.changeCursor(null);

        adapter.setSelfNubeNumber(selfNubeNumber);

        adapter.setShareCallBack(new ShareCursorAdapter.ShareCallBack() {

            @Override
            public void sharePic(final String threadId, final String receiver,
                                 final String showName, final String headUrl,
                                 final int threadType) {
                // TODO:极会议版本需要对设备类型进行过滤
                if (!ShareLocalActivity.mIsForward) {
                    byte ovell = ButelOvell.getNubeOvell(receiver);
                    // 分享时，对设备类型进行过滤
                    if (getShareType() == CHOOSER_TYPE_IMAGE) {
                        // 图片分享：
                        if (!ButelOvell.hasSendPicturesAbility(ovell)) {

                            Toast.makeText(ShareLocalActivity.this,
                                R.string.this_equipment_cannot_share_picture, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else if (getShareType() == CHOOSER_TYPE_VIDEO) {
                        // 视频分享
                        if (!ButelOvell.hasSendVedioAbility(ovell)) {
                            Toast.makeText(ShareLocalActivity.this,
                                R.string.this_equipment_cannot_share_vedio, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        // 同时包含 图片、视频
                        if (!ButelOvell.hasCallAbility(ovell)
                            && ButelOvell.hasSendVedioAbility(ovell)) {
                            Toast.makeText(ShareLocalActivity.this,
                                getString(R.string.this_equipment_cannot_share_picture), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (ButelOvell.hasSendPicturesAbility(ovell)
                            && !ButelOvell.hasSendVedioAbility(ovell)) {
                            Toast.makeText(ShareLocalActivity.this,
                                getString(R.string.this_equipment_cannot_share_vedio), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!ButelOvell.hasSendPicturesAbility(ovell)
                            && !ButelOvell.hasSendVedioAbility(ovell)) {
                            Toast.makeText(ShareLocalActivity.this,
                                R.string.cannot_share_picture_vedio, Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                }
                LayoutInflater inflater = LayoutInflater
                    .from(ShareLocalActivity.this);
                View selfView = inflater.inflate(
                    R.layout.share_confirm_dialog_view, null);//分享确认弹框布局文件

                // 自定义dialog view
                initSelfControl(selfView, threadId, receiver, showName,
                    headUrl, threadType);

                conDlg = new CommonDialog(ShareLocalActivity.this,
                    getLocalClassName(), 300);
                conDlg.addView(selfView);
                conDlg.setCancelable(false);
                // conDlg.setTitle(getString(R.string.share_dialog_title));
                conDlg.setTitleVisible(getString(R.string.send_to));
                //Toast.makeText(ShareLocalActivity.this,"数据类型"+chatForwardType,Toast.LENGTH_LONG).show();
                if(shareFlag == 0){
                    switch (chatForwardType)
                    {
                        case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                            String txt= chatForwardIntent.getExtras().getString("chatForwardTxt");
                            conDlg.setTransmitInfo(txt);
                            break;
                        case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                            if(chatPicVideoPath.startsWith("http://")){
                                conDlg.setTrasmitPic(chatPicVideoPath,R.drawable.default_link_pic,0);
                            }else{
                                Bitmap bitmap= BitmapFactory.decodeFile(chatPicVideoPath);
                                conDlg.setTransmitPic(bitmap);
                            }
                            break;
                        case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                            if(chatPicVideoPath.startsWith("http://")){
                                conDlg.setTrasmitPic(chatPicVideoPath,R.drawable.default_link_pic,1);
                            }else{
                                Bitmap bitmap1=FileManager.createVideoThumbnail(chatPicVideoPath);
                                Bitmap  bitmap2 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.video_icon);
                                Bitmap bitmap3=combineBitmap(bitmap1,bitmap2);
                                conDlg.setTransmitPic(bitmap3);
                            }
                            break;
                        case FileTaskManager.NOTICE_TYPE_VCARD_SEND:
                            String chatForwardVcardName=chatForwardIntent.getExtras().getString("chatForwardVcardName");
                            String chatForwardVcardNumber=chatForwardIntent.getExtras().getString("chatForwardVcardNumber");
                            //Toast.makeText(ShareLocalActivity.this,"名片"+chatForwardVcardName+","+chatForwardVcardNumber,Toast.LENGTH_LONG).show();
                            conDlg.setTransmitCardInfo(chatForwardVcardName,Integer.parseInt(chatForwardVcardNumber));
                            break;
                        case FileTaskManager.NOTICE_TYPE_MEETING_INVITE:
                            String chatForwardCreator=chatForwardIntent.getExtras().getString("chatForwardCreator");
                            String chatForwardMeetingRoomId=chatForwardIntent.getExtras().getString("chatForwardMeetingRoomId");
                            conDlg.setTransmitMeetingInfoInstance(chatForwardCreator,chatForwardMeetingRoomId);
                            break;
                        case FileTaskManager.NOTICE_TYPE_MEETING_BOOK:
                            String chatForwardCreator2=chatForwardIntent.getExtras().getString("chatForwardCreator");
                            String chatForwardMeetingRoomId2=chatForwardIntent.getExtras().getString("chatForwardMeetingRoomId");
                            String chatForwardMeetingTopic=chatForwardIntent.getExtras().getString("chatForwardMeetingTopic");
                            String chatForwardDate=chatForwardIntent.getExtras().getString("chatForwardDate");
                            String chatForwardHms=chatForwardIntent.getExtras().getString("chatForwardHms");
                            conDlg.setTransmitMeetingInfoBook(chatForwardCreator2,chatForwardMeetingRoomId2,chatForwardMeetingTopic,chatForwardDate,chatForwardHms);
                            break;
                    case FileTaskManager.NOTICE_TYPE_MANY_MSG_FORWARD:
                        int noticeNum=chatForwardIntent.getExtras().getInt("noticeNum");
                        String me=chatForwardIntent.getExtras().getString("me");
                        String theOther=chatForwardIntent.getExtras().getString("theOther");
                        if(forwradType == 0)
                        {
                            if(noticeNum>0) {
                                conDlg.setTransmitItemByItem(noticeNum+"");
                            }
                        }
                        else if(forwradType == 1)
                        {
                            conDlg.setSingleMerge(me,theOther);
                        }
                        break;
                        case FileTaskManager.NOTICE_TYPE_ARTICAL_SEND:
                            conDlg.setTransmitInfo(getString(R.string.article) + chatForwardIntent.getExtras().getString("chatForwardTxt"));
                            break;
                        case FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND:
                            conDlg.setTransmitInfo(getString(R.string.chat_record_bracket) + chatForwardIntent.getExtras().getString("chatForwardTxt"));
                            break;
                        default:
                            break;
                    }
                }else if(shareFlag == 1){
                    //从收藏内转发
                    collectItemInfo.getType();
                    switch (collectItemInfo.getType()){
                        case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                            String txt= collectItemInfo.getTxt();
                            conDlg.setTransmitInfo(txt);
                            break;
                        case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                            conDlg.setTrasmitPic(collectItemInfo.getRemoteUrl(),R.drawable.default_link_pic,0);
                            break;
                        case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                            conDlg.setTrasmitPic(collectItemInfo.getThumbnailRemoteUrl(),R.drawable.default_link_pic,1);
                            break;
                        case FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND:
                            conDlg.setTransmitInfo(getString(R.string.chat_record_bracket) + collectItemInfo.getText());
                            break;
                        default:
                            break;
                    }
                }else{
                    conDlg.setTransmitInfo(getString(R.string.article) + articleInfo.getTitle());
                }

                conDlg.setCancleButton(null, R.string.btn_cancle);
                conDlg.setPositiveButton(new CommonDialog.BtnClickedListener() {

                    @Override
                    public void onBtnClicked() {

                        if (!ShareLocalActivity.mIsForward) {

                            // 本地图片分享
                            onSendPicMsg(threadId, receiver, threadType);
                            // 跳转到聊天界面
                            skipToChatActivity(threadType, threadId, receiver,
                                showName);
                        } else {
                            // 消息转发
                            forwardMsg(receiver,threadType);
                        }

                    }
                }, R.string.btn_send);
                conDlg.showDialog();
            }
        });
        convsList.setAdapter(adapter);

    }

    /**
     * 合并两个bitmap
     * @param background
     * @param foreground
     * @return
     */
    private Bitmap combineBitmap(Bitmap background, Bitmap foreground) {
        if( background == null ) {
            return null;
        }

        int bgWidth = background.getWidth()/2;
        int bgHeight = background.getHeight()/2;
        //int fgWidth = foreground.getWidth();
        //int fgHeight = foreground.getHeight();
        //create the new blank bitmap 创建一个新的和SRC长度宽度一样的位图
        Bitmap newbmp = createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newbmp);
        //draw bg into
        cv.drawBitmap(background, 0, 0, null);//在 0，0坐标开始画入bg
        //draw fg into
        cv.drawBitmap(foreground, bgWidth/2-foreground.getWidth()/2, bgHeight/2-foreground.getHeight()/2, null);//在 0，0坐标开始画入fg ，可以从任意位置画入
        //save all clip
        cv.save(Canvas.ALL_SAVE_FLAG);//保存
        //store
        cv.restore();//存储
        return newbmp;
    }

    /**
     *
     * @param nube
     * @return true:该设备不支持发送此类型消息；false:支持
     */
    private boolean filterDeviceType(int msgType, String nube) {
        byte ovell = ButelOvell.getNubeOvell(nube);
        // 文本
        if (FileTaskManager.NOTICE_TYPE_TXT_SEND == msgType) {
            if (!ButelOvell.hasSendMessageAbility(ovell)) {
                return true;
            }
        }
        // 语音
        if (FileTaskManager.NOTICE_TYPE_AUDIO_SEND == msgType) {
            if (!ButelOvell.hasSendRecordAbility(ovell)) {
                return true;
            }
        }
        // 视频
        if (FileTaskManager.NOTICE_TYPE_VEDIO_SEND == msgType) {
            if (!ButelOvell.hasSendVedioAbility(ovell)) {
                return true;
            }
        }
        // 图片
        if (FileTaskManager.NOTICE_TYPE_PHOTO_SEND == msgType) {
            if (!ButelOvell.hasSendPicturesAbility(ovell)) {
                return true;
            }
        }
        // 名片
        if (FileTaskManager.NOTICE_TYPE_VCARD_SEND == msgType) {
            if (!ButelOvell.hasSendCardAbility(ovell)) {
                return true;
            }
        }
        // 视频通话
        if (FileTaskManager.NOTICE_TYPE_RECORD == msgType) {
            if (!ButelOvell.hasCallAbility(ovell)) {
                return true;
            }
        }
        // 视频会议
        if (FileTaskManager.NOTICE_TYPE_MEETING_INVITE == msgType) {
            if (!ButelOvell.hasMeetingAbility(ovell)) {
                return true;
            }
        }

        return false;
    }

    private String getMsgType(int msgType) {
        if (msgType == FileTaskManager.NOTICE_TYPE_TXT_SEND) {
            return getString(R.string.txt);
        } else if (msgType == FileTaskManager.NOTICE_TYPE_AUDIO_SEND) {
            return getString(R.string.voice);
        } else if (msgType == FileTaskManager.NOTICE_TYPE_PHOTO_SEND) {
            return getString(R.string.picture);
        } else if (msgType == FileTaskManager.NOTICE_TYPE_VEDIO_SEND) {
            return getString(R.string.video);
        } else if (msgType == FileTaskManager.NOTICE_TYPE_VCARD_SEND) {
            return getString(R.string.chat_vcard_title);
        }
        return "";
    }

    private int getShareType() {
        if (mListPhotoPath != null && mListPhotoPath.size() > 0) {

            for (int i = 0; i < mListPhotoPath.size(); i++) {
                if (MediaFile.isImageFileType(mListPhotoPath.get(i))) {
                    if (shareType == CHOOSER_TYPE_VIDEO) {
                        shareType = CHOOSER_TYPE_ALL;
                        return shareType;
                    }
                    shareType = CHOOSER_TYPE_IMAGE;

                } else if (MediaFile.isVideoFileType(mListPhotoPath.get(i))) {
                    if (shareType == CHOOSER_TYPE_IMAGE) {
                        shareType = CHOOSER_TYPE_ALL;
                        return shareType;
                    }
                    shareType = CHOOSER_TYPE_VIDEO;
                }
            }

        }
        return shareType;
    }

    private void forwardMsg(String receiver,int threadType) {
        if(threadType == ThreadsTable.TYPE_SINGLE_CHAT){
            int relationCode =  FriendsManager.getInstance().getFriendRelationByNubeNumber(receiver);
            if(relationCode != FriendsManager.RELATION_TYPE_BOTH){
                CustomLog.e(TAG,receiver + "不是好友，不能转发");
                CustomToast.show(this,getString(R.string.not_friend_validation_friend),CustomToast.LENGTH_SHORT);
                return;
            }
        }
        if(shareFlag ==1 ){
            if(collectItemInfo != null){
                MedicalApplication.getFileTaskManager()
                        .forwardMessageForCollectionOther(receiver, collectItemInfo,pos);
            }else{
                // 转发消息
                if (TextUtils.isEmpty(msgId)) {
                    CustomLog.d(TAG,"待转发消息数据丢失");
                    return;
                }
                MedicalApplication.getFileTaskManager()
                        .forwardMessageForCollection(receiver, msgId,pos);
            }
            finish();
            //TODO这里需要陈从江提供一个 转发的是第几个图片的位置
        }
        else if(shareFlag==2)
        {
            MedicalApplication.getFileTaskManager()
                    .forwordArticleMsg(receiver, articleInfo,pos);
        }
        else{
            //0：逐条转发  1：合并转发
            if(forwradType == 0){
                if (!TextUtils.isEmpty(msgId)) {
                    String[] ids = msgId.split(",");
                    for (String id : ids) {
                        if (!TextUtils.isEmpty(id)) {
                            MedicalApplication.getFileTaskManager()
                                    .forwardMessage(receiver, id);
                        }
                    }
                }
            }else {
                boolean isForwardSucc = MedicalApplication.getFileTaskManager().combineMsgForforward(receiver,msgId);
                if(!isForwardSucc){
                    final CustomDialog tipDlg = new CustomDialog(ShareLocalActivity.this);
                    String tip = getString(R.string.content_too_long_not_send);
                    tipDlg.setTip(tip);
                    tipDlg.removeCancelBtn();
                    tipDlg.setOkBtnText(getString(R.string.iknow));
                    tipDlg.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                        @Override
                        public void onClick(CustomDialog customDialog) {
                            tipDlg.dismiss();
                            finish();
                        }
                    });
                    tipDlg.show();
                }
            }

        }
    }

    private void initSelfControl(View selfView, String threadId,
                                 String receiver, String showName, String headUrl, int threadType) {

        TextView nameView = (TextView) selfView.findViewById(R.id.name_txt);
        TextView numView = (TextView) selfView
            .findViewById(R.id.recv_num_field);
        SharePressableImageView icon = (SharePressableImageView) selfView
            .findViewById(R.id.contact_icon);

        if (threadType == ThreadsTable.TYPE_GROUP_CHAT) {
            // 群成员人数
            groupMemberSize = groupDao.queryGroupMemberCnt(threadId);
            numView.setVisibility(View.VISIBLE);
            numView.setText(groupMemberSize + getString(R.string.person));
            Glide.with(this).load(headUrl).placeholder(R.drawable.group_icon)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .crossFade().into(icon.shareImageview);

//            groupName = groupDao.getGroupNameByGid(threadId);
            nameView.setText(showName);

        } else {
            int sexIconId = IMCommonUtil.getHeadIdBySex(new NetPhoneDaoImpl(this).getSexByNumber(receiver));

            Glide.with(this).load(headUrl).placeholder(sexIconId)
                .error(sexIconId).centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade().into(icon.shareImageview);
            nameView.setText(showName);
            numView.setVisibility(View.GONE);
        }
    }

    private void skipToChatActivity(int threadType, String threadId,
                                    String receiver, String showName) {
        // LogUtil.begin("");
        if (threadType == ThreadsTable.TYPE_GROUP_CHAT) {
            CustomLog.d(TAG,"跳到 群聊天 消息页面");
            // 群组聊天
            Intent pdintent = new Intent(ShareLocalActivity.this,
                ChatActivity.class);
            pdintent.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                ChatActivity.VALUE_NOTICE_FRAME_TYPE_LIST);
            pdintent.putExtra(ChatActivity.KEY_CONVERSATION_NUBES, threadId);
            pdintent.putExtra(ChatActivity.KEY_CONVERSATION_ID, threadId);
            pdintent.putExtra(ChatActivity.KEY_CONVERSATION_SHORTNAME, showName);
            pdintent.putExtra(ChatActivity.KEY_CONVERSATION_TYPE,
                ChatActivity.VALUE_CONVERSATION_TYPE_MULTI);
            startActivity(pdintent);
        } else {
            // 点对点聊天
            CustomLog.d(TAG,"跳到 点对点 消息页面");
            Intent pdintent = new Intent(ShareLocalActivity.this,
                ChatActivity.class);
            pdintent.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                ChatActivity.VALUE_NOTICE_FRAME_TYPE_NUBE);
            pdintent.putExtra(ChatActivity.KEY_CONVERSATION_NUBES, receiver);
            pdintent.putExtra(ChatActivity.KEY_CONVERSATION_SHORTNAME, showName);
            pdintent.putExtra(ChatActivity.KEY_CONVERSATION_TYPE,
                ChatActivity.VALUE_CONVERSATION_TYPE_SINGLE);
            startActivity(pdintent);
        }
        finish();
    }

    /**
     * 发送图片消息
     *
     * @param
     * @return
     */
    public boolean onSendPicMsg(final String threadId, final String receiver,
                                final int threadType) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                for (String picPath : mListPhotoPath) {

                    if (MediaFile.isImageFileType(picPath)) {
                        CustomLog.d(TAG,"准备发送图片：");
                        List<String> localFiles = new ArrayList<String>();
                        localFiles.add(picPath);

                        // 图片宽高
                        int[] imgSize = FileManager.getImageSizeByPath(
                            ShareLocalActivity.this, picPath);
                        CustomLog.d(TAG,"图片尺寸：" + imgSize[0] + "|" + imgSize[1] + "|"
                            + imgSize[2]);

                        ButelPAVExInfo extInfo = new ButelPAVExInfo();

                        if (imgSize[2] == 90 || imgSize[2] == 270) {
                            // 图片有方向的场合，为了显示正常，需要将宽高对调
                            extInfo.setWidth(imgSize[1]);
                            extInfo.setHeight(imgSize[0]);
                        } else {
                            extInfo.setWidth(imgSize[0]);
                            extInfo.setHeight(imgSize[1]);
                        }

                        String uuid = "";
                        if (threadType == ThreadsTable.TYPE_GROUP_CHAT) {
                            // 插入发送记录
                            uuid = noticeDao.createSendFileNotice(
                                selfNubeNumber, threadId, localFiles, "",
                                FileTaskManager.NOTICE_TYPE_PHOTO_SEND, "",
                                threadId, extInfo);
                        } else {
                            // 插入发送记录
                            uuid = noticeDao.createSendFileNotice(
                                selfNubeNumber, receiver, localFiles, "",
                                FileTaskManager.NOTICE_TYPE_PHOTO_SEND, "",
                                threadId, extInfo);
                        }

                        // 进入发送任务队列
                        MedicalApplication.getFileTaskManager().addTask(uuid,
                            null);

                    } else {

                        CustomLog.d(TAG,"准备发送视频：");
                        // 视频每次只能发一个
                        String videoPath = picPath;

                        List<String> localFiles = new ArrayList<String>();
                        localFiles.add(videoPath);

                        // 视频尺寸
                        int videoDuration = FileManager.getVideoDurationByPath(
                            ShareLocalActivity.this, videoPath);
                        ButelPAVExInfo extInfo = new ButelPAVExInfo();
                        // 单位秒
                        extInfo.setDuration(videoDuration / 1000);
                        String uuid = "";
                        if (threadType == ThreadsTable.TYPE_GROUP_CHAT) {
                            // 插入发送记录
                            uuid = noticeDao.createSendFileNotice(
                                selfNubeNumber, threadId, localFiles, "",
                                FileTaskManager.NOTICE_TYPE_VEDIO_SEND, "",
                                threadId, extInfo);
                        } else {
                            // 插入发送记录
                            uuid = noticeDao.createSendFileNotice(
                                selfNubeNumber, receiver, localFiles, "",
                                FileTaskManager.NOTICE_TYPE_VEDIO_SEND, "",
                                threadId, extInfo);

                        }
                        // 进入发送任务队列
                        MedicalApplication.getFileTaskManager().addTask(uuid,
                            null);

                    }

                }
            }
        }).start();

        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case BACK_FROM_SELECT_LINK:
                    CustomLog.d(TAG,"选择联系人返回");
                    if (data == null) {
                        return;
                    }
                    Bundle selRes = data.getExtras();
                    if (selRes != null) {
                        // 初始化收件人相关数据
                        receiverNameMap.clear();
                        final ArrayList<String> selectNickNames = selRes
                            .getStringArrayList(SelectLinkManActivity.START_RESULT_NICKNAME);
                        final ArrayList<String> selectName = selRes
                            .getStringArrayList(SelectLinkManActivity.START_RESULT_NAME);
                        final ArrayList<String> selectNumber = selRes
                            .getStringArrayList(SelectLinkManActivity.START_RESULT_NUMBER);
                        receiverNumberLst = selRes
                            .getStringArrayList(SelectLinkManActivity.START_RESULT_NUBE);

                        if (selectName == null
                            || selectName.size() != receiverNumberLst.size()) {
                            CustomLog.d(TAG,"选择收件人返回数据不整合");
                            return;
                        }
                        if (selectNickNames == null
                            || selectNickNames.size() != receiverNumberLst
                            .size()) {
                            CustomLog.d(TAG,"选择收件人返回数据不整合");
                            return;
                        }
                        if (selectNumber == null
                            || selectNumber.size() != receiverNumberLst.size()) {
                            CustomLog.d(TAG,"选择收件人返回数据不整合");
                            return;
                        }
                        for (int i = 0; i < receiverNumberLst.size(); i++) {
                            // 收件人名称
                            String nubeNum = receiverNumberLst.get(i);

                            // 产品要求按照ShowNameUtil中的显示规则显示名字--add on 2015/6/29
                            ShowNameUtil.NameElement element = ShowNameUtil.getNameElement(
                                selectName.get(i), selectNickNames.get(i),
                                selectNumber.get(i), nubeNum);

                            String showName = ShowNameUtil.getShowName(element);

                            receiverNameMap.put(nubeNum, showName);
                        }
                        CustomLog.d(TAG,"选择联系人个数:" + receiverNameMap.size());
                        if (receiverNameMap.size() > 1) {
                            // 群聊
                            createGroup();
                        } else {
                            if (!mIsForward) {
                                CustomLog.d(TAG,"分享给单人");
                                onSendPicMsg("", receiverNumberLst.get(0),
                                    ThreadsTable.TYPE_SINGLE_CHAT);
                                // 单聊
                                // 跳转到聊天界面
                                skipToChatActivity(ThreadsTable.TYPE_SINGLE_CHAT,
                                    "", receiverNumberLst.get(0),
                                    receiverNameMap.get(receiverNumberLst
                                        .get(0)));

                            } else {
                                // 转发消息
                                CustomLog.d(TAG,"转发给单人");
                                forwardMsg(receiverNumberLst.get(0),ThreadsTable.TYPE_SINGLE_CHAT);
                            }

                        }
                    }
                    break;
                default:
                    break;

            }
        }
    }

    /*
     * 调用GroupChatInterfaceManager的接口进行群的创建
     */
    public void createGroup() {
        groupChatInterfaceManager = new GroupChatInterfaceManager(
            ShareLocalActivity.this, new GroupChatInterfaceManager.GroupInterfaceListener() {
            @Override
            public void onResult(String _interfaceName,
                                 boolean successOrfaliure, String _successReslut) {
                LogUtil.d("接口" + _interfaceName + "返回信息"
                    + _successReslut);
                if (_interfaceName
                    .equals(UrlConstant.METHOD_CREATE_GROUP)) {
                    // 建群结束 ，跳入chatActivity页面
                    removeLoadingView();
                    if (successOrfaliure) {
                        LogUtil.d("create group success:gid="+ _successReslut);
                        if (!mIsForward) {
                            LogUtil.d("分享给多人");
                            onSendPicMsg(_successReslut, _successReslut,ThreadsTable.TYPE_GROUP_CHAT);
                            // 建群结束 ，跳入chatActivity页面
                            skipToChatActivity(ThreadsTable.TYPE_GROUP_CHAT,_successReslut, _successReslut, "");
                        } else {
                            LogUtil.d("转发给多人");
                            forwardMsg(_successReslut,ThreadsTable.TYPE_GROUP_CHAT);
                        }
                    }
                }
            }
        });
        showLoadingView(getString(R.string.wait_creat_group_chat), new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                removeLoadingView();
                CustomLog.d(TAG, "取消创建群聊");
            }
        }, true);

        groupChatInterfaceManager.createGroup("", receiverNumberLst);
    }

    private CommonWaitDialog dialog = null;

    private void showWaitDialog(String hint) {
        if (dialog != null) {
            dialog.clearAnimation();
            dialog = null;
        }
        dialog = new CommonWaitDialog(this, hint);
        dialog.startAnimation();
    }

    private void closeWaitDialog() {
        if (dialog != null) {
            dialog.clearAnimation();
            dialog = null;
        }
    }

    private void initParams() {
        LogUtil.begin("");
        Intent intent = getIntent();
        collectItemInfo = (DataBodyInfo) intent.getSerializableExtra(KEY_COLLECTION_ITEM_INFO);
        articleInfo = ((DataBodyInfo)intent.getSerializableExtra("articleInfo"));


        if(collectItemInfo != null){
            msgId = collectItemInfo.collectionId;
            shareFlag = 1;
            mIsForward = true;
            pos = -1;
            CustomLog.d(TAG,"收藏页面转发消息");
            getTitleBar().setBack(null,null);
        }
        else if(articleInfo != null)
        {
            shareFlag=2;
            mIsForward=true;
            pos=-1;
            getTitleBar().setBack(null,null);
        }
        else {
            Bundle extras = intent.getExtras();
            chatForwardIntent=intent;
            chatPicVideoPath=extras.getString("chatForwardPath");
            chatForwardType=extras.getInt("chatForwardType",-11);
            if (extras != null) {
                mIsForward = extras.getBoolean(KEY_ACTION_FORWARD);
                forwradType = extras.getInt(FORWARD_TYPE);
                if (!mIsForward) {
                    getTitleBar().setBack(null,null);
                    LogUtil.d("分享--- [图片/视频]");
                    // 本地相册分享
                    String action = intent.getAction();
                    if (Intent.ACTION_SEND.equals(action)) {
                        // 单文件分享
                        singleShare(extras);
                    } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                        // 多文件分享
                        multiShare(extras);
                    } else {
                        LogUtil.d("initParams unkown action...");
                    }
                } else {
                    // 消息转发
                    msgId = extras.getString(MSG_ID);
                    shareFlag = extras.getInt(KEY_COLLECTION_FORWARD, 0);
                    pos = extras.getInt(KEY_COLLECTION_FORWARD_POS,-1);
                    getTitleBar().setBackText(getString(R.string.btn_cancle));
                    LogUtil.d("消息转发，msgId=" + msgId);
                }

            }
        }

        LogUtil.end("");
    }

    // 分享文件大小限制
    private boolean isBeyoundLimit(String path, int chooserType) {
        if (path != null) {
            File file = new File(path);
            if (file != null && file.exists()) {
                long signal_size = file.length();
                LogUtil.d("file length:" + signal_size);
                if (chooserType == CHOOSER_TYPE_VIDEO) {
                    // 视频不支持30M以上
                    if (signal_size > IMConstant.MAX_VIDEO_FILE_SIZE) {

                        return true;
                    }
                } else {
                    if (signal_size > IMConstant.MAX_IMAGE_FILE_SIZE) {

                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void singleShare(Bundle bundle) {
        LogUtil.begin("");
        if (bundle.containsKey(Intent.EXTRA_STREAM)) {
            try {
                if (mListPhotoPath == null) {
                    mListPhotoPath = new ArrayList<String>();
                }
                Uri uri = (Uri) bundle.getParcelable(Intent.EXTRA_STREAM);
                LogUtil.d("uri=" + uri);
                LogUtil.d("uri.getPath=" + uri.getPath());
                // 根据Uri判断文件类型：
                ContentResolver resolver = getContentResolver();
                String fileType = resolver.getType(uri);
                // 某些文件夹下的图片在媒体库里无法访问，造成fileType=null。导致：从文件夹下分享时，分享不出去
                // 此处再做一次判断
                if (fileType == null) {
                    LogUtil.d("getType()方法取得的fileType为null");
                    if (MediaFile.isImageFileType(uri.getPath())) {
                        fileType = "image/*";

                    } else if (MediaFile.isVideoFileType(uri.getPath())) {
                        fileType = "video/*";
                    }
                }
                String path = null;
                LogUtil.d("fileType=" + fileType);

                if (fileType.startsWith("image")) {
                    path = FileManager.getPhotoPath(this, uri);
                } else if (fileType.startsWith("video")) {
                    path = FileManager.getVideoPath(this, uri);
                }
                LogUtil.d("path=" + path);
                if (!TextUtils.isEmpty(path)) {
                    // 系统分享只能分享照片，排除其他文件
                    if (MediaFile.isImageFileType(path)) {
                        mListPhotoPath.add(path);
                        LogUtil.d("单文件[图片]分享 ：Path=" + path);

                    } else if (MediaFile.isVideoFileType(path)) {
                        if (!isBeyoundLimit(path, CHOOSER_TYPE_VIDEO)) {
                            mListPhotoPath.add(path);
                            LogUtil.d("单文件[视频]分享 ：Path=" + path);
                        } else {
                            Toast.makeText(
                                ShareLocalActivity.this,
                                getString(R.string.share_video_beyond_limit_single),
                                Toast.LENGTH_SHORT).show();
                            LogUtil.d("视频文件>30M,请选择小于30M的视频分享");
                            return;
                        }

                    } else {
                        Toast.makeText(this,
                            getString(R.string.share_is_illeagel),
                            Toast.LENGTH_SHORT).show();
                        finish();
                        LogUtil.d("该文件不是合法的[图片/视频]文件,不能分享：path=" + path);
                    }

                } else {
                    LogUtil.d("path is empty....");
                }
            } catch (Exception e) {
                LogUtil.e("Exception", e);
            }
        } else {
            Toast.makeText(this, R.string.info_not_complete_not_share, Toast.LENGTH_SHORT).show();
            LogUtil.d("文件信息不完整，无法分享");
        }
        LogUtil.end("");
    }

    /**
     * 分享文件总数：图片+视频<=9
     *
     * @param bundle
     */
    private void multiShare(Bundle bundle) {
        LogUtil.begin("");
        if (bundle.containsKey(Intent.EXTRA_STREAM)) {
            try {
                List<Uri> uris = new ArrayList<Uri>();
                uris = bundle.getParcelableArrayList(Intent.EXTRA_STREAM);
                if (mListPhotoPath == null) {
                    mListPhotoPath = new ArrayList<String>();
                }
                Uri uri = null;
                String path = null;
                int j = 0;
                int max_count = MultiBucketChooserActivity.MAX_IMAGE_COUNT;
                for (int i = 0; i < uris.size(); i++) {
                    uri = uris.get(i);

                    LogUtil.d("uri=" + uri);
                    LogUtil.d("uri.getPath=" + uri.getPath());

                    // 根据Uri判断文件类型：
                    ContentResolver resolver = getContentResolver();
                    String fileType = resolver.getType(uri);
                    // 某些文件夹下的图片在媒体库里无法访问，造成fileType=null。导致：从文件夹下分享时，分享不出去
                    // 此处再做一次判断
                    if (fileType == null) {
                        LogUtil.d("getType()方法取得的fileType为null");
                        if (MediaFile.isImageFileType(uri.getPath())) {
                            fileType = "image/*";

                        } else if (MediaFile.isVideoFileType(uri.getPath())) {
                            fileType = "video/*";
                        }
                    }
                    LogUtil.d("fileType=" + fileType);

                    if (fileType.startsWith("image")) {
                        path = FileManager.getPhotoPath(this, uri);
                    } else if (fileType.startsWith("video")) {
                        path = FileManager.getVideoPath(this, uri);
                    }
                    LogUtil.d("path=" + path);

                    if (!TextUtils.isEmpty(path)) {
                        // 图片文件限制总数
                        if (MediaFile.isImageFileType(path)) {
                            j++;
                            mListPhotoPath.add(path);
                            LogUtil.d("第" + (i + 1) + "个文件是图片文件:path=" + path);

                        } else if (MediaFile.isVideoFileType(path)) {
                            if (!isBeyoundLimit(path, CHOOSER_TYPE_VIDEO)) {
                                j++;
                                mListPhotoPath.add(path);
                                LogUtil.d("第" + (i + 1) + "个文件是视频文件:path="
                                    + path);
                            } else {
                                Toast.makeText(
                                    ShareLocalActivity.this,
                                    getString(
                                        R.string.share_video_beyond_limit_multi,
                                        i + 1), Toast.LENGTH_SHORT)
                                    .show();
                                // Toast.makeText(ShareLocalActivity.this,
                                // "第" + i + "个文件是视频文件,但是>30M，移除",
                                // Toast.LENGTH_SHORT).show();
                                LogUtil.d("第" + (i + 1)
                                    + "个文件是视频文件,但是>30M，移除:path=" + path);
                            }

                        } else {
                            LogUtil.d("第" + (i + 1) + "个文件不是[图片/视频]文件，移除：path="
                                + path);
                        }
                        if (j >= max_count) {
                            break;
                        }

                    } else {
                        LogUtil.d("文件路径 path is empty");
                    }
                }
                if (j == max_count && j < uris.size()) {
                    Toast.makeText(
                        getBaseContext(),
                        getString(R.string.select_picture_num_more)
                            + MultiBucketChooserActivity.MAX_IMAGE_COUNT
                            + getString(R.string.have_remove_more_photo), Toast.LENGTH_SHORT)
                        .show();
                }
                return;
            } catch (Exception e) {
                LogUtil.e("Exception:", e);
            }
        } else {
            Toast.makeText(this, getString(R.string.info_not_complete_not_share), Toast.LENGTH_SHORT).show();
            LogUtil.d("文件信息不完整，无法分享");
        }
        LogUtil.end("");
    }

    @Override
    protected void onResume() {
        LogUtil.begin("");
        super.onResume();

        // 加载数据
        initData();

        adapter.notifyDataSetChanged();

        LogUtil.end("");
    }

    private Handler mHandler = new Handler();

    private void initData() {
        LogUtil.begin("");

        if (mHandler != null) {
            mHandler.postDelayed(queryRunnable, 50);
        }
        LogUtil.end("");
    }

    // 查询数据Runnable
    private Runnable queryRunnable = new Runnable() {
        public void run() {
            // 查询动态数据
            QueryNoticeAsyncTask task = new QueryNoticeAsyncTask(
                ShareLocalActivity.this);
            CustomLog.d(TAG,"查询动态数据");
            task.setQueryTaskListener(new QueryNoticeAsyncTask.QueryTaskPostListener() {
                @Override
                public void onQuerySuccess(Cursor cursor) {
                    // hideLoadingMsg();

                    CustomLog.d(TAG,"QueryNoticeAsyncTask onQuerySuccess...");

                    // 查询时，置之前的cursor为null
                    if (adapter != null) {
                        adapter.changeCursor(cursor);
                    }

                }

                @Override
                public void onQueryFailure() {
                    // 查询失败
                    Toast.makeText(
                        ShareLocalActivity.this,
                        getResources()
                            .getString(R.string.toast_load_failed),
                        Toast.LENGTH_SHORT).show();
                    CustomLog.d(TAG,"QueryNoticeAsyncTask onQueryFailure...");
                }

            });
            task.executeOnExecutor(ThreadPoolManger.THREAD_POOL_EXECUTOR, this
                .getClass().getName());
        }
    };

    @Override
    protected void onPause() {
        LogUtil.begin("");
        super.onPause();
        LogUtil.end("");
    }

    @Override
    protected void onNewIntent(Intent intent) {
         LogUtil.begin("");
        super.onNewIntent(intent);

         LogUtil.end("");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 不finish,则当按下home键，再进入时，可视仍停留在 选择页--此处finish(),把选择页特性当作dialog来处理
        if (finishFlag) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        LogUtil.begin("");
        AppP2PAgentManager.getInstance().removeImMsgResultListener();
        super.onDestroy();
        if(conDlg != null){
            conDlg.resetDialog();
        }

        LogUtil.end("");
    }
}
