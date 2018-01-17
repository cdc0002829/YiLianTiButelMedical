package cn.redcdn.hvs.contacts.contact.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.hpucenter.data.DTInfo;
import cn.redcdn.datacenter.medicalcenter.MDSAppSearchUsers;
import cn.redcdn.datacenter.medicalcenter.data.MDSDetailInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.contacts.contact.StringHelper;
import cn.redcdn.hvs.contacts.contact.butelDataAdapter.ContactSetImp;
import cn.redcdn.hvs.contacts.contact.database.DBConf;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.interfaces.ContactCallback;
import cn.redcdn.hvs.contacts.contact.interfaces.ContactOperation;
import cn.redcdn.hvs.contacts.contact.interfaces.ResponseEntry;
import cn.redcdn.hvs.im.bean.FriendInfo;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.log.CustomLog;

public class ContactManager implements ContactOperation {
    private static final String TAG = ContactManager.class.getSimpleName();
    private static ContactManager mInstance = null;
    private Context mContext = null;
    private List<IContactListChanged> iContactListChanged = null;
    // private int changeCount;
    private String myTable;
    public static final String suffix = "_contact";
    public static final String prefix = "my_";
    //  myTable = prefix + account + suffix;
    public static final String TABLE_NAME = "t_nubefriend";
    public static final String HPU_TABLE_NAME = "t_hpu_friends";
    private ContactSync dataSync = null;
    private ShareContactSync shareDataSync = null;
    private String hpuTable;



    public static String customerServiceNum1 = "68000001";
    public static String customerServiceNum2 = "68000002";


    public static final String customerServiceName = "视频客服";

    public static final String groupChat = "群聊";
    public static final String publicNumber = "公众号";

    public static int cannotAdd = -100;//不能加自己为好友

    /**
     * <pre>
     * CAUTION :
     * 实例化 给外界调用
     * </pre>
     */
    public synchronized static ContactManager getInstance(Context context) {
        if (mInstance == null) {
            CustomLog.d(TAG, "ContactManager getInstance");
            mInstance = new ContactManager();
            mInstance.mContext = context;
            // ContactDBOperater.getInstance(context);
            // mInstance.dataHandlerThread = new HandlerThread("updateHandlerThread");
            // mInstance.dataHandlerThread.start();
            // mInstance.dataHandler = new Handler(
            // mInstance.dataHandlerThread.getLooper());
            // mInstance.state = INIT_STATE;
        }
        return mInstance;
    }

    private ContactManager(){

        if(!TextUtils.isEmpty(SettingData.getInstance().CUSTEMER_TEL1)){
            customerServiceNum1 = SettingData.getInstance().CUSTEMER_TEL1;
        }

        if(!TextUtils.isEmpty(SettingData.getInstance().CUSTEMER_TEL2)){
            customerServiceNum2 = SettingData.getInstance().CUSTEMER_TEL2;
        }

    }

    public void initData(String account) {
        CustomLog.d(TAG, "initData NOW account: " + account);
        myTable = TABLE_NAME;
        hpuTable = HPU_TABLE_NAME;
        CustomLog.d(TAG, "initData myTable " + myTable);
        ContactDBOperater.getInstance(mContext);
        RecommendManager.getInstance(mContext);

        // TODO
        contactDataSync(true);
        doShareContactSync();
    }

    public String getMyTable() {
        return myTable;
    }

    public boolean checkNubeIsCustomService(String nubeNumber) {
        if (nubeNumber == null || nubeNumber.equals("")) {
            return false;
        }
        if (nubeNumber.equals(customerServiceNum1)) {
            return true;
        }
        if (nubeNumber.equals(customerServiceNum2)) {
            return true;
        }
        return false;
    }

    public boolean isContactExist(String nubeNumber) {
        if (checkNubeIsCustomService(nubeNumber)) {
            return true;
        }
        boolean isExist = false;
        String sql = "select count(*) from " + myTable + " where nubeNumber = '"
                + nubeNumber + "' and isDeleted = 0 ";
        Cursor c = ContactDBOperater.getInstance(mContext).rawQuery(sql, myTable);
        if (c != null && c.moveToNext()) {
            if (c.getInt(0) > 0) {
                isExist = true;
            }
        }
        if (c != null) {
            c.close();
        }
        CustomLog.d(TAG, "isContactExist " + isExist);
        return isExist;
    }

