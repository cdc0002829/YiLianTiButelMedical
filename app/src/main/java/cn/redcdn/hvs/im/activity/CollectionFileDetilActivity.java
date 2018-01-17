package cn.redcdn.hvs.im.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.bean.ButelFileInfo;
import cn.redcdn.hvs.im.bean.CollectionBean;
import cn.redcdn.hvs.im.bean.CollectionEntity;
import cn.redcdn.hvs.im.bean.FileTaskBean;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.collection.CollectionFileManager;
import cn.redcdn.hvs.im.dao.CollectionDao;
import cn.redcdn.hvs.im.dao.DtNoticesDao;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.fileTask.ChangeUIInterface;
import cn.redcdn.hvs.im.fileTask.DownloadTaskManager;
import cn.redcdn.hvs.im.manager.CollectionManager;
import cn.redcdn.hvs.im.view.CommonDialog;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.PopDialogActivity;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.NetWorkUtil;
import java.io.File;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CollectionFileDetilActivity extends BaseActivity {
    private static final String TAG = CollectionFileDetilActivity.class.getSimpleName();

    private ImageView collectionIcon;
    private TextView collectionName;
    private TextView collectionDurationTxt;
    private Button openAndContinueBtn;
    private TextView openFileHint;
    private ProgressBar downLoadBar;
    private Button stopBtn;
    private ButelFileInfo fileInfo;
    private CollectionBean bean;
    private RelativeLayout loadLayout;
    private RelativeLayout openOrReloadLayout;
    private CommonDialog delDialog;
    private NoticesBean nBean;
    public static final String COLLECTION_FILE_DATA = "collection_file_data";
    public static final String COLLECTION_FILE_TYPE = "collection_file_type";
    private static final int MSG_LOAD_SUCCES = 1000;
    private static final int MSG_LOAD_FAIL = 1001;
    private static final int MSG_LOAD_ONPROCESS = 1002;
    public static final String ATTACHMENT_FILE_TYPE = "ATTACHMENT_FILE_TYPE";

    public static final int PDF = 0;
    public static final int DOC = 1;
    public static final int DOCX = 2;
    public static final int NOT_SUPPORT = 3;

    private String fileSuffix; // 文件后缀

    private int fileType;
    private String fileDetailType;

    // 0表示从收藏列表页面跳转过来，1表示从消息页面跳转
    private int collectionType = 0;
    private boolean isFileDownload;
    private File fileTarget;
    private DtNoticesDao mDtNoticesDao;
    private NoticesDao mNoticeDao;
    private boolean isUDTMsgFlag;


    private class DownLoadObject {
        public long mcurrent;
        public long mtotal;
    }



    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_SUCCES:
                    CollectionDao mCollectionDao = new CollectionDao(
                        CollectionFileDetilActivity.this);

                    if (collectionType == 1) {
                        // 更新body localFile 字段

                        NoticesBean mbean;

                        if (isUDTMsgFlag){
                            mbean = mDtNoticesDao.getNoticeById(nBean.getId());

                        }else {
                            mbean = mNoticeDao.getNoticeById(nBean.getId());
                        }
                        fileInfo = ButelFileInfo.parseJsonStr(mbean.getBody(), false);

                    } else {
                        CollectionEntity mCollectionEntity = mCollectionDao
                            .getCollectionEntityById(bean.getId());
                        fileInfo = ButelFileInfo.parseJsonStr(mCollectionEntity
                            .getBody(), true);
                    }
                    loadLayout.setVisibility(View.GONE);
                    openOrReloadLayout.setVisibility(View.VISIBLE);
                    openAndContinueBtn.setText("使用第三方应用打开");
                    openFileHint.setVisibility(View.VISIBLE);
                    break;
                case MSG_LOAD_FAIL:
                    loadLayout.setVisibility(View.GONE);
                    openOrReloadLayout.setVisibility(View.VISIBLE);
                    openAndContinueBtn.setText("继续下载");
                    openFileHint.setVisibility(View.GONE);
                    collectionDurationTxt.setText("正在下载..." + "(0|0)");
                    downLoadBar.setProgress(0);
                    if (!NetWorkUtil
                        .isNetworkConnected(CollectionFileDetilActivity.this)) {
                        CustomToast.show(CollectionFileDetilActivity.this,
                            getString(R.string.setting_internet),
                            Toast.LENGTH_SHORT);
                    } else {
                        CustomToast.show(CollectionFileDetilActivity.this,
                            getString(R.string.collection_file_load_fail),
                            Toast.LENGTH_SHORT);
                    }
                    break;
                case MSG_LOAD_ONPROCESS:
                    DownLoadObject mObj = (DownLoadObject) msg.obj;
                    setLoadLayout(mObj.mcurrent, mObj.mtotal);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_collection_file_detail_layout);
        initData();
        initView();
    }


    public void initData() {
        Intent i = getIntent();
        collectionType = i.getIntExtra(COLLECTION_FILE_TYPE, 0);
        fileType = i.getIntExtra(ATTACHMENT_FILE_TYPE, -1);

        setFileType();

        if (collectionType == 1) {
            nBean = (NoticesBean) i.getSerializableExtra(COLLECTION_FILE_DATA);
            fileInfo = ButelFileInfo.parseJsonStr(nBean.getBody(), false);
        }

        isUDTMsgFlag = isDTMsg(nBean);

        mNoticeDao = new NoticesDao(
            CollectionFileDetilActivity.this);
        mDtNoticesDao = new DtNoticesDao(CollectionFileDetilActivity.this);


    }

    /**
     * 如果当前消息是医联体诊疗文字消息
     */
    private boolean isDTMsg(NoticesBean noticesBean) {

        String extendInfo = noticesBean.getExtInfo();
        try {
            JSONObject extendInfoObj = new JSONObject(extendInfo);
            String hpuMsgFlag = extendInfoObj.optString("medicalComboMsg");
            if (TextUtils.isEmpty(hpuMsgFlag)) {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }
    /**
     * 设置文件类型
     */
    private void setFileType() {
        CustomLog.i(TAG, "setFileType()");

        if (fileType == PDF){
            fileDetailType = "pdf";
        }else if (fileType == DOC){
            fileDetailType = "doc";
        }else if (fileType == DOCX){
            fileDetailType = "docx";
        }
    }


    public void initView() {
        collectionName = (TextView) findViewById(R.id.collecion_file_name);
        collectionDurationTxt = (TextView) findViewById(R.id.download_duration);
        collectionIcon = (ImageView) findViewById(R.id.collection_file_icon);
        openAndContinueBtn = (Button) findViewById(R.id.collection_openfile_btn);
        stopBtn = (Button) findViewById(R.id.collection_stop_btn);
        downLoadBar = (ProgressBar) findViewById(R.id.collection_load_progress);
        openFileHint = (TextView) findViewById(R.id.collection_openfile_txt);
        loadLayout = (RelativeLayout) findViewById(R.id.collection_download_ly);
        openOrReloadLayout = (RelativeLayout) findViewById(R.id.openorreload_layout);
        showUI();

        getTitleBar().setTitle("文件预览");
        getTitleBar().setBack(null, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                finish();
            }
        });

    }


    @SuppressLint("NewApi")
    private void showUI() {

        collectionName.setText(fileInfo.getFileName());
        collectionIcon.setImageResource(CollectionFileManager.getInstance()
            .getNoticeFileDrawableId(fileDetailType));
        final File fileInit = new File(fileInfo.getLocalPath());
        if (!fileInit.exists()) {
            ProgressListener progressListener = new ProgressListener(loadLayout);
            if (collectionType == 1) {
                progressListener.setParams(nBean.getId(), fileInfo.getRemoteUrl(),
                    0, mHandler);
            }
            // fileTaskMgr中管理文件下载进度（已经开始了，绑定一下；未开始，则开始下载并绑定）

            openOrReloadLayout.setVisibility(View.GONE);
            loadLayout.setVisibility(View.VISIBLE);
            openFileHint.setVisibility(View.GONE);
            collectionDurationTxt.setText("正在下载..." + "(0|0)");

            downLoadFile();
        } else {
            isFileDownload = true;

            openOrReloadLayout.setVisibility(View.VISIBLE);
            loadLayout.setVisibility(View.GONE);
            openFileHint.setVisibility(View.VISIBLE);
        }
        openAndContinueBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if ("继续下载".equals(openAndContinueBtn.getText())) {
                    openOrReloadLayout.setVisibility(View.GONE);
                    loadLayout.setVisibility(View.VISIBLE);
                    openFileHint.setVisibility(View.GONE);
                    collectionDurationTxt.setText("正在下载..." + "(0|0)");

                    // 下载文件
                    downLoadFile();
                } else {
                    // 调用第三方应用打开文件

                    if (isFileDownload) {
                        CollectionFileManager.getInstance().OpenFile(fileInit,
                            CollectionFileDetilActivity.this);
                    } else {

                        File fileDownload = new File(fileInfo.getLocalPath());

                        CollectionFileManager.getInstance().OpenFile(fileDownload,
                            CollectionFileDetilActivity.this);
                    }

                }
            }
        });

        stopBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO 停止下载
                if (collectionType == 1) {
                    DownloadTaskManager.getInstance(
                        CollectionFileDetilActivity.this).deletLoadTask(
                        nBean.getId());
                } else {
                    DownloadTaskManager.getInstance(
                        CollectionFileDetilActivity.this).deletLoadTask(
                        bean.getId());
                }
                loadLayout.setVisibility(View.GONE);
                openOrReloadLayout.setVisibility(View.VISIBLE);
                openAndContinueBtn.setText("继续下载");
                collectionDurationTxt.setText("正在下载..." + "(0|0)");
                downLoadBar.setProgress(0);
                openFileHint.setVisibility(View.GONE);
            }
        });
    }


    private void showTitleMore() {
        CustomLog.i(TAG, "showTitleMore()");
        List<PopDialogActivity.MenuInfo> mMoreinfo
            = new ArrayList<PopDialogActivity.MenuInfo>();// 更多按钮操作
        mMoreinfo.add(new PopDialogActivity.MenuInfo(R.drawable.collection_forward, "转发",
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (collectionType == 1) {
                        Intent i = new Intent(CollectionFileDetilActivity.this,
                            ShareLocalActivity.class);
                        i.putExtra(ShareLocalActivity.KEY_ACTION_FORWARD, true);
                        i.putExtra(ShareLocalActivity.MSG_ID, nBean.getId());
                        startActivity(i);
                    } else {
                        CollectionFileManager.getInstance().onMsgForward(
                            CollectionFileDetilActivity.this, bean);
                    }
                }
            }));
        if (collectionType == 1) {
            mMoreinfo.add(
                new PopDialogActivity.MenuInfo(R.drawable.collection_file_detail_btn, "收藏",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CollectionManager.getInstance()
                                .addCollectionByNoticesBean(CollectionFileDetilActivity.this,
                                    nBean);
                        }
                    }));
        } else {
            mMoreinfo.add(new PopDialogActivity.MenuInfo(R.drawable.collection_delete, "删除",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        delDialog = new CommonDialog(
                            CollectionFileDetilActivity.this,
                            getLocalClassName(), 104);
                        delDialog.setMessage(R.string.del_tip);
                        delDialog.setCancelable(false);
                        delDialog.setCancleButton(new CommonDialog.BtnClickedListener() {
                            @Override
                            public void onBtnClicked() {
                                delDialog.dismiss();
                            }
                        }, R.string.cancel_message);
                        delDialog.setPositiveButton(
                            new CommonDialog.BtnClickedListener() {
                                @Override
                                public void onBtnClicked() {
                                    CollectionManager.getInstance()
                                        .removeCollection(bean);
                                    delDialog.dismiss();
                                    CollectionFileDetilActivity.this
                                        .finish();
                                }
                            }, R.string.confirm_message);
                        delDialog.showDialog();
                    }
                }));
        }
        PopDialogActivity.setMenuInfo(mMoreinfo);
        startActivity(new Intent(this, PopDialogActivity.class));
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void onProcessing(FileTaskBean bean, long current, long total) {

    }


    public void onSuccess(FileTaskBean bean, String result) {
    }


    public void onFailure(FileTaskBean bean, Throwable error, String msg) {
    }


    /**
     * <dd>Description:文件下载进度监控</dd> <dd>Copyright: Copyright (C) 2014</dd>
     */
    private class ProgressListener extends ChangeUIInterface {
        private WeakReference<RelativeLayout> progressLineReference;

        private int position = -1;
        private Handler handler;


        public void setParams(String id, String remoteUrl, int position,
                              Handler handler) {
            this.position = position;
            this.handler = handler;
        }


        public ProgressListener(RelativeLayout progressLine) {
            progressLineReference = new WeakReference<RelativeLayout>(
                progressLine);
        }


        public void onStart(FileTaskBean bean) {
            // 开始文件任务

            final RelativeLayout progressLine = progressLineReference.get();
            if (progressLine != null) {
                runOnUiThread(new Runnable() {
                    public void run() {
                    }
                });
            }
        }


        public void onProcessing(FileTaskBean bean, long current, long total) {
            // 文件任务进度

            if (current < 0 || total <= 0) {
                return;
            }
            if (!DownloadTaskManager.getInstance(CollectionFileDetilActivity.this)
                .isExFileTaskBean(bean)) {
                return;
            }
            final RelativeLayout progressLine = progressLineReference.get();
            DownLoadObject obj = new DownLoadObject();
            obj.mcurrent = current;
            obj.mtotal = total;

            Message msg = handler.obtainMessage();
            msg.what = MSG_LOAD_ONPROCESS;
            msg.obj = obj;
            handler.sendMessage(msg);
            if (progressLine != null) {
                runOnUiThread(new Runnable() {
                    @SuppressLint({ "NewApi", "CutPasteId" })
                    public void run() {

                    }
                });
            }
        }


        public void onSuccess(FileTaskBean bean, final String result) {
            // 文件任务成功完成
            if (!DownloadTaskManager.getInstance(CollectionFileDetilActivity.this)
                .isExFileTaskBean(bean)) {
                return;
            }

            if (fileType == PDF) {
                fileSuffix = ".pdf";
            } else if (fileType == DOC) {
                fileSuffix = ".doc";
            } else {
                fileSuffix = ".docx";
            }

            File file = new File(bean.getResultUrl()); // 没有文件后缀
            fileTarget = new File(bean.getResultUrl() + fileSuffix);

            boolean isRenameSuccess = file.renameTo(fileTarget);
            if (isRenameSuccess) {
                CustomLog.i(TAG, "文件重命名成功");
            } else {
                CustomLog.i(TAG, "文件重命名失败");
                return;
            }

            NoticesBean mMsgBean;

            if (isUDTMsgFlag){
                mMsgBean = mDtNoticesDao.getNoticeById(nBean.getId());
            }else {
                mMsgBean = mNoticeDao.getNoticeById(nBean.getId());
            }

            String newBody = "";

            try {
                JSONArray bodyInfoJSONArray = new JSONArray(mMsgBean.getBody());
                JSONObject bodyInfoJSONObject = bodyInfoJSONArray.getJSONObject(
                    0);  // 获取首个 JSONObject
                bodyInfoJSONObject.put("localUrl", fileTarget.getAbsolutePath()); // 将新的文件写入数据库中

                JSONArray newJSONArray = new JSONArray();
                newJSONArray.put(bodyInfoJSONObject);
                newBody = newJSONArray.toString();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (isUDTMsgFlag){
                mDtNoticesDao.updateNotice(mMsgBean.getId(), newBody);
            }else {
                mNoticeDao.updateNotice(mMsgBean.getId(), newBody);
            }


            Message msg = handler.obtainMessage();
            msg.what = MSG_LOAD_SUCCES;
            // handler.sendMessage(msg);
            handler.sendMessageDelayed(msg, 500);
            final RelativeLayout progressLine = progressLineReference.get();
            if (progressLine != null) {
                runOnUiThread(new Runnable() {
                    public void run() {
                    }
                });
            }

        }


        public void onFailure(FileTaskBean bean, Throwable error, String msg) {
            // 文件任务失败
            if (!DownloadTaskManager.getInstance(CollectionFileDetilActivity.this)
                .isExFileTaskBean(bean)) {
                return;
            }
            final RelativeLayout progressLine = progressLineReference.get();
            Message mmsg = handler.obtainMessage();
            mmsg.what = MSG_LOAD_FAIL;
            handler.sendMessage(mmsg);
            if (progressLine != null) {
                runOnUiThread(new Runnable() {
                    public void run() {

                    }
                });
            }
        }
    }


    @SuppressLint("NewApi")
    private void setLoadLayout(long current, long total) {
        final float pro = current / (total * 1.0f);
        NumberFormat numFormat = NumberFormat.getNumberInstance();
        numFormat.setMaximumFractionDigits(2);
        String totalStr = CollectionFileManager.getInstance().convertStorage(
            total);
        String currentStr = CollectionFileManager.getInstance().convertStorage(
            current);
        collectionDurationTxt.setText("正在下载..." + "(" + currentStr + "|"
            + totalStr + ")");
        int duration = (int) (pro * 100);
        downLoadBar.setProgress(duration);
    }


    private void downLoadFile() {
        final ProgressListener progressListener = new ProgressListener(
            loadLayout);
        if (collectionType == 1) {
            progressListener.setParams(nBean.getId(), fileInfo.getRemoteUrl(),
                0, mHandler);
        } else {
            progressListener.setParams(bean.getId(), fileInfo.getRemoteUrl(),
                0, mHandler);
        }
        if (NetWorkUtil.isWifiConnected(CollectionFileDetilActivity.this)) {
            if (collectionType == 1) {
                DownloadTaskManager.getInstance(this).downloadFile(
                    nBean.getId(), "", true, progressListener, -2);
            }
        } else {
            if (!NetWorkUtil
                .isNetworkConnected(CollectionFileDetilActivity.this)) {
                // 当前无网络连接，放弃下载
                CustomToast.show(CollectionFileDetilActivity.this,
                    getString(R.string.setting_internet),
                    Toast.LENGTH_SHORT);
                return;
            }
            // 视频大小大于3M时候出现提醒框
            //TODO
            // 非wifi连接下，提示是否继续下载
            CommonUtil.alertDataConsumeDialog(
                CollectionFileDetilActivity.this, new CommonDialog.BtnClickedListener() {
                    @Override
                    public void onBtnClicked() {
                        CustomLog.d(TAG, "非Wifi网络下，流量使用确认对话框 中，点击‘确定’");
                        if (collectionType == 1) {
                            DownloadTaskManager.getInstance(
                                CollectionFileDetilActivity.this)
                                .downloadFile(nBean.getId(), "", true,
                                    progressListener, -2);
                        } else {
                            DownloadTaskManager.getInstance(
                                CollectionFileDetilActivity.this)
                                .downloadFile(bean.getId(), "", true,
                                    progressListener, 0);
                        }
                    }
                }, new CommonDialog.BtnClickedListener() {
                    @Override
                    public void onBtnClicked() {
                        Message mmsg = mHandler.obtainMessage();
                        mmsg.what = MSG_LOAD_FAIL;
                        mHandler.sendMessage(mmsg);
                    }
                });
        }
    }
}
