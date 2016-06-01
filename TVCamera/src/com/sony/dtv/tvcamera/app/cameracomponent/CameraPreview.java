package com.sony.dtv.tvcamera.app.cameracomponent;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;

import com.sony.dtv.tvcamera.utils.Utils;

public class CameraPreview {

    private static final String TAG = "CameraPreview";

    private Camera mCamera;
    private String mHostName;
    private Handler mHandler;
    private Context mContext;

    private int mPreviewWidth = Utils.CAMERA_SIZE_720P_WIDTH;
    private int mPreviewHeight = Utils.CAMERA_SIZE_720P_HEIGHT;
    private int mPictureWidth = Utils.CAMERA_SIZE_1080P_WIDTH;
    private int mPictureHeight = Utils.CAMERA_SIZE_1080P_HEIGHT;
    private Camera.PreviewCallback mPreviewCallback;

    private boolean cameraOpened = false;

    public CameraPreview(Handler handler, String name, Context context) {
        mHostName = name;
        mHandler = handler;
        mContext = context;
    }

    public boolean initCamera(SurfaceTexture surface) {

        if (mCamera != null) return true;

        boolean isFirstLaunch = Utils.isFirstLaunch();
        Log.d(TAG, "isFirstLaunch = " + isFirstLaunch);

        try {
            boolean isCameraInsert = Utils.checkCameraInsert(mContext);
            if (!isCameraInsert) {
                Log.d(TAG, "Camera isn't inserted");
                if (isFirstLaunch && !cameraOpened) {
                    mHandler.sendEmptyMessage(CameraEncoder.CAMERA_NO_CAMERA);
                } else {
                    Utils.sendSonyCameraNotify(mContext, CameraEncoder.CAMERA_TERMINATE);
                    mHandler.sendEmptyMessage(CameraEncoder.CAMERA_TERMINATE);
                }
                return false;
            }

            Log.d(TAG, mHostName + ", mCamera open()");
            try {
                mCamera = Camera.open(0);
            } catch (Exception e) {
                Log.d(TAG, "mCamera open() failed");
                if (isFirstLaunch && !cameraOpened) {
                    mHandler.sendEmptyMessage(CameraEncoder.CAMERA_NO_CAMERA);
                } else {
                    Utils.sendSonyCameraNotify(mContext, CameraEncoder.CAMERA_TERMINATE);
                    mHandler.sendEmptyMessage(CameraEncoder.CAMERA_TERMINATE);
                }
                e.printStackTrace();
                return false;
            }
            Camera.Parameters parameters = mCamera.getParameters();

            if (isFirstLaunch && !Utils.checkCameraType(mContext)) {
                Log.d(TAG, "mCamera checkCameraType() failed");
                mHandler.sendEmptyMessage(CameraEncoder.CAMERA_NOT_COMPATIBLE);
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
                return false;
            }

            parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
            parameters.setPictureSize(mPictureWidth, mPictureHeight);
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(0);
            mCamera.setPreviewTexture(surface);
            if (mPreviewCallback != null) {
                mCamera.setPreviewCallback(mPreviewCallback);
            }
        } catch (Exception e) {
            Log.d(TAG, "mCamera setParameters() failed");
            if (isFirstLaunch)
                mHandler.sendEmptyMessage(CameraEncoder.CAMERA_NOT_COMPATIBLE);
            e.printStackTrace();
            return false;
        }
        cameraOpened = true;
        return true;
    }

    public void start() {
        if (mCamera != null) {
            Log.d(TAG, mHostName + ", mCamera startPreview()");
            mCamera.startPreview();
        }
    }

    public void stop() {
        if (mCamera != null) {
            Log.d(TAG, mHostName + ", mCamera stopPreview()");
            mCamera.stopPreview();
        }
    }

    public void release() {
        if (mCamera != null) {
            Log.d(TAG, mHostName + ", mCamera release()");
            if (mPreviewCallback != null) {
                mCamera.setPreviewCallback(null);
            }
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void notifyEndOfTextureDestroyed() {
        release();
    }

    protected Camera getCurrentCamera() {
        return mCamera;
    }

    protected void setCameraPictureSize(int width, int height) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureSize(width, height);
            mCamera.setParameters(parameters);
        } else {
            mPictureWidth = width;
            mPictureHeight = height;
        }
    }

    protected void setCameraPreviewCallback(Camera.PreviewCallback callback) {
        mPreviewCallback = callback;
    }
}
