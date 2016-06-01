package com.sony.dtv.tvcamera.app.cameracomponent;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sony.dtv.tvcamera.app.CameraStatusListener;
import com.sony.dtv.tvcamera.app.CheckMemoryListener;
import com.sony.dtv.tvcamera.app.USBStatusListener;
import com.sony.dtv.tvcamera.utils.SettingUtil;
import com.sony.dtv.tvcamera.utils.Utils;

public class CameraEncoder extends Handler {
    public final static int CAMERA_START_PREVIEW_720 = 0x01;
    public final static int CAMERA_START_PREVIEW_1080 = 0x02;
    public final static int CAMERA_START_RECORDER_360 = 0x03;
    public final static int CAMERA_START_RECORDER_720 = 0x04;
    public final static int CAMERA_START_RECORDER_1080 = 0x05;
    public final static int CAMERA_STOP_RECORDER = 0x06;
    public final static int CAMERA_STOP_PREVIEW = 0x07;
    public final static int CAMERA_STOP_RECORDER_PREVIEW = 0x08;
    public final static int CAMERA_NO_CAMERA = 0x09;
    public final static int CAMERA_TERMINATE = 0x0A;
    public final static int CAMERA_ERROR = 0x0B;
    public final static int USB_ERROR = 0X0C;
    public final static int CAMERA_RELEASE_PREVIEW = 0X0D;
    public final static int CAMERA_START_PREVIEW = 0X0E;
    public final static int CAMERA_NOT_COMPATIBLE = 0X0F;
    public final static int SECURITY_CAMERA_START_AUDIO_RECORDING = 0x10;
    public final static int SECURITY_CAMERA_STOP_AUDIO_RECORDING = 0x11;
    public final static int CAMERA_ONLY_START_PREVIEW_1080 = 0x12;
    public final static int CAMERA_ONLY_RELEASE_PREVIEW = 0X13;
    public final static int CAMERA_ONLY_STOP_PREVIEW = 0x14;
    private static final String TAG = "CameraEncoder";
    private CameraPreview mPreview = null;
    private CameraMediaRecorder mMediaRecorder = null;
    private SurfaceTexture mSurfaceTexture;
    private Context mContext;
    private String mMIMEType = "video/avc";

    public void setSurfaceTexture(SurfaceTexture surface) {
        if (mSurfaceTexture != surface) {
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
            }
            mSurfaceTexture = surface;
        }

