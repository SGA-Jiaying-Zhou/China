package com.sony.dtv.tvcamera.app;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.sony.dtv.camerarecognition.CameraRecognition;
import com.sony.dtv.camerarecognition.CameraRecognitionError;
import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.cameracomponent.CameraEncoder;
import com.sony.dtv.tvcamera.app.receiver.SonyCameraDetachedReceiver;
import com.sony.dtv.tvcamera.extension.TvAppExtension;
import com.sony.dtv.tvcamera.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

public class SecurityCameraService extends Service implements USBStatusListener, CheckMemoryListener {

    private static final String TAG = "SecurityCameraService";

    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;
    private boolean mIsCapturing = false;

    private boolean mIsCameraRecognitionOpened = false;
    private PowerManager.WakeLock wakeLock;
    private boolean mIsRecording = false;
    private CameraEncoder mCameraEncoder = null;
    private SonyCameraDetachedReceiver mSonyCameraDetachedReceiver;

    private int mSkipCount = 0;
    private static final int SKIP_NUM = 10;
    private static final int STOP_RECORDING_TIME = 31000;
    private static final int THREE_SECOND = 180000;

    private final Handler mHandler = new Handler();

    Runnable mInitCameraRecognitionTask = new Runnable() {
        @Override
        public void run() {
            mIsCameraRecognitionOpened = false;

            mCameraRecognition = new CameraRecognition();
            // Register callback.
            if (null != mCameraRecognition) {
                int ret = mCameraRecognition.registerRecognitionCallback(mMyRecognitionCallback);
                Log.d(TAG, "call registerRecognitionCallback. ret=" + ret);
            /*Utils.handleCameraRecognitionError(ret, getApplicationContext());*/
                setCameraRecognitionStringID(ret);
                if (CameraRecognitionError.RESULT_OK != ret) {
                    return;
                }
            }

            if (null != mCameraRecognition) {
                // Open.
                mIsCameraRecognitionOpened = false;
                int ret = mCameraRecognition.openRecognition(getApplicationContext());
                Log.d(TAG, "call openRecognition. ret=" + ret);
            /*Utils.handleCameraRecognitionError(ret, getApplicationContext());*/
                setCameraRecognitionStringID(ret);
                if (CameraRecognitionError.RESULT_OK != ret) {
                    return;
                }
            }
        }
    };

    Runnable mStartCapturingTask = new Runnable() {
        @Override
        public void run() {
            startCapturing();
        }
    };

/*    Runnable mStopCapturingTask = new Runnable() {
        @Override
        public void run() {
            stopCapturing();
        }
    };*/

    Runnable mStartRecordingTask = new Runnable() {
        @Override
        public void run() {
            startRecording();
            mHandler.postDelayed(mStopRecorderTask, STOP_RECORDING_TIME);
        }
    };

