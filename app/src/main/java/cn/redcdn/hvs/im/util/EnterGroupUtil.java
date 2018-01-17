package cn.redcdn.hvs.im.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import org.json.JSONObject;

import cn.redcdn.datacenter.groupchat.GroupAddoneself;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.ThreadsDao;
import cn.redcdn.hvs.im.manager.GroupChatInterfaceManager;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;

/**
 * Desc
 * Created by wangkai on 2017/9/15.
 */

public class EnterGroupUtil implements GroupChatInterfaceManager.GroupInterfaceListener{

    private final String TAG = "EnterGroupUtil";

    private Context mContext;
    private String mCurrGroupId;
    private EnterGroupListener mEnterGroupListener;
    private GroupChatInterfaceManager mInterfaceManager = null;
    private Dialog mLoadingDialog = null;
    public boolean isEnterGroupSuccess;

    public EnterGroupUtil(Context context,String groupId) {
        mContext = context;
        mCurrGroupId = groupId;
        isEnterGroupSuccess = false;
        mInterfaceManager = new GroupChatInterfaceManager((Activity) mContext, this);
    }

    public void enterGroup(){
        if (TextUtils.isEmpty(mCurrGroupId)) {
            CustomLog.e(TAG, "groupId is null");
            return;
        }
        CustomLog.d(TAG, "EnterGroup gid:" + mCurrGroupId);
        String loginNubeNumber = AccountManager.getInstance(mContext).getNube();
        GroupDao groupDao = new GroupDao(mContext);
        if (groupDao.isGroupMember(mCurrGroupId, loginNubeNumber)) {
            enterChatActivity(mCurrGroupId);
        } else {
            //加入群组成功，但请求群详情失败
            if (isEnterGroupSuccess && !TextUtils.isEmpty(mCurrGroupId)) {
                showLoadingView(mContext.getString(R.string.adding_group),
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                removeLoadingView();
                            }
                        }, true);
                mInterfaceManager.queryGroupDetail(mCurrGroupId);
            } else {
                addGroupRequest(mCurrGroupId);
            }
        }
    }

    private void addGroupRequest(final String groupId) {
        showLoadingView(mContext.getString(R.string.adding_group),
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        removeLoadingView();
                    }
                }, true);

        GroupAddoneself request = new GroupAddoneself() {
            @Override
            protected void onSuccess(JSONObject responseContent) {
                CustomLog.d(TAG, "加入群组成功");
                isEnterGroupSuccess = true;
                mInterfaceManager.queryGroupDetail(groupId);
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                CustomLog.d(TAG,
                        "加入群组失败，请重试,statusCode:" + statusCode + " statusInfo" + statusInfo);
                //                CustomToast.show(mContext,"加入群组失败，请重试",1);
                if (statusCode == -212) {
                    isEnterGroupSuccess = true;
                    mInterfaceManager.queryGroupDetail(groupId);
                } else if (statusCode == MDS_TOKEN_DISABLE) {
                    removeLoadingView();
                    mEnterGroupListener.OnFailed(statusCode,statusInfo);
                    AccountManager.getInstance(mContext).tokenAuthFail(statusCode);
                } else {
                    removeLoadingView();
                    mEnterGroupListener.OnFailed(statusCode,statusInfo);
                    CustomToast.show(mContext, statusInfo, Toast.LENGTH_LONG);
                }

            }
        };
        CustomLog.d(TAG, "addGroupRequest gid:" + groupId + " token:"
                + AccountManager.getInstance(mContext).getToken());
        request.addOneselfToGroup(groupId, AccountManager.getInstance(mContext).getToken());
    }

    private void enterChatActivity(String groupId) {

        ThreadsDao threadsDao = new ThreadsDao(mContext);
        if (!threadsDao.isExistThread(mCurrGroupId)) {
            threadsDao.createThreadFromGroup(mCurrGroupId);
            CustomLog.d(TAG, "群会话不存在，建立会话");
        }
        Intent i = new Intent(mContext, ChatActivity.class);
        i.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                ChatActivity.VALUE_NOTICE_FRAME_TYPE_LIST);
        i.putExtra(ChatActivity.KEY_CONVERSATION_ID, groupId);
        i.putExtra(ChatActivity.KEY_CONVERSATION_TYPE, ChatActivity.VALUE_CONVERSATION_TYPE_MULTI);
        i.putExtra(ChatActivity.KEY_CONVERSATION_NUBES, groupId);
        mContext.startActivity(i);
        mEnterGroupListener.OnSuccess();
    }

    private void showLoadingView(String message,
                                   final DialogInterface.OnCancelListener listener, boolean cancelAble) {
        try {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        } catch (Exception ex) {
            CustomLog.d(TAG, ex.toString());
        }
        mLoadingDialog = CommonUtil.createLoadingDialog(mContext, message, listener);
        mLoadingDialog.setCancelable(cancelAble);
        mLoadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    listener.onCancel(dialog);
                }
                return false;
            }
        });
        try {
            mLoadingDialog.show();
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
    }

    private void removeLoadingView() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            try {
                mLoadingDialog.dismiss();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            mLoadingDialog = null;
        }
    }

    public void setEnterGroupListener(EnterGroupListener listener){
        mEnterGroupListener = listener;
    }

    @Override
    public void onResult(String _interfaceName, boolean isSuccess, String result) {
        if (isSuccess) {
            CustomLog.d(TAG, "查询群信息 成功");
            enterChatActivity(mCurrGroupId);
            removeLoadingView();
        } else {
            removeLoadingView();
            CustomLog.d(TAG, "查询群信息失败");
            mEnterGroupListener.OnFailed(-1,"查询群组失败");
            CustomToast.show(mContext, mContext.getString(R.string.query_group_info_fail_try), 1);
        }
    }


    public interface EnterGroupListener{
        void OnSuccess();
        void OnFailed(int statusCode, String statusInfo);
    }
}
