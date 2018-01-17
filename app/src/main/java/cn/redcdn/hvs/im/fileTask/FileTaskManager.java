package cn.redcdn.hvs.im.fileTask;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;
import cn.redcdn.datacenter.collectcenter.DataBodyInfo;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.agent.AppP2PAgentManager;
import cn.redcdn.hvs.im.bean.BookMeetingExInfo;
import cn.redcdn.hvs.im.bean.CollectionEntity;
import cn.redcdn.hvs.im.bean.FileTaskBean;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.bean.ThreadsBean;
import cn.redcdn.hvs.im.column.NoticesTable;
import cn.redcdn.hvs.im.dao.CollectionDao;
import cn.redcdn.hvs.im.dao.DtNoticesDao;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.dao.ThreadsDao;
import cn.redcdn.hvs.im.manager.CollectionManager;
import cn.redcdn.hvs.im.preference.DaoPreference.PrefType;
import cn.redcdn.hvs.im.util.CompressUtil;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.util.xutils.http.HttpHandler;
import cn.redcdn.hvs.im.util.xutils.http.HttpUtils;
import cn.redcdn.hvs.im.util.xutils.http.client.RequestParams;
import cn.redcdn.hvs.im.work.BizConstant;
import cn.redcdn.hvs.util.StringUtil;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.LogUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static cn.redcdn.hvs.im.IMConstant.APP_ROOT_FOLDER;
import static cn.redcdn.hvs.im.manager.CollectionManager.isValidFilePath;

/***
 * 负责文件的上传、下载，并维护t_notices表的状态，
 * 若是上传文件，会发送消息至消息服务器，而且发送SIP通知，通知对方及时获取消息
 * 利用线程池，多记录并发执行；单个记录中的多个文件是顺序下载或上传
 *
 * @author miaolikui
 */

public class FileTaskManager {

    private static final String TAG = "FileTaskManager";
    private Context mcontext = null;
    private Map<String, List<FileTaskBean>> fileTaskMap
        = new ConcurrentHashMap<String, List<FileTaskBean>>();
    private Map<String, HttpHandler> runTaskQueue = new ConcurrentHashMap<String, HttpHandler>();
    private NoticesDao noticedao;

    // handler消息
    public static final int MSG_SUCCESS = 2001;
    public static final int MSG_FAILURE = 2002;
    //(废弃2003值的使用)
    public static final int MSG_VCARD_SAVE = 2003;
    public static final int MSG_IMAGE_COMPRESSED = 2004;
    //(废弃2005值的使用)
    public static final int MSG_VIDEO_THUMB_SEND = 2005;
    public static final int NO_SDCARD = 2006;
    //(废弃2007值的使用)
    public static final int SAVE_SDCARD = 2007;

    // 下载文件保存的文件夹
    //    public static String FILE_DIR = CommonConstant.SAVE_DIR_NAME;
    // 压缩文件保存的文件夹
    public static String FILE_COMPRESS_DIR = "compress";
    // 接收到的文件保存的文件夹
    public static String FILE_DOWNLOAD_DIR = "download";
    // 本地录制文件保存的文件夹（视频和声音）
    public static String FILE_RECORD_DIR = "record";
    // 调用系统相机拍照保存的路径
    public static String FILE_TAKE_PHOTO_DIR = "takePhoto";
    // 名片分享产生的vcf文件保存的路径
    public static String FILE_VCARD_DIR = "vcard";

    // 每条消息（FileTaskBean）的状态
    public static final int TASK_STATUS_READY = 0;
    public static final int TASK_STATUS_RUNNING = 1;
    public static final int TASK_STATUS_SUCCESS = 2;
    public static final int TASK_STATUS_FAIL = 3;
    // 图片压缩中 (READY->COMPRESSING->RUNNING->SUCCESS/FAIL)
    // 仅是内存中的状态，不会修改数据库中的值
    public static final int TASK_STATUS_COMPRESSING = 4;

    // 发送的类型
    // 好友邀请（添加好友发送认证、通话‘禁止陌生人来电’发送认证）
    public static final int NOTICE_TYPE_FRIEND_SEND = 1;
    // 图片文件分享
    public static final int NOTICE_TYPE_PHOTO_SEND = 2;
    // 视频文件分享
    public static final int NOTICE_TYPE_VEDIO_SEND = 3;
    // 名片分享
    public static final int NOTICE_TYPE_VCARD_SEND = 4;
    // 未接来电提醒
    public static final int NOTICE_TYPE_IPCALL_SEND = 5;
    // 通过回执（新朋友中点‘加为好友’、平板‘加好友’操作）
    public static final int NOTICE_TYPE_FEEEDBACK_SEND = 6;
    // 声音分享
    public static final int NOTICE_TYPE_AUDIO_SEND = 7;
    // 文字消息
    public static final int NOTICE_TYPE_TXT_SEND = 8;
    // 添加好友信息
    public static final int NOTICE_TYPE_DESCRIPTION = 9;

    // office 文件消息（目前支持 word, pdf）
    public static final int NOTICE_TYPE_ATTACHMENT_FILE = 16;

    public static final int NOTICE_TYPE_ARTICAL_SEND = 30;
    //聊天记录
    public static final int NOTICE_TYPE_CHATRECORD_SEND = 31;

    public static final int NOTICE_TYPE_REMIND_SEND = 13;// 群公告消息  @所有人消息

    public static final int NOTICE_TYPE_MANY_MSG_FORWARD = 14; //逐条转发、合并转发消息类型

    public static final int NOTICE_TYPE_REMIND_ONE_SEND = 15; //@xxx  具体@某个人消息

    //========================极会议集成添加===========================
    // 通话记录
    public static final int NOTICE_TYPE_RECORD = 10;
    // 会议邀请
    public static final int NOTICE_TYPE_MEETING_INVITE = 11;
    // 预约会议
    public static final int NOTICE_TYPE_MEETING_BOOK = 12;

    //附件  类型
    public static final int NOTICE_TYPE_URL = 20;//20160512 URL格式，目前只在收藏时使用，转消息发送时，需要转换成文本

    public static final int NOTICE_TYPE_FILE = 21;//20160512附件格式，目前只在收藏时使用

    private MDSAccountInfo userAccountInfo = null;

    private CollectionDao mCollectionDao = null;

    public static long PICTURE_COMPRESSION = IMConstant.DEFAULT_IMAGE_SEND_SIZE;

    public static int STRANGER_TYPE_REQUEST = 0;
    public static int STRANGER_TYPE_REPLAY = 1;
    private DtNoticesDao dtNoticedao;


