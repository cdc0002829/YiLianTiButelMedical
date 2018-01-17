package cn.redcdn.hvs.im.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <dl>
 * <dt>RemotePhotoBean.java</dt>
 * <dd>Description:远程图片类</dd>
 * <dd>Copyright: Copyright (C) 2014</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2014-1-6 下午2:17:14</dd>
 * </dl>
 *
 * @author zhaguitao
 */
public class PhotoBean implements Parcelable {

    private String taskId = "";
    private String littlePicUrl = "";
    private String remoteUrl = "";
    private String localPath = "";
    private int type = 0;
    private boolean isFrom =false;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getLittlePicUrl() {
        return littlePicUrl;
    }

    public void setLittlePicUrl(String littlePicUrl) {
        this.littlePicUrl = littlePicUrl;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isFrom() {
        return isFrom;
    }

    public void setFrom(boolean isFrom) {
        this.isFrom = isFrom;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskId);
        dest.writeString(littlePicUrl);
        dest.writeString(remoteUrl);
        dest.writeString(localPath);
        dest.writeInt(type);
        dest.writeInt(isFrom?1:0);
    }

    // 添加一个静态成员,名为CREATOR,该对象实现了Parcelable.Creator接口
    public static final Parcelable.Creator<PhotoBean> CREATOR = new Parcelable.Creator<PhotoBean>() {
        @Override
        public PhotoBean createFromParcel(Parcel source) {
            // 从Parcel中读取数据，返回person对象
            PhotoBean bean = new PhotoBean();
            bean.setTaskId(source.readString());
            bean.setLittlePicUrl(source.readString());
            bean.setRemoteUrl(source.readString());
            bean.setLocalPath(source.readString());
            bean.setType(source.readInt());
            bean.setFrom(source.readInt()==1?true:false);
            return bean;
        }

        @Override
        public PhotoBean[] newArray(int size) {
            return new PhotoBean[size];
        }
    };
}