    public String getHeadUrlByNube(String nubeNumber) {
        String url = "";
        String sql = "select headUrl from " + myTable + " where nubeNumber = '"
                + nubeNumber + "' and isDeleted = 0 ";
        Cursor c = ContactDBOperater.getInstance(mContext).rawQuery(sql, myTable);
        if (c != null && c.moveToNext()) {
            url = c.getString(0);
        }
        if (c != null) {
            c.close();
        }
        return url;
    }

    /**
     * 从医联体通讯录中获取医联体列表
     * @return
     */
    public List<DTInfo> getDTListFromData(){
         List<DTInfo> dtInfos = new ArrayList<>();
        String sql = "select * from "+ hpuTable+" group by phuId ";
        Cursor c = ContactDBOperater.getInstance(mContext).rawQuery(sql,hpuTable);
        while (c != null &&c.moveToNext()){
            DTInfo info = new DTInfo();
            info.id = c.getString(c.getColumnIndex(DBConf.PHUID));
            info.name =  c.getString(c.getColumnIndex(DBConf.PHUNAME));
            dtInfos.add(info);
        }
        if(c != null){
            c.close();
        }
        return dtInfos;
    }

    /**
     * 获取某个医联体下的联系人列
     *
     */
    public List<Contact> getContactsBydtId(String id){
        List<Contact> mList = new ArrayList<>();


        String sql = "select * from " + hpuTable + " where phuId = " + "'"+id+"'";
        Cursor c = ContactDBOperater.getInstance(mContext).rawQuery(sql,hpuTable);
        while (c!=null&&c.moveToNext()){
            Contact contact = getDataFromDTCursor(c);
            mList.add(contact);
        }
        if (c!=null){
            c.close();
        }
        return mList;

    }

    public  List<Contact> queryHpuContactByNube(String nubeNumber){
        List<Contact> mList = new ArrayList<>();
        String sql = "select * from "+ hpuTable +" where nubeNumber = " + nubeNumber;
        Cursor c = ContactDBOperater.getInstance(mContext).rawQuery(sql,hpuTable);
        if (c!=null){
            while (c.moveToNext()){
                mList.add(getDataFromDTCursor(c));
            }
        }
        if (c!=null){
            c.close();
        }
        return mList;
    }


    public void addContact(String nubeNumber, ContactCallback callback) {
        CustomLog.d(TAG, "addContact： nubeNumber：" + nubeNumber);
        if (TextUtils.isEmpty(nubeNumber)) {
            CustomLog.d(TAG, "nubeNumber is empty");
            ResponseEntry response = new ResponseEntry();
            response.status=-1;
            response.content = -1;
            callback.onFinished(response);
        } else if(nubeNumber.equals(AccountManager.getInstance(mContext).getNube())){
            CustomLog.d(TAG,"不能添加自己为好友");
            ResponseEntry response = new ResponseEntry();
            response.status=cannotAdd;
            response.content = -1;
            callback.onFinished(response);
        } else if(null!=getContactInfoByNubeNumber(nubeNumber).getContactId()
                &&!getContactInfoByNubeNumber(nubeNumber).getContactId().isEmpty()){
            CustomLog.d(TAG,"已在通讯录中");
            ResponseEntry response = new ResponseEntry();
            response.status=0;
            response.content = 0;
            callback.onFinished(response);
        }else{

            FriendInfo friendInfo = FriendsManager.getInstance().getFriendByNubeNumber(nubeNumber);

            if(checkIsEmpty(friendInfo.getName())||
                    checkIsEmpty(friendInfo.getNubeNumber())||
                    checkIsEmpty(friendInfo.getHeadUrl())||
                    checkIsEmpty(friendInfo.getWorkUnit())||
                    checkIsEmpty(friendInfo.getWorkUnitType())||
                    ((!friendInfo.getWorkUnitType().equals("1"))&&(!friendInfo.getWorkUnitType().equals("2")))||
                    checkIsEmpty(friendInfo.getDepartment())||
                    checkIsEmpty(friendInfo.getProfessional())||
                    checkIsEmpty(friendInfo.getOfficeTel())||
                    (checkIsEmpty(friendInfo.getNumber())&&checkIsEmpty(friendInfo.getEmail()))||
                    checkIsEmpty(String.valueOf(friendInfo.getUserFrom()))){
                searchUser(nubeNumber,callback);
            }else{
                handleAddContact(friendInfo,callback);
            }
        }
    }

