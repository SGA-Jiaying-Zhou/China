/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sony.dtv.tvcamera.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.settings.ActionBehavior;
import com.sony.dtv.tvcamera.app.settings.ActionKey;
import com.sony.dtv.tvcamera.app.settings.BaseSettingsActivity;
import com.sony.dtv.tvcamera.app.settings.dialog.Action;
import com.sony.dtv.tvcamera.app.settings.dialog.ActionAdapter;
import com.sony.dtv.tvcamera.utils.SettingUtil;
import com.sony.dtv.tvcamera.utils.Utils;

public class SettingActivity extends BaseSettingsActivity implements
        ActionAdapter.Listener {

    private String mKey;
    private String mTitle;
    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;
    private String TAG = "SettingActivity";
    private Action mDestinationAction;
    private int usb_count;
    private boolean mIsReturnPressed = false;
    BroadcastReceiver mTvActionReceiver = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        TVCameraApp.setSettingActivity(this);
        mIsReturnPressed = false;
        IntentFilter tvActionfilters = new IntentFilter();
        tvActionfilters.addAction(Utils.INTENT_ACTION_CLOSE_APP);
        if (mTvActionReceiver == null) {
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
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onActionClicked(Action action) {
        /*
         * For list preferences
         */
        switch ((ActionType) mState) {
            case VIDEO_SIZE:
                Log.d(TAG, "onActionClicked()------case VIDEO_SIZE:");
                mSettings = getSharedPreferences("setting", 0);
                String video_size_key = mSettings.getString("video_size_key", "");
                SharedPreferences old_video_size_key = getSharedPreferences("old_size", 0);
                mEditor = old_video_size_key.edit();
                mEditor.putString("key_values", video_size_key);
                mEditor.commit();
                mKey = action.getKey();
                mTitle = action.getTitle();
                mEditor = mSettings.edit();
                mEditor.putString("video_size_key", mKey);
                mEditor.putString("video_size_title", mTitle);
                mEditor.commit();
                goBack();
                return;

            case VIDEO_QUALITY:
                Log.d(TAG, "onActionClicked()------case VIDEO_QUALITY:");
                mKey = action.getKey();
                mTitle = action.getTitle();
                mEditor = mSettings.edit();
                mEditor.putString("video_quality_key", mKey);
                mEditor.putString("video_quality_title", mTitle);
                mEditor.commit();
                goBack();
                return;

            case VIDEO_DESTINATION:
                Log.d(TAG, "onActionClicked()------case VIDEO_DESTINATION:");
                mKey = action.getKey();
                mTitle = action.getTitle();
                mEditor = mSettings.edit();
                mEditor.putString("destination_key", mKey);
                mEditor.putString("destination_title", mTitle);
                mEditor.commit();
                goBack();
                return;
            default:
                break;
        }

        /*
         * For regular states
         */
        ActionKey<ActionType, ActionBehavior> actionKey = new ActionKey<>(
                ActionType.class, ActionBehavior.class, action.getKey());
        final ActionType type = actionKey.getType();

        final ActionBehavior behavior = actionKey.getBehavior();
        if (behavior == null) {
            return;
        }

        switch (behavior) {
            case INIT:
                Log.d(TAG, "onActionClicked()------case INIT:");
                setState(type, true);
                break;
            case ON:
                setProperty(true);
                break;
            case OFF:
                setProperty(false);
                break;
            default:
        }

    }

    @Override
    protected Object getInitialState() {
        return ActionType.VIDEO_SETTINGS;
    }

    @Override
    protected void refreshActionList() {
        mActions.clear();


        switch ((ActionType) mState) {
            case VIDEO_SETTINGS:
                Log.d(TAG, "refreshActionList------case VIDEO_SETTINGS");
                mSettings = getSharedPreferences("setting", 0);
                String keyVideoSize = mSettings.getString("video_size_key", "");
                String keyVideoQuality = mSettings.getString("video_quality_key", "");
                String keyDestination = mSettings.getString("destination_key", "");

                String currentVideoSize = null;
                if (!TextUtils.equals(keyVideoSize, "")) {
                    currentVideoSize = mSettings.getString("video_size_title", "");
                } else {
                    currentVideoSize = "1920 x 1080";
                    mEditor = mSettings.edit();
                    mEditor.putString("video_size_key", Utils.VIDEO_SIZE_1080);
                    mEditor.putString("video_size_title", "1920 x 1080");
                    mEditor.commit();
                }
                String currentVideoQuality = null;
                mEditor = mSettings.edit();
                SharedPreferences old_size = getSharedPreferences("old_size", 0);
                SettingUtil.refreshQuality();

                if (TextUtils.equals(keyVideoQuality, Utils.VIDEO_QUALITY_HIGH)) {
                    currentVideoQuality = mSettings.getString("video_quality_title",
                            "");
                } else if (TextUtils.equals(keyVideoQuality, Utils.VIDEO_QUALITY_MIDDLE)) {
                    currentVideoQuality = mSettings.getString("video_quality_title",
                            "");
                } else if (TextUtils.equals(keyVideoQuality, Utils.VIDEO_QUALITY_LOW)) {
                    currentVideoQuality = mSettings.getString("video_quality_title",
                            "");
                } else {
                    String tenMbps = "(10Mbps)";
                    currentVideoQuality = getString(R.string.video_quality_high) + tenMbps;

                    mEditor = mSettings.edit();
                    mEditor.putString("video_quality_key", Utils.VIDEO_QUALITY_HIGH);
                    mEditor.putString("video_quality_title", currentVideoQuality);
                    mEditor.commit();
                }
                SharedPreferences lSettings = getSharedPreferences("usb", 0);
                usb_count = lSettings.getInt("usb_count", 0);
                String currentDestination = null;

                if (usb_count == 1 && keyDestination == null) {

                    currentDestination = "USB1";
                    mEditor = mSettings.edit();
                    mEditor.putString("destination_key", "USB1");
                    mEditor.putString("destination_title", "USB1");
                    mEditor.commit();
                    mDestinationAction.setEnabled(false);
                } else if (usb_count == 0) {

                    currentDestination = "";
                    mEditor = mSettings.edit();
                    mEditor.putString("destination_key", "");
                    mEditor.putString("destination_title", "");
                    mEditor.commit();

                }
                if (!TextUtils.equals(keyDestination, "")) {
                    currentDestination = mSettings.getString("destination_title", "");
                }

                mActions.add(ActionType.VIDEO_SIZE.toAction(mResources,
                        currentVideoSize));

                mActions.add(ActionType.VIDEO_QUALITY.toAction(mResources,
                        currentVideoQuality));

                if (usb_count == 0 || usb_count == 1) {
                    mDestinationAction = ActionType.VIDEO_DESTINATION.toAction(mResources, currentDestination);
                    mDestinationAction.setEnabled(false);
                    mActions.add(mDestinationAction);
                } else if (usb_count > 1) {
                    mActions.add(ActionType.VIDEO_DESTINATION.toAction(mResources,
                            currentDestination));
                }

                break;
            case VIDEO_SIZE:
                Log.d(TAG, "refreshActionList------case VIDEO_SIZE");

                String[] sKeys = {Utils.VIDEO_SIZE_1080, Utils.VIDEO_SIZE_720, Utils.VIDEO_SIZE_360};
                String[] sTitle = {"1920 x 1080", "1280 x 720", "640 x 360"};
                mActions = Action.createActionsFromArrays(sKeys, sTitle);

                for (Action action : mActions) {
                    action.setChecked(action.getKey().equals(
                            mSettings.getString("video_size_key", "")));

                    SharedPreferences CameraMode = getSharedPreferences("cameramode", Utils.CAMERA_MODE_PAP_1);
                    int CameraModeValue = CameraMode.getInt("cameramodevalue", Utils.CAMERA_MODE_PAP_1);
                    if (CameraModeValue == Utils.CAMERA_MODE_FULL_CAMERA) {
                        action.setEnabled(action.getKey().equals(Utils.VIDEO_SIZE_1080));
                    }

                }

                break;

            case VIDEO_QUALITY:
                String[] qKeys;
                String[] qTitle = null;
                String VIDEO_SIZE_1080_BPS_HIGH = "(10Mbps)";
                String VIDEO_SIZE_720_BPS_HIGH = "(8Mbps)";
                String VIDEO_SIZE_360_BPS_HIGH = "(1.2Mbps)";
                String VIDEO_SIZE_1080_BPS_MIDDLE = "(3.3Mbps)";
                String VIDEO_SIZE_720_BPS_MIDDLE = "(2.7Mbps)";
                String VIDEO_SIZE_360_BPS_MIDDLE = "(400kbps)";
                String VIDEO_SIZE_1080_BPS_LOW = "(1.25Mbps)";
                String VIDEO_SIZE_720_BPS_LOW = "(1Mbps)";
                String VIDEO_SIZE_360_BPS_LOW = "(150kbps)";
                Log.d(TAG, "refreshActionList------case VIDEO_QUALITY");
                mSettings = getSharedPreferences("setting", 0);
                String key_Video_Size = mSettings.getString("video_size_key", "");
                qKeys = new String[]{Utils.VIDEO_QUALITY_HIGH, Utils.VIDEO_QUALITY_MIDDLE, Utils.VIDEO_QUALITY_LOW};

                if (TextUtils.equals(key_Video_Size, Utils.VIDEO_SIZE_1080)) {
                    qTitle = new String[]{getString(R.string.video_quality_high) + VIDEO_SIZE_1080_BPS_HIGH, getString(R.string.video_quality_middle) + VIDEO_SIZE_1080_BPS_MIDDLE, getString(R.string.video_quality_low) + VIDEO_SIZE_1080_BPS_LOW};
                } else if (TextUtils.equals(key_Video_Size, Utils.VIDEO_SIZE_720)) {
                    qTitle = new String[]{getString(R.string.video_quality_high) + VIDEO_SIZE_720_BPS_HIGH, getString(R.string.video_quality_middle) + VIDEO_SIZE_720_BPS_MIDDLE, getString(R.string.video_quality_low) + VIDEO_SIZE_720_BPS_LOW};
                } else if (TextUtils.equals(key_Video_Size, Utils.VIDEO_SIZE_360)) {
                    qTitle = new String[]{getString(R.string.video_quality_high) + VIDEO_SIZE_360_BPS_HIGH, getString(R.string.video_quality_middle) + VIDEO_SIZE_360_BPS_MIDDLE, getString(R.string.video_quality_low) + VIDEO_SIZE_360_BPS_LOW};
                }
                mActions = Action.createActionsFromArrays(qKeys, qTitle);
                for (Action action : mActions) {
                    action.setChecked(action.getKey().equals(
                            mSettings.getString("video_quality_key", "")));
                }
                break;
            case VIDEO_DESTINATION:
                Log.d(TAG, "refreshActionList------case VIDEO_DESTINATION");

                mSettings = getSharedPreferences("usb", 0);
                usb_count = mSettings.getInt("usb_count", 0);

                String[] dKeys = new String[usb_count];
                String[] dTitle = new String[usb_count];

                for (int i = 1; i <= usb_count; i++) {
                    dKeys[i - 1] = "USB" + i;
                    dTitle[i - 1] = "USB" + i;
                }

                mActions = Action.createActionsFromArrays(dKeys, dTitle);

                mSettings = getSharedPreferences("setting", 0);
                for (Action action : mActions) {
                    action.setChecked(action.getKey().equals(
                            mSettings.getString("destination_key", "")));
                }
            default:
                break;

        }
    }

    @Override
    protected void updateView() {
        refreshActionList();
        switch ((ActionType) mState) {
            case VIDEO_SETTINGS:
                Log.d(TAG, "updateView()------case VIDEO_SETTINGS");
                setView(R.string.video_settings, R.string.settings_app_name, R.string.help_text,
                        R.drawable.settings_video);
                break;
            case VIDEO_SIZE:
                Log.d(TAG, "updateView()------case VIDEO_SIZE");
                setView(R.string.title_video_size, R.string.video_settings, 0,
                        R.drawable.settings_video);
                break;

            case VIDEO_QUALITY:
                Log.d(TAG, "updateView()------ case VIDEO_QUALITY");
                setView(R.string.title_video_quality, R.string.video_settings, 0,
                        R.drawable.settings_video);
                break;

            case VIDEO_DESTINATION:
                Log.d(TAG, "updateView()------case VIDEO_DESTINATION");
                setView(R.string.title_video_destination, R.string.video_settings,
                        0, R.drawable.settings_video);
                break;
            default:
                break;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterTvActionReceiver();
        TVCameraApp.setSettingActivity(null);
    }

    private void unregisterTvActionReceiver() {
        if (null != mTvActionReceiver) {
            unregisterReceiver(mTvActionReceiver);
            mTvActionReceiver = null;
        }
    }

    @Override
    protected void setProperty(boolean enable) {
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // TODO Auto-generated method stub
        Log.v(TAG, "keyEvent:" + event);

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                mIsReturnPressed = true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "mIsReturnPressed = " + mIsReturnPressed);
        if (!mIsReturnPressed) {
            Log.i(TAG, "send closeAppIntent!");
            Intent closeAppIntent = new Intent();
            closeAppIntent.setAction(Utils.INTENT_ACTION_CLOSE_APP);
            sendBroadcast(closeAppIntent);
            finish();
        } else {
            mIsReturnPressed = false;
        }
    }
}