    Runnable mStopRecorderTask = new Runnable() {
        @Override
        public void run() {
            stopRecorder();
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
                    stopRecorder();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!Utils.isErrorShowed()) {
                                Utils.setIsErrorShowed(true);
                                Log.d(TAG, "security_camera_no_usb_toast");
                                /*Toast.makeText(getApplicationContext(), R.string.security_camera_no_usb_toast,
                                        Toast.LENGTH_SHORT).show();*/
                                TVCameraApp.setSecurityCameraStringID(R.string.security_camera_no_usb_toast);
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
            int imageLength = 0;
            if (image != null) {
                imageLength = image.length;
            }
            Log.d(TAG, "onRecognize." +
                    "captureType=" + String.format("0x%08x", captureType) +
                    " image.length=" + imageLength);
            Log.d(TAG, "result=" + result.toString());
            if (captureType == CameraRecognition.CAPTURE_TYPE_MOVING) {
                if (mSkipCount < SKIP_NUM) {
                    mSkipCount++;
                    return;
                }

                Log.d(TAG, "Received CAPTURE_TYPE_MOVING image");
                final JSONObject jsonResult = result;
                Log.d(TAG, "jsonResult = " + jsonResult);
                int num = extractNum(jsonResult);
                Log.d(TAG, "num = " + num);

                if (num != 0) {
                    mHandler.removeCallbacks(mStartCapturingTask);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            closeCameraRecognition();
                        }
                    });
                    mHandler.post(mStartRecordingTask);
                }
            } else if ((captureType == CameraRecognition.CAPTURE_TYPE_ONE_SHOT)
                    || (captureType == CameraRecognition.CAPTURE_TYPE_SMILE)
                    || (captureType == CameraRecognition.CAPTURE_TYPE_ONE_SHOT_WITHOUT_ANALYSIS)) {
            } else {
                Log.w(TAG, "unknown captureType=" + captureType);
            }
        }

        @Override
        public void onOpen(int result) {
            Log.d(TAG, "onOpen. result=" + result);
            if (!mIsCameraRecognitionOpened) {
                mIsCameraRecognitionOpened = true;
                if (TVCameraApp.getSecurityCameraService() == null) {
                    if (null != mCameraRecognition) {
                        int ret = mCameraRecognition.closeRecognition();
                        Log.d(TAG, "call closeRecognition. ret=" + ret);
                        /*Utils.handleCameraRecognitionError(ret, getApplicationContext(), true, true);*/
                        setCameraRecognitionStringID(ret);
                        mCameraRecognition = null;
                    }
                    return;
                }
            }

            if ((CameraRecognitionError.RESULT_ERR_CAMERA_DEVICE_UNAVAILABLE == result)
                    || (!Utils.checkCameraType(getApplicationContext()))) {
                /*Utils.CameraSupportError(getApplicationContext());*/
                setCameraRecognitionStringID(result);
                stopSelf();
                return;
            }

            getUsbPath(null);
            checkStorage();

            if (mUSBList.size() == 0) {
                if (!Utils.isErrorShowed()) {
                    Utils.setIsErrorShowed(true);
                    Log.d(TAG, "security_camera_no_usb_toast");
                    /*Toast.makeText(getApplicationContext(), R.string.security_camera_no_usb_toast, Toast.LENGTH_LONG).show();*/
                    TVCameraApp.setSecurityCameraStringID(R.string.security_camera_no_usb_toast);
                    stopSelf();
                    return;
                }
            }

            // Init.
            /*Utils.handleCameraRecognitionError(result, getApplicationContext());*/
            setCameraRecognitionStringID(result);
            if (CameraRecognitionError.RESULT_OK != result) {
                if (null != mCameraRecognition) {
                    result = mCameraRecognition.closeRecognition();
                    Log.d(TAG, "call closeRecognition. ret=" + result);
                    /*Utils.handleCameraRecognitionError(result, getApplicationContext());*/
                    setCameraRecognitionStringID(result);
                    mCameraRecognition = null;
                }
                return;
            }

            if (null != mCameraRecognition) {
                int ret = mCameraRecognition.initRecognition(
                        Utils.CAMERA_SIZE_720P_WIDTH, // resolutionWidth.
                        Utils.CAMERA_SIZE_720P_HEIGHT, // resolutionHeight.
                        CameraRecognition.CAPTURE_TYPE_MOVING // captureType
                );
                Log.d(TAG, "call initRecognition. ret=" + ret);
                /*Utils.handleCameraRecognitionError(ret, getApplicationContext());*/
                setCameraRecognitionStringID(result);
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
            /*Utils.handleCameraRecognitionError(errorCode, getApplicationContext(), true, true);*/
            setCameraRecognitionStringID(errorCode);
            if (CameraRecognitionError.RESULT_OK != errorCode) {
                mHandler.removeCallbacks(mStartCapturingTask);
                stopSelf();
            }
        }
    }

    Runnable mCheckMemoryCapacityTask = new Runnable() {
        @Override
        public void run() {
            checkMemoryCapacity();
        }
    };

    public void startNewLoop() {
        if (null != mInitCameraRecognitionTask) {
            mHandler.removeCallbacks(mInitCameraRecognitionTask);
            mHandler.post(mInitCameraRecognitionTask);
        }
    }

    private void startRecording() {

        if (!mIsRecording) {
            mCameraEncoder.sendEmptyMessage(CameraEncoder.SECURITY_CAMERA_START_AUDIO_RECORDING);

            int CameraEncoderMode = 0;
            SharedPreferences sSettings = getSharedPreferences("setting", 0);
            String videoSize = sSettings.getString("video_size_key", "");

            if (Utils.VIDEO_SIZE_360.equals(videoSize)) {
                CameraEncoderMode = CameraEncoder.CAMERA_START_RECORDER_360;
            } else if (Utils.VIDEO_SIZE_720.equals(videoSize)) {
                CameraEncoderMode = CameraEncoder.CAMERA_START_RECORDER_720;
            } else if (Utils.VIDEO_SIZE_1080.equals(videoSize)) {
                CameraEncoderMode = CameraEncoder.CAMERA_START_RECORDER_1080;
            } else {
                CameraEncoderMode = CameraEncoder.CAMERA_START_RECORDER_1080;
            }

            Message msg = mCameraEncoder.obtainMessage(CameraEncoderMode);
            mCameraEncoder.sendMessage(msg);
            mIsRecording = true;
        } else {
            /*
             * stop
			 */
            stopRecorder();
        }

    }

    private void setCameraRecognitionStringID(int ret) {
        if (CameraRecognitionError.RESULT_OK == ret) {
            return;
        }

        if (!Utils.isErrorShowed()) {
            Utils.setIsErrorShowed(true);
            if (CameraRecognitionError.RESULT_ERR_CAMERA_DEVICE_UNAVAILABLE == ret) {
                TVCameraApp.setSecurityCameraStringID(R.string.security_camera_no_camera_toast);
            } else {
                TVCameraApp.setSecurityCameraStringID(R.string.security_camera_error_toast);
            }
        }
    }

    public void stopRecorder() {
        if (mIsRecording) {
            mIsRecording = false;
            Message msg = mCameraEncoder.obtainMessage(CameraEncoder.CAMERA_STOP_RECORDER);
            mCameraEncoder.sendMessage(msg);
            mCameraEncoder.sendEmptyMessage(CameraEncoder.SECURITY_CAMERA_STOP_AUDIO_RECORDING);
            mHandler.removeCallbacks(mCheckMemoryCapacityTask);
        }

    }

    public int extractNum(JSONObject result) {
        int num = 0;
        try {
            String tempStr = result.optString("MovingResult");
            JSONObject tempJsonObject = null;
            if (!TextUtils.isEmpty(tempStr)) {
                tempJsonObject = new JSONObject(tempStr);
                num = tempJsonObject.optInt("num");
                return num;
            }
        } catch (JSONException e) {
            Log.w(TAG, e);
            return num;
        }
        return num;
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
                Log.d(TAG, "security_camera_usb_read_only_toast");
                /*Toast.makeText(getApplicationContext(), R.string.security_camera_usb_read_only_toast, Toast.LENGTH_SHORT).show();*/
                TVCameraApp.setSecurityCameraStringID(R.string.security_camera_usb_read_only_toast);
            }
        } else if (Utils.getMemoryCapacity(mUSBCurrentPath) <= Utils.SECURITY_CAMERA_MIN_USB_SIZE) {
            setUSBStatus(false);
            if (!Utils.isErrorShowed()) {
                Utils.setIsErrorShowed(true);
                Log.d(TAG, "security_camera_memory_little_toast");
                /*Toast.makeText(getApplicationContext(), R.string.security_camera_memory_little_toast, Toast.LENGTH_SHORT).show();*/
                TVCameraApp.setSecurityCameraStringID(R.string.security_camera_memory_little_toast);
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
    public void getUsbPath(String default_usb) {
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

            mSettings = getSharedPreferences("setting", 0);
            String keyDestination = mSettings.getString("destination_key", "");

            int curUSBListSize = mUSBList.size();
            if (TextUtils.equals(keyDestination, "") || curUSBListSize == 1 || (preUSBListSize - curUSBListSize) == 1) {
                keyDestination = "USB1";
                mEditor = mSettings.edit();
                mEditor.putString("destination_key", keyDestination);
                mEditor.putString("destination_title", keyDestination);
                mEditor.commit();
            }
            SharedPreferences USBPath = getSharedPreferences("usb", 0);
            mEditor = USBPath.edit();
            mEditor.putString("old_usb_current_path", mUSBCurrentPath);
            mEditor.commit();
            mUSBCurrentPath = mUSBPath.get(keyDestination);
            mEditor.putString("usb_current_path", mUSBCurrentPath);
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
            mIsCapturing = true;
            Log.d(TAG, "call startRecognition ret=" + ret);
            /*Utils.handleCameraRecognitionError(ret, getApplicationContext());*/
            setCameraRecognitionStringID(ret);
            if (CameraRecognitionError.RESULT_OK != ret) {
                stopSelf();
            }
        }
    }

    public void stopCapturing() {
        // Stop.
        if ((null != mCameraRecognition) && (mIsCapturing)) {
            mIsCapturing = false;
            int ret = mCameraRecognition.stopRecognition();
            Log.d(TAG, "call stopRecognition ret=" + ret);
            /*Utils.handleCameraRecognitionError(ret, getApplicationContext(), true, true);*/
            setCameraRecognitionStringID(ret);
            if (CameraRecognitionError.RESULT_OK != ret) {
                stopSelf();
            }
        }
    }

    @Override
    public void notifyUSBStatus(boolean isUSBAvailable) {
        if (mIsRecording && isUSBAvailable == false) {
            setUSBStatus(false);
        }
    }

    private void checkMemoryCapacity() {
        if (Utils.getMemoryCapacity(mUSBCurrentPath) <= Utils.SECURITY_CAMERA_MIN_USB_SIZE) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!Utils.isErrorShowed()) {
                        Utils.setIsErrorShowed(true);
                        Log.d(TAG, "security_camera_memory_little_toast");
                        /*Toast.makeText(getApplicationContext(), R.string.security_camera_memory_little_toast,
                                Toast.LENGTH_SHORT).show();*/
                        TVCameraApp.setSecurityCameraStringID(R.string.security_camera_memory_little_toast);
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
        boolean isCameraInsert = Utils.checkCameraInsert(getApplicationContext());
        if (!isCameraInsert) {
            Log.d(TAG, "Camera isn't inserted");
            Utils.ShowCameraSupportErrorDialog(getApplicationContext(), Utils.getCameraSupportType(), true);
            stopSelf();
            return 0;
        }

        Camera camera;
        try {
            camera = Camera.open(0);
        } catch (Exception e) {
            Log.d(TAG, "mCamera open() failed");
            Utils.ShowCameraSupportErrorDialog(getApplicationContext(), Utils.getCameraSupportType(), true);
            stopSelf();
            return 0;
        }

        if (null != camera) {
            camera.release();
            camera = null;
        }

        if (!Utils.checkCameraType(getApplicationContext())) {
            Log.d(TAG, "mCamera checkCameraType() failed");
            Utils.ShowCameraSupportErrorDialog(getApplicationContext(), Utils.CAMERA_NOT_COMPATIBLE, true);
            stopSelf();
            return 0;
        }

        if (null != mInitCameraRecognitionTask) {
            mHandler.removeCallbacks(mInitCameraRecognitionTask);
            mHandler.postDelayed(mInitCameraRecognitionTask, THREE_SECOND);
        }

        Log.d(TAG, "onStartCommand end");
        return 0;
    }

    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass()
                    .getCanonicalName());
            if (null != wakeLock) {
                Log.i(TAG, "call acquireWakeLock");
                wakeLock.acquire();
            }
        }
    }

    private void releaseWakeLock() {
        if (null != wakeLock && wakeLock.isHeld()) {
            Log.i(TAG, "call releaseWakeLock");
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        acquireWakeLock();
        mSkipCount = 0;
        Utils.setIsErrorShowed(false);
        TVCameraApp.setSecurityCameraService(this);
        SharedPreferences sp = getSharedPreferences("securityCamera", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isNeedStartSecurityCamera", false);
        editor.commit();
        TvAppExtension tvAppExtension = TVCameraApp.getTvAppExtension();
        if (null != tvAppExtension) {
            tvAppExtension.notifyServiceStatus();
        }

        String filePath = getFilesDir().getAbsolutePath() + "/" + Utils.SECURITY_RECORD_PATH;
        File file = new File(filePath);
        deleteFiles(file);

        mIsRecording = false;
        mCameraEncoder = new CameraEncoder(this, TAG);

        IntentFilter usbChangeFilter = new IntentFilter();
        usbChangeFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mSonyCameraDetachedReceiver = new SonyCameraDetachedReceiver(this);
        registerReceiver(mSonyCameraDetachedReceiver, usbChangeFilter);

        mUSBList = new LinkedList<>();
        mUSBPath = new HashMap<>();

        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        mUSBDeviceBroadcastReceiver = new USBDeviceBroadcastReceiver();
        registerReceiver(mUSBDeviceBroadcastReceiver, filter);
    }

    private void closeCameraRecognition() {
        if ((null != mCameraRecognition) && (mIsCapturing)) {
            mIsCapturing = false;
            int ret = mCameraRecognition.stopRecognition();
            Log.d(TAG, "call stopRecognition ret=" + ret);
            /*Utils.handleCameraRecognitionError(ret, getApplicationContext(), true, true);*/
            setCameraRecognitionStringID(ret);
        }

        if (null != mCameraRecognition) {
            int ret = mCameraRecognition.closeRecognition();
            Log.d(TAG, "call closeRecognition. ret=" + ret);
            /*Utils.handleCameraRecognitionError(ret, getApplicationContext(), true, true);*/
            setCameraRecognitionStringID(ret);
            mCameraRecognition = null;
        }
    }

    private void removeCallbackTask() {
        mHandler.removeCallbacks(mStartCapturingTask);
        mHandler.removeCallbacks(mInitCameraRecognitionTask);
        mInitCameraRecognitionTask = null;
        /*mHandler.removeCallbacks(mStopCapturingTask);*/
        mHandler.removeCallbacks(mStartRecordingTask);
        mHandler.removeCallbacks(mStopRecorderTask);
        mHandler.removeCallbacks(mCheckMemoryCapacityTask);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();

        removeCallbackTask();

        stopRecorder();

        if (null != mSonyCameraDetachedReceiver) {
            unregisterReceiver(mSonyCameraDetachedReceiver);
            mSonyCameraDetachedReceiver = null;
        }

        if (mIsCameraRecognitionOpened) {
            closeCameraRecognition();
        }

        if (null != mUSBDeviceBroadcastReceiver) {
            unregisterReceiver(mUSBDeviceBroadcastReceiver);
            mUSBDeviceBroadcastReceiver = null;
        }

        TVCameraApp.setSecurityCameraService(null);
        TvAppExtension tvAppExtension = TVCameraApp.getTvAppExtension();
        if (null != tvAppExtension) {
            tvAppExtension.notifyServiceStatus();
        }
        releaseWakeLock();
    }

    @Override
    public void notifyStartCheckMemoryCapacityTask() {
        mHandler.removeCallbacks(mCheckMemoryCapacityTask);
        mHandler.postDelayed(mCheckMemoryCapacityTask, Utils.CHECK_MEM_CAPACITY_INTERVAL);
    }

    public void deleteFiles(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File subFiles[] = file.listFiles();
                for (File subFile : subFiles) {
                    deleteFiles(subFile);
                }
            }
            file.delete();
        }
    }

}