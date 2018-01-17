package cn.redcdn.hvs.im.fileTask;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.bean.CollectionEntity;
import cn.redcdn.hvs.im.bean.FileTaskBean;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.dao.CollectionDao;
import cn.redcdn.hvs.im.dao.DtNoticesDao;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.task.DownloadTask;
import cn.redcdn.log.CustomLog;

import com.butel.connectevent.utils.LogUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * 线程管理队列
 */

public class DownloadTaskManager {
    private Context mContext;
    // handler消息
    public static final int MSG_SUCCESS = 2001;
    public static final int MSG_FAILURE = 2002;
    public static final int NO_SDCARD = 2006;

    // 每条消息（FileTaskBean）的状态
    public static final int TASK_STATUS_READY = 0;
    public static final int TASK_STATUS_RUNNING = 1;
    public static final int TASK_STATUS_SUCCESS = 2;
    public static final int TASK_STATUS_FAIL = 3;
    // 接收到的文件保存的文件夹
    public static String FILE_DOWNLOAD_DIR = "download";
    // 临时文件保存路径
    public static String TEMP_DIR = "temp";
    // 收藏的文件的保存的文件夹
    public static String FILE_COLLECTION_DIR = "collections";
    private Map<String, DownloadTask> downloadTasks;
    private Map<String, FileTaskBean> templist;

    private static DownloadTaskManager downloadTaskMananger;

    private CollectionDao collectionDao;
    private NoticesDao mNoticeDao;
    private DtNoticesDao mDtNoticeDao;

