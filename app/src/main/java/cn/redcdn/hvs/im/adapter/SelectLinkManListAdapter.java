package cn.redcdn.hvs.im.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.butel.connectevent.base.CommonConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.bean.ShowNameUtil.NameElement;
import cn.redcdn.hvs.im.column.NubeFriendColumn;
import cn.redcdn.hvs.im.dao.MedicalDao;
import cn.redcdn.hvs.im.dao.MedicalDaoImpl;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.view.RoundImageView;
import cn.redcdn.log.CustomLog;

/**
 * <dl>
 * <dt>SelectLinkManListAdapter.java</dt>
 * <dd>Description:选择联系人list的adapter</dd>
 * @author sunkai
 */

public class SelectLinkManListAdapter extends BaseAdapter implements SectionIndexer {

    private final String TAG = "SelectLinkManListAdapter";

    private List<ContactFriendBean> list = null;
    private List<String> listInvite = new ArrayList<String>();
    private Context mContext;
    private ChangeDataListener changeDataListener; // 处理出家变化
    private Map<Integer, Boolean> indexMap; // 保存选中的checkbox状态
    private Map<String, Integer> positonMap = new HashMap<String, Integer>();
    private Map<String, String> addFriend;// 记录已添加用户
    private MedicalDao dao;
//    private NewFriendDao newFriendDao;
    private boolean hasCatalog = false; // 是否显示按字母分类，true是显示，false是不显示
    private boolean isMultiSelected = false; // 是单选还是多选模式，true是多选，false 是单选
    private boolean isShowBtn = false; // 是显示按钮还是显示radiobutton，true是按钮，false
    // 是radiobutton
    private Map<String, String> contactNameByNumber = new HashMap<String, String>();// key:phoneNumber
    // value:name通讯录中的名字
    private boolean hasSelectedButton = true; // 联系人是否可选，true是可选，false是不可选
    private boolean isClickAble = false; // 头像是否可点击，true是可以点击，false是不可点击
    private String lastCatalog = ""; // 最新一条的首字母
    private String currentLetter = "";// 当前首字母
    private String max = String.valueOf((char) (255)); // 常用字符最大ASCII
    private String[] letters = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#" };
    private String[] letters2 = { "★", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z", "#" };// ☆
    private int lastCount = 0;
    private boolean isShowImage = true;
    private boolean isShowGroup = false;// 转发选择联系人时，是否显示群组信息

    public SelectLinkManListAdapter(Context mContext, boolean isSignal,
                                    boolean isShowImage) {
        addFriend = new HashMap<String, String>();
        dao = new MedicalDaoImpl(mContext);
//        newFriendDao = new NewFriendDao(mContext);
        this.mContext = mContext;
        indexMap = new HashMap<Integer, Boolean>();
        this.isShowImage = isShowImage;
        hasCatalog = false;
        this.isMultiSelected = isSignal;
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        // return null;
        // 极会议-需要取得联系人nube以判断设备类型-分享使用
        if (list != null && list.size() > 0) {
            return list.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup arg2) {
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.linkman_list_item, null);
            viewHolder.itemDivider = view
                    .findViewById(R.id.linkman_item_divider);
            viewHolder.tvTitle = (TextView) view
                    .findViewById(R.id.linkman_name);
            viewHolder.tvLayout = (RelativeLayout) view
                    .findViewById(R.id.catalog_layout);
            viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
            viewHolder.select = (CheckBox) view
                    .findViewById(R.id.linkman_select);
            viewHolder.rbSignal = (RadioButton) view
                    .findViewById(R.id.rb_linkman_select);
            viewHolder.acceptBtn = (Button) view.findViewById(R.id.accept_btn);
            viewHolder.describeText = (TextView) view
                    .findViewById(R.id.describe_text);
            viewHolder.linkmanPhone = (TextView) view
                    .findViewById(R.id.linkman_phone);
            viewHolder.linkManIcon = (ImageView) view.findViewById(R.id.linkman_img);
            viewHolder.deviceType = (ImageView) view
                    .findViewById(R.id.device_type);
            viewHolder.deviceTypeView = view.findViewById(R.id.lt_device_type);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        // 极会议版本：最后一行线要顶到头
        View line_bottom = view.findViewById(R.id.linkman_line_bottom);
        if (position == getCount() - 1) {
            line_bottom.setVisibility(View.VISIBLE);
        } else {
            line_bottom.setVisibility(View.GONE);
        }
        // 头像显示
        if (isShowImage) {
            int headId = IMCommonUtil.getHeadIdBySex(list.get(position).getSex());

            if (!TextUtils.isEmpty(list.get(position).getNubeNumber())
                    && list.get(position).getNubeNumber().length() > 12) {// 群组数据
                headId=R.drawable.group_icon;
            }

            Glide.with(view.getContext())
                    .load(list.get(position).getHeadUrl())
                    .placeholder(headId).error(headId).centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .crossFade().into(viewHolder.linkManIcon);

            if (isClickAble) {
                // 点击接收人头像可以跳转到详情页
                gotoContactDetail(viewHolder, position);
            } else {
                viewHolder.linkManIcon.setEnabled(false);
                viewHolder.linkManIcon.setClickable(false);
            }

        } else {
            viewHolder.linkManIcon.setVisibility(View.GONE);
        }

        // 是否可以点击选择联系人
        if (!hasSelectedButton) {
            viewHolder.select.setVisibility(View.GONE);
            viewHolder.rbSignal.setVisibility(View.GONE);
            viewHolder.acceptBtn.setVisibility(View.GONE);
            // viewHolder.select.setVisibility(View.GONE);
            // viewHolder.rbSignal.setVisibility(View.GONE);
        } else {
            if (isMultiSelected) {
                // 可以选择多个联系人
                viewHolder.select.setVisibility(View.VISIBLE);
                viewHolder.rbSignal.setVisibility(View.GONE);
                viewHolder.acceptBtn.setVisibility(View.GONE);
                viewHolder.select.setChecked(indexMap.get(position));
                // liujc 如果已是本群好友，默认不可点击
                if (indexMap.get(position)
                        && listInvite != null
                        && listInvite.contains(list.get(position)
                        .getNubeNumber())) {
                    viewHolder.select.setChecked(false);
                    viewHolder.select.setEnabled(false);
                } else {
                    viewHolder.select.setEnabled(true);
                }
            } else {
                // 只能单选联系人
                viewHolder.select.setVisibility(View.GONE);
                viewHolder.rbSignal.setVisibility(View.VISIBLE);
                viewHolder.acceptBtn.setVisibility(View.GONE);
                viewHolder.rbSignal.setChecked(indexMap.get(position));
            }
            // if (isShowBtn) {// 显示邀请联系人界面的添加和邀请按钮
            // LogUtil.d("isShowBtn=" + isShowBtn);
            // viewHolder.deviceType.setVisibility(View.GONE);
            // addFirend(viewHolder, position);
            // } else {
            // // TODO:极会议中-选择联系人、选择名片，需要显示设备类型；手机通讯录好友不显示
            // String nube = list.get(position).getNubeNumber();
            // LogUtil.d("isShowBtn=" + isShowBtn + "|nube=" + nube);
            // viewHolder.deviceType.setImageResource(ButelOvell
            // .getNubeIconId(nube));
            // viewHolder.deviceType.setVisibility(View.VISIBLE);
            // }
        }
        if (isShowBtn) {// 显示邀请联系人界面的添加和邀请按钮
            CustomLog.d(TAG,"isShowBtn=" + isShowBtn);
            viewHolder.deviceTypeView.setVisibility(View.GONE);
            addFirend(viewHolder, position);
        } else {
            // TODO:极会议中-选择联系人、选择名片，需要显示设备类型；手机通讯录好友不显示
//            String nube = list.get(position).getNubeNumber();
//            CustomLog.d("","isShowBtn=" + isShowBtn + "|nube=" + nube);
//            viewHolder.deviceType.setImageResource(ButelOvell
//                    .getNubeIconId(nube));
//            viewHolder.deviceTypeView.setVisibility(View.VISIBLE);
        }
        doCatalog(hasCatalog, viewHolder, position);

        viewHolder.tvTitle.setText(getDisplayName(list.get(position)));
        if (NubeFriendColumn.LOCAL_FIND.equals(list.get(position)
                .getInviteType())) {// 本地发现用户，显示通讯录中的名字
            String tempNameString = contactNameByNumber.get(list.get(position)
                    .getNumber());
            String contactName = dao.getContactNameByNumber(list.get(position)
                    .getNumber());
            NameElement element = ShowNameUtil.getNameElement(tempNameString,
                    contactName, "", "");
            String name = ShowNameUtil.getShowName(element);
            viewHolder.linkmanPhone.setText(R.string.mobile_linkman + name);
            viewHolder.linkmanPhone.setVisibility(View.VISIBLE);
            // TODO: 极会议中需要显示：头像、nube表中名字、本地名称描述（description）:
            CustomLog.d(TAG,"url=" + list.get(position).getHeadUrl());
            int headId = IMCommonUtil.getHeadIdBySex(list.get(position).getSex());

            if (TextUtils.isEmpty(list.get(position).getHeadUrl())) {
                viewHolder.linkManIcon.setImageResource(headId);
            } else {
                int defaultImgRes = headId;
                Glide.with(view.getContext())
                        .load(list.get(position).getHeadUrl())
                        .placeholder(defaultImgRes)
                        .error(defaultImgRes)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .crossFade()
                        .into(viewHolder.linkManIcon);

//				Glide.with(view.getContext())
//						.load(list.get(position).getHeadUrl())
//						.placeholder(headId).error(headId).centerCrop()
//						.diskCacheStrategy(DiskCacheStrategy.SOURCE)
//						.crossFade().into(viewHolder.linkManIcon);
            }

            // 为了使用方便，而在SendVcardActiviAcceptBtnClickty中给list中的ContactFriendBean设置的LocalName为nube表中的名字
            // String nubeName = list.get(position).getLocalName();
            // viewHolder.tvTitle.setText(nubeName);
            // TODO:极会议版本：要求显示：昵称-视讯号
            String nickName = list.get(position).getNickname();
            if (TextUtils.isEmpty(nickName)) {
                nickName = list.get(position).getNubeNumber();
            }
            viewHolder.tvTitle.setText(nickName);
            // 极会议
        }
        // 极会议：不显示手机号码，而显示：“手机联系人：***”
        // if (!TextUtils.isEmpty(list.get(position).getNubeNumber())
        // && !"1".equals(list.get(position).getInviteType())) {
        // viewHolder.linkmanPhone.setText(list.get(position).getNubeNumber());
        // } else {
        // viewHolder.linkmanPhone.setText(list.get(position).getNumber());
        // }
        return view;
    }

    private void doCatalog(boolean hasCatalog, final ViewHolder viewHolder,
                           int position) {
        if (hasCatalog == false) {
            return;
        }
        currentLetter = getFirstSimplePinYinByPos(position);

        if (TextUtils.isEmpty(list.get(position).getNubeNumber())) {// 邀请
            CustomLog.d(TAG,"显示邀请");
            setShowLetter(viewHolder, position);
            setInvite(viewHolder);
            return;
        }

        if (list.get(position).getNubeNumber().length() > 12) {// 群组数据，纳贝号字段存放的群组的gid
            currentLetter = mContext.getString(R.string.group);
            setShowLetter(viewHolder, position);
            viewHolder.describeText.setVisibility(View.GONE);
            viewHolder.acceptBtn.setVisibility(View.GONE);
            return;
        }

        if (NubeFriendColumn.LOCAL_FIND.equals(list.get(position)
                .getInviteType())) {
            CustomLog.d(TAG,"显示本地");
            // currentLetter = "本地";
            // lastCatalog = "本地";
            setShowLetter(viewHolder, position);
            if (TextUtils.isEmpty(addFriend.get(list.get(position)
                    .getNubeNumber()))) {
                viewHolder.acceptBtn
                        .setBackgroundResource(R.drawable.common_btn_seletor);
                viewHolder.acceptBtn.setTextColor(mContext.getResources()
                        .getColorStateList(R.color.big_btn_text_color));
                // viewHolder.acceptBtn.setText(R.string.add_friend_message);
                viewHolder.describeText.setVisibility(View.GONE);
            } else {
                // viewHolder.acceptBtn.setText(AndroidUtil.getString(R.string.newfriend_has_passed_status));
                viewHolder.acceptBtn.setVisibility(View.GONE);
                viewHolder.describeText.setVisibility(View.VISIBLE);
            }
            return;
        }
        setShowLetter(viewHolder, position);
    }

    private void setInvite(final ViewHolder viewHolder) {
        viewHolder.describeText.setVisibility(View.GONE);
        viewHolder.acceptBtn.setBackgroundResource(0);
        viewHolder.acceptBtn.setTextColor(0xf2497c);
        viewHolder.acceptBtn.setText(R.string.invite_contact);
    }

    private void setShowLetter(final ViewHolder viewHolder, int pos) {

        if (pos == 0) {
            viewHolder.tvLayout.setVisibility(View.VISIBLE);
            viewHolder.tvLetter.setVisibility(View.VISIBLE);
            viewHolder.tvLetter.setText(currentLetter);
            viewHolder.itemDivider.setVisibility(View.GONE);
            lastCatalog = currentLetter;
        } else {
            if (currentLetter.equals(lastCatalog)
                    && currentLetter.equals(getFirstSimplePinYinByPos(pos - 1))) {
                viewHolder.tvLayout.setVisibility(View.GONE);
                viewHolder.tvLetter.setVisibility(View.GONE);
                viewHolder.itemDivider.setVisibility(View.VISIBLE);
            } else {
                if (!currentLetter.equals(getFirstSimplePinYinByPos(pos - 1))) {
                    viewHolder.tvLayout.setVisibility(View.VISIBLE);
                    viewHolder.tvLetter.setVisibility(View.VISIBLE);
                    viewHolder.tvLetter.setText(currentLetter);
                    viewHolder.itemDivider.setVisibility(View.GONE);
                    lastCatalog = currentLetter;
                } else {
                    viewHolder.tvLayout.setVisibility(View.GONE);
                    viewHolder.tvLetter.setVisibility(View.GONE);
                    viewHolder.itemDivider.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    /**
     * @Title: getLocalName
     * @Description:根据phoneNumber获取手机通讯录中的备注名
     * @date: 2015-03-13 上午11：35
     * @author liujc
     */
    public String getLocalName(String phoneNumber) {
        // 查找系统联系人
        List<ContactFriendBean> localcontactlist = dao.getLocationLinkmanData();
        String localName = "";
        for (int local = 0; local < localcontactlist.size(); local++) {
            ContactFriendBean localContactFriendBean = localcontactlist.get(local);
            String temp = IMCommonUtil.simpleFormatMoPhone(localContactFriendBean
                    .getNumber());
            if (temp.equalsIgnoreCase(phoneNumber)) {
                localName = localContactFriendBean.getName();
                localName = IMCommonUtil.fliteIllegalChar(localName);
            }
        }
        return localName;
    }

    /**
     * @Title: gotoContactDetail
     * @Description: 点击联系人头像，跳转到联系人的详情页面
     */
    private void gotoContactDetail(ViewHolder viewHolder, final int position) {
        viewHolder.linkManIcon.setEnabled(true);
        viewHolder.linkManIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomLog.d(TAG,"点击联系人头像，跳转到联系人的详情页面");
//				Intent intent = new Intent(mContext,
//						ButelContactDetailActivity.class);
//				intent.putExtra(ButelContactDetailActivity.KEY_NUBE_NUMBER,
//						list.get(position).getNubeNumber());
//				intent.putExtra(ButelContactDetailActivity.KEY_FRIEND_INFO,
//						list.get(position));
//				mContext.startActivity(intent);

                if(changeDataListener!=null){
                    changeDataListener.onClickHeadIcon(list.get(position), position);
                }
            }
        });
    }

    /**
     * @Title: setSection
     * @Description: 初始化快速定位条显示数据
     * @param posMap
     *            快速定位点的最初位置
     * @param illegalDataCount
     *            被移动至开始位置的非法数据的位置
     * @param subMap
     *            实际位置与初始位置的变动值
     * @return: void
     */
    public void setSection(Map<String, Integer> posMap, int illegalDataCount,
                           Map<String, Integer> subMap) {
        this.positonMap = posMap;
        int count = 0;
        lastCount = 0;
        int size = letters.length;
        if (isShowGroup) {
            for (int index = 0; index < size; index++) {
                if (subMap.get(letters[index]) != null
                        && subMap.get(letters[index]) > 0) {
                    // 累加的原因：是subMap记录的个数只是快速定位点开始到其前面
                    count = count + subMap.get(letters[index]);
                }
                if (positonMap.get(letters[index]) != null) {
                    // 加1原因：初始定位的位置为每个快速定位点的最后一条，+1即为其后面一条的第一个位置
                    int currentCount = positonMap.get(letters[index]) + 1;
                    positonMap.put(letters[index], lastCount);
                    lastCount = currentCount;
                }
            }
        } else {
            for (int index = 0; index < size; index++) {
                if (subMap.get(letters[index]) != null
                        && subMap.get(letters[index]) > 0) {
                    // 累加的原因：是subMap记录的个数只是快速定位点开始到其前面
                    count = count + subMap.get(letters[index]);
                }
                if (positonMap.get(letters[index]) != null) {
                    // 加1原因：初始定位的位置为每个快速定位点的最后一条，+1即为其后面一条的第一个位置
                    int currentCount = positonMap.get(letters[index]) - count
                            + 1;
                    if (!"xx".equals(letters[index])
                            && subMap.get("xx") != null && index == 0) {
                        lastCount = lastCount + subMap.get("xx");
                    }
                    positonMap.put(letters[index], lastCount);
                    CustomLog.d("TAG", "letters[index], lastCount" + letters[index]
                            + "-" + lastCount);
                    lastCount = currentCount;
                }
            }
        }

        if (illegalDataCount > 0) {
            positonMap.put("#", list.size() - illegalDataCount);
            CustomLog.d("TAG", "list.size() - illegalDataCount=" + list.size() + "-"
                    + illegalDataCount);
        }
    }

    /**
     * @param friend
     * @return 联系人姓名
     */
    private String getDisplayName(ContactFriendBean friend) {
        String name = "";
        if (friend != null) {
            ShowNameUtil.NameElement element = ShowNameUtil.getNameElement(friend.getName(),
                    friend.getNickname(), friend.getNumber(),
                    friend.getNubeNumber());
            name = ShowNameUtil.getShowName(element);
            // name = friend.getName();
            // if (TextUtils.isEmpty(name)) {
            // name = friend.getNickname();
            // if (TextUtils.isEmpty(name)) {
            // //add by zzwang 根据弱化视讯号需求，添加手机号显示逻辑
            // name = friend.getNumber();
            // if(TextUtils.isEmpty(name)){
            // name = friend.getNubeNumber();
            // }
            // }
            // }
        }
        return name;
    }

    /**
     * @Title: getFirstSimplePinYinByPos
     * @Description: 根据位置获取该条联系人名称拼音首字符，除a-z、A-Z外其他均返回为"#"
     * @param position
     * @return: String
     */
    private String getFirstSimplePinYinByPos(int position) {
        String sortKey = list.get(position).getPym();
        if (TextUtils.isEmpty(sortKey)) {
            return "#";
        } else {
            sortKey = sortKey.substring(0, 1);
            sortKey = sortKey.toUpperCase();
            if (sortKey.compareTo(max) > 0) {
                // 比最大常用ASCII大的异常数据
                return "#";
            } else if (!IMConstant.letter.contains(sortKey)) {
                // 不包含在定位条内的非法数据
                return "#";
            }
        }
        return sortKey;
    }

    @Override
    public Object[] getSections() {
        return letters;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 1;
    }

    @Override
    public int getPositionForSection(int section) {
        if (positonMap == null || positonMap.size() == 0) {
            return -1;
        }
        Integer pos = positonMap.get(letters[section]);
        if (pos != null) {
            return pos;
        } else {
            return -1;
        }
    }

    /**
     * @author: sunkai
     * @Title: selectAll
     * @Description: 全选
     * @date: 2013-8-9 下午2:55:30
     */
    public void selectAll() {
        if (list != null && list.size() >= 0) {
            for (int i = 0; i < list.size(); i++) {
                indexMap.put(i, true);
                changeDataListener.selectItem(list.get(i), i);
            }
        }
    }

    /**
     * @author: sunkai
     * @Title: disSelectAll
     * @Description: 全不选
     * @date: 2013-8-9 下午2:55:44
     */
    public void disSelectAll() {
        if (list != null && list.size() >= 0) {
            for (int i = 0; i < list.size(); i++) {
                indexMap.put(i, false);
            }
            changeDataListener.disSelectAll();
        }
    }

    /**
     * @author: sunkai
     * @Title: setIndexMap
     * @Description: 修改checkbox的点击状态，需外部调用 notifyDataSetChanged()
     * @param position
     * @param isChecked
     * @date: 2013-8-9 下午2:57:43
     */
    public void select(int position, boolean isChecked) {
        indexMap.put(position, isChecked);
        if (isChecked) {
            changeDataListener.selectItem(list.get(position), position);
        } else {
            changeDataListener.disSelectItem(list.get(position), position);
        }
    }

    /**
     *
     * Description:根据position获取联系人ContactId
     *
     * @param position
     * @return
     */
    public String getSelectContact(int position) {
        return list.get(position).getContactId();
    }

    /**
     *
     * Description:根据position取消联系人选中状态
     *
     * @param position
     */
    public void disselect(int position) {
        indexMap.put(position, false);
        changeDataListener.disSelectItem(list.get(position), position);

    }

    private SelectLinkManCallBack callBack;

    public interface SelectLinkManCallBack {

        /**
         * @Title: AcceptCallBack
         * @Description: 通过该条好友邀请/添加按钮点击回调
         * @param添加、邀请按钮
         * @param phoneNumbe
         *            手机号码
         * @param nubeNumber
         *            视讯号
         * @param newFriendId
         *            新朋友ID
         * @param ContactFriendBean
         * @return: inviteType 1：本地发现 2：手机通讯录
         */
        public void AcceptBtnClick(String phoneNumbe, String nubeNumber,
                                   String newFriendId, ContactFriendBean ContactFriendBean,
                                   String inviteType);
    }

    public void setSelectLinkManCallBack(SelectLinkManCallBack callBack) {
        this.callBack = callBack;
    }

    /**
     * <dl>
     * <dt>SelectLinkManListAdapter.java</dt>
     * <dd>Description:联系人列表的viewHolder</dd>
     * <dd>Copyright: Copyright (C) 2011</dd>
     * <dd>Company: 安徽青牛信息技术有限公司</dd>
     * <dd>CreateDate: 2013-8-9 下午2:56:36</dd>
     * </dl>
     *
     * @author sunkai
     */
    public class ViewHolder {
        public View itemDivider;
        public TextView tvTitle;
        public TextView tvLetter;
        public RelativeLayout tvLayout;
        public CheckBox select;
        public TextView linkmanPhone;
        public RadioButton rbSignal;
        public Button acceptBtn;
        public TextView describeText;
        public ImageView linkManIcon;

        public ImageView deviceType;
        public View deviceTypeView;
    }

    /**
     * @author: sunkai
     * @Title: isHasCatalog
     * @Description: 是否显示分类信息
     * @return
     * @date: 2013-8-9 下午4:38:42
     */
    public boolean isHasCatalog() {
        return hasCatalog;
    }

    /**
     * @author: wangyf
     * @Title: setHasSelectedButton
     * @Description: 是否显示选择按钮
     * @return
     */
    public void setHasSelectedButton(boolean hasSelectedButton) {
        this.hasSelectedButton = hasSelectedButton;
    }

    /**
     * @author: wangyf
     * @Title: setIsClickAble
     * @Description: 是否可以点击联系人
     * @return
     */
    public void setIsClickAble(boolean isClickAble) {
        this.isClickAble = isClickAble;
    }

    /**
     * @author: wangyf
     * @Title: setList
     * @Description: 接收数据
     * @return
     */
    public void setList(List<ContactFriendBean> list) {
        this.list = list;
    }

    /**
     * @author: sunkai
     * @Title: setHasCatalog
     * @Description: 设置显示分类信息
     * @param hasCatalog
     * @date: 2013-8-9 下午4:39:15
     */
    public void setHasCatalog(boolean hasCatalog) {
        this.hasCatalog = hasCatalog;
    }

    /**
     * @author: sunkai
     * @Title: setList
     * @Description: 设置数据源
     * @param list
     * @date: 2013-8-14 下午5:56:45
     */
    public void setList(List<ContactFriendBean> list, int j) {
        this.list = list;
        if (null == list) {
            return;
        }
        if (j >= 0) {
            indexMap.put(j, true);
        }
        for (int i = 0; i < list.size(); i++) {
            if (i != j) {
                indexMap.put(i, false);
            }
        }
    }

    public void setList(List<ContactFriendBean> list,
                        ArrayList<String> selectedNumberList) {
        this.list = list;
        if (null == list) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            if (selectedNumberList != null
                    && selectedNumberList.contains(list.get(i).getNubeNumber())) {
                select(i, true);
            } else {
                select(i, false);
            }
        }
    }

    // liujc 设置邀请好友入群时 排重数据源处理
    public void setInviteList(List<ContactFriendBean> list,
                              ArrayList<String> selectedNumberList) {
        this.listInvite = selectedNumberList;
        this.list = list;
        if (null == list) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            if (selectedNumberList != null
                    && selectedNumberList.contains(list.get(i).getNubeNumber())) {
                select(i, true);
            } else {
                select(i, false);
            }
        }
    }

