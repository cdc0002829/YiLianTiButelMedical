/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\Perforce\\005_Medical\\android\\src\\YLTButelMedical\\app\\src\\main\\aidl\\cn\\redcdn\\authentication\\server\\RealAuthManager.aidl
 */
package cn.redcdn.authentication.server;
public interface RealAuthManager extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements cn.redcdn.authentication.server.RealAuthManager
{
private static final java.lang.String DESCRIPTOR = "cn.redcdn.authentication.server.RealAuthManager";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an cn.redcdn.authentication.server.RealAuthManager interface,
 * generating a proxy if needed.
 */
public static cn.redcdn.authentication.server.RealAuthManager asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof cn.redcdn.authentication.server.RealAuthManager))) {
return ((cn.redcdn.authentication.server.RealAuthManager)iin);
}
return new cn.redcdn.authentication.server.RealAuthManager.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_setUserInfo:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _arg4;
_arg4 = data.readString();
java.lang.String _arg5;
_arg5 = data.readString();
java.lang.String _arg6;
_arg6 = data.readString();
java.lang.String _arg7;
_arg7 = data.readString();
int _result = this.setUserInfo(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getAuthenticateInfo:
{
data.enforceInterface(DESCRIPTOR);
AuthenticateInfo _result = this.getAuthenticateInfo();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_authenticate:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _arg4;
_arg4 = data.readString();
java.lang.String _arg5;
_arg5 = data.readString();
java.lang.String _arg6;
_arg6 = data.readString();
java.lang.String _arg7;
_arg7 = data.readString();
java.lang.String _arg8;
_arg8 = data.readString();
java.lang.String _arg9;
_arg9 = data.readString();
int _result = this.authenticate(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_release:
{
data.enforceInterface(DESCRIPTOR);
this.release();
reply.writeNoException();
return true;
}
case TRANSACTION_setAccountAttr:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
int _result = this.setAccountAttr(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_cancelSetAccountAttr:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.cancelSetAccountAttr();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements cn.redcdn.authentication.server.RealAuthManager
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public int setUserInfo(java.lang.String userCenterUrl, java.lang.String imei, java.lang.String appKey, java.lang.String account, java.lang.String password, java.lang.String productId, java.lang.String appType, java.lang.String deviceType) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(userCenterUrl);
_data.writeString(imei);
_data.writeString(appKey);
_data.writeString(account);
_data.writeString(password);
_data.writeString(productId);
_data.writeString(appType);
_data.writeString(deviceType);
mRemote.transact(Stub.TRANSACTION_setUserInfo, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public AuthenticateInfo getAuthenticateInfo() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
AuthenticateInfo _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAuthenticateInfo, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = AuthenticateInfo.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int authenticate(java.lang.String token, java.lang.String userCenterUrl, java.lang.String imei, java.lang.String appKey, java.lang.String account, java.lang.String password, java.lang.String productId, java.lang.String appType, java.lang.String deviceType, java.lang.String appInfo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(token);
_data.writeString(userCenterUrl);
_data.writeString(imei);
_data.writeString(appKey);
_data.writeString(account);
_data.writeString(password);
_data.writeString(productId);
_data.writeString(appType);
_data.writeString(deviceType);
_data.writeString(appInfo);
mRemote.transact(Stub.TRANSACTION_authenticate, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void release() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_release, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int setAccountAttr(java.lang.String token, java.lang.String name, java.lang.String headUrl) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(token);
_data.writeString(name);
_data.writeString(headUrl);
mRemote.transact(Stub.TRANSACTION_setAccountAttr, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int cancelSetAccountAttr() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_cancelSetAccountAttr, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_setUserInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getAuthenticateInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_authenticate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_release = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_setAccountAttr = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_cancelSetAccountAttr = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
}
public int setUserInfo(java.lang.String userCenterUrl, java.lang.String imei, java.lang.String appKey, java.lang.String account, java.lang.String password, java.lang.String productId, java.lang.String appType, java.lang.String deviceType) throws android.os.RemoteException;
public AuthenticateInfo getAuthenticateInfo() throws android.os.RemoteException;
public int authenticate(java.lang.String token, java.lang.String userCenterUrl, java.lang.String imei, java.lang.String appKey, java.lang.String account, java.lang.String password, java.lang.String productId, java.lang.String appType, java.lang.String deviceType, java.lang.String appInfo) throws android.os.RemoteException;
public void release() throws android.os.RemoteException;
public int setAccountAttr(java.lang.String token, java.lang.String name, java.lang.String headUrl) throws android.os.RemoteException;
public int cancelSetAccountAttr() throws android.os.RemoteException;
}
