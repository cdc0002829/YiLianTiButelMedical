package cn.redcdn.hvs.im.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.im.activity.EmbedWebViewActivity;
import cn.redcdn.hvs.im.adapter.ChatListAdapter;
import cn.redcdn.hvs.im.bean.ButelFileInfo;
import cn.redcdn.hvs.im.bean.CollectionBean;
import cn.redcdn.hvs.im.bean.CollectionEntity;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.collection.ButeleCollectionFile;
import cn.redcdn.hvs.im.collection.CollectionFileManager;
import cn.redcdn.hvs.im.collection.CollectionUpdateWorkManager;
import cn.redcdn.hvs.im.dao.CollectionDao;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.NetPhoneDaoImpl;
import cn.redcdn.hvs.im.fileTask.DownloadTaskManager;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.preference.DaoPreference.PrefType;
import cn.redcdn.hvs.im.view.BottomMenuWindow.MenuClickedListener;
import cn.redcdn.hvs.im.view.MedicalAlertDialog;
import cn.redcdn.hvs.profiles.activity.CollectionActivity;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.DateUtil;
import cn.redcdn.hvs.util.StringUtil;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.LogUtil;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static cn.redcdn.hvs.MedicalApplication.context;

/**
 * @ClassName: CollectionManager.java
 * @Description: 收藏管理类
 * @author: 牛犇，陈从江
 * @date: 2016年5月10日
 */
public class CollectionManager {
    private static CollectionManager CollectionManager;
    private CollectionDao mCollectionDao;


    public static CollectionManager getInstance() {
        if (null == CollectionManager) {
            CollectionManager = new CollectionManager();
        }
        return CollectionManager;
    }


    public CollectionManager() {
        mCollectionDao = new CollectionDao(MedicalApplication.getContext());
    }


	/*-----------------------------------------------------------------------------------------------查询 开始-------------------------------------------------------------------------------------------------------------------------*/


    /**
     * 获取指定类型 的所有显示bean
     */
    public List<CollectionBean> getCollectionRecordsByType(int type) {
        List<CollectionBean> allBeans = mCollectionDao.getAllCollectionRecord();
        if (type < 0) {
            return allBeans;
        }
        List<CollectionBean> queryBeans = new ArrayList<CollectionBean>();
        for (CollectionBean bean : allBeans) {
            if (bean.isMatch(type)) {
                queryBeans.add(bean);
            }
        }
        return queryBeans;
    }


    /**
     * @return 在allBeans下根据text模糊匹配
     */
    public List<CollectionBean> getCollectionRecordsByText(List<CollectionBean> allBeans, String text) {
        if (TextUtils.isEmpty(text)) {
            return allBeans;
        }

        List<CollectionBean> queryBeans = new ArrayList<CollectionBean>();
        for (CollectionBean bean : allBeans) {
            if (bean.isMatch(text)) {
                queryBeans.add(bean);
            }
        }
        return queryBeans;
    }
    /*-----------------------------------------------------------------------------------------------查询 结束-------------------------------------------------------------------------------------------------------------------------*/


	/*-----------------------------------------------------------------------------------添加/删除  收藏记录-----------------------------------------------------------------------------------------------------------------------------*/


