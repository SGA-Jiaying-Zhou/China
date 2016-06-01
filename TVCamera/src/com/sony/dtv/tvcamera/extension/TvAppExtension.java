package com.sony.dtv.tvcamera.extension;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.sony.dtv.scrums.action.libtvdotactionextension.TvDotActionExtensionProvider;
import com.sony.dtv.tvcamera.app.permission.ManageCriticalPermissionExtActivity;
import com.sony.dtv.tvcamera.app.permission.PermissionConstants;
import com.sony.dtv.tvcamera.app.ServiceStatusListener;
import com.sony.dtv.tvcamera.app.TVCameraApp;
import com.sony.dtv.tvcamera.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class TvAppExtension extends TvDotActionExtensionProvider implements ServiceStatusListener {

    private final List<String> mCurrentStateList = new ArrayList<>();
    private String TAG = "TvAppExtension";

    public static final String ACTION_LAUNCH_CAMERA = "launch_camera";
    public static final String ACTION_START_CAMERA_RECOGNITION = "start_camera_recognition";
    public static final String ACTION_STOP_CAMERA_RECOGNITION = "close_camera_recognition";
    public static final String ACTION_START_SECURITY_CAMERA = "start_security_camera";
    public static final String ACTION_STOP_SECURITY_CAMERA = "stop_security_camera";
    public static final String ACTION_CAMERA_SETTINGS = "camera_settings";

    @Override
    protected void dispatchActionEvent(String actionName, List<String> list) {
        // TODO Auto-generated method stub
        Log.v(TAG, "actionName:" + actionName);
        Log.v(TAG, "list:" + list);

        Utils.setIsErrorShowed(false);

        Service cameraRecognitionService = TVCameraApp.getCameraRecognitionService();
        if (actionName.equals(ACTION_LAUNCH_CAMERA)) {
            if (null != cameraRecognitionService) {
                cameraRecognitionService.stopSelf();
                TVCameraApp.setCameraRecognitionService(null);
            }
            TVCameraApp.setIsTerminateKeyPress(false);
            TVCameraApp.setIsHomeKeyPress(false);

            Intent intent = new Intent(this, ManageCriticalPermissionExtActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("launchMode", PermissionConstants.MODE_LAUNCH_CAMERA);
            startActivity(intent);
        } else if (actionName.equals(ACTION_START_CAMERA_RECOGNITION)) {
            if (null == cameraRecognitionService) {
                Intent intent = new Intent(this, ManageCriticalPermissionExtActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("launchMode", PermissionConstants.MODE_START_CAMERA_RECOGNITION);
                startActivity(intent);
            }
        } else if (actionName.equals(ACTION_STOP_CAMERA_RECOGNITION)) {
            if (null != cameraRecognitionService) {
                Log.d(TAG, "Stopping Camera Recognition Service");
                cameraRecognitionService.stopSelf();
            }
        } else if (actionName.equals(ACTION_START_SECURITY_CAMERA)) {
            Intent intent = new Intent(this, ManageCriticalPermissionExtActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("launchMode", PermissionConstants.MODE_START_SECURITY_CAMERA);
            startActivity(intent);
        } else if (actionName.equals(ACTION_STOP_SECURITY_CAMERA)) {
            SharedPreferences sp = getSharedPreferences("securityCamera", 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isNeedStartSecurityCamera", false);
            editor.commit();
            updateCurrentStatus();
            TVCameraApp.unregisterScreenActionReceiver(getApplicationContext());
        } else if (actionName.equals(ACTION_CAMERA_SETTINGS)) {
            if (null != cameraRecognitionService) {
                cameraRecognitionService.stopSelf();
                TVCameraApp.setCameraRecognitionService(null);
            }

            Intent intent = new Intent(this, ManageCriticalPermissionExtActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("launchMode", PermissionConstants.MODE_CAMERA_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    protected List<String> getCurrentStateList() {
        Log.d(TAG, "getCurrentStateList");
        return mCurrentStateList;
    }

    @Override
    protected void onBindFromTvDotAction(Intent intent) {
        Log.d(TAG, "onBindFromTvDotAction");
        super.onBindFromTvDotAction(intent);

        TVCameraApp.setTvAppExtension(this);

        if (Utils.getCameraSupportType() == Utils.CAMERA_NOT_SUPPORT) {
            return;
        }

        updateCurrentStatus();
    }

    @Override
    protected void onUnbindFromTvDotAction(Intent intent) {
        Log.d(TAG, "onUnbindFromTvDotAction");
        super.onUnbindFromTvDotAction(intent);
        TVCameraApp.setTvAppExtension(null);
    }

    @Override
    public void notifyServiceStatus() {
        updateCurrentStatus();
    }

    private void updateCurrentStatus() {
        mCurrentStateList.clear();
        SharedPreferences sp = getSharedPreferences("securityCamera", 0);
        boolean isNeedStartSecurityCamera = sp.getBoolean("isNeedStartSecurityCamera", false);

        if (null != TVCameraApp.getCameraRecognitionService()) {
            if (isNeedStartSecurityCamera) {
                mCurrentStateList.add("SM0SC0");
            } else {
                mCurrentStateList.add("SM0SC1");
            }
        } else {
            if (isNeedStartSecurityCamera) {
                mCurrentStateList.add("SM1SC0");
            } else {
                mCurrentStateList.add("SM1SC1");
            }
        }

        notifyCurrentStateList(mCurrentStateList);
    }
}
