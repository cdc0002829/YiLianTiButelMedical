package cn.redcdn.hvs.profiles.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.collectcenter.CollectionInfo;
import cn.redcdn.datacenter.collectcenter.DataBodyInfo;
import cn.redcdn.datacenter.collectcenter.DeleteCollectItems;
import cn.redcdn.datacenter.collectcenter.GetCollectItems;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.manager.CollectionManager;
import cn.redcdn.hvs.im.view.CommonDialog;
import cn.redcdn.hvs.im.view.SharePressableImageView;
import cn.redcdn.hvs.officialaccounts.activity.ArticleActivity;
import cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity;
import cn.redcdn.hvs.profiles.adapter.CollectionListAdapter;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity.INTENT_DATA_ARTICLE_ID;
import static cn.redcdn.hvs.profiles.adapter.CollectionListAdapter.CHAT_TYPE;

/**
 * Created by Administrator on 2017/2/24.
 */

public class CollectionActivity extends BaseActivity implements CollectionListAdapter.CollectionDataListCallBack {
    public static final int IMAGE_TYPE = 2;
    public static final int VEDIO_TYPE = 3;
    public static final int AUDIO_TYPE = 7;
    public static final int WORD_TYPE = 8;
    public static final int ARTICAL_TYPE = 30;
    private XRecyclerView recyclerView;
    private CollectionListAdapter mCollectionListAdapter;
    List<DataBodyInfo> mCollectionInfo;
    private RelativeLayout collectionDataList1;
    private RelativeLayout collectionNoData;


