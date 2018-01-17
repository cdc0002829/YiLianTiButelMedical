package cn.redcdn.hvs.meeting.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.bean.FriendInfo;
import cn.redcdn.hvs.im.bean.StrangerMessage;
import cn.redcdn.hvs.im.column.StrangerMessageTable;
import cn.redcdn.hvs.im.interfaces.FriendCallback;
import cn.redcdn.hvs.im.interfaces.ResponseEntry;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.log.CustomLog;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/15 0015.
 */
public class FriendRelationActivity extends BaseActivity implements View.OnClickListener {

    private Button button, button1, button2, button3, button4, button5, button6, button7, button8,button9,button10,button11;
    private TextView manageInfo;
    private String info ="" ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_relation_activity);
        initView();
    }


    private void initView() {
        button = (Button) findViewById(R.id.button);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button6 = (Button) findViewById(R.id.button6);
        button7 = (Button) findViewById(R.id.button7);
        button8 = (Button) findViewById(R.id.button8);
        button9 = (Button) findViewById(R.id.button9);
        button10 = (Button) findViewById(R.id.button10);
        button11 = (Button) findViewById(R.id.button11);
        manageInfo = (TextView) findViewById(R.id.textView_manage_info);
        button.setOnClickListener(this);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
        button6.setOnClickListener(this);
        button7.setOnClickListener(this);
        button8.setOnClickListener(this);
        button9.setOnClickListener(this);
        button10.setOnClickListener(this);
        button11.setOnClickListener(this);
    }


    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                FriendsManager.getInstance().addFriend(new FriendInfo("60006000", "蔡国测试", "www,hvs,com", FriendInfo.RELATION_TYPE_POSITIVE,
                    "119@qq.com", "2", "304", "erke",
                    "主任", "12345678", 1, 1,"15261200078"));
                FriendsManager.getInstance().addFriend(new FriendInfo("60006001", "薛睿测试", "www,hvs,com", FriendInfo.RELATION_TYPE_BOTH,
                    "119@qq.com", "1", "304", "erke",
                    "主任", "12345678", 1, 0,"15261200078"));
                break;
            case R.id.button1:
                FriendsManager.getInstance().deleteFriendRecord( "60006000");
                manageInfo.setText("");
                break;
            case R.id.button2:
                FriendsManager.getInstance().modifyFriendRelation(1, "12345678");
                break;
            case R.id.button3:
                final FriendInfo friendInfo = FriendsManager.getInstance().getFriendByNubeNumber("60006000");
                if (friendInfo != null) {
                    manageInfo.setText(friendInfo.toString());
                } else {
                    manageInfo.setText("friendInfo=null");
                }

                break;
            case R.id.button4:
                FriendsManager.getInstance().addStrangerMsg(new StrangerMessage("60006000", "www.hvs.com",
                    "蔡国测试", 0, "插入消息成功", String.valueOf(System.currentTimeMillis()), 0));
                FriendsManager.getInstance().addStrangerMsg(new StrangerMessage("60006001", "www.hvs.com",
                    "薛睿测试", 0, "插入消息成功", String.valueOf(System.currentTimeMillis()), 0));
                break;
            case R.id.button5:
               Cursor cursor = FriendsManager.getInstance().getAllStrangerMsg();
                List strangeMsgList = new ArrayList();
                if (cursor != null) {
                    info ="";
                    while (cursor.moveToNext()) {
                        StrangerMessage strangerMessage =  cursor2Entity(cursor);
                        info =  info + strangerMessage.toString();
                        strangeMsgList.add(strangerMessage);
                    }
                    manageInfo.setText("strangeMsgList.size()=="+strangeMsgList.size()+" "+info);
                } else {
                    manageInfo.setText("cursor=null");
                }
                break;
            case R.id.button6:
                FriendsManager.getInstance().getAllFriends(new FriendCallback() {
                    @Override public void onFinished(ResponseEntry result) {
                        info="size == "  +((List<FriendInfo>)result.content).size();
                        for(FriendInfo friendInfo: (List<FriendInfo>)result.content){
                            info=  info+friendInfo.toString();
                        }
                        manageInfo.setText(info);
                    }
                });
                break;
            case R.id.button7:
              int i=  FriendsManager.getInstance().getNotReadMsgSize();
                manageInfo.setText(i+"");
              //   StrangerMsgDao strangeMsgDao = new StrangerMsgDao(this);
              //
              //   List list =strangeMsgDao.getNotReadMes();
              //   if(null == list || list.size() ==0 ){
              //       manageInfo.setText(info+"list.size()==0  或者  null == list");
              //   }else{
              //       info="size == " +list.size();
              //       for(StrangerMessage strangerMessage : strangeMsgDao.getNotReadMes()){
              //           manageInfo.setText(info+strangerMessage.toString());
              //       }
              //   }
                break;
            case R.id.button8:
                FriendsManager.getInstance().setAllMesRead();
                break;
            case R.id.button9:
                FriendsManager.getInstance().deleteAllFriendMsg();
                manageInfo.setText("");

                break;
            case R.id.button10://查询所有好友关系
            FriendsManager.getInstance().getAllFriends(new FriendCallback() {
                @Override public void onFinished(ResponseEntry result) {
                    List<FriendInfo>  list =(List<FriendInfo>) result.content;
                    if (list != null) {
                        info = "info.size()=="+list.size()+" ";
                        for (FriendInfo friendInfo : list){
                            info= info+friendInfo.toString()+" " ;
                        }
                        manageInfo.setText(info.toString());
                    } else {
                        manageInfo.setText("friendInfo=null");
                    }
                }
            });
                break;
            case R.id.button11://删除所有好友关系
                FriendsManager.getInstance().deleteAllFriendInfo();
        }
    }

    private StrangerMessage cursor2Entity(Cursor cursor) {
        if (cursor == null || cursor.isClosed()||cursor.getCount()==0) {
            return null;
        }
        StrangerMessage strangerMessage=new StrangerMessage();
        try {
            strangerMessage.setStrangerNubeNumber(cursor.getString(cursor.getColumnIndexOrThrow(StrangerMessageTable.STRANGER_NUBE_NUMBER)));
            strangerMessage.setStrangerHead(cursor.getString(cursor.getColumnIndexOrThrow(StrangerMessageTable.STRANGER_HEAD)));
            strangerMessage.setStrangerName(cursor.getString(cursor.getColumnIndexOrThrow(StrangerMessageTable.STRANGER_NAME)));
            strangerMessage.setMsgDirection(cursor.getInt(cursor.getColumnIndexOrThrow(StrangerMessageTable.MSG_DIRECTION)));
            strangerMessage.setMsgContent(cursor.getString(cursor.getColumnIndexOrThrow(StrangerMessageTable.MSG_CONTENT)));
            strangerMessage.setIsRead(cursor.getInt(cursor.getColumnIndexOrThrow(StrangerMessageTable.IS_Read)));
            strangerMessage.setTime(cursor.getString(cursor.getColumnIndexOrThrow(StrangerMessageTable.TIME)));
        } catch (Exception e) {
            CustomLog.e("cursor2Entity Exception", e.toString());
            return null;
        }
        return strangerMessage;
    }
}
