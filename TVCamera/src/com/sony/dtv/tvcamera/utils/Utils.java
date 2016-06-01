package com.sony.dtv.tvcamera.utils;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sony.dtv.camerarecognition.CameraRecognition;
import com.sony.dtv.camerarecognition.CameraRecognitionError;
import com.sony.dtv.provider.modelvariation.util.ModelVariationKey;
import com.sony.dtv.provider.modelvariation.util.ModelVariationUtil;
import com.sony.dtv.provider.modelvariation.util.ModelVariationValue;
import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.SonyCameraNotifyService;
import com.sony.dtv.tvcamera.app.TVCameraApp;
import com.sony.dtv.tvcamera.app.photosetting.PhotoSettingConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * A collection of utility methods, all static.
 */
public class Utils {
    private static final String TAG = "Utils";
    public static final String RECORD_PATH = "SonyTVCamera_record";
    public static final String PHOTO_PATH = "SonyTVCamera_photo";
    public static final String SMILESHUTTER_PATH = "SonyTVCamera_smileshutter";
    public static final String SECURITY_RECORD_PATH = "SonyTVCamera_presence_detector";

    public static final String INTENT_PRE_TV_ACTION_LOADED = "PRE_TV_ACTION_LOADED";
    public static final String INTENT_ACTION_CLOSE_APP = "CLOSE_TVCAMERA";
    public static final String INTENT_TV_ACTION_LOADED = "TV_ACTION_LOADED";

    public static final String INTENT_TV_ACTION_STATUS_CHANGED = "com.sony.dtv.tvcameraapp.intent.action.STATUS_CHANGED";

    public static final int CAMERA_MODE = 0;
    public static final int PHOTO_MODE = 1;

    public static final int CAMERA_MODE_PAP_1 = 0;
    public static final int CAMERA_MODE_PAP_2 = 1;
    public static final int CAMERA_MODE_FULL_CAMERA = 2;
    public static final int CAMERA_MODE_NUM = 3;

    public static final String VIDEO_SIZE_360 = "Size360";
    public static final String VIDEO_SIZE_720 = "Size720";
    public static final String VIDEO_SIZE_1080 = "Size1080";

    public static final String VIDEO_QUALITY_HIGH = "High";
    public static final String VIDEO_QUALITY_MIDDLE = "Middle";
    public static final String VIDEO_QUALITY_LOW = "Low";

    public static final int CAMERA_NOT_SUPPORT = 0;
    public static final int CAMERA_SUPPORT_OPTIONAL = 1;
    public static final int CAMERA_SUPPORT_BUNDLED = 2;
    public static final int CAMERA_NOT_COMPATIBLE = -1;

    public static final int CAMERA_SIZE_1080P_WIDTH = 1920;
    public static final int CAMERA_SIZE_1080P_HEIGHT = 1080;
    public static final int CAMERA_SIZE_720P_WIDTH = 1280;
    public static final int CAMERA_SIZE_720P_HEIGHT = 720;
    public static final int CAMERA_SIZE_360P_WIDTH = 640;
    public static final int CAMERA_SIZE_360P_HEIGHT = 360;

    public static boolean isFirstLaunch = false;
    public static boolean isErrorShowed = false;

    public static final int SECURITY_CAMERA_NO_MESSAGE = 0;

    public static final int SECURITY_CAMERA_MIN_USB_SIZE = 50;
    public static final int CHECK_MEM_CAPACITY_INTERVAL = 10000;

    public static final int USB_DEVICE_TYPE_CAMERA = 0x00;
    public static final int USB_DEVICE_TYPE_KEYBOARD = 0x01;
    public static final int USB_DEVICE_TYPE_MOUSE = 0x02;
    public static final int USB_DEVICE_TYPE_U_DISK = 0x03;
    public static final int USB_DEVICE_TYPE_AUDIO = 0x04;

    public static boolean isFirstLaunch() {
        return isFirstLaunch;
    }

    public static void setIsFirstLaunch(boolean isFirst) {
        isFirstLaunch = isFirst;
    }

    private static String saveFilePath = null;

    public static String getSaveFileAbsolutePath() {
        return saveFilePath;
    }

    public static void setSaveFileAbsolutePath(String saveFileAbsolutePath) {
        saveFilePath = saveFileAbsolutePath;
    }