    /**
     * @author: liujc
     * @Title: setContactNameByNumber
     * @Description: 设置数据源,用于显示本地发现用户的姓名为通讯录中的名字
     * @param map
     * @date: 2015-03-16 上午 10：58
     */
    public void setContactNameByNumber(Map<String, String> map) {
        this.contactNameByNumber = map;
    }

    public boolean isMultiSelected() {
        return isMultiSelected;
    }

    public void setMultiSelected(boolean isMultiSelected) {
        this.isMultiSelected = isMultiSelected;
    }

    public boolean isShowBtn() {
        return isShowBtn;
    }

    public void setisShowBtn(boolean isShowBtn) {
        this.isShowBtn = isShowBtn;
    }

    // 在快速检索条上显示群组的检索字母
    public void setLetters() {
        letters = letters2;
        isShowGroup = true;
    }

    public ChangeDataListener getChangeDataListener() {
        return changeDataListener;
    }

    public void setChangeDataListener(ChangeDataListener changeDataListener) {
        this.changeDataListener = changeDataListener;
    }

    public interface ChangeDataListener {

        /**
         * @author: sunkai
         * @Title: disSelectItem
         * @Description: 不选择这一条数据。如果列表数据已经被选中，则移除。
         * @param friend
         * @date: 2013-8-16 上午11:03:18
         */
        public void disSelectItem(ContactFriendBean friend, int position);

