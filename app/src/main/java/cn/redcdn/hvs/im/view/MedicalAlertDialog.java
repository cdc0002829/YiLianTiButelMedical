package cn.redcdn.hvs.im.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.view.BottomMenuWindow.MenuClickedListener;

/**
 * Desc 聊天界面
 * Created by wangkai on 2017/3/1.
 */

public class MedicalAlertDialog {
    public Context context;
    public Dialog ad;
    public TextView titleView;
    public TextView messageView;
    public Button firstBtn;
    public Button secondBtn;
    public Button thirdBtn;
    public Button fourthBtn;
    public Button fiveBtn;
    public Button sixBtn;
    public RelativeLayout firstBtn_Layout;
    public RelativeLayout secondBtn_Layout;
    public RelativeLayout thirdBtn_Layout;
    public RelativeLayout fourthBtn_Layout;
    public RelativeLayout fiveBtn_Layout;
    public RelativeLayout SixBtn_Layout;
    public View viewFisrt;
    public View viewSecond;
    public View viewThird;
    public View viewFourth;
    public View viewFive;
    public View viewSix;
    public Window window;
    public View contentView;
    private List<RelativeLayout> dispButtons = new ArrayList<RelativeLayout>();
    private List<View> dispViews = new ArrayList<View>();
    private int showFlag = 1;
    private int viewPosition = Gravity.BOTTOM;

    public MedicalAlertDialog(Context context) {
        this.context = context;
        initView();
        initWidget();
    }

    public MedicalAlertDialog(Context context,int position){
        this.context = context;
        this.viewPosition = position;
        initView();
        initWidget();
    }

    private void initWidget(){
        firstBtn_Layout = (RelativeLayout) contentView.findViewById(R.id.btn_first_layout);
        secondBtn_Layout = (RelativeLayout) contentView.findViewById(R.id.btn_second_layout);
        thirdBtn_Layout = (RelativeLayout) contentView.findViewById(R.id.btn_third_layout);
        fourthBtn_Layout = (RelativeLayout) contentView.findViewById(R.id.btn_fourth_layout);
        fiveBtn_Layout = (RelativeLayout) contentView.findViewById(R.id.btn_five_layout);
        SixBtn_Layout = (RelativeLayout) contentView.findViewById(R.id.btn_six_layout);
        firstBtn = (Button) contentView.findViewById(R.id.btn_first);
        secondBtn = (Button) contentView.findViewById(R.id.btn_second);
        thirdBtn = (Button) contentView.findViewById(R.id.btn_third);
        fourthBtn = (Button) contentView.findViewById(R.id.btn_fourth);
        fiveBtn = (Button) contentView.findViewById(R.id.btn_five);
        sixBtn = (Button) contentView.findViewById(R.id.btn_six);
        viewFisrt = (View) contentView.findViewById(R.id.line1);
        viewSecond = (View) contentView.findViewById(R.id.line2);
        viewThird = (View) contentView.findViewById(R.id.line3);
        viewFourth = (View) contentView.findViewById(R.id.line4);
        viewFive = (View) contentView.findViewById(R.id.line5);
        viewSix = (View) contentView.findViewById(R.id.line6);
    }

