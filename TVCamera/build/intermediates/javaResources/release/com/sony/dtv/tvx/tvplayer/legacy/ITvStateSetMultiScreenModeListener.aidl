/*
 * Copyright 2015 Sony Corporation
 */
package com.sony.dtv.tvx.tvplayer.legacy;

/**
 * Interface definition for a callback to be invoked when
  * {@link ITvPlayerService#setMultiScreenMode()} results were found.
 */
interface ITvStateSetMultiScreenModeListener {
    /**
     * Called when {@link ITvPlayerService#setMultiScreenMode()} has been completed.
     *
     * @param mode The successful completion the multi screen mode.
     * The multi screen mode can be {@code SINGLE} or {@code PIP} or {@code PAP}.
     */
    void notifyDone(String mode);

    /**
     * Called when {@link ITvPlayerService#setMultiScreenMode()} has been completed.
     *
     * @param error error code following:
     * <ul>
     * <li>0: Same the multi screen mode was passed.
     * </ul>
     */
    void notifyFail(int error);
}
