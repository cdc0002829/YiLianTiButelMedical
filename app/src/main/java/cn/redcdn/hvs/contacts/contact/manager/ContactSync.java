package cn.redcdn.hvs.contacts.contact.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import cn.redcdn.datacenter.cantacts.DownloadContactsData;
import cn.redcdn.datacenter.cantacts.UploadContacts;
import cn.redcdn.datacenter.cantacts.data.ContanctsInfo;
import cn.redcdn.datacenter.medicalcenter.MDSAppSearchUsers;
import cn.redcdn.datacenter.medicalcenter.data.MDSDetailInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.StringHelper;
import cn.redcdn.hvs.contacts.contact.butelDataAdapter.ContactSetImp;
import cn.redcdn.hvs.contacts.contact.database.DBConf;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.log.CustomLog;
import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.redcdn.keyeventwrite.KeyEventWrite.write;
import static java.lang.String.valueOf;

public class ContactSync extends Thread {
    private final String TAG = ContactSync.class.getSimpleName();
    private int state;
    private final int INIT_STATE = 0;
    private final int SYNC_STATE = 1;
    private final int STOP_STATE = 2;
    private MDSAppSearchUsers search = null;
    private DownloadContactsData downLoad = null;
    private UploadContacts upload = null;
    private Context context;
    private String table;
    private static final int DEFAULT_UPDATE_COUNT = 50;
    private int startLineNo;
    private int downLong;
    private List<ContanctsInfo> totalList = new ArrayList<>();
    private boolean isSync = false;
    private boolean isNeedNotify = false;
    private List<IContactListChanged> iContactListChanged = null;
    private  Map<String,ContanctsInfo>  timemapMaxInfoList = null;

    @Override
    public void run() {
        if (state == INIT_STATE) {
            if (isSync) {
                doDataSync();
            } else {
                getInitialData();
                doUploadDownload();
                if (isNeedNotify) {
                    notifyListner();
                }
            }
        } else {
            CustomLog.d(TAG, "do nothing state = " + state);
        }
        super.run();
    }

    private void doDataSync() {
        getInitialData();
        if (state != STOP_STATE) {
            List<List<String>> nubeList = getAllNubeNumber();
            if (nubeList != null) {
                doSearchAccount(nubeList);
            }
        }
        doUploadDownload();
        notifyListner();
        if (!RecommendManager.getInstance(context).isHasGetList) {
            RecommendManager.getInstance(context).isHasGetList = true;
            RecommendManager.getInstance(context).doGetRecommendList(0,
                    RecommendManager.FROM_BOOT);
        }
    }

