package com.sony.dtv.tvcamera.extension;

import android.content.Intent;
import android.util.Log;

import com.sony.dtv.scrums.action.libtvdotactionextension.TvDotActionExtensionProvider;
import com.sony.dtv.tvcamera.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class PhotoExtension extends TvDotActionExtensionProvider {

    private final List<String> mCurrentStateList = new ArrayList<>();
    private String TAG = "PhotoExtension";

    @Override
    protected void dispatchActionEvent(String event, List<String> states) {
        Log.v(TAG, "event: " + event);
        Log.v(TAG, "states: " + states);

        if (Utils.INTENT_ACTION_CLOSE_APP.equals(event)) {
            Intent intent = new Intent();
            intent.setAction(Utils.INTENT_ACTION_CLOSE_APP);
            sendBroadcast(intent);
        }
    }

    @Override
    protected List<String> getCurrentStateList() {
        // TODO Auto-generated method stub
        return mCurrentStateList;
    }

    @Override
    protected void onBindFromTvDotAction(Intent intent) {
        // TODO Auto-generated method stub
        super.onBindFromTvDotAction(intent);
        Log.d(TAG, "onBindFromTvDotAction");
        mCurrentStateList.add("stateDefault");
        notifyCurrentStateList(mCurrentStateList);
    }

    @Override
    protected void onUnbindFromTvDotAction(Intent intent) {
        // TODO Auto-generated method stub
        super.onUnbindFromTvDotAction(intent);
        Log.d(TAG, "onUnbindFromTvDotAction");
    }

}