    public static final String KEY_RECEIVER = "key_receive";
    private boolean isComeIM = false;
    private String mReceive = "";
    private GetCollectItems mGetCollectItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        mCollectionListAdapter = new CollectionListAdapter(this);
        initDataFirst();
        initView();
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.favorite));

        if (isComeIM) {
            titleBar.setBackText(getString(R.string.btn_cancle));
        } else {
            titleBar.enableBack();
        }
    }

    private void initDataFirst() {
        mReceive = getIntent().getStringExtra(KEY_RECEIVER);
        if (mReceive != null && mReceive.length() > 0) {
            isComeIM = true;
            CustomLog.d(TAG, "IM 跳转过来");
        } else {
            isComeIM = false;
            CustomLog.d(TAG, "其他activity 跳转过来");
        }
        mGetCollectItems = new GetCollectItems() {
            @Override
            protected void onSuccess(List<CollectionInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                for (int i = 0; i < responseContent.size(); i++) {
                    CollectionInfo collectionInfo = responseContent.get(i);
                    int type = collectionInfo.getType();
                    List<DataBodyInfo> dataList = collectionInfo.getDataList();
                    for (int i1 = 0; i1 < dataList.size(); i1++) {
                        DataBodyInfo dataBodyInfo = dataList.get(i1);
                        String collectionId = dataBodyInfo.getCollectionId();
                        String localUrl = dataBodyInfo.getLocalUrl();
                        String remoteUrl = dataBodyInfo.getRemoteUrl();
                        String thumbnailRemoteUrl = dataBodyInfo.getThumbnailRemoteUrl();
                        String fileType = dataBodyInfo.getFileType();
                        int photoHeight = dataBodyInfo.getPhotoHeight();
                        int photoWidh = dataBodyInfo.getPhotoWidh();
                        int duration = dataBodyInfo.getDuration();
                        Long size = dataBodyInfo.getSize();
                        String fileName = dataBodyInfo.getFileName();
                        String compressPath = dataBodyInfo.getCompressPath();
                        String txt = dataBodyInfo.getTxt();
                        String officialAccountId = dataBodyInfo.getOfficialAccountId();
                        String name = dataBodyInfo.getName();
                        String articleId = dataBodyInfo.getArticleId();
                        String createTime = dataBodyInfo.getCreateTime();
                        int isforwarded = dataBodyInfo.getIsforwarded();
                        String forwarderNube = dataBodyInfo.getForwarderNube();
                        String forwarderName = dataBodyInfo.getForwarderName();
                        String forwarderHeaderUrl = dataBodyInfo.getForwarderHeaderUrl();
                        int dataBodyInfoType = dataBodyInfo.getType();
                        String collecTime = dataBodyInfo.getCollecTime();
                        String groupName = dataBodyInfo.getGroupName();
                        String combineHeadUrl = dataBodyInfo.getCombineHeadUrl();
                        String combineName = dataBodyInfo.getCombineName();
                        String messageTime = dataBodyInfo.getMessageTime();
                        String cardname = dataBodyInfo.getCardname();
                        String cardCode = dataBodyInfo.getCardCode();
                        String cardUrl = dataBodyInfo.getCardUrl();
                        String text = dataBodyInfo.getText();
                        CustomLog.i("CollectionActivity::responseContent:dataBodyInfo", "collectionId=" + collectionId + ",localUrl=" + localUrl + ",remoteUrl=" + remoteUrl + ",thumbnailRemoteUrl=" + thumbnailRemoteUrl + ",fileType=" + fileType + ",photoHeight=" + photoHeight + ",photoWidh=" + photoWidh + ",duration=" + duration + ",size=" + size + ",fileName=" + fileName + ",compressPath=" + compressPath + ",txt=" + txt + ",officialAccountId=" + officialAccountId + ",name=" + name + ",articleId" + articleId + ",createTime=" + createTime + ",isforwarded=" + isforwarded + ",forwarderNube=" + forwarderNube + ",forwarderName=" + forwarderName + ",forwarderHeaderUrl=" + forwarderHeaderUrl + ",dataBodyInfoType=" + dataBodyInfoType + ",collecTime=" + collecTime + ",groupName=" + groupName + ",combineHeadUrl=" + combineHeadUrl + ",combineName=" + combineName + ",messageTime=" + messageTime + ",cardname=" + cardname + ",cardCode=" + cardCode + ",cardUrl=" + cardUrl + ",text=" + text);
                        List<DataBodyInfo> combineInfoList = dataBodyInfo.getCombineInfoList();
                        for (int i2 = 0; i2 < combineInfoList.size(); i2++) {
                            DataBodyInfo dataBodyInfo1 = combineInfoList.get(i2);
                            String collectionId1 = dataBodyInfo1.getCollectionId();
                            String localUrl1 = dataBodyInfo1.getLocalUrl();
                            String remoteUrl1 = dataBodyInfo1.getRemoteUrl();
                            String thumbnailRemoteUrl1 = dataBodyInfo1.getThumbnailRemoteUrl();
                            String fileType1 = dataBodyInfo1.getFileType();
                            int photoHeight1 = dataBodyInfo1.getPhotoHeight();
                            int photoWidh1 = dataBodyInfo1.getPhotoWidh();
                            int duration1 = dataBodyInfo1.getDuration();
                            Long size1 = dataBodyInfo1.getSize();
                            String fileName1 = dataBodyInfo1.getFileName();
                            String compressPath1 = dataBodyInfo1.getCompressPath();
                            String txt1 = dataBodyInfo1.getTxt();
                            String officialAccountId1 = dataBodyInfo1.getOfficialAccountId();
                            String name1 = dataBodyInfo1.getName();
                            String articleId1 = dataBodyInfo1.getArticleId();
                            String createTime1 = dataBodyInfo1.getCreateTime();
                            int isforwarded1 = dataBodyInfo1.getIsforwarded();
                            String forwarderNube1 = dataBodyInfo1.getForwarderNube();
                            String forwarderName1 = dataBodyInfo1.getForwarderName();
                            String forwarderHeaderUrl1= dataBodyInfo1.getForwarderHeaderUrl();
                            int dataBodyInfoType1 = dataBodyInfo1.getType();
                            String collecTime1 = dataBodyInfo1.getCollecTime();
                            String groupName1 = dataBodyInfo1.getGroupName();
                            String combineHeadUrl1 = dataBodyInfo1.getCombineHeadUrl();
                            String combineName1 = dataBodyInfo1.getCombineName();
                            String messageTime1 = dataBodyInfo1.getMessageTime();
                            String cardname1 = dataBodyInfo1.getCardname();
                            String cardCode1 = dataBodyInfo1.getCardCode();
                            String cardUrl1 = dataBodyInfo1.getCardUrl();
                            String text1 = dataBodyInfo1.getText();
                            int type1 = dataBodyInfo1.getType();
                            CustomLog.i("CollectionActivity::responseContent:dataBodyInfo:combineInfoList", "collectionId=" + collectionId1 + ",localUrl=" + localUrl1 + ",remoteUrl=" + remoteUrl1 + ",thumbnailRemoteUrl=" + thumbnailRemoteUrl1 + ",fileType=" + fileType1 + ",photoHeight=" + photoHeight1 + ",photoWidh=" + photoWidh1 + ",duration=" + duration1 + ",size=" + size1 + ",fileName=" + fileName1 + ",compressPath=" + compressPath1 + ",txt=" + txt1 + ",officialAccountId=" + officialAccountId1 + ",name=" + name1 + ",articleId" + articleId1 + ",createTime=" + createTime1 + ",isforwarded=" + isforwarded1 + ",forwarderNube=" + forwarderNube1 + ",forwarderName=" + forwarderName1 + ",forwarderHeaderUrl=" + forwarderHeaderUrl1 + ",dataBodyInfoType=" + dataBodyInfoType1 + ",collecTime=" + collecTime1 + ",groupName=" + groupName1 + ",combineHeadUrl=" + combineHeadUrl1 + ",combineName=" + combineName1 + ",messageTime=" + messageTime1 + ",cardname=" + cardname1 + ",cardCode=" + cardCode1 + ",cardUrl=" + cardUrl1 + ",text=" + text1+",type="+type1);
                        }

                    }
                    String operatorNube = collectionInfo.getOperatorNube();
                    String time = collectionInfo.getTime();
                    CustomLog.i("CollectionActivity::responseContent:collectionInfo", "type=" + type + ",operatorNube=" + operatorNube + ",time=" + time);
                }
                mCollectionInfo = new ArrayList<>();
                for (int i = 0; i < responseContent.size(); i++) {
                    CollectionInfo collectionInfo = responseContent.get(i);
                    int type = collectionInfo.getType();
                    List<DataBodyInfo> dataList = collectionInfo.getDataList();
                    for (int j = 0; j < dataList.size(); j++) {
                        DataBodyInfo dataBodyInfo = dataList.get(j);
                        dataBodyInfo.setType(type);
                        mCollectionInfo.add(dataBodyInfo);
                    }
                }
                mCollectionListAdapter.setData(mCollectionInfo);
                recyclerView.refreshComplete();
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                if (statusCode == -201) {
                    AccountManager.getInstance(CollectionActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(CollectionActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
            }
        };

        MDSAccountInfo userInfo = AccountManager.getInstance(MedicalApplication.getContext()).getAccountInfo();
        mGetCollectItems.getCollectionItem(userInfo.getNube(), userInfo.getAccessToken(), 0);
        CollectionActivity.this.showLoadingView(getString(R.string.data_load), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                mGetCollectItems.cancel();
                CollectionActivity.this.finish();
                CustomToast.show(getApplicationContext(), R.string.cancel_loading, Toast.LENGTH_SHORT);
            }
        });
    }

    ;

    private void initData() {
        mReceive = getIntent().getStringExtra(KEY_RECEIVER);
        if (mReceive != null && mReceive.length() > 0) {
            isComeIM = true;
            CustomLog.d(TAG, "IM 跳转过来");
        } else {
            isComeIM = false;
            CustomLog.d(TAG, "其他activity 跳转过来");
        }
        mGetCollectItems = new GetCollectItems() {
            @Override
            protected void onSuccess(List<CollectionInfo> responseContent) {
                super.onSuccess(responseContent);
                for (int i = 0; i < responseContent.size(); i++) {
                    CollectionInfo collectionInfo = responseContent.get(i);
                    int type = collectionInfo.getType();
                    List<DataBodyInfo> dataList = collectionInfo.getDataList();
                    for (int i1 = 0; i1 < dataList.size(); i1++) {
                        DataBodyInfo dataBodyInfo = dataList.get(i1);
                        String collectionId = dataBodyInfo.getCollectionId();
                        String localUrl = dataBodyInfo.getLocalUrl();
                        String remoteUrl = dataBodyInfo.getRemoteUrl();
                        String thumbnailRemoteUrl = dataBodyInfo.getThumbnailRemoteUrl();
                        String fileType = dataBodyInfo.getFileType();
                        int photoHeight = dataBodyInfo.getPhotoHeight();
                        int photoWidh = dataBodyInfo.getPhotoWidh();
                        int duration = dataBodyInfo.getDuration();
                        Long size = dataBodyInfo.getSize();
                        String fileName = dataBodyInfo.getFileName();
                        String compressPath = dataBodyInfo.getCompressPath();
                        String txt = dataBodyInfo.getTxt();
                        String officialAccountId = dataBodyInfo.getOfficialAccountId();
                        String name = dataBodyInfo.getName();
                        String articleId = dataBodyInfo.getArticleId();
                        String createTime = dataBodyInfo.getCreateTime();
                        int isforwarded = dataBodyInfo.getIsforwarded();
                        String forwarderNube = dataBodyInfo.getForwarderNube();
                        String forwarderName = dataBodyInfo.getForwarderName();
                        String forwarderHeaderUrl = dataBodyInfo.getForwarderHeaderUrl();
                        int dataBodyInfoType = dataBodyInfo.getType();
                        String collecTime = dataBodyInfo.getCollecTime();
                        String groupName = dataBodyInfo.getGroupName();
                        String combineHeadUrl = dataBodyInfo.getCombineHeadUrl();
                        String combineName = dataBodyInfo.getCombineName();
                        String messageTime = dataBodyInfo.getMessageTime();
                        String cardname = dataBodyInfo.getCardname();
                        String cardCode = dataBodyInfo.getCardCode();
                        String cardUrl = dataBodyInfo.getCardUrl();
                        String text = dataBodyInfo.getText();
                        CustomLog.i("CollectionActivity::responseContent:dataBodyInfo", "collectionId=" + collectionId + ",localUrl=" + localUrl + ",remoteUrl=" + remoteUrl + ",thumbnailRemoteUrl=" + thumbnailRemoteUrl + ",fileType=" + fileType + ",photoHeight=" + photoHeight + ",photoWidh=" + photoWidh + ",duration=" + duration + ",size=" + size + ",fileName=" + fileName + ",compressPath=" + compressPath + ",txt=" + txt + ",officialAccountId=" + officialAccountId + ",name=" + name + ",articleId" + articleId + ",createTime=" + createTime + ",isforwarded=" + isforwarded + ",forwarderNube=" + forwarderNube + ",forwarderName=" + forwarderName + ",forwarderHeaderUrl=" + forwarderHeaderUrl + ",dataBodyInfoType=" + dataBodyInfoType + ",collecTime=" + collecTime + ",groupName=" + groupName + ",combineHeadUrl=" + combineHeadUrl + ",combineName=" + combineName + ",messageTime=" + messageTime + ",cardname=" + cardname + ",cardCode=" + cardCode + ",cardUrl=" + cardUrl + ",text=" + text);
                        List<DataBodyInfo> combineInfoList = dataBodyInfo.getCombineInfoList();
                        for (int i2 = 0; i2 < combineInfoList.size(); i2++) {
                            DataBodyInfo dataBodyInfo1 = combineInfoList.get(i2);
                            String collectionId1 = dataBodyInfo1.getCollectionId();
                            String localUrl1 = dataBodyInfo1.getLocalUrl();
                            String remoteUrl1 = dataBodyInfo1.getRemoteUrl();
                            String thumbnailRemoteUrl1 = dataBodyInfo1.getThumbnailRemoteUrl();
                            String fileType1 = dataBodyInfo1.getFileType();
                            int photoHeight1 = dataBodyInfo1.getPhotoHeight();
                            int photoWidh1 = dataBodyInfo1.getPhotoWidh();
                            int duration1 = dataBodyInfo1.getDuration();
                            Long size1 = dataBodyInfo1.getSize();
                            String fileName1 = dataBodyInfo1.getFileName();
                            String compressPath1 = dataBodyInfo1.getCompressPath();
                            String txt1 = dataBodyInfo1.getTxt();
                            String officialAccountId1 = dataBodyInfo1.getOfficialAccountId();
                            String name1 = dataBodyInfo1.getName();
                            String articleId1 = dataBodyInfo1.getArticleId();
                            String createTime1 = dataBodyInfo1.getCreateTime();
                            int isforwarded1 = dataBodyInfo1.getIsforwarded();
                            String forwarderNube1 = dataBodyInfo1.getForwarderNube();
                            String forwarderName1 = dataBodyInfo1.getForwarderName();
                            String forwarderHeaderUrl1= dataBodyInfo1.getForwarderHeaderUrl();
                            int dataBodyInfoType1 = dataBodyInfo1.getType();
                            String collecTime1 = dataBodyInfo1.getCollecTime();
                            String groupName1 = dataBodyInfo1.getGroupName();
                            String combineHeadUrl1 = dataBodyInfo1.getCombineHeadUrl();
                            String combineName1 = dataBodyInfo1.getCombineName();
                            String messageTime1 = dataBodyInfo1.getMessageTime();
                            String cardname1 = dataBodyInfo1.getCardname();
                            String cardCode1 = dataBodyInfo1.getCardCode();
                            String cardUrl1 = dataBodyInfo1.getCardUrl();
                            String text1 = dataBodyInfo1.getText();
                            int type1 = dataBodyInfo1.getType();
                            CustomLog.i("CollectionActivity::responseContent:dataBodyInfo:combineInfoList", "collectionId=" + collectionId1 + ",localUrl=" + localUrl1 + ",remoteUrl=" + remoteUrl1 + ",thumbnailRemoteUrl=" + thumbnailRemoteUrl1 + ",fileType=" + fileType1 + ",photoHeight=" + photoHeight1 + ",photoWidh=" + photoWidh1 + ",duration=" + duration1 + ",size=" + size1 + ",fileName=" + fileName1 + ",compressPath=" + compressPath1 + ",txt=" + txt1 + ",officialAccountId=" + officialAccountId1 + ",name=" + name1 + ",articleId" + articleId1 + ",createTime=" + createTime1 + ",isforwarded=" + isforwarded1 + ",forwarderNube=" + forwarderNube1 + ",forwarderName=" + forwarderName1 + ",forwarderHeaderUrl=" + forwarderHeaderUrl1 + ",dataBodyInfoType=" + dataBodyInfoType1 + ",collecTime=" + collecTime1 + ",groupName=" + groupName1 + ",combineHeadUrl=" + combineHeadUrl1 + ",combineName=" + combineName1 + ",messageTime=" + messageTime1 + ",cardname=" + cardname1 + ",cardCode=" + cardCode1 + ",cardUrl=" + cardUrl1 + ",text=" + text1+",type="+type1);
                        }

                    }
                    String operatorNube = collectionInfo.getOperatorNube();
                    String time = collectionInfo.getTime();
                    CustomLog.i("CollectionActivity::responseContent:collectionInfo", "type=" + type + ",operatorNube=" + operatorNube + ",time=" + time);
                }
                mCollectionInfo = new ArrayList<>();
                for (int i = 0; i < responseContent.size(); i++) {
                    CollectionInfo collectionInfo = responseContent.get(i);
                    int type = collectionInfo.getType();
                    List<DataBodyInfo> dataList = collectionInfo.getDataList();
                    for (int j = 0; j < dataList.size(); j++) {
                        DataBodyInfo dataBodyInfo = dataList.get(j);
                        dataBodyInfo.setType(type);
                        mCollectionInfo.add(dataBodyInfo);
                    }
                }
                mCollectionListAdapter.setData(mCollectionInfo);
                recyclerView.refreshComplete();
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                if (statusCode == -201) {
                    AccountManager.getInstance(CollectionActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(CollectionActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
            }
        };

        MDSAccountInfo userInfo = AccountManager.getInstance(MedicalApplication.getContext()).getAccountInfo();
        mGetCollectItems.getCollectionItem(userInfo.getNube(), userInfo.getAccessToken(), 0);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGetCollectItems.cancel();
    }

    private View inflate;
    private TextView choosePhoto;
    private TextView takePhoto;
    private Dialog dialog;

    private void mShowDialog(final DataBodyInfo data) {
        dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate = LayoutInflater.from(this).inflate(R.layout.delete_dialog, null);
        //初始化控件
        choosePhoto = (TextView) inflate.findViewById(R.id.dy_tv);
        takePhoto = (TextView) inflate.findViewById(R.id.cancle_tv);
        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DeleteCollectItems deleteCollectItems = new DeleteCollectItems() {
                    @Override
                    protected void onSuccess(JSONObject responseContent) {
                        super.onSuccess(responseContent);
                        mCollectionInfo.remove(data);
                        mCollectionListAdapter.setData(mCollectionInfo);
                        CollectionManager.getInstance().deleteCollectionById(data.getCollectionId());
                        CustomToast.show(getApplicationContext(),getString(R.string.delete_collection_suc), 5000);
                        String remoteUrl = data.getRemoteUrl();
                        String[] split = remoteUrl.split("\\.");
                        String split1 = split[split.length - 1];
                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(data.getRemoteUrl()) + "." + split1);
                        if (file.exists()) {
                            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(data.getRemoteUrl()) + "." + split1;
                            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            ContentResolver mContentResolver = CollectionActivity.this.getContentResolver();
                            String where = MediaStore.Video.Media.DATA + "='" + path + "'";
                            mContentResolver.delete(uri, where, null);
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri urii = Uri.fromFile(file);
                            intent.setData(urii);
                            CollectionActivity.this.sendBroadcast(intent);
                        }
                    }

                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        super.onFail(statusCode, statusInfo);
                        CustomToast.show(getApplicationContext(), getString(R.string.delete_collection_fail), 5000);
                    }
                };
                String id = data.getCollectionId();
                String nube = AccountManager.getInstance(CollectionActivity.this)
                        .getAccountInfo().getNube();
                String accessToken = AccountManager.getInstance(CollectionActivity.this)
                        .getAccountInfo().getAccessToken();
                deleteCollectItems.deleteCollectionItems(nube, id, accessToken);
                dialog.dismiss();
            }
        });
        takePhoto.setOnClickListener(mbtnHandleEventListener);
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
            case R.id.cancle_tv:
                dialog.dismiss();
                break;
        }
    }


    private void initView() {
        collectionDataList1 = (RelativeLayout) findViewById(R.id.collection_data_list1);
        collectionNoData = (RelativeLayout) findViewById(R.id.collection_no_data);
        recyclerView = (XRecyclerView) findViewById(R.id.collection_recyclerview);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(mCollectionListAdapter);
        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                initData();
            }

            @Override
            public void onLoadMore() {
                recyclerView.loadMoreComplete();
            }
        });
        mCollectionListAdapter.setOnItemClickListener(new CollectionListAdapter.OnRecyclerViewItemClickListener() {
            @Override

            public void onItemClick(View view, DataBodyInfo data) {
                if (isComeIM) {
                    CustomLog.d(TAG, "发送收藏消息，直接返回");
                    showForwardDialog(data);
                    return;
                }
                switch (data.getType()) {
                    case WORD_TYPE:
                        Intent intentWordActivity = new Intent();
                        intentWordActivity.setClass(getApplicationContext(), CollectionWordActivity.class);
                        intentWordActivity.putExtra(CollectionWordActivity.COLLECTION_TEXT_DATA, data);
                        startActivity(intentWordActivity);
                        break;
                    case IMAGE_TYPE:
                        Intent intentImageActivity = new Intent();
                        intentImageActivity.setClass(getApplicationContext(), CollectionImageActivity.class);
                        intentImageActivity.putExtra(CollectionImageActivity.COLLECTION_IMAGE_DATA, data);
                        startActivity(intentImageActivity);
                        break;
                    case VEDIO_TYPE:
                        ConnectivityManager con = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
                        boolean wifi = con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
                        boolean internet = con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
                        if (!wifi && !internet) {
                            CustomToast.show(CollectionActivity.this,getString(R.string.cnnect_web),CustomToast.LENGTH_LONG);
                            return;
                        }
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
                    case CHAT_TYPE:
                        Intent intentChatImageActivity = new Intent();
                        intentChatImageActivity.setClass(getApplicationContext(), ChatCollectionActivity.class);
                        intentChatImageActivity.putExtra(ChatCollectionActivity.COLLECTION_CHAT_DATA, data);
                        startActivity(intentChatImageActivity);
                        break;
                    default:
                        break;
                }
            }

        });
        mCollectionListAdapter.setLongItemClickListener(new CollectionListAdapter.OnLongViewItemClickListener() {
            @Override
            public void longClick(View view, DataBodyInfo data) {
                mShowDialog(data);
            }
        });
    }

    private void showForwardDialog(final DataBodyInfo bean) {
        if (bean.getType() == FileTaskManager.NOTICE_TYPE_AUDIO_SEND) {
            CustomToast.show(CollectionActivity.this, getString(R.string.collect_voice_not_forward), CustomToast.LENGTH_SHORT);
            return;
        }

        // 图片，视频，文件，转发之前需判断数据有效性
        if (bean.getType() == FileTaskManager.NOTICE_TYPE_PHOTO_SEND
                || bean.getType() == FileTaskManager.NOTICE_TYPE_VEDIO_SEND) {
            boolean isValidFile = CollectionManager.getInstance().isValidFilePath(bean.getLocalUrl());
            if (!isValidFile && TextUtils.isEmpty(bean.getRemoteUrl())) {
                // 本地文件存在，或者服务端文件存在，则任务此数据有效
                CustomLog.d(TAG, "本地文件或者服务器文件不存在,localUrl:"
                        + bean.getLocalUrl() + "remoteURL:" + bean.getRemoteUrl());
                return;
            }
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View selfView = inflater.inflate(R.layout.share_confirm_dialog_view,
                null);

        // 自定义dialog view
        initSelfControl(selfView);
        CommonDialog dialog = new CommonDialog(this, getLocalClassName(), 415);
        dialog.addView(selfView);
        dialog.setCancelable(false);
        dialog.setTitleVisible(getString(R.string.share_dialog_title));

        switch (bean.getType()) {
            case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                String txt = bean.getTxt();
                dialog.setTransmitInfo(txt);
                break;
            case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                dialog.setTrasmitPic(bean.getRemoteUrl(), R.drawable.default_link_pic,0);
                break;
            case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                dialog.setTrasmitPic(bean.getThumbnailRemoteUrl(), R.drawable.default_link_pic,1);
                break;
            case FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND:
                dialog.setTransmitInfo(getString(R.string.chat_content1) + bean.getText());
                break;
            case FileTaskManager.NOTICE_TYPE_ARTICAL_SEND:
                dialog.setTransmitInfo(getString(R.string.artical_content) + bean.getTitle());
                break;
            default:
                break;
        }

        dialog.setCancleButton(null, getString(R.string.btn_cancle));
        dialog.setPositiveButton(new CommonDialog.BtnClickedListener() {

            @Override
            public void onBtnClicked() {
                // 进行下面的发消息流程
                CustomLog.d(TAG, "点击了提示框中的 确定 按钮");
                doSendMsg(bean);
            }
        }, R.string.btn_send);
        dialog.showDialog();
    }

    private void initSelfControl(View selfView) {

        TextView nameView = (TextView) selfView.findViewById(R.id.name_txt);
        TextView numView = (TextView) selfView
                .findViewById(R.id.recv_num_field);
        SharePressableImageView icon = (SharePressableImageView) selfView
                .findViewById(R.id.contact_icon);

        Intent dataIntent = getIntent();

        String chattype = dataIntent.getStringExtra("chatType");
        String headUrl = dataIntent.getStringExtra("headUrl");
        Glide.with(CollectionActivity.this).
                load(headUrl)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade().into(icon.shareImageview);
        nameView.setText(dataIntent.getStringExtra("chatNames"));

        if(chattype.equals("group")){
            int groupMemberSize = dataIntent.getIntExtra("chatNumber",0);
            numView.setVisibility(View.VISIBLE);
            numView.setText(groupMemberSize + getString(R.string.person));
        }else {
            numView.setVisibility(View.GONE);
        }
    }

    private void doSendMsg(DataBodyInfo itemInfo) {
        if(itemInfo.getType() == FileTaskManager.NOTICE_TYPE_ARTICAL_SEND){
            if (new FileTaskManager(CollectionActivity.this).forwordArticleMsg(mReceive, itemInfo, -1)) {
                CollectionActivity.this.finish();
            } else {
                CustomToast.show(CollectionActivity.this,getString(R.string.collection_send_fail), CustomToast.LENGTH_SHORT);
            }
        }else{
            if (new FileTaskManager(CollectionActivity.this).forwardMessageForCollectionOther(mReceive, itemInfo, -1)) {
                CollectionActivity.this.finish();
            } else {
                CustomToast.show(CollectionActivity.this,getString(R.string.collection_send_fail), CustomToast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onDataSizeChanged(int count) {
        if (count <= 0) {
            collectionNoData.setVisibility(View.VISIBLE);
            collectionDataList1.setVisibility(View.GONE);
        } else {
            collectionNoData.setVisibility(View.GONE);
            collectionDataList1.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initData();
    }
}
