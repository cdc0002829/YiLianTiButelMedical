package cn.redcdn.hvs.im.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import cn.redcdn.datacenter.friendValidation.AddFriend;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.interfaces.ContactCallback;
import cn.redcdn.hvs.contacts.contact.interfaces.ResponseEntry;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.hvs.contacts.manager.FriendSync;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.agent.AppP2PAgentManager;
import cn.redcdn.hvs.im.bean.FriendInfo;
import cn.redcdn.hvs.im.bean.StrangerMessage;
import cn.redcdn.hvs.im.column.FriendRelationTable;
import cn.redcdn.hvs.im.dao.FriendsRelationDao;
import cn.redcdn.hvs.im.dao.StrangerMsgDao;
import cn.redcdn.hvs.im.interfaces.FriendCallback;
import cn.redcdn.hvs.im.interfaces.IFriendRelation;
import cn.redcdn.hvs.im.task.GetAllFriendAsyncTasks;
import cn.redcdn.log.CustomLog;
import org.json.JSONObject;

import java.util.List;

/**
 * @author LeeDong
 * @version 1.0
 */
public class FriendsManager {

    private String TAG = getClass().getName();
    public static final int RELATION_TYPE_BOTH = 0;//已经是好友
    public static final int RELATION_TYPE_NEGATIVE = 2;//别人请求加我为好友
    public static final int RELATION_TYPE_NONE = -1;//无关系状态
    public static final int RELATION_TYPE_POSITIVE = 1;//主动请求加别人为好友
    public static final int FRIENDSMANAGER_UNINIT = -3;//FriendsManager没有初始化
    private boolean isInit =false;
    private static FriendsManager friendsManager;
    private FriendsRelationDao friendsRelationDao;
    private StrangerMsgDao strangerMsgDao;
    private IFriendRelation iFriendRelation;
    private FriendSync dataSync = null;
    private Context mContext;
    private AddFriend add = null;

    private FriendsManager() {
    }


    public static FriendsManager getInstance() {
        if (null == friendsManager) {
            friendsManager = new FriendsManager();
        }
        return friendsManager;
    }


    /**
     * 添加好友    好友数据库中已经存在好友关系 | 删除该条记录
     */
    public int addFriend(FriendInfo friendInfo) {
        CustomLog.i(TAG,"addFriend  friendInfo== "+friendInfo);
        if (!checkInitState())
            return FRIENDSMANAGER_UNINIT;
        if(friendInfo==null){
            CustomLog.i(TAG,"friendInfo==null");
            return -1;
        }
        CustomLog.i(TAG,"addFriend friendInfo=="+friendInfo.toString());
        FriendInfo friendInfoResult = friendsRelationDao.queryFriendInfoByNubeNumber(friendInfo.getNubeNumber());
        if (friendInfoResult == null) {
            friendsRelationDao.insert(friendInfo);
            if (friendInfo.getRelationType() == FriendInfo.RELATION_TYPE_BOTH) {
                addContact(friendInfo.getNubeNumber());
            }
        } else {
            CustomLog.i(TAG,"该好友信息已在数据库中，DBFriendInfo == " +friendInfoResult);
            switch (friendInfo.getRelationType()) {
                case FriendInfo.RELATION_TYPE_NONE:
                case FriendInfo.RELATION_TYPE_NEGATIVE:
                case FriendInfo.RELATION_TYPE_POSITIVE:
                    modifyFriendInfo(friendInfo);// 此处存在风险    需要保证addFriend方法调用的时候保证    信息是全的
                    // friendsRelationDao.updateFriendRelationStatus(friendInfo.getNubeNumber(), friendInfo.getRelationType());
                    break;
                case FriendInfo.RELATION_TYPE_BOTH:
                    modifyFriendInfo(friendInfo);// 此处存在风险    需要保证addFriend方法调用的时候保证    信息是全的
                    // friendsRelationDao.updateFriendRelationStatus(friendInfo.getNubeNumber(), friendInfo.getRelationType());
                    addContact(friendInfo.getNubeNumber());
            }
        }
        return 0;
    }