    private boolean checkIsEmpty(String str){
        if(null!=str&&!str.isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    private void handleAddContact(FriendInfo friendInfo,ContactCallback callback){
        final Contact contact = new Contact();
        contact.setFirstName(friendInfo.getName());
        contact.setNickname(friendInfo.getName());
        contact.setName(friendInfo.getName());
        contact.setContactId(CommonUtil.getUUID());
        contact.setNubeNumber(friendInfo.getNubeNumber());
        contact.setHeadUrl(friendInfo.getHeadUrl());
        contact.setWorkUnit(friendInfo.getWorkUnit());
        if(null!=friendInfo.getWorkUnitType()&&friendInfo.getWorkUnitType().equals("1")){
            contact.setWorkUnitType(1);
        }else if(null!=friendInfo.getWorkUnitType()&&friendInfo.getWorkUnitType().equals("2")){
            contact.setWorkUnitType(2);
        }else{
            contact.setWorkUnitType(2);
        }
        contact.setDepartment(friendInfo.getDepartment());
        contact.setProfessional(friendInfo.getProfessional());
        contact.setOfficeTel(friendInfo.getOfficeTel());

        if(null!= friendInfo.getNumber()){
            contact.setNumber(friendInfo.getNumber());
        }else if(null != friendInfo.getEmail()){
            contact.setEmail(friendInfo.getEmail());
        }

        contact.setAppType("mobile");
        contact.setUserFrom(friendInfo.getUserFrom());


        try {
            ContactCallback mycallback = new ContactCallback() {
                @Override
                public void onFinished(ResponseEntry result) {
                    if (result != null && result.status == 0) {
                        CustomLog.d(TAG, "addContact onFinished ");
                        // 若推荐列表中有，则删除后，添加一个新的作为推荐，contactId要变
                        // TODO
                        // 推荐列表 未添加变为已添加
                        RecommendManager.getInstance(mContext).changeBeAdded(contact);
                        // TODO 同步
                        contactDataSync(false);
                        notifyListner();
                    }

                }
            };
            contact.setFullPym(StringHelper.getPingYin(contact.getNickname()));
            CustomLog.d(TAG, "addContact insert start： ");
            CustomAsyncTask task = new CustomAsyncTask();
            task.setCallback(callback);
            task.setCallback(mycallback);
            task.setContentValues(contactToContentValues(contact));
            task.setTable(myTable);
            task.setOpertionStatus(CustomAsyncTask.OPERATE_INSERT);
            task.setContext(mContext);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            } else {
                task.execute("");
            }

        } catch (Exception e) {
            ResponseEntry response = new ResponseEntry();
            response.status=-1;
            response.content = -1;
            callback.onFinished(response);
        }

    }

    private void searchUser(final String nubeNumber, final ContactCallback callback){

        CustomLog.d(TAG,"searchUser by nube: nubeNumber:"+nubeNumber);

        final FriendInfo friendInfo = new FriendInfo();
        MDSAppSearchUsers searchUser = new MDSAppSearchUsers(){
            @Override
            protected void onSuccess(List<MDSDetailInfo> responseContent) {
                CustomLog.d(TAG,"searchUser by nube success: nubeNumber:" + nubeNumber);
                List<MDSDetailInfo> list = responseContent;
                if (list != null && list.size() > 0) {
                    CustomLog.d(TAG,"用户存在");
                    friendInfo.setNubeNumber(list.get(0).getNubeNumber());
                    friendInfo.setName(list.get(0).getNickName());
                    friendInfo.setHeadUrl(list.get(0).getHeadThumUrl());
                    friendInfo.setEmail(list.get(0).getMail());
                    friendInfo.setWorkUnitType(list.get(0).getWorkUnitType());
                    friendInfo.setWorkUnit(list.get(0).getWorkUnit());
                    friendInfo.setDepartment(list.get(0).getDepartment());
                    friendInfo.setProfessional(list.get(0).getProfessional());
                    friendInfo.setOfficeTel(list.get(0).getOfficTel());

                    FriendInfo info = FriendsManager.getInstance().getFriendByNubeNumber(nubeNumber);

                    if(null!=info&&null!=String.valueOf(info.getUserFrom())){
                        friendInfo.setUserFrom(info.getUserFrom());
                    }else{
                        friendInfo.setUserFrom(0);
                    }

                    friendInfo.setIsDeleted(FriendInfo.NOT_DELETE);
                    friendInfo.setNumber(list.get(0).getMobile());
                    handleAddContact(friendInfo,callback);
                }else{
                    CustomLog.d(TAG,"用户不存在");

                    ResponseEntry response = new ResponseEntry();
                    response.status=-5;
                    response.content = -1;
                    callback.onFinished(response);

                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                CustomLog.d(TAG,"searchUser by nube fail: nubeNumber:" + nubeNumber);
                CustomLog.e(TAG, "onFail" + "statusCode:" + statusCode + " statusInfo:" + statusInfo);
                FriendInfo info =  new FriendInfo();
                if(null!=FriendsManager.getInstance().getFriendByNubeNumber(nubeNumber)){
                    info = FriendsManager.getInstance().getFriendByNubeNumber(nubeNumber);
                }
                handleAddContact(info,callback);
            }
        };
        String[] arraylist = { nubeNumber };
        int result = searchUser.appSearchUsers(AccountManager.getInstance(mContext).getToken(), 3, arraylist);
        CustomLog.d(TAG,"searchUser by nube:nubeNumber:" + nubeNumber + " result:" + result);
    }

    @Override
    public void addContact(final Contact contact, ContactCallback callback) {
        CustomLog.d(TAG, "addContact ： " + contact.toString());
        if (contact != null) {
            if (TextUtils.isEmpty(contact.getNubeNumber())) {
                CustomLog.d(TAG, "nubeNumber is empty");
                ResponseEntry response = new ResponseEntry();
                response.status=-1;
                response.content = -1;
                callback.onFinished(response);
            } else if(contact.getNubeNumber().equals(AccountManager.getInstance(mContext).getNube())){
                CustomLog.d(TAG,"不能添加自己为好友");
                ResponseEntry response = new ResponseEntry();
                response.status=cannotAdd;
                response.content = -1;
                callback.onFinished(response);
            } else if(null!=getContactInfoByNubeNumber(contact.getNubeNumber()).getContactId()
                    &&!getContactInfoByNubeNumber(contact.getNubeNumber()).getContactId().isEmpty()){
                CustomLog.d(TAG,"已在通讯录中");
                ResponseEntry response = new ResponseEntry();
                response.status=0;
                response.content = 0;
                callback.onFinished(response);
            }else {
                /** insert */
                // changeCount++;
                try {
                    ContactCallback mycallback = new ContactCallback() {
                        @Override
                        public void onFinished(ResponseEntry result) {
                            if (result != null && result.status == 0) {
                                CustomLog.d(TAG, "addContact onFinished ");
                                // 若推荐列表中有，则删除后，添加一个新的作为推荐，contactId要变
                                // TODO
                                // 推荐列表 未添加变为已添加
                                RecommendManager.getInstance(mContext).changeBeAdded(contact);
                                // TODO 同步
                                contactDataSync(false);
                                notifyListner();
                            }

                        }
                    };
                    contact.setFullPym(StringHelper.getPingYin(contact.getNickname()));
                    CustomLog.d(TAG, "addContact insert start： ");
                    CustomAsyncTask task = new CustomAsyncTask();
                    task.setCallback(callback);
                    task.setCallback(mycallback);
                    task.setContentValues(contactToContentValues(contact));
                    task.setTable(myTable);
                    task.setOpertionStatus(CustomAsyncTask.OPERATE_INSERT);
                    task.setContext(mContext);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                    } else {
                        task.execute("");
                    }

                } catch (Exception e) {
                    ResponseEntry response = new ResponseEntry();
                    response.status=-1;
                    response.content = -1;
                    callback.onFinished(response);
                }
            }
        }

    }

    /**
     * 添加群组到通讯录
     * @param groupId 群组id
     * @param groupName 群组名称
     * @param headUrl 群头像url地址
     * @param callback 回调结果监听
     */
    public void saveGroupToContacts(final String groupId, final String groupName, final String headUrl, ContactCallback callback) {
        CustomLog.d(TAG, "addGroup groupId: " + groupId + " |groupName: " + groupName + " |headUrl: " + headUrl);
        final Contact contact = new Contact();
        contact.setAccountType(1); // 账号类型 0: 个人， 1：群
        contact.setNubeNumber(groupId);
        contact.setName(groupName);
        contact.setNickname(groupName);
        contact.setHeadUrl(headUrl);

        addContact(contact, callback);
    }

    /**
     * 从通讯录删除群组
     * @param groupId 群组id
     * @param callback 回调结果监听
     */
    public void removeGroupFromContacts(final String groupId, ContactCallback callback) {
        groupLogicDeleteContactByNube(groupId, callback);
    }

    public void groupLogicDeleteContactByNube(final String id, ContactCallback callback) {
        try {
            // changeCount++;
            ContactCallback mycallback = new ContactCallback() {
                @Override
                public void onFinished(ResponseEntry result) {
                    if (result != null && result.status == 0) {
                        CustomLog.d(TAG, "logicDeleteContactByNube onFinished ");
                        // 若推荐列表中有，则删除后，添加一个新的作为推荐，contactId要变
//                        // TODO
//                        RecommendManager.getInstance(mContext).changeIdAndBeAdded(id);
                        // TODO 同步
                        contactDataSync(false);
                    }

                }
            };
            ContentValues values = new ContentValues();
            values.put(DBConf.ISDELETED, 1);
            values.put(DBConf.SYNCSTAT, 0);
            CustomAsyncTask task = new CustomAsyncTask();
            task.setCallback(callback);
            task.setCallback(mycallback);
            task.setContentValues(values);
            task.setTable(myTable);
            task.setWhereClause(DBConf.NUBENUMBER + " = ? ");
            task.setWhereArgs(new String[]{id});
            task.setOpertionStatus(CustomAsyncTask.OPERATE_UPDATE);
            task.setContext(mContext);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            } else {
                task.execute("");
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "更新好友表删除状态   Exception " + e);
        }
    }

