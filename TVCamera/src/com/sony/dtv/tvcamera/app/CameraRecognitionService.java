package com.sony.dtv.tvcamera.app;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sony.dtv.camerarecognition.CameraRecognition;
import com.sony.dtv.camerarecognition.CameraRecognitionError;
import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.photosetting.PhotoSettingConstants;
import com.sony.dtv.tvcamera.extension.TvAppExtension;
import com.sony.dtv.tvcamera.utils.Utils;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

public class CameraRecognitionService extends Service implements USBStatusListener {

    private static final String TAG = "RecognitionService";

    private Bitmap mPhotoBitmap = null;
    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;
    private boolean mIsCapturing = false;

    private boolean mIsCameraRecognitionOpened = false;

    private final Handler mHandler = new Handler();

    Runnable mStartCapturingTask = new Runnable() {
        @Override
        public void run() {
            startCapturing();
        }
    };

    LinkedList<String> mUSBList;
    private HashMap<String, String> mUSBPath;
    String USB_DEVICE_PATH = "/storage/sda1";
    String USB_DEVICE_SDCARD = "sdcard";
    String USB_DEVICE_FILTER = "/storage/sd";
    private USBDeviceBroadcastReceiver mUSBDeviceBroadcastReceiver = null;
    private String mUSBCurrentPath;

    private class USBDeviceBroadcastReceiver extends BroadcastReceiver {
        boolean bIsNeedStorageCheck = false;

