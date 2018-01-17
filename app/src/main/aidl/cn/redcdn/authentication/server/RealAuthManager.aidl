package cn.redcdn.authentication.server;
import cn.redcdn.authentication.server.AuthenticateInfo;
interface RealAuthManager{
  int setUserInfo(String userCenterUrl, String imei, String appKey, String account, String password, String productId, String appType,String deviceType);
  AuthenticateInfo getAuthenticateInfo();
  int authenticate(String token, String userCenterUrl, String imei, String appKey, String account, String password, String productId, String appType, String deviceType,String appInfo);
  void release();
  int setAccountAttr(String token, String name, String headUrl);
  int cancelSetAccountAttr();
}