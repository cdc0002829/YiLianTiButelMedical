package cn.redcdn.hvs.udtroom.configs;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.hpucenter.data.CSLRoomDetailInfo;
import cn.redcdn.datacenter.hpucenter.data.HPUCommonCode;
import cn.redcdn.log.CustomLog;

/**
 * @author guoyx
 */
public class UDTGlobleData {
    private final String TAG = getClass().getName();
    public int DTState;

    private CSLRoomDetailInfo mCSLRoomDetailInfo;

    private List<DateChangeListener> mDateChangeListenerList;
    private String mAccountId; //视讯号
    private String mDTId; //诊疗号


    public enum DOCTOR_TYPE {
        REQUEST, //求诊医生
        RESPONSE, //接诊医生
        OTHER //旁观者（既不是求诊医生也不是接诊医生）
    }


    public interface DateChangeListener {
        void onDateChanged();
    }


    public void init(String accountId, String dtId) {
        mAccountId = accountId;
        mDTId = dtId;
        mDateChangeListenerList = new ArrayList<>();
        mCSLRoomDetailInfo = new CSLRoomDetailInfo();
    }


    public void release() {
        mDateChangeListenerList.clear();
        mDateChangeListenerList = null;
    }


    public void addListener(DateChangeListener listener) {
        if (listener != null && mDateChangeListenerList != null) {
            mDateChangeListenerList.add(listener);
        }
    }


    public void removeListener(DateChangeListener listener) {
        if (listener != null && mDateChangeListenerList != null) {
            mDateChangeListenerList.remove(listener);
        }
    }


    public void updateCSLRoomDetailInfo(CSLRoomDetailInfo info) {
        if (null != info) {
            mCSLRoomDetailInfo = info;
            if (mDateChangeListenerList != null) {
                for (DateChangeListener listener : mDateChangeListenerList) {
                    listener.onDateChanged();
                }
            }
        }
    }


    /**
     * 获取诊疗状态
     * HPUCommonCode.SEEK_STATE_NOW = 1;// 接诊中
     * HPUCommonCode.SEEK_STATE_NOT = 2;// 待接诊
     * HPUCommonCode.SEEK_STATE_END = 3;// 结束
     */
    public int getState() {
        int state = 1;
        try {
            state = Integer.valueOf(mCSLRoomDetailInfo.state);
        } catch (Exception ex) {
            CustomLog.e(TAG, "getState() error: " + ex.getMessage());
        }

        return state;
    }


    /**
     * 获取预约号
     */
    public String getCurNum() {
        return mCSLRoomDetailInfo.curNum;
    }


    /**
     * 获取当前类型（求诊者、接诊者、旁观者）
     */
    public DOCTOR_TYPE getDoctorType() {
        DOCTOR_TYPE type = DOCTOR_TYPE.OTHER;
        if (mAccountId.equalsIgnoreCase(getRequestNubeNumber())) {
            type = DOCTOR_TYPE.REQUEST;
        } else if (mAccountId.equalsIgnoreCase(getResponseNubeNumber())) {
            type = DOCTOR_TYPE.RESPONSE;
        }
        return type;
    }


    public String getDTId() {
        return mDTId;
    }


    /**
     * 获取诊疗室名称
     */
    public String getDTRoomName() {
        return mCSLRoomDetailInfo.getPatientName() + "联合会诊室";
    }


    /**
     * 获取诊疗群号
     */
    public String getDTGroupID() {
        CustomLog.i(TAG, "getDTGroupID()");
        String groupID = mCSLRoomDetailInfo.getGroupId();
        return groupID;
    }


    /**
     * 获取会议ID
     */
    public String getMeetingId() {
        return mCSLRoomDetailInfo.getMeetingNo();
    }


    /**
     * 获取预约时间
     */
    public String getSchedulDate() {
        return mCSLRoomDetailInfo.schedulDate;
    }


    /**
     * 获取服务器时间
     */
    public String getServerDate() {
        return mCSLRoomDetailInfo.curSystemTime;
    }


