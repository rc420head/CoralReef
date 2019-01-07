/*
 * Copyright (C) 2018 Havoc-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.candy.candyshop.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.database.ContentObserver;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

import com.android.settings.R;
import com.android.internal.logging.nano.MetricsProto; 
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.util.candy.AwesomeAnimationHelper;

import org.candy.candyshop.preference.SystemSettingSeekBarPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Animations extends SettingsPreferenceFragment  implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "Animations";
    private static final String SCREEN_OFF_ANIMATION = "screen_off_animation";
    private static final String KEY_TOAST_ANIMATION = "toast_animation";
    private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
    private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
    private static final String PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";

    private ListPreference mScreenOffAnimation;
    private ListPreference mToastAnimation;
    private ListPreference mTileAnimationStyle;
    private ListPreference mTileAnimationDuration;
    private ListPreference mTileAnimationInterpolator;

    protected Context mContext;

    private int[] mAnimations;
    private String[] mAnimationsStrings;
    private String[] mAnimationsNum;

    Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.candy_animations);

        mContext = getActivity().getApplicationContext();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        // Screen Off Animations 
        mScreenOffAnimation = (ListPreference) findPreference(SCREEN_OFF_ANIMATION); 
        int screenOffStyle = Settings.Global.getInt(resolver, 
                 Settings.Global.SCREEN_OFF_ANIMATION, 0); 
        mScreenOffAnimation.setValue(String.valueOf(screenOffStyle)); 
        mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntry()); 
        mScreenOffAnimation.setOnPreferenceChangeListener(this); 

        // Toast animations
        mToastAnimation = (ListPreference) findPreference(KEY_TOAST_ANIMATION);
        mToastAnimation.setSummary(mToastAnimation.getEntry());
        int CurrentToastAnimation = Settings.System.getInt(getContentResolver(), Settings.System.TOAST_ANIMATION, 1);
        mToastAnimation.setValueIndex(CurrentToastAnimation); //set to index of default value
        mToastAnimation.setSummary(mToastAnimation.getEntries()[CurrentToastAnimation]);
        mToastAnimation.setOnPreferenceChangeListener(this);

        // QS animations
        mTileAnimationStyle = (ListPreference) findPreference(PREF_TILE_ANIM_STYLE);
        mTileAnimationStyle.setOnPreferenceChangeListener(this);
        int tileAnimationStyle = Settings.System.getIntForUser(resolver,
                Settings.System.ANIM_TILE_STYLE, 0,
                UserHandle.USER_CURRENT);

        mTileAnimationDuration = (ListPreference) findPreference(PREF_TILE_ANIM_DURATION);
        mTileAnimationDuration.setEnabled(tileAnimationStyle > 0);

        mTileAnimationInterpolator = (ListPreference) findPreference(PREF_TILE_ANIM_INTERPOLATOR);
        mTileAnimationInterpolator.setEnabled(tileAnimationStyle > 0);

        mAnimations = AwesomeAnimationHelper.getAnimationsList();
        int animqty = mAnimations.length;
        mAnimationsStrings = new String[animqty];
        mAnimationsNum = new String[animqty];
        for (int i = 0; i < animqty; i++) {
            mAnimationsStrings[i] = AwesomeAnimationHelper.getProperName(getActivity().getApplicationContext(), mAnimations[i]);
            mAnimationsNum[i] = String.valueOf(mAnimations[i]);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == mScreenOffAnimation) { 
            Settings.Global.putInt(getContentResolver(), 
                    Settings.Global.SCREEN_OFF_ANIMATION, Integer.valueOf((String) newValue)); 
            int valueIndex = mScreenOffAnimation.findIndexOfValue((String) newValue); 
            mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntries()[valueIndex]); 
            return true; 
        } else if (preference == mToastAnimation) {
            int index = mToastAnimation.findIndexOfValue((String) newValue);
            Settings.System.putString(getContentResolver(), Settings.System.TOAST_ANIMATION, (String) newValue);
            mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
            Toast.makeText(mContext, "Toast Test", Toast.LENGTH_SHORT).show();
            return true;
        } else if (preference == mTileAnimationStyle) {
            int value = Integer.valueOf((String) newValue);
            mTileAnimationDuration.setEnabled(value > 0);
            mTileAnimationInterpolator.setEnabled(value > 0);
            return true;

        }
        return false; 
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putInt(resolver,
                Settings.Global.SCREEN_OFF_ANIMATION, 0);
        Settings.System.putIntForUser(resolver,
                Settings.System.ANIM_TILE_STYLE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ANIM_TILE_DURATION, 2000, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ANIM_TILE_INTERPOLATOR, 0, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CANDY;
    }
}
