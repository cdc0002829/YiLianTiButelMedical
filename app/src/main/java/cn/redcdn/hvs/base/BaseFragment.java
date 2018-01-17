package cn.redcdn.hvs.base;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.datacenter.offaccscenter.MDSAppGetOffAccInfo;
import cn.redcdn.datacenter.offaccscenter.data.OffAccdetailInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.ContactCardActivity;
import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.activity.EmbedWebViewActivity;
import cn.redcdn.hvs.im.activity.GroupAddActivity;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.officialaccounts.DingYueActivity;
import cn.redcdn.hvs.officialaccounts.activity.ArticlePreviewActivity;
import cn.redcdn.hvs.profiles.activity.OutDateActivity;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

import static android.content.ContentValues.TAG;
import static cn.redcdn.hvs.officialaccounts.activity.ArticlePreviewActivity.INTENT_DATA_ARTICLE_ID;
import static cn.redcdn.hvs.profiles.ProfilesFragment.ARTICLE_PREVIEW;
import static cn.redcdn.hvs.profiles.ProfilesFragment.GROUP_TYPE;
import static cn.redcdn.hvs.profiles.ProfilesFragment.PERSON_TYPE;
import static cn.redcdn.hvs.profiles.ProfilesFragment.TFTM;
import static cn.redcdn.hvs.profiles.ProfilesFragment.WE_TYPE;

/**
 * @ClassName: UDTBaseFragment
 * @Description: Fragment的基类
 * @Author: yaodetao
 * @Date: 2017/1/7 20:01.
 */

