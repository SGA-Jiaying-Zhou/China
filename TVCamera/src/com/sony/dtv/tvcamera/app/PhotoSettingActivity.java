package com.sony.dtv.tvcamera.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;

import com.sony.dtv.camerarecognition.CameraRecognition;
import com.sony.dtv.camerarecognition.CameraRecognitionError;
import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.cameracomponent.CameraEncoder;
import com.sony.dtv.tvcamera.app.photosetting.OptionItem;
import com.sony.dtv.tvcamera.app.photosetting.PhotoSettingConstants;
import com.sony.dtv.tvcamera.app.photosetting.SettingFragment;
import com.sony.dtv.tvcamera.app.receiver.SonyCameraDetachedReceiver;
import com.sony.dtv.tvcamera.app.widget.FaceRecognitionView;
import com.sony.dtv.tvcamera.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressLint("NewApi")
public class PhotoSettingActivity extends Activity implements SettingFragment.UpdateCallBack, TextureView.SurfaceTextureListener, CameraStatusListener, Camera.PreviewCallback {

    private static final String TAG = "PhotoSettingActivity";

    private BroadcastReceiver mFinishReceiver = null;
    private boolean mIsReturnPressed = false;
    private boolean mIsLaunchFromTVAction = false;

    private Bitmap mPhotoBitmap = null;
    private FaceRecognitionView mFaceRecognitionView = null;
    private volatile CameraRecognition mCameraRecognition = null;
    private MyRecognitionCallback mMyRecognitionCallback = new MyRecognitionCallback();

    private FragmentManager mFragmentManager;

    private OptionItem SMILE_SHUTTER;
    private OptionItem PICTURE_SIZE;
    private OptionItem PHOTO_DESTINATION;

    private USBChangeBroadcastReceiver mUSBChangeReceiver;

    private boolean mIsCameraRecognitionOpened = false;
    private boolean mIsCameraRecognitionStarted = false;
    private boolean mIsPhotoSettingDestroyed = false;

    private SettingFragment mSettingFragment;

    private SonyCameraDetachedReceiver mSonyCameraDetachedReceiver;
    private CameraEncoder mCameraEncoder = null;
    //    private boolean mIsPreviewStartFlag = false;
    private TextureView mCameraTextureView;

    private long mPreviewStartTime;

    private volatile byte[] mPreviewData = null;
    private ExecutorService mExecutorService;
    private Object mLock = new Object();
    LinkedList<String> mUSBList;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.v(TAG, "onSurfaceTextureAvailable");
        boolean isFirstLaunch = Utils.isFirstLaunch();
        Log.d(TAG, "isFirstLaunch = " + isFirstLaunch);
        if (isFirstLaunch) {
            int type = Utils.getCameraSupportType();
            if (type == Utils.CAMERA_NOT_SUPPORT) {
                Utils.CameraSupportError(getApplication());
                return;
            }
        }

        mCameraEncoder.setSurfaceTexture(surface);
        mCameraEncoder.setCameraPreviewCallback(this);
        startPreview();

