/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/zhoujy/merge/FY16/China/TVCamera/src/com/sony/dtv/tvx/tvplayer/legacy/ITvPlayerService.aidl
 */
package com.sony.dtv.tvx.tvplayer.legacy;
/**
 *  Interface for provides a Tv player state.
 */
public interface ITvPlayerService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.sony.dtv.tvx.tvplayer.legacy.ITvPlayerService
{
private static final java.lang.String DESCRIPTOR = "com.sony.dtv.tvx.tvplayer.legacy.ITvPlayerService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.sony.dtv.tvx.tvplayer.legacy.ITvPlayerService interface,
 * generating a proxy if needed.
 */
public static com.sony.dtv.tvx.tvplayer.legacy.ITvPlayerService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.sony.dtv.tvx.tvplayer.legacy.ITvPlayerService))) {
return ((com.sony.dtv.tvx.tvplayer.legacy.ITvPlayerService)iin);
}
return new com.sony.dtv.tvx.tvplayer.legacy.ITvPlayerService.Stub.Proxy(obj);
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
case TRANSACTION_getCurrentFavoriteId:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getCurrentFavoriteId();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getCurrentInputInfo:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
android.os.Bundle _result = this.getCurrentInputInfo(_arg0);
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
case TRANSACTION_getPathInfo:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getPathInfo(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getMultiScreenMode:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getMultiScreenMode();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_setMultiScreenMode:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetMultiScreenModeListener _arg1;
_arg1 = com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetMultiScreenModeListener.Stub.asInterface(data.readStrongBinder());
int _result = this.setMultiScreenMode(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getPapScreenSize:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getPapScreenSize();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_setPapScreenSize:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPapScreenSizeListener _arg2;
_arg2 = com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPapScreenSizeListener.Stub.asInterface(data.readStrongBinder());
int _result = this.setPapScreenSize(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getPipSubScreenPosition:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getPipSubScreenPosition();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_setPipSubScreenPosition:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPipSubScreenPositionListener _arg1;
_arg1 = com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPipSubScreenPositionListener.Stub.asInterface(data.readStrongBinder());
int _result = this.setPipSubScreenPosition(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getFocusScreen:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getFocusScreen();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setFocusScreen:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _result = this.setFocusScreen(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_sendKeyEvent:
{
data.enforceInterface(DESCRIPTOR);
android.view.KeyEvent _arg0;
if ((0!=data.readInt())) {
_arg0 = android.view.KeyEvent.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.sendKeyEvent(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.sony.dtv.tvx.tvplayer.legacy.ITvPlayerService
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
     * Returns the currently valid favorite id.
     *
     * @return the Id in the case of Favorite Mode.<br>
     *         case of non-Favorite Mode returns Null.
     */
@Override public java.lang.String getCurrentFavoriteId() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCurrentFavoriteId, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Returns the current input {@link Bundle}.
     *
     * @param id The id should be one of the followings:
     * <ul>
     * <li>0 : Focused screen including SINGLE mode
     * <li>1 : PIP : BIG screen, PAP : LEFT screen
     * <li>2 : PIP : SMALL screen, PAP : RIGHT screen
     * </ul>
     * @return detail is following, null if id is invalid:
     * <ul>
     * <li>Key "type": TUNER, EXTERNAL_INPUT. null if TvPlayer is hidden.
     * <li>Key "channel_id": same as TvContract.Channels._ID. -1 if TvPlayer is hidden.
     * <li>Key "type_external_input": COMPOSITE, SCART, COMPONENT, VGA, HDMI.
      * null if TvPlayer is hidden.
     * <li>Key "input_id": input id of current channel. null if TvPlayer is hidden.
     * </ul>
     */
@Override public android.os.Bundle getCurrentInputInfo(int id) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(id);
mRemote.transact(Stub.TRANSACTION_getCurrentInputInfo, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.os.Bundle.CREATOR.createFromParcel(_reply);
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
/**
     * Returns the path info that TIS/MW should be use.
     *
     * The path info can be {@code MAIN} or {@code SUB}.
     *
     * @return Returns {@code MAIN} if single mode.
     * Returns specified inputId is to determine whether the {@code MAIN} or {@code SUB} if PIP or PAP mode.
     */
@Override public java.lang.String getPathInfo(java.lang.String inputId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(inputId);
mRemote.transact(Stub.TRANSACTION_getPathInfo, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Returns the current multi screen mode.
     *
     * @return Returns {@code SINGLE} if single mode.
     * Returns {@code PIP} if PAP mode.
     * Returns {@code PAP} if PAP mode.
     */
@Override public java.lang.String getMultiScreenMode() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getMultiScreenMode, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Sets the current multi screen mode.
     *
     * The multi screen mode can be {@code SINGLE} or {@code PIP} or {@code PAP}.
     *
     * @param mode multi screen mode
     * @param listener {@link ITvStateSetMultiScreenModeListener} to receive the result of this method is success or not.
     * @return 0 if succeeded, else if error.
     */
@Override public int setMultiScreenMode(java.lang.String mode, com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetMultiScreenModeListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(mode);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setMultiScreenMode, _data, _reply, 0);
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
     * Returns the screen size of PAP.
     *
     * @reutrn Returns {@code null} if current screen mode is not PAP.
     * Returns {@code mainBig} if main screen is big.
     * Returns {@code mainSmall} if if main screen is small.
     */
@Override public java.lang.String getPapScreenSize() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPapScreenSize, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Sets the screen size of PAP.
     *
     * @param screen screen of left or right of PAP. This parameter can be {@code main} or {@code sub}
     * @param size {@code +1} to become bigger the specified screen.
     * {@code -1} to become smaller the s@ecified screen.
     * @param listener {@link ITvStateSetPapScreenSizeListener} to receive the result of this method is success or not.
     */
@Override public int setPapScreenSize(java.lang.String screen, java.lang.String size, com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPapScreenSizeListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(screen);
_data.writeString(size);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setPapScreenSize, _data, _reply, 0);
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
     * Returns the sub screen position of PIP.
     *
     * @reutrn Returns {@code null} if current screen mode is not PIP.
     * Returns {@code leftTop} if sub screen is in the top left.
     * Returns {@code leftBottom} if sub screen is in the bottom left.
     * Returns {@code rightTop} if sub screen is in the top right.
     * Returns {@code rightBottom} if sub screen is in the bottom right.
     */
@Override public java.lang.String getPipSubScreenPosition() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPipSubScreenPosition, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Sets the sub screen position of PIP.
     *
     * @param position screen position of PIP. This parameter can be {@code leftTop}, {@code leftBottom}, {@code rightTop} or {@code rightBottom}.
     * @param listener {@link ITvStateSetPipSubScreenPositionListener} to receive the result of this method is success or not.
     */
@Override public int setPipSubScreenPosition(java.lang.String position, com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPipSubScreenPositionListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(position);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setPipSubScreenPosition, _data, _reply, 0);
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
     * Returns the value, which means focused screen.
     *
     * @return the following value:
     * <ul>
     * <li>1 : when PIP BIG screen / when PAP LEFT screen (include SINGLE case)
     * <li>2 : when PIP SMALL screen / when PAP RIGHT screen
     * <li>Other : Error code
     * </ul>
     */
@Override public int getFocusScreen() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getFocusScreen, _data, _reply, 0);
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
     * Sets the value, which means focus screen.
     *
     * @param position This parameter can be following:
     * <ul>
     * <li>1 : when PIP BIG screen / when PAP LEFT screen
     * <li>2 : when PIP SMALL screen / when PAP RIGHT screen
     * </ul>
     * @return Returns {@code 0} if succeeded, else if error.
     */
@Override public int setFocusScreen(int position) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(position);
mRemote.transact(Stub.TRANSACTION_setFocusScreen, _data, _reply, 0);
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
     * Send key event to TvPlayer.
     *
     * @param event {@link KeyEvent} to send the key event
     */
@Override public void sendKeyEvent(android.view.KeyEvent event) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((event!=null)) {
_data.writeInt(1);
event.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_sendKeyEvent, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_getCurrentFavoriteId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getCurrentInputInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getPathInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getMultiScreenMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_setMultiScreenMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getPapScreenSize = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_setPapScreenSize = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getPipSubScreenPosition = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_setPipSubScreenPosition = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getFocusScreen = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_setFocusScreen = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_sendKeyEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
}
/**
     * Returns the currently valid favorite id.
     *
     * @return the Id in the case of Favorite Mode.<br>
     *         case of non-Favorite Mode returns Null.
     */
public java.lang.String getCurrentFavoriteId() throws android.os.RemoteException;
/**
     * Returns the current input {@link Bundle}.
     *
     * @param id The id should be one of the followings:
     * <ul>
     * <li>0 : Focused screen including SINGLE mode
     * <li>1 : PIP : BIG screen, PAP : LEFT screen
     * <li>2 : PIP : SMALL screen, PAP : RIGHT screen
     * </ul>
     * @return detail is following, null if id is invalid:
     * <ul>
     * <li>Key "type": TUNER, EXTERNAL_INPUT. null if TvPlayer is hidden.
     * <li>Key "channel_id": same as TvContract.Channels._ID. -1 if TvPlayer is hidden.
     * <li>Key "type_external_input": COMPOSITE, SCART, COMPONENT, VGA, HDMI.
      * null if TvPlayer is hidden.
     * <li>Key "input_id": input id of current channel. null if TvPlayer is hidden.
     * </ul>
     */
public android.os.Bundle getCurrentInputInfo(int id) throws android.os.RemoteException;
/**
     * Returns the path info that TIS/MW should be use.
     *
     * The path info can be {@code MAIN} or {@code SUB}.
     *
     * @return Returns {@code MAIN} if single mode.
     * Returns specified inputId is to determine whether the {@code MAIN} or {@code SUB} if PIP or PAP mode.
     */
public java.lang.String getPathInfo(java.lang.String inputId) throws android.os.RemoteException;
/**
     * Returns the current multi screen mode.
     *
     * @return Returns {@code SINGLE} if single mode.
     * Returns {@code PIP} if PAP mode.
     * Returns {@code PAP} if PAP mode.
     */
public java.lang.String getMultiScreenMode() throws android.os.RemoteException;
/**
     * Sets the current multi screen mode.
     *
     * The multi screen mode can be {@code SINGLE} or {@code PIP} or {@code PAP}.
     *
     * @param mode multi screen mode
     * @param listener {@link ITvStateSetMultiScreenModeListener} to receive the result of this method is success or not.
     * @return 0 if succeeded, else if error.
     */
public int setMultiScreenMode(java.lang.String mode, com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetMultiScreenModeListener listener) throws android.os.RemoteException;
/**
     * Returns the screen size of PAP.
     *
     * @reutrn Returns {@code null} if current screen mode is not PAP.
     * Returns {@code mainBig} if main screen is big.
     * Returns {@code mainSmall} if if main screen is small.
     */
public java.lang.String getPapScreenSize() throws android.os.RemoteException;
/**
     * Sets the screen size of PAP.
     *
     * @param screen screen of left or right of PAP. This parameter can be {@code main} or {@code sub}
     * @param size {@code +1} to become bigger the specified screen.
     * {@code -1} to become smaller the s@ecified screen.
     * @param listener {@link ITvStateSetPapScreenSizeListener} to receive the result of this method is success or not.
     */
public int setPapScreenSize(java.lang.String screen, java.lang.String size, com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPapScreenSizeListener listener) throws android.os.RemoteException;
/**
     * Returns the sub screen position of PIP.
     *
     * @reutrn Returns {@code null} if current screen mode is not PIP.
     * Returns {@code leftTop} if sub screen is in the top left.
     * Returns {@code leftBottom} if sub screen is in the bottom left.
     * Returns {@code rightTop} if sub screen is in the top right.
     * Returns {@code rightBottom} if sub screen is in the bottom right.
     */
public java.lang.String getPipSubScreenPosition() throws android.os.RemoteException;
/**
     * Sets the sub screen position of PIP.
     *
     * @param position screen position of PIP. This parameter can be {@code leftTop}, {@code leftBottom}, {@code rightTop} or {@code rightBottom}.
     * @param listener {@link ITvStateSetPipSubScreenPositionListener} to receive the result of this method is success or not.
     */
public int setPipSubScreenPosition(java.lang.String position, com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPipSubScreenPositionListener listener) throws android.os.RemoteException;
/**
     * Returns the value, which means focused screen.
     *
     * @return the following value:
     * <ul>
     * <li>1 : when PIP BIG screen / when PAP LEFT screen (include SINGLE case)
     * <li>2 : when PIP SMALL screen / when PAP RIGHT screen
     * <li>Other : Error code
     * </ul>
     */
public int getFocusScreen() throws android.os.RemoteException;
/**
     * Sets the value, which means focus screen.
     *
     * @param position This parameter can be following:
     * <ul>
     * <li>1 : when PIP BIG screen / when PAP LEFT screen
     * <li>2 : when PIP SMALL screen / when PAP RIGHT screen
     * </ul>
     * @return Returns {@code 0} if succeeded, else if error.
     */
public int setFocusScreen(int position) throws android.os.RemoteException;
/**
     * Send key event to TvPlayer.
     *
     * @param event {@link KeyEvent} to send the key event
     */
public void sendKeyEvent(android.view.KeyEvent event) throws android.os.RemoteException;
}