        if (surface == null) {
            if (mPreview != null) {
                mPreview.notifyEndOfTextureDestroyed();
            }
        }
    }

    public CameraEncoder(Context context, String name) {
        mContext = context;
        mPreview = new CameraPreview(this, name, context);
        mMediaRecorder = new CameraMediaRecorder(this, context);
    }

    public Camera getCurrentCamera() {
        return mPreview.getCurrentCamera();
    }

    public void setCameraPictureSize(int width, int height) {
        mPreview.setCameraPictureSize(width, height);
    }

    public void setCameraPreviewCallback(Camera.PreviewCallback callback) {
        mPreview.setCameraPreviewCallback(callback);
    }

    @Override
    public void handleMessage(Message msg) {
        //TODO Auto-generated method stub
        Log.d(TAG, "msg" + msg);
        switch (msg.what) {
            case CAMERA_START_PREVIEW_720:
                if (mPreview.initCamera(mSurfaceTexture)) {
                    mPreview.start();
                    mMediaRecorder.mAudioRec.start_AudioRecording();//kasai
                    ((CameraStatusListener) mContext).notifyCameraStatus(true);
                } else {
                    Log.d(TAG, "initCamera error!");
                    return;
                }

                break;

            case CAMERA_START_PREVIEW_1080:
                if (mPreview.initCamera(mSurfaceTexture)) {
                    mPreview.start();
                    mMediaRecorder.mAudioRec.start_AudioRecording();//kasai
                    ((CameraStatusListener) mContext).notifyCameraStatus(true);
                } else {
                    Log.d(TAG, "initCamera error!");
                    return;
                }

                break;

            case CAMERA_ONLY_START_PREVIEW_1080:
                if (mPreview.initCamera(mSurfaceTexture)) {
                    mPreview.start();
                    ((CameraStatusListener) mContext).notifyCameraStatus(true);
                } else {
                    Log.d(TAG, "initCamera error!");
                    return;
                }

                break;

            case CAMERA_START_RECORDER_360:
                if (mMediaRecorder.initMediaCodec(mMIMEType, Utils.CAMERA_SIZE_360P_WIDTH, Utils.CAMERA_SIZE_360P_HEIGHT, SettingUtil.getBitRate(null))) {
                    mMediaRecorder.start();
                    ((CheckMemoryListener) mContext).notifyStartCheckMemoryCapacityTask();
                } else {
                    Log.d(TAG, "initMediaCodec error!");
                }
                break;

            case CAMERA_START_RECORDER_720:
                if (mMediaRecorder.initMediaCodec(mMIMEType, Utils.CAMERA_SIZE_720P_WIDTH, Utils.CAMERA_SIZE_720P_HEIGHT, SettingUtil.getBitRate(null))) {
                    mMediaRecorder.start();
                    ((CheckMemoryListener) mContext).notifyStartCheckMemoryCapacityTask();
                } else {
                    Log.d(TAG, "initMediaCodec error!");
                }
                break;
            case SECURITY_CAMERA_START_AUDIO_RECORDING:
                mMediaRecorder.mAudioRec.start_AudioRecording();//kasai
                break;
            case SECURITY_CAMERA_STOP_AUDIO_RECORDING:
                mMediaRecorder.mAudioRec.stop_AudioRecording();//kasai
                break;
            case CAMERA_START_RECORDER_1080:
                if (mMediaRecorder.initMediaCodec(mMIMEType, Utils.CAMERA_SIZE_1080P_WIDTH, Utils.CAMERA_SIZE_1080P_HEIGHT, SettingUtil.getBitRate(null))) {
                    mMediaRecorder.start();
                    ((CheckMemoryListener) mContext).notifyStartCheckMemoryCapacityTask();
                } else {
                    Log.d(TAG, "initMediaCodec error!");
                }
                break;

            case CAMERA_STOP_PREVIEW:
                mMediaRecorder.mAudioRec.stop_AudioRecording();//kasai
                mPreview.stop();
                break;

            case CAMERA_ONLY_STOP_PREVIEW:
                mPreview.stop();
                break;

            case CAMERA_START_PREVIEW:
                mPreview.start();
                mMediaRecorder.mAudioRec.start_AudioRecording();//kasai
                break;

            case CAMERA_RELEASE_PREVIEW:
                mMediaRecorder.mAudioRec.stop_AudioRecording();//kasai
                mPreview.release();
                break;

            case CAMERA_ONLY_RELEASE_PREVIEW:
                mPreview.release();
                break;

            case CAMERA_STOP_RECORDER:
                mMediaRecorder.stop();
                break;

            case CAMERA_STOP_RECORDER_PREVIEW:
                mMediaRecorder.stop();
                mMediaRecorder.mAudioRec.stop_AudioRecording();//kasai
                mPreview.stop();
                break;

            case CAMERA_NO_CAMERA:
                ((CameraStatusListener) mContext).notifyCameraStatus(false);
                Utils.CameraSupportError(mContext);
                mMediaRecorder.mAudioRec.stop_AudioRecording();//kasai
                mPreview.stop();
                break;

            case CAMERA_TERMINATE:
                ((CameraStatusListener) mContext).notifyCameraStatus(false);
                mMediaRecorder.mAudioRec.stop_AudioRecording();//kasai
                mPreview.stop();
                break;

            case CAMERA_ERROR:
                ((CameraStatusListener) mContext).notifyCameraStatus(false);
                Utils.exitTVCamera(mContext);
                mMediaRecorder.mAudioRec.stop_AudioRecording();//kasai
                mPreview.stop();
                break;

            case USB_ERROR:
                ((USBStatusListener) mContext).notifyUSBStatus(false);
                Utils.USBError(mContext);
                break;

            case CAMERA_NOT_COMPATIBLE:
                ((CameraStatusListener) mContext).notifyCameraStatus(false);
                Utils.ShowCameraSupportErrorDialog(mContext, Utils.CAMERA_NOT_COMPATIBLE);
                mMediaRecorder.mAudioRec.stop_AudioRecording();//kasai
                mPreview.stop();
                break;

            default:
                break;
        }
    }
}
