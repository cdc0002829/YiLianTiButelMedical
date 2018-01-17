package cn.redcdn.hvs.im.bean;

/**
 * Created by guoyx on 2017/2/25.
 */

import android.text.TextUtils;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.manager.HtmlParseManager;
import com.butel.connectevent.utils.LogUtil;
import java.io.Serializable;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



/**
 * @ClassName: CollectionBean
 * @Description: 收藏表的bean
 * @author niuben
 * @date 2016-5-9 上午10:52:00
 */
public class CollectionBean implements Serializable {

    private static final long serialVersionUID = 1287147906126641266L;

    /**
     * 系统生成唯一ID，uuid
     */
    private String id = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * 操作时间
     */
    private long operateTime = -1L;

    public long getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(long operateTime) {
        this.operateTime = operateTime;
    }

    // operatorNube,operatorMobile,operatorName,operatorNickname在检索匹配时会用到，
    // 所以在set这四个数据时，需确保不会为null，以免匹配时发生NullPointerException
    /**
     * 操作者nube号
     */
    private String operatorNube = "";
    /**
     * 操作者名称
     */
    private String operatorMobile = "";
    private String operatorName = "";
    private String operatorNickname = "";

    public String getShowName() {
        return ShowNameUtil.getShowName(ShowNameUtil.getNameElement(
            operatorName, operatorNickname, operatorMobile, operatorNube));
    }

    public void setOperatorMobile(String operatorMobile) {
        if (operatorMobile == null) {
            this.operatorMobile = "";
        } else {
            this.operatorMobile = operatorMobile;
        }
    }

    public void setOperatorName(String operatorName) {
        if (operatorName == null) {
            this.operatorName = "";
        } else {
            this.operatorName = operatorName;
        }
    }

    public void setOperatorNickname(String operatorNickname) {
        if (operatorNickname == null) {
            this.operatorNickname = "";
        } else {
            this.operatorNickname = operatorNickname;
        }
    }

    public String getOperatorNube() {
        return operatorNube;
    }

    public void setOperatorNube(String operatorNube) {
        if (operatorNube == null) {
            this.operatorNube = "";
        } else {
            this.operatorNube = operatorNube;
        }
    }

    /**
     * 操作者头像
     */
    private String operatorHeadUrl;

    public void setOperatorHeadUrl(String operatorHeadUrl) {
        this.operatorHeadUrl = operatorHeadUrl;
    }

    public String getOperatorHeadUrl() {
        return operatorHeadUrl;
    }

    /**
     * 类型：见Filetask中的定义
     */
    private int type = -1;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * 与消息体对应
     */
    private String body = "";

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * 与消息的extinfo对应
     */
    private String extinfo = "";

    public String getExtinfo() {
        return extinfo;
    }

    public void setExtinfo(String extinfo) {
        this.extinfo = extinfo;
    }

    public boolean isMatch(int type) {
        return this.type == type;
    }

    /**
     * 原则，页面显示什么匹配什么
     *
     * @param match
     * @return
     */
    public boolean isMatch(String match) {
        // 显示名称匹配
        if (this.getShowName().contains(match)) {
            return true;
        }
        // 类型匹配
        switch (this.getType()) {
            case FileTaskManager.NOTICE_TYPE_URL:
                try {
                    JSONArray bodyArray = new JSONArray(this.getBody());
                    if (bodyArray != null && bodyArray.length() > 0) {
                        JSONObject bodyObj = bodyArray.optJSONObject(0);
                        String pageData = bodyObj.optString("webData");
                        if (TextUtils.isEmpty(pageData)) {
                            String text = bodyObj.optString("txt");
                            if (text != null && text.contains(match)) {
                                return true;
                            }
                        } else {
                            List<WebpageBean> webPages = HtmlParseManager
                                .getInstance().convertWebpageBean(
                                    new JSONArray(pageData));
                            for (int i = 0; i < webPages.size(); i++) {
                                WebpageBean webpageBean = webPages.get(i);
                                if ((webpageBean.getTitle() != null && webpageBean
                                    .getTitle().contains(match))
                                    || (webpageBean.getDescription() != null && webpageBean
                                    .getDescription().contains(match))
                                    || (webpageBean.getHeaderStr() != null && webpageBean
                                    .getHeaderStr().contains(match))
                                    || (webpageBean.getFooterStr() != null && webpageBean
                                    .getFooterStr().contains(match))) {
                                    return true;
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    LogUtil.e("JSONException", e);
                }
                break;
            case FileTaskManager.NOTICE_TYPE_TXT_SEND:
                try {
                    JSONArray bodyArray = new JSONArray(this.getBody());
                    if (bodyArray != null && bodyArray.length() > 0) {
                        JSONObject bodyObj = bodyArray.optJSONObject(0);
                        if (bodyObj.optString("txt").contains(match)) {
                            return true;
                        }
                    }
                } catch (JSONException e) {
                    LogUtil.e("JSONException", e);
                }
                break;
            case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                break;
            case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
                break;
            case FileTaskManager.NOTICE_TYPE_AUDIO_SEND:
                break;
            case FileTaskManager.NOTICE_TYPE_FILE:
                ButelFileInfo info = ButelFileInfo.parseJsonStr(this.getBody(), true);
                if (info.getFileName().contains(match)) {
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }
}
