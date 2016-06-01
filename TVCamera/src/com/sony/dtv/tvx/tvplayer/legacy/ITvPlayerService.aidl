/*
 * Copyright 2015 Sony Corporation
 */
package com.sony.dtv.tvx.tvplayer.legacy;

import android.os.Bundle;
import android.view.KeyEvent;

import com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetMultiScreenModeListener;
import com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPapScreenSizeListener;
import com.sony.dtv.tvx.tvplayer.legacy.ITvStateSetPipSubScreenPositionListener;

/**
 *  Interface for provides a Tv player state.
 */
interface ITvPlayerService {
    /**
     * Returns the currently valid favorite id.
     *
     * @return the Id in the case of Favorite Mode.<br>
     *         case of non-Favorite Mode returns Null.
     */
    String getCurrentFavoriteId();

    /**
     * Returns the current input {@link Bundle}.
     *
     * @param id The id should be one of the followings:
     * <ul>
     * <li>0 : Focused screen including SINGLE mode
     * <li>1 : PIP : BIG screen, PAP : LEFT screen
     * <li>2 : PIP : SMALL screen, PAP : RIGHT screen
     * </ul>
     * @return detail is following, null if id is invalid:
     * <ul>
     * <li>Key "type": TUNER, EXTERNAL_INPUT. null if TvPlayer is hidden.
     * <li>Key "channel_id": same as TvContract.Channels._ID. -1 if TvPlayer is hidden.
     * <li>Key "type_external_input": COMPOSITE, SCART, COMPONENT, VGA, HDMI.
      * null if TvPlayer is hidden.
     * <li>Key "input_id": input id of current channel. null if TvPlayer is hidden.
     * </ul>
     */
    Bundle getCurrentInputInfo(int id);

    /**
     * Returns the path info that TIS/MW should be use.
     *
     * The path info can be {@code MAIN} or {@code SUB}.
     *
     * @return Returns {@code MAIN} if single mode.
     * Returns specified inputId is to determine whether the {@code MAIN} or {@code SUB} if PIP or PAP mode.
     */
    String getPathInfo(String inputId);

    /**
     * Returns the current multi screen mode.
     *
     * @return Returns {@code SINGLE} if single mode.
     * Returns {@code PIP} if PAP mode.
     * Returns {@code PAP} if PAP mode.
     */
    String getMultiScreenMode();

    /**
     * Sets the current multi screen mode.
     *
     * The multi screen mode can be {@code SINGLE} or {@code PIP} or {@code PAP}.
     *
     * @param mode multi screen mode
     * @param listener {@link ITvStateSetMultiScreenModeListener} to receive the result of this method is success or not.
     * @return 0 if succeeded, else if error.
     */
    int setMultiScreenMode(String mode, ITvStateSetMultiScreenModeListener listener);

    /**
     * Returns the screen size of PAP.
     *
     * @reutrn Returns {@code null} if current screen mode is not PAP.
     * Returns {@code mainBig} if main screen is big.
     * Returns {@code mainSmall} if if main screen is small.
     */
    String getPapScreenSize();

    /**
     * Sets the screen size of PAP.
     *
     * @param screen screen of left or right of PAP. This parameter can be {@code main} or {@code sub}
     * @param size {@code +1} to become bigger the specified screen.
     * {@code -1} to become smaller the s@ecified screen.
     * @param listener {@link ITvStateSetPapScreenSizeListener} to receive the result of this method is success or not.
     */
    int setPapScreenSize(String screen, String size, ITvStateSetPapScreenSizeListener listener);

    /**
     * Returns the sub screen position of PIP.
     *
     * @reutrn Returns {@code null} if current screen mode is not PIP.
     * Returns {@code leftTop} if sub screen is in the top left.
     * Returns {@code leftBottom} if sub screen is in the bottom left.
     * Returns {@code rightTop} if sub screen is in the top right.
     * Returns {@code rightBottom} if sub screen is in the bottom right.
     */
    String getPipSubScreenPosition();

    /**
     * Sets the sub screen position of PIP.
     *
     * @param position screen position of PIP. This parameter can be {@code leftTop}, {@code leftBottom}, {@code rightTop} or {@code rightBottom}.
     * @param listener {@link ITvStateSetPipSubScreenPositionListener} to receive the result of this method is success or not.
     */
    int setPipSubScreenPosition(String position, ITvStateSetPipSubScreenPositionListener listener);

    /**
     * Returns the value, which means focused screen.
     *
     * @return the following value:
     * <ul>
     * <li>1 : when PIP BIG screen / when PAP LEFT screen (include SINGLE case)
     * <li>2 : when PIP SMALL screen / when PAP RIGHT screen
     * <li>Other : Error code
     * </ul>
     */
    int getFocusScreen();

    /**
     * Sets the value, which means focus screen.
     *
     * @param position This parameter can be following:
     * <ul>
     * <li>1 : when PIP BIG screen / when PAP LEFT screen
     * <li>2 : when PIP SMALL screen / when PAP RIGHT screen
     * </ul>
     * @return Returns {@code 0} if succeeded, else if error.
     */
    int setFocusScreen(int position);

    /**
     * Send key event to TvPlayer.
     *
     * @param event {@link KeyEvent} to send the key event
     */
    void sendKeyEvent(in KeyEvent event);
}
