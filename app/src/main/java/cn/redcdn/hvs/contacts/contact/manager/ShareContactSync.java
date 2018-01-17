package cn.redcdn.hvs.contacts.contact.manager;

import android.accounts.Account;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.redcdn.datacenter.hpucenter.HPUGetShareContactList;
import cn.redcdn.datacenter.hpucenter.data.ContactInfo;
import cn.redcdn.datacenter.hpucenter.data.ShareContactInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.contacts.contact.butelDataAdapter.ContactSetImp;
import cn.redcdn.hvs.contacts.contact.database.DBConf;
import cn.redcdn.hvs.contacts.contact.hpucontact.DtNoticesTable;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.dao.DtNoticesDao;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.provider.HVSProvider;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.meeting.util.CommonUtil;
import cn.redcdn.log.CustomLog;

/**
 * Created by caizx on 2017/11/21.
 */

public class ShareContactSync extends Thread {

    private  String TAG = ShareContactSync.class.getSimpleName();

    private Context context;
    private  boolean isSync;
    private List<IContactListChanged> iContactListChanged = null;
    private boolean isNeedNotify = false;
    private String table;
    private List<ContactInfo> insertList = new ArrayList<>();
    private List<String> deletList = new ArrayList<>();
    private List<ContactInfo> updateList = new ArrayList<>();

    private int state;
    private final int INIT_STATE = 0;
    private final int SYNC_STATE = 1;
    private final int STOP_STATE = 2;
    private long updateTime = 0;
    private int num = 0;
    private Uri uri = null;
    private int enDtid = 2;

    @Override
    public void run() {
        super.run();
        if(state == INIT_STATE){
                doDataSync();
        }
    }

