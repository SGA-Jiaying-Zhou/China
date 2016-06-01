/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/zhoujy/merge/FY16/China/TVCamera/src/com/sony/dtv/tvx/tvplayer/legacy/ITvDecimateService.aidl
 */
package com.sony.dtv.tvx.tvplayer.legacy;
/**
 * Interface definition for a request to DecimateService.
 */
public interface ITvDecimateService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateService
{
private static final java.lang.String DESCRIPTOR = "com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateService interface,
 * generating a proxy if needed.
 */
public static com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateService))) {
return ((com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateService)iin);
}
return new com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateService.Stub.Proxy(obj);
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
case TRANSACTION_requestScalePermission:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener _arg2;
_arg2 = com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener.Stub.asInterface(data.readStrongBinder());
int _result = this.requestScalePermission(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_cancelRequestScalePermission:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.cancelRequestScalePermission(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_changeLayout:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
float _arg1;
_arg1 = data.readFloat();
float _arg2;
_arg2 = data.readFloat();
float _arg3;
_arg3 = data.readFloat();
float _arg4;
_arg4 = data.readFloat();
boolean _arg5;
_arg5 = (0!=data.readInt());
int _result = this.changeLayout(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateService
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
/**
     * Requests permission for the layout change.
     *
     * @param className the identifier of client
     * @param priority only support priority NORMAL (=1)
     * @param listener the listener for receiving a callback
     * @return 0 if success, otherwise error
     * @throws UnsupportedOperationException if the priority is not NORMAL (=1)
     */
@Override public int requestScalePermission(java.lang.String className, int priority, com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(className);
_data.writeInt(priority);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_requestScalePermission, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Cancel the permission request.
     *
     * @param className the identifier of client
     * @return 0 if success, otherwise error
     */
@Override public int cancelRequestScalePermission(java.lang.String className) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(className);
mRemote.transact(Stub.TRANSACTION_cancelRequestScalePermission, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Change the size of the content area.
     *
     * @param className the identifier of client
     * @param height the height of decimate size. Relative value with that full screen is 1. (0, …, 1)
     * @param width the width of decimate size. Relative value with that full screen is 1. (0, …, 1)
     * @param x the X coordinate (left top is 0). Relative value with that full screen is 1. (0, …, 1)
     * @param y the Y coordinate (left top is 0). Relative value with that full screen is 1. (0, …, 1)
     * @param multiPictureCancel only support {@code true}
     * @throws UnsupportedOperationException if the multiPictureCancel is {@code false}
     */
@Override public int changeLayout(java.lang.String className, float height, float width, float x, float y, boolean multiPictureCancel) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(className);
_data.writeFloat(height);
_data.writeFloat(width);
_data.writeFloat(x);
_data.writeFloat(y);
_data.writeInt(((multiPictureCancel)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_changeLayout, _data, _reply, 0);
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
static final int TRANSACTION_requestScalePermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_cancelRequestScalePermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_changeLayout = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
/**
     * Requests permission for the layout change.
     *
     * @param className the identifier of client
     * @param priority only support priority NORMAL (=1)
     * @param listener the listener for receiving a callback
     * @return 0 if success, otherwise error
     * @throws UnsupportedOperationException if the priority is not NORMAL (=1)
     */
public int requestScalePermission(java.lang.String className, int priority, com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener listener) throws android.os.RemoteException;
/**
     * Cancel the permission request.
     *
     * @param className the identifier of client
     * @return 0 if success, otherwise error
     */
public int cancelRequestScalePermission(java.lang.String className) throws android.os.RemoteException;
/**
     * Change the size of the content area.
     *
     * @param className the identifier of client
     * @param height the height of decimate size. Relative value with that full screen is 1. (0, …, 1)
     * @param width the width of decimate size. Relative value with that full screen is 1. (0, …, 1)
     * @param x the X coordinate (left top is 0). Relative value with that full screen is 1. (0, …, 1)
     * @param y the Y coordinate (left top is 0). Relative value with that full screen is 1. (0, …, 1)
     * @param multiPictureCancel only support {@code true}
     * @throws UnsupportedOperationException if the multiPictureCancel is {@code false}
     */
public int changeLayout(java.lang.String className, float height, float width, float x, float y, boolean multiPictureCancel) throws android.os.RemoteException;
}
