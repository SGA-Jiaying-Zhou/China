package com.sony.dtv.tvcamera.app.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;


public abstract class ManageNoncriticalPermissionExtActivity extends Activity {

    private static final String TAG = "ManageNoncritical";

    HashSet<String> dontAskAgainSet = new HashSet<>();

    Handler mHandler = new Handler();

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (int i=0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, permissions[i] + ":granted");
            } else {
                Log.v(TAG, permissions[i] + ":denied");
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    Log.v(TAG,"onRequestPermissionsResult shouldShowRequestPermissionRationale("+permissions[i]+"):false");
                    dontAskAgainSet.add(permissions[i]);
                } else {
                    Log.v(TAG,"onRequestPermissionsResult shouldShowRequestPermissionRationale("+permissions[i]+"):true");
                }
            }
        }
        saveState();

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                onStoragePermissionDenied();
                return;
            }
        }
        onStoragePermissionGranted();
    }

    protected abstract void onStoragePermissionGranted();
    protected abstract void onStoragePermissionDenied();
    protected abstract void onBeforeStartPromptActivity();

    protected abstract String getName();

    private void showPermissionPromptActivity() {
        Intent intent = new Intent(this, NoncriticalPermissionPromptActivity.class);
        String name = getName();
        String[] permissions = new String[]{};

        PackageManager pm = getPackageManager();
        String permissionInfo = "";
        try {
            PermissionGroupInfo storageGroupInfo = pm.getPermissionGroupInfo(Manifest.permission_group.STORAGE, 0);
            permissionInfo = String.valueOf(storageGroupInfo.loadLabel(pm));
            permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        intent.putExtra(PermissionConstants.NAME_INFO, name);
        intent.putExtra(PermissionConstants.PERMISSION_INFO, permissionInfo);
        intent.putExtra(PermissionConstants.PERMISSIONS_KEY, permissions);
        startActivityForResult(intent, PermissionConstants.NONCRITICAL_REQUEST_CODE);
    }

    protected boolean isStoragePermissionDenied() {
        return ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED;
    }

    protected void checkStoragePermission() {
        loadState();
        ArrayList<String> list = new ArrayList<>();

        if (isStoragePermissionDenied()){
            Log.v(TAG,"WRITE_EXTERNAL_STORAGE is not granted");
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            list.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            dontAskAgainSet.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            dontAskAgainSet.remove(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        saveState();
        requestPermission(list.toArray(new String[list.size()]), dontAskAgainSet.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    private void requestPermission(String[] permissionList, boolean showPrompt) {
        if (permissionList.length == 0) {
            onStoragePermissionGranted();
            return;
        }

        if (showPrompt) {
            onBeforeStartPromptActivity();
            showPermissionPromptActivity();
        } else {
            ActivityCompat.requestPermissions(this, permissionList, 0);
        }
    }

    private void saveState(){
        SharedPreferences sharedPreferences = getSharedPreferences("Permission",0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        for(String permission: dontAskAgainSet){
            editor.putBoolean(permission,true);
        }
        editor.commit();
    }

    private void loadState(){
        dontAskAgainSet.clear();
        SharedPreferences sharedPreferences = getSharedPreferences("Permission", 0);
        for(String permission: sharedPreferences.getAll().keySet()){
            dontAskAgainSet.add(permission);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult(), requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == PermissionConstants.NONCRITICAL_REQUEST_CODE) {
            if (resultCode == PermissionConstants.NONCRITICAL_GRANTED) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onStoragePermissionGranted();
                    }
                }, 800);
            } else if (resultCode == PermissionConstants.NONCRITICAL_DENIED) {
                onStoragePermissionDenied();
            }
        }
    }
}