    /***
     * 压缩图片（独立工作线程），完成后（不论成功与否）handler通知主线程
     *
     * @param uuid
     * @param filepath
     */
    private void compressImageThread(final String uuid, final String filepath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CustomLog.d(TAG, "compressImageThread,uuid=" + uuid + "|filepath=" + filepath);

                String outpath = getCompressFilePath(filepath);
                boolean success = CompressUtil.compressImage(filepath, outpath);
                CustomLog.d(TAG,
                    "CompressUtil.compressImage,outpath=" + outpath + "|success=" + success);

                Message msg = syncHandler.obtainMessage();
                msg.what = MSG_IMAGE_COMPRESSED;
                Bundle data = new Bundle();
                data.putBoolean("compress_success", success);
                data.putString("compress_uuid", uuid);
                data.putString("compress_srcpath", filepath);
                data.putString("compress_outpath", outpath);
                msg.setData(data);
                syncHandler.sendMessage(msg);
            }
        }
        ).start();
    }


    private String getCompressFilePath(String srcfilepath) {
        CustomLog.d(TAG, "getCompressFilePath begin,srcfilePaht" + srcfilepath);

        int index = srcfilepath.lastIndexOf(File.separator);
        String lastpart = srcfilepath.substring(index);
        String path = Environment.getExternalStorageDirectory()
            + File.separator + IMConstant.APP_ROOT_FOLDER
            + File.separator + FILE_COMPRESS_DIR
            + lastpart;
        CustomLog.d(TAG, "path:" + path);
        return path;
    }


    private void updateCompressPath2Bean(String uuid, String srcpath, String compressedpath) {
        CustomLog.d(TAG, "updateCompressPath2Bean begin,uuid:" + uuid + "|srcpath:" + srcpath +
            "|compressedpath:" + compressedpath);
        if (TextUtils.isEmpty(uuid)) {
            return;
        }
        if (fileTaskMap == null || !fileTaskMap.containsKey(uuid)) {
            return;
        }

        List<FileTaskBean> tempList = fileTaskMap.get(uuid);
        if (tempList == null || tempList.size() == 0) {
            return;
        }
        FileTaskBean bean = null;
        int length = tempList.size();
        for (int i = 0; i < length; i++) {
            bean = tempList.get(i);
            if (uuid.equals(bean.getUuid())
                && srcpath.equals(bean.getSrcUrl())) {
                bean.setCompressedPath(compressedpath);
                break;
            }
        }
    }


    private Handler syncHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch (msg.what) {
                case NO_SDCARD:
                    CustomLog.d(TAG, "NO_SDCARD,Toast:外部存储卡没有准备好，无法下载");
                    Toast.makeText(mcontext, "外部存储卡没有准备好，无法下载", Toast.LENGTH_SHORT).show();
                    //TODO:需要清理内存中的map数据
                    break;

                case MSG_IMAGE_COMPRESSED: {
                    CustomLog.d(TAG, "MSG_IMAGE_COMPRESSED");
                    Bundle data = msg.getData();
                    if (data != null) {
                        boolean success = data.getBoolean("compress_success");
                        String uuid = data.getString("compress_uuid");
                        String srcpath = data.getString("compress_srcpath");
                        String outpath = data.getString("compress_outpath");
                        if (success) {
                            updateCompressPath2Bean(uuid, srcpath, outpath);
                        }
                        startFirstValidFileTask(uuid);
                    }
                }
                break;

                case MSG_SUCCESS: {

                    Bundle data = msg.getData();
                    String uuid = data.getString("uuid");
                    String srcUrl = data.getString("srcUrl");
                    CustomLog.d(TAG, "MSG_SUCCESS，uuid:" + uuid + "|srcUrl:" + srcUrl);
                    FileTaskBean bean = findRecentlyRunningFileTask(uuid, srcUrl);
                    if (bean != null) {
                        bean.convertSuccessStringToResultUrl();
                        // 更改压缩后文件后缀名为空
                        if (bean.getType() == NOTICE_TYPE_PHOTO_SEND
                            && bean.getStatus() == TASK_STATUS_SUCCESS) {
                            String compressedpath = bean.getCompressedPath();
                            if (!TextUtils.isEmpty(compressedpath)) {
                                String newpath = compressedpath.replace(".",
                                    System.currentTimeMillis() + "");
                                File old = new File(compressedpath);
                                if (old.exists()) {
                                    old.renameTo(new File(newpath));
                                }
                            }
                        }

                        if (bean.getStatus() == TASK_STATUS_SUCCESS) {
                            //处理单独下载文件的情况
                            if (bean.isSingleDownload()) {
                                // updateTaskStatus(uuid, TASK_STATUS_SUCCESS, true);
                                //TODO:20141127 在新的消息需求中，每条消息只有一张图片或视频；
                                //此处暂可认为完全成功；（理想情况下，应该check下其他同记录中的taskbean）
                                // 仅保存body字段，不修改消息中的状态
                                updateBodybutTaskStatus(uuid);
                                CustomLog.d(TAG, "fileTaskMap.remove:" + uuid);
                                fileTaskMap.remove(uuid);
                                runTaskQueue.remove(getKeyString(srcUrl));
                                return;
                            }
                            if (bean.getIndex() + 1 == bean.getTotal_count()) {
                                if (!bean.isFrom()) {
                                    //发送的消息
                                    sendMessageBody(bean.getType(), uuid, srcUrl);
                                } else {
                                    //接收的消息
                                    updateTaskStatus(uuid, TASK_STATUS_SUCCESS,
                                        true);
                                    CustomLog.d(TAG, "fileTaskMap.remove:" + uuid);
                                    fileTaskMap.remove(uuid);
                                    runTaskQueue.remove(getKeyString(srcUrl));
                                }
                            } else {
                                startFirstValidFileTask(uuid);
                            }
                        } else {
                            // miaolk add 20140314
                            if (!TextUtils.isEmpty(bean.getErrorTip())) {
                                CustomLog.d(TAG, "Toast:" + bean.getErrorTip());
                                Toast.makeText(mcontext, bean.getErrorTip(),
                                    Toast.LENGTH_SHORT).show();
                            }

                            if (bean.isSingleDownload()) {
                                updateBodybutTaskStatus(uuid);
                            } else {
                                updateTaskStatus(uuid, TASK_STATUS_FAIL, true);
                            }

                            CustomLog.d(TAG, "fileTaskMap.remove:" + uuid);
                            fileTaskMap.remove(uuid);
                            runTaskQueue.remove(getKeyString(srcUrl));
                        }
                    }
                }
                break;
                case MSG_FAILURE: {
                    Bundle data = msg.getData();
                    String uuid = data.getString("uuid");
                    String srcUrl = data.getString("srcUrl");
                    CustomLog.d(TAG, "MSG_FAILURE,uuid:" + uuid + "|srcUrl:" + srcUrl);
                    FileTaskBean bean = findRecentlyRunningFileTask(uuid, srcUrl);
                    if (bean != null) {
                        // 下载失败的场景下，主动删除temp文件
                        String destFileName = bean.getResultUrl();
                        if (bean.isFrom() && !TextUtils.isEmpty(destFileName)) {
                            // 判断文件本地文件是否存在，
                            File file = new File(destFileName);
                            if (file != null && file.exists()) {
                                file.delete();
                                file = null;
                            }
                            bean.setResultUrl("");
                        }

                        bean.setStatus(TASK_STATUS_FAIL);
                        if (bean.isSingleDownload()) {
                            updateBodybutTaskStatus(uuid);
                        } else {
                            updateTaskStatus(uuid, TASK_STATUS_FAIL, true);
                        }
                        CustomLog.d(TAG, "fileTaskMap.remove:" + uuid);
                        fileTaskMap.remove(uuid);
                        runTaskQueue.remove(getKeyString(srcUrl));
                    }
                }
                break;
            }
        }
    };


    //
    public FileTaskManager(Context context) {
        noticedao = new NoticesDao(context);
        dtNoticedao = new DtNoticesDao(context);
        mcontext = context;
        initStorageDir();
        initCompressDir();
        initRecordDir();
        initTakePhotoDir();
        initVCFDir();
        userAccountInfo = AccountManager.getInstance(mcontext).getAccountInfo();
    }


    /**
     * @author: zhaguitao
     * @Title: updateRunningTask2Fail
     * @Description: 打开应用时，需要将执行中的消息状态改为失败，
     * 退出则认为停止任务，以便再次进入应用时，可以重新开始任务
     * @date: 2014-2-19 上午11:38:51
     */
    public void updateRunningTask2Fail() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.d("updateRunningTask2Fail");
                Cursor cursor = null;
                try {
                    cursor = noticedao.queryAllRunningNotices();
                    if (cursor != null && cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        int idIdx = cursor
                            .getColumnIndex(NoticesTable.NOTICE_COLUMN_ID);
                        String uuid = "";
                        do {
                            uuid = cursor.getString(idIdx);
                            // 正在执行状态的消息，状态更新为失败
                            updateTaskStatus(uuid, TASK_STATUS_FAIL, false);
                            removeMap(uuid);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    LogUtil.e("updateRunningTask2Fail", e);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                        cursor = null;
                    }
                }
            }
        }).start();
    }


    /***
     * 从数据库中找出该记录，转化成FileTaskBean,并启动首个filetask
     *
     * @param uuid
     * @param uiInterfaces
     */
    public void addTask(final String uuid, final ChangeUIInterface uiInterfaces) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                CustomLog.d("FileTaskManager", "addTask");
                Looper.prepare();
                executeTask(uuid, uiInterfaces);
                Looper.loop();
            }
        }).start();
    }


    /**
     * 从 HPU 数据库中找出该记录，转化成FileTaskBean,并启动首个filetask
     */
    public void addDTTask(final String uuid, final ChangeUIInterface uiInterfaces) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CustomLog.d("FileTaskManager", "addTask");
                Looper.prepare();
                executeDTTask(uuid, uiInterfaces);
                Looper.loop();
            }
        }).start();
    }


    /**
     * 执行诊疗任务
     */
    private synchronized boolean executeDTTask(String uuid, ChangeUIInterface uiInterfaces) {
        CustomLog.d("FileTaskManager", "executeDTTask  uuid : " + uuid);

        boolean error = false;

        boolean existed = false;
        List<FileTaskBean> templist = null;
        // 是否已经开启了该任务
        templist = findFileTasks(uuid);
        if (templist == null || templist.size() == 0) {
            // 没有开启的情况下，构建file task list
            templist = createDTFileTaskList(uuid, false);
        } else {
            existed = true;
        }
        if (templist == null || templist.size() == 0) {
            return false;
        }

        if (uiInterfaces != null) {
            for (FileTaskBean item : templist) {
                item.setChangui(uiInterfaces);
                item.setPauseUiChange(false);
            }
        }
        if (existed) {
            // 已开启的情况下，只需要把uiInterfaces 设置到file task中即可
            return true;
        }
        CustomLog.d(TAG, "fileTaskMap.put:" + uuid);
        fileTaskMap.put(uuid, templist);

        updateDTTaskStatus(uuid, TASK_STATUS_RUNNING, false);
        // 启动任务
        if (!error) {
            if (!templist.get(0).isFrom()) {
                error = sendDTSCIMMsg(uuid);
            } else {
                error = startFirstValidFileTask(uuid);
            }
        }
        // 如果启动失败，删除刚加入的filetasks
        if (!error) {
            CustomLog.d(TAG, "fileTaskMap.remove:" + uuid);
            fileTaskMap.remove(uuid);
        } else {
        }
        templist = null;
        return true;
    }


    /**
     * 绑定上传下载进度
     */
    public void setChgUIInterface(final String uuid,
                                  ChangeUIInterface uiInterfaces) {
        List<FileTaskBean> templist = findFileTasks(uuid);
        if (templist != null && templist.size() >= 0) {
            if (uiInterfaces != null) {
                for (FileTaskBean item : templist) {
                    CustomLog.d(TAG, "setChangui:" + uuid);
                    item.setChangui(uiInterfaces);
                    item.setPauseUiChange(false);
                }
            }
        }
    }


    private synchronized boolean executeTask(String uuid, ChangeUIInterface uiInterfaces) {
        CustomLog.d("FileTaskManager", "executeTask begin uuid:" + uuid);

        boolean error = false;

        boolean existed = false;
        List<FileTaskBean> templist = null;
        // 是否已经开启了该任务
        templist = findFileTasks(uuid);
        if (templist == null || templist.size() == 0) {
            // 没有开启的情况下，构建file task list
            templist = createFileTaskList(uuid, false);
        } else {
            existed = true;
        }
        if (templist == null || templist.size() == 0) {
            return false;
        }

        if (uiInterfaces != null) {
            for (FileTaskBean item : templist) {
                item.setChangui(uiInterfaces);
                item.setPauseUiChange(false);
            }
        }
        if (existed) {
            // 已开启的情况下，只需要把uiInterfaces 设置到file task中即可
            return true;
        }
        CustomLog.d(TAG, "fileTaskMap.put:" + uuid);
        fileTaskMap.put(uuid, templist);

        updateTaskStatus(uuid, TASK_STATUS_RUNNING, false);
        // 启动任务
        if (!error) {
            if (!templist.get(0).isFrom()) {
                // 如果是发送的消息
                error = sendSCIMMsg(uuid);
            } else {
                // 如果是接收的消息
                error = startFirstValidFileTask(uuid);
            }
        }
        // 如果启动失败，删除刚加入的filetasks
        if (!error) {
            CustomLog.d(TAG, "fileTaskMap.remove:" + uuid);
            fileTaskMap.remove(uuid);
        } else {

        }
        templist = null;
        return true;
    }


    /***
     * 根据UUID 和 url,启动单个文件的下载
     *
     * @param uuid
     * @param url
     * @param uiInterfaces 进度展示回调方法
     */
    public void addSingleFileDownloadTask(final String uuid, final String url,
                                          final boolean froceDownload, final ChangeUIInterface uiInterfaces) {
        CustomLog.d(TAG, "uuid=" + uuid + "|url=" + url);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                executeSingleFileTask(uuid, url, froceDownload, uiInterfaces);
                Looper.loop();
            }
        }).start();
    }


    private boolean executeSingleFileTask(String uuid, String url, boolean froceDownload,
                                          ChangeUIInterface uiInterfaces) {
        CustomLog.d(TAG, "uuid=" + uuid + "|url=" + url);

        boolean existed = false;
        List<FileTaskBean> templist = null;
        // 是否已经开启了该任务
        templist = findFileTasks(uuid);
        if (templist == null || templist.size() == 0) {
            // 没有开启的情况下，构建file task list
            if (isUDTMsg(uuid)) {
                templist = createDTFileTaskList(uuid, froceDownload);
            } else {
                templist = createFileTaskList(uuid, froceDownload);
            }
        } else {
            existed = true;
        }
        if (templist == null || templist.size() == 0) {
            return false;
        }

        // 找到该下载的bean对象
        FileTaskBean bean = null;
        for (FileTaskBean item : templist) {
            if (item.getSrcUrl().equals(url)) {
                bean = item;
                if (uiInterfaces != null) {
                    item.setChangui(uiInterfaces);
                    item.setPauseUiChange(false);
                }
                // 如果该文件的记录不是正在下载（existed==true 内存中有其map记录）
                // 而且该处于待下载状态，则设置单文件现在标志;
                if (!existed && item.getStatus() == TASK_STATUS_READY) {
                    item.setSingleDownload(true);
                }
            }
        }

        fileTaskMap.put(uuid, templist);
        templist = null;
        // 启动任务
        if (bean != null
            && (bean.getStatus() == TASK_STATUS_READY || bean.getStatus() == TASK_STATUS_FAIL)) {
            DownFileRequestCallBack callback = new DownFileRequestCallBack(
                bean, syncHandler);
            download(bean.getSrcUrl(), bean.getUuid(), callback);
            bean.setStatus(TASK_STATUS_RUNNING);
        }

        return true;
    }


    /**
     * @author: zhaguitao
     * @Title: delTaskAndFile
     * @Description: 停止任务，删除下载的文件，删除消息
     * @date: 2014-1-15 下午5:55:04
     */
    public void cancelTaskAndDelFile(final String uuid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CustomLog.d(TAG, "cancelTaskAndDelFile begin");
                List<FileTaskBean> templist = findFileTasks(uuid);
                if (templist == null || templist.size() == 0) {
                    templist = createFileTaskList(uuid, false);
                } else {
                    for (FileTaskBean bean : templist) {
                        if (bean.getStatus() == TASK_STATUS_RUNNING) {
                            // 若任务已启动，则先取消任务
                            bean.setStatus(TASK_STATUS_FAIL);
                            String key = getKeyString(bean.getSrcUrl());
                            HttpHandler runhandler = runTaskQueue.get(key);
                            if (runhandler != null) {
                                runhandler.cancel(true);
                            }
                            runTaskQueue.remove(key);
                        }
                    }
                }

                if (templist != null && templist.size() > 0) {
                    for (FileTaskBean bean : templist) {
                        delTaskFile(bean);
                    }
                }

                // 删除消息
                int count = noticedao.deleteNotice(uuid);
                boolean isSuccess = false;
                if (count > 0) {
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }
                CustomLog.d(TAG, "cancelTaskAndDelFile,删除消息:" + uuid + "|" + isSuccess);
            }
        }).start();
    }


    /**
     * @author: zhaguitao
     * @Title: delTaskFile
     * @Description: 删除任务文件
     * @date: 2014-1-15 下午7:23:13
     */
    private void delTaskFile(FileTaskBean bean) {
        String localFilePath = "";
        int type = bean.getType();
        switch (type) {
            case NOTICE_TYPE_VEDIO_SEND:
            case NOTICE_TYPE_VCARD_SEND:
            case NOTICE_TYPE_PHOTO_SEND:
            case NOTICE_TYPE_AUDIO_SEND:
                if (!bean.isFrom()) {
                    // 发送消息的场合
                    localFilePath = bean.getSrcUrl();
                } else {
                    // 接收消息的场合
                    localFilePath = bean.getResultUrl();
                }
                break;
            default:
                break;
        }

        CustomLog.d(TAG, "type:" + type + "|localFilePath:" + localFilePath);
        final File file = new File(localFilePath);
        if (file.exists()) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // 文件在任务停止2s后删除，以防任务还未停止，文件还在读写，无法删除
                    boolean success = file.delete();
                    CustomLog.d(TAG, "删除文件:"
                        + file.getAbsolutePath() + "|" + success);
                }
            }, 2000);
        }
    }


    /***
     * 在外面的列表中滚动或删除时调用
     *
     * @param uuid
     */
    public void cancelTask(final String uuid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                cancelRunningTask(uuid);
                Looper.loop();
            }
        }).start();
    }


    /***
     * 从FileTaskBean list中找到正在运行的filetask,获得srcUrl
     * 由srcUrl转成key，从runTaskQueue中找到handler,取消任务
     * 回写已经操作的结果到本地数据库body字段中
     *
     * @param uuid
     * @return
     */
    private boolean cancelRunningTask(String uuid) {
        CustomLog.d(TAG, "cancelRunningTask begin uuid:" + uuid);

        FileTaskBean bean = null;
        // 从FileTaskBean list中找到正在运行的filetask,
        List<FileTaskBean> templist = findFileTasks(uuid);
        if (templist == null || templist.size() == 0) {
            return false;
        }
        int length = templist.size();
        int i = 0;
        for (i = 0; i < length; i++) {
            bean = templist.get(i);
            if (bean.getStatus() != TASK_STATUS_SUCCESS) {
                break;
            }
        }
        if (i < length) {
            if (bean.getStatus() == TASK_STATUS_RUNNING) {
                bean.setStatus(TASK_STATUS_FAIL);
                // 由srcUrl转成key，从runTaskQueue中找到handler,取消任务
                String key = getKeyString(bean.getSrcUrl());
                HttpHandler runhandler = runTaskQueue.get(key);
                if (runhandler != null) {
                    runhandler.cancel(true);
                }
                runTaskQueue.remove(key);
            }
            bean.setStatus(TASK_STATUS_FAIL);
            // 回写已经操作的结果到本地数据库body字段中
            updateTaskStatus(uuid, TASK_STATUS_FAIL, true);
            CustomLog.d(TAG, "fileTaskMap.remove:" + uuid);
            fileTaskMap.remove(uuid);
            return true;

        } else {
            if (bean.getStatus() == TASK_STATUS_SUCCESS) {
                // 全部成功处理完成，不手动改变其为失败状态
                return false;
            } else {
                if (bean.getStatus() == TASK_STATUS_RUNNING) {
                    bean.setStatus(TASK_STATUS_FAIL);
                    String key = getKeyString(bean.getSrcUrl());
                    HttpHandler runhandler = runTaskQueue.get(key);
                    if (runhandler != null) {
                        runhandler.cancel(true);
                    }
                    runTaskQueue.remove(key);
                }
                bean.setStatus(TASK_STATUS_FAIL);
                updateTaskStatus(uuid, TASK_STATUS_FAIL, true);
                CustomLog.d(TAG, "fileTaskMap.remove:" + uuid);
                fileTaskMap.remove(uuid);
                return true;
            }
        }
    }


    /***
     * 解析body中的JSON串， 把动态消息的一条记录转换为FileTaskBean list
     *
     * @param uuid
     * @param body
     * @param type
     * @param from true:接收到的消息;
     *             false:发送的消息
     * @return
     */
    public synchronized List<FileTaskBean> getFileTaskListByBody(String uuid, String body,
                                                                 int type, boolean from) {

        CustomLog.d(TAG, "uuid:" + uuid
            + "|body:" + body
            + "|type:" + type
            + "|from:" + from);

        if (!TextUtils.isEmpty(body)) {
            List<FileTaskBean> tasklist = null;
            switch (type) {

                case NOTICE_TYPE_IPCALL_SEND:
                case NOTICE_TYPE_FRIEND_SEND:
                    break;
                case NOTICE_TYPE_FILE:
                case NOTICE_TYPE_TXT_SEND:
                case NOTICE_TYPE_CHATRECORD_SEND:
                case NOTICE_TYPE_MEETING_INVITE:
                case NOTICE_TYPE_MEETING_BOOK:
                case NOTICE_TYPE_VEDIO_SEND:
                case NOTICE_TYPE_VCARD_SEND:
                case NOTICE_TYPE_PHOTO_SEND:
                case NOTICE_TYPE_AUDIO_SEND:
                case NOTICE_TYPE_ARTICAL_SEND:
                case NOTICE_TYPE_REMIND_SEND:
                case NOTICE_TYPE_REMIND_ONE_SEND: {
                    try {
                        JSONArray array = new JSONArray(body);
                        JSONObject obj = null;
                        tasklist = new ArrayList<FileTaskBean>();
                        int length = array.length();
                        for (int i = 0; i < length; i++) {
                            FileTaskBean filetask = new FileTaskBean();
                            filetask.setFrom(from);
                            obj = array.getJSONObject(i);
                            filetask.setRawBodyItemData(obj.toString());

                            String local = obj.optString("localUrl");
                            String romote = obj.optString("remoteUrl");
                            long filesize = obj.optLong("size");
                            String thumb = obj.optString("thumbnail");
                            String compressedpath = obj.optString("compressPath");
                            //						boolean oversize = obj.optBoolean("overSize");
                            filetask.setCompressedPath(compressedpath);
                            //						filetask.setOverSized(oversize);
                            filetask.setFilesize(filesize);
                            if (!from) {
                                // 发送的消息，本地路径为源，服务器端的URL为结果
                                filetask.setSrcUrl(local);
                                filetask.setResultUrl(romote);
                            } else {
                                // 接收的消息，服务器端的URL为源，本地路径为结果
                                filetask.setSrcUrl(romote);
                                filetask.setResultUrl(local);
                            }
                            filetask.setThumbnailUrl(thumb);
                            filetask.setUuid(uuid);
                            filetask.setType(type);
                            filetask.setIndex(i);
                            filetask.setTotal_count(length);
                            if (!from) {
                                // 发送的消息，如果有服务器的URL则认为成功过，设置为SUCCESS
                                // 否则设置为READY，需要上传服务器
                                if (NOTICE_TYPE_TXT_SEND == type ||
                                    NOTICE_TYPE_MEETING_INVITE == type ||
                                    NOTICE_TYPE_MEETING_BOOK == type || NOTICE_TYPE_FILE == type
                                    || NOTICE_TYPE_CHATRECORD_SEND == type
                                    || NOTICE_TYPE_ARTICAL_SEND == type
                                    || NOTICE_TYPE_REMIND_SEND == type) {
                                    filetask.setStatus(TASK_STATUS_SUCCESS);
                                } else {
                                    if (TextUtils.isEmpty(romote)) {
                                        filetask.setStatus(TASK_STATUS_READY);
                                    } else {
                                        filetask.setStatus(TASK_STATUS_SUCCESS);
                                    }
                                }
                            } else {
                                // 接收到的消息
                                if (NOTICE_TYPE_TXT_SEND == type ||
                                    NOTICE_TYPE_MEETING_INVITE == type ||
                                    NOTICE_TYPE_MEETING_BOOK == type || NOTICE_TYPE_FILE == type
                                    || NOTICE_TYPE_CHATRECORD_SEND == type
                                    || NOTICE_TYPE_ARTICAL_SEND == type
                                    || NOTICE_TYPE_REMIND_SEND == type) {
                                    filetask.setStatus(TASK_STATUS_SUCCESS);
                                } else {
                                    if (!TextUtils.isEmpty(local)) {
                                        String filepath = getLocalPathFromURL(romote,
                                            filetask.getUuid(), false);
                                        File file = new File(filepath);
                                        if (file != null && file.exists()) {
                                            filetask.setResultUrl(filepath);
                                            // 接收到的消息，本地文件路径不为空，且文件存在
                                            filetask.setStatus(TASK_STATUS_SUCCESS);
                                        } else {
                                            // 接收到的消息，本地文件路径不为空，但文件不存在
                                            // 需要重新下载
                                            String tmpfilepath = getLocalPathFromURL(
                                                romote, filetask.getUuid(), true);
                                            filetask.setResultUrl(tmpfilepath);
                                            filetask.setStatus(TASK_STATUS_READY);
                                        }
                                    } else {
                                        // 接收到的消息,本地文件路径为空,以temp为后缀的临时文件
                                        // 进行下载
                                        filetask.setResultUrl(getLocalPathFromURL(romote,
                                            filetask.getUuid(), true));
                                        filetask.setStatus(TASK_STATUS_READY);
                                    }
                                }
                            }

                            tasklist.add(filetask);
                        }
                    } catch (JSONException e) {
                        CustomLog.e(TAG, "JSONException" + e.toString());
                    }
                }
                break;

            }
            return tasklist;
        }
        return null;
    }


    /***
     * 根据UUID，把动态消息的一条记录转换为FileTaskBean list
     *
     * @param uuid
     * @return
     */
    public synchronized List<FileTaskBean> createFileTaskList(String uuid, boolean froceDownload) {
        CustomLog.i(TAG, "createFileTaskList,uudi:" + uuid);

        NoticesBean bean = noticedao.getNoticeById(uuid);
        if (bean != null) {
            boolean from = false;
            String own = AccountManager.getInstance(mcontext).getAccountInfo().nube;
            if (bean.getSender().endsWith(own)) {
                from = false;
            } else {
                from = true;
            }

            if (froceDownload) {
                from = true;
            }
            // 已发送成功的消息不再发送；
            // 但接收到的消息，可能因为已下载的图片、视频被删除;
            // 用户又再次查看，需要再次加入任务经行下载
            if (!from && bean.getStatus() == TASK_STATUS_SUCCESS) {
                CustomLog.d(TAG, "createFileTaskList 发送任务已成功，无需再次发送");
                return null;
            }

            return getFileTaskListByBody(uuid, bean.getBody(), bean.getType(), from);
        }
        return null;
    }


    /***
     * 根据UUID，把动态消息的一条记录转换为FileTaskBean list
     *
     * @param uuid
     * @return
     */
    public synchronized List<FileTaskBean> createDTFileTaskList(String uuid, boolean froceDownload) {
        CustomLog.d(TAG, "createDTFileTaskList begin,uudi:" + uuid);
        NoticesBean bean = dtNoticedao.getNoticeById(uuid);
        if (bean != null) {
            boolean from = false;
            String own = AccountManager.getInstance(mcontext).getAccountInfo().nube;
            if (bean.getSender().endsWith(own)) {
                from = false;
            } else {
                from = true;
            }

            if (froceDownload) {
                from = true;
            }
            // 已发送成功的消息不再发送；
            // 但接收到的消息，可能因为已下载的图片、视频被删除;
            // 用户又再次查看，需要再次加入任务经行下载
            if (!from && bean.getStatus() == TASK_STATUS_SUCCESS) {
                CustomLog.d(TAG, "createDTFileTaskList 发送任务已成功，无需再次发送");
                return null;
            }

            return getFileTaskListByBody(uuid, bean.getBody(), bean.getType(), from);
        }
        return null;
    }


    /***
     * 启动一个需要下载或上传的FileTaskBean
     *
     * @param uuid
     * @return
     */
    private synchronized boolean startFirstValidFileTask(String uuid) {
        CustomLog.d(TAG, "startFirstValidFileTask() | uuid:" + uuid);

        // 找到第一个需要启动的 filetask, 启动http接口进行网络连接
        FileTaskBean bean = findFirstValidFileTask(uuid);
        if (bean != null) {
            // 找到的是最后一个文件，并且是成功状态（文件全下载完成或全部上传完毕，只是任务的消息状态为失败）
            if ((bean.getStatus() == TASK_STATUS_SUCCESS)
                && (bean.getIndex() + 1 == bean.getTotal_count())) {
                Message msg = syncHandler.obtainMessage();
                msg.what = FileTaskManager.MSG_SUCCESS;
                Bundle data = new Bundle();
                data.putString("uuid", bean.getUuid());
                data.putString("srcUrl", bean.getSrcUrl());
                msg.setData(data);
                syncHandler.sendMessage(msg);
                return true;
            }

            switch (bean.getType()) {
                case NOTICE_TYPE_VEDIO_SEND: {
                    CustomLog.d(TAG, "NOTICE_TYPE_VEDIO_SEND");
                    if (bean.isFrom()) {
                        DownFileRequestCallBack callback = new DownFileRequestCallBack(
                            bean, syncHandler);
                        download(bean.getSrcUrl(), bean.getUuid(), callback);
                        bean.setStatus(TASK_STATUS_RUNNING);
                    } else {
                        CommomFileRequestCallBack callback = new CommomFileRequestCallBack(
                            bean, syncHandler);
                        upload(bean.getSrcUrl(), "", callback);
                        bean.setStatus(TASK_STATUS_RUNNING);
                    }
                }
                break;
                case NOTICE_TYPE_VCARD_SEND:
                case NOTICE_TYPE_AUDIO_SEND: {
                    if (bean.isFrom()) {
                        DownFileRequestCallBack callback = new DownFileRequestCallBack(
                            bean, syncHandler);
                        download(bean.getSrcUrl(), bean.getUuid(), callback);
                        bean.setStatus(TASK_STATUS_RUNNING);
                    } else {
                        CommomFileRequestCallBack callback = new CommomFileRequestCallBack(
                            bean, syncHandler);
                        upload(bean.getSrcUrl(), "", callback);
                        bean.setStatus(TASK_STATUS_RUNNING);
                    }
                }
                break;
                case NOTICE_TYPE_PHOTO_SEND: {
                    if (bean.isFrom()) {
                        DownFileRequestCallBack callback = new DownFileRequestCallBack(
                            bean, syncHandler);
                        download(bean.getSrcUrl(), bean.getUuid(), callback);
                        bean.setStatus(TASK_STATUS_RUNNING);
                    } else {
                        if (bean.getFilesize() > PICTURE_COMPRESSION) {
                            // 需要增加个压缩过程
                            String compressedpath = bean.getCompressedPath();
                            if (TASK_STATUS_COMPRESSING == bean.getStatus()
                                && TextUtils.isEmpty(compressedpath)) {
                                CommomFileRequestCallBack callback = new CommomFileRequestCallBack(
                                    bean, syncHandler);
                                upload(bean.getSrcUrl(), "", callback);
                                bean.setStatus(TASK_STATUS_RUNNING);
                                return true;
                            }

                            // 先判断源文件是否存在，不存在，则不需要压缩
                            if (TextUtils.isEmpty(compressedpath)
                                && initCompressDir() && isValidFilePath(bean.getSrcUrl())) {
                                compressImageThread(uuid, bean.getSrcUrl());
                                bean.setStatus(TASK_STATUS_COMPRESSING);
                            } else {
                                File file = new File(compressedpath);
                                if (file != null && file.exists()) {
                                    CommomFileRequestCallBack callback
                                        = new CommomFileRequestCallBack(
                                        bean, syncHandler);
                                    upload(bean.getSrcUrl(), compressedpath,
                                        callback);
                                    bean.setStatus(TASK_STATUS_RUNNING);
                                } else {
                                    // 先判断源文件是否存在，不存在，则不需要压缩
                                    if (initCompressDir() && isValidFilePath(bean.getSrcUrl())) {
                                        compressImageThread(uuid, bean.getSrcUrl());
                                        bean.setStatus(TASK_STATUS_COMPRESSING);
                                    } else {
                                        CommomFileRequestCallBack callback
                                            = new CommomFileRequestCallBack(
                                            bean, syncHandler);
                                        upload(bean.getSrcUrl(), compressedpath,
                                            callback);
                                        bean.setStatus(TASK_STATUS_RUNNING);
                                    }
                                }
                            }

                        } else {
                            CommomFileRequestCallBack callback = new CommomFileRequestCallBack(
                                bean, syncHandler);
                            upload(bean.getSrcUrl(), "", callback);
                            bean.setStatus(TASK_STATUS_RUNNING);
                        }
                    }
                }
                break;
            }
            return true;
        }
        return false;
    }


    /***
     * 从FileTaskBean list数据构建动态消息的body字段
     *
     * @param tempList
     * @return
     */
    public String createBodyStringFromList(List<FileTaskBean> tempList) {
        CustomLog.i(TAG, "createBodyStringFromList()");

        if (tempList == null || tempList.size() == 0) {
            return "";
        }
        FileTaskBean bean = tempList.get(0);
        int type = bean.getType();
        switch (type) {
            case NOTICE_TYPE_FILE: {
                JSONArray body = new JSONArray();
                int length = tempList.size();
                for (int i = 0; i < length; i++) {
                    bean = tempList.get(i);
                    JSONObject object = null;
                    try {
                        object = new JSONObject(bean.getRawBodyItemData());
                    } catch (JSONException e) {
                        CustomLog.e(TAG, "JSONException" + e.toString());
                    }

                    if (object != null) {
                        body.put(object);
                    }
                }

                if (body.length() > 0) {
                    return body.toString();
                }
            }
            break;
            case NOTICE_TYPE_MEETING_INVITE: {
                JSONArray body = new JSONArray();
                int length = tempList.size();
                for (int i = 0; i < length; i++) {
                    bean = tempList.get(i);
                    JSONObject object = null;
                    try {
                        object = new JSONObject(bean.getRawBodyItemData());
                        object.put("showMeeting", false);
                    } catch (JSONException e) {
                        CustomLog.e(TAG, "JSONException" + e.toString());
                    }

                    if (object != null) {
                        body.put(object);
                    }
                }

                if (body.length() > 0) {
                    return body.toString();
                }
            }
            break;
            case NOTICE_TYPE_FRIEND_SEND:
            case NOTICE_TYPE_IPCALL_SEND:
                break;
            case NOTICE_TYPE_VEDIO_SEND:
            case NOTICE_TYPE_VCARD_SEND:
            case NOTICE_TYPE_PHOTO_SEND:
            case NOTICE_TYPE_AUDIO_SEND: {
                JSONArray body = new JSONArray();
                int length = tempList.size();
                for (int i = 0; i < length; i++) {
                    bean = tempList.get(i);
                    JSONObject object = null;
                    try {
                        object = new JSONObject(bean.getRawBodyItemData());
                        if (!bean.isFrom()) {
                            //发送的消息 需要更新romteUrl、compressPath和thumbnail
                            object.put("remoteUrl", bean.getResultUrl());
                            object.put("compressPath", bean.getCompressedPath());
                            object.put("thumbnail", bean.getThumbnailUrl());
                        } else {
                            //接收的消息 仅需要更新 localUrl
                            String result = bean.getResultUrl();
                            if (bean.getStatus() == TASK_STATUS_SUCCESS) {
                                if (!TextUtils.isEmpty(result) && result.endsWith(".temp")) {
                                    result.replace(".temp", "");
                                }
                            }
                            object.put("localUrl", result);
                            object.put("size", bean.getFilesize());
                        }
                    } catch (JSONException e) {
                        CustomLog.e(TAG, "JSONException" + e.toString());
                    }

                    if (object != null) {
                        body.put(object);
                    }
                }

                if (body.length() > 0) {
                    return body.toString();
                }
            }
            break;
        }

        return "";
    }


    private String createBodyString(String uuid) {
        CustomLog.d(TAG, "createBodyString(), uuid:" + uuid);
        // 从FileTaskBean list中找到同uuid的对象， 组成JSON ARRAY
        // 再tostring(),得到需要的对象
        List<FileTaskBean> tempList = findFileTasks(uuid);
        return createBodyStringFromList(tempList);
    }


    /***
     * 找到该记录中最近运行的FileTaskBean
     *
     * @param uuid   消息UUID
     * @param srcUrl FileTaskBean的源地址
     * @return
     */
    private FileTaskBean findRecentlyRunningFileTask(String uuid, String srcUrl) {
        CustomLog.d(TAG, "findRecentlyRunningFileTask begin ,uuid:" + uuid + "srcUrl:" + srcUrl);
        if (TextUtils.isEmpty(uuid)) {
            return null;
        }
        if (fileTaskMap == null || !fileTaskMap.containsKey(uuid)) {
            return null;
        }
        List<FileTaskBean> templist = fileTaskMap.get(uuid);
        if (templist == null || templist.size() == 0) {
            return null;
        }
        FileTaskBean bean = null;
        int length = templist.size();
        for (int i = 0; i < length; i++) {
            bean = templist.get(i);
            if (uuid.equals(bean.getUuid())) {
                if (srcUrl.equals(bean.getSrcUrl())) {
                    break;
                }
            }
        }

        return bean;
    }


    private FileTaskBean findFirstValidFileTask(String uuid) {
        CustomLog.d(TAG, "findFirstValidFileTask uuid:" + uuid);

        if (TextUtils.isEmpty(uuid)) {
            return null;
        }
        if (fileTaskMap == null || !fileTaskMap.containsKey(uuid)) {
            return null;
        }
        List<FileTaskBean> templist = fileTaskMap.get(uuid);
        if (templist == null || templist.size() == 0) {
            return null;
        }
        FileTaskBean bean = null;
        int length = templist.size();
        for (int i = 0; i < length; i++) {
            bean = templist.get(i);
            if (uuid.endsWith(bean.getUuid())) {
                if (bean.getStatus() != TASK_STATUS_SUCCESS
                    && (TextUtils.isEmpty(bean.getResultUrl()) || bean
                    .getResultUrl().endsWith(".temp"))) {
                    break;
                }
            }
        }

        return bean;
    }


    public List<FileTaskBean> findFileTasks(String uuid) {
        CustomLog.d("FileTaskManager", "findFileTasks begin,uuid:" + uuid);
        if (TextUtils.isEmpty(uuid)) {
            return null;
        }
        if (fileTaskMap == null || !fileTaskMap.containsKey(uuid)) {
            return null;
        }
        List<FileTaskBean> templist = fileTaskMap.get(uuid);
        if (templist == null || templist.size() == 0) {
            return null;
        } else {
            return templist;
        }

    }


    /***
     * 根据UUID 更新该条记录的状态
     *
     * @param uuid
     * @param status
     * @param change_body 是否更新body字段
     * @return
     */
    private boolean updateTaskStatus(String uuid, int status,
                                     boolean change_body) {
        CustomLog.d(TAG,
            "updateTaskStatus() , | uuid:" + uuid + " | status:" + status + " | change_body:" +
                change_body);
        // 根据uuid更改记录中body 和 status 字段
        if (TextUtils.isEmpty(uuid)) {
            return false;
        }
        if (change_body) {
            String body = createBodyString(uuid);
            CustomLog.d(TAG, "body:" + body);
            if (TextUtils.isEmpty(body)) {
                int count = noticedao.updateNotice(uuid, status);
                if (count > 0) {
                    return true;
                }
            } else {
                int count = noticedao.updateNotice(uuid, body, status);
                if (count > 0) {
                    return true;
                }
            }
        } else {
            int count = noticedao.updateNotice(uuid, status);
            if (count > 0) {
                return true;
            }
        }
        return false;
    }


    /**
     * 根据UUID 更新该条记录的状态的body字段，不修改状态
     */
    public boolean updateBodybutTaskStatus(String uuid) {
        CustomLog.d(TAG, "updateBodybutTaskStatus() ,uuid:" + uuid);

        // 根据uuid更改记录中body 和 status 字段
        if (TextUtils.isEmpty(uuid)) {
            return false;
        }

        String body = createBodyString(uuid);
        CustomLog.d(TAG, "body:" + body);
        if (!TextUtils.isEmpty(body)) {
            int count = 0;
            if (noticedao.getNoticeById(uuid) != null) {
                count = noticedao.updateNotice(uuid, body);
            } else {
                count = dtNoticedao.updateNotice(uuid, body);
            }

            if (count > 0) {
                return true;
            }
        }

        return false;
    }


    /**
     * HPU 需求，根据UUID 更新该条记录的状态的body字段，不修改状态
     */
    public boolean updateDTBodybutTaskStatus(String uuid) {
        CustomLog.d(TAG, "updateBodybutTaskStatus begin,uuid:" + uuid);
        // 根据uuid更改记录中body 和 status 字段
        if (TextUtils.isEmpty(uuid)) {
            return false;
        }

        String body = createBodyString(uuid);
        CustomLog.d(TAG, "body:" + body);
        if (!TextUtils.isEmpty(body)) {
            int count = dtNoticedao.updateNotice(uuid, body);
            if (count > 0) {
                return true;
            }
        }

        return false;
    }


    public boolean updateMeetingShowFlag(String uuid) {
        CustomLog.d(TAG, "updateMeetingShowFlag() | uuid : " + uuid);
        // 根据uuid更改记录中body 和 status 字段
        if (TextUtils.isEmpty(uuid)) {
            return false;
        }

        NoticesBean bean = noticedao.getNoticeById(uuid);
        if (bean != null && bean.getType() == NOTICE_TYPE_MEETING_INVITE) {
            String body = createBodyString(uuid);
            CustomLog.d(TAG, "body:" + body);
            if (!TextUtils.isEmpty(body)) {
                int count = noticedao.updateNotice(uuid, body);
                if (count > 0) {
                    return true;
                }
            }
        }
        return false;
    }


    public void upload(String srcPath, String compressedpath,
                       CommomFileRequestCallBack callback) {
        CustomLog.d(TAG, "upload begin,srcPaht:" + srcPath + "|compressedpath:" + compressedpath);
        File file = null;
        if (TextUtils.isEmpty(compressedpath)) {
            if (TextUtils.isEmpty(srcPath)) {
                CustomLog.d(TAG, "download srcPath is empty");
                //TODO:?
                return;
            }

            file = new File(srcPath);
            if (!file.exists()) {
                CustomLog.d(TAG, "upload srcPath is not exists:" + srcPath);
                // TODO:在文件不存在时，上传逻辑在本处终止，还是由http发现抛fail信息终止？
                // 暂由http处理 miaolk marked
                // return;
            }
        } else {
            file = new File(compressedpath);
            if (file != null && !file.exists()) {
                CustomLog.d(TAG, "compressedpath is not exists:" + compressedpath);
                // TODO:在文件不存在时，上传逻辑在本处终止，还是由http发现抛fail信息终止？
                file = new File(srcPath); // 尝试传原图至服务器
            }
        }
        RequestParams params = new RequestParams();
        params.addBodyParameter("image", file);
        HttpUtils http = new HttpUtils();
        http.configTimeout(120 * 1000);

        HttpHandler handler = null;

        String key = getKeyString(srcPath);
        runTaskQueue.put(key, handler);
    }


    public void download(String srcUrl, String uuid, DownFileRequestCallBack callback) {
        CustomLog.d(TAG, "download begin,srcUrl:" + srcUrl);
        if (!initStorageDir()) {
            if (syncHandler != null) {
                syncHandler.sendEmptyMessage(NO_SDCARD);
            }
            return;
        }

        // 由下载URL 得到本地存储的路径
        String destFileName = getLocalPathFromURL(srcUrl, uuid, true);
        if (TextUtils.isEmpty(destFileName)) {
            CustomLog.d(TAG, "download create temp destFileName is empty");
            return;
        }
        // 判断文件本地文件是否存在，并返回已有文件大小
        boolean isresume = false;
        //TODO:20141125 不在支持断点续传
        File file = new File(destFileName);
        if (file != null && file.exists()) {
            file.delete();
            file = null;
        }

        // 参数
        RequestParams params = null;
        HttpUtils http = new HttpUtils();
        http.configTimeout(120 * 1000);
        HttpHandler handler = http.download(srcUrl, params, destFileName,
            isresume, callback);
        String key = getKeyString(srcUrl);
        runTaskQueue.put(key, handler);
    }


    public static boolean initRecordDir() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment
            .getExternalStorageState())) {
            return false;
        }
        String dirpath = Environment.getExternalStorageDirectory()
            + File.separator + APP_ROOT_FOLDER
            + File.separator + FILE_RECORD_DIR;
        File file = new File(dirpath);
        if (file.exists()) {
            return true;
        } else {
            return file.mkdirs();
        }
    }
    //


    /**
     * 获得录制文件保存的路径；
     * 当没有SD卡或SDK卡没有初始化好、以及创建文件夹失败的情况下，返回空
     */
    public static String getRecordDir() {
        if (!initRecordDir()) {
            return "";
        }

        String dirpath = Environment.getExternalStorageDirectory()
            + File.separator + APP_ROOT_FOLDER
            + File.separator + FILE_RECORD_DIR;
        return dirpath;
    }


    private boolean initCompressDir() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment
            .getExternalStorageState())) {
            return false;
        }
        String dirpath = Environment.getExternalStorageDirectory()
            + File.separator + IMConstant.APP_ROOT_FOLDER
            + File.separator + FILE_COMPRESS_DIR;
        File file = new File(dirpath);
        if (file.exists()) {
            return true;
        } else {
            return file.mkdirs();
        }
    }


    public static String getVCFDir() {
        if (!initVCFDir()) {
            return "";
        }

        String dirpath = Environment.getExternalStorageDirectory()
            + File.separator + IMConstant.APP_ROOT_FOLDER
            + File.separator + FILE_VCARD_DIR;
        return dirpath;
    }


    private static boolean initVCFDir() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment
            .getExternalStorageState())) {
            return false;
        }
        String dirpath = Environment.getExternalStorageDirectory()
            + File.separator + IMConstant.APP_ROOT_FOLDER
            + File.separator + FILE_VCARD_DIR;
        File file = new File(dirpath);
        if (file.exists()) {
            return true;
        } else {
            return file.mkdirs();
        }
    }


    private boolean initStorageDir() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment
            .getExternalStorageState())) {
            return false;
        }
        String dirpath = Environment.getExternalStorageDirectory()
            + File.separator + IMConstant.APP_ROOT_FOLDER
            + File.separator + FILE_DOWNLOAD_DIR;
        File file = new File(dirpath);
        if (file.exists()) {
            return true;
        } else {
            return file.mkdirs();
        }
    }


    public String getLocalPathFromURL(String url, String uuid, boolean tempfile) {
        CustomLog.d(TAG, "getLocalPathFromURL begin,url:" + url + "|tempfile:" + tempfile);
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        int index = url.lastIndexOf("/");
        if (index == -1) {
            return "";
        }
        String lastpart = url.substring(index);

        String path = Environment.getExternalStorageDirectory()
            + File.separator + IMConstant.APP_ROOT_FOLDER
            + File.separator + FILE_DOWNLOAD_DIR;
        String threadId = noticedao.getThreadIdById(uuid);
        if (TextUtils.isEmpty(threadId)) {
            threadId = dtNoticedao.getThreadIdById(uuid);
        }
        if (!TextUtils.isEmpty(threadId)) {
            String tmppath = path + File.separator + threadId;
            File file = new File(tmppath);
            boolean succ = false;
            if (!file.exists()) {
                succ = file.mkdirs() || file.isDirectory();
            } else {
                succ = true;
            }
            if (succ) {
                path = tmppath;
            }
        }
        path = path + lastpart;
        if (tempfile) {
            path = path + ".temp";
        }
        CustomLog.d(TAG, "path:" + path);

        return path;
    }


    private String getKeyString(String pathOrUrl) {
        CustomLog.d(TAG, "getKeyString begin,pathOrUrl:" + pathOrUrl);
        String key = "";
        String lastpart = "";
        if (!TextUtils.isEmpty(pathOrUrl)) {
            // 找到连接或路径的最后一段
            if (pathOrUrl.startsWith("http:")) {
                int index = pathOrUrl.lastIndexOf("/");
                lastpart = pathOrUrl.substring(index + 1);
            } else {
                int index = pathOrUrl.lastIndexOf(File.separator);
                lastpart = pathOrUrl.substring(index + 1);
            }
            // 去除文件名中的点号
            key = lastpart.replace(".", "");
        }
        CustomLog.d(TAG, "getKeyString end,key:" + key);
        return key;
    }


    /**
     * 转发消息：根据uuid查出要转发的本地的消息
     * 以此为基础构建出一条新的记录，保存到数据库中；
     * 再发送消息，成功后，在SIP短消息通知对方立即接收
     *
     * @param receiver 接收对象的视频号（多个视频号用分号分割）
     * @param uuid 要转发的消息的uuid
     * @return 数据库中新记录的uuid; 返回空串，则表明参数为空 或 插入数据记录失败
     */
    public String forwardMessage(String receiver, String uuid) {

        LogUtil.begin("receiver:" + receiver
            + "|uuid:" + uuid);

        if (TextUtils.isEmpty(receiver)) {
            LogUtil.d("receiver 为空");
            return "";
        }

        if (TextUtils.isEmpty(uuid)) {
            LogUtil.d("uuid 为空");
            return "";
        }

        String newItemuuid = buildForwardRecord(receiver, uuid);

        if (TextUtils.isEmpty(newItemuuid)) {
            LogUtil.d("newItemuuid 为空,构建记录失败");
            return "";
        }
        //加入到发送队列中
        addTask(newItemuuid, null);

        return newItemuuid;
    }


    public boolean forwordArticleMsg(String receiver, DataBodyInfo itemInfo, int position) {
        LogUtil.begin("receiver:" + receiver + "|uuid:" + itemInfo + "|position=" + position);
        if (TextUtils.isEmpty(receiver)) {
            LogUtil.d("receiver 为空");
            return false;
        }
        String newMsgId = buildArticleForwardRecord(receiver,
            convertCollectDeteilInfoToEntity(itemInfo));
        if (TextUtils.isEmpty(newMsgId)) {
            LogUtil.d("newItemuuid 为空,构建记录失败");
            return false;
        }
        //加入到发送队列中
        addTask(newMsgId, null);
        return true;
    }


    private String buildArticleForwardRecord(String receiver, CollectionEntity entity) {

        String sender = MedicalApplication.getPreference()
            .getKeyValue(PrefType.LOGIN_NUBENUMBER, "");
        String recipentIds = StringUtil.sortRecipentIds(receiver, ';');
        NoticesBean bean = new NoticesBean();
        bean.setSender(sender);
        bean.setReciever(recipentIds);
        try {
            JSONArray bodyArray = new JSONArray();
            JSONObject bodyObject = new JSONObject();
            bodyObject.put("articleInfo", new JSONObject(entity.getBody()));
            bodyObject.put("text", "文章");
            bodyObject.put("subtype", "article");
            bodyArray.put(0, bodyObject);
            bean.setBody(bodyArray.toString());
        } catch (Exception e) {

        }

        bean.setStatus(TASK_STATUS_READY);
        bean.setType(FileTaskManager.NOTICE_TYPE_ARTICAL_SEND);
        bean.setIsNew(0);
        bean.setIsRead(1);
        long curtime = System.currentTimeMillis();
        bean.setSendTime(curtime);
        bean.setReceivedTime(curtime);
        bean.setTitle("");
        String newItemuuid = StringUtil.getUUID();
        bean.setId(newItemuuid);
        bean.setMsgId(newItemuuid);
        bean.setFailReplyId("");
        JSONObject extInfoObj = new JSONObject();
        try {
            extInfoObj.put("msgHead", AccountManager.getInstance(mcontext).getName() + "[文章]");
            extInfoObj.put("ver", "1.00");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (extInfoObj != null) {
            bean.setExtInfo(extInfoObj.toString());
        } else {
            bean.setExtInfo("");
        }
        //TODO 添加转发群组
        // 关联会话记录
        ThreadsDao threadsDao = new ThreadsDao(mcontext);
        if (recipentIds.length() < 12) {
            String covstid = threadsDao
                .createThread(recipentIds, curtime, true);
            if (TextUtils.isEmpty(covstid)) {
                LogUtil.d("createSendFileNotice createThread id==null");
                return "";
            }
            bean.setThreadsId(covstid);
        } else {
            if (!threadsDao.isExistThread(recipentIds)) {
                threadsDao.createThreadFromGroup(recipentIds);
            } else {
                threadsDao.updateLastTime(recipentIds, curtime);
            }
            bean.setThreadsId(recipentIds);
        }

        newItemuuid = noticedao.insertNotice(bean);
        LogUtil.end("newItemuuid:" + newItemuuid);
        return newItemuuid;
    }


    public boolean combineMsgForforward(String receiver, String uuids) {
        LogUtil.begin("receiver:" + receiver + "|uuid:" + uuids);

        if (TextUtils.isEmpty(receiver)) {
            LogUtil.d("receiver 为空");
            return false;
        }

        if (TextUtils.isEmpty(uuids)) {
            LogUtil.d("uuid 为空");
            return false;
        }

        String newItemuuid = buildCombineForwardRecord(receiver, uuids);

        if (TextUtils.isEmpty(newItemuuid)) {
            LogUtil.d("newItemuuid 为空,构建记录失败");
            return false;
        }
        //加入到发送队列中
        addTask(newItemuuid, null);
        return true;
    }


    private String buildCombineForwardRecord(String receiver, String uuids) {

        MDSAccountInfo selfInfo = AccountManager.getInstance(mcontext).getAccountInfo();

        String[] ids = uuids.split(",");
        String recipentIds = StringUtil.sortRecipentIds(receiver, ';');
        String createBody = "";
        String chatTitle = "";

        GroupDao groupDao = new GroupDao(mcontext);
        ThreadsDao threadDao = new ThreadsDao(mcontext);

        JSONArray array = new JSONArray();

        for (String id : ids) {
            NoticesBean oldbean = noticedao.getNoticeById(id);
            if (oldbean == null) {
                continue;
            }
            String sender = oldbean.getSender();
            ThreadsBean threadBean = threadDao.getThreadById(oldbean.getThreadsId());
            String userName = "";
            String userHeadUrl = "";
            //单聊
            if (threadBean.getType() == 1) {
                if (sender.equals(selfInfo.getNube())) {
                    userName = selfInfo.getNickName();
                    userHeadUrl = selfInfo.getHeadThumUrl();
                } else {
                    Contact userInfo = ContactManager.getInstance(mcontext)
                        .getContactInfoByNubeNumber(sender);
                    userName = userInfo.getNickname();
                    userHeadUrl = userInfo.getHeadUrl();
                }
                if (TextUtils.isEmpty(chatTitle)) {
                    ShowNameUtil.NameElement element = ShowNameUtil.getNameElement(
                        threadBean.getRecipientIds());
                    chatTitle = selfInfo.getNickName() + "和"
                        + ShowNameUtil.getShowName(element) + "的聊天记录";
                }
            } else if (threadBean.getType() == 2) {
                //群聊
                GroupMemberBean memberBean = groupDao.queryGroupMember(threadBean.getRecipientIds(),
                    sender);
                userName = memberBean.getNickName();
                userHeadUrl = memberBean.getHeadUrl();

                if (TextUtils.isEmpty(chatTitle)) {
                    chatTitle = groupDao.getGroupNameByGid(threadBean.getRecipientIds())
                        + "的聊天记录";
                }
            }

            JSONObject object = null;
            object = new JSONObject();
            try {
                object.put("username", userName);
                object.put("userheadUrl", userHeadUrl);
            } catch (Exception e) {
                CustomLog.d(TAG, e.toString());
            }

            JSONArray bodyArray;
            Long msgReceiveTime = (oldbean.getReceivedTime() > 0
                                   ? oldbean.getReceivedTime() : oldbean.getSendTime()) / 1000;

            switch (oldbean.getType()) {
                case FileTaskManager.NOTICE_TYPE_TXT_SEND: {
                    String text = "";
                    try {
                        bodyArray = new JSONArray(oldbean.getBody());
                        if (bodyArray != null && bodyArray.length() > 0) {
                            JSONObject bodyObj = bodyArray.optJSONObject(0);
                            text = bodyObj.optString("txt");
                        }
                    } catch (JSONException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    try {
                        object.put("type", FileTaskManager.NOTICE_TYPE_TXT_SEND);
                        object.put("text", text);
                        object.put("createtime", msgReceiveTime);
                        array.put(object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case FileTaskManager.NOTICE_TYPE_REMIND_SEND: {
                    try {
                        bodyArray = new JSONArray(oldbean.getBody());
                        if (bodyArray != null && bodyArray.length() > 0) {
                            JSONObject bodyObj = bodyArray.optJSONObject(0);
                            String text = bodyObj.optString("text");
                            object.put("type", FileTaskManager.NOTICE_TYPE_TXT_SEND);
                            object.put("text", text);
                            object.put("createtime", msgReceiveTime);
                            array.put(object);
                        }
                    } catch (JSONException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    break;
                }
                case FileTaskManager.NOTICE_TYPE_REMIND_ONE_SEND: {

                    String text = "";
                    try {
                        bodyArray = new JSONArray(oldbean.getBody());
                        if (bodyArray != null && bodyArray.length() > 0) {
                            JSONObject bodyObj = bodyArray.optJSONObject(0);
                            text = bodyObj.optString("text");
                        }
                    } catch (JSONException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    //解析@字符
                    if (text.contains(IMConstant.SPECIAL_CHAR + "")) {
                        ArrayList<String> result = new ArrayList<String>();
                        result = IMCommonUtil.getDispList(text);
                        for (int i = 0; i < result.size(); i++) {
                            GroupMemberBean gbean = groupDao.queryGroupMember(
                                oldbean.getThreadsId(), result.get(i));
                            if (gbean != null) {
                                ShowNameUtil.NameElement element = ShowNameUtil.getNameElement(
                                    gbean.getName(), gbean.getNickName(),
                                    gbean.getPhoneNum(), gbean.getNubeNum());
                                String MName = ShowNameUtil.getShowName(element);
                                text = text.replace("@" + result.get(i)
                                    + IMConstant.SPECIAL_CHAR, "@" + MName
                                    + IMConstant.SPECIAL_CHAR);
                            }
                        }
                    }

                    try {
                        object.put("type", FileTaskManager.NOTICE_TYPE_TXT_SEND);
                        object.put("text", text);
                        object.put("createtime", msgReceiveTime);
                        array.put(object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case FileTaskManager.NOTICE_TYPE_PHOTO_SEND: {
                    try {
                        bodyArray = new JSONArray(oldbean.getBody());
                        if (bodyArray != null && bodyArray.length() > 0) {
                            JSONObject bodyObj = bodyArray.optJSONObject(0);
                            object.put("type", FileTaskManager.NOTICE_TYPE_PHOTO_SEND);
                            object.put("remoteUrl", bodyObj.optString("remoteUrl"));
                            object.put("thumbnail", bodyObj.optString("thumbnail"));
                            object.put("width", bodyObj.optString("width"));
                            object.put("height", bodyObj.optString("height"));
                            object.put("createtime", msgReceiveTime);
                            array.put(object);
                        }
                    } catch (JSONException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    break;
                }
                case FileTaskManager.NOTICE_TYPE_VEDIO_SEND: {
                    try {
                        bodyArray = new JSONArray(oldbean.getBody());
                        if (bodyArray != null && bodyArray.length() > 0) {
                            JSONObject bodyObj = bodyArray.optJSONObject(0);
                            object.put("type", FileTaskManager.NOTICE_TYPE_VEDIO_SEND);
                            object.put("remoteUrl", bodyObj.optString("remoteUrl"));
                            object.put("size", bodyObj.optString("size"));
                            object.put("duration", bodyObj.optString("duration"));
                            object.put("thumbnail", bodyObj.optString("thumbnail"));
                            object.put("createtime", msgReceiveTime);
                            array.put(object);
                        }
                    } catch (JSONException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    break;
                }
                case FileTaskManager.NOTICE_TYPE_VCARD_SEND: {
                    try {
                        bodyArray = new JSONArray(oldbean.getBody());
                        if (bodyArray != null && bodyArray.length() > 0) {
                            JSONObject bodyObj = bodyArray.optJSONObject(0);
                            object.put("type", FileTaskManager.NOTICE_TYPE_VCARD_SEND);
                            object.put("cardname", bodyObj.optString("name"));
                            object.put("cardCode", bodyObj.optString("code"));
                            object.put("cardUrl", bodyObj.optString("url"));
                            object.put("createtime", msgReceiveTime);
                            array.put(object);
                        }
                    } catch (JSONException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    break;
                }

                case FileTaskManager.NOTICE_TYPE_ARTICAL_SEND: {
                    try {
                        bodyArray = new JSONArray(oldbean.getBody());
                        if (bodyArray != null && bodyArray.length() > 0) {
                            JSONObject bodyObj = bodyArray.optJSONObject(0);
                            JSONObject tmpObj = bodyObj.optJSONObject("articleInfo");
                            object.put("type", FileTaskManager.NOTICE_TYPE_ARTICAL_SEND);
                            object.put("articleId", tmpObj.optString("articleId"));
                            object.put("title", tmpObj.optString("title"));
                            object.put("previewUrl", tmpObj.optString("previewUrl"));
                            object.put("introduction", tmpObj.optString("introduction"));
                            object.put("articleType", tmpObj.optInt("articleType"));
                            object.put("officeName", tmpObj.optString("officeName"));
                            object.put("createtime", msgReceiveTime);
                            object.put("offAccLogoUrl", tmpObj.optString("previewUrl"));
                            array.put(object);
                        }
                    } catch (JSONException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }

        try {
            JSONArray combineArray = new JSONArray();
            JSONObject combineObject = new JSONObject();
            combineObject.put("chatrecordInfo", array);
            combineObject.put("text", chatTitle);
            combineObject.put("subtype", "chat_record");
            combineArray.put(combineObject);
            createBody = combineArray.toString();

        } catch (Exception e) {
            CustomLog.d(TAG, e.toString());
        }

        String sender = AccountManager.getInstance(mcontext).getNube();
        NoticesBean bean = new NoticesBean();
        bean.setSender(sender);
        bean.setReciever(recipentIds);
        bean.setBody(createBody);
        bean.setStatus(TASK_STATUS_READY);
        bean.setType(FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND);

        bean.setIsNew(0);
        long curtime = System.currentTimeMillis();
        bean.setSendTime(curtime);
        bean.setReceivedTime(curtime);
        bean.setTitle(chatTitle);
        String newItemuuid = StringUtil.getUUID();
        bean.setId(newItemuuid);

        bean.setMsgId(newItemuuid);
        bean.setFailReplyId("");
        // 修改下extInfo中id的值
        JSONObject extInfoObj = new JSONObject();
        try {
            extInfoObj.put("msgHead", AccountManager.getInstance(mcontext).getName() + "[聊天记录]");
            extInfoObj.put("ver", "1.00");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (extInfoObj != null) {
            bean.setExtInfo(extInfoObj.toString());
        } else {
            bean.setExtInfo("");
        }
        //TODO 添加转发群组
        // 关联会话记录
        ThreadsDao threadsDao = new ThreadsDao(mcontext);
        if (recipentIds.length() < 12) {
            String covstid = threadsDao
                .createThread(recipentIds, curtime, true);
            if (TextUtils.isEmpty(covstid)) {
                LogUtil.d("createSendFileNotice createThread id==null");
                return "";
            }
            bean.setThreadsId(covstid);
        } else {
            if (!threadsDao.isExistThread(recipentIds)) {
                threadsDao.createThreadFromGroup(recipentIds);
            } else {
                threadsDao.updateLastTime(recipentIds, curtime);
            }
            bean.setThreadsId(recipentIds);
        }

        if (bean.getBody().toString().length() > 2048) {
            CustomLog.d(TAG, "消息内容过长 ：" + bean.getBody().length());
            return "";
        }

        newItemuuid = noticedao.insertNotice(bean);
        LogUtil.end("newItemuuid:" + newItemuuid);
        return newItemuuid;
    }


    /**
     * 构建转发消息记录，返回新记录的ID
     *
     * @param receiver 接收对象的视频号（多个视频号用分号分割）
     * @param uuid 将要转发的消息uuid
     * @return 返回新记录的id
     */
    private String buildForwardRecord(String receiver, String uuid) {

        LogUtil.begin("receiver:" + receiver + "|uuid:" + uuid);

        NoticesBean oldbean = noticedao.getNoticeById(uuid);
        GroupDao groupDao = new GroupDao(mcontext);
        if (oldbean == null) {
            LogUtil.d("buildForwardRecord getNoticeById ==null");
            return "";
        }
        // sort 接收对象的视频号
        String recipentIds = StringUtil.sortRecipentIds(receiver, ';');
        String createBody = "";
        LogUtil.d("after sorted recipentIds:" + recipentIds);

        //TODO:重新构建body字段
        JSONArray bodyArray;
        String text = "";
        try {
            bodyArray = new JSONArray(oldbean.getBody());
            if (bodyArray != null && bodyArray.length() > 0) {
                JSONObject bodyObj = bodyArray.optJSONObject(0);
                if (oldbean.getType() == FileTaskManager.NOTICE_TYPE_REMIND_ONE_SEND) {
                    oldbean.setType(FileTaskManager.NOTICE_TYPE_TXT_SEND);
                    text = bodyObj.optString("text");
                } else {
                    text = bodyObj.optString("txt");
                }

            }
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if (text.contains(IMConstant.SPECIAL_CHAR + "")) {
            ArrayList<String> result = new ArrayList<String>();
            result = IMCommonUtil.getDispList(text);
            for (int i = 0; i < result.size(); i++) {
                GroupMemberBean gbean = groupDao.queryGroupMember(
                    oldbean.getThreadsId(), result.get(i));
                if (gbean != null) {
                    ShowNameUtil.NameElement element = ShowNameUtil.getNameElement(
                        gbean.getName(), gbean.getNickName(),
                        gbean.getPhoneNum(), gbean.getNubeNum());
                    String MName = ShowNameUtil.getShowName(element);
                    text = text.replace("@" + result.get(i)
                        + IMConstant.SPECIAL_CHAR, "@" + MName
                        + IMConstant.SPECIAL_CHAR);
                }
            }
            JSONArray array = new JSONArray();
            JSONObject object = null;
            object = new JSONObject();
            try {
                object.put("txt", text);
                array.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            createBody = array.toString();
        } else {
            createBody = oldbean.getBody();
        }
        String sender = MedicalApplication.getPreference()
            .getKeyValue(PrefType.LOGIN_NUBENUMBER, "");
        NoticesBean bean = new NoticesBean();
        bean.setSender(sender);
        bean.setReciever(recipentIds);
        bean.setBody(createBody);
        bean.setStatus(TASK_STATUS_READY);
        bean.setType(oldbean.getType());

        bean.setIsNew(0);
        long curtime = System.currentTimeMillis();
        bean.setSendTime(curtime);
        bean.setReceivedTime(curtime);
        bean.setTitle(oldbean.getTitle());

        String newItemuuid = StringUtil.getUUID();
        bean.setId(newItemuuid);

        bean.setMsgId(newItemuuid);
        bean.setFailReplyId("");
        // 修改下extInfo中id的值
        String oldExt = oldbean.getExtInfo();
        JSONObject extObj = null;
        if (!TextUtils.isEmpty(oldExt)) {
            try {
                extObj = new JSONObject(oldExt);
                extObj.put("id", bean.getId());
                extObj.put("ver", BizConstant.MSG_VERSION);
            } catch (JSONException e) {
                LogUtil.e("JSONException", e);
                e.printStackTrace();
            }
        } else {
            try {
                extObj = new JSONObject();
                extObj.put("id", bean.getId());
                extObj.put("text", "");
                extObj.put("ver", BizConstant.MSG_VERSION);
            } catch (JSONException e) {
                LogUtil.e("JSONException", e);
                e.printStackTrace();
            }
        }
        if (extObj != null) {
            bean.setExtInfo(extObj.toString());
        } else {
            bean.setExtInfo("");
        }
        //TODO 添加转发群组
        // 关联会话记录
        ThreadsDao threadsDao = new ThreadsDao(mcontext);
        if (recipentIds.length() < 12) {
            String covstid = threadsDao
                .createThread(recipentIds, curtime, true);
            if (TextUtils.isEmpty(covstid)) {
                LogUtil.d("createSendFileNotice createThread id==null");
                return "";
            }
            bean.setThreadsId(covstid);
        } else {
            if (!threadsDao.isExistThread(recipentIds)) {
                threadsDao.createThreadFromGroup(recipentIds);
            } else {
                threadsDao.updateLastTime(recipentIds, curtime);
            }
            bean.setThreadsId(recipentIds);
        }

        newItemuuid = noticedao.insertNotice(bean);
        LogUtil.end("newItemuuid:" + newItemuuid);
        return newItemuuid;
    }


    //0:主动请求  1：回复消息
    public void sendStrangerMsg(String senderNubeNum, String receiverNubeNum,
                                String senderNickName, String senderHeadurl,
                                String content, boolean isReplayMsg) {
        SCIMBean bean = new SCIMBean();
        bean.msgType = "common";
        try {
            JSONObject titleObj = new JSONObject();
            titleObj.put("sender", senderNubeNum);
            titleObj.put("msgInfo", "来自Butel Android客户端");
            bean.title = titleObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] tmpStrArray = new String[1];
        tmpStrArray[0] = receiverNubeNum;
        bean.recvs = tmpStrArray;
        bean.recvsLen = 1;

        try {
            JSONObject contentObj = new JSONObject();
            contentObj.put("nickname", senderNickName);
            contentObj.put("headurl", senderHeadurl);
            if (isReplayMsg) {
                contentObj.put("isReplayMsg", STRANGER_TYPE_REPLAY);
            } else {
                contentObj.put("isReplayMsg", STRANGER_TYPE_REQUEST);
            }
            contentObj.put("text", content);
            contentObj.put("subtype", "stranger_msg");
            bean.text = contentObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        bean.thumUrl = "";
        bean.durationSec = 0;
        try {
            JSONObject extendObj = new JSONObject();
            extendObj.put("msgHead", senderNickName + "：" + content);
            extendObj.put("ver", "1.00");
            bean.extJson = extendObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        AppP2PAgentManager.getInstance().sendIMMessage(bean);
    }


    public void sendAddFriendMsg(String senderNubeNum, String receiverNubeNum
        , String senderNickName, String senderHeadurl) {
        SCIMBean bean = new SCIMBean();
        bean.msgType = "common";
        try {
            JSONObject titleObj = new JSONObject();
            titleObj.put("sender", senderNubeNum);
            titleObj.put("msgInfo", "来自Butel Android客户端");
            bean.title = titleObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] tmpStrArray = new String[1];
        tmpStrArray[0] = receiverNubeNum;
        bean.recvs = tmpStrArray;
        bean.recvsLen = 1;

        try {
            JSONObject contentObj = new JSONObject();
            contentObj.put("nickname", senderNickName);
            contentObj.put("headurl", senderHeadurl);
            contentObj.put("subtype", "add_friend_msg");
            bean.text = contentObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        bean.thumUrl = "";
        bean.durationSec = 0;
        try {
            JSONObject extendObj = new JSONObject();
            extendObj.put("msgHead", "");
            extendObj.put("ver", "1.00");
            bean.extJson = extendObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        AppP2PAgentManager.getInstance().sendIMMessage(bean);
    }


    public void sendDeleteFriendMsg(String senderNubeNum, String receiverNubeNum) {
        LogUtil.begin("receiver:" + receiverNubeNum);
        SCIMBean bean = new SCIMBean();
        bean.msgType = "common";
        try {
            JSONObject titleObj = new JSONObject();
            titleObj.put("sender", senderNubeNum);
            titleObj.put("msgInfo", "来自Butel Android客户端");
            bean.title = titleObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] tmpStrArray = new String[1];
        tmpStrArray[0] = receiverNubeNum;
        bean.recvs = tmpStrArray;
        bean.recvsLen = 1;

        try {
            JSONObject contentObj = new JSONObject();
            contentObj.put("subtype", "delete_friend_msg");
            bean.text = contentObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        bean.thumUrl = "";
        bean.durationSec = 0;
        try {
            JSONObject extendObj = new JSONObject();
            extendObj.put("msgHead", "");
            extendObj.put("ver", "1.00");
            bean.extJson = extendObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        AppP2PAgentManager.getInstance().sendIMMessage(bean);
    }


    /**
     * 发送修改诊疗状态消息，开始诊疗、结束诊疗时调用。
     *
     * @param state 诊疗操作状态，0：开始诊疗；1：结束诊疗
     * @param senderNubeNum 发送者视讯号
     * @param receiverNubeNum 接收者视讯号
     * @param dtId 诊疗单号
     * @param dotcorHeadUrl 接诊医生头像地址
     * @param doctorNickName 接诊医生名称
     * {"msgType":common,
     * "title":{"sender":"90008990","来自Butel Android客户端"},
     * "recvs”:[“dcb992915a4541c09dad7dbc3e6c5dcd"],
     * "recvsLen":1,
     * "text”:{
     * "doctorInfo":{“headUrl":"","nickName":""}，
     * "consulationId": "", //诊疗单号
     * "state":"start", //"start"：表示接诊，"end"：表示结束会诊
     * "subtype":"reception_msg"
     * },
     * "thumUrl":”",
     * "durationSec":0,
     * “groupid”:”dcb992915a4541c09dad7dbc3e6c5dcd”,
     * "appExtendInfo":{"msgHead":"","ver":"1.00”}}
     */
    public void sendChangeDtStateMsg(int state, String senderNubeNum, String receiverNubeNum, String dtId, String dotcorHeadUrl, String doctorNickName) {
        CustomLog.i(TAG, "sendChangeDtStateMsg()");

        SCIMBean bean = new SCIMBean();
        bean.msgType = "common";
        try {
            JSONObject titleObj = new JSONObject();
            titleObj.put("sender", senderNubeNum);
            titleObj.put("msgInfo", "来自Butel Android客户端");
            bean.title = titleObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] tmpStrArray = new String[1];
        tmpStrArray[0] = receiverNubeNum;
        bean.recvs = tmpStrArray;
        bean.recvsLen = 1;

        try {
            JSONObject contentObj = new JSONObject();
            contentObj.put("consulationId", dtId);
            contentObj.put("state", state == 0 ? "start" : "end");
            contentObj.put("subtype", BizConstant.MSG_SUB_TYPE_DT_OPERATION);
            JSONObject doctdorObj = new JSONObject();
            doctdorObj.put("headUrl", dotcorHeadUrl);
            doctdorObj.put("nickName", doctorNickName);
            contentObj.put("doctorInfo", doctdorObj);
            bean.text = contentObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        bean.thumUrl = "";
        bean.durationSec = 0;
        try {
            JSONObject extendObj = new JSONObject();
            extendObj.put("msgHead", "");
            extendObj.put("ver", "1.00");
            extendObj.put("medicalComboMsg", 1);
            bean.extJson = extendObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        AppP2PAgentManager.getInstance().sendIMMessage(bean);
    }


    /**
     * 发送转诊结论
     *
     * @param resultType 诊疗结果。0：本地诊疗；1：转诊
     * @param senderNubeNum 发送者视讯号
     * @param receiverNubeNum 接收者视讯号
     * @param dtId 求诊单号
     * @param transferId 转诊单号，本地诊疗填“”
     * IM_SendMessage {"msgType":common,
     * "title":{"sender":"90008990","来自Butel Android客户端"},
     * "recvs":90007862,
     * "recvsLen":1,
     * "text":{
     * "consulationId”:””,
     * "referralId”:””,
     * “advice”:”local”,
     * "subtype":”diagnosis_msg"
     * },
     * "thumUrl":,
     * "durationSec":0,
     * “groupid”:””,
     * "appExtendInfo":{"msgHead":"","ver":"1.00”}}
     */
    public void sendDTResultMsg(int resultType, String senderNubeNum, String receiverNubeNum, String dtId, String transferId) {
        SCIMBean bean = new SCIMBean();
        bean.msgType = "common";
        try {
            JSONObject titleObj = new JSONObject();
            titleObj.put("sender", senderNubeNum);
            titleObj.put("msgInfo", "来自Butel Android客户端");
            bean.title = titleObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] tmpStrArray = new String[1];
        tmpStrArray[0] = receiverNubeNum;
        bean.recvs = tmpStrArray;
        bean.recvsLen = 1;

        try {
            JSONObject contentObj = new JSONObject();
            contentObj.put("advice", resultType == 0
                                     ? BizConstant.MSG_DT_RESULT_TYPE_LOCAL
                                     : BizConstant.MSG_DT_RESULT_TYPE_TRANSFER);
            contentObj.put("consulationId", dtId);
            contentObj.put("referralId", transferId);
            contentObj.put("subtype", BizConstant.MSG_SUB_TYPE_DT_RESULT);
            bean.text = contentObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        bean.thumUrl = "";
        bean.durationSec = 0;
        try {
            JSONObject extendObj = new JSONObject();
            extendObj.put("msgHead", "");
            extendObj.put("ver", "1.00");
            bean.extJson = extendObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        AppP2PAgentManager.getInstance().sendIMMessage(bean);
    }


    private void sendMessageBody(int type, String uuid, String srcUrl) {
        CustomLog.d(TAG,
            "sendMessageBody begin type:" + type + "|uuid:" + uuid + "|srcUrl:" + srcUrl);
        switch (type) {
            case NOTICE_TYPE_PHOTO_SEND:
            case NOTICE_TYPE_VCARD_SEND:
            case NOTICE_TYPE_VEDIO_SEND:
            case NOTICE_TYPE_AUDIO_SEND:
            case NOTICE_TYPE_TXT_SEND:
            case NOTICE_TYPE_MEETING_INVITE:
            case NOTICE_TYPE_FILE:
                break;
            default:
        }
    }


    public void updateStatusAfterIM(String uuid, boolean succ) {
        CustomLog.d(TAG, "updateStatusAfterIM()" + uuid + " succ:" + succ);

        if (isUDTMsg(uuid)) {
            if (succ) {
                updateDTTaskStatus(uuid, TASK_STATUS_SUCCESS, true);
            } else {
                updateDTTaskStatus(uuid, TASK_STATUS_FAIL, true);
            }

        } else {
            if (succ) {
                updateTaskStatus(uuid, TASK_STATUS_SUCCESS, true);
            } else {
                updateTaskStatus(uuid, TASK_STATUS_FAIL, true);
            }
        }

    }


    private boolean updateDTTaskStatus(String uuid, int status,
                                       boolean change_body) {
        CustomLog.d(TAG,
            "updateTaskStatus begin,uuid:" + uuid + "|status:" + status + "|change_body:" +
                change_body);
        // 根据uuid更改记录中body 和 status 字段
        if (TextUtils.isEmpty(uuid)) {
            return false;
        }
        if (change_body) {
            String body = createBodyString(uuid);
            CustomLog.d(TAG, "body:" + body);
            if (TextUtils.isEmpty(body)) {
                int count = dtNoticedao.updateNotice(uuid, status);
                if (count > 0) {
                    return true;
                }
            } else {
                int count = dtNoticedao.updateNotice(uuid, body, status);
                if (count > 0) {
                    return true;
                }
            }
        } else {
            int count = dtNoticedao.updateNotice(uuid, status);
            if (count > 0) {
                return true;
            }
        }
        return false;
    }


    /**
     * 检查当前消息是否为联合诊疗室消息，通过在数据库中查出该消息，检查扩展字段
     */
    private boolean isUDTMsg(String uuid) {
        CustomLog.i(TAG, "isUDTMsg()");

        NoticesBean msgBean = dtNoticedao.getNoticeById(uuid);

        if (msgBean == null) {
            return false;
        } else {
            return true;
        }
    }


    // 删除消息map任务里面对应数值
    public void removeMap(String uuid) {
        if (TextUtils.isEmpty(uuid) || fileTaskMap == null
            || fileTaskMap.size() == 0) {
            CustomLog.d(TAG, "updateStatusAfterIM uuid:" + uuid + " fileTaskMap:"
                + fileTaskMap);
            return;
        }
        CustomLog.d(TAG, "fileTaskMap.remove:" + uuid);
        fileTaskMap.remove(uuid);
    }


    // 更新消息发送时间为服务器返回的发送时间
    public void updateTime(String uuid, long time) {
        CustomLog.d(TAG, "updateStatusAfterIM uuid:" + uuid + " 发送消息成功服务器返回时间:" + time);
        if (TextUtils.isEmpty(uuid)) {
            return;
        }

        if (time != 0) {
            if (isUDTMsg(uuid)) {
                dtNoticedao.updateNotice(uuid, time);
            } else {
                noticedao.updateNotice(uuid, time);
            }
        }
    }

    //=======END==========IM Connect集成适配===================

    //==================== START 集成SDK Connect=============================


    public static class SCIMBean {
        public String msgType = "";
        public String title = "";
        public String[] recvs = null;
        public int recvsLen = 0;
        public String text = "";
        // 当thumUrl不为空时，filePath可以忽略
        public String filePath = "";
        public String thumUrl = "";
        public int upLoadFilTimeOutSec = AppP2PAgentManager.UPLOADFILE_TIMEOUT;
        public int durationSec = 0;
        // 当isGroupMsg为true时，groupId有意义
        public String groupId = "";
        public boolean isGroupMsg = false;
        // 消息存储在本地表中的uuid
        public String uuid = "";
        public String extJson = "";
    }


    public boolean sendDTSCIMMsg(String uuid) {
        SCIMBean bean = convert2DTSCIMBean(uuid);
        if (bean != null) {
            //TODO:
            AppP2PAgentManager.getInstance().sendIMMessage(bean);
            //更新会议弹屏状态
            updateMeetingShowFlag(bean.uuid);
            return true;
        } else {
            CustomLog.d(TAG, "convert2SCIMBean bean对象为空");
        }
        return false;
    }


    public boolean sendSCIMMsg(String uuid) {
        CustomLog.i(TAG, "sendSCIMMsg()");

        SCIMBean bean = convert2SCIMBean(uuid);
        if (bean != null) {
            //TODO:
            AppP2PAgentManager.getInstance().sendIMMessage(bean);
            //更新会议弹屏状态
            updateMeetingShowFlag(bean.uuid);
            return true;
        } else {
            CustomLog.d(TAG, "convert2SCIMBean bean对象为空");
        }
        return false;
    }


    public SCIMBean convert2DTSCIMBean(String uuid) {

        SCIMBean message = null;
        NoticesBean bean = dtNoticedao.getNoticeById(uuid);
        boolean isGroupMsg = false;
        if (bean != null) {
            message = new SCIMBean();
            message.uuid = uuid;
            if (bean.getReciever().equals(bean.getThreadsId())) {
                isGroupMsg = true;
                message.groupId = bean.getReciever();
                message.recvsLen = 1;
            }
            message.isGroupMsg = isGroupMsg;
            int type = bean.getType();
            List<String> filePathList = new ArrayList<String>();
            List<String> resultUrlList = new ArrayList<String>();
            List<String> thumbs = new ArrayList<String>();
            List<FileTaskBean> urls = createDTFileTaskList(uuid, false);
            String temp = "";
            for (FileTaskBean url : urls) {
                temp = url.getSrcUrl();
                if (!TextUtils.isEmpty(temp)) {
                    filePathList.add(temp);
                }
                temp = url.getResultUrl();
                if (!TextUtils.isEmpty(temp)) {
                    resultUrlList.add(temp);
                }
                temp = url.getThumbnailUrl();
                if (!TextUtils.isEmpty(temp)) {
                    thumbs.add(temp);
                }
            }
            String receiver = bean.getReciever();
            String titlecontext = bean.getTitle();

            try {
                if (!TextUtils.isEmpty(receiver)) {
                    String[] reciever_list = receiver.split(";");
                    if (reciever_list != null && reciever_list.length > 0) {
                        message.recvs = reciever_list;
                        message.recvsLen = reciever_list.length;
                    } else {
                        CustomLog.d(TAG, "发送对象为空");
                        return null;
                    }
                } else {
                    CustomLog.d(TAG, "发送对象为空");
                    return null;
                }

                if (resultUrlList == null || resultUrlList.size() == 0) {
                    //文件上没有上传成功
                    if (filePathList.size() > 0) {
                        //TODO:目前SDK connect的发送消息接口，一次只能上传一个文件
                        message.filePath = filePathList.get(0);
                        message.upLoadFilTimeOutSec = AppP2PAgentManager.UPLOADFILE_TIMEOUT;
                    } else {
                        if (type == NOTICE_TYPE_PHOTO_SEND
                            || type == NOTICE_TYPE_VEDIO_SEND
                            || type == NOTICE_TYPE_AUDIO_SEND) {
                            CustomLog.d(TAG, "文件上没有上传成功,且文件路径为空");
                            return null;
                        }
                    }
                } else {
                    List<String> url = new ArrayList<String>();
                    String orgUrl = "";
                    String thumbUrl = "";
                    if (filePathList.size() > 0) {
                        message.filePath = filePathList.get(0);
                    } else {
                        message.filePath = "";
                    }
                    for (int i = 0; i < resultUrlList.size(); i++) {
                        orgUrl = resultUrlList.get(i);
                        if (type == NOTICE_TYPE_PHOTO_SEND) {
                            // bigUrl占位
                            if (!TextUtils.isEmpty(orgUrl)) {
                                url.add(orgUrl);
                            }
                        }
                        if (!TextUtils.isEmpty(orgUrl)) {
                            url.add(orgUrl);
                        }
                        if (thumbs != null && thumbs.size() > i) {
                            thumbUrl = thumbs.get(i);
                            if (!TextUtils.isEmpty(thumbUrl)) {
                                url.add(thumbUrl);
                            }
                        }
                    }
                    message.thumUrl = StringUtil.list2String(url, ',');
                }

                String snipText = "";
                if (type == NOTICE_TYPE_MEETING_INVITE) {
                    snipText = "邀请你加入会议";
                } else if (type == NOTICE_TYPE_MEETING_BOOK) {
                    snipText = "预约你参加会议";
                } else if (type == NOTICE_TYPE_TXT_SEND) {
                    snipText = getSnipTxt(bean);
                } else if (type == NOTICE_TYPE_CHATRECORD_SEND) {
                    snipText = "[聊天记录]";
                } else if (type == NOTICE_TYPE_ARTICAL_SEND) {
                    snipText = "[文章]";
                } else if (type == NOTICE_TYPE_REMIND_SEND) {
                    snipText = "[公告]";
                }
                //根据消息类型从body字段中找出部分扩展的信息+消息的版本号
                //放置到SC消息的text字段
                String text = createCSIMTxt(type, bean.getBody());
                message.text = text;

                message.extJson = createDTCSIMAppExtInfo(type, snipText,
                    bean.getBody(), bean.getExtInfo(), "");

                // 兼容平板侧的JSON串定义
                if (type == NOTICE_TYPE_FRIEND_SEND || type == NOTICE_TYPE_FEEEDBACK_SEND) {
                    message.title = titlecontext;
                } else {
                    JSONObject title = new JSONObject();
                    String own = AccountManager.getInstance(mcontext).getAccountInfo().nube;
                    title.put("sender", own);
                    if (TextUtils.isEmpty(titlecontext)) {
                        title.put("msgInfo", "来自Butel Android客户端");
                    } else {
                        title.put("msgInfo", titlecontext);
                    }
                    message.title = title.toString();
                }

                switch (type) {
                    case NOTICE_TYPE_PHOTO_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_PIC_2;
                        break;
                    case NOTICE_TYPE_FRIEND_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_VCARD;
                        break;
                    case NOTICE_TYPE_FEEEDBACK_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_MULTITRUST;
                        break;
                    case NOTICE_TYPE_VEDIO_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_VIDEO_2;
                        message.durationSec = getDuration(bean.getBody());
                        break;
                    case NOTICE_TYPE_VCARD_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_POSTCARD;
                        break;
                    case NOTICE_TYPE_AUDIO_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_AUDIO;
                        message.durationSec = getDuration(bean.getBody());
                        break;
                    case NOTICE_TYPE_IPCALL_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_IPCALL;
                        break;
                    case NOTICE_TYPE_TXT_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_TXT;
                        break;
                    case NOTICE_TYPE_FILE:
                    case NOTICE_TYPE_MEETING_INVITE:
                    case NOTICE_TYPE_MEETING_BOOK:
                    case NOTICE_TYPE_CHATRECORD_SEND:
                    case NOTICE_TYPE_ARTICAL_SEND:
                    case NOTICE_TYPE_REMIND_SEND:
                    case NOTICE_TYPE_REMIND_ONE_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_COMMON;
                        break;
                    default:
                        CustomLog.d(TAG, "无法识别的消息类型");
                }
            } catch (JSONException e) {
                CustomLog.e(TAG, "JSONException" + e.toString());
                message = null;
            }
        }

        return message;
    }


    public SCIMBean convert2SCIMBean(String uuid) {
        CustomLog.i(TAG, "convert2SCIMBean()");

        SCIMBean message = null;
        NoticesBean bean = noticedao.getNoticeById(uuid);
        boolean isGroupMsg = false;
        if (bean != null) {
            message = new SCIMBean();
            message.uuid = uuid;
            if (bean.getReciever().equals(bean.getThreadsId())) {
                isGroupMsg = true;
                message.groupId = bean.getReciever();
                message.recvsLen = 1;
            }
            message.isGroupMsg = isGroupMsg;

            int type = bean.getType();
            List<String> filePathList = new ArrayList<String>();
            List<String> resultUrlList = new ArrayList<String>();
            List<String> thumbs = new ArrayList<String>();

            List<FileTaskBean> urls = createFileTaskList(uuid, false);

            String temp = "";
            for (FileTaskBean url : urls) {
                temp = url.getSrcUrl();
                if (!TextUtils.isEmpty(temp)) {
                    filePathList.add(temp);
                }
                temp = url.getResultUrl();
                if (!TextUtils.isEmpty(temp)) {
                    resultUrlList.add(temp);
                }
                temp = url.getThumbnailUrl();
                if (!TextUtils.isEmpty(temp)) {
                    thumbs.add(temp);
                }
            }
            String receiver = bean.getReciever();
            String titlecontext = bean.getTitle();

            try {
                if (!TextUtils.isEmpty(receiver)) {
                    String[] reciever_list = receiver.split(";");
                    if (reciever_list != null && reciever_list.length > 0) {
                        message.recvs = reciever_list;
                        message.recvsLen = reciever_list.length;
                    } else {
                        CustomLog.d(TAG, "发送对象为空");
                        return null;
                    }
                } else {
                    CustomLog.d(TAG, "发送对象为空");
                    return null;
                }

                if (resultUrlList == null || resultUrlList.size() == 0) {
                    //文件上没有上传成功
                    if (filePathList.size() > 0) {
                        //TODO:目前SDK connect的发送消息接口，一次只能上传一个文件
                        message.filePath = filePathList.get(0);
                        message.upLoadFilTimeOutSec = AppP2PAgentManager.UPLOADFILE_TIMEOUT;
                    } else {
                        if (type == NOTICE_TYPE_PHOTO_SEND
                            || type == NOTICE_TYPE_VEDIO_SEND
                            || type == NOTICE_TYPE_AUDIO_SEND) {
                            CustomLog.d(TAG, "文件上没有上传成功,且文件路径为空");
                            return null;
                        }
                    }
                } else {
                    List<String> url = new ArrayList<String>();
                    String orgUrl = "";
                    String thumbUrl = "";
                    if (filePathList.size() > 0) {
                        message.filePath = filePathList.get(0);
                    } else {
                        message.filePath = "";
                    }
                    for (int i = 0; i < resultUrlList.size(); i++) {
                        orgUrl = resultUrlList.get(i);
                        if (type == NOTICE_TYPE_PHOTO_SEND) {
                            // bigUrl占位
                            if (!TextUtils.isEmpty(orgUrl)) {
                                url.add(orgUrl);
                            }
                        }
                        if (!TextUtils.isEmpty(orgUrl)) {
                            url.add(orgUrl);
                        }
                        if (thumbs != null && thumbs.size() > i) {
                            thumbUrl = thumbs.get(i);
                            if (!TextUtils.isEmpty(thumbUrl)) {
                                url.add(thumbUrl);
                            }
                        }
                    }
                    message.thumUrl = StringUtil.list2String(url, ',');
                }

                String snipText = "";
                if (type == NOTICE_TYPE_MEETING_INVITE) {
                    snipText = "邀请你视频会诊";
                } else if (type == NOTICE_TYPE_MEETING_BOOK) {
                    snipText = "发来了预约会诊邀请";
                } else if (type == NOTICE_TYPE_TXT_SEND) {
                    snipText = getSnipTxt(bean);
                } else if (type == NOTICE_TYPE_CHATRECORD_SEND) {
                    snipText = "[聊天记录]";
                } else if (type == NOTICE_TYPE_ARTICAL_SEND) {
                    snipText = "[文章]";
                } else if (type == NOTICE_TYPE_REMIND_SEND) {
                    snipText = "[公告]";
                } else if (type == NOTICE_TYPE_REMIND_ONE_SEND) {
                    snipText = getRemindMsgSnipText(bean);
                }
                //根据消息类型从body字段中找出部分扩展的信息+消息的版本号
                //放置到SC消息的text字段
                String text = createCSIMTxt(type, bean.getBody());
                message.text = text;

                message.extJson = createCSIMAppExtInfo(type, snipText,
                    bean.getBody(), bean.getExtInfo(), "");

                // 兼容平板侧的JSON串定义
                if (type == NOTICE_TYPE_FRIEND_SEND || type == NOTICE_TYPE_FEEEDBACK_SEND) {
                    message.title = titlecontext;
                } else {
                    JSONObject title = new JSONObject();
                    String own = AccountManager.getInstance(mcontext).getAccountInfo().nube;
                    title.put("sender", own);
                    if (TextUtils.isEmpty(titlecontext)) {
                        title.put("msgInfo", "来自Butel Android客户端");
                    } else {
                        title.put("msgInfo", titlecontext);
                    }
                    message.title = title.toString();
                }

                switch (type) {
                    case NOTICE_TYPE_PHOTO_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_PIC_2;
                        break;
                    case NOTICE_TYPE_FRIEND_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_VCARD;
                        break;
                    case NOTICE_TYPE_FEEEDBACK_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_MULTITRUST;
                        break;
                    case NOTICE_TYPE_VEDIO_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_VIDEO_2;
                        message.durationSec = getDuration(bean.getBody());
                        break;
                    case NOTICE_TYPE_VCARD_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_POSTCARD;
                        break;
                    case NOTICE_TYPE_AUDIO_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_AUDIO;
                        message.durationSec = getDuration(bean.getBody());
                        break;
                    case NOTICE_TYPE_IPCALL_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_IPCALL;
                        break;
                    case NOTICE_TYPE_TXT_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_TXT;
                        break;
                    case NOTICE_TYPE_FILE:
                    case NOTICE_TYPE_ATTACHMENT_FILE:
                    case NOTICE_TYPE_MEETING_INVITE:
                    case NOTICE_TYPE_MEETING_BOOK:
                    case NOTICE_TYPE_CHATRECORD_SEND:
                    case NOTICE_TYPE_ARTICAL_SEND:
                    case NOTICE_TYPE_REMIND_SEND:
                    case NOTICE_TYPE_REMIND_ONE_SEND:
                        message.msgType = BizConstant.MSG_BODY_TYPE_COMMON;
                        break;
                    default:
                        CustomLog.d(TAG, "无法识别的消息类型");
                }
            } catch (JSONException e) {
                CustomLog.e(TAG, "JSONException" + e.toString());
                message = null;
            }
        }

        return message;
    }


    private String getRemindMsgSnipText(NoticesBean bean) {
        CustomLog.i(TAG, "getRemindMsgSnipText()");

        String remindSnip = "";
        String remindNubeName = "";
        String nubeMaterial = "";
        String nube = "";

        try {
            JSONArray bodyJsonArray = new JSONArray(bean.getBody());
            JSONObject bodyJSONObject = bodyJsonArray.getJSONObject(0);
            nubeMaterial = bodyJSONObject.optString("text");
            nube = nubeMaterial.substring(1).replaceAll("\\s+", ""); // 去除第一个无关字符并去除字符串中所有的空格
            remindNubeName = getGroupMemberName(bean.getReciever(), nube);

            remindSnip = "@" + remindNubeName;

        } catch (JSONException e) {
            e.printStackTrace();
            CustomLog.e(TAG, "getRemindMsgSnipText | " + e.toString());
        }

        return remindSnip;
    }


    private int getDuration(String body) {

        int duration = 0;
        if (!TextUtils.isEmpty(body)) {
            try {
                JSONArray array = new JSONArray(body);
                if (array != null && array.length() > 0) {
                    JSONObject item = array.getJSONObject(0);
                    duration = item.optInt("duration");
                }
                array = null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return duration;
    }


    private String createCSIMTxt(int msgtype, String body) {
        CustomLog.d(TAG, "createCSIMTxt beginmsgtype:" + msgtype
            + "|body:" + body);

        String text = "";
        if (NOTICE_TYPE_PHOTO_SEND == msgtype) {
            text = "[照片]";
        } else if (NOTICE_TYPE_VEDIO_SEND == msgtype) {
            text = "[视频]";
        } else if (NOTICE_TYPE_AUDIO_SEND == msgtype) {
            text = "[语音]";
        } else if (NOTICE_TYPE_VCARD_SEND == msgtype) {
            text = "[名片]";
        } else if (NOTICE_TYPE_TXT_SEND == msgtype) {

            if (!TextUtils.isEmpty(body)) {
                try {
                    JSONArray array = new JSONArray(body);
                    if (array != null && array.length() > 0) {
                        JSONObject item = array.getJSONObject(0);
                        text = item.optString("txt");
                    }
                    array = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else if (NOTICE_TYPE_MEETING_INVITE == msgtype) {
            JSONObject obj = new JSONObject();
            if (!TextUtils.isEmpty(body)) {
                try {
                    JSONArray array = new JSONArray(body);
                    if (array != null && array.length() > 0) {
                        JSONObject item = array.getJSONObject(0);

                        JSONObject cardInfo = new JSONObject();
                        cardInfo.put("inviterId", item.optString("inviterId"));
                        cardInfo.put("inviterName", item.optString("inviterName"));
                        cardInfo.put("inviterHeadUrl", item.optString("inviterHeadUrl"));
                        cardInfo.put("meetingRoom", item.optString("meetingRoom"));
                        cardInfo.put("meetingUrl", item.optString("meetingUrl"));
                        cardInfo.put("showMeeting", item.optBoolean("showMeeting"));

                        obj.put("meetingInfo", cardInfo);
                    }
                    array = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                obj.put("subtype", BizConstant.MSG_SUB_TYPE_MEETING);
                obj.put("text", "会议邀请");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            text = obj.toString();
        } else if (NOTICE_TYPE_MEETING_BOOK == msgtype) {
            JSONObject obj = new JSONObject();
            if (!TextUtils.isEmpty(body)) {
                try {
                    JSONArray array = new JSONArray(body);
                    if (array != null && array.length() > 0) {
                        JSONObject item = array.getJSONObject(0);
                        JSONObject cardInfo = new JSONObject();
                        cardInfo.put(BookMeetingExInfo.BOOK_NUBE,
                            item.optString(BookMeetingExInfo.BOOK_NUBE));
                        cardInfo.put(BookMeetingExInfo.BOOK_NAME,
                            item.optString(BookMeetingExInfo.BOOK_NAME));
                        cardInfo.put(BookMeetingExInfo.MEETING_ROOM,
                            item.optString(BookMeetingExInfo.MEETING_ROOM));
                        cardInfo.put(BookMeetingExInfo.MEETING_THEME,
                            item.optString(BookMeetingExInfo.MEETING_THEME));
                        cardInfo.put(BookMeetingExInfo.MEETING_TIME,
                            item.optLong(BookMeetingExInfo.MEETING_TIME));
                        cardInfo.put(BookMeetingExInfo.MEETING_URL,
                            item.optString(BookMeetingExInfo.MEETING_URL));
                        obj.put(BookMeetingExInfo.BOOK_MEETING_INFO, cardInfo);
                    }
                    array = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                obj.put("subtype", BizConstant.MSG_SUB_TYPE_MEETING_BOOK);
                obj.put("text", "会议预约");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            text = obj.toString();
        } else if (NOTICE_TYPE_FILE == msgtype) {
            JSONObject obj = new JSONObject();
            if (!TextUtils.isEmpty(body)) {
                try {
                    JSONArray array = new JSONArray(body);
                    if (array != null && array.length() > 0) {
                        JSONObject item = array.getJSONObject(0);

                        JSONObject cardInfo = new JSONObject();
                        cardInfo.put("size", item.optLong("size"));
                        cardInfo.put("fileName", item.optString("fileName"));
                        cardInfo.put("fileType", item.optString("fileType"));
                        cardInfo.put("localUrl", item.optString("localUrl"));
                        cardInfo.put("remoteUrl", item.optString("remoteUrl"));
                        obj.put("fileInfo", cardInfo);
                    }
                    array = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                obj.put("subtype", BizConstant.MSG_SUB_TYPE_FILE);
                obj.put("ver", BizConstant.MSG_VERSION);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            text = obj.toString();
        } else if (NOTICE_TYPE_CHATRECORD_SEND == msgtype) {

            JSONArray array = null;
            try {
                array = new JSONArray(body);
                JSONObject item = array.getJSONObject(0);
                JSONArray detailArray = item.optJSONArray("chatrecordInfo");
                for (int i = 0; i < detailArray.length(); i++) {
                    JSONObject obj = detailArray.getJSONObject(i);
                    switch (obj.optInt("type")) {
                        case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                        case FileTaskManager.NOTICE_TYPE_REMIND_SEND:
                        case FileTaskManager.NOTICE_TYPE_REMIND_ONE_SEND:
                            obj.put("type", BizConstant.MSG_BODY_TYPE_TXT);
                            break;
                        case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                            obj.put("type", BizConstant.MSG_BODY_TYPE_VIDEO_2);
                            break;
                        case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                            obj.put("type", BizConstant.MSG_BODY_TYPE_PIC_2);
                            break;
                        case FileTaskManager.NOTICE_TYPE_ARTICAL_SEND:
                            obj.put("type", BizConstant.MSG_BODY_TYPE_ARTICLE);
                            break;
                        case FileTaskManager.NOTICE_TYPE_VCARD_SEND:
                            obj.put("type", BizConstant.MSG_BODY_TYPE_POSTCARD);
                            break;
                        default:
                            break;
                    }
                }
                text = item.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (NOTICE_TYPE_ARTICAL_SEND == msgtype) {
            JSONArray array = null;
            try {
                array = new JSONArray(body);
                JSONObject item = array.getJSONObject(0);
                text = item.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (NOTICE_TYPE_REMIND_SEND == msgtype
            || NOTICE_TYPE_REMIND_ONE_SEND == msgtype) {
            JSONArray array = null;
            try {
                array = new JSONArray(body);
                JSONObject item = array.getJSONObject(0);
                text = item.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return text;
    }


    private String createDTCSIMAppExtInfo(int msgtype, String snipTxt, String body, String extInfo, String bodytype) {
        CustomLog.d(TAG, "createDTCSIMAppExtInfo begin msgtype:" + msgtype
            + "|snipTxt:" + snipTxt
            + "|body:" + body
            + "|extInfo:" + extInfo
            + "|bodytype:" + bodytype);
        ;

        String msghead = getMsgHead(msgtype, snipTxt);
        if (TextUtils.isEmpty(msghead)) {
            msghead = getMsgHeadByStrType(bodytype, snipTxt);
        }

        JSONObject obj = new JSONObject();
        if (NOTICE_TYPE_PHOTO_SEND == msgtype || NOTICE_TYPE_VEDIO_SEND == msgtype) {

            if (!TextUtils.isEmpty(body)) {
                try {
                    JSONArray array = new JSONArray(body);
                    if (array != null && array.length() > 0) {
                        JSONObject item = array.getJSONObject(0);
                        obj.put("width", item.optString("width"));
                        obj.put("height", item.optString("height"));
                    }
                    array = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (!TextUtils.isEmpty(extInfo)) {
                try {
                    JSONObject extObject = new JSONObject(extInfo);
                    obj.put("fileName", extObject.optString("fileName"));
                    obj.put("fileSize", extObject.optString("fileSize"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else if (NOTICE_TYPE_VEDIO_SEND == msgtype) {

            if (!TextUtils.isEmpty(extInfo)) {
                try {
                    JSONObject extObject = new JSONObject(extInfo);
                    obj.put("fileName", extObject.optString("fileName"));
                    obj.put("fileSize", extObject.optString("fileSize"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else if (NOTICE_TYPE_AUDIO_SEND == msgtype) {

            if (!TextUtils.isEmpty(extInfo)) {
                try {
                    JSONObject extObject = new JSONObject(extInfo);
                    obj.put("fileName", extObject.optString("fileName"));
                    obj.put("fileSize", extObject.optString("fileSize"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else if (NOTICE_TYPE_VCARD_SEND == msgtype) {

            if (!TextUtils.isEmpty(body)) {
                try {
                    JSONArray array = new JSONArray(body);
                    if (array != null && array.length() > 0) {
                        JSONObject item = array.getJSONObject(0);

                        JSONArray cardArray = new JSONArray();
                        JSONObject cardInfo = new JSONObject();
                        cardInfo.put("code", item.optString("code"));
                        cardInfo.put("name", item.optString("name"));
                        cardInfo.put("phone", item.optString("phone"));
                        cardInfo.put("url", item.optString("url"));
                        cardInfo.put("userid", item.optString("userid"));
                        cardInfo.put("sex", item.optString("sex"));
                        cardArray.put(cardInfo);

                        obj.put("card", cardArray.toString());
                    }
                    array = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (!TextUtils.isEmpty(extInfo)) {
                try {
                    JSONObject extObject = new JSONObject(extInfo);
                    obj.put("fileName", extObject.optString("fileName"));
                    obj.put("fileSize", extObject.optString("fileSize"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            try {
                String mobile = MedicalApplication.getPreference().getKeyValue(
                    PrefType.LOGIN_MOBILE, "");
                obj.put("caller_mobile_num", mobile);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (NOTICE_TYPE_TXT_SEND == msgtype) {
            // do nothing
        } else if (NOTICE_TYPE_MEETING_INVITE == msgtype) {
            // do nothing
        } else if (NOTICE_TYPE_FILE == msgtype) {
            // do nothing
        } else {
            // do nothing
        }

        try {
            obj.put("ver", BizConstant.MSG_VERSION);
            obj.put("msgHead", msghead);
            obj.put("medicalComboMsg", 1); // 医联体添加特殊扩展字段
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj.toString();
    }


    private String createCSIMAppExtInfo(int msgtype, String snipTxt, String body, String extInfo, String bodytype) {
        CustomLog.d(TAG, "createCSIMAppExtInfo begin msgtype:" + msgtype
            + "|snipTxt:" + snipTxt
            + "|body:" + body
            + "|extInfo:" + extInfo
            + "|bodytype:" + bodytype);
        ;

        String msghead = getMsgHead(msgtype, snipTxt);
        if (TextUtils.isEmpty(msghead)) {
            msghead = getMsgHeadByStrType(bodytype, snipTxt);
        }

        JSONObject obj = new JSONObject();
        if (NOTICE_TYPE_PHOTO_SEND == msgtype || NOTICE_TYPE_VEDIO_SEND == msgtype) {

            if (!TextUtils.isEmpty(body)) {
                try {
                    JSONArray array = new JSONArray(body);
                    if (array != null && array.length() > 0) {
                        JSONObject item = array.getJSONObject(0);
                        obj.put("width", item.optString("width"));
                        obj.put("height", item.optString("height"));
                    }
                    array = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (!TextUtils.isEmpty(extInfo)) {
                try {
                    JSONObject extObject = new JSONObject(extInfo);
                    obj.put("fileName", extObject.optString("fileName"));
                    obj.put("fileSize", extObject.optString("fileSize"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else if (NOTICE_TYPE_VEDIO_SEND == msgtype) {

            if (!TextUtils.isEmpty(extInfo)) {
                try {
                    JSONObject extObject = new JSONObject(extInfo);
                    obj.put("fileName", extObject.optString("fileName"));
                    obj.put("fileSize", extObject.optString("fileSize"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else if (NOTICE_TYPE_AUDIO_SEND == msgtype) {

            if (!TextUtils.isEmpty(extInfo)) {
                try {
                    JSONObject extObject = new JSONObject(extInfo);
                    obj.put("fileName", extObject.optString("fileName"));
                    obj.put("fileSize", extObject.optString("fileSize"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else if (NOTICE_TYPE_VCARD_SEND == msgtype) {

            if (!TextUtils.isEmpty(body)) {
                try {
                    JSONArray array = new JSONArray(body);
                    if (array != null && array.length() > 0) {
                        JSONObject item = array.getJSONObject(0);

                        JSONArray cardArray = new JSONArray();
                        JSONObject cardInfo = new JSONObject();
                        cardInfo.put("code", item.optString("code"));
                        cardInfo.put("name", item.optString("name"));
                        cardInfo.put("phone", item.optString("phone"));
                        cardInfo.put("url", item.optString("url"));
                        cardInfo.put("userid", item.optString("userid"));
                        cardInfo.put("sex", item.optString("sex"));
                        cardArray.put(cardInfo);

                        obj.put("card", cardArray.toString());
                    }
                    array = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (!TextUtils.isEmpty(extInfo)) {
                try {
                    JSONObject extObject = new JSONObject(extInfo);
                    obj.put("fileName", extObject.optString("fileName"));
                    obj.put("fileSize", extObject.optString("fileSize"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            try {
                String mobile = MedicalApplication.getPreference().getKeyValue(
                    PrefType.LOGIN_MOBILE, "");
                obj.put("caller_mobile_num", mobile);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (NOTICE_TYPE_TXT_SEND == msgtype) {
            // do nothing
        } else if (NOTICE_TYPE_MEETING_INVITE == msgtype) {
            // do nothing
        } else if (NOTICE_TYPE_FILE == msgtype) {
            // do nothing
        } else {
            // do nothing
        }

        try {
            obj.put("ver", BizConstant.MSG_VERSION);
            obj.put("msgHead", msghead);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj.toString();
    }


    private String getMsgHeadByStrType(String bodytype, String snipTxt) {

        String offlineMsg = "";
        String typecontext = "";
        String name = ShowNameUtil.getShowName(ShowNameUtil.getNameElement("",
            userAccountInfo.nickName,
            userAccountInfo.mobile,
            userAccountInfo.nube));

        if (BizConstant.MSG_BODY_TYPE_POSTCARD.equals(bodytype)) {
            //typecontext = BizConstant.MSG_TYPE_POSTCARD_SEND_SM3S;
            typecontext = "[名片]";
            offlineMsg = name + ":" + typecontext;
        } else if (BizConstant.MSG_BODY_TYPE_PIC_2.equals(bodytype)) {
            //typecontext = BizConstant.MSG_TYPE_PHOTO_SM1_2;
            typecontext = "[图片]";
            offlineMsg = name + ":" + typecontext;
        } else if (BizConstant.MSG_BODY_TYPE_VIDEO_2.equals(bodytype)) {
            //typecontext = BizConstant.MSG_TYPE_VIDEO_SM2_2;
            typecontext = "[视频]";
            offlineMsg = name + ":" + typecontext;
        } else if (BizConstant.MSG_BODY_TYPE_AUDIO.equals(bodytype)) {
            //typecontext = BizConstant.MSG_TYPE_AUDIO_SEND_SM3AU;
            typecontext = "[语音]";
            offlineMsg = name + ":" + typecontext;
        } else if (BizConstant.MSG_BODY_TYPE_TXT.equals(bodytype)) {
            //			if(TextUtils.isEmpty(snipTxt)){
            //				typecontext = BizConstant.MSG_TYPE_TXT_SM3TXT;
            //			}else{
            //				typecontext = BizConstant.MSG_TYPE_TXT_SM3TXT+snipTxt;
            //			}
            typecontext = snipTxt;
            offlineMsg = name + ":" + typecontext;
        } else if (BizConstant.MSG_BODY_TYPE_VCARD.equals(bodytype)) {
            //typecontext = BizConstant.MSG_TYPE_VCARD_SEND_SM3;
            typecontext = "有一条好友邀请消息";
            offlineMsg = typecontext;
        } else if (BizConstant.MSG_BODY_TYPE_MULTITRUST.equals(bodytype)) {
            //typecontext = BizConstant.MSG_TYPE_VCARD_RECEIVE_SM4;
            typecontext = "通过了你的好友邀请";
            offlineMsg = name + ":" + typecontext;
        } else if (BizConstant.MSG_BODY_TYPE_ONEKEYVISIT.equals(bodytype)) {
            //typecontext = BizConstant.MSG_TYPE_ONEKEYVISIT_SEND_SM3OK;

        } else if (BizConstant.MSG_BODY_TYPE_TXT.equals(bodytype)) {

        } else if (BizConstant.MSG_SUB_TYPE_CHATRECORD.equals(bodytype)) {
            typecontext = "[聊天记录]";
            offlineMsg = name + ":" + typecontext;
        } else if (BizConstant.MSG_SUB_TYPE_ARTICLE.equals(bodytype)) {
            typecontext = "[文章]";
            offlineMsg = name + ":" + typecontext;
        } else {
            //			if(TextUtils.isEmpty(snipTxt)){
            //				typecontext = BizConstant.MSG_TYPE_TXT_SM3TXT;
            //			}else{
            //				typecontext = BizConstant.MSG_TYPE_TXT_SM3TXT+snipTxt;
            //			}
        }

        return offlineMsg;
    }


    private String getMsgHead(int type, String snipTxt) {
        CustomLog.i(TAG, "getMsgHead()");

        String offlineMsg = "";
        String typecontext = "";
        String name = ShowNameUtil.getShowName(ShowNameUtil.getNameElement("",
            userAccountInfo.nickName,
            userAccountInfo.mobile,
            userAccountInfo.nube));

        switch (type) {
            case NOTICE_TYPE_VCARD_SEND:
                //typecontext = BizConstant.MSG_TYPE_POSTCARD_SEND_SM3S;
                typecontext = "[名片]";
                offlineMsg = name + ":" + typecontext;
                break;
            case NOTICE_TYPE_PHOTO_SEND:
                //typecontext = BizConstant.MSG_TYPE_PHOTO_SM1_2;
                typecontext = "发来一张图片";
                offlineMsg = name + typecontext;
                break;
            case NOTICE_TYPE_VEDIO_SEND:
                //typecontext = BizConstant.MSG_TYPE_VIDEO_SM2_2;
                typecontext = "发来一段视频";
                offlineMsg = name + typecontext;
                break;
            case NOTICE_TYPE_AUDIO_SEND:
                //typecontext = BizConstant.MSG_TYPE_AUDIO_SEND_SM3AU;
                typecontext = "发来一段语音";
                offlineMsg = name + typecontext;
                break;
            case NOTICE_TYPE_FILE:
                typecontext = "[文件]";
                offlineMsg = name + ":" + typecontext;
                break;
            case NOTICE_TYPE_MEETING_INVITE:
                typecontext = snipTxt;
                offlineMsg = name + typecontext;
                break;
            case NOTICE_TYPE_MEETING_BOOK:
                typecontext = snipTxt;
                offlineMsg = name + typecontext;
                break;
            case NOTICE_TYPE_TXT_SEND:
                typecontext = snipTxt;
                offlineMsg = name + ":" + typecontext;
                break;
            case NOTICE_TYPE_FRIEND_SEND:
                typecontext = "有一条好友邀请消息";
                offlineMsg = typecontext;
                break;
            case NOTICE_TYPE_FEEEDBACK_SEND:
                typecontext = "通过了你的好友邀请";
                offlineMsg = name + ":" + typecontext;
                break;
            case NOTICE_TYPE_CHATRECORD_SEND:
                typecontext = snipTxt;
                offlineMsg = name + ":" + typecontext;
                break;
            case NOTICE_TYPE_ARTICAL_SEND:
                typecontext = snipTxt;
                offlineMsg = name + ":" + typecontext;
                break;
            case NOTICE_TYPE_REMIND_SEND:
                typecontext = snipTxt;
                offlineMsg = name + ":" + typecontext;
                break;
            case NOTICE_TYPE_REMIND_ONE_SEND:
                typecontext = snipTxt;
                offlineMsg = name + typecontext;
                break;
            default:

        }
        return offlineMsg;
    }


    private String getGroupMemberName(String gid, String number) {
        GroupDao groupDao = new GroupDao(mcontext);

        GroupMemberBean bean = groupDao.queryGroupMember(gid, number);
        String name = bean != null ? bean.getDispName() : number;
        return name;
    }


    /**
     * 截取文字消息的前25个字符
     */
    private String getSnipTxt(NoticesBean bean) {
        String snipTxt = "";
        if (bean != null && NOTICE_TYPE_TXT_SEND == bean.getType()) {
            String body = bean.getBody();
            if (!TextUtils.isEmpty(body)) {
                try {
                    JSONArray array = new JSONArray(body);
                    if (array != null && array.length() > 0) {
                        JSONObject item = array.getJSONObject(0);
                        snipTxt = item.optString("txt");
                    }
                    array = null;
                    if (!TextUtils.isEmpty(snipTxt) && snipTxt.length() > 25) {
                        snipTxt = snipTxt.substring(0, 26) + "...";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return snipTxt;
    }


    /**
     * @param receiver 接收者
     * @param uuid 收藏记录的ID
     * @param position ：-1，表示分享全部收藏记录。0表示分享第一个位置的记录，1表示分享第二个位置上的记录，依次类推
     */
    public boolean forwardMessageForCollection(String receiver, String uuid, int position) {
        LogUtil.begin("receiver:" + receiver + "|uuid:" + uuid + "|position=" + position);
        if (TextUtils.isEmpty(receiver)) {
            LogUtil.d("receiver 为空");
            return false;
        }
        List<String> msgIds = buildForwardMsgByCollection(receiver,
            mCollectionDao.getCollectionEntityById(uuid), position);
        for (String id : msgIds) {
            addTask(id, null);
        }
        return msgIds.size() > 0;
    }


    /**
     * @param receiver 接收者
     * @param itemInfo 收藏记录的详细信息
     * @param position ：-1，表示分享全部收藏记录。0表示分享第一个位置的记录，1表示分享第二个位置上的记录，依次类推
     */
    public boolean forwardMessageForCollectionOther(String receiver, DataBodyInfo itemInfo, int position) {
        LogUtil.begin("receiver:" + receiver + "|uuid:" + itemInfo + "|position=" + position);
        if (TextUtils.isEmpty(receiver)) {
            LogUtil.d("receiver 为空");
            return false;
        }
        List<String> msgIds = buildForwardMsgByCollection(receiver,
            convertCollectDeteilInfoToEntity(itemInfo), position);
        for (String id : msgIds) {
            addTask(id, null);

        }
        return msgIds.size() > 0;
    }


    public void forwardArticleMessage(String receiver, DataBodyInfo itemInfo, int position) {
        LogUtil.begin("receiver:" + receiver + "|uuid:" + itemInfo + "|position=" + position);
        if (TextUtils.isEmpty(receiver)) {
            LogUtil.d("receiver 为空");
            return;
        }
        List<String> msgIds = buildForwardMsgByCollection(receiver,
            convertCollectDeteilInfoToEntity(itemInfo), position);
        for (String id : msgIds) {
            addTask(id, null);

        }
    }


    private CollectionEntity convertCollectDeteilInfoToEntity(DataBodyInfo item) {
        CollectionEntity entity = new CollectionEntity();
        try {

            if (item.getCollecTime().length() > 0) {
                entity.setOperateTime(Long.parseLong(item.getCollecTime()));
            }
            entity.setType(item.getType());
            JSONObject itemObj = new JSONObject();
            JSONArray itemArray = new JSONArray();

            if (item.getType() == 8) {  //文本
                itemObj.put("txt", item.getTxt());
                itemArray.put(0, itemObj);
                entity.setBody(itemArray.toString());
            } else if (item.getType() == 3) { //视频
                itemObj.putOpt("fileName",
                    item.getFileName().length() > 0 ? item.getFileName() : "");
                itemObj.putOpt("remoteUrl",
                    item.getRemoteUrl().length() > 0 ? item.getRemoteUrl() : "");
                itemObj.putOpt("duration", item.getDuration());
                itemObj.putOpt("localUrl",
                    item.getLocalUrl().length() > 0 ? item.getLocalUrl() : "");
                itemObj.putOpt("thumbnailRemoteUrl",
                    item.getThumbnailRemoteUrl().length() > 0 ? item.getThumbnailRemoteUrl() : "");
                itemObj.putOpt("size", item.getSize());
                itemObj.putOpt("height", item.getPhotoHeight());
                itemObj.putOpt("width", item.getPhotoWidh());
                itemArray.put(0, itemObj);
                entity.setBody(itemArray.toString());
            } else if (item.getType() == 2) { //图片

                itemObj.putOpt("remoteUrl",
                    item.getRemoteUrl().length() > 0 ? item.getRemoteUrl() : "");
                itemObj.putOpt("duration", item.getDuration());
                itemObj.putOpt("localUrl",
                    item.getLocalUrl().length() > 0 ? item.getLocalUrl() : "");
                itemObj.putOpt("thumbnailRemoteUrl",
                    item.getThumbnailRemoteUrl().length() > 0 ? item.getThumbnailRemoteUrl() : "");
                itemObj.putOpt("size", item.getSize());
                itemObj.putOpt("height", item.getPhotoHeight());
                itemObj.putOpt("width", item.getPhotoWidh());
                itemObj.putOpt("compressPath", "");
                itemArray.put(0, itemObj);
                entity.setBody(itemArray.toString());
            } else if (item.getType() == FileTaskManager.NOTICE_TYPE_ARTICAL_SEND)//文章
            {
                itemObj.putOpt("articleId", item.getArticleId());
                itemObj.putOpt("title", item.getTitle());
                itemObj.putOpt("previewUrl", item.getPreviewUrl());
                itemObj.putOpt("introduction", item.getIntroduction());
                itemObj.putOpt("articleType", item.getArticleType());
                itemObj.putOpt("officeName", item.getName());
                entity.setBody(itemObj.toString());
            } else if (item.getType() == FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND ||
                item.getType() == 55) {
                //55是收藏界面自己定义的类型，这边统一当成聊天记录来处理
                JSONArray array = new JSONArray();
                try {
                    for (int i = 0; i < item.combineInfoList.size(); i++) {
                        JSONObject tmpObj = new JSONObject();
                        DataBodyInfo itemInfo = item.combineInfoList.get(i);
                        tmpObj.put("userheadUrl", itemInfo.getForwarderHeaderUrl());
                        tmpObj.put("username", itemInfo.getForwarderName());
                        tmpObj.put("createtime", Long.parseLong(itemInfo.getMessageTime()));
                        switch (itemInfo.getType()) {
                            case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                            case FileTaskManager.NOTICE_TYPE_REMIND_SEND:
                            case FileTaskManager.NOTICE_TYPE_REMIND_ONE_SEND:
                                tmpObj.put("type", FileTaskManager.NOTICE_TYPE_TXT_SEND);
                                tmpObj.put("text", itemInfo.getTxt());
                                break;
                            case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                                tmpObj.put("type", FileTaskManager.NOTICE_TYPE_VEDIO_SEND);
                                tmpObj.put("fileName", itemInfo.getFileName().length() > 0
                                                       ? itemInfo.getFileName()
                                                       : "");
                                tmpObj.put("remoteUrl", itemInfo.remoteUrl);
                                tmpObj.put("duration", itemInfo.getDuration());
                                tmpObj.put("thumbnail",
                                    itemInfo.getThumbnailRemoteUrl().length() > 0
                                    ? itemInfo.getThumbnailRemoteUrl()
                                    : "");
                                tmpObj.put("size", itemInfo.getSize());
                                break;
                            case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                                tmpObj.put("type", FileTaskManager.NOTICE_TYPE_PHOTO_SEND);
                                tmpObj.put("remoteUrl", itemInfo.getRemoteUrl().length() > 0
                                                        ? itemInfo.getRemoteUrl()
                                                        : "");
                                tmpObj.put("thumbnail",
                                    itemInfo.getThumbnailRemoteUrl().length() > 0
                                    ? itemInfo.getThumbnailRemoteUrl()
                                    : "");
                                tmpObj.put("size", itemInfo.getSize());
                                tmpObj.put("height", itemInfo.getPhotoHeight());
                                tmpObj.put("width", itemInfo.getPhotoWidh());
                                break;
                            case FileTaskManager.NOTICE_TYPE_ARTICAL_SEND:
                                tmpObj.put("type", FileTaskManager.NOTICE_TYPE_ARTICAL_SEND);
                                tmpObj.put("articleId", itemInfo.getArticleId());
                                tmpObj.put("title", itemInfo.getTitle());
                                tmpObj.put("previewUrl", itemInfo.getPreviewUrl());
                                tmpObj.put("introduction", itemInfo.getIntroduction());
                                tmpObj.put("articleType", itemInfo.getArticleType());
                                tmpObj.put("officeName", itemInfo.getName());
                                tmpObj.put("isforwarded", itemInfo.getIsforwarded());
                                break;
                            case FileTaskManager.NOTICE_TYPE_VCARD_SEND:
                                tmpObj.put("type", FileTaskManager.NOTICE_TYPE_VCARD_SEND);
                                tmpObj.put("cardname", itemInfo.getCardname());
                                tmpObj.put("cardCode", itemInfo.getCardCode());
                                tmpObj.put("cardUrl", itemInfo.getCardUrl());
                                break;
                            default:
                                break;
                        }
                        array.put(i, tmpObj);
                    }

                } catch (Exception e) {
                    CustomLog.d(TAG, "收藏夹内转发聊天记录，解析json出错");
                }
                itemObj.put("text", item.getText());
                itemObj.put("subtype", "chat_record");
                itemObj.put("chatrecordInfo", array);
                itemArray.put(0, itemObj);
                entity.setBody(itemArray.toString());

            } else {
                CustomLog.d(TAG, "不能发送的收藏，无需解析");
            }
        } catch (Exception e) {
            CustomLog.d(TAG, e.toString());
        }
        return entity;
    }


    /**
     * 构建转发消息记录，返回新记录的ID
     *
     * @param receiver 接收对象的视频号（多个视频号用分号分割）
     * @param entity 将要转发的消息uuid
     * @param position ：-1，表示分享收藏记录。>0，表示将收藏记录拆分开，分享某个位置上的记录
     * @return 返回新记录的id
     */
    private List<String> buildForwardMsgByCollection(String receiver, CollectionEntity entity, int position) {
        List<String> ids = new ArrayList<String>();
        if (entity == null) {
            LogUtil.d("entity==null");
            return ids;
        }
        //链接类型转换成文字类型进行转发
        if (NOTICE_TYPE_URL == entity.getType()) {
            entity.setType(NOTICE_TYPE_TXT_SEND);
        }
        String sender = MedicalApplication.getPreference()
            .getKeyValue(PrefType.LOGIN_NUBENUMBER, "");
        String recipentIds = StringUtil.sortRecipentIds(receiver, ';');

        List<JSONArray> newArrays = getNewBodyList(entity.getBody(), position);
        for (JSONArray newArrary : newArrays) {
            NoticesBean bean = new NoticesBean();
            bean.setSender(sender);
            bean.setReciever(recipentIds);

            // added by zhaguitao on 20160622 for 转发收藏数据时，须将收藏body中的key（thumbnailRemoteUrl）转换成消息body中的key（thumbnail）
            // 本来android这边收藏也用thumbnail就好了，但ios客户端不愿意转成thumbnail，所以为了适配ios客户端，android客户端转换
            Map<String, String> keys = new HashMap<String, String>();
            keys.put("thumbnailRemoteUrl", "thumbnail");
            keys.put("photoWidth", "width");
            keys.put("photoHeight", "height");
            bean.setBody(CollectionManager.modifyBodyJsonKey(newArrary, keys));

            bean.setStatus(TASK_STATUS_READY);
            if (entity.getType() == 55) {
                entity.setType(FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND);
            }
            bean.setType(entity.getType());
            bean.setIsNew(0);
            bean.setIsRead(1);
            long curtime = System.currentTimeMillis();
            bean.setSendTime(curtime);
            bean.setReceivedTime(curtime);
            bean.setTitle("");
            String newItemuuid = StringUtil.getUUID();
            bean.setId(newItemuuid);
            bean.setMsgId(newItemuuid);
            bean.setFailReplyId("");
            JSONObject extObj = null;
            try {
                extObj = new JSONObject();
                extObj.put("id", bean.getId());
                extObj.put("text", "");
                extObj.put("ver", BizConstant.MSG_VERSION);
            } catch (JSONException e) {
                LogUtil.e("JSONException", e);
            }
            //            }

            if (extObj != null) {
                bean.setExtInfo(extObj.toString());
            } else {
                bean.setExtInfo("");
            }
            //TODO 添加转发群组
            ThreadsDao threadsDao = new ThreadsDao(mcontext);
            if (recipentIds.length() < 12) {
                String covstid = threadsDao.createThread(recipentIds, curtime, true);
                if (TextUtils.isEmpty(covstid)) {
                    LogUtil.d("createThread id==null");
                    return ids;
                }
                bean.setThreadsId(covstid);
            } else {
                if (!threadsDao.isExistThread(recipentIds)) {
                    threadsDao.createThreadFromGroup(recipentIds);
                } else {
                    threadsDao.updateLastTime(recipentIds, curtime);
                }
                bean.setThreadsId(recipentIds);
            }
            newItemuuid = noticedao.insertNotice(bean);
            LogUtil.end("newItemuuid:" + newItemuuid);
            ids.add(newItemuuid);
        }
        return ids;
    }


    private List<JSONArray> getNewBodyList(String body, int position) {
        List<JSONArray> newArrays = new ArrayList<JSONArray>();
        try {
            JSONArray oldBodyArray = new JSONArray(body);
            if (position == -1) {
                for (int i = 0; i < oldBodyArray.length(); i++) {
                    JSONArray newArray = new JSONArray();
                    newArray.put(oldBodyArray.get(i));
                    newArrays.add(newArray);
                }
            } else {
                JSONArray newArray = new JSONArray();
                newArray.put(oldBodyArray.get(position));
                newArrays.add(newArray);
            }
        } catch (JSONException e) {
            LogUtil.e("JSONException body", e);
        }
        return newArrays;
    }


    public static String getTakePhotoDir() {
        if (!initTakePhotoDir()) {
            return "";
        }

        String dirpath = Environment.getExternalStorageDirectory()
            + File.separator + IMConstant.APP_ROOT_FOLDER
            + File.separator + FILE_TAKE_PHOTO_DIR;
        return dirpath;
    }


    public static boolean initTakePhotoDir() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment
            .getExternalStorageState())) {
            return false;
        }

        String dirpath = Environment.getExternalStorageDirectory()
            + File.separator + IMConstant.APP_ROOT_FOLDER
            + File.separator + FILE_TAKE_PHOTO_DIR;
        File file = new File(dirpath);

        if (file.exists()) {
            return true;
        } else {
            return file.mkdirs();
        }
    }
}
