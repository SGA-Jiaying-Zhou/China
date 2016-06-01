package com.sony.dtv.tvcamera.app.permission;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.TVCameraApp;
import com.sony.dtv.tvcamera.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;


public class ManageCriticalPermissionExtActivity extends Activity {

    private static final String TAG = "ManageCritical";

    private int mLaunchMode = 0;

    HashSet<String> dontAskAgainSet = new HashSet<>();

    BroadcastReceiver mTvActionReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        TVCameraApp.registerTerminateKeyReceiver();

        Intent intent = getIntent();
        if (intent != null) {
            mLaunchMode = intent.getIntExtra("launchMode", 0);
        }

        registerTvActionReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        startCheckPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, permissions[i] + ":granted");
            } else {
                Log.v(TAG, permissions[i] + ":denied");
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    Log.v(TAG, "onRequestPermissionsResult shouldShowRequestPermissionRationale(" + permissions[i] + "):false");
                    dontAskAgainSet.add(permissions[i]);
                } else {
                    Log.v(TAG, "onRequestPermissionsResult shouldShowRequestPermissionRationale(" + permissions[i] + "):true");
                }
            }
        }
        saveState();

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                closeApp();
                return;
            }
        }
        allPermissionsGranted();
    }

    protected void allPermissionsGranted() {
        Log.d(TAG, "allPermissionsGranted()");

        PermissionUtils.allPermissionsGranted(mLaunchMode, this);
        finish();
    }

    private boolean isCameraPermissionDenied() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED;
    }

    private boolean isRecordAudioPermissionDenied() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED;
    }

    private boolean isStoragePermissionDenied() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED;
    }

    protected void startCheckPermission() {
        Log.d(TAG, "startCheckPermission()");

        loadState();
        ArrayList<String> list = new ArrayList<>();
        HashSet<String> dontAskAgainSetTemp = new HashSet<>();
        dontAskAgainSetTemp.addAll(dontAskAgainSet);

        if (isCameraPermissionDenied()) {  // four mode both need the camera
            Log.v(TAG, "CAMERA is not granted");
            list.add(Manifest.permission.CAMERA);
        } else {
            dontAskAgainSet.remove(Manifest.permission.CAMERA);
            dontAskAgainSetTemp.remove(Manifest.permission.CAMERA);
        }

        if (isRecordAudioPermissionDenied()) {
            if (mLaunchMode == PermissionConstants.MODE_HOME_CAMERA || mLaunchMode == PermissionConstants.MODE_LAUNCH_CAMERA || mLaunchMode == PermissionConstants.MODE_START_SECURITY_CAMERA) {
                Log.v(TAG, "RECORD_AUDIO is not granted");
                list.add(Manifest.permission.RECORD_AUDIO);
            } else {
                dontAskAgainSetTemp.remove(Manifest.permission.RECORD_AUDIO);
            }
        } else {
            dontAskAgainSet.remove(Manifest.permission.RECORD_AUDIO);
            dontAskAgainSetTemp.remove(Manifest.permission.RECORD_AUDIO);
        }

        if (isStoragePermissionDenied()) {
            if (mLaunchMode == PermissionConstants.MODE_START_CAMERA_RECOGNITION || mLaunchMode == PermissionConstants.MODE_START_SECURITY_CAMERA) {
                Log.v(TAG, "WRITE_EXTERNAL_STORAGE is not granted");
                list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                list.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                dontAskAgainSetTemp.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                dontAskAgainSetTemp.remove(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            dontAskAgainSet.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            dontAskAgainSet.remove(Manifest.permission.READ_EXTERNAL_STORAGE);

            dontAskAgainSetTemp.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            dontAskAgainSetTemp.remove(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        saveState();
        requestPermission(list.toArray(new String[list.size()]), dontAskAgainSetTemp);
    }

    private void requestPermission(String[] permissionList, HashSet<String> dontAskAgainSet) {
        if (permissionList.length == 0) {
            allPermissionsGranted();
            return;
        }

        Log.i(TAG, "requestPermissions GrantPermissionsActivity");

        if (dontAskAgainSet.size() > 0) {
            showPermissionPromptActivity();
            finish();
        } else {
            ActivityCompat.requestPermissions(this, permissionList, 0);
        }

    }

    private void showPermissionPromptActivity() {
        Log.d(TAG, "showPermissionPromptActivity()");

        Intent intent = new Intent(this, CriticalPermissionPromptActivity.class);
        String name = "";
        String[] permissions = new String[]{};

        PackageManager pm = getPackageManager();
        String permissionInfo = "";
        try {
            PermissionGroupInfo cameraGroupInfo = pm.getPermissionGroupInfo(Manifest.permission_group.CAMERA, 0);
            PermissionGroupInfo microphoneGroupInfo = pm.getPermissionGroupInfo(Manifest.permission_group.MICROPHONE, 0);
            PermissionGroupInfo storageGroupInfo = pm.getPermissionGroupInfo(Manifest.permission_group.STORAGE, 0);

            if (mLaunchMode == PermissionConstants.MODE_HOME_CAMERA || mLaunchMode == PermissionConstants.MODE_LAUNCH_CAMERA) {
                name = getString(R.string.app_name);
                permissionInfo = String.valueOf(cameraGroupInfo.loadLabel(pm)) + '\n' + String.valueOf(microphoneGroupInfo.loadLabel(pm));
                permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
            } else if (mLaunchMode == PermissionConstants.MODE_START_CAMERA_RECOGNITION) {
                name = getString(R.string.smile_shutter);
                permissionInfo = String.valueOf(storageGroupInfo.loadLabel(pm)) + '\n' + String.valueOf(cameraGroupInfo.loadLabel(pm));
                permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            } else if (mLaunchMode == PermissionConstants.MODE_START_SECURITY_CAMERA) {
                name = getString(R.string.security_camera_title);
                permissionInfo = String.valueOf(storageGroupInfo.loadLabel(pm)) + '\n' +
                        String.valueOf(cameraGroupInfo.loadLabel(pm)) + '\n' +
                        String.valueOf(microphoneGroupInfo.loadLabel(pm));
                permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            } else if (mLaunchMode == PermissionConstants.MODE_CAMERA_SETTINGS) {
                name = getString(R.string.camera_settings);
                permissionInfo = String.valueOf(cameraGroupInfo.loadLabel(pm));
                permissions = new String[]{Manifest.permission.CAMERA};
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        intent.putExtra(PermissionConstants.NAME_INFO, name);
        intent.putExtra(PermissionConstants.PERMISSION_INFO, permissionInfo);
        intent.putExtra(PermissionConstants.PERMISSIONS_KEY, permissions);
        intent.putExtra(PermissionConstants.LAUNCH_MODE_KEY, mLaunchMode);
        startActivity(intent);
    }

    private void closeApp() {
        Intent closeAppIntent = new Intent();
        closeAppIntent.setAction(Utils.INTENT_ACTION_CLOSE_APP);
        sendBroadcast(closeAppIntent);
    }

    private void saveState() {
        SharedPreferences sharedPreferences = getSharedPreferences("Permission", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        for (String permission : dontAskAgainSet) {
            editor.putBoolean(permission, true);
        }
        editor.commit();
    }

    private void loadState() {
        dontAskAgainSet.clear();
        SharedPreferences sharedPreferences = getSharedPreferences("Permission", 0);
        for (String permission : sharedPreferences.getAll().keySet()) {
            dontAskAgainSet.add(permission);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        unregisterTvActionReceiver();
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
}
