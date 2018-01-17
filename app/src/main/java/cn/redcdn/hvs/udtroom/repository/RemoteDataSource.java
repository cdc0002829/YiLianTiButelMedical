package cn.redcdn.hvs.udtroom.repository;

import android.support.annotation.NonNull;
import cn.redcdn.datacenter.hpucenter.HPUGetCslRoomDetail;
import cn.redcdn.datacenter.hpucenter.data.CSLRoomDetailInfo;
import cn.redcdn.log.CustomLog;
/**
 * @author guoyx
 *
 *         数据源，通过 DataCenter 获取数据
 */

public class RemoteDataSource {
    private static final String TAG = RemoteDataSource.class.getSimpleName();

    /**
     * 获取诊疗室详情
     *
     * @param dtID  诊疗 ID
     */
    public void getRemoteCSLRoomDetailData(
        @NonNull String token,
        @NonNull String dtID, @NonNull final DataCallback callback) {
        CustomLog.i(TAG, "getRemoteCSLRoomDetailData()");

        HPUGetCslRoomDetail roomDetail = new HPUGetCslRoomDetail() {
            @Override protected void onSuccess(CSLRoomDetailInfo responseContent) {
                super.onSuccess(responseContent);
                CustomLog.i(TAG, "onSuccess()");

                callback.onSuccess(responseContent);

            }

            @Override protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.i(TAG, "onFail()");
                CustomLog.e(TAG, "statusCode = " + statusCode + "statusInfo = " + statusInfo);

                callback.onFailed(statusCode, statusInfo);

            }
        };

        roomDetail.getcslroomdetail(token, dtID);
    }

    public interface DataCallback {
        void onSuccess(CSLRoomDetailInfo data);
        void onFailed(int statusCode, String statusInfo);
    }
}
