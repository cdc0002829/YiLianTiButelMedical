package cn.redcdn.hvs.im.work.collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.butel.connectevent.utils.LogUtil;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.bean.CollectionEntity;
import cn.redcdn.hvs.im.preference.DaoPreference;
import cn.redcdn.hvs.im.util.xutils.http.SyncResult;
import cn.redcdn.hvs.im.util.xutils.http.client.RequestParams;
import cn.redcdn.hvs.im.util.xutils.http.client.HttpRequest.HttpMethod;

/**
 * <dl>
 * <dt>CollectionDownLoadWork.java</dt>
 * <dd>Description:下载收藏数据线程</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 北京红云融通技术有限公司</dd>
 * <dd>CreateDate: 2016-5-13 下午2:44:34</dd>
 * </dl>
 *
 * @author 牛犇
 */
public class CollectionDownLoadWork extends CollectionBaseLoadWork {

    public CollectionDownLoadWork(Context context,
                                  CollectionLoadCallBack callBack) {
        super(context, callBack);
    }

    @Override
    public void syncLoadCollection() {
        LogUtil.begin("同步下载 我的收藏数据");
        if (isFinish) {
            LogUtil.d("任务已经被取消，不再调用接口");
            return;
        }

        SyncResult syncResult = queryCollectionItems();

        if (isFinish) {
            LogUtil.d("接口返回，该任务已经被取消的场合，不做处理");
            return;
        }

        if (syncResult != null && syncResult.isOK()) {
            LogUtil.d("接口调用成功，同步到本地操作");
            parseSyncResult(syncResult.getResult());
        } else {
            LogUtil.d("接口调用失败");
        }
    }

    private void parseSyncResult(String result) {
        LogUtil.begin("");
        try {
            JSONObject object = new JSONObject(result);
            String status = object.optString("status");
            LogUtil.d("status =" + status);
            if ("0".equals(status)) {
                MedicalApplication.getPreference().setKeyValue(
                        DaoPreference.PrefType.KEY_COLLECTION_QUERY_STARTTIME,
                        object.optLong("time") + "");
                JSONArray jsonArray = object.optJSONArray("userInfo");
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        updateCollectionRecord(jsonArray.optJSONObject(i));
                    }
                } else {
                    LogUtil.d("jsonArray=null");
                }
            }
        } catch (JSONException e) {
            LogUtil.e("JSONException", e);
        }
        LogUtil.end("");
    }

    /**
     * {"data":"lkopquiprwqeopq","time":1462947164,"status":"1"}
     *
     * @param item
     */
    private void updateCollectionRecord(JSONObject item) {
        CollectionEntity server = new CollectionEntity();
        server.setId(item.optString("id"));
        server.setStatus("0".equals(item.optString("status")) ? CollectionEntity.STATUS_INVALID
                : CollectionEntity.STATUS_EFFECTIVE);
        server.setOperateTime(item.optLong("time"));
        server.setSyncStatus(CollectionEntity.SYNCSTATUS_UNEED);
        try {
            JSONObject object = new JSONObject(item.optString("data"));
            server.setOperatorNube(object.optString("operatorNube"));
            server.setType(object.optInt("type"));
            server.setBody(object.optString("body"));
            server.setExtinfo(object.optString("extinfo"));
        } catch (Exception e) {
            LogUtil.e("返回数据异常，不入数据库:" + item.toString(), e);
        }

        insertOrUpdate(mCollectionDao.getCollectionEntityById(server.getId()),
                server);
    }

    private void insertOrUpdate(CollectionEntity local, CollectionEntity server) {
        if (local == null
                && (CollectionEntity.STATUS_INVALID == server.getStatus())) {
            // 本地无该条数据，服务端无效，不做处理
            return;
        }

        if (local == null
                && (CollectionEntity.STATUS_EFFECTIVE == server.getStatus())) {
            // 本地无该条数据，服务端有效，直接插入
            mCollectionDao.insert(server);
            return;
        }

        if (local != null
                && (CollectionEntity.SYNCSTATUS_NEED == local.getStatus())) {
            // 本地有该条数据，且需要同步，不处理
            return;
        }

        if (local != null
                && (CollectionEntity.SYNCSTATUS_UNEED == local.getStatus())
                && (CollectionEntity.STATUS_EFFECTIVE == server.getStatus())) {
            // 本地有该条数据且不需要同步，服务端有效，直接更新
            // 不更新本地数据的时间
            server.setOperateTime(local.getOperateTime());
            mCollectionDao.update(server);
            return;
        }

        if (local != null
                && (CollectionEntity.SYNCSTATUS_UNEED == local.getStatus())
                && (CollectionEntity.STATUS_INVALID == server.getStatus())) {
            // 本地有该条数据且不需要同步，服务端无效，删除
            mCollectionDao.deleteRecordById(local.getId());
            return;
        }
    }

    private SyncResult queryCollectionItems() {
        LogUtil.begin("");
        String startTime = MedicalApplication.getPreference().getKeyValue(
                DaoPreference.PrefType.KEY_COLLECTION_QUERY_STARTTIME, "0");
        RequestParams params = GetInterfaceParams.getQueryCollectionItemParams(
                getSelfNube(), getToken(), startTime);
        SyncResult result = httpUtils.sendSync(HttpMethod.POST, getUrl(),
                params, IMConstant.ACTIVITY_ACCESSTOKEN_INVALID);
        LogUtil.d(result.isOK() + "|" + result.getResult());
        return result;
    }
}