package cn.redcdn.hvs.im.agent;

import android.content.Context;
import android.text.TextUtils;

import com.butel.connectevent.api.IGroupButelConn_V2_4;

import java.io.Serializable;
import java.util.List;

import cn.redcdn.hvs.im.UrlConstant;
import cn.redcdn.hvs.im.manager.GroupChatInterfaceManager.GroupInterfaceListener;
import cn.redcdn.hvs.im.work.MessageGroupEventParse;
import cn.redcdn.hvs.util.StringUtil;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

public class AppGroupManager {

    private final String TAG = "AppGroupManager";

    private static IGroupButelConn_V2_4 groupClient = null;
    public static AppGroupManager groupMgr = null;
    private GroupInterfaceListener groupListener = null;

    private Context mContext = null;


    public AppGroupManager(Context context) {
        mContext = context;
        groupClient = AppP2PAgentManager.getInstance().getGroupButelP2PAgent();
    }


    public static AppGroupManager getInstance(Context context) {
        if (groupMgr == null || groupClient == null) {
            groupMgr = null;
            groupClient = null;
            groupMgr = new AppGroupManager(context);
        }
        return groupMgr;
    }


    public void setGroupInterfaceListener(GroupInterfaceListener listener) {
        this.groupListener = listener;
    }


    private void setGroupInterfaceBean(int code, GroupInterfaceBean bean) {
        AppP2PAgentManager.getInstance().setGroupInterfaceBean(code, bean);
    }


    private void logToast(String toast) {
        CustomLog.d(TAG, toast);
    }


    /**
     * 描述：创建群组
     * 参数：
     * [IN][OPTIONAL] strGroupName	 群组名字，不设置则根据客户段规则显示（前三个人的昵称
     * [IN][OPTIONAL] strHeadUrl	 群组头像，不设置则客户端显示默认头像。
     * [IN] strUserList	 群组用户列表，逗号隔开的nube号列表，形如:77776666,77776667
     * 返回值：同步返回结果 >0：表示SeqId(与回执带回SeqId一致)， <=0：表示失败
     */
    public int GroupCreate(String strGroupName, List<String> userList) {
        CustomLog.i(TAG, "GroupCreate()");

        int code = AppP2PAgentManager.DEFAULT_ERROR_CODE;
        if (groupClient != null) {
            String strUserList = "";
            if (userList != null) {
                strUserList = StringUtil.list2String(userList, ',');
            }
            logToast("GroupCreate strGroupName:" + strGroupName + " strUserList:" + strUserList);
            code = groupClient.GroupCreate(strGroupName, "", strUserList, "");

            if (groupListener != null && code <= 0) {
                groupListener.onResult(UrlConstant.METHOD_CREATE_GROUP, false, "接口调用失败");
            } else {
                //调用接口成功
                setGroupInterfaceBean(code,
                    new GroupInterfaceBean(-1, "", strGroupName, groupListener));
            }
        }
        logToast("GroupCreate return Code = " + code);
        return code;
    }


    /**
     * 描述：群组信息更新（修改）
     * 参数：
     * [IN]	 strGroupID	 群组ID
     * [IN][OPTIONAL] strGroupName	 群组名字，不设置则根据客户段规则显示（前三个人的昵称）
     * [IN][OPTIONAL] strHeadUrl	 群组头像，不设置则客户端显示默认头像
     * 返回值：同步返回结果 >0：表示SeqId(与回执带回SeqId一致)， <=0：表示失败
     */
    public int GroupUpdate(String strGroupID, String strGroupName) {

        logToast("GroupUpdate strGroupName:" + strGroupName + " strGroupID:" + strGroupID);
        int code = AppP2PAgentManager.DEFAULT_ERROR_CODE;
        if (groupClient != null) {
            code = groupClient.GroupUpdate(strGroupID, strGroupName, "");

            if (groupListener != null && code <= 0) {
                //                showToast("接口调用失败"+"("+code+")");
                groupListener.onResult(UrlConstant.METHOD_EDIT_GROUP, false, "接口调用失败");
            } else {
                setGroupInterfaceBean(code,
                    new GroupInterfaceBean(-1, strGroupID, strGroupName, groupListener));
            }

        }
        logToast("GroupUpdate return Code = " + code);
        return code;
    }


