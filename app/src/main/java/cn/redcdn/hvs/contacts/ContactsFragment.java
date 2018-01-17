package cn.redcdn.hvs.contacts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.contacts.contact.AddContactActivity;
import cn.redcdn.hvs.contacts.contact.ContactCardActivity;
import cn.redcdn.hvs.contacts.contact.ContactTransmitConfig;
import cn.redcdn.hvs.contacts.contact.ListViewAdapter;
import cn.redcdn.hvs.contacts.contact.butelDataAdapter.ContactSetImp;
import cn.redcdn.hvs.contacts.contact.hpucontact.ShareContactActivity;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.interfaces.ContactCallback;
import cn.redcdn.hvs.contacts.contact.interfaces.ResponseEntry;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.hvs.contacts.contact.manager.IContactListChanged;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.PopDialogActivity;
import cn.redcdn.hvs.util.ScannerActivity;
import cn.redcdn.hvs.util.SideBar;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

import static android.content.ContentValues.TAG;


/**
 * Created by thinkpad on 2017/2/7.
 * Created by thinkpad on 2017/2/7.
 *
 */

public class ContactsFragment extends BaseFragment {

    private ListView lvContact;
    public static TextView tvSelect = null;
    private ContactSetImp mContactSetImp=null;
    private ListViewAdapter contactAdapter;
    /*** 定义消息类型 */
    private final int MSG_UPDATAUI = 0x66660000;
    private final int MSG_RESUMEDATA = 0x66660005;
    //用于右上角下拉菜单
    private List<PopDialogActivity.MenuInfo> moreInfo;
    public static final int SCAN_CODE = 222;
    public static SideBar mSideBar;
    private List<LetterInfo> letterInfoList= null;
    private boolean isFirstResume = true;
    private StrangerMessageObserver observeStrangeRelation;
    private ShareContactObserver shareContactObserver;
    private final int MSG_MESSAGE_NUMBER_CHANGED = 701;
    private IContactListChanged ic = null;

    private final int MSG_MESSAGE_SHARE_CONTACT_CHANGED = 702;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View l1 = View.inflate(getActivity(), R.layout.contacts_fragment, null);
        mSideBar = (SideBar)l1.findViewById(R.id.sidebar_contact_fragment);
        tvSelect = (TextView) l1.findViewById(R.id.fragment_tvselect);
        tvSelect.setVisibility(View.INVISIBLE);
        lvContact = (ListView) l1.findViewById(R.id.fragment_listView);
        lvContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                int dtCount = AccountManager.getInstance(getActivity()).hpuList.size();

