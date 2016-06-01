package com.sony.dtv.tvcamera.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sony.dtv.osdplanevisibilitymanager.OsdPlaneVisibilityManager;
import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.cameracomponent.CameraEncoder;
import com.sony.dtv.tvcamera.app.permission.ManageNoncriticalPermissionExtActivity;
import com.sony.dtv.tvcamera.app.receiver.SonyCameraDetachedReceiver;
import com.sony.dtv.tvcamera.utils.Utils;
import com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener;
import com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateService;
import com.sony.dtv.tvx.tvplayer.legacy.ITvPlayerService;
import com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetMultiScreenModeListener;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

@SuppressLint("NewApi")
public class MirrorActivity extends ManageNoncriticalPermissionExtActivity implements View.OnClickListener, CameraStatusListener, USBStatusListener,
        TextureView.SurfaceTextureListener, CheckMemoryListener {

    private ITvDecimateService mDecimateService;
    private ITvPlayerService mPlayerService;
    private static final String TAG = "MirrorActivity";
    private static final String DECIMATESERVICEID = "TVCamera";
    private TextureView mCameraTextureView;
    private TextView mCurrentTimeTextView;
    private TextView mRecordingTimeTextView;
    private long mRecordingTime = 0;
    private boolean mIsRecording = false;
    private RelativeLayout mPrepareImageLayout;
    private ImageView mRecordingImage;
    private Button mCameraCaptureButton = null;
    private Button mSettingButton;
    private Button mSwitchWindowButton;
    private Button mAlbumButton;
    private Button mMirrorButton;
    private Button mSwitchToPhotoButton;
    private int mCameraMode = Utils.CAMERA_MODE_PAP_1;
    private static final int LAST_FOCUS_ON_CAMERA_CAPTURE = 0;
    private static final int CAMERA_MODE_ON_SETTING = 1;
    private static final int CAMERA_MODE_ON_SWITCH_WINDOW = 2;
    private static final int CAMERA_MODE_ON_ALBUM = 3;
    private static final int CAMERA_MODE_ON_CHANGE_MIRROR = 4;
    private static final int CAMERA_MODE_ON_SWITCH_TO_PHOTO = 5;
    private int mLastFocus = -1;
    private Matrix mTransform;
    private float mPivotX;
    private final String ALBUM_INTENT = "com.sonyericsson.album.ACTION_MAIN";
    LinkedList<String> mUSBList;
    private HashMap<String, String> mUSBPath;
    String USB_DEVICE_PATH = "/storage/sda1";
    String USB_DEVICE_SDCARD = "sdcard";
    String USB_DEVICE_FILTER = "/storage/sd";
    private USBDeviceBroadcastReceiver mUSBDeviceBroadcastReceiver = null;
    private BroadcastReceiver mFinishReceiver = null;
    private String mUSBCurrentPath;
    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;
    private CameraEncoder mCameraEncoder = null;

    private OsdPlaneVisibilityManager mOsdPlaneVisibilityManager = null;

    private boolean mIsPreviewStartFlag = false;
    private boolean mIsEnterSetting = false;
    private boolean mIsCameraAvailable = false;
    private boolean mIsUSBAvailable = false;
    private boolean mIsSwitchButtonPressed = false;
    boolean mIsNeedDisableFiercely = false;
    private boolean mIsMirror = false;
    private boolean mIsToastCanShown = true;
    private boolean mIsRecEnable = false;

    private boolean mIsLoadingFinished = false;

    private boolean mIsCheckStoragePermission = false;
    /**
     * mHolder Broadcast to receive action form TvAction service
     */
    BroadcastReceiver mTvActionReceiver = null;

    private SonyCameraDetachedReceiver mSonyCameraDetachedReceiver;

    private final Handler mHandler = new Handler();
    private final Runnable mUpdateCurrentTimeTask = new Runnable() {
        public void run() {
            long sysTime = System.currentTimeMillis();
            String strTimeFormat = null;
            boolean is24HourFormat = DateFormat.is24HourFormat(getApplicationContext());
            Log.v(TAG, "is24HourFormat = " + is24HourFormat);
            if (is24HourFormat) {
                Log.v(TAG, "24 hour");
                strTimeFormat = "yyyy/MM/dd(E) HH:mm";
            } else {
                Log.v(TAG, "12 hour");
                strTimeFormat = "yyyy/MM/dd(E) hh:mm";
            }
            CharSequence sysTimeStr = DateFormat.format(strTimeFormat, sysTime);
            mCurrentTimeTextView.setText(sysTimeStr);
            mHandler.removeCallbacks(mUpdateCurrentTimeTask);
            mHandler.postDelayed(mUpdateCurrentTimeTask, 60000);
        }
    };

    private final Runnable mUpdateRecodingTimeTask = new Runnable() {
        public void run() {
            mRecordingTime = mRecordingTime + 1000;
            String recodingTime = showTimeCount(mRecordingTime);
            mRecordingTimeTextView.setText(recodingTime);
            mHandler.removeCallbacks(mUpdateRecodingTimeTask);
            mHandler.postDelayed(mUpdateRecodingTimeTask, 1000);
        }
    };

    private final Runnable mShowFullViewTask = new Runnable() {
        public void run() {
            Log.d(TAG, "mShowFullViewTask");
            mPrepareImageLayout.setVisibility(View.GONE);
            mHandler.post(mSetDefaultFocusTask);
            Log.d(TAG, "mPrepareImage GONE");
        }
    };

    private final Runnable mSetDefaultFocusTask = new Runnable() {
        public void run() {
            setFocus();
            setBasicButtonFocusable(true);
        }
    };

    private final Runnable mChangeVideoViewTask = new Runnable() {
        public void run() {
            if (mDecimateService == null) {
                return;
            }
            try {
                switch (mCameraMode) {
                    case Utils.CAMERA_MODE_PAP_1:
                        mDecimateService.changeLayout(DECIMATESERVICEID, 0.632f, 0.632f, 0.003f,
                                0.183f, true);
                        break;
                    case Utils.CAMERA_MODE_PAP_2:
                        mDecimateService.changeLayout(DECIMATESERVICEID, 0.495f, 0.495f,
                                0.003f, 0.252f, true);
                        break;
                    case Utils.CAMERA_MODE_FULL_CAMERA:
                        mDecimateService.changeLayout(DECIMATESERVICEID, 1.0f, 1.0f, 0.0f, 0.0f,
                                true);
                        break;
                    default:
                        break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private ServiceConnection mPlayerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "mPlayerServiceConnection onServiceDisconnected");
            mPlayerService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "mPlayerServiceConnection onServiceConnected");
            mPlayerService = ITvPlayerService.Stub
                    .asInterface(service);
            if (mPlayerService != null) {
                try {
                    Bundle inputInfo = mPlayerService.getCurrentInputInfo(0);
                    String type = inputInfo.getString("type");
                    Log.d(TAG, "type = " + type);
                    if (type == null) {
                        Log.i(TAG, "type == null");
                        finish();
                        return;
                    }

                    String multiScreenMode = mPlayerService.getMultiScreenMode();
                    Log.d(TAG, "getMultiScreenMode : " + multiScreenMode);
                    if (!TextUtils.isEmpty(multiScreenMode) && !multiScreenMode.equals("SINGLE")) {
                        mPlayerService.setMultiScreenMode(
                                "SINGLE", mListener_setMultiScreenMode
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private ITvStateSetMultiScreenModeListener.Stub mListener_setMultiScreenMode = new ITvStateSetMultiScreenModeListener.Stub() {
        @Override
        public void notifyDone(String mode) throws RemoteException {
            Log.d(TAG, "TvStateSetMultiScreenModeListener.notifyDone : " + mode);
        }

        @Override
        public void notifyFail(int error) throws RemoteException {
            Log.d(TAG, "TvStateSetMultiScreenModeListener.notifyFail : " + error);
        }
    };

    private ServiceConnection mDecimateServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "mDecimateServiceConnection onServiceConnected");
            int requestResult = -1;
            mDecimateService = ITvDecimateService.Stub.asInterface(service);

            if (mDecimateService != null) {
                try {
                    requestResult = mDecimateService.requestScalePermission(
                            DECIMATESERVICEID, 1, mITvDecimateListener);
                    Log.d(TAG, "requestResult = " + requestResult);
                    if (0 != requestResult) {
                        Log.d(TAG, "requestScalePermission Error!");
                    }
                } catch (RemoteException ex) {
                    // TODO Auto-generated catch block
                    ex.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "mDecimateServiceConnection onServiceDisconnected");
            mDecimateService = null;
        }

    };

    ITvDecimateListener.Stub mITvDecimateListener = new ITvDecimateListener.Stub() {

        @Override
        public void notifyPermissionGranted() throws RemoteException {
            Log.d(TAG, "mDecimateServiceConnection notifyPermissionGranted");

            mHandler.removeCallbacks(mChangeVideoViewTask);
            mHandler.postDelayed(mChangeVideoViewTask, 2500);
        }

        @Override
        public boolean notifyPermissionDeprived() throws RemoteException {
            Log.d(TAG, "mDecimateServiceConnection notifyPermissionDeprived");
            return true;
        }

        @Override
        public void notifyLayoutChanged() throws RemoteException {
            Log.d(TAG, "mDecimateServiceConnection notifyLayoutChanged");
        }
    };

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
        mHandler.removeCallbacks(mSetCameraContentViewTask);
        if (mDecimateService != null) {
            try {
                mDecimateService.changeLayout(DECIMATESERVICEID, 1.0f, 1.0f, 0.0f, 0.0f,
                        true);
                mDecimateService.cancelRequestScalePermission(DECIMATESERVICEID);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (null != mSonyCameraDetachedReceiver) {
            unregisterReceiver(mSonyCameraDetachedReceiver);
            mSonyCameraDetachedReceiver = null;
        }

        releasePreview();

        if (!mIsToastCanShown) {
            mIsToastCanShown = true;
            mHandler.removeCallbacks(mToastTask);
        }

        unbindService(mDecimateServiceConnection);
        unbindService(mPlayerServiceConnection);

        Log.i(TAG, "mIsEnterSetting = " + mIsEnterSetting);
        if (!mIsEnterSetting && !mIsCheckStoragePermission) {
            finish();
        } else {
            mIsEnterSetting = false;
            mIsCheckStoragePermission = false;
        }
    }

    Runnable mToastTask = new Runnable() {
        @Override
        public void run() {
            mIsToastCanShown = true;
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // TODO Auto-generated method stub
        Log.v(TAG, "keyEvent:" + event);
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if ((mCameraCaptureButton != null) && (mCameraCaptureButton.hasFocus())) {
                        if (mUSBList.size() == 0) {
                            if (mIsToastCanShown) {
                                Toast.makeText(this, R.string.no_usb, Toast.LENGTH_SHORT).show();
                            }
                            AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            manager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID);
                        } else if (mUSBList.size() >= 1 && Utils.getMemoryCapacity(mUSBCurrentPath) <= 30) {
                            if (mIsToastCanShown) {
                                Toast.makeText(this, R.string.memory_little_tip, Toast.LENGTH_SHORT).show();
                            }
                            AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            manager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID);
                        }

                        if (mIsToastCanShown) {
                            mIsToastCanShown = false;
                            mHandler.postDelayed(mToastTask, 5000);
                        }
                    }
                }
            case KeyEvent.KEYCODE_SEARCH:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return super.dispatchKeyEvent(event);
            default:
                try {
                    mPlayerService.sendKeyEvent(event);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return true;
        }
    }

    public String showTimeCount(long time) {
        if (time >= 360000000) {
            mRecordingTime = 0;
            return "00:00:00";
        }
        String timeCount;
        long hourc = time / 3600000;
        String hour = "0" + hourc;
        hour = hour.substring(hour.length() - 2, hour.length());

        long minuec = (time - hourc * 3600000) / (60000);
        String minue = "0" + minuec;
        minue = minue.substring(minue.length() - 2, minue.length());

        long secc = (time - hourc * 3600000 - minuec * 60000) / 1000;
        String sec = "0" + secc;
        sec = sec.substring(sec.length() - 2, sec.length());
        timeCount = hour + ":" + minue + ":" + sec;
        return timeCount;
    }

    private final Runnable mSetCameraContentViewTask = new Runnable() {
        public void run() {
            setCameraContentView();
        }
    };

    private void setCameraContentView() {
        mCameraEncoder = new CameraEncoder(this, TAG);

        switch (mCameraMode) {
            case Utils.CAMERA_MODE_PAP_1:
                setContentView(R.layout.camera_pap_1);
                break;
            case Utils.CAMERA_MODE_PAP_2:
                setContentView(R.layout.camera_pap_2);
                break;
            case Utils.CAMERA_MODE_FULL_CAMERA:
                setContentView(R.layout.camera_full_camera);
                break;
            default:
                break;
        }

        mPrepareImageLayout = (RelativeLayout) findViewById(R.id.prepare_image);

        init();

        mHandler.removeCallbacks(mUpdateCurrentTimeTask);
        mHandler.postDelayed(mUpdateCurrentTimeTask, 0);

        mHandler.removeCallbacks(mShowFullViewTask);
        mHandler.postDelayed(mShowFullViewTask, 3000);

        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        mUSBDeviceBroadcastReceiver = new USBDeviceBroadcastReceiver();
        registerReceiver(mUSBDeviceBroadcastReceiver, filter);

        getUsbPath(null);

        IntentFilter usbChangeFilter = new IntentFilter();
        usbChangeFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mSonyCameraDetachedReceiver = new SonyCameraDetachedReceiver(mCameraEncoder, this);
        registerReceiver(mSonyCameraDetachedReceiver, usbChangeFilter);

        mIsLoadingFinished = true;
    }

    private String[] mCriticalPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    private boolean isPermissionDenied(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        for (String permission : mCriticalPermissions) {
            if (isPermissionDenied(permission)) {
                finish();
                return;
            }
        }

        if (TVCameraApp.isTerminateKeyPress()) {
            finish();
            return;
        }

        Intent intent = getIntent();
        boolean isFirstLaunchIntent = intent.getBooleanExtra("isLaunchIntent", false);
        Utils.setIsFirstLaunch(isFirstLaunchIntent);
        Log.i(TAG, "isFirstLaunchIntent = " + isFirstLaunchIntent);
        if (isFirstLaunchIntent) {
            Utils.sendStateChangeIntent(true);
        }

        mIsEnterSetting = false;
        mIsCheckStoragePermission = false;
        TVCameraApp.registerTerminateKeyReceiver();
        mIsCameraAvailable = false;
        mIsUSBAvailable = false;
        mIsNeedDisableFiercely = false;
        mIsSwitchButtonPressed = false;

        if(null == mOsdPlaneVisibilityManager) {
            mOsdPlaneVisibilityManager = new OsdPlaneVisibilityManager();
        }

        mOsdPlaneVisibilityManager.forceOsdPlaneInvisible(getApplicationContext(), true);
        Log.i(TAG, "forceOsdPlaneInvisible TRUE");

        setContentView(R.layout.activity_pre_load);

        mUSBList = new LinkedList<>();
        mUSBPath = new HashMap<>();

        // Register action from Tv Action
        registerTvActionReceiver();
        mLastFocus = LAST_FOCUS_ON_CAMERA_CAPTURE;
        int lastFocusOnSwitchToCamera = intent.getIntExtra("FocusOnSwitchToCamera", 0);
        if (lastFocusOnSwitchToCamera != 0) {
            mLastFocus = CAMERA_MODE_ON_SWITCH_TO_PHOTO;
        }

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

        SharedPreferences sp = getSharedPreferences("tvcameramode", Context.MODE_PRIVATE);
        mEditor = sp.edit();
        mEditor.putInt("tvcameramodevalue", Utils.CAMERA_MODE);
        mEditor.commit();

        mIsLoadingFinished = false;

        SharedPreferences CameraMode = getSharedPreferences("cameramode", Context.MODE_PRIVATE);
        mCameraMode = CameraMode.getInt("cameramodevalue", Utils.CAMERA_MODE_PAP_1);

        if (isFirstLaunchIntent) {
            mHandler.postDelayed(mSetCameraContentViewTask, 5000);
        } else {
            mHandler.post(mSetCameraContentViewTask);
        }
    }

    private void registerTvActionReceiver() {
        IntentFilter tvActionFilters = new IntentFilter();
        tvActionFilters.addAction(Utils.INTENT_ACTION_CLOSE_APP);
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
            registerReceiver(mTvActionReceiver, tvActionFilters);
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

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause!");
        if (mIsRecording) {
            stopRecorder();
        }
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
        if (!mIsToastCanShown) {
            mIsToastCanShown = true;
            mHandler.removeCallbacks(mToastTask);
        }

        unregisterTvActionReceiver();
        if (null != mUSBDeviceBroadcastReceiver) {
            unregisterReceiver(mUSBDeviceBroadcastReceiver);
            mUSBDeviceBroadcastReceiver = null;
        }
        if (null != mFinishReceiver) {
            unregisterReceiver(mFinishReceiver);
            mFinishReceiver = null;
        }


        if(null != mOsdPlaneVisibilityManager) {
            mOsdPlaneVisibilityManager.forceOsdPlaneInvisible(getApplicationContext(), false);
            Log.i(TAG, "forceOsdPlaneInvisible FALSE");
            mOsdPlaneVisibilityManager = null;
        }

        if (!mIsSwitchButtonPressed) {
            Utils.sendStateChangeIntent(false);
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        Log.i(TAG, "onStart");
        getUsbPath(null);
        checkStorage();

        if (mUSBList.size() == 0) {
            Toast.makeText(this, R.string.no_usb, Toast.LENGTH_LONG).show();
        }

        bindService((new Intent()).setClassName("com.sony.dtv.tvx",
                "com.sony.dtv.tvx.tvplayer.legacy.TvDecimateService"),
                mDecimateServiceConnection, BIND_AUTO_CREATE);

        bindService((new Intent()).setClassName("com.sony.dtv.tvx",
                "com.sony.dtv.tvx.tvplayer.legacy.TvPlayerService"),
                mPlayerServiceConnection, BIND_AUTO_CREATE);

        mIsRecEnable = false;

        if ((mIsLoadingFinished) && (null == mSonyCameraDetachedReceiver)) {
            IntentFilter usbChangeFilter = new IntentFilter();
            usbChangeFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            mSonyCameraDetachedReceiver = new SonyCameraDetachedReceiver(mCameraEncoder, this);
            registerReceiver(mSonyCameraDetachedReceiver, usbChangeFilter);
        }
    }

    private void removeCallbackTask() {
        mHandler.removeCallbacks(mSetCameraContentViewTask);
        mHandler.removeCallbacks(mUpdateCurrentTimeTask);
        mHandler.removeCallbacks(mUpdateRecodingTimeTask);
        mHandler.removeCallbacks(mShowFullViewTask);
        mHandler.removeCallbacks(mSetDefaultFocusTask);
        mHandler.removeCallbacks(mCheckMemoryCapacityTask);
    }

    protected void init() {
        mCameraTextureView = (TextureView) findViewById(R.id.camera_preview);
        mCameraCaptureButton = (Button) findViewById(R.id.camera_capture);
        mSettingButton = (Button) findViewById(R.id.setting);
        mSwitchWindowButton = (Button) findViewById(R.id.switch_window);
        mAlbumButton = (Button) findViewById(R.id.album);
        mMirrorButton = (Button) findViewById(R.id.mirror);
        mSwitchToPhotoButton = (Button) findViewById(R.id.to_photo);
        mCurrentTimeTextView = (TextView) findViewById(R.id.current_time);
        mRecordingTimeTextView = (TextView) findViewById(R.id.recoding_time);
        mRecordingImage = (ImageView) findViewById(R.id.recoding_img);

        mCameraCaptureButton.setOnClickListener(this);
        mSwitchWindowButton.setOnClickListener(this);
        mAlbumButton.setOnClickListener(this);
        mSettingButton.setOnClickListener(this);
        mMirrorButton.setOnClickListener(this);
        mSwitchToPhotoButton.setOnClickListener(this);
        mCameraTextureView.setSurfaceTextureListener(this);

        mSettingButton.setOnFocusChangeListener(mOnFocusChangeListener);
        mMirrorButton.setOnFocusChangeListener(mOnFocusChangeListener);
        mSwitchToPhotoButton.setOnFocusChangeListener(mOnFocusChangeListener);
        mSwitchWindowButton.setOnFocusChangeListener(mOnFocusChangeListener);
        mAlbumButton.setOnFocusChangeListener(mOnFocusChangeListener);

        setCameraCaptureButtonStatus();
    }


    private void findFocus() {
        if (mCameraCaptureButton.hasFocus()) {
            mLastFocus = LAST_FOCUS_ON_CAMERA_CAPTURE;
        } else if (mSettingButton.hasFocus()) {
            mLastFocus = CAMERA_MODE_ON_SETTING;
        } else if (mSwitchWindowButton.hasFocus()) {
            mLastFocus = CAMERA_MODE_ON_SWITCH_WINDOW;
        } else if (mAlbumButton.hasFocus()) {
            mLastFocus = CAMERA_MODE_ON_ALBUM;
        } else if (mMirrorButton.hasFocus()) {
            mLastFocus = CAMERA_MODE_ON_CHANGE_MIRROR;
        } else if (mSwitchToPhotoButton.hasFocus()) {
            mLastFocus = CAMERA_MODE_ON_CHANGE_MIRROR;
        }
    }

    private void setFocus() {
        Button defaultFocusedButton = mCameraCaptureButton;
        switch (mLastFocus) {
            case LAST_FOCUS_ON_CAMERA_CAPTURE:
                break;
            case CAMERA_MODE_ON_SETTING:
                defaultFocusedButton = mSettingButton;
                break;
            case CAMERA_MODE_ON_SWITCH_WINDOW:
                defaultFocusedButton = mSwitchWindowButton;
                break;
            case CAMERA_MODE_ON_ALBUM:
                defaultFocusedButton = mAlbumButton;
                break;
            case CAMERA_MODE_ON_CHANGE_MIRROR:
                defaultFocusedButton = mMirrorButton;
                break;
            case CAMERA_MODE_ON_SWITCH_TO_PHOTO:
                defaultFocusedButton = mSwitchToPhotoButton;
                break;
            default:
                break;
        }
        defaultFocusedButton.setFocusable(true);
        defaultFocusedButton.setFocusableInTouchMode(true);
        defaultFocusedButton.requestFocus();
        defaultFocusedButton.requestFocusFromTouch();
    }

    @Override
    public void notifyStartCheckMemoryCapacityTask() {
        mHandler.removeCallbacks(mCheckMemoryCapacityTask);
        mHandler.postDelayed(mCheckMemoryCapacityTask, Utils.CHECK_MEM_CAPACITY_INTERVAL);
    }

    private void StartRecording(int CameraEncoderMode) {

        if (!mIsRecording) {
            Message msg = mCameraEncoder.obtainMessage(CameraEncoderMode);
            mCameraEncoder.sendMessage(msg);
            mCameraCaptureButton.setBackgroundResource(R.drawable.rec_stop_button);
            mCameraCaptureButton.setText(R.string.rec_stop);
            mIsRecording = !mIsRecording;
            disableCaptureButtonFiercely(true);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    disableCaptureButtonFiercely(false);
                }
            }, 2500);

            mSettingButton.setFocusable(false);
            mSettingButton.setEnabled(false);
            mSwitchWindowButton.setFocusable(false);
            mSwitchWindowButton.setEnabled(false);
            mSwitchToPhotoButton.setFocusable(false);
            mSwitchToPhotoButton.setEnabled(false);

            mRecordingTime = 0;
            mRecordingTimeTextView.setText("00:00:00");
            mHandler.removeCallbacks(mUpdateRecodingTimeTask);
            mHandler.postDelayed(mUpdateRecodingTimeTask, 1000);
            mRecordingImage.setVisibility(View.VISIBLE);
            mRecordingTimeTextView.setVisibility(View.VISIBLE);
            mAlbumButton.setEnabled(false);
            mAlbumButton.setFocusable(false);
        } else {
            /*
             * stop
			 */
            stopRecorder();
        }

    }

    public void stopRecorder() {
        Message msg1 = mCameraEncoder.obtainMessage(CameraEncoder.CAMERA_STOP_RECORDER);
        mCameraEncoder.sendMessage(msg1);
        mHandler.removeCallbacks(mCheckMemoryCapacityTask);
        mCameraCaptureButton.setBackgroundResource(R.drawable.rec_start_button);
        mCameraCaptureButton.setText(R.string.rec_start);
        mIsRecording = !mIsRecording;
        mSettingButton.setFocusable(true);
        mSettingButton.setEnabled(true);
        mSwitchWindowButton.setFocusable(true);
        mSwitchWindowButton.setEnabled(true);
        mSwitchToPhotoButton.setFocusable(true);
        mSwitchToPhotoButton.setEnabled(true);
        mHandler.removeCallbacks(mUpdateRecodingTimeTask);
        mRecordingImage.setVisibility(View.GONE);
        mRecordingTimeTextView.setVisibility(View.GONE);
        mAlbumButton.setEnabled(true);
        mAlbumButton.setFocusable(true);
        if (Utils.getMemoryCapacity(mUSBCurrentPath) <= 30) {
            setUSBStatus(false);
        }

    }

    private void setBasicButtonFocusable(boolean isFocusable) {
        Log.d(TAG, "setBasicButtonFocusable isFocusable = " + isFocusable);
        mCameraCaptureButton.setFocusable(isFocusable);
        mSettingButton.setFocusable(isFocusable);
        mSwitchToPhotoButton.setFocusable(isFocusable);
        mSwitchWindowButton.setFocusable(isFocusable);
        mMirrorButton.setFocusable(isFocusable);
        mAlbumButton.setFocusable(isFocusable);
    }

    private void Mirror() {
        mTransform.postScale(-1, 1, mPivotX, 0);
        mCameraTextureView.setTransform(mTransform);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_capture:
                Log.i(TAG, "camera capture be pressed!");
                checkStoragePermission();
                break;

            case R.id.setting:
                Log.i(TAG, "setting be pressed!");
                mIsEnterSetting = true;
                Intent startSetting = new Intent(getApplicationContext(),
                        SettingActivity.class);

                startActivity(startSetting);
                break;

            case R.id.album:
                Log.i(TAG, "album be pressed!");
                stopPreview();
                Intent startMain = new Intent(ALBUM_INTENT);
                startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(startMain);
                break;

            case R.id.switch_window:
                stopPreview();

                if (mDecimateService == null) {
                    return;
                }
                Log.i(TAG, "switch window be pressed!");
                mCameraTextureView.setVisibility(View.INVISIBLE);
                findFocus();

                mCameraMode = (mCameraMode + 1) % Utils.CAMERA_MODE_NUM;
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);
                    String keySize;
                    String KeySizeTitle;
                    String key_size_previous;
                    String key_size_previous_title;
                    switch (mCameraMode) {
                        case Utils.CAMERA_MODE_PAP_1:
                            key_size_previous = sharedPreferences.getString("key_size_previous", "");
                            key_size_previous_title = sharedPreferences.getString("key_size_previous_title", "");
                            keySize = sharedPreferences.getString("video_size_key", "");

                            if (Utils.VIDEO_SIZE_1080.equals(keySize) && !TextUtils.equals(key_size_previous, keySize)) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("video_size_key", key_size_previous);
                                editor.putString("video_size_title", key_size_previous_title);
                                editor.commit();
                            }

                            mDecimateService.changeLayout(DECIMATESERVICEID, 0.632f, 0.632f, 0.003f,
                                    0.183f, true);
                            setContentView(R.layout.camera_pap_1);
                            break;
                        case Utils.CAMERA_MODE_PAP_2:
                            mDecimateService.changeLayout(DECIMATESERVICEID, 0.495f, 0.495f,
                                    0.003f, 0.252f, true);
                            setContentView(R.layout.camera_pap_2);
                            break;
                        case Utils.CAMERA_MODE_FULL_CAMERA:
                            keySize = sharedPreferences.getString("video_size_key", "");
                            KeySizeTitle = sharedPreferences.getString("video_size_title", "");
                            if (Utils.VIDEO_SIZE_1080.equals(keySize) || Utils.VIDEO_SIZE_720.equals(keySize) || Utils.VIDEO_SIZE_360.equals(keySize)) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("key_size_previous", keySize);
                                editor.putString("key_size_previous_title", KeySizeTitle);

                                editor.putString("video_size_key", Utils.VIDEO_SIZE_1080);
                                editor.putString("video_size_title", "1920 x 1080");
                                editor.commit();
                            }

                            mDecimateService.changeLayout(DECIMATESERVICEID, 1.0f, 1.0f, 0.0f, 0.0f,
                                    true);
                            setContentView(R.layout.camera_full_camera);
                            break;
                        default:
                            break;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                mPrepareImageLayout = (RelativeLayout) findViewById(R.id.prepare_image);
                mPrepareImageLayout.setVisibility(View.GONE);

                init();

                SharedPreferences CameraMode = getSharedPreferences("cameramode", Utils.CAMERA_MODE_PAP_1);
                mEditor = CameraMode.edit();
                mEditor.putInt("cameramodevalue", mCameraMode);
                mEditor.commit();

                if (mUSBList.size() == 0 || Utils.getMemoryCapacity(mUSBCurrentPath) <= 30) {
                    setUSBStatus(false);
                }

                mHandler.removeCallbacks(mSetDefaultFocusTask);
                mHandler.postDelayed(mSetDefaultFocusTask, 200);

                mHandler.removeCallbacks(mUpdateCurrentTimeTask);
                mHandler.postDelayed(mUpdateCurrentTimeTask, 200);

                break;

            case R.id.mirror:
                SharedPreferences mirrorPreferences = getSharedPreferences("mirror", 0);
                SharedPreferences.Editor mirrorEditor = mirrorPreferences.edit();
                Mirror();
                mIsMirror = !mIsMirror;
                mirrorEditor.putBoolean("mirrorflags", mIsMirror);
                mirrorEditor.commit();
                break;

            case R.id.to_photo:
                releasePreview();

                Intent myIntent = new Intent().setClass(this, PhotoActivity.class);
                myIntent.putExtra("FocusOnSwitchToPhoto", 1);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(myIntent);
                finish();
                mIsSwitchButtonPressed = true;
                break;
        }

    }

    private void stopPreview() {
        if (mIsPreviewStartFlag) {
            Message msg = mCameraEncoder.obtainMessage(CameraEncoder.CAMERA_STOP_PREVIEW);
            mCameraEncoder.sendMessage(msg);
            mIsPreviewStartFlag = false;
        }

    }

    private void releasePreview() {
        if (mIsPreviewStartFlag) {
            Message msg = mCameraEncoder.obtainMessage(CameraEncoder.CAMERA_RELEASE_PREVIEW);
            mCameraEncoder.sendMessage(msg);
            mIsPreviewStartFlag = false;
        }
    }

    // TextureView class
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                                          int height) {
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

        SharedPreferences sSettings = getSharedPreferences("setting", 0);
        String videoSize = sSettings.getString("video_size_key", "");
        if (videoSize.equals("")) {
            mEditor = sSettings.edit();
            mEditor.putString("video_size_key", Utils.VIDEO_SIZE_1080);
            mEditor.putString("video_size_title", "1920 x 1080");
            mEditor.commit();
            videoSize = Utils.VIDEO_SIZE_1080;
        }
        int cameraEncoderMode = -1;
        switch (mCameraMode) {
            case Utils.CAMERA_MODE_PAP_1: {
                if (Utils.VIDEO_SIZE_1080.equals(videoSize)) {
                    cameraEncoderMode = CameraEncoder.CAMERA_START_PREVIEW_1080;
                } else if (Utils.VIDEO_SIZE_720.equals(videoSize)) {
                    cameraEncoderMode = CameraEncoder.CAMERA_START_PREVIEW_720;
                } else if (Utils.VIDEO_SIZE_360.equals(videoSize)) {
                    cameraEncoderMode = CameraEncoder.CAMERA_START_PREVIEW_720;
                }
            }
            break;

            case Utils.CAMERA_MODE_PAP_2: {
                if (Utils.VIDEO_SIZE_1080.equals(videoSize)) {
                    cameraEncoderMode = CameraEncoder.CAMERA_START_PREVIEW_1080;
                } else if (Utils.VIDEO_SIZE_720.equals(videoSize)) {
                    cameraEncoderMode = CameraEncoder.CAMERA_START_PREVIEW_720;
                } else if (Utils.VIDEO_SIZE_360.equals(videoSize)) {
                    cameraEncoderMode = CameraEncoder.CAMERA_START_PREVIEW_720;
                }
            }
            break;

            case Utils.CAMERA_MODE_FULL_CAMERA: {
                cameraEncoderMode = CameraEncoder.CAMERA_START_PREVIEW_1080;
            }
            break;
            default:
                break;
        }

        if (cameraEncoderMode != -1) {
            Message msg = mCameraEncoder.obtainMessage(cameraEncoderMode);
            mCameraEncoder.sendMessage(msg);
        }
        mIsPreviewStartFlag = true;
        mTransform = new Matrix();
        mPivotX = mCameraTextureView.getWidth() / 2;

        boolean isCameraInsert = Utils.checkCameraInsert(getApplicationContext());
        if (!isCameraInsert) {
            return;
        }

        SharedPreferences mirrorPreferences = getSharedPreferences("mirror", 0);
        if (Utils.needMirror(this)) {
            mIsMirror = true;
        } else {
            mIsMirror = mirrorPreferences.getBoolean("mirrorflags", false);
        }
        if (mIsMirror) {
            Mirror();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                            int height) {
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCameraEncoder.setSurfaceTexture(null);
        return false;

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

    @Override
    protected void onStoragePermissionGranted() {
        Log.d(TAG, "onStoragePermissionGranted()");

        checkStorage();
        if (!mIsRecEnable) return;

        SharedPreferences sSettings = getSharedPreferences("setting", 0);
        String videoSize = sSettings.getString("video_size_key", "");
        if (mCameraMode == Utils.CAMERA_MODE_PAP_1) {
            if (Utils.VIDEO_SIZE_360.equals(videoSize)) {
                StartRecording(CameraEncoder.CAMERA_START_RECORDER_360);
            } else if (Utils.VIDEO_SIZE_720.equals(videoSize)) {
                StartRecording(CameraEncoder.CAMERA_START_RECORDER_720);
            } else if (Utils.VIDEO_SIZE_1080.equals(videoSize)) {
                StartRecording(CameraEncoder.CAMERA_START_RECORDER_1080);
            } else {
                StartRecording(CameraEncoder.CAMERA_START_RECORDER_1080);
            }
        } else if (mCameraMode == Utils.CAMERA_MODE_PAP_2) {
            if (Utils.VIDEO_SIZE_360.equals(videoSize)) {
                StartRecording(CameraEncoder.CAMERA_START_RECORDER_360);
            } else if (Utils.VIDEO_SIZE_720.equals(videoSize)) {
                StartRecording(CameraEncoder.CAMERA_START_RECORDER_720);
            } else if (Utils.VIDEO_SIZE_1080.equals(videoSize)) {
                StartRecording(CameraEncoder.CAMERA_START_RECORDER_1080);
            } else {
                StartRecording(CameraEncoder.CAMERA_START_RECORDER_1080);
            }
        } else if (mCameraMode == Utils.CAMERA_MODE_FULL_CAMERA) {
            StartRecording(CameraEncoder.CAMERA_START_RECORDER_1080);
        }
    }

    @Override
    protected void onStoragePermissionDenied() {
        Log.d(TAG, "onStoragePermissionDenied()");
        checkStorage();
    }

    @Override
    protected void onBeforeStartPromptActivity() {
        mIsCheckStoragePermission = true;
        setCameraCaptureButtonStatus(false);
    }

    @Override
    protected String getName() {
        return getString(R.string.video);
    }

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
            bIsNeedStorageCheck = !TextUtils.equals(mUSBCurrentPath, preUSBCurrentPath);
            if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_EJECT)) {
                if (mIsRecording && bIsNeedStorageCheck) {
                    stopRecorder();
                    new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle(R.string.dialog_title)
                            .setMessage(R.string.u_disk_pulled_out)
                            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (bIsNeedStorageCheck) {
                                        checkStorage();
                                    }
                                }

                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (bIsNeedStorageCheck) {
                                checkStorage();
                            }
                        }
                    }).show();
                } else {
                    if (!mIsRecording) {
                        Toast.makeText(context, context.getString(R.string.u_disk_pulled_out),
                                Toast.LENGTH_SHORT).show();
                    }

                    if (bIsNeedStorageCheck) {
                        checkStorage();
                    }
                }
                if (!mIsToastCanShown) {
                    mIsToastCanShown = true;
                    mHandler.removeCallbacks(mToastTask);
                }
            } else if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_MOUNTED)) {
                if (bIsNeedStorageCheck) {
                    checkStorage();
                }
            }
        }
    }

    public void checkMemoryCapacity() {
        mHandler.removeCallbacks(mCheckMemoryCapacityTask);
        if (Utils.getMemoryCapacity(mUSBCurrentPath) <= 30) {
            stopRecorder();
            new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle(R.string.dialog_title)
                    .setMessage(R.string.memory_little)
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "Alert insert camera Dialog!");
                        }
                    }).show();
        } else if (Utils.getVideoMemoryCapacity(Utils.getSaveFileAbsolutePath()) >= 3.5 * 1024) { //3.5 * 1024
            mCameraEncoder.sendEmptyMessage(CameraEncoder.CAMERA_STOP_RECORDER);
            SharedPreferences sSettings = getSharedPreferences("setting", 0);
            String videoSize = sSettings.getString("video_size_key", "");
            int recodingMode = -1;
            if (mCameraMode == Utils.CAMERA_MODE_PAP_1) {
                if (Utils.VIDEO_SIZE_360.equals(videoSize)) {
                    recodingMode = CameraEncoder.CAMERA_START_RECORDER_360;
                } else if (Utils.VIDEO_SIZE_720.equals(videoSize)) {
                    recodingMode = CameraEncoder.CAMERA_START_RECORDER_720;
                } else if (Utils.VIDEO_SIZE_1080.equals(videoSize)) {
                    recodingMode = CameraEncoder.CAMERA_START_RECORDER_1080;
                } else {
                    recodingMode = CameraEncoder.CAMERA_START_RECORDER_1080;
                }
            } else if (mCameraMode == Utils.CAMERA_MODE_PAP_2) {
                if (Utils.VIDEO_SIZE_360.equals(videoSize)) {
                    recodingMode = CameraEncoder.CAMERA_START_RECORDER_360;
                } else if (Utils.VIDEO_SIZE_720.equals(videoSize)) {
                    recodingMode = CameraEncoder.CAMERA_START_RECORDER_720;
                } else if (Utils.VIDEO_SIZE_1080.equals(videoSize)) {
                    recodingMode = CameraEncoder.CAMERA_START_RECORDER_1080;
                } else {
                    recodingMode = CameraEncoder.CAMERA_START_RECORDER_1080;
                }
            } else if (mCameraMode == Utils.CAMERA_MODE_FULL_CAMERA) {
                recodingMode = CameraEncoder.CAMERA_START_RECORDER_1080;
            }
            mCameraEncoder.sendEmptyMessage(recodingMode);
            mHandler.postDelayed(mCheckMemoryCapacityTask, Utils.CHECK_MEM_CAPACITY_INTERVAL);
        } else {
            mHandler.postDelayed(mCheckMemoryCapacityTask, Utils.CHECK_MEM_CAPACITY_INTERVAL);
        }
    }


    Runnable mCheckMemoryCapacityTask = new Runnable() {
        @Override
        public void run() {
            checkMemoryCapacity();
        }
    };

    private void checkStorage() {
        File USBStorage = null;
        if (mUSBCurrentPath != null) {
            USBStorage = new File(mUSBCurrentPath);
        }
        if (mUSBList.size() == 0 || USBStorage == null) {
            setUSBStatus(false);
        } else if (!isStoragePermissionDenied() && ((!USBStorage.canWrite()) || (!Utils.checkUsbWritablity(mUSBCurrentPath)))) {
            setUSBStatus(false);
            Toast.makeText(this, R.string.read_only, Toast.LENGTH_SHORT).show();
        } else if (!isStoragePermissionDenied() && (Utils.getMemoryCapacity(mUSBCurrentPath) <= 30)) {
            setUSBStatus(false);
            Toast.makeText(this, R.string.memory_little_tip, Toast.LENGTH_SHORT).show();
        } else {
            setUSBStatus(true);
        }
    }

    private void setCameraCaptureButtonStatus() {
        if ((mIsCameraAvailable) && (mIsUSBAvailable)) {
            mIsRecEnable = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mIsRecEnable) {
                        if (mCameraCaptureButton != null) {
                            mCameraCaptureButton.setEnabled(true);
                            mCameraCaptureButton.setTextColor(getResources().getColor(R.color.rec_text_normal));
                        }
                        mIsRecEnable = false;
                    }
                }
            }, 2000);
        } else {
            mIsRecEnable = false;
            if (mCameraCaptureButton != null) {
                mCameraCaptureButton.setEnabled(false);
                mCameraCaptureButton.setTextColor(getResources().getColor(R.color.rec_text_disable));
            }
        }
    }

    private void setCameraCaptureButtonStatus(boolean enable) {

        if (enable) {
            mIsRecEnable = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mIsRecEnable) {
                        if (mCameraCaptureButton != null) {
                            mCameraCaptureButton.setEnabled(true);
                            mCameraCaptureButton.setTextColor(getResources().getColor(R.color.rec_text_normal));
                        }
                        mIsRecEnable = false;
                    }
                }
            }, 2000);
        } else {
            mIsRecEnable = false;
            if (mCameraCaptureButton != null) {
                mCameraCaptureButton.setTextColor(getResources().getColor(R.color.rec_text_disable));
                mCameraCaptureButton.setEnabled(false);
            }
        }
    }

    View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                v.setScaleX(0.8f);
                v.setScaleY(0.8f);
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        }
    };

    public void setUSBStatus(boolean isUSBAvailable) {
        mIsUSBAvailable = isUSBAvailable;
        updateCaptureButtonStatus();
    }

    public void disableCaptureButtonFiercely(boolean isNeedDisableFiercely) {
        mIsNeedDisableFiercely = isNeedDisableFiercely;
        updateCaptureButtonStatus();
    }

    public void updateCaptureButtonStatus() {
        if (!mIsNeedDisableFiercely && mIsCameraAvailable && mIsUSBAvailable) {
            setCameraCaptureButtonStatus(true);
        } else {
            setCameraCaptureButtonStatus(false);
        }
    }

    @Override
    public void notifyCameraStatus(boolean isCameraAvailable) {
        mIsCameraAvailable = isCameraAvailable;
        updateCaptureButtonStatus();
        if (mIsRecording) {
            stopRecorder();
        }
    }

    @Override
    public void notifyUSBStatus(boolean isUSBAvailable) {
        if (mIsRecording && !isUSBAvailable) {
            mHandler.removeCallbacks(mCheckMemoryCapacityTask);
            mCameraCaptureButton.setBackgroundResource(R.drawable.rec_start_button);
            mCameraCaptureButton.setText(R.string.rec_start);
            mIsRecording = !mIsRecording;
            mSettingButton.setFocusable(true);
            mSettingButton.setEnabled(true);
            mSwitchWindowButton.setFocusable(true);
            mSwitchWindowButton.setEnabled(true);
            mSwitchToPhotoButton.setFocusable(true);
            mSwitchToPhotoButton.setEnabled(true);
            mHandler.removeCallbacks(mUpdateRecodingTimeTask);
            mRecordingImage.setVisibility(View.GONE);
            mRecordingTimeTextView.setVisibility(View.GONE);
            mAlbumButton.setEnabled(true);
            mAlbumButton.setFocusable(true);
            /*setUSBStatus(false);*/
        }
    }
}
