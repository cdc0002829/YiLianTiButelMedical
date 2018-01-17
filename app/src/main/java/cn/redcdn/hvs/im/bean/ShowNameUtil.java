package cn.redcdn.hvs.im.bean;

import android.content.Context;
import android.text.TextUtils;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.dao.NetPhoneDaoImpl;
import cn.redcdn.hvs.im.preference.DaoPreference.PrefType;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class ShowNameUtil {

    public static class NameElement{
        public String remarkName ="";
        public String nickName ="";
        public String mobile ="";
        public String nubeNumber ="";
    }

    public static NameElement getNameElement(String remarkname,String nickname, String mobile, String nube){
        NameElement element = new NameElement();
        element.remarkName = remarkname;
        element.nickName = nickname;
        element.mobile = mobile;
        element.nubeNumber = nube;

        return element;
    }

    public static String getShowName(NameElement element){
        String showname = "";
        if(element!=null){

            if(!TextUtils.isEmpty(element.nickName)){
                showname = element.nickName;
            }
//			else if(!TextUtils.isEmpty(element.mobile)){
//				showname = element.mobile;
//			}
            else if(!TextUtils.isEmpty(element.nubeNumber)){
                if (element.nubeNumber.equals(MedicalApplication.getPreference().getKeyValue(
                        PrefType.KEY_BUTEL_PUBLIC_NO, ""))) {
                    // 可视官方帐号
                    showname = MedicalApplication.getContext().getString(R.string.str_butel_name);
                } else {
                    showname = element.nubeNumber;
                }
            }else if(!TextUtils.isEmpty(element.remarkName)){
                showname = element.remarkName;
            }
        }
        return showname;
    }
    public static String getShowNumber(NameElement element) {
        String shownumber = "";
        if (element != null) {
            if (!TextUtils.isEmpty(element.nubeNumber)) {
                shownumber = element.nubeNumber;
            }
        }
        return shownumber;
    }

    public static NameElement getNameElement(String nube) {
        Context context = MedicalApplication.getContext();
        NetPhoneDaoImpl contactDao = new NetPhoneDaoImpl(context);
        ContactFriendBean contact = contactDao.queryFriendInfoByNube(nube);

        NameElement element = new NameElement();
        if (contact != null) {
            element.remarkName = contact.getName();
            element.nickName = contact.getNickname();
            element.mobile = contact.getNumber();
            element.nubeNumber = contact.getNubeNumber();
        } else {
            // element.mobile = com.channelsoft.netphone.asyncTask.QueryPhoneNumberHelper.getPhoneNumberByNubeFromCache(nube);
            element.nubeNumber = nube;
        }

        return element;
    }


    public static String getShowName(String nube){
        if(!TextUtils.isEmpty(nube)){
            NameElement element = getNameElement(nube);
            return getShowName(element);
        }
        return "";
    }



}