    /**
     * 获取时间段
     */
    public String getRangNumber() {
        return mCSLRoomDetailInfo.rangeNumber;
    }


    /**
     * 获取求诊医生职称
     */
    public String getRequestProfessional() {
        return mCSLRoomDetailInfo.requestProfessional;
    }


    /**
     * 获取接诊医生职称
     */
    public String getResponseProfessional() {
        return mCSLRoomDetailInfo.responseProfessional;
    }


    /**
     * 获取求诊医生头像
     */
    public String getRequestHeadThumUrl() {
        return mCSLRoomDetailInfo.requestHeadThumUrl;
    }


    /**
     * 获取接诊医生头像
     */
    public String getResponseHeadThumUrl() {
        return mCSLRoomDetailInfo.responseHeadThumUrl;
    }


    /**
     * 获取患者姓名
     */
    public String getPatientAge() {
        return mCSLRoomDetailInfo.patientAge;
    }


    /**
     * 获取患者id
     */
    public String getPatientId() {
        return mCSLRoomDetailInfo.patientId;
    }


    /**
     * 获取患者姓名
     */
    public String getPatientName() {
        return mCSLRoomDetailInfo.patientName;
    }


    /**
     * 获取患者证件类型（身份证、出生证等）
     */
    public String getCardType() {
        return mCSLRoomDetailInfo.cardType;
    }


    /**
     * 获取患者证件号
     */
    public String getPatientCardNum() {
        return mCSLRoomDetailInfo.patientCard;
    }


    /**
     * 获取患者手机号
     */
    public String getPatientMobile() {
        return mCSLRoomDetailInfo.patientMobile;
    }


    /**
     * 获取患者性别
     */
    public String getPatientSex() {
        return mCSLRoomDetailInfo.patientSex;
    }


    /**
     * 获取患者身高
     */
    public String getPatientHeight() {
        return mCSLRoomDetailInfo.height;
    }


    /**
     * 获取患者体重
     */
    public String getPatientWeight() {
        return mCSLRoomDetailInfo.weight;
    }


    /**
     * 获取患者主诉
     */
    public String getPatientChief() {
        return mCSLRoomDetailInfo.chief;
    }


    /**
     * 获取患者查体
     */
    public String getPhysical() {
        return mCSLRoomDetailInfo.physical;
    }


    /**
     * 获取患者辅助检查
     */
    public String getAssCheckUrl() {
        return mCSLRoomDetailInfo.assCheckUrl;
    }


    /**
     * 获取患者目前待解决问题
     */
    public String getProblem() {
        return mCSLRoomDetailInfo.problem;
    }


    /**
     * 获取监护人身份证号
     */
    public String getGuardCardNum() {
        return mCSLRoomDetailInfo.guardCard;
    }


    /**
     * 获取监护人手机号
     */
    public String getGuardMobile() {
        return mCSLRoomDetailInfo.guardMobile;
    }


    /**
     * 获取监护人姓名
     */
    public String getGuardName() {
        return mCSLRoomDetailInfo.guardName;
    }


    /**
     * 获取求诊医生视讯号
     */
    public String getRequestNubeNumber() {
        return mCSLRoomDetailInfo.requestNubeNumber;
    }


    /**
     * 获取求诊医生医院
     */
    public String getRequestHosp() {
        return mCSLRoomDetailInfo.requestHosp;
    }


    /**
     * 获取求诊医生科室
     */
    public String getRequestDep() {
        return mCSLRoomDetailInfo.requestDep;
    }


    /**
     * 获取求诊医生姓名
     */
    public String getRequestName() {
        return mCSLRoomDetailInfo.requestName;
    }


    /**
     * 获取接诊医生视讯号
     */
    public String getResponseNubeNumber() {
        return mCSLRoomDetailInfo.responseNubeNumber;
    }


    /**
     * 获取接诊医生医院
     */
    public String getResponseHosp() {
        return mCSLRoomDetailInfo.responseHosp;
    }


