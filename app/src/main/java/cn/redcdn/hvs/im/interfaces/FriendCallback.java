package cn.redcdn.hvs.im.interfaces;

/**
 * @author LeeDong
 * @version 1.0
 * @created 08-����-2017 ���� 9:53:30
 */
public interface FriendCallback {
	public ResponseEntry m_ResponseEntry = null;//修正
	public void onFinished(ResponseEntry result);
}