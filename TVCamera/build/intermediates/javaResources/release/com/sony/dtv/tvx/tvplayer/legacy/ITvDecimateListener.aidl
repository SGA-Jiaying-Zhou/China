/*
 * Copyright 2015 Sony Corporation
 */
package com.sony.dtv.tvx.tvplayer.legacy;

/**
 * Interface definition for a callback to be invoked from DecimateService.
 */
interface ITvDecimateListener {
    /**
     * Calls when the permission granted.
     */
    void notifyPermissionGranted();

    /**
     * calls when the permission deprived.
     *
     * @return if want to return to the full screen, return true
     */
    boolean notifyPermissionDeprived();

    /**
     * Calls when the layout has been changed.
     */
    void notifyLayoutChanged();
}
