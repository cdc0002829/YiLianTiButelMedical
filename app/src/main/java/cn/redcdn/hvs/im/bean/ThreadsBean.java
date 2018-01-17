package cn.redcdn.hvs.im.bean;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

import cn.redcdn.hvs.im.column.ThreadsTable;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class ThreadsBean implements Serializable {
    private static final long serialVersionUID = -7047060636994585759L;
    public static String DRAFTTXT = "draftText";
    /**
     * 主键 32位UUID，由手机端产生
     */
    private String id = "";
    /**
     * 会话的创建的时间  yyyyMMddHHmmss
     */
    private long createTime = -1L;
    /**
     * 最后一条信息的时间 yyyyMMddHHmmss
     */
    private long lastTime = -1L;

    public int getSaveContact() {
        return saveContact;
    }

    public void setSaveContact(int saveContact) {
        this.saveContact = saveContact;
    }

    /**

     * 是否保存到通讯录
     */
    private int saveContact = 0;
    /**
     * 会话类型：1：单聊；2：群聊
     */
    private int type;
    /**
     * 会话参与人员视频号 （排除自己的视频号，多个视频号以分号分割）
     */
    private String recipientIds = "";
    /**
     * 扩展字段；以JSON的形式，保存草稿文本
     */
    private String extendInfo = "";
    /**
     * 是否置顶 0：不置顶，1：置顶
     */
    private int top = ThreadsTable.TOP_NO;
    /**
     * 保留字段，暂没有使用
     */
    private String reserverStr1 = "";
    /**
     * 保留字段，暂没有使用
     */
    private String reserverStr2 = "";
    /**
     * 草稿文本，方便页面调用，省略json解析
     */
    private String draftTxt = "";
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<String> getRecipientIdList() {
        return null;
    }

    public String getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(String recipientIds) {
        this.recipientIds = recipientIds;
    }

    public String getExtendInfo() {
        return extendInfo;
    }

    /**
     * 此字段为JSON格式，不建议用户直接调用
     *
     * @param extendInfo
     */
    public void setExtendInfo(String extendInfo) {
        this.extendInfo = extendInfo;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public String getReserverStr1() {
        return reserverStr1;
    }

    public void setReserverStr1(String reserverStr1) {
        this.reserverStr1 = reserverStr1;
    }

    public String getReserverStr2() {
        return reserverStr2;
    }

    public void setReserverStr2(String reserverStr2) {
        this.reserverStr2 = reserverStr2;
    }

    public String getDraftTxt() {
        if (!TextUtils.isEmpty(draftTxt)) {
            return draftTxt;
        }
        if (TextUtils.isEmpty(extendInfo)) {
            return "";
        } else {
            try {
                JSONObject obj = new JSONObject(extendInfo);
                draftTxt = obj.optString(DRAFTTXT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return draftTxt;
    }

    public void setDraftTxt(String draftTxt) {
        this.draftTxt = draftTxt;
    }

    /**
     * 在内存中重新维护extendInfo字段，但不更新bean中数据
     *
     * @param draft 新的文字草稿
     * @return 返回更新draftText后的extendInfo
     */
    public String updateExtendInfoDraft(String draft) {
        String extendInfoUpdate = "";
        if (TextUtils.isEmpty(extendInfo)) {
            JSONObject obj = new JSONObject();
            try {
                obj.put(DRAFTTXT, draft);
                extendInfoUpdate = obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            try {
                JSONObject obj = new JSONObject(extendInfo);
                obj.put(DRAFTTXT, draft);
                extendInfoUpdate = obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                return extendInfo;
            }
        }
        return extendInfoUpdate;
    }
}
