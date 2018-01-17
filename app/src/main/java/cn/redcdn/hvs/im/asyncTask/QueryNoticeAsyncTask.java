package cn.redcdn.hvs.im.asyncTask;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.im.MessageFragment;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.dao.ThreadsDao;

/**
 * <dl>
 * <dt>QueryNoticeAsyncTask.java</dt>
 * <dd>Description:动态消息异步查询任务</dd>
 *
 * @author zhaguitao
 */
public class QueryNoticeAsyncTask extends
    NetPhoneAsyncTask<String, String, Cursor> {

    private NoticesDao noticesDao;

    private ThreadsDao threadDao = null;


    private QueryTaskPostListener listener = null;
    private int noticeCount;

    public QueryNoticeAsyncTask(Context context) {
        threadDao = new ThreadsDao(context);
    }


    public void setNoticesDao(NoticesDao noticesDao) {
        this.noticesDao = noticesDao;
    }


    @Override
    protected Cursor doInBackground(String... args) {
        boolean updateIsNew = true;

        if (args != null && args.length > 0) {
            String currentTag = args[0];
            if (!MessageFragment.class.getName().equals(currentTag)) {
                // 当前标签不是动态列表时，不更新isnew字段
                updateIsNew = false;
            }
        }

        if (updateIsNew) {
            // 更新所有消息isnew为非新消息
            //            dao.updateReadStatus();
        }

        //        vcardCnt = dao.countVcardNotice();

        //产品要求按照ShowNameUtil中的显示规则显示名字,暂时去掉这种查询出所有名字的方法--add on 2015/6/29

        // 查询所有nube好友
        //        Cursor nubeNameCursor = dao.queryNubeNames();
        //        if (nubeNameCursor != null && nubeNameCursor.getCount() > 0) {
        //            nubeNamesMap = new HashMap<String, String>();
        //            while (nubeNameCursor.moveToNext()) {
        //                nubeNamesMap.put(nubeNameCursor.getString(0),
        //                        nubeNameCursor.getString(1));
        //            }
        //        }
        //        if (nubeNameCursor != null) {
        //            nubeNameCursor.close();
        //            nubeNameCursor = null;
        //        }

        if(noticesDao != null){
            noticeCount = noticesDao.getNewNoticeCount();
            Intent intent = new Intent("NoticeCountBroaddcase");
            if(noticeCount > 0){
                intent.putExtra("newNoticeCount", noticeCount);
            }else{
                int noticeCountOfNotDisturb = noticesDao.getNewNoticeCountOfNoDisturb();
                if(noticeCountOfNotDisturb > 0){
                    intent.putExtra("newNoticeCount", NoticesDao.NO_DISTRUB_FLAG);
                }else {
                    intent.putExtra("newNoticeCount", 0);
                }
            }
            MedicalApplication.getContext().sendBroadcast(intent);
        }

        Cursor[] cursors = new Cursor[2];
        cursors[0] = threadDao.getAllThreadsInfoTop(); //置顶消息
        cursors[1] = threadDao.getAllThreadsInfo();  //未置顶消息
//        return threadDao.getAllThreadsInfo();
        return new MergeCursor(cursors);
    }


    @Override
    protected void onPostExecute(Cursor cursor) {
        super.onPostExecute(cursor);

        if (listener != null) {
            if (cursor == null) {
                listener.onQueryFailure();
            } else {
                listener.onQuerySuccess(cursor);
            }
        }
    }


    public void setQueryTaskListener(QueryTaskPostListener listener) {
        this.listener = listener;
    }


    public interface QueryTaskPostListener {
        public void onQuerySuccess(Cursor cursor);

        public void onQueryFailure();
    }

    /**
     * 更新 titlebar 未读消息数
     */

}