    // 创建一个可重用固定线程数的线程池
    private ExecutorService pool = Executors.newFixedThreadPool(3);
    // 5个线程并发下载，提高下载速度
    private static int httpThreadCount = 5;
    private static String CollectionPath = Environment.getExternalStorageDirectory()
            + File.separator + IMConstant.APP_ROOT_FOLDER
            + File.separator + FILE_COLLECTION_DIR;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            Thread tread = new Thread(r, "HttpUtils #" + mCount.getAndIncrement());
            tread.setPriority(Thread.NORM_PRIORITY - 1);
            return tread;
        }
    };

    private static final Executor executor = Executors.newFixedThreadPool(httpThreadCount, sThreadFactory);
    private DownloadTaskManager(Context mContext) {

        this.mContext = mContext;
        downloadTasks = new ConcurrentHashMap<String, DownloadTask>();
        collectionDao = new CollectionDao(mContext);
        mNoticeDao = new NoticesDao(mContext);
        mDtNoticeDao = new DtNoticesDao(mContext);
        templist = new ConcurrentHashMap<String, FileTaskBean>();
        initCollectionDir();
    }

    public static synchronized DownloadTaskManager getInstance(Context mContext) {
        if (downloadTaskMananger == null) {
            downloadTaskMananger = new DownloadTaskManager(mContext);
        }
        return downloadTaskMananger;
    }

    public static boolean initCollectionDir() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            return false;
        }
        File file = new File(CollectionPath);
        if (file.exists()) {
            return true;
        } else {
            return file.mkdirs();
        }
    }

    public void downloadFile(final String uuid, String remoteSrc,
                             final boolean froceDownload, final ChangeUIInterface uiInterfaces,int pos) {
        boolean existed = false;
        FileTaskBean fileTaskBean = null;
        if (TextUtils.isEmpty(remoteSrc)) {
            fileTaskBean = findTaskFromQueue(uuid);
        } else {
            fileTaskBean = findTaskFromQueue(remoteSrc);
        }
        if (fileTaskBean == null) {
            // 队列中没有该任务
            fileTaskBean = createFileTaskList(uuid, remoteSrc,
                    froceDownload,pos);
        } else {
            existed = true;
        }
        if (fileTaskBean == null) {
            return;
        }
        if (uiInterfaces != null) {
            fileTaskBean.setChangui(uiInterfaces);
            fileTaskBean.setPauseUiChange(false);
        }
        if (!existed && fileTaskBean.getStatus() == TASK_STATUS_READY) {
            fileTaskBean.setSingleDownload(true);
        }

        LogUtil.d("bean.getSrcUrl=" + fileTaskBean.getSrcUrl());
        if(TextUtils.isEmpty(remoteSrc)){
            addToTempList(uuid, fileTaskBean);
        }else{
            addToTempList(remoteSrc, fileTaskBean);
        }

        if (fileTaskBean != null
                && (fileTaskBean.getStatus() == TASK_STATUS_READY || fileTaskBean
                .getStatus() == TASK_STATUS_FAIL)) {

            DownFileRequestCallBack callback = new DownFileRequestCallBack(
                    fileTaskBean, syncHandler);
            // 添加下载任务到任务管理器
            if (TextUtils.isEmpty(remoteSrc)) {
                addDownloadTask(uuid, new DownloadTask(uuid, callback));
            }else{
                addDownloadTask(remoteSrc, new DownloadTask(remoteSrc, callback));
            }
            // 从管理器中取出放入线程池
            DownloadTask downloadTask;
            if (TextUtils.isEmpty(remoteSrc)) {
                downloadTask = getDownloadTask(uuid);
            } else {
                downloadTask = getDownloadTask(remoteSrc);
            }

            if (downloadTask != null) {
                LogUtil.d("execute:downloadTask by uuid="
                        + downloadTask.getFileId());
                executor.execute(downloadTask);
//				pool.execute(downloadTask);
            }
        }
    }

    private FileTaskBean findTaskFromQueue(String uuid) {
        LogUtil.d("begin----- uuid:" + uuid);
        if (TextUtils.isEmpty(uuid)) {
            return null;
        }
        if (templist == null || !templist.containsKey(uuid)) {
            return null;
        }
        FileTaskBean bean = templist.get(uuid);
        if (templist == null || templist.size() == 0) {
            return null;
        } else {
            return bean;
        }

    }

    private void addToTempList(String uuid, FileTaskBean bean) {
        if (!templist.containsKey(uuid)) {
            templist.put(uuid, bean);
        }
    }

    // 1.先执行
    public boolean addDownloadTask(String uuid, DownloadTask downloadTask) {
        synchronized (downloadTasks) {
            if (!downloadTasks.containsKey(uuid)) {
                // 增加下载任务
                downloadTasks.put(uuid, downloadTask);
                return false;
            } else {
                return true;
            }
        }

    }

    // /**
    // * 该任务是否已在任务队列中
    // *
    // * @param fileId
    // * @return
    // */
    // public boolean isTaskRepeat(String fileId) {
    // synchronized (taskIdSet) {
    // if (taskIdSet.contains(fileId)) {
    // return true;
    // } else {
    // BLog.d("下载管理器增加下载任务：" + fileId);
    // taskIdSet.add(fileId);
    // return false;
    // }
    // }
    // }

    public DownloadTask getDownloadTask(String uuid) {
        synchronized (downloadTasks) {
            if (downloadTasks.size() > 0) {
                DownloadTask downloadTask = downloadTasks.get(uuid);
//				LogUtil.d("取出下载任务：" + downloadTask.getFileId());
                return downloadTask;
            }
        }
        return null;
    }

    /***
     * 根据UUID，把动态消息的一条记录转换为FileTaskBean list
     *
     * @param uuid
     * @return
     */
    public synchronized FileTaskBean createFileTaskList(String uuid,
                                                        String srcpath, boolean froceDownload,int pos) {
        LogUtil.d("Begin ----uuid:" + uuid);
        CollectionEntity bean = null;
        NoticesBean nBean = null;
        if(pos == -2){
            nBean = mNoticeDao.getNoticeById(uuid);
            if (nBean == null){
                nBean = mDtNoticeDao.getNoticeById(uuid);
            }
        }else{
            bean = collectionDao.getCollectionEntityById(uuid);
        }
        if (bean != null||nBean!=null) {
            boolean from = false;

            String own = AccountManager.getInstance(mContext).getNube();
            if (bean!= null&&bean.getOperatorNube().endsWith(own)) {
                from = false;
            } else {
                from = true;
            }

            if (nBean!= null&&nBean.getSender().endsWith(own)) {
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
            if (!from && bean != null&&bean.getStatus() == TASK_STATUS_SUCCESS) {
                LogUtil.d("createFileTaskList 发送任务已成功，无需再次发送");
                return null;
            }

            if (!from && nBean != null&&nBean.getStatus() == TASK_STATUS_SUCCESS) {
                LogUtil.d("createFileTaskList 发送任务已成功，无需再次发送");
                return null;
            }

            if (TextUtils.isEmpty(srcpath)) {
                if(bean!=null){
                    return getFileTaskListByBody(uuid, bean.getBody(),
                            bean.getType(), from, "",pos, true);
                }else{
                    return getFileTaskListByBody(uuid, nBean.getBody(),
                            nBean.getType(), from, "",pos, false);
                }
            } else {
                return getFileTaskListByBody(srcpath, bean.getBody(),
                        bean.getType(), from, uuid,pos, true);
            }
        }
        return null;
    }

    /***
     * 解析body中的JSON串， 把动态消息的一条记录转换为FileTaskBean list
     *
     * @param uuid
     * @param body
     * @param type
     * @param from
     *            true:接收到的消息; false:发送的消息
     * @return
     */
    public synchronized FileTaskBean getFileTaskListByBody(String uuid,
                                                           String body, int type, boolean from, String id,int pos, boolean isCollection) {

        LogUtil.d("uuid:" + uuid + "|body:" + body + "|type:" + type + "|from:"
                + from);

        if (!TextUtils.isEmpty(body)) {
            FileTaskBean taskBean = null;
            switch (type) {
                case FileTaskManager.NOTICE_TYPE_AUDIO_SEND:
                case FileTaskManager.NOTICE_TYPE_FILE:
                case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                case FileTaskManager.NOTICE_TYPE_ATTACHMENT_FILE:
                    try {
                        JSONArray array = new JSONArray(body);
                        JSONObject obj = null;
                        taskBean = new FileTaskBean();
                        taskBean.setFrom(from);
                        if (TextUtils.isEmpty(id)) {
                            obj = array.getJSONObject(0);
                        } else {
                            obj = array.getJSONObject(pos);
                        }
                        taskBean.setRawBodyItemData(obj.toString());
                        taskBean.setPos(pos);
                        String local = obj.optString("localUrl");
                        String romote = obj.optString("remoteUrl");
                        long filesize = obj.optLong("size");
                        String thumb = "";
                        if (isCollection) {
                            thumb = obj.optString("thumbnailRemoteUrl");
                        } else {
                            thumb = obj.optString("thumbnail");
                        }
                        String compressedpath = obj.optString("compressPath");
                        LogUtil.d("romote=" + romote);
                        LogUtil.d("local=" + local);
                        // boolean oversize = obj.optBoolean("overSize");
                        taskBean.setCompressedPath(compressedpath);
                        taskBean.setFilesize(filesize);
                        String localpath = "";
                        if (!from) {
                            // 发送的消息，本地路径为源，服务器端的URL为结果
                            taskBean.setSrcUrl(local);
                            taskBean.setResultUrl(romote);
                        } else {
                            // 接收的消息，服务器端的URL为源，本地路径为结果
                            taskBean.setSrcUrl(romote);
                            //						taskBean.setResultUrl(local);
                            if(type == FileTaskManager.NOTICE_TYPE_FILE){
                                String endPath = getFiletypeByFileName(romote);
                                localpath = CollectionPath+File.separator+System.currentTimeMillis()+"."+endPath;
                            }else{
                                localpath = CollectionPath+File.separator+System.currentTimeMillis();
                            }
                            taskBean.setResultUrl(localpath);

                        }
                        taskBean.setThumbnailUrl(thumb);
                        taskBean.setUuid(uuid);
                        taskBean.setType(type);
                        taskBean.setIndex(0);
                        if (TextUtils.isEmpty(id)) {
                            taskBean.setId(uuid);
                        } else {
                            taskBean.setId(id);
                            taskBean.setPos(pos);
                        }
                        // 一条消息中最多只有一个文件【图片/视频/语音】
                        // --一次发送n张图片，是当作n条图片消息来处理的--on 2015/7/24 by wxy
                        taskBean.setTotal_count(1);
                        if (!from) {
                        } else {
                            if (!TextUtils.isEmpty(local)) {
                                String filepath = getLocalPathFromURL(romote,
                                    taskBean.getUuid(), false);
                                File file = new File(filepath);
                                if (file != null && file.exists()) {
                                    //								taskBean.setResultUrl(local);
                                    if(type == FileTaskManager.NOTICE_TYPE_FILE){
                                        String endPath = getFiletypeByFileName(romote);
                                        localpath = CollectionPath+File.separator+System.currentTimeMillis()+"."+endPath;
                                    }else{
                                        localpath = CollectionPath+File.separator+System.currentTimeMillis();
                                    }

                                    // 接收到的消息，本地文件路径不为空，且文件存在
                                    taskBean.setStatus(TASK_STATUS_SUCCESS);
                                } else {
                                    // 接收到的消息，本地文件路径不为空，但文件不存在
                                    // 需要重新下载
                                    String tmpfilepath = getLocalPathFromURL(
                                        romote, taskBean.getUuid(), true);
                                    //								taskBean.setResultUrl(local);
                                    if(type == FileTaskManager.NOTICE_TYPE_FILE){
                                        String endPath = getFiletypeByFileName(romote);
                                        localpath = CollectionPath+File.separator+System.currentTimeMillis()+"."+endPath;
                                    }else{
                                        localpath = CollectionPath+File.separator+System.currentTimeMillis();
                                    }
                                    taskBean.setStatus(TASK_STATUS_READY);
                                }
                            } else {
                                // 接收到的消息,本地文件路径为空,以temp为后缀的临时文件
                                // 进行下载
                                //							taskBean.setResultUrl(local);
                                if(type == FileTaskManager.NOTICE_TYPE_FILE){
                                    String endPath = getFiletypeByFileName(romote);
                                    localpath = CollectionPath+File.separator+System.currentTimeMillis()+"."+endPath;
                                }else{
                                    localpath = CollectionPath+File.separator+System.currentTimeMillis();
                                }
                                taskBean.setStatus(TASK_STATUS_READY);
                            }
                        }

                    } catch (JSONException e) {
                        LogUtil.e("JSONException", e);
                    }
                case FileTaskManager.NOTICE_TYPE_PHOTO_SEND: {
                    try {
                        JSONArray array = new JSONArray(body);
                        JSONObject obj = null;
                        taskBean = new FileTaskBean();
                        taskBean.setFrom(from);
                        if (TextUtils.isEmpty(id)) {
                            obj = array.getJSONObject(0);
                        } else {
                            obj = array.getJSONObject(pos);
                        }
                        taskBean.setRawBodyItemData(obj.toString());
                        taskBean.setPos(pos);
                        String local = obj.optString("localUrl");
                        String romote = obj.optString("remoteUrl");
                        long filesize = obj.optLong("size");
                        String thumb = "";
                        if (isCollection) {
                            thumb = obj.optString("thumbnailRemoteUrl");
                        } else {
                            thumb = obj.optString("thumbnail");
                        }
                        String compressedpath = obj.optString("compressPath");
                        LogUtil.d("romote=" + romote);
                        LogUtil.d("local=" + local);
                        // boolean oversize = obj.optBoolean("overSize");
                        taskBean.setCompressedPath(compressedpath);
                        taskBean.setFilesize(filesize);
                        String localpath = "";
                        if (!from) {
                            // 发送的消息，本地路径为源，服务器端的URL为结果
                            taskBean.setSrcUrl(local);
                            taskBean.setResultUrl(romote);
                        } else {
                            // 接收的消息，服务器端的URL为源，本地路径为结果
                            taskBean.setSrcUrl(romote);
//						taskBean.setResultUrl(local);
                            if(type == FileTaskManager.NOTICE_TYPE_FILE){
                                String endPath = getFiletypeByFileName(romote);
                                localpath = CollectionPath+File.separator+System.currentTimeMillis()+"."+endPath;
                            }else{
                                localpath = CollectionPath+File.separator+System.currentTimeMillis();
                            }
                            taskBean.setResultUrl(localpath);

                        }
                        taskBean.setThumbnailUrl(thumb);
                        taskBean.setUuid(uuid);
                        taskBean.setType(type);
                        taskBean.setIndex(0);
                        if (TextUtils.isEmpty(id)) {
                            taskBean.setId(uuid);
                        } else {
                            taskBean.setId(id);
                            taskBean.setPos(pos);
                        }
                        // 一条消息中最多只有一个文件【图片/视频/语音】
                        // --一次发送n张图片，是当作n条图片消息来处理的--on 2015/7/24 by wxy
                        taskBean.setTotal_count(1);
                        if (!from) {
                        } else {
                            if (!TextUtils.isEmpty(local)) {
                                String filepath = getLocalPathFromURL(romote,
                                        taskBean.getUuid(), false);
                                File file = new File(filepath);
                                if (file != null && file.exists()) {
//								taskBean.setResultUrl(local);
                                    if(type == FileTaskManager.NOTICE_TYPE_FILE){
                                        String endPath = getFiletypeByFileName(romote);
                                        localpath = CollectionPath+File.separator+System.currentTimeMillis()+"."+endPath;
                                    }else{
                                        localpath = CollectionPath+File.separator+System.currentTimeMillis();
                                    }

                                    // 接收到的消息，本地文件路径不为空，且文件存在
                                    taskBean.setStatus(TASK_STATUS_SUCCESS);
                                } else {
                                    // 接收到的消息，本地文件路径不为空，但文件不存在
                                    // 需要重新下载
                                    String tmpfilepath = getLocalPathFromURL(
                                            romote, taskBean.getUuid(), true);
//								taskBean.setResultUrl(local);
                                    if(type == FileTaskManager.NOTICE_TYPE_FILE){
                                        String endPath = getFiletypeByFileName(romote);
                                        localpath = CollectionPath+File.separator+System.currentTimeMillis()+"."+endPath;
                                    }else{
                                        localpath = CollectionPath+File.separator+System.currentTimeMillis();
                                    }
                                    taskBean.setStatus(TASK_STATUS_READY);
                                }
                            } else {
                                // 接收到的消息,本地文件路径为空,以temp为后缀的临时文件
                                // 进行下载
//							taskBean.setResultUrl(local);
                                if(type == FileTaskManager.NOTICE_TYPE_FILE){
                                    String endPath = getFiletypeByFileName(romote);
                                    localpath = CollectionPath+File.separator+System.currentTimeMillis()+"."+endPath;
                                }else{
                                    localpath = CollectionPath+File.separator+System.currentTimeMillis();
                                }
                                taskBean.setStatus(TASK_STATUS_READY);
                            }
                        }

                    } catch (JSONException e) {
                        LogUtil.e("JSONException", e);
                    }
                }
                break;

            }
            return taskBean;
        }
        return null;
    }

    public String getLocalPathFromURL(String url, String uuid, boolean tempfile) {
        LogUtil.d("url:" + url + "|tempfile:" + tempfile);
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        int index = url.lastIndexOf("/");
        String lastpart = url.substring(index);

        String path = Environment.getExternalStorageDirectory()
                + File.separator + IMConstant.APP_ROOT_FOLDER
                + File.separator + FILE_DOWNLOAD_DIR;
        String tmppath = path;
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
        path = path + lastpart;
        if (tempfile) {
            path = path + ".temp";
        }
        LogUtil.d("path:" + path);

        return path;
    }

    @SuppressLint("HandlerLeak")
    private Handler syncHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case NO_SDCARD:
                    LogUtil.d("NO_SDCARD,外部存储卡没有准备好，无法下载");
                    break;

                case FileTaskManager.MSG_SUCCESS: {
                    Bundle data = msg.getData();
                    String uuid = data.getString("uuid");
                    String srcUrl = data.getString("srcUrl");
                    LogUtil.d("MSG_SUCCESS,uuid:" + uuid + "|srcUrl:" + srcUrl);

                    FileTaskBean bean = templist.get(uuid);
                    if (bean != null) {
                        bean.convertSuccessStringToResultUrl();
                        // 更改压缩后文件后缀名为空
                        if (bean.getType() == FileTaskManager.NOTICE_TYPE_PHOTO_SEND
                                && bean.getStatus() == TASK_STATUS_SUCCESS) {
                            String compressedpath = bean.getCompressedPath();
                            if (!TextUtils.isEmpty(compressedpath)) {
                                String newpath = compressedpath.replace(".",
                                        System.currentTimeMillis() + "");
                                File old = new File(compressedpath);
                                if (old.exists()) {
                                    old.renameTo(new File(bean.getResultUrl()));
                                }
                            }
                        }

                        if (bean.getStatus() == TASK_STATUS_SUCCESS) {
                            // 处理单独下载文件的情况
                            if (bean.isSingleDownload()) {
                                // updateTaskStatus(uuid, TASK_STATUS_SUCCESS,
                                // true);
                                // TODO:20141127 在新的消息需求中，每条消息只有一张图片或视频；
                                // 此处暂可认为完全成功；（理想情况下，应该check下其他同记录中的taskbean）
                                // 仅保存body字段，不修改消息中的状态
                                updateBodybutTaskStatus(uuid);
                                LogUtil.d("templist.remove:" + uuid);
                                templist.remove(uuid);
                                downloadTasks.remove(uuid);
                                return;
                            }
                            if (bean.isFrom()) {
                                // 接收的消息
                                updateTaskStatus(uuid, TASK_STATUS_SUCCESS, true);
                                LogUtil.d("fileTaskMap.remove:" + uuid);
                                templist.remove(uuid);
                                downloadTasks.remove(uuid);
                            }

                        } else {
                            if (!TextUtils.isEmpty(bean.getErrorTip())) {
                                LogUtil.d("Toast:" + bean.getErrorTip());
                                Toast.makeText(mContext, bean.getErrorTip(),
                                        Toast.LENGTH_SHORT).show();
                            }

                            if (bean.isSingleDownload()) {
                                updateBodybutTaskStatus(uuid);
                            } else {
                                updateTaskStatus(uuid, TASK_STATUS_FAIL, true);
                            }

                            LogUtil.d("templist.remove:" + uuid);
                            templist.remove(uuid);
                            downloadTasks.remove(uuid);
                        }
                    }

                }
                break;

                case FileTaskManager.MSG_FAILURE: {
                    Bundle data = msg.getData();
                    String uuid = data.getString("uuid");
                    String srcUrl = data.getString("srcUrl");
                    LogUtil.d("MSG_FAILURE,uuid:" + uuid + "|srcUrl:" + srcUrl);

                    FileTaskBean bean = templist.get(uuid);
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
                        LogUtil.d("templist.remove:" + uuid);
                        templist.remove(uuid);
                        downloadTasks.remove(uuid);
                    }

                }
                break;

            }
        }
    };

    /**
     * 根据UUID 更新该条记录的状态的body字段，不修改状态
     *
     * @param uuid
     * @return
     */
    private boolean updateBodybutTaskStatus(String uuid) {
        // LogUtil.begin("uuid:" + uuid);
        // 根据uuid更改记录中body 和 status 字段
        if (TextUtils.isEmpty(uuid)) {
            return false;
        }

        String body = createBodyString(uuid);
        LogUtil.d("body:" + body);
        if (!TextUtils.isEmpty(body)) {
            //TODO 更新body
            List<FileTaskBean> tempList = findFileTasks(uuid);
            FileTaskBean bean = tempList.get(0);
            if(bean.getPos()==-2){
                if (mNoticeDao.getNoticeById(bean.getId())!=null){
                    mNoticeDao.updateNotice(bean.getId(), body);
                }else{
                    mDtNoticeDao.updateNotice(bean.getId(), body);
                    CustomLog.d("更新body","mDtnOtice id = "+bean.getId()+"body = "+body);
                }

            }else{
                collectionDao.upDateBodyById(bean.getId(), body);
            }
        }

        return false;
    }

    private String createBodyString(String uuid) {
        // LogUtil.begin("uuid:" + uuid);
        // 从FileTaskBean list中找到同uuid的对象， 组成JSON ARRAY
        // 再tostring(),得到需要的对象
        List<FileTaskBean> tempList = findFileTasks(uuid);
        return createBodyStringFromList(tempList);
    }

    /***
     * 从FileTaskBean list数据构建动态消息的body字段
     *
     * @param tempList
     * @return
     */
    public String createBodyStringFromList(List<FileTaskBean> tempList) {
        if (tempList == null || tempList.size() == 0) {
            return "";
        }
        FileTaskBean bean = tempList.get(0);
        CollectionEntity mCollectionEntity = null;
        NoticesBean mbean = null;
        if(bean.getPos()==-2){
            mbean = mNoticeDao.getNoticeById(bean.getId());
            if (mbean == null){
                mbean = mDtNoticeDao.getNoticeById(bean.getId());
            }
        }else{
            mCollectionEntity = collectionDao
                    .getCollectionEntityById(bean.getId());
        }
        JSONArray body = null;
        int type = bean.getType();
        switch (type) {
            case FileTaskManager.NOTICE_TYPE_AUDIO_SEND:
            case FileTaskManager.NOTICE_TYPE_FILE:
            case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
            case FileTaskManager.NOTICE_TYPE_PHOTO_SEND: {
                int length = tempList.size();
                for (int i = 0; i < length; i++) {
                    bean = tempList.get(i);
                    JSONObject object = null;
                    try {
                        if(bean.getPos() == -1||bean.getPos()==-2){
                            body = new JSONArray();
                        }else{
                            body = new JSONArray(mCollectionEntity.getBody());
                        }
                        object = new JSONObject(bean.getRawBodyItemData());
                        if (!bean.isFrom()) {
                            // // 发送的消息 需要更新romteUrl、compressPath和thumbnail
                            // object.put("remoteUrl", bean.getResultUrl());
                            // object.put("compressPath", bean.getCompressedPath());
                            // object.put("thumbnail", bean.getThumbnailUrl());
                        } else {
                            // 接收的消息 仅需要更新 localUrl
                            String result = bean.getResultUrl();
                            if (bean.getStatus() == TASK_STATUS_SUCCESS) {
                                if (!TextUtils.isEmpty(result)
                                        && result.endsWith(".temp")) {
                                    result.replace(".temp", "");
                                }
                            }
                            object.put("localUrl", result);
                            object.put("size", bean.getFilesize());
                        }
                    } catch (JSONException e) {
                        LogUtil.e("JSONException", e);
                    }

                    if (object != null) {
                        if(bean.getPos() == -1||bean.getPos()==-2){
                            body.put(object);
                        }else{
                            try {
                                body.put(bean.getPos(),object);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
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

    private List<FileTaskBean> findFileTasks(String uuid) {
        LogUtil.d("begin----uuid:" + uuid);
        if (TextUtils.isEmpty(uuid)) {
            return null;
        }
        if (templist == null || !templist.containsKey(uuid)) {
            return null;
        }
        FileTaskBean bean = templist.get(uuid);
        List<FileTaskBean> list = new ArrayList<FileTaskBean>();
        if (bean == null) {
            return null;
        } else {
            list.add(bean);
            return list;
        }

    }

    /***
     * 根据UUID 更新该条记录的状态
     *
     * @param uuid
     * @param status
     * @param change_body
     *            是否更新body字段
     * @return
     */
    private boolean updateTaskStatus(String uuid, int status,
                                     boolean change_body) {
        LogUtil.d("updateTaskStatus ---- uuid:" + uuid + "|status:" + status
                + "|change_body:" + change_body);
        // 根据uuid更改记录中body 和 status 字段
        if (TextUtils.isEmpty(uuid)) {
            return false;
        }
        if (change_body) {
            String body = createBodyString(uuid);
            LogUtil.d("body:" + body);
            if (TextUtils.isEmpty(body)) {

            } else {
                //TODO 更新body
                List<FileTaskBean> tempList = findFileTasks(uuid);
                FileTaskBean bean = tempList.get(0);
                collectionDao.upDateBodyById(bean.getId(), body);
            }
        }
        return false;
    }

    public void deletLoadTask(String id) {
        FileTaskBean bean = templist.get(id);
        if (bean != null) {
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
            LogUtil.d("templist.remove:" + id);
            downloadTasks.get(id).stopDownload();
            templist.remove(id);
            downloadTasks.remove(id);
        }
    }

    public String getFiletypeByFileName(String fName){
        String type="";
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0|| fName.length() - dotIndex > 6) {
            return type;
        }
        return fName.substring(dotIndex+1, fName.length()).toLowerCase();
    }

    public boolean isExFileTaskBean(FileTaskBean bean){
        return templist.containsKey(bean.getUuid());
    }
}