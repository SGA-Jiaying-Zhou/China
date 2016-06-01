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
import android.view.View;

import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.settings.ActionBehavior;
import com.sony.dtv.tvcamera.app.settings.ActionKey;
import com.sony.dtv.tvcamera.app.settings.BaseSettingsActivity;
import com.sony.dtv.tvcamera.app.settings.dialog.Action;
import com.sony.dtv.tvcamera.app.settings.dialog.ActionAdapter;
import com.sony.dtv.tvcamera.utils.Utils;

import java.util.HashMap;
import java.util.LinkedList;

public class TVCameraSettingActivity extends BaseSettingsActivity implements
        ActionAdapter.Listener {

    private String TAG = "TVCameraSettingActivity";

    LinkedList<String> mUSBList;
    private HashMap<String, String> mUSBPath;
    String USB_DEVICE_PATH = "/storage/sda1";
    String USB_DEVICE_SDCARD = "sdcard";
    String USB_DEVICE_FILTER = "/storage/sd";

    private String mUSBCurrentPath;

    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;

    private USBDeviceBroadcastReceiver mUSBDeviceBroadcastReceiver = null;
    BroadcastReceiver mTvActionReceiver = null;

    private class USBDeviceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            Log.d(TAG, "action..." + action);
            String action_path = intent.getData().getPath();
            Log.d(TAG, "action_path..." + action_path);
            getUsbPath(USB_DEVICE_PATH);
        }
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
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        TVCameraApp.registerTerminateKeyReceiver();

        mUSBList = new LinkedList<>();
        mUSBPath = new HashMap<>();

        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        mUSBDeviceBroadcastReceiver = new USBDeviceBroadcastReceiver();
        registerReceiver(mUSBDeviceBroadcastReceiver, filter);

        getUsbPath(null);

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
        showCustomHint(View.VISIBLE, R.string.tv_camera_setting_hit);
    }

    @Override
    public void onActionClicked(Action action) {

        /*
         * For regular states
         */
        ActionKey<ActionType, ActionBehavior> actionKey = new ActionKey<>(
                ActionType.class, ActionBehavior.class, action.getKey());
        mState = actionKey.getType();

        final ActionBehavior behavior = actionKey.getBehavior();
        if (behavior == null) {
            return;
        }

        /*
         * For list preferences
         */
        switch ((ActionType) mState) {
            case VIDEO_SETTINGS:
                Log.d(TAG, "onActionClicked()------case VIDEO_SETTINGS:");
                Intent startVideoSetting = new Intent(getApplicationContext(),
                        SettingActivity.class);

                startActivity(startVideoSetting);
                return;

            case PHOTO_SETTINGS:
                Log.d(TAG, "onActionClicked()------case PHOTO_SETTINGS:");
                Intent startPhotoSetting = new Intent(getApplicationContext(),
                        PhotoSettingActivity.class);
                startPhotoSetting.putExtra("isLaunchIntent", true);

                startActivity(startPhotoSetting);
                return;

            default:
                break;
        }
    }

    @Override
    protected Object getInitialState() {
        return ActionType.TV_CAMERA_SETTINGS;
    }

    @Override
    protected void refreshActionList() {
        mActions.clear();

        switch ((ActionType) mState) {
            case TV_CAMERA_SETTINGS:
                Log.d(TAG, "refreshActionList------case VIDEO_SETTINGS");

                mActions.add(ActionType.VIDEO_SETTINGS.toAction(mResources,
                        getString(R.string.video_setting_sub_content)));

                mActions.add(ActionType.PHOTO_SETTINGS.toAction(mResources,
                        getString(R.string.photo_setting_sub_content)));

                break;

            default:
                break;
        }
    }

    @Override
    protected void updateView() {
        refreshActionList();
        switch ((ActionType) mState) {
            case TV_CAMERA_SETTINGS:
                Log.d(TAG, "updateView()------case TV_CAMERA_SETTINGS");
                setView(R.string.tv_camera_settings, R.string.settings_app_name,
                        R.string.settings_app_name, R.drawable.settings_video);
                break;

            default:
                break;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterTvActionReceiver();
        if (null != mUSBDeviceBroadcastReceiver) {
            unregisterReceiver(mUSBDeviceBroadcastReceiver);
            mUSBDeviceBroadcastReceiver = null;
        }
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

        if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                && (event.getAction() == KeyEvent.ACTION_DOWN)) {
            finish();
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }
}
