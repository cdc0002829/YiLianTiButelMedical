package cn.redcdn.hvs.contacts.manager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.redcdn.datacenter.friendValidation.AddFriend;
import cn.redcdn.datacenter.friendValidation.DeleteFriend;
import cn.redcdn.datacenter.friendValidation.SyncFriend;
import cn.redcdn.datacenter.friendValidation.data.friendInfo;
import cn.redcdn.datacenter.friendValidation.data.friends;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.interfaces.ResponseEntry;
import cn.redcdn.hvs.im.bean.FriendInfo;
import cn.redcdn.hvs.im.column.FriendRelationTable;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.log.CustomLog;

/**
 * Created by Administrator on 2017/5/7 0007.
 */

public class FriendSync extends Thread {
    private static final String TAG = FriendSync.class.getSimpleName();
    private int state;
    private final int INIT_STATE = 0;
    private final int SYNC_STATE = 1;
    private final int STOP_STATE = 2;
    private Context context;
    private AddFriend add = null;
    private DeleteFriend delete = null;
    private SyncFriend sync = null;
    private static FriendSync mInstance = null;

    public synchronized static FriendSync getInstance(Context context){
        if(mInstance==null){
            CustomLog.d(TAG, "FriendSync getInstance");
            mInstance = new FriendSync();
            mInstance.context = context;
        }
        return mInstance;
    }

    @Override
    public void run(){
        if(state == INIT_STATE){
            state = SYNC_STATE;
            doDataSync();
        }else{
            CustomLog.d(TAG,"do nothing state = " + state);
        }
        super.run();
    }

    private void doDataSync(){
        if(state!=STOP_STATE){
            doGetFriendsRealtions();
        }else{
            CustomLog.d(TAG,"doDataSync: state==STOP_STATE");
        }
    }

    /**
     * 调好友关系服务器的获取好友关系接口
     */
    private void doGetFriendsRealtions(){
        CustomLog.d(TAG,"doGetFriendsRealtions");
        try{
            if(state != STOP_STATE){
                sync = new SyncFriend(){};
                String syncTime = "0";
                SharedPreferences sharedPreferences = MedicalApplication.shareInstance().getSharedPreferences("SYNC_TIME", Activity.MODE_PRIVATE);
                    //读取sharedPreferences
                    if(null!=sharedPreferences.getString(AccountManager.getInstance(context).getNube(),null)){
                        CustomLog.d(TAG,"sharedPreferences 视讯号:"
                                +AccountManager.getInstance(context).getNube()
                                +"读取 sharedPreferences的时间:"
                                +sharedPreferences.getString(AccountManager.getInstance(context).getNube(),null));
                        syncTime = sharedPreferences.getString(AccountManager.getInstance(context).getNube(),null);
                    }else{
                        CustomLog.d(TAG,"syncTime=0");
                        syncTime="0";
                    }

                CustomLog.d(TAG,"Thread.currentThread().getId():" + Thread.currentThread().getId());

                try{
                    CustomLog.d(TAG,"sync.syscfriend start");
                    friends result = sync.syscfriend(AccountManager.getInstance(context).getToken(),
                            AccountManager.getInstance(context).getNube(),syncTime);
                    if(null!=result&&(result.code.equals("0")||result.code.equals("-912"))){
                        CustomLog.d(TAG,"doGetFriendsRealtions success， code：" + result.code);

                            CustomLog.d(TAG,"result.synchronousTime:"+result.synchronousTime
                                    +" result.friendList.size():"+result.friendList.size());

                            if(null!=result.synchronousTime&&!result.synchronousTime.isEmpty()){
                                CustomLog.d(TAG,"存入sharedPreferences的时间"+ result.synchronousTime+" sharedPreferences 视讯号："+AccountManager.getInstance(context).getNube());
                                //存入sharedPreferences
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(AccountManager.getInstance(context).getNube(),result.synchronousTime);
                                editor.commit();
                            }

                            List<friendInfo> list = new ArrayList<friendInfo>();
                            list = result.friendList;
                            if(list != null && state != STOP_STATE){
                                compareFriendsRelations(list,getAllFriendsRelations());
                            }

                    }else{
                        CustomLog.e(TAG,"doGetFriendsRealtions fail");
                        CustomLog.d(TAG,"60秒后重新执行doGetFriendsRealtions()");
                        Thread.currentThread().sleep(60000);
                        CustomLog.d(TAG,"重新执行doGetFriendsRealtions()");
                        doGetFriendsRealtions();
                    }

                    }catch (Exception e){
                    CustomLog.e(TAG,"doGetFriendsRealtions Exception:" + e);
                    CustomLog.d(TAG,"60秒后重新执行doGetFriendsRealtions()");
                    Thread.currentThread().sleep(60000);
                    CustomLog.d(TAG,"重新执行doGetFriendsRealtions()");
                    doGetFriendsRealtions();
                }

            }
        }catch(Exception e){
            CustomLog.e(TAG,"doGetFriendsRealtions "+e);
        }
    }

