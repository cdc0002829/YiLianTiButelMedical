package cn.redcdn.hvs.util;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.redcdn.hvs.R;

/**
 * 公用 TitleBar
 * Created by guoyx on 2017/2/22.
 */

public class TitleBar {

    private Activity mActivity = null;

    // 返回按钮
    private Button backBtn = null;
    private LinearLayout leftView = null;
    // 自定义按钮
    private LinearLayout rightView = null;
    private Button rightBtn = null;
    // 标题
    private TextView titleTxt = null;
    // 副标题
    private TextView subTitleTxt = null;
    // 标题
    private RelativeLayout titleLine = null;

    private View titleView = null;

    //右边标记位
    private TextView subRightBtn=null;
    private TextView backTxt;

    private Button bigBackBt;
    private ImageView imgSub;
    private ImageView imgVoiceSub;


    public TitleBar(Activity activity, View parent) {
        mActivity = activity;
        initWidget(parent);
    }

    public void setTitle(String title) {

        titleTxt.setText(title);
    }

    public void setTitleDrawable(int left, int top, int right, int bottom, int drawablePadding) {
        titleTxt.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        if (drawablePadding != 0) {
            titleTxt.setCompoundDrawablePadding(drawablePadding);
        }
    }

    public void setTitle(int titleRes) {
        titleTxt.setText(titleRes);
    }

    public void setTitleOnClickListener(View.OnClickListener listener) {
        titleTxt.setOnClickListener(listener);
    }

    public void setSubTitle(String subTitle) {
        if (!TextUtils.isEmpty(subTitle)) {
            subTitleTxt.setVisibility(View.VISIBLE);
            subTitleTxt.setText(subTitle);
        } else {
            subTitleTxt.setVisibility(View.GONE);
        }
    }

    public void setBackground(int index){
        if(titleView != null) {
            titleView.setBackgroundColor(index);
        }
    }

