package com.sony.dtv.tvcamera.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sony.dtv.tvcamera.R;

public class EnjoyActivity extends Activity {

    private BroadcastReceiver mFinishReceiver = null;
    private String TAG = "EnjoyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enjoy);
        mFinishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                    Log.d(TAG, "ACTION_LOCALE_CHANGED");
                    finish();
                }
            }
        };
        IntentFilter locale_changed_filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
        registerReceiver(mFinishReceiver, locale_changed_filter);
    }

    public void onExit(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != mFinishReceiver) {
            unregisterReceiver(mFinishReceiver);
            mFinishReceiver = null;
        }
    }
}
