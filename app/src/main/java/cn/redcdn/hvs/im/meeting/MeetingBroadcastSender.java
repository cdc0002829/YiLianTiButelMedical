package cn.redcdn.hvs.im.meeting;

import android.content.Context;
import android.content.Intent;
import cn.redcdn.hvs.meeting.bean.MeetingItemInfo;
import java.util.ArrayList;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

public class MeetingBroadcastSender {

    private final static String FORMAT = "%s%s";
    private static final String Broadcast_OnInit_Suffix = ".channelsoft.oninit";
    private static final String Broadcast_OnNowMeeting_Suffix = ".channelsoft.onnowmeeting";
    private static final String Broadcast_OnCreateMeeting_Suffix = ".channelsoft.oncreatemeeting";
    private static final String Broadcast_OnJoinMeeting_Suffix = ".channelsoft.onjoinmeeting";
    private static final String Broadcast_MeetingException_Suffix =".channelsoft.meetingexception";


    public static String getOnInitActionName(Context context){
        return  String.format(FORMAT,  context.getPackageName(),Broadcast_OnInit_Suffix);
    }

    public static String getOnNowMeetingActionName(Context context){
        return  String.format(FORMAT,  context.getPackageName(),Broadcast_OnNowMeeting_Suffix);
    }

    public static String getOnCreateMeetingActionName(Context context){
        return String.format(FORMAT,  context.getPackageName(),Broadcast_OnCreateMeeting_Suffix);
    }

    public static String getOnJoinMeetingActionName(Context context){
        return String.format(FORMAT,  context.getPackageName(),Broadcast_OnJoinMeeting_Suffix);
    }

    public static String getMeetingExceptionActionName(Context context){
        return String.format(FORMAT,  context.getPackageName(),Broadcast_MeetingException_Suffix);
    }

    public static void sendOnInit(Context context,int code){
        Intent i = new Intent(getOnInitActionName(context));
        i.putExtra("code", code);
        context.sendBroadcast(i);
    }

//    public static void sendOnNowMeeting(Context context,int code,List<MeetingItemInfo> list){
//        Intent i = new Intent(getOnNowMeetingActionName(context));
//        i.putExtra("code", code);
//        i.putExtra("data", (Serializable)list);
//        context.sendBroadcast(i);
//    }
//
   public static void sendOnCreateMeeting(Context context, int code, String contextid, MeetingItemInfo info, ArrayList<String> userlist) {
       // LogUtil.testD_JMeetingManager("start");
       if (null != userlist) {
           // LogUtil.testD_JMeetingManager(
           //     String.format("code=%s,contextid = %s,userlist.size = %s,meetinfo = %s", code,
           //         contextid, userlist.size(), info.mMeetingNumber));
           Intent i = new Intent(getOnCreateMeetingActionName(context));
           i.putExtra("code", code);
           i.putExtra("contextid", contextid);
           i.putStringArrayListExtra("userlist", userlist);
           i.putExtra("meetinfo", info);
           context.sendBroadcast(i);
       } else {
           sendErrorBroadcast(context, "onCreateMeeting  error: userlist null");
       }
       // LogUtil.testD_JMeetingManager("end");
   }
//
//    public static void sendOnjoinMeeting(Context context,int code,String meetid){
//        Intent i = new Intent(getOnJoinMeetingActionName(context));
//        i.putExtra("code", code);
//        i.putExtra("meetid",meetid);
//        context.sendBroadcast(i);
//    }
//
   public static void sendErrorBroadcast(Context context ,String msg){
       // LogUtil.testD_JMeetingManager(msg);
   }
}
