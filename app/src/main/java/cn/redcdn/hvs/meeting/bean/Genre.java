package cn.redcdn.hvs.meeting.bean;

import java.util.List;

/**
 * Created by Administrator on 2017/11/21.
 */

public class Genre  {
   public String titile;
    public List<JointCoonsultatioonRoomBean>jointCoonsultatioonRoomBeanList;

    public Genre(String titile,List<JointCoonsultatioonRoomBean> jointCoonsultatioonRoomBeanList ) {
        this.jointCoonsultatioonRoomBeanList = jointCoonsultatioonRoomBeanList;
        this.titile = titile;
    }

    public Genre() {

    }

    public String getTitile() {

        return titile;
    }

    public void setTitile(String titile) {
        this.titile = titile;
    }

    public List<JointCoonsultatioonRoomBean> getJointCoonsultatioonRoomBeanList() {
        return jointCoonsultatioonRoomBeanList;
    }

    public void setJointCoonsultatioonRoomBeanList(List<JointCoonsultatioonRoomBean> jointCoonsultatioonRoomBeanList) {
        this.jointCoonsultatioonRoomBeanList = jointCoonsultatioonRoomBeanList;
    }
}
