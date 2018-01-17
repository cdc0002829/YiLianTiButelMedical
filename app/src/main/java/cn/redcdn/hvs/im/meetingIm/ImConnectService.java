package cn.redcdn.hvs.im.meetingIm;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.agent.AppP2PAgentManager;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.column.NoticesTable;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.interfaces.IMsgReceiveListener;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.imservice.IIMServe;
import cn.redcdn.imservice.IIMServeCB;
import cn.redcdn.imservice.IMMessageBean;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.LogUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static cn.redcdn.hvs.MedicalApplication.context;
import static cn.redcdn.hvs.MedicalApplication.getFileTaskManager;

/**
 * Desc
 * Created by wangkai on 2017/8/24.
 */

public class ImConnectService extends Service {

    private final String TAG = "ImConnectService";
    private NoticesDao noticeDao;
    private GroupDao groupDao;
    private String accountNum = AccountManager.getInstance(this).getAccountInfo().getNube();
    private RemoteCallbackList<IIMServeCB> mRemoteCallbackList;
    public static String relatedGroupId = null;
    private final int SEND_TYPE = 0;    //发送消息
    private final int RECEIVE_TYPE = 1; //接收消息
    private ExecutorService mSingleThreadExecutor = null;

    @Override
    public void onCreate() {
        super.onCreate();
        noticeDao = new NoticesDao(this);
        groupDao = new GroupDao(this);
        mRemoteCallbackList = new RemoteCallbackList();
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();
        //监听消息是否发送成功
        // 正常情况：发送消息正调成功调用 onSuccess 异步成功调用 onFinalResule，失败再通知会议室
        // 异常： 发送消息正调失败调用 onFailed
        AppP2PAgentManager.getInstance().setImMsgResultListener(new AppP2PAgentManager.ImMsgResultInterface() {
            @Override
            public void onSuccess(String uuid) {
                msgChange(uuid,SEND_TYPE);
            }

            @Override
            public void onFailed(String uuid) {
                msgChange(uuid,SEND_TYPE);
            }

            @Override
            public void onFinalResult(boolean isSuccess, String uuid) {
                if(!isSuccess){
                    msgChange(uuid,SEND_TYPE);
                }
            }
        });
        AppP2PAgentManager.getInstance().setImMsgReceiveListener(new IMsgReceiveListener() {
            @Override
            public void onMsgReceive(String msguuid) {
                msgChange(msguuid,RECEIVE_TYPE);
            }
        });
        CustomLog.d(TAG,"ImConnectService onCreate");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        CustomLog.d(TAG,"ImConnectService onBind");
        relatedGroupId = intent.getStringExtra("gid");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        CustomLog.d(TAG,"ImConnectService onDestroy");
        super.onDestroy();
        mRemoteCallbackList = null;
        relatedGroupId = "";
        noticeDao = null;
        groupDao = null;
        mSingleThreadExecutor = null;
        AppP2PAgentManager.getInstance().removeImMsgResultListener();
        AppP2PAgentManager.getInstance().removeImMsgReceiveListener();
        CustomLog.d(TAG,"ImConnectService onDestroy");
    }