    /**
     * 删除好友接口，对好友关系进行降级
     * @param nubeNumber
     * @param callback
     */
    public void deleteFriend(final String nubeNumber, final DeleteFriendCallback callback){
        CustomLog.d(TAG," deleteFriend start " +"friend nubeNumber:"+ nubeNumber);

        delete = new DeleteFriend() {

            @Override
            protected void onSuccess(JSONObject responseContent) {
                super.onSuccess(responseContent);
                CustomLog.d(TAG,"deleteFriend onSuccess");
                ResponseEntry response = new ResponseEntry();
                response.status=0;
                response.content = responseContent;

                MedicalApplication.getFileTaskManager().sendDeleteFriendMsg(
                        AccountManager.getInstance(context).getNube(),
                        nubeNumber);

                int result = FriendsManager.getInstance().deleteFriend(nubeNumber);

                callback.onFinished(response);
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                CustomLog.d(TAG,"deleteFriend onFail statusCode:"+String.valueOf(statusCode)+" statusInfo:"+statusInfo);
                ResponseEntry response = new ResponseEntry();

                if(statusCode==-911){
                    CustomLog.d(TAG,"该好友已删除");

                    MedicalApplication.getFileTaskManager().sendDeleteFriendMsg(
                            AccountManager.getInstance(context).getNube(),
                            nubeNumber);

                    int result = FriendsManager.getInstance().deleteFriend(nubeNumber);
                    response.status=-2;
                    response.content = -1;
                    callback.onFinished(response);
                }else if(statusCode==-914){
                    CustomLog.d(TAG,"该好友不存在");

                    MedicalApplication.getFileTaskManager().sendDeleteFriendMsg(
                            AccountManager.getInstance(context).getNube(),
                            nubeNumber);

                    int result = FriendsManager.getInstance().deleteFriend(nubeNumber);
                    response.status=-4;
                    response.content = -1;
                    callback.onFinished(response);
                }else{
                    response.status=-1;
                    response.content = -1;
                    callback.onFinished(response);
                }

            }

        };

        int deleteFriendResult = delete.deletefriend(AccountManager.getInstance(context).getToken(),
                AccountManager.getInstance(context).getNube(),nubeNumber);

        if(deleteFriendResult!=0){
            CustomLog.d(TAG,"deleteFriendResult!=0");
            ResponseEntry response = new ResponseEntry();
            response.status=-1;
            response.content = -1;
            callback.onFinished(response);
        }

    }

    /**
     * 添加好友接口
     * @param friendInfo
     * @param callback
     */
    public void addFriend(final FriendInfo friendInfo,final AddFriendCallback callback){
        CustomLog.d(TAG," addFriend start ");

        add = new AddFriend() {

            @Override
            protected void onSuccess(JSONObject responseContent) {
                super.onSuccess(responseContent);
                ResponseEntry response = new ResponseEntry();
                response.status=0;
                response.content = responseContent;

                MedicalApplication.getFileTaskManager().sendAddFriendMsg(
                        AccountManager.getInstance(context).getNube(),
                        friendInfo.getNubeNumber(),
                        AccountManager.getInstance(context).getName(),
                        AccountManager.getInstance(context).getAccountInfo().getHeadThumUrl());

                friendInfo.setRelationType(FriendInfo.RELATION_TYPE_BOTH);
                int result = FriendsManager.getInstance().addFriend(friendInfo);

                callback.onFinished(response);
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                ResponseEntry response = new ResponseEntry();

                if(statusCode==-910){
                    CustomLog.d(TAG,"该好友已存在");
                    MedicalApplication.getFileTaskManager().sendAddFriendMsg(
                            AccountManager.getInstance(context).getNube(),
                            friendInfo.getNubeNumber(),
                            AccountManager.getInstance(context).getName(),
                            AccountManager.getInstance(context).getAccountInfo().getHeadThumUrl());

                    friendInfo.setRelationType(FriendInfo.RELATION_TYPE_BOTH);
                    int result = FriendsManager.getInstance().addFriend(friendInfo);
                    response.status=-3;
                    response.content = -1;
                    callback.onFinished(response);
                }else{
                    response.status=-1;
                    response.content = -1;
                    callback.onFinished(response);
                }
            }

        };

        int addFriendResult = add.addfriend(AccountManager.getInstance(context).getToken(),
                AccountManager.getInstance(context).getNube(),
                AccountManager.getInstance(context).getName(),
                friendInfo.getNubeNumber(),
                friendInfo.getName());

        if(addFriendResult!=0){
            ResponseEntry response = new ResponseEntry();
            response.status=-1;
            response.content = -1;
            callback.onFinished(response);
        }

    }

