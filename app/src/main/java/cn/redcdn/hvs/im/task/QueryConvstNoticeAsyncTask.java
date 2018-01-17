package cn.redcdn.hvs.im.task;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.agent.AppP2PAgentManager;
import cn.redcdn.hvs.im.asyncTask.NetPhoneAsyncTask;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.column.NoticesTable;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.LogUtil;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Desc    查询会话消息
 * Created by wangkai on 2017/2/24.
 */

public class QueryConvstNoticeAsyncTask extends NetPhoneAsyncTask<String, String, Integer> {

    private static final String TAG = QueryConvstNoticeAsyncTask.class.getSimpleName();

    /**
     * 全部查询
     */
    public static final int QUERY_TYPE_ALL = 1;
    /**
     * 条件查询
     */
    public static final int QUERY_TYPE_COND = 2;
    /**
     * 分页查询
     */
    public static final int QUERY_TYPE_PAGE = 3;

    private NoticesDao dao = null;
    private String convstId = "";
    private Cursor dataCursor = null;
    // 查询方式
    private int queryType = QUERY_TYPE_ALL;
    // 起始消息时间
    private long recvTimeBegin = 0l;
    // 分页查询条数
    private int pageCnt = IMConstant.NOTICE_PAGE_CNT;

    private QueryTaskPostListener listener = null;

    // private Map<String, String> nubeNamesMap = null;


    public QueryConvstNoticeAsyncTask(Context context,
                                      String convstId,
                                      int queryType,
                                      long recvTimeBegin,
                                      int pageCnt) {
        dao = new NoticesDao(context);
        this.convstId = convstId;
        this.queryType = queryType;
        this.recvTimeBegin = recvTimeBegin;
        this.pageCnt = pageCnt;

        dataCursor = dao.queryAllNotice(convstId);
        //dataCursor 返回可能为null 直接调用getCount会崩
        if (dataCursor != null && dataCursor.getCount() == 1 && this.recvTimeBegin >= 1) {
            this.recvTimeBegin = 1;
        }
        dataCursor.close();
        dataCursor = null;

    }


    @Override
    protected Integer doInBackground(String... params) {

        if (TextUtils.isEmpty(convstId)) {
            return 0;
        }

        try {
            // 更新该会话下所有消息isnew为非新消息  进入会话钱，更新改内容
            //设置消息已读
            setMsgRead();
            dao.updateNewStatusInConvst(convstId);
            switch (queryType) {
                case QUERY_TYPE_ALL:
                    // 全部查询
                    dataCursor = dao.queryAllNotices(convstId);
                    break;
                case QUERY_TYPE_COND:
                    // 条件查询 （查询某一时间点至最新一条消息的所有数据）
                    dataCursor = dao.queryCondNotices(convstId, recvTimeBegin);
                    break;
                case QUERY_TYPE_PAGE:
                    // 分页查询（查询某一会话 id 下的前 30 条数据）
                    dataCursor = dao.queryPageNotices(convstId, recvTimeBegin, pageCnt);
                    break;
                default:
                    break;
            }
            return 0;
        } catch (Exception e) {
            LogUtil.e("Exception", e);
            return -1;
        }
    }


    private void setMsgRead() throws Exception {
        CustomLog.i(TAG, "setMsgRead()");

        Cursor unReadCursor = null;
        ArrayList<NoticesBean> unReadMsgList = new ArrayList<NoticesBean>();
        try {
            unReadCursor = dao.getUnreadNotice(convstId);
            if (unReadCursor != null && unReadCursor.getCount() > 0) {
                unReadCursor.moveToFirst();
                do {
                    NoticesBean item = NoticesTable.pureUnReadCursor(unReadCursor);
                    unReadMsgList.add(item);
                } while (unReadCursor.moveToNext());
            }
        } catch (Exception e) {
            CustomLog.e("updateRunningTask2Fail", e.toString());
        } finally {
            if (unReadCursor != null) {
                unReadCursor.close();
                unReadCursor = null;
            }
        }
        HashMap<String, String> serverIds = new HashMap<String, String>();
        if (unReadMsgList.size() > 0) {
            for (int i = 0; i < unReadMsgList.size(); i++) {
                NoticesBean bean = unReadMsgList.get(i);
                if (serverIds.containsKey(bean.getServerId())) {
                    String tmpStr = serverIds.get(bean.getServerId());
                    tmpStr = tmpStr + "," + bean.getId();
                    serverIds.put(bean.getServerId(), tmpStr);
                    String[] valueArray = tmpStr.split(",");
                    if (10 == valueArray.length) {
                        AppP2PAgentManager.getInstance().markMsgRead(serverIds);
                        AppP2PAgentManager.getInstance().markMsgReadOne(bean.getServerId(), tmpStr);
                        serverIds.remove(bean.getServerId());
                        continue;
                    }
                } else {
                    serverIds.put(bean.getServerId(), bean.getId());
                }
            }

            if (serverIds.size() > 0) {
                AppP2PAgentManager.getInstance().markMsgRead(serverIds);
            }
        }
    }


    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);

        if (listener != null) {
            if (result < 0) {
                listener.onQueryFailure();
            } else {
                listener.onQuerySuccess(dataCursor);
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
}
