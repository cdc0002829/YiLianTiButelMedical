package cn.redcdn.hvs.im.work;

import android.text.TextUtils;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.interfaces.ContactCallback;
import cn.redcdn.hvs.contacts.contact.interfaces.ResponseEntry;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.bean.ShowNameUtil.NameElement;
import cn.redcdn.hvs.im.column.GroupMemberTable;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.dao.ThreadsDao;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.work.MessageReceiveAsyncTask.PrivateMessage;
import cn.redcdn.hvs.util.StringUtil;
import cn.redcdn.log.CustomLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static cn.redcdn.hvs.MedicalApplication.context;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

public class MessageGroupEventParse {

    private final String TAG = "MessageGroupEventParse";

    public static String GROUP_PUBLIC_NUMBER = "10001";

    public static String EVENT_CREATE_GROUP = "CreateGroupEvent";
    public static String EVENT_EDIT_GROUP = "GroupEditEvent";
    public static String EVENT_ADD_USER = "GroupAddUsersEvent";
    public static String EVENT_DEL_USER = "GroupDelUsersEvent";
    public static String EVENT_QUITE_GROUP = "QuitGroupEvent";
    public static String EVENT_DEL_GROUP = "DeleteGroupEvent";

    private PrivateMessage msg = null;
    private static int type = FileTaskManager.NOTICE_TYPE_DESCRIPTION;
    private static String loginNube = "";
    private GroupDao groupDao = null;


    public MessageGroupEventParse(PrivateMessage msg) {
        this.msg = msg;
        this.groupDao = new GroupDao(MedicalApplication.getContext());
        type = FileTaskManager.NOTICE_TYPE_DESCRIPTION;
        loginNube = AccountManager.getInstance(MedicalApplication.getContext())
            .getAccountInfo().nube;
    }


    public static String getGroupPublicNumber() {
        String number = "10001";//im系统消息发送者
        return TextUtils.isEmpty(number) ? GROUP_PUBLIC_NUMBER : number;
    }


    public boolean groupExist(String gid) {

        if (groupDao == null) {
            this.groupDao = new GroupDao(MedicalApplication.getContext());
        }
        return groupDao.existGroup(gid);
    }


    public void createEmptyGroup() {
        if (groupDao != null) {
            groupDao.createGroup(msg.gid, "", "", "", "");
        }
    }


