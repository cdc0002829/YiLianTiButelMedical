package cn.redcdn.hvs.im.dao;

/**
 * Created by guoyx on 2017/2/25.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.bean.CollectionBean;
import cn.redcdn.hvs.im.bean.CollectionEntity;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.column.CollectionTable;
import cn.redcdn.hvs.im.column.NubeFriendColumn;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.manager.CollectionManager;
import com.butel.connectevent.utils.LogUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import cn.redcdn.hvs.im.preference.DaoPreference.PrefType;
import cn.redcdn.hvs.im.provider.HVSProvider;


/**
 *
 * @ClassName: CollectionDao
 * @Description:收藏分享dao
 * @author niuben
 * @Company:北京红云融通技术有限公司
 * @date 2016-5-9  09:33
 */

public class CollectionDao {

    private static final String[] select_columns = {
            CollectionTable.ID,
            CollectionTable.OPERATETIME,
            CollectionTable.STATUS,
            CollectionTable.SYNCSTATUS,
            CollectionTable.OPERATORNUBE,
            CollectionTable.TYPE,
            CollectionTable.BODY,
            CollectionTable.EXTINFO};

    private Context mcontext = null;

    public CollectionDao(Context context) {
        this.mcontext = context;
    }

    public CollectionEntity getCollectionEntityById(String id){
        Cursor cursor = query(CollectionTable.ID, id);
        CollectionEntity entity=null;
        try {
            if (cursor != null&&cursor.getCount()>0) {
                if (cursor.moveToFirst()) {
                    entity = cursor2Entity(cursor);
                }
            }
        } catch (Exception e) {
            LogUtil.e("Exception", e);
        }
        if (cursor != null) {
            cursor.close();
        }
        return entity;
    }

    /**
     * @param Id
     * @param body
     * @return 根据ID更新SyncStatus的值
     */
    public int upDateBodyById(String Id,String body){
        ContentValues updateValues=new ContentValues();
        updateValues.put(CollectionTable.BODY, body);
        return update(CollectionTable.ID, Id, updateValues);
    }

    /**
     * 获取所有需要同步的数据
     * @return
     */
    public List<CollectionEntity> getAllSyncRecord(){
        Cursor cursor=query(CollectionTable.SYNCSTATUS, CollectionEntity.SYNCSTATUS_NEED+"");
        List<CollectionEntity> mbeans= new ArrayList<CollectionEntity>();
        if (cursor!=null&&cursor.getCount()>0){
            if (cursor.moveToFirst()){
                do{
                    CollectionEntity bean=cursor2Entity(cursor);
                    mbeans.add(bean);
                }while (cursor.moveToNext());
            }
        }
        //关闭cursor
        if (cursor!=null){
            cursor.close();
        }
        return mbeans;
    }

    public int removeCollectionById(String id){
        ContentValues updateValues=new ContentValues();
        updateValues.put(CollectionTable.SYNCSTATUS, CollectionEntity.SYNCSTATUS_NEED);
        updateValues.put(CollectionTable.STATUS, CollectionEntity.STATUS_INVALID);
        return update(CollectionTable.ID, id, updateValues);
    }

    /**
     *
     * @param id
     * @param SyncStatus
     * @param operatorTime
     * @return
     */
    public int upDateDateAfterSyncSuccessById(String id,int SyncStatus,long operatorTime){
        ContentValues updateValues=new ContentValues();
        updateValues.put(CollectionTable.SYNCSTATUS, SyncStatus);
        updateValues.put(CollectionTable.OPERATETIME, operatorTime);
        return update(CollectionTable.ID, id, updateValues);
    }

    /**
     *
     * @param Id
     * @param SyncStatus
     * @return 根据ID更新SyncStatus的值
     */
    public int upDateSyncStatusById(String Id,int SyncStatus){
        ContentValues updateValues=new ContentValues();
        updateValues.put(CollectionTable.SYNCSTATUS, SyncStatus);
        return update(CollectionTable.ID, Id, updateValues);
    }


    private String getSelfNube() {
        return MedicalApplication.getPreference().getKeyValue(PrefType.LOGIN_NUBENUMBER, "");
    }
    private String getSelfMobile() {
        return MedicalApplication.getPreference().getKeyValue(PrefType.LOGIN_MOBILE, "");
    }
    private String getSelfNickName() {
        return MedicalApplication.getPreference().getKeyValue(PrefType.USER_NICKNAME, "");
    }
    private String getSelfHeadUrl() {
        return MedicalApplication.getPreference().getKeyValue(PrefType.USER_HEAD_ICON_URL, "");
    }