    private boolean checkInitState() {
        if (isInit) {
            return true;
        }else{
            CustomLog.i(TAG, "FriendsManager 没有初始化");
            return false;
        }
    }


    /**
     * 删除好友根据还有状态降级   提供给通讯删除好友使用   1、通讯录删除好友  2、同步服务器数据is_delete 为 -1
     *
     *   对状态的修改需要有日志
     *   A  到  B  的状态积
     *
     */
    public int deleteFriend(String nubeNumber) {//此处有隐患   一旦好友关系数据库中没有查到这个人 好友关系状态就不能改变   加期待关系参数
        // 修改好友关系 将好友关系字段降级
        //查询当前好友关系状态   根据当前好友关系状态降级
        CustomLog.i(TAG, "deleteFriend nubeNumber==" + nubeNumber);
        if (!checkInitState())
            return FRIENDSMANAGER_UNINIT;
        Cursor cursor = friendsRelationDao.queryRelationTypeByNubeNumber(nubeNumber);
        if (cursor.moveToNext()) {
            int i = cursor.getInt(cursor.getColumnIndexOrThrow(FriendRelationTable.RELATION_TYPE));
            CustomLog.i(TAG, "deleteFriend RELATION_TYPE==" +i );
            friendsRelationDao.updateFriendRelationIsDelete(nubeNumber,FriendInfo.NOT_DELETE);
            switch (i) {
                case RELATION_TYPE_BOTH://已经是好友
                    friendsRelationDao.updateFriendRelationStatus(nubeNumber, RELATION_TYPE_NEGATIVE);
                    break;
                case RELATION_TYPE_POSITIVE://请求加为好友状态
                    friendsRelationDao.updateFriendRelationStatus(nubeNumber, RELATION_TYPE_NONE);
                    break;
                case -1://无关系状态
                    break;
                case RELATION_TYPE_NEGATIVE://被请求加为好友状态
                    //无论本端删除好友操作  不能改变对端对我的好友请求意图
                    break;
            }
        }
        return 0;
    }


    /**
     * 删除好友记录 (将is_delete字段置为删除状态)
     */
    public void deleteFriendRecord(String nubeNumber) {
        CustomLog.i(TAG, "deleteFriendRecord nubeNumber==" + nubeNumber);
        if (!checkInitState())
            return ;
        friendsRelationDao.updateFriendRelationIsDelete(nubeNumber,FriendInfo.HAS_DELETE);
    }


    /**
     * 删除全部好友记录 (从数据库中删除)
     */
    public void deleteAllFriendInfo() {
        CustomLog.i(TAG, "deleteAllFriendInfo" );
        if (!checkInitState())
            return ;
        friendsRelationDao.deleteAllFriendInfo();
    }


    /**
     * 获取好友关系表中所有好友关系  异步接口
     */
    public void getAllFriends(FriendCallback callback) {
        CustomLog.i(TAG, "getAllFriends");
        if (!checkInitState())
            return ;
        GetAllFriendAsyncTasks getAllFriendAsyncTasks = new GetAllFriendAsyncTasks();
        getAllFriendAsyncTasks.setCallBack(callback);
        getAllFriendAsyncTasks.execute();
    }

    /**
     * 获取好友关系表中所有好友关系 包含isdelete为零 同步接口
     */
    public Cursor getAllFriendsInfoSync() {
        CustomLog.i(TAG,"getAllFriendsInfoSync 同步接口");
        if (!checkInitState())
            return null;
     return friendsRelationDao.getAllFriendIncludeIsDelete();
    }

    /**
     * 根据nube号查询号好友信息（包含好友关系）
     *
     * @return FriendInfo   有可能为空注意判断
     */
    public FriendInfo getFriendByNubeNumber(String nubeNumber) {
        if (!checkInitState())
            return null;
        if(nubeNumber==null||nubeNumber.isEmpty()){
            CustomLog.i(TAG, "getFriendByNubeNumber nubeNumber==null");
            return null;
        }
        CustomLog.i(TAG, "getFriendByNubeNumber nubeNumber==" + nubeNumber);
        FriendInfo friendInfo = friendsRelationDao.queryFriendInfoByNubeNumber(nubeNumber);
        if (friendInfo!=null){
            CustomLog.i(TAG,"friendInfo=="+friendInfo.toString());
        }
        return friendInfo;
    }


