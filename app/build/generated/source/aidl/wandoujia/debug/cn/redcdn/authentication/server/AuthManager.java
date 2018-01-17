/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\Perforce\\005_Medical\\android\\src\\YLTButelMedical\\app\\src\\main\\aidl\\cn\\redcdn\\authentication\\server\\AuthManager.aidl
 */
package cn.redcdn.authentication.server;
public interface AuthManager extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements cn.redcdn.authentication.server.AuthManager
{
private static final java.lang.String DESCRIPTOR = "cn.redcdn.authentication.server.AuthManager";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an cn.redcdn.authentication.server.AuthManager interface,
 * generating a proxy if needed.
 */
public static cn.redcdn.authentication.server.AuthManager asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof cn.redcdn.authentication.server.AuthManager))) {
return ((cn.redcdn.authentication.server.AuthManager)iin);
}
return new cn.redcdn.authentication.server.AuthManager.Stub.Proxy(obj);
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
case TRANSACTION_getInstance:
{
data.enforceInterface(DESCRIPTOR);
android.os.Bundle _arg0;
if ((0!=data.readInt())) {
_arg0 = android.os.Bundle.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
cn.redcdn.authentication.server.RealAuthManager _result = this.getInstance(_arg0);
reply.writeNoException();
reply.writeStrongBinder((((_result!=null))?(_result.asBinder()):(null)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements cn.redcdn.authentication.server.AuthManager
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
@Override public cn.redcdn.authentication.server.RealAuthManager getInstance(android.os.Bundle callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
cn.redcdn.authentication.server.RealAuthManager _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((callback!=null)) {
_data.writeInt(1);
callback.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_getInstance, _data, _reply, 0);
_reply.readException();
_result = cn.redcdn.authentication.server.RealAuthManager.Stub.asInterface(_reply.readStrongBinder());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getInstance = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public cn.redcdn.authentication.server.RealAuthManager getInstance(android.os.Bundle callback) throws android.os.RemoteException;
}