    /**
     * 更新群组名称
     * @param groupId 群组id
     * @param groupName 群组名称
     * @param callback 回调结果监听
     */
    public void updateGroupName(final String groupId, final String groupName, ContactCallback callback ) {

        CustomLog.d(TAG, "ContactManager::updateGroupName() groupId: " + groupId + " |groupName: " + groupName);

        ContactCallback mycallback = new ContactCallback() {
            @Override
            public void onFinished(ResponseEntry result) {
                if (result != null && result.status == 0) {
                    CustomLog.d(TAG, "updateGroupName onFinished ");
                    // TODO 同步
                    contactDataSync(false);
                }
            }
        };

        CustomAsyncTask task = new CustomAsyncTask();
        task.setCallback(callback);
        task.setCallback(mycallback);
        ContentValues values = new ContentValues();
        if (TextUtils.isEmpty(groupName)) {
            values.put(DBConf.NICKNAME, mContext.getString(R.string.unnamed));
            values.put(DBConf.FIRSTNAME, StringHelper.getHeadChar(mContext.getString(R.string.unnamed)));
            values.put(DBConf.PINYIN, StringHelper.getAllPingYin(mContext.getString(R.string.unnamed)));
            values.put(DBConf.NAME, mContext.getString(R.string.unnamed));
        } else {
            values.put(DBConf.NICKNAME, groupName);
            values.put(DBConf.FIRSTNAME, StringHelper.getHeadChar(groupName));
            values.put(DBConf.PINYIN, StringHelper.getAllPingYin(groupName));
            values.put(DBConf.NAME, groupName);
        }
        values.put(DBConf.SYNCSTAT, 0);

        task.setContentValues(values);
        task.setTable(myTable);
        task.setWhereClause(DBConf.NUBENUMBER + " = ? ");
        task.setWhereArgs(new String[]{checkIsNull(groupId)});

        task.setOpertionStatus(CustomAsyncTask.OPERATE_UPDATE);
        task.setContext(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        } else {
            task.execute("");
        }
    }

