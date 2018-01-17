package cn.redcdn.hvs.meeting.util;

import android.text.TextUtils;
import android.util.Log;

import com.butel.connectevent.utils.CommonUtil;
import com.butel.connectevent.utils.FileService;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.meeting.constant.CommonConstant;

public class LogUtil {

    /** 日志级别 */
	public static final String LOG_D = "D";
	public static final String LOG_I = "I";
	public static final String LOG_E = "E";

	public static final String LOG_BEGIN = "begin";
	public static final String LOG_END = "end";

	// Log关闭变量
	public static final boolean bOpenLog = true;
	// Log保存到文件开关变量
	public static final boolean bOpenSaveLogToFile = false;

	private static FileService fileService = null;

    public static int d(String msg) {
        String[] classMethod = CommonUtil.getClassMethod(new Exception(),
                "LogUtil", "d");
        return d(classMethod[0], classMethod[1], msg);
    }

    public static int d(String tag, String method, String msg) {
        int ret = -1;
        if (bOpenLog) {
            ret = Log.d(tag, method + "-" + msg);
        }
        saveLogToFile(LOG_D, tag, method, "#", msg);
        return ret;
    }

    public static int i(String tag, String method, String sipId, String status,
            String msg) {
        int ret = -1;
        if (bOpenLog) {
            ret = Log.i(tag, method + "-" + sipId + "-" + status + "-" + msg);
        }
        saveLogToFile(LOG_I, tag, method, sipId, status, msg);
        return ret;
    }

    public static int begin(String msg) {
        String[] classMethod = CommonUtil.getClassMethod(new Exception(),
                "LogUtil", "begin");
        return i(classMethod[0], classMethod[1], "#", LOG_BEGIN, msg);
    }

    public static int end(String msg) {
        String[] classMethod = CommonUtil.getClassMethod(new Exception(),
                "LogUtil", "end");
        return i(classMethod[0], classMethod[1], "#", LOG_END, msg);
    }

    public static int e(String msg, Throwable e) {
        String[] classMethod = CommonUtil.getClassMethod(new Exception(),
                "LogUtil", "e");
        return e(classMethod[0], classMethod[1], msg, e);
    }

	public static int e(String tag, String method, String msg, Throwable e) {
	    int ret = -1;
        if (bOpenLog) {
            ret = Log.e(tag, method + "-" + msg, e);
            MobclickAgent.reportError(MedicalApplication.getContext(), tag
                    + "-" + method + "-" + msg + "-" + e.getLocalizedMessage());
        }
        saveLogToFile(LOG_E, tag, method, "#", msg + ":" + e.getLocalizedMessage());
		return ret;
	}
	
	  /**
     * 控件sdk相关测试日志打印
     * @param msg
     * @return
     */
    public static int testD_JMeetingManager(String msg){
        String[] classMethod = CommonUtil.getClassMethod(new Exception(),
                "LogUtil", "d");
        return d("JMmanager",classMethod[0] + "-" +classMethod[1], msg);
    }
    
    public static int testE_JMeetingManager(String msg,Throwable e){
    	String[] classMethod = CommonUtil.getClassMethod(new Exception(),
                "LogUtil", "d");
        return e("JMmanager",classMethod[0] + "-" +classMethod[1], msg,e);
    }

    public static void saveLogToFile(String logLevel, String tag,
            String method, String sipId, String strMessage) {
        saveLogToFile(logLevel, tag, method, sipId, "", strMessage);
    }

	/**
	 * @author: liyb
	 * @Title: saveLogToFile
	 * @Description: 保存日志到文件
	 * @param strMessage
	 *            待保存的日志
	 * @date: 2012-10-13 下午08:12:28
	 */
    public static void saveLogToFile(String logLevel, String tag,
            String method, String sipId, String status, String strMessage) {
        if (FileService.bOpenSaveLogToFile) {
            if (fileService == null) {
                fileService = new FileService();
            }
            fileService.saveLogToFile(logLevel, tag, method, sipId, status, strMessage);
        }
    }

	public static void saveEventToFile(String context) {	
		    if (fileService == null) {
		        fileService = new FileService();
		    }
		    fileService.saveEventToFile(context);
	}
	
	public static void removeEventToUmeng() {	
	    if (fileService == null) {
	        fileService = new FileService();
	    }
	    String context = fileService.readEventFromFile();
	    if(!TextUtils.isEmpty(context)){
	    	String[] events = context.split(System.getProperty("line.separator"));
	    	String item ="";
	    	if(events!=null&&events.length>0){
	    		for(int i=0;i<events.length;i++){
	    			item = events[i];
	    			if(!TextUtils.isEmpty(item)){
	    				if(item.startsWith(CommonConstant.UMENG_KEY_SIPREG_DURATION)){
	    					String duration = item.substring(CommonConstant.UMENG_KEY_SIPREG_DURATION.length()+1);
	    					HashMap<String,String> m = new HashMap<String,String>();
							m.put("__ct__", duration);
							MobclickAgent.onEvent(MedicalApplication.getContext(),
									CommonConstant.UMENG_KEY_SIPREG_DURATION, m);
	    				}else{
	    					MobclickAgent.onEvent(MedicalApplication.getContext(),
									item);
	    				}
	    			}
	    		}
	    	}
	    }
	    fileService.emptyEventFile();
	}
}
