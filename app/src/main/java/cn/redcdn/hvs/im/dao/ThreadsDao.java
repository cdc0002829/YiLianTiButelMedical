package cn.redcdn.hvs.im.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.im.bean.ThreadsBean;
import cn.redcdn.hvs.im.column.ThreadsTable;
import cn.redcdn.hvs.util.StringUtil;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class ThreadsDao {

    private final String TAG = "ThreadsDao";

    private static String[] select_columns = {
            ThreadsTable.THREADS_COLUMN_ID,
            ThreadsTable.THREADS_COLUMN_CREATETIME,
            ThreadsTable.THREADS_COLUMN_LASTTIME,
            ThreadsTable.THREADS_COLUMN_TYPE,
            ThreadsTable.THREADS_COLUMN_RECIPIENTIDS,
            ThreadsTable.THREADS_COLUMN_TOP,
            ThreadsTable.THREADS_COLUMN_EXTENDINFO,
            ThreadsTable.THREADS_COLUMN_RESERVERSTR1,
            ThreadsTable.THREADS_COLUMN_RESERVERSTR2 };

    private Context mcontext = null;

    public ThreadsDao(Context context) {
        this.mcontext = context;
    }

    /**
     * 此处关联notice表，查询出所有未置顶的会话信息
     * @return
     */
    public Cursor getAllThreadsInfo() {

        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
                "NETPHONE_THREADS_GETALL");
        try {
            return mcontext.getContentResolver().query(uri, null, null, null,
                    null);
        } catch (Exception e) {
            CustomLog.d("THREADS:", "query Exception error" + e.toString());

        }
        CustomLog.d("THREADS:", "query result is null");
        return null;
    }

    /**
     * 此处关联notice表，查询出所有置顶的会话信息
     * @return
     */
    public Cursor getAllThreadsInfoTop() {

        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
                "NETPHONE_THREADS_GETALL_TOP");
        try {
            return mcontext.getContentResolver().query(uri, null, null, null,
                    null);
        } catch (Exception e) {
            CustomLog.d("THREADS:", "query Exception error" + e.toString());

        }
        CustomLog.d("THREADS:", "query result is null");
        return null;
    }

    /**
     * 根据参与者的id(不含有自己的id)，查询对应的会话信息
     * @param numbers 参与者的id(不含有自己的id)，多个ID用分号分割
     * @return 若有会话则返回该会话, 否则返回null
     */
    public ThreadsBean getThreadByRecipentIds(String numbers){
        if(TextUtils.isEmpty(numbers)){
            return null;
        }
        Cursor cursor = null;
        ThreadsBean bean = null;
        try {
            cursor = mcontext.getContentResolver()
                    .query(ProviderConstant.NETPHONE_THREADS_URI,
                            select_columns,
                            ThreadsTable.THREADS_COLUMN_RECIPIENTIDS+"= ? ",
                            new String[]{numbers},
                            null);
            if(cursor!=null&&cursor.moveToFirst()){
                bean = ThreadsTable.pureCursor(cursor);
            }
        }catch(Exception e){
            CustomLog.e("ThreadsDao","Exception" + e.toString());
        }finally{
            if(cursor!=null){
                cursor.close();
                cursor = null;
            }
        }
        return bean;
    }

    /**
     * 创建一个会话：根据参与者的id创建会话；如果已有会话则不创建,仅更新lasttime字段
     * @param numbers 参与者的id(不含有自己的id)，多个ID用分号分割
     * @param lasttime 最近的消息的接收字段  单位毫秒，当该值为空时，用当前时间
     * @param sorted   id是否已经是自然有序
     * @return  返回新创建的会话id 或 已有会话的id; 返回空字符传表示创建不成功
     */
    public String createThread(String numbers,long lasttime,boolean sorted){
        if(TextUtils.isEmpty(numbers)){
            return "";
        }
        String recipentIds = numbers;
        //增加排序功能
        if(!sorted&&numbers.contains(";")){
            String[] tempIds = numbers.split(";");
            if(tempIds!=null&&tempIds.length>1){
                List<String> idlist = new ArrayList<String>();
                for(int i=0;i<tempIds.length;i++){
                    String item = tempIds[i];
                    idlist.add(item);
                }
                Collections.sort(idlist);
                recipentIds = StringUtil.list2String(idlist, ';');
            }
        }

        String id = getThreadIDByRecipentIds(recipentIds);

        if(TextUtils.isEmpty(id)){
            id = StringUtil.getUUID();
            long curtime = System.currentTimeMillis();
            ThreadsBean bean = new ThreadsBean();
            bean.setId(id);
            bean.setCreateTime(curtime);
            bean.setLastTime(lasttime<=0?curtime:lasttime);
            bean.setRecipientIds(recipentIds);
            bean.setExtendInfo("");
            bean.setReserverStr1("0");
            bean.setReserverStr2("");
            bean.setType(ThreadsTable.TYPE_SINGLE_CHAT);

//            String butelPubNubeNum = NetPhoneApplication.getPreference().getKeyValue(
//                    DaoPreference.PrefType.KEY_BUTEL_PUBLIC_NO, "");
//            if (!TextUtils.isEmpty(butelPubNubeNum) && butelPubNubeNum.equals(recipentIds)) {
//                // 官方帐号会话的场合，需置顶
//                bean.setTop(ThreadsTable.TOP_YES);
//            }

            ContentValues values = ThreadsTable.makeContentValue(bean);
            if(values==null){
                return "";
            }

            Uri url = mcontext.getContentResolver()
                    .insert(ProviderConstant.NETPHONE_THREADS_URI, values);
            if(url==null){
                return "";
            }
        }else{
            if(lasttime>0){
                boolean succ = updateLastTime(id,lasttime);
                CustomLog.d(TAG,"createThread updateLastTime succ="+succ);
            }
        }

        return id;
    }

    /**
     * 根据参与者的id(不含有自己的id)，检查是否有会话存在
     * @param numbers 参与者的id(不含有自己的id)，多个ID用分号分割
     * @return 若有会话则返回该会话的id, 否则返回空字符传
     */
    public String getThreadIDByRecipentIds(String numbers){
        if(TextUtils.isEmpty(numbers)){
            return "";
        }
        Cursor cursor = null;
        String id = "";
        try {
            cursor = mcontext.getContentResolver()
                    .query(ProviderConstant.NETPHONE_THREADS_URI,
                            select_columns,
                            ThreadsTable.THREADS_COLUMN_RECIPIENTIDS+"= ? ",
                            new String[]{numbers},
                            null);
            if(cursor!=null&&cursor.moveToFirst()){
                id = cursor.getString(0);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(cursor!=null){
                cursor.close();
                cursor = null;
            }
        }
        return id;
    }

    /**
     * 根据id获得对应的ThreadsBean
     * @param id  会话的id
     * @return 返回ThreadsBean对象;值可能是null
     */
    public ThreadsBean getThreadById(String id){
        if(TextUtils.isEmpty(id)){
            return null;
        }
        Cursor cursor = null;
        ThreadsBean bean = null;
        try {
            cursor = mcontext.getContentResolver()
                    .query(ProviderConstant.NETPHONE_THREADS_URI,
                            select_columns,
                            ThreadsTable.THREADS_COLUMN_ID +"= ? ",
                            new String[]{id},
                            null);
            if(cursor!=null&&cursor.moveToFirst()){
                bean = ThreadsTable.pureCursor(cursor);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(cursor!=null){
                cursor.close();
                cursor = null;
            }
        }
        return bean;
    }

    /**
     * 根据会话ID，更新会话中的lasttime字段
     * @param id 会话id
     * @param lasttime 最近的消息的接收字段  单位毫秒，
     * @return true:更新成功
     */
    public boolean updateLastTime(String id,long lasttime){
        if(TextUtils.isEmpty(id)){
            return false;
        }
        if(lasttime<=0){
            return false;
        }
        ThreadsBean bean = getThreadById(id);
        if(bean==null){
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(ThreadsTable.THREADS_COLUMN_LASTTIME, lasttime);
        int count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_THREADS_URI,
                values,
                ThreadsTable.THREADS_COLUMN_ID +" = ?",
                new String[]{id});
        if(count>0){
            return true;
        }
        return false;
    }

    /**
     * 根据会话ID，更新会话中的top,topTime字段
     * @param id 会话id
     * @param top 消息是否置顶， 0：不置顶，1：置顶
     * @return true:更新成功
     */
    public boolean updateTop(String id,int top){
        if(TextUtils.isEmpty(id)){
            return false;
        }
        if(top<0){
            return false;
        }
        ThreadsBean bean = getThreadById(id);
        if(bean==null){
            return false;
        }
        String topTime = "";
        if(top == ThreadsTable.TOP_YES){
            topTime = String.valueOf(System.currentTimeMillis());
        }
        ContentValues values = new ContentValues();
        values.put(ThreadsTable.THREADS_COLUMN_TOP, top);
        values.put(ThreadsTable.THREADS_COLUMN_RESERVERSTR2,topTime);
        int count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_THREADS_URI,
                values,
                ThreadsTable.THREADS_COLUMN_ID +" = ?",
                new String[]{id});
        if(count>0){
            return true;
        }
        return false;
    }

    /**
     * 根据会话ID，更新会话中的免打扰字段
     * @param id 会话id
     * @param reserverStr1 消息是否免打扰 "0"：打扰，"1"：不打扰
     * @return true:更新成功
     */
    public boolean updateDoNotDisturb(String id,String reserverStr1){
        if(TextUtils.isEmpty(id)){
            return false;
        }
        if(reserverStr1.length() == 0){
            return false;
        }
        ThreadsBean bean = getThreadById(id);
        if(bean==null){
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(ThreadsTable.THREADS_COLUMN_RESERVERSTR1, reserverStr1);
        int count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_THREADS_URI,
                values,
                ThreadsTable.THREADS_COLUMN_ID +" = ?",
                new String[]{id});
        if(count>0){
            return true;
        }
        return false;
}




    public boolean isExistThread(String id){
        if(TextUtils.isEmpty(id)){
            return false;
        }
        Cursor cursor = null;
        boolean exist = false;
        try {
            cursor = mcontext.getContentResolver()
                    .query(ProviderConstant.NETPHONE_THREADS_URI,
                            select_columns,
                            ThreadsTable.THREADS_COLUMN_ID +"= ? ",
                            new String[]{id},
                            null);
            if(cursor!=null&&cursor.moveToFirst()){
                exist =  true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(cursor!=null){
                cursor.close();
                cursor = null;
            }
        }
        return exist;
    }

    public String createThreadFromGroup(String gid){

        if(!TextUtils.isEmpty(gid)){
//			GroupDao groupDao = new GroupDao(mcontext);
//			GroupBean gbean = groupDao.getGroupInfo(gid);
//			if(gbean!=null){
            long curtime = System.currentTimeMillis();
            ThreadsBean bean = new ThreadsBean();
            bean.setId(gid);
            bean.setCreateTime(curtime);
            bean.setLastTime(curtime);
            bean.setRecipientIds(gid);
            bean.setExtendInfo("");
            bean.setReserverStr1("0");
            bean.setReserverStr2("");
            bean.setType(ThreadsTable.TYPE_GROUP_CHAT);
            bean.setTop(ThreadsTable.TOP_NO);

            ContentValues values = ThreadsTable.makeContentValue(bean);
            if(values==null){
                return "";
            }

            Uri url = mcontext.getContentResolver()
                    .insert(ProviderConstant.NETPHONE_THREADS_URI, values);
            if(url==null){
                return "";
            }
//			}
        }

        return gid;
    }

    /**
     * 根据会话id,删除会话和会话下的消息，
     * 以及异步线程删除该会话中收到、下载的图片、视频、音频等
     * （删除文件功能暂不提供）
     * @param id 会话id
     * @return >0 删除成功；否则 表示删除失败或因参数异常没有执行删除
     */
    public int deleteThread(String id){
        if(TextUtils.isEmpty(id)){
            return -1;
        }
        int count = 0;
        //删除会话的所有消息
        NoticesDao msgDao = new NoticesDao(mcontext);
        count = msgDao.deleteAllNoticesInConversation(id);
        CustomLog.d(TAG,"deleteThread deleteAllNoticesInConversation count="+count);
        // 删除会话
        count = mcontext.getContentResolver().delete(
                ProviderConstant.NETPHONE_THREADS_URI,
                ThreadsTable.THREADS_COLUMN_ID +" = ?",
                new String[]{id});
        CustomLog.d(TAG,"deleteThread count="+count);
        // TODO：线程删除该会话下的下载的图片、视频、声音文件等（暂不实现）
        return count;
    }

    /**
     * 根据视频号ID，更新会话中的extendinfo字段中json:draftText的值
     * 如果相关视频号还没有对应的会话，先创建会话，在更新草稿
     * @param numbers 参与者的id(不含有自己的id)，多个ID用分号分割
     * @param draft 文字草稿，可以是空值，但不能为NULL
     * @return true：保存成功
     *         false:没有保存或保存失败
     */
    public  boolean saveDraft(String numbers,String draft){
        if(TextUtils.isEmpty(numbers)){
            return false;
        }
        if(draft == null){
            return false;
        }
        String id = createThread(numbers, 0,false);
        return saveDraftById(id,draft);
    }

    /**
     * 根据会话ID，更新会话中的extendinfo字段中json:draftText的值
     * @param id  会话的id
     * @param draft 文字草稿, 可以是空值，但不能是NULL
     * @return true：保存成功
     *         false:没有保存或保存失败
     */
    public boolean saveDraftById(String id,String draft){
        if(TextUtils.isEmpty(id)){
            return false;
        }
        if(draft == null){
            return false;
        }

        ThreadsBean bean = getThreadById(id);
        if(bean==null){
            return false;
        }
        String extendinfo = bean.updateExtendInfoDraft(draft);
        ContentValues values = new ContentValues();
        values.put(ThreadsTable.THREADS_COLUMN_EXTENDINFO, extendinfo);
        int count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_THREADS_URI,
                values,
                ThreadsTable.THREADS_COLUMN_ID +" = ?",
                new String[]{id});
        if(count>0){
            return true;
        }
        return false;
    }

}