    public void updateGroupHeadUrl(final String groupId, final String headUrl, ContactCallback callback) {
        CustomLog.d(TAG, "ContactManager::updateGroupName() groupId: " + groupId + " |groupName: " + headUrl);
        ContactCallback mycallback = new ContactCallback() {
            @Override
            public void onFinished(ResponseEntry result) {
                if (result != null && result.status == 0) {
                    CustomLog.d(TAG, "updateGroupName onFinished ");
                    // TODO 同步
                    contactDataSync(false);
                }
            }
        };
        CustomAsyncTask task = new CustomAsyncTask();
        task.setCallback(callback);
        task.setCallback(mycallback);
        ContentValues values = new ContentValues();
        values.put(DBConf.PICURL,checkIsNull(headUrl));
        values.put(DBConf.SYNCSTAT,0);
        task.setTable(myTable);
        task.setWhereClause(DBConf.NUBENUMBER + " = ? ");
        task.setWhereArgs(new String[]{checkIsNull(groupId)});
        task.setOpertionStatus(CustomAsyncTask.OPERATE_UPDATE);
        task.setContext(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        } else {
            task.execute("");
        }
    }

    private void contactDataSync(boolean isSync) {
        if (dataSync != null) {
            dataSync.cancle();
            dataSync = null;
        }

        dataSync = new ContactSync();
        dataSync.init(mContext, isSync, iContactListChanged, myTable);
        dataSync.start();
    }