    /**
     * 从数据库中查好友关系
     * @return
     */
    private Map<String,FriendInfo> getAllFriendsRelations(){
        CustomLog.d(TAG,"getAllFriendsRelations");
        Map<String,FriendInfo> list = new HashMap<String, FriendInfo>();
        if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE) {
            Cursor cursor = null;
            try {

                cursor = FriendsManager.getInstance().getAllFriendsInfoSync();

            } catch (Exception e) {
                CustomLog.e(TAG,"getAllFriendsRelations Exception");
            }

            if(cursor != null){
                CustomLog.d(TAG,"getAllFriendsRelations cursor != null");
                list = new HashMap<String,FriendInfo>();
                while (cursor.moveToNext()){
                    list.put(getDataFromCursor(cursor).getNubeNumber(),getDataFromCursor(cursor));
                }
            }

            if(cursor != null){
                cursor.close();
                cursor = null;
            }

        }

        return list;
    }

    /**
     * 对比本地的好友关系数据库和好友关系服务器的数据
     * @param list 好友关系服务器的数据
     * @param friendsRelations 本地的好友关系数据库
     */
    private void compareFriendsRelations(List<friendInfo> list, Map<String,FriendInfo> friendsRelations){
        CustomLog.d(TAG," cfr start ");
        if( list != null && list.size() > 0) {
            for (int a = 0 ; a<list.size(); a ++) {
                try {
                    if(null!=friendsRelations){
                        FriendInfo info1 = friendsRelations.get(list.get(a).getNube());
                        if (info1 != null) {
                            CustomLog.d(TAG,"cfr info1!=null,nubenumber: "+list.get(a).getNube());
                            if (list.get(a).getStatus() == 1 && info1.getRelationType() != FriendInfo.RELATION_TYPE_BOTH) {
                                CustomLog.d(TAG, "same nubeNumber, addFriend");
                                handleAddFriend(info1);
                            } else if (list.get(a).getStatus() == -1&& info1.getRelationType() == FriendInfo.RELATION_TYPE_BOTH) {
                                CustomLog.d(TAG, "same nubeNumber, deleteFriend");
                                modifyFriendRelation(list.get(a).getNube());
                            } else if(list.get(a).getStatus() == -2) {
                                CustomLog.d(TAG, "same nubeNumber, status == -2,deleteFriendRecord ");
                                //调friendmanager的删除好友记录（数据库记录）接口
                                FriendsManager.getInstance().deleteFriendRecord(list.get(a).getNube());
                            }
                        }else{
                            CustomLog.d(TAG,"cfr info1==null,nubenumber: "+list.get(a).getNube());
                            handleNewFriend(list.get(a));
                        }
                    }else{
                        CustomLog.d(TAG,"cfr friendsRelations==null");
                        handleNewFriend(list.get(a));
                    }
                } catch (Exception e) {
                    CustomLog.e(TAG, "cfr fail" + e.toString());
                }
            }
        }else{
            CustomLog.d(TAG," cfr list == null || list.size() <= 0 ");
        }
    }

    private void handleNewFriend(friendInfo info){
        CustomLog.d(TAG,"handleNewFriend");
        FriendInfo info2= new FriendInfo(info.getNube(),
                info.getNickName(),
                Contact.USER_FROM_SYNC,
                FriendInfo.HAS_DELETE); //设置delete状态，目的是在新朋友页面不显示该好友
        if(info.getStatus() == 1){  //是好友关系
            CustomLog.d(TAG, "handleNewFriend, addFriend");
            handleAddFriend(info2);
        }else if(info.getStatus() == -1){  //已不是好友关系
            CustomLog.d(TAG, "handleNewFriend, status==-1");
        }else if(info.getStatus() == -2){  //用户已在用户中心删除
            CustomLog.d(TAG, "handleNewFriend, status==-2");
        }
    }

    private void handleAddFriend(FriendInfo info){

        info.setRelationType(FriendInfo.RELATION_TYPE_BOTH);

        try{
            CustomLog.d(TAG,"handleAddFriend, info.getNubeNumber():"+info.getNubeNumber()
                    +" info.getName():"+info.getName()
                    +" info.getRelationType():"+String.valueOf(info.getRelationType())
                    +" info.getIsDeleted():"+String.valueOf(info.getIsDeleted()));
        }catch(Exception e){
            CustomLog.e(TAG, "handleAddFriend log fail" + e.toString());
        }

        CustomLog.d(TAG,"handleAddFriend ");

        int result = FriendsManager.getInstance().addFriend(info);
        if(result==0){
            CustomLog.d(TAG,"addFriend success");
        }else{
            CustomLog.d(TAG,"addFriend fail, result:"+String.valueOf(result));
        }
    }

    private void modifyFriendRelation(String nubeNumber){
        CustomLog.d(TAG,"modifyFriendRelation");
        FriendsManager.getInstance().modifyFriendRelation(FriendInfo.RELATION_TYPE_POSITIVE,nubeNumber);
    }

    private FriendInfo getDataFromCursor(Cursor cursor){
        FriendInfo friendInfo = new FriendInfo();
        friendInfo.setNubeNumber(cursor.getString(cursor
                .getColumnIndex(FriendRelationTable.NUBE_NUMBER)));
        friendInfo.setName(cursor.getString(cursor
                .getColumnIndex(FriendRelationTable.NAME)));
        friendInfo.setHeadUrl(cursor.getString(cursor
                .getColumnIndex(FriendRelationTable.HEAD_URL)));
        friendInfo.setRelationType(cursor.getInt(cursor
                .getColumnIndex(FriendRelationTable.RELATION_TYPE)));
        friendInfo.setWorkUnit(cursor.getString(cursor
                .getColumnIndex(FriendRelationTable.WORK_UNIT)));
        friendInfo.setDepartment(cursor.getString(cursor
                .getColumnIndex(FriendRelationTable.DEPARTMENT)));
        friendInfo.setProfessional(cursor.getString(cursor
                .getColumnIndex(FriendRelationTable.PROFESSIONAL)));
        friendInfo.setOfficeTel(cursor.getString(cursor
                .getColumnIndex(FriendRelationTable.OFFICETEL)));
        friendInfo.setUserFrom(cursor.getInt(cursor
                .getColumnIndex(FriendRelationTable.USER_FROM)));
        friendInfo.setIsDeleted(cursor.getInt(cursor
                .getColumnIndex(FriendRelationTable.IS_DELETED)));
        friendInfo.setEmail(cursor.getString(cursor
                .getColumnIndex(FriendRelationTable.EMAIL)));
        friendInfo.setWorkUnitType(cursor.getString(cursor
                .getColumnIndex(FriendRelationTable.WORK_UNIT_TYPE)));
        friendInfo.setNumber(cursor.getString(cursor
                .getColumnIndex(FriendRelationTable.PHONE_NUMBER)));
        CustomLog.d(TAG,"getDataFromCursor:"+friendInfo.toString());
        return friendInfo;
    }

    public void init(){
        CustomLog.d(TAG,"init");
        state = INIT_STATE;
    }

    public void cancelAdd(){
        if(add != null){
            add.cancel();
            add = null;
        }
    }

    public void cancelDelete(){
        if(delete != null){
            delete.cancel();
            delete = null;
        }
    }

    public void cancel(){
        state = STOP_STATE;
        if(add != null){
            add.cancel();
            add = null;
        }
        if(delete != null){
            delete.cancel();
            delete = null;
        }
        if(sync !=null){
            sync.cancel();
            sync = null;
        }
    }

}
