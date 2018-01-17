package cn.redcdn.hvs.im.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.contacts.contact.hpucontact.DtNoticesTable;
import cn.redcdn.hvs.im.bean.BookMeetingExInfo;
import cn.redcdn.hvs.im.bean.ButelFileInfo;
import cn.redcdn.hvs.im.bean.ButelMeetingExInfo;
import cn.redcdn.hvs.im.bean.ButelPAVExInfo;
import cn.redcdn.hvs.im.bean.ButelVcardBean;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.column.NoticesTable;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.im.work.BizConstant;
import cn.redcdn.hvs.util.StringUtil;
import cn.redcdn.log.CustomLog;

/**
 * Created by caizx on 2017/11/24.
 */

public class DtNoticesDao {
    private static final String TAG = "DtNoticesDao";

    private static String[] select_columns = { NoticesTable.NOTICE_COLUMN_ID,
        NoticesTable.NOTICE_COLUMN_TYPE, NoticesTable.NOTICE_COLUMN_ISNEW,
        NoticesTable.NOTICE_COLUMN_ISREAD, NoticesTable.NOTICE_COLUMN_BODY,
        NoticesTable.NOTICE_COLUMN_SENDTIME,
        NoticesTable.NOTICE_COLUMN_SENDER,
        NoticesTable.NOTICE_COLUMN_RECIEVER,
        NoticesTable.NOTICE_COLUMN_STATUS,
        NoticesTable.NOTICE_COLUMN_RECEIVEDTIME,
        NoticesTable.NOTICE_COLUMN_TITLE, NoticesTable.NOTICE_COLUMN_MSGID,
        NoticesTable.NOTICE_COLUMN_EXTINFO,
        NoticesTable.NOTICE_COLUMN_FAIL_REPLYID,
        NoticesTable.NOTICE_COLUMN_THREADSID };

    private static String[] select_unread_columns = { NoticesTable.NOTICE_COLUMN_ID,
        NoticesTable.NOTICE_COLUMN_SEVERID };

    private Context mcontext = null;


    public DtNoticesDao(Context context) {
        this.mcontext = context;
    }


    public static final int NO_DISTRUB_FLAG = -1;


