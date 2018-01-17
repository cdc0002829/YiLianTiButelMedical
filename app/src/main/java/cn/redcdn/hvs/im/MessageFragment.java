package cn.redcdn.hvs.im;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.HomeActivity;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.contacts.contact.AddContactActivity;
import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.activity.SelectLinkManActivity;
import cn.redcdn.hvs.im.adapter.MessageListCursorAdapter;
import cn.redcdn.hvs.im.agent.AppP2PAgentManager;
import cn.redcdn.hvs.im.asyncTask.QueryNoticeAsyncTask;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.common.ThreadPoolManger;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.manager.GroupChatInterfaceManager;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.work.MessageReceiveAsyncTask;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.PopDialogActivity;
import cn.redcdn.hvs.util.ScannerActivity;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

import static android.content.Context.MODE_PRIVATE;
import static cn.redcdn.hvs.im.activity.ChatActivity.KEY_SERVICE_NUBE_INFO;

/**
 * Created by thinkpad on 2017/2/14.
 */
public class MessageFragment extends BaseFragment {

    // 视图
    private View contentView = null;
    // sip服务连接提示view
    private View leftTipView;
    /**
     * 扫描跳转Activity RequestCode
     */
    public static final int REQUEST_CODE = 111;

    private final String TAG = "MessageFragment";

    private Activity mActivity;

    private ListView noticeLv = null;

    //用于右上角下拉菜单
    private List<PopDialogActivity.MenuInfo> moreInfo;
    /**
     * 选择联系人返回
     */
    private static final int REQUEST_SELECT_LINK = 1;

    /**
     * 收件人nube号
     */
    private ArrayList<String> receiverNumberLst = new ArrayList<String>();
    /**
     * 建群时，选择联系人键值对 <nube,name>
     */
    private Map<String, String> receiverNameMap = new HashMap<String, String>();

    private GroupChatInterfaceManager groupChatInterfaceManager;

    // 动态消息列表适配器
    private MessageListCursorAdapter messageListCursorAdapter = null;

    // 自身视讯号
    private String selfNubeNumber = "";

