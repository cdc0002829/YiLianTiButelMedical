package cn.redcdn.hvs.profiles.activity;

import android.os.Bundle;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;

/**
 * Created by Administrator on 2017/3/13.
 */

public class OutDateActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outdate);
        getTitleBar().enableBack();
    }
}