    /**
     * 获取接诊医生科室
     */
    public String getResponseDep() {
        return mCSLRoomDetailInfo.responseDep;
    }


    /**
     * 获取接诊医生姓名
     */
    public String getResponseName() {
        return mCSLRoomDetailInfo.responseName;
    }


    /**
     * 求诊方是否评价
     */
    public boolean isRequestEvaluate() {
        return TextUtils.isEmpty(mCSLRoomDetailInfo.requestScore) ? false : true;
    }


    /**
     * 接诊方是否评价
     */
    public boolean isRespoonseEvaluate() {
        return TextUtils.isEmpty(mCSLRoomDetailInfo.responseScore) ? false : true;
    }


    /**
     * 获取求诊评价分数
     */
    public int getRequestScore() {
        int score = 0;
        try {
            score = Integer.valueOf(mCSLRoomDetailInfo.requestScore);
        } catch (Exception ex) {
            CustomLog.e(TAG, "getRequestScore() error." + ex.getMessage());
        }
        return score;
    }


    /**
     * 获取求诊评价内容
     */
    public String getRequestReview() {
        return mCSLRoomDetailInfo.requestReview;
    }


    /**
     * 获取接诊方评价分数
     */
    public int getResponseScore() {
        int score = 0;
        try {
            score = Integer.valueOf(mCSLRoomDetailInfo.responseScore);
        } catch (Exception ex) {
            CustomLog.e(TAG, "getResponseScore() error." + ex.getMessage());
        }
        return score;
    }


    /**
     * 获取接诊评价内容
     */
    public String getResponseReview() {
        return mCSLRoomDetailInfo.getResponseReview();
    }


    /**
     * 获取诊疗意见（只有本地诊疗时有）
     */
    public String getAdvice() {
        return mCSLRoomDetailInfo.advice;
    }


    /**
     * 获取诊疗结论，目前包含两种结论：1（本地治疗）；2（转诊）
     * 0：无结论，转诊未结束
     * 1：本地治疗
     * 2：转诊
     */
    public int getDTResult() {
        int result = 0;
        try {
            result = Integer.valueOf(mCSLRoomDetailInfo.transferFlg);
        } catch (Exception ex) {
            CustomLog.e(TAG, "getDTResult() error: " + ex.getMessage());
        }
        return result;
    }


    /**
     * 获取转诊ID
     */
    public String getTransferId() {
        return mCSLRoomDetailInfo.getTransferId();
    }


    /**
     * 获取转诊诊疗意见
     */
    public String getTransferAdvice() {
        return mCSLRoomDetailInfo.getTransferAdvice();
    }


    /**
     * 获取转诊医院
     */
    public String getTransferHosp() {
        return mCSLRoomDetailInfo.getTransferHosp();
    }


    /**
     * 获取转诊科室
     */
    public String getTransferDept() {
        return mCSLRoomDetailInfo.getTransferDept();
    }


    /**
     * 获取转诊科室类型
     *
     * @return 0：无转诊，1：普通门诊；2：副高门诊；3：专家门诊
     */
    public int getTransferSectionType() {
        int type = 0;
        try {
            type = Integer.valueOf(mCSLRoomDetailInfo.getSectionType());
        } catch (Exception ex) {
            CustomLog.e(TAG, "getTransferType() error: " + ex.getMessage());
        }
        return type;
    }


    /**
     * 获取转诊排班时间
     *
     * @return YYYYMMDD
     */
    public String getTransferScheduDate() {
        return mCSLRoomDetailInfo.getTransferSchedulDate();
    }


    /**
     * 获取转诊时间段
     *
     * @return 上午、下午
     */
    public String getTransferRange() {
        String range = "上午";
        if (HPUCommonCode.RANGE_AFTERNOON.equalsIgnoreCase(
            mCSLRoomDetailInfo.getTransferRangeFlg())) {
            range = "下午";
        }
        return range;
    }
    public String getTransferDoctorName(){
        return mCSLRoomDetailInfo.expertName;
    }
}