    /**
     * 描述：群组添加成员
     * 参数：
     * [IN]	 strGroupID	 群组ID
     * [IN]	 strUserList	 群组用户列表，逗号隔开的nube号列表，形如:77776666,77776667
     * 返回值：同步返回结果 >0：表示SeqId(与回执带回SeqId一致)， <=0：表示失败
     */
    public int GroupAddUsers(String strGroupID, List<String> userList) {
        int code = AppP2PAgentManager.DEFAULT_ERROR_CODE;
        if (groupClient != null) {
            String strUserList = "";
            if (userList != null) {
                strUserList = StringUtil.list2String(userList, ',');
            }
            logToast("GroupAddUsers strGroupID:" + strGroupID + " strUserList:" + strUserList);
            code = groupClient.GroupAddUsers(strGroupID, strUserList, "");

            if (groupListener != null && code <= 0) {
                //                showToast("接口调用失败"+"("+code+")");
                groupListener.onResult(UrlConstant.METHOD_ADD_USERS, false, "接口调用失败");
            } else {
                setGroupInterfaceBean(code,
                    new GroupInterfaceBean(-1, strGroupID, "", groupListener));
            }
        }
        logToast("GroupAddUsers return Code = " + code);
        return code;
    }


    /**
     * 描述：群组删除成员
     * 参数：
     * [IN]	 strGroupID	 群组ID
     * [IN]	 strUserList	 群组用户列表，逗号隔开的nube号列表，形如:77776666,77776667
     * 返回值：同步返回结果 >0：表示SeqId(与回执带回SeqId一致)， <=0：表示失败
     */
    public int GroupDelUsers(String strGroupID, List<String> userList) {
        int code = AppP2PAgentManager.DEFAULT_ERROR_CODE;
        if (groupClient != null) {
            String strUserList = "";
            if (userList != null) {
                strUserList = StringUtil.list2String(userList, ',');
            }
            logToast("GroupDelUsers strGroupID:" + strGroupID + " strUserList:" + strUserList);
            code = groupClient.GroupDelUsers(strGroupID, strUserList);

            if (groupListener != null && code <= 0) {
                //                showToast("接口调用失败"+"("+code+")");
                groupListener.onResult(UrlConstant.METHOD_DEL_USERS, false, "接口调用失败");
            } else {
                setGroupInterfaceBean(code,
                    new GroupInterfaceBean(-1, strGroupID, "", groupListener));
            }
        }
        logToast("GroupDelUsers return Code = " + code);
        return code;
    }


    /**
     * 描述：退出群组
     * 参数：
     * [IN]	 strGroupID	 群组ID
     * [IN][OPTIONAL]	strNewOwnerNube	群主nub号码，如果是群主退出，newOwnerNube为空则无群主。
     * 返回值：同步返回结果 >0：表示SeqId(与回执带回SeqId一致)， <=0：表示失败
     */
    public int GroupQuit(String strGroupID, String strNewOwnerNube) {
        int code = AppP2PAgentManager.DEFAULT_ERROR_CODE;
        if (groupClient != null) {
            code = groupClient.GroupQuit(strGroupID, strNewOwnerNube);

            if (groupListener != null && code <= 0) {
                //                showToast("接口调用失败"+"("+code+")");
                groupListener.onResult(UrlConstant.METHOD_QUITE_GROUP, false, "接口调用失败");
            } else {
                int type = TextUtils.isEmpty(strNewOwnerNube) ? -1 : 0;
                setGroupInterfaceBean(code,
                    new GroupInterfaceBean(type, strGroupID, "", groupListener));
            }
        }
        logToast("GroupQuit return Code = " + code);
        return code;
    }


    /**
     * 描述：解散群组
     * 参数：
     * [IN]	 strGroupID	 群组ID
     * 返回值：同步返回结果 >0：表示SeqId(与回执带回SeqId一致)， <=0：表示失败
     */
    public int GroupDelete(String strGroupID) {
        int code = AppP2PAgentManager.DEFAULT_ERROR_CODE;
        if (groupClient != null) {
            code = groupClient.GroupDelete(strGroupID);
            if (groupListener != null && code <= 0) {
                //                showToast("接口调用失败"+"("+code+")");
                groupListener.onResult(UrlConstant.METHOD_DEL_GROUP, false, "接口调用失败");
            } else {
                setGroupInterfaceBean(code,
                    new GroupInterfaceBean(-1, strGroupID, "", groupListener));
            }
        }
        logToast("GroupDelete return Code = " + code);
        return code;
    }