    /**
     * 从消息收藏，适用于音频、图片、视频
     */
    public void addCollectionByNoticesBean(Context context, NoticesBean bean) {
        CustomLog.d("CollectionManager",
            "addCollection body" + bean.getBody() + " uuid:" + bean.getId());
        //收藏中增加 ForwarderName、ForwarderHeaderUrl参数
        String newBody = bean.getBody();
        MDSAccountInfo loginUserInfo = AccountManager.getInstance(context).getAccountInfo();
        if (loginUserInfo.getNube().equals(bean.getSender())) {
            newBody = addNewMsgToBody(bean.getBody(), loginUserInfo.getNickName(),
                loginUserInfo.getHeadThumUrl(), loginUserInfo.getNube(), bean.getThreadsId());
        } else {
            ContactFriendBean friendBean = new NetPhoneDaoImpl(context).queryFriendInfoByNube(
                bean.getSender());
            if (friendBean != null && !TextUtils.isEmpty(friendBean.getNubeNumber())) {
                newBody = addNewMsgToBody(bean.getBody(), friendBean.getNickname(),
                    friendBean.getHeadUrl(), friendBean.getNubeNumber(), bean.getThreadsId());
            } else {
                //收藏群中的陌生人消息时，从群成员表中查信息
                GroupDao groupDao = new GroupDao(MedicalApplication.getContext());
                if (groupDao.existGroup(bean.getThreadsId())) {
                    GroupMemberBean memberBean = groupDao.queryGroupMember(bean.getThreadsId(),
                        bean.getSender());
                    newBody = addNewMsgToBody(bean.getBody(), memberBean.getNickName(),
                        memberBean.getHeadUrl(), bean.getSender(), bean.getThreadsId());
                } else {
                    if (bean.getSender().equals(SettingData.getInstance().adminNubeNum)) {
                        String KEY_SERVICE_NUBE_INFO = "ServiceNubeInfo";
                        SharedPreferences preferences = MedicalApplication.getContext()
                            .getSharedPreferences(KEY_SERVICE_NUBE_INFO, context.MODE_PRIVATE);
                        String nickName = preferences.getString("USERNAME",
                            SettingData.getInstance().adminNubeNum);
                        String headUrl = preferences.getString("HEAD_URL", "");
                        newBody = addNewMsgToBody(bean.getBody(), nickName, headUrl,
                            bean.getSender(), bean.getThreadsId());
                    } else {
                        newBody = addNewMsgToBody(bean.getBody(), bean.getSender(), "",
                            bean.getSender(), bean.getThreadsId());
                    }
                }

            }
        }
        bean.setBody(newBody);

        int code = mCollectionDao.addCollectionRecordByNoticesBean(bean);
        switch (code) {
            case -1:
                CustomToast.show(context, context.getString(R.string.collect_fail), 1);
                break;
            case 1:
                CustomToast.show(context, context.getString(R.string.have_collected), 1);
                break;
            case 0:
                CustomToast.show(context, context.getString(R.string.collect_success), 1);
                break;
            default:
                break;
        }
        startUpDateWork();
    }


    private String addNewMsgToBody(String oldBody, String name, String headUrl, String operatorNube, String threadId) {

        try {
            JSONArray jsonArray = new JSONArray(oldBody);
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            //收藏列表在显示文章时，头像使用的是 offAccLogoUrl 字段
            if (jsonObject.has("ArticleId")) {
                jsonObject.put("offAccLogoUrl", headUrl);
            }
            jsonObject.put("operatorNube", operatorNube);
            jsonObject.putOpt("ForwarderName", name);
            jsonObject.putOpt("ForwarderHeaderUrl", headUrl);
            GroupDao groupDao = new GroupDao(MedicalApplication.getContext());
            if (groupDao.existGroup(threadId)) {
                jsonObject.putOpt("GroupName", groupDao.getOriginGroupNameByGid(threadId));
            } else {
                jsonObject.putOpt("GroupName", "");
            }
            return jsonArray.toString();
        } catch (Exception e) {
            CustomLog.d("addNewMsgToBody", "解析json 出错");
        }
        return "";
    }


    public void addCollectionByNoticesBeans(List<NoticesBean> beanList) {
        if (beanList != null && beanList.size() > 0) {
            for (NoticesBean bean : beanList) {
                //收藏中增加 ForwarderName、ForwarderHeaderUrl参数
                String newBody = bean.getBody();
                MDSAccountInfo loginUserInfo = AccountManager.getInstance(context).getAccountInfo();
                if (loginUserInfo.getNube().equals(bean.getSender())) {
                    newBody = addNewMsgToBody(bean.getBody(), loginUserInfo.getNickName(),
                        loginUserInfo.getHeadThumUrl(), loginUserInfo.getNube(),
                        bean.getThreadsId());
                } else {
                    ContactFriendBean friendBean = new NetPhoneDaoImpl(
                        context).queryFriendInfoByNube(bean.getSender());
                    if (friendBean != null && !TextUtils.isEmpty(friendBean.getNubeNumber())) {
                        newBody = addNewMsgToBody(bean.getBody(), friendBean.getNickname(),
                            friendBean.getHeadUrl(), friendBean.getNubeNumber(),
                            bean.getThreadsId());
                    } else {
                        newBody = addNewMsgToBody(bean.getBody(), bean.getSender(), "",
                            bean.getSender(), bean.getThreadsId());
                    }
                }
                bean.setBody(newBody);
                int code = mCollectionDao.addCollectionRecordByNoticesBean(bean);
                LogUtil.d("code : " + code);
            }
            startUpDateWork();
        } else {
            LogUtil.d("beanList is empty");
        }
    }


