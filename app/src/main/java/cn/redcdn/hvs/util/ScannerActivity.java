package cn.redcdn.hvs.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;

/**
 * 定制化显示扫描界面
 */
public class ScannerActivity extends BaseActivity {

    private CaptureFragment captureFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //定制二维码扫描界面
        setContentView(R.layout.activity_scanner);
        captureFragment = new CaptureFragment();
        // 为二维码扫描 框 界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.my_camera);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_my_container, captureFragment)
                .commit();

        initTitleBar();

    }


    private void initTitleBar() {
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getResources().getString(R.string.titlebar_middle_scan));
        titleBar.enableBack();
    }


    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
            bundle.putString(CodeUtils.RESULT_STRING, result);
            resultIntent.putExtras(bundle);
            ScannerActivity.this.setResult(RESULT_OK, resultIntent);
            ScannerActivity.this.finish();
            overridePendingTransition(0, 0);
        }


        @Override
        public void onAnalyzeFailed() {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
            bundle.putString(CodeUtils.RESULT_STRING, "");
            resultIntent.putExtras(bundle);
            ScannerActivity.this.setResult(RESULT_OK, resultIntent);
            ScannerActivity.this.finish();
        }
    };


}
