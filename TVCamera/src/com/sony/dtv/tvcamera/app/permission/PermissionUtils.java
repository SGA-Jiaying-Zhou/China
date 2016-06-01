package com.sony.dtv.tvcamera.app.permission;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.sony.dtv.tvcamera.app.MirrorActivity;
import com.sony.dtv.tvcamera.app.PhotoActivity;
import com.sony.dtv.tvcamera.app.SecCameraIntroActivity;
import com.sony.dtv.tvcamera.app.SmileIntroActivity;
import com.sony.dtv.tvcamera.app.TVCameraSettingActivity;
import com.sony.dtv.tvcamera.utils.Utils;

public class PermissionUtils {

    public static final String TAG = "PermissionUtils";

    protected static void allPermissionsGranted(int launchMode, Context context) {
        if (launchMode == PermissionConstants.MODE_HOME_CAMERA) {
            SharedPreferences sp = context.getSharedPreferences("tvcameramode", Context.MODE_PRIVATE);
            int TVCameraMode = sp.getInt("tvcameramodevalue", Utils.CAMERA_MODE);
            Log.d(TAG, "TVCameraMode = " + TVCameraMode);
            Intent myIntent = null;
            if (TVCameraMode == Utils.CAMERA_MODE) {
                myIntent = new Intent().setClass(context, MirrorActivity.class);
            } else {
                myIntent = new Intent().setClass(context, PhotoActivity.class);
            }
            myIntent.putExtra("isLaunchIntent", true);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(myIntent);
            Utils.setIsErrorShowed(false);
        } else if (launchMode == PermissionConstants.MODE_LAUNCH_CAMERA) {
            SharedPreferences sp = context.getSharedPreferences("tvcameramode", 0);
            int TVCameraMode = sp.getInt("tvcameramodevalue", Utils.CAMERA_MODE);
            Log.d(TAG, "TVCameraMode = " + TVCameraMode);
            Intent intent = null;
            if (TVCameraMode == Utils.CAMERA_MODE) {
                intent = new Intent().setClass(context, MirrorActivity.class);
            } else {
                intent = new Intent().setClass(context, PhotoActivity.class);
            }
            intent.putExtra("isLaunchIntent", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } else if (launchMode == PermissionConstants.MODE_START_CAMERA_RECOGNITION) {
            Intent intent = new Intent().setClass(context, SmileIntroActivity.class);
            intent.putExtra("isLaunchFromTVAction", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } else if (launchMode == PermissionConstants.MODE_START_SECURITY_CAMERA) {
            Intent intent = new Intent().setClass(context, SecCameraIntroActivity.class);
            intent.putExtra("isLaunchFromTVAction", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } else if (launchMode == PermissionConstants.MODE_CAMERA_SETTINGS) {
            Intent intent = new Intent().setClass(context, TVCameraSettingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }
}
