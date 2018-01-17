package cn.redcdn.hvs.im.task;

import android.os.AsyncTask;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.im.bean.FriendInfo;
import cn.redcdn.hvs.im.dao.FriendsRelationDao;
import cn.redcdn.hvs.im.interfaces.FriendCallback;
import cn.redcdn.hvs.im.interfaces.ResponseEntry;
import cn.redcdn.log.CustomLog;
import java.util.List;

/**
 * Created by caiguo on 2017/5/11 0011.
 */
public class GetAllFriendAsyncTasks extends AsyncTask<String, Integer, List> {
    private String TAG = getClass().getName();
    private FriendCallback friendCallback;


    //onPreExecute方法用于在执行后台任务前做一些UI操作
    @Override
    protected void onPreExecute() {
        CustomLog.i(TAG, "onPreExecute() called");
    }


    //doInBackground方法内部执行后台任务,不可在此方法内修改UI
    @Override
    protected List doInBackground(String... params) {
        CustomLog.i(TAG, "doInBackground(Params... params) called");
        try {
            FriendsRelationDao friendsRelationDao = new FriendsRelationDao(MedicalApplication.getContext());
            List<FriendInfo> list = friendsRelationDao.getAllNewFriendInfo();
            return list;
        } catch (Exception e) {
            CustomLog.i(TAG, e.getMessage());
        }
        return null;
    }


    //onProgressUpdate方法用于更新进度信息
    @Override
    protected void onProgressUpdate(Integer... progresses) {
        CustomLog.i(TAG, "onProgressUpdate(Progress... progresses) called");
    }


    //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
    @Override
    protected void onPostExecute(List List) {
       ResponseEntry responseEntry= new ResponseEntry();
        responseEntry.content=List;
        responseEntry.status=0;
        friendCallback.onFinished(responseEntry);
        CustomLog.i(TAG, "onPostExecute(Result result) called");
    }


    //onCancelled方法用于在取消执行中的任务时更改UI
    @Override
    protected void onCancelled() {
        CustomLog.i(TAG, "onCancelled() called");
    }


    public void setCallBack(FriendCallback friendCallback) {
        this.friendCallback = friendCallback;
    }
}