    /**
     * 根据nube号查询号好友关系
     */
    public int getFriendRelationByNubeNumber(String nubeNumber) {
        if(nubeNumber==null||"".equals(nubeNumber)||nubeNumber.length()!=8){
            CustomLog.e(TAG, "getFriendRelationByNubeNumber nubeNumber==null|为空值||length!=8 ");
            return -1;
        }
        CustomLog.i(TAG, "getFriendRelationByNubeNumber nubeNumber==" + nubeNumber);
        if (!checkInitState())
            return FRIENDSMANAGER_UNINIT;
        List<Contact> contacts = ContactManager.getInstance(mContext).queryHpuContactByNube(nubeNumber);
        if (contacts.size()>0){
            return RELATION_TYPE_BOTH;
        }
        FriendInfo friendInfo = friendsRelationDao.queryFriendInfoByNubeNumber(nubeNumber);
        if (friendInfo != null) {
            return friendInfo.getRelationType();
        }

        return -1;
    }


    /**
     * 获取好友确认消息
     */
    public Cursor getFriendValidationMsg(String nubeNumber) {//不应该返回cursor 应该返回 list<StrangeMessage>
        if (!checkInitState())
            return null;
        if(nubeNumber==null){
            CustomLog.e(TAG,"getFriendValidationMsg nubeNumber==null");
            return null;
        }
        CustomLog.i(TAG, "getFriendValidationMsg nubeNumber==" + nubeNumber);
        return strangerMsgDao.getMsgByNubeNumber(nubeNumber);
    }

    /**
     * 获取全部好友消息
     */
    public Cursor getAllStrangerMsg() {//不应该返回cursor 应该返回 list<StrangeMessage>
        CustomLog.i(TAG, "getAllStrangerMsg");
        if (!checkInitState())
            return null;
        return strangerMsgDao.getAllStrangerMsg();
    }

    /**
     * 1、修改好友关系  public 测试需要使用所有改成 private
     */
    public void modifyFriendRelation(int relationType, String nubeNumber) {//需要查询是否有还有关系这条记录     封装个人通信录表数据库更新逻辑
        CustomLog.i(TAG, "modifyFriendRelation  relationType=="+relationType+";nubeNumber=="+nubeNumber);
        if (!checkInitState())
            return ;
        if (nubeNumber==null||nubeNumber.isEmpty()||"".equals(String.valueOf(relationType))){
            return;
        }
        friendsRelationDao.updateFriendRelationStatus(nubeNumber, relationType);
    }

    /**
     * 1、更新好友信息    根据视讯号更新所有信息
     */
    public void modifyFriendInfo(FriendInfo friendInfo) {//需要查询是否有还有关系这条记录     封装个人通信录表数据库更新逻辑
        if (!checkInitState())
            return ;
        if (friendInfo==null){
          CustomLog.i(TAG,"friendInfo==null");
        }
        CustomLog.i(TAG, "modifyFriendInfo  friendInfo=="+friendInfo.toString());
        friendsRelationDao.updateFriendInfo(friendInfo);
    }
    /**
     * 1、修改好友关系  public 测试需要使用所有改成 private
     */
    public void deleteAllFriendMsg() {//需要查询是否有还有关系这条记录     封装个人通信录表数据库更新逻辑
        CustomLog.i(TAG, "deleteAllFriendMsg");
        if (!checkInitState())
            return ;
       strangerMsgDao.deleteMsg();
    }


