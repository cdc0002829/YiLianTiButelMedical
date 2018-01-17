package cn.redcdn.hvs.im.bean;

/**
 * @ClassName: CollectionEntity
 * @Description:  收藏表与数据库强相关的对象
 */
public class CollectionEntity{
    /**
     * 系统生成唯一ID，uuid
     */
    private String id="";

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 操作时间
     */
    private long operateTime=-1L;
    public long getOperateTime() {
        return operateTime;
    }
    public void setOperateTime(long operateTime) {
        this.operateTime = operateTime;
    }

    public static final int STATUS_INVALID=0;
    public static final int STATUS_EFFECTIVE=1;
    /**
     * 有效状态
     */
    private int status =STATUS_EFFECTIVE;
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 同步状态
     */
    public static final int SYNCSTATUS_UNEED=0;
    public static final int SYNCSTATUS_NEED=1;
    private int syncStatus =0;
    public int getSyncStatus() {
        return syncStatus;
    }
    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    /**
     * 操作者nube号
     */
    private String operatorNube;
    public String getOperatorNube() {
        return operatorNube;
    }
    public void setOperatorNube(String operatorNube) {
        this.operatorNube = operatorNube;
    }

    /**
     *类型：见Filetask中的定义
     */
    private int type =-1;
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }

    /**
     * 与消息体对应
     */
    private String body ="";
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * 与消息的extinfo对应
     */
    private String extinfo="";
    public String getExtinfo() {
        return extinfo;
    }
    public void setExtinfo(String extinfo) {
        this.extinfo = extinfo;
    }
}