    /**
     * 获取所有有效记录,且已知类型的收藏（图片，文字，链接、视频、文件、语音）
     * @return
     */
    public List<CollectionBean> getAllCollectionRecord(){
        String nube=getSelfNube();
        String mobile=getSelfMobile();
        String nickName=getSelfNickName();
        String headUrl=getSelfHeadUrl();
        Cursor cursor=getAllRecordsCursor();
        List<CollectionBean> mbeans= new ArrayList<CollectionBean>();
        if (cursor!=null&&cursor.getCount()>0){
            if (cursor.moveToFirst()){
                do{
                    if (cursor.getInt(cursor.getColumnIndexOrThrow(CollectionTable.STATUS))==CollectionEntity.STATUS_EFFECTIVE){
                        CollectionBean bean=new CollectionBean();
                        bean.setId(cursor.getString(cursor.getColumnIndexOrThrow(CollectionTable.ID)));
                        bean.setOperateTime(cursor.getLong(cursor.getColumnIndexOrThrow(CollectionTable.OPERATETIME)));
                        bean.setOperatorNube(cursor.getString(cursor.getColumnIndexOrThrow(CollectionTable.OPERATORNUBE)));

                        if (nube.equals(bean.getOperatorNube())){
                            bean.setOperatorMobile(mobile);
                            bean.setOperatorNickname(nickName);
                            bean.setOperatorName("");
                            bean.setOperatorHeadUrl(headUrl);
                        }else {
                            bean.setOperatorMobile(cursor.getString(cursor.getColumnIndexOrThrow(NubeFriendColumn.NUMBER)));
                            bean.setOperatorNickname(cursor.getString(cursor.getColumnIndexOrThrow(NubeFriendColumn.NICKNAME)));
                            bean.setOperatorName(cursor.getString(cursor.getColumnIndexOrThrow(NubeFriendColumn.NAME)));
                            bean.setOperatorHeadUrl(cursor.getString(cursor.getColumnIndexOrThrow(NubeFriendColumn.HEADURL)));
                        }

                        bean.setType(cursor.getInt(cursor.getColumnIndexOrThrow(CollectionTable.TYPE)));
                        bean.setBody(cursor.getString(cursor.getColumnIndexOrThrow(CollectionTable.BODY)));
                        bean.setExtinfo(cursor.getString(cursor.getColumnIndexOrThrow(CollectionTable.EXTINFO)));
                        if (FileTaskManager.NOTICE_TYPE_AUDIO_SEND==bean.getType()||
                                FileTaskManager.NOTICE_TYPE_TXT_SEND==bean.getType()||
                                FileTaskManager.NOTICE_TYPE_VEDIO_SEND==bean.getType()||
                                FileTaskManager.NOTICE_TYPE_PHOTO_SEND==bean.getType()||
                                FileTaskManager.NOTICE_TYPE_URL==bean.getType()||
                                FileTaskManager.NOTICE_TYPE_FILE==bean.getType()){
                            mbeans.add(bean);
                        }else {
                            LogUtil.d("未知类型"+bean.getType());
                        }
                    }
                }while (cursor.moveToNext());
            }
        }
        //关闭cursor
        if (cursor!=null){
            cursor.close();
        }
        return mbeans;
    }

    private Cursor getAllRecordsCursor(){
        Cursor cursor;
        try {
            cursor = mcontext.getContentResolver().query(HVSProvider.URI_QUERY_ALL_COLLECTION_RECORD, null, null, null, null);
        } catch (Exception e) {
            LogUtil.e("Exception", e);
            return null;
        }
        return cursor;
    }


    /**
     * @param  bean //NoticesBean
     * @return -1 插入失败，0插入成功，1已有该记录
     */
    public int addCollectionRecordByNoticesBean(NoticesBean bean){
        if (bean==null){
            return -1;
        }

        CollectionEntity localEntity = getCollectionEntityById(bean.getId());
        if (localEntity!=null){
            if (CollectionEntity.STATUS_EFFECTIVE == localEntity.getStatus()){
                // 已收藏的场合，更新本地收藏时间，不需要同步到服务端
                ContentValues updateValues = new ContentValues();
                updateValues.put(CollectionTable.OPERATETIME,
                        System.currentTimeMillis()/1000);
                update(CollectionTable.ID, bean.getId(), updateValues);
                return 1;
            }else {
                upDateStatusById(bean.getId(),CollectionEntity.STATUS_EFFECTIVE);
                return 0;
            }
        }

        CollectionEntity entity=new CollectionEntity();
        entity.setId(bean.getId());
        entity.setOperateTime(System.currentTimeMillis()/1000);//待收藏成功后回写该时间
        entity.setStatus(CollectionEntity.STATUS_EFFECTIVE);
        entity.setSyncStatus(CollectionEntity.SYNCSTATUS_NEED);
        entity.setOperatorNube(bean.getSender());
        entity.setType(bean.getType());

        // added by zhaguitao on 20160622 for 收藏消息时，须将消息body中的key（thumbnail）转换成收藏body中的key（thumbnailRemoteUrl）
        // 本来android这边收藏也用thumbnail就好了，但ios客户端不愿意转成thumbnail，所以为了适配ios客户端，android客户端转换
        try {
            Map<String, String> keys = new HashMap<String, String>();
            keys.put("thumbnail", "thumbnailRemoteUrl");
            keys.put("width", "photoWidth");
            keys.put("height", "photoHeight");
            entity.setBody(CollectionManager.modifyBodyJsonKey(new JSONArray(bean.getBody()), keys));
        } catch (JSONException e) {
            LogUtil.e("", e);
        }

        // 不需要保存extInfo，选择收藏发送消息时，会重新生成extInfo
//		entity.setExtinfo(bean.getExtInfo());
        insert(entity2ContentValue(entity));
        return 0;
    }