    /**
     * 添加陌生人消息(? 调用时机)
     */
    public int addStrangerMsg(StrangerMessage strangerMessage) {
        if (strangerMessage==null){
            CustomLog.i(TAG,"strangerMessage==null");
            return  -1;
        }
        if (!checkInitState())
            return FRIENDSMANAGER_UNINIT;
        CustomLog.i(TAG, "addStrangerMsg strangerMessage==" + strangerMessage.toString());
        strangerMsgDao.insert(strangerMessage);
        //   询问 调用时机 辨别是否需要 添加一下 更新好友关系为not_Delete 状态
        FriendInfo friendInfoResult = friendsRelationDao.queryFriendInfoByNubeNumber(strangerMessage.getStrangerNubeNumber());
        if(friendInfoResult!=null){
            friendsRelationDao.updateFriendRelationIsDelete(strangerMessage.getStrangerNubeNumber(),FriendInfo.NOT_DELETE);
        }
        return 0;
    }


    /**
     * 获取未读消息数
     *
     * @return 未读消息个数
     */
    public int getNotReadMsgSize() {
        CustomLog.i(TAG, "getNotReadMsgSize");
        if (!checkInitState())
            return FRIENDSMANAGER_UNINIT;
        int size = strangerMsgDao.getNotReadMesSize();
        CustomLog.i(TAG,"size=="+size);
        return size;
    }


    public void setAllMesRead() {
        if (!checkInitState())
            return ;
        CustomLog.i(TAG, "setAllMesRead");
        strangerMsgDao.updateAllMsgIsRead();
    }


