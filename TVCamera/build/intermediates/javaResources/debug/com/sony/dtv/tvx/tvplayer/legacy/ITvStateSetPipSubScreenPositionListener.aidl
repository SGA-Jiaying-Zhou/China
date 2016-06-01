/*
 * Copyright 2015 Sony Corporation
 */
package com.sony.dtv.tvx.tvplayer.legacy;

/**
 * Interface definition for a callback to be invoked when
  * {@link ITvPlayerService#setPipSubScreenPosition()} results were found.
 */
interface ITvStateSetPipSubScreenPositionListener {
    /**
     * Called when {@link ITvPlayerService#setPipSubScreenPosition()} has been completed.
     *
     * @param position screen position of PIP.
     * This parameter can be {@code leftTop}, {@code leftBottom}, {@code rightTop} or {@code rightBottom}.
     */
    void notifyDone(String position);

    /**
     * Called when {@link ITvPlayerService#setPipSubScreenPosition()} has been completed.
     *
     * @param error error code
     */
    void notifyFail(int error);
}
