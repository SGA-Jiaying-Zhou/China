/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/zhoujy/merge/FY16/China/TVCamera/src/com/sony/dtv/tvx/tvplayer/legacy/ITvDecimateListener.aidl
 */
package com.sony.dtv.tvx.tvplayer.legacy;
/**
 * Interface definition for a callback to be invoked from DecimateService.
 */
public interface ITvDecimateListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener
{
private static final java.lang.String DESCRIPTOR = "com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener interface,
 * generating a proxy if needed.
 */
public static com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener))) {
return ((com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener)iin);
}
return new com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener.Stub.Proxy(obj);
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
case TRANSACTION_notifyPermissionGranted:
{
data.enforceInterface(DESCRIPTOR);
this.notifyPermissionGranted();
reply.writeNoException();
return true;
}
case TRANSACTION_notifyPermissionDeprived:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.notifyPermissionDeprived();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_notifyLayoutChanged:
{
data.enforceInterface(DESCRIPTOR);
this.notifyLayoutChanged();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener
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
     * Calls when the permission granted.
     */
@Override public void notifyPermissionGranted() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_notifyPermissionGranted, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * calls when the permission deprived.
     *
     * @return if want to return to the full screen, return true
     */
@Override public boolean notifyPermissionDeprived() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_notifyPermissionDeprived, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Calls when the layout has been changed.
     */
@Override public void notifyLayoutChanged() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_notifyLayoutChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_notifyPermissionGranted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_notifyPermissionDeprived = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_notifyLayoutChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
/**
     * Calls when the permission granted.
     */
public void notifyPermissionGranted() throws android.os.RemoteException;
/**
     * calls when the permission deprived.
     *
     * @return if want to return to the full screen, return true
     */
public boolean notifyPermissionDeprived() throws android.os.RemoteException;
/**
     * Calls when the layout has been changed.
     */
public void notifyLayoutChanged() throws android.os.RemoteException;
}