    /**
     * @param convstId 会话ID
     * @return cursor对象，可能会因异常而返回null
     * @throws Exception
     * @author: zhaguitao
     * @Title: queryAllNotices
     * @Description: 查询某会话下全部消息 ， 请注意在使用结束后主动的close获得的cursor
     */
    public Cursor queryConvstNoticesCursor(String convstId) throws Exception {
        if (TextUtils.isEmpty(convstId)) {
            return null;
        }
        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
            "QUERY_ALL_DT_NOTICES" + "/" + convstId);
        try {
            return mcontext.getContentResolver().query(uri, null, null, null, null);
            //            return mcontext.getContentResolver().query(
            //                    ProviderConstant.NETPHONE_HPU_NOTICE_URI, select_columns,
            //                    NoticesTable.NOTICE_COLUMN_THREADSID + " = ? ",
            //                    new String[] { convstId },
            //                    NoticesTable.NOTICE_COLUMN_RECEIVEDTIME + " ASC");
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
            throw e;
        }
    }


    /**
     * @param convstId 会话ID
     * @param recvTimeBegin 起始接收时间
     * @return cursor对象，可能会因异常而返回null
     * @throws Exception
     * @author: zhaguitao
     * @Title: queryCondNotices
     * @Description: 查询某会话下起始接收时间开始的全部消息
     */
    public Cursor queryNotices(String convstId, long recvTimeBegin) throws Exception {
        if (TextUtils.isEmpty(convstId)) {
            return null;
        }
        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
            "QUERY_COND_DT_NOTICES" + "/" + convstId + "/" + recvTimeBegin);
        try {
            return mcontext.getContentResolver().query(uri, null, null, null, null);
            //            return mcontext.getContentResolver().query(
            //                    ProviderConstant.NETPHONE_HPU_NOTICE_URI, select_columns,
            //                    NoticesTable.NOTICE_COLUMN_THREADSID + " = ? AND "
            //                            + NoticesTable.NOTICE_COLUMN_RECEIVEDTIME + " >= ? ",
            //                    new String[] { convstId, recvTimeBegin + "" },
            //                    NoticesTable.NOTICE_COLUMN_RECEIVEDTIME + " ASC");
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
            throw e;
        }
    }


    /**
     * @param convstId 会话ID
     * @param recvTimeBegin 起始接收时间
     * @return cursor对象，可能会因异常而返回null
     * @throws Exception
     * @author: zhaguitao
     * @Title: queryPageNotices
     * @Description: 查询某会话下起始接收时间之前的一页消息
     */
    public Cursor queryPageNotices(String convstId, long recvTimeBegin, int pageCnt)
        throws Exception {
        if (TextUtils.isEmpty(convstId) || pageCnt <= 0) {
            return null;
        }
        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
            "QUERY_PAGE_DT_NOTICES" + "/" + convstId + "/" + recvTimeBegin + "/" + pageCnt);
        try {
            return mcontext.getContentResolver().query(uri, null, null, null, null);
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
            throw e;
        }
    }


    /**
     * 根据threadid查询到该会话下的所有的图片类型的消息
     *
     * @param threadid 会话的id
     * @param type 消息类型（图片、视频、声音）
     */
    private Cursor getAllPAVInConvst(String threadid, int type)
        throws Exception {

        if (TextUtils.isEmpty(threadid)) {
            return null;
        }

        try {
            return mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                select_columns,
                NoticesTable.NOTICE_COLUMN_THREADSID + " = ? AND "
                    + NoticesTable.NOTICE_COLUMN_TYPE + " = ?",
                new String[] { threadid, type + "" },
                NoticesTable.NOTICE_COLUMN_SENDTIME + " ASC");
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
            throw e;
        }
    }


    /**
     * 根据threadid查询到该会话下的所有的图片类型的消息
     *
     * @param threadid 会话的id
     * @param type 消息类型（图片、视频、声音）
     */
    public List<NoticesBean> getAllPAVInConversation(String threadid, int type) {
        Cursor cursor = null;
        List<NoticesBean> list = null;
        NoticesBean bean = null;
        try {
            cursor = getAllPAVInConvst(threadid, type);
            if (cursor != null && cursor.getCount() > 0) {
                list = new ArrayList<NoticesBean>();
                cursor.moveToFirst();
                do {
                    bean = NoticesTable.pureCursor(cursor);
                    list.add(bean);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomLog.e(TAG, "Exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }


    /**
     * 获取会话中所有的图片和视频
     *
     * @param threadid 会话的id
     * 消息类型（图片、视频、声音）
     */
    public List<NoticesBean> getAllPicAndVideoInConversation(String threadid) {
        //        Cursor cursor = null;
        //        List<NoticesBean> list = null;
        //        NoticesBean bean = null;
        //        try {
        //            cursor = mcontext.getContentResolver().query(
        //                    ProviderConstant.NETPHONE_HPU_NOTICE_URI,
        //                    select_columns,
        //                    NoticesTable.NOTICE_COLUMN_THREADSID + " = ? AND "
        //                            + NoticesTable.NOTICE_COLUMN_TYPE + " IN (?,?)",
        //                    new String[] { threadid, FileTaskManager.NOTICE_TYPE_PHOTO_SEND + ""
        //                            ,FileTaskManager.NOTICE_TYPE_VEDIO_SEND + ""},
        //                    NoticesTable.NOTICE_COLUMN_SENDTIME + " ASC");
        //
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //            CustomLog.e(TAG,"Exception" + e.toString());
        //        }
        //        return cursor;

        Cursor cursor = null;
        List<NoticesBean> list = null;
        NoticesBean bean = null;
        try {
            cursor = mcontext.getContentResolver().query(ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                select_columns,
                DtNoticesTable.NOTICE_COLUMN_THREADSID + " = ? AND "
                    + DtNoticesTable.NOTICE_COLUMN_TYPE + " IN (?,?)",
                new String[] { threadid, FileTaskManager.NOTICE_TYPE_PHOTO_SEND + ""
                    , FileTaskManager.NOTICE_TYPE_VEDIO_SEND + "" },
                DtNoticesTable.NOTICE_COLUMN_SENDTIME + " ASC");
            if (cursor != null && cursor.getCount() > 0) {
                list = new ArrayList<NoticesBean>();
                cursor.moveToFirst();
                do {
                    bean = NoticesTable.pureCursor(cursor);
                    list.add(bean);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomLog.e(TAG, "Exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }


    /**
     * @author: zhaguitao
     * @Title: countVcardNotice
     * @Description: 统计名片信息 （该方法将废弃）
     * @date: 2014-1-6 上午11:03:34
     */
    public int countVcardNotice() {
        int vcardCnt = 0;
        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
            "COUNT_VCARD_NOTICES");
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(uri, null, null, null,
                null);

            if (cursor != null) {
                cursor.moveToFirst();
                vcardCnt = cursor.getInt(0);
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return vcardCnt;
    }


    /**
     * @author: lihs
     * @Title: queryVcardCursor
     * @Description: 查询名片信息 （该方法将废弃）
     * @date: 2014-1-3 上午11:33:41
     */
    public Cursor queryVcardCursor() {

        Uri uri = Uri.parse(ProviderConstant.MEDICAL_URI
            + "/QUERY_VCARD_CURSOR");
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(uri, null, null, null,
                null);
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return cursor;
    }


    /**
     * 删除所有的名片消息（此方法会废弃）
     */
    public boolean clearVcardMsg() {

        Uri deleteVcardUri = Uri.withAppendedPath(
            ProviderConstant.MEDICAL_URI, "DELETE_VCARD_MSG");
        try {
            mcontext.getContentResolver().delete(
                deleteVcardUri,
                NoticesTable.NOTICE_COLUMN_TYPE + "= '4'" + " OR "
                    + NoticesTable.NOTICE_COLUMN_TYPE + "= '40'", null);
            return true;
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return false;
    }


    /**
     * @author: zhaguitao
     * @Title: queryNoRunningSendNotices
     * @Description: 查询准备分享或分享失败的消息
     * @date: 2013-8-9 上午11:29:54
     */
    public Cursor queryNoRunningSendNotices() {
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                select_columns,
                "(" + DtNoticesTable.NOTICE_COLUMN_STATUS + "=? or "
                    + DtNoticesTable.NOTICE_COLUMN_STATUS
                    + "=?) AND CAST(" + DtNoticesTable.NOTICE_COLUMN_TYPE
                    + " AS INT) < ?",
                new String[] { FileTaskManager.TASK_STATUS_READY + "",
                    FileTaskManager.TASK_STATUS_FAIL + "", 10 + "" },
                null);
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return cursor;
    }


    /**
     * @author: zhaguitao
     * @Title: queryAllRunningNotices
     * @Description: 查询状态为执行中的消息
     * @date: 2014-2-19 上午11:12:48
     */
    public Cursor queryAllRunningNotices() {
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI, select_columns,
                DtNoticesTable.NOTICE_COLUMN_STATUS + " = ?",
                new String[] { FileTaskManager.TASK_STATUS_RUNNING + "" },
                null);
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return cursor;
    }


    /**
     * @author: zhaguitao
     * @Title: queryNubeNames
     * @Description:查询视频联系人名称
     */
    public Cursor queryNubeNames() {
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_SIMPLE_DT_LINKMAN");
        try {
            cursor = mcontext.getContentResolver().query(uri, null, null, null,
                null);
            CustomLog.d(TAG, "查询视频联系人名称success");
        } catch (Exception e) {
            CustomLog.e(TAG, "查询视频联系人名称 Exception" + e.toString());
            return null;
        }
        return cursor;
    }


    /**
     * 根据消息的uuid获得该消息的bean对象
     *
     * @param uuid 消息的id
     * @return bean对象
     */
    public NoticesBean getNoticeById(String uuid) {

        if (TextUtils.isEmpty(uuid)) {
            return null;
        }

        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI, select_columns,
                NoticesTable.NOTICE_COLUMN_ID + " = ?",
                new String[] { uuid },
                NoticesTable.NOTICE_COLUMN_RECEIVEDTIME + " ASC");
            if (cursor != null) {

                NoticesBean bean = null;
                if (cursor.moveToFirst()) {
                    bean = NoticesTable.pureCursor(cursor);
                }
                cursor.close();
                cursor = null;
                return bean;
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }


    /**
     * 根据消息的id,获得会话的id
     *
     * @param uuid 消息的id
     * @return 会话的id;空串表示没有获得到
     */
    public String getThreadIdById(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            return "";
        }
        NoticesBean bean = getNoticeById(uuid);
        if (bean != null) {
            return bean.getThreadsId();
        }
        return "";
    }


    /**
     * 插入一条数据到本地数据库； 页面中不建议调用，仅在Dao方法中内部使用
     *
     * @param bean 要插入的对象
     * @return 在成功插入后，将此uuid返回
     */
    public String insertNotice(NoticesBean bean) {
        if (bean == null) {
            return "";
        }
        ContentValues value = NoticesTable.makeContentValue(bean);
        String uuid = bean.getId();
        Uri url = null;
        if (value != null) {
            try {
                url = mcontext.getContentResolver().insert(
                    ProviderConstant.NETPHONE_HPU_NOTICE_URI, value);
            } catch (Exception e) {
                e.printStackTrace();
                CustomLog.e(TAG, "Exception" + e.toString());
            }
            value = null;
        }
        if (url != null) {
            return uuid;
        }
        return "";
    }


    public synchronized int queryALLUnreadNotice()
        throws Exception {
        Cursor cursor = null;
        int count = 0;
        //        //计算今天的日期的时间戳 从00:00-24:00
        long current = System.currentTimeMillis();//当前时间毫秒数
        CustomLog.d(TAG, "当前是时间戳:+" + current);
        long zero = current / (1000 * 3600 * 24) * (1000 * 3600 * 24) -
            TimeZone.getDefault().getRawOffset();//今天零点零分零秒的毫秒数
        long twelve = zero + 24 * 60 * 60 * 1000 - 1;//今天23点59分59秒的毫秒数
        CustomLog.d(TAG, "纠正时间 = " + TimeZone.getDefault().getRawOffset());
        CustomLog.d(TAG, "今天零点时间戳:" + zero);
        CustomLog.d(TAG, "今天24点时间戳:" + twelve);
        if (current > twelve) {
            zero = zero + 24 * 60 * 60 * 1000;
            twelve = zero + 24 * 3600 * 1000 - 1;
            CustomLog.d(TAG, "次日凌晨0点-8点修正时间戳 0点时间 = " + zero + "24点时间戳 =" + twelve);
        }
        String zeroStr = String.valueOf(zero);
        String twelveStr = String.valueOf(twelve);
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                select_unread_columns,
                DtNoticesTable.NOTICE_COLUMN_ISNEW + " = ? AND " +
                    DtNoticesTable.NOTICE_COLUMN_SENDTIME + " > ? AND " +
                    DtNoticesTable.NOTICE_COLUMN_SENDTIME +
                    " < ?",
                new String[] { "1", zeroStr, twelveStr },
                DtNoticesTable.NOTICE_COLUMN_SENDTIME + " ASC ");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    count = count + 1;
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return count;
    }


    /**
     * 通过threadsId 查询所有未读消息
     */
    public synchronized int queryUnreadNotice(String threadId)
        throws Exception {
        if (TextUtils.isEmpty(threadId)) {
            return 0;
        }
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                select_unread_columns,
                DtNoticesTable.NOTICE_COLUMN_ISNEW + " = ? AND "
                    + DtNoticesTable.NOTICE_COLUMN_THREADSID + " = ? ",
                new String[] { "1", threadId },
                DtNoticesTable.NOTICE_COLUMN_RECEIVEDTIME + " ASC ");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    count = count + 1;
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return count;
    }


    public int updateNotice(String uuid, String body, int status) {
        ContentValues value = new ContentValues();
        value.put(NoticesTable.NOTICE_COLUMN_BODY, body);
        value.put(NoticesTable.NOTICE_COLUMN_STATUS, status);
        int count = 0;
        try {
            count = mcontext.getContentResolver()
                .update(ProviderConstant.NETPHONE_HPU_NOTICE_URI, value,
                    NoticesTable.NOTICE_COLUMN_ID + "=?",
                    new String[] { uuid });
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        value = null;
        return count;
    }


    public int updateNotice(String uuid, String body) {
        ContentValues value = new ContentValues();
        value.put(NoticesTable.NOTICE_COLUMN_BODY, body);
        int count = 0;
        try {
            count = mcontext.getContentResolver()
                .update(ProviderConstant.NETPHONE_HPU_NOTICE_URI, value,
                    NoticesTable.NOTICE_COLUMN_ID + "=?",
                    new String[] { uuid });
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        value = null;
        return count;
    }


    public int updateNotice(String uuid, long time) {
        ContentValues value = new ContentValues();
        value.put(NoticesTable.NOTICE_COLUMN_SENDTIME, time);
        int count = 0;
        try {
            count = mcontext.getContentResolver()
                .update(ProviderConstant.NETPHONE_HPU_NOTICE_URI, value,
                    NoticesTable.NOTICE_COLUMN_ID + "=?",
                    new String[] { uuid });
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        NoticesBean bean = getNoticeById(uuid);
        if (bean != null) {
            // threadsDao.updateLastTime(bean.getThreadsId(), time);
        }
        value = null;
        return count;
    }


    public int updateNotice(String uuid, int status) {
        ContentValues value = new ContentValues();
        value.put(NoticesTable.NOTICE_COLUMN_STATUS, status);
        int count = 0;
        try {
            count = mcontext.getContentResolver()
                .update(ProviderConstant.NETPHONE_HPU_NOTICE_URI, value,
                    NoticesTable.NOTICE_COLUMN_ID + "=?",
                    new String[] { uuid });
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        value = null;
        return count;
    }


    public Cursor queryAllNotice(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            return null;
        }
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                select_columns,
                DtNoticesTable.NOTICE_COLUMN_THREADSID + " = ? ",
                new String[] { uuid },
                DtNoticesTable.NOTICE_COLUMN_SENDTIME + " ASC");
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return cursor;
    }


    /**
     * 修改声音消息是否被下载收听
     *
     * @param uuid 消息的id
     * @param read [red]是否被收听 true:已听 false:未听
     */
    public int updateAudioIsRead(String uuid, boolean read) {
        int count = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(DtNoticesTable.NOTICE_COLUMN_ISREAD, read ? 0 : 1);
            count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI, values,
                DtNoticesTable.NOTICE_COLUMN_ID + " = ? ",
                new String[] { uuid });
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return count;
    }


    /**
     * 更新某个会话下的所有未读的新消息为已读（isNew字段）
     *
     * @throws Exception
     */
    public synchronized int updateNewStatusInConvst(String threadsid)
        throws Exception {
        CustomLog.i(TAG, "updateNewStatusInConvst()");

        int count = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(NoticesTable.NOTICE_COLUMN_ISNEW, "0");
            count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                values,
                NoticesTable.NOTICE_COLUMN_THREADSID + " = ? AND "
                    + NoticesTable.NOTICE_COLUMN_ISNEW + " = ? ",
                new String[] { threadsid, "1" });
            CustomLog.i(TAG, "update newStatus msg count" + count);
            mcontext.getContentResolver()
                .notifyChange(ProviderConstant.NETPHONE_HPU_NOTICE_URI, null);
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
            throw e;
        }

        return count;
    }


    public synchronized Cursor getUnreadNotice(String threadsid)
        throws Exception {
        if (TextUtils.isEmpty(threadsid)) {
            return null;
        }
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                select_unread_columns,
                DtNoticesTable.NOTICE_COLUMN_ISNEW + " = ? AND "
                    + DtNoticesTable.NOTICE_COLUMN_THREADSID + " = ? ",
                new String[] { "1", threadsid },
                DtNoticesTable.NOTICE_COLUMN_RECEIVEDTIME + " ASC ");
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return cursor;
    }


    public int updateNewStatus(String uuid) {
        int count = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(DtNoticesTable.NOTICE_COLUMN_ISNEW, "0");
            count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                values,
                DtNoticesTable.NOTICE_COLUMN_ID + " = ? AND "
                    + DtNoticesTable.NOTICE_COLUMN_ISNEW + " = ? ",
                new String[] { uuid, "1" });
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return count;
    }


    /**
     * 获得所有打扰的未读新消息的数目（isNew字段） 主要用在主页面底部的红圈显示
     *
     * @return 返回新消息的数目
     */
    public int getNewNoticeCount() {
        int count = 0;
        Cursor cursor = null;
        try {
            Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
                + "/QUERY_UNREAD_NOTICES_DT_COUNT");

            cursor = mcontext.getContentResolver().query(uri, null, null, null, null);

            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomLog.e(TAG, "Exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return count;
    }


    /**
     * 获得所有免打扰的未读新消息的数目（isNew字段） 主要用在主页面底部的红圈显示
     *
     * @return 返回新消息的数目
     */
    public int getNewNoticeCountOfNoDisturb() {
        int count = 0;
        Cursor cursor = null;
        try {
            Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
                + "/QUERY_UNREAD_NOTICES_DT_COUNT_NODISTURB");

            cursor = mcontext.getContentResolver().query(uri, null, null, null, null);

            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomLog.e(TAG, "Exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return count;
    }


    /**
     * 根据视频号，查询该人发送的消息中的未读的数目
     *
     * @param sendernumber 发送者的视频号
     * @return 未读的数目
     */
    public int getNewNoticeCountByNumber(String sendernumber) {
        if (TextUtils.isEmpty(sendernumber)) {
            return 0;
        }
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                select_columns,
                DtNoticesTable.NOTICE_COLUMN_ISNEW + " = ? AND "
                    + DtNoticesTable.NOTICE_COLUMN_SENDER + " = ? ",
                new String[] { "1", sendernumber },
                DtNoticesTable.NOTICE_COLUMN_RECEIVEDTIME + " ASC ");
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomLog.e(TAG, "Exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return count;
    }


    /**
     * 删除会话中的的一条记录。 如果删除的是列表最底部的一条记录，请使用deleteLastNotice方法；
     * 或者调用updateThreadTime(threadid)变更时间， 否则会导致 会话表中的lasttime字段不更新
     */
    public int deleteNotice(String uuid) {
        int count = 0;
        try {
            count = mcontext.getContentResolver()
                .delete(ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                    DtNoticesTable.NOTICE_COLUMN_ID + "=?",
                    new String[] { uuid });
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return count;
    }


    /**
     * 批量删除多条记录
     */
    public int deleteNotices(List<String> uuids) {
        int count = 0;
        try {
            count = mcontext.getContentResolver()
                .delete(ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                    DtNoticesTable.NOTICE_COLUMN_ID + " IN ( " + StringUtil.list2DBINString(uuids) +
                        " )",
                    new String[] {});
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return count;
    }


    /**
     * 删除会话中的的最近一条记录(列表最底部的一条记录)， 会同步修改 会话表中lasttime字段 为前一条记录的接收时间
     *
     * @param uuid 最近一条记录的uuid
     * @param threadid 该记录的threadid
     * @param preRevieverTime 前一条记录的接收时间 ； 如果是空串，则不更新会话表的lasttime字段
     */
    public int deleteLastNotice(String uuid, String threadid,
                                long preRevieverTime) {
        int count = 0;
        try {
            count = mcontext.getContentResolver()
                .delete(ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                    DtNoticesTable.NOTICE_COLUMN_ID + "=?",
                    new String[] { uuid });
        } catch (Exception e) {
            e.printStackTrace();
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        // 在删除记录成功的时候，同步更新会话的lasttime字段
        if (count > 0 && !TextUtils.isEmpty(threadid) && (preRevieverTime > 0)) {

        }
        return count;
    }


    /**
     * 根据threadsid，拿到该会话下的最近一条信息的接收时间
     *
     * @param threadid 会话的id
     * @return long 接收时间；0表示没有记录 或 出现异常
     */
    private long getLastItemRecTime(String threadid) {
        Cursor cursor = null;
        long rectime = 0;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI, select_columns,
                DtNoticesTable.NOTICE_COLUMN_THREADSID + " = ?",
                new String[] { threadid },
                DtNoticesTable.NOTICE_COLUMN_RECEIVEDTIME + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                rectime = cursor
                    .getLong(cursor
                        .getColumnIndex(DtNoticesTable.NOTICE_COLUMN_RECEIVEDTIME));
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return rectime;
    }


    /**
     * @author: zhaguitao
     * @Title: deleteNoticesBetweenDate
     * @Description: 删除指定时间内的消息 （该方法会废弃）
     * @date: 2013-8-15 下午5:59:33
     */
    public int deleteNoticesByDay(String dbDay) {
        int count = 0;
        try {
            count = mcontext.getContentResolver().delete(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                "substr(" + DtNoticesTable.NOTICE_COLUMN_RECEIVEDTIME
                    + ", 1, 8) = ? ", new String[] { dbDay });
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return count;
    }


    /**
     * 根据会话id,删除该会话下的所有消息 此场景下删除，会话表中的lasttime字段不用更新
     *
     * @param threadid 会话id, 不能为空
     * @return <=0：参数为空 删除失败 或 该会话下无消息 >0: 删除的消息数目
     */
    public int deleteAllNoticesInConversation(String threadid) {
        if (TextUtils.isEmpty(threadid)) {
            return -1;
        }
        int count = 0;
        try {
            count = mcontext.getContentResolver().delete(
                ProviderConstant.NETPHONE_HPU_NOTICE_URI,
                DtNoticesTable.NOTICE_COLUMN_THREADSID + " = ?",
                new String[] { threadid });
        } catch (Exception e) {
            e.printStackTrace();
            CustomLog.e(TAG, "Exception" + e.toString());
        }
        return count;
    }


    /***
     * 创建一条待发送的消息记录到表中
     *
     * @param sender
     *            发送者nubeNumber
     * @param receiver
     *            接收者nubeNumber，可以是多人，用分号分割(群发场景)；
     *            若是群组聊天，此处应该为群组的group id
     * @param localfiles
     *            待发送的本地文件的list； 尽管此处是list，但真正传递的仅是第一个文件;
     *            在类型为文字时，该值可以为空
     * @param title
     *            标题（暂无具体意义） 在分享名片时，为其他终端接收有意义，此处需要传递字段 “分享 xxx 的名片”
     * @param type
     *            消息的类型（视频、图片、声音、文字、名片）
     * @param txt
     *            文字文本 （当类型为文字时，才有意义）
     * @param threadid
     *            会话id;当该字段为NULL或空串时，将根据receiver创建一个会话；
     *            若是群组聊天，此处应该为群组的group id,且不能为空；
     * @param extInfo
     *            扩展信息： 名片类型 请使用ButelVcardBean对象； 视频、图片、声音类型
     *            请使用ButelPAVExInfo对象； 文字类型， 可以不填写；
     *            通话记录类型，为Ingeter；
     *            会议邀请类型，请使用ButelMeetingExInfo对象；
     * @return 返回新插入的消息的uuid, 当返回值为空串，表示没有插入 或 插入失败
     * @throws Exception
     */
    @SuppressWarnings("unused")
    public String createSendFileNotice(String sender, String receiver,
                                       List<String> localfiles, String title, int type, String txt,
                                       String threadid, Object extInfo) {

        if (TextUtils.isEmpty(sender) || TextUtils.isEmpty(receiver)) {
            CustomLog.d(TAG, "createSendFileNotice  sender or receiver==null");
            return "";
        }
        // sort 接收对象的视频号
        String recipentIds = StringUtil.sortRecipentIds(receiver, ';');
        CustomLog.d(TAG, "after sorted recipentIds:" + recipentIds);

        String body = "";
        if (FileTaskManager.NOTICE_TYPE_PHOTO_SEND == type
            || FileTaskManager.NOTICE_TYPE_VEDIO_SEND == type
            || FileTaskManager.NOTICE_TYPE_AUDIO_SEND == type) {

            if (localfiles != null && localfiles.size() > 0) {
                JSONArray array = new JSONArray();
                JSONObject object = null;
                try {
                    String path = "";
                    object = new JSONObject();
                    path = localfiles.get(0);
                    object.put("localUrl", path);
                    object.put("remoteUrl", "");
                    if (!TextUtils.isEmpty(path)) {
                        long size = 0;
                        File file = new File(path);
                        if (file.exists()) {
                            size = file.length();
                        }
                        object.put("size", size);
                    } else {
                        object.put("size", "0");
                    }
                    object.put("compressPath", "");
                    // object.put("overSize",
                    // ChatActivity.isOverImageSize(path));
                    if (extInfo != null) {
                        ButelPAVExInfo bean = null;
                        try {
                            bean = (ButelPAVExInfo) extInfo;
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                            CustomLog.e(TAG, "ClassCastException" + e.toString());
                        }
                        if (bean != null) {
                            object.put("width", bean.getWide());
                            object.put("height", bean.getHeight());
                            object.put("duration", bean.getDuration());
                        }
                    }
                    array.put(object);
                    body = array.toString();
                } catch (JSONException e) {
                    CustomLog.e(TAG, "JSONException" + e.toString());
                    e.printStackTrace();
                }
            } else {
                CustomLog.d(TAG, "createSendFileNotice pav path==null");
                return "";
            }
        } else if (FileTaskManager.NOTICE_TYPE_VCARD_SEND == type) {

            if (extInfo != null) {
                ButelVcardBean bean = null;
                try {
                    bean = (ButelVcardBean) extInfo;
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    CustomLog.e(TAG, "ClassCastException" + e.toString());
                    return "";
                }
                JSONArray array = new JSONArray();
                JSONObject object = null;
                object = new JSONObject();
                try {
                    object.put("name", bean.getNickname());
                    object.put("code", bean.getNubeNumber());
                    object.put("url", bean.getHeadUrl());
                    object.put("userid", bean.getUserId());
                    object.put("phone", bean.getPhoneNumber());
                    object.put("sex", bean.getSex());
                    if (localfiles != null && localfiles.size() > 0) {
                        object.put("localUrl", localfiles.get(0));
                        object.put("remoteUrl", "");
                        object.put("size", "");
                    } else {
                        // TODO:在没有产生vcf文件的情况下，
                        // 在发送时,在用上面的info再次创建一下
                        object.put("localUrl", "");
                        object.put("remoteUrl", "");
                        object.put("size", "");
                    }
                    array.put(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                body = array.toString();
            } else {
                CustomLog.d(TAG, "createSendFileNotice vard  extinfo==null");
                return "";
            }
        } else if (FileTaskManager.NOTICE_TYPE_TXT_SEND == type) {
            if (TextUtils.isEmpty(txt)) {
                CustomLog.d(TAG, "createSendFileNotice  txt==null");
                return "";
            }
            JSONArray array = new JSONArray();
            JSONObject object = null;
            object = new JSONObject();
            try {
                object.put("txt", txt);
                array.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            body = array.toString();
        } else if (FileTaskManager.NOTICE_TYPE_REMIND_SEND == type) {

            if (TextUtils.isEmpty(txt)) {
                CustomLog.d(TAG, "createSendFileNotice  txt==null");
                return "";
            }
            JSONArray array = new JSONArray();
            try {
                array.put(new JSONObject(txt));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            body = array.toString();
        } else if (FileTaskManager.NOTICE_TYPE_REMIND_ONE_SEND == type) {

            if (TextUtils.isEmpty(txt)) {
                CustomLog.d(TAG, "createSendFileNotice  txt==null");
                return "";
            }
            JSONArray array = new JSONArray();
            JSONObject txtObj = new JSONObject();
            try {
                txtObj.put("content", "@某个人");
                txtObj.put("text", txt);
                txtObj.put("subtype", "remind_notice_one");
            } catch (Exception e) {
                CustomLog.d(TAG, "sendGroupMsg" + e.toString());
            }
            array.put(txtObj);
            body = array.toString();
        } else if (FileTaskManager.NOTICE_TYPE_RECORD == type) {
            if (TextUtils.isEmpty(txt)) {
                CustomLog.d(TAG, "createSendFileNotice  txt==null");
                return "";
            }
            JSONArray array = new JSONArray();
            JSONObject object = null;
            object = new JSONObject();
            try {
                object.put("txt", txt);
                object.put("calltype", extInfo);
                array.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            body = array.toString();
        } else if (FileTaskManager.NOTICE_TYPE_MEETING_INVITE == type) {

            if (extInfo != null) {
                ButelMeetingExInfo bean = null;
                try {
                    bean = (ButelMeetingExInfo) extInfo;
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    CustomLog.e(TAG, "ClassCastException" + e.toString());
                    return "";
                }
                JSONArray array = new JSONArray();
                JSONObject object = null;
                object = new JSONObject();
                try {
                    object.put("inviterId", bean.getInviterId());
                    object.put("inviterName", bean.getInviterName());
                    object.put("inviterHeadUrl", bean.getInviterHeadUrl());
                    object.put("meetingRoom", bean.getMeetingRoom());
                    object.put("meetingUrl", bean.getMeetingUrl());
                    object.put("showMeeting", bean.isShowMeeting());
                    array.put(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                body = array.toString();
            } else {
                CustomLog.d(TAG, "createSendFileNotice meeting invite  extinfo==null");
                return "";
            }
        } else if (FileTaskManager.NOTICE_TYPE_MEETING_BOOK == type) {
            if (extInfo != null) {
                BookMeetingExInfo bean = null;
                try {
                    bean = (BookMeetingExInfo) extInfo;
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    CustomLog.e(TAG, "ClassCastException" + e.toString());
                    return "";
                }
                JSONArray array = new JSONArray();
                JSONObject object = null;
                object = new JSONObject();
                try {
                    object.put(BookMeetingExInfo.BOOK_NUBE, bean.getBookNube());
                    object.put(BookMeetingExInfo.BOOK_NAME, bean.getBookName());
                    object.put(BookMeetingExInfo.MEETING_TIME, bean.getMeetingTime() / 1000);
                    //20160316  IOS 要求发送消息时,使用秒。
                    object.put(BookMeetingExInfo.MEETING_ROOM, bean.getMeetingRoom());
                    object.put(BookMeetingExInfo.MEETING_URL, bean.getMeetingUrl());
                    object.put(BookMeetingExInfo.MEETING_THEME, bean.getMeetingTheme());
                    array.put(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                body = array.toString();
            } else if (FileTaskManager.NOTICE_TYPE_FILE == type) {
                if (extInfo != null) {
                    ButelFileInfo bean = null;
                    try {
                        bean = (ButelFileInfo) extInfo;
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                        CustomLog.e(TAG, "ClassCastException" + e.toString());
                        return "";
                    }
                    JSONArray array = new JSONArray();
                    JSONObject object = null;
                    object = new JSONObject();
                    try {
                        object.put("size", bean.getSize());
                        object.put("fileName", bean.getFileName());
                        object.put("fileType", bean.getFileType());
                        object.put("localUrl", bean.getLocalPath());
                        object.put("remoteUrl", bean.getRemoteUrl());
                        array.put(object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    body = array.toString();
                } else {
                    return "";
                }
            } else {
                CustomLog.d(TAG, "createSendFileNotice book meeting invite  extinfo==null");
                return "";
            }
        } else {
            CustomLog.d(TAG, "createSendFileNotice  非法的type");
            return "";
        }

        Cursor cursor = null;
        long curtime = System.currentTimeMillis();
        NoticesBean bean = new NoticesBean();
        bean.setSender(sender);
        bean.setReciever(recipentIds);
        bean.setBody(body);
        bean.setStatus(0);
        bean.setType(type);
        bean.setIsNew(0);
        bean.setIsRead(1); //发送的消息默认读过了
        bean.setTitle(title);

        String uuid = StringUtil.getUUID();
        bean.setId(uuid);
        if (TextUtils.isEmpty(threadid)) {
        } else {

            bean.setThreadsId(threadid);
        }

        try {
            cursor = queryAllNotice(threadid);
            if (cursor != null && cursor.moveToLast()) {
                curtime = cursor.getLong(cursor
                    .getColumnIndex(NoticesTable.NOTICE_COLUMN_SENDTIME)) + 10;
                if (curtime < System.currentTimeMillis()) {
                    curtime = System.currentTimeMillis();
                }
            } else {
                // 为了修改新建会话时，服务端时间早于本地时间出现消息不能刷新显示问题，直接初始值为0
                curtime = 1;
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "queryAllNotice 查询异常" + e.toString());
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }

        bean.setSendTime(curtime);
        if (curtime == 1) {
            bean.setReceivedTime(System.currentTimeMillis());
        }
        // 下面两个字段不再维护
        bean.setMsgId("");
        bean.setFailReplyId("");

        JSONObject extObj = new JSONObject();
        try {
            extObj.put("id", bean.getId());
            extObj.put("text", "");
            extObj.put("ver", BizConstant.MSG_VERSION);
            extObj.put("medicalComboMsg", 1);
        } catch (JSONException e) {
            CustomLog.e(TAG, "JSONException" + e.toString());
            e.printStackTrace();
        }
        bean.setExtInfo(extObj.toString());

        uuid = insertNotice(bean);
        return uuid;
    }


    public String createRecPAVMsgBody(String remoteUrl, String thumbUrl,
                                      String extinfo, int type) {
        String body = "";
        if (remoteUrl != null) {
            JSONArray array = new JSONArray();
            JSONObject object = null;
            try {
                object = new JSONObject();
                object.put("localUrl", "");
                object.put("remoteUrl", remoteUrl);
                object.put("thumbnail", thumbUrl);
                object.put("size", "");
                if (!TextUtils.isEmpty(extinfo)) {
                    try {
                        JSONObject extObject = new JSONObject(extinfo);
                        if (FileTaskManager.NOTICE_TYPE_PHOTO_SEND == type) {
                            object.put("width", extObject.optString("width"));
                            object.put("height", extObject.optString("height"));
                            object.put("size", extObject.optLong("fileSize"));
                            object.put("fileName", extObject.optString("fileName"));
                        } else if (FileTaskManager.NOTICE_TYPE_VEDIO_SEND == type) {
                            object.put("duration",
                                extObject.optString("vediolen"));
                            object.put("width", extObject.optString("width"));
                            object.put("height", extObject.optString("height"));
                            object.put("size", extObject.optLong("fileSize"));
                            object.put("fileName", extObject.optString("fileName"));
                        } else if (FileTaskManager.NOTICE_TYPE_AUDIO_SEND == type) {
                            object.put("duration",
                                extObject.optString("audiolen"));
                            object.put("thumbnail", "");
                            object.put("isRead", "false");
                            object.put("size", extObject.optLong("fileSize"));
                            object.put("fileName", extObject.optString("fileName"));
                        }
                    } catch (JSONException e) {
                        CustomLog.e(TAG, "JSONException" + e + toString());
                        e.printStackTrace();
                    }
                }
                array.put(object);
                body = array.toString();
            } catch (JSONException e) {
                CustomLog.e(TAG, "JSONException" + e + toString());
                e.printStackTrace();
            }
        } else {
            CustomLog.d(TAG, "createReceiveFileNotice P A V remoteUrl==null");
            return "";
        }
        return body;
    }


    public String createRecCardMsgBody(String remoteUrl, String extinfo) {
        String body = "";
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        if (!TextUtils.isEmpty(extinfo)) {
            try {
                JSONObject extObject = new JSONObject(extinfo);
                JSONObject item = null;
                String card = extObject.optString("card");
                if (!TextUtils.isEmpty(extinfo)) {
                    JSONArray cardObj = new JSONArray(card);
                    item = cardObj.getJSONObject(0);
                    object.put("code", item.optString("code"));
                    object.put("name", item.optString("name"));
                    object.put("phone", item.optString("phone"));
                    object.put("url", item.optString("url"));
                    object.put("userid", item.optString("userid"));
                    object.put("sex", item.optString("sex"));
                } else {
                    CustomLog.d(TAG, "createReceiveFileNotice card==null");
                    return "";
                }

                object.put("localUrl", "");
                object.put("remoteUrl", remoteUrl);
                object.put("size", "");

                array.put(object);
                body = array.toString();
            } catch (JSONException e) {
                CustomLog.e(TAG, "JSONException" + e.toString());
                e.printStackTrace();
            }
        } else {
            CustomLog.d(TAG, "createReceiveFileNotice card extInfo==null");
            return "";
        }
        return body;
    }


    public String createRecCollectionFileMsgBody(String extinfo, String obj) {
        String body = "";
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        if (!TextUtils.isEmpty(extinfo)) {
            try {
                JSONArray arr = new JSONArray(obj);
                String mobj = "";
                if (array.length() > 0) {
                    mobj = (String) arr.get(0);
                }
                JSONObject extObject = new JSONObject(extinfo);
                String meeting = extObject.optString("fileInfo");
                if (!TextUtils.isEmpty(meeting)) {
                    JSONObject meetingObject = new JSONObject(meeting);
                    object.put("size", meetingObject.optLong("size"));
                    object.put("fileName", meetingObject.optString("fileName"));
                    object.put("fileType", meetingObject.optString("fileType"));
                    object.put("localUrl", meetingObject.optString("localUrl"));
                    if (TextUtils.isEmpty(meetingObject.optString("remoteUrl"))) {
                        object.put("remoteUrl", mobj);
                    } else {
                        object.put("remoteUrl", meetingObject.optString("remoteUrl"));
                    }
                } else {
                    CustomLog.d(TAG, "createRecMeetingInviteMsgBody meetinginfo==null");
                    return "";
                }
                array.put(object);
                body = array.toString();
            } catch (JSONException e) {
                CustomLog.e(TAG, "JSONException" + e.toString());
                e.printStackTrace();
            }
        } else {
            CustomLog.d(TAG, "createReceiveFileNotice meetinginfo extInfo==null");
            return "";
        }
        return body;
    }


    public String createRecMeetingInviteMsgBody(String extinfo) {
        String body = "";
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        if (!TextUtils.isEmpty(extinfo)) {
            try {
                JSONObject extObject = new JSONObject(extinfo);
                String meeting = extObject.optString("meetingInfo");
                if (!TextUtils.isEmpty(meeting)) {
                    JSONObject meetingObject = new JSONObject(meeting);
                    object.put("inviterId", meetingObject.optString("inviterId"));
                    object.put("inviterName", meetingObject.optString("inviterName"));
                    object.put("inviterHeadUrl", meetingObject.optString("inviterHeadUrl"));
                    object.put("meetingRoom", meetingObject.optString("meetingRoom"));
                    object.put("meetingUrl", meetingObject.optString("meetingUrl"));
                    object.put("showMeeting", false);
                } else {
                    CustomLog.d(TAG, "createRecMeetingInviteMsgBody meetinginfo==null");
                    return "";
                }
                array.put(object);
                body = array.toString();
            } catch (JSONException e) {
                CustomLog.d(TAG, "JSONException" + e.toString());
                e.printStackTrace();
            }
        } else {
            CustomLog.d(TAG, "createReceiveFileNotice meetinginfo extInfo==null");
            return "";
        }
        return body;
    }


    public String createRecMeetingBookMsgBody(String extinfo) {
        String body = "";
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        if (!TextUtils.isEmpty(extinfo)) {
            try {
                JSONObject extObject = new JSONObject(extinfo);
                String meeting = extObject.optString(BookMeetingExInfo.BOOK_MEETING_INFO);
                if (!TextUtils.isEmpty(meeting)) {
                    JSONObject meetingObject = new JSONObject(meeting);
                    object.put(BookMeetingExInfo.BOOK_NUBE,
                        meetingObject.optString(BookMeetingExInfo.BOOK_NUBE));
                    object.put(BookMeetingExInfo.BOOK_NAME,
                        meetingObject.optString(BookMeetingExInfo.BOOK_NAME));
                    object.put(BookMeetingExInfo.MEETING_ROOM,
                        meetingObject.optString(BookMeetingExInfo.MEETING_ROOM));
                    object.put(BookMeetingExInfo.MEETING_URL,
                        meetingObject.optString(BookMeetingExInfo.MEETING_URL));
                    object.put(BookMeetingExInfo.MEETING_TIME,
                        meetingObject.optLong(BookMeetingExInfo.MEETING_TIME));
                    object.put(BookMeetingExInfo.MEETING_THEME,
                        meetingObject.optString(BookMeetingExInfo.MEETING_THEME));
                } else {
                    CustomLog.d(TAG, "createRecMeetingBookMsgBody meetinginfo==null");
                    return "";
                }
                array.put(object);
                body = array.toString();
            } catch (JSONException e) {
                CustomLog.e(TAG, "JSONException" + e.toString());
                e.printStackTrace();
            }
        } else {
            CustomLog.d(TAG, "createReceiveFileNotice meetinginfo extInfo==null");
            return "";
        }
        return body;
    }


    public String createRecTxtMsgBody(String txt) {
        String body = "";
        if (!TextUtils.isEmpty(txt)) {
            JSONArray array = new JSONArray();
            JSONObject object = new JSONObject();
            try {
                object.put("txt", txt);
                array.put(object);
                body = array.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            CustomLog.d(TAG, "createReceiveFileNotice txt ==null");
            return "";
        }
        return body;
    }


    public String createRecChatRecordMsgBody(String extinfo) {
        String body = "";
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        if (!TextUtils.isEmpty(extinfo)) {
            try {
                JSONObject extObject = new JSONObject(extinfo);
                JSONArray chatRecordArray = extObject.optJSONArray("chatrecordInfo");
                if (chatRecordArray == null) {
                    CustomLog.e(TAG, "聊天记录 chatrecordInfo 为空");
                    return body;
                }
                //将子类型的string type 转化为 int type
                for (int i = 0; i < chatRecordArray.length(); i++) {
                    JSONObject obj = chatRecordArray.getJSONObject(i);
                    String typeStr = obj.optString("type");
                    if (typeStr.equals(BizConstant.MSG_BODY_TYPE_TXT)) {
                        obj.put("type", FileTaskManager.NOTICE_TYPE_TXT_SEND);
                    } else if (typeStr.equals(BizConstant.MSG_BODY_TYPE_VIDEO_2)) {
                        obj.put("type", FileTaskManager.NOTICE_TYPE_VEDIO_SEND);
                    } else if (typeStr.equals(BizConstant.MSG_BODY_TYPE_PIC_2)) {
                        obj.put("type", FileTaskManager.NOTICE_TYPE_PHOTO_SEND);
                    } else if (typeStr.equals(BizConstant.MSG_BODY_TYPE_ARTICLE)) {
                        obj.put("type", FileTaskManager.NOTICE_TYPE_ARTICAL_SEND);
                    } else if (typeStr.equals(BizConstant.MSG_BODY_TYPE_POSTCARD)) {
                        obj.put("type", FileTaskManager.NOTICE_TYPE_VCARD_SEND);
                    } else {
                        CustomLog.e(TAG, "chatrecord type error " + typeStr);
                    }
                }

                if (chatRecordArray != null) {
                    object.put("chatrecordInfo", chatRecordArray);
                    object.put("text", extObject.optString("text"));
                    object.put("subtype", extObject.optString("subtype"));
                    array.put(object);
                    body = array.toString();
                }
            } catch (JSONException e) {
                CustomLog.e(TAG, "JSONException" + e.toString());
                e.printStackTrace();
            }
        } else {
            CustomLog.d(TAG, "createRecChatRecordMsgBody extInfo==null");
            return "";
        }
        return body;
    }


    public String createRecArticleMsgBody(String extinfo) {
        String body = "";
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        if (!TextUtils.isEmpty(extinfo)) {
            try {
                JSONObject extObject = new JSONObject(extinfo);
                JSONObject articleInfo = extObject.optJSONObject("articleInfo");
                if (articleInfo != null) {
                    object.put("articleInfo", articleInfo);
                    object.put("text", "文章");
                    object.put("subtype", extObject.optString("subtype"));
                    array.put(object);
                    body = array.toString();
                } else {
                    CustomLog.d(TAG, "createRecArticleMsgBody extInfo==null");
                    return "";
                }
            } catch (JSONException e) {
                CustomLog.e(TAG, "JSONException" + e.toString());
                e.printStackTrace();
            }
        } else {
            CustomLog.d(TAG, "createRecArticleMsgBody extInfo==null");
            return "";
        }
        return body;
    }


    public String createRecRemindMsgBody(String extinfo) {
        String body = "";
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        if (!TextUtils.isEmpty(extinfo)) {
            try {
                JSONObject extObject = new JSONObject(extinfo);
                if (extObject != null) {
                    object.put("content", extObject.optString("content"));
                    object.put("text", extObject.optString("text"));
                    object.put("subtype", extObject.optString("subtype"));
                    array.put(object);
                    body = array.toString();
                } else {
                    CustomLog.d(TAG, "createRecRemindMsgBody extObject==null");
                    return "";
                }
            } catch (JSONException e) {
                CustomLog.e(TAG, "JSONException" + e.toString());
                e.printStackTrace();
            }
        } else {
            CustomLog.d(TAG, "createRecRemindMsgBody extObject extInfo==null");
            return "";
        }
        return body;
    }


    public String createRecRecordMsgBody(String txt, int calltype) {
        String body = "";
        if (!TextUtils.isEmpty(txt)) {
            JSONArray array = new JSONArray();
            JSONObject object = new JSONObject();
            try {
                object.put("txt", txt);
                object.put("calltype", calltype);
                array.put(object);
                body = array.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            CustomLog.d(TAG, "createReceiveFileNotice txt ==null");
            return "";
        }
        return body;
    }


    /**
     * 创建一条收到的消息
     *
     * @param msgid 消息的msgid （尽量使用服务端返回的， 仅在收到91以前版本时，分享的多个图片拆解时，可以为空，需手动创建一个）
     * @param sender 消息的发送者ID
     * @param receiver 消息的接收这ID (一般为自己登录的帐号ID)
     * @param body 消息体，已经使用上面的三个方法转化为本地的Json数组；
     * createRecPAVMsgBody()---图片、声音、视频类型转换；
     * createRecCardMsgBody()---名片转换 ；
     * createRecTxtMsgBody---文字转换；
     * createRecRecordMsgBody()---通话记录/会议邀请记录 转换
     * @param type 消息的类型（图片、声音、视频、名片、文字、通话/会议记录）
     * @param title 消息的标题
     * @param extinfo 消息的扩展字段
     * @param sendTime 消息的发送时间
     * @param groupid 如果是群组聊天信息需要将group id作为会话的thread id
     * @return 新创建的消息的id 当返回值为空串，表示没有插入 或 插入失败
     */
    public String createReceiveMsgNotice(String msgid, String sender,
                                         String receiver, String body, int type, String title,
                                         String extinfo, String sendTime, String groupid, String severId) {

        CustomLog.d(TAG, "createReceiveMsgNotice msgid:" + msgid + "|sender:" + sender
            + "|receiver:" + receiver + "|body:" + body + "|type:" + type
            + "|title:" + title + "|extinfo:" + extinfo + "|sendTime:"
            + sendTime + "|groupid:" + groupid);

        if (TextUtils.isEmpty(body)) {
            CustomLog.d(TAG, "createReceiveFileNotice body ==null");
            return "";
        }

        NoticesBean bean = new NoticesBean();
        String id = TextUtils.isEmpty(msgid) ? StringUtil.getUUID() : msgid;
        bean.setId(id);
        bean.setSender(sender);
        bean.setReciever(receiver);
        bean.setBody(body);
        bean.setStatus(2);
        bean.setType(type);
        bean.setIsNew(1);
        bean.setIsRead(1);
        bean.setServerId(severId);
        // 因名片分享title在转发时仍需要，故此处保存下
        if (type == FileTaskManager.NOTICE_TYPE_VCARD_SEND
            || type == FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND) {
            bean.setTitle(title);
        }

        long curtime = System.currentTimeMillis();
        // TODO:此处的是接收时间需要转换一下
        long serTime = 0;
        try {
            if (!TextUtils.isEmpty(sendTime)) {
                serTime = Long.parseLong(sendTime);
            } else {
                serTime = curtime;
            }
        } catch (NumberFormatException e) {
            serTime = curtime;
        }
        bean.setSendTime(serTime);
        bean.setReceivedTime(serTime);
        if (TextUtils.isEmpty(groupid)) {
            // 普通的点对点消息
        } else {
            bean.setThreadsId(groupid);
        }
        // 下面两个字段不再维护
        bean.setMsgId("");
        bean.setFailReplyId("");

        if (!TextUtils.isEmpty(extinfo)) {
            try {
                JSONObject extObj = new JSONObject(extinfo);
                extObj.put("id", extObj.optString("id"));
                extObj.put("text", extObj.optString("text"));
                extObj.put("ver", extObj.optString("ver"));
                bean.setExtInfo(extObj.toString());
            } catch (JSONException e) {
                CustomLog.e(TAG, "JSONException" + e.toString());
                e.printStackTrace();
                bean.setExtInfo("");
            }
        } else {
            bean.setExtInfo("");
        }

        String uuid = insertNotice(bean);
        return uuid;
    }


    public void createAddFriendMsgTip(String targetShortName, String targetNubeNumber) {
        String tmpStr = targetShortName
            + "已经是你的好友了，可以开始聊天了";
        createAddFriendTxt("", targetNubeNumber, AccountManager.getInstance(mcontext).getNube(),
            null, "",
            FileTaskManager.NOTICE_TYPE_DESCRIPTION, tmpStr, "", null
            , System.currentTimeMillis() + "");
    }


    //创建添加好友插入的默认消息
    //本函数有两个使用场景1、主动插入的一句文本（不带时间戳 sendTime为空）
    //2、接收到的群组事件消息（带有服务器的时间戳sendTime不为空）
    //作为场景2，为配合会话详情中的排序
    //receivedTime 和 sendTime 一致
    //如createReceiveMsgNotice函数中一致
    //若是场景1，receivedTime 和 sendTime 更需要一致
    public String createAddFriendTxt(String msgid, String receiver, String sender,
                                     List<String> localfiles, String title, int type, String txt,
                                     String threadid, Object extInfo, String sendTime) {
        String body = "";
        // sort 接收对象的视频号
        String recipentIds = StringUtil.sortRecipentIds(receiver, ';');
        CustomLog.d(TAG, "after sorted recipentIds:" + recipentIds + " txt:" + txt);
        JSONArray array = new JSONArray();
        JSONObject object = null;
        object = new JSONObject();
        try {
            object.put("txt", txt);
            array.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        body = array.toString();
        NoticesBean bean = new NoticesBean();
        bean.setReciever(recipentIds);
        bean.setBody(body);
        bean.setType(type);
        bean.setTitle(title);
        bean.setSender(sender);

        long curtime = System.currentTimeMillis();
        // bean.setReceivedTime(curtime);
        // TODO:此处的是接收时间需要转换一下
        long serTime = 0;
        Cursor cursor = null;
        try {
            cursor = queryAllNotice(threadid);
            if (cursor != null && cursor.moveToLast()) {
                try {
                    if (!TextUtils.isEmpty(sendTime)) {
                        serTime = Long.parseLong(sendTime);
                    } else {
                        serTime = cursor
                            .getLong(cursor
                                .getColumnIndex(NoticesTable.NOTICE_COLUMN_SENDTIME)) + 10;
                        if (serTime < curtime) {
                            // 为了防止最新一条消息的时间比当前时间小很多，这样导致在会话列表里面排序
                            //会出现问题可能没有出现在最上面；所以变为当前时间
                            serTime = curtime;
                        }
                    }
                } catch (NumberFormatException e) {
                    serTime = cursor
                        .getLong(cursor
                            .getColumnIndex(NoticesTable.NOTICE_COLUMN_SENDTIME)) + 10;
                    if (serTime < curtime) {
                        // 为了防止最新一条消息的时间比当前时间小很多，这样导致在会话列表里面排序
                        //会出现问题可能没有出现在最上面；所以变为当前时间
                        serTime = curtime;
                    }
                }
            } else {
                // 为了修改新建会话时，服务端时间早于本地时间出现消息不能刷新显示问题，直接初始值为0
                //  修改时间为1，出现查找时候初始时间为0，这样会出现查找显示问题，现在修改为1
                serTime = 1;
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "queryAllNotice 查询异常" + e.toString());
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        bean.setSendTime(serTime);
        if (serTime == 1) {
            bean.setReceivedTime(curtime);
        }
        String uuid = msgid;
        if (TextUtils.isEmpty(msgid)) {
            uuid = StringUtil.getUUID();
        }
        bean.setId(uuid);
        if (TextUtils.isEmpty(threadid)) {
        } else {
            bean.setThreadsId(threadid);
            if (serTime == 1) {
            } else {
            }
        }
        // 下面两个字段不再维护
        bean.setMsgId("");
        bean.setFailReplyId("");

        JSONObject extObj = new JSONObject();
        try {
            extObj.put("id", bean.getId());
            extObj.put("text", "");
            extObj.put("ver", BizConstant.MSG_VERSION);
            extObj.put("medicalComboMsg", 1);
        } catch (JSONException e) {
            CustomLog.d(TAG, "JSONException" + e.toString());
            e.printStackTrace();
        }
        bean.setExtInfo(extObj.toString());
        uuid = insertNotice(bean);
        return uuid;
    }

}
