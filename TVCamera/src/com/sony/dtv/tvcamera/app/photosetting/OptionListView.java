package com.sony.dtv.tvcamera.app.photosetting;

import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;

public class OptionListView extends ListView {

    public OptionListView(Context context) {
        super(context);
    }

    public OptionListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OptionListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OptionListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        View selectedView = getSelectedView();
        OptionAdapter.ViewHolder vh = (OptionAdapter.ViewHolder)selectedView.getTag();
        boolean isNeedDisable = vh.needDisable;

        if (isNeedDisable && event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            AudioManager manager = (AudioManager) selectedView.getContext().getSystemService(Context.AUDIO_SERVICE);
            manager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