    public static boolean isErrorShowed() {
        return isErrorShowed;
    }

    public static void setIsErrorShowed(boolean isShowed) {
        Log.v(TAG, "isErrorShowed = " + isErrorShowed);
        isErrorShowed = isShowed;
    }

    private static HashMap<String, Integer> modelNameResource = new HashMap<>();

    static {
        modelNameResource.put("KDL-65W850C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KDL-75W850C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-65X9300C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-75X9400C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-55X9000C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-65X9000C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-75X9100C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-55X8500C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-65X8500C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-75X8500C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-43X8300C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-49X8300C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-55S8500C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-65S8500C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-49X8000C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-55X8000C", CAMERA_SUPPORT_OPTIONAL);
        modelNameResource.put("KD-65X8000C", CAMERA_SUPPORT_OPTIONAL);
    }

    public static final String SONY_CAMERA_ID = "054C:0A92";

    public static void scanFile(String filePath) {
        MediaScannerConnection.scanFile(TVCameraApp.getInstance(), new String[]{filePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.v("MediaScanWork", "file " + path + " was scanned successfully:" + uri);
            }
        });
    }

    public static long getMemoryCapacity(String FilePath) {
        if (TextUtils.isEmpty(FilePath)) {
            return 0;
        }
        try {
            File USBMemory = new File(FilePath);
            Long availableSize = USBMemory.getFreeSpace() / 1024 / 1024;
            return availableSize;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static long getVideoMemoryCapacity(String FilePath) {
        Log.d(TAG, "FilePath = " + FilePath);
        if (TextUtils.isEmpty(FilePath)) {
            return 0;
        }
        try {
            File VideoMemory = new File(FilePath);
            Long availableSize = VideoMemory.length() / 1024 / 1024;
            Log.d(TAG, "availableSize = " + availableSize);
            return availableSize;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getCameraSupportType() {
        int resultValue = CAMERA_NOT_SUPPORT;
        String Model = ModelVariationUtil.get(TVCameraApp.getInstance().getContentResolver(), ModelVariationKey.USB_CAMERA);
        String modelName = ModelVariationUtil.get(TVCameraApp.getInstance().getContentResolver(), ModelVariationKey.MODEL_NAME);
        Log.d(TAG, "USB CAMERA:" + Model + " MODEL_NAME:" + modelName);
        if (Model == null || Model.isEmpty()) {
            resultValue = CAMERA_NOT_SUPPORT;
        } else {
            try {
                if (TextUtils.equals(Model, ModelVariationValue.USB_CAMERA_NOT_SUPPORTED)) {
                    if (modelNameResource.containsKey(modelName)) {
                        resultValue = modelNameResource.get(modelName);
                    } else {
                        resultValue = CAMERA_NOT_SUPPORT;
                    }
                } else if (TextUtils.equals(Model, ModelVariationValue.USB_CAMERA_SUPPORTED_OPTIONAL)) {
                    resultValue = CAMERA_SUPPORT_OPTIONAL;
                } else if (TextUtils.equals(Model, ModelVariationValue.USB_CAMERA_SUPPORTED_BUNDLED)) {
                    resultValue = CAMERA_SUPPORT_BUNDLED;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                resultValue = CAMERA_NOT_SUPPORT;
            }
        }
        Log.d(TAG, "CameraSupportType:" + resultValue);
        return resultValue;
    }

    public static void sendStateChangeIntent(boolean isLaunched) {
        Intent intent = new Intent(INTENT_TV_ACTION_STATUS_CHANGED);
        intent.putExtra("com.sony.dtv.tvcameraapp.intent.extra.IS_LAUNCHED", isLaunched);
        Log.d(TAG, "sendStateChangeIntent: " + intent);
        Log.d(TAG, "isLaunched: " + isLaunched);
        TVCameraApp.getInstance().sendBroadcast(intent);
    }

    public static boolean saveImage(Bitmap photoBitmap, int captureType, Context context) {
        return saveImage(photoBitmap, captureType, context, false);
    }

    public static boolean saveImage(Bitmap photoBitmap, int captureType, Context context, boolean isShowToast) {
        SharedPreferences USBPath = context.getSharedPreferences("usb", 0);
        String SaveFilePath = USBPath.getString("usb_current_photo_path", "");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateStr = sdf.format(new Date());

        int captureWidth = 0;
        int captureHeight = 0;

        SharedPreferences smileShutter = context.getSharedPreferences(PhotoSettingConstants.SP_NAME, Context.MODE_PRIVATE);
        String PictureSizeKey = smileShutter.getString(PhotoSettingConstants.PictureSizeKey, context.getString(R.string.picture_size_defValue));
        if (PictureSizeKey.equals("1920 x 1080")) {
            captureWidth = Utils.CAMERA_SIZE_1080P_WIDTH;
            captureHeight = Utils.CAMERA_SIZE_1080P_HEIGHT;
        } else if (PictureSizeKey.equals("1280 x 720")) {
            captureWidth = Utils.CAMERA_SIZE_720P_WIDTH;
            captureHeight = Utils.CAMERA_SIZE_720P_HEIGHT;
        } else {
            captureWidth = Utils.CAMERA_SIZE_360P_WIDTH;
            captureHeight = Utils.CAMERA_SIZE_360P_HEIGHT;
        }

        Log.d(TAG, "captureWidth = " + captureWidth);
        Log.d(TAG, "captureHeight = " + captureHeight);

        if (captureType == CameraRecognition.CAPTURE_TYPE_ONE_SHOT_WITHOUT_ANALYSIS) {
            SaveFilePath += "/" + PHOTO_PATH;
        } else if (captureType == CameraRecognition.CAPTURE_TYPE_SMILE) {
            SaveFilePath += "/" + SMILESHUTTER_PATH;
        }

        File filePath = new File(SaveFilePath);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }

        String saveFileAbsolutePath = SaveFilePath + "/" + dateStr + "_" + captureWidth + "x" + captureHeight + ".jpeg";
        File f = new File(SaveFilePath + "/", dateStr + "_" + captureWidth + "x" + captureHeight + ".jpeg");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
            photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Log.d(TAG, "have saved photo, saveFileAbsolutePath = " + saveFileAbsolutePath);
            Utils.scanFile(saveFileAbsolutePath);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            Utils.USBError(context, isShowToast);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Utils.USBError(context, isShowToast);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void exitTVCamera(Context context) {
        Log.d(TAG, "exitTVCamera!");
        Intent closeAppIntent = new Intent();
        closeAppIntent.setAction(Utils.INTENT_ACTION_CLOSE_APP);
        context.sendBroadcast(closeAppIntent);
    }

    public static void handleCameraRecognitionError(int result, final Context context) {
        handleCameraRecognitionError(result, context, false, false);
    }

    public static String getCameraRecognitionResultString(int result, final Context context) {
        String resultString = null;
        Log.d(TAG, "result = " + result);
        switch (result) {
            case CameraRecognitionError.RESULT_OK:
                resultString = "Succeeded";
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_JAR_BINDER_FAILED:
                resultString = context.getString(R.string.tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_JAR_CALLBACK_ISNOT_REGISTERED:
                resultString = context.getString(R.string.tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_JAR_CP_FAILED:
                resultString = context.getString(R.string.tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_JAR_SAVE_IMAGE_FAILED:
                resultString = context.getString(R.string.tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_JAR_UNSUPPORTED_FORMAT:
                resultString = context.getString(R.string.tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_JAR_URI_CREATE_FAILED:
                resultString = context.getString(R.string.tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_SERVICE_LOADLIBRARY_FAILED:
                resultString = context.getString(R.string.tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_SERVICE_UNSUPPORTED_RESOLUTION:
                resultString = context.getString(R.string.tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_SERVICE_PUTIMAGE_WRITE_FAILED:
                resultString = context.getString(R.string.tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_SERVICE_INCOMPATIBLE_LIBRARY:
                resultString = context.getString(R.string.tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_CAMERA_DEVICE_UNAVAILABLE:
                resultString = context.getString(R.string.camera_terminate);
                break;
            case CameraRecognitionError.RESULT_ERR_NOT_OPENED:
                resultString = context.getString(R.string.camera_device_open_failed);
                break;
            case CameraRecognitionError.RESULT_ERR_NOT_INITIALIZED:
                resultString = context.getString(R.string.camera_device_initialization_failed);
                break;
            case CameraRecognitionError.RESULT_ERR_BUSY:
                resultString = context.getString(R.string.camera_device_is_busy);
                break;
            case CameraRecognitionError.RESULT_ERR_UNSUPPORTED_RESOLUTION:
                resultString = context.getString(R.string.tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_CAMERA_RECOGNITION_INTERNAL:
                resultString = context.getString(R.string.tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_BINDER_ERROR:
                resultString = context.getString(R.string.tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ALREADY_OPENED:
                resultString = context.getString(R.string.camera_recognition_is_already_opened);
                break;
            default:
                break;
        }
        return resultString;
    }

    public static String getSmileShutterCameraRecognitionResultString(int result, final Context context) {
        String resultString = null;
        Log.d(TAG, "result = " + result);
        switch (result) {
            case CameraRecognitionError.RESULT_OK:
                resultString = "Succeeded";
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_JAR_BINDER_FAILED:
                resultString = context.getString(R.string.smile_shutter_tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_JAR_CALLBACK_ISNOT_REGISTERED:
                resultString = context.getString(R.string.smile_shutter_tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_JAR_CP_FAILED:
                resultString = context.getString(R.string.smile_shutter_tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_JAR_SAVE_IMAGE_FAILED:
                resultString = context.getString(R.string.smile_shutter_tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_JAR_UNSUPPORTED_FORMAT:
                resultString = context.getString(R.string.smile_shutter_tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_JAR_URI_CREATE_FAILED:
                resultString = context.getString(R.string.smile_shutter_tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_SERVICE_LOADLIBRARY_FAILED:
                resultString = context.getString(R.string.smile_shutter_tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_SERVICE_UNSUPPORTED_RESOLUTION:
                resultString = context.getString(R.string.smile_shutter_tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_SERVICE_PUTIMAGE_WRITE_FAILED:
                resultString = context.getString(R.string.smile_shutter_tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_RECOGNITION_SERVICE_INCOMPATIBLE_LIBRARY:
                resultString = context.getString(R.string.smile_shutter_tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_CAMERA_DEVICE_UNAVAILABLE:
                resultString = context.getString(R.string.smile_shutter_camera_terminate);
                break;
            case CameraRecognitionError.RESULT_ERR_NOT_OPENED:
                resultString = context.getString(R.string.smile_shutter_camera_device_open_failed);
                break;
            case CameraRecognitionError.RESULT_ERR_NOT_INITIALIZED:
                resultString = context.getString(R.string.smile_shutter_camera_device_initialization_failed);
                break;
            case CameraRecognitionError.RESULT_ERR_BUSY:
                resultString = context.getString(R.string.smile_shutter_camera_device_is_busy);
                break;
            case CameraRecognitionError.RESULT_ERR_UNSUPPORTED_RESOLUTION:
                resultString = context.getString(R.string.smile_shutter_tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_CAMERA_RECOGNITION_INTERNAL:
                resultString = context.getString(R.string.smile_shutter_tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ERR_BINDER_ERROR:
                resultString = context.getString(R.string.smile_shutter_tv_camera_is_abnormal);
                break;
            case CameraRecognitionError.RESULT_ALREADY_OPENED:
                resultString = context.getString(R.string.smile_shutter_camera_recognition_is_already_opened);
                break;
            default:
                break;
        }
        return resultString;
    }

    public static void handleCameraRecognitionError(int result, final Context context, boolean isShowToast, boolean isSmileShutterMode) {
        if (isErrorShowed) {
            return;
        }

        if (result == CameraRecognitionError.RESULT_OK) {
            return;
        }

        isErrorShowed = true;
        final String errorString;
        if (isSmileShutterMode) {
            errorString = getSmileShutterCameraRecognitionResultString(result, context);
        } else {
            errorString = getCameraRecognitionResultString(result, context);
        }
        final Handler handler = new Handler(context.getMainLooper());
        if (!isShowToast) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertDialog = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle(R.string.dialog_title)
                            .setMessage(errorString)
                            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Utils.exitTVCamera(context);
                                }

                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    Utils.exitTVCamera(context);
                                }
                            }).create();
                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alertDialog.show();
                }
            });

        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, errorString,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        Service cameraRecognitionService = TVCameraApp.getCameraRecognitionService();
        if (null != cameraRecognitionService) {
            Log.d(TAG, "cameraRecognitionService stopSelf");
            cameraRecognitionService.stopSelf();
            TVCameraApp.setCameraRecognitionService(null);
        }
    }

    public static void USBError(final Context context) {
        USBError(context, false);
    }

    public static void USBError(final Context context, boolean isShowToast) {
        if (isErrorShowed) {
            Log.d(TAG, "isErrorShowed");
            return;
        }

        Log.d(TAG, "USBError");

        final Handler handler = new Handler(context.getMainLooper());
        if (isShowToast) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, context.getString(R.string.usb_error),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertDialog = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle(R.string.dialog_title)
                            .setMessage(R.string.usb_error)
                            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                }
                            }).create();
                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alertDialog.show();
                }
            });

            Service cameraRecognitionService = TVCameraApp.getCameraRecognitionService();
            if (null != cameraRecognitionService) {
                Log.d(TAG, "cameraRecognitionService stopSelf");
                cameraRecognitionService.stopSelf();
                TVCameraApp.setCameraRecognitionService(null);
            }
        }
    }

    public static void ShowCameraSupportErrorDialog(final Context context, int type) {
        ShowCameraSupportErrorDialog(context, type, false);
    }

    public static void ShowCameraSupportErrorDialog(final Context context, int type, final boolean isSecurityCameraCheck) {
        if (!isSecurityCameraCheck) {
            if (isErrorShowed) {
                return;
            }
            Utils.setIsErrorShowed(true);
        }
        switch (type) {
            case Utils.CAMERA_SUPPORT_OPTIONAL:
            case Utils.CAMERA_SUPPORT_BUNDLED:
            case Utils.CAMERA_NOT_SUPPORT:
                AlertDialog alertDialog = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                        .setMessage(R.string.no_camera)
                        .setPositiveButton(R.string.app_exit_button, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!isSecurityCameraCheck) {
                                    Utils.exitTVCamera(context);
                                }
                            }

                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                if (!isSecurityCameraCheck) {
                                    Utils.exitTVCamera(context);
                                }
                            }
                        }).create();
                alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertDialog.show();
                break;
            case Utils.CAMERA_NOT_COMPATIBLE:
                LayoutInflater inflater = LayoutInflater.from(context);
                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.not_compatible_camera_dialog, null);
                AlertDialog notCompatibleDialog = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                        .setView(layout)
                        .setPositiveButton(R.string.app_exit_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!isSecurityCameraCheck) {
                                    Utils.exitTVCamera(context);
                                }
                            }

                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                if (!isSecurityCameraCheck) {
                                    Utils.exitTVCamera(context);
                                }
                            }
                        }).create();
                notCompatibleDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                notCompatibleDialog.show();
                break;
        }
    }

    public static void CameraSupportError(final Context context) {
        int type = Utils.getCameraSupportType();
        ShowCameraSupportErrorDialog(context, type);
    }

    public static boolean checkCameraType(final Context context) {
        boolean isSonyCamera = false;
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> map = manager.getDeviceList();
        Set<Map.Entry<String, UsbDevice>> entries = map.entrySet();
        for (Map.Entry<String, UsbDevice> entry : entries) {
            UsbDevice device = entry.getValue();
            String usbId = String.format("%04x:%04x", device.getVendorId(), device.getProductId());
            Log.d(TAG, "USB_ID : " + usbId);
            if (Utils.SONY_CAMERA_ID.equalsIgnoreCase(usbId)) {
                isSonyCamera = true;
                break;
            }
        }

        Log.d(TAG, "isSonyCamera --- " + isSonyCamera);
        return isSonyCamera ? true : false;
    }

    public static void sendSonyCameraNotify(Context context, int what) {
        Intent intent = new Intent(context, SonyCameraNotifyService.class);
        intent.putExtra("what", what);
        context.startService(intent);
    }

    public static boolean checkUsbWritablity(String SaveFilePath) {
        Log.d(TAG, "USB START CHECK");

        File file = new File(SaveFilePath + "/" + "testFile");
        file.mkdirs();
        if (file.exists()) {
            file.delete();
            Log.d(TAG, "USB CAN WRITE");
            return true;
        } else {
            Log.d(TAG, "USB CAN'T WRITE");
            return false;
        }
    }

    public static boolean needMirror(Context context) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(0, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            SharedPreferences mirrorPreferences = context.getSharedPreferences("mirror", 0);
            if (!mirrorPreferences.contains("mirrorflags")) {
                SharedPreferences.Editor mirrorEditor = mirrorPreferences.edit();
                mirrorEditor.putBoolean("mirrorflags", true);
                mirrorEditor.commit();
                return true;
            }
        }
        return false;
    }

    public static LinkedList<String> getUSBPathList(LinkedList<String> usbPaths, Context context) {
        LinkedList<String> newUsbPaths = new LinkedList<>();
        LinkedList<String> oldUsbPaths = new LinkedList(usbPaths);
        LinkedList<String> latestUsbPaths = new LinkedList(usbPaths);
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method getVolumeList = storageManager.getClass().getDeclaredMethod("getVolumeList");

            Class classStorage = Class.forName("android.os.storage.StorageVolume");
            Field fieldPath = classStorage.getDeclaredField("mPath");
            fieldPath.setAccessible(true);
            Field fieldState = classStorage.getDeclaredField("mState");
            fieldState.setAccessible(true);
            Field fieldEmulated = classStorage.getDeclaredField("mEmulated");
            fieldEmulated.setAccessible(true);

            Object object = getVolumeList.invoke(storageManager);

            int length = Array.getLength(object);

            for (int i = 0; i < length; i++) {
                Object storageVolume = Array.get(object, i);
                String state = (String) fieldState.get(storageVolume);
                if (!state.equalsIgnoreCase("mounted")) {
                    continue;
                }
                Boolean emulated = fieldEmulated.getBoolean(storageVolume);
                if (emulated) {
                    continue;
                }
                String filePath = ((File) fieldPath.get(storageVolume)).toString();
                if (oldUsbPaths.indexOf(filePath) == -1) {
                    latestUsbPaths.add(filePath);
                }
                newUsbPaths.add(filePath);
            }

            String oldDevice = null;
            length = oldUsbPaths.size();

            for (int i = 0; i < length; i++) {
                oldDevice = oldUsbPaths.get(i);
                if (newUsbPaths.indexOf(oldDevice) == -1) {
                    latestUsbPaths.remove(oldDevice);
                }
            }

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "latestUsbPaths = " + latestUsbPaths);
        return latestUsbPaths;
    }

    private static int getDeviceType(int base_class, int sub_class, int protocol) {
        int type = -1;
        switch (base_class) {
            case 0x01:
                type = USB_DEVICE_TYPE_AUDIO;
                break;
            case 0x03:
                if (sub_class == 1) {
                    if (protocol == 1) {
                        type = USB_DEVICE_TYPE_KEYBOARD;
                    } else if (protocol == 2) {
                        type = USB_DEVICE_TYPE_MOUSE;
                    }
                } else {
                    Log.e(TAG, "unknown hid device" + " sub_class " + sub_class
                            + " protocol " + protocol);
                }
                break;
            case 0x08:
                type = USB_DEVICE_TYPE_U_DISK;
                break;
            case 0x0e:
                type = USB_DEVICE_TYPE_CAMERA;
                break;
            default:
                Log.e(TAG, "unknown usb device " + "base class " + base_class
                        + " sub_class " + sub_class + " protocol " + protocol);
                break;
        }
        return type;
    }

    public static boolean checkCameraInsert(final Context context) {
        boolean isCameraInsert = false;
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        Map<String, UsbDevice> devices = manager.getDeviceList();

        if (devices.size() > 0) {
            for (UsbDevice new_device : devices.values()) {
                Log.d(TAG, "usb device size is " + devices.size() + " device name " + new_device.getDeviceName());
                if (new_device.getInterfaceCount() > 0) {
                    UsbInterface usb_interface = new_device.getInterface(0);
                    int type = getDeviceType(usb_interface.getInterfaceClass(),
                            usb_interface.getInterfaceSubclass(),
                            usb_interface.getInterfaceProtocol());

                    if (type == Utils.USB_DEVICE_TYPE_CAMERA) {
                        isCameraInsert = true;
                        return isCameraInsert;
                    }
                } else {
                    Log.w(TAG, "usb interface count is 0\n" + new_device.toString());
                }
            }
        }
        return isCameraInsert;
    }
}
