package com.sony.dtv.tvcamera.app;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.photosetting.PhotoSettingConstants;

public class SmileIntroActivity extends Activity {
    private static final String TAG = "SmileIntroActivity";
    private BroadcastReceiver mFinishReceiver = null;
    private Button mButtonSettings;
    private Button mButtonStart;
    private Button mButtonCancel;
    private boolean mIsLaunchFromTVAction = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smile_intro);

        Intent intent = getIntent();
        mIsLaunchFromTVAction  = intent.getBooleanExtra("isLaunchFromTVAction", false);
        Log.d(TAG, "mIsLaunchFromTVAction = " + mIsLaunchFromTVAction);

        mButtonSettings =(Button)findViewById(R.id.enter);
        mButtonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SmileIntroActivity.this, PhotoSettingActivity.class);
                if (mIsLaunchFromTVAction) {
                    intent.putExtra("isLaunchFromTVAction", mIsLaunchFromTVAction);
                }
                intent.putExtra("isLaunchIntent", true);
                startActivity(intent);
                finish();
            }
        });

        mButtonStart = (Button) findViewById(R.id.start);

        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences smileShutter = getSharedPreferences(PhotoSettingConstants.SP_NAME, Context.MODE_PRIVATE);
                String smileShutterKey = smileShutter.getString(PhotoSettingConstants.SmileShutterKey, getString(R.string.smile_shutter_defValue));
                Log.d(TAG, "smileShutterKey = " + smileShutterKey);
                if (smileShutterKey.equals("On")) {
                    Service cameraRecognitionService = TVCameraApp.getCameraRecognitionService();
                    if (null == cameraRecognitionService) {
                        Log.d(TAG, "Starting Camera Recognition Service");
                        Intent myIntent = new Intent().setClass(SmileIntroActivity.this, CameraRecognitionService.class);
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND);
                        startService(myIntent);
                    }
                } else {
                    Intent intent = new Intent().setClass(SmileIntroActivity.this, PhotoSettingActivity.class);
                    intent.putExtra("isLaunchFromTVAction", true);
                    intent.putExtra("isLaunchIntent", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

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
        mButtonSettings.setOnFocusChangeListener(mOnFocusChangeListener);

        mFinishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                    Log.i(TAG, "ACTION_LOCALE_CHANGED");
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
            } else {
                mButtonStart.setTextColor(getResources().getColor(R.color.feature_intro_text_no_focus));
            }

            if (mButtonCancel.hasFocus()) {
                mButtonCancel.setTextColor(getResources().getColor(R.color.feature_intro_text_focus));
            } else {
                mButtonCancel.setTextColor(getResources().getColor(R.color.feature_intro_text_no_focus));
            }

            if (mButtonSettings.hasFocus()) {
                mButtonSettings.setTextColor(getResources().getColor(R.color.feature_intro_text_focus));
            } else {
                mButtonSettings.setTextColor(getResources().getColor(R.color.feature_intro_text_no_focus));
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mFinishReceiver) {
            unregisterReceiver(mFinishReceiver);
            mFinishReceiver = null;
        }
    }
}
