/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.sony.dtv.tvcamera.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.SparseIntArray;

import com.sony.dtv.tvcamera.R;

public class TVCameraMediaActionSound {
	private SoundPool mSoundPool;
	private SparseIntArray mSoundMap = new SparseIntArray();
	private final Handler mHandler = new Handler();

	private static final float FULL_LEFT_VOLUME = 1.0f;
	private static final float FULL_RIGHT_VOLUME = 1.0f;
	private static final int DEFAULT_PRIORITY = 1;
	private static final int DO_NOT_LOOP = 0;
	private static final float DEFAULT_RATE = 1.0f;

	private static final String TAG = "TVCameraMediaActionSound";

	/**
	 * Construct a new MediaActionSound instance. Only a single instance is
	 * needed for playing any platform media action sound; you do not need a
	 * separate instance for each sound type.
	 */
	public TVCameraMediaActionSound(Context context) {
		mSoundPool = new SoundPool(2, AudioManager.STREAM_SYSTEM, 0);
		loadSounds(context);
	}

	private void loadSounds(Context context) {
		int[] sounds = {
				R.raw.scan_kasha,
				R.raw.selftimer
		};
		for (int sound : sounds) {
			mSoundMap.put(sound, mSoundPool.load(context, sound, 1));
		}
	}

	private void play(final int resId) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				int sound = mSoundMap.get(resId);
				mSoundPool.play(sound, FULL_LEFT_VOLUME, FULL_RIGHT_VOLUME, DEFAULT_PRIORITY,
						DO_NOT_LOOP, DEFAULT_RATE);
			}
		});
	}

	public void playScanKaSha() {
		play(R.raw.scan_kasha);
	}

	public void playSelfTimer() {
		play(R.raw.selftimer);
	}
	/**
	 * Free up all audio resources used by this MediaActionSound instance. Do
	 * not call any other methods on a MediaActionSound instance after calling
	 * release().
	 */
	public void release() {
		if (mSoundPool != null) {
			mSoundPool.release();
			mSoundPool = null;
		}
	}
}
