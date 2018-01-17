package cn.redcdn.hvs.profiles.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cn.redcdn.datacenter.collectcenter.DataBodyInfo;
import cn.redcdn.datacenter.collectcenter.DeleteCollectItems;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.contacts.contact.ContactCardActivity;
import cn.redcdn.hvs.im.collection.CollectionFileManager;
import cn.redcdn.hvs.im.manager.CollectionManager;
import cn.redcdn.hvs.officialaccounts.activity.ArticleActivity;
import cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity;
import cn.redcdn.hvs.profiles.adapter.CollectionChatListAdapter;
import cn.redcdn.hvs.profiles.collection.SaveImageUtils;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.OpenBigImageActivity;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity.INTENT_DATA_ARTICLE_ID;

/**
 * Created by Administrator on 2017/4/28.
 */

public class ChatCollectionActivity extends BaseActivity {
    public static final int IMAGE_TYPE = 2;
    public static final int VEDIO_TYPE = 3;
    public static final int AUDIO_TYPE = 7;
    public static final int WORD_TYPE = 8;
    public static final int ARTICAL_TYPE = 30;
    public static final int CARD_TYPE = 4;
    public static final String COLLECTION_CHAT_DATA = "collection_chat_data";
    private DataBodyInfo bean;
    private RecyclerView collectionCahtRecyclerview;
    private CollectionChatListAdapter mCollectionChatListAdapter;
    private Button rightButton;
    private Button backButton;
    private TextView collectionTextTime;
    private TextView middleText;
    private boolean isSameDate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_collection);
        mCollectionChatListAdapter = new CollectionChatListAdapter();
        initData();
        initView();

    }

    private View inflate;
    private TextView zhuanfa;
    private TextView cacleZhuanfa;
    private TextView deleteZhuanfa;
    private Dialog dialog;

    private void showDialog() {
        dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate = LayoutInflater.from(this).inflate(R.layout.zhuanfa_dialog1, null);
        //初始化控件
        zhuanfa = (TextView) inflate.findViewById(R.id.zhuanfa_tv);
        deleteZhuanfa = (TextView) inflate.findViewById(R.id.delete_zhuanfa_tv);
        cacleZhuanfa = (TextView) inflate.findViewById(R.id.cancle_zhuanfa_tv);
        zhuanfa.setOnClickListener(mbtnHandleEventListener);
        deleteZhuanfa.setOnClickListener(mbtnHandleEventListener);
        cacleZhuanfa.setOnClickListener(mbtnHandleEventListener);
        //将布局设置给Dialog
        dialog.setContentView(inflate);


        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();

        dialogWindow.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        dialogWindow.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = 20;//设置Dialog距离底部的距离
//       将属性设置给窗体
        dialogWindow.setAttributes(lp);
        dialog.show();//显示对话框
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.zhuanfa_tv:
                dialog.dismiss();
                CustomLog.d(TAG, "转发文本");
                CollectionFileManager.getInstance().onCollectMsgForward(
                        ChatCollectionActivity.this, bean);
                break;
            case R.id.delete_zhuanfa_tv:
                DeleteCollectItems deleteCollectItems = new DeleteCollectItems() {
                    @Override
                    protected void onSuccess(JSONObject responseContent) {
                        super.onSuccess(responseContent);
                        CustomToast.show(getApplicationContext(), getString(R.string.delete_collection_suc), 5000);
                        CollectionManager.getInstance().deleteCollectionById(bean.getCollectionId());
                        ChatCollectionActivity.this.finish();
                    }

                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        super.onFail(statusCode, statusInfo);
                        CustomToast.show(getApplicationContext(), getString(R.string.delete_collection_fail), 5000);
                    }
                };
                String id = bean.getCollectionId();
                String nube = AccountManager.getInstance(this)
                        .getAccountInfo().getNube();
                String accessToken = AccountManager.getInstance(this)
                        .getAccountInfo().getAccessToken();
                deleteCollectItems.deleteCollectionItems(nube, id, accessToken);
                dialog.dismiss();
                break;
            case R.id.cancle_zhuanfa_tv:
                dialog.dismiss();
                break;
            case R.id.cancle1_zhuanfa_tv:
                dialog1.dismiss();
                break;
            case R.id.right_btn:
                showDialog();
                break;
            case R.id.setting_resolution_back:
                ChatCollectionActivity.this.finish();
                break;
        }
    }

    private void initView() {
        rightButton = (Button) findViewById(R.id.right_btn);
        backButton = (Button) findViewById(R.id.setting_resolution_back);
        middleText = (TextView) findViewById(R.id.middle_text);
        collectionTextTime = (TextView) findViewById(R.id.collection_text_time);
        collectionCahtRecyclerview = (RecyclerView) findViewById(R.id.collection_chat_recyclerview);
        setNameAndIcon();
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        collectionCahtRecyclerview.setLayoutManager(mLinearLayoutManager);
        mCollectionChatListAdapter.setData(bean.getCombineInfoList());
        if (isSameDate) {
            mCollectionChatListAdapter.setDate(true);
        } else {
            mCollectionChatListAdapter.setDate(false);
        }

        collectionCahtRecyclerview.setAdapter(mCollectionChatListAdapter);
        mCollectionChatListAdapter.setOnItemClickListener(new CollectionChatListAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, DataBodyInfo data) {
                switch (data.getType()) {
                    case IMAGE_TYPE:
                        Intent intent_inputimage = new Intent(ChatCollectionActivity.this, OpenBigImageActivity.class);
                        intent_inputimage.putExtra(OpenBigImageActivity.DATE_TYPE, OpenBigImageActivity.DATE_TYPE_Internet);
                        intent_inputimage.putExtra(OpenBigImageActivity.DATE_URL, data.getRemoteUrl());
                        startActivity(intent_inputimage);
                        break;
                    case VEDIO_TYPE:
                        Intent intentVedioActivity = new Intent();
                        intentVedioActivity.setClass(getApplicationContext(), CollectionVedioActivity.class);
                        intentVedioActivity.putExtra(CollectionVedioActivity.COLLECTION_VEDIO_DATA, data);
                        startActivity(intentVedioActivity);
                        break;
                    case AUDIO_TYPE:
                        Intent intentAudioActivity = new Intent();
                        intentAudioActivity.setClass(getApplicationContext(), CollectionAudioActivity.class);
                        intentAudioActivity.putExtra(CollectionAudioActivity.COLLECTION_AUDIO_DATA, data);
                        startActivity(intentAudioActivity);
                        break;
                    case ARTICAL_TYPE:
                        Intent intentArticalActivity = new Intent();
                        int type = data.getArticleType();
                        if (1 == type) {
                            intentArticalActivity.setClass(getApplicationContext(), ArticleActivity.class);
                        } else if (2 == type) {
                            intentArticalActivity.setClass(getApplicationContext(), VideoPublishActivity.class);
                        }

                        intentArticalActivity.putExtra(INTENT_DATA_ARTICLE_ID, data.getArticleId());
                        startActivity(intentArticalActivity);
                        break;
                    case CARD_TYPE:
                        Intent intentCardActivity = new Intent();
                        intentCardActivity.setClass(getApplicationContext(), ContactCardActivity.class);
                        intentCardActivity.putExtra("nubeNumber", data.getCardCode());
                        startActivity(intentCardActivity);
                        break;
                    default:
                        break;
                }
            }
        });
        backButton.setOnClickListener(mbtnHandleEventListener);
        rightButton.setOnClickListener(mbtnHandleEventListener);
        rightButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.meeting_title_more, 0);

        mCollectionChatListAdapter.setLongItemClickListener(new CollectionChatListAdapter.OnLongViewItemClickListener() {
            @Override
            public void longClick(View view, DataBodyInfo data) {
                switch (data.getType()) {
                    case IMAGE_TYPE:
                        mShowDialog(data);
                        break;
                }
            }
        });
    }

    TextView cacleZhuanfa1;
    TextView saveTv;
    private Dialog dialog1;
    private View inflate1;

    private void mShowDialog(final DataBodyInfo data) {
        dialog1 = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate1 = LayoutInflater.from(this).inflate(R.layout.zhuanfa_dialog3, null);
        //初始化控件
        cacleZhuanfa1 = (TextView) inflate1.findViewById(R.id.cancle1_zhuanfa_tv);
        saveTv = (TextView) inflate1.findViewById(R.id.save);
        saveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePhoto(data.getRemoteUrl());
                dialog1.dismiss();
            }
        });
        cacleZhuanfa1.setOnClickListener(mbtnHandleEventListener);
        //将布局设置给Dialog
        dialog1.setContentView(inflate1);


        //获取当前Activity所在的窗体
        Window dialogWindow = dialog1.getWindow();

        dialogWindow.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        dialogWindow.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = 20;//设置Dialog距离底部的距离
