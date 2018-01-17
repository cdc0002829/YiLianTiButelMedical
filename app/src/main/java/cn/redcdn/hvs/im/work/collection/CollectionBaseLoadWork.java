package cn.redcdn.hvs.im.work.collection;

import android.content.Context;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.im.UrlConstant;
import cn.redcdn.hvs.im.dao.CollectionDao;
import cn.redcdn.hvs.im.util.xutils.http.HttpUtils;
import cn.redcdn.hvs.im.preference.DaoPreference.PrefType;

/**
 * <dl>
 * <dt>CollectionBaseLoadWork.java</dt>
 * <dd>Description:同步 收藏数据线程</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 北京红云融通技术有限公司</dd>
 * <dd>CreateDate: 2016-5-13 下午2:44:34</dd>
 * </dl>
 * @author 牛犇
 */

public abstract class CollectionBaseLoadWork implements Runnable {

    protected CollectionDao mCollectionDao;
    protected CollectionLoadCallBack mCallBack;
    protected HttpUtils httpUtils = null;
    protected boolean isFinish = false;

    public void setFinish(boolean finish) {
        this.isFinish = finish;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public CollectionBaseLoadWork(Context context, CollectionLoadCallBack callBack) {
        mCollectionDao=new CollectionDao(context);
        mCallBack=callBack;
        isFinish=false;
        this.httpUtils = new HttpUtils();
        this.httpUtils.configTimeout(30 * 1000);
    }

    @Override
    public void run() {
        syncLoadCollection();
        mCallBack.onLoadEnd();
    }

    public abstract void syncLoadCollection();

    protected String getUrl(){
        return  UrlConstant.getCommUrl(PrefType.KEY_FAVORITE_SERVER_URL);
    }

    protected String getSelfNube(){
        return MedicalApplication.getPreference().getKeyValue(PrefType.LOGIN_NUBENUMBER, "");
    }

    protected String getToken(){
        return MedicalApplication.getPreference().getKeyValue(PrefType.LOGIN_ACCESSTOKENID, "");
    }

    public interface CollectionLoadCallBack{
        public void onLoadEnd();
    }

}