    public void doShareContactSync(){
        if(shareDataSync != null){
            shareDataSync.cancle();
            shareDataSync = null;
        }
        shareDataSync = new ShareContactSync();
        shareDataSync.init(mContext,true,iContactListChanged,hpuTable);
        shareDataSync.start();
    }

    private ContentValues contactToContentValues(Contact contact) {
        CustomLog.d(TAG, "contactToContentValues " + contact.toString());
        ContentValues values = new ContentValues();
        if (TextUtils.isEmpty(contact.getContactId())) {
            values.put(DBConf.CONTACTID, CommonUtil.getUUID());
        } else {
            values.put(DBConf.CONTACTID, contact.getContactId());
        }
        if (TextUtils.isEmpty(contact.getNickname())) {
            values.put(DBConf.NICKNAME, mContext.getString(R.string.unnamed));
            values.put(DBConf.FIRSTNAME, StringHelper.getHeadChar(mContext.getString(R.string.unnamed)));
            values.put(DBConf.PINYIN, StringHelper.getAllPingYin(mContext.getString(R.string.unnamed)));
        } else {
            values.put(DBConf.NICKNAME, contact.getNickname());
            values.put(DBConf.FIRSTNAME, StringHelper.getHeadChar(contact.getNickname()));
            values.put(DBConf.PINYIN, StringHelper.getAllPingYin(contact.getNickname()));
        }
        if (TextUtils.isEmpty(contact.getName())) {
            values.put(DBConf.NAME, mContext.getString(R.string.unnamed));
        } else {
            values.put(DBConf.NAME, contact.getName());
        }
        values.put(DBConf.LASTNAME, String.valueOf(contact.getLastTime()));
        values.put(DBConf.ISDELETED, contact.getIsDeleted());
        values.put(DBConf.PHONENUMBER, checkIsNull(contact.getNumber()));
        values.put(DBConf.PICURL, checkIsNull(contact.getPicUrl()));
        values.put(DBConf.USERTYPE, contact.getUserType());
        values.put(DBConf.NUBENUMBER, checkIsNull(contact.getNubeNumber()));
        values.put(DBConf.USERFROM, contact.getUserFrom());
        values.put(DBConf.CONTACTUSERID, checkIsNull(contact.getContactUserId()));
        values.put(DBConf.APPTYPE, checkIsNull(contact.getAppType()));
        values.put(DBConf.SYNCSTAT, 0);

        values.put(DBConf.EMAIL, contact.getEmail());
        values.put(DBConf.ACCOUNT_TYPE, contact.getAccountType());
        values.put(DBConf.WORKUNIT_TYPE, contact.getWorkUnitType());
        values.put(DBConf.WORK_UNIT, contact.getWorkUnit());
        values.put(DBConf.DEPARTMENT, contact.getDepartment());
        values.put(DBConf.PROFESSIONAL, contact.getProfessional());
        values.put(DBConf.OFFICETEL, contact.getOfficeTel());
        values.put(DBConf.SAVE_TO_CONTACTS_TIME, contact.getSaveToContactsTime());

        return values;
    }