    public void initView(){
        if(viewPosition == Gravity.BOTTOM){
            ad =  new Dialog(context, R.style.ActionSheetDialogStyle);
        }else {
            ad = new android.app.AlertDialog.Builder(context).create();
        }

        ad.show();
        if (null != ad) {
            contentView = ((Activity) context).getLayoutInflater().inflate(
                    R.layout.medicalalertialog_layout, null);
            ad.setContentView(contentView, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            Window window = ad.getWindow();
            window.setGravity(viewPosition);
            ad.setCanceledOnTouchOutside(true);
            // 设置窗口大小和位置
            WindowManager windowManager = ((Activity)context).getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width =display.getWidth(); // 设置宽度
            ad.onWindowAttributesChanged(lp);
        }
    }

    public void setTitle(int resId) {
        titleView.setText(resId);
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    public void setMessage(int resId) {
        messageView.setText(resId);
    }

    public void setMessage(String message) {
        messageView.setText(message);
    }

    public void addButtonFirst(final MenuClickedListener listener,
                               String textContent){
        addButtonFirst(listener,textContent, 0);
    }

    public void addButtonFirst(final MenuClickedListener listener,
                               String textContent, int leftIcon) {
        firstBtn.setText(textContent);
        firstBtn.setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0,0,0);
        firstBtn_Layout.setVisibility(View.VISIBLE);
        dispButtons.add(firstBtn_Layout);
        dispViews.add(viewFisrt);
        firstBtn_Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onMenuClicked();
                }
            }
        });
    }

    public void addButtonSecond(final MenuClickedListener listener,
                                String textContent) {
        addButtonSecond(listener,textContent, 0);
    }

    public void addButtonSecond(final MenuClickedListener listener,
                                String textContent, int leftIcon) {
        // 普通按钮2
        secondBtn.setText(textContent);
        secondBtn.setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0,0,0);
        secondBtn_Layout.setVisibility(View.VISIBLE);
        dispButtons.add(secondBtn_Layout);
        dispViews.add(viewSecond);
        secondBtn_Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onMenuClicked();
                }
            }
        });
    }

    public void addButtonThird(final MenuClickedListener listener,
                               String textContent) {
        addButtonThird(listener,textContent, 0);
    }

    public void addButtonThird(final MenuClickedListener listener,
                               String textContent, int leftIcon) {
        // 普通按钮3
        thirdBtn.setText(textContent);
        thirdBtn.setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0,0,0);
        thirdBtn_Layout.setVisibility(View.VISIBLE);
        dispButtons.add(thirdBtn_Layout);
        dispViews.add(viewThird);
        thirdBtn_Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onMenuClicked();
                }
            }
        });
    }

    public void addButtonForth(final MenuClickedListener listener,
                               String textContent) {
        addButtonForth(listener,textContent, 0);
    }

    public void addButtonForth(final MenuClickedListener listener,
                               String textContent, int leftIcon) {
        // 普通按钮4
        fourthBtn.setText(textContent);
        fourthBtn.setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0,0,0);
        fourthBtn_Layout.setVisibility(View.VISIBLE);
        dispButtons.add(fourthBtn_Layout);
        dispViews.add(viewFourth);
        fourthBtn_Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onMenuClicked();
                }
            }
        });
    }

    public void addButtonFive(final MenuClickedListener listener,
                              String textContent) {
        addButtonFive(listener,textContent, 0);
    }

    public void addButtonFive(final MenuClickedListener listener,
                              String textContent, int leftIcon) {
        // 普通按钮5
        fiveBtn.setText(textContent);
        fiveBtn.setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0,0,0);
        fiveBtn_Layout.setVisibility(View.VISIBLE);
        dispButtons.add(fiveBtn_Layout);
        dispViews.add(viewFive);
        fiveBtn_Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onMenuClicked();
                }
            }
        });
    }

    public void show() {
        if (dispButtons.size() > 0) {
            for (int i = 0; i < dispButtons.size(); i++) {
                RelativeLayout dispButton = dispButtons.get(i);
                View dispView = dispViews.get(i);
                if (i == 0) {
                    if (dispButtons.size() == 1) {
                        // 只有一个按钮，显示上下圆角按钮
                        if(showFlag == 1){
                            dispButton
                                    .setBackgroundResource(R.drawable.bg_first_item_shap);
                        }else{
                            dispButton
                                    .setBackgroundResource(R.drawable.meet_dele_btn_seletor);
                            viewFisrt.setVisibility(View.GONE);
                            viewSecond.setVisibility(View.GONE);
                            viewThird.setVisibility(View.GONE);
                            viewFourth.setVisibility(View.GONE);
                            viewFive.setVisibility(View.GONE);
                            viewSix.setVisibility(View.GONE);
                        }
                    } else {
                        // 多于一个按钮，显示上圆角按钮
                        dispButton
                                .setBackgroundResource(R.drawable.bg_item_shape_top);
                        dispView.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (i < dispButtons.size() - 1) {
                        // 中间按钮
                        dispButton
                                .setBackgroundResource(R.drawable.bg_item_shape);
                        dispView.setVisibility(View.VISIBLE);
                    } else {
                        // 下圆角按钮
                        dispButton
                                .setBackgroundResource(R.drawable.bg_item_shape_bottom);
                    }
                }
            }
        }
    }

    public void setSelected(int res) {
        ((RelativeLayout) ((Activity) context).getLayoutInflater()
                .inflate(R.layout.medicalalertialog_layout, null)
                .findViewById(res)).setPressed(true);
    }

    public void setShowFlag() {
        showFlag = 0;
        secondBtn.setTextColor(Color.parseColor("#ffffff"));
    }

    public void dismiss() {
        ad.dismiss();
    }


    public void addButtonSix(final MenuClickedListener listener,
                              String textContent) {
        addButtonSix(listener,textContent, 0);
    }

    public void addButtonSix(final MenuClickedListener listener,
                              String textContent, int leftIcon) {
        // 普通按钮6
        sixBtn.setText(textContent);
        sixBtn.setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0,0,0);
        SixBtn_Layout.setVisibility(View.VISIBLE);
        dispButtons.add(SixBtn_Layout);
        dispViews.add(viewSecond);
        SixBtn_Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onMenuClicked();
                }
            }
        });
    }

    public void addButtonSingle(final MenuClickedListener listener,
                               String textContent){
        addButtonSingle(listener,textContent, 0);
    }

    public void addButtonSingle(final MenuClickedListener listener,
                             String textContent, int leftIcon) {
        // 普通按钮 1
        firstBtn.setText(textContent);
        firstBtn.setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0,0,0);
        firstBtn_Layout.setVisibility(View.VISIBLE);
        dispButtons.add(firstBtn_Layout);
        firstBtn_Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onMenuClicked();
                }
            }
        });
    }
}
