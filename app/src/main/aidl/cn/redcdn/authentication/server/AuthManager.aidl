package cn.redcdn.authentication.server;
import cn.redcdn.authentication.server.AuthenticateInfo;
import cn.redcdn.authentication.server.RealAuthManager;
interface AuthManager{
  RealAuthManager getInstance(in Bundle callback);
}