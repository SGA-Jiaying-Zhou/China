package com.sony.dtv.tvcamera.app;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.permission.ManageCriticalPermissionExtActivity;
import com.sony.dtv.tvcamera.app.permission.PermissionConstants;
import com.sony.dtv.tvcamera.utils.Utils;
import com.sony.dtv.tvinput.provider.SonyTvContract;

import java.util.List;

public class PreMainActivity extends Activity {

    private static final String TAG = "PreMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		/*
         * Lets Load SonyTvPlayer with a Input using tvState interface
		 * Use TvAction as our launcher
		 */
        Log.i(TAG, "Preload Started");

        setContentView(R.layout.activity_pre_load);
        TVCameraApp.registerTerminateKeyReceiver();

        Service cameraRecognitionService = TVCameraApp.getCameraRecognitionService();
        if (null != cameraRecognitionService) {
            cameraRecognitionService.stopSelf();
        }

        Activity photoSettingActivity = TVCameraApp.getPhotoSettingActivity();
        if (null != photoSettingActivity) {
            photoSettingActivity.finish();
        }

        Activity settingActivity = TVCameraApp.getSettingActivity();
        if (null != settingActivity) {
            settingActivity.finish();
        }

        LocalBroadcastManager lBM =
                LocalBroadcastManager.getInstance(getApplicationContext());
        lBM.registerReceiver(stateListener, new IntentFilter(Utils.INTENT_PRE_TV_ACTION_LOADED));

        launchTVPlayer();
    }

    private BroadcastReceiver stateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Got the Finish Action!!");
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(stateListener);
            PreMainActivity.this.finish();
        }

    };

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        startMainActivity();
    }

    public void startMainActivity() {
        if (!TVCameraApp.isTerminateKeyPress()) {
            Intent intent = new Intent(getApplicationContext(), ManageCriticalPermissionExtActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_FROM_BACKGROUND);
            intent.putExtra("launchMode", PermissionConstants.MODE_HOME_CAMERA);
            startActivity(intent);
        } else {
            TVCameraApp.setIsTerminateKeyPress(false);
            TVCameraApp.setIsHomeKeyPress(false);
        }

        finish();
    }

    private void launchTVPlayer() {
        String sortOrder = SonyTvContract.LastWatchedInput.DEFAULT_SORT_ORDER;
        String[] projection = {
                SonyTvContract.LastWatchedInput.INPUT_TYPE,
                SonyTvContract.LastWatchedInput.INPUT_ID};

        Cursor cursor = getApplicationContext().getContentResolver().
                query(SonyTvContract.LastWatchedInput.CONTENT_URI,
                        projection, null, null, sortOrder);
        Uri uri = null;

        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToLast()) {
                int lastWatchedInputType = cursor.getInt(0);
                String lastWatchedInputId = cursor.getString(1);

                if (lastWatchedInputType == SonyTvContract.LastWatchedInput.INPUT_TYPE_TUNER) {
                    uri = TvContract.buildChannelsUriForInput(lastWatchedInputId);
                } else {
                    uri = TvContract
                            .buildChannelUriForPassthroughInput(lastWatchedInputId);
                }
            }
            cursor.close();
        }

        Intent myIntent = null;
        Log.d(TAG, "URI is " + uri);
        if (null != uri) {
            myIntent = new Intent(Intent.ACTION_VIEW, uri);
        } else {
            myIntent = new Intent();
        }
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_FROM_BACKGROUND);

        if (isIntentAvailable(myIntent)) {
            startActivity(myIntent);
        }
    }

    public boolean isIntentAvailable(Intent intent) {
        final PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}