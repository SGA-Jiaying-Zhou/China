package com.sony.dtv.tvcamera.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.sony.dtv.tvcamera.app.TVCameraApp;
import com.sony.dtv.tvcamera.utils.Utils;

public class BootReceiver extends BroadcastReceiver {
    private String TAG = "BootReceiver";
    private CameraNotification mCameraNotification;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: action: " + action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_LOCALE_CHANGED)) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                SharedPreferences sp = context.getSharedPreferences("securityCamera", 0);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("isNeedStartSecurityCamera", false);
                editor.commit();
                TVCameraApp.setSecurityCameraStringID(Utils.SECURITY_CAMERA_NO_MESSAGE);
            }

            if (Utils.getCameraSupportType() != Utils.CAMERA_NOT_SUPPORT) {
                mCameraNotification = new CameraNotification(context);
                mCameraNotification.cancelRecommendation();
                mCameraNotification.buildRecommendationPicture(CameraNotification.CAMERA_NOTIFY_MIRROR);
            }
        }
    }
}
