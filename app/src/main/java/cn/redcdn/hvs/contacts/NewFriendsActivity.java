package cn.redcdn.hvs.contacts;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.contacts.NewFriendsListViewAdapter.buttonClick;
import cn.redcdn.hvs.contacts.contact.AddContactActivity;
import cn.redcdn.hvs.contacts.contact.ContactTransmitConfig;
import cn.redcdn.hvs.contacts.contact.interfaces.ResponseEntry;
import cn.redcdn.hvs.contacts.manager.AddFriendCallback;
import cn.redcdn.hvs.contacts.manager.FriendSync;
import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.bean.FriendInfo;
import cn.redcdn.hvs.im.bean.StrangerMessage;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.interfaces.FriendCallback;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.hvs.im.activity.ChatActivity.VALUE_CONVERSATION_TYPE_SINGLE;

/**
 * Created by Administrator on 2017/5/3 0003.
 */

public class NewFriendsActivity extends BaseActivity {

    private Button btnBack;
    private Button btnAddFriend;
    private ListView lvNewFriends;
    private NewFriendsListViewAdapter newFriendsAdapter;
    private List<FriendInfo> newfriendInfoList;
    private int mPosition;
    private final int MSG_DATA_CHANGED = 601;
    private final int MSG_FRIEND_CHAT = 602;
    private FriendRelationObserver observeFriendRelation;
    private StrangerMessageObserver observeStrangeRelation;
    private LinearLayout llNoNewFriend;
    private Button btnNoAddFriend;
    private boolean getAllContactComplete = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_DATA_CHANGED:
                    newFriendsAdapter = new NewFriendsListViewAdapter(NewFriendsActivity.this,newfriendInfoList,new bc(),new blc());
                    lvNewFriends.setAdapter(newFriendsAdapter);

                            if(newfriendInfoList.size()==0&&getAllContactComplete){
                                llNoNewFriend.setVisibility(View.VISIBLE);
                            }else{
                                llNoNewFriend.setVisibility(View.INVISIBLE);
                            }

