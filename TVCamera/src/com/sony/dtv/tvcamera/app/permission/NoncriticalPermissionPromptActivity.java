package com.sony.dtv.tvcamera.app.permission;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.TVCameraApp;
import com.sony.dtv.tvcamera.utils.Utils;

public class NoncriticalPermissionPromptActivity extends Activity implements View.OnFocusChangeListener {

    private static final String TAG = "Noncritical";

    private int mType = -1;

    Button mAgreeBtn;
    Button mDisagreeBtn;
    View mBtnPanel;

    TextView mPermissionTitle;
    TextView mPermissionMessage;
    TextView mPermissionInfo;
    TextView mPermissionOperation;

    private boolean mManageAppPermissionsLaunched = false;
    private String[] mRequestPermissions = null;
    private String[] mCriticalPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    BroadcastReceiver mTvActionReceiver = null;

    private View.OnClickListener mAgreeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            manageAppPermissions();
        }
    };

    private void manageAppPermissions() {
        Log.d(TAG, "manageAppPermissions()");
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        startActivity(intent);
        Toast.makeText(this, R.string.permission_toast, Toast.LENGTH_LONG).show();
        mManageAppPermissionsLaunched = true;
    }

    private View.OnClickListener mDisagreeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setResult(PermissionConstants.NONCRITICAL_DENIED);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_permission);

        TVCameraApp.registerTerminateKeyReceiver();

        if (savedInstanceState != null) {
            mManageAppPermissionsLaunched = savedInstanceState.getBoolean("mManageAppPermissionsLaunched");
        }

        if (getIntent() != null) {
            mRequestPermissions = getIntent().getStringArrayExtra(PermissionConstants.PERMISSIONS_KEY);
        }

        registerTvActionReceiver();

        initBtnFunction();
        initPermissionInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        if (mManageAppPermissionsLaunched) {

            Log.d(TAG, "start check critical permissions");
            boolean criticalPermissionsGranted = true;
            for (String permission : mCriticalPermissions) {
                if (isPermissionDenied(permission)) {
                    criticalPermissionsGranted = false;
                    break;
                }
            }
            if (criticalPermissionsGranted) {
                if (mRequestPermissions != null) {
                    Log.d(TAG, "start check request permissions");
                    boolean allRequestPermissionsGranted = true;
                    for (String permission : mRequestPermissions) {
                        if (isPermissionDenied(permission)) {
                            allRequestPermissionsGranted = false;
                            break;
                        }
                    }
                    if (allRequestPermissionsGranted) {
                        setResult(PermissionConstants.NONCRITICAL_GRANTED);
                        finish();
                    }
                }
            } else {
                finish();
            }
            mManageAppPermissionsLaunched = false;
        }
    }

    private boolean isPermissionDenied(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mManageAppPermissionsLaunched", mManageAppPermissionsLaunched);
    }

    private void initPermissionInfo() {
        Log.d(TAG, "initPermissionInfo()");
        Intent intent = getIntent();
        String name = intent.getStringExtra(PermissionConstants.NAME_INFO);
        String permission = intent.getStringExtra(PermissionConstants.PERMISSION_INFO);

        mPermissionTitle = (TextView) findViewById(R.id.permission_title);
        mPermissionMessage = (TextView) findViewById(R.id.permission_message);
        mPermissionInfo = (TextView) findViewById(R.id.permission_info);
        mPermissionOperation = (TextView) findViewById(R.id.permission_operation);

        mPermissionTitle.setText(String.format(
                getString(R.string.permission2_prompt_title),
                name));
        mPermissionMessage.setText(String.format(
                getString(R.string.permission2b_prompt_message),
                name));

        mPermissionInfo.setText(permission);
        mPermissionOperation.setText(R.string.permission2_prompt_operation);
    }

    private void initBtnFunction() {
        mAgreeBtn = (Button) findViewById(R.id.btn_agree);
        mDisagreeBtn = (Button) findViewById(R.id.btn_disagree);

        mAgreeBtn.setOnClickListener(mAgreeClickListener);
        mDisagreeBtn.setOnClickListener(mDisagreeClickListener);

        mAgreeBtn.setOnFocusChangeListener(this);
        mDisagreeBtn.setOnFocusChangeListener(this);
        mBtnPanel = findViewById(R.id.btn_Panel);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(PermissionConstants.NONCRITICAL_DENIED);
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (mAgreeBtn.hasFocus()) {
            ObjectAnimator.ofInt(mBtnPanel, "ScrollY", mBtnPanel.getScrollY(), 0).start();
        } else if (mDisagreeBtn.hasFocus()) {
            ObjectAnimator.ofInt(mBtnPanel, "ScrollY", 0, mDisagreeBtn.getTop() - mAgreeBtn.getTop()).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        unregisterTvActionReceiver();
    }

    private void registerTvActionReceiver() {
        IntentFilter tvActionFilters = new IntentFilter();
        tvActionFilters.addAction(Utils.INTENT_ACTION_CLOSE_APP);
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
            registerReceiver(mTvActionReceiver, tvActionFilters);
        } catch (Exception ex) {
            Log.e(TAG, "Exception: " + ex);
        }
    }

    private void unregisterTvActionReceiver() {
        if (null != mTvActionReceiver) {
            unregisterReceiver(mTvActionReceiver);
            mTvActionReceiver = null;
        }
    }
}