        @Override
        public void onReceive(final Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            Log.d(TAG, "action..." + action);
            String action_path = intent.getData().getPath();
            Log.d(TAG, "action_path..." + action_path);
            String preUSBCurrentPath = mUSBCurrentPath;
            getUsbPath(USB_DEVICE_PATH);
            if (!TextUtils.equals(mUSBCurrentPath, preUSBCurrentPath)) {
                bIsNeedStorageCheck = true;
            } else {
                bIsNeedStorageCheck = false;
            }
            if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_EJECT)) {
                if (bIsNeedStorageCheck == true) {
                    stopCapturing();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!Utils.isErrorShowed()) {
                                Utils.setIsErrorShowed(true);
                                Log.d(TAG, "USB storage device has been disconnected.");
                                Toast.makeText(getApplicationContext(), R.string.u_disk_pulled_out,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    stopSelf();
                }

            } else if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_MOUNTED)) {
                if (bIsNeedStorageCheck) {
                    checkStorage();
                }
            }
        }
    }

    private CameraRecognition mCameraRecognition = null;
    private MyRecognitionCallback mMyRecognitionCallback = new MyRecognitionCallback();

    class MyRecognitionCallback implements CameraRecognition.RecognitionCallback {
        @Override
        public void onRecognize(int captureType, byte[] image, JSONObject result) {
            int imageLength = image.length;
            Log.d(TAG, "onRecognize." +
                    "captureType=" + String.format("%#08x", captureType) +
                    " image.length=" + imageLength);
            Log.d(TAG, "result=" + result.toString());
            if (captureType == CameraRecognition.CAPTURE_TYPE_SMILE) {
                Log.d(TAG, "Received CAPTURE_TYPE_SMILE image");
                saveImage(image);
            } else if ((captureType == CameraRecognition.CAPTURE_TYPE_ONE_SHOT)
                    || (captureType == CameraRecognition.CAPTURE_TYPE_ONE_SHOT_WITHOUT_ANALYSIS)) {
                Log.d(TAG, "Received CAPTURE_TYPE_ONE_SHOT image");
            } else {
                Log.w(TAG, "unknown captureType=" + captureType);
            }
        }

        private void saveImage(byte[] image) {
            if (Utils.getMemoryCapacity(mUSBCurrentPath) <= 3) {
                setUSBStatus(false);
            }

            Bitmap photoBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            Utils.saveImage(photoBitmap, CameraRecognition.CAPTURE_TYPE_SMILE, getApplicationContext(), true);

            if (null != mPhotoBitmap) {
                mPhotoBitmap.recycle();
                mPhotoBitmap = null;
            }
            checkMemoryCapacity();
        }

        @Override
        public void onOpen(int result) {
            Log.d(TAG, "onOpen. result=" + result);
            if (!mIsCameraRecognitionOpened) {
                mIsCameraRecognitionOpened = true;
                if (TVCameraApp.getCameraRecognitionService() == null) {
                    if (null != mCameraRecognition) {
                        int ret = mCameraRecognition.closeRecognition();
                        Log.d(TAG, "call closeRecognition. ret=" + ret);
                        Utils.handleCameraRecognitionError(ret, getApplicationContext(), true, true);
                        mCameraRecognition = null;
                    }
                    return;
                }
            }

            if (CameraRecognitionError.RESULT_ERR_CAMERA_DEVICE_UNAVAILABLE == result) {
                Utils.CameraSupportError(getApplicationContext());
                stopSelf();
                return;
            }

            if (!Utils.checkCameraType(getApplicationContext())) {
                Utils.ShowCameraSupportErrorDialog(getApplicationContext(), Utils.CAMERA_NOT_COMPATIBLE);
                stopSelf();
                return;
            }

            getUsbPath(null);
            checkStorage();

            if (mUSBList.size() == 0) {
                if (!Utils.isErrorShowed()) {
                    Utils.setIsErrorShowed(true);
                    Log.d(TAG, "no_usb_smile_shutter");
                    Toast.makeText(getApplicationContext(), R.string.no_usb_smile_shutter, Toast.LENGTH_LONG).show();
                    stopSelf();
                    return;
                }
            }

            // Init.
            Utils.handleCameraRecognitionError(result, getApplicationContext());
            if (CameraRecognitionError.RESULT_OK != result) {
                if (null != mCameraRecognition) {
                    result = mCameraRecognition.closeRecognition();
                    Log.d(TAG, "call closeRecognition. ret=" + result);
                    Utils.handleCameraRecognitionError(result, getApplicationContext());
                    mCameraRecognition = null;
                }
                return;
            }

            int captureWidth = 0;
            int captureHeight = 0;
            SharedPreferences smileShutter = getSharedPreferences(PhotoSettingConstants.SP_NAME, Context.MODE_PRIVATE);
            String PictureSizeKey = smileShutter.getString(PhotoSettingConstants.PictureSizeKey, getString(R.string.picture_size_defValue));

            if (PictureSizeKey.equals("1920 x 1080")) {
                captureWidth = Utils.CAMERA_SIZE_1080P_WIDTH;
                captureHeight = Utils.CAMERA_SIZE_1080P_HEIGHT;
            } else if (PictureSizeKey.equals("1280 x 720")) {
                captureWidth = Utils.CAMERA_SIZE_720P_WIDTH;
                captureHeight = Utils.CAMERA_SIZE_720P_HEIGHT;
            } else {
                captureWidth = Utils.CAMERA_SIZE_360P_WIDTH;
                captureHeight = Utils.CAMERA_SIZE_360P_HEIGHT;
            }

            Log.d("Utils", "captureWidth = " + captureWidth);
            Log.d("Utils", "captureHeight = " + captureHeight);

            if (null != mCameraRecognition) {
                int ret = mCameraRecognition.initRecognition(
                        captureWidth, // resolutionWidth.
                        captureHeight, // resolutionHeight.
                        CameraRecognition.CAPTURE_TYPE_SMILE // captureType
                );
                Log.d(TAG, "call initRecognition. ret=" + ret);
                Utils.handleCameraRecognitionError(ret, getApplicationContext());
                if (CameraRecognitionError.RESULT_OK != result) {
                    return;
                }
            } else {
                return;
            }

            mHandler.removeCallbacks(mStartCapturingTask);
            mHandler.post(mStartCapturingTask);
        }

        @Override
        public void onError(int errorCode) {
            Log.w(TAG, "onError. ret=" + errorCode);
            Utils.handleCameraRecognitionError(errorCode, getApplicationContext(), true, true);
        }
    }

    private void checkStorage() {
        File USBStorage = null;
        if (mUSBCurrentPath != null) {
            USBStorage = new File(mUSBCurrentPath);
        }
        if (mUSBList.size() == 0 || USBStorage == null) {
            setUSBStatus(false);
        } else if ((USBStorage.canWrite() == false) || (!Utils.checkUsbWritablity(mUSBCurrentPath))) {
            setUSBStatus(false);
            if (!Utils.isErrorShowed()) {
                Utils.setIsErrorShowed(true);
                Log.d(TAG, "USB read_only!");
                Toast.makeText(getApplicationContext(), R.string.read_only, Toast.LENGTH_SHORT).show();
            }
        } else if (Utils.getMemoryCapacity(mUSBCurrentPath) <= 3) {
            setUSBStatus(false);
            if (!Utils.isErrorShowed()) {
                Utils.setIsErrorShowed(true);
                Log.d(TAG, "memory_little_tip_smile_shutter");
                Toast.makeText(getApplicationContext(), R.string.memory_little_tip_smile_shutter, Toast.LENGTH_SHORT).show();
            }
        } else {
            setUSBStatus(true);
        }
    }

    public void setUSBStatus(boolean isUSBAvailable) {
        if (!isUSBAvailable) {
            stopSelf();
        }
    }

    /* get mounted storage space */
    private void getUsbPath(String default_usb) {
        int preUSBListSize = mUSBList.size();
        //mUSBList.clear();
        if (default_usb == null || default_usb.equalsIgnoreCase(" ")) {
            default_usb = USB_DEVICE_SDCARD;
        }

        mUSBList = Utils.getUSBPathList(mUSBList, getApplicationContext());

        mSettings = getSharedPreferences("usb", 0);
        mEditor = mSettings.edit();
        mEditor.putInt("usb_count", mUSBList.size());
        mEditor.commit();

        if (mUSBList.size() > 0) {
            if (mUSBList.indexOf(default_usb) >= 0) {
                USB_DEVICE_PATH = default_usb;
            } else {
                USB_DEVICE_PATH = mUSBList.get(0);
            }

            mUSBPath.clear();
            for (int i = 0; i < mUSBList.size(); i++) {
                mUSBPath.put("USB" + (i + 1), mUSBList.get(i));
            }

            mSettings = getSharedPreferences(PhotoSettingConstants.SP_NAME, 0);
            String keyDestination = mSettings.getString(PhotoSettingConstants.DestinationKey, "");

            int curUSBListSize = mUSBList.size();
            if (TextUtils.equals(keyDestination, "") || curUSBListSize == 1 || (preUSBListSize - curUSBListSize) == 1) {
                keyDestination = "USB1";
                mEditor = mSettings.edit();
                mEditor.putString(PhotoSettingConstants.DestinationKey, keyDestination);
                mEditor.putString("destination_title", keyDestination);
                mEditor.commit();
            }
            SharedPreferences USBPath = getSharedPreferences("usb", 0);
            mEditor = USBPath.edit();
            mEditor.putString("old_usb_current_photo_path", mUSBCurrentPath);
            mEditor.commit();
            mUSBCurrentPath = mUSBPath.get(keyDestination);
            mEditor.putString("usb_current_photo_path", mUSBCurrentPath);
            mEditor.commit();
        } else {
            SharedPreferences USBPath = getSharedPreferences("usb", 0);
            USBPath.edit().clear().commit();
            mUSBCurrentPath = null;
        }
        Log.d(TAG, "usbRoot " + USB_DEVICE_PATH);
    }

    private void startCapturing() {
        if (null != mCameraRecognition) {
            int ret = mCameraRecognition.startRecognition();
            Log.d(TAG, "call startRecognition ret=" + ret);
            Utils.handleCameraRecognitionError(ret, getApplicationContext());

            if (null != mPhotoBitmap) {
                mPhotoBitmap.recycle();
                mPhotoBitmap = null;
            }
        }
    }

    public void stopCapturing() {
        // Stop.
        if (null != mCameraRecognition) {
            int ret = mCameraRecognition.stopRecognition();
            Log.d(TAG, "call stopRecognition ret=" + ret);
            Utils.handleCameraRecognitionError(ret, getApplicationContext(), true, true);
        }
    }

    @Override
    public void notifyUSBStatus(boolean isUSBAvailable) {
        if (mIsCapturing && isUSBAvailable == false) {
            mIsCapturing = !mIsCapturing;
            setUSBStatus(false);
        }
    }

    private void checkMemoryCapacity() {
        if (Utils.getMemoryCapacity(mUSBCurrentPath) <= 3) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!Utils.isErrorShowed()) {
                        Utils.setIsErrorShowed(true);
                        Log.d(TAG, "memory_little_smile_shutter");
                        Toast.makeText(getApplicationContext(), R.string.memory_little_smile_shutter,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
        // TODO Auto-generated method stub
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        mIsCameraRecognitionOpened = false;

        mCameraRecognition = new CameraRecognition();
        // Register callback.
        if (null != mCameraRecognition) {
            int ret = mCameraRecognition.registerRecognitionCallback(mMyRecognitionCallback);
            Log.d(TAG, "call registerRecognitionCallback. ret=" + ret);
            Utils.handleCameraRecognitionError(ret, getApplicationContext());
            if (CameraRecognitionError.RESULT_OK != ret) {
                return 0;
            }
        }

        if (null != mCameraRecognition) {
            // Open.
            mIsCameraRecognitionOpened = false;
            int ret = mCameraRecognition.openRecognition(getApplicationContext());
            Log.d(TAG, "call openRecognition. ret=" + ret);
            Utils.handleCameraRecognitionError(ret, getApplicationContext());
            if (CameraRecognitionError.RESULT_OK != ret) {
                return 0;
            }
        }

        Log.i(TAG, "onStartCommand end");
        return 0;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        Utils.setIsErrorShowed(false);
        TVCameraApp.setCameraRecognitionService(this);
        TvAppExtension tvAppExtension = TVCameraApp.getTvAppExtension();
        if (null != tvAppExtension) {
            tvAppExtension.notifyServiceStatus();
        }

        mUSBList = new LinkedList<>();
        mUSBPath = new HashMap<>();

        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        mUSBDeviceBroadcastReceiver = new USBDeviceBroadcastReceiver();
        registerReceiver(mUSBDeviceBroadcastReceiver, filter);
    }

    private void closeCameraRecognition() {
        if (null != mCameraRecognition) {
            int ret = mCameraRecognition.stopRecognition();
            Log.d(TAG, "call stopRecognition ret=" + ret);
            Utils.handleCameraRecognitionError(ret, getApplicationContext(), true, true);
        }

        if (null != mCameraRecognition) {
            int ret = mCameraRecognition.closeRecognition();
            Log.d(TAG, "call closeRecognition. ret=" + ret);
            Utils.handleCameraRecognitionError(ret, getApplicationContext(), true, true);
            mCameraRecognition = null;
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();

        if (mIsCameraRecognitionOpened) {
            closeCameraRecognition();
        }

        if (null != mUSBDeviceBroadcastReceiver) {
            unregisterReceiver(mUSBDeviceBroadcastReceiver);
            mUSBDeviceBroadcastReceiver = null;
        }

        TVCameraApp.setCameraRecognitionService(null);
        TvAppExtension tvAppExtension = TVCameraApp.getTvAppExtension();
        if (null != tvAppExtension) {
            tvAppExtension.notifyServiceStatus();
        }
    }
}