//       将属性设置给窗体
        dialogWindow.setAttributes(lp);
        dialog1.show();//显示对话框
    }

    private void savePhoto(final String remoteUrl) {
        if (remoteUrl != null) {
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final Bitmap myBitmap = Glide.with(ChatCollectionActivity.this)//上下文
                                        .load(remoteUrl)//url
                                        .asBitmap() //必须
                                        .centerCrop()
                                        .into(500, 500)
                                        .get();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SaveImageUtils.saveImageToGallerys(ChatCollectionActivity.this, myBitmap);
                                    }
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            ).start();
        }
    }

    private void setNameAndIcon() {
        if (bean.getText() != null&&bean.getText().length()>5) {
            middleText.setText(bean.getText().substring(0, bean.getText().length()-5));
        }
        List<DataBodyInfo> combineInfoList = bean.getCombineInfoList();
        String fromMessageTime = combineInfoList.get(0).getMessageTime();
        String toMessageTime = combineInfoList.get(combineInfoList.size() - 1).getMessageTime();
        fromMessageTime = fromMessageTime + "000";
        toMessageTime = toMessageTime + "000";
        long l1 = Long.parseLong(fromMessageTime);
        Date d1 = new Date(l1);
        long l2 = Long.parseLong(toMessageTime);
        Date d2 = new Date(l2);
        SimpleDateFormat format = new SimpleDateFormat(getString(R.string.date));
        if (format.format(d1).equals(format.format(d2))) {
            collectionTextTime.setText(format.format(d1));
            isSameDate = true;
        } else {
            collectionTextTime.setText(format.format(d1) + "~" + format.format(d2));
            isSameDate = false;
        }

    }

    private void initData() {
        Intent i = getIntent();
        bean = (DataBodyInfo) i.getSerializableExtra(COLLECTION_CHAT_DATA);
    }

}
