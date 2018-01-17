package cn.redcdn.hvs.meeting.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/11/21.
 */

public class JointCoonsultatioonRoomBean implements Parcelable {
    private int zhenliaoState;
    private String name;
    private int type;
    private String hospital;
    private String department;

    public int getZhenliaoState() {
        return zhenliaoState;
    }

    public void setZhenliaoState(int zhenliaoState) {
        this.zhenliaoState = zhenliaoState;
    }

    private  String range;
    private int unreadNotice;

    public int getUnreadNotice() {
        return unreadNotice;
    }

    public void setUnreadNotice(int unreadNotice) {
        this.unreadNotice = unreadNotice;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String doctorName;
    private String date;
    public String id;

    public void setName(String name) {
        this.name = name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public JointCoonsultatioonRoomBean(String name, int type,String hospital,String department,String doctorName,String date,String id,String range,int unreadNotice) {
        this.name = name;
        this.type = type;
        this.hospital=hospital;
        this.department=department;
        this.doctorName=doctorName;
        this.date=date;
        this.id=id;
        this.range=range;
        this.unreadNotice=unreadNotice;
    }
    public String getName() {
        return name;
    }
    public int getType() {
        return type;
    }
    public String getHospital(){
        return hospital;
    }
    public String getDepartment(){
        return department;
    }

    public String getDoctorName(){
        return doctorName;
    }
    public  String getDate(){
        return date;
    }



    protected JointCoonsultatioonRoomBean(Parcel in) {
    }

    public static final Creator<JointCoonsultatioonRoomBean> CREATOR = new Creator<JointCoonsultatioonRoomBean>() {
        @Override
        public JointCoonsultatioonRoomBean createFromParcel(Parcel in) {
            return new JointCoonsultatioonRoomBean(in);
        }

        @Override
        public JointCoonsultatioonRoomBean[] newArray(int size) {
            return new JointCoonsultatioonRoomBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
