package cn.redcdn.hvs.im.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.redcdn.datacenter.collectcenter.DataBodyInfo;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.contacts.contact.AddContactActivity;
import cn.redcdn.hvs.contacts.contact.ContactTransmitConfig;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.adapter.CustomHorizontalScrollViewAdapter;
import cn.redcdn.hvs.im.adapter.SelectLinkManListAdapter;
import cn.redcdn.hvs.im.adapter.SelectLinkManListAdapter.ChangeDataListener;
import cn.redcdn.hvs.im.adapter.SelectLinkManListAdapter.ViewHolder;
import cn.redcdn.hvs.im.bean.CollectionEntity;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.column.ThreadsTable;
import cn.redcdn.hvs.im.common.CommonWaitDialog;
import cn.redcdn.hvs.im.common.SideBar;
import cn.redcdn.hvs.im.dao.CollectionDao;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.MedicalDao;
import cn.redcdn.hvs.im.dao.MedicalDaoImpl;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.manager.FileManager;
import cn.redcdn.hvs.im.util.ButelOvell;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.view.CommonDialog;
import cn.redcdn.hvs.im.view.CustomHorizontalScrollView;
import cn.redcdn.hvs.im.view.SharePressableImageView;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.graphics.Bitmap.createBitmap;
import static cn.redcdn.hvs.R.string.btn_cancle;

/**
 * Desc   选择联系人
 * Created by wangkai on 2017/2/25.
 */

public class SelectLinkManActivity extends BaseActivity implements ChangeDataListener {

    private final String TAG = "SelectLinkManActivity";

    /**
     * 进入本页面的flag,不同的flag,影响到按‘完成’按钮时，数据的返回内容 和 页面的跳转方向
     */
    public static final String ACTIVITY_FLAG = "activity_flag";
    /**
     * 此字符串为一常量定义，是 ACTIVITY_FLAG 的一个期望值； 表示希望返回选中的联系人信息
     */
    public static final String AVITVITY_START_FOR_RESULT = "activity_strat_for_result";

    /**
     * 本页面标题显示的文字， 当不传入或传入为空串时，使用默认值:(“选择联系人”)
     */
    public static final String AVITVITY_TITLE = "activity_title";
    // 进入页面，首先取出title，防止到处使用getIntent().getStringExtra(AVITVITY_TITLE)，而导致getIntent()产生的NullpointerException
    private static String title = "";
    /**
     * 表示是否单选：true：表示不单选；false:表示单选
     */
    public static final String KEY_IS_SIGNAL_SELECT = "key_is_signal_select";

    /**
     * 已选择的nube号码；从别的的页面传入； 场景如：从新建消息页面，先选择了一些联系人，此后又进入本页面重新选择其他联系人，
     * 本页面需要将先前已选择的联系人标识为选中状态。
     */
    public static final String KEY_SELECTED_NUBENUMBERS = "key_selected_nubenumbers";

    /**
     * 单击即刻返回的标记 页面上不显示多选或单选按钮，标题上不显示‘完成’字符； 单击item项，即可返回；
     */
    public static final String KEY_SINGLE_CLICK_BACK = "key_single_click_back";

    public static final String START_RESULT_NAME = "names";
    public static final String START_RESULT_NUBE = "nubeNumbers";
    // add by zzwang: 增加弱化视讯号需求，添加onActivityForResult 手机号数据
    public static final String START_RESULT_NUMBER = "numbers";
    public static final String START_RESULT_USERID = "userIds";
    public static final String START_RESULT_NICKNAME = "nickNames";
    public static final String START_RESULT_HEADURL = "headUrls";

    private ListView listVew;
    private SelectLinkManListAdapter adapter;
    private List<ContactFriendBean> list;
    private MedicalDao dao;
    private Handler handler;
    private boolean startForResult = false;
    private boolean isNotSignale = false;
    private boolean backBySingleClick = false;// --注释：发送名片时、消息转发时，单选，立刻返回--wxy
    // on 2015/7/10

    // 保存选中的数据,key为联系人contactId
    private Map<String, ContactFriendBean> selectMap;
    // 标记之前选中的联系人
    private ArrayList<String> sharePictureManLst = null;

    private static final int MSG_UPDATE_UI = 0;
    private static final int MSG_BUSINESS_END = 3;
    private static final int MSG_BUSINESS_ERROR = 2;
    private static final int MSG_TITLE_COMPLETE_DISENABLE = 4;
    private static final int MSG_TITLE_COMPLETE_ENABLE = 5;

    /**
     * 多选联系人的场景下，最多可选择8个人； 2014.11.22 在新消息版本中已不再有选择的上限
     */
    @SuppressWarnings("unused")
    private static final int MUTIL_SELECT_MAX_COUNT = 8;

    private CommonWaitDialog waitDialog = null;

    private TitleBar titleBar;

    // // 没有联系人时提示
    // private TextView emptyText;
    // 没有联系人时显示
    private View emptyView;
    // 快速定位条
    private SideBar sideBar;
    // 快速定位条，选中提示
    private TextView mDialogText;
    private View dialogView;

    // 异常数据个数
    private int illegalDataCount = 0;
    // 合法数据位置
    private Map<String, Integer> positonMap = new HashMap<String, Integer>();
    // 常用字符最大ASCII
    private String max = String.valueOf((char) (255));
    private String currentSortKey = "";
    private Map<String, Integer> subMap = new HashMap<String, Integer>();

    // 群聊
    private CustomHorizontalScrollView mHorizontalScrollView;
    private CustomHorizontalScrollViewAdapter mAdapter;
    private List<ContactFriendBean> list2;// 存放选择的联系人
    private ArrayList<String> listContact;// 存放已建群中的联系人纳贝号
    private Map<String, Integer> mapPosition;// 存放选择的联系人以及对应的position
    public static final String OPT_FLAG = "opt_flag";// 标志从哪个界面进入，如移交群主，选择名片，选择联系人
    public static final String OPT_INVITE_START_FOR_RESULT = "opt_invite_start_for_result";
    public static final String OPT_HAND_OVER_START_FOR_RESULT = "opt_hand_over_start_for_result";
    public static final String OPT_FORWARDING_FOR_RESULT = "opt_forwarding_for_result";
    public static final String HAND_OVER_MASTER_LIST = "hand_over_master_list";// 移交群主
    // 时
    // 携带群成员信息
    private List<ContactFriendBean> GroupList;
    private GroupDao groupDao;

    // 不同的入口进入时，会对选择联系人列表有不同的过滤要求（转发、分享、会议）
    public final static String ACTIVTY_PURPOSE = "activity_purpose";

    // 本地分享入口-----+ 消息转发入口
    private boolean shareFlag = false;
    /**
     * 极会议版本：转发消息时对用户设备做了限制-a,转发文字/视频，只转发给手机用户，过滤掉M1/N8J/X1/N7/N8用户； b,转发图片/名片，
     * 只转发给手机/N7/N8用户，过滤掉M1/N8J/X1用户
     */
    public static final String MSG_FORWARD = "msg_forword";
    private boolean mIsForward = false;

    public final static String SHARE_PIC = "share_pic";
    private int shareType = 0;
    private NoticesDao noticesDao = null;
    // 会议入口：需要过滤 X1/N7/N8
    private boolean mIsMeeting = false;
    public final static String J_MEETING = "j_meeting";
    // 新建消息入口：
    private boolean mIsMsg = false;
    public final static String NEW_MSG = "new_msg";
    // 群邀请入口：
    private boolean mIsInvite = false;
    public final static String GROUP_INVITE = "group_invite";
    // 收藏转发入口标记1表示收藏转发入口，0表示其他
    private int collFlag = 0;
    private CollectionDao mCollectionDao = null;

