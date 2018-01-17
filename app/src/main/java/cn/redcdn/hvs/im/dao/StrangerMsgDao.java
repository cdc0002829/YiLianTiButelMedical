package cn.redcdn.hvs.im.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import cn.redcdn.hvs.im.bean.StrangerMessage;
import cn.redcdn.hvs.im.column.StrangerMessageTable;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.LogUtil;
import java.util.ArrayList;
import java.util.List;

public class StrangerMsgDao {
    private String TAG = getClass().getName();
    private Context mContext;
    private int id;
    private String strangerNubeNumber;
    private String strangerHead;
    private String strangerName;
    private int msgDirection;//区别发送、接收  0|发送   1|接收
    private String msgContent;
    private String time;


    public StrangerMsgDao(Context context) {
        this.mContext = context;
    }


    public void insert(StrangerMessage strangerMessage) {
        CustomLog.i(TAG,"insert strangerMessage == "+ strangerMessage.toString());
        insert(entity2ContentValue(strangerMessage));
    }


    /**
     * 插入数据库
     */
    private Uri insert(ContentValues values) {
        if (values == null) {
            return null;
        }
        Uri uri = null;
        try {
            uri = mContext.getContentResolver().insert(StrangerMessageTable.URI, values);
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return uri;
    }


    public Cursor getMsgByNubeNumber(String nubeNumber) {
        CustomLog.i(TAG,"getMsgByNubeNumber");
        if (TextUtils.isEmpty(nubeNumber)||nubeNumber.isEmpty()) {
            CustomLog.i(TAG,"nubeNumber"+nubeNumber);
            return null;
        }else{
            CustomLog.i(TAG,"nubeNumber == " +nubeNumber );
        }
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                StrangerMessageTable.URI,
                null,
                StrangerMessageTable.STRANGER_NUBE_NUMBER + " = ? ",
                new String[] { nubeNumber },
                StrangerMessageTable.TIME + " DESC");
        } catch (Exception e) {
            CustomLog.i(TAG, "Exception" + e.toString());
        }
        return cursor;
    }


    /**
     * 获取所有陌生人消息
     */
    public Cursor getAllStrangerMsg() {
        CustomLog.i(TAG,"getAllStrangerMsg");
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                StrangerMessageTable.URI,
                null,
                null,
                null,
                StrangerMessageTable.TIME + " DESC");
        } catch (Exception e) {
            CustomLog.i(TAG, "Exception" + e.toString());
        }
        return cursor;
    }


    public int getNotReadMesSize() {
        CustomLog.i(TAG,"getNotReadMesSize");
        Cursor cursor = null;
        try {
            Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
                "STRANGER_MESSAGE_TABLE_QUERY_NOT_READ_MSG");
            cursor = mContext.getContentResolver().query(
                uri,
                null,
                null, null,
                // StrangerMessageTable.IS_Read + " = ? ",
                // new String[] { "0" },
                null);
        } catch (Exception e) {
            CustomLog.i(TAG, "Exception" + e.toString());
        }
        List<StrangerMessage> list = cursor2Entity(cursor);
       if(list!=null){
          return list.size();
       }else{
           return 0;
       }
}


    public List<StrangerMessage> getNotReadMes() {
        CustomLog.i(TAG,"getNotReadMes");
        Cursor cursor = null;
        try {
            Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
                "STRANGER_MESSAGE_TABLE_QUERY_NOT_READ_MSG");
            cursor = mContext.getContentResolver().query(
                uri,
                null,
                null,
                null,
                null);
        } catch (Exception e) {
            CustomLog.i(TAG, "Exception" + e.toString());
        }
        return cursor2Entity(cursor);
    }


    /**
     * 标记所有消息为已读
     */
    public void updateAllMsgIsRead() {
        CustomLog.i(TAG,"updateAllMsgIsRead");
        CustomLog.d("StrangerMsgDao", "updateAllMsgRead");
        ContentValues value = new ContentValues();
        value.put(StrangerMessageTable.IS_Read, StrangerMessageTable.HAS_READ);
        try {
            mContext.getContentResolver().update(
                StrangerMessageTable.URI,
                value,
                null,
                null);
        } catch (Exception e) {
            CustomLog.e("updateAllMsgRead Exception", e.toString());
        }
    }


    private ContentValues entity2ContentValue(StrangerMessage strangerMessage) {
        if (strangerMessage == null) {
            return null;
        }
        ContentValues value = new ContentValues();
        value.put(StrangerMessageTable.STRANGER_NUBE_NUMBER, strangerMessage.getStrangerNubeNumber());
        value.put(StrangerMessageTable.STRANGER_HEAD, strangerMessage.getStrangerHead());
        value.put(StrangerMessageTable.STRANGER_NAME, strangerMessage.getStrangerName());
        value.put(StrangerMessageTable.MSG_DIRECTION, strangerMessage.getMsgDirection());
        value.put(StrangerMessageTable.MSG_CONTENT, strangerMessage.getMsgContent());
        value.put(StrangerMessageTable.IS_Read, strangerMessage.getIsRead());
        value.put(StrangerMessageTable.TIME, strangerMessage.getTime());
        return value;
    }


    private List<StrangerMessage> cursor2Entity(Cursor cursor) {
        if (cursor == null || cursor.isClosed() || cursor.getCount() == 0) {//  || 如果遇到一个true，则停止其它条件的判断，返回true
            return null;
        }
        List<StrangerMessage> list = new ArrayList();
        StrangerMessage strangerMessage = new StrangerMessage();
        try {
            while (cursor.moveToNext()) {
                strangerMessage.setStrangerNubeNumber(
                    cursor.getString(cursor.getColumnIndexOrThrow(StrangerMessageTable.STRANGER_NUBE_NUMBER)));
                strangerMessage.setStrangerHead(cursor.getString(cursor.getColumnIndexOrThrow(StrangerMessageTable.STRANGER_HEAD)));
                strangerMessage.setStrangerName(cursor.getString(cursor.getColumnIndexOrThrow(StrangerMessageTable.STRANGER_NAME)));
                strangerMessage.setMsgDirection(cursor.getInt(cursor.getColumnIndexOrThrow(StrangerMessageTable.MSG_DIRECTION)));
                strangerMessage.setMsgContent(cursor.getString(cursor.getColumnIndexOrThrow(StrangerMessageTable.MSG_CONTENT)));
                strangerMessage.setTime(cursor.getString(cursor.getColumnIndexOrThrow(StrangerMessageTable.TIME)));
                strangerMessage.setIsRead(cursor.getInt(cursor.getColumnIndexOrThrow(StrangerMessageTable.IS_Read)));
                list.add(strangerMessage);
            }
        } catch (Exception e) {
            CustomLog.e("cursor2Entity Exception", e.toString());
            return null;
        }
        return list;
    }


    /**
     * 删除全部消息记录
     */
    public int deleteMsg() {//返回值?
        CustomLog.i("StrangerMsgDao", "deleteMsg");
        int count = 0;
        try {
            count = mContext.getContentResolver().delete(
                StrangerMessageTable.URI,
                null,
                null);
        } catch (Exception e) {
            LogUtil.e("deleteNewFriendByNubeNumber Exception", e);
        }
        return count;
    }


    public void setMesRead(String nubeNumber) {
        CustomLog.i(TAG, "setMesRead nubeNumber=="+nubeNumber);
        ContentValues value = new ContentValues();
        value.put(StrangerMessageTable.IS_Read, StrangerMessageTable.HAS_READ);
        try {
            mContext.getContentResolver().update(
                StrangerMessageTable.URI,
                value,
                StrangerMessageTable.STRANGER_NUBE_NUMBER + " =?  ",
                new String[] { nubeNumber });
        } catch (Exception e) {
            CustomLog.e("setMesRead Exception", e.toString());
        }}
}