package cn.redcdn.hvs.util;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.hvs.R;

/**
 * titlebar 上的弹出下拉菜单
 */

public class PopDialogActivity extends Activity implements View.OnClickListener {
    //定义6个按钮区域
    private LinearLayout layout_item_1;
    private LinearLayout layout_item_2;
    private LinearLayout layout_item_3;
    private LinearLayout layout_item_4;
    private LinearLayout layout_item_5;
    private LinearLayout layout_item_6;

    private TextView txtView_1;
    private TextView txtView_2;
    private TextView txtView_3;
    private TextView txtView_4;
    private TextView txtView_5;
    private TextView txtView_6;

    private ImageView imgView_1;
    private ImageView imgView_2;
    private ImageView imgView_3;
    private ImageView imgView_4;
    private ImageView imgView_5;
    private ImageView imgView_6;


    public static int MAX_ITEM_COUNT = 6;
    public static List<MenuInfo> itemList = new ArrayList<MenuInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pop_dialog);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color));
        }

        if(itemList==null||itemList.size()==0){
            finish();
            return ;
        }

        initView();
        initData();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        itemList.clear();
    }

    /**
     * 本页面的菜单项最多支持6个，多余的不显示
     * @param infos
     */
    public static void setMenuInfo(List<MenuInfo> infos){
        if(infos!=null){
            itemList.clear();
            for(MenuInfo item:infos){
                itemList.add(item);
            }
        }
    }


    /**
     * 初始化组件
     */
    private void initView(){
        //得到布局组件对象并设置监听事件
        layout_item_1 = (LinearLayout)findViewById(R.id.id_item_layout_1);
        layout_item_2 = (LinearLayout)findViewById(R.id.id_item_layout_2);
        layout_item_3 = (LinearLayout)findViewById(R.id.id_item_layout_3);
        layout_item_4 = (LinearLayout)findViewById(R.id.id_item_layout_4);
        layout_item_5 = (LinearLayout)findViewById(R.id.id_item_layout_5);
        layout_item_6 = (LinearLayout)findViewById(R.id.id_item_layout_6);

        txtView_1 = (TextView) layout_item_1.findViewById(R.id.id_textView1);
        txtView_2 = (TextView) layout_item_2.findViewById(R.id.id_textView2);
        txtView_3 = (TextView) layout_item_3.findViewById(R.id.id_textView3);
        txtView_4 = (TextView) layout_item_4.findViewById(R.id.id_textView4);
        txtView_5 = (TextView) layout_item_5.findViewById(R.id.id_textView5);
        txtView_6 = (TextView) layout_item_6.findViewById(R.id.id_textView6);

        imgView_1 = (ImageView) layout_item_1.findViewById(R.id.id_imageView1);
        imgView_2 = (ImageView) layout_item_2.findViewById(R.id.id_imageView2);
        imgView_3 = (ImageView) layout_item_3.findViewById(R.id.id_imageView3);
        imgView_4 = (ImageView) layout_item_4.findViewById(R.id.id_imageView4);
        imgView_5 = (ImageView) layout_item_5.findViewById(R.id.id_imageView5);
        imgView_6 = (ImageView) layout_item_6.findViewById(R.id.id_imageView6);


        layout_item_1.setOnClickListener(this);
        layout_item_2.setOnClickListener(this);
        layout_item_3.setOnClickListener(this);
        layout_item_4.setOnClickListener(this);
        layout_item_5.setOnClickListener(this);
        layout_item_6.setOnClickListener(this);
    }

    private void initData(){
        if(itemList==null||itemList.size()==0){
            layout_item_1.setVisibility(View.GONE);
            layout_item_2.setVisibility(View.GONE);
            layout_item_3.setVisibility(View.GONE);
            layout_item_4.setVisibility(View.GONE);
            layout_item_5.setVisibility(View.GONE);
            layout_item_6.setVisibility(View.GONE);

        }else{
            int size = itemList.size();
            for(int i=0; i<size&&i<MAX_ITEM_COUNT; i++){
                MenuInfo item = itemList.get(i);
                if(i==0){
                    layout_item_1.setVisibility(View.VISIBLE);
                    txtView_1.setText(item.itemTxt);
                    imgView_1.setImageResource(item.imgRes_Id);
                } else if(i==1){
                    layout_item_2.setVisibility(View.VISIBLE);
                    txtView_2.setText(item.itemTxt);
                    imgView_2.setImageResource(item.imgRes_Id);
                } else if(i==2){
                    layout_item_3.setVisibility(View.VISIBLE);
                    txtView_3.setText(item.itemTxt);
                    imgView_3.setImageResource(item.imgRes_Id);
                } else if(i==3){
                    layout_item_4.setVisibility(View.VISIBLE);
                    txtView_4.setText(item.itemTxt);
                    imgView_4.setImageResource(item.imgRes_Id);
                } else if(i==4){
                    layout_item_5.setVisibility(View.VISIBLE);
                    txtView_5.setText(item.itemTxt);
                    imgView_5.setImageResource(item.imgRes_Id);
                } else if(i==5){
                    layout_item_6.setVisibility(View.VISIBLE);
                    txtView_6.setText(item.itemTxt);
                    imgView_6.setImageResource(item.imgRes_Id);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        finish();
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {

        if(itemList==null||itemList.size()==0){
            finish();
            return;
        }
        MenuInfo item = null;
        switch (v.getId()) {
            case R.id.id_item_layout_1:
                item = itemList.get(0);
                if(item.clickListener!=null){
                    item.clickListener.onClick(v);
                }
                break;
            case R.id.id_item_layout_2:
                item = itemList.get(1);
                if(item.clickListener!=null){
                    item.clickListener.onClick(v);
                }
                break;
            case R.id.id_item_layout_3:
                item = itemList.get(2);
                if(item.clickListener!=null){
                    item.clickListener.onClick(v);
                }
                break;
            case R.id.id_item_layout_4:
                item = itemList.get(3);
                if(item.clickListener!=null){
                    item.clickListener.onClick(v);
                }
                break;
            case R.id.id_item_layout_5:
                item = itemList.get(4);
                if(item.clickListener!=null){
                    item.clickListener.onClick(v);
                }
                break;
            case R.id.id_item_layout_6:
                item = itemList.get(5);
                if(item.clickListener!=null){
                    item.clickListener.onClick(v);
                }
                break;
            default:
                break;
        }
        finish();
    }

    public static class MenuInfo{
        public int imgRes_Id =0;
        public String itemTxt = "";
        public View.OnClickListener clickListener = null;

        public MenuInfo(int id, String txt, View.OnClickListener listener){
            this.imgRes_Id = id;
            this.itemTxt = txt;
            this.clickListener = listener;
        }

    }


    @Override public void finish() {
        super.finish();
        overridePendingTransition(0,0);
    }
}