    /**
     * 将body数组中的json对象的key替换
     */
    public static String modifyBodyJsonKey(JSONArray bodyArray, Map<String, String> keys) {
        if (bodyArray == null) {
            return "";
        }

        if (keys == null || keys.isEmpty()) {
            return bodyArray.toString();
        }

        try {
            for (int i = 0; i < bodyArray.length(); i++) {
                JSONObject itemObj = bodyArray.optJSONObject(i);

                for (Entry<String, String> item : keys.entrySet()) {
                    String fromKey = item.getKey();

                    if (itemObj.has(fromKey)) {
                        String toKey = item.getValue();
                        String fromKeyValue = itemObj.optString(fromKey);
                        itemObj.remove(fromKey);
                        itemObj.put(toKey, fromKeyValue);

                        bodyArray.put(i, itemObj);
                    }
                }
            }

            return bodyArray.toString();
        } catch (Exception e) {
            LogUtil.e("", e);
            String resultStr = bodyArray.toString();
            for (Entry<String, String> item : keys.entrySet()) {
                resultStr.replace("\"" + item.getKey() + "\":",
                    "\"" + item.getValue() + "\":");
            }
            return resultStr;
        }
    }


    /**
     * 收藏本地图片
     */
    public void addCollectionFromPic(Context context, List<String> localfiles) {
        if (localfiles == null || localfiles.size() == 0) {
            LogUtil.d("localfiles为空");
            return;
        }
        String body = getCollectionPicBody(context, localfiles);
        if (TextUtils.isEmpty(body)) {
            return;
        }
        CollectionEntity entity = new CollectionEntity();
        entity.setId(StringUtil.getUUID());
        entity.setOperateTime(System.currentTimeMillis() / 1000);
        entity.setOperatorNube(
            MedicalApplication.getPreference().getKeyValue(PrefType.LOGIN_NUBENUMBER, ""));
        entity.setType(FileTaskManager.NOTICE_TYPE_PHOTO_SEND);
        entity.setStatus(CollectionEntity.STATUS_EFFECTIVE);
        entity.setSyncStatus(CollectionEntity.SYNCSTATUS_NEED);
        entity.setBody(body);
        entity.setExtinfo("");
        mCollectionDao.insert(entity);
        startUpDateWork();
    }


    public String getCollectionPicBody(Context context, List<String> localfiles) {
        JSONArray bodyArray = new JSONArray();
        for (String localfile : localfiles) {
            JSONObject object = new JSONObject();
            try {
                object.put("localUrl", localfile);
                object.put("remoteUrl", "");
                long size = 0;
                if (!TextUtils.isEmpty(localfile)) {
                    File file = new File(localfile);
                    if (file.exists()) {
                        size = file.length();
                    }
                }
                object.put("size", size);
                object.put("compressPath", "");
                int[] imgSize = FileManager.getImageSizeByPath(context, localfile);
                if (imgSize[2] == 90 || imgSize[2] == 270) {// 图片有方向的场合，为了显示正常，需要将宽高对调
                    object.put("photoWidth", imgSize[1]);
                    object.put("photoHeight", imgSize[0]);
                } else {
                    object.put("photoWidth", imgSize[0]);
                    object.put("photoHeight", imgSize[1]);
                }
                object.put("duration", 0);
                bodyArray.put(object);
            } catch (JSONException e) {
                LogUtil.e("JSONException", e);
            }
        }
        return bodyArray.toString();
    }


    /**
     * 从本地收藏文件
     */
    public void addCollectionFromLocal(Context context, ButeleCollectionFile file) {
        CollectionEntity entity = new CollectionEntity();
        entity.setId(StringUtil.getUUID());
        entity.setOperateTime(System.currentTimeMillis() / 1000);
        entity.setOperatorNube(
            MedicalApplication.getPreference().getKeyValue(PrefType.LOGIN_NUBENUMBER, ""));
        entity.setType(FileTaskManager.NOTICE_TYPE_FILE);
        entity.setStatus(CollectionEntity.STATUS_EFFECTIVE);
        entity.setSyncStatus(CollectionEntity.SYNCSTATUS_NEED);

        ButelFileInfo info = new ButelFileInfo();
        info.setFileName(file.getMtext());
        info.setSize(file.getFileSize());
        info.setLocalPath(file.getFilePath());
        info.setFileType(file.getFileType());
        entity.setBody(ButelFileInfo.toCollectionRecordBody(info));
        entity.setExtinfo("");
        mCollectionDao.insert(entity);
        startUpDateWork();
    }