    public void init(Context context, boolean isSync,
                     List<IContactListChanged> iContactListChanged, String table) {
        this.context = context;
        this.isSync = isSync;
        this.iContactListChanged = iContactListChanged;
        this.table = table;
        isNeedNotify = false;
        state = INIT_STATE;
        uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI, table);
    }

    public void doDataSync(){
        updateTime = getMaxtimeFromDB();
        num = ContactManager.getInstance(context).getDTListFromData().size();
        state = SYNC_STATE;
       doDownloadData();
    }

    /**
     * 调用接口下载数据
     */
    private  void doDownloadData(){
        try {
            try {
                HPUGetShareContactList getShareContactList = new HPUGetShareContactList() {
                };
                ShareContactInfo contactInfo = getShareContactList.getlist(AccountManager.getInstance(context).getMdsToken(), updateTime, num);
                if (contactInfo.code == -1) {
                    CustomLog.d(TAG, "获取共享通讯录 返回数据为空");
                    
                } else if (contactInfo.code == 0) {
                    CustomLog.d(TAG, "获取共享通讯录成功");
                    if (contactInfo.contactInfos == null) {
                        CustomLog.e(TAG, "获取共享通讯录成员列表为空");
                    } else {
                        if (contactInfo.dtFlag != 0) {
                            enDtid = contactInfo.dtFlag;
                        }
                        compareContat(contactInfo.contactInfos, getAllShareContacts());
                    }
                } else {
                    // TODO: 2017/11/28  这种情况 需要延迟请求？
                    CustomLog.e(TAG, "获取共享通讯录 失败 错误码 status = " + contactInfo.code + " 错误信息 msg = " + contactInfo.msg);
                }
            } catch (Exception e) {
                CustomLog.e(TAG, "获取共享通讯录 异常 " + e.getMessage());
                CustomLog.d(TAG, "延迟三分钟执行 共享通讯录 同步逻辑!");
                Thread.currentThread().sleep(180000);
                CustomLog.d(TAG, "重新执行共享通讯录 同步逻辑!");
                doDownloadData();
            }
        }catch (Exception e){
            CustomLog.e(TAG, "获取共享通讯录 异常  " + e.getMessage());
        }
    }

    /**
     * 获取本地医联体数据库 所有数据
     * @return
     */
    private Map<String,Contact> getAllShareContacts(){
        String sql = "select * from "+table;
        Map<String,Contact> info = new HashMap<String,Contact>();
        List<Contact> contacts = new ArrayList<>();
        if (AccountManager.getInstance(context).getLoginState() == AccountManager.LoginState.ONLINE){
            Cursor cursor = ContactDBOperater.getInstance(context).queryAllContacts(sql,table);
            if (cursor!=null){
                while (cursor.moveToNext()){
                    info.put(getDataFromCursor(cursor).getNubeNumber(),getDataFromCursor(cursor));
                }
            }
        }
        return info;
    }

    /**
     * 对比本地数据库文件进行 添加 删除 更新操作
     * @param infos
     * @param contacts
     */
    private void compareContat(List<ContactInfo> infos,Map<String,Contact> contacts){
        //// TODO: 2017/11/23 判断如果账号有关联的医联体 则往下执行
        if(enDtid == 2){
            if (contacts.size()>0&&infos.size()>0&&state != STOP_STATE){
                //删除数据
                if (deleteContact() == 0){
                    insertList.addAll(infos);
                }
            }else if (infos.size() == 0 && contacts.size()>0){
                AccountManager.getInstance(context).hpuList = ContactManager.getInstance(context).getDTListFromData();
                context.getContentResolver().notifyChange(uri, null);
            }else if(infos.size()>0&&contacts.size() == 0){
                //添加数据
                insertList.addAll(infos);
            }
        }else{
            deleteContact();
        }
      if (insertList!=null&&insertList.size()>0){
          insertContact(insertList);
      }
    }

    /**
     * 添加数据到共享通讯录数据库
     * @param list
     */
    private void insertContact(List<ContactInfo> list){

        List<ContentValues> insertValues = new ArrayList<>();
        for (ContactInfo info:list){
            ContentValues values = new ContentValues();
             values.put(DBConf.PHUID,info.dtId);
             values.put(DBConf.PHUNAME,info.dtName);
             values.put(DBConf.PHUNICKNAME,info.nickName);
             values.put(DBConf.NUBENUMBER,info.nube);
             values.put(DBConf.PICURL,info.headThumUrl);
             values.put(DBConf.WORK_UNIT,info.workUnit);
             values.put(DBConf.DEPARTMENT,info.department);
             values.put(DBConf.UPDATETIME,info.updateTime);
            insertValues.add(values);
        }
        if(AccountManager.getInstance(context).getLoginState() == AccountManager.LoginState.ONLINE){
            try{
                if(insertValues.size()>0){
                    long result = ContactDBOperater.getInstance(context).applyInsertBatchlogic(table,insertValues);
                    if(result == 0){
                        if(result==0){
                            CustomLog.d(TAG, "insertContacts result==0");
                           //添加数据库成功
                            AccountManager.getInstance(context).hpuList = ContactManager.getInstance(context).getDTListFromData();
                          context.getContentResolver().notifyChange(uri, null);
                        }
                        CustomLog.d(TAG, "insertContacts success");
                    }
                }
            }catch (Exception e){
                CustomLog.e(TAG, "insertShareContacts Exception " + e);
            }
        }
    }

    /**
     * 物理删除联系人
     * @return
     */
    private long deleteContact(){

        long recount = -1;
        if (AccountManager.getInstance(context).getLoginState() == AccountManager.LoginState.ONLINE){
            try{
                String sql = "delete from "+table;
                long result = ContactDBOperater.getInstance(context).deleteAll(sql,table);
                if(result==0){
                    CustomLog.d(TAG, "deleteShateContacts result==0");
                    // TODO: 2017/11/23 添加如果enDtid 表示该联系人没有所属医联体 执行监听 否则不执行
                    if (enDtid == 1){
                        AccountManager.getInstance(context).hpuList = ContactManager.getInstance(context).getDTListFromData();
                      context.getContentResolver().notifyChange(uri, null);
                    }
                }
                CustomLog.d(TAG, "物理删除共享联系人 success");
               recount = result;
            }catch (Exception e){
                 CustomLog.e(TAG, "物理删除联系人失败   Exception " + e);
                return  -1;
            }
        }
        return recount;
    }

    /**
     * 获取本地数据库中联系人数据中更新时间最大的
     * @return
     */
    private long getMaxtimeFromDB(){
        Map<String,Contact> info = getAllShareContacts();
        long time = 0;
        if(info != null&&info.size() != 0) {
            for (String key : info.keySet()) {
                Contact tmpInfo = info.get(key);
                if (tmpInfo.getUpdateTime()>time){
                    time = tmpInfo.getUpdateTime();
                }
            }
        }
        return time;
    }

    /**
     * 将数据库对象 转换为ContactInfo
     * @param cursor
     * @return
     */
   private Contact getDataFromCursor(Cursor cursor){
      Contact contact = new Contact();
      contact.setPhuId(cursor.getString(cursor.getColumnIndex(DBConf.PHUID)));
      contact.setPhuName(cursor.getString(cursor.getColumnIndex(DBConf.PHUNAME)));
      contact.setNubeNumber(cursor.getString(cursor.getColumnIndex(DBConf.NUBENUMBER)));
      contact.setNickname(cursor.getString(cursor.getColumnIndex(DBConf.PHUNICKNAME)));
      contact.setHeadUrl(cursor.getString(cursor.getColumnIndex(DBConf.PICURL)));
      contact.setWorkUnit(cursor.getString(cursor.getColumnIndex(DBConf.WORK_UNIT)));
      contact.setDepartment(cursor.getString(cursor.getColumnIndex(DBConf.DEPARTMENT)));
      contact.setUpdateTime(cursor.getLong(cursor.getColumnIndex(DBConf.UPDATETIME)));
      return  contact;
  }

    public void cancle() {
        state = STOP_STATE;
        isSync = false;
        isNeedNotify = false;
        iContactListChanged = null;
    }

}