    private void notifyListner() {
        if (iContactListChanged != null && iContactListChanged.size() > 0) {
            CustomLog.d(TAG, "notifyListner  iContactListChanged ");
            if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
                for (IContactListChanged listener : iContactListChanged) {
                    String sql = "select * from " + table
                            + " where isDeleted= 0 order by fullPym ";
                    Cursor cursor = ContactDBOperater.getInstance(context).rawQuery(sql,
                            table);
                    if (cursor != null) {
                        ContactSetImp imp = new ContactSetImp();
                        imp.setSrcData(cursor);
                        listener.onListChange(imp);
                    } else {
                        listener.onListChange(null);
                    }
                }
            }
        }
        state = INIT_STATE;
    }

    private void getInitialData() {
        state = SYNC_STATE;
        doDeleteData();
    }

    // TODO sync 0 lastime 0 isdelete 1 的不需要上传，直接本地删除 浪费资源
    private void doDeleteData() {
        if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
            try {
                CustomLog.d(TAG, "doDeleteData ");
                ContactDBOperater.getInstance(context).delete(table,
                        "isDeleted = 1 AND lastTime = 0 AND syncStat = 0  ", null);
            } catch (Exception e) {
                CustomLog.e(TAG, "doDeleteData " + e);
            }
        }
    }

    private void doUploadDownload() {
        if (state != STOP_STATE) {
            doUploadContact(getNeedUpdateContacts());
        }
        if (state != STOP_STATE) {
            startLineNo = 1;
            downLong = 0;
            long time = getMaxTimestamp();
            doDownloadContactsData(startLineNo, DEFAULT_UPDATE_COUNT, time, 1);
        }
    }

    private List<List<String>> getAllNubeNumber() {
        String sql = "select nubeNumber nubeNumber  from " + table
                + " tn where tn.isDeleted=0 ";
        CustomLog.d(TAG, "getAllNubeNumber " + sql);
        Cursor cursor = null;
        List<List<String>> list = null;
        List<String> item = null;
        if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
            try {
                cursor = ContactDBOperater.getInstance(context).queryAllContacts(sql,
                        table);
                if (cursor != null) {
                    list = new ArrayList<List<String>>();
                    item = new ArrayList<String>();
                    while (cursor.moveToNext()) {
                        item.add(cursor.getString(cursor.getColumnIndex("nubeNumber")));
                        if (item != null && item.size() == DEFAULT_UPDATE_COUNT) {
                            list.add(new ArrayList<String>(item));
                            item.clear();
                        }
                    }
                    if (item != null && item.size() > 0) {
                        list.add(new ArrayList<String>(item));
                        item.clear();
                    }
                    item = null;
                }
                CustomLog.d(TAG, "getAllNubeNumber " + list.size());
            } catch (Exception e) {
                CustomLog.e(TAG, "getAllNubeNumber " + e);
            }
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        return list;
    }

    private void doSearchAccount(List<List<String>> list) {
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                try {
                    if (state != STOP_STATE) {
                        if (list.get(i) != null && list.get(i).size() > 0) {
                            String[] array = new String[list.get(i).size()];

                            search = new MDSAppSearchUsers(){

                            };
                            List<MDSDetailInfo> users = search.appSearchUsersSync(
                                    AccountManager.getInstance(context).getToken(), 0, list.get(i).toArray(array));

                            if (users != null) {
                                write(KeyEventConfig.SYNC_CONTACTS_INFO_MOBILE
                                                + "_ok"
                                                + "_"
                                                + AccountManager.getInstance(context).getAccountInfo().nube);
                            } else {
                                write(KeyEventConfig.SYNC_CONTACTS_INFO_MOBILE
                                                + "_fail"
                                                + "_"
                                                + AccountManager.getInstance(context).getAccountInfo().nube
                                                + "_接口返回null");
                            }
                            if (users != null && state != STOP_STATE) {
                                compareContacts(users, getAllContacts());
                            }
                        }

                    }
                } catch (Exception e) {
                    CustomLog.e(TAG, "SearchAccount " + e);
                    write(KeyEventConfig.SYNC_CONTACTS_INFO_MOBILE
                            + "_fail" + "_"
                            + AccountManager.getInstance(context).getAccountInfo().nube
                            + "_" + e.toString());
                }
            }
        }
    }

    private void doUploadContact(List<List<ContanctsInfo>> list) {
        CustomLog.d(TAG,"doUploadContact start");
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                try {
                    if (state != STOP_STATE) {

                        try {
                            upload = new UploadContacts() {
                            };
                            List<ContanctsInfo> users = upload.uploadContactsDataSync(
                                    list.get(i), AccountManager.getInstance(context).getToken());
                            if (users != null && users.size() > 0) {
                                if (users.get(0).status == 1) {
//                                    updateContactsStatus(users, 1);
                                    CustomLog.d(TAG,"uploadContactsDataSync success,status == 1");
                                    updateContactsTimeStamp(users, 1);
                                    KeyEventWrite
                                            .write(KeyEventConfig.UPLOAD_DATA_TO_CONTACTSERVER
                                                    + "_ok"
                                                    + "_"
                                                    + AccountManager.getInstance(context).getAccountInfo().nube);
                                } else if (users.get(0).status == -1003) {
                                    CustomLog.d(TAG,"uploadContactsDataSync fail,status == -1003");
//                                    updateStatusAndDelete(users, 1);
                                    List<String> deleteList = new ArrayList<String>();
                                    List<ContanctsInfo> tmp = new ArrayList<ContanctsInfo>();
                                    for (ContanctsInfo contact:users){
                                        if(contact.isDeleted == 1){
                                            deleteList.add(contact.contactId);
                                        }
                                        tmp.add(contact);
                                    }
                                    deleteContacts(deleteList);
                                    updateStatusAndDelete(tmp,1);
                                    write(KeyEventConfig.UPLOAD_DATA_TO_CONTACTSERVER
                                                    + "_fail"
                                                    + "_"
                                                    + AccountManager.getInstance(context).getAccountInfo().nube
                                                    + "_-1003_与服务器冲突");
                                    isNeedNotify = true;
                                } else {
                                    CustomLog.d(TAG,"uploadContactsDataSync fail,status == "+users.get(0).status);
                                    write(KeyEventConfig.UPLOAD_DATA_TO_CONTACTSERVER
                                                    + "_fail"
                                                    + "_"
                                                    + AccountManager.getInstance(context).getAccountInfo().nube
                                                    + "_" + users.get(0).status);
                                }
                            }
                        } catch (Exception e){
                            CustomLog.e(TAG,"doUploadContact Exception:" + e);
                            CustomLog.d(TAG,"60秒后重新执行doUploadContact");
                            Thread.currentThread().sleep(60000);
                            CustomLog.d(TAG,"重新执行doUploadContact");
                            doUploadContact(list);
                        }
                    }
                } catch (Exception e) {
                    CustomLog.e(TAG, "doUploadContact " + e);
                    write(KeyEventConfig.UPLOAD_DATA_TO_CONTACTSERVER
                            + "_fail" + "_"
                            + AccountManager.getInstance(context).getAccountInfo().nube
                            + "_" + e.toString());
                }
            }
        }
    }

    private void doDownloadContactsData(int start, int max, long time,
                                        int isDelete) {
        CustomLog.d(TAG,"doDownloadContactsData start");
        try {
            if (state != STOP_STATE) {

                try{
                    downLoad = new DownloadContactsData() {
                    };
                    List<ContanctsInfo> list = new ArrayList<>();
                           list = downLoad.downloadContactsDataSync(time,
                            start, max, isDelete, AccountManager.getInstance(context)
                                    .getToken());
                    if (list != null && list.size() > 0) {
                        write(KeyEventConfig.DOWNLAOD_DATA_FROM_CONTACTSERVER
                                        + "_ok"
                                        + "_"
                                        + AccountManager.getInstance(context).getAccountInfo().nube);
                        CustomLog.d(TAG, "doDownloadContactsData the number of result is "
                                + list.size());
                        downLong = list.size();
                        startLineNo += downLong;
                        if (totalList.size() == 0){
                            totalList = list;
                        }else {
                            totalList.addAll(list);
                        }
//                        handleDustData(list, getAllContacts());
                        doDownloadContactsData(startLineNo, DEFAULT_UPDATE_COUNT, time, 1);
                    } else {
                        CustomLog.d(TAG, "doDownloadContactsData the number of result is 0 ");
                        if ( totalList.size()>0){
                            handleDustData(totalList, getAllContacts());
                        }
                    }
                } catch (Exception e){
                    CustomLog.e(TAG,"doDownloadContactsData Exception:" + e);
                    CustomLog.d(TAG,"60秒后重新执行doDownloadContactsData");
                    Thread.currentThread().sleep(60000);
                    CustomLog.d(TAG,"重新执行doDownloadContactsData");
                    doDownloadContactsData(start,max,time,isDelete);
                }
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "doDownloadContactsData " + e);
            write(KeyEventConfig.DOWNLAOD_DATA_FROM_CONTACTSERVER
                    + "_fail" + "_"
                    + AccountManager.getInstance(context).getAccountInfo().nube
                    + "_" + e.toString());
        }
    }

    public void updateContactsStatus(List<ContanctsInfo> list, int mark) {
        CustomLog.d(TAG, "更新好友表同步状态updateContactsStatus start");
        String ids = "";
        if (list != null && list.size() > 0) {
            try {
                Iterator<ContanctsInfo> it = list.iterator();
                StringBuilder builder = new StringBuilder();
                while (it != null && it.hasNext()) {
                    ContanctsInfo info = it.next();
                    builder.append("'").append(info.contactId).append("'").append(",");
                }
                ids = builder.toString();
                if (ids != null && ids.length() > 1) {
                    ids = ids.substring(0, ids.length() - 1);
                }
            } catch (Exception e) {
                CustomLog.e(TAG, "更新好友表同步状态 主装 Exception " + e);
            }
        }
        if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
            try {
                if (!TextUtils.isEmpty(ids)) {
                    CustomLog.d(TAG, "更新好友表同步状态updateContactsStatus ids " + ids);
                    ContentValues values = new ContentValues();
                    values.put("syncStat", mark);
                    ContactDBOperater.getInstance(context).update(table, values,
                            "contactId in (" + ids + " )", null);
                    CustomLog.d(TAG, "更新好友表同步状态 success");
                }
            } catch (Exception e) {
                CustomLog.e(TAG, "更新好友表同步状态   Exception " + e);
            }
        }
    }

    private void handleDustData(List<ContanctsInfo> contacts,
                                Map<String, Contact> allContacts) {
        try {
            if (state != STOP_STATE) {
                List<ContanctsInfo> updateList = null;
                List<String> deleteList = null;
                Map<String ,ContanctsInfo> deleteListInfo = null;
                Map<String, ContanctsInfo> insertList = null;
                List<ContanctsInfo> logicalDeleteList = null; //逻辑删除列表
                Map<String,List<ContanctsInfo>> secondMap = null; //二次处理数据
                if (contacts != null && contacts.size() > 0 && allContacts != null) {
                    updateList = new ArrayList<ContanctsInfo>();
                    deleteList = new ArrayList<String>();
                    insertList = new HashMap<String, ContanctsInfo>();
                    logicalDeleteList = new ArrayList<ContanctsInfo>();
                    secondMap = new HashMap<String,List<ContanctsInfo>>();
                    deleteListInfo = new HashMap<String,ContanctsInfo>();
                    timemapMaxInfoList = getMaxtimemap(contacts);
                    for (ContanctsInfo temp : contacts) {
                        try {
                            if (temp.nubeNumber != null
                                    && allContacts.containsKey(temp.nubeNumber)) {
                                if (allContacts.get(temp.nubeNumber) != null) {
                                    if (allContacts.get(temp.nubeNumber).getContactId()
                                            .equals(temp.contactId)) {
                                        if (1 == temp.isDeleted) {
                                            // 物理删除
                                            deleteList.add(temp.contactId);
                                            deleteListInfo.put(temp.nubeNumber,temp);
                                        } else {
                                            // 更新时间戳、删除标记、同步状态
                                            updateList.add(temp);
                                        }
                                    }else if (!allContacts.get(temp.nubeNumber).getContactId().equals(temp.contactId)){
                                        //contactId 不一样 nube一样
                                        if (checkListExitNube(updateList,temp.nubeNumber,deleteListInfo) !=0){
                                            if (temp.isDeleted == 0){
                                               addLogicalDeleteList(logicalDeleteList,temp);
                                            }
                                        }else{
                                            if (temp.isDeleted == 0){
                                                if (secondMap.containsKey(temp.nubeNumber)){
                                                    List<ContanctsInfo> tmpList = secondMap.get(temp.nubeNumber);
                                                    tmpList.add(temp);
                                                    secondMap.put(temp.nubeNumber,tmpList);
                                                }else{
                                                    List<ContanctsInfo> tmpList = new ArrayList<ContanctsInfo>();
                                                    tmpList.add(temp);
                                                    secondMap.put(temp.nubeNumber,tmpList);
                                                }
                                            }
                                        }

                                    }
                                } else if (0 == temp.isDeleted) {
                                    if (insertList.containsKey(temp.nubeNumber)){
                                        if (insertList.get(temp.nubeNumber).timestamp < temp.timestamp){
                                            insertList.remove(temp.nubeNumber);
                                            insertList.put(temp.nubeNumber,temp);
                                        }else{
                                            addLogicalDeleteList(logicalDeleteList,temp);
                                        }
                                    }else{
                                        insertList.put(temp.nubeNumber, temp);
                                    }
                                }
                            } else if (temp.nubeNumber != null && temp.contactId != null
                                    && temp.isDeleted == 0) {
                                // 新添加数据（本地没有）
                                if (insertList.containsKey(temp.nubeNumber)){
                                    if (insertList.get(temp.nubeNumber).timestamp < temp.timestamp){
                                        insertList.remove(temp.nubeNumber);
                                        insertList.put(temp.nubeNumber,temp);
                                    }else{
                                        addLogicalDeleteList(logicalDeleteList,temp);
                                    }
                                }else{
                                    insertList.put(temp.nubeNumber, temp);
                                }

                            }
                        } catch (Exception e) {
                            CustomLog.e(TAG, "handleDustData 处理数据 " + e);
                        }
                    }
                }
                //二次处理
                if (secondMap != null&&secondMap.size()>0){
                    for (String nube:secondMap.keySet()){
                        List<ContanctsInfo> infos = secondMap.get(nube);
                        if (infos.size()>0){
                            ContanctsInfo  maxInfo = getMaxtimeInfo(infos);
                            int  flag = checkListExitNube(updateList,nube,deleteListInfo);
                            if (flag == 1){
                                if (deleteListInfo.get(nube).timestamp < maxInfo.timestamp){
                                    insertList.put(nube,maxInfo);
                                    infos.remove(maxInfo);
                                    if (infos.size()>0){
                                        for (ContanctsInfo deleteInfo:infos){
                                            addLogicalDeleteList(logicalDeleteList,deleteInfo);
                                        }
                                    }
                                }else{
                                    for (ContanctsInfo deleteInfo:infos){
                                        addLogicalDeleteList(logicalDeleteList,deleteInfo);
                                    }
                                }
                            }else if (flag == 2){
                                for (ContanctsInfo deleteInfo:infos){
                                    addLogicalDeleteList(logicalDeleteList,deleteInfo);
                                }
                            }else if (flag == 0){
                                for (ContanctsInfo deleteInfo:infos){
                                    addLogicalDeleteList(logicalDeleteList,deleteInfo);
                                }
//                                if (insertList.size()>0){
//                                    for (String tmpNube:insertList.keySet()){
//                                        ContanctsInfo insertInfo = insertList.get(tmpNube);
//                                        ContanctsInfo maxTimeInfo = timemapMaxInfoList.get(tmpNube);
//                                        if (insertInfo.timestamp < maxTimeInfo.timestamp){
//                                            if (maxTimeInfo.isDeleted == 1){
//                                                insertList.remove(tmpNube);
//                                                if (allContacts.containsKey(tmpNube)){
//                                                    deleteList.add(allContacts.get(tmpNube).getContactId());
//                                                }
//                                                addLogicalDeleteList(logicalDeleteList,insertInfo);
//                                            }else {
//                                                insertList.remove(tmpNube);
//                                                insertList.put(tmpNube,maxTimeInfo);
//                                                addLogicalDeleteList(logicalDeleteList,insertInfo);
//                                            }
//                                        }
//                                    }
//                                }
                            }

                        }
                    }
                }
//                for (String nube:insertList.keySet()){
//                        if (timemapMaxInfoList.containsKey(nube)){
//                            ContanctsInfo tempInfo = timemapMaxInfoList.get(nube);
//                            ContanctsInfo insertInfo = insertList.get(nube);
//                            if (nube.length()>10){
//                                insertList.remove(nube);
//                                addLogicalDeleteList(logicalDeleteList,insertInfo);
//                            }else{
//                                if (tempInfo.isDeleted == 1  &&(insertInfo.timestamp<tempInfo.timestamp)){
//                                    insertList.remove(nube);
//                                    addLogicalDeleteList(logicalDeleteList,insertInfo);
//                                }
//                            }
//
//                        }
//                    }

                //上面方法会报 java.util.ConcurrentModificationException
                for (Iterator<Map.Entry<String, ContanctsInfo>> it = insertList.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String, ContanctsInfo> entry = it.next();
                    if (timemapMaxInfoList.containsKey(entry.getKey())){
                        ContanctsInfo insertInfo = insertList.get(entry.getKey());
                        ContanctsInfo tempInfo = timemapMaxInfoList.get(entry.getKey());
                        if(entry.getValue().number.length() > 10){
                            it.remove();
                            addLogicalDeleteList(logicalDeleteList,insertInfo);
                        }else{
                            if (tempInfo.isDeleted == 1  &&(insertInfo.timestamp<tempInfo.timestamp)){
                                it.remove();
                                addLogicalDeleteList(logicalDeleteList,insertInfo);
                            }
                        }
                    }
                }

                if (logicalDeleteList != null && !logicalDeleteList.isEmpty()) {
                    CustomLog.d(TAG, "handleDustData  logicalDeleteList");
                    logicalDeleteContacts(logicalDeleteList);
                }
                if (deleteList != null && !deleteList.isEmpty()) {
                    CustomLog.d(TAG, "handleDustData deleteList ");
                    deleteContacts(deleteList);
                }
                if (updateList != null && !updateList.isEmpty()) {
                    CustomLog.d(TAG, "handleDustData updateContacts ");
                    updateContacts(updateList, 1);
                }
                if (insertList != null && !insertList.isEmpty()) {
                    CustomLog.d(TAG, "handleDustData insertList ");
                    isNeedNotify = true;
                    insertContacts(new ArrayList<ContanctsInfo>(insertList.values()));
                }

            }
        } catch (Exception e) {
            CustomLog.e(TAG, "handleDustData 调Db " + e);
        }
    }


    /**
     *
     * @param contacts
     * 1.将服务器返回的数据进行整理 归纳出同一个视讯号下的 所有信息
     * 2.再归纳同一个视讯号所有的信息里面时间戳最大的信息
     * @return  Map<String,ContanctsInfo>
     */
    private  Map<String,ContanctsInfo>  getMaxtimemap(List<ContanctsInfo> contacts){
        if (timemapMaxInfoList == null){
            timemapMaxInfoList = new HashMap<String,ContanctsInfo>();
        }
        Map<String,List<ContanctsInfo>> servieList = null;
        for (ContanctsInfo info :contacts){
             if (servieList == null){
                 servieList = new HashMap<String,List<ContanctsInfo>>();
                 List<ContanctsInfo> cInfo = new ArrayList<ContanctsInfo>();
                 cInfo.add(info);
                 servieList.put(info.nubeNumber,cInfo);
             }else {
                 if (servieList.containsKey(info.nubeNumber)){
                     List<ContanctsInfo> dInfo = servieList.get(info.nubeNumber);
                     dInfo.add(info);
                     servieList.put(info.nubeNumber,dInfo);
                 }else{
                     List<ContanctsInfo> fInfo = new ArrayList<ContanctsInfo>();
                     fInfo.add(info);
                     servieList.put(info.nubeNumber,fInfo);
                 }
             }
        }
        if (servieList.size()>0){
            for (String nube:servieList.keySet()){
                List<ContanctsInfo> gInfos = servieList.get(nube);
                ContanctsInfo mInfo = getMaxtimeInfo(gInfos);
                if (timemapMaxInfoList!=null&&timemapMaxInfoList.containsKey(nube)){
                    ContanctsInfo finfo = timemapMaxInfoList.get(nube);
                    if (finfo.timestamp < mInfo.timestamp){
                        timemapMaxInfoList.put(mInfo.nubeNumber,mInfo);
                    }
                }else {
                    timemapMaxInfoList.put(mInfo.nubeNumber,mInfo);
                }
            }
        }
        return timemapMaxInfoList;
    }

    /**
     * @param
     * @param nubeNumber
     * @return  1 代表deleteList中存在  2 代表updateList中存在 0表示不存在
      */
    private int checkListExitNube(List<ContanctsInfo> contancts,String nubeNumber,Map<String,ContanctsInfo> tempList){
        int flag_update = 0;
        int flag_delete = 0;
        if (contancts.size()>0&&contancts!=null){
            for(ContanctsInfo info : contancts ){
                 if (info.nubeNumber.equals(nubeNumber)){
                    flag_update = 1;
                     break;
                 }
            }
        }
        if (tempList.size()>0&&tempList!=null){
            for (String temp:tempList.keySet()){
                if (temp.equals(nubeNumber)){
                    flag_delete = 1;
                    break;
                }
            }
        }
        if (flag_delete != 0&&flag_update==0){
            return  1;
        }else  if (flag_delete == 0 && flag_update != 0){
            return  2;
        }else  if (flag_delete == 1 && flag_update ==1){
            return  3;
        }
        return 0;
    }

    private  ContanctsInfo getMaxtimeInfo(List<ContanctsInfo> contacts){

            ContanctsInfo tmpInfo = null;
            for (ContanctsInfo info:contacts){
                if (tmpInfo == null){
                    tmpInfo = info;
                }else {
                    if (tmpInfo.timestamp < info.timestamp){
                        tmpInfo = info;
                    }
                }
            }
            return  tmpInfo;
    }

    private List<ContanctsInfo> addLogicalDeleteList(List<ContanctsInfo> list,ContanctsInfo info){
        if (list.size()>0){
            boolean isExit = false;
            for (ContanctsInfo tempInfo:list){
                if (tempInfo.nubeNumber.equals(info.nubeNumber)&&tempInfo.contactId.equals(info.contactId)){
                    isExit = true;
                    break;
                }
            }
            if (!isExit){
                list.add(info);
            }
        }else {
            list.add(info);
        }
        return  list;
    }
    private void logicalDeleteContacts(List<ContanctsInfo> list){
        CustomLog.d(TAG,"logicalDeleteContacts start"+list.toString());
        if (!list.isEmpty() && state != STOP_STATE){
            List<ContentValues> logicalDeleteList = new ArrayList<ContentValues>();
            for (int i = 0; i < list.size() ;i++){
                ContentValues deleteCvs = new ContentValues();
                if (TextUtils.isEmpty(list.get(i).nickName)) {
                    deleteCvs.put(DBConf.NICKNAME, context.getString(R.string.unnamed));
                    deleteCvs.put(DBConf.FIRSTNAME, StringHelper.getHeadChar(context.getString(R.string.unnamed)));
                    deleteCvs.put(DBConf.PINYIN, StringHelper.getAllPingYin(context.getString(R.string.unnamed)));
                } else {
                    deleteCvs.put(DBConf.NICKNAME, list.get(i).nickName);
                    deleteCvs.put(DBConf.FIRSTNAME,
                            StringHelper.getHeadChar(list.get(i).nickName));
                    deleteCvs
                            .put(DBConf.PINYIN, StringHelper.getAllPingYin(list.get(i).nickName));
                }
                if (TextUtils.isEmpty(list.get(i).name)) {
                    deleteCvs.put(DBConf.NAME, checkIsNull(context.getString(R.string.unnamed)));
                } else {
                    deleteCvs.put(DBConf.NAME, checkIsNull(list.get(i).name));
                }
                deleteCvs.put(DBConf.CONTACTID, list.get(i).contactId);
                deleteCvs.put(DBConf.LASTTIME, valueOf(list.get(i).timestamp));
                deleteCvs.put(DBConf.ISDELETED, 1);
                deleteCvs.put(DBConf.SYNCSTAT, 0);
                deleteCvs.put(DBConf.PICURL, list.get(i).urls);
                deleteCvs.put(DBConf.PHONENUMBER, checkIsNull(list.get(i).number));
                deleteCvs.put(DBConf.USERTYPE, list.get(i).ServiceType);
                deleteCvs.put(DBConf.NUBENUMBER, checkIsNull(list.get(i).nubeNumber));
                deleteCvs.put(DBConf.USERFROM, list.get(i).UserFrom);
                deleteCvs.put(DBConf.CONTACTUSERID, checkIsNull(list.get(i).contactUserId));
                deleteCvs.put(DBConf.APPTYPE, checkIsNull(list.get(i).AppType));
                deleteCvs.put(DBConf.EMAIL, list.get(i).email);
                deleteCvs.put(DBConf.ACCOUNT_TYPE, list.get(i).accountType);
                deleteCvs.put(DBConf.WORKUNIT_TYPE, list.get(i).workUnitType);
                deleteCvs.put(DBConf.WORK_UNIT, list.get(i).workUnit);
                deleteCvs.put(DBConf.DEPARTMENT, list.get(i).department);
                deleteCvs.put(DBConf.PROFESSIONAL, list.get(i).professional);
                deleteCvs.put(DBConf.OFFICETEL, list.get(i).officeTel);
                deleteCvs.put(DBConf.SAVE_TO_CONTACTS_TIME, list.get(i).saveToContactsTime);
                logicalDeleteList.add(deleteCvs);
            }
            if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
                try {
                    if (logicalDeleteList.size() > 0) {
                        long result = ContactDBOperater.getInstance(context).applyInsertBatchlogic(table,
                                logicalDeleteList);
                        if(result==0){
                            CustomLog.d(TAG, "LogocalDeleteList result==0");
                            notifyListner();
                        }
                        CustomLog.d(TAG, "LogocalDeleteContacts success");
                    }
                } catch (Exception e) {
                    CustomLog.e(TAG, "LogocalDeleteContacts success" + e);
                }
            }
        }
    }
    private void insertContacts(List<ContanctsInfo> list) {
        CustomLog.d(TAG, "insertContacts start " + list.toString());
        if (!list.isEmpty() && state != STOP_STATE) {
            List<ContentValues> insertList = new ArrayList<ContentValues>();
            for (int i = 0; i < list.size(); i++) {
                ContentValues addCvs = new ContentValues();
                if (TextUtils.isEmpty(list.get(i).nickName)) {
                    addCvs.put(DBConf.NICKNAME, context.getString(R.string.unnamed));
                    addCvs.put(DBConf.FIRSTNAME, StringHelper.getHeadChar(context.getString(R.string.unnamed)));
                    addCvs.put(DBConf.PINYIN, StringHelper.getAllPingYin(context.getString(R.string.unnamed)));
                } else {
                    addCvs.put(DBConf.NICKNAME, list.get(i).nickName);
                    addCvs.put(DBConf.FIRSTNAME,
                            StringHelper.getHeadChar(list.get(i).nickName));
                    addCvs
                            .put(DBConf.PINYIN, StringHelper.getAllPingYin(list.get(i).nickName));
                }
                if (TextUtils.isEmpty(list.get(i).name)) {
                    addCvs.put(DBConf.NAME, checkIsNull(context.getString(R.string.unnamed)));
                } else {
                    addCvs.put(DBConf.NAME, checkIsNull(list.get(i).name));
                }
                addCvs.put(DBConf.CONTACTID, list.get(i).contactId);
                addCvs.put(DBConf.LASTTIME, valueOf(list.get(i).timestamp));
                addCvs.put(DBConf.ISDELETED, list.get(i).isDeleted);
                addCvs.put(DBConf.SYNCSTAT, 1);
                addCvs.put(DBConf.PICURL, list.get(i).urls);
                addCvs.put(DBConf.PHONENUMBER, checkIsNull(list.get(i).number));
                addCvs.put(DBConf.USERTYPE, list.get(i).ServiceType);
                addCvs.put(DBConf.NUBENUMBER, checkIsNull(list.get(i).nubeNumber));
                addCvs.put(DBConf.USERFROM, list.get(i).UserFrom);
                addCvs.put(DBConf.CONTACTUSERID, checkIsNull(list.get(i).contactUserId));
                addCvs.put(DBConf.APPTYPE, checkIsNull(list.get(i).AppType));
                addCvs.put(DBConf.EMAIL, list.get(i).email);
                addCvs.put(DBConf.ACCOUNT_TYPE, list.get(i).accountType);
                addCvs.put(DBConf.WORKUNIT_TYPE, list.get(i).workUnitType);
                addCvs.put(DBConf.WORK_UNIT, list.get(i).workUnit);
                addCvs.put(DBConf.DEPARTMENT, list.get(i).department);
                addCvs.put(DBConf.PROFESSIONAL, list.get(i).professional);
                addCvs.put(DBConf.OFFICETEL, list.get(i).officeTel);
                addCvs.put(DBConf.SAVE_TO_CONTACTS_TIME, list.get(i).saveToContactsTime);
                insertList.add(addCvs);
            }
            if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
                try {
                    if (insertList.size() > 0) {
                        long result = ContactDBOperater.getInstance(context).applyInsertBatch(table,
                                insertList);
                        if(result==0){
                            CustomLog.d(TAG, "insertContacts result==0");
                            notifyListner();
                        }
                        CustomLog.d(TAG, "insertContacts success");
                    }
                } catch (Exception e) {
                    CustomLog.e(TAG, "insertContacts Exception " + e);
                }
            }
        }
    }

    // 更新时间戳
    public void updateContactsTimeStamp(List<ContanctsInfo> list, int mark) {
        CustomLog.d(TAG, "updateContacts start " + list.toString());
        if (!list.isEmpty()) {
            List<ContentValues> updateList = new ArrayList<ContentValues>();
            for (int i = 0; i < list.size(); i++) {
                ContentValues addCvs = new ContentValues();
                addCvs.put(DBConf.CONTACTID, list.get(i).contactId);
                addCvs.put(DBConf.LASTTIME, valueOf(list.get(i).timestamp));
                addCvs.put(DBConf.SYNCSTAT, mark);
                updateList.add(addCvs);
            }
            if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
                try {
                    if (updateList.size() > 0) {
                        long result = ContactDBOperater.getInstance(context).applyUpdateBatch(table,
                                updateList);
                        if(result==0){
                            CustomLog.d(TAG, "updateContacts result==0");
                            notifyListner();
                        }
                        CustomLog.d(TAG, "updateContacts 批量更新时间戳   success");
                    }
                } catch (Exception e) {
                    CustomLog.e(TAG, "updateContacts 批量更新时间戳   Exception " + e);
                }
            }
        }

    }

    // 事件戳，isdelete，头像，昵称 同步字段
    public void updateContacts(List<ContanctsInfo> list, int mark) {
        CustomLog.d(TAG, "updateContacts start " + list.toString());
        if (!list.isEmpty()) {
            List<ContentValues> updateList = new ArrayList<ContentValues>();
            for (int i = 0; i < list.size(); i++) {
                ContentValues addCvs = new ContentValues();
                addCvs.put(DBConf.CONTACTID, list.get(i).contactId);
                addCvs.put(DBConf.LASTTIME, valueOf(list.get(i).timestamp));
                addCvs.put(DBConf.ISDELETED, list.get(i).isDeleted);
                addCvs.put(DBConf.SYNCSTAT, mark);
                addCvs.put(DBConf.NICKNAME, list.get(i).nickName);
                addCvs.put(DBConf.FIRSTNAME, StringHelper.getHeadChar(list.get(i).nickName));
                addCvs.put(DBConf.PINYIN, StringHelper.getAllPingYin(list.get(i).nickName));
                addCvs.put(DBConf.PICURL, list.get(i).urls);
                updateList.add(addCvs);
            }
            if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
                try {
                    if (updateList.size() > 0) {
                        long result = ContactDBOperater.getInstance(context).applyUpdateBatch(table,
                                updateList);
                        if(result==0){
                            CustomLog.d(TAG, "updateContacts result==0");
                            notifyListner();
                        }
                        CustomLog.d(TAG, "updateContacts 批量更新时间戳   success");
                    }
                } catch (Exception e) {
                    CustomLog.e(TAG, "updateContacts 批量更新时间戳   Exception " + e);
                }
            }
        }

    }

    public int deleteContacts(List<String> list) {
        String ids = "";
        if (list != null && !list.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String id : list) {
                builder.append("'").append(id).append("'").append(",");
            }
            ids = builder.toString();
            if (ids != null && ids.length() > 1) {
                ids = ids.substring(0, ids.length() - 1);
            }
        }
        if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
            try {
                if (!TextUtils.isEmpty(ids)) {
                    long result = ContactDBOperater.getInstance(context).delete(table,
                            "contactId in (" + ids + " )", null);
                    if(result==0){
                        CustomLog.d(TAG, "deleteContacts result==0");
                        notifyListner();
                    }
                    CustomLog.d(TAG, "物理删除联系人 success");
                    return 0;
                }
            } catch (Exception e) {
                CustomLog.e(TAG, "物理删除联系人失败   Exception " + e);
            }
        }
        return -1;
    }

    public void updateStatusAndDelete(List<ContanctsInfo> list, int mark) {
        if (list != null && !list.isEmpty()) {
            List<ContentValues> updateList = new ArrayList<ContentValues>();
            for (int i = 0; i < list.size(); i++) {
                ContentValues addCvs = new ContentValues();
                addCvs.put("contactId", list.get(i).contactId);
                // addCvs.put("lastTime", String.valueOf(list.get(i).getLastTime()));
                addCvs.put("syncStat", mark); // 抛到主线程去做修改
                updateList.add(addCvs);
            }
            if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
                try {
                    if (updateList.size() > 0) {
                        ContactDBOperater.getInstance(context).applyUpdateBatch(table,
                                updateList);
                        CustomLog.d(TAG, "批量更新同步状态、isdelete   success");
                    }
                } catch (Exception e) {
                    CustomLog.e(TAG, "批量更新同步状态、isdelete间戳   Exception " + e);
                }
            }
        }

    }

    private String checkIsNull(String str) {
        if (str != null) {
            return str;
        } else {
            return "";
        }
    }

    private Map<String, Contact> getAllContacts() {
        String sql = "select * from " + table;
        Map<String, Contact> list = new HashMap<String, Contact>();
        if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
            Cursor cursor = ContactDBOperater.getInstance(context).queryAllContacts(
                    sql, table);
            if (cursor != null) {
                list = new HashMap<String, Contact>();
                while (cursor.moveToNext()) {
                    list.put(getDataFromCursor(cursor).getNubeNumber(),
                            getDataFromCursor(cursor));
                }
            }
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }

    private long getMaxTimestamp() {
        long timestamp = 0;
        if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE) {
            try {
                String sql = "select max(lastTime) lastTime from " + table;
                timestamp = ContactDBOperater.getInstance(context).getMaxTimeStamp(sql,
                        table);
            } catch (Exception e) {
                CustomLog.e(TAG, "getMaxTimestamp " + e);
            }
        }
        return timestamp;
    }

    private void compareContacts(List<MDSDetailInfo> list,
                                 Map<String, Contact> nubeContacts) {
        List<Contact> updatList = null;
        if (nubeContacts != null && list != null && list.size() > 0) {
            Contact c = null;
            updatList = new ArrayList<Contact>();
            for (MDSDetailInfo entry : list) {
                try {
                    c = nubeContacts.get(entry.nubeNumber);
                    String nickName = "";
                    if (null==entry.nickName||TextUtils.isEmpty(entry.nickName)) {
                        nickName = context.getString(R.string.unnamed);
                    } else {
                        nickName = entry.nickName;
                    }

                    c.setFullPym(StringHelper.getPingYin(nickName));

                    if (c != null && entry != null) {

                        if(entry.getNickName()!=null&&c.getNickname()!= null
                                &&!entry.getNickName().equals(c.getNickname())) {
                            c.setNickname(entry.getNickName());
                        }

                        if(entry.getMail()!=null&&c.getEmail()!=null
                                &&!entry.getMail().equals(c.getEmail())){
                            c.setEmail(entry.getMail());
                        }

                        if(entry.getMobile()!=null&&c.getNumber()!=null
                                &&!entry.getMobile().equals(c.getNumber())){
                            c.setNumber(entry.getMobile());
                        }

                        if(entry.getHeadThumUrl()!=null&&c.getHeadUrl()!=null
                                &&!entry.getHeadThumUrl().equals(c.getHeadUrl())){
                            c.setHeadUrl(entry.getHeadThumUrl());
                        }

                        if(entry.getWorkUnit()!=null&&c.getWorkUnit()!=null
                                &&!entry.getWorkUnit().equals(c.getWorkUnit())){
                            c.setWorkUnit(entry.getWorkUnit());
                        }

                        if(entry.getWorkUnitType()!=null&& valueOf(c.getWorkUnitType())!=null
                                &&!entry.getWorkUnitType().equals(valueOf(c.getWorkUnitType()))){
                            c.setWorkUnitType(Integer.valueOf(entry.getWorkUnitType()));
                        }

                        if(entry.getDepartment()!=null&&c.getDepartment()!=null
                                &&!entry.getDepartment().equals(c.getDepartment())){
                            c.setDepartment(entry.getDepartment());
                        }

                        if(entry.getProfessional()!=null&&c.getProfessional()!=null
                                &&!entry.getProfessional().equals(c.getProfessional())){
                            c.setProfessional(entry.getProfessional());
                        }

                        if(entry.getOfficTel()!=null&&c.getOfficeTel()!=null
                                &&!entry.getOfficTel().equals(c.getOfficeTel())){
                            c.setOfficeTel(entry.getOfficTel());
                        }

                        updatList.add(c);

                    }
                } catch (Exception e) {
                    CustomLog.e(TAG, "compareContacts  " + e.toString());
                }
            }
            if (updatList.size() > 0 && state != STOP_STATE) {
                updateContactInfos(updatList);
            }
        }
    }

    private void updateContactInfos(List<Contact> list) {
        if (list != null && !list.isEmpty()) {
            List<ContentValues> updateList = new ArrayList<ContentValues>();
            for (int i = 0; i < list.size(); i++) {
                Contact c = list.get(i);
                ContentValues values = new ContentValues();
                // 头像 昵称 权限 设备类型 同步状态
                values.put(DBConf.CONTACTID, c.getContactId());
                values.put(DBConf.NICKNAME, checkIsNull(c.getNickname()));
                values.put(DBConf.PICURL, checkIsNull(c.getPicUrl()));
                values.put(DBConf.USERTYPE, c.getUserType());
                values.put(DBConf.FIRSTNAME, checkIsNull(c.getFirstName()));
                values.put(DBConf.PINYIN, checkIsNull(StringHelper.getAllPingYin(c.getNickname())));
                values.put(DBConf.WORK_UNIT,c.getWorkUnit());
                values.put(DBConf.DEPARTMENT,c.getDepartment());
                values.put(DBConf.PROFESSIONAL,c.getProfessional());
                values.put(DBConf.OFFICETEL,c.getOfficeTel());

                values.put(DBConf.SYNCSTAT, 0);
                updateList.add(values);
            }
            if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
                try {
                    if (updateList.size() > 0) {
                        ContactDBOperater.getInstance(context).applyUpdateBatch(table,
                                updateList);
                        CustomLog.d(TAG, "批量更新好友头像等等   success");
                    }
                } catch (Exception e) {
                    CustomLog.e(TAG, "批量更新好友头像等等   Exception " + e);
                }
            }
        }
    }

    private Contact getDataFromCursor(Cursor cursor) {
        Contact c = new Contact();
        c.setContactId(cursor.getString(cursor.getColumnIndex(DBConf.CONTACTID)));
        c.setName(cursor.getString(cursor.getColumnIndex(DBConf.NAME)));
        c.setNubeNumber(cursor.getString(cursor.getColumnIndex(DBConf.NUBENUMBER)));
        c.setNickname(cursor.getString(cursor.getColumnIndex(DBConf.NICKNAME)));
        c.setAppType(cursor.getString(cursor.getColumnIndex(DBConf.APPTYPE)));
        c.setPicUrl(cursor.getString(cursor.getColumnIndex(DBConf.PICURL)));
        c.setIsDeleted(cursor.getInt(cursor.getColumnIndex(DBConf.ISDELETED)));
        c.setLastTime(cursor.getLong(cursor.getColumnIndex(DBConf.LASTTIME)));
        c.setNumber(cursor.getString(cursor.getColumnIndex(DBConf.PHONENUMBER)));
        c.setContactUserId(cursor.getString(cursor
                .getColumnIndex(DBConf.CONTACTUSERID)));
        c.setUserType(cursor.getInt(cursor.getColumnIndex(DBConf.USERTYPE)));
        c.setUserFrom(cursor.getInt(cursor.getColumnIndex(DBConf.USERFROM)));
        c.setFullPym(StringHelper.getPingYin(c.getNickname()));
        return c;
    }

    private ContanctsInfo getContanctsInfoFromCursor(Cursor cursor) {
        ContanctsInfo c = new ContanctsInfo();
        c.contactId = cursor.getString(cursor.getColumnIndex(DBConf.CONTACTID));
        c.name = cursor.getString(cursor.getColumnIndex(DBConf.NAME));
        c.nubeNumber = cursor.getString(cursor.getColumnIndex(DBConf.NUBENUMBER));
        c.nickName = cursor.getString(cursor.getColumnIndex(DBConf.NICKNAME));
        c.AppType = cursor.getString(cursor.getColumnIndex(DBConf.APPTYPE));
        c.urls = cursor.getString(cursor.getColumnIndex(DBConf.PICURL));
        c.isDeleted = cursor.getInt(cursor.getColumnIndex(DBConf.ISDELETED));
        c.timestamp = cursor.getLong(cursor.getColumnIndex(DBConf.LASTTIME));
        c.number = cursor.getString(cursor.getColumnIndex(DBConf.PHONENUMBER));
        c.contactUserId = cursor.getString(cursor.getColumnIndex(DBConf.CONTACTUSERID));
        c.ServiceType = cursor.getInt(cursor.getColumnIndex(DBConf.USERTYPE));
        c.UserFrom = cursor.getInt(cursor.getColumnIndex(DBConf.USERFROM));
        c.accountType = cursor.getInt(cursor.getColumnIndex(DBConf.ACCOUNT_TYPE));
        c.firstName = cursor.getString(cursor.getColumnIndex(DBConf.FIRSTNAME));
        c.email = cursor.getString(cursor.getColumnIndex(DBConf.EMAIL));
        c.workUnitType = cursor.getInt(cursor.getColumnIndex(DBConf.WORKUNIT_TYPE));
        c.workUnit = cursor.getString(cursor.getColumnIndex(DBConf.WORK_UNIT));
        c.professional = cursor.getString(cursor.getColumnIndex(DBConf.PROFESSIONAL));
        c.department = cursor.getString(cursor.getColumnIndex(DBConf.DEPARTMENT));
        c.officeTel = cursor.getString(cursor.getColumnIndex(DBConf.OFFICETEL));
        c.saveToContactsTime = cursor.getString(cursor.getColumnIndex(DBConf.SAVE_TO_CONTACTS_TIME));
        return c;
    }

    private List<List<ContanctsInfo>> getNeedUpdateContacts() {
        String sql = "select contactId contactId,name name,nickname nickname,firstName firstName,"
                + " lastTime lastTime,isDeleted isDeleted,number number,nubeNumber nubeNumber,"
                + "contactUserId contactUserId,headUrl headUrl,userType userType,userFrom userFrom,appType appType, email email,accountType accountType, workUnitType workUnitType,"
                + "workUnit workUnit, department department, professional professional, officeTel officeTel, saveToContactsTime saveToContactsTime"
                + " from " + table + " tn where tn.syncStat=0";
        List<List<ContanctsInfo>> uploadList = new ArrayList<List<ContanctsInfo>>();
        if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
            Cursor cursor = ContactDBOperater.getInstance(context)
                    .queryNeedUpdateContacts(sql, table);
            if (cursor != null) {
                List<ContanctsInfo> item = new ArrayList<ContanctsInfo>();
                while (cursor.moveToNext()) {
                    item.add(getContanctsInfoFromCursor(cursor));
                    if (item.size() == DEFAULT_UPDATE_COUNT) {
                        uploadList.add(new ArrayList<ContanctsInfo>(item));
                        item.clear();
                    }
                }
                if (item != null && item.size() > 0) {
                    uploadList.add(new ArrayList<ContanctsInfo>(item));
                    item.clear();
                }
                item = null;
            }
            CustomLog.d(TAG, "getNeedUpdateContacts " + uploadList.toString());
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return uploadList;
    }

    public void init(Context context, boolean isSync,
                     List<IContactListChanged> iContactListChanged, String table) {
        state = INIT_STATE;
        this.context = context;
        this.isSync = isSync;
        this.iContactListChanged = iContactListChanged;
        this.table = table;
        isNeedNotify = false;
    }

    public void cancle() {
        state = STOP_STATE;
        isSync = false;
        isNeedNotify = false;
        iContactListChanged = null;
        if (search != null) {
            search.cancel();
            search = null;
        }
        if (downLoad != null) {
            downLoad.cancel();
            downLoad = null;
        }
        if (upload != null) {
            upload.cancel();
            upload = null;
        }
    }
}
