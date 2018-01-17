package cn.redcdn.hvs.im.work;

import android.text.TextUtils;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.dao.DtNoticesDao;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.work.MessageReceiveAsyncTask.PrivateMessage;
import cn.redcdn.log.CustomLog;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

public class MessageBaseParse {

    private final String TAG = "MessageBaseParse";
    protected NoticesDao noticesDao = null;
    protected DtNoticesDao dtNoticesDao = null;

    public MessageBaseParse(){
        this.noticesDao = new NoticesDao(MedicalApplication.getContext());
        this.dtNoticesDao = new DtNoticesDao(MedicalApplication.getContext());
    }

    public String getStringById(int id){
        if(id>0){
            return MedicalApplication.getContext().getString(id);
        }else{
            return "";
        }
    }

    public boolean insertIncompatibleTxtTip(PrivateMessage pMsg,String serverId){
        //因存在重复下载的场景，故在存储此提示信息时将msgid+'N'插入数据库中id字段
        //在每次插入前需要判断下是否已经插入过。
        if(pMsg==null){
            CustomLog.d(TAG,"insertIncompatibleTxtTip msg==null");
            return false;
        }
        String id = pMsg.msgId;//.replace("-", "")+"N";
        NoticesBean repeatData = noticesDao.getNoticeById(id);
        if (repeatData != null) {
            CustomLog.d(TAG,"该消息已经存在，不再往NoticesTable表中加入文本提示，重复消息下载消息ID="+id);
            return true;
        }else{
            String tiptxt = getStringById(R.string.messsge_incompatible_hint_txt);
            String body = noticesDao.createRecTxtMsgBody(tiptxt);
            String uuid = noticesDao.createReceiveMsgNotice(id,
                    pMsg.sender,
                    pMsg.receivers,
                    body,
                    FileTaskManager.NOTICE_TYPE_TXT_SEND,
                    getStringById(R.string.messsge_title_update),
                    pMsg.extendedInfo,
                    pMsg.time,
                    pMsg.gid,serverId);
            CustomLog.d(TAG,"消息版本不兼容，插入本地txt uuid="+uuid);

            if(!TextUtils.isEmpty(uuid)){
                return true;
            }

            return false;
        }
    }

    /**
     * 向本地插入一条文本信息
     * @param pMsg  从服务端收到的消息
     * @param txt   文本消息的正文
     * @param title 标题（暂没有具体意义）
     * @return true:插入成功；false:失败
     */
    public boolean insertTxtMessage(PrivateMessage pMsg,String txt, String title){

        CustomLog.d(TAG,"insertTxtMessage pMsg:" + pMsg
                +"|txt"+txt
                +"|title"+title);

        if(pMsg==null){
            return false;
        }
//        String id = pMsg.msgId.replace("-", "");
        String body = noticesDao.createRecTxtMsgBody(txt);
        String uuid = noticesDao.createReceiveMsgNotice(pMsg.msgId,
                pMsg.sender,
                pMsg.receivers,
                body,
                FileTaskManager.NOTICE_TYPE_TXT_SEND,
                title,
                pMsg.extendedInfo,
                pMsg.time,
                pMsg.gid,"");
        CustomLog.d(TAG,"消息版本不兼容，插入本地txt uuid="+uuid);

        if(!TextUtils.isEmpty(uuid)){
            return true;
        }

        return false;
    }


    public List<String> getFileUrl(String body){
        if (!TextUtils.isEmpty(body)) {
            try{
                List<String> romteList = new ArrayList<String>();
                Object jsonType = (new JSONTokener(body)).nextValue();
                if(jsonType instanceof JSONArray){
                    JSONArray array = (JSONArray) jsonType;
                    int length = array.length();
                    for (int i = 0; i < length; i++) {
                        romteList.add(array.getString(i));
                    }
                }else{
                    romteList.add(body);
                }
                return romteList;
            }catch(JSONException e){
                CustomLog.e(TAG,"JSONException" + e.toString());
            }
        }
        return null;
    }

    public static ExtInfo convertExtInfo(String extInfo){
        CustomLog.d("MessageBaseParse","extInfo:" + extInfo);
        ExtInfo info = new ExtInfo();
        if(!TextUtils.isEmpty(extInfo)){
            try {
                Object jsonType = (new JSONTokener(extInfo)).nextValue();
                if (jsonType instanceof JSONObject) {
                    JSONObject obj = ((JSONObject) jsonType);
                    info.text = obj.optString("text");
                    info.id = obj.optString("id");
                    info.meetingInfo = obj.optString("meetingInfo");
                    info.subtype = obj.optString("subtype");
                    info.serverId = obj.optString("severid");
                    if(obj.has("thumbUrls")){
                        info.thumb = obj.optString("thumbUrls");
                    }else{
                        info.thumb = "";
                    }
                    info.ver = obj.optString("ver");
                    info.fileInfo = obj.optString("fileInfo");
                    info.chatRecordInfo = obj.optString("chatrecordInfo");
                    info.articleInfo = obj.optString("articleInfo");
                    info.content = obj.optString("content");

                } else if (jsonType instanceof JSONArray) {
                    info.thumb = extInfo;
                }
            } catch (JSONException e) {
                CustomLog.e("MessageBaseParse","JSONException" + e.toString());
            }
        }
        return info;
    }

    public static class ExtInfo{
        public String text ="";
        public String id ="";
        public String thumb ="";
        public String ver ="";
        public String subtype ="";
        public String meetingInfo="";
        public String fileInfo="";
        public String serverId = "";
        public String chatRecordInfo = "";
        public String articleInfo = "";
        public String content = "";
    }
}
