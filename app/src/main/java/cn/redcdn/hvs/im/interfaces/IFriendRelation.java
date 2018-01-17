package cn.redcdn.hvs.im.interfaces;

/**
 * @author LeeDong
 * @version 1.0
 */
public interface IFriendRelation {

	/**
	 * @param nubeNumber
	 */
	public void onFriendAdded(String nubeNumber,String userName,String headUrl);

	/**
	 * 
	 * @param nubeNumber   好友关系下降(单边,或者无关系)
	 */
	public void onFriendDeleted(String nubeNumber);

	/**
	 * nubeNumber: String
	 * name: String
	 * headUrl: String
	 * msgType: int
	 * msgContent: String
	 * time: String
	 * 
	 * @param nubeNumber
	 * @param name
	 * @param headUrl
	 * @param msgContent
	 * @param time
	 * @param strangeMsgType  1|主动消息   0|被动回复
	 */
	public void onMsgArrived(String nubeNumber, String name, String headUrl, String msgContent, String time,int strangeMsgType);

}