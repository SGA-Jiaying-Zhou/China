/*
 * Copyright 2015 Sony Corporation
 */
package com.sony.dtv.tvx.tvplayer.legacy;

/**
 * Interface definition for a callback to be invoked when
  * {@link ITvPlayerService#setPapScreenSize()} results were found.
 */
interface ITvStateSetPapScreenSizeListener {
    /**
     * Called when {@link ITvPlayerService#setPapScreenSize()} has been completed.
     */
    void notifyDone();

    /**
     * Called when {@link ITvPlayerService#setPapScreenSize()} has been completed.
     *
     * @param error error code
     */
    void notifyFail(int error);
}
