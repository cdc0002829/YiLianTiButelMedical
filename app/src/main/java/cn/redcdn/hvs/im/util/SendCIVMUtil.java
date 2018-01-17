package cn.redcdn.hvs.im.util;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.activity.MultiBucketChooserActivity;
import cn.redcdn.hvs.im.activity.RecordFinishVideoActivity;
import cn.redcdn.hvs.im.activity.RecordingVideoAndPictureActivity;
import cn.redcdn.hvs.im.activity.SelectLinkManActivity;
import cn.redcdn.hvs.im.bean.ButelPAVExInfo;
import cn.redcdn.hvs.im.bean.ButelVcardBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.manager.FileManager;
import cn.redcdn.hvs.meeting.activity.ReserveMeetingRoomActivity;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.profiles.dialog.CameraImageDialog;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.jmeetingsdk.MeetingInfo;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.LogUtil;
import com.butel.connectevent.utils.NetWorkUtil;
import java.util.ArrayList;
import java.util.List;

import static cn.redcdn.hvs.MedicalApplication.context;
import static cn.redcdn.hvs.im.activity.ChatActivity.PERMISSIONS_REQUEST_AUDIO_RECORD_CODE;
import static cn.redcdn.hvs.im.activity.ChatActivity.PERMISSIONS_REQUEST_CAMERA_CODE;

/**
 * <dl>
 * <dt>SendCIVMUtil.java</dt>
 * <dd>Description:发送图片/视频/名片/会议相关，公共类</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 北京红云融通技术有限公司</dd>
 * <dd>CreateDate: 2016-1-15 下午13:41:40</dd>
 * </dl>
 *
 * @author niuben
 */
public class SendCIVMUtil {
    private static final String TAG = SendCIVMUtil.class.getSimpleName();

    public static final int ACTION_SHARE_VIDEO_FROM_CAMERA = 1100;
    public static final int ACTION_SHARE_VIDEO_FROM_NATIVE = 1101;
    public static final int ACTION_SHARE_PIC_FROM_CAMERA = 1102;
    public static final int ACTION_SHARE_PIC_FROM_NATIVE = 1103;
    public static final int ACTION_SHARE_VCARD = 1104;

    public static final int ACTION_FOR_RESERVE_MEETING = 2000;
    public static final int RESULTCODE_RESERVE_MEETING_SUCCESS = 2001;
    public static final int RESULTCODE_RESERVE_MEETING_FAILURE = 2002;

    public static String cameraFilePath = "";

    private static Activity mActivity;
    private static int operationCode = 0; // 0：未操作 1:创建会议中 2:加入会议中
    private CameraImageDialog cid;


    private static boolean checkNetWork(Activity activity) {
        boolean connect = NetWorkUtil.isNetworkConnected(activity);
        if (!connect) {
            showToast(
                activity,
                activity.getResources().getString(
                    R.string.no_network_connect));
        }
        CustomLog.d("TAG", "connect=" + connect);
        return connect;
    }


    /**
     * @param activity 显示 发图片 的activity
     */
    public static void sendPatient(final Activity activity) {
        CustomLog.d("TAG", "发送图片");
        sendPatientPic(activity);
        CustomLog.d("TAG", "");
    }


    /**
     * @param activity 显示 发图片 的activity
     */
    public static void sendPic(final Activity activity) {
        CustomLog.d("TAG", "发送图片");
        sendPicFromNative(activity);
        CustomLog.d("TAG", "");
    }


    /**
     * 跳转到选择图片 页面
     */
    private static void sendPatientPic(Activity activity) {
        CustomLog.d("TAG", "选择图片");
        Intent i = new Intent(activity,
            MultiBucketChooserActivity.class);
        i.putExtra(MultiBucketChooserActivity.KEY_BUCKET_TYPE,
            MultiBucketChooserActivity.BUCKET_TYPE_IMAGE);
        i.putExtra(MultiBucketChooserActivity.KEY_FROM_TYPE,
            MultiBucketChooserActivity.FROM_TYPE_PATIENT);
        activity.startActivityForResult(i, ACTION_SHARE_PIC_FROM_NATIVE);
        CustomLog.d("TAG", "");
    }