        /**
         * @author: sunkai
         * @Title: selectItem
         * @Description: 选择一条数据，讲数据加入到map
         * @param friend
         * @date: 2013-8-16 上午11:04:19
         */
        public void selectItem(ContactFriendBean friend, int position);

        /**
         * @author: sunkai
         * @Title: disSelectAll
         * @Description: 全不选列表数据
         * @date: 2013-8-16 上午11:05:42
         */
        public void disSelectAll();
        /**
         * 点击Item项中图像部分的响应事件回调
         * @param friend
         * @param position
         */
        public void onClickHeadIcon(ContactFriendBean friend, int position);
    }

    private void addFirend(final ViewHolder viewHolder, final int position) {

        viewHolder.select.setVisibility(View.GONE);
        viewHolder.rbSignal.setVisibility(View.GONE);
        viewHolder.acceptBtn.setVisibility(View.VISIBLE);
        viewHolder.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomLog.d("SelectLinkManListAdapter","点击了新朋友界面的接受按钮");
                if (callBack != null) {
                    if (NubeFriendColumn.LOCAL_FIND.equals(list.get(position)
                            .getInviteType())) {
                        addFriend.put(list.get(position).getNubeNumber(), list
                                .get(position).getNubeNumber());
                        viewHolder.acceptBtn.setVisibility(View.GONE);
                        viewHolder.describeText.setVisibility(View.VISIBLE);
                    }
                    callBack.AcceptBtnClick(list.get(position).getNumber(),
                            list.get(position).getNubeNumber(),
                            list.get(position).getSourcesId(), list
                                    .get(position), list.get(position)
                                    .getInviteType());
                }
            }
        });

    }
}