    private RelativeLayout chooseGroupLayout = null;

    public static final String KEY_IS_NEED_SELECT_GROUP = "key_is_need_select_group";

    private Button addContactFriendBtn;
    private boolean isRefreshList = false;    //如果跳转至通讯录，回来在 onResume 时刷新页面数据

    private int chatForwardType = -1;//转发的消息类型  现定义：-2位 逐条、合并消息的转发
    private int forwradType = 0;//转发类型 0：逐条转发  1：合并转发
    private DataBodyInfo collectItemInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_linkman);
        // mInflater = LayoutInflater.from(this);
        list2 = new ArrayList<ContactFriendBean>();
        listContact = new ArrayList<String>();
        mapPosition = new HashMap<String, Integer>();
        CustomLog.d(TAG, "oncreate begin");

        obtainInfo();

        initWidget();

        disSelectAll();

        initData();

        CustomLog.d(TAG, "oncreate end");
    }


    private void obtainInfo() {
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            if (null != bundle.getString(ACTIVITY_FLAG)
                && AVITVITY_START_FOR_RESULT.equals(bundle
                .get(ACTIVITY_FLAG))) {
                startForResult = true;
                CustomLog.d(TAG, "startForResult = true");
                // TODO:转发/分享-flag
                if (MSG_FORWARD.equals(bundle.getString(ACTIVTY_PURPOSE))) {
                    CustomLog.d(TAG, "转发");
                    shareFlag = true;
                    mIsForward = true;
                    chatForwardType = bundle.getInt("chatForwardType");
                    forwradType = bundle.getInt(ShareLocalActivity.FORWARD_TYPE);
                    collectItemInfo = (DataBodyInfo) bundle.getSerializable("collectItemInfo");
                }
                if (SHARE_PIC.equals(bundle.getString(ACTIVTY_PURPOSE))) {
                    shareFlag = true;
                    CustomLog.d(TAG, "分享");
                    shareType = bundle.getInt(ShareLocalActivity.SHARE_TYPE);

                }
                // TODO:召开会议-flag
                if (J_MEETING.equals(bundle.getString(ACTIVTY_PURPOSE))) {
                    mIsMeeting = false;

                }
                // TODO:新建消息入口
                if (NEW_MSG.equals(bundle.getString(ACTIVTY_PURPOSE))) {
                    mIsMsg = true;
                }
                // TODO:群邀请入口
                if (GROUP_INVITE.equals(bundle.getString(ACTIVTY_PURPOSE))) {
                    mIsInvite = true;
                }
            }

            if (bundle.getBoolean(KEY_IS_SIGNAL_SELECT)) {
                isNotSignale = true;
                CustomLog.d(TAG, "isNotSignale = true");
            }
            chooseGroupLayout = (RelativeLayout) findViewById(R.id.choose_group_layout);
            if (!OPT_FORWARDING_FOR_RESULT.equals(bundle.getString(OPT_FLAG))) {
                sharePictureManLst = bundle
                    .getStringArrayList(KEY_SELECTED_NUBENUMBERS);
                chooseGroupLayout.setVisibility(View.GONE);
            } else {
                chooseGroupLayout.setVisibility(View.VISIBLE);
            }
            boolean isNeedSelectGroupLayout = false;
            isNeedSelectGroupLayout = bundle.getBoolean(KEY_IS_NEED_SELECT_GROUP);
            if (isNeedSelectGroupLayout) {
                chooseGroupLayout.setVisibility(View.VISIBLE);
            } else {
                chooseGroupLayout.setVisibility(View.GONE);
            }

            if (bundle.getBoolean(KEY_SINGLE_CLICK_BACK)) {
                backBySingleClick = true;
                CustomLog.d(TAG, "backBySingleClick = true");
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isRefreshList) {
            //从搜索联系人界面回到此界面时刷新数据
            initData();
        }

        isRefreshList = false;
    }


    @SuppressLint("HandlerLeak") class SelectHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            if (waitDialog != null) {
                waitDialog.clearAnimation();
                waitDialog = null;
            }

            switch (msg.what) {
                case MSG_UPDATE_UI:
                    if (adapter != null) {
                        if (list == null || list.size() == 0) {
                            emptyView.setVisibility(View.VISIBLE);
                            listVew.setVisibility(View.GONE);
                            sideBar.setVisibility(View.GONE);
                        } else {
                            listVew.setVisibility(View.VISIBLE);
                            sideBar.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                            // 邀请好友入群，需要传入排重数据源
                            if (OPT_INVITE_START_FOR_RESULT.equals(getIntent()
                                .getStringExtra(OPT_FLAG))) {
                                listContact.clear();
                                listContact.addAll(sharePictureManLst);
                                adapter.setInviteList(list, listContact);
                            } else {
                                adapter.setList(list, sharePictureManLst);
                            }
                            // adapter.setList(list, sharePictureManLst);
                            adapter.setSection(positonMap, illegalDataCount, subMap);

                            adapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case MSG_BUSINESS_ERROR:
                    Toast.makeText(SelectLinkManActivity.this,
                        getString(R.string.selectlinkman_toast),
                        Toast.LENGTH_SHORT).show();
                    CustomLog.d(TAG, "Toast：加载数据出错");
                    emptyView.setVisibility(View.VISIBLE);
                    break;
                case MSG_BUSINESS_END:
                    // Bundle data = msg.getData();
                    // if (data != null) {
                    // String info = data.getString("info");
                    // if (!TextUtils.isEmpty(info)) {
                    // Toast.makeText(SelectLinkManActivity.this, info,
                    // Toast.LENGTH_SHORT).show();
                    // CustomLog("数据消息信息");
                    // }
                    // }
                    // Intent intent = new Intent();
                    // intent.setClass(SelectLinkManActivity.this,
                    // MainFragmentActivity.class);
                    // intent.putExtra(MainFragmentActivity.TAB_INDICATOR_INDEX,
                    // MainFragmentActivity.TAB_INDEX_MESSAGE);
                    // startActivity(intent);
                    // finish();
                    break;
                case MSG_TITLE_COMPLETE_DISENABLE:
                    if (backBySingleClick) {
                        titleBar.setRightBtnVisibility(View.INVISIBLE);
                        return;
                    }
                    titleBar.setTopRightBtn(false,
                        getResources().getString(R.string.btn_ok),
                        getResources().getColor(R.color.select_linkman_btn_disable_color), null);
                    mHorizontalScrollView.setVisibility(View.GONE);
                    break;
                case MSG_TITLE_COMPLETE_ENABLE:
                    // 选中联系人时，才显示
                    //极会议版本-召开会议时，即使没有人，也可以点击“确定”--begin
                    if (list2 != null && list2.size() > 0) {
                        mHorizontalScrollView.setVisibility(View.VISIBLE);
                    } else {
                        mHorizontalScrollView.setVisibility(View.GONE);
                    }
                    //极会议版本-召开会议时，即使没有人，也可以点击“确定”--end
                    if (backBySingleClick) {
                        titleBar.setRightBtnVisibility(View.INVISIBLE);
                        return;
                    }
                    titleBar.setTopRightBtn(
                        true,
                        getResources().getString(R.string.btn_ok),
                        getResources().getColor(
                            R.color.select_linkman_btn_ok_color),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CustomLog.d(TAG, "点击完成按钮");
                                if (CommonUtil.isFastDoubleClick()) {

                                    return;
                                }
                                // 分享入口
                                if (shareFlag) {
                                    showShareDialog();
                                } else {
                                    submitData();
                                }

                            }
                        });
                    // if (selectMap.size() - sharePictureManLst.size() > 0)
                    if (0 < list2.size()) {
                        if (list2.size() > 99) {
                            titleBar.setTopRightBtnText(getResources().getString(
                                R.string.btn_ok)
                                + "(99+)");
                        } else {
                            titleBar.setTopRightBtnText(getResources().getString(
                                R.string.btn_ok)
                                + "(" + list2.size() + ")");
                        }

                    } else {
                        titleBar.setTopRightBtnText(getResources().getString(
                            R.string.btn_ok));
                    }
                default:
                    break;
            }

        }
    }


    private void initWidget() {
        CustomLog.d(TAG, "initWidget begin");
        mHorizontalScrollView = (CustomHorizontalScrollView) findViewById(
            R.id.id_horizontalScrollView);
        //极会议：头像间距设置
        mHorizontalScrollView.setSpace(20);
        //极会议：头像宽度设置
        mHorizontalScrollView.setIconWidth(72);

        mAdapter = new CustomHorizontalScrollViewAdapter(this, list2);
        dao = new MedicalDaoImpl(this);
        groupDao = new GroupDao(this);
        handler = new SelectHandler();
        selectMap = new HashMap<String, ContactFriendBean>();

        titleBar = getTitleBar();
        titleBar.setBackText(getString(R.string.btn_cancle));
        title = getIntent().getStringExtra(AVITVITY_TITLE);
        if (TextUtils.isEmpty(title)) {
            titleBar.setTitle(getString(R.string.select_receive_message));
        } else {
            titleBar.setTitle(title);
            String titleStr = getResources().getString(R.string.call_jmeeting);
            if (titleStr.equals(title)) {
                //因为bug:0020185-要求会议页面title:召开视频会议---但是，选择联系人页面作为默认的Title.
                //当不传参数时，会有一些响应的逻辑处理。因此，此处需将title重置为"".
                title = "";
            }
        }

        // handler.sendEmptyMessage(MSG_TITLE_COMPLETE_DISENABLE);
        // TODO:召开会议时，选择0人，也可以点击“确定”--start
        if (mIsMeeting) {
            handler.sendEmptyMessage(MSG_TITLE_COMPLETE_ENABLE);
        } else {
            handler.sendEmptyMessage(MSG_TITLE_COMPLETE_DISENABLE);
        }
        // TODO:召开会议时，选择0人，也可以点击“确定”--end

        adapter = new SelectLinkManListAdapter(this, isNotSignale, true);
        adapter.setChangeDataListener(this);
        adapter.setHasCatalog(true);
        if (OPT_FORWARDING_FOR_RESULT.equals(getIntent().getStringExtra(
            OPT_FLAG))) {
            adapter.setLetters();
        }
        if (backBySingleClick) {
            adapter.setHasSelectedButton(false);
            titleBar.setRightBtnVisibility(View.INVISIBLE);
        } else {
            adapter.setHasSelectedButton(true);
        }

        listVew = (ListView) findViewById(R.id.lv_linkman);
        listVew.setDivider(null);
        emptyView = this.findViewById(R.id.empty_view);
        listVew.setAdapter(adapter);
        listVew.setDividerHeight(0);
        listVew.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long arg3) {

                SelectLinkManListAdapter.ViewHolder holder = (ViewHolder) view.getTag();
                // liujc 如果已是本群好友，默认不可点击
                if (OPT_INVITE_START_FOR_RESULT.equals(getIntent()
                    .getStringExtra(OPT_FLAG))
                    && listContact != null
                    && listContact.contains(list.get(position)
                    .getNubeNumber())) {
                    holder.select.setEnabled(false);
                    CustomLog.d(TAG, "该联系人已是本群好友：" + list.get(position).getNubeNumber());
                    return;
                } else {
                    holder.select.setEnabled(true);
                }
                // TODO:极会议版本-本地分享时，根据设备类型过滤好友--start
                if (shareFlag && !mIsForward) {
                    ContactFriendBean friend = (ContactFriendBean) listVew
                        .getItemAtPosition(position);
                    if (checkDeviceLimit(friend)) {
                        return;
                    }
                }
                // TODO:极会议版本-本地分享时，根据设备类型过滤好友--end

                if (backBySingleClick) {
                    // TODO:
                    if (isNotSignale) {
                        holder.select.setChecked(true);
                        adapter.select(position, holder.select.isChecked());
                    } else {
                        holder.rbSignal.toggle();
                        adapter.select(position, holder.rbSignal.isChecked());
                    }
                    submitData();
                    return;
                }

                if (isNotSignale) {
                    holder.select.toggle();
                } else {
                    holder.rbSignal.toggle();
                }

                if (!isNotSignale) {
                    adapter.disSelectAll();
                    handler.sendEmptyMessage(MSG_TITLE_COMPLETE_DISENABLE);
                }

                if (isNotSignale) {
                    if (holder.select.isChecked()) {
                        // if (selectMap.size() < MUTIL_SELECT_MAX_COUNT) {
                        // adapter.select(position, holder.select.isChecked());
                        // handler.sendEmptyMessage(MSG_TITLE_COMPLETE_ENABLE);
                        // } else {
                        // Toast.makeText(SelectLinkManActivity.this,
                        // "最多只能选择8个联系人", Toast.LENGTH_SHORT).show();
                        // CustomLog("最多只能选择8个联系人");
                        // }
                        adapter.select(position, holder.select.isChecked());
                        handler.sendEmptyMessage(MSG_TITLE_COMPLETE_ENABLE);
                    } else {
                        adapter.select(position, holder.select.isChecked());
                        // if (selectMap.size() - sharePictureManLst.size() ==
                        // 0)
                        if (0 == list2.size() && !mIsMeeting) {
                            handler.sendEmptyMessage(MSG_TITLE_COMPLETE_DISENABLE);
                        } else {
                            handler.sendEmptyMessage(MSG_TITLE_COMPLETE_ENABLE);
                        }
                    }

                } else {
                    adapter.select(position, holder.rbSignal.isChecked());
                    handler.sendEmptyMessage(MSG_TITLE_COMPLETE_ENABLE);
                }

                adapter.notifyDataSetChanged();
            }
        });
        initSideBar();
        chooseGroupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectLinkManActivity.this, SelectGroupActivity.class);
                startActivity(intent);
            }
        });

        addContactFriendBtn = (Button) findViewById(R.id.contacts__addfriend_btn);

        addContactFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                //如果跳转至通讯录，回来在 onResume 时刷新页面数据
                isRefreshList = true;
                CustomLog.d(TAG, "bButtonAddFriend onClick");
                Intent intent1 = new Intent();
                intent1.setClass(SelectLinkManActivity.this, AddContactActivity.class);
                intent1.putExtra("REQUEST_CODE", ContactTransmitConfig.REQUEST_CONTACT_CODE);
                startActivityForResult(intent1, 0);
            }
        });

    }


    /**
     * @param friend :该行 ContactFrendPo对象
     * @return true:设备不支持；false:设备支持
     */
    private boolean checkDeviceLimit(ContactFriendBean friend) {

        // 修改：用能力值去判断设备发送消息类型的能力--modify on 2015-12-8
        //        byte ovell = ButelOvell.getNubeOvell(friend.getNubeNumber());
        //
        //        if (shareType == ShareLocalActivity.CHOOSER_TYPE_IMAGE) {
        //            // 图片分享：
        //            if (!ButelOvell.hasSendPicturesAbility(ovell)) {
        //
        //                Toast.makeText(this, "该设备不能分享图片", Toast.LENGTH_SHORT).show();
        //                return true;
        //            }
        //        } else if (shareType == ShareLocalActivity.CHOOSER_TYPE_VIDEO) {
        //            // 视频分享
        //            if (!ButelOvell.hasSendVedioAbility(ovell)) {
        //                Toast.makeText(this, "该设备不能分享视频", Toast.LENGTH_SHORT).show();
        //                return true;
        //            }
        //        } else {
        //            // 同时包含 图片、视频
        //            if (!ButelOvell.hasSendPicturesAbility(ovell)
        //                    && !ButelOvell.hasSendVedioAbility(ovell)) {
        //
        //                Toast.makeText(this, "该设备不能分享图片和视频", Toast.LENGTH_SHORT).show();
        //                return true;
        //            }
        //            if (!ButelOvell.hasSendPicturesAbility(ovell)
        //                    && ButelOvell.hasSendVedioAbility(ovell)) {
        //
        //                Toast.makeText(this, "该设备不能分享图片", Toast.LENGTH_SHORT).show();
        //                return true;
        //            }
        //            if (ButelOvell.hasSendPicturesAbility(ovell)
        //                    && !ButelOvell.hasSendVedioAbility(ovell)) {
        //
        //                Toast.makeText(this, "该设备不能分享视频", Toast.LENGTH_SHORT).show();
        //                return true;
        //            }
        //
        //        }
        return false;
    }


    private void initSideBar() {

        sideBar = (SideBar) findViewById(R.id.linkman_sideBar);
        // 在屏幕中央添加一个TextView，用来提示快速定位条选中的字母
        dialogView = (View) LayoutInflater.from(SelectLinkManActivity.this)
                .inflate(R.layout.linkman_list_position, null);
        mDialogText = (TextView) dialogView.findViewById(R.id.position_text);
        sideBar.setTextView(mDialogText);
        sideBar.setPopView(dialogView);
        sideBar.setListView(listVew);

    }


    // 初始化布局
    private void initHorizontalScrollView() {
        // if (selectMap.size() - sharePictureManLst.size()== 0)
        // //有该判断是为了是否将由其他页面携带过来的数据显示到顶部横向滚动条中
        if (list2.size() == 0) {
            //极会议中：取消所选的人，“确定”还是可点击的--begin-
            if (mIsMeeting) {
                handler.sendEmptyMessage(MSG_TITLE_COMPLETE_ENABLE);
            } else {
                handler.sendEmptyMessage(MSG_TITLE_COMPLETE_DISENABLE);
            }
            //极会议中：取消所选的人，“确定”还是可点击的--end-
        } else {
            handler.sendEmptyMessage(MSG_TITLE_COMPLETE_ENABLE);
        }
        // mHorizontalScrollView = (CustomHorizontalScrollView)
        // findViewById(R.id.id_horizontalScrollView);
        mHorizontalScrollView.setCount(list2.size());
        // mAdapter = new CustomHorizontalScrollViewAdapter(this, list2);
        mAdapter.setData(list2);
        // 添加滚动回调
        mHorizontalScrollView
            .setCurrentImageChangeListener(
                new CustomHorizontalScrollView.CurrentImageChangeListener() {
                    @Override
                    public void onCurrentImgChanged(int position,
                                                    View viewIndicator) {
                        // viewIndicator.setBackgroundColor(Color
                        // .parseColor("#AA024DA4"));
                    }
                });
        // 添加点击回调
        mHorizontalScrollView
            .setOnItemClickListener(new CustomHorizontalScrollView.OnItemClickListener() {

                public void onClick(View view, int pos) {
                    if (OPT_INVITE_START_FOR_RESULT.equals(getIntent()
                        .getStringExtra(OPT_FLAG))
                        && listContact != null
                        && listContact.contains(list2.get(pos)
                        .getNubeNumber())) {
                        return;
                    }
                    CustomLog.d(TAG, "点击对应手机号：" + list2.get(pos).getNumber());
                    adapter.select(
                        mapPosition.get(list2.get(pos).getContactId()),
                        false);
                    adapter.notifyDataSetChanged();
                }
            });
        // 设置适配器
        mHorizontalScrollView.initDatas(mAdapter);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (waitDialog != null) {
            waitDialog.clearAnimation();
            waitDialog = null;
        }
    }


    private void initData() {

        if (waitDialog == null) {
            waitDialog = new CommonWaitDialog(this,
                getString(R.string.selectlinkman_waitdialog));
            waitDialog.startAnimation();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // list = dao.getAppLinkmanData();
                    // 移交群主 数据源设置
                    if (OPT_HAND_OVER_START_FOR_RESULT.equals(getIntent()
                        .getStringExtra(OPT_FLAG))) {
                        list = (List<ContactFriendBean>) getIntent().getExtras()
                            .get(HAND_OVER_MASTER_LIST);
                    } else {

                        //获取联系人数据
                        list = dao.getAppLinkmanData();

                        // TODO:转发时，根据消息类型过滤联系人
                        if (mIsForward) {
                            CustomLog.d(TAG, "转发-根据消息类型过滤联系人");
                            String msgId = getIntent().getExtras().getString(
                                ShareLocalActivity.MSG_ID);
                            boolean singleMsg = true;
                            if (!TextUtils.isEmpty(msgId)) {
                                String[] ids = msgId.split(",");
                                if (ids != null && ids.length > 1) {
                                    singleMsg = false;
                                }
                            }
                            collFlag = getIntent().getExtras()
                                .getInt(ShareLocalActivity.KEY_COLLECTION_FORWARD, 0);
                            if (singleMsg) {
                                int type = 0;
                                if (collFlag == 0) {
                                    noticesDao = new NoticesDao(
                                        SelectLinkManActivity.this);
                                    NoticesBean bean = noticesDao.getNoticeById(msgId);
                                    type = bean.getType();
                                } else if(collFlag == 2) {
                                    type = FileTaskManager.NOTICE_TYPE_ARTICAL_SEND;
                                }else{
                                        mCollectionDao = new CollectionDao(SelectLinkManActivity.this);
                                        CollectionEntity mCollectionEntity
                                                = mCollectionDao.getCollectionEntityById(msgId);
                                        type = mCollectionEntity.getType();
                                }
                                filterListbyDeviceType(type);
                            }
                        }
                        // TODO:召开会议-需要过滤 X1/N7/N8
                        if (mIsMeeting) {
                            CustomLog.d(TAG, "召开会议-过滤联系人");
                            List<String> nubes = new ArrayList<String>();
                            if (list != null && list.size() > 0) {
                                for (int i = 0; i < list.size(); i++) {
                                    String nube = list.get(i).getNubeNumber();
                                    byte ovell = ButelOvell.getNubeOvell(nube);
                                    if (!ButelOvell.hasMeetingAbility(ovell)) {
                                        nubes.add(nube);
                                    }
                                }
                                // 从联系人移除：
                                for (int i = 0; i < nubes.size(); i++) {
                                    String nube = nubes.get(i);
                                    for (int j = 0; j < list.size(); j++) {
                                        if (list.get(j).getNubeNumber()
                                            .equals(nube)) {
                                            list.remove(j);
                                            CustomLog.d(TAG, "过滤没有会议能力的联系人：nube="
                                                + nube);
                                            break;
                                        }
                                    }
                                }
                            }

                        }
                        // TODO:与会话功能相关入口（消息、群邀请）:要求设备有会话能力（可以发文本和语音）
                        if (mIsMsg || mIsInvite) {
                            if (mIsMsg) {
                                CustomLog.d(TAG, "消息-过滤联系人");
                            } else {
                                CustomLog.d(TAG, "群邀请-过滤联系人");
                            }

                            List<String> nubes = new ArrayList<String>();
                            if (list != null && list.size() > 0) {
                                for (int i = 0; i < list.size(); i++) {
                                    String nube = list.get(i).getNubeNumber();
                                    byte ovell = ButelOvell.getNubeOvell(nube);
                                    if (!ButelOvell
                                        .hasSendMessageAbility(ovell)
                                        || !ButelOvell
                                        .hasSendRecordAbility(ovell)) {
                                        nubes.add(nube);
                                    }
                                }
                                // 从联系人移除：
                                for (int i = 0; i < nubes.size(); i++) {
                                    String nube = nubes.get(i);
                                    for (int j = 0; j < list.size(); j++) {
                                        if (list.get(j).getNubeNumber()
                                            .equals(nube)) {
                                            list.remove(j);
                                            CustomLog.d(TAG, "过滤没有会话能力的联系人：nube="
                                                + nube);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        // 转发时选择收件人中的群组数据源
                        if (OPT_FORWARDING_FOR_RESULT.equals(getIntent()
                            .getStringExtra(OPT_FLAG))) {
                            GroupList = groupDao.queryAllGroup();
                        }// --转发入口已修改：目前用的是和分享同一个入口-- annotation added on
                        // 2015-11-20
                    }
                    if (OPT_FORWARDING_FOR_RESULT.equals(getIntent()
                        .getStringExtra(OPT_FLAG))) {
                        sortListData2();
                    } else {
                        sortListData();
                    }
                    handler.sendEmptyMessage(MSG_UPDATE_UI);

                } catch (Exception e) {
                    CustomLog.e(TAG, "handler.sendEmptyMessage出现异常" + e.toString());
                    e.printStackTrace();
                    if (handler != null) {
                        handler.sendEmptyMessage(MSG_BUSINESS_ERROR);
                    }
                }
            }
        }).start();
    }


    /**
     * 该方法只有转发消息调用：根据消息类型，筛选nube好友列表。屏蔽某些设备好友
     *
     * @param msgType 消息类型
     */
    private void filterListbyDeviceType(int msgType) {
        List<String> nubes = new ArrayList<String>();
        byte ovell;

        if (FileTaskManager.NOTICE_TYPE_TXT_SEND == msgType
                || FileTaskManager.NOTICE_TYPE_ARTICAL_SEND == msgType) {
            CustomLog.d(TAG, "转发的消息类型为:文本");

            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    ContactFriendBean contact = list.get(i);
                    String nube = contact.getNubeNumber();
                    //                    ovell = ButelOvell.getNubeOvell(nube);
                    //                    if (!ButelOvell.hasSendMessageAbility(ovell)) {
                    //                        CustomLog.d("文本：过滤  " + list.get(i).getNubeNumber());
                    //                        nubes.add(list.get(i).getNubeNumber());
                    //                    } else {
                    //                        CustomLog.d("文本：不过滤  " + list.get(i).getNubeNumber());
                    //                    }
                }
            }
        } else if (FileTaskManager.NOTICE_TYPE_AUDIO_SEND == msgType) {
            CustomLog.d(TAG, "转发的消息类型为:语音");

            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    ContactFriendBean contact = list.get(i);
                    String nube = contact.getNubeNumber();
                    //                    ovell = ButelOvell.getNubeOvell(nube);
                    //                    if (!ButelOvell.hasSendRecordAbility(ovell)) {
                    //                        nubes.add(list.get(i).getNubeNumber());
                    //                    }
                }
            }
        } else if (FileTaskManager.NOTICE_TYPE_VEDIO_SEND == msgType) {
            CustomLog.d(TAG, "转发的消息类型为:视频");

            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    ContactFriendBean contact = list.get(i);
                    String nube = contact.getNubeNumber();
                    //                    ovell = ButelOvell.getNubeOvell(nube);
                    //                    if (!ButelOvell.hasSendVedioAbility(ovell)) {
                    //                        nubes.add(list.get(i).getNubeNumber());
                    //                    }
                }
            }
        } else if (FileTaskManager.NOTICE_TYPE_PHOTO_SEND == msgType) {
            CustomLog.d(TAG, "转发的消息类型为:图片");

            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    ContactFriendBean contact = list.get(i);
                    String nube = contact.getNubeNumber();
                    //                    ovell = ButelOvell.getNubeOvell(nube);
                    //                    if (!ButelOvell.hasSendPicturesAbility(ovell)) {
                    //                        nubes.add(list.get(i).getNubeNumber());
                    //                    }
                }
            }
        } else if (FileTaskManager.NOTICE_TYPE_VCARD_SEND == msgType) {
            CustomLog.d(TAG, "转发的消息类型为:名片");

            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    ContactFriendBean contact = list.get(i);
                    String nube = contact.getNubeNumber();
                    //                    ovell = ButelOvell.getNubeOvell(nube);
                    //                    if (!ButelOvell.hasSendCardAbility(ovell)) {
                    //                        nubes.add(list.get(i).getNubeNumber());
                    //                    }
                }
            }
        } else if (FileTaskManager.NOTICE_TYPE_RECORD == msgType) {
            // 视频通话：手机、N8J、X1、N7/N8;过滤：M1
            CustomLog.d(TAG, "转发的消息类型为:视频通话");
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    ContactFriendBean contact = list.get(i);
                    String nube = contact.getNubeNumber();
                    //                    ovell = ButelOvell.getNubeOvell(nube);
                    //                    if (!ButelOvell.hasCallAbility(ovell)) {
                    //                        nubes.add(list.get(i).getNubeNumber());
                    //                    }
                }
            }
        } else if (FileTaskManager.NOTICE_TYPE_MEETING_INVITE == msgType) {
            // 视频会议：手机、M1、N8J;过滤：X1,N7/N8
            CustomLog.d(TAG, "转发的消息类型为:视频会议");
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    ContactFriendBean contact = list.get(i);
                    String nube = contact.getNubeNumber();
                    //                    ovell = ButelOvell.getNubeOvell(nube);
                    //                    if (!ButelOvell.hasMeetingAbility(ovell)) {
                    //                        nubes.add(list.get(i).getNubeNumber());
                    //                    }
                }
            }
        }
        // 从联系人移除：
        for (int i = 0; i < nubes.size(); i++) {
            String nube = nubes.get(i);
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).getNubeNumber().equals(nube)) {
                    list.remove(j);
                    CustomLog.d(TAG, "转发时，过滤联系人：nube=" + nube);
                    break;
                }
            }
        }
    }


    private String getDisplayName(List<String> names, List<String> nickNames,
                                  List<String> nubes) {

        if (names == null || names.size() == 0) {
            return "";
        }
        String name = "";
        if (names.size() < 3) {
            for (int i = 0; i < names.size(); i++) {
                if (!TextUtils.isEmpty(names.get(i))) {
                    name += names.get(i) + "、";
                } else if (!TextUtils.isEmpty(nickNames.get(i))) {
                    name += nickNames.get(i) + "、";
                } else {
                    name += nubes.get(i) + "、";
                }

            }
            name = name.substring(0, name.length() - 1);
        } else {

            for (int i = 0; i < 3; i++) {
                if (!TextUtils.isEmpty(names.get(i))) {
                    name += names.get(i) + "、";
                } else if (!TextUtils.isEmpty(nickNames.get(i))) {
                    name += nickNames.get(i) + "、";
                } else {
                    name += nubes.get(i) + "、";
                }
            }
            name = name.substring(0, name.length() - 1);
        }

        return name;
    }


    // 设置“本地分享”确认发送dialog
    private void initSelfControl(View selfView, List<String> nubes,
                                 List<String> names, List<String> nickNames, List<String> headUrls) {

        TextView nameView = (TextView) selfView.findViewById(R.id.name_txt);
        TextView numView = (TextView) selfView
            .findViewById(R.id.recv_num_field);
        SharePressableImageView icon = (SharePressableImageView) selfView
            .findViewById(R.id.contact_icon);

        if (names.size() > 1) {
            // 群成员人数
            int groupMemberSize = names.size();

            numView.setVisibility(View.VISIBLE);
            numView.setText(groupMemberSize + getString(R.string.person));

            icon.shareImageview.setImageResource(R.drawable.group_icon);

            String groupName = getDisplayName(names, nickNames, nubes);
            nameView.setText(groupName);

        } else {

            int sexIconId = IMCommonUtil.getHeadIdBySex(getString(R.string.man));
            Glide.with(SelectLinkManActivity.this).
                load(headUrls.get(0))
                .placeholder(sexIconId).
                error(sexIconId).centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade().into(icon.shareImageview);

            // 名称显示规则
            String singleName = "";
            if (!TextUtils.isEmpty(names.get(0))) {
                singleName += names.get(0);
            } else if (!TextUtils.isEmpty(nickNames.get(0))) {
                singleName += nickNames.get(0);
            } else {
                singleName += nubes.get(0);
            }
            nameView.setText(singleName);
            numView.setVisibility(View.GONE);
        }
    }


    public void showShareDialog() {

        Iterator<String> iter = selectMap.keySet().iterator();
        ArrayList<String> nubeNumbers = new ArrayList<String>();
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> nickNames = new ArrayList<String>();
        ArrayList<String> headUrls = new ArrayList<String>();
        while (iter.hasNext()) {
            ContactFriendBean po = selectMap.get(iter.next());
            // 过滤掉携带过来的元素，只返回本次选中的记录
            if (sharePictureManLst != null
                && sharePictureManLst.contains(po.getNubeNumber())) {
                continue;
            }
            nubeNumbers.add(po.getNubeNumber());
            names.add(po.getName());
            nickNames.add(po.getNickname());
            headUrls.add(po.getHeadUrl());
        }
        // 如果是从本地分享“选择"页面进入，则先弹出确认dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View selfView = inflater.inflate(R.layout.share_confirm_dialog_view,
            null);

        // 自定义dialog view
        initSelfControl(selfView, nubeNumbers, names, nickNames, headUrls);

        CommonDialog conDlg = new CommonDialog(this, getLocalClassName(), 300);
        conDlg.addView(selfView);
        conDlg.setCancelable(false);
        conDlg.setTitleVisible(getString(R.string.share_dialog_title));
        if(collFlag == 0){
            switch (chatForwardType)
            {
                case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                    String txt= getIntent().getExtras().getString("chatForwardTxt");
                    conDlg.setTransmitInfo(txt);
                    break;
                case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                    String localPicFilePath = getIntent().getExtras().getString("chatForwardPath");
                    if(localPicFilePath.startsWith("http://")){
                        conDlg.setTrasmitPic(localPicFilePath,R.drawable.default_link_pic,0);
                    }else{
                        Bitmap bitmap= BitmapFactory.decodeFile(localPicFilePath);
                        conDlg.setTransmitPic(bitmap);
                    }
                    break;
                case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                    String localVideoFilePath = getIntent().getExtras().getString("chatForwardPath");
                    if(localVideoFilePath.startsWith("http://")){
                        conDlg.setTrasmitPic(localVideoFilePath,R.drawable.default_link_pic,1);
                    }else{
                        Bitmap bitmap1= FileManager.createVideoThumbnail(localVideoFilePath);
                        Bitmap  bitmap2 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.video_icon);
                        Bitmap bitmap3=combineBitmap(bitmap1,bitmap2);
                        conDlg.setTransmitPic(bitmap3);
                    }
                    break;
                case FileTaskManager.NOTICE_TYPE_VCARD_SEND:
                    String chatForwardVcardName=getIntent().getExtras().getString("chatForwardVcardName");
                    String chatForwardVcardNumber=getIntent().getExtras().getString("chatForwardVcardNumber");
                    //Toast.makeText(ShareLocalActivity.this,"名片"+chatForwardVcardName+","+chatForwardVcardNumber,Toast.LENGTH_LONG).show();
                    conDlg.setTransmitCardInfo(chatForwardVcardName,Integer.parseInt(chatForwardVcardNumber));
                    break;
                case FileTaskManager.NOTICE_TYPE_MEETING_INVITE:
                    String chatForwardCreator=getIntent().getExtras().getString("chatForwardCreator");
                    String chatForwardMeetingRoomId=getIntent().getExtras().getString("chatForwardMeetingRoomId");
                    conDlg.setTransmitMeetingInfoInstance(chatForwardCreator,chatForwardMeetingRoomId);
                    break;
                case FileTaskManager.NOTICE_TYPE_MEETING_BOOK:
                    String chatForwardCreator2=getIntent().getExtras().getString("chatForwardCreator");
                    String chatForwardMeetingRoomId2=getIntent().getExtras().getString("chatForwardMeetingRoomId");
                    String chatForwardMeetingTopic=getIntent().getExtras().getString("chatForwardMeetingTopic");
                    String chatForwardDate=getIntent().getExtras().getString("chatForwardDate");
                    String chatForwardHms=getIntent().getExtras().getString("chatForwardHms");
                    conDlg.setTransmitMeetingInfoBook(chatForwardCreator2,chatForwardMeetingRoomId2,chatForwardMeetingTopic,chatForwardDate,chatForwardHms);
                    break;
                case FileTaskManager.NOTICE_TYPE_MANY_MSG_FORWARD:
                    int noticeNum=getIntent().getExtras().getInt("noticeNum");
                    String me=getIntent().getExtras().getString("me");
                    String theOther=getIntent().getExtras().getString("theOther");
                    if(forwradType == 0)
                    {
                        if(noticeNum>0) {
                            conDlg.setTransmitItemByItem(noticeNum+"");
                        }
                    }
                    else if(forwradType == 1)
                    {
                        conDlg.setSingleMerge(me,theOther);
                    }
                    break;
                case FileTaskManager.NOTICE_TYPE_ARTICAL_SEND:
                    conDlg.setTransmitInfo(getString(R.string.article) + getIntent().getExtras().getString("chatForwardTxt"));
                    break;
                case FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND:
                    conDlg.setTransmitInfo(getString(R.string.chat_record_bracket) + getIntent().getExtras().getString("chatForwardTxt"));
                    break;
                default:
                    break;
            }
        }else if(collFlag == 1) {
            //从收藏内转发
            if (collectItemInfo == null) {
                CustomLog.e(TAG, "收藏信息传过来为空");
                return;
            }
            collectItemInfo.getType();
            switch (collectItemInfo.getType()) {
                case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                    String txt = collectItemInfo.getTxt();
                    conDlg.setTransmitInfo(txt);
                    break;
                case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                    conDlg.setTrasmitPic(collectItemInfo.getRemoteUrl(), R.drawable.default_link_pic,0);
                    break;
                case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                    conDlg.setTrasmitPic(collectItemInfo.getThumbnailRemoteUrl(), R.drawable.default_link_pic,1);
                    break;
                default:
                    break;
            }
        }
        else{
            //文章类型转发
            String txt= getIntent().getExtras().getString("articleTitle");
            conDlg.setTransmitInfo(getString(R.string.artical) + txt);
        }
        conDlg.setCancleButton(null, btn_cancle);
        conDlg.setPositiveButton(new CommonDialog.BtnClickedListener() {

            @Override
            public void onBtnClicked() {
                // 进行下面的发消息流程

                submitData();
            }
        }, R.string.btn_send);
        conDlg.showDialog();
    }


    private void submitData() {
        // if (0 == selectMap.size() - sharePictureManLst.size())
        if (0 == list2.size()
            && !OPT_HAND_OVER_START_FOR_RESULT.equals(getIntent()
            .getStringExtra(OPT_FLAG))
            && !getString(R.string.select_vcard_title).equals(title)
            && !OPT_FORWARDING_FOR_RESULT.equals(getIntent()
            .getStringExtra(OPT_FLAG)) && !mIsMeeting) {// 增加一个！mIsMeeting条件
            Toast.makeText(this, getString(R.string.select_tip),
                Toast.LENGTH_SHORT).show();
            CustomLog.d(TAG, "选择了的联系人数：" + selectMap.size());
            adapter.disSelectAll();
            adapter.notifyDataSetChanged();
            return;
        }

        if (startForResult) {

            CustomLog.d(TAG, "选择了的联系人数：" + selectMap.size());
            Iterator<String> iter = selectMap.keySet().iterator();
            ArrayList<String> nubeNumbers = new ArrayList<String>();
            ArrayList<String> numbers = new ArrayList<String>();
            ArrayList<String> names = new ArrayList<String>();
            ArrayList<String> nickNames = new ArrayList<String>();
            ArrayList<String> headUrls = new ArrayList<String>();
            ArrayList<String> userIds = new ArrayList<String>();
            while (iter.hasNext()) {
                ContactFriendBean po = selectMap.get(iter.next());
                // 过滤掉携带过来的元素，只返回本次选中的记录
                if (sharePictureManLst != null
                    && sharePictureManLst.contains(po.getNubeNumber())) {
                    continue;
                }
                nubeNumbers.add(po.getNubeNumber());
                numbers.add(po.getNumber());
                names.add(po.getName());
                nickNames.add(po.getNickname());
                headUrls.add(po.getHeadUrl());
                // 此处应该传contactUserId，但数据库检索时，
                // contactUserId数据保存到了po对象的sourcesId，故此处取sourcesId
                userIds.add(po.getSourcesId());
            }
            Intent intent = new Intent();
            intent.putStringArrayListExtra(START_RESULT_NUMBER, numbers);
            intent.putStringArrayListExtra(START_RESULT_NUBE, nubeNumbers);
            intent.putStringArrayListExtra(START_RESULT_NAME, names);
            intent.putStringArrayListExtra(START_RESULT_NICKNAME, nickNames);
            intent.putStringArrayListExtra(START_RESULT_HEADURL, headUrls);
            intent.putStringArrayListExtra(START_RESULT_USERID, userIds);
            this.setResult(RESULT_OK, intent);
            this.finish();
        } else {
            Intent intent = new Intent();
            Iterator<String> iter = selectMap.keySet().iterator();
            StringBuffer strBuff = new StringBuffer();
            if (iter.hasNext()) {

                //				MobclickAgent.onEvent(this,
                //						CommonConstant.UMENG_KEY_N_Share_card_receiver);
                ArrayList<ContactFriendBean> nubeList = new ArrayList<ContactFriendBean>();
                while (iter.hasNext()) {
                    ContactFriendBean po = selectMap.get(iter.next());
                    nubeList.add(po);
                    strBuff.append(po.getNubeNumber() + ";");
                }
                // 20150316 删除ShareVcardConfirmActivity
                // // TODO:此处需要把ContactFriendBean 对象的list传递到下个页面中，快速构建列表
                // Bundle nubeExtras = new Bundle();
                // nubeExtras.putSerializable("linkman", nubeList);
                // intent.putExtra("nubelinkman", nubeExtras);
                // // 把上个页面传入的名片信息继续向下传递
                // intent.putExtra("systemlinkman",
                // getIntent().getBundleExtra("systemlinkman"));
                // // 接收人的视频号串
                // intent.putExtra("phone", strBuff.toString());
                //
                // intent.setClass(this, ShareVcardConfirmActivity.class);
                // startActivityForResult(intent, 10001);
            } else {
                Toast.makeText(this, getString(R.string.none_linkman),
                    Toast.LENGTH_SHORT).show();
                CustomLog.d(TAG, "未获得联系人号码");
                adapter.disSelectAll();
                adapter.notifyDataSetChanged();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 10001) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        }
    }


    /**
     * @author: sunkai
     * @Title: disSelectItem
     * @Description: 不选择该数据，如果已经选择，则移除
     * @date 2013-8-16 上午11:09:48
     */
    @Override
    public void disSelectItem(ContactFriendBean friend, int position) {
        if (selectMap.containsKey(friend.getContactId())) {
            selectMap.remove(friend.getContactId());
            list2.remove(friend);
            mapPosition.remove(adapter.getSelectContact(position));
            initHorizontalScrollView();
            CustomLog.d(TAG, "删除了一个联系人");
        }

    }


    /**
     * @author: sunkai
     * @Title: selectItem
     * @Description: 选择该数据，加入到map
     * @date 2013-8-16 上午11:10:09
     */
    @Override
    public void selectItem(ContactFriendBean friend, int position) {
        mapPosition.put(adapter.getSelectContact(position), position);// liujc
        // 存放对应联系人的位置，以便点击头像去除选中状态
        selectMap.put(friend.getContactId(), friend);
        if (TextUtils.isEmpty(friend.getNubeNumber())) {
            Toast.makeText(SelectLinkManActivity.this,
                getString(R.string.no_nubenumber), Toast.LENGTH_SHORT)
                .show();
            CustomLog.d(TAG, "该联系人无视讯号");
        }
        // 表示由新建消息进入，未携带任何数据，直接插入list2，
        // 如果由邀请入群聊进入，判断是否已是群成员，如果已是群成员则不显示在顶部横向滚动条内
        if (TextUtils.isEmpty(title)
            && sharePictureManLst != null
            && (sharePictureManLst.isEmpty() || !sharePictureManLst
            .contains(friend.getNubeNumber()))) {
            list2.add(friend);
            initHorizontalScrollView();
        }
        // 移交群主 直接跳转故不需要点击完成按钮
        // else if
        // (OPT_HAND_OVER_START_FOR_RESULT.equals(getIntent().getStringExtra(OPT_FLAG)))
        // {
        // list2.clear();
        // selectMap.clear();
        // selectMap.put(friend.getContactId(), friend);
        // list2.add(friend);
        // initHorizontalScrollView();
        // }
    }


    /**
     * @author: sunkai
     * @Title: disSelectAll
     * @Description: 全不选所有数据，清空map
     * @date 2013-8-16 上午11:10:56
     */
    @Override
    public void disSelectAll() {
        selectMap.clear();
    }


    @Override
    public void onClickHeadIcon(ContactFriendBean friend, int position) {

    }


    private void sortListData2() {
        subMap.clear();
        positonMap.clear();
        if (list == null && GroupList == null) {
            return;
        }

        // 不包含在定位条内的非法数据
        List<ContactFriendBean> tempList = new ArrayList<ContactFriendBean>();
        // 比最大常用ASCII大的异常数据
        List<ContactFriendBean> tempList2 = new ArrayList<ContactFriendBean>();
        int tempsize = 0;
        String sortKey = "";
        if (GroupList != null) {
            tempsize = GroupList.size();
            // 本地发现好友定位
            if (tempsize > 0) {
                for (int tempcount = tempsize - 1; tempcount >= 0; tempcount--)// 遍历群组信息
                {
                    sortKey = "☆";
                    // 记录当前的位置需要被减一
                    if (subMap.get(sortKey) != null) {
                        subMap.put(sortKey, subMap.get(sortKey) + 1);
                    } else {
                        subMap.put(sortKey, 1);
                    }
                    if (positonMap.get(sortKey) == null) {
                        positonMap.put(sortKey, tempcount);
                    }
                }

            }
            GroupList.addAll(list);
            list.clear();
            list.addAll(GroupList);
        }
        currentSortKey = "";
        int size = list.size();
        for (int count = size - 1; count >= 0 + tempsize; count--) {
            sortKey = list.get(count).getPym();
            if (TextUtils.isEmpty(sortKey)) {
                tempList.add(list.get(count)); // 倒叙遍历，小的添加到最底部
                list.remove(count);
                if (!TextUtils.isEmpty(currentSortKey)) {
                    // 记录当前的位置需要被减一
                    if (subMap.get(currentSortKey) != null) {
                        subMap.put(currentSortKey,
                            subMap.get(currentSortKey) + 1);
                    } else {
                        subMap.put(currentSortKey, 1);
                    }
                }
                continue;
            } else {
                sortKey = sortKey.substring(0, 1);
                sortKey = sortKey.toUpperCase();
                if (sortKey.compareTo(max) > 0) {// 比最大常用ASCII大的异常数据
                    tempList2.add(list.get(count));
                    list.remove(count);
                    if (!TextUtils.isEmpty(currentSortKey)) {
                        // 记录当前的位置需要被减一
                        if (subMap.get(currentSortKey) != null) {
                            subMap.put(currentSortKey,
                                subMap.get(currentSortKey) + 1);
                        } else {
                            subMap.put(currentSortKey, 1);
                        }
                    }
                    continue;
                } else if (!IMConstant.letter.contains(sortKey)) {
                    // 不包含在定位条内的非法数据
                    tempList.add(list.get(count));
                    list.remove(count);
                    if (!TextUtils.isEmpty(currentSortKey)) {
                        // 记录当前的位置需要被减一
                        if (subMap.get(currentSortKey) != null) {
                            subMap.put(currentSortKey,
                                subMap.get(currentSortKey) + 1);
                        } else {
                            subMap.put(currentSortKey, 1);
                        }
                    }
                    continue;
                }
            }
            if (positonMap.get(sortKey) == null) {
                currentSortKey = sortKey; // 当前sortkey
                positonMap.put(sortKey, count);
                CustomLog.d("TAG", "currentSortKey:" + sortKey);
            }
        }
        if (tempList2 != null && tempList2.size() > 0) {
            illegalDataCount = illegalDataCount + tempList2.size();
            list.addAll(tempList2);
        }
        if (tempList != null && tempList.size() > 0) {
            illegalDataCount = illegalDataCount + tempList.size();
            list.addAll(tempList);
        }
    }


    private void sortListData() {
        CustomLog.d(TAG, "sortListData数据排序");
        if (list == null) {
            return;
        }
        // 不包含在定位条内的非法数据
        List<ContactFriendBean> tempList = new ArrayList<ContactFriendBean>();
        // 比最大常用ASCII大的异常数据
        List<ContactFriendBean> tempList2 = new ArrayList<ContactFriendBean>();
        int size = list.size();
        String sortKey = "";
        for (int count = size - 1; count >= 0; count--) {
            sortKey = list.get(count).getPym();
            if (TextUtils.isEmpty(sortKey)) {
                tempList.add(list.get(count)); // 倒叙遍历，小的添加到最底部
                list.remove(count);
                if (!TextUtils.isEmpty(currentSortKey)) {
                    // 记录当前的位置需要被减一
                    if (subMap.get(currentSortKey) != null) {
                        subMap.put(currentSortKey,
                            subMap.get(currentSortKey) + 1);
                    } else {
                        subMap.put(currentSortKey, 1);
                    }
                }
                continue;
            } else {
                sortKey = sortKey.substring(0, 1);
                sortKey = sortKey.toUpperCase();
                if (sortKey.compareTo(max) > 0) {// 比最大常用ASCII大的异常数据
                    tempList2.add(list.get(count));
                    list.remove(count);
                    if (!TextUtils.isEmpty(currentSortKey)) {
                        // 记录当前的位置需要被减一
                        if (subMap.get(currentSortKey) != null) {
                            subMap.put(currentSortKey,
                                subMap.get(currentSortKey) + 1);
                        } else {
                            subMap.put(currentSortKey, 1);
                        }
                    }
                    continue;
                } else if (!IMConstant.letter.contains(sortKey)) {
                    // 不包含在定位条内的非法数据
                    tempList.add(list.get(count));
                    list.remove(count);
                    if (!TextUtils.isEmpty(currentSortKey)) {
                        // 记录当前的位置需要被减一
                        if (subMap.get(currentSortKey) != null) {
                            subMap.put(currentSortKey,
                                subMap.get(currentSortKey) + 1);
                        } else {
                            subMap.put(currentSortKey, 1);
                        }
                    }
                    continue;
                }
            }
            if (positonMap.get(sortKey) == null) {
                currentSortKey = sortKey; // 当前sortkey
                positonMap.put(sortKey, count);
            }
        }
        if (tempList2 != null && tempList2.size() > 0) {
            // 倒序
            Collections.reverse(tempList2);
            illegalDataCount = illegalDataCount + tempList2.size();
            list.addAll(tempList2);
        }
        if (tempList != null && tempList.size() > 0) {
            // 倒序
            Collections.reverse(tempList);
            illegalDataCount = illegalDataCount + tempList.size();
            list.addAll(tempList);
        }
    }

    /**
     * 合并两个bitmap
     * @param background
     * @param foreground
     * @return
     */
    private Bitmap combineBitmap(Bitmap background, Bitmap foreground) {
        if( background == null ) {
            return null;
        }

        int bgWidth = background.getWidth()/2;
        int bgHeight = background.getHeight()/2;
        //int fgWidth = foreground.getWidth();
        //int fgHeight = foreground.getHeight();
        //create the new blank bitmap 创建一个新的和SRC长度宽度一样的位图
        Bitmap newbmp = createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newbmp);
        //draw bg into
        cv.drawBitmap(background, 0, 0, null);//在 0，0坐标开始画入bg
        //draw fg into
        cv.drawBitmap(foreground, bgWidth/2-foreground.getWidth()/2, bgHeight/2-foreground.getHeight()/2, null);//在 0，0坐标开始画入fg ，可以从任意位置画入
        //save all clip
        cv.save(Canvas.ALL_SAVE_FLAG);//保存
        //store
        cv.restore();//存储
        return newbmp;
    }

}