                    break;
                case MSG_FRIEND_CHAT:
                    //发送“已是好友，可以聊天”消息
                    int position = msg.arg1;
                    NoticesDao noticeDao = new NoticesDao(NewFriendsActivity.this);
                    noticeDao.createAddFriendMsgTip(newfriendInfoList.get(position).getName(),newfriendInfoList.get(position).getNubeNumber());
//                    enterChatActivity(newfriendInfoList.get(position).getNubeNumber());
                    break;
                default:
                    break;
            }
        }
    };

    //进入聊天界面
    private void enterChatActivity(String nubeNumber) {
        if (nubeNumber == null) {
            CustomToast.show(this, getString(R.string.nub_exit), 1);
            return;
        }

        if (nubeNumber.length() == 0) {
            CustomToast.show(this, getString(R.string.nub_exit), 1);
            return;
        }

        Intent i = new Intent(NewFriendsActivity.this, ChatActivity.class);
        i.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                ChatActivity.VALUE_NOTICE_FRAME_TYPE_NUBE);
        i.putExtra(ChatActivity.KEY_CONVERSATION_NUBES,
                nubeNumber);
        i.putExtra(ChatActivity.KEY_CONVERSATION_SHORTNAME,
                nubeNumber);
        i.putExtra(ChatActivity.KEY_CONVERSATION_TYPE,
                VALUE_CONVERSATION_TYPE_SINGLE);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friends);        
        initUI();
        initListener();
        initData();
    }

    private void initUI(){
        btnBack = (Button) findViewById(R.id.new_friends_back);
        btnAddFriend = (Button) findViewById(R.id.new_friends_add);
        lvNewFriends = (ListView) findViewById(R.id.new_friends_list);
        llNoNewFriend = (LinearLayout) findViewById(R.id.no_newfriend_layout);
        btnNoAddFriend = (Button) findViewById(R.id.newfriend_addfriend_btn);
    }

    private void initData(){

        getAllContactComplete = false;

        //设置所有陌生人消息为已读
        FriendsManager.getInstance().setAllMesRead();

        newfriendInfoList = new ArrayList<FriendInfo>();

        getAllFriends();

    }

    private void getAllFriends(){
        FriendsManager.getInstance().getAllFriends(
                new FriendCallback(){
                    @Override
                    public void onFinished(cn.redcdn.hvs.im.interfaces.ResponseEntry result){
                        newfriendInfoList=(List<FriendInfo>)result.content;

                        if(null!=newfriendInfoList&&newfriendInfoList.size()>0){
                            CustomLog.d(TAG,"newfriendInfoList.size():"+newfriendInfoList.size());
                        }else{
                            CustomLog.d(TAG,"newfriendInfoList==null||newfriendInfoList.size()==0");
                        }
                        getAllContactComplete = true;
                        mHandler.sendEmptyMessage(MSG_DATA_CHANGED);

                        if (observeFriendRelation == null) {
                            observeFriendRelation = new FriendRelationObserver();
                            getContentResolver().registerContentObserver(
                                    ProviderConstant.Friend_Relation_URI, true,
                                    observeFriendRelation);
                        }

                        if (observeStrangeRelation == null) {
                            observeStrangeRelation = new StrangerMessageObserver();
                            getContentResolver().registerContentObserver(
                                    ProviderConstant.Strange_Message_URI, true,
                                    observeStrangeRelation);
                        }

                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initListener(){
        btnBack.setOnClickListener(mbtnHandleEventListener);
        btnAddFriend.setOnClickListener(mbtnHandleEventListener);
        btnNoAddFriend.setOnClickListener(mbtnHandleEventListener);
    }

    public class blc implements NewFriendsListViewAdapter.buttonLongClick{
        @Override
        public void itemLongClicked(View v,final int position){

            getAllFriends();

        }
    }

    public class bc implements buttonClick {
        @Override
        public void itemClicked(View v,final int position){
            if(null!=newfriendInfoList&&newfriendInfoList.size()>0&&newfriendInfoList.get(position).getRelationType()==FriendInfo.RELATION_TYPE_NONE){
                //请求添加好友：通过im发送请求添加好友的陌生人消息，并通过friendmanager修改本地好友状态
                Intent addFriendIntent = new Intent();
                addFriendIntent.setClass(NewFriendsActivity.this, VerificationActivity.class);
                addFriendIntent.putExtra("nubeNumber",newfriendInfoList.get(position).getNubeNumber());
                startActivityForResult(addFriendIntent, ContactTransmitConfig.REQUEST_VERIFICATION_CODE);

            }else if(null!=newfriendInfoList&&newfriendInfoList.size()>0&&newfriendInfoList.get(position).getRelationType()==FriendInfo.RELATION_TYPE_NEGATIVE){
                //接受好友请求：调friendsync接口添加好友（请求好友关系服务器成功后，通过im发送价位好友的陌生人消息，并通过friendmanager修改本地好友状态）

                newfriendInfoList.get(position).setRelationType(FriendInfo.RELATION_TYPE_BOTH);
                newfriendInfoList.get(position).setUserFrom(0);

                NewFriendsActivity.this.showLoadingView(getString(R.string.adding_friend), new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        CustomToast.show(NewFriendsActivity.this, getString(R.string.cancle_add),
                                Toast.LENGTH_LONG);
                        FriendSync.getInstance(NewFriendsActivity.this).cancelAdd();
                    }
                });

                FriendSync.getInstance(NewFriendsActivity.this).addFriend(newfriendInfoList.get(position),new AddFriendCallback(){
                @Override
                public void onFinished(ResponseEntry result) {
                    NewFriendsActivity.this.removeLoadingView();
                    if(result.status==0){
                        CustomLog.d(TAG,getString(R.string.add_friend_seccess));
                        CustomToast.show(NewFriendsActivity.this,getString(R.string.add_friend_seccess), Toast.LENGTH_LONG);

                        Message msg = new Message();
                        msg.what = MSG_FRIEND_CHAT;
                        msg.arg1 = position;
                        mHandler.sendMessage(msg);

                    }else if(result.status==-3){
                        CustomLog.d(TAG,"好友已存在");
                        CustomToast.show(NewFriendsActivity.this,getString(R.string.add_friend_seccess), Toast.LENGTH_LONG);

                        Message msg = new Message();
                        msg.what = MSG_FRIEND_CHAT;
                        msg.arg1 = position;
                        mHandler.sendMessage(msg);
                    }else{
                        CustomLog.d(TAG,"添加好友失败 result.status="+result.status);
                        CustomToast.show(NewFriendsActivity.this,getString(R.string.add_friend_fail),Toast.LENGTH_LONG);
                    }
                }
            });

            }

            mPosition = position;
            newFriendsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void todoClick(int id){
        super.todoClick(id);
        switch(id){
            case R.id.new_friends_back:
                FriendsManager.getInstance().setAllMesRead();
                finish();
                break;
            case R.id.new_friends_add:
                Intent intent = new Intent();
                intent.setClass(this, AddContactActivity.class);
                startActivity(intent);
                break;
            case R.id.newfriend_addfriend_btn:
                Intent intent2 = new Intent();
                intent2.setClass(this, AddContactActivity.class);
                startActivity(intent2);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        CustomLog.d(TAG, "resultfrom" + resultCode);

        if (resultCode == ContactTransmitConfig.RESULT_VERIFICATION_CODE) {

            String message = data.getStringExtra("message");

            newFriendsAdapter.notifyDataSetChanged();

            FriendInfo friendInfo = new FriendInfo();
            friendInfo.setNubeNumber(newfriendInfoList.get(mPosition).getNubeNumber());
            friendInfo.setName(newfriendInfoList.get(mPosition).getName());
            friendInfo.setHeadUrl(newfriendInfoList.get(mPosition).getHeadUrl());
            friendInfo.setRelationType(FriendInfo.RELATION_TYPE_POSITIVE);
            friendInfo.setWorkUnitType(String.valueOf(newfriendInfoList.get(mPosition).getWorkUnitType()));
            friendInfo.setWorkUnit(newfriendInfoList.get(mPosition).getWorkUnit());
            friendInfo.setDepartment(newfriendInfoList.get(mPosition).getDepartment());
            friendInfo.setProfessional(newfriendInfoList.get(mPosition).getProfessional());
            friendInfo.setOfficeTel(newfriendInfoList.get(mPosition).getOfficeTel());
            friendInfo.setUserFrom(Integer.valueOf(newfriendInfoList.get(mPosition).getUserFrom()));
            friendInfo.setIsDeleted(FriendInfo.NOT_DELETE);
            if(null!=newfriendInfoList.get(mPosition).getEmail()){
                friendInfo.setEmail(newfriendInfoList.get(mPosition).getEmail());
            }
            if(null!=newfriendInfoList.get(mPosition).getNumber()){
                friendInfo.setNumber(newfriendInfoList.get(mPosition).getNumber());
            }

            MedicalApplication.getFileTaskManager().sendStrangerMsg(AccountManager.getInstance(this).getNube(),
                    friendInfo.getNubeNumber(), AccountManager.getInstance(this).getName(),
                    AccountManager.getInstance(this).getAccountInfo().getHeadThumUrl(), message, false);

            StrangerMessage strangerMessage = new StrangerMessage(
                    friendInfo.getNubeNumber(),
                    friendInfo.getHeadUrl(),
                    friendInfo.getName(),
                    0,
                    message,
                    String.valueOf(System.currentTimeMillis()), 1);

            int addStrangerMessageResult = FriendsManager.getInstance().addStrangerMsg(strangerMessage);

            if (addStrangerMessageResult == 0) {
                CustomLog.d(TAG, "addStrangerMsg 成功");
            } else {
                CustomLog.d(TAG, "addStrangerMsg 失败， addStrangerMessageResult：" + addStrangerMessageResult);
            }

            int addFriendResult = FriendsManager.getInstance().addFriend(friendInfo);

            if (addFriendResult == 0) {
                CustomLog.d(TAG, "addFriend 成功");
            } else {
                CustomLog.d(TAG, "addFriend 失败， addFriendResult：" + addFriendResult);
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (observeFriendRelation != null) {
            getContentResolver().unregisterContentObserver(observeFriendRelation);
            observeFriendRelation = null;
        }

        if (observeStrangeRelation != null) {
            getContentResolver().unregisterContentObserver(observeStrangeRelation);
            observeStrangeRelation = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        FriendsManager.getInstance().setAllMesRead();
    }

    /**
     * 监听好友关系表
     */
    private class FriendRelationObserver extends ContentObserver{

        public FriendRelationObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.d(TAG,"好友关系数据库数据发生变更");
            getAllFriends();
        }

    }

    /**
     * 监听陌生人消息表
     */
    private class StrangerMessageObserver extends ContentObserver{

        public StrangerMessageObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.d(TAG,"陌生人消息数据库数据发生变更");
            mHandler.sendEmptyMessage(MSG_DATA_CHANGED);
        }

    }

}
