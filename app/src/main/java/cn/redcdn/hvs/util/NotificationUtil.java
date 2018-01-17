package cn.redcdn.hvs.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.work.MessageReceiveAsyncTask;
import cn.redcdn.log.CustomLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import static android.R.attr.id;


/**
 * Desc
 * Created by wangkai on 2017/2/23.
 */

public class NotificationUtil {

    private static final String TAG = "NotificationUtil";

    private static Context context = MedicalApplication.getContext();
    public static NotificationManager mNotificationManager = (NotificationManager) context
            .getSystemService(Context.NOTIFICATION_SERVICE);
    private static final String NOTIFACATION_STYLE_NOANSWER = "0";// 未接来电
    public static final String NOTIFACATION_STYLE_MSG = "1";// 消息
    public static final String NOTIFACATION_STYLE_FRI = "2";// 新的朋友
    public static final String NOTIFACATION_STYLE_ALARM = "3";// 报警消息
    public static final String NOTIFACATION_STYLE_UNRELATED = "4";// 解除关联
    public static final String NOTIFACATION_STYLE_CALL_ON = "10";// 正在通话中的通知栏ID
    public static final int NOTIFACATION_GROUP_MSG_BASEID = 4000;// 群组消息的通知栏ID基数

    public static PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1,
                new Intent(), flags);
        return pendingIntent;
    }

    // 清除应用所有通知栏
    public static void cancelAllNotifacation() {
        CustomLog.d(TAG,"cancelAllNotifacation begin");
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        CustomLog.d(TAG,"cancelAllNotifacation end");
    }

    public static void cancelNotifacationForMsg() {
        cleanNotifacationIdCache(NOTIFACATION_STYLE_MSG);
    }

    public static void cancelNotifacationForFriend() {
        cleanNotifacationIdCache(NOTIFACATION_STYLE_FRI);
    }

    private static void cleanNotifacationIdCache(String msgstyle) {
        CustomLog.d(TAG,"cleanNotifacationIdCache begin , msgstyle=" + msgstyle);
        String ids = "";
//        if (NOTIFACATION_STYLE_NOANSWER.equals(msgstyle)) {
//            ids = NetPhoneApplication.getPreference().getKeyValue(
//                    PrefType.KEY_NOTIFICATION_IDS, "");
//        }
//        if (NOTIFACATION_STYLE_MSG.equals(msgstyle)) {
//            ids = NetPhoneApplication.getPreference().getKeyValue(
//                    PrefType.KEY_NOTIFICATION_MSG_IDS, "");
//        }
//        if (NOTIFACATION_STYLE_FRI.equals(msgstyle)) {
//            ids = NetPhoneApplication.getPreference().getKeyValue(
//                    PrefType.KEY_NOTIFICATION_FRI_IDS, "");
//        }

        if (!TextUtils.isEmpty(ids) && mNotificationManager != null) {
            Log.d(TAG, "通知栏消息ids:" + ids);
            String[] idArray = ids.split(",");
            if (idArray != null && idArray.length > 0) {
                for (String id : idArray) {
                    Log.d(TAG, "遍历通知栏消息id:" + id);
                    try {
                        mNotificationManager.cancel(Integer.parseInt(id));
                    } catch (Exception e) {
                        CustomLog.e(TAG,"清除通知栏消息异常.消息ID：" + id + e.toString());
                        continue;
                    }
                }
            }
        }

//        if (NOTIFACATION_STYLE_NOANSWER.equals(msgstyle)) {
//            NetPhoneApplication.getPreference().setKeyValue(
//                    PrefType.KEY_NOTIFICATION_IDS, "");
//        }
//        if (NOTIFACATION_STYLE_MSG.equals(msgstyle)) {
//            NetPhoneApplication.getPreference().setKeyValue(
//                    PrefType.KEY_NOTIFICATION_MSG_IDS, "");
//        }
//        if (NOTIFACATION_STYLE_FRI.equals(msgstyle)) {
//            NetPhoneApplication.getPreference().setKeyValue(
//                    PrefType.KEY_NOTIFICATION_FRI_IDS, "");
//        }
        CustomLog.d(TAG,"cleanNotifacationIdCache end");
    }

    /**
     * 更新缓存中的id
     *
     * @param msgstyle
     *            通知栏消息的类型
     * @param id
     *            id
     * @param decrease
     *            true:删除id;false增加id
     */
    private static void updateNotifacationIdCache(String msgstyle, int id,
                                                  boolean decrease) {
        CustomLog.d(TAG,"updateNotifacationIdCache begin");
        String ids = "";
//        if (NOTIFACATION_STYLE_NOANSWER.equals(msgstyle)) {
//            ids = NetPhoneApplication.getPreference().getKeyValue(
//                    PrefType.KEY_NOTIFICATION_IDS, "");
//        }
//        if (NOTIFACATION_STYLE_MSG.equals(msgstyle)) {
//            ids = NetPhoneApplication.getPreference().getKeyValue(
//                    PrefType.KEY_NOTIFICATION_MSG_IDS, "");
//        }
//        if (NOTIFACATION_STYLE_FRI.equals(msgstyle)) {
//            ids = NetPhoneApplication.getPreference().getKeyValue(
//                    PrefType.KEY_NOTIFICATION_FRI_IDS, "");
//        }

        if (decrease) {
            if (TextUtils.isEmpty(ids)) {

            } else {
                ids.replace(id + "", "");
                ids.replace(",,", ",");
            }
        } else {
            if (TextUtils.isEmpty(ids)) {
                ids = "" + id;
            } else {
                if (!ids.contains("" + id)) {
                    ids = ids + "," + id;
                }
            }
        }

//        if (NOTIFACATION_STYLE_NOANSWER.equals(msgstyle)) {
//            NetPhoneApplication.getPreference().setKeyValue(
//                    PrefType.KEY_NOTIFICATION_IDS, ids);
//        }
//        if (NOTIFACATION_STYLE_MSG.equals(msgstyle)) {
//            NetPhoneApplication.getPreference().setKeyValue(
//                    PrefType.KEY_NOTIFICATION_MSG_IDS, ids);
//        }
//        if (NOTIFACATION_STYLE_FRI.equals(msgstyle)) {
//            NetPhoneApplication.getPreference().setKeyValue(
//                    PrefType.KEY_NOTIFICATION_FRI_IDS, ids);
//        }
        CustomLog.d(TAG,"updateNotifacationIdCache end");
    }

    /**
     * 根据视频号取消状态栏上的通知提醒； 在进入某个个人会话时，调用该方法
     *
     * @param nubeNumber
     */
    public static void cancelNewMsgNotifacation(String nubeNumber) {
        CustomLog.d(TAG,"cancelNewMsgNotifacation begin");
        if (mNotificationManager != null && !TextUtils.isEmpty(nubeNumber)) {

            int id = 1;
            try {
                //取消报警消息的状态栏通知-begain-2015-12-24
                if (nubeNumber.equals("90000000")) {
                    id = Integer
                            .parseInt(nubeNumber + NOTIFACATION_STYLE_ALARM);
                } else {
                    id = Integer.parseInt(nubeNumber + NOTIFACATION_STYLE_MSG);
                }
                // -取消报警消息的状态栏通知--end
//				id = Integer.parseInt(nubeNumber + NOTIFACATION_STYLE_MSG);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            mNotificationManager.cancel(id);

            updateNotifacationIdCache(NOTIFACATION_STYLE_MSG, id, true);
        }
        CustomLog.d(TAG,"cancelNewMsgNotifacation end");
    }

    /**
     *
     * Description:调用系统通知栏，新朋友和新消息的通知栏提醒带有小图标
     *
     * @author liujc
     * @time 2014-12-2 下午08:49
     * @param titleString
     *            通知栏第一次显示提示文字
     * @param name
     *            姓名
     * @param number
     *            纳贝号
     * @param contextSting
     *            显示内容
     * @param intent
     * @param msgStyle
     * @param needSound ture:需要声音，fasle不需要声音
     */
    public static void sendNotifacationForSmallIcoMSG(String titleString,
                                                      String name, String number, String contextSting, Intent intent,
                                                      String msgStyle,boolean needSound) {

//	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//	        sendNotifacationForMSG(titleString, name, number,
//	                contextSting, intent, msgStyle, needSound);
//	        return;
//	    }

        CustomLog.d(TAG,"sendNotifacationForSmallIcoMSG begin");

        NotificationCompat.Builder mBuilder = new Builder(context);
        int id = 1;
        try {
            id = Integer.parseInt(number + msgStyle);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        PendingIntent pIntent = null;
        if (intent != null) {
            pIntent = PendingIntent.getActivity(context, id, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
        }
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher);
        mBuilder.setContentText(contextSting)
                .setContentIntent(
                        getDefalutIntent(Notification.FLAG_ONGOING_EVENT))
                .setWhen(System.currentTimeMillis()).setTicker(titleString)
                .setPriority(Notification.PRIORITY_DEFAULT).setOngoing(false)
                .setAutoCancel(true).setLargeIcon(bm).setContentTitle(name)
                .setContentIntent(pIntent)
                .setSmallIcon(R.mipmap.ic_launcher);
        Notification notify = mBuilder.build();
        notify.flags = Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_SHOW_LIGHTS;
        if (needSound){
            notify.defaults = Notification.DEFAULT_SOUND;
        }
        notify.ledARGB = 0xff00ff00;
        notify.ledOnMS = 1;
        notify.ledOffMS = 1;

        if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")){
            try {
                Field field = notify.getClass().getDeclaredField("extraNotification");
                Object extraNotification = field.get(notify);
                Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
                method.invoke(extraNotification, MessageReceiveAsyncTask.newMsgCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mNotificationManager.notify(id, notify);
        CustomLog.d(TAG,"sendNotifacationForSmallIcoMSG end");
    }

    /**
     *
     * Description:自定义新朋友和新消息的通知栏提醒
     *
     * @author liujc
     * @time 2014-11-25 下午05:02
     * @param titleString
     *            通知栏第一次显示提示文字
     * @param name
     *            姓名
     * @param number
     *            纳贝号
     * @param contextSting
     *            显示内容
     * @param intent
     * @param msgStyle
     *            消息类型 ：1表示新消息的通知 2表示新朋友的通知
     */
    public static void sendNotifacationForMSG(String titleString, String name,
                                              String number, String contextSting, Intent intent, String msgStyle, boolean needSound) {
        CustomLog.d(TAG,"sendNotifacationForMSG begin");
        NotificationCompat.Builder mBuilder = new Builder(context);
        RemoteViews mRemoteViews = new RemoteViews(context.getPackageName(),
                R.layout.notice_for_send_msg);
        mRemoteViews.setImageViewResource(R.id.butel_notification_icon,
                R.mipmap.ic_launcher);
        mRemoteViews
                .setTextViewText(R.id.butel_notification_name, contextSting);

        if (TextUtils.isEmpty(name.trim())) {
            name = number;
        }
        mRemoteViews.setTextViewText(R.id.butel_notification_title, name);
        mRemoteViews.setViewVisibility(R.id.butel_notification_time_msg,
                View.VISIBLE);
        SimpleDateFormat sdformat = new SimpleDateFormat("MM-dd HH:mm");
        String nowTime = sdformat.format(new Date(System.currentTimeMillis()));
        mRemoteViews.setTextViewText(R.id.butel_notification_time_msg, nowTime);
        int id = 1;
        try {
            id = Integer.parseInt(number + msgStyle);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        PendingIntent pIntent = null;
        if (intent != null) {
            pIntent = PendingIntent.getActivity(context, id, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
        }
        mRemoteViews.setOnClickPendingIntent(R.id.ll_custom_button, pIntent);
        mBuilder.setContent(mRemoteViews)
                .setContentIntent(
                        getDefalutIntent(Notification.FLAG_ONGOING_EVENT))
                .setWhen(System.currentTimeMillis()).setTicker(titleString)
                .setPriority(Notification.PRIORITY_DEFAULT).setOngoing(false)
                .setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher);
        Notification notify = mBuilder.build();
        notify.flags = Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_SHOW_LIGHTS;
        if (needSound){
            notify.defaults = Notification.DEFAULT_SOUND;
        }
        notify.ledARGB = 0xff00ff00;
        notify.ledOnMS = 1;
        notify.ledOffMS = 1;
        mNotificationManager.notify(id, notify);
        if (NOTIFACATION_STYLE_MSG.equals(msgStyle)) {
            updateNotifacationIdCache(NOTIFACATION_STYLE_MSG, id, false);
        } else if (NOTIFACATION_STYLE_FRI.equals(msgStyle)) {
            updateNotifacationIdCache(NOTIFACATION_STYLE_FRI, id, false);
        }
        CustomLog.d(TAG,"sendNotifacationForMSG end");
    }

    // 根据id清除通知栏
    public static void cancelNotifacationById(String notifyid) {
        CustomLog.d(TAG,"cancelNotifacationById begin");
        if (TextUtils.isEmpty(notifyid)) {
            return;
        }

        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        String str = notifyid;
        Pattern pattern = Pattern.compile("^[0-9]*$"); // 通过拨号盘输入非法字符，给通知栏固定一个id用于删除
        if (!pattern.matcher(str).matches()) {
            mNotificationManager.cancel(R.string.app_name);
        } else {
            int id = -1;
            try {
                id = Integer.parseInt(notifyid);
            } catch (NumberFormatException e) {
                id = -1;
            }
            if (id == -1) {
                return;
            }
            mNotificationManager.cancel(id);
        }
        // 清除客服号的通知栏消息
//        String phone = NetPhoneApplication.getPreference().getKeyValue(
//                PrefType.CUSTOM_SERVICE_PHONE, "");
//        if (!TextUtils.isEmpty(phone)) {
//            String phones[] = phone.split("#");
//            if (phones != null && phones.length > 0) {
//                for (String phoneItem : phones) {
//                    mNotificationManager.cancel(Integer.parseInt(phoneItem));
//                }
//            }
//        }
        CustomLog.d(TAG,"cancelNotifacationById end");
    }

    // 存储空间不足通知栏栏提示
    public static int NOTIFY_NOSPACE_ID = 1005;

    public static void sendNoSpaceNotifacation() {
        CustomLog.d(TAG,"sendNoSpaceNotifacation begin");
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0,
                new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notify = new Notification(R.mipmap.ic_launcher,
                CommonUtil.getString(R.string.notification_no_space),
                System.currentTimeMillis());
        notify.flags = Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_SHOW_LIGHTS;
        notify.ledARGB = 0xff00ff00;
        notify.ledOnMS = 1;
        notify.ledOffMS = 1;
//        notify.setLatestEventInfo(context,
//                CommonUtil.getString(R.string.notification_no_space),
//                CommonUtil.getString(R.string.notification_no_space_content),
//                pIntent);
        mNotificationManager.notify(NOTIFY_NOSPACE_ID, notify);
        CustomLog.d(TAG,"sendNoSpaceNotifacation end");
    }

    // 软件升级下载失败通知栏提示
    private static int NOTIFY_ID = 1001;

    public static void cancelNotifacation() {
        CustomLog.d(TAG,"cancelNotifacation begin");
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFY_ID);
        CustomLog.d(TAG,"cancelNotifacation end");
    }

    public static int NOTIFY_AUDIOCALL_ID = 1002;

    public static int getGroupNotifyID(String gid) {
        GroupDao gDao = new GroupDao(context);
        // int id = gDao.getGroupColumnId(gid);
        return id + NotificationUtil.NOTIFACATION_GROUP_MSG_BASEID;
    }
}