                    if(position==0){
//                        Intent intent = new Intent();
//                        intent.setClass(getActivity(), VerificationActivity.class);
//                        startActivityForResult(intent, 0);
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), NewFriendsActivity.class);
                        startActivityForResult(intent, 0);
                    }else if(position==1){
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), ContactsGroupChatActivity.class);
                        startActivityForResult(intent, 0);
                    }else if(position==2){
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), ContactsPublicNumberActivity.class);
                        startActivityForResult(intent, 0);
                    }else if(dtCount>0&& position > 2&&position<=2+dtCount){
                        //跳转到某个医联体通讯录列表 通过id查询
                        Contact contact = (Contact)mContactSetImp.getItem(position);
                         Intent intent = new Intent();
                         intent.setClass(getActivity(),ShareContactActivity.class);
                         intent.putExtra(ShareContactActivity.HPU_ID,contact.getNumber());
                         intent.putExtra(ShareContactActivity.HPU_NAME,contact.getNickname());
                         startActivity(intent);
                    }else{
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), ContactCardActivity.class);
                        intent.putExtra("contact", (Contact)mContactSetImp.getItem(position));
                        intent.putExtra("contactFragment","contactFragment");
                        intent.putExtra("REQUEST_CODE", ContactTransmitConfig.REQUEST_CONTACT_CODE);
                        startActivityForResult(intent, 0);
                    }
            }
        });
        return l1;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getResources().getString(R.string.titlebar_middle_contact));
        titleBar.enableRightBtn("", R.drawable.btn_meetingfragment_addmeet,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CommonUtil.isFastDoubleClick()) {
                            return;
                        }
                        showMoreTitle();
                    }
                });

        mHandler.sendEmptyMessage(MSG_RESUMEDATA);

    }

    private void showMoreTitle() {
        if (moreInfo == null) {
            moreInfo = new ArrayList<PopDialogActivity.MenuInfo>();
            moreInfo.add(new PopDialogActivity.MenuInfo(R.drawable.temp_pop_dialog_addfriend, getString(R.string.add_friend),
                    new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setClass(getActivity(), AddContactActivity.class);
                            startActivityForResult(intent, 0);
                        }
                    }));
            moreInfo.add(new PopDialogActivity.MenuInfo(R.drawable.temp_pop_dialog_scan, getString(R.string.my_scan),
                    new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            //扫一扫
                            boolean result = CommonUtil.selfPermissionGranted(getActivity(), Manifest.permission.CAMERA);
                            if (!result) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    PermissionGen.with(ContactsFragment.this)
                                            .addRequestCode(100)
                                            .permissions(Manifest.permission.CAMERA)
                                            .request();
                                } else {
                                    openAppDetails(getString(R.string.no_photo_permission));
                                }
                            }else {
                                Intent intentScan = new Intent();
                                intentScan.setClass(getActivity(), ScannerActivity.class);
                                startActivityForResult(intentScan, SCAN_CODE);
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
        startActivityForResult(intentScan, SCAN_CODE);
    }

    @PermissionFail(requestCode = 100)
    public void openCameraFail(){
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

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RESUMEDATA:
                    CustomLog.d(TAG, "mHandler  MSG_RESUMEDATA 更新聯繫人數據");

                    registerListener();

                    initContactsInfo();
                    break;
                case MSG_UPDATAUI:
                    if(isFirstResume){
                        isFirstResume = false;
                        initContactAdapter();
                    }else{
                        if (contactAdapter != null) {
                            contactAdapter.updateDataSet(0, mContactSetImp);
                        }
                    }
                    switchLayout();
                    break;
                case MSG_MESSAGE_NUMBER_CHANGED:
                    initContactAdapter();
                    break;
                case MSG_MESSAGE_SHARE_CONTACT_CHANGED:
                    initHpuList();
                    break;
                default:
                    break;
            }
        }
    };

    private void registerListener(){
        if (observeStrangeRelation == null) {
            observeStrangeRelation = new StrangerMessageObserver();
            getActivity().getContentResolver().registerContentObserver(
                    ProviderConstant.Strange_Message_URI, true,
                    observeStrangeRelation);
        }
        if(shareContactObserver == null){
            shareContactObserver = new ShareContactObserver();
            getActivity().getContentResolver().registerContentObserver(ProviderConstant.Share_Contact_URI,true,shareContactObserver);
        }

        ic = new IContactListChanged() {
            @Override
            public void onListChange(ContactSetImp set) {
                CustomLog.d(TAG, " IContactListChanged change");
                initContactsInfo();
            }
        };
        ContactManager.getInstance(getActivity())
                .registerUpdateListener(ic);
    }


    private void initContactAdapter() {
        contactAdapter = new ListViewAdapter(getActivity(), FriendsManager.getInstance().getNotReadMsgSize());
        if(null!=mContactSetImp){
            contactAdapter.addDataSet(mContactSetImp);
            lvContact.setAdapter(contactAdapter);
        }else{
            CustomLog.e(TAG, "mContactSetImp is null");
        }
        CustomLog.i(TAG, "initContactAdapter");
    }

    private  void initHpuList(){
            initContactsInfo();
    }

    @Override
    public void todoClick(int id) {
        super.todoClick(id);
            switch (id) {
                default:
                    break;
            }
    }

    private void switchLayout() {
        CustomLog.i(TAG, "switchLayout");
        // 设置需要显示的提示框
        mSideBar.setTextView(tvSelect);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = CommonUtil.getLetterPosition(letterInfoList, s);
                if (position != -1) {
                    lvContact.setSelection(position);
                }
                mSideBar.setBackgroundColor(Color.parseColor("#e3e4e5"));
            }
        });
        if (mContactSetImp != null) {
            if (mContactSetImp.getCount() == 0) {
                lvContact.setVisibility(View.INVISIBLE);
                mSideBar.setVisibility(View.INVISIBLE);
            } else {
                lvContact.setVisibility(View.VISIBLE);
                mSideBar.setVisibility(View.VISIBLE);
            }
        }else{
            lvContact.setVisibility(View.INVISIBLE);
            mSideBar.setVisibility(View.INVISIBLE);
        }
        if (contactAdapter != null) {
            contactAdapter.notifyDataSetChanged();
        }
    }

    private void initContactsInfo() {
        CustomLog.i(TAG, "initContactsInfo");
        ContactManager.getInstance(getActivity()).getAllContacts(
                new ContactCallback() {

                    @Override
                    public void onFinished(ResponseEntry result) {
                        CustomLog.i(TAG, "onFinish! status: " + result.status
                                + " | content: " + result.content);
                        if (result.status >= 0) {
                            letterInfoList= new ArrayList<LetterInfo>();
                            mContactSetImp = (ContactSetImp) result.content;
                            if(null!=mContactSetImp&&mContactSetImp.getCount()>0){
                                for(int i=0;i<mContactSetImp.getCount();i++){
                                    Contact tContact = (Contact)mContactSetImp.getItem(i);
                                    if(null!=tContact.getFirstName()){
                                        LetterInfo letterInfo = new LetterInfo(){};
                                        letterInfo.setLetter(tContact.getFirstName());
                                        letterInfoList.add(letterInfo);
                                    }
                                }
                            }
                            mHandler.sendEmptyMessage(MSG_UPDATAUI);
                        }
                    }
                },true);
    }

    @Override
    public void onResume() {
        CustomLog.d(TAG, "onresume");
        super.onResume();

    }

    @Override
    public void onDestroy() {
        CustomLog.i(TAG, "onDestroy");
        super.onDestroy();
        if (observeStrangeRelation != null) {
            getActivity().getContentResolver().unregisterContentObserver(observeStrangeRelation);
            observeStrangeRelation = null;
        }
        if (shareContactObserver !=null){
            getActivity().getContentResolver().unregisterContentObserver(shareContactObserver);
            shareContactObserver = null;
        }
        if (ic != null) {
            ContactManager.getInstance(getActivity())
                    .unRegisterUpdateListener(ic);
            CustomLog.d(TAG, "onStop ic" + (ic == null));
        }
    }

    @Override
    public void onStop() {
        CustomLog.i(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        CustomLog.d(TAG, "resultfrom"+resultCode);
        if(requestCode == SCAN_CODE){
            parseBarCodeResult(data);   
        }
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {

    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    /**
     * 监听陌生人消息表
     */
    private class StrangerMessageObserver extends ContentObserver {

        public StrangerMessageObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.d(TAG,"陌生人消息数据库数据发生变更");
            mHandler.sendEmptyMessage(MSG_MESSAGE_NUMBER_CHANGED);
        }
    }
    /**
     * 监听医联体通讯录
     */
    private class ShareContactObserver extends ContentObserver {

        public ShareContactObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.d(TAG,"共享通讯录数据发生变更");
            mHandler.sendEmptyMessage(MSG_MESSAGE_SHARE_CONTACT_CHANGED);
        }
    }

}

