package cn.redcdn.hvs.im.interfaces;

public class ResponseEntry {
  public int status; // >= 0 则认为是操作成功
  public Object content; // 返回值  返回list<FriendInfo>
}
