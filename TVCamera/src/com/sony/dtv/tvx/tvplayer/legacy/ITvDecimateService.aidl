/*
 * Copyright 2015 Sony Corporation
 */
package com.sony.dtv.tvx.tvplayer.legacy;

import com.sony.dtv.tvx.tvplayer.legacy.ITvDecimateListener;

/**
 * Interface definition for a request to DecimateService.
 */
interface ITvDecimateService {
    /**
     * Requests permission for the layout change.
     *
     * @param className the identifier of client
     * @param priority only support priority NORMAL (=1)
     * @param listener the listener for receiving a callback
     * @return 0 if success, otherwise error
     * @throws UnsupportedOperationException if the priority is not NORMAL (=1)
     */
    int requestScalePermission(String className, int priority, ITvDecimateListener listener);

    /**
     * Cancel the permission request.
     *
     * @param className the identifier of client
     * @return 0 if success, otherwise error
     */
    int cancelRequestScalePermission(String className);

    /**
     * Change the size of the content area.
     *
     * @param className the identifier of client
     * @param height the height of decimate size. Relative value with that full screen is 1. (0, …, 1)
     * @param width the width of decimate size. Relative value with that full screen is 1. (0, …, 1)
     * @param x the X coordinate (left top is 0). Relative value with that full screen is 1. (0, …, 1)
     * @param y the Y coordinate (left top is 0). Relative value with that full screen is 1. (0, …, 1)
     * @param multiPictureCancel only support {@code true}
     * @throws UnsupportedOperationException if the multiPictureCancel is {@code false}
     */
    int changeLayout(String className, float height, float width, float x, float y,
         boolean multiPictureCancel);
}