    public boolean parseMessage() {

        String event = "";
        String body = msg.body;
        String gid = "";
        try {
            JSONObject obj = new JSONObject(body);
            event = obj.optString("eventName");
            gid = obj.optString("gid");

            if (EVENT_CREATE_GROUP.equals(event)) {
                // 保存到群组表中
                createGroupEvent(body);
            } else if (EVENT_EDIT_GROUP.equals(event)) {
                // 修改群名
                editGroupEvent(body);
            } else if (EVENT_ADD_USER.equals(event)) {
                // 判定是否包含了自己
                // 包含自己，先同步本群组的详情，然后拼装提示语
                // 否则直接把showname串起来显示
                addUserEvent(body);
            } else if (EVENT_DEL_USER.equals(event)) {
                // 移出成员
                delUserEvent(body);
            } else if (EVENT_QUITE_GROUP.equals(event)) {
                // 退出群组
                quiteGroupEvent(body);
            } else if (EVENT_DEL_GROUP.equals(event)) {
                // 解散群组(暂不需调用)
            } else {
                CustomLog.d(TAG, "未知群组事件,不解析直接丢弃");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }


    private boolean createGroupEvent(String msgBody) {
        String gid = "";
        String gname = "";
        String gmanager = "";
        String headurl = "";
        String createdTime = "";

        try {
            JSONObject obj = new JSONObject(msgBody);
            gid = obj.optString("gid");
            gname = obj.optString("groupName");
            gmanager = obj.optString("managerNube");
            headurl = obj.optString("headUrl");
            createdTime = obj.optString("createDate");

            CustomLog.d(TAG, "createGroupEvent gid=" + gid + " gname=" + gname
                + " gmanager=" + gmanager + " headurl=" + headurl
                + " createdTime=" + createdTime);

            JSONObject opreator = new JSONObject(obj.optString("opreator"));
            String opName = "";
            String opHeadurl = "";
            String opMobile = "";
            String opNube = "";
            opHeadurl = opreator.optString("headUrl");
            opMobile = opreator.optString("mobile");
            opName = opreator.optString("nickName");
            opNube = opreator.optString("nubeNumber");

            CustomLog.d(TAG, "createGroupEvent opName=" + opName + " opHeadurl=" + opHeadurl
                + " opMobile=" + opMobile + " opNube=" + opNube);

            //TODO:保存群组表
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }


    private boolean editGroupEvent(String msgBody) {
        String gid = "";
        String gname = "";
        String headurl = "";
        String opreateTime = "";
        String opNube = "";

        String eventMessage = "";
        try {
            JSONObject obj = new JSONObject(msgBody);
            gid = obj.optString("gid");
            gname = obj.optString("groupName");
            headurl = obj.optString("headUrl");
            opreateTime = obj.optString("opreateTime");
            groupDao.updateGroupName(gid, gname);
            updateGroupNameToDB(gid, gname);

            try {
                JSONObject opreator = new JSONObject(obj.optString("opreator"));
                String opName = "";
                String opHeadurl = "";
                String opMobile = "";

                opHeadurl = opreator.optString("headUrl");
                opMobile = opreator.optString("mobile");
                opName = opreator.optString("nickName");
                opNube = opreator.optString("nubeNumber");

                if (opNube.equals(loginNube)) {
                    // eventMessage = "你修改了群名 "+gname;
                } else {
                    // checkGroupExist(gid);
                    String showName = getShowName(gid, opNube, opName, opMobile);
                    eventMessage = showName +
                        context.getResources().getString(R.string.modified_group_name) + gname +
                        "\"";
                    groupDao.updateGroupName(gid, gname);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(eventMessage)) {
            CustomLog.d(TAG, "editGroupEvent eventMessage=" + eventMessage);
            //收到群名称修改消息，如果会话不存在，则创建新的会话
            createGroupThread(gid);
            insertGroupEventDiscription(msg.msgId, gid, opNube, eventMessage, msg.time);
            return true;
        }

        return false;
    }


    private synchronized void createGroupThread(String gid) {
        ThreadsDao threadsDao = new ThreadsDao(MedicalApplication.getContext());
        if (!threadsDao.isExistThread(gid)) {
            CustomLog.d(TAG, "createGroupThread 群组不存在，创建群组" + gid);
            threadsDao.createThreadFromGroup(gid);
        }
    }


    private boolean addUserEvent(String msgBody) {
        String eventMessage = "";
        String gid = "";
        String gheadurl = "";
        String opNube = "";
        try {
            JSONObject obj = new JSONObject(msgBody);
            gid = obj.optString("gid");
            gheadurl = obj.optString("headUrl");
            groupDao.updateGroupHeadUrl(gid, gheadurl);

            JSONObject opreator = new JSONObject(obj.optString("opreator"));
            String opName = "";
            String opHeadurl = "";
            String opMobile = "";

            opHeadurl = opreator.optString("headUrl");
            opMobile = opreator.optString("mobile");
            opName = opreator.optString("nickName");
            opNube = opreator.optString("nubeNumber");
            String opShowName = getShowName(gid, opNube, opName, opMobile);
            updateGroupHeadUrlToDB(gid, gheadurl);
            JSONArray userlist = new JSONArray(obj.optString("userList"));
            if (userlist != null && userlist.length() > 0) {
                List<String> nubeList = new ArrayList<String>();
                List<GroupMemberBean> memberList = new ArrayList<GroupMemberBean>();
                int length = userlist.length();
                GroupMemberBean bean = null;
                String headurl = "";
                String nickname = "";
                String mobile = "";
                String nube = "";
                int gender = GroupMemberTable.GENDER_MALE;
                for (int i = 0; i < length; i++) {
                    JSONObject user = userlist.getJSONObject(i);
                    headurl = user.optString("headUrl");
                    nickname = user.optString("nickName");
                    mobile = user.optString("mobile");
                    nube = user.optString("nubeNumber");
                    gender = user.optString("gender").trim().equals("女") ?
                             GroupMemberTable.GENDER_FEMALE : GroupMemberTable.GENDER_MALE;

                    //TODO:开放此处判断，方便记录群中曾出现的成员信息，以便在消息表中展现
                    // 20150706 MLK marked:需要增加此判断，否则会因消息的先后顺序（本地接收处理慢，或其他原因）
                    // 导致群成员的状态不正确，产生如0018088的故障
                    // 转而在delUserEvent事件中 addOrUpdateMember形式将成员信息加入表中。
                    if (!groupDao.isGroupMember(gid, nube)) {
                        bean = new GroupMemberBean();
                        bean.setGid(gid);
                        bean.setPhoneNum(mobile);
                        bean.setNubeNum(nube);
                        bean.setHeadUrl(headurl);
                        bean.setNickName(nickname);
                        bean.setGender(gender);

                        ShowNameUtil.NameElement element = ShowNameUtil.getNameElement("", nickname,
                            mobile, nube);
                        String showName = ShowNameUtil.getShowName(element);
                        bean.setShowName(showName);

                        bean.setRemoved(GroupMemberTable.REMOVED_FALSE);

                        memberList.add(bean);
                    }
                    nubeList.add(nube);
                }

                if (opNube.equals(loginNube) && nubeList.get(0).equals(loginNube)) {
                    //主动邀请方
                    eventMessage = "您通过" + "扫描二维码" +
                        context.getResources().getString(R.string.joined_a_group_chat);
                } else if (opNube.equals(loginNube)) {
                    eventMessage = "";
                } else if (nubeList.contains(loginNube)) {
                    //被邀请方
                    //checkGroupExist(gid);//仍然需要，当本地没有群信息时的处理
                    groupDao.addMemberAfterDelete(memberList);

                    List<String> tmpNubeList = new ArrayList<String>();
                    for (int i = 0; i < nubeList.size(); i++) {
                        if (!nubeList.get(i).equals(loginNube)) {
                            tmpNubeList.add(nubeList.get(i));
                        }
                    }
                    //                    nubeList.clear();
                    nubeList.add(opNube);
                    //                    nubeList.add(loginNube);
                    String tmpStr = StringUtil.list2String(getPartMembersName(gid, tmpNubeList),
                        '、');
                    List<String> nameList = getAllMembersName(gid, nubeList);

                    if (nameList == null || nameList.size() == 0) {
                        if (TextUtils.isEmpty(tmpStr)) {
                            eventMessage = opShowName + context.getResources()
                                .getString(R.string.invited_you_to_join_a_group_chat);
                        } else {
                            eventMessage = opShowName +
                                context.getResources().getString(R.string.invite_you_and) + tmpStr +
                                context.getResources().getString(R.string.joined_a_group_chat);
                        }

                    } else {
                        if (tmpStr.length() == 0) {
                            eventMessage = opShowName + context.getResources()
                                .getString(
                                    R.string.invited_you_to_join_the_group_chat__there_are_people_who_participate_in_group_chat) +
                                StringUtil.list2String(nameList, '、');
                        } else {
                            eventMessage = opShowName +
                                context.getResources().getString(R.string.invite_you_and) + tmpStr +
                                context.getResources()
                                    .getString(
                                        R.string.joined_the_group_chat_people_involved_in_group_chat_there) +
                                StringUtil.list2String(nameList, '、');
                        }
                    }
                } else {
                    //群中第三方人员
                    //checkGroupExist(gid);
                    groupDao.addMemberAfterDelete(memberList);
                    String tmpStr = StringUtil.list2String(getPartMembersName(gid, nubeList), '、');
                    if (opShowName.equals(tmpStr)) {
                        eventMessage = tmpStr + context.getResources().getString(R.string.grouped_by_scanning_twodimensional_code);
                    } else {
                        eventMessage = opShowName + context.getResources().getString(R.string.invite) + tmpStr +
                            context.getResources().getString(R.string.joined_a_group_chat);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(eventMessage)) {
            //收到添加好友消息，如果会话不存在，则创建新的会话
            createGroupThread(gid);

            CustomLog.d(TAG, "addUserEvent eventMessage=" + eventMessage);
            insertGroupEventDiscription(msg.msgId, gid, opNube, eventMessage, msg.time);
            return true;
        }
        return false;
    }


    private boolean delUserEvent(String msgBody) {
        String eventMessage = "";
        String gid = "";
        String gheadurl = "";
        String opNube = "";
        try {
            JSONObject obj = new JSONObject(msgBody);
            gid = obj.optString("gid");
            gheadurl = obj.optString("headUrl");
            groupDao.updateGroupHeadUrl(gid, gheadurl);

            JSONObject opreator = new JSONObject(obj.optString("opreator"));
            String opName = "";
            String opHeadurl = "";
            String opMobile = "";

            opHeadurl = opreator.optString("headUrl");
            opMobile = opreator.optString("mobile");
            opName = opreator.optString("nickName");
            opNube = opreator.optString("nubeNumber");
            String opShowName = getShowName(gid, opNube, opName, opMobile);

            updateGroupHeadUrlToDB(gid, gheadurl);

            JSONArray userlist = new JSONArray(obj.optString("userList"));
            if (userlist != null && userlist.length() > 0) {

                List<String> nubeList = new ArrayList<String>();
                List<GroupMemberBean> memberList = new ArrayList<GroupMemberBean>();
                int length = userlist.length();
                GroupMemberBean bean = null;
                String headurl = "";
                String nickname = "";
                String mobile = "";
                String nube = "";
                int gender = GroupMemberTable.GENDER_MALE;

                for (int i = 0; i < length; i++) {
                    JSONObject user = userlist.getJSONObject(i);
                    headurl = user.optString("headUrl");
                    nickname = user.optString("nickName");
                    mobile = user.optString("mobile");
                    nube = user.optString("nubeNumber");
                    gender = user.optString("gender").trim().equals("女") ?
                             GroupMemberTable.GENDER_FEMALE : GroupMemberTable.GENDER_MALE;

                    bean = new GroupMemberBean();
                    bean.setGid(gid);
                    bean.setPhoneNum(mobile);
                    bean.setNubeNum(nube);
                    bean.setHeadUrl(headurl);
                    bean.setNickName(nickname);
                    bean.setGender(gender);

                    NameElement element = ShowNameUtil.getNameElement("", nickname, mobile, nube);
                    String showName = ShowNameUtil.getShowName(element);
                    bean.setShowName(showName);

                    bean.setRemoved(GroupMemberTable.REMOVED_TRUE);

                    memberList.add(bean);

                    nubeList.add(nube);
                }

                if (opNube.equals(loginNube)) {
                    //主动踢人方
                    // eventMessage = "你将"+StringUtil.list2String(nameList, '、')+context.getResources().getString(R.string.moved_out_of_group_chat);
                } else if (nubeList.contains(loginNube)) {
                    //被踢方
                    //checkGroupExist(gid);
                    eventMessage = context.getResources().getString(R.string.you_are_being) + opShowName + context.getResources().getString(R.string.moved_out_of_group_chat);
                    //					for(int i = 0;i<nubeList.size();i++){
                    //						String nube = nubeList.get(i);
                    //						groupDao.setMemberRemoved(gid, nube);
                    //					}
                    groupDao.addOrUpdateMember(memberList);
                } else {
                    //群中第三方人员
                    // 产品要求 第三方不感知
                    //checkGroupExist(gid);
                    // eventMessage = opShowName+"将"+StringUtil.list2String(getPartMembersName(gid, nubeList), '、')+context.getResources().getString(R.string.moved_out_of_group_chat);
                    //					for(int i = 0;i<nubeList.size();i++){
                    //						String nube = nubeList.get(i);
                    //						groupDao.setMemberRemoved(gid, nube);
                    //					}
                    groupDao.addOrUpdateMember(memberList);
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(eventMessage)) {
            CustomLog.d(TAG, "delUserEvent eventMessage=" + eventMessage);
            insertGroupEventDiscription(msg.msgId, gid, opNube, eventMessage, msg.time);
            return true;
        }
        return false;
    }


    //更新群的头像到数据库中
    private void updateGroupHeadUrlToDB(String gid, String headUrl) {
        ContactManager.getInstance(MedicalApplication.getContext())
            .updateGroupHeadUrl(gid, headUrl, new ContactCallback() {
                @Override
                public void onFinished(ResponseEntry result) {
                    CustomLog.d(TAG, "更换图像 result:" + result.status);
                }
            });
    }


    private void updateGroupNameToDB(String gid, String newGroupName) {
        ContactManager.getInstance(MedicalApplication.getContext())
            .updateGroupName(gid, newGroupName, new ContactCallback() {
                @Override
                public void onFinished(ResponseEntry result) {
                    CustomLog.d(TAG, "updateGroupName result.status:" + result.status);
                }
            });
    }


    private boolean quiteGroupEvent(String msgBody) {
        String gid = "";
        String gheadurl = "";
        String opreateTime = "";
        String eventMessage = "";
        String newOwner = "";
        String oldOwner = "";
        String opNube = "";
        try {
            JSONObject obj = new JSONObject(msgBody);
            gid = obj.optString("gid");
            gheadurl = obj.optString("headUrl");
            groupDao.updateGroupHeadUrl(gid, gheadurl);

            opreateTime = obj.optString("opreateTime");
            newOwner = obj.optString("managerNube");
            oldOwner = groupDao.getGroupManager(gid);

            JSONObject opreator = new JSONObject(obj.optString("opreator"));
            String opName = "";
            String opHeadurl = "";
            String opMobile = "";
            opHeadurl = opreator.optString("headUrl");
            opMobile = opreator.optString("mobile");
            opName = opreator.optString("nickName");
            opNube = opreator.optString("nubeNumber");
            String opShowName = getShowName(gid, opNube, opName, opMobile);
            if (opNube.equals(loginNube)) {
                // eventMessage = "你退出了群聊 ";
                groupDao.setMemberRemoved(gid, opNube);
                // 保护性措施
                groupDao.delGroup(gid);
                groupDao.delMembersByGid(gid);
                new ThreadsDao(MedicalApplication.getContext()).deleteThread(gid);
            } else {
                //checkGroupExist(gid);
                if (opNube.equals(oldOwner)) {
                    // 群主退群
                    if (!TextUtils.isEmpty(newOwner)) {
                        if (loginNube.equals(newOwner)) {
                            // 新群主移交至本人
                            eventMessage = opShowName + context.getResources().getString(R.string.move_the_group_to_you_and_exit_the_group_chat);
                        } else {
                            String othername = getShowName(gid, newOwner, "", "");
                            eventMessage = opShowName + context.getResources().getString(R.string.transfer_the_group_leader) + othername + context.getResources().getString(R.string.and_quit_group_chat);
                        }
                        groupDao.updateGroupManager(gid, newOwner);
                    } else {
                        // 群主直接退群(当前版本无需提醒)
                        // eventMessage = context.getResources().getString(R.string.lord) + opShowName + context.getResources().getString(R.string.lord);
                        groupDao.updateGroupManager(gid, "");
                    }
                    groupDao.setMemberRemoved(gid, opNube);
                } else {
                    // 普通成员退群
                    if (loginNube.equals(oldOwner)) {
                        // 本人是群主，需要提醒(当前版本无需提醒)
                        // eventMessage = opShowName + context.getResources().getString(R.string.lord);
                    } else {
                        // 产品要求 第三方普通成员不感知
                        // eventMessage = opShowName+context.getResources().getString(R.string.lord);
                    }
                    groupDao.setMemberRemoved(gid, opNube);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(eventMessage)) {
            CustomLog.d(TAG, "quiteGroupEvent eventMessage=" + eventMessage);
            insertGroupEventDiscription(msg.msgId, gid, opNube, eventMessage, msg.time);
            return true;
        }

        return false;
    }


    /**
     * 查询list里的人名
     */
    private List<String> getPartMembersName(String gid, List<String> includedList) {

        List<String> nameList = null;

        if (includedList != null && includedList.size() > 0) {
            if (includedList.size() == 1) {
                GroupMemberBean bean = groupDao.queryGroupMember(gid, includedList.get(0));
                String showName = bean != null ? bean.getDispName() : includedList.get(0);
                nameList = new ArrayList<String>();
                nameList.add(showName);
            } else {
                LinkedHashMap<String, GroupMemberBean> map = groupDao.queryGroupMembers(gid);
                int length = includedList.size();
                GroupMemberBean bean = null;
                nameList = new ArrayList<String>();
                for (int i = 0; i < length; i++) {
                    bean = map != null ? map.get(includedList.get(i)) : null;
                    if (bean != null) {
                        nameList.add(bean.getDispName());
                    } else {
                        nameList.add(includedList.get(i));
                    }
                }
            }
        }
        return nameList;
    }


    /**
     * 排除list里的人名
     */
    private List<String> getAllMembersName(String gid, List<String> excludedList) {

        List<String> nameList = null;
        LinkedHashMap<String, GroupMemberBean> map = groupDao.queryGroupMembers(gid);
        if (map != null) {
            if (excludedList != null && excludedList.size() > 0) {
                int length = excludedList.size();
                for (int i = 0; i < length; i++) {
                    map.remove(excludedList.get(i));
                }
            }
            nameList = new ArrayList<String>();
            Iterator<Entry<String, GroupMemberBean>> it = map.entrySet()
                .iterator();
            while (it.hasNext()) {
                Map.Entry<String, GroupMemberBean> entry = it.next();
                String nuber = entry.getKey();
                GroupMemberBean bean = entry.getValue();
                if (bean != null) {
                    nameList.add(bean.getDispName());
                } else {
                    nameList.add(nuber);
                }
            }
        }
        return nameList;
    }


    private String getShowName(String gid, String nube, String nickName, String mobile) {
        String showName = "";
        if (groupDao.isGroupMember(gid, nube)) {
            GroupMemberBean bean = groupDao.queryGroupMember(gid, nube);
            showName = bean != null ? bean.getDispName() : nube;
        } else {

            NameElement element = ShowNameUtil.getNameElement("", nickName, mobile, nube);
            showName = ShowNameUtil.getShowName(element);

        }
        return showName;
    }


    /**
     * @param msgid 用于排重
     */
    public static void insertGroupEventDiscription(String msgid, String gid, String optionNube, String eventMsg, String sendtime) {
        CustomLog.d("MessageGroupEventParse",
            " msgId:" + msgid + " optionNube:" + optionNube + " eventMsg:" + eventMsg);
        NoticesDao noticeDao = new NoticesDao(MedicalApplication.getContext());
        NoticesBean repeatData = noticeDao.getNoticeById(msgid);
        if (repeatData != null) {
            CustomLog.d("MessageGroupEventParse", "msgid=" + msgid + ",重复数据不插入");
        } else {
            noticeDao.createAddFriendTxt(msgid, gid, optionNube, null, "", type, eventMsg, gid,
                null, sendtime);
        }
    }
}
