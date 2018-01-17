package cn.redcdn.hvs.im.collection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.im.bean.CollectionBean;
import cn.redcdn.hvs.im.dao.CollectionDao;

/**
 * Desc
 * Created by wangkai on 2017/3/8.
 */

public class CollectionViewUpdateManager {
    private static CollectionViewUpdateManager manager;
    private CollectionDao mCollectionDao;
    private static  boolean dBChanged=false;
    private static  boolean broadcastArrived=false;

    public static CollectionViewUpdateManager getInstance(){
        if (null==manager){
            manager=new CollectionViewUpdateManager();
        }
        return manager;
    }

    private  CollectionViewUpdateManager(){
        mCollectionDao=new CollectionDao(MedicalApplication.getContext());
    }

    /**
     * 获取所有有效的显示bean
     * @return
     */
    public List<CollectionBean> getAllCollectionRecord(){
        return mCollectionDao.getAllCollectionRecord();
    }

    ArrayList<DBdateChange> CollectionTableChanges=new ArrayList<CollectionViewUpdateManager.DBdateChange>();
    private CollectionObserver  CollectionObserver;
    public void addCollectionTableChanged(DBdateChange changed){
        CollectionTableChanges.add(changed);
        registerContentCollectionObserver();
    }

    public void removeCollectionTableChanged(DBdateChange changed){
        CollectionTableChanges.remove(changed);
        if (CollectionTableChanges.size()==0){
            unRegisterContentCollectionObserver();
        }
    }

    public void sendUpdataViewBroadcast(){
        Intent intent=new Intent();
        intent.setAction(CollectionViewUpdateManager.ACTION_UPDATE_COLLECTION_VIEW);
        MedicalApplication.getContext().sendBroadcast(intent);
        broadcastArrived=true;
        doCallBack();
    }

    /*-----------------------------------------------------------------------广播   开始--------------------------------------------------------------------------------------------------------------------*/
    public final static String ACTION_UPDATE_COLLECTION_VIEW="ACTION_UPDATE_COLLECTION_VIEW";

    private void registerBoardcast(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_COLLECTION_VIEW);
        MedicalApplication.getContext().registerReceiver(reciver, filter);
    }

    private void unRegisterBoardcast(){
        MedicalApplication.getContext().unregisterReceiver(reciver);
    }

    private BroadcastReceiver reciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_UPDATE_COLLECTION_VIEW.equals(intent.getAction())) {
                broadcastArrived=true;
                doCallBack();
            }
        }
    };
    /*-----------------------------------------------------------------------广播   结束--------------------------------------------------------------------------------------------------------------------*/

    /*-----------------------------------------------------------------------数据库   开始--------------------------------------------------------------------------------------------------------------------*/
    private void registerContentCollectionObserver(){
        if (CollectionObserver == null) {
            registerBoardcast();
            CollectionObserver = new CollectionObserver();
//            MedicalApplication.getContext().getContentResolver().registerContentObserver(CollectionTable.URI, true, CollectionObserver);
        }
    }
    private void unRegisterContentCollectionObserver(){
        if (CollectionObserver != null) {
            unRegisterBoardcast();
            MedicalApplication.getContext().getContentResolver().unregisterContentObserver(CollectionObserver);
            CollectionObserver=null;
        }
    }

    private class CollectionObserver extends ContentObserver {
        public CollectionObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            dBChanged=true;
            doCallBack();
        }
    }
	/*-----------------------------------------------------------------------数据库   结束--------------------------------------------------------------------------------------------------------------------*/

    private void doCallBack(){
        if (dBChanged&&broadcastArrived){
            for (DBdateChange change:CollectionTableChanges){
                change.onChange();
            }
            dBChanged=false;
            broadcastArrived=false;
        }
    }

    public interface DBdateChange{
        void onChange();
    }
}
