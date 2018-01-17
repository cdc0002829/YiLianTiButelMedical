package cn.redcdn.hvs.officialaccounts.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.officialaccounts.fragment.ContentFragment;
import cn.redcdn.hvs.officialaccounts.fragment.IntroFragment;
import cn.redcdn.hvs.officialaccounts.widget.SlidingUpPanelLayout;

/**
 * Created by ${chenghb} on 2017/2/27.
 */
public class OfficialMainActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = OfficialMainActivity.class.getName();

    FrameLayout main;
    FrameLayout sliding;
    SlidingUpPanelLayout mSlidingLayout;
    Button show_btn;
    protected FragmentManager mFragmentManager;
    private String officialAccountId;
    private String officialName;
    private boolean flag = true;
    private float fromDegrees = 0f;
    private float toDegrees = 0f;
    private RotateAnimation animation;
    IntroFragment introFragment;
    ContentFragment contentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officialmain);
        main = (FrameLayout) findViewById(R.id.main);
        sliding = (FrameLayout) findViewById(R.id.sliding);
        mSlidingLayout = (SlidingUpPanelLayout) findViewById(R.id.activity_mainofficial);
        show_btn = (Button) findViewById(R.id.show_btn);
        show_btn.setOnClickListener(this);
//        Animation shake = AnimationUtils.loadAnimation(getApplication(), R.anim.shake);
//        show_btn.startAnimation(shake);
        TranslateAnimation alphaAnimation2 = new TranslateAnimation(0F, 0F,0F,-30F);  //同一个x轴 (开始结束都是50f,所以x轴保存不变)  y轴开始点50f  y轴结束点80f
        alphaAnimation2.setDuration(200);  //设置时间
        alphaAnimation2.setRepeatCount(7);  //为重复执行的次数。如果设置为n，则动画将执行n+1次。INFINITE为无限制播放
        alphaAnimation2.setRepeatMode(Animation.REVERSE);  //为动画效果的重复模式，常用的取值如下。RESTART：重新从头开始执行。REVERSE：反方向执行

        // AccelerateDecelerateInterpolator 在动画开始与介绍的地方速率改变比较慢，在中间的时候加速
        // AccelerateInterpolator  在动画开始的地方速率改变比较慢，然后开始加速
        // AnticipateInterpolator 开始的时候向后然后向前甩
        // AnticipateOvershootInterpolator 开始的时候向后然后向前甩一定值后返回最后的值
        // BounceInterpolator   动画结束的时候弹起
        // CycleInterpolator 动画循环播放特定的次数，速率改变沿着正弦曲线
        // DecelerateInterpolator 在动画开始的地方快然后慢
        // LinearInterpolator   以常量速率改变
        // OvershootInterpolator    向前甩一定值后再回到原来位置

        //上面那些效果可以自已尝试下
        alphaAnimation2.setInterpolator(new  AccelerateDecelerateInterpolator());//动画结束的时候弹起
        show_btn.setAnimation(alphaAnimation2);
        alphaAnimation2.start();
        mFragmentManager = getSupportFragmentManager();

        initTitleBar();
        setListener();
        //传值
        Intent intent = getIntent();
        officialAccountId = intent.getStringExtra("officialAccountId");
        officialName = intent.getStringExtra("officialName");

    }

    protected void initTitleBar() {
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    protected void setListener() {
        mSlidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                show_btn.setEnabled(false);

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                show_btn.setEnabled(true);
                if (mSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    if (isShow) {

                    } else {
                        excuteAnimat(false);
                    }
                } else if (mSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                    if (isShow) {
                        excuteAnimat(true);
                    } else {

                    }
                    isShow = false;
                }

            }
        });

        mSlidingLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
    }

    //实现动画的旋转
    private void excuteAnimat(boolean flag) {
        if (flag) {
            fromDegrees = 180f;
            toDegrees = 360f;
        } else {
            fromDegrees = 0;
            toDegrees = 180f;
        }
        animation = new RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(300);//设置动画持续时间

        animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        show_btn.startAnimation(animation);
    }

    //返回键
    @Override
    public void onBackPressed() {
        if (mSlidingLayout != null &&
                (mSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED
                        || mSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            // 关闭列表
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        initData();

    }

    protected void initData() {
        Bundle bundle = new Bundle();
        bundle.putString("officialAccountId", officialAccountId);
        bundle.putString("officialName", officialName);
        IntroFragment introFragment = (IntroFragment) IntroFragment.createInstance(20);
        ContentFragment contentFragment = (ContentFragment) ContentFragment.createInstance(20);
        contentFragment.addTitleBackListener(new ContentFragment.TitleBackListener() {
            @Override
            public void onTitleBackPress() {
                if (mSlidingLayout != null &&
                        (mSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED
                                || mSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
                    // 关闭列表
                    mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

                }
            }
        });
        introFragment.setArguments(bundle);
        contentFragment.setArguments(bundle);
        // 主界面显示
        switchFragment(R.id.main, introFragment);
        // 拉出界面
        switchFragment(R.id.sliding, contentFragment);

    }

    /**
     * 提供方法切换Fragment
     *
     * @param fragment
     */
    protected void switchFragment(@IdRes int idRes, Fragment fragment) {

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        //循环的的pop返回栈
        int backStackEntryCount = mFragmentManager.getBackStackEntryCount();
        while (backStackEntryCount > 0) {
            mFragmentManager.popBackStack();
            backStackEntryCount--;
        }
        transaction.replace(idRes, fragment);
        transaction.commitAllowingStateLoss();
    }

    boolean isShow;

    @Override
    public void onClick(View view) {
        if (isShow) {
            show_btn.setEnabled(true);
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            excuteAnimat(true);

        } else {
            show_btn.setEnabled(true);
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            excuteAnimat(false);
        }
        isShow = !isShow;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);

    }
}