    private NoticesDao noticeDao;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomLog.d(TAG, "MessageFragment oncreate");
        mActivity = getActivity();
        noticeDao = new NoticesDao(mActivity);
        selfNubeNumber = AccountManager.getInstance(getActivity()).getAccountInfo().nube;
        saveAdminNubeInfo();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.message_fragment, container, false);
        noticeLv = (ListView) contentView.findViewById(R.id.notice_list);
        noticeLv.setDivider(null);
        noticeLv.setDividerHeight(0);

        return contentView;
    }


    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        leftTipView = LayoutInflater.from(getActivity()).inflate(R.layout.left_tip_view, null);
        TitleBar titleBar = getTitleBar();
        titleBar.enableRightBtn("", R.drawable.btn_meetingfragment_addmeet,
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMoreTitle();
                }
            });

        int mScreenWidth = IMCommonUtil.getDeviceSize(getActivity()).x;
        messageListCursorAdapter = new MessageListCursorAdapter(mActivity,
            null, mScreenWidth);
        //初始化，清空cursor
        messageListCursorAdapter.changeCursor(null);

        //设置 Nube 号
        messageListCursorAdapter.setSelfNubeNumber(selfNubeNumber);
        messageListCursorAdapter.setUIChangeListener(
            new MessageListCursorAdapter.UIChangeListener() {
                @Override
                public void onRefreshProgress() {
                    if (isAdded()) {
                        updateUIDisplay();
                    }
                }
            });

        noticeLv.setAdapter(messageListCursorAdapter);

        // 加载数据
        initData();
        registerSipReceiver();
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    private void updateUIDisplay() {
        if (messageListCursorAdapter != null) {
            if (View.VISIBLE == noticeLv.getVisibility()) {
                initData();
            }
        }
    }


    private void showMoreTitle() {

        if (moreInfo == null) {

            moreInfo = new ArrayList<PopDialogActivity.MenuInfo>();

            moreInfo.add(
                new PopDialogActivity.MenuInfo(R.drawable.temp_pop_dialog_startgroupchat,
                    getString(R.string.Initiated_group_chat),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TODO:新需求，新建消息跳入选择联系人页面--add at 15/6/18
                            Intent i = new Intent(getActivity(), SelectLinkManActivity.class);
                            i.putExtra(SelectLinkManActivity.ACTIVITY_FLAG,
                                SelectLinkManActivity.AVITVITY_START_FOR_RESULT);
                            // 极会议-增加一个参数区别入口
                            i.putExtra(SelectLinkManActivity.ACTIVTY_PURPOSE,
                                SelectLinkManActivity.NEW_MSG);
                            i.putExtra(SelectLinkManActivity.KEY_IS_SIGNAL_SELECT,
                                true);
                            i.putStringArrayListExtra(
                                SelectLinkManActivity.KEY_SELECTED_NUBENUMBERS,
                                new ArrayList<String>());
                            //为了适配该页面进入可以选择一个群加 OPT_FLAG
                            i.putExtra(SelectLinkManActivity.KEY_IS_NEED_SELECT_GROUP,
                                true);
                            startActivityForResult(i, REQUEST_SELECT_LINK);
                        }
                    }));
            moreInfo.add(
                new PopDialogActivity.MenuInfo(R.drawable.temp_pop_dialog_addfriend,
                    getString(R.string.add_friend),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setClass(getActivity(), AddContactActivity.class);
                            startActivityForResult(intent, 0);
                        }
                    }));
            moreInfo.add(new PopDialogActivity.MenuInfo(R.drawable.temp_pop_dialog_scan,
                getString(R.string.my_scan),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //扫一扫
                        if (CommonUtil.isFastDoubleClick()) {
                            return;
                        }
                        boolean result =  CommonUtil.selfPermissionGranted(getActivity(), Manifest.permission.CAMERA);
                        if (!result) {
                            // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                PermissionGen.with(MessageFragment.this)
                                        .addRequestCode(100)
                                        .permissions(Manifest.permission.CAMERA)
                                        .request();
                            } else {
                                openAppDetails(getString(R.string.no_photo_permission));
                            }
                        }else {
                            Intent intentScan = new Intent();
                            intentScan.setClass(getActivity(), ScannerActivity.class);
                            startActivityForResult(intentScan, REQUEST_CODE);
                        }
                    }
                }));
        }

        PopDialogActivity.setMenuInfo(moreInfo);
        startActivity(new Intent(getActivity(), PopDialogActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @PermissionSuccess(requestCode = 100)
    public void openCameraSuccess(){
        Intent intentScan = new Intent();
        intentScan.setClass(getActivity(), ScannerActivity.class);
        startActivityForResult(intentScan, REQUEST_CODE);
    }

    @PermissionFail(requestCode = 100)
    public void penCameraFail(){
        openAppDetails(getString(R.string.no_photo_permission));
    }

    private void openAppDetails(String tip) {
        final CustomDialog dialog = new CustomDialog(getActivity());
        dialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                dialog.dismiss();
            }
        });
        dialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                try {
                    startActivity(intent);
                } catch (Exception ex) {
                    CustomLog.d(TAG, "跳转到设置权限界面异常 Exception：" + ex.getMessage());
                }
            }
        });
        dialog.setTip(tip + getString(R.string.permission_setting));
        dialog.setCenterBtnText(getString(R.string.iknow));
        dialog.setOkBtnText(getString(R.string.permission_handsetting));
        dialog.show();
    }



    @Override
    protected void setListener() {

    }


    private Handler mHandler = new Handler();

    //上次查询成功时间
    long lastQueryTime = 0;


    /**
     * IM 消息数据库查询策略
     */
    @Override
    protected void initData() {
        int t1 = 1000;
        int t2 = 1000;
        if (System.currentTimeMillis() - lastQueryTime < t1) {
            return;
        }
        lastQueryTime = System.currentTimeMillis();
        mHandler.postDelayed(queryRunnable, t2);
    }


    // 查询数据Runnable
    private Runnable queryRunnable = new Runnable() {
        public void run() {
            queryData();
        }
    };


    private void queryData() {
        // 查询动态数据
        QueryNoticeAsyncTask task = new QueryNoticeAsyncTask(mActivity);
        task.setNoticesDao(noticeDao);
        CustomLog.d(TAG, "查询动态数据");
        task.setQueryTaskListener(new QueryNoticeAsyncTask.QueryTaskPostListener() {
            @Override
            public void onQuerySuccess(Cursor cursor) {

                CustomLog.d(TAG, "QueryNoticeAsyncTask onQuerySuccess...");

                //查询时，置之前的cursor为null
                if (messageListCursorAdapter != null) {
                    messageListCursorAdapter.changeCursor(cursor);
                }

                // 刷新界面数据
                if (cursor == null || cursor.getCount() == 0) {
                    // 没有数据
                    contentView.findViewById(R.id.no_notice_layout)
                        .setVisibility(View.VISIBLE);
                } else {
                    contentView.findViewById(R.id.no_notice_layout)
                        .setVisibility(View.GONE);
                }

            }


            @Override
            public void onQueryFailure() {
                // 查询失败

                contentView.findViewById(R.id.no_notice_layout)
                    .setVisibility(View.GONE);
                if (isAdded()) {
                    Toast.makeText(mActivity, getResources().getString(R.string.toast_load_failed)
                        , Toast.LENGTH_SHORT).show();
                }
                CustomLog.d("TAG", "QueryNoticeAsyncTask onQueryFailure...");
            }

        });
        if (mActivity != null) {
            task.executeOnExecutor(ThreadPoolManger.THREAD_POOL_EXECUTOR,
                ((HomeActivity) mActivity)
                    .getCurrentTag());
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onDestroy() {
        CustomLog.d(TAG, "onDestroy begin");
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
        if (messageListCursorAdapter != null) {
            messageListCursorAdapter.onDestoryView();
        }
        CustomLog.d(TAG, "onDestroy end");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            parseBarCodeResult(data);
        } else if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                // TODO:建群
                case REQUEST_SELECT_LINK:
                    if (data == null) {
                        return;
                    }
                    Bundle selRes = data.getExtras();
                    if (selRes != null) {
                        // 初始化收件人相关数据
                        receiverNameMap.clear();
                        final ArrayList<String> selectNickNames = selRes
                            .getStringArrayList(SelectLinkManActivity.START_RESULT_NICKNAME);
                        final ArrayList<String> selectName = selRes
                            .getStringArrayList(SelectLinkManActivity.START_RESULT_NAME);
                        final ArrayList<String> selectNumber = selRes
                            .getStringArrayList(SelectLinkManActivity.START_RESULT_NUMBER);
                        receiverNumberLst = selRes
                            .getStringArrayList(SelectLinkManActivity.START_RESULT_NUBE);
                        //
                        if (selectName == null
                            || selectName.size() != receiverNumberLst.size()) {
                            CustomLog.d(TAG, "选择收件人返回数据不整合");
                            return;
                        }
                        if (selectNickNames == null
                            || selectNickNames.size() != receiverNumberLst
                            .size()) {
                            CustomLog.d(TAG, "选择收件人返回数据不整合");
                            return;
                        }
                        if (selectNumber == null
                            || selectNumber.size() != receiverNumberLst.size()) {
                            CustomLog.d(TAG, "选择收件人返回数据不整合");
                            return;
                        }
                        for (int i = 0; i < receiverNumberLst.size(); i++) {
                            // 收件人名称
                            String nubeNum = receiverNumberLst.get(i);

                            //产品要求按照ShowNameUtil中的显示规则显示名字--add on 2015/6/29
                            ShowNameUtil.NameElement element = ShowNameUtil.getNameElement(
                                selectName.get(i),
                                selectNickNames.get(i), selectNumber.get(i),
                                nubeNum);

                            String showName = ShowNameUtil.getShowName(element);

                            receiverNameMap.put(nubeNum, showName);
                        }
                        if (receiverNameMap.size() > 1) {
                            //群聊
                            createGroup();
                        } else {
                            //单聊
                            // 跳转到聊天界面
                            Intent i = new Intent(getActivity(), ChatActivity.class);
                            i.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                                ChatActivity.VALUE_NOTICE_FRAME_TYPE_NUBE);
                            i.putExtra(ChatActivity.KEY_CONVERSATION_NUBES,
                                receiverNumberLst.get(0));
                            CustomLog.d(TAG, "the receiverNuber is " + receiverNumberLst.get(0));
                            String name = receiverNameMap.get(receiverNumberLst
                                .get(0));

                            i.putExtra(ChatActivity.KEY_CONVERSATION_SHORTNAME,
                                name);
                            i.putExtra(ChatActivity.KEY_CONVERSATION_TYPE,
                                ChatActivity.VALUE_CONVERSATION_TYPE_SINGLE);
                            //						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

    }


    /*
      * 调用GroupChatInterfaceManager的接口进行群的创建
      */
    public void createGroup() {
        CustomLog.i(TAG, "createGroup()");

        groupChatInterfaceManager = new GroupChatInterfaceManager(
            getActivity(), new GroupChatInterfaceManager.GroupInterfaceListener() {
            @Override
            public void onResult(String _interfaceName,
                                 boolean successOrfaliure, String _successReslut) {
                CustomLog.d(TAG, "接口" + _interfaceName + "返回信息" + _successReslut);

                if (_interfaceName.equals(
                    UrlConstant.METHOD_CREATE_GROUP)) {// 建群结束 ，跳入chatActivity页面

                    if (successOrfaliure) {
                        //IM SDK回调 建群成功
                        CustomLog.i(TAG,"Jump to Chatactivity");

                        Intent i = new Intent(getActivity(),
                            ChatActivity.class);
                        i.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                            ChatActivity.VALUE_NOTICE_FRAME_TYPE_LIST);
                        i.putExtra(ChatActivity.KEY_CONVERSATION_NUBES,
                            _successReslut);
                        i.putExtra(ChatActivity.KEY_CONVERSATION_TYPE,
                            ChatActivity.VALUE_CONVERSATION_TYPE_MULTI);
                        i.putExtra(ChatActivity.KEY_CONVERSATION_ID, _successReslut);
                        i.putExtra(ChatActivity.KEY_CONVERSATION_EXT, "");

                        startActivity(i);

                    } else {

                        //IM SDK回调 建群失败
                        CustomToast.show(MedicalApplication.getContext(),
                            getString(R.string.creat_group_fail_try_again), 1);
                    }
                    removeLoadingView();
                }
            }
        });


        showLoadingView(getString(R.string.wait_creat_group_chat),
            new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeLoadingView();
                    CustomLog.d(TAG, "取消创建群聊");
                }
            }, true);
        groupChatInterfaceManager.createGroup("", receiverNumberLst);
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        // TODO:重新查询？
    }


    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppP2PAgentManager.updatesip)) {
                CustomLog.d(TAG, AppP2PAgentManager.updatesip);
                updateSip();
            } else if (intent.getAction().equals("NoticeCountBroaddcase")) {
                int count = intent.getIntExtra("newNoticeCount", 0);
                CustomLog.d(TAG, "updateNoticeCount");
                updateTitleNotice(count);
            }
        }
    };


    private void updateSip() {
        boolean netConnect = IMCommonUtil.isNetworkAvailable(getActivity());
        CustomLog.d(TAG, "网络链接状态：" + netConnect + " p2p连接状态" + IMConstant.isP2PConnect);
        if (IMConstant.isP2PConnect && netConnect) {
            getTitleBar().removeCustomLeftView();
        } else {
            getTitleBar().adjustTextOnMessageFragment();
            getTitleBar().addCustomLeftView(leftTipView);
            MessageReceiveAsyncTask.FLAG_OF_NOTIFICATION_NOT_ON_LINE_MSG = true;
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //相当于Fragment的onResume
            if (contentView != null) {
                contentView.requestLayout();
            }
        } else {
            //相当于Fragment的onPause
        }
    }


    private void updateTitleNotice(int noticeCount) {

        if (noticeCount > 0) {
            if (noticeCount > 99) {
                getTitleBar().setTitle(getString(R.string.titlebar_middle_message) + "(99+)");
            } else {
                getTitleBar().setTitle(
                    getString(R.string.titlebar_middle_message) + "(" + noticeCount
                        + ")");
            }

        } else {
            getTitleBar().setTitle(getString(R.string.titlebar_middle_message));
        }
    }


    //注册im连接状态通知
    private void registerSipReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppP2PAgentManager.updatesip);
        filter.addAction("NoticeCountBroaddcase");
        getActivity().registerReceiver(mReceiver, filter);
        updateSip();
    }


    private void saveAdminNubeInfo() {
        SharedPreferences preferences = getActivity()
            .getSharedPreferences(KEY_SERVICE_NUBE_INFO, MODE_PRIVATE);
        if (!preferences.contains("HEAD_URL")) {
            CustomLog.d(TAG, "设置服务号的默认名称和头像");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("HEAD_URL", SettingData.getInstance().adminHeadUrl);
            editor.putString("USERNAME", SettingData.getInstance().adminNickName);
            CustomLog.d(TAG, "adminUrl:" + SettingData.getInstance().adminHeadUrl
                + " adminName:" + SettingData.getInstance().adminNickName);
            editor.commit();
        } else {
            CustomLog.d(TAG, "服务号默认名称头像已存在");
        }

    }
}
