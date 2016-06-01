package com.sony.dtv.tvcamera.app;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.cameracomponent.CameraEncoder;
import com.sony.dtv.tvcamera.utils.Utils;

public class SonyCameraNotifyService extends Service {

    private static final String TAG = "SonyCameraUsbNotify";

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "msg.what is " + msg.what);
            Log.d(TAG, "isErrorShowed is " + Utils.isErrorShowed());

            switch (msg.what) {
                case CameraEncoder.CAMERA_TERMINATE:
                    if (!Utils.isErrorShowed()) {
                        Utils.setIsErrorShowed(true);
                        Service securityCameraService = TVCameraApp.getSecurityCameraService();
                        if (null == securityCameraService) {
                            AlertDialog dialog = new AlertDialog.Builder(SonyCameraNotifyService.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle(R.string.dialog_title)
                                    .setMessage(R.string.camera_terminate)
                                    .setPositiveButton(R.string.app_exit_button, new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Utils.exitTVCamera(SonyCameraNotifyService.this);
                                        }

                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            Utils.exitTVCamera(SonyCameraNotifyService.this);
                                        }
                                    }).create();
                            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            dialog.show();
                        } else {
                            TVCameraApp.setSecurityCameraStringID(R.string.security_camera_no_camera_toast);
                            securityCameraService.stopSelf();
                        }
                    }
                    break;

                default:
                    break;
            }

            return true;
        }
    });

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mHandler.sendEmptyMessage(intent.getIntExtra("what", -1));
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