    public void removeCollection(CollectionBean bean) {
        mCollectionDao.removeCollectionById(bean.getId());
        startUpDateWork();
    }


    public void deleteCollectionById(String collectId) {
        if (collectId.length() == 0) {
            CustomLog.d("CollectManage",
                "deleteCollectionById collectId 为空 collectId:" + collectId);
            return;
        }
        mCollectionDao.deleteRecordById(collectId);
    }


    /**
     * 显示删除和转发
     */
    public boolean showForwordAndDelete(final Activity activity, final CollectionBean bean) {
        MedicalAlertDialog menuDlg = new MedicalAlertDialog(activity);
        if (FileTaskManager.NOTICE_TYPE_TXT_SEND == bean.getType() ||
            FileTaskManager.NOTICE_TYPE_URL == bean.getType() ||
            FileTaskManager.NOTICE_TYPE_VEDIO_SEND == bean.getType() ||
            FileTaskManager.NOTICE_TYPE_PHOTO_SEND == bean.getType() ||
            FileTaskManager.NOTICE_TYPE_FILE == bean.getType()) {
            menuDlg.addButtonFirst(new MenuClickedListener() {
                @Override
                public void onMenuClicked() {
                    // 图片，视频，文件，转发之前需判断数据有效性
                    if (bean.getType() == FileTaskManager.NOTICE_TYPE_PHOTO_SEND
                        || bean.getType() == FileTaskManager.NOTICE_TYPE_VEDIO_SEND
                        || bean.getType() == FileTaskManager.NOTICE_TYPE_FILE) {
                        if (!checkDataValid(bean.getBody())) {
                            return;
                        }
                    }

                    CollectionFileManager.getInstance().onMsgForward(activity, bean);
                }
            }, activity.getString(R.string.chat_forward));
        }

        menuDlg.addButtonSecond(new MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                removeCollection(bean);
            }
        }, activity.getString(R.string.chat_delete));
        menuDlg.show();
        return true;
    }
	/*-----------------------------------------------------------------------------------添加/删除  收藏记录结束-----------------------------------------------------------------------------------------------------------------------------*/



	/*---------------------------------------------------------------------页面跳转 开始-----------------------------------------------------------------------------------------------------------*/


    public void goToCollectionDetails(Context mContext, CollectionBean bean) {
        switch (bean.getType()) {
            case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                CollectionFileManager.getInstance().gotoCollectionTextActivity(mContext, bean);
                break;
            case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                if (checkDataValid(bean.getBody())) {
                    CollectionFileManager.getInstance().gotoVieWPohto(mContext, bean, false);
                }
                break;
            case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                if (checkDataValid(bean.getBody())) {
                    CollectionFileManager.getInstance().gotoVieWPohto(mContext, bean, true);
                }
                break;
            case FileTaskManager.NOTICE_TYPE_AUDIO_SEND:
                break;
            case FileTaskManager.NOTICE_TYPE_FILE:
                if (checkDataValid(bean.getBody())) {
                    CollectionFileManager.getInstance().gotoCollectionFileActivity(mContext, bean);
                }
                break;
            case FileTaskManager.NOTICE_TYPE_URL:
                //TODO
                break;
            default:
                break;
        }
    }


    public boolean checkDataValid(String bodyStr) {
        if (TextUtils.isEmpty(bodyStr)) {
            return true;
        }

        try {
            JSONArray bodyArray = new JSONArray(bodyStr);
            JSONObject dataObj = bodyArray.optJSONObject(0);

            String localPath = dataObj.optString("localUrl");
            String remoteUrl = dataObj.optString("remoteUrl");

            if (isValidFilePath(localPath) || !TextUtils.isEmpty(remoteUrl)) {
                // 本地文件存在，或者服务端文件存在，则任务此数据有效
                return true;
            }
        } catch (Exception e) {
            LogUtil.e("Exception", e);
        }

        // 无效数据提示
        CommonUtil.showToast(R.string.collection_data_invalid, MedicalApplication.getContext());

        return false;
    }


    public void goToEmbedWebViewActivity(Context mContext, String url) {
        Intent intent = new Intent(mContext, EmbedWebViewActivity.class);
        intent.putExtra(EmbedWebViewActivity.KEY_PARAMETER_URL, url);
        intent.putExtra(EmbedWebViewActivity.KEY_PARAMETER_TITLE, ChatListAdapter.ACTIVITY_FLAG);
        mContext.startActivity(intent);
    }


    public void goToMyCollectionActivity(Context mContext) {
        Intent intent = new Intent(mContext, CollectionActivity.class);
        mContext.startActivity(intent);
    }


    public static final String KEY_QUERY_COLLECTIONID = "key_query_collection_activity_nube";
    public static final String KEY_QUERY_BACKFLAG = "key_query_collection_activity_back";
    public static final int KEY_QUERY_COLLECTION_ACTIVITY_REQUESTCODE = 10001;


    public void goToQueryCollectionActivity(Activity activity, boolean canBack) {
        //        Intent intent = new Intent(activity,QueryCollectionActivity.class);
        //        intent.putExtra(KEY_QUERY_BACKFLAG, canBack);
        //        if (canBack){
        //            activity.startActivityForResult(intent, KEY_QUERY_COLLECTION_ACTIVITY_REQUESTCODE);
        //        }else{
        //            activity.startActivity(intent);
        //        }
    }


    public void goToSharedCollectionActivity(Context mContext, String receiver) {
        //        Intent intent = new Intent(mContext,SharedCollectionActivity.class);
        //        intent.putExtra(SharedCollectionActivity.KEY_RECEIVER, receiver);
        //        mContext.startActivity(intent);
    }
	/*---------------------------------------------------------------------页面跳转 功能结束-----------------------------------------------------------------------------------------------------------*/


    /**
     * 触发同步线程
     */
    public void startUpDateWork() {
        CollectionUpdateWorkManager.getInstance().startUpdate();
    }


    /**
     * 停止同步 线程
     */
    public void stopUpDateWork() {
        CollectionUpdateWorkManager.getInstance().stopUpdate();
    }


    private void showToast(Context context, String toast) {
        CustomToast.show(context, toast, 1);
        LogUtil.d(toast);
    }


    /**
     * 是否存在本地文件
     */
    public static boolean isValidFilePath(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        if (file.length() == 0) {
            return false;
        }
        return true;
    }


    public boolean isValidImgFilePath(String filePath) {
        if (isValidFilePath(filePath)) {
            if (filePath.endsWith(".temp")) {
                return false;
            }
            return true;
        }
        return false;
    }

    //	/**
    //	 * @Title: formatDispDateStrForCollection
    //	 * 通话记录： 今天：10:30 （显示具体时间，24小时制，精确的分钟）
    //	 *               今天之前：10-30 12:23 （日期+时间）
    //	 *               去年：13-10-30（年+月+日）(年的优先级高于昨天)
    //	 * @param value
    //	 *            ：时间
    //	 * @param DateUtil中的format
    //	 * @return
    //	 */
    //	public String formatDispDateStrForCollection(Context context,
    //			long value, String format) {
    //		Date date = new Date(value*1000);
    //		String dateStr = DateUtil.formatDate2String(date,DateUtil.FORMAT_YYYY_MM_DD_HH_MM);
    //		Calendar cal = Calendar.getInstance();
    //		cal.setTime(date);
    //		Calendar nowCal = Calendar.getInstance();
    //
    //		if (cal.get(Calendar.YEAR) != nowCal.get(Calendar.YEAR)) {
    //			// 跨年了，此处应显示 年、月、日
    //			return dateStr.substring(0, 10);
    //		}
    //		int dayInterval = DateUtil.realDateIntervalDay(cal.getTime(),nowCal.getTime());
    //		if (dayInterval == 0) {
    //			return dateStr.substring(11, 16);
    //		}
    //
    //		return dateStr.substring(5, 16);
    //	}


    /**
     * @param value ：时间
     * @Title: formatDispDateStrForCollection
     * 通话记录： 今天：10:30 （显示具体时间，24小时制，精确的分钟）
     * 今天之前：10-30
     * 去年：13-10-30
     */
    public String formatDispDateStrForCollection(Context context,
                                                 long value, String format) {
        Date date = new Date(value * 1000);
        String dateStr = DateUtil.formatDate2String(date, DateUtil.FORMAT_YYYY_MM_DD_HH_MM);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Calendar nowCal = Calendar.getInstance();

        if (cal.get(Calendar.YEAR) != nowCal.get(Calendar.YEAR)) {
            // 跨年了，此处应显示 年、月、日
            return dateStr.substring(0, 10);
        }
        int dayInterval = DateUtil.realDateIntervalDay(cal.getTime(), nowCal.getTime());
        if (dayInterval == 0) {
            // 当天，显示时分
            return dateStr.substring(11, 16);
        }

        // 今天之前，显示月日
        return dateStr.substring(5, 10);
    }


    /*-------------------------------------------------------------------播放音频 功能开始-----------------------------------------------------------------------------------------------------------*/
    private String curPlayingAuMsgId = "";// 当前收听的音频的消息ID
    private WeakReference<View> currentPlayVoiceView = null;// 当前收听的音频的View
    private MediaPlayer mMediaPlayer = null;// 音频播放


    public void doClickAudioMsg(CollectionBean bean, Context context, View voiceView) {
        if (bean.getId().equals(curPlayingAuMsgId)) {
            stopCurAuPlaying();
            return;
        }

        if (!TextUtils.isEmpty(curPlayingAuMsgId)) {
            stopCurAuPlaying();
        }

        startAuPlaying(bean, context, voiceView);
    }


    private void startAuPlaying(CollectionBean bean, Context context, View voiceView) {
        ButelFileInfo info = ButelFileInfo.parseJsonStr(bean.getBody(), true);
        // 接收的音频，若本地不存在，则重新下载
        if (TextUtils.isEmpty(info.getLocalPath())) {
            showToast(context, context.getString(R.string.toast_downloading_aud));
            // 还未开始下载或正在下载，开始下载
            DownloadTaskManager.getInstance(context).downloadFile(bean.getId(),
                "", true, null, 0);
            return;
        }

        File audFile = new File(info.getLocalPath());
        if (!audFile.exists()) {
            showToast(context, context.getString(R.string.toast_downloading_aud));
            // 开始下载
            DownloadTaskManager.getInstance(context).downloadFile(bean.getId(),
                "", true, null, 0);
            return;
        }

        curPlayingAuMsgId = bean.getId();
        voiceView.setTag(bean);
        currentPlayVoiceView = new WeakReference<View>(voiceView);
        //展示动画
        showAudioAnimation(bean.getId(), voiceView);
        playAudio(info.getLocalPath(), context);
    }


    private void playAudio(String audioPath, Context context) {
        LogUtil.d("audioPath:" + audioPath);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        try {
            ((AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE))
                .setMode(AudioManager.MODE_NORMAL);
            mMediaPlayer = MediaPlayer.create(context.getApplicationContext(),
                Uri.fromFile(new File(audioPath)));
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    // 播放完成后，停止播放动画
                    stopCurAuPlaying();
                }
            });
            mMediaPlayer.start();
        } catch (Exception e) {
            LogUtil.e("Exception", e);
            stopCurAuPlaying();
            return;
        }
    }


    public void stopCurAuPlaying() {
        // 停止播放
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        // 停止动画
        if (currentPlayVoiceView != null) {
            View playingView = currentPlayVoiceView.get();
            if (playingView != null) {
                playingView.setBackgroundResource(R.drawable.audio_left_icon_3);
            }
        }

        // 初始化数据
        curPlayingAuMsgId = null;
        if (currentPlayVoiceView != null) {
            currentPlayVoiceView.clear();
            currentPlayVoiceView = null;
        }
    }


    public void showAudioAnimation(String collectionId, View voiceView) {
        if (collectionId.equals(curPlayingAuMsgId)) {
            voiceView.setBackgroundResource(R.drawable.audio_left_playing);
            final AnimationDrawable drawable = (AnimationDrawable) voiceView.getBackground();
            voiceView.post(new Runnable() {
                @Override
                public void run() {
                    drawable.start();
                }
            });
        } else {
            voiceView.setBackgroundResource(R.drawable.audio_left_icon_3);
        }
    }

}