    public void logicDeleteContactByNubeNumber(final String nubeNumber, ContactCallback callback) {
        try {
            CustomLog.d(TAG,"logicDeleteContactByNubeNumber, nubeNumber:"+nubeNumber);
            // changeCount++;
            ContactCallback mycallback = new ContactCallback() {

                @Override
                public void onFinished(ResponseEntry result) {
                    if (result != null && result.status == 0) {
                        CustomLog.d(TAG, "logicDeleteContactById onFinished ");
                        // 若推荐列表中有，则删除后，添加一个新的作为推荐，contactId要变
                        // TODO
                        String id = ContactManager.getInstance(mContext)
                                .getContactInfoByNubeNumber(nubeNumber)
                                .getContactId();
                        RecommendManager.getInstance(mContext).changeIdAndBeAdded(id);
                        // TODO 同步
                        contactDataSync(false);
                        notifyListner();
                    }
                }
            };
            ContentValues values = new ContentValues();
            values.put(DBConf.ISDELETED, 1);
            values.put(DBConf.SYNCSTAT, 0);
            CustomAsyncTask task = new CustomAsyncTask();
            task.setCallback(callback);
            task.setCallback(mycallback);
            task.setContentValues(values);
            task.setTable(myTable);
            task.setWhereClause(DBConf.NUBENUMBER + " = ? ");
            task.setWhereArgs(new String[]{nubeNumber});
            task.setOpertionStatus(CustomAsyncTask.OPERATE_UPDATE);
            task.setContext(mContext);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            } else {
                task.execute("");
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "更新好友表删除状态   Exception " + e);
        }

    }

    public void registerUpdateListener(IContactListChanged listener) {
        CustomLog.i(TAG, "registerLoadListener");
        if (iContactListChanged == null) {
            iContactListChanged = new ArrayList<IContactListChanged>();
        }
        if (listener != null && !iContactListChanged.contains(listener)) {
            iContactListChanged.add(listener);
        }
    }

    // 推荐页面和主页面销毁时要调用这个接口，否则一直增加，内存泄露
    public void unRegisterUpdateListener(IContactListChanged listener) {
        if (iContactListChanged != null && iContactListChanged.size() > 0
                && listener != null && iContactListChanged.contains(listener)) {
            iContactListChanged.remove(listener);
        }
    }

    /**
     * 查询所有联系人（无序）
     *
     * @return 所有联系人列表 还要排序
     */
    @Override
    public void getAllContacts(ContactCallback callback,
                               boolean isNeedCustomerService) {
        String sql = "select * from " + myTable
                + " where isDeleted= 0 and accountType= 0  order by fullPym ";
        CustomLog.d(TAG, "getAllContacts " + sql);
        CustomAsyncTask task = new CustomAsyncTask();
        task.setCallback(callback);
        task.setTable(myTable);
        task.setCustomerServiceType(isNeedCustomerService);
        task.setOpertionStatus(CustomAsyncTask.OPERATE_RAWQUERY);
        task.setContext(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sql);
        } else {
            task.execute(sql);
        }
    }

    /**
     * 查询组信息
     *
     * @param callback
     */
    public void getAllGroups(ContactCallback callback) {
        String sql = "select * from " + myTable
                + " where isDeleted= 0 and accountType= 1 order by fullPym ";
        CustomLog.d(TAG, "getAllGroups " + sql);
        CustomAsyncTask task = new CustomAsyncTask();
        task.setCallback(callback);
        task.setTable(myTable);
        task.setCustomerServiceType(false);
        task.setOpertionStatus(CustomAsyncTask.OPERATE_RAWQUERY);
        task.setContext(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sql);
        } else {
            task.execute(sql);
        }
    }

    public void clearInfos() {
        if (dataSync != null) {
            dataSync.cancle();
            dataSync = null;
        }
        if (shareDataSync != null) {
            shareDataSync.cancle();
            shareDataSync = null;
        }
        RecommendManager.getInstance(mContext).clearInfos();
        ContactDBOperater.getInstance(mContext).release();
    }

    private String checkIsNull(String str) {
        if (str != null) {
            return str;
        } else {
            return "";
        }
    }

    public Contact getContactInfoByNubeNumber(String nubenumber) {

        Contact contact = new Contact();

//        String sql = "select * from " + myTable + " where nubeNumber = '"
//                + nubenumber + "' and isDeleted = 0 ";
        String sql1 = "select f.* from (select nubeNumber,number,userFrom,headUrl,email,contactId,nickname,appType,workUnit,workUnitType,department,professional,officeTel,isDeleted from "+
                myTable + " union "+
                "select nubeNumber,null as number,0 as userFrom,headUrl,null as email,null as contactId,nickName as nickname,0 as appType,workUnit,0 as workUnitType,department,null as professional,null as officeTel,0 as isDeleted from "+
                hpuTable+") f"+" where f.nubeNumber = '"+nubenumber+"' and f.isDeleted = 0";

        Cursor c = ContactDBOperater.getInstance(mContext).rawQuery(sql1, myTable);

        if (c != null && c.moveToNext()) {
            contact = getDataFromCursor(c);
        }

        if (c != null) {
            c.close();
        }

        try{
            CustomLog.d(TAG,"getContactInfoByNubeNumber,"
                    + " nubeNumber:" + contact.getNubeNumber()
                    + " number:" + contact.getNumber()
                    + " userFrom:" + contact.getUserFrom()
                    + " headUrl:" + contact.getHeadUrl()
                    + " email:" + contact.getEmail()
                    + " contactId:" + contact.getContactId()
                    + " nickname:" + contact.getNickname()
                    + " appType:" + contact.getAppType()
                    + " workUnit:" + contact.getWorkUnit()
                    + " workUnitType:" + contact.getWorkUnitType()
                    + " department:" + contact.getDepartment()
                    + " professional:" + contact.getProfessional()
                    + " officeTel:" + contact.getOfficeTel());
        }catch(Exception e){
            CustomLog.d(TAG,"getContactInfoByNubeNumber error:"+ e);
        }

        return contact;
    }

    private Contact getDataFromCursor(Cursor cursor){

        Contact contact = new Contact();

        contact.setNubeNumber(cursor.getString(cursor
                .getColumnIndex("nubeNumber")));
        contact.setNumber(cursor.getString(cursor
                .getColumnIndex("number")));
        contact.setUserFrom(cursor.getInt(cursor
                .getColumnIndex("userFrom")));
        contact.setHeadUrl(cursor.getString(cursor
                .getColumnIndex("headUrl")));
        contact.setEmail(cursor.getString(cursor
                .getColumnIndex("email")));
        contact.setContactId(cursor.getString(cursor
                .getColumnIndex("contactId")));
        contact.setNickname(cursor.getString(cursor
                .getColumnIndex("nickname")));
        contact.setAppType(cursor.getString(cursor
                .getColumnIndex("appType")));
        contact.setWorkUnit(cursor.getString(cursor
                .getColumnIndex("workUnit")));
        contact.setWorkUnitType(cursor.getInt(cursor
                .getColumnIndex("workUnitType")));
        contact.setDepartment(cursor.getString(cursor
                .getColumnIndex("department")));
        contact.setProfessional(cursor.getString(cursor
                .getColumnIndex("professional")));
        contact.setOfficeTel(cursor.getString(cursor
                .getColumnIndex("officeTel")));

        return contact;
    }

    private Contact getDataFromDTCursor(Cursor cursor){
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

    private void notifyListner() {
        if (iContactListChanged != null && iContactListChanged.size() > 0) {
            CustomLog.d(TAG, "notifyListner  iContactListChanged ");
            if(AccountManager.getInstance(mContext).getLoginState()== AccountManager.LoginState.ONLINE){
                for (IContactListChanged listener : iContactListChanged) {
                    String sql = "select * from " + myTable
                            + " where isDeleted= 0 order by fullPym ";
                    Cursor cursor = ContactDBOperater.getInstance(mContext).rawQuery(sql,
                            myTable);
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
    }

}
