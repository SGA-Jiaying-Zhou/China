package com.sony.dtv.tvcamera.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import com.sony.dtv.tvcamera.app.SonyCameraNotifyService;
import com.sony.dtv.tvcamera.app.cameracomponent.CameraEncoder;
import com.sony.dtv.tvcamera.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SonyCameraDetachedReceiver extends BroadcastReceiver {

    private static final String TAG = "SonyCameraDetached";

    Handler mHandler = null;

    public SonyCameraDetachedReceiver (Context context) {
        this(null, context);
    }

    public SonyCameraDetachedReceiver (Handler handler, Context context) {
        mHandler = handler;
        context.startService(new Intent(context, SonyCameraNotifyService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean sonyCameraReject = true;
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String,UsbDevice> map = manager.getDeviceList();
        Set<Map.Entry<String, UsbDevice>> entries = map.entrySet();
        for (Map.Entry<String, UsbDevice> entry : entries) {
            UsbDevice device = entry.getValue();
            String usbId = String.format("%04x:%04x", device.getVendorId(), device.getProductId());
            Log.d(TAG, "USB_ID : " + usbId);
            if (Utils.SONY_CAMERA_ID.equalsIgnoreCase(usbId)) {
                sonyCameraReject = false;
                break;
            }
        }

        Log.d(TAG, "sonyCameraReject is " + sonyCameraReject);
        if (sonyCameraReject) {
            Utils.sendSonyCameraNotify(context, CameraEncoder.CAMERA_TERMINATE);
            if (mHandler != null) {
                mHandler.sendEmptyMessage(CameraEncoder.CAMERA_TERMINATE);
            }
        }
    }
}
