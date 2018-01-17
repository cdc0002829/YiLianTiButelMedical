package cn.redcdn.hvs.im.collection;

import com.butel.connectevent.utils.LogUtil;
import com.butel.connectevent.utils.NetWorkUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.im.bean.CollectionEntity;
import cn.redcdn.hvs.im.dao.CollectionDao;
import cn.redcdn.hvs.im.work.collection.CollectionDownLoadWork;
import cn.redcdn.hvs.im.work.collection.CollectionUpLoadWork;
import cn.redcdn.hvs.im.work.collection.CollectionBaseLoadWork.CollectionLoadCallBack;
import cn.redcdn.log.CustomLog;

/**
 * Desc 收藏 同步数据管理类
 * Created by wangkai on 2017/3/8.
 */

public class CollectionUpdateWorkManager {

    private final String TAG = "CollectionUpdateWorkManager";
    private static CollectionUpdateWorkManager collectionUpdateWorkManager;
    private CollectionDao mCollectionDao;

    public static CollectionUpdateWorkManager getInstance(){
        if (null==collectionUpdateWorkManager){
            collectionUpdateWorkManager=new CollectionUpdateWorkManager();
        }
        return collectionUpdateWorkManager;
    }

    private  CollectionUpdateWorkManager(){
        mCollectionDao=new CollectionDao(MedicalApplication.getContext());
        threadPoolExecutor.setThreadFactory(new CollectionThreadFactory());
    }

    private int threadNumber=0;//一次请求数据的进程数,0个说明请求结束或者未开始
    private List<CollectionUpLoadWork> upLoadWorks=new ArrayList<CollectionUpLoadWork>();
    private CollectionDownLoadWork downLoadWork;
    private boolean reStart=false;
    private CollectionLoadCallBack mCallBack=new CollectionLoadCallBack() {
        @Override
        public void onLoadEnd() {
            threadNumber=threadNumber-1;
            if (threadNumber<=0){
                upLoadWorks.clear();
                downLoadWork=null;
                if (reStart){
                    reStart=false;
                    startUpdate();
                }else {
                    CollectionViewUpdateManager.getInstance().sendUpdataViewBroadcast();
                }
            }
        }
    };

    public void startUpdate() {
        CustomLog.d(TAG,"开始 同步收藏数据");
        CollectionViewUpdateManager.getInstance().sendUpdataViewBroadcast();
        if (!NetWorkUtil.isNetworkConnected(MedicalApplication.getContext())) {
            CustomLog.d(TAG,"网络连接不可用");
            return;
        }

        if(threadNumber<=0){
            List<CollectionEntity> entities = mCollectionDao.getAllSyncRecord();
            threadNumber=entities.size()+1;//n个同步上传的线程+1个同步下载的线程

            downLoadWork=new CollectionDownLoadWork(MedicalApplication.getContext(),mCallBack);
            threadPoolExecutor.execute(downLoadWork);

            for(CollectionEntity entity:entities){
                CollectionUpLoadWork upLoadWork=new CollectionUpLoadWork(MedicalApplication.getContext(),mCallBack,entity);
                upLoadWorks.add(upLoadWork);
                threadPoolExecutor.execute(upLoadWork);
            }
        }else {
            reStart=true;
        }
    }

    public void stopUpdate() {
        LogUtil.d("停止 同步收藏数据");
        reStart=false;
        if (downLoadWork!=null){
            downLoadWork.setFinish(true);
        }
        for (CollectionUpLoadWork upLoadWork:upLoadWorks){
            upLoadWork.setFinish(true);
        }
    }

    /**----------------——-------------------------------------------------------------------------线程池  开始---------------------------------------------------------------------------------------*/
    private  final static int CORE_POOL_SIZE=5;
    private  final static int MAX_POOL_SIZE=30;
    private  final static long  KEEP_ALIVE_TIME=10;

    BlockingQueue<Runnable> workQueue=new ArrayBlockingQueue<Runnable>(10);
    ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(
            CORE_POOL_SIZE,MAX_POOL_SIZE,KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,workQueue,new ThreadPoolExecutor.AbortPolicy());

    /**---------------------------线程池》提高优先级-------------------------------------------*/
    private static final int THREAD_PRIORITY = 8;

    private static class CollectionThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CollectionThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()){
                t.setDaemon(false);
            }
            t.setPriority(THREAD_PRIORITY);
            return t;
        }
    }
}
