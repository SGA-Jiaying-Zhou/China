package com.sony.dtv.tvcamera.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.extension.TvAppExtension;
import com.sony.dtv.tvcamera.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

public class SecCameraIntroActivity extends Activity {
    private static final String TAG = "SecCameraIntroActivity";
    private BroadcastReceiver mFinishReceiver = null;
    private Button mButtonStart;
    private Button mButtonCancel;

    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;

    LinkedList<String> mUSBList;
    private HashMap<String, String> mUSBPath;
    String USB_DEVICE_PATH = "/storage/sda1";
    String USB_DEVICE_SDCARD = "sdcard";
    String USB_DEVICE_FILTER = "/storage/sd";

    private String mUSBCurrentPath;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.security_camera_intro);

        mUSBList = new LinkedList<>();
        mUSBPath = new HashMap<>();

        mButtonStart = (Button) findViewById(R.id.start);

        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");
                boolean isCameraInsert = Utils.checkCameraInsert(getApplicationContext());
                if (!isCameraInsert) {
                    Log.d(TAG, "Camera isn't inserted");
                    Utils.ShowCameraSupportErrorDialog(getApplicationContext(), Utils.getCameraSupportType(), true);
                    return;
                }

                try {
                    mCamera = Camera.open(0);
                } catch (Exception e) {
                    Log.d(TAG, "mCamera open() failed");
                    Utils.ShowCameraSupportErrorDialog(getApplicationContext(), Utils.getCameraSupportType(), true);
                    return;
                }

                if (null != mCamera) {
                    mCamera.release();
                    mCamera = null;
                }

                if (!Utils.checkCameraType(getApplicationContext())) {
                    Log.d(TAG, "mCamera checkCameraType() failed");
                    Utils.ShowCameraSupportErrorDialog(getApplicationContext(), Utils.CAMERA_NOT_COMPATIBLE, true);
                    return;
                }

                getUsbPath(null);
                if (!checkStorage()) {
                    return;
                }

                SharedPreferences sp = getSharedPreferences("securityCamera", 0);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("isNeedStartSecurityCamera", true);
                editor.commit();
                TvAppExtension tvAppExtension = TVCameraApp.getTvAppExtension();
                if (null != tvAppExtension) {
                    tvAppExtension.notifyServiceStatus();
                }
                TVCameraApp.setSecurityCameraStringID(Utils.SECURITY_CAMERA_NO_MESSAGE);
                TVCameraApp.registerScreenActionReceiver(getApplicationContext());
                finish();
            }
        });

        mButtonCancel = (Button) findViewById(R.id.cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mButtonStart.setOnFocusChangeListener(mOnFocusChangeListener);
        mButtonCancel.setOnFocusChangeListener(mOnFocusChangeListener);

        mFinishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                    Log.d(TAG, "ACTION_LOCALE_CHANGED");
                    finish();
                }
            }
        };
        IntentFilter locale_changed_filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);

        registerReceiver(mFinishReceiver, locale_changed_filter);

    }

    View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {

            if (mButtonStart.hasFocus()) {
                mButtonStart.setTextColor(getResources().getColor(R.color.feature_intro_text_focus));
                mButtonCancel.setTextColor(getResources().getColor(R.color.feature_intro_text_no_focus));
                return;
            } else {
                mButtonStart.setTextColor(getResources().getColor(R.color.feature_intro_text_no_focus));
                mButtonCancel.setTextColor(getResources().getColor(R.color.feature_intro_text_focus));
            }

        }
    };

    private boolean checkStorage() {
        boolean isUSBAvailable = false;
        File USBStorage = null;
        int stringId = Utils.SECURITY_CAMERA_NO_MESSAGE;
        if (mUSBCurrentPath != null) {
            USBStorage = new File(mUSBCurrentPath);
        }
        if (mUSBList.size() == 0 || USBStorage == null) {
            isUSBAvailable = false;
            Log.d(TAG, "security_camera_no_usb_dialog");
            stringId = R.string.security_camera_no_usb_dialog;
        } else if ((USBStorage.canWrite() == false) || (!Utils.checkUsbWritablity(mUSBCurrentPath))) {
            isUSBAvailable = false;
            Log.d(TAG, "security_camera_usb_read_only_dialog!");
            stringId = R.string.security_camera_usb_read_only_dialog;
        } else if (Utils.getMemoryCapacity(mUSBCurrentPath) <= Utils.SECURITY_CAMERA_MIN_USB_SIZE) {
            isUSBAvailable = false;
            Log.d(TAG, "security_camera_memory_little_dialog");
            stringId = R.string.security_camera_memory_little_dialog;
        } else {
            isUSBAvailable = true;
        }
        if (!isUSBAvailable) {
            AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext(), AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                    .setMessage(stringId)
                    .setPositiveButton(R.string.app_exit_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }

                    }).create();
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alertDialog.show();
        }
        return isUSBAvailable;
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
    protected void onDestroy() {
        super.onDestroy();
        if (null != mFinishReceiver) {
            unregisterReceiver(mFinishReceiver);
            mFinishReceiver = null;
        }
    }
}