    /**
     * 设置固定nubeNumber号的消息为已读
     * @param nubeNumber 视讯号
     */
    public void setMesRead(String nubeNumber) {
        CustomLog.i(TAG, "setMesReadByNube  nubeNumber == " + "nubeNumber");
        if (!checkInitState())
            return ;
        strangerMsgDao.setMesRead( nubeNumber);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (IMConstant.isP2PConnect) { //连接上了
                AppP2PAgentManager.getInstance().setIFriendRelationListener(iFriendRelation);
            }
//            else {
//                AppP2PAgentManager.getInstance().setIFriendRelationListener(null);
//            }
        }
    };


    public void init(Context context) {
        CustomLog.i(TAG,"init");
        isInit=true;
        mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppP2PAgentManager.updatesip);
        mContext.registerReceiver(mReceiver, filter);    //注册广播接收 IM_agent init成功  然后将 IFriendRelation传递给 IM_agent
        friendsRelationDao = new FriendsRelationDao(MedicalApplication.getContext()) {};
        strangerMsgDao = new StrangerMsgDao(MedicalApplication.getContext());
        iFriendRelation = new IFriendRelation() {
            //对端添加自己为好友到通讯录  已经添加到个人通讯录服务器
            @Override public void onFriendAdded(String nubeNumber, String userName, String headUrl) {
                if (!checkInitState())
                    return ;
                CustomLog.i(TAG,"onFriendAdded nubeNumber=="+nubeNumber+";userName=="+userName+";headUrl=="+headUrl);
                FriendInfo friendInfo = friendsRelationDao.queryFriendInfoByNubeNumber(nubeNumber);
                if (friendInfo == null) {//这个时候好友关系为互为好友状态
                    friendsRelationDao.insert(
                        new FriendInfo(nubeNumber, userName, headUrl, FriendInfo.RELATION_TYPE_BOTH, Contact.USER_FROM_SEND_OR_ACCEPT,
                            FriendInfo.NOT_DELETE));//如果存在数据中没有这条数据的情况 添加的话需要加参数
                } else {
                    modifyFriendRelation(RELATION_TYPE_BOTH, nubeNumber);
                    friendsRelationDao.updateFriendRelationIsDelete(nubeNumber,FriendInfo.NOT_DELETE);
                }
                addContact(nubeNumber);
            }

            //a b 为好友   a 删除 b    （a关系 b请a    b关系  b请a）    b删除a  ( a关系 无关系    b关系    无关系  )
            @Override
            public void onFriendDeleted(String nubeNumber) {
                if (!checkInitState())
                    return ;
                if (nubeNumber==null||nubeNumber.isEmpty()){
                    CustomLog.i(TAG,"nubeNumber==null||nubeNumber.isEmpty()");
                    return;
                }
                CustomLog.i(TAG,"onFriendDeleted nubeNumber=="+nubeNumber);
                Cursor cursor = friendsRelationDao.queryRelationTypeByNubeNumber(nubeNumber);
                    while (cursor.moveToNext()){
                    int i = cursor.getInt(cursor.getColumnIndexOrThrow(FriendRelationTable.RELATION_TYPE));
                        CustomLog.i(TAG,"当前好友关系i=="+i);
                        switch (i) {
                            case RELATION_TYPE_BOTH:
                                friendsRelationDao.updateFriendRelationStatus(nubeNumber, RELATION_TYPE_POSITIVE);
                                friendsRelationDao.updateFriendRelationIsDelete(nubeNumber,FriendInfo.NOT_DELETE);
                                break;
                            case RELATION_TYPE_POSITIVE:
                                //主动加别人为好友 对端做任何操作不不能改变我对他的好友请求意图
                                break;
                            case RELATION_TYPE_NONE:
                                break;
                            case RELATION_TYPE_NEGATIVE:
                                CustomLog.i(TAG,"降级好友关系为无关");
                                friendsRelationDao.updateFriendRelationStatus(nubeNumber, RELATION_TYPE_NONE);//及时置为无关  也不讲isDelete修改为0
                                friendsRelationDao.updateFriendRelationIsDelete(nubeNumber,FriendInfo.NOT_DELETE);
                                break;
                        }
                }
            }


            //删除 time 字端     消息到达(   1|主动请求消息         2|被动回复消息  3|普通消息  普通消息暂时不发送到该方法)
            @Override
            public void onMsgArrived(String nubeNumber, String name, String headUrl, String msgContent, String time, int strangeMsgType) {
                // 新的消息到达的时候需要将好友关系数据库中的is_delete更新
                if (!checkInitState())
                    return ;
                CustomLog.i(TAG, " onMsgArrived  nubeNumber ==" + nubeNumber);
                StrangerMessage strangerMessage = new StrangerMessage(nubeNumber, headUrl,
                    name, 1, msgContent, time, 0);
                strangerMsgDao.insert(strangerMessage);
                FriendInfo queryResultFriendInfo = friendsRelationDao.queryFriendInfoByNubeNumber(nubeNumber);
                if (queryResultFriendInfo == null) {
                    switch (strangeMsgType) {
                        case 0:   //    0|主动消息
                            friendsRelationDao.insert(new FriendInfo(nubeNumber, name, headUrl, FriendInfo.RELATION_TYPE_NEGATIVE,
                                Contact.USER_FROM_SEND_OR_ACCEPT, FriendInfo.NOT_DELETE));
                            break;
                        case 1:   //   1|回复请求  原来我可能请求过对端   但是数据库目前没有数据(换手机  主动删除)    这种情况没有考虑
                            friendsRelationDao.insert(new FriendInfo(nubeNumber, name, headUrl, FriendInfo.RELATION_TYPE_POSITIVE,
                                Contact.USER_FROM_SEND_OR_ACCEPT, FriendInfo.NOT_DELETE));
                            break;
                    }
                } else {
                    friendsRelationDao.updateFriendRelationIsDelete(nubeNumber,FriendInfo.NOT_DELETE);
                    int i = queryResultFriendInfo.getRelationType();
                    switch (i) {
                        case RELATION_TYPE_POSITIVE:  //自己请求过加对方为好友
                            if (strangeMsgType == 0) { //  0|主动消息 (添加好友的时候发送的第一条消息)    1|回复请求   将好友关系更新未互为好友关系  添加到通讯录  并且告诉对端更新好友状态为已经是好友
                                friendsRelationDao.updateFriendRelationStatus(nubeNumber, FriendInfo.RELATION_TYPE_BOTH);
                                addContact(nubeNumber);
                                friendRealtionSycn(queryResultFriendInfo);
                                MDSAccountInfo mdsAccountInfo = AccountManager.getInstance(mContext).getAccountInfo();
                                if (mdsAccountInfo!=null&&!"".equals(mdsAccountInfo)){
                                    String senderNubeNum = mdsAccountInfo.getNube();
                                    String receiverNubeNum = queryResultFriendInfo.getNubeNumber();
                                    String senderNickName = mdsAccountInfo.getNickName();
                                    String senderHeadurl = mdsAccountInfo.getHeadThumUrl();
                                    MedicalApplication.shareInstance()
                                        .getFileTaskManager()
                                        .sendAddFriendMsg(senderNubeNum, receiverNubeNum, senderNickName, senderHeadurl);
                                }else{
                                    CustomLog.i(TAG,"获取账号信息为空");
                                }
                            }
                            break;
                        case RELATION_TYPE_NONE://之前是好友，但是相互删除，当前状态为null
                            if (strangeMsgType == 0) {//（存在记录 关系|无关  主动消息）  更新本端|被请求
                                friendsRelationDao.updateFriendRelationStatus(nubeNumber, FriendInfo.RELATION_TYPE_NEGATIVE);
                            }
                            break;
                    }
                }
            }
        };
        friendDataSync();
    }


    /**
     * 向通讯录数据库中添加联系人
     */
    private void addContact(String nubeNumber) {
        if(nubeNumber==null||nubeNumber.isEmpty()){
            CustomLog.i(TAG,"nubeNumber == null 或者 空值 ");
            return;
        }
        CustomLog.i(TAG, "addContact: nubeNumber==" + nubeNumber);
        if (!checkInitState())
            return ;
        Contact contact = ContactManager.getInstance(mContext).getContactInfoByNubeNumber(nubeNumber);
        if (null != contact.getContactId()
            && !contact.getContactId().isEmpty()) {
        } else {
            ContactManager.getInstance(mContext).addContact(nubeNumber, new ContactCallback() { //更新个人通讯录数据库
                @Override public void onFinished(ResponseEntry result) {
                    CustomLog.i(TAG, "statues=" + result.status + "content=" + result.content);
                }
            });
        }
    }


    public void release() {
        CustomLog.i(TAG, "release");
        if (!checkInitState())
            return ;
        isInit=false;
        mContext.unregisterReceiver(mReceiver);
        AppP2PAgentManager.getInstance().removeFriendRelationListener();
        mContext = null;
        friendsManager = null;
        strangerMsgDao = null;
        friendsRelationDao=null;
        iFriendRelation = null;
        dataSync.cancel();
        dataSync = null;
        cancelAdd();
    }


    private void friendDataSync() {
        CustomLog.i(TAG, "friendDataSync");
        if (!checkInitState())
            return ;
        if (dataSync != null) {
            dataSync.cancel();
            dataSync = null;
        }
        dataSync = new FriendSync();
        dataSync.getInstance(MedicalApplication.getContext()).init();
        dataSync.start();
    }




    /**
     * 向好友关系服务器添加好友
     * @param friendInfo
     */
    private void friendRealtionSycn(final FriendInfo friendInfo){
        CustomLog.d(TAG," addFriend start ");
        add = new AddFriend() {
            @Override
            protected void onSuccess(JSONObject responseContent) {
                super.onSuccess(responseContent);
                CustomLog.i(TAG,"AddFriend|onSuccess");
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                if (statusCode==-910){
                    CustomLog.i(TAG,friendInfo.getNubeNumber()+"好友关系服务器上已经是好友");
                }else{   CustomLog.i(TAG,"AddFriend|onFail  statusCode =="+statusCode+"statusInfo =="+statusInfo);};
            }

        };

        int addFriendResult = add.addfriend(AccountManager.getInstance(mContext).getToken(),
            AccountManager.getInstance(mContext).getNube(),
            AccountManager.getInstance(mContext).getName(),
            friendInfo.getNubeNumber(),
            friendInfo.getName());

        if(addFriendResult!=0){
          CustomLog.i(TAG,"addFriend 好友关系服务器同请求失败 addFriendResult=="+addFriendResult);
        }
    }

    public void cancelAdd(){
        if(add != null){
            add.cancel();
            add = null;
        }
    }

}

