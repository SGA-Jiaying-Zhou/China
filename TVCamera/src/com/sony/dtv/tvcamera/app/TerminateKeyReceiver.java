package com.sony.dtv.tvcamera.app;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sony.dtv.tvcamera.utils.Utils;

import java.util.List;

public class TerminateKeyReceiver extends BroadcastReceiver {
    private static final String TAG = "TerminateKeyReceiver";
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private static final String SYSTEM_DIALOG_REASON_SEARCH_KEY = "search";

    private static final String HOME_PACKAGE = "com.google.android.leanbacklauncher";

    public static final String ACTION_GUIDE = "com.sony.dtv.intent.action.GUIDE";
    public static final String ACTION_HELP = "com.sony.dtv.intent.action.HELP";
    public static final String ACTION_SCREEN_OFF = Intent.ACTION_SCREEN_OFF;

    private Context mContext = null;

    private final Handler mHandler = new Handler();
    private final Runnable mTerminalTask = new Runnable() {
        public void run() {
            Log.i(TAG, "mTerminalTask run!");
            Log.i(TAG, "send closeAppIntent!");
            Intent closeAppIntent = new Intent();
            closeAppIntent.setAction(Utils.INTENT_ACTION_CLOSE_APP);
            mContext.sendBroadcast(closeAppIntent);

            if (!isTopActivity(HOME_PACKAGE)) {
                Log.i(TAG, "launch home intent!");
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(startMain);
            }
            mContext = null;
            mHandler.removeCallbacks(mTerminalTask);
        }
    };

    private boolean isTopActivity(String packageName) {
        ActivityManager activityManager = (ActivityManager) mContext.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        if (list.size() == 0) return false;
        for (ActivityManager.RunningAppProcessInfo process : list) {
            Log.d(TAG, Integer.toString(process.importance));
            Log.d(TAG, process.processName);
            if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    process.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public void closePreActivity(Context context) {
        Intent LoadIntent = new Intent(Utils.INTENT_PRE_TV_ACTION_LOADED);
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(LoadIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Log.i(TAG, "onReceive: intent: " + action);
        mContext = context;
        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            Log.i(TAG, "reason: " + reason);

            if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)
                    || SYSTEM_DIALOG_REASON_SEARCH_KEY.equals(reason)) {
                Log.i(TAG, "homekey");
                TVCameraApp.setIsHomeKeyPress(true);
                exit(context);
/*                mHandler.removeCallbacks(mTerminalTask);
                long delay = 0;
                Log.d(TAG, "App.isNeedDelay() = " + App.isNeedDelay());
*//*                if(App.isNeedDelay()){
                    Log.d(TAG, "isNeedDelay!");
                    delay = 3000;
                }*//*
                mHandler.postDelayed(mTerminalTask, delay);*/
            }
        } else if ((ACTION_GUIDE.equals(action))
                || (ACTION_HELP.equals(action))
                || (ACTION_SCREEN_OFF.equals(action))) {
            exit(context);
        }
    }

    private void exit(Context context) {
        TVCameraApp.unregisterTerminateKeyReceiver();
        TVCameraApp.setIsTerminateKeyPress(true);

        Log.i(TAG, "send closeAppIntent!");
        Intent closeAppIntent = new Intent();
        closeAppIntent.setAction(Utils.INTENT_ACTION_CLOSE_APP);
        context.sendBroadcast(closeAppIntent);

        closePreActivity(context);
    }
}
