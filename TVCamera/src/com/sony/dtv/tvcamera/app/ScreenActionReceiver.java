package com.sony.dtv.tvcamera.app;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.sony.dtv.tvcamera.utils.Utils;

public class ScreenActionReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenActionReceiver";
    private static boolean mIsHaveReceivePowerOff = false;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "onReceive: action: " + action);

        if (action.equals(Intent.ACTION_SCREEN_ON)) {
            Log.i(TAG, "Stopping SecurityCameraService");
            Intent myIntent = new Intent().setClass(context, SecurityCameraService.class);
            context.stopService(myIntent);
            Log.i(TAG, "unBind done");

            if (mIsHaveReceivePowerOff) {
                TVCameraApp.unregisterScreenActionReceiver(context);
                mIsHaveReceivePowerOff = false;

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int stringID = TVCameraApp.getSecurityCameraStringID();
                        Log.d(TAG, "stringID = " + stringID);
                        if (stringID != Utils.SECURITY_CAMERA_NO_MESSAGE) {
                            Log.d(TAG, "Toast stringID = " + stringID);
                            Toast.makeText(context, stringID, Toast.LENGTH_LONG).show();
                        }
                    }
                }, 5000);
            }
        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            mIsHaveReceivePowerOff = true;
            Service cameraRecognitionService = TVCameraApp.getCameraRecognitionService();
            if (null != cameraRecognitionService) {
                cameraRecognitionService.stopSelf();
                TVCameraApp.setCameraRecognitionService(null);
            }

            Intent myIntent = new Intent().setClass(context, SecurityCameraService.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND);
            if (mIsHaveReceivePowerOff) {
                context.startService(myIntent);
            }

            Log.i(TAG, "Bind done");
        }
    }
}