    /**
     *
     * @param Id
     * @param status
     * @return 根据ID更新SyncStatus的值
     */
    public int upDateStatusById(String Id,int status){
        ContentValues updateValues=new ContentValues();
        updateValues.put(CollectionTable.STATUS, status);
        return update(CollectionTable.ID, Id, updateValues);
    }

    public void insert(CollectionEntity entity){
        insert(entity2ContentValue(entity));
    }

    public int update(CollectionEntity entity){
        return update(CollectionTable.ID, entity.getId(), entity2ContentValue(entity));
    }


    public int deleteRecordById(String Id){
        return delete(CollectionTable.ID, Id);
    }

    private ContentValues entity2ContentValue(CollectionEntity entity) {
        if (entity == null) {
            return null;
        }
        ContentValues value = new ContentValues();
        value.put(CollectionTable.ID,entity.getId());
        value.put(CollectionTable.OPERATETIME,entity.getOperateTime());
        value.put(CollectionTable.STATUS,entity.getStatus());
        value.put(CollectionTable.SYNCSTATUS,entity.getSyncStatus());
        value.put(CollectionTable.OPERATORNUBE,entity.getOperatorNube());
        value.put(CollectionTable.TYPE,entity.getType());
        value.put(CollectionTable.BODY,entity.getBody());
        value.put(CollectionTable.EXTINFO,entity.getExtinfo());
        return value;
    }

    private CollectionEntity cursor2Entity(Cursor cursor) {
        if (cursor == null || cursor.isClosed()||cursor.getCount()==0) {
            return null;
        }
        CollectionEntity entity=new CollectionEntity();
        try {
            entity.setId(cursor.getString(cursor.getColumnIndexOrThrow(CollectionTable.ID)));
            entity.setOperateTime(cursor.getLong(cursor.getColumnIndexOrThrow(CollectionTable.OPERATETIME)));
            entity.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(CollectionTable.STATUS)));
            entity.setSyncStatus(cursor.getInt(cursor.getColumnIndexOrThrow(CollectionTable.SYNCSTATUS)));
            entity.setOperatorNube(cursor.getString(cursor.getColumnIndexOrThrow(CollectionTable.OPERATORNUBE)));
            entity.setType(cursor.getInt(cursor.getColumnIndexOrThrow(CollectionTable.TYPE)));
            entity.setBody(cursor.getString(cursor.getColumnIndexOrThrow(CollectionTable.BODY)));
            entity.setExtinfo(cursor.getString(cursor.getColumnIndexOrThrow(CollectionTable.EXTINFO)));
        } catch (Exception e) {
            LogUtil.e("Exception", e);
            return null;
        }
        return entity;
    }


    /**
     * 插入数据库
     * @param values
     */
    private Uri insert(ContentValues values){
        if (values==null){
            return null;
        }
        Uri uri=null;
        try {
            uri = mcontext.getContentResolver().insert(CollectionTable.URI, values);
        }catch (Exception e){
            LogUtil.e("Exception",e);
        }
        return uri;
    }


    /**
     * 当key值为value的查询数据
     * @param key CollectionTable的属性
     * @param value CollectionTable的属性 对应数据库值
     * @return
     */
    private Cursor query(String key,String value){
        if (TextUtils.isEmpty(key)||TextUtils.isEmpty(value)){
            return null;
        }
        Cursor cursor=null;
        try {
            cursor = mcontext.getContentResolver().query(
                    CollectionTable.URI, select_columns,
                    key + " = ?",
                    new String[]{value}, null);
        }catch (Exception e){
            LogUtil.e("Exception",e);
        }
        return cursor;
    }

    /**
     * 当key值为value的删除数据
     * @param key CollectionTable的属性
     * @param value CollectionTable的属性 对应数据库值
     * @return
     */
    private int delete(String key,String value){
        if (TextUtils.isEmpty(key)||TextUtils.isEmpty(value)){
            return -1;
        }
        int count=0;
        try {
            count = mcontext.getContentResolver().delete(
                    CollectionTable.URI,
                    key + " = ?",
                    new String[]{value});
        }catch (Exception e){
            LogUtil.e("Exception",e);
        }
        return count;
    }

    /**
     * 当key值为value时更新数据库
     * @param key
     * @param value
     * @param updateValues
     * @return
     */
    private int update(String key,String value,ContentValues updateValues){
        if (TextUtils.isEmpty(key)||TextUtils.isEmpty(value)){
            return -1;
        }
        int count=0;
        try {
            count = mcontext.getContentResolver().update(
                    CollectionTable.URI,
                    updateValues,
                    key +" = ?",
                    new String[]{value});
        }catch (Exception e){
            LogUtil.e("Exception",e);
        }
        return count;
    }


}