    public void enableBack() {
        backBtn.setVisibility(View.VISIBLE);
        bigBackBt.setVisibility(View.VISIBLE);
        // 默认返回事件，关闭当前activity
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                mActivity.finish();
            }
        });
        bigBackBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                mActivity.finish();
            }
        });
    }

    public Button getBackBtn(){
        return backBtn;
    }

    /**
     * @author: zhaguitao
     * @Title: setBack
     * @Description: setBack返回键
     * @param backStr
     * @param clickListener
     * @date: 2013-12-17 下午4:53:11
     */
    public void setBack(String backStr, View.OnClickListener clickListener) {
        backBtn.setVisibility(View.VISIBLE);
        bigBackBt.setVisibility(View.VISIBLE);
        if (backStr != null) {
            backBtn.setText(backStr);
        }
        if (clickListener != null) {
            // 用户自定义返回事件
            backBtn.setOnClickListener(clickListener);
            bigBackBt.setOnClickListener(clickListener);
        } else {
            // 默认返回事件，关闭当前activity
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CommonUtil.isFastDoubleClick()) {
                        return;
                    }
                    mActivity.finish();
                }
            });

            bigBackBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CommonUtil.isFastDoubleClick()) {
                        return;
                    }
                    mActivity.finish();
                }
            });
        }
    }

    /**
     * @author: zhaguitao
     * @Title: enableRightBtn
     * @Description: enable右侧按钮
     * @param btnStr
     * @param icon
     * @param clickListener
     * @date: 2013-12-17 下午4:55:29
     */
    public void enableRightBtn(String btnStr, int icon,
                               View.OnClickListener clickListener) {
        rightBtn.setVisibility(View.VISIBLE);
        if (btnStr != null) {
            rightBtn.setTextColor(Color.GRAY);
            rightBtn.setText(btnStr);
        }
        if (icon > 0) {
            rightBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0);
        }
        if (clickListener != null) {
            // 用户自定义事件
            rightBtn.setOnClickListener(clickListener);
        }
    }

    public void setRightBtnClicked(boolean isClicked){
        rightBtn.setEnabled(isClicked);
    }

    /***
     * 不建议使用
     * @param isClicked
     * @param btnStr
     * @param color
     * @param clickListener
     */
    public void setTopRightBtn(boolean isClicked,String btnStr,int color,View.OnClickListener clickListener){
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setTextColor(color);
        rightBtn.setText(btnStr);
        if(!isClicked){
            rightBtn.setEnabled(false);
        }else{
            rightBtn.setEnabled(true);
            rightBtn.setOnClickListener(clickListener);
        }
    }

    public void setTopRightBtnText(String text){
        rightBtn.setText(text);
    }
    public void setRightBtnVisibility(int visibility) {
        rightBtn.setVisibility(visibility);
    }

    public void addCustomTitleView(View view) {
        if (titleLine != null) {
            titleLine.addView(view);
        }
    }

    public void addCustomRightView(View view) {
        if (rightView != null) {
            rightView.addView(view);
        }
    }

    public void addCustomLeftView(View view) {
        if (leftView != null) {
            leftView.removeAllViews();
            leftView.setVisibility(View.VISIBLE);
            leftView.addView(view);
        }
    }
    public void removeCustomLeftView() {
        if (leftView != null) {
            leftView.setVisibility(View.INVISIBLE);
        }
    }

    public boolean isShowing() {
        if (titleView.getVisibility() == View.VISIBLE) {
            return true;
        } else {
            return false;
        }
    }

    public void hide() {
        Animation mHiddenAction = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f);
        mHiddenAction.setDuration(200);
        titleView.startAnimation(mHiddenAction);
        titleView.setVisibility(View.GONE);
    }

    public void show() {
        Animation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(200);
        titleView.startAnimation(mShowAction);
        titleView.setVisibility(View.VISIBLE);
    }

    public void setTitleVisibility(int visibility) {
        titleView.setVisibility(visibility);
    }

    /**
     * @author: zhaguitao
     * @Title: initWidget
     * @Description: 初始化控件
     * @param parent
     * @date: 2013-12-17 下午4:33:31
     */
    private void initWidget(View parent) {
        backBtn = (Button) parent.findViewById(R.id.back_btn);
        bigBackBt = (Button)parent.findViewById(R.id.back_btn_big);
        leftView = (LinearLayout) parent.findViewById(R.id.title_left);
        rightView = (LinearLayout) parent.findViewById(R.id.title_right);
        rightBtn = (Button) parent.findViewById(R.id.right_btn);
        titleTxt = (TextView) parent.findViewById(R.id.title_txt);
        subTitleTxt = (TextView) parent.findViewById(R.id.sub_title_txt);
        titleLine = (RelativeLayout) parent.findViewById(R.id.title_center);
        titleView = parent.findViewById(R.id.title);
        subRightBtn=(TextView)parent.findViewById(R.id.new_family_contact);
        backTxt = (TextView)parent.findViewById(R.id.back_str);
        imgSub = (ImageView) parent.findViewById(R.id.not_disturb);
//        imgGroupSub = (ImageView) parent.findViewById(R.id.not_disturb_sub);

        imgVoiceSub = (ImageView) parent.findViewById(R.id.not_disturb_headset);
//        imgVoiceGroupSub = (ImageView) parent.findViewById(R.id.not_disturb_headset_sub);

    }

    public View getTitleView() {
        return titleView;
    }
    public TextView getSubRightBtn(){
        return subRightBtn;
    }

    /**
     * 设置公众号标题字体颜色和返回按钮背景设置,其他模块不要使用此方法
     */
    public void setTitleTextColor(int color) {
        titleTxt.setTextColor(color);
//        final Drawable drawable = mActivity.getResources().getDrawable(
//            R.drawable.butel_video_view_btn_left_selector);
//        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
//            drawable.getMinimumHeight());
//        backBtn.setCompoundDrawables(drawable, null, null, null);
    }


    /**
     * 因为添加了自定义 loading view,所以需要调整 titlebar 文字位置
     */
    public void adjustTextOnMessageFragment(){
        titleTxt.setPadding(0,0,30,0);

    }

    public void setBackText(String backStr){
        if (!backStr.isEmpty()){
            backTxt.setVisibility(View.VISIBLE);
            backTxt.setText(backStr);

            backTxt.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (CommonUtil.isFastDoubleClick()){
                        return;
                    }
                    mActivity.finish();
                }
            });

        }

    }


    /**
     * 单聊免打扰
     * @param flag
     */
    public void showSlientViewSingle(Boolean flag){
        if (flag == true){
            imgSub.setVisibility(View.VISIBLE);
        }else{
            imgSub.setVisibility(View.GONE);
        }
    }


    /**
     * 群聊免打扰
     * @param flag
     */
    public void showSlientViewGroup(Boolean flag){
        if (flag == true){
            imgSub.setVisibility(View.VISIBLE);
        }else{
            imgSub.setVisibility(View.GONE);
        }
    }


    public void showHeadsetView(Boolean flag){
        if (flag == true){
            imgVoiceSub.setVisibility(View.VISIBLE);
        }else{
            imgVoiceSub.setVisibility(View.GONE);
        }
    }

}