        mPreviewStartTime = SystemClock.elapsedRealtime();

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCameraEncoder.setSurfaceTexture(null);
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void notifyCameraStatus(boolean isCameraAvailable) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        long previewCurTime = SystemClock.elapsedRealtime();
        if (previewCurTime - mPreviewStartTime >= 800 && mPreviewData == null && mIsCameraRecognitionStarted) {
            mPreviewStartTime = previewCurTime;
            mPreviewData = data;
            mExecutorService.execute(mPreviewDataAnalysisTask);
        }
    }

    Runnable mPreviewDataAnalysisTask = new Runnable() {
        @Override
        public void run() {
            synchronized (mLock) {
                if (mCameraRecognition == null) {
                    mPreviewData = null;
                    Log.d(TAG, "mPreviewDataAnalysisTask ... mCameraRecognition == null !!!");
                    return;
                }
                Log.d(TAG, "call putImage begin");
                int ret = mCameraRecognition.putImage(mPreviewData, 1);
                Log.d(TAG, "call putImage, ret = " + ret);
                if (CameraRecognitionError.RESULT_OK != ret) {
                    Log.e(TAG, "putImage failed. ret=" + ret);
                }
                Log.d(TAG, "call putImage end");
                mPreviewData = null;
            }
        }
    };

    class MyRecognitionCallback implements CameraRecognition.RecognitionCallback {
        @Override
        public void onRecognize(int captureType, byte[] image, JSONObject result) {
            Log.d(TAG, "onRecognize." +
                    "captureType=" + String.format("%#08x", captureType));
            Log.d(TAG, "result=" + result.toString());
            if (captureType == CameraRecognition.CAPTURE_TYPE_SMILE) {
                Log.d(TAG, "Received CAPTURE_TYPE_SMILE image");
            } else if (captureType == CameraRecognition.CAPTURE_TYPE_ONE_SHOT) {
                Log.d(TAG, "Received CAPTURE_TYPE_ONE_SHOT image");
                drawFaceRecognitionView(result);
            } else if (captureType == CameraRecognition.CAPTURE_TYPE_ONE_SHOT_WITHOUT_ANALYSIS) {
                Log.d(TAG, "Received CAPTURE_TYPE_ONE_SHOT_WITHOUT_ANALYSIS image");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mFaceRecognitionView.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                Log.w(TAG, "unknown captureType=" + captureType);
            }
        }

        public ArrayList<RectF> extractRectFList(JSONObject result) {
            ArrayList<RectF> rectFList = new ArrayList<>();
            try {
                String tempStr = result.optString("FaceResult");
                JSONArray tempJsonArray = null;
                JSONObject tempJsonObject = null;
                if (!TextUtils.isEmpty(tempStr)) {
                    tempJsonObject = new JSONObject(tempStr);
                    tempStr = tempJsonObject.optString("FaceData");
                    if (!TextUtils.isEmpty(tempStr)) {
                        tempJsonArray = new JSONArray(tempStr);

                        for (int i = 0; i < tempJsonArray.length(); i++) {
                            tempJsonObject = tempJsonArray.getJSONObject(i);
                            tempStr = tempJsonObject.optString("areaInfo");
                            if (!TextUtils.isEmpty(tempStr)) {
                                tempJsonObject = new JSONObject(tempStr);
                                float xPosition = 0f;
                                float yPosition = 0f;
                                float width = 0f;
                                float height = 0f;
                                if (!TextUtils.isEmpty(tempJsonObject.optString("x"))) {
                                    xPosition = (float) tempJsonObject.optDouble("x");
                                }
                                if (!TextUtils.isEmpty(tempJsonObject.optString("y"))) {
                                    yPosition = (float) tempJsonObject.optDouble("y");
                                }
                                if (!TextUtils.isEmpty(tempJsonObject.optString("width"))) {
                                    width = (float) tempJsonObject.optDouble("width");
                                }
                                if (!TextUtils.isEmpty(tempJsonObject.optString("height"))) {
                                    height = (float) tempJsonObject.optDouble("height");
                                }
                                RectF rect = new RectF(xPosition, yPosition, xPosition + width, yPosition + height);
                                rectFList.add(rect);
                                Log.d(TAG, "rectFList = " + rectFList);
                            }
                        }
                    }
                    return rectFList;
                }
            } catch (JSONException e) {
                Log.w(TAG, e);
                return null;
            }
            return null;
        }

        private void drawFaceRecognitionView(JSONObject result) {
            final JSONObject jsonResult = result;
            Log.d(TAG, "jsonResult = " + jsonResult);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences sp = getSharedPreferences(PhotoSettingConstants.SP_NAME, Context.MODE_PRIVATE);
                    String smileShutterKey = sp.getString(PhotoSettingConstants.SmileShutterKey, getString(R.string.smile_shutter_defValue));

                    if (smileShutterKey.equals("On")) {
                        mFaceRecognitionView.setVisibility(View.VISIBLE);
                        mFaceRecognitionView.updateFaceRecognitionRect(extractRectFList(jsonResult));
                    } else {
                        mFaceRecognitionView.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        @Override
        public void onOpen(int result) {
            Log.d(TAG, "onOpen. result=" + result);
            Log.d(TAG, "onOpen. mIsCameraRecognitionOpened = " + mIsCameraRecognitionOpened);
            if (!mIsCameraRecognitionOpened) {
                mIsCameraRecognitionOpened = true;
                Log.d(TAG, "onOpen. mIsPhotoSettingDestroyed = " + mIsPhotoSettingDestroyed);
                if (mIsPhotoSettingDestroyed) {
                    Log.d(TAG, "onOpen. mCameraRecognition = " + mCameraRecognition);
                    if (null != mCameraRecognition) {
                        int ret = mCameraRecognition.closeRecognition();
                        Log.d(TAG, "call closeRecognition. ret=" + ret);
                        Utils.handleCameraRecognitionError(ret, getApplicationContext(), true, true);
                        mCameraRecognition = null;
                    }
                    return;
                }
            }
/*
            if (CameraRecognitionError.RESULT_ERR_CAMERA_DEVICE_UNAVAILABLE == result) {
                Utils.CameraSupportError(PhotoSettingActivity.this);
                return;
            }*/

            // Init.
            Utils.handleCameraRecognitionError(result, PhotoSettingActivity.this);
            if (CameraRecognitionError.RESULT_OK != result) {
                if (null != mCameraRecognition) {
                    result = mCameraRecognition.closeRecognition();
                    Log.d(TAG, "call closeRecognition. ret=" + result);
                    Utils.handleCameraRecognitionError(result, PhotoSettingActivity.this);
                    mCameraRecognition = null;
                }
                return;
            }

            if (null != mCameraRecognition) {
                int ret = mCameraRecognition.initRecognition(
                        Utils.CAMERA_SIZE_720P_WIDTH, // resolutionWidth.
                        Utils.CAMERA_SIZE_720P_HEIGHT, // resolutionHeight.
                        CameraRecognition.CAPTURE_TYPE_ONE_SHOT // captureType
                );
                Log.d(TAG, "call initRecognition. ret=" + ret);
                Utils.handleCameraRecognitionError(ret, PhotoSettingActivity.this);
                if (CameraRecognitionError.RESULT_OK != ret) {
                    return;
                }

                mHandler.removeCallbacks(mStartCapturingTask);
                mHandler.post(mStartCapturingTask);
            }
        }

        @Override
        public void onError(int errorCode) {
            Log.w(TAG, "onError. ret=" + errorCode);
            Utils.handleCameraRecognitionError(errorCode, PhotoSettingActivity.this);
        }
    }

    private void closeCameraRecognition() {
        synchronized (mLock) {
            if (null != mCameraRecognition) {
                int ret = mCameraRecognition.stopRecognition();
                Log.d(TAG, "call stopRecognition ret=" + ret);
                Utils.handleCameraRecognitionError(ret, getApplicationContext(), true, true);
                mIsCameraRecognitionStarted = false;
            }

            if (null != mCameraRecognition) {
                int ret = mCameraRecognition.closeRecognition();
                Log.d(TAG, "call closeRecognition. ret=" + ret);
                Utils.handleCameraRecognitionError(ret, getApplicationContext(), true, true);
                mCameraRecognition = null;
            }
        }
    }

    /**
     * mHolder Broadcast to receive action form TvAction service
     */
    BroadcastReceiver mTvActionReceiver = null;
    private final Handler mHandler = new Handler();

    public Fragment createSettingFragment() {
        mSettingFragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putBoolean(SettingFragment.EXTRA_IS_VISIBLE, mIsLaunchFromTVAction);
        mSettingFragment.setArguments(args);
        mSettingFragment.setCallBack(this);
        return mSettingFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        TVCameraApp.setPhotoSettingActivity(this);

        mIsPhotoSettingDestroyed = false;
        mIsCameraRecognitionOpened = false;
        Utils.setIsErrorShowed(false);
        mIsReturnPressed = false;

        mUSBList = new LinkedList<>();

        Intent intent = getIntent();
        mIsLaunchFromTVAction = intent.getBooleanExtra("isLaunchFromTVAction", false);
        Log.d(TAG, "mIsLaunchFromTVAction = " + mIsLaunchFromTVAction);

        boolean isFirstLaunchIntent = intent.getBooleanExtra("isLaunchIntent", false);
        Utils.setIsFirstLaunch(isFirstLaunchIntent);
        Log.d(TAG, "isFirstLaunchIntent = " + isFirstLaunchIntent);

        TVCameraApp.registerTerminateKeyReceiver();
        setContentView(R.layout.photo_setting);

        mCameraTextureView = (TextureView) findViewById(R.id.camera_preview);
        mCameraTextureView.setSurfaceTextureListener(this);

        updateUsbState(getApplicationContext());

        OptionItem.clearOptionItemList();
        initStaticOptionItems(getApplicationContext());
        updateDynamicOptionItems(getApplicationContext());

        mFragmentManager = getFragmentManager();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        Fragment fragmentSetting = mFragmentManager.findFragmentById(R.id.fragment_Setting);
        if (fragmentSetting == null) {
            fragmentSetting = createSettingFragment();
            ft.add(R.id.fragment_Setting, fragmentSetting);
        }
        ft.commit();

        // Register action from Tv Action
        registerTvActionReceiver();

        mFinishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                    finish();
                }
            }
        };
        IntentFilter locale_changed_filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
        registerReceiver(mFinishReceiver, locale_changed_filter);

        mFaceRecognitionView = (FaceRecognitionView) findViewById(R.id.face_recognition_view);

        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbFilter.addDataScheme("file");
        mUSBChangeReceiver = new USBChangeBroadcastReceiver();
        registerReceiver(mUSBChangeReceiver, usbFilter);

        mExecutorService = Executors.newSingleThreadExecutor();
    }

    private void registerTvActionReceiver() {
        IntentFilter tvActionfilters = new IntentFilter();
        tvActionfilters.addAction(Utils.INTENT_ACTION_CLOSE_APP);
        mTvActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "action: " + action);
                if (Utils.INTENT_ACTION_CLOSE_APP.equals(action)) {
                    unregisterTvActionReceiver();
                    finish();
                }
            }
        };
        try {
            registerReceiver(mTvActionReceiver, tvActionfilters);
        } catch (Exception ex) {
            Log.e(TAG, "Exception: " + ex);
        }
    }

    private void unregisterTvActionReceiver() {
        if (null != mTvActionReceiver) {
            unregisterReceiver(mTvActionReceiver);
            mTvActionReceiver = null;
        }
    }

    private void initStaticOptionItems(Context context) {
        // modify you data follow these
        SMILE_SHUTTER =
                newOptionItems("SMILE_SHUTTER",
                        PhotoSettingConstants.SmileShutterKey,
                        R.string.smile_shutter_title,
                        R.array.smile_shutter_entries,
                        R.array.smile_shutter_entryValues,
                        R.string.smile_shutter_defValue);

        PICTURE_SIZE =
                newOptionItems("PICTURE_SIZE",
                        PhotoSettingConstants.PictureSizeKey,
                        R.string.picture_size_title,
                        R.array.picture_size_entries,
                        R.array.picture_size_entryValues,
                        R.string.picture_size_defValue);

        OptionItem.addToOptionItemList(SMILE_SHUTTER);
        OptionItem.addToOptionItemList(PICTURE_SIZE);
    }

    private void updateDynamicOptionItems(Context applicationContext) {
        OptionItem.removeFromOptionItemList(PHOTO_DESTINATION);

        List<String> usbPaths = updateUsbPaths();

        PHOTO_DESTINATION =
                newOptionItems("PHOTO_DESTINATION",
                        PhotoSettingConstants.DestinationKey,
                        R.string.photo_destination_title,
                        usbPaths,
                        usbPaths,
                        "USB1");

        OptionItem.addToOptionItemList(PHOTO_DESTINATION);
    }

    private OptionItem newOptionItems(String viewKey, String valueKey, int titleId, int entriesId, int entryValuesId, int defValueId) {
        return new OptionItem(viewKey, valueKey,
                getString(titleId),
                Arrays.asList(getResources().getStringArray(entriesId)),
                Arrays.asList(getResources().getStringArray(entryValuesId)),
                getString(defValueId));
    }

    private OptionItem newOptionItems(String viewKey, String valueKey, int titleId, List<String> entries, List<String> entryValues, String defValue) {
        return new OptionItem(viewKey, valueKey,
                getString(titleId),
                entries,
                entryValues,
                defValue);

    }

    private void removeCallbackTask() {
        mHandler.removeCallbacks(mInitCameraRecognitionTask);
        mHandler.removeCallbacks(mStartCapturingTask);
    }

    @Override
    public void updateOptionItems() {
        updateDynamicOptionItems(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause!");
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume!");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();

        removeCallbackTask();

        if (mIsCameraRecognitionOpened) {
            closeCameraRecognition();
        }

        mIsPhotoSettingDestroyed = true;

        unregisterTvActionReceiver();

        if (null != mFinishReceiver) {
            unregisterReceiver(mFinishReceiver);
            mFinishReceiver = null;
        }

        if (null != mPhotoBitmap) {
            mPhotoBitmap.recycle();
            mPhotoBitmap = null;
        }

        OptionItem.clearOptionItemList();

        if (null != mUSBChangeReceiver) {
            unregisterReceiver(mUSBChangeReceiver);
        }

        if ((!Utils.isErrorShowed()) && ((mIsReturnPressed) || (TVCameraApp.isHomeKeyPress()))) {
            SharedPreferences smileShutter = getSharedPreferences(PhotoSettingConstants.SP_NAME, Context.MODE_PRIVATE);
            String smileShutterKey = smileShutter.getString(PhotoSettingConstants.SmileShutterKey, getString(R.string.smile_shutter_defValue));
            Log.d(TAG, "smileShutterKey = " + smileShutterKey);
            if ((mIsLaunchFromTVAction) && (smileShutterKey.equals("On"))) {
                Log.d(TAG, "start CameraRecognitionService!");
                Intent myIntent = new Intent().setClass(this, CameraRecognitionService.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND);
                startService(myIntent);
            }
        }

        TVCameraApp.setPhotoSettingActivity(null);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        Log.i(TAG, "onStart");

        mHandler.removeCallbacks(mInitCameraRecognitionTask);
        mHandler.postDelayed(mInitCameraRecognitionTask, 100);

        mCameraEncoder = new CameraEncoder(this, TAG);

        IntentFilter usbChangeFilter = new IntentFilter();
        usbChangeFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mSonyCameraDetachedReceiver = new SonyCameraDetachedReceiver(mCameraEncoder, this);
        registerReceiver(mSonyCameraDetachedReceiver, usbChangeFilter);
    }

    private void startPreview() {
        int cameraEncoderMode = -1;
        cameraEncoderMode = CameraEncoder.CAMERA_ONLY_START_PREVIEW_1080;

        if (cameraEncoderMode != -1) {
            Message msg = mCameraEncoder.obtainMessage(cameraEncoderMode);
            mCameraEncoder.sendMessage(msg);
        }

/*        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1, mCameraTextureView.getWidth() / 2, 0);
        mCameraTextureView.setTransform(matrix);*/
    }

    private void startCapturing() {
        if (null != mCameraRecognition) {
            int ret = mCameraRecognition.startRecognition();
            Log.d(TAG, "adb call startRecognition ret=" + ret);
            Utils.handleCameraRecognitionError(ret, getApplicationContext());
            if (ret == CameraRecognitionError.RESULT_OK) {
                mIsCameraRecognitionStarted = true;
            }
        }
    }

    Runnable mInitCameraRecognitionTask = new Runnable() {
        @Override
        public void run() {

            if (null == mCameraRecognition) {
                mCameraRecognition = new CameraRecognition();

                // Register callback.
                int ret = mCameraRecognition.registerRecognitionCallback(mMyRecognitionCallback);
                Log.d(TAG, "call registerRecognitionCallback. ret=" + ret);
                Utils.handleCameraRecognitionError(ret, getApplicationContext());

                mIsCameraRecognitionOpened = false;
                // Open.
                ret = mCameraRecognition.openRecognition(getApplicationContext(), false);
                Log.d(TAG, "call openRecognition. ret=" + ret);
                Utils.handleCameraRecognitionError(ret, getApplicationContext());
            }
        }
    };

    Runnable mStartCapturingTask = new Runnable() {
        @Override
        public void run() {
            startCapturing();
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // TODO Auto-generated method stub
        Log.v(TAG, "keyEvent:" + event);

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                if (mSettingFragment.isAllOptionView()) {
                    mIsReturnPressed = true;
//                    mHandler.removeCallbacks(mStartCapturingTask);

                    if (mIsCameraRecognitionOpened) {
                        closeCameraRecognition();
                    }
                    releasePreview();
                }

            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private void releasePreview() {
        Message msg = mCameraEncoder.obtainMessage(CameraEncoder.CAMERA_ONLY_RELEASE_PREVIEW);
        mCameraEncoder.sendMessage(msg);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mStartCapturingTask);

        releasePreview();

        if (mIsCameraRecognitionOpened) {
            closeCameraRecognition();
        }

        if (null != mSonyCameraDetachedReceiver) {
            unregisterReceiver(mSonyCameraDetachedReceiver);
        }

        Log.i(TAG, "mIsReturnPressed = " + mIsReturnPressed);
        if (!mIsReturnPressed) {
            Log.i(TAG, "send closeAppIntent!");
            Intent closeAppIntent = new Intent();
            closeAppIntent.setAction(Utils.INTENT_ACTION_CLOSE_APP);
            sendBroadcast(closeAppIntent);
            finish();
        }/* else {
            mIsReturnPressed = false;
        }*/
    }

    private class USBChangeBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateUsbState(context);
        }
    }

    private void updateUsbState(Context context) {
        mUSBList = Utils.getUSBPathList(mUSBList, getApplicationContext());

        SharedPreferences.Editor editor = context.getSharedPreferences("usb", Context.MODE_PRIVATE).edit();
        editor.putInt("usb_count", mUSBList.size());
        editor.commit();
        updatePathSPValue();
    }

    private int getUsbCount() {
        return getSharedPreferences("usb", Context.MODE_PRIVATE).getInt("usb_count", 0);
    }

    private List<String> updateUsbPaths() {
        updatePathSPValue();

        List<String> usbPaths = new ArrayList<>();

        for (int i = 0; i < getUsbCount(); i++) {
            usbPaths.add("USB" + (i + 1));
        }

        return usbPaths;
    }

    private String getCurUsbPath() {
        return getSharedPreferences(PhotoSettingConstants.SP_NAME, Context.MODE_PRIVATE).getString(PhotoSettingConstants.DestinationKey, "");
    }

    private void setCurUsbPath(String value) {
        SharedPreferences.Editor editor = getSharedPreferences(PhotoSettingConstants.SP_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(PhotoSettingConstants.DestinationKey, value);
        editor.commit();
    }

    private void updatePathSPValue() {
        String photoPathKey = PhotoSettingConstants.DestinationKey;
        String photoPathValue = getCurUsbPath();
        if ((getUsbCount() == 1 && !photoPathKey.equals("USB1")) || (getUsbCount() > 1 && TextUtils.isEmpty(photoPathValue))) {
            photoPathValue = "USB1";
            setCurUsbPath(photoPathValue);
        } else if (getUsbCount() == 0) {
            setCurUsbPath("");
        }
    }

    private String USB_DEVICE_FILTER = "/storage/sd";
    private String USB_DEVICE_SDCARD = "sdcard";
}