    /**
     * 跳转到选择图片 页面
     */
    private static void sendPicFromNative(Activity activity) {
        CustomLog.d("TAG", "选择图片");
        Intent i = new Intent(activity,
            MultiBucketChooserActivity.class);
        i.putExtra(MultiBucketChooserActivity.KEY_BUCKET_TYPE,
            MultiBucketChooserActivity.BUCKET_TYPE_IMAGE);
        activity.startActivityForResult(i, ACTION_SHARE_PIC_FROM_NATIVE);
        CustomLog.d("TAG", "");
    }


    /**
     * 调用系统相机拍照, 点击素材面板拍摄按钮
     */
    public static void sendPicFromCamera(final Activity activity) {
        CustomLog.i(TAG, "sendPicFromCamera()");

        boolean result = CommonUtil.selfPermissionGranted(MedicalApplication.getContext(),
            Manifest.permission.CAMERA);
        CustomLog.i(TAG, "< 23 hasCameraPermission = " + result);

        if (!result) {
            if (CommonUtil.isRuntimeAboveMarshmallow()) {
                ActivityCompat.requestPermissions(activity,
                    new String[] { Manifest.permission.CAMERA },
                    PERMISSIONS_REQUEST_CAMERA_CODE);
            } else {
                showPermissionDialog(Manifest.permission.CAMERA, activity);
            }
        } else {
            jumpToRecordActivity(activity, "pic", ACTION_SHARE_PIC_FROM_CAMERA);
        }
    }


