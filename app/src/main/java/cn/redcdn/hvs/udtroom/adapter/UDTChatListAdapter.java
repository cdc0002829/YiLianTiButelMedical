package cn.redcdn.hvs.udtroom.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.datacenter.collectcenter.DataBodyInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.contacts.contact.ContactCardActivity;
import cn.redcdn.hvs.contacts.contact.hpucontact.DtNoticesTable;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.activity.EmbedWebViewActivity;
import cn.redcdn.hvs.im.activity.ViewPhotosActivity;
import cn.redcdn.hvs.im.activity.ViewUDTPhotosActivity;
import cn.redcdn.hvs.im.bean.BookMeetingExInfo;
import cn.redcdn.hvs.im.bean.ButelFileInfo;
import cn.redcdn.hvs.im.bean.FileTaskBean;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.bean.PhotoBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.bean.ShowNameUtil.NameElement;
import cn.redcdn.hvs.im.bean.WebpageBean;
import cn.redcdn.hvs.im.collection.CollectionFileManager;
import cn.redcdn.hvs.im.column.NoticesTable;
import cn.redcdn.hvs.im.dao.DtNoticesDao;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.fileTask.ChangeUIInterface;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.manager.CollectionManager;
import cn.redcdn.hvs.im.manager.HtmlParseManager;
import cn.redcdn.hvs.im.manager.HtmlParseManager.OnClickBack;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.util.MediaFile;
import cn.redcdn.hvs.im.util.PlayerManager;
import cn.redcdn.hvs.im.util.smileUtil.EmojiconTextView;
import cn.redcdn.hvs.im.view.BottomMenuWindow;
import cn.redcdn.hvs.im.view.BottomMenuWindow.MenuClickedListener;
import cn.redcdn.hvs.im.view.MedicalAlertDialog;
import cn.redcdn.hvs.im.view.SharePressableImageView;
import cn.redcdn.hvs.im.view.XCRoundImageViewByXfermode;
import cn.redcdn.hvs.meeting.activity.ReserveSuccessActivity;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.officialaccounts.activity.ArticleActivity;
import cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity;
import cn.redcdn.hvs.profiles.activity.ChatCollectionActivity;
import cn.redcdn.hvs.udtroom.configs.UDTGlobleData;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.DateUtil;
import cn.redcdn.log.CustomLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.butel.connectevent.utils.LogUtil;
import java.io.File;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity.INTENT_DATA_ARTICLE_ID;
import static cn.redcdn.hvs.util.CommonUtil.getString;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class UDTChatListAdapter extends CursorAdapter {
    public static final String ACTIVITY_FLAG = "UDTChatListAdapter";
    private static final String TAG = UDTChatListAdapter.class.getSimpleName();

    /**
     * 1.type的值必须从0开始，否侧会报ArrayIndexOutOfBoundsException 2.用于多布局
     */
    private static final int NOTICE_TYPE_RECV_TXT = 0;
    private static final int NOTICE_TYPE_SENT_TXT = 1;
    private static final int NOTICE_TYPE_RECV_IMAGE = 2;
    private static final int NOTICE_TYPE_SENT_IMAGE = 3;
    private static final int NOTICE_TYPE_RECV_VIDEO = 4;
    private static final int NOTICE_TYPE_SENT_VIDEO = 5;
    private static final int NOTICE_TYPE_RECV_VOICE = 6;
    private static final int NOTICE_TYPE_SENT_VOICE = 7;
    private static final int NOTICE_TYPE_RECV_VCARD = 8;
    private static final int NOTICE_TYPE_SENT_VCARD = 9;
    private static final int NOTICE_TYPE_SENT_MEETING = 10;
    private static final int NOTICE_TYPE_RECV_MEETING = 11;
    private static final int NOTICE_TYPE_SENT_CHATRECORD = 12;
    private static final int NOTICE_TYPE_RECV_CHATRECORD = 13;
    private static final int NOTICE_TYPE_SENT_ARTICLE = 14;
    private static final int NOTICE_TYPE_RECV_ARTICLE = 15;

    // 上传下载任务管理器
    private FileTaskManager fileTaskMgr = null;

    private LayoutInflater layoutInflater = null;
    private CallbackInterface callbackIf = null;
    private DtNoticesDao noticeDao = null;
    private GroupDao groupDao = null;
    private Context mContext = null;

    // 刷新界面上传下载进度任务ID
    private List<String> changUIProgressTaskIds = new ArrayList<String>();
    // 上传文件进度值
    private Map<String, Float> mTaskFileProgressMap = new HashMap<String, Float>();
    // 当前收听的音频的消息ID
    private String curPlayingAuMsgId = "";
    // 当前收听的音频的View
    private WeakReference<View> currentPlayVoiceView = null;
    // 音频播放
    private MediaPlayer mMediaPlayer = null;

    // 自身视讯号
    private String selfNubeNumber = "";
    // 数据游标
    private Cursor dataCursor = null;
    // 图片最大尺寸
    private int picMaxWidthSize = 280;
    // 图片最小尺寸
    private int picMinWidthSize = 156;
    // 图片最大尺寸
    private int picMaxHeightSize = 280;
    // 图片最小尺寸
    private int picMinHeightSize = 156;

    //图片默认尺寸
    private int picDefaultSize = 280;

    // 图片默认尺寸
    //    private int picDefaultSize = 150;
    // 音频短尺寸
    private int audioWidthS = 100;
    // 音频长尺寸
    private int audioWidthL = 300;
    // 消息类型
    private int noticeType;
    // 群id
    private String groupId = "";
    private String headUrl = "";
    private int userDefaultHeadUrl;
    private String targetNumber = "";
    private String targetShortName = "";
    private String butelPubNubeNum = "10000";
    private static String dateday = "";

    // 是否是多选模式
    private boolean bMultiCheckMode = false;

    // 加载框视图
    //    private CommonWaitDialog mWaitDialog;

    //当前是否在播放音频
    private boolean isPlayingAuMsg = false;

    //语音消息扬声器播放模式
    private static final Boolean SPEAKER = true;
    //语音消息听筒播放模式
    private static final Boolean HEADSET = false;
    //SharedPreferences Name
    private static final String VOICE_PREFS_NAME = "VoicePrefsFile";
    //会话类型
    private int conversationType;

    //会议链接点击监听
    MeetingLinkClickListener meetingLinkClickListener;

    private SharedPreferences voiceMsgSettings;

    private PlayerManager playerManager = PlayerManager.getManager();

    //TODO 群成员头像和名称数据库存在问题，造成目前可能取不到的问题，使用会诊信息进行展示
    private String tmp_target_docName = "";
    private String tmp_target_docHeadUrl = "";


    public void onDestroy() {
        if (dataCursor != null) {
            dataCursor.close();
            dataCursor = null;
        }

    }


    public void setSelfNubeNumber(String nubeNumber) {
        this.selfNubeNumber = nubeNumber;
    }


    public void setCallbackInterface(CallbackInterface cbIf) {
        this.callbackIf = cbIf;
    }


    public void setMultiCheckMode(boolean multicheck) {
        this.bMultiCheckMode = multicheck;
        this.notifyDataSetChanged();
    }


    public boolean isMultiCheckMode() {
        return this.bMultiCheckMode;
    }


    public void changeCursor(Cursor newCursor) {
        LogUtil.d("changeCursor");
        Cursor oldCursor = this.dataCursor;
        this.dataCursor = newCursor;
        this.notifyDataSetChanged();
        if (oldCursor != null) {
            oldCursor.close();
        }
    }


    /**
     * 将上一页数据的cursor合并到原cursor
     *
     * @param pageCursor 上一页数据cursor
     */
    public void mergeLastPageCursor(Cursor pageCursor) {
        if (pageCursor == null) {
            return;
        }
        if (dataCursor == null) {
            dataCursor = pageCursor;
        } else {
            Cursor[] cursors = new Cursor[2];
            cursors[0] = pageCursor;
            cursors[1] = dataCursor;
            dataCursor = new MergeCursor(cursors);
        }

        this.notifyDataSetChanged();
    }


    /**
     * 清空数据
     */
    public void clearData() {
        Cursor oldCursor = this.dataCursor;
        this.dataCursor = null;
        this.notifyDataSetChanged();
        if (oldCursor != null) {
            oldCursor.close();
        }
    }


    public void setNoticeType(int type, String groupId) {
        this.noticeType = type;
        this.groupId = groupId;
        this.targetNumber = groupId;
    }


    /**
     * @param context
     * @param c
     */
    public UDTChatListAdapter(Context context, Cursor c, DtNoticesDao noticeDao,
                              String number, String targetShortName) {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
        this.mContext = context;
        this.dataCursor = c;
        this.targetNumber = number;
        this.targetShortName = targetShortName;
        this.layoutInflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.fileTaskMgr = MedicalApplication.getFileTaskManager();
        this.noticeDao = noticeDao;
        this.groupDao = new GroupDao(mContext);
        this.headUrl = AccountManager.getInstance(MedicalApplication.getContext())
            .getAccountInfo().headThumUrl;
        this.userDefaultHeadUrl = IMCommonUtil.getHeadIdBySex(getString(R.string.man));
        int screenWidth = IMCommonUtil.getScreenWidth(context);
        audioWidthS = screenWidth / 4;
        audioWidthL = screenWidth * 2 / 3;
    }


    public void setDTGlobleData(UDTGlobleData data) {
        switch (data.getDoctorType()) {
            case REQUEST: //自己是求诊者，对方为接诊者
                tmp_target_docName = data.getResponseName();
                tmp_target_docHeadUrl = data.getResponseHeadThumUrl();
                break;
            case RESPONSE://自己是接诊者，对方为求诊者
                tmp_target_docName = data.getRequestName();
                tmp_target_docHeadUrl = data.getRequestHeadThumUrl();
                break;
        }

        CustomLog.d(TAG,
            "docName: " + tmp_target_docName + " |docHeadUrl: " + tmp_target_docHeadUrl);
    }


    @Override
    public void bindView(View arg0, Context arg1, Cursor arg2) {
    }


    @Override
    public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
        return null;
    }


    public int getCount() {
        return dataCursor == null ? 0 : dataCursor.getCount();
    }


    public Cursor getItem(int position) {
        if (dataCursor != null) {
            dataCursor.moveToPosition(position);
            return dataCursor;
        } else {
            return null;
        }
    }


    public long getItemId(int position) {
        return position;
    }


    public int getViewTypeCount() {
        return 16;
    }


    /**
     * 获取item类型
     */
    public int getItemViewType(int position) {
        Cursor cursor = getItem(position);
        int type = (Integer) getCursorDataByCol(cursor,
            NoticesTable.NOTICE_COLUMN_TYPE, CURSOR_COL_TYPE_INT);
        String sender = (String) getCursorDataByCol(cursor,
            NoticesTable.NOTICE_COLUMN_SENDER, CURSOR_COL_TYPE_STRING);
        switch (type) {
            case FileTaskManager.NOTICE_TYPE_TXT_SEND:
            case FileTaskManager.NOTICE_TYPE_REMIND_SEND:
            case FileTaskManager.NOTICE_TYPE_REMIND_ONE_SEND:
                return isSendNotice(sender) ? NOTICE_TYPE_SENT_TXT
                                            : NOTICE_TYPE_RECV_TXT;
            case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                return isSendNotice(sender) ? NOTICE_TYPE_SENT_IMAGE
                                            : NOTICE_TYPE_RECV_IMAGE;
            case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                return isSendNotice(sender) ? NOTICE_TYPE_SENT_VIDEO
                                            : NOTICE_TYPE_RECV_VIDEO;
            case FileTaskManager.NOTICE_TYPE_VCARD_SEND:
                return isSendNotice(sender) ? NOTICE_TYPE_SENT_VCARD
                                            : NOTICE_TYPE_RECV_VCARD;
            case FileTaskManager.NOTICE_TYPE_AUDIO_SEND:
                return isSendNotice(sender) ? NOTICE_TYPE_SENT_VOICE
                                            : NOTICE_TYPE_RECV_VOICE;
            case FileTaskManager.NOTICE_TYPE_MEETING_INVITE:

            case FileTaskManager.NOTICE_TYPE_MEETING_BOOK:
                return isSendNotice(sender) ? NOTICE_TYPE_SENT_MEETING
                                            : NOTICE_TYPE_RECV_MEETING;
            case FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND:
                return isSendNotice(sender) ? NOTICE_TYPE_SENT_CHATRECORD
                                            : NOTICE_TYPE_RECV_CHATRECORD;
            case FileTaskManager.NOTICE_TYPE_ARTICAL_SEND:
                return isSendNotice(sender) ? NOTICE_TYPE_SENT_ARTICLE
                                            : NOTICE_TYPE_RECV_ARTICLE;
            default:
                // invalid
                return -1;
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final NoticesBean bean = DtNoticesTable.pureChatCursor(getItem(position),
            noticeType);
        LogUtil.d("getView:position=" + position + "type:" + bean.getType()
            + bean.getBody());
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = createViewByNotice(bean);
            switch (bean.getType()) {
                case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                case FileTaskManager.NOTICE_TYPE_REMIND_SEND:
                case FileTaskManager.NOTICE_TYPE_REMIND_ONE_SEND:
                    holder.noticeTxt = (EmojiconTextView) convertView
                        .findViewById(R.id.tv_chatcontent);
                    holder.runningPb = (ProgressBar) convertView
                        .findViewById(R.id.msg_running_pb);
                    // 链接布局：在handleTextMessage中判断，如果不需要，就隐藏
                    // 注意：layout在接收消息时！=null，对与发送消息，其xml中没有lauout。取出来为null
                    // 因此要保证layout出现的场合必须是接收的消息
                    holder.layout = (LinearLayout) convertView
                        .findViewById(R.id.message_recv);
                    break;
                case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                    // 视频消息
                    holder.durationTxt = (TextView) convertView
                        .findViewById(R.id.duration_txt);
                case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                    // 图片
                    holder.imgLayout = (FrameLayout) convertView
                        .findViewById(R.id.img_frame);
                    holder.imgIv = (ImageView) convertView
                        .findViewById(R.id.img_iv);
                    holder.progressLine = (LinearLayout) convertView
                        .findViewById(R.id.progress_line);
                    holder.progressTxt = (TextView) convertView
                        .findViewById(R.id.progress_txt);
                    holder.imgFrameIv = (ImageView) convertView
                        .findViewById(R.id.img_frame_iv);
                    holder.imgIvMask = (ImageView) convertView
                        .findViewById(R.id.img_iv_mask);
                    break;
                case FileTaskManager.NOTICE_TYPE_VCARD_SEND:
                    // 名片消息
                    holder.vcardBg = (RelativeLayout) convertView
                        .findViewById(R.id.vcard_bg);
                    holder.vcardHeadIv = (ImageView) convertView
                        .findViewById(R.id.vcard_head_iv);
                    holder.vcardNameTxt = (TextView) convertView
                        .findViewById(R.id.vcard_name_txt);
                    holder.vcardNubeTxt = (TextView) convertView
                        .findViewById(R.id.vcard_nube_txt);
                    holder.runningPb = (ProgressBar) convertView
                        .findViewById(R.id.msg_running_pb);
                    break;
                case FileTaskManager.NOTICE_TYPE_AUDIO_SEND:
                    // 音频消息
                    holder.audioBg = (RelativeLayout) convertView
                        .findViewById(R.id.audio_bg);
                    holder.audioIcon = (ImageView) convertView
                        .findViewById(R.id.audio_icon);
                    holder.audioDuration = (TextView) convertView
                        .findViewById(R.id.audio_duration);
                    holder.readStatus = (ImageView) convertView
                        .findViewById(R.id.read_status);
                    holder.runningPb = (ProgressBar) convertView
                        .findViewById(R.id.msg_running_pb);
                    break;
                case FileTaskManager.NOTICE_TYPE_DESCRIPTION:
                    holder.noticeAddTxt = (TextView) convertView
                        .findViewById(R.id.tv_addcontent);
                    break;
                case FileTaskManager.NOTICE_TYPE_RECORD:
                    holder.noticeTxt = (EmojiconTextView) convertView
                        .findViewById(R.id.tv_chatcontent);
                    holder.runningPb = (ProgressBar) convertView
                        .findViewById(R.id.msg_running_pb);
                    holder.layout = (LinearLayout) convertView
                        .findViewById(R.id.message_recv);
                    break;
                case FileTaskManager.NOTICE_TYPE_FILE:
                    holder.vcardBg = (RelativeLayout) convertView
                        .findViewById(R.id.vcard_bg);
                    holder.vcardNameTxt = (TextView) convertView
                        .findViewById(R.id.vcard_name_txt);
                    holder.runningPb = (ProgressBar) convertView
                        .findViewById(R.id.msg_running_pb);
                    holder.vcardNubeTxt = (TextView) convertView
                        .findViewById(R.id.vcard_nube_txt);
                    holder.vcardDetail = (TextView) convertView
                        .findViewById(R.id.meeting_detail);
                    holder.vcardHeadIv = (ImageView) convertView
                        .findViewById(R.id.vcard_head_iv);// icon两个类型显示不一样
                    holder.mProgressBar = (ProgressBar) convertView
                        .findViewById(R.id.file_upload_progress);
                    break;
                case FileTaskManager.NOTICE_TYPE_MEETING_BOOK:
                case FileTaskManager.NOTICE_TYPE_MEETING_INVITE:
                    holder.vcardBg = (RelativeLayout) convertView
                        .findViewById(R.id.vcard_bg);
                    holder.vcardNameTxt = (TextView) convertView
                        .findViewById(R.id.vcard_name_txt);
                    holder.vcardDetail = (TextView) convertView
                        .findViewById(R.id.meeting_detail);
                    holder.runningPb = (ProgressBar) convertView
                        .findViewById(R.id.msg_running_pb);
                    holder.vcardNubeTxt = (TextView) convertView
                        .findViewById(R.id.vcard_nube_txt);
                    holder.vcardHeadIv = (ImageView) convertView
                        .findViewById(R.id.vcard_head_iv);// icon两个类型显示不一样
                    break;
                case FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND:
                    holder.chatrecordbg = (RelativeLayout) convertView
                        .findViewById(R.id.chatrecord_bg);
                    holder.chatRecordTitleTxt = (TextView) convertView
                        .findViewById(R.id.chatrecord_title_txt);
                    holder.chatRecordDetail = (TextView) convertView
                        .findViewById(R.id.chatrecord_detail_txt);
                    holder.runningPb = (ProgressBar) convertView
                        .findViewById(R.id.msg_running_pb);
                    break;
                case FileTaskManager.NOTICE_TYPE_ARTICAL_SEND:
                    holder.articleBg = (RelativeLayout) convertView
                        .findViewById(R.id.article_bg);
                    holder.articleTitle = (TextView) convertView
                        .findViewById(R.id.article_title_txt);
                    holder.articleDetail = (TextView) convertView
                        .findViewById(R.id.article_content_txt);
                    holder.articleImage = (ImageView) convertView
                        .findViewById(R.id.article_image);
                    holder.runningPb = (ProgressBar) convertView
                        .findViewById(R.id.msg_running_pb);
                    break;
                case FileTaskManager.NOTICE_TYPE_ATTACHMENT_FILE:
                    holder.vcardBg = (RelativeLayout) convertView
                        .findViewById(R.id.vcard_bg);
                    holder.vcardNameTxt = (TextView) convertView
                        .findViewById(R.id.vcard_name_txt);
                    holder.runningPb = (ProgressBar) convertView
                        .findViewById(R.id.msg_running_pb);
                    holder.vcardNubeTxt = (TextView) convertView
                        .findViewById(R.id.vcard_nube_txt);
                    holder.vcardDetail = (TextView) convertView
                        .findViewById(R.id.meeting_detail);
                    holder.vcardHeadIv = (ImageView) convertView
                        .findViewById(R.id.vcard_head_iv);// icon两个类型显示不一样
                    holder.mProgressBar = (ProgressBar) convertView
                        .findViewById(R.id.file_upload_progress);
                    break;
            }

            holder.timeTv = (TextView) convertView.findViewById(R.id.timestamp);
            holder.retryBtn = (ImageButton) convertView
                .findViewById(R.id.retry_btn);
            holder.contactIcon = (SharePressableImageView) convertView
                .findViewById(R.id.contact_icon_notice);
            holder.contactName = (TextView) convertView
                .findViewById(R.id.user_name);

            //多选操作
            holder.checkLayout = (RelativeLayout) convertView
                .findViewById(R.id.select_layout);
            holder.checkbox = (CheckBox) convertView
                .findViewById(R.id.linkman_select);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (bean.getType()) {
            case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                handleTextMessage(bean, holder);
                break;
            case FileTaskManager.NOTICE_TYPE_REMIND_SEND:
                handleRemindTxtMessage(bean, holder);
                break;
            case FileTaskManager.NOTICE_TYPE_REMIND_ONE_SEND:
                hanhandleRemindOneTxtMessagedle(bean, holder);
                break;
            case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                handleImageMessage(bean, holder, false);
                break;
            case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                handleImageMessage(bean, holder, true);
                break;
            case FileTaskManager.NOTICE_TYPE_VCARD_SEND:
                handleVcardMessage(bean, holder);
                break;
            case FileTaskManager.NOTICE_TYPE_AUDIO_SEND:
                handleAudioMessage(bean, holder);
                break;
            case FileTaskManager.NOTICE_TYPE_RECORD:
                handleCallRecord(bean, holder);
                break;
            case FileTaskManager.NOTICE_TYPE_MEETING_INVITE:
                handleInviteMeeting(bean, holder);
                break;
            case FileTaskManager.NOTICE_TYPE_MEETING_BOOK:
                handleBookMeeting(bean, holder);
                break;
            case FileTaskManager.NOTICE_TYPE_FILE:
                handleFileMsg(bean, holder);
                break;
            case FileTaskManager.NOTICE_TYPE_DESCRIPTION:
                handleDescription(bean, holder);
                break;
            case FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND:
                handleChatRecordMsg(bean, holder);
                break;
            case FileTaskManager.NOTICE_TYPE_ARTICAL_SEND:
                handleArticleMsg(bean, holder);
                break;
            case FileTaskManager.NOTICE_TYPE_ATTACHMENT_FILE:
                handleAttachFileMsg(bean, holder);
                break;
        }
        if (position == 0) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 20, 0, 10);
            holder.timeTv.setLayoutParams(layoutParams);
        }
        // 重发按钮
        holder.retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }

                if (bean.getStatus() == FileTaskManager.TASK_STATUS_FAIL) {
                    // 失败状态，才有重试按钮
                    CustomLog.i(TAG, "Retry Button Click");
                    startTask(bean);
                }
            }
        });

        // 时间
        if (position == 0) {
            // 第一个消息显示
            holder.timeTv.setVisibility(View.VISIBLE);
            if (bean.getSendTime() == 1 || bean.getSendTime() == 0) {
                holder.timeTv.setText(getDispTimestamp(bean.getReceivedTime()));
                CustomLog.d(ACTIVITY_FLAG, "IM 消息接收时间" + bean.getReceivedTime());
            } else {
                holder.timeTv.setText(getDispTimestamp(bean.getSendTime()));
                CustomLog.d(ACTIVITY_FLAG, "IM 消息发送时间" + bean.getSendTime());
            }
        } else {
            // 两条消息时间离得如果稍长，显示时间
            long lastTime = (Long) getCursorDataByCol(getItem(position - 1),
                NoticesTable.NOTICE_COLUMN_SENDTIME, CURSOR_COL_TYPE_LONG);
            CustomLog.d(ACTIVITY_FLAG, lastTime + "");
            if (lastTime == 1) {
                lastTime = (Long) getCursorDataByCol(getItem(position - 1),
                    NoticesTable.NOTICE_COLUMN_RECEIVEDTIME,
                    CURSOR_COL_TYPE_LONG);
            }
            if (isCloseEnough(lastTime, bean.getSendTime())) {
                holder.timeTv.setVisibility(GONE);
            } else {
                holder.timeTv.setVisibility(View.VISIBLE);
                holder.timeTv.setText(getDispTimestamp(bean.getSendTime()));
            }
        }
        if (holder.checkLayout != null) {
            if (bMultiCheckMode) {
                holder.checkLayout.setVisibility(View.VISIBLE);
                holder.checkbox.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            // TODO Auto-generated method stub
                            onCheckBoxChecked(bean, isChecked);
                        }
                    });
                if (hasChecked(bean.getId())) {
                    holder.checkbox.setChecked(true);
                } else {
                    holder.checkbox.setChecked(false);
                }
            } else {
                holder.checkLayout.setVisibility(GONE);
            }
        }
        return convertView;
    }


    private void startTask(NoticesBean bean) {
        CustomLog.i(TAG, "startTask");

        if (NetConnectHelper.NETWORKTYPE_INVALID == NetConnectHelper.getNetWorkType(mContext)) {
            showToast(R.string.no_network_connect);
            return;
        }

        if (checkDataValid(bean.getType(), bean.getBody())) {
            // 重新开始分享
            fileTaskMgr.addDTTask(bean.getId(), null);
        }
    }


    /**
     * 判断数据是否有效
     */
    private boolean checkDataValid(int type, String body) {
        // remoteUrl为空的场合（还未上传成功），图片、视频、音频的数据，需判断源文件是否存在，不存在，则提示源文件不存在
        if (type == FileTaskManager.NOTICE_TYPE_PHOTO_SEND
            || type == FileTaskManager.NOTICE_TYPE_VEDIO_SEND
            || type == FileTaskManager.NOTICE_TYPE_AUDIO_SEND) {
            // 发送图片、视频、音频
            ButelFileInfo fileInfo = ButelFileInfo.parseJsonStr(body, false);
            if (TextUtils.isEmpty(fileInfo.getRemoteUrl())
                && !isValidFilePath(fileInfo.getLocalPath())) {
                // 无数据源，无法发送
                String fileTxt = mContext.getString(R.string.toast_no_pic);
                if (type == FileTaskManager.NOTICE_TYPE_VEDIO_SEND) {
                    fileTxt = mContext.getString(R.string.toast_no_video);
                } else if (type == FileTaskManager.NOTICE_TYPE_AUDIO_SEND) {
                    fileTxt = mContext.getString(R.string.toast_no_aud);
                }
                showToast(fileTxt);
                return false;
            }
        }

        return true;
    }


    private View createViewByNotice(NoticesBean bean) {
        switch (bean.getType()) {
            case FileTaskManager.NOTICE_TYPE_TXT_SEND:// 文字消息
            case FileTaskManager.NOTICE_TYPE_REMIND_SEND:
            case FileTaskManager.NOTICE_TYPE_REMIND_ONE_SEND:
                return isSendNotice(bean.getSender()) ? layoutInflater.inflate(
                    R.layout.chat_row_sent_message, null) : layoutInflater
                           .inflate(R.layout.chat_row_recv_message, null);
            case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:// 图片消息
                return isSendNotice(bean.getSender()) ? layoutInflater.inflate(
                    R.layout.chat_row_sent_picture, null) : layoutInflater
                           .inflate(R.layout.chat_row_recv_picture, null);
            case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:// 视频消息
                return isSendNotice(bean.getSender()) ? layoutInflater.inflate(
                    R.layout.chat_row_sent_video, null) : layoutInflater
                           .inflate(R.layout.chat_row_recv_video, null);
            case FileTaskManager.NOTICE_TYPE_VCARD_SEND:// 名片消息
                return isSendNotice(bean.getSender()) ? layoutInflater.inflate(
                    R.layout.chat_row_sent_vcard, null) : layoutInflater
                           .inflate(R.layout.chat_row_recv_vcard, null);
            case FileTaskManager.NOTICE_TYPE_AUDIO_SEND:// 音频消息
                return isSendNotice(bean.getSender()) ? layoutInflater.inflate(
                    R.layout.chat_row_sent_audio, null) : layoutInflater
                           .inflate(R.layout.chat_row_recv_audio, null);
            case FileTaskManager.NOTICE_TYPE_DESCRIPTION:
                return layoutInflater.inflate(R.layout.chat_row_add_massege, null);
            case FileTaskManager.NOTICE_TYPE_RECORD:// 通话记录
                return isSendNotice(bean.getSender()) ? layoutInflater.inflate(
                    R.layout.chat_row_sent_message, null) : layoutInflater
                           .inflate(R.layout.chat_row_recv_message, null);
            case FileTaskManager.NOTICE_TYPE_FILE:
                return isSendNotice(bean.getSender()) ? layoutInflater.inflate(
                    R.layout.chat_send_file_layout, null) : layoutInflater
                           .inflate(R.layout.chat_rec_file_layout, null);
            case FileTaskManager.NOTICE_TYPE_MEETING_INVITE:// 会议邀请信息
            case FileTaskManager.NOTICE_TYPE_MEETING_BOOK:// 会议预约信息,布局相同，但icon不同
                return isSendNotice(bean.getSender()) ? layoutInflater.inflate(
                    R.layout.chat_row_send_meeting, null) : layoutInflater
                           .inflate(R.layout.chat_row_recv_meeting, null);
            case FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND:
                return isSendNotice(bean.getSender()) ? layoutInflater.inflate(
                    R.layout.chat_row_send_chatrecord, null) : layoutInflater
                           .inflate(R.layout.chat_row_recv_chatrecord, null);
            case FileTaskManager.NOTICE_TYPE_ARTICAL_SEND:
                return isSendNotice(bean.getSender()) ? layoutInflater.inflate(
                    R.layout.chat_row_send_article, null) : layoutInflater
                           .inflate(R.layout.chat_row_recv_article, null);
            case FileTaskManager.NOTICE_TYPE_ATTACHMENT_FILE:
                return isSendNotice(bean.getSender()) ? layoutInflater.inflate(
                    R.layout.chat_send_file_layout, null) : layoutInflater
                           .inflate(R.layout.chat_rec_file_layout, null);
            default:
                // invalid
                return null;
        }
    }


    private void handleDescription(final NoticesBean bean,
                                   final ViewHolder holder) {
        String text = "";
        try {
            JSONArray bodyArray = new JSONArray(bean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                text = bodyObj.optString("txt");
                if (!TextUtils.isEmpty(text) && text.length() > 3000) {
                    text = text.substring(0, 3000);
                    text = text + "...";
                }
            }
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }
        if (text.contains(groupDao.getString(R.string.send_friend_validation))) {
            SpannableString spanableInfo = new SpannableString(text);
            int startIndex = text.length() - 4;
            spanableInfo.setSpan(new Clickable(clickListener), text.length() - 6, text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.noticeAddTxt.setText(spanableInfo);
            holder.noticeAddTxt.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            holder.noticeAddTxt.setText(text);
        }
    }


    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            callbackIf.addNewFriend();
        }
    };


    class Clickable extends ClickableSpan {
        private final View.OnClickListener mListener;


        public Clickable(View.OnClickListener l) {
            mListener = l;
        }


        /**
         * 重写父类点击事件
         */
        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }


        /**
         * 重写父类updateDrawState方法  我们可以给TextView设置字体颜色,背景颜色等等...
         */
        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(mContext.getResources().getColor(R.color.send_add_friend));
        }
    }


    private void handleChatRecordMsg(final NoticesBean bean, final ViewHolder holder) {
        showStatus(bean, holder);
        // 名片内容
        String detailContentStr = "";
        try {
            JSONArray bodyArray = new JSONArray(bean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                holder.chatRecordTitleTxt.setText(bodyObj.optString("text"));
                JSONArray detailArray = bodyObj.optJSONArray("chatrecordInfo");
                int maxSize = detailArray.length() > 4 ? 4 : detailArray.length();
                for (int i = 0; i < maxSize; i++) {
                    JSONObject tmpObj = detailArray.optJSONObject(i);
                    switch (tmpObj.optInt("type")) {
                        case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                        case FileTaskManager.NOTICE_TYPE_REMIND_SEND:
                        case FileTaskManager.NOTICE_TYPE_REMIND_ONE_SEND:
                            String tmpStr = tmpObj.optString("text");
                            if (tmpStr.length() > 10) {
                                tmpStr = tmpStr.substring(0, 10) + "...";
                            }
                            detailContentStr = detailContentStr + tmpObj.optString("username")
                                + "：" + tmpStr + '\n';
                            break;
                        case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                            detailContentStr = detailContentStr + tmpObj.optString("username")
                                + "：" + getString(R.string.str_video_thread) + '\n';
                            break;
                        case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                            detailContentStr = detailContentStr + tmpObj.optString("username")
                                + "：" + getString(R.string.str_pic_thread) + '\n';
                            break;
                        case FileTaskManager.NOTICE_TYPE_ARTICAL_SEND:
                            detailContentStr = detailContentStr + tmpObj.optString("username")
                                + "：" + getString(R.string.article) + '\n';
                            break;
                        case FileTaskManager.NOTICE_TYPE_VCARD_SEND:
                            detailContentStr = detailContentStr + tmpObj.optString("username")
                                + "：" + getString(R.string.str_vcard_thread) + '\n';
                            break;

                    }
                }
            }
            holder.chatRecordDetail.setText(
                detailContentStr.substring(0, detailContentStr.length() - 1));
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }
        loadHeadImage(bean, holder);

        // 长按
        holder.chatrecordbg.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onLongClickedifMultiSeclect()) {
                    return true;
                }
                // 显示转发/删除 菜单
                MedicalAlertDialog menuDlg = new MedicalAlertDialog(v
                    .getContext());
                if (bean.getStatus() == FileTaskManager.TASK_STATUS_FAIL) {
                    // 失败状态，长按显示重试按钮
                    menuDlg.addButtonFirst(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            if (preJudgment()) {
                                return;
                            }
                            startTask(bean);
                        }
                    }, mContext.getString(R.string.chat_resend));
                } else {
                    menuDlg.addButtonFirst(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            if (preJudgment()) {
                                return;
                            }
                            if (callbackIf != null) {
                                //                                callbackIf.onMsgForward(bean.getId(),
                                //                                        bean.getSender(), bean.getType(),
                                //                                        bean.getStatus(), null);
                                //将文章的title传过去，在转发弹框中显示
                                callbackIf.onMsgForward(bean.getId(),
                                    bean.getSender(), bean.getType(),
                                    bean.getStatus(), null,
                                    holder.chatRecordTitleTxt.getText().toString(), null, null,
                                    null, null, null, null, null);
                            }
                        }
                    }, mContext.getString(R.string.chat_forward));
                }
                menuDlg.addButtonThird(new MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        //兼容收藏时，解析文本用 txt
                        if (checkDataValid(bean.getType(), bean.getBody())) {
                            try {
                                JSONArray bodyArray = new JSONArray(bean.getBody());
                                if (bodyArray != null && bodyArray.length() > 0) {
                                    JSONObject bodyObj = bodyArray.optJSONObject(0);
                                    JSONArray detailArray = bodyObj.optJSONArray("chatrecordInfo");
                                    for (int i = 0; i < detailArray.length(); i++) {
                                        JSONObject tmpObj = detailArray.optJSONObject(i);
                                        if (tmpObj.optInt("type") ==
                                            FileTaskManager.NOTICE_TYPE_TXT_SEND) {
                                            tmpObj.put("txt", tmpObj.optString("text"));
                                        } else if (tmpObj.optInt("type") ==
                                            FileTaskManager.NOTICE_TYPE_ARTICAL_SEND) {
                                            tmpObj.put("offAccLogoUrl",
                                                tmpObj.optString("previewUrl"));
                                        }
                                    }
                                }
                                CollectionManager.getInstance()
                                    .addCollectionByNoticesBean(mContext, bean);
                            } catch (Exception e) {
                                CustomLog.e("ChatListAdapter", "收藏聊天记录，解析json出错");
                            }
                        }
                    }
                }, getString(R.string.collect_str));

                menuDlg.addButtonForth(new MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        if (preJudgment()) {
                            return;
                        }
                        if (callbackIf != null) {
                            callbackIf.onMsgDelete(bean.getId(),
                                bean.getReceivedTime(), getCount());
                            changUIProgressTaskIds.remove(bean.getId());
                            mTaskFileProgressMap.remove(bean.getId());
                        }
                    }
                }, mContext.getString(R.string.chat_delete));

                //更多 按钮
                addMoreItem(menuDlg, bean, 5);
                menuDlg.show();
                return true;
            }
        });

        // 单击查看
        holder.chatrecordbg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickedifMultiSeclect(bean, holder)) {
                    return;
                }
                DataBodyInfo chatRecordInfo = new DataBodyInfo();
                ArrayList<DataBodyInfo> tmpArrayList = new ArrayList<DataBodyInfo>();
                try {
                    JSONArray bodyArray = new JSONArray(bean.getBody());
                    if (bodyArray != null && bodyArray.length() > 0) {
                        JSONObject bodyObj = bodyArray.optJSONObject(0);
                        JSONArray detailArray = bodyObj.optJSONArray("chatrecordInfo");
                        chatRecordInfo.setText(bodyObj.optString("text"));
                        for (int i = 0; i < detailArray.length(); i++) {
                            JSONObject tmpObj = detailArray.optJSONObject(i);
                            DataBodyInfo data = new DataBodyInfo();
                            data.setForwarderName(tmpObj.optString("username"));
                            data.setForwarderHeaderUrl(tmpObj.optString("userheadUrl"));
                            data.setMessageTime(tmpObj.optString("createtime"));
                            switch (tmpObj.optInt("type")) {
                                case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                                    data.setType(FileTaskManager.NOTICE_TYPE_TXT_SEND);
                                    data.setTxt(tmpObj.optString("text"));
                                    break;
                                case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                                    data.setType(FileTaskManager.NOTICE_TYPE_VEDIO_SEND);
                                    data.setRemoteUrl(tmpObj.optString("remoteUrl"));
                                    data.setSize(tmpObj.optLong("size"));
                                    data.setDuration(tmpObj.optInt("duration"));
                                    data.setThumbnailRemoteUrl(tmpObj.optString("thumbnail"));
                                    break;
                                case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                                    data.setType(FileTaskManager.NOTICE_TYPE_PHOTO_SEND);
                                    data.setRemoteUrl(tmpObj.optString("remoteUrl"));
                                    data.setThumbnailRemoteUrl(tmpObj.optString("thumbnail"));
                                    data.setPhotoWidh(tmpObj.optInt("width"));
                                    data.setPhotoHeight(tmpObj.optInt("height"));
                                    break;
                                case FileTaskManager.NOTICE_TYPE_ARTICAL_SEND:
                                    data.setType(FileTaskManager.NOTICE_TYPE_ARTICAL_SEND);
                                    data.setArticleId(tmpObj.optString("articleId"));
                                    data.setTitle(tmpObj.optString("title"));
                                    data.setPreviewUrl(tmpObj.optString("previewUrl"));
                                    data.setIntroduction(tmpObj.optString("introduction"));
                                    data.setArticleType(tmpObj.optInt("articleType"));
                                    data.setName(tmpObj.optString("officeName"));
                                    data.setOffAccLogoUrl(tmpObj.optString("previewUrl"));
                                    data.setIsforwarded(1);
                                    break;
                                case FileTaskManager.NOTICE_TYPE_VCARD_SEND:
                                    data.setType(FileTaskManager.NOTICE_TYPE_VCARD_SEND);
                                    data.setCardname(tmpObj.optString("cardname"));
                                    data.setCardCode(tmpObj.optString("cardCode"));
                                    data.setCardUrl(tmpObj.optString("cardUrl"));
                                    break;
                            }
                            tmpArrayList.add(data);
                        }
                    }
                } catch (Exception e) {

                }
                chatRecordInfo.setCombineInfoList(tmpArrayList);
                chatRecordInfo.setType(FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND);
                Intent intent = new Intent(mContext, ChatCollectionActivity.class);
                intent.putExtra(ChatCollectionActivity.COLLECTION_CHAT_DATA, chatRecordInfo);
                mContext.startActivity(intent);
            }
        });
    }


    private void handleArticleMsg(final NoticesBean bean, final ViewHolder holder) {
        showStatus(bean, holder);
        // 名片内容
        String detailContentStr = "";
        try {
            JSONArray bodyArray = new JSONArray(bean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                JSONObject tmpObj = bodyObj.optJSONObject("articleInfo");

                holder.articleTitle.setText(tmpObj.optString("title"));
                holder.articleDetail.setText(tmpObj.optString("introduction"));

                int articleType = tmpObj.optInt("articleType");
                CustomLog.d(ACTIVITY_FLAG, "articletype:" + articleType);
                //1:文本文章  2:视频文章
                //如果有图片地址  就显示
                String previewUrl = tmpObj.optString("previewUrl");
                if (previewUrl.length() > 0) {
                    holder.articleImage.setVisibility(View.VISIBLE);
                    Glide.with(mContext)
                        .load(tmpObj.optString("previewUrl"))
                        .placeholder(R.drawable.default_link_pic_article)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE).crossFade()
                        .into(holder.articleImage);
                } else {
                    holder.articleImage.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }
        loadHeadImage(bean, holder);

        // 长按
        holder.articleBg.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onLongClickedifMultiSeclect()) {
                    return true;
                }
                // 显示转发/删除 菜单
                MedicalAlertDialog menuDlg = new MedicalAlertDialog(v
                    .getContext());
                if (bean.getStatus() == FileTaskManager.TASK_STATUS_FAIL) {
                    // 失败状态，长按显示重试按钮
                    menuDlg.addButtonFirst(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            if (preJudgment()) {
                                return;
                            }
                            startTask(bean);
                        }
                    }, mContext.getString(R.string.chat_resend));
                } else {
                    menuDlg.addButtonFirst(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            if (preJudgment()) {
                                return;
                            }
                            if (callbackIf != null) {
                                //将文章的title传过去，在转发弹框中显示
                                callbackIf.onMsgForward(bean.getId(),
                                    bean.getSender(), bean.getType(),
                                    bean.getStatus(), null,
                                    holder.articleTitle.getText().toString(), null, null, null,
                                    null, null, null, null);

                            }
                        }
                    }, mContext.getString(R.string.chat_forward));
                }
                menuDlg.addButtonThird(new MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        if (checkDataValid(bean.getType(), bean.getBody())) {
                            JSONArray newBodyArray = new JSONArray();
                            try {
                                JSONObject newBodyObj = new JSONObject();
                                JSONArray bodyArray = new JSONArray(bean.getBody());
                                if (bodyArray != null && bodyArray.length() > 0) {
                                    JSONObject bodyObj = bodyArray.optJSONObject(0);
                                    JSONObject tmpObj = bodyObj.optJSONObject("articleInfo");
                                    newBodyObj.put("ArticleId", tmpObj.optString("articleId"));
                                    newBodyObj.put("title", tmpObj.optString("title"));
                                    newBodyObj.put("previewUrl", tmpObj.optString("previewUrl"));
                                    newBodyObj.put("introduction",
                                        tmpObj.optString("introduction"));
                                    newBodyObj.put("articleType", tmpObj.optInt("articleType"));
                                    newBodyObj.put("name", tmpObj.optString("officeName"));
                                    newBodyObj.put("isforwarded", 1);
                                    newBodyArray.put(0, newBodyObj);
                                }
                            } catch (Exception e) {
                                CustomLog.e(ACTIVITY_FLAG, "articleBg on click" + e.toString());
                            }
                            bean.setBody(newBodyArray.toString());
                            CollectionManager.getInstance()
                                .addCollectionByNoticesBean(mContext, bean);
                        }
                    }
                }, getString(R.string.collect_str));

                menuDlg.addButtonForth(new MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        if (preJudgment()) {
                            return;
                        }
                        if (callbackIf != null) {
                            callbackIf.onMsgDelete(bean.getId(),
                                bean.getReceivedTime(), getCount());
                            changUIProgressTaskIds.remove(bean.getId());
                            mTaskFileProgressMap.remove(bean.getId());
                        }
                    }
                }, mContext.getString(R.string.chat_delete));

                //更多 按钮
                addMoreItem(menuDlg, bean, 5);
                menuDlg.show();
                return true;
            }
        });

        // 单击查看
        holder.articleBg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickedifMultiSeclect(bean, holder)) {
                    return;
                }
                try {
                    JSONArray bodyArray = new JSONArray(bean.getBody());
                    if (bodyArray != null && bodyArray.length() > 0) {
                        JSONObject bodyObj = bodyArray.optJSONObject(0);
                        JSONObject tmpObj = bodyObj.optJSONObject("articleInfo");
                        ;
                        int articleType = tmpObj.optInt("articleType");
                        Intent intent = new Intent();
                        if (articleType == 1) {
                            intent.setClass(mContext, ArticleActivity.class);
                        } else if (articleType == 2) {
                            intent.setClass(mContext, VideoPublishActivity.class);
                        } else {
                            CustomLog.e(ACTIVITY_FLAG, "文章类型错误：" + articleType);
                        }
                        intent.putExtra(INTENT_DATA_ARTICLE_ID, tmpObj.optString("articleId"));
                        mContext.startActivity(intent);
                    }
                } catch (Exception e) {
                    CustomLog.e(ACTIVITY_FLAG, "articleBg on click" + e.toString());
                }
            }
        });
    }


    /**
     * 处理附件类型消息，目前支持 PDF，word
     */
    private void handleAttachFileMsg(final NoticesBean bean,
                                     final UDTChatListAdapter.ViewHolder holder) {

        showStatus(bean, holder);
        loadHeadImage(bean, holder);

        String fileName = "";
        String typeName = "";
        long fileSize = 0;

        try {
            JSONArray bodyInfoJSONArray = new JSONArray(bean.getBody());
            JSONObject bodyInfoJSONObject = bodyInfoJSONArray.getJSONObject(0);
            String remoteUrl = bodyInfoJSONObject.optString("remoteUrl");

            if (remoteUrl.endsWith(".pdf")) {
                typeName = "pdf";
            } else if (remoteUrl.endsWith(".doc")) {
                typeName = "doc";
            } else if (remoteUrl.endsWith(".docx")) {
                typeName = "docx";
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONObject extInfoJSONObject = new JSONObject(bean.getExtInfo());
            fileSize = extInfoJSONObject.optLong("fileSize");
            fileName = extInfoJSONObject.optString("fileName");

        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }

        holder.vcardHeadIv.setBackgroundResource(CollectionFileManager
            .getInstance().getNoticeFileDrawableId(typeName));

        holder.vcardNameTxt.setText(fileName);
        holder.vcardNubeTxt.setText(CollectionFileManager.getInstance().convertStorage(fileSize));
        holder.vcardBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (onItemClickedifMultiSeclect(bean, holder)) {
                    return;
                }
                CollectionFileManager.getInstance()
                    .gotoCollectionFileForNoticeActivity(mContext, bean);
            }
        });

        holder.vcardBg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                if (onLongClickedifMultiSeclect()) {
                    return true;
                }
                return true;
            }
        });

    }


    private void handleFileMsg(final NoticesBean bean,
                               final ViewHolder holder) {

        showStatus(bean, holder);
        loadHeadImage(bean, holder);

        String fileName = "";
        long fileSize = 0;
        String typeName = "";
        String remoteUrl = "";
        try {
            JSONArray bodyArray = new JSONArray(bean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                fileSize = bodyObj.optLong("size");
                fileName = bodyObj.optString("fileName");
                typeName = bodyObj.optString("fileType");
                remoteUrl = bodyObj.optString("remoteUrl");
            }
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }

        if (isSendNotice(bean.getSender())) {

            // 发送的消息才有状态
            switch (bean.getStatus()) {
                case FileTaskManager.TASK_STATUS_SUCCESS:
                    holder.mProgressBar.setVisibility(GONE);
                    detachFileUploadProgressView(holder.mProgressBar, bean.getId());
                    changUIProgressTaskIds.remove(bean.getId());
                    mTaskFileProgressMap.remove(bean.getId());
                    break;
                case FileTaskManager.TASK_STATUS_READY:
                case FileTaskManager.TASK_STATUS_RUNNING:
                case FileTaskManager.TASK_STATUS_COMPRESSING:
                    Float curPro = mTaskFileProgressMap.get(bean.getId());
                    if (curPro != null) {
                        // 初始化进度值
                        int duration = (int) (curPro * 100);
                        holder.mProgressBar.setProgress(duration);
                    } else {
                        holder.mProgressBar.setVisibility(GONE);
                    }
                    if (TextUtils.isEmpty(remoteUrl)) {
                        holder.mProgressBar.setVisibility(View.VISIBLE);
                        // 状态为进行中，需要更新界面进度
                        if (!changUIProgressTaskIds.contains(bean.getId())) {
                            // 文件上传任务
                            changUIProgressTaskIds.add(bean.getId());
                        }
                        attachFileUploadProgressView(holder.mProgressBar, bean.getId());
                    } else {
                        holder.mProgressBar.setVisibility(GONE);
                    }
                    break;
                case FileTaskManager.TASK_STATUS_FAIL:
                    // 重发按钮
                    changUIProgressTaskIds.remove(bean.getId());
                    mTaskFileProgressMap.remove(bean.getId());
                    holder.mProgressBar.setVisibility(GONE);
                    break;
            }
        }

        holder.vcardHeadIv.setBackgroundResource(CollectionFileManager
            .getInstance().getNoticeFileDrawableId(typeName));
        holder.vcardNameTxt.setText(fileName);
        holder.vcardNubeTxt.setText(CollectionFileManager.getInstance().convertStorage(fileSize));
        holder.vcardBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (onItemClickedifMultiSeclect(bean, holder)) {
                    return;
                }
                CollectionFileManager.getInstance()
                    .gotoCollectionFileForNoticeActivity(mContext, bean);
            }
        });

        holder.vcardBg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                if (onLongClickedifMultiSeclect()) {
                    return true;
                }
                // 显示 删除 菜单
                MedicalAlertDialog menuDlg = new MedicalAlertDialog(v
                    .getContext());
                if (bean.getStatus() == FileTaskManager.TASK_STATUS_FAIL) {
                    // 失败状态，长按显示重试按钮
                    menuDlg.addButtonFirst(new BottomMenuWindow.MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            startTask(bean);
                        }
                    }, mContext.getString(R.string.chat_resend));
                } else {
                    menuDlg.addButtonSecond(new BottomMenuWindow.MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            if (preJudgment()) {
                                return;
                            }
                            if (checkDataValid(bean.getType(), bean.getBody()) &&
                                callbackIf != null) {
                                callbackIf.onMsgForward(bean.getId(),
                                    bean.getSender(), bean.getType(),
                                    bean.getStatus(), null);
                            }
                        }
                    }, mContext.getString(R.string.chat_forward));
                }
                menuDlg.addButtonThird(new BottomMenuWindow.MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        if (checkDataValid(bean.getType(), bean.getBody())) {
                            CollectionManager.getInstance()
                                .addCollectionByNoticesBean(mContext, bean);
                        }
                    }
                }, getString(R.string.collect_str));
                menuDlg.addButtonForth(new BottomMenuWindow.MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        if (preJudgment()) {
                            return;
                        }
                        if (callbackIf != null) {
                            callbackIf.onMsgDelete(bean.getId(),
                                bean.getReceivedTime(), getCount());
                        }
                    }
                }, mContext.getString(R.string.chat_delete));
                addMoreItem(menuDlg, bean, 5);
                menuDlg.show();
                return true;
            }
        });

    }


    private void handleInviteMeeting(final NoticesBean bean,
                                     final ViewHolder holder) {
        showStatus(bean, holder);
        holder.vcardHeadIv.setImageResource(R.drawable.m_chat_meet_icon);
        loadHeadImage(bean, holder);

        String showName = "";
        String meetingRoom = "";
        String meetingUrl = "";
        try {
            JSONArray bodyArray = new JSONArray(bean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                meetingRoom = bodyObj.optString("meetingRoom");
                // TODO:liyun 的要求(IOS 发送的URL 仅有域名部分；要求Android保存一致)
                meetingUrl = bodyObj.optString("meetingUrl") + meetingRoom;
                String inviterId = bodyObj.optString("inviterId");
                showName = ShowNameUtil
                    .getShowName(ShowNameUtil.getNameElement(
                        getShowName(inviterId),
                        bodyObj.optString("inviterName"), "", inviterId));

                CustomLog.d(ACTIVITY_FLAG,
                    "立即会议邀请者ID" + inviterId + "," +
                        "预约会议会议室号: " + meetingRoom + "," +
                        "立即会议 URL: " + meetingUrl + ",");
            }
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }

        final String mMeetingNum = meetingRoom;
        holder.vcardDetail.setVisibility(GONE);
        holder.vcardNameTxt.setText(
            showName + getString(R.string.creat_vedio_consultation) + meetingRoom);
        final String showName2 = showName;
        final String meetingRoom2 = meetingRoom;
        holder.vcardNubeTxt.setText(getString(R.string.contact_card_btn));
        final String meetURl = meetingUrl;

        //设置点击事件
        holder.vcardBg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (onItemClickedifMultiSeclect(bean, holder)) {
                    return;
                }

                if (MedicalMeetingManage.getInstance().getActiveMeetingId().equals(mMeetingNum) ||
                    TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())) {
                    final MedicalMeetingManage meetingManager = MedicalMeetingManage.getInstance();

                    int isSuccess = meetingManager.joinMeeting(mMeetingNum,
                        new MedicalMeetingManage.OnJoinMeetingListener() {
                            @Override
                            public void onJoinMeeting(String valueDes, int valueCode) {
                                if (valueCode < 0) {
                                    CustomToast.show(mContext,
                                        mContext.getString(R.string.join_consultation_fail_sigh),
                                        1);
                                }
                            }
                        });

                    if (isSuccess == 0) {
                        // CustomToast.show(mContext, "加入会诊成功", 1);
                    } else if (isSuccess == -9992) {
                        CustomToast.show(mContext, getString(R.string.login_checkNetworkError), 1);
                    } else {
                        CustomToast.show(mContext, getString(R.string.join_consultation_fail_sigh),
                            1);
                    }
                } else {
                    CustomToast.show(mContext, getString(R.string.is_video_meeting) +
                            MedicalMeetingManage.getInstance().getActiveMeetingId(),
                        CustomToast.LENGTH_SHORT);
                }
            }

        });
        //及时会议长按点击事件
        holder.vcardBg.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                if (onLongClickedifMultiSeclect()) {
                    return true;
                }
                // 显示 删除 菜单
                MedicalAlertDialog menuDlg = new MedicalAlertDialog(v
                    .getContext());

                if (bean.getStatus() == FileTaskManager.TASK_STATUS_FAIL) {
                    // 失败状态，长按显示重试按钮
                    menuDlg.addButtonFirst(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            startTask(bean);
                        }
                    }, mContext.getString(R.string.chat_resend));
                } else {
                    menuDlg.addButtonSecond(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            if (preJudgment()) {
                                return;
                            }
                            if (!TextUtils.isEmpty(
                                MedicalMeetingManage.getInstance().getActiveMeetingId())) {
                                CustomToast.show(mContext, getString(R.string.is_video_meeting) +
                                        MedicalMeetingManage.getInstance().getActiveMeetingId(),
                                    CustomToast.LENGTH_SHORT);
                                return;
                            }
                            if (callbackIf != null) {
                                callbackIf.reCreatMeeting();
                            }
                        }
                    }, mContext.getString(R.string.re_create_meeting));
                    menuDlg.addButtonThird(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            if (preJudgment()) {
                                return;
                            }
                            if (callbackIf != null) {
                                callbackIf.onMsgForward(bean.getId(),
                                    bean.getSender(), bean.getType(),
                                    bean.getStatus(), null, null, null, null, showName2,
                                    meetingRoom2, null, null, null);
                            }
                        }
                    }, mContext.getString(R.string.chat_forward));
                    menuDlg.addButtonForth(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            if (preJudgment()) {
                                return;
                            }
                            IMCommonUtil.copy2Clipboard(v.getContext(),
                                holder.vcardNameTxt.getText().toString() + '\n'
                                    + meetURl);
                            showToast(R.string.toast_copy_ok);
                        }
                    }, mContext.getString(R.string.chat_copy));
                }
                menuDlg.addButtonFive(new MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        if (preJudgment()) {
                            return;
                        }
                        if (callbackIf != null) {
                            callbackIf.onMsgDelete(bean.getId(),
                                bean.getReceivedTime(), getCount());
                        }
                    }
                }, mContext.getString(R.string.chat_delete));

                addMoreItem(menuDlg, bean, 6);
                menuDlg.show();
                return true;
            }
        });
    }


    private void handleBookMeeting(final NoticesBean bean,
                                   final ViewHolder holder) {
        showStatus(bean, holder);
        holder.vcardHeadIv.setImageResource(R.drawable.m_chat_meet_book_icon);
        loadHeadImage(bean, holder);
        final BookMeetingExInfo exInfo = new BookMeetingExInfo();
        try {
            JSONArray bodyArray = new JSONArray(bean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                exInfo.setBookNube(bodyObj
                    .optString(BookMeetingExInfo.BOOK_NUBE));
                exInfo.setBookName(ShowNameUtil.getShowName(ShowNameUtil
                    .getNameElement(getShowName(exInfo.getBookNube()),
                        bodyObj.optString(BookMeetingExInfo.BOOK_NAME),
                        "", exInfo.getBookNube())));
                exInfo.setMeetingRoom(bodyObj
                    .optString(BookMeetingExInfo.MEETING_ROOM));// 88888888
                exInfo.setMeetingTheme(bodyObj
                    .optString(BookMeetingExInfo.MEETING_THEME));// 产品部会议
                // 2016/1/16 14:00 IOS要求发送消息时body体为秒
                exInfo.setMeetingTime(bodyObj
                    .optLong(BookMeetingExInfo.MEETING_TIME) * 1000);
                exInfo.setMeetingUrl(bodyObj
                    .optString(BookMeetingExInfo.MEETING_URL));// http://jihuiyi.cn/butel/
            }
            CustomLog.d(ACTIVITY_FLAG,
                "预约会议号" + exInfo.getBookNube() + "," +
                    "预约会议名称: " + exInfo.getBookName() + "," +
                    "预约会议会议室号: " + exInfo.getMeetingRoom() + "," +
                    "预约会议主题: " + exInfo.getMeetingTheme() + "," +
                    "预约会议时间: " + exInfo.getMeetingTime() + "," +
                    "预约会议 URL: " + exInfo.getMeetingUrl() + ",");

        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }
        String describe = exInfo.getBookName()
            + mContext.getString(R.string.order_vedio_consultation)
            + exInfo.getMeetingRoom();

        String meetingDetail =
            mContext.getString(R.string.theme) + exInfo.getMeetingTheme()
                + "\n"
                + mContext.getString(R.string.meeting_time) +
                DateUtil.formatMs2String(exInfo.getMeetingTime(),
                    DateUtil.FORMAT_YYYY_MM_DD_HH_MM_N);
        holder.vcardNameTxt.setText(describe);
        holder.vcardNubeTxt.setText(getString(R.string.reserve_meeting));
        holder.vcardDetail.setVisibility(View.VISIBLE);
        holder.vcardDetail.setText(meetingDetail);
        holder.vcardBg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (onItemClickedifMultiSeclect(bean, holder)) {
                    return;
                }
                Intent i = new Intent(mContext,
                    ReserveSuccessActivity.class);
                i.putExtra(ReserveSuccessActivity.KEY_BOOK_MEETING_EXINFO,
                    exInfo);//跳转到预约详情页面
                mContext.startActivity(i);
            }
        });
        //预约会议 item 长按事件
        holder.vcardBg.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                if (onLongClickedifMultiSeclect()) {
                    return true;
                }
                // 显示 删除 菜单
                MedicalAlertDialog menuDlg = new MedicalAlertDialog(v
                    .getContext());
                if (bean.getStatus() == FileTaskManager.TASK_STATUS_FAIL) {
                    // 失败状态，长按显示重试按钮
                    menuDlg.addButtonFirst(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            startTask(bean);
                        }
                    }, mContext.getString(R.string.chat_resend));
                } else {
                    menuDlg.addButtonSecond(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            if (preJudgment()) {
                                return;
                            }
                            if (callbackIf != null) {
                                callbackIf.reBookMeeting();
                            }
                        }
                    }, mContext.getString(R.string.reBookMeeting));
                    menuDlg.addButtonThird(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            if (preJudgment()) {
                                return;
                            }
                            if (callbackIf != null) {
                                callbackIf.onMsgForward(bean.getId(),
                                    bean.getSender(), bean.getType(),
                                    bean.getStatus(), null, null, null, null, exInfo.getBookName(),
                                    exInfo.getMeetingRoom(), exInfo.getMeetingTheme(),
                                    DateUtil.formatMs2String(exInfo.getMeetingTime(),
                                        DateUtil.FORMAT_YYYY_MM_DD_HH_MM_N), null);
                            }
                        }
                    }, mContext.getString(R.string.chat_forward));

                    menuDlg.addButtonForth(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            //                            MobclickAgent.onEvent(mContext,
                            //                                    UmengEventConstant.EVENT_COPY_MSG);
                            if (preJudgment()) {
                                return;
                            }
                            String tmpStr = holder.vcardNameTxt.getText().toString() + '\n'
                                + holder.vcardDetail.getText().toString() + '\n'
                                + exInfo.getMeetingUrl()
                                + exInfo.getMeetingRoom();
                            IMCommonUtil.copy2Clipboard(v.getContext(), tmpStr);
                            showToast(R.string.toast_copy_ok);
                        }
                    }, mContext.getString(R.string.chat_copy));
                }
                menuDlg.addButtonFive(new MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        //                        MobclickAgent.onEvent(mContext,
                        //                                UmengEventConstant.EVENT_DELETE_MSG);
                        if (preJudgment()) {
                            return;
                        }
                        if (callbackIf != null) {
                            callbackIf.onMsgDelete(bean.getId(),
                                bean.getReceivedTime(), getCount());
                        }
                    }
                }, mContext.getString(R.string.chat_delete));

                addMoreItem(menuDlg, bean, 6);
                menuDlg.show();
                return true;
            }
        });
    }


    private String getShowName(String nube) {
        String name = "";
        if (isSendNotice(nube)) {// 是自己
            name = AccountManager.getInstance(MedicalApplication.getContext()).getName();
        } else {
            if (noticeType == ChatActivity.VALUE_CONVERSATION_TYPE_MULTI) {// 群聊
                name = groupDao.queryGroupMember(groupId, nube).getDispName();
            } else {// 单聊
                name = ShowNameUtil.getShowName(nube);
            }
        }
        return name;

    }


    private void handleCallRecord(final NoticesBean bean,
                                  final ViewHolder holder) {
        loadHeadImage(bean, holder);
        holder.retryBtn.setVisibility(GONE);
        holder.runningPb.setVisibility(GONE);
        holder.noticeTxt.setVisibility(View.VISIBLE);

        String text = "";
        int callType = 0;
        try {
            JSONArray bodyArray = new JSONArray(bean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                text = bodyObj.optString("txt");
                callType = bodyObj.optInt("calltype");
            }
            holder.noticeTxt.setCompoundDrawablePadding(10);
            if (callType == 0) {
                if (isSendNotice(bean.getSender())) {
                    holder.noticeTxt.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.m_chat_audio_call_send, 0, 0, 0);
                } else {
                    holder.layout.setVisibility(GONE);
                    holder.noticeTxt.setCompoundDrawablesWithIntrinsicBounds(0,
                        0, R.drawable.m_chat_audio_call_receiver, 0);
                }
            } else {
                if (isSendNotice(bean.getSender())) {
                    holder.noticeTxt.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.m_chat_vedio_call_send, 0, 0, 0);
                } else {
                    holder.layout.setVisibility(GONE);
                    holder.noticeTxt.setCompoundDrawablesWithIntrinsicBounds(0,
                        0, R.drawable.m_chat_vedio_call_receiver, 0);
                }
            }
            holder.noticeTxt.setText(text);

            final int type = callType;
            holder.noticeTxt.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    if (onItemClickedifMultiSeclect(bean, holder)) {
                        return;
                    }
                    if (preJudgment()) {
                        return;
                    }
                    //                    MobclickAgent.onEvent(mContext,
                    //                            UmengEventConstant.EVENT_REPLAY_CALL);
                    //                    if (1 == type) {
                    //                        OutCallUtil.makeNormalCall((Activity) mContext,
                    //                                targetNumber, OutCallUtil.CT_SIP_AV, "", "");
                    //                    } else {
                    //                        OutCallUtil.makeNormalCall((Activity) mContext,
                    //                                targetNumber, OutCallUtil.CT_SIP_AUDIO, "", "");
                    //                    }
                }
            });

            holder.noticeTxt.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if (onLongClickedifMultiSeclect()) {
                        return true;
                    }
                    // 显示 删除 菜单
                    MedicalAlertDialog menuDlg = new MedicalAlertDialog(v
                        .getContext());
                    menuDlg.addButtonFirst(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            //                            MobclickAgent.onEvent(mContext,
                            //                                    UmengEventConstant.EVENT_DELETE_MSG);
                            if (preJudgment()) {
                                return;
                            }
                            if (callbackIf != null) {
                                callbackIf.onMsgDelete(bean.getId(),
                                    bean.getReceivedTime(), getCount());
                            }
                        }
                    }, mContext.getString(R.string.chat_delete));
                    addMoreItem(menuDlg, bean, 2);
                    menuDlg.show();
                    return true;
                }
            });
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }

    }


    private void handleRemindTxtMessage(final NoticesBean bean, final ViewHolder holder) {
        loadHeadImage(bean, holder);

        String text = "";
        try {
            JSONArray bodyArray = new JSONArray(bean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                text = bodyObj.optString("text");
                holder.noticeTxt.setText(text);

                bean.setType(FileTaskManager.NOTICE_TYPE_TXT_SEND);

                JSONObject newBodyObj = new JSONObject();
                newBodyObj.put("txt", text);
                bodyArray.put(0, newBodyObj);
                bean.setBody(bodyArray.toString());

                onTextLongClick(holder.noticeTxt, bean,
                    false, holder);

                //                接收的消息才需要考虑链接的图文显示
                //                if (!isSendNotice(bean.getSender())) {
                //                    // 在layout set visible之前remove
                //                    holder.layout.removeAllViews();
                //                    holder.noticeTxt.setVisibility(View.VISIBLE);
                //                    holder.layout.setVisibility(GONE);
                //                }

                holder.layout.removeAllViews();
                holder.noticeTxt.setVisibility(View.VISIBLE);
                holder.layout.setVisibility(GONE);
            }
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }
        showStatus(bean, holder);
    }


    private void hanhandleRemindOneTxtMessagedle(final NoticesBean bean, final ViewHolder holder) {
        loadHeadImage(bean, holder);

        String text = "";
        try {
            JSONArray bodyArray = new JSONArray(bean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                text = bodyObj.optString("text");
                text = getNewTextMsg(text);
                holder.noticeTxt.setText(text);

                bean.setType(FileTaskManager.NOTICE_TYPE_TXT_SEND);

                JSONObject newBodyObj = new JSONObject();
                newBodyObj.put("txt", bodyObj.optString("text"));
                bodyArray.put(0, newBodyObj);
                bean.setBody(bodyArray.toString());
                onTextLongClick(holder.noticeTxt, bean,
                    false, holder);

                // 接收的消息才需要考虑链接的图文显示
                //                if (!isSendNotice(bean.getSender())) {
                //                    // 在layout set visible之前remove
                //                    holder.layout.removeAllViews();
                //                    holder.noticeTxt.setVisibility(View.VISIBLE);
                //                    holder.layout.setVisibility(GONE);
                //                }

                holder.layout.removeAllViews();
                holder.noticeTxt.setVisibility(View.VISIBLE);
                holder.layout.setVisibility(GONE);
            }
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }
        showStatus(bean, holder);
    }


    private void handleTextMessage(final NoticesBean bean,
                                   final ViewHolder holder) {
        loadHeadImage(bean, holder);

        String text = "";
        String pageData = "";
        try {
            JSONArray bodyArray = new JSONArray(bean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                String oriText = bodyObj.optString("txt");
                text = oriText;
                pageData = bodyObj.optString("webData");
                LogUtil.d("handleTextMessage 处理文字消息 " + text);
                if (!TextUtils.isEmpty(text) && text.length() > 3000) {
                    text = text.substring(0, 3000);
                    text = text + "...";
                }

                CustomLog.i(TAG, "notice txt = " + text);
                text = getNewTextMsg(text);

                holder.noticeTxt.setText(text);
                // 取出文字中的url信息
                List<String> urls = HtmlParseManager.getInstance().getUrls(text);

                // 1.发送的消息，有链接时，修改noticeTxt的显示规则
                // 2.接收的消息，有链接时，在解析结束前，在noticeTxt上显示，需修改其显示规则
                // 显示规则：点击url，进入内嵌的web浏览器
                if (urls.size() > 0) {
                    LogUtil.d("消息中有链接，修改其显示规则");

                    HtmlParseManager.getInstance().changeShowRules(
                        holder.noticeTxt, new OnClickBack() {
                            @Override
                            public void OnClick(String mUrl) {
                                //判断是否为会议链接

                                if (mUrl.contains(MedicalMeetingManage.JMEETING_INVITE_URL)) {
                                    String meetingId = mUrl.substring(
                                        MedicalMeetingManage.JMEETING_INVITE_URL.length());
                                    meetingLinkClickListener.meetingLinkClick(meetingId);
                                } else {
                                    Intent intent = new Intent(mContext,
                                        EmbedWebViewActivity.class);
                                    intent.putExtra(
                                        EmbedWebViewActivity.KEY_PARAMETER_URL,
                                        mUrl);
                                    intent.putExtra(
                                        EmbedWebViewActivity.KEY_PARAMETER_TITLE,
                                        ACTIVITY_FLAG);
                                    mContext.startActivity(intent);
                                }

                            }
                        });
                }

                onTextLongClick(holder.noticeTxt, bean,
                    !isSendNotice(bean.getSender()) && urls.size() > 0, holder);

                LogUtil.d("urls.size=" + urls.size() + "\nurls="
                    + urls.toString());
                LogUtil.d("webData=" + pageData);

                // 接收的消息才需要考虑链接的图文显示  发送端也要显示链接的图文
                if (true) {

                    // 在layout set visible之前remove
                    holder.layout.removeAllViews();
                    if (urls.size() > 0) {

                        // 首先显示链接文本，因为链接解析需要时间，防止短暂出现一条空白item
                        holder.noticeTxt.setVisibility(View.VISIBLE);
                        holder.layout.setVisibility(GONE);

                        if (TextUtils.isEmpty(pageData)) {
                            LogUtil.d("webData为空，将urls加入任务队列解析");
                            // parseHtmlThread()方法返回false时，表示本次没有成功解析(网络不好的时候)，直接
                            // 作为字符串显示出来
                            HtmlParseManager.getInstance()
                                .parseHtmlToNoticesThread(bean.getId(),
                                    urls, oriText);
                        } else {
                            // pageData不为空，直接取出来 显示
                            LogUtil.d("webData不为空，直接取出来显示");
                            JSONArray pageArray = new JSONArray(pageData);
                            List<WebpageBean> webPages = null;
                            webPages = HtmlParseManager.getInstance()
                                .convertWebpageBean(pageArray);

                            // 文字区域
                            View txtView;
                            TextView txt;

                            WebpageBean pageBean = null;

                            for (int i = 0; i < webPages.size(); i++) {
                                pageBean = webPages.get(i);

                                // 链接区域
                                View linkView;
                                View picTxt;
                                XCRoundImageViewByXfermode imgView;
                                TextView dscrView;
                                TextView titleView;
                                TextView urlView;

                                // 循环每个链接
                                final String srcUrl = pageBean.getSrcUrl();

                                // 显示链接前的文字
                                txtView = layoutInflater.inflate(
                                    R.layout.text_item, null);
                                txt = (TextView) txtView
                                    .findViewById(R.id.tv_chatcontent);
                                // 分隔线
                                View line1 = txtView
                                    .findViewById(R.id.line_bottom);

                                if (!TextUtils.isEmpty(pageBean.getHeaderStr())) {
                                    txt.setText(pageBean.getHeaderStr());
                                    holder.layout.addView(txtView);
                                    line1.setVisibility(View.VISIBLE);
                                }
                                // 显示链接
                                linkView = layoutInflater.inflate(
                                    R.layout.link_item, null);
                                // 如果解析不了，则直接显示链接字符串
                                urlView = (TextView) linkView
                                    .findViewById(R.id.link_addr);
                                picTxt = linkView.findViewById(R.id.pic_txt);
                                titleView = (TextView) linkView
                                    .findViewById(R.id.msg_title);
                                dscrView = (TextView) linkView
                                    .findViewById(R.id.msg_abstract);
                                imgView = (XCRoundImageViewByXfermode) linkView
                                    .findViewById(R.id.img);
                                imgView.setType(XCRoundImageViewByXfermode.TYPE_ROUND);
                                imgView.setRoundBorderRadius(10);

                                // 点击，跳入网页
                                // TODO:链接区域长按事件-
                                onTextLongClick(linkView, bean, true, holder);

                                linkView.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        LogUtil.d("linkView is click");
                                        if (srcUrl.contains(
                                            MedicalMeetingManage.JMEETING_INVITE_URL)) {
                                            String meetingId = srcUrl.substring(
                                                MedicalMeetingManage.JMEETING_INVITE_URL.length());
                                            meetingLinkClickListener.meetingLinkClick(meetingId);
                                        } else {
                                            Intent intent = new Intent(mContext,
                                                EmbedWebViewActivity.class);
                                            intent.putExtra(
                                                EmbedWebViewActivity.KEY_PARAMETER_URL,
                                                srcUrl);
                                            intent.putExtra(
                                                EmbedWebViewActivity.KEY_PARAMETER_TITLE,
                                                ACTIVITY_FLAG);
                                            mContext.startActivity(intent);
                                        }
                                    }
                                });
                                if (!pageBean.isValid()) {
                                    LogUtil.d("is not valid ");
                                    // 标题摘要都为空，直接显示链接str
                                    urlView.setVisibility(View.VISIBLE);
                                    picTxt.setVisibility(GONE);

                                    urlView.setText(pageBean.getSrcUrl());

                                    HtmlParseManager.getInstance()
                                        .changeShowRules(urlView,
                                            new OnClickBack() {
                                                @Override
                                                public void OnClick(
                                                    String mUrl) {
                                                    if (mUrl.contains(
                                                        MedicalMeetingManage.JMEETING_INVITE_URL)) {
                                                        String meetingId = mUrl.substring(
                                                            MedicalMeetingManage.JMEETING_INVITE_URL
                                                                .length());
                                                        meetingLinkClickListener.meetingLinkClick(
                                                            meetingId);
                                                    } else {
                                                        Intent intent = new Intent(
                                                            mContext,
                                                            EmbedWebViewActivity.class);
                                                        intent.putExtra(
                                                            EmbedWebViewActivity.KEY_PARAMETER_URL,
                                                            mUrl);
                                                        intent.putExtra(
                                                            EmbedWebViewActivity.KEY_PARAMETER_TITLE,
                                                            ACTIVITY_FLAG);
                                                        mContext.startActivity(intent);
                                                    }
                                                }
                                            });

                                    // urlView绑定长按事件
                                    onTextLongClick(linkView, bean, true, holder);
                                } else {
                                    LogUtil.d("is valid");
                                    // 标题为空
                                    urlView.setVisibility(GONE);
                                    picTxt.setVisibility(View.VISIBLE);
                                    titleView.setText(pageBean.getTitle());
                                    dscrView.setText(pageBean.getDescription());
                                    if (TextUtils.isEmpty(pageBean.getImgUrl())) {
                                        imgView.setImageResource(R.drawable.default_link_pic);
                                    } else {
                                        // 下载时，显示一张默认图片
                                        Glide.with(mContext)
                                            .load(pageBean.getImgUrl())
                                            .placeholder(
                                                R.drawable.default_link_pic)
                                            .error(R.drawable.default_link_pic)
                                            .centerCrop()
                                            .diskCacheStrategy(
                                                DiskCacheStrategy.SOURCE)
                                            .crossFade().into(imgView);
                                    }
                                }
                                holder.layout.addView(linkView);
                                // 链接文字
                                if (i < webPages.size() - 1) {
                                    View line = linkView
                                        .findViewById(R.id.divider_line);
                                    line.setVisibility(View.VISIBLE);
                                }
                                // 内容加载完成，才显示图文信息
                                holder.noticeTxt.setVisibility(GONE);
                                holder.layout.setVisibility(View.VISIBLE);
                            }
                            //                            LogUtil.d("显示完链接之后的text=" + text);
                            if (pageBean != null && !TextUtils.isEmpty(pageBean.getFooterStr())) {
                                // 最后一个链接后面还有文字，显示出来
                                txtView = layoutInflater.inflate(
                                    R.layout.text_item, null);
                                txt = (TextView) txtView
                                    .findViewById(R.id.tv_chatcontent);
                                txt.setText(pageBean.getFooterStr());
                                holder.layout.addView(txtView);
                                View line = txtView.findViewById(R.id.line_top);
                                line.setVisibility(View.VISIBLE);
                            }
                            // TODO:链接区域长按事件---转发/复制/删除
                            onTextLongClick(holder.layout, bean, true, holder);
                        }
                    } else {
                        holder.noticeTxt.setVisibility(View.VISIBLE);
                        holder.layout.setVisibility(GONE);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }
        showStatus(bean, holder);
    }


    private void handleImageMessage(final NoticesBean bean,
                                    final ViewHolder holder, final boolean isVideo) {

        ViewGroup.LayoutParams params = holder.imgIv.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        holder.imgIv.setLayoutParams(params);
        // 图片内容
        final ButelFileInfo fileInfo = ButelFileInfo.parseJsonStr(bean
            .getBody(), false);

        // 图片显示大小
        int[] imgSize = null;

        imgSize = caculateImgSize(fileInfo.getWidth(), fileInfo.getHeight());
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.imgLayout
            .getLayoutParams();
        lp.width = IMCommonUtil.getDimensionX(mContext, imgSize[0]);//imgSize[0];
        lp.height = IMCommonUtil.getDimensionY(mContext, imgSize[1]);//height;//imgSize[1];
        holder.imgLayout.setLayoutParams(lp);
        final boolean isSend = isSendNotice(bean.getSender());
        loadHeadImage(bean, holder);

        // 显示图片
        showImage(isSend, isVideo, fileInfo.getLocalPath(),
            fileInfo.getThumbUrl(), fileInfo.getRemoteUrl(), holder.imgIv,
            fileInfo.getWidth(), fileInfo.getHeight());

        // 视频时长
        if (isVideo) {
            holder.durationTxt.setText(getDispDuration(fileInfo.getDuration()));
        }

        // 显示状态
        if (isSend) {
            // 发送的消息才有状态
            switch (bean.getStatus()) {
                case FileTaskManager.TASK_STATUS_SUCCESS:
                    holder.progressTxt.setText("0%");
                    holder.progressLine.setVisibility(GONE);
                    holder.imgIvMask.setVisibility(GONE);
                    holder.retryBtn.setVisibility(GONE);

                    changUIProgressTaskIds.remove(bean.getId());
                    mTaskFileProgressMap.remove(bean.getId());
                    detachUploadProgressView(holder.progressTxt, bean.getId());
                    break;
                case FileTaskManager.TASK_STATUS_READY:
                case FileTaskManager.TASK_STATUS_RUNNING:
                case FileTaskManager.TASK_STATUS_COMPRESSING:
                    holder.progressLine.setVisibility(View.VISIBLE);
                    holder.imgIvMask.setVisibility(View.VISIBLE);
                    holder.retryBtn.setVisibility(GONE);

                    Float curPro = mTaskFileProgressMap.get(bean.getId());
                    if (curPro != null) {
                        // 初始化进度值
                        NumberFormat numFormat = NumberFormat.getNumberInstance();
                        numFormat.setMaximumFractionDigits(2);
                        holder.progressTxt.setText(numFormat.format(curPro
                            .floatValue() * 100) + "%");
                    } else {
                        holder.progressTxt.setText("0%");
                    }

                    // 状态为进行中，需要更新界面进度
                    if (!changUIProgressTaskIds.contains(bean.getId())) {
                        // 文件上传任务
                        changUIProgressTaskIds.add(bean.getId());
                    }
                    attachUploadProgressView(holder.progressTxt, bean.getId());
                    break;
                case FileTaskManager.TASK_STATUS_FAIL:
                    // 重发按钮
                    holder.retryBtn.setVisibility(View.VISIBLE);
                    holder.progressTxt.setText("0%");
                    holder.progressLine.setVisibility(GONE);
                    holder.imgIvMask.setVisibility(GONE);

                    changUIProgressTaskIds.remove(bean.getId());
                    mTaskFileProgressMap.remove(bean.getId());
                    detachUploadProgressView(holder.progressTxt, bean.getId());
                    break;
            }
        } else {
            holder.progressTxt.setText("0%");
            holder.progressLine.setVisibility(GONE);
            holder.imgIvMask.setVisibility(GONE);
            holder.retryBtn.setVisibility(GONE);
        }

        // 单击查看
        holder.imgFrameIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickedifMultiSeclect(bean, holder)) {
                    return;
                }

                int type = -1;
                int index = -1;
                ArrayList<PhotoBean> photos = null;
                if (isVideo) {
                    type = FileTaskManager.NOTICE_TYPE_VEDIO_SEND;
                } else {
                    type = FileTaskManager.NOTICE_TYPE_PHOTO_SEND;
                }

                photos = getAllPAVFileList(bean.getThreadsId(), type,
                    bean.getId());
                index = getIndexFromList(photos, bean.getId());

                Intent i = new Intent(mContext, ViewUDTPhotosActivity.class);
                i.putParcelableArrayListExtra(
                    ViewUDTPhotosActivity.KEY_PHOTOS_LIST, photos);
                i.putExtra(ViewUDTPhotosActivity.KEY_PHOTOS_SELECT_INDEX, index);
                i.putExtra(ViewUDTPhotosActivity.KEY_REMOTE_FILE, true);
                i.putExtra(ViewUDTPhotosActivity.KEY_VIDEO_FILE, isVideo);
                i.putExtra(ViewUDTPhotosActivity.KEY_VIDEO_LEN,
                    fileInfo.getDuration());
                i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.putExtra(ViewUDTPhotosActivity.KEY_COLLECTION_SCAN, true);
                mContext.startActivity(i);

            }
        });
    }


    private void playSlientVideo(ButelFileInfo videoInfo, NoticesBean bean) {

        int type = -1;
        int index = -1;
        ArrayList<PhotoBean> photos = null;

        photos = getAllPAVFileList(bean.getThreadsId(), type,
            bean.getId());
        index = getIndexFromList(photos, bean.getId());

        Intent i = new Intent(mContext, ViewPhotosActivity.class);
        i.putParcelableArrayListExtra(
            ViewPhotosActivity.KEY_PHOTOS_LIST, photos);
        i.putExtra(ViewPhotosActivity.KEY_PHOTOS_SELECT_INDEX, index);
        i.putExtra(ViewPhotosActivity.KEY_REMOTE_FILE, true);
        i.putExtra(ViewPhotosActivity.KEY_COLLECTION_SCAN, true);
        i.putExtra(ViewPhotosActivity.SILENT_PLAY, true);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mContext.startActivity(i);
    }


    /**
     * 获得会话中相同类型的所有文件信息; 如果是视频，则只获取指定的一条记录;
     *
     * @param threadId 会话的id
     * @param type 消息类型（图片、视频、声音）
     * @param vedioUuid 消息的id
     * @return 文件信息list
     */
    private ArrayList<PhotoBean> getAllPAVFileList(String threadId, int type,
                                                   String vedioUuid) {
        DtNoticesDao noticeDao = new DtNoticesDao(mContext);

        ArrayList<PhotoBean> photolist = null;
        PhotoBean bean = null;
        NoticesBean item = null;
        List<NoticesBean> rawlist = null;

        rawlist = noticeDao.getAllPicAndVideoInConversation(threadId);

        if (rawlist != null) {
            photolist = new ArrayList<PhotoBean>();
            int length = rawlist.size();
            for (int i = 0; i < length; i++) {
                item = rawlist.get(i);
                String body = item.getBody();
                String thumbUrl = "";
                String localUrl = "";
                String remoteUrl = "";
                String id = item.getId();
                boolean isfrom = !isSendNotice(item.getSender());
                try {
                    JSONArray array = new JSONArray(body);
                    if (array != null && array.length() > 0) {
                        for (int j = 0; j < array.length(); j++) {
                            JSONObject obj = array.getJSONObject(j);
                            thumbUrl = obj.optString("thumbnail");
                            localUrl = obj.optString("localUrl");
                            remoteUrl = obj.optString("remoteUrl");

                            bean = new PhotoBean();
                            bean.setLittlePicUrl(thumbUrl);
                            bean.setLocalPath(localUrl);
                            bean.setRemoteUrl(remoteUrl);
                            bean.setTaskId(id);
                            bean.setType(item.getType());
                            bean.setFrom(isfrom);

                            photolist.add(bean);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
        return photolist;
    }


    /**
     * 获得某个记录在同类记录中的index,从0开始
     *
     * @param photoList 上面方法中获得的同类消息的list
     * @param uuid 消息的id
     */
    private int getIndexFromList(ArrayList<PhotoBean> photoList, String uuid) {

        if (TextUtils.isEmpty(uuid)) {
            return -1;
        }

        if (photoList != null && photoList.size() > 0) {
            int length = photoList.size();
            PhotoBean item = null;
            for (int i = 0; i < length; i++) {
                item = photoList.get(i);
                if (item.getTaskId().equals(uuid)) {
                    return i;
                }
            }
        }

        return -1;
    }


    private void handleVcardMessage(final NoticesBean bean,
                                    final ViewHolder holder) {
        showStatus(bean, holder);
        // 名片内容
        try {
            JSONArray bodyArray = new JSONArray(bean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                holder.nubeNumber = bodyObj.optString("code");
                holder.nickName = bodyObj.optString("name");
                holder.phoneNum = bodyObj.optString("phone");
                holder.headUrl = bodyObj.optString("url");
                holder.sex = bodyObj.optString("sex");
            }
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }
        loadHeadImage(bean, holder);

        holder.vcardHeadIv.setImageResource(IMCommonUtil
            .getHeadIdBySex(holder.sex));
        Glide.with(mContext).load(holder.headUrl)
            .placeholder(IMCommonUtil.getHeadIdBySex(holder.sex))
            .error(IMCommonUtil.getHeadIdBySex(holder.sex)).centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.SOURCE).crossFade()
            .into(holder.vcardHeadIv);

        ShowNameUtil.NameElement element = ShowNameUtil.getNameElement("", holder.nickName,
            holder.phoneNum, holder.nubeNumber);
        holder.vcardNameTxt.setText(ShowNameUtil.getShowName(element));
        holder.vcardNubeTxt.setText(ShowNameUtil.getShowNumber(element));

        // 长按
        holder.vcardBg.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onLongClickedifMultiSeclect()) {
                    return true;
                }
                // 显示转发/删除 菜单
                MedicalAlertDialog menuDlg = new MedicalAlertDialog(v
                    .getContext());
                if (bean.getStatus() == FileTaskManager.TASK_STATUS_FAIL) {
                    // 失败状态，长按显示重试按钮
                    menuDlg.addButtonFirst(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            if (preJudgment()) {
                                return;
                            }
                            startTask(bean);
                        }
                    }, mContext.getString(R.string.chat_resend));
                } else {
                    menuDlg.addButtonFirst(new MenuClickedListener() {
                        @Override
                        public void onMenuClicked() {
                            if (preJudgment()) {
                                return;
                            }
                            if (callbackIf != null) {
                                callbackIf.onMsgForward(bean.getId(),
                                    bean.getSender(), bean.getType(),
                                    bean.getStatus(), null, null, holder.nickName,
                                    holder.nubeNumber, null, null, null, null, null);
                            }
                        }
                    }, mContext.getString(R.string.chat_forward));
                }
                menuDlg.addButtonSecond(new MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        if (preJudgment()) {
                            return;
                        }
                        if (callbackIf != null) {
                            callbackIf.onMsgDelete(bean.getId(),
                                bean.getReceivedTime(), getCount());
                        }
                    }
                }, mContext.getString(R.string.chat_delete));
                addMoreItem(menuDlg, bean, 3);
                menuDlg.show();
                return true;
            }
        });

        // 单击查看
        holder.vcardBg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickedifMultiSeclect(bean, holder)) {
                    return;
                }

                Intent intent = new Intent(v.getContext(), ContactCardActivity.class);
                intent.putExtra("nubeNumber", holder.nubeNumber);
                intent.putExtra("searchType", "5"); // 5:群内添加
                if (groupId.length() > 0) {
                    intent.putExtra("groupName", groupDao.getGroupNameByGid(groupId));
                }
                mContext.startActivity(intent);
            }
        });
    }


    private void handleAudioMessage(final NoticesBean bean,
                                    final ViewHolder holder) {
        // 音频内容
        int duration = 0;
        try {
            JSONArray bodyArray = new JSONArray(bean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                duration = bodyObj.optInt("duration");
            }
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }

        holder.audioDuration.setText(duration + "''");
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.audioBg
            .getLayoutParams();

        if (duration <= 20) {
            lp.width = audioWidthS;
        } else if (duration >= 60) {
            lp.width = audioWidthL;
        } else {
            lp.width = audioWidthS + (duration - 20)
                * ((audioWidthL - audioWidthS) / 40);
        }
        holder.audioBg.setLayoutParams(lp);

        boolean isSend = isSendNotice(bean.getSender());

        if (bean.getId().equals(curPlayingAuMsgId)) {
            // 当前音频正在播放，继续显示其播放动画
            int playRes = 0;
            if (isSend) {
                holder.audioIcon.setBackgroundResource(R.drawable.audio_right_playing);
            } else {
                holder.audioIcon.setBackgroundResource(R.drawable.audio_left_playing);
            }
            final AnimationDrawable drawable = (AnimationDrawable) holder.audioIcon
                .getBackground();
            holder.audioIcon.post(new Runnable() {
                @Override
                public void run() {
                    drawable.start();
                }
            });
        } else {
            // 不在播放，显示静态图标
            int iconRes = 0;
            if (isSend) {
                iconRes = R.drawable.audio_right_icon_3;
            } else {
                iconRes = R.drawable.audio_left_icon_3;
            }
            holder.audioIcon.setBackgroundResource(iconRes);
        }

        loadHeadImage(bean, holder);
        showStatus(bean, holder);

        if (!isSend && bean.getIsRead() > 0) {
            // 还未收听的场合，显示小圆点
            holder.readStatus.setVisibility(View.VISIBLE);
        } else {
            holder.readStatus.setVisibility(View.INVISIBLE);
        }

        // 长按事件
        holder.audioBg.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                if (onLongClickedifMultiSeclect()) {
                    return true;
                }

                //长按时停止播放其他音频
                stopOthersPlay(bean);

                //构造 Dialog
                buildOperateDialog(v, bean);
                return true;
            }
        });

        // 单击事件
        holder.audioBg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                if (onItemClickedifMultiSeclect(bean, holder)) {
                    return;
                }
                // 当前音频未收听过，更新为已收听音频
                if (bean.getIsRead() > 0) {
                    noticeDao.updateAudioIsRead(bean.getId(), true);
                }

                if (bean.getId().equals(curPlayingAuMsgId)) {
                    // 当前音频正在播放，则停止播放
                    stopCurAuPlaying();
                } else {
                    if (!TextUtils.isEmpty(curPlayingAuMsgId)) {
                        // 当前其他音频正在播放，先停止播放的音频
                        stopCurAuPlaying();
                    }
                    // 开始播放点击的音频

                    startAuPlaying(bean, holder.audioIcon);
                }
            }
        });
    }


    private void buildOperateDialog(View v, final NoticesBean bean) {
        MedicalAlertDialog menuDlg = new MedicalAlertDialog(v
            .getContext());
        if (bean.getStatus() == FileTaskManager.TASK_STATUS_FAIL) {
            // 失败状态，长按显示重试按钮
            menuDlg.addButtonFirst(new MenuClickedListener() {
                @Override
                public void onMenuClicked() {
                    startTask(bean);
                }
            }, mContext.getString(R.string.chat_resend));
        }

        String text = getDialogItemText();

        menuDlg.addButtonSecond(new MenuClickedListener() {
            @Override
            public void onMenuClicked() {

                if (getVoicePref() == SPEAKER) {   //扬声器模式
                    writePrefTo(HEADSET);
                    IMCommonUtil.makeModeChangeToast(mContext, getString(R.string.mode_headset),
                        R.drawable.headset_on);

                } else {                           //听筒模式
                    writePrefTo(SPEAKER);
                    IMCommonUtil.makeModeChangeToast(mContext, getString(R.string.mode_speaker),
                        R.drawable.speaker_on);
                }

            }
        }, text);
        menuDlg.addButtonThird(new MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                if (checkDataValid(bean.getType(), bean.getBody())) {
                    CollectionManager.getInstance()
                        .addCollectionByNoticesBean(mContext, bean);
                }
            }
        }, getString(R.string.collect_str));

        menuDlg.addButtonForth(new MenuClickedListener() {
            @Override
            public void onMenuClicked() {

                if (callbackIf != null) {
                    if (bean.getId().equals(curPlayingAuMsgId)) {
                        // 当前音频正在播放，则停止播放
                        stopCurAuPlaying();
                    }
                    callbackIf.onMsgDelete(bean.getId(),
                        bean.getReceivedTime(), getCount());
                }
            }
        }, mContext.getString(R.string.chat_delete));
        addMoreItem(menuDlg, bean, 5);
        menuDlg.show();

    }


    private String getDialogItemText() {
        String text;
        if (getVoicePref() == SPEAKER) {   //扬声器模式
            text = getString(R.string.dialog_item_headset);
        } else {                           //听筒模式
            text = getString(R.string.dialog_item_speaker);
        }
        return text;
    }


    /**
     * 写播放模式 SharedPreferences
     */
    private void writePrefTo(Boolean mode) {
        SharedPreferences voiceSettings = mContext
            .getSharedPreferences(VOICE_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = voiceSettings.edit();

        editor.putBoolean("VOICE_PLAY_MODE", mode);

        editor.commit();
    }


    private void stopOthersPlay(NoticesBean bean) {
        if (bean.getId().equals(curPlayingAuMsgId)) {
            // 当前音频正在播放，则停止播放
            stopCurAuPlaying();
        } else {
            if (!TextUtils.isEmpty(curPlayingAuMsgId)) {
                // 当前其他音频正在播放，先停止播放的音频
                stopCurAuPlaying();
            }
        }
    }


    private void loadHeadImage(NoticesBean bean, ViewHolder holder) {
        if (isSendNotice(bean.getSender())) {
            setMyselfImage(holder, bean);
        } else {
            loadUserHeadIcon(holder, bean);
        }
    }


    private void startAuPlaying(NoticesBean bean, View voiceView) {
        String localPath = "";
        try {
            JSONArray bodyArray = new JSONArray(bean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                localPath = bodyObj.optString("localUrl");
            }
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }

        boolean isSend = isSendNotice(bean.getSender());

        boolean exist = false;
        if (isSend) {
            // 因为音频没有转发功能，因此，发送的音频不存在下载的问题
            if (!isValidFilePath(localPath)) {
                // 发送的消息，若本地音频不存在，则提示
                showToast(R.string.toast_no_aud);
                return;
            } else {
                exist = true;
            }
        } else {
            // 接收的音频，若本地不存在，则重新下载
            if (TextUtils.isEmpty(localPath) || localPath.endsWith(".temp")) {
                // 还未开始下载或正在下载，开始下载
                showToast(R.string.toast_downloading_aud);
                fileTaskMgr.addDTTask(bean.getId(), null);
                return;
            } else {
                File audFile = new File(localPath);
                if (audFile.exists()) {
                    exist = true;
                } else {
                    // 语音文件不存在，重新开始下载
                    showToast(R.string.toast_downloading_aud);
                    fileTaskMgr.addDTTask(bean.getId(), null);
                    return;
                }
            }
        }

        // 保存数据
        curPlayingAuMsgId = bean.getId();
        voiceView.setTag(bean);

        if (exist) {
            // 音频文件已存在的场合，直接开始播放，不存在的场合，等待下载成功后开始播放
            playAudio(localPath, voiceView.getContext(), voiceView, isSend, curPlayingAuMsgId);
        }
    }


    private void playAudio(String audioPath, Context context, final View voiceView
        , final boolean isSender, final String playMsgId) {

        isPlayingAuMsg = true;
        LogUtil.d("audioPath:" + audioPath);
        try {

            playAudioByPref(audioPath);

            playerManager.play(audioPath, new PlayerManager.PlayCallback() {
                @Override
                public void onPrepared() {
                    curPlayingAuMsgId = playMsgId;
                    currentPlayVoiceView = new WeakReference<View>(voiceView);
                    int playRes = 0;
                    if (isSender) {
                        voiceView.setBackgroundResource(R.drawable.audio_right_playing);
                    } else {
                        voiceView.setBackgroundResource(R.drawable.audio_left_playing);
                    }
                    final AnimationDrawable drawable = (AnimationDrawable) voiceView
                        .getBackground();
                    voiceView.post(new Runnable() {
                        @Override
                        public void run() {
                            drawable.start();
                        }
                    });
                }


                @Override
                public void onComplete() {
                }


                @Override
                public void onStop() {
                    stopCurAuPlaying();
                }
            });

        } catch (Exception e) {
            CustomLog.e("音频播放异常", String.valueOf(e));
            CustomToast.show(mContext, R.string.toast_aud_damaged, 1);
            stopCurAuPlaying();
            return;
        }
    }


    /**
     * 根据 SharedPreferences 选择播放语音信息模式
     */
    private void playAudioByPref(String audioPath) {
        if (getVoicePref() == SPEAKER) {
            playerManager.changeToSpeaker();
        } else {
            playerManager.changeToReceiver();
            showPrompt();                       //当前是听筒播放时，每次都显示 Toast
        }

    }


    //将音频路径写入 SharedPreferences
    private void setAudioPath(String audioPath) {
        SharedPreferences voiceSettings = mContext
            .getSharedPreferences(VOICE_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = voiceSettings.edit();

        editor.putString("VOICE_PATH", audioPath);

        editor.commit();
    }


    //当前是听筒播放时，每次都显示 Toast
    private void showPrompt() {
        IMCommonUtil.makeModeChangeToast(mContext, getString(R.string.mode_headset),
            R.drawable.headset_on);
    }


    public boolean getVoicePref() {
        SharedPreferences voiceSettings = mContext.getSharedPreferences(VOICE_PREFS_NAME,
            MODE_PRIVATE);
        boolean playMode = voiceSettings.getBoolean("VOICE_PLAY_MODE"
            , SPEAKER);  //第二个参数是 SharedPreference 不存在时返回的默认值

        return playMode;
    }


    public void stopCurAuPlaying() {
        if (!isPlayingAuMsg) {
            return;
        }
        isPlayingAuMsg = false;

        // 停止播放
        //        if (mMediaPlayer != null) {
        //            mMediaPlayer.stop();
        //            mMediaPlayer.release();
        //            mMediaPlayer = null;
        //        }

        playerManager.stop();

        // 停止动画
        if (currentPlayVoiceView != null) {
            View playingView = currentPlayVoiceView.get();
            if (playingView != null) {
                // 停止动画
                NoticesBean msg = (NoticesBean) playingView.getTag();
                // 重置图标
                int iconRes = 0;
                if (isSendNotice(msg.getSender())) {
                    iconRes = R.drawable.audio_right_icon_3;
                } else {
                    iconRes = R.drawable.audio_left_icon_3;
                }
                playingView.setBackgroundResource(iconRes);
            }
        }

        // 初始化数据
        curPlayingAuMsgId = null;
        if (currentPlayVoiceView != null) {
            currentPlayVoiceView.clear();
            currentPlayVoiceView = null;
        }
    }


    /**
     * 解除绑定上传进度显示
     */
    private void detachUploadProgressView(TextView view, String id) {
        FileTaskProgressListener progressListener = null;
        Object tagObj = view.getTag();
        if (tagObj != null) {
            progressListener = (FileTaskProgressListener) tagObj;
            progressListener.setId(id);
        }
    }


    /**
     * 绑定上传进度显示，并开始任务
     */
    private void attachUploadProgressView(TextView view, String id) {
        CustomLog.i(TAG, "attachUploadProgressView()");

        FileTaskProgressListener progressListener = null;
        Object tagObj = view.getTag();
        if (tagObj != null) {
            progressListener = (FileTaskProgressListener) tagObj;
            progressListener.setId(id);
            progressListener.setAttachedView(view);
        } else {
            progressListener = new FileTaskProgressListener(view, id);
            view.setTag(progressListener);
        }
        fileTaskMgr.setChgUIInterface(id, progressListener);
    }


    public void setConversationType(int conversationType) {
        this.conversationType = conversationType;
    }


    public void onPause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }
    }


    /**
     * @ClassName: ChatListAdapter.java
     * @Description: 文件上传下载任务类
     * @author: gtzha
     * @date: 2014年11月18日
     */
    private class FileTaskProgressListener extends ChangeUIInterface {
        private WeakReference<TextView> viewReference;

        public String id;


        public void setId(String _id) {
            id = _id;
        }


        public void setAttachedView(TextView view) {
            viewReference = new WeakReference<TextView>(view);
        }


        public FileTaskProgressListener(TextView view, String _id) {
            viewReference = new WeakReference<TextView>(view);
            id = _id;
        }


        public void onStart(FileTaskBean bean) {
            // 开始文件任务
            LogUtil.d("开始文件任务 onStart：" + bean.getUuid());
        }


        public void onProcessing(FileTaskBean bean, long current, long total) {
            // 文件任务进度
            LogUtil.d("onProcessing:" + bean.getUuid() + ":" + current + "/"
                + total);
            if (current < 0 || total <= 0) {
                LogUtil.d("onProcessing:数据不合法，不做更新");
                return;
            }
            if (!changUIProgressTaskIds.contains(bean.getUuid())) {
                // 已不需要更新界面
                LogUtil.d("onProcessing:已不需要更新界面");
                return;
            }

            final float pro = current / (total * 1.0f);
            mTaskFileProgressMap.put(bean.getUuid(), pro);

            final TextView proView = viewReference.get();
            if (id.equals(bean.getUuid()) && proView != null) {
                proView.post(new Runnable() {
                    @Override
                    public void run() {
                        NumberFormat numFormat = NumberFormat
                            .getNumberInstance();
                        numFormat.setMaximumFractionDigits(2);
                        proView.setText(numFormat.format(pro * 100) + "%");
                    }
                });
            }
        }


        public void onSuccess(FileTaskBean bean, String result) {
            // 文件任务成功完成
            LogUtil.d("onSuccess:" + bean.getUuid() + ":" + result);
        }


        public void onFailure(FileTaskBean bean, Throwable error, String msg) {
            LogUtil.d("onFailure:" + bean.getUuid() + ":" + msg);
            if (MediaFile.isWebpImageFileType(bean.getSrcUrl())) {
                showToast(mContext.getString(R.string.not_support_send_webp_picture));
                LogUtil.d("path=" + bean.getSrcUrl());
            }
        }
    }


    /**
     * 解除绑定上传进度显示
     */
    private void detachFileUploadProgressView(ProgressBar view, String id) {
        FileTaskProgressListener progressListener = null;
        Object tagObj = view.getTag();
        if (tagObj != null) {
            progressListener = (FileTaskProgressListener) tagObj;
            progressListener.setId(id);
        }
    }


    /**
     * 绑定上传进度显示，并开始任务
     */
    private void attachFileUploadProgressView(ProgressBar view, String id) {
        FileProgressListener progressListener = null;
        Object tagObj = view.getTag();
        if (tagObj != null) {
            progressListener = (FileProgressListener) tagObj;
            progressListener.setId(id);
            progressListener.setAttachedView(view);
        } else {
            progressListener = new FileProgressListener(view, id);
            view.setTag(progressListener);
        }
        fileTaskMgr.setChgUIInterface(id, progressListener);
    }


    /**
     * @ClassName: ChatListAdapter.java
     * @Description: 文件上传下载任务类
     * @author: gtzha
     * @date: 2014年11月18日
     */
    private class FileProgressListener extends ChangeUIInterface {
        private WeakReference<ProgressBar> viewReference;

        public String id;


        public void setId(String _id) {
            id = _id;
        }


        public void setAttachedView(ProgressBar view) {
            viewReference = new WeakReference<ProgressBar>(view);
        }


        public FileProgressListener(ProgressBar view, String _id) {
            viewReference = new WeakReference<ProgressBar>(view);
            id = _id;
        }


        public void onStart(FileTaskBean bean) {
            // 开始文件任务
            LogUtil.d("开始文件任务 onStart：" + bean.getUuid());
        }


        public void onProcessing(FileTaskBean bean, long current, long total) {
            // 文件任务进度
            LogUtil.d("onProcessing:" + bean.getUuid() + ":" + current + "/"
                + total);
            if (current < 0 || total <= 0) {
                LogUtil.d("onProcessing:数据不合法，不做更新");
                return;
            }
            if (!changUIProgressTaskIds.contains(bean.getUuid())) {
                // 已不需要更新界面
                LogUtil.d("onProcessing:已不需要更新界面");
                return;
            }

            final float pro = current / (total * 1.0f);
            mTaskFileProgressMap.put(bean.getUuid(), pro);

            final ProgressBar proView = viewReference.get();
            if (id.equals(bean.getUuid()) && proView != null) {
                proView.post(new Runnable() {
                    @Override
                    public void run() {
                        int duration = (int) (pro * 100);
                        proView.setProgress(duration);
                    }
                });
            }
        }


        public void onSuccess(FileTaskBean bean, String result) {
            // 文件任务成功完成
            LogUtil.d("onSuccess:" + bean.getUuid() + ":" + result);
        }


        public void onFailure(FileTaskBean bean, Throwable error, String msg) {
            LogUtil.d("onFailure:" + bean.getUuid() + ":" + msg);
            if (MediaFile.isWebpImageFileType(bean.getSrcUrl())) {
                showToast(getString(R.string.not_support_send_webp_picture));
                LogUtil.d("path=" + bean.getSrcUrl());
            }
        }
    }


    /**
     * 是否是发送的消息
     */
    public boolean isSendNotice(String noticeSender) {
        return selfNubeNumber.equals(noticeSender);
    }


    public static class ViewHolder {
        // 时间
        TextView timeTv;
        // 状态（正在发送，重发按钮）
        ImageButton retryBtn;
        ProgressBar runningPb;

        // 文字
        EmojiconTextView noticeTxt;
        // TextView noticeTxt;
        TextView noticeAddTxt;
        // 显示链接相关的view
        LinearLayout layout;
        View txtView;
        TextView txt_msg;
        View linkView;

        // 图片，视频
        FrameLayout imgLayout;
        ImageView imgIv;
        LinearLayout progressLine;
        TextView progressTxt;
        ImageView imgFrameIv;
        ImageView imgIvMask;
        TextView durationTxt;

        // 名片
        RelativeLayout vcardBg;
        ImageView vcardHeadIv;
        TextView vcardNameTxt;
        TextView vcardDetail;
        TextView vcardNubeTxt;
        // 接收端跳转到联系人页面避免再次查询
        String nubeNumber = "";
        String phoneNum = "";
        String nickName = "";
        String headUrl = "";
        String sex = "";

        // 音频
        RelativeLayout audioBg;
        ImageView audioIcon;
        TextView audioDuration;
        ImageView readStatus;

        SharePressableImageView contactIcon;
        TextView contactName;
        //文件
        ProgressBar mProgressBar;
        //多选的checkbox
        RelativeLayout checkLayout;
        CheckBox checkbox;

        //聊天记录
        RelativeLayout chatrecordbg;
        TextView chatRecordTitleTxt;
        TextView chatRecordDetail;

        //文章
        RelativeLayout articleBg;
        TextView articleTitle;
        ImageView articleImage;
        TextView articleDetail;

    }


    /**
     * 时间显示样式
     */
    private String getDispTimestamp(long dbTime) {

        String dateStr = DateUtil.formatMs2String(dbTime,
            DateUtil.FORMAT_YYYY_MM_DD_HH_MM);

        CustomLog.d(ACTIVITY_FLAG, "IM 消息时间戳：" + dateStr);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dbTime);

        Calendar nowCal = Calendar.getInstance();

        if (cal.get(Calendar.YEAR) < nowCal.get(Calendar.YEAR)) {
            // 跨年了，显示全部（2014-11-15 14:11）
            return dateStr;
        } else {
            int dayInterval = DateUtil.realDateIntervalDay(cal.getTime(),
                nowCal.getTime());
            if (dayInterval == 0) {
                // 当天（14:11）
                return dateStr.substring(11, 16);
            } else if (dayInterval == 1) {
                return getString(R.string.date_yesterday) + dateStr.substring(11, 16);
            } else {
                // 显示月份（11-15 14:11）
                return dateStr.substring(5, 16);
            }
        }
    }


    private static final int CURSOR_COL_TYPE_STRING = 1;
    private static final int CURSOR_COL_TYPE_INT = 2;
    private static final int CURSOR_COL_TYPE_LONG = 3;


    private Object getCursorDataByCol(Cursor cursor, String column, int type) {
        CustomLog.i(TAG, "getCursorDataByCol()");

        if (cursor != null && !cursor.isClosed()) {
            try {
                switch (type) {
                    case CURSOR_COL_TYPE_STRING:
                        return cursor.getString(cursor
                            .getColumnIndexOrThrow(column));
                    case CURSOR_COL_TYPE_INT:
                        return cursor.getInt(cursor.getColumnIndexOrThrow(column));
                    case CURSOR_COL_TYPE_LONG:
                        return cursor.getLong(cursor.getColumnIndexOrThrow(column));
                }
                cursor.close();
            } catch (Exception e) {
                LogUtil.e("getCursorDataByCol Exception", e);
            }
        }
        return 0;
    }


    /**
     * 判断时间间隔是否足够近（5分钟之内）
     */
    private boolean isCloseEnough(long dateFrom, long dateTo) {
        Calendar fromCal = Calendar.getInstance();
        fromCal.setTimeInMillis(dateFrom);
        fromCal.set(Calendar.SECOND, 0);
        fromCal.set(Calendar.MILLISECOND, 0);

        Calendar toCal = Calendar.getInstance();
        toCal.setTimeInMillis(dateTo);
        toCal.set(Calendar.SECOND, 0);
        toCal.set(Calendar.MILLISECOND, 0);

        fromCal.add(Calendar.MINUTE, 5);

        if (fromCal.compareTo(toCal) >= 0) {
            // 5分钟之内
            return true;
        }
        return false;
    }


    /**
     * 显示图片大小计算
     */
    private int[] caculateImgSize(int picWidth, int picHeight) {
        int targetWidth = picDefaultSize;
        int targetHeight = picDefaultSize;
        if (picWidth > 0 && picHeight > 0) {
            //竖屏
            if (picHeight >= picWidth) {
                // 竖图，先将高度限定到最大高度
                targetHeight = picMaxHeightSize;

                // 等比例计算宽度
                targetWidth = picWidth * targetHeight / picHeight;

                // 限定到最大最小宽度之间
                if (targetWidth > picMaxWidthSize) {
                    targetWidth = picMaxWidthSize;
                } else if (targetWidth < picMinWidthSize) {
                    targetWidth = picMinWidthSize;
                }
            } else {
                // 横图，先将宽度限定到最大宽度
                targetWidth = picMaxWidthSize;

                // 等比例计算高度
                targetHeight = picHeight * targetWidth / picWidth;

                // 限定到最大最小高度之间
                if (targetHeight > picMaxHeightSize) {
                    targetHeight = picMaxHeightSize;
                } else if (targetHeight < picMinHeightSize) {
                    targetHeight = picMinHeightSize;
                }
            }
        }

        return new int[] { targetWidth, targetHeight };
    }


    /**
     * 显示图片，发送的消息，优先显示本地图片，其他都显示缩略图
     */
    private void showImage(boolean isSend, boolean isVideo,
                           String localPath, String thumbUrl, String remoteUrl,
                           ImageView imageView, int width, int height) {

        //        imageView.setImageBitmap(null);
        //        imageView.setBackgroundColor(Color.TRANSPARENT);
        if (isVideo) {
            if (isValidFilePath(localPath)) {
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                Glide.with(mContext).load(localPath)
                    .placeholder(R.drawable.chat_empty_img)
                    .error(R.drawable.chat_empty_img).centerCrop()
                    .crossFade().into(imageView);
            } else if (!TextUtils.isEmpty(thumbUrl)) {
                if (width == 0 || height == 0) {
                    Glide.with(mContext).load(thumbUrl)
                        .placeholder(R.drawable.chat_empty_img)
                        .error(R.drawable.chat_empty_img).centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .crossFade().into(imageView);
                } else {
                    loadIntoUseFitWidth(mContext, thumbUrl, R.drawable.chat_empty_img, imageView);
                }
            } else {
                if (width == 0 || height == 0) {
                    Glide.with(mContext).load(remoteUrl)
                        .placeholder(R.drawable.chat_empty_img)
                        .error(R.drawable.chat_empty_img).centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .crossFade().into(imageView);
                } else {
                    loadIntoUseFitWidth(mContext, remoteUrl, R.drawable.chat_empty_img, imageView);
                }
            }
        } else {
            if (isValidFilePath(localPath)) {
                loadIntoUseFitWidth(mContext, localPath, R.drawable.chat_empty_img, imageView);
            } else if (!TextUtils.isEmpty(thumbUrl)) {
                loadIntoUseFitWidth(mContext, thumbUrl, R.drawable.chat_empty_img, imageView);
            } else {
                loadIntoUseFitWidth(mContext, remoteUrl, R.drawable.chat_empty_img, imageView);
            }
        }
    }


    private void loadIntoUseFitWidth(Context context, final String imageUrl, int errorImageId, final ImageView imageView) {
        Glide.with(context)
            .load(imageUrl)
            .placeholder(errorImageId)
            .dontAnimate()
            .error(errorImageId)
            .into(imageView);
    }


    /**
     * 显示时长
     */
    private String getDispDuration(int duration) {
        if (duration == 0) {
            return "";
        }

        int minute = duration / 60;
        int second = duration % 60;

        String result = "";
        if (minute < 10) {
            result += "0";
        }
        result += minute + ":";
        if (second < 10) {
            result += "0";
        }
        result += second;

        return result;
    }


    public void onStop() {
        // 停止播放音频
        stopCurAuPlaying();
    }


    /**
     * 停止正在播放的音频但不停止动画
     */
    public void stopPreviousAudio() {

        // 停止播放
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        // 初始化数据
        curPlayingAuMsgId = null;
        if (currentPlayVoiceView != null) {
            currentPlayVoiceView.clear();
            currentPlayVoiceView = null;
        }
    }


    public interface CallbackInterface {
        void onMsgDelete(String uuid, long receivedTime, int dataCnt);

        void onMsgForward(String uuid, String sender, int msgType,
                          int msgStatus, String localPath);

        void onMsgForward(String uuid, String sender, int msgType,
                          int msgStatus, String localPath, String txt, String vcardName, String vcardNumber, String creator, String meetingRoomId, String meetingTopic, String date, String hms);

        void onSetSelectMemeber(String name, String nube);

        void onMoreClick(String uuid, int msgType, int msgStatus, boolean checked);

        void reBookMeeting();

        void reCreatMeeting();

        void addNewFriend();
    }


    private boolean isValidFilePath(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        if (file.length() == 0) {
            file.delete();
            return false;
        }
        if (filePath.endsWith(".temp")) {
            return false;
        }
        return true;
    }


    // 加载自己头像
    private void setMyselfImage(final ViewHolder holder, final NoticesBean bean) {
        LogUtil.d("setMyselfImage: noticeType" + noticeType + "  targetnumber:"
            + targetNumber + "  Body:" + bean.getBody());
        if (!TextUtils.isEmpty(headUrl)) {
            Glide.with(mContext).load(headUrl)
                .placeholder(IMCommonUtil.getHeadIdBySex(bean.getSex()))
                .error(IMCommonUtil.getHeadIdBySex(bean.getSex()))
                .centerCrop().diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade().into(holder.contactIcon.shareImageview);
        } else {
            holder.contactIcon.shareImageview
                .setImageResource(userDefaultHeadUrl);
        }
    }


    // 加载聊天成员头像，群聊情况下加载名称
    //TODO kevin add 群头像目前数据库操作存在问题，使用会诊室内头像和名称
    private void loadUserHeadIcon(final ViewHolder holder,
                                  final NoticesBean bean) {
        LogUtil.d("loadUserHeadIcon: noticeType" + noticeType
            + "  targetnumber:" + targetNumber + "  Body:" + bean.getBody());
        if (noticeType == ChatActivity.VALUE_CONVERSATION_TYPE_MULTI
            || targetNumber.length() > 12) {
            NameElement element = ShowNameUtil.getNameElement(
                bean.getMemberName(), bean.getmNickName(),
                bean.getmPhone(), bean.getSender());
            final String MName = tmp_target_docName;//ShowNameUtil.getShowName(element); kevin TODO
            holder.contactName.setText(MName);
            holder.contactName.setVisibility(View.VISIBLE);
            holder.contactIcon.pressableTextview
                .setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        callbackIf.onSetSelectMemeber(MName,
                            bean.getSender());
                        return true;
                    }
                });
        } else {
            holder.contactName.setVisibility(GONE);
        }

        if (bean.getSender().equals(butelPubNubeNum)) {
            // 官方头像
            holder.contactIcon.shareImageview
                .setImageResource(R.drawable.system_icon);
        } else if (bean.getSender().equals(SettingData.getInstance().adminNubeNum)) {
            String tmpHeadUrl = getAdminHeadUrl();
            if (TextUtils.isEmpty(tmpHeadUrl)) {
                holder.contactIcon.shareImageview
                    .setImageResource(R.drawable.system_icon);
            } else {
                Glide.with(mContext).load(tmpHeadUrl)
                    .placeholder(IMCommonUtil.getHeadIdBySex(bean.getSex()))
                    .error(IMCommonUtil.getHeadIdBySex(bean.getSex()))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .crossFade().into(holder.contactIcon.shareImageview);
            }
        } else {
            String userUrl = tmp_target_docHeadUrl;//bean.getHeadUrl(); kevin TODO
            if (TextUtils.isEmpty(userUrl)) {
                CustomLog.d(ACTIVITY_FLAG, bean.getSender() + "headurl is empty");
                holder.contactIcon.shareImageview.setImageResource(R.drawable.head_default);
            } else {
                Glide.with(mContext).load(userUrl)
                    .placeholder(R.drawable.head_default)
                    .error(R.drawable.head_default)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .crossFade().into(holder.contactIcon.shareImageview);
            }
        }

        final String nubNumber = bean.getSender();
        holder.contactIcon.pressableTextview.setVisibility(View.VISIBLE);
        holder.contactIcon.pressableTextview
            .setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (CommonUtil.isFastDoubleClick()
                        || bean.getSender().equals(butelPubNubeNum)
                        || bean.getSender().equals(SettingData.getInstance().adminNubeNum)) {
                        return;
                    }
                    Intent intent = new Intent(v.getContext(), ContactCardActivity.class);

                    intent.putExtra("nubeNumber", nubNumber);
                    intent.putExtra("searchType", "5"); // 5:群内添加
                    //                    intent.putExtra(ContactCardActivity.KEY_FROM_IM, true);
                    if (groupId.length() > 0) {
                        intent.putExtra("groupName", groupDao.getGroupNameByGid(groupId));
                    }
                    mContext.startActivity(intent);
                }
            });
    }


    /**
     * 发送文字/
     */
    private void showStatus(NoticesBean bean, ViewHolder holder) {
        if (isSendNotice(bean.getSender())) {
            // 发送的消息才有状态
            switch (bean.getStatus()) {
                case FileTaskManager.TASK_STATUS_SUCCESS:
                    holder.retryBtn.setVisibility(GONE);
                    holder.runningPb.setVisibility(GONE);
                    break;
                case FileTaskManager.TASK_STATUS_READY:
                case FileTaskManager.TASK_STATUS_RUNNING:
                case FileTaskManager.TASK_STATUS_COMPRESSING:
                    holder.retryBtn.setVisibility(GONE);
                    holder.runningPb.setVisibility(GONE);
                    break;
                case FileTaskManager.TASK_STATUS_FAIL:
                    // 重发按钮
                    holder.retryBtn.setVisibility(View.VISIBLE);
                    holder.runningPb.setVisibility(GONE);
                    break;
            }
        } else {
            holder.retryBtn.setVisibility(GONE);
            holder.runningPb.setVisibility(GONE);
        }
    }


    private boolean preJudgment() {
        // if (isFriend()&& !butelPubNubeNum.equals(targetNumber)) {
        // showToast(targetShortName + "还不是您的好友，快去添加好友吧");
        // return true;
        // }
        return false;
    }


    private void showToast(int toastId) {
        showToast(mContext.getString(toastId));
    }


    private void showToast(String toast) {
        CustomToast.show(mContext, toast, Toast.LENGTH_SHORT);
    }


    private void onTextLongClick(final View v, final NoticesBean bean,
                                 final boolean hasUrl, final ViewHolder holder) {

    }


    private HashSet<String> uuidList = new HashSet<String>();
    private LinkedHashMap<String, NoticesBean> uuidMap = new LinkedHashMap<String, NoticesBean>();


    private void updateCheckedData(String uuid, boolean checked, NoticesBean bean) {
        if (uuidList != null) {
            if (checked) {
                if (uuidList.size() > 30) {
                    showAlertDlg();
                    return;
                }
                uuidList.add(uuid);
                uuidMap.put(uuid, bean);
            } else {
                uuidList.remove(uuid);
                uuidMap.remove(uuid);
            }
        }
    }


    private void showAlertDlg() {
        final CustomDialog tipDlg = new CustomDialog(mContext);
        String tip = mContext.getString(R.string.sorry_most_forward_30);
        tipDlg.setTip(tip);

        tipDlg.removeCancelBtn();
        tipDlg.setOkBtnText(getString(R.string.iknow));
        tipDlg.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                tipDlg.dismiss();
            }
        });
        tipDlg.show();
    }


    private boolean hasChecked(String uuid) {
        if (!TextUtils.isEmpty(uuid)) {
            if (uuidList != null && uuidList.contains(uuid)) {
                return true;
            }
        }
        return false;
    }


    public HashSet<String> getCheckedData() {
        return uuidList;
    }


    public LinkedHashMap<String, NoticesBean> getCheckedDataMap() {
        return uuidMap;
    }


    public boolean hasCheckedData() {
        if (uuidList != null && uuidList.size() > 0) {
            return true;
        }
        return false;
    }


    public void cleanCheckedData() {
        if (uuidList != null) {
            uuidList.clear();
        }
        if (uuidMap != null) {
            uuidMap.clear();
        }
    }


    private boolean onLongClickedifMultiSeclect() {
        //隐藏软键盘
        CommonUtil.hideSoftInputFromWindow((Activity) mContext);
        if (bMultiCheckMode) {
            return true;
        }
        return false;
    }


    private boolean onItemClickedifMultiSeclect(NoticesBean bean, ViewHolder holder) {
        if (bMultiCheckMode) {
            if (bean != null && holder != null && holder.checkbox != null) {
                boolean status = holder.checkbox.isChecked();
                holder.checkbox.setChecked(!status);
                return true;
            }
        }
        return false;
    }


    private void onCheckBoxChecked(NoticesBean bean, boolean checked) {
        if (bean != null) {
            if (callbackIf != null) {
                updateCheckedData(bean.getId(), checked, bean);
                callbackIf.onMoreClick(bean.getId(), bean.getType(), bean.getStatus(), checked);
            }
        }
    }


    private void addMoreItem(MedicalAlertDialog menuDlg, final NoticesBean bean, int index) {
        if (menuDlg != null && bean != null) {
            MenuClickedListener listener = new MenuClickedListener() {
                @Override
                public void onMenuClicked() {

                    if (callbackIf != null) {
                        updateCheckedData(bean.getId(), true, bean);
                        callbackIf.onMoreClick(bean.getId(), bean.getType(), bean.getStatus(),
                            true);

                    }
                }
            };

            switch (index) {
                case 1:
                    menuDlg.addButtonFirst(listener, mContext.getString(R.string.more));
                    break;
                case 2:
                    menuDlg.addButtonSecond(listener, mContext.getString(R.string.more));
                    break;
                case 3:
                    menuDlg.addButtonThird(listener, mContext.getString(R.string.more));
                    break;
                case 4:
                    menuDlg.addButtonForth(listener, mContext.getString(R.string.more));
                    break;
                case 5:
                    menuDlg.addButtonFive(listener, mContext.getString(R.string.more));
                    break;
                case 6:
                    menuDlg.addButtonSix(listener, mContext.getString(R.string.more));
                    break;
                default:

            }
        }
    }


    public void setMeetingLinkClickListener(MeetingLinkClickListener listener) {
        this.meetingLinkClickListener = listener;
    }


    public interface MeetingLinkClickListener {
        void meetingLinkClick(String meetingId);
    }


    private String getNewTextMsg(String text) {
        CustomLog.i(TAG, "getNewTextMsg()");

        if (noticeType == ChatActivity.VALUE_CONVERSATION_TYPE_MULTI) {
            ArrayList<String> dispNubeList = new ArrayList<String>();
            dispNubeList = CommonUtil.getDispList(text);
            for (int i = 0; i < dispNubeList.size(); i++) {
                GroupMemberBean gbean = groupDao.queryGroupMember(
                    groupId, dispNubeList.get(i));
                if (gbean != null) {
                    ShowNameUtil.NameElement element = ShowNameUtil.getNameElement(
                        gbean.getName(), gbean.getNickName(),
                        gbean.getPhoneNum(), gbean.getNubeNum());
                    String MName = ShowNameUtil.getShowName(element);
                    text = text.replace("@" + dispNubeList.get(i)
                        + IMConstant.SPECIAL_CHAR, "@" + MName
                        + IMConstant.SPECIAL_CHAR);
                }
            }
        }
        return text;
    }


    private String getAdminHeadUrl() {
        SharedPreferences preferences = mContext.
            getSharedPreferences(ChatActivity.KEY_SERVICE_NUBE_INFO, MODE_PRIVATE);
        return preferences.getString("HEAD_URL", "");
    }
}
