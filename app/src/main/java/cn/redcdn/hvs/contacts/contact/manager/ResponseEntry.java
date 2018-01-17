package cn.redcdn.hvs.contacts.contact.manager;

public class ResponseEntry {
  public int status; // >= 0 则认为是操作成功
  public Object content; // 返回值，根据操作请求不同，可能返回 DataSet 或者 int
}