    private IIMServe.Stub mBinder = new IIMServe.Stub(){

        @Override
        public void sendTextMsg(String groupId, String msg) throws RemoteException {
            CustomLog.d(TAG,"发送消息 groupId:" + groupId + " msg:" + msg);
            String uuid = noticeDao.createSendFileNotice(accountNum, groupId,null,""
                    , FileTaskManager.NOTICE_TYPE_TXT_SEND, msg, groupId, null);
            getFileTaskManager().addTask(uuid, null);
        }

        @Override
        public void queryHistoryMsg(long beginTime) throws RemoteException {
            CustomLog.d(TAG,"queryHistoryMsg begintime:" + beginTime);
            try {
                Cursor cursor = noticeDao.queryPageNotices(relatedGroupId,beginTime,20);
                List<IMMessageBean> tmpList = parseHistroy(cursor);
                CustomLog.d(TAG,"query success,size is " + tmpList);
                int len = mRemoteCallbackList.beginBroadcast();
                for (int i = 0; i < len; i++) {
                    IIMServeCB callback = mRemoteCallbackList.getBroadcastItem(i);
                    try {
                        callback.onQueryHistoryMsg(tmpList);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                mRemoteCallbackList.finishBroadcast();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void registerCallBack(IIMServeCB callback) throws RemoteException {
            mRemoteCallbackList.register(callback);
        }

        @Override
        public void unRegisterCallBack(IIMServeCB callback) throws RemoteException {
            mRemoteCallbackList.unregister(callback);

        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            try {
                super.onTransact(code, data, reply, flags);
            } catch (RuntimeException e) {
                CustomLog.e(TAG, e.toString());
            }
            return true;
        }
    };

    private List<IMMessageBean> parseHistroy(Cursor cursor){
        List<IMMessageBean> histroyList = new ArrayList<IMMessageBean>();
        for (int i = 0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            NoticesBean noticesBean = NoticesTable.pureChatCursor(cursor, ChatActivity.VALUE_CONVERSATION_TYPE_MULTI);
            if(noticesBean.getType() == FileTaskManager.NOTICE_TYPE_DESCRIPTION){
                continue;
            }
            IMMessageBean bean = noticeBean2ImMsg(noticesBean);
            histroyList.add(bean);
        }
        return histroyList;
    }

    private void msgChange(String uuid,int type){
        if(TextUtils.isEmpty(relatedGroupId)){
            CustomLog.e(TAG,"会议室没有绑定groupid,groupid:" + relatedGroupId);
            return;
        }
        NoticesBean noticesBean = noticeDao.getNoticeById(uuid);
        if(noticesBean == null){
            CustomLog.e(TAG,"未查到该消息记录");
            return;
        }
        if(type == SEND_TYPE && noticesBean.getType() != FileTaskManager.NOTICE_TYPE_TXT_SEND){
            CustomLog.d(TAG,"会议室内发送的消息类型错误,类型：" + noticesBean.getType());
            return;
        }
        GroupMemberBean memberBean = groupDao.
                queryGroupMember(noticesBean.getThreadsId(),noticesBean.getSender());
        if(memberBean != null){
            noticesBean.setHeadUrl(memberBean.getHeadUrl());
            noticesBean.setmNickName(memberBean.getNickName());
        }
        final IMMessageBean imMessageBean = noticeBean2ImMsg(noticesBean);
        CustomLog.d(TAG,"im 消息更新 msgId:" + imMessageBean.getMsgId() + " nubeNumber:"
                + imMessageBean.getNubeNumber() + " nickName:" + imMessageBean.getNickName()
                + " headUrl:" + imMessageBean.getHeadUrl() + " time:" + imMessageBean.getTime()
                +  " msgContent:" + imMessageBean.getMsgContent()
                + " msgStatus:" + noticesBean.getStatus());


        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    int len = mRemoteCallbackList.beginBroadcast();
                    for (int i = 0; i < len; i++) {
                        IIMServeCB callback = mRemoteCallbackList.getBroadcastItem(i);
                        try {
                            callback.onMsgUpdate(imMessageBean);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    mRemoteCallbackList.finishBroadcast();
                }catch (Exception e){
                    CustomLog.e(TAG,e.toString());
                }

            }
        });

    }

    private IMMessageBean noticeBean2ImMsg(NoticesBean noticesBean){

        IMMessageBean imMessageBean = new IMMessageBean();
        imMessageBean.setMsgId(noticesBean.getId());
        imMessageBean.setNubeNumber(noticesBean.getSender());
        //数据库表中  0：准备 1：正在进行 2：发送成功 3：失败
        //imservice   0：成功 1：失败
        //除了失败，其余情况都认为是成功状态
        imMessageBean.setMsgStatus(noticesBean.getStatus() == 3 ? 1 : 0);
        imMessageBean.setTime(noticesBean.getSendTime() > 0 ? noticesBean.getSendTime()
                : noticesBean.getReceivedTime());
        imMessageBean.setNickName(noticesBean.getmNickName());
        imMessageBean.setHeadUrl(noticesBean.getHeadUrl());
        imMessageBean.setMsgContent(getMsgContent(noticesBean.getType(),noticesBean.getBody()));
        return imMessageBean;
    }


    private String getMsgContent(int msgType,String msgBody){
        String msgTxt = "";
        switch (msgType) {
            case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                try {
                    JSONArray bodyArray = new JSONArray(msgBody);
                    JSONObject bodyObj = bodyArray.optJSONObject(0);
                    msgTxt = bodyObj.optString("txt");
                    if (!TextUtils.isEmpty(msgTxt) && msgTxt.length() > 3000) {
                        msgTxt = msgTxt.substring(0, 3000);
                        msgTxt = msgTxt + "...";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case FileTaskManager.NOTICE_TYPE_REMIND_SEND:
                try {
                    JSONArray bodyArray = new JSONArray(msgBody);
                    if (bodyArray != null && bodyArray.length() > 0) {
                        JSONObject bodyObj = bodyArray.optJSONObject(0);
                        msgTxt = bodyObj.optString("text");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case FileTaskManager.NOTICE_TYPE_REMIND_ONE_SEND:
                try {
                    JSONArray bodyArray = new JSONArray(msgBody);
                    if (bodyArray != null && bodyArray.length() > 0) {
                        JSONObject bodyObj = bodyArray.optJSONObject(0);
                        msgTxt = bodyObj.optString("text");
                        //替换@nube号为@具体姓名
                        ArrayList<String> dispNubeList = new ArrayList<String>();
                        dispNubeList = CommonUtil.getDispList(msgTxt);
                        for (int i = 0; i < dispNubeList.size(); i++) {
                            GroupMemberBean gbean = groupDao.queryGroupMember(
                                    relatedGroupId, dispNubeList.get(i));
                            if (gbean != null) {
                                ShowNameUtil.NameElement element = ShowNameUtil.getNameElement(
                                        gbean.getName(), gbean.getNickName(),
                                        gbean.getPhoneNum(), gbean.getNubeNum());
                                String MName = ShowNameUtil.getShowName(element);
                                msgTxt = msgTxt.replace("@" + dispNubeList.get(i)
                                        + IMConstant.SPECIAL_CHAR, "@" + MName
                                        + IMConstant.SPECIAL_CHAR);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                // 视频消息
                msgTxt = getResources().getString(R.string.sent_a_video);
                break;
            case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                // 图片
                msgTxt = getResources().getString(R.string.send_a_picture);
                break;

            case FileTaskManager.NOTICE_TYPE_VCARD_SEND:
                // 名片消息
                try {
                    JSONArray bodyArray = new JSONArray(msgBody);
                    if (bodyArray != null && bodyArray.length() > 0) {
                        JSONObject bodyObj = bodyArray.optJSONObject(0);
                        String cardName = bodyObj.optString("name");
                        msgTxt = getResources().getString(R.string.sent_out) + cardName + getResources().getString(R.string.of_the_business_card);
                    }
                } catch (Exception e) {
                    LogUtil.e("JSONArray Exception", e);
                }
                break;
            case FileTaskManager.NOTICE_TYPE_AUDIO_SEND:
                // 音频消息
                msgTxt = context.getResources().getString(R.string.sent_a_voice);
                break;
            case FileTaskManager.NOTICE_TYPE_DESCRIPTION:
                break;
            case FileTaskManager.NOTICE_TYPE_MEETING_BOOK:
                msgTxt = context.getResources().getString(R.string.sent_a_scheduled_appointment_invitation);
                break;
            case FileTaskManager.NOTICE_TYPE_MEETING_INVITE:
                msgTxt = context.getResources().getString(R.string.invite_you_to_video_consultation);
                break;
            case FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND:
                msgTxt = context.getResources().getString(R.string.sent_a_chat_record);
                break;
            case FileTaskManager.NOTICE_TYPE_ARTICAL_SEND:
                msgTxt = context.getResources().getString(R.string.sent_an_article);
                break;
            default:
                msgTxt = "";
                break;
        }
        return msgTxt;
    }
}