    /**
     * 展示设置权限Dialog
     */
    private static void showPermissionDialog(String permission, final Activity activity) {
        CustomLog.i(TAG, "showPermissionDialog()");

        String dialogContent = "";
        if (permission.equalsIgnoreCase(Manifest.permission.CAMERA)) {
            dialogContent = activity.getString(R.string.no_camera_permission_dialog_content);
        } else if (permission.equalsIgnoreCase(Manifest.permission.RECORD_AUDIO)) {
            dialogContent = activity.getString(R.string.no_audio_permission_dialog_content);
        } else if (permission.equalsIgnoreCase(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            dialogContent = activity.getString(R.string.no_storage_permission_dialog_content);
        }
        final CustomDialog cd = new CustomDialog(activity);
        cd.setTip(dialogContent);
        cd.setCancelBtnText(activity.getString(R.string.btn_cancle));
        cd.setOkBtnText(activity.getString(R.string.go_to_setting));
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
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

                try {
                    cd.dismiss();
                    activity.startActivity(intent);
                } catch (Exception e) {
                    CustomLog.e(TAG, "go to permission apply " + e.toString());
                }
            }
        });
        cd.show();
    }


    /**
     * 选择图片 返回
     *
     * @param activity 需要处理的context
     * @param data 返回回来的数据
     * @param sender 发送者
     * @param receiver 接收者
     * @param convstId 会话id（单聊时，可不传，群聊时传入groupid）
     */
    public static boolean onSendPicFromNativeBack(final Activity activity,
                                                  final Intent data, final String sender, final String receiver,
                                                  final String convstId) {
        CustomLog.i(TAG, "onSendPicFromNativeBack()");

        if (data == null) {
            LogUtil.d("data==null");
            return false;
        }
        if (!checkNetWork(activity)) {
            return false;
        }
        final ArrayList<String> selectedPicList = data.getExtras()
            .getStringArrayList(Intent.EXTRA_STREAM);
        if (selectedPicList == null || selectedPicList.size() == 0) {
            LogUtil.d("selectedPicList为空");
            return false;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (String picPath : selectedPicList) {
                    doSendPic(activity, picPath, sender, receiver, convstId);
                }
            }
        }).start();
        return true;
    }


    /**
     * 拍摄完照片后返回的操作
     *
     * @param activity 需要处理的context
     * @param sender 发送者
     * @param receiver 接收者
     * @param convstId 会话id（单聊时，可不传，群聊时传入groupid）
     */
    public static boolean onSendPicFromCameraBack(final Activity activity,
                                                  final String sender, final String receiver, final String convstId) {
        if (!checkNetWork(activity)) {
            return false;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(cameraFilePath)) {
                    IMCommonUtil.scanFileAsync(activity, cameraFilePath);
                }
                doSendPic(activity, cameraFilePath, sender, receiver, convstId);
            }
        }).start();
        return true;
    }


    /**
     * 正在的发图片接口test
     */
    private static void doSendPic(Activity activity, String picPath,
                                  String sender, String receiver, String convstId) {
        CustomLog.i(TAG, "doSendPic()");

        List<String> localFiles = new ArrayList<String>();
        localFiles.add(picPath);
        int[] imgSize = FileManager.getImageSizeByPath(activity, picPath);
        CustomLog.d(TAG, "图片尺寸：" + imgSize[0] + "|" + imgSize[1] + "|" + imgSize[2]);
        ButelPAVExInfo extInfo = new ButelPAVExInfo();
        if (imgSize[2] == 90 || imgSize[2] == 270) {// 图片有方向的场合，为了显示正常，需要将宽高对调
            extInfo.setWidth(imgSize[1]);
            extInfo.setHeight(imgSize[0]);
        } else {
            extInfo.setWidth(imgSize[0]);
            extInfo.setHeight(imgSize[1]);
        }

        String uuid = new NoticesDao(activity).createSendFileNotice(sender,
            receiver, localFiles, "",
            FileTaskManager.NOTICE_TYPE_PHOTO_SEND, "", convstId, extInfo);
        MedicalApplication.getFileTaskManager().addTask(uuid, null);
    }


    /**
     * 发名片的 入口
     */
    public static void sendVcard(Activity activity) {
        // LogUtil.begin("发送 名片 ");
        Intent intent = new Intent(activity, SelectLinkManActivity.class);
        intent.putExtra(SelectLinkManActivity.ACTIVITY_FLAG,
            SelectLinkManActivity.AVITVITY_START_FOR_RESULT);
        intent.putExtra(SelectLinkManActivity.KEY_IS_SIGNAL_SELECT, false);
        intent.putExtra(SelectLinkManActivity.AVITVITY_TITLE, activity
            .getResources().getString(R.string.select_vcard_title));
        intent.putExtra(SelectLinkManActivity.KEY_SINGLE_CLICK_BACK, true);
        intent.putStringArrayListExtra(
            SelectLinkManActivity.KEY_SELECTED_NUBENUMBERS,
            new ArrayList<String>());
        activity.startActivityForResult(intent, ACTION_SHARE_VCARD);
        // LogUtil.end("");
    }


    /**
     * 发名片的消息的task
     */
    public static boolean onSendVcardBack(final Activity activity, final ButelVcardBean bean, final String sender, final String receiver, final String convstId) {
        CustomLog.i(TAG, "onSendVcardBack()");

        if (bean == null) {
            LogUtil.d("bean==null");
            return false;
        }

        if (!checkNetWork(activity)) {
            return false;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String title = ShowNameUtil.getShowName(ShowNameUtil
                    .getNameElement("", bean.getNickname(),
                        bean.getPhoneNumber(), bean.getNubeNumber()));
                String vcfFilePath = PostCardUtil.ContactHandler.createNubeVcf(activity, bean);
                LogUtil.d("vcfFilePath = " + vcfFilePath);
                List<String> localpath = null;
                if (!TextUtils.isEmpty(vcfFilePath)) {
                    localpath = new ArrayList<String>();
                    localpath.add(vcfFilePath);
                }
                String uuid = new NoticesDao(activity).createSendFileNotice(
                    sender, receiver, localpath, activity.getResources()
                        .getString(R.string.title_share_vcard, title),
                    FileTaskManager.NOTICE_TYPE_VCARD_SEND, "", convstId, bean);
                MedicalApplication.getFileTaskManager().addTask(uuid, null);
            }
        }).start();
        return true;
    }


    /**
     * 发视频入口 在移动网络环境，非Wifi网络，每次触发视频、语音通话及视频分享操作，需提醒用户，告知操作将产生的结果
     *
     * @param activity 展示VideoMenu的入口
     */
    public static void sendVideo(final Activity activity) {
        CustomLog.i(TAG, "sendVideo()");

        if (!checkNetWork(activity)) {
            return;
        }
        checkVideoPermission(activity);
    }


    /**
     * 检查拍摄视频所需权限 （相机，声音，存储）
     */
    private static void checkVideoPermission(final Activity activity) {
        CustomLog.i(TAG, "checkVideoPermission()");

        boolean hasCameraPermission = CommonUtil.selfPermissionGranted(
            MedicalApplication.getContext(),
            Manifest.permission.CAMERA);
        boolean hasAudioPermission = CommonUtil.selfPermissionGranted(activity,
            Manifest.permission.RECORD_AUDIO);
        String hasStoragePermission = Environment.getExternalStorageState();

        if (!hasCameraPermission) {
            CustomLog.i(TAG, "no camera permission");

            if (CommonUtil.isRuntimeAboveMarshmallow()) {
                ActivityCompat.requestPermissions(activity,
                    new String[] { Manifest.permission.CAMERA },
                    PERMISSIONS_REQUEST_CAMERA_CODE);
            } else {
                showPermissionDialog(Manifest.permission.CAMERA, activity);
            }
        } else {
            if (!hasStoragePermission.equals(Environment.MEDIA_MOUNTED)) {
                CustomLog.i(TAG, "no storage permission");
                showPermissionDialog(Manifest.permission.WRITE_EXTERNAL_STORAGE, activity);
            } else {
                if (!hasAudioPermission) {
                    if (CommonUtil.isRuntimeAboveMarshmallow()) {
                        ActivityCompat.requestPermissions(activity,
                            new String[] { Manifest.permission.RECORD_AUDIO },
                            PERMISSIONS_REQUEST_AUDIO_RECORD_CODE);
                    } else {
                        showPermissionDialog(Manifest.permission.RECORD_AUDIO, activity);
                    }
                } else {
                    jumpToRecordActivity(activity, "video", ACTION_SHARE_VIDEO_FROM_CAMERA);
                }

            }
        }
    }


    private static void jumpToRecordActivity(Activity activity, String video, int actionShareVideoFromCamera) {
        CustomLog.i(TAG, "jumpToRecordActivity()");

        Intent i = new Intent(activity,
            RecordingVideoAndPictureActivity.class);
        i.putExtra("where", video);
        activity.startActivityForResult(i,
            actionShareVideoFromCamera);
    }


    /**
     * 选择视频返回
     *
     * @param activity 需要处理的context
     * @param data 返回回来的数据
     * @param sender 发送者
     * @param receiver 接收者
     * @param convstId 会话id（单聊时，可不传，群聊时传入groupid）
     */

    public static boolean onSendVideoFromNativeBack(final Activity activity,
                                                    final Intent data, final String sender, final String receiver,
                                                    final String convstId) {
        CustomLog.i(TAG, "onSendVideoFromNativeBack()");

        if (data == null) {
            LogUtil.d("data == null");
            return false;
        }

        if (!checkNetWork(activity)) {
            return false;
        }

        final ArrayList<String> selectedVideoList = data.getExtras()
            .getStringArrayList(Intent.EXTRA_STREAM);
        if (selectedVideoList == null || selectedVideoList.size() == 0) {
            LogUtil.d("selectedVideoList为空");
            return false;
        }
        for (int i = 0; i < selectedVideoList.size(); i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 视频每次只能发一个
                    String videoPath = selectedVideoList.get(finalI);
                    int videoDuration = FileManager.getVideoDurationByPath(
                        activity, videoPath);
                    doSendVedio(activity, videoPath, videoDuration, sender,
                        receiver, convstId);
                }
            }).start();
        }

        LogUtil.end("");
        return true;
    }


    /**
     * 拍摄视频返回
     *
     * @param activity 需要处理的context
     * @param data 返回回来的数据
     * @param sender 发送者
     * @param receiver 接收者
     * @param convstId 会话id（单聊时，可不传，群聊时传入groupid）
     */
    public static boolean onSendVideoFromCameraBack(final Activity activity,
                                                    final Intent data, final String sender, final String receiver,
                                                    final String convstId) {
        LogUtil.begin("");
        if (data == null) {
            LogUtil.d("data == null");
            return false;
        }
        if (!checkNetWork(activity)) {
            return false;
        }
        // 拍摄视频分享
        new Thread(new Runnable() {
            @Override
            public void run() {
                String filePath = data
                    .getStringExtra(RecordFinishVideoActivity.KEY_VIDEO_FILE_PATH);
                if (!TextUtils.isEmpty(filePath)) {
                    IMCommonUtil.scanFileAsync(activity, filePath);
                }
                // 视频时长
                int duration = data.getIntExtra(
                    RecordFinishVideoActivity.KEY_VIDEO_FILE_DURATION, 0) * 1000;
                doSendVedio(activity, filePath, duration, sender, receiver,
                    convstId);
            }
        }).start();
        LogUtil.end("");
        return true;
    }


    /**
     * 发视频接口test
     */
    private static void doSendVedio(Activity activity, String picPath,
                                    int duration, String sender, String receiver, String convstId) {
        LogUtil.begin("picPath=" + picPath);
        List<String> localFiles = new ArrayList<String>();
        localFiles.add(picPath);

        ButelPAVExInfo extInfo = new ButelPAVExInfo();
        extInfo.setDuration(duration / 1000);

        Bitmap imageBit = FileManager.createVideoThumbnail(picPath);
        int imageWidth = 0;
        int imageHeight = 0;
        if (imageBit != null) {
            imageWidth = imageBit.getWidth();
            imageHeight = imageBit.getHeight();
        }
        extInfo.setWidth(imageWidth);
        extInfo.setHeight(imageHeight);
        String uuid = new NoticesDao(activity).createSendFileNotice(sender,
            receiver, localFiles, "",
            FileTaskManager.NOTICE_TYPE_VEDIO_SEND, "", convstId, extInfo);

        // 进入发送任务队列
        MedicalApplication.getFileTaskManager().addTask(uuid, null);
        LogUtil.end("");
    }

    // ///////////////////////////////////////发视频功能结束///////////////////////////////////

    ////////////////////////////////////////召开会议开始//////////////////////////////////////


    /**
     * @param tag 那个页面创建的改消息
     * @param conversationType 消息类型（单聊/群聊）
     * @param targetId 单聊时nube号，群聊时传入groupId
     * @param selfNube 本地账户（由于页面都已经获取不到，这里直接传入，没有再读数据库）
     * @return false 创建失败 true 创建成功
     */
    public static boolean conveneMeeting(final Activity activity, String tag, int conversationType, final String targetId, final String selfNube) {
        // LogUtil.begin(
        //     "tag=" + tag + "|conversationType=" + conversationType + "|targetId=" + targetId +
        //         "|selfNube=" + selfNube);
        operationCode = 0;
        if (!checkNetWork(activity)) {
            showToast(activity, context.getResources()
                .getString(R.string.network_not_to_force__please_check_the_network));
            return false;
        }
        mActivity = activity;

        ArrayList<String> nubeList = new ArrayList<String>();
        if (conversationType == ChatActivity.VALUE_CONVERSATION_TYPE_MULTI) {
            GroupDao mgroupDap = new GroupDao(activity);
            if (targetId.length() > 12 && !mgroupDap.isGroupMember(targetId, selfNube)) {
                showToast(activity,
                    context.getResources().getString(R.string.consultation_failed_to_create));
                return false;
            }
            nubeList = mgroupDap.queryGroupNumbers(targetId);
        } else {
            nubeList.add(targetId);
        }

        final MedicalMeetingManage meetingManager = MedicalMeetingManage.getInstance();
        showLoadingView("正在创建会诊", new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (operationCode == 0) {
                    CustomLog.d(TAG, "未开始创建，就点击返回键");
                } else if (operationCode == 1) {
                    meetingManager.cancelCreateMeeting("SendCIVMUtil");
                } else if (operationCode == 2) {
                    meetingManager.cancelJoinMeeting(getClass().getName());
                } else {
                    CustomLog.d(TAG, "operationCode nuber error" + operationCode);
                }
                removeLoadingView();
            }
        }, true);
        operationCode = 1;
        int reslut = meetingManager.createMeeting(TAG, nubeList,
            new MedicalMeetingManage.OnCreateMeetingListener() {
                @Override
                public void onCreateMeeting(int valueCode, MeetingInfo meetingInfo) {
                    CustomLog.d(TAG, "onCreatemeeting valueDes:");
                    if (valueCode != 0) {
                        CustomLog.d(TAG, "onCreateMeeting 返回失败");
                        removeLoadingView();
                        CustomToast.show(activity, context.getResources()
                                .getString(R.string.failed_to_create_a_consultation__please_try_again),
                            1);
                        return;
                    }
                    final String meetingId = meetingInfo.meetingId;
                    CustomLog.d(TAG, "onCreateMeeting 收到,meetingInfo==" + meetingInfo.meetingId);
                    CustomLog.d(TAG, "开始加入会诊,meetingId:" + meetingInfo.meetingId);
                    operationCode = 2;
                    int joinResult = meetingManager.joinMeeting(meetingInfo.meetingId,
                        new MedicalMeetingManage.OnJoinMeetingListener() {
                            @Override
                            public void onJoinMeeting(String valueDes, int valueCode) {
                                removeLoadingView();
                                CustomLog.d(TAG,
                                    "onJoinMeeting valueCode" + valueCode + " valueDes" + valueDes);
                                if (valueCode != 0) {
                                    CustomLog.d(TAG, "加入会诊失败");
                                    CustomToast.show(activity, context.getResources()
                                            .getString(
                                                R.string.failed_to_create_a_consultation__please_try_again),
                                        1);
                                    return;
                                }
                                ArrayList<String> list = new ArrayList<String>();
                                list.add(targetId);
                                MedicalMeetingManage manager = MedicalMeetingManage.getInstance();
                                // manager.setContext(activity);
                                manager.inviteMeeting(list, meetingId);
                            }
                        });
                    if (joinResult == 0) {
                        CustomLog.d(TAG, "加入会诊成功");
                    } else {
                        CustomLog.d(TAG, "加入会诊失败");
                        CustomToast.show(activity, context.getResources()
                            .getString(R.string.join_the_consultation_failed_please_try_again), 1);
                        removeLoadingView();
                    }
                }
            });
        if (reslut == 0) {
            CustomLog.d(TAG, "创建会诊成功");
        } else {
            CustomLog.d(TAG, "创建会诊失败");
            CustomToast.show(activity, context.getResources()
                .getString(R.string.failed_to_create_a_consultation__please_try_again), 1);
            removeLoadingView();
        }
        return true;
    }


    /**
     * @param tag 当前Tag
     * @param conversationType 消息类型（单聊/群聊）
     * @param targetId 单聊时nube号，群聊时传入groupId
     * @param intent 创建消息返回的intent
     * @return true 是当前需要处理的，false不是需要处理的
     */
    public static boolean onConveneMeetingBack(String tag, int conversationType, String targetId, Intent intent) {
        if (!tag.equals(intent.getStringExtra("contextid"))) {
            return false;
        }
        int code = intent.getIntExtra("code", -1);
        LogUtil.d("code=" + code);
        if (code == 0) {
        }
        return true;
    }
    ////////////////////////////////////////召开会议结束//////////////////////////////////////

    ////////////////////////////////////////预约会议开始//////////////////////////////////////


    public static boolean bookMeeting(Activity activity, String nubeOrGid, boolean isgroup) {
        Intent intent = new Intent(activity, ReserveMeetingRoomActivity.class);
        ArrayList<String> nubes = new ArrayList<String>();
        if (isgroup) {
            intent.putExtra("gid", nubeOrGid);
            ArrayList<String> nubelist = new GroupDao(activity).queryGroupNumbers(nubeOrGid);
            if (nubelist != null) {
                nubes.addAll(nubelist);
            }
        } else {
            nubes.add(nubeOrGid);
        }
        intent.putStringArrayListExtra("userlist", nubes);
        activity.startActivityForResult(intent, ACTION_FOR_RESERVE_MEETING);
        return true;
    }


    protected static void showToast(Context context, String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }


    private static Dialog dialog = null;


    private static void showLoadingView(String message,
                                        final DialogInterface.OnCancelListener listener, boolean cancelAble) {
        CustomLog.i(TAG, "MeetingActivity::showLoadingDialog() msg: " + message);
        try {
            if (dialog != null) {
                dialog.dismiss();
            }
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
        if (mActivity == null) {
            CustomLog.d(TAG, "activity 为 null,初始化dialog失败");
            return;
        }
        dialog = cn.redcdn.hvs.util.CommonUtil.createLoadingDialog(mActivity, message, listener);
        dialog.setCancelable(cancelAble);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    listener.onCancel(dialog);
                }
                return false;
            }
        });
        try {
            dialog.show();
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
    }


    private static void removeLoadingView() {

        CustomLog.i(TAG, "MeetingActivity::removeLoadingView()");

        if (dialog != null) {

            dialog.dismiss();

            dialog = null;

        }

    }

}