public abstract class BaseFragment extends Fragment {
    private View mViewRoot;
    private TitleBar titleBar;
    private boolean isHandleEvent = false;
    public View.OnClickListener mbtnHandleEventListener = null;
    private static final int IsHandleMsg = 99;
    private String mGroupId;
    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";
    private LinkedHashMap<String, GroupMemberBean> memberDateList
        = new LinkedHashMap<String, GroupMemberBean>();//显示数据
    private MDSAccountInfo loginUserInfo = null;
    private GroupDao mGroupDao;
    private Dialog dialog = null;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }

        mbtnHandleEventListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (isHandleEvent == true) {
                    CustomLog.w(TAG, "触摸过快,返回");
                    return;
                } else {
                    CustomLog.d(TAG, "触摸成功,isHandleEvent = true");
                    todoClick(v.getId());
                    isHandleEvent = true;
                    Message msg = Message.obtain();
                    msg.what = IsHandleMsg;
                    msg.obj = v.getId();
                    isHandleEventhandler.sendMessageDelayed(msg, 200);
                }
            }

        };
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 为空就去加载布局，onCreateView在界面切换的时候会被多次调用,防止界面跳转回来的时候显示空白
        if (mViewRoot == null) {
            mViewRoot = createView(inflater, container, savedInstanceState);
        }
        if (mViewRoot == null) {
            CustomLog.d(TAG, "UDTBaseFragment|onCreateView|mViewRoot|null");
        } else {
            CustomLog.d(TAG, "UDTBaseFragment|onCreateView|mViewRoot|" + mViewRoot);
        }
        return mViewRoot;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    /**
     * 当前的界面被切换出去的时候被调用,解决ViewGroup只有一个子View的bug
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mViewRoot != null) {
            ViewParent parent = mViewRoot.getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) parent;
                viewGroup.removeView(mViewRoot);
            }
        }

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    protected abstract View createView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState);


    public TitleBar getTitleBar() {
        if (titleBar == null) {
            titleBar = new TitleBar(getActivity(), getView());
        }
        return titleBar;
    }


    /**
        * 初始化布局
     */
    protected void initView() {

    }

    protected void showLoadingView(String message,
                                   DialogInterface.OnCancelListener listener) {
        CustomLog.i(TAG, "MeetingActivity::showLoadingDialog() msg: " + message);
        try {
            if (dialog != null) {
                dialog.dismiss();
            }
            dialog = CommonUtil.createLoadingDialog(getActivity(), message, listener);

            dialog.show();
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
    }


    /**
     * 设置监听器
     */
    protected abstract void setListener();

    /**
     * 加载数据
     */
    protected abstract void initData();


    public void todoClick(int i) {

    }


    Handler isHandleEventhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == IsHandleMsg) {
                isHandleEvent = false;
                System.out.println("200ms到时，isHandleEvent = false");
            }
        }
    };


    /**
     * 公共解析二维码方法
     */
    public void parseBarCodeResult(Intent data) {
        //处理扫描结果（在界面上显示）
        if (data != null) {
            //                parseBarCodeResult(data);
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }
            if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                String result = bundle.getString(CodeUtils.RESULT_STRING);
                CustomLog.d(TAG, "解析的二维码字符串为:" + result);
                //                    Toast.makeText(getActivity(), "解析结果:" + result, Toast.LENGTH_LONG).show();
                String[] split = result.split("\\?");
                //                    String https = split[0];
                //                    CustomLog.e("TAG", https);
                //                    if (https.equals(HTTPS)) {
                if (split.length < 2) {
                    CustomToast.show(getContext(), getString(R.string.noOurCompanyQRcode), 8000);
                    return;
                }
                String[] split1 = split[1].split("=");
                if (split1.length < 2) {
                    CustomToast.show(getContext(), getString(R.string.noOurCompanyQRcode), 8000);
                    return;
                }
                String s = split1[1];
                String[] split2 = s.split("_");
                if (split2.length < 3) {
                    CustomToast.show(getContext(), getString(R.string.noOurCompanyQRcode), 8000);
                    return;
                }
                mGroupId = split2[1];
                CustomLog.e("TAG", split2[0]);
                CustomLog.e("TAG", split2[1]);
                CustomLog.e("TAG", split2[2]);
                //                        if (https.contains(HTTPS)) {
                if (split2[0].equals(PERSON_TYPE)) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), ContactCardActivity.class);
                    intent.putExtra("nubeNumber", split2[1]);
                    intent.putExtra("searchType", "4");
                    startActivity(intent);

                } else if (split2[0].equals(GROUP_TYPE)) {
                    long nowTime = System.currentTimeMillis();
                    long startTime = Long.parseLong(split2[2]);
                    long time = nowTime - startTime;
                    long days = time / (1000 * 60 * 60 * 24);
                    if (days >= 7) {
                        Intent outDateIntent = new Intent();
                        outDateIntent.setClass(getContext(), OutDateActivity.class);
                        startActivity(outDateIntent);
                    } else {
                        mGroupDao = new GroupDao(getActivity());
                        loginUserInfo = AccountManager.getInstance(getActivity()).getAccountInfo();
                        ;
                        memberDateList = mGroupDao.queryGroupMembers(mGroupId);
                        if (memberDateList.containsKey(loginUserInfo.getNube())) {
                            CustomLog.d(TAG, "用户属于该群，直接进入");
                            enterChatActivity();
                        } else {
                            Intent personIntent = new Intent();
                            personIntent.putExtra(GroupAddActivity.GROUP_ID, split2[1]);
                            personIntent.putExtra(GroupAddActivity.GROUP_ID_FROM,
                                GroupAddActivity.GROUP_ID_FROM);
                            personIntent.setClass(getContext(), GroupAddActivity.class);
                            startActivity(personIntent);
                        }
                    }

                } else if (split2[0].equals(WE_TYPE)) {
                    MDSAppGetOffAccInfo mdsAppGetOffAccInfo = new MDSAppGetOffAccInfo() {
                        @Override
                        protected void onSuccess(OffAccdetailInfo responseContent) {
                            super.onSuccess(responseContent);
                            String id = responseContent.getId();
                            Intent intentWeChat = new Intent();
                            intentWeChat.putExtra("officialAccountId", id);
                            intentWeChat.setClass(getActivity(), DingYueActivity.class);
                            startActivity(intentWeChat);
                        }


                        @Override
                        protected void onFail(int statusCode, String statusInfo) {
                            super.onFail(statusCode, statusInfo);
                            CustomToast.show(getContext(),
                                getString(R.string.officialAccountsNotExist), 8000);
                            return;
                        }
                    };
                    mdsAppGetOffAccInfo.appGetOffAccInfo(AccountManager.getInstance(getActivity())
                        .getAccountInfo().getToken(), split2[1]);
                } else if (split2[0].equals(ARTICLE_PREVIEW)) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), ArticlePreviewActivity.class);
                    intent.putExtra(INTENT_DATA_ARTICLE_ID, split2[1]);
                    startActivity(intent);
                }else if (split2[0].equals(TFTM)){
                    Intent intent = new Intent(getActivity(),
                            EmbedWebViewActivity.class);
                    intent.putExtra(
                            EmbedWebViewActivity.KEY_PARAMETER_URL,
                            result+System.currentTimeMillis());
                   startActivity(intent);
                }
                else {
                    CustomToast.show(getContext(), getString(R.string.noOurCompanyQRcode), 8000);
                    return;
                }
                //                        }
                //                    } else {
                //                        CustomToast.show(getContext(), "亲，这不是本公司的二维码哦", 8000);
                //                        return;
                //                    }

            } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                Toast.makeText(getActivity(), getString(R.string.parseTheQrcodeFailed),
                    Toast.LENGTH_LONG).show();
                CustomToast.show(getContext(), getString(R.string.parseTheQrcodeFailed), 8000);
            }
        }

    }


    private void enterChatActivity() {
        Intent i = new Intent(getActivity(), ChatActivity.class);
        i.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
            ChatActivity.VALUE_NOTICE_FRAME_TYPE_LIST);
        i.putExtra(ChatActivity.KEY_CONVERSATION_ID, mGroupId);
        i.putExtra(ChatActivity.KEY_CONVERSATION_TYPE, ChatActivity.VALUE_CONVERSATION_TYPE_MULTI);
        i.putExtra(ChatActivity.KEY_CONVERSATION_NUBES, mGroupId);
        startActivity(i);
    }


    public void showLoadingView(String message,
                                final DialogInterface.OnCancelListener listener, boolean cancelAble) {
        CustomLog.i(TAG, "MeetingActivity::showLoadingDialog() msg: " + message);
        try {
            if (dialog != null) {
                dialog.dismiss();
            }
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
        dialog = cn.redcdn.hvs.util.CommonUtil.createLoadingDialog(getActivity(), message,
            listener);
        dialog.setCancelable(cancelAble);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    listener.onCancel(dialog);
                }
                return false;
            }
        });
        try {
            dialog.show();
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
    }


    protected void showLoadingView(String message) {
        CustomLog.i(TAG, "MeetingActivity::showLoadingDialog() msg: " + message);
        try {
            if (dialog != null) {
                dialog.dismiss();
            }
            dialog = CommonUtil.createLoadingDialog(getActivity(), message);
            dialog.show();
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
    }


    protected void removeLoadingView() {

        CustomLog.i(TAG, "MeetingActivity::removeLoadingView()");

        if (dialog != null) {

            dialog.dismiss();

            dialog = null;

        }

    }

}