    /**
     * 描述：查询群组
     * 参数：
     * [IN]	 strGroupID	 群组ID
     * 返回值：同步返回结果 >0：表示SeqId(与回执带回SeqId一致)， <=0：表示失败
     */
    public int GroupQueryDetail(String strGroupID) {
        int code = AppP2PAgentManager.DEFAULT_ERROR_CODE;
        if (groupClient != null) {
            code = groupClient.GroupQueryDetail(strGroupID);

            if (groupListener != null && code <= 0) {
                //                showToast("接口调用失败"+"("+code+")");
                groupListener.onResult(UrlConstant.METHOD_QUERY_GROUP_DETAIL, false, "接口调用失败");
            } else {
                setGroupInterfaceBean(code,
                    new GroupInterfaceBean(-1, strGroupID, "", groupListener));
            }
        }
        logToast("GroupQueryDetail return Code = " + code);
        return code;
    }


    public int groupQueryDetailBackgroud(String strGroupID, MessageGroupEventParse eventParse) {
        CustomLog.i(TAG, "groupQueryDetailBackgroud()");

        int code = AppP2PAgentManager.DEFAULT_ERROR_CODE;
        if (groupClient != null) {
            code = groupClient.GroupQueryDetail(strGroupID);

            if (code <= 0 && eventParse != null) {
                eventParse.createEmptyGroup();
                eventParse.parseMessage();
            } else {
                AppP2PAgentManager.getInstance().setGroupEventParse(code + "", eventParse);
                setGroupInterfaceBean(code, new GroupInterfaceBean(-1, strGroupID, "", null));
            }
        }

        CustomLog.i(TAG, "groupQueryDetailBackgroud() return Code = " + code);
        return code;
    }


    /**
     * 描述：得到跟自己相关的所有群组
     * 参数：
     * 返回值：同步返回结果 >0：表示SeqId(与回执带回SeqId一致)， <=0：表示失败
     */
    public int GroupGetAll() {
        int code = AppP2PAgentManager.DEFAULT_ERROR_CODE;
        if (groupClient != null) {
            code = groupClient.GroupGetAll();

            if (groupListener != null && code <= 0) {
                //                showToast("接口调用失败"+"("+code+")");
                groupListener.onResult(UrlConstant.METHOD_GET_ALL_GROUP, false, "接口调用失败");
            } else {
                setGroupInterfaceBean(code, new GroupInterfaceBean(-1, "", "", groupListener));
            }

        }
        logToast("GroupGetAll return Code = " + code);
        return code;
    }


    public int GroupAddMyself(String groupId, String exttendInfo) {
        CustomLog.i(TAG, "GroupAddMyself()");

        int code = AppP2PAgentManager.DEFAULT_ERROR_CODE;
        if (groupClient != null) {
            code = groupClient.AddOneselfToGroup(groupId, exttendInfo);
            CustomLog.i(TAG, "AddOneselfToGroup return code = " + code);
            if (groupListener != null && code <= 0) {
                //                showToast("接口调用失败"+"("+code+")");
                groupListener.onResult(UrlConstant.METHOD_GET_ALL_GROUP, false, "接口调用失败");
            } else {
                setGroupInterfaceBean(code, new GroupInterfaceBean(-1, groupId, "", groupListener));
            }

        }
        logToast("GroupGetAll return Code = " + code);
        return code;
    }

    //    private void showToast(String toast){
    //        if (mContext!=null){
    //            LogUtil.d(toast);
    //            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    //        }else {
    //            LogUtil.d("mContext==null"+toast);
    //        }
    //
    //    }


    public class GroupInterfaceBean implements Serializable {
        private static final long serialVersionUID = 1007546912712314048L;

        private final int groupQuitType;

        private final String groupId;

        private final String groupName;

        private final GroupInterfaceListener groupListener;


        public GroupInterfaceBean(int quitType, String gid, String gname, GroupInterfaceListener glistener) {
            this.groupQuitType = quitType;
            this.groupId = gid;
            this.groupName = gname;
            this.groupListener = glistener;
        }


        public int getGroupQuitType() {
            return groupQuitType;
        }


        public String getGroupId() {
            return groupId;
        }


        public String getGroupName() {
            return groupName;
        }


        public GroupInterfaceListener getGroupListener() {
            CustomLog.i(TAG, "getGroupListener()");

            return groupListener;
        }
    }
}
