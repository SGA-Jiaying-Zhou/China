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

import com.sony.dtv.tvcamera.R;
import com.sony.dtv.tvcamera.app.settings.ActionBehavior;
import com.sony.dtv.tvcamera.app.settings.ActionKey;
import com.sony.dtv.tvcamera.app.settings.dialog.Action;

import android.content.res.Resources;

enum ActionType {
    /*
     * 
     */
    VIDEO_SETTINGS(R.string.video_settings),
    VIDEO_SIZE(R.string.title_video_size),
    VIDEO_QUALITY(R.string.video_quality),
    VIDEO_DESTINATION(R.string.video_destination),
    PHOTO_SETTINGS(R.string.setting_title),
    TV_CAMERA_SETTINGS(R.string.tv_camera_settings);

    private final int mTitleResource;
    private final int mDescResource;

    private ActionType(int titleResource) {
        mTitleResource = titleResource;
        mDescResource = 0;
    }

    private ActionType(int titleResource, int descResource) {
        mTitleResource = titleResource;
        mDescResource = descResource;
    }
    String getTitle(Resources resources) {
        return resources.getString(mTitleResource);
    }

    String getDescription(Resources resources) {
        if (mDescResource != 0) {
            return resources.getString(mDescResource);
        }
        return null;
    }

    Action toAction(Resources resources) {
        return toAction(resources, true/*enabled*/);
    }

    Action toAction(Resources resources, boolean enabled) {
        return toAction(resources, getDescription(resources), enabled, false/* not checked */);
    }

    Action toAction(Resources resources, String description) {
        return toAction(resources, description, true/*enabled*/, false /* not checked */);
    }

    Action toAction(Resources resources, String description, boolean enabled) {
        return toAction(resources, description, enabled, false /* not checked */);
    }

    Action toAction(Resources resources, String description, boolean enabled, boolean checked) {
        return new Action.Builder()
                .key(getKey(this, ActionBehavior.INIT))
                .title(getTitle(resources))
                .description(description)
                .enabled(enabled)
                .checked(checked)
                .build();
    }

    private String getKey(ActionType t, ActionBehavior b) {
        return new ActionKey<ActionType, ActionBehavior>(t, b).getKey();
    }
}
