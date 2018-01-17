package cn.redcdn.hvs.meeting.util;

import android.text.TextUtils;

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
	
	/**
	 * 会议外呼弹屏时，名称展现获取
	 * 先从本地数据库取好友信息，不存在的话则使用p2p传来的昵称值
	 * @param nube
	 * @param nickname
	 * @return
	 */
	public static String getIncomingCallName(String nube ,String nickname){
		if(!TextUtils.isEmpty(nube)){
//			Context context = NetPhoneApplication.getContext();
//			NetPhoneDaoImpl contactDao =  new NetPhoneDaoImpl(context);
//			ContactFriendPo contact = contactDao.queryFriendInfoByNube(nube);
			
			NameElement element = new NameElement();
//			if(contact!=null){
//				element.remarkName = contact.getName();
//				element.nickName = contact.getNickname();
//				element.mobile = contact.getNumber();
//				element.nubeNumber = contact.getNubeNumber();
//			}else{
//				element.nickName = nickname;
//				element.nubeNumber = nube;
//			}
			return getShowName(element);
			
		}else{
			return "";
		}
	}
	
	public static NameElement getNameElement(String nube){
//		Context context = NetPhoneApplication.getContext();
//		NetPhoneDaoImpl contactDao =  new NetPhoneDaoImpl(context);
//		ContactFriendPo contact = contactDao.queryFriendInfoByNube(nube);
		
		NameElement element = new NameElement();
//		if(contact!=null){
//			element.remarkName = contact.getName();
//			element.nickName = contact.getNickname();
//			element.mobile = contact.getNumber();
//			element.nubeNumber = contact.getNubeNumber();
//		}else{
//			element.mobile = QueryPhoneNumberHelper.getPhoneNumberByNubeFromCache(nube);
//			element.nubeNumber = nube;
//		}
		
		return element;
	}
	
	
	public static String getShowName(NameElement element){
		String showname = "";
		if(element!=null){
			if(!TextUtils.isEmpty(element.remarkName)){
				showname = element.remarkName;
			}else if(!TextUtils.isEmpty(element.nickName)){
				showname = element.nickName;
			}
//			else if(!TextUtils.isEmpty(element.mobile)){
//				showname = element.mobile;
//			}
//			else if(!TextUtils.isEmpty(element.nubeNumber)){
//			    if (element.nubeNumber.equals(NetPhoneApplication.getPreference().getKeyValue(
//			            PrefType.KEY_BUTEL_PUBLIC_NO, ""))) {
//			        // 可视官方帐号
//			        showname = NetPhoneApplication.getContext().getString(R.string.str_butel_name);
//			    } else {
//			        showname = element.nubeNumber;
//			    }
//			}
		}
		return showname;
	}
	
	public static String getShowName(String nube){
		if(!TextUtils.isEmpty(nube)){
			NameElement element = getNameElement(nube);
			return getShowName(element);
		}
		return "";
	}
	
	public static String getShowNumber(NameElement element){
		String shownumber = "";
		if(element!=null){
//			if(!TextUtils.isEmpty(element.mobile)){
//				shownumber = element.mobile;
//			}else 
				if(!TextUtils.isEmpty(element.nubeNumber)){
				shownumber = element.nubeNumber;
			}
		}
		return shownumber;
	}
	
	public static String getShowNumber(String nube){
		if(!TextUtils.isEmpty(nube)){
			NameElement element = getNameElement(nube);
			return getShowNumber(element);
		}
		return "";
	}
}
