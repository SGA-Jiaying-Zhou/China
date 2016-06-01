/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/zhoujy/merge/FY16/China/TVCamera/src/com/sony/dtv/tvx/tvplayer/legacy/ITvStateSetPipSubScreenPositionListener.aidl
 */
package com.sony.dtv.tvx.tvplayer.legacy;
/**
 * Interface definition for a callback to be invoked when
  * {@link ITvPlayerService#setPipSubScreenPosition()} results were found.
 */
public interface ITvStateSetPipSubScreenPositionListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPipSubScreenPositionListener
{
private static final java.lang.String DESCRIPTOR = "com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPipSubScreenPositionListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPipSubScreenPositionListener interface,
 * generating a proxy if needed.
 */
public static com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPipSubScreenPositionListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPipSubScreenPositionListener))) {
return ((com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPipSubScreenPositionListener)iin);
}
return new com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPipSubScreenPositionListener.Stub.Proxy(obj);
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
case TRANSACTION_notifyDone:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.notifyDone(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_notifyFail:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.notifyFail(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPipSubScreenPositionListener
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
     * Called when {@link ITvPlayerService#setPipSubScreenPosition()} has been completed.
     *
     * @param position screen position of PIP.
     * This parameter can be {@code leftTop}, {@code leftBottom}, {@code rightTop} or {@code rightBottom}.
     */
@Override public void notifyDone(java.lang.String position) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(position);
mRemote.transact(Stub.TRANSACTION_notifyDone, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Called when {@link ITvPlayerService#setPipSubScreenPosition()} has been completed.
     *
     * @param error error code
     */
@Override public void notifyFail(int error) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(error);
mRemote.transact(Stub.TRANSACTION_notifyFail, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_notifyDone = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_notifyFail = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * Called when {@link ITvPlayerService#setPipSubScreenPosition()} has been completed.
     *
     * @param position screen position of PIP.
     * This parameter can be {@code leftTop}, {@code leftBottom}, {@code rightTop} or {@code rightBottom}.
     */
public void notifyDone(java.lang.String position) throws android.os.RemoteException;
/**
     * Called when {@link ITvPlayerService#setPipSubScreenPosition()} has been completed.
     *
     * @param error error code
     */
public void notifyFail(int error) throws android.os.RemoteException;
}
