package cn.redcdn.hvs.im.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.adapter.NoticeGridViewAdapter;
import cn.redcdn.hvs.im.adapter.NoticeGridViewAdapter.ComentNoticeListener1;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.util.SoundMeter;
import cn.redcdn.hvs.im.util.permissions.AudioPermissionCheckUtils;
import cn.redcdn.hvs.im.util.smileUtil.DefaultEmojicons;
import cn.redcdn.hvs.im.util.smileUtil.Emojicon;
import cn.redcdn.hvs.im.util.smileUtil.EmojiconEditText;
import cn.redcdn.hvs.im.util.smileUtil.SmileLayout;
import cn.redcdn.hvs.im.util.smileUtil.SmileLayout.OnEmojiconBackspaceClickedListener;
import cn.redcdn.hvs.im.util.smileUtil.SmileLayout.OnEmojiconClickedListener;
import cn.redcdn.hvs.im.view.VoiceTipView;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;
import java.io.File;

import static cn.redcdn.hvs.R.drawable.record_btn_playing;
import static cn.redcdn.hvs.im.activity.ChatActivity.PERMISSIONS_REQUEST_AUDIO_RECORD_CODE;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class ChatInputFragment extends BaseFragment {

    private final String TAG = "ChatInputFragment";

    public static final int MSG_TYPE_PIC = 1;//照片
    public static final int MSG_TYPE_VIDEO = 2;//拍视频
    public static final int MSG_TYPE_MEETING = 3;//视频会议
    public static final int MSG_TYPE_VCARD = 4;//名片
    public static final int MSG_TYPE_AUDIO_CALL = 5;//语音电话
    public static final int MSG_TYPE_VEDIO_CALL = 6;//视频电话
    public static final int MSG_TYPE_COLLECTION = 7;//收藏
    public static final int MSG_TYPE_CAMERA = 8;//拍照
    private CustomDialog dialog;

    // 发送按钮
    private Button commentSendBtn;
    // 复选框按钮
    private ImageButton moreSelectBtn;
    // 录音按钮
    private ImageButton noticeRecordLeftBtn;
    // 录音按钮中间
    private TextView noticeRecordCenter;
    // 焦点控制
    private TextView focusTv;
    // 评论编辑框
    private LinearLayout editLL;
    private EmojiconEditText commentEditText;

    // 素材面板区
    private LinearLayout moreSelctLayout;

    // 表情面板区
    private SmileLayout smileLayout;
    private CheckBox emojiSelect;

    // 附件gridview
    private GridView noticeGridView = null;
    // 录音是否发送标志
    private boolean isRecordSend = false;
    // 录音帮助类
    private SoundMeter recorder = null;
    // 录音秒数
    private int rcdSeconds = 0;
    private int rcdIndex = 0;
    // 记录编辑栏里面变化前长度
    private int beforeLength = 0;
    // 记录编辑栏里面变化前的定位位置
    private int beforePos = 0;
    private boolean selectGroupflag = true;
    private String saveString = "";
    private boolean deleFlag = false;
    private View menuView;
    private PopupWindow popupWindow = null;
    private ImageView microPhoenImageView = null;
    private TextView recordTxtTip = null;
    private TextView countDownTxt;
    private NoticeGridViewAdapter noticeGridViewAdapter = null;
    private String nube = "";
    private ImageView voice;
    public Activity mContext;
    public SendCallbackInterface callback = null;
    public boolean isShowing = false;

    private View mRootView;
    private ListView noticeListview;
    private Cursor chatDataCursor;
    private VoiceTipView voiceTipView;


    public void setNubeNum(String nubeNum) {
        CustomLog.d(TAG, "setNubeNum:" + nubeNum);
        this.nube = nubeNum;
    }


    public void setListview(ListView listview) {
        this.noticeListview = listview;
    }


    public void setVoiceInfo(VoiceTipView view, Cursor cursor) {
        this.voiceTipView = view;
        this.chatDataCursor = cursor;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        CustomLog.d(TAG, "onCreateView");
        mRootView = (LinearLayout) inflater.inflate(R.layout.chat_input_layout,
            container, false);
        initWidget();
        return mRootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CustomLog.d(TAG, "onActivityCreated");

        initData();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CustomLog.d(TAG, "onSaveInstanceState");
        outState.putString("nube", nube);
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        CustomLog.d(TAG, "onViewStateRestored");
        if (savedInstanceState != null) {
            nube = savedInstanceState.getString("nube");
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CustomLog.d(TAG, "onDestroyView");
    }


    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }


    @Override protected void setListener() {

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        CustomLog.d(TAG, "onDetach");
    }


    @Override
    public void onDetach() {
        super.onDetach();
        CustomLog.d(TAG, "onDetach");
    }


    protected void initData() {
        CustomLog.d(TAG, "initData");
        // 初始化，这句话一定要加
        smileLayout.init(DefaultEmojicons.DATA);

        noticeGridViewAdapter = new NoticeGridViewAdapter(
            this.nube);

        noticeGridView.setAdapter(noticeGridViewAdapter);
        noticeGridViewAdapter
            .setComentNoticeListener1(new ComentNoticeListener1() {
                @Override
                public void sendType(int type) {
                    switch (type) {
                        case MSG_TYPE_PIC:
                            sharePhoto();
                            break;
                        case MSG_TYPE_VIDEO:
                            shareVideo();
                            break;
                        case MSG_TYPE_MEETING:
                            makeMeetingRoom();
                            break;
                        case MSG_TYPE_VCARD:
                            shareVcard();
                            break;
                        case MSG_TYPE_AUDIO_CALL:
                            makeAudioCall();
                            break;
                        case MSG_TYPE_VEDIO_CALL:
                            makeVedioCall();
                            break;
                        case MSG_TYPE_COLLECTION:
                            toShareCollection();
                            break;
                        case MSG_TYPE_CAMERA:
                            ShareCamera();
                            break;
                    }
                }
            });
    }


    private void initWidget() {
        CustomLog.d(TAG, "initWidget");
        moreSelctLayout = (LinearLayout) mRootView
            .findViewById(R.id.ll_attachment_panel);
        // 表情面板
        smileLayout = (SmileLayout) mRootView.findViewById(R.id.write_smile_panel);
        emojiSelect = (CheckBox) mRootView.findViewById(R.id.emoji_cb);

        commentSendBtn = (Button) mRootView
            .findViewById(R.id.notice_send_btn);
        moreSelectBtn = (ImageButton) mRootView
            .findViewById(R.id.notice_more_select_btn);
        focusTv = (TextView) mRootView.findViewById(R.id.focus_tv);
        focusTv.requestFocus();

        editLL = (LinearLayout) mRootView
            .findViewById(R.id.edit_ll);
        commentEditText = (EmojiconEditText) mRootView
            .findViewById(R.id.notice_comment_text);

        noticeRecordLeftBtn = (ImageButton) mRootView
            .findViewById(R.id.notice_record_leftbtn);

        noticeRecordCenter = (TextView) mRootView
            .findViewById(R.id.notice_record_btncenter);

        noticeGridView = (GridView) mRootView
            .findViewById(R.id.attachment_gridview);

        initOnclik();

        if (!TextUtils.isEmpty(draftTxt)) {
            setDraftTxt(draftTxt);
        }

        recorder = new SoundMeter();
    }


    public void changedata(String nubeN) {
        nube = nubeN;
        if (!isShowing) {
            return;
        }
        if (noticeGridViewAdapter != null) {
            noticeGridViewAdapter.setData(nubeN);
        }
    }


    private String draftTxt;


    public void setDraftTxt(String draftTxt) {
        CustomLog.d(TAG, "draft:" + draftTxt);
        if (!isShowing) {
            return;
        }
        if (commentEditText == null) {
            this.draftTxt = draftTxt;
            return;
        }
        if (!TextUtils.isEmpty(draftTxt)) {
            selectGroupflag = false;
            commentEditText.setText(draftTxt);
            commentEditText.setSelection(draftTxt.length());
            commentEditText.requestFocus();
        } else {
            commentEditText.setText("");
            if (!focusTv.hasFocus()) {
                // 当前焦点不在focusTv的场合，设置焦点到focusTv
                focusTv.requestFocus();
                selectGroupflag = true;
            }
        }
    }


    public void setSpecialtxt(String name) {
        if (!isShowing) {
            return;
        }
        CustomLog.d(TAG, "编辑栏有@某人：" + name);
        int pos = commentEditText.getSelectionStart();
        String textStr = commentEditText.getEditableText().toString();
        StringBuffer sb = new StringBuffer();
        sb.append(textStr);
        sb.insert(pos, name + IMConstant.SPECIAL_CHAR);
        String setText = sb.toString();
        commentEditText.setText(setText);
        if (!commentEditText.hasFocus()) {
            commentEditText.requestFocus();
        } else {
            // 显示软键盘
            CommonUtil.showSoftInputFromWindow(mContext);
        }
        commentEditText.setSelection(pos + name.length() + 1);
    }


    public String obtainInputTxt() {
        if (commentEditText == null) {
            return "";
        }
        return commentEditText.getText().toString().trim();
    }


    // 进行控件的隐藏
    public void setHide() {
        if (!isShowing) {
            return;
        }
        if (moreSelctLayout.getVisibility() == View.VISIBLE) {
            moreSelctLayout.setVisibility(View.GONE);
        }
        if (emojiSelect.isChecked()) {
            emojiSelect.setChecked(false);
        }
    }


    public void setEditHide() {
        if (editLL.getVisibility() == View.VISIBLE) {
            editLL.setVisibility(View.GONE);
        }
    }


    //对控件进行显示
    public void setEditShow() {
        if (editLL.getVisibility() == View.GONE) {
            editLL.setVisibility(View.VISIBLE);
        }
    }


    // 相关控件点击事件的处理操作
    private void initOnclik() {
        smileLayout.setOnItemClickedListener(new OnEmojiconClickedListener() {
            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                SmileLayout.input(commentEditText, emojicon);
            }
        });
        smileLayout.setOnBackspaceClickedListener(new OnEmojiconBackspaceClickedListener() {
            @Override
            public void onEmojiconBackspaceClicked(View v) {
                SmileLayout.backspace(commentEditText);
            }
        });

        // 表情选择
        emojiSelect.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (moreSelctLayout.getVisibility() == View.VISIBLE
                        && smileLayout.getVisibility() == View.VISIBLE) {
                        return;
                    }

                    // 关闭软键盘
                    CommonUtil.hideSoftInputFromWindow(mContext);

                    noticeGridView.setVisibility(View.GONE);
                    smileLayout.setVisibility(View.VISIBLE);

                    // 面板延迟显示，防止软键盘关闭太慢（软键盘关闭后占用屏幕高度），直接就显示面板的话，会导致面板被顶上去再下来的效果
                    moreSelctLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 显示表情选择区域
                            moreSelctLayout.setVisibility(View.VISIBLE);
                        }
                    }, 100);
                }
            }
        });
        emojiSelect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // checkbox的onclick事件在onCheckedChanged事件之后执行到

                if (!emojiSelect.isChecked()) {
                    // 点击之后，状态变为 unchecked状态
                    // 隐藏素材选择面板
                    if (moreSelctLayout.getVisibility() == View.VISIBLE) {
                        moreSelctLayout.setVisibility(View.GONE);
                    }

                    // 显示软键盘
                    CommonUtil.showSoftInputFromWindow(mContext);
                }
            }
        });
        noticeRecordLeftBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 显示中间的录音按钮
                if (!TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())) {
                    CustomToast.show(getActivity(), getString(R.string.is_video_wait_try),
                        CustomToast.LENGTH_SHORT);
                    return;
                } else {
                    if (noticeRecordCenter.getVisibility() == View.GONE) {
                        setRecordMode();
                    } else {
                        setInputMode();
                    }
                }
            }
        });

        // 监听消息编辑框焦点事件
        commentEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                CustomLog.d(TAG, "输入框 onFocusChange:" + hasFocus);
                if (hasFocus) {
                    // 输入框获取焦点的场合，隐藏素材面板，并显示软键盘
                    moreSelctLayout.postDelayed(new Runnable() {
                        public void run() {
                            moreSelctLayout.setVisibility(View.GONE);
                        }
                    }, 10);
                    CommonUtil.showSoftInputFromWindow(mContext);
                }
            }
        });

        commentEditText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                if (!commentEditText.hasFocus()) {
                    // 当前焦点在输入框内的场合，touch输入框，输入框获取焦点
                    commentEditText.requestFocus();
                    commentEditText.requestFocusFromTouch();
                } else {
                    moreSelctLayout.setVisibility(View.GONE);
                    CommonUtil.showSoftInputFromWindow(mContext);
                }
                if (emojiSelect.isChecked()) {
                    emojiSelect.setChecked(false);
                }
            }
        });

        //        commentEditText.setOnTouchListener(new OnTouchListener() {
        //            @Override
        //            public boolean onTouch(View v, MotionEvent event) {
        //                if (CommonUtil.isFastDoubleClick()) {
        //                    return false;
        //                }
        //                if (!commentEditText.hasFocus()) {
        //                    // 当前焦点在输入框内的场合，touch输入框，输入框获取焦点
        //                    commentEditText.requestFocus();
        //                    commentEditText.requestFocusFromTouch();
        //                } else {
        //                    moreSelctLayout.setVisibility(View.GONE);
        //                    CommonUtil.showSoftInputFromWindow(mContext);
        //                }
        //                if (emojiSelect.isChecked()) {
        //                    emojiSelect.setChecked(false);
        //                }
        //                return true;
        //            }
        //        });

        // 动态监听编辑框文字个数情况
        commentEditText.addTextChangedListener(mTextWatcher);

        // 发送按钮事件
        commentSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }

                if (callback != null && !callback.doPreSendCheck()) {
                    return;
                }

                if (TextUtils.isEmpty(commentEditText.getEditableText()
                    .toString().trim())) {
                    commentEditText.setText("");
                } else {
                    sendMessage();
                }
            }
        });
        // 监听更多加号按钮事件
        moreSelectBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //                if (CommonUtil.isFastDoubleClick()) {
                //                    return;
                //                }

                if (callback != null && !callback.doPreSendCheck()) {
                    return;
                }

                if (noticeRecordCenter.getVisibility() == View.VISIBLE) {
                    noticeRecordCenter.setVisibility(View.GONE);
                    editLL.setVisibility(View.VISIBLE);
                    noticeRecordLeftBtn.setBackgroundResource(R.drawable.m_record_left_btn);
                }

                // 点击素材选择加号按钮
                if (moreSelctLayout.getVisibility() == View.VISIBLE
                    && noticeGridView.getVisibility() == View.VISIBLE) {
                    // 素材面板显示的场合，隐藏素材面板，显示软键盘
                    moreSelctLayout.setVisibility(View.GONE);
                    commentEditText.requestFocus();
                    CommonUtil.showSoftInputFromWindow(mContext);
                } else {

                    // 关闭软键盘
                    CommonUtil.hideSoftInputFromWindow(mContext);

                    //当素材面板显示时清除 EditText 焦点，目的是防止锁屏后系统拿到焦点弹出软键盘
                    commentEditText.clearFocus();

                    moreSelctLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (smileLayout.getVisibility() == View.VISIBLE) {
                                smileLayout.setVisibility(View.GONE);
                            }

                            moreSelctLayout.setVisibility(View.VISIBLE);
                            noticeGridView.setVisibility(View.VISIBLE);
                        }
                    }, 200);

                    emojiSelect.setChecked(false);
                }

                if (commentEditText.getText().toString().trim().length() > 0) {
                    // 已输入文字的场合，显示发送按钮
                    commentSendBtn.setVisibility(View.VISIBLE);
                    moreSelectBtn.setVisibility(View.GONE);
                }
            }
        });
    }


    /**
     * 设置为录音模式
     */
    private void setRecordMode() {

        // 隐藏输入框
        editLL.setVisibility(View.GONE);
        // 显示按住说话按钮
        noticeRecordCenter.setVisibility(View.VISIBLE);

        // 隐藏素材选择面板
        if (moreSelctLayout.getVisibility() == View.VISIBLE) {
            moreSelctLayout.setVisibility(View.GONE);
        }

        // 显示素材面板展开按钮
        moreSelectBtn.setVisibility(View.VISIBLE);

        // 发送按钮显示的场合，隐藏发送按钮
        if (commentSendBtn.getVisibility() == View.VISIBLE) {
            commentSendBtn.setVisibility(View.GONE);
        }

        // 左边按钮图标改为键盘图标
        noticeRecordLeftBtn
            .setBackgroundResource(R.drawable.m_left_keybord_btn);

        // 关闭软键盘
        CommonUtil.hideSoftInputFromWindow(mContext);

        emojiSelect.setChecked(false);
    }


    /**
     * 设置为输入模式
     */
    private void setInputMode() {

        // 隐藏按住说话按钮
        noticeRecordCenter.setVisibility(View.GONE);
        // 显示输入框
        editLL.setVisibility(View.VISIBLE);

        // 隐藏素材选择面板
        moreSelctLayout.setVisibility(View.GONE);

        if (commentEditText.getText().toString().trim().length() > 0) {
            // 已输入文字的场合，显示发送按钮
            commentSendBtn.setVisibility(View.VISIBLE);
            moreSelectBtn.setVisibility(View.GONE);
        } else {
            // 显示素材面板展开按钮
            commentSendBtn.setVisibility(View.GONE);
            moreSelectBtn.setVisibility(View.VISIBLE);
        }

        if (!commentEditText.hasFocus()) {
            commentEditText.requestFocus();
        } else {
            // 弹出软键盘
            CommonUtil.showSoftInputFromWindow(mContext);
        }
        commentEditText.setSelection(commentEditText.getText().toString().length());

        // 左边按钮图标改为话筒图标
        noticeRecordLeftBtn.setBackgroundResource(R.drawable.m_record_left_btn);
    }


    public boolean isPanelVisible() {
        if (isShowing) {
            return moreSelctLayout.getVisibility() == View.VISIBLE;
        } else {
            return false;
        }
    }


    // 发送文字消息
    public void sendMessage() {
        if (!isShowing) {
            return;
        }
        //      if (isFriend()) {
        //          Toast.makeText(mContext, nube + "还不是您的好友，快去添加好友吧",
        //                  Toast.LENGTH_SHORT).show();
        //          return;
        //      }
        // 发送文字消息
        if (callback != null) {

            if (callback.onSendTxtMsg(commentEditText.getEditableText()
                .toString())) {
                commentEditText.setText("");
            }

        }
    }


    private void shareVideo() {
        //      if (isFriend()) {
        //          Toast.makeText(mContext, nube + "还不是您的好友，快去添加好友吧",
        //                  Toast.LENGTH_SHORT).show();
        //          return;
        //      }

        if (callback != null) {
            callback.onSendVideo();
        } else {
            CustomLog.d(TAG, "callback==null");
        }
    }


    private void sharePhoto() {

        if (callback != null) {
            callback.onSendPic();
        } else {
            CustomLog.d(TAG, "callback==null");
        }
    }


    private void ShareCamera() {

        if (callback != null) {
            callback.onSendPicFromCamera();
        } else {
            CustomLog.d(TAG, "callback==null");
        }
    }


    public void shareVcard() {
        if (!isShowing) {
            return;
        }
        //      if (isFriend()) {
        //          Toast.makeText(mContext, nube + "还不是您的好友，快去添加好友吧",
        //                  Toast.LENGTH_SHORT).show();
        //          return;
        //      }
        // 分享名片
        if (callback != null) {
            callback.onSendVcard();
        } else {
            CustomLog.d(TAG, "callback==null");
        }
    }


    public void makeAudioCall() {
        if (!isShowing) {
            return;
        }
        //      if (isFriend()) {
        //          Toast.makeText(mContext, nube + "还不是您的好友，快去添加好友吧",
        //                  Toast.LENGTH_SHORT).show();
        //          return;
        //      }
        if (callback != null) {
            callback.onAudioCall();
        } else {
            CustomLog.d(TAG, "callback==null");
        }
    }


    public void makeVedioCall() {
        if (!isShowing) {
            return;
        }
        //      if (isFriend()) {
        //          Toast.makeText(mContext, nube + "还不是您的好友，快去添加好友吧",
        //                  Toast.LENGTH_SHORT).show();
        //          return;
        //      }
        if (callback != null) {
            callback.onVedioCall();
        } else {
            CustomLog.d(TAG, "callback==null");
        }
    }


    public void toShareCollection() {
        if (!isShowing) {
            return;
        }
        if (callback != null) {
            callback.onShareCollection();
        }
    }


    public void makeMeetingRoom() {
        if (!isShowing) {
            return;
        }
        //      if (isFriend()) {
        //          Toast.makeText(mContext, nube + "还不是您的好友，快去添加好友吧",
        //                  Toast.LENGTH_SHORT).show();
        //          return;
        //      }
        if (callback != null) {
            callback.onMeetingCall();
        } else {
            CustomLog.d(TAG, "callback==null");
        }
    }


    // 录音状态，0：空闲状态，1：准备中状态，2：正在录音状态
    private int recordingStatus = RECORDING_STATUS_IDLE;
    private static final int RECORDING_STATUS_IDLE = 0;
    private static final int RECORDING_STATUS_READY = 1;
    private static final int RECORDING_STATUS_ING = 2;


    public boolean handleRecordLayoutTouchEvent(MotionEvent event) {
        CustomLog.i(TAG, "handleRecordLayoutTouchEvent()");
        if (!isShowing) {
            return true;
        }

        int cur_x = (int) event.getX();
        int cur_y = (int) event.getY();
        int[] recordBtnLocation = new int[2];
        noticeRecordCenter.getLocationInWindow(recordBtnLocation);
        int recordBtnX = (int) recordBtnLocation[0];
        int recordBtnY = (int) recordBtnLocation[1];

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (cur_x > recordBtnX
                    && cur_x < recordBtnX + noticeRecordCenter.getWidth()
                    && cur_y > recordBtnY
                    && cur_y < recordBtnY + noticeRecordCenter.getWidth()) {

                    if (CommonUtil.isRuntimeAboveMarshmallow()) {
                        boolean result = CommonUtil.selfPermissionGranted(mContext,
                            Manifest.permission.RECORD_AUDIO);
                        if (!result) {
                            ActivityCompat.requestPermissions(mContext,
                                new String[] {
                                    Manifest.permission.RECORD_AUDIO },
                                PERMISSIONS_REQUEST_AUDIO_RECORD_CODE);

                        } else {
                            prepareRecordAudio();
                        }
                    } else {
                        boolean hasPermission = AudioPermissionCheckUtils.checkAudioPermission(
                            mContext);
                        CustomLog.i(TAG, "< 23 hasAudioPermission = " + hasPermission);
                        if (!hasPermission) {
                            showPermissionDialog(Manifest.permission.RECORD_AUDIO);
                        } else {
                            prepareRecordAudio();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (recordingStatus == RECORDING_STATUS_ING) {
                    if (cur_x < recordBtnX
                        || cur_x > recordBtnX + noticeRecordCenter.getWidth()
                        || cur_y < recordBtnY
                        || cur_y > recordBtnY + noticeRecordCenter.getWidth()) {
                        // 手指移动到录音按钮外，提示松开取消
                        noticeRecordCenter.setText(R.string.loosen_fingers_cancel_send);

                        recordTxtTip.setText(R.string.loosen_fingers_cancel_send);
                        if (popupWindow != null) {
                            hideVoiceTipView();
                            microPhoenImageView
                                .setBackgroundResource(R.drawable.meeting_record_cancle_icon);
                        }
                        isRecordSend = false;
                    } else {
                        recordTxtTip.setText(R.string.fingers_up_cancel_send);
                        noticeRecordCenter.setText(R.string.lonsen_send);
                        int playRes = 0;
                        playRes = record_btn_playing;
                        microPhoenImageView.setBackgroundResource(record_btn_playing);
                        final AnimationDrawable drawable = (AnimationDrawable) microPhoenImageView
                            .getBackground();
                        microPhoenImageView.post(new Runnable() {
                            @Override
                            public void run() {
                                drawable.start();
                            }
                        });
                        isRecordSend = true;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                // 手指抬起，若在录音，执行结束录音操作
                noticeRecordCenter.setText(R.string.press_on_speak);
                noticeRecordCenter.setBackgroundResource(R.drawable.edittext_boder);
                stopRecord();
                break;
            default:
                break;
        }
        return true;
    }


    /**
     * 展示设置权限Dialog
     */
    private void showPermissionDialog(String permission) {
        CustomLog.i(TAG, "showPermissionDialog()");

        String dialogContent = "";
        if (permission.equalsIgnoreCase(Manifest.permission.CAMERA)) {
            dialogContent = getString(R.string.no_camera_permission_dialog_content);
        } else if (permission.equalsIgnoreCase(Manifest.permission.RECORD_AUDIO)) {
            dialogContent = getString(R.string.no_audio_permission_dialog_content);
        }
        final CustomDialog cd = new CustomDialog(mContext);
        cd.setTip(dialogContent);
        cd.setCancelBtnText(getString(R.string.btn_cancle));
        cd.setOkBtnText(getString(R.string.go_to_setting));
        cd.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                cd.dismiss();
            }
        });
        cd.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override public void onClick(CustomDialog customDialog) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

                try {
                    cd.dismiss();
                    startActivity(intent);
                } catch (Exception e) {
                    CustomLog.e(TAG, "go to permission apply " + e.toString());
                }
            }
        });
        cd.show();
    }


    public void prepareRecordAudio() {
        CustomLog.i(TAG, "prepareRecordAudio()");

        noticeRecordCenter.setText(R.string.lonsen_send);
        noticeRecordCenter.setBackgroundResource(R.drawable.edittext_boder_press);
        if (noticeRecordCenter.getVisibility() == View.VISIBLE) {
            callback.onAudioRecStart();
            startRecord();
        }
        showVoiceTipView();
    }


    private void startRecord() {
        CustomLog.d(TAG, "开始录音");
        if (recordingStatus != RECORDING_STATUS_IDLE) {
            CustomLog.d(TAG, "录音已经开始");
            return;
        }
        showPopWinwow();
        recordingStatus = RECORDING_STATUS_READY;

        noticeRecordLeftBtn.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 延迟200ms开始录音
                if (recordingStatus == RECORDING_STATUS_READY) {

                    isRecordSend = true;
                    recordingStatus = RECORDING_STATUS_ING;

                    boolean started = recorder.start();
                    CustomLog.d(TAG, "record started:" + started);

                    rcdSeconds = 0;
                    rcdProgressRun = new RcdRunnable();
                    rcdProgressRun.index = rcdIndex++;
                    new Thread(rcdProgressRun).start();
                }
            }
        }, 100);
    }


    private RcdRunnable rcdProgressRun = null;


    class RcdRunnable implements Runnable {

        public boolean isCancel = false;
        public int index = 0;


        @Override
        public void run() {
            while (recordingStatus == RECORDING_STATUS_ING && !isCancel) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rcdSeconds < 10) {
                        } else {
                            if (rcdSeconds >= 50 && rcdSeconds < 60) {
                                int remainSeconds = 60 - rcdSeconds;
                                microPhoenImageView.setVisibility(View.INVISIBLE);
                                voice.setVisibility(View.INVISIBLE);
                                countDownTxt.setVisibility(View.VISIBLE);
                                countDownTxt.setText(String.valueOf(remainSeconds));
                            } else if (rcdSeconds >= 60) {

                                CustomLog.d(TAG, "录音满60s，自动停止");
                                stopRecord();
                                return;
                            }
                        }
                        rcdSeconds++;
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    CustomLog.e(TAG, "InterruptedException" + e.toString());
                }
            }
        }
    }


    public void stopRecord() {
        if (!isShowing) {
            return;
        }
        CustomLog.d(TAG, "停止录音");

        if (popupWindow != null) {
            microPhoenImageView.setBackgroundResource(R.drawable.meeting_record_icon_6);
            popupWindow.dismiss();
        }
        if (recordingStatus == RECORDING_STATUS_IDLE) {
            CustomLog.d(TAG, "录音已停止");
            return;
        } else if (recordingStatus == RECORDING_STATUS_READY) {
            CustomLog.d(TAG, "录音正在准备中");
            recordingStatus = RECORDING_STATUS_IDLE;
            //根据需求，当快速点击录音按钮时，隐藏 语音消息 的闪烁动画
            hideVoiceTipView();
            CustomToast.show(MedicalApplication.getContext(), R.string.toast_aud_rcd_short,
                1);
            return;
        }

        if (isRecordSend) {

            File rcdFile = new File(recorder.getRcdFilePath());
            if (!rcdFile.exists()) {
                CustomLog.i(TAG, "录音文件不存在，可能是因为没有录音权限");
                //TODO 弹出对话框
                IMCommonUtil.alertPermissionDialog(mContext, null, null,
                    R.string.permission_mk_hint);
                isRecordSend = false;
                resetRecordParams();
                hideVoiceTipView();
                return;
            } else if (rcdFile.length() == 0) {
                CustomLog.i(TAG, "录音文件不存在，可能是因为没有录音权限");
                IMCommonUtil.alertPermissionDialog(mContext, null, null,
                    R.string.permission_mk_hint);
                isRecordSend = false;
                resetRecordParams();
                hideVoiceTipView();
                return;
            }


            noticeRecordLeftBtn.postDelayed(new Runnable() {
                @Override
                public void run() {

                    stopRecording();

                    sendRecordedFile();

                    resetRecordParams();
                }
            }, 500);
        } else {
            stopRecording();

            // 取消发送，删除录音文件
            File rcdFile = new File(recorder.getRcdFilePath());
            if (rcdFile != null && rcdFile.exists()) {
                boolean del = rcdFile.delete();
                CustomLog.d(TAG, "取消发送，删除录音文件:" + del);
            }

            resetRecordParams();
        }
    }


    private void resetRecordParams() {
        recordingStatus = RECORDING_STATUS_IDLE;
        isRecordSend = false;
        rcdSeconds = 0;
    }


    private void stopRecording() {
        if (rcdProgressRun != null) {
            CustomLog.d(TAG, "cancelRun,index:" + rcdProgressRun.index);
            rcdProgressRun.isCancel = true;
            rcdProgressRun = null;
        }

        recorder.stop();
    }


    private void sendRecordedFile() {

        int recordTime = rcdSeconds >= 60 ? 60 : rcdSeconds - 1;

        if (recordTime < 1) {
            CustomLog.d(TAG, "录音时间太短，不发送");
            hideVoiceTipView();
            CustomToast.show(MedicalApplication.getContext(), R.string.toast_aud_rcd_short,
                Toast.LENGTH_SHORT);
            isRecordSend = false;
        } else {
            File rcdFile = new File(recorder.getRcdFilePath());
            if (!rcdFile.exists()) {
                CustomLog.d(TAG, "录音文件不存在，不发送");
                isRecordSend = false;
            } else if (rcdFile.length() < 800) {
                CustomLog.d(TAG, "录音文件小于800Byte，不发送:" + rcdFile.length());
                isRecordSend = false;
            }
        }

        if (isRecordSend) {
            // 发送语音
            CustomLog.d(TAG, "发送音频：" + recordTime + "s | " + recorder.getRcdFilePath());
            callback.onSendAudio(recorder.getRcdFilePath(), recordTime);
        } else {
            // 取消发送，删除录音文件
            File rcdFile = new File(recorder.getRcdFilePath());
            if (rcdFile != null && rcdFile.exists()) {
                boolean del = rcdFile.delete();
                CustomLog.d(TAG, "取消发送，删除录音文件:" + del);
            }
        }
    }


    public void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    public void onPause() {
        super.onPause();
        CustomLog.i(TAG, "onPause()");

        stopRecord();
    }


    public void onStop() {
        super.onStop();
        CustomLog.i(TAG, "onStop()");
    }


    private void showPopWinwow() {

        menuView = View.inflate(MedicalApplication.getContext(),
            R.layout.m_record_poupowindow_layout, null);
        int width = IMCommonUtil.dp2px(MedicalApplication.getContext(), 146);

        popupWindow = new PopupWindow(menuView, width,
            width, true);

        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        popupWindow.setTouchable(false);

        recordTxtTip = (TextView) menuView.findViewById(R.id.canclebtn);

        countDownTxt = (TextView) menuView.findViewById(R.id.count_down_txt);
        microPhoenImageView = (ImageView) menuView
            .findViewById(R.id.m_record_audio_icon);
        voice = (ImageView) menuView.findViewById(R.id.voice);
        popupWindow.showAtLocation(
            mContext.findViewById(R.id.notice_listview),
            Gravity.CENTER, 0, 0);

        microPhoenImageView.setBackgroundResource(record_btn_playing);
        final AnimationDrawable drawable = (AnimationDrawable) microPhoenImageView
            .getBackground();
        microPhoenImageView.post(new Runnable() {
            @Override
            public void run() {
                drawable.start();
            }
        });
    }


    public interface SendCallbackInterface {
        public boolean doPreSendCheck();

        public boolean onSendTxtMsg(String txtMsg);

        public void onSendPic();

        public void onSendPicFromCamera();

        public void onSendVideo();

        public void onSendVcard();

        public void onSendAudio(String rcdFilePah, int rcdLenth);

        public void onSelectGroupMemeber();

        public void onAudioCall();

        public void onVedioCall();

        public void onMeetingCall();

        public void onAudioRecStart();

        public void onShareCollection();
    }


    // 显示下面的消息im面板
    public void showSelectlayout() {
        if (!isShowing) {
            return;
        }
        CustomLog.d(TAG, "showSelectlayout");

        if (commentEditText.getText().toString().trim().length() > 0) {
            return;
        }

        moreSelctLayout.setVisibility(View.VISIBLE);
        noticeGridView.setVisibility(View.VISIBLE);

        // 隐藏按住说话按钮
        noticeRecordCenter.setVisibility(View.GONE);
        // 显示输入框
        editLL.setVisibility(View.VISIBLE);

        // 左边按钮图标改为话筒图标
        noticeRecordLeftBtn.setBackgroundResource(R.drawable.m_record_left_btn);
    }


    // 文字变化监控
    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            String editTextStr = commentEditText.getEditableText()
                .toString();
            int pos = commentEditText.getSelectionStart();
            if (TextUtils.isEmpty(editTextStr.trim())) {
                // 输入框文字清空的场合，发送按钮变为素材选择加号按钮
                commentSendBtn.setVisibility(View.GONE);
                moreSelectBtn.setVisibility(View.VISIBLE);
            } else {
                // 输入框有文字输入的场合，素材选择加号按钮变为发送按钮
                moreSelectBtn.setVisibility(View.GONE);
                commentSendBtn.setVisibility(View.VISIBLE);
                if (deleFlag && beforeLength > editTextStr.length()) {
                    deleFlag = false;
                    commentEditText.removeTextChangedListener(mTextWatcher);
                    commentEditText.setText(saveString);
                    commentEditText.setSelection(beforePos);
                    commentEditText.addTextChangedListener(mTextWatcher);
                    saveString = "";
                }
                if (beforeLength < editTextStr.length() && selectGroupflag) {
                    if ("@".equals(editTextStr.charAt(pos - 1)
                        + "")) {
                        editTextStr = commentEditText.getEditableText()
                            .toString();
                        callback.onSelectGroupMemeber();
                    }
                }
                selectGroupflag = true;
            }
        }


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

            int position = commentEditText.getSelectionStart();
            String mStr = commentEditText.getEditableText().toString();
            beforeLength = mStr.length();
            if (after > 1) {
                selectGroupflag = false;
            }
            if (after <= 0) {
                if (position < 3) {
                    return;
                }
                if (mStr.charAt(position - 1) == IMConstant.SPECIAL_CHAR) {
                    String str = "";
                    str = mStr.substring(0, position - 1);
                    int pos = str.lastIndexOf('@');
                    beforePos = pos;
                    if (pos != -1) {
                        String startStr = mStr.substring(0, pos);
                        String lastStr = mStr.substring(position,
                            mStr.length());
                        saveString = startStr + lastStr;
                        deleFlag = true;
                    }
                }
            }
        }


        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }
    };


    @Override
    public void onResume() {
        super.onResume();

        //在此处清除焦点以防切换后台再返回程序时弹出软键盘。
        commentEditText.clearFocus();
        if (noticeRecordCenter.getVisibility() == View.VISIBLE) {
            noticeRecordCenter.setText(R.string.press_on_say);
        }
    }


    private void showVoiceTipView() {
        voiceTipView.setTipInfo(
            AccountManager.getInstance(mContext).getAccountInfo().getHeadThumUrl()
            , chatDataCursor);
        voiceTipView.startAnimation();
        noticeListview.addFooterView(voiceTipView);
    }


    public void hideVoiceTipView() {
        // chatDataCursor.close();
        chatDataCursor = null;
        voiceTipView.stopAnimation();
        noticeListview.removeFooterView(voiceTipView);
    }

}
