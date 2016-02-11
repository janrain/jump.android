/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2011, Janrain, Inc.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *  * Neither the name of the Janrain, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
package com.janrain.android.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.util.*;
import com.janrain.android.engage.JREngage;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * @internal
 * @class PrefUtils
 * Utility class used for managing preferences in the Android global shared preferences.
 */
public final class PrefUtils {
    public static final String KEY_JR_USER_INPUT = "jr_pref_user_input.";
    public static final String KEY_JR_FORCE_REAUTH = "jr_pref_force_reauth.";
    public static final String KEY_JR_HIDE_POWERED_BY = "jr_hide_powered_by";
    public static final String KEY_JR_CONFIGURATION_ETAG = "jr_configuration_etag";
    public static final String KEY_JR_LAST_USED_AUTH_PROVIDER = "jr_last_used_auth_provider";
    public static final String KEY_JR_LAST_USED_AUTH_PROVIDER_PERMISSIONS = "jr_last_used_auth_provider_permissions";
    public static final String KEY_JR_LAST_USED_SHARING_PROVIDER = "jr_last_used_social_provider";
    public static final String KEY_JR_RP_BASE_URL = "jr_base_url";
    public static final String KEY_JR_ENGAGE_LIBRARY_VERSION = "jr_engage_library_version";
    public static final String KEY_JR_USER_COMMENT = "jr_user_comment";
    public static final String KEY_JR_USER_COMMENT_TIME = "jr_user_comment_time";
    public static final String KEY_JR_UNIVERSALLY_UNIQUE_ID = "jr_universally_unique_id";

    private PrefUtils() {}

    /**
     * Wrapper for getting shared preference string value by key.
     *
     * @param key      The key of the preference to get.
     * @param defValue The default value if not found.
     * @return The value of the preference, or defValue if not found.
     */
    public static String getString(String key, String defValue) {
        return getSharedPreferences().getString(key, defValue);
    }

    /**
     * Wrapper for getting shared preference boolean value by key.
     *
     * @param key      The key of the preference to get.
     * @param defValue The default value if not found.
     * @return The value of the preference, or defValue if not found.
     */
    public static boolean getBoolean(String key, boolean defValue) {
        return getSharedPreferences().getBoolean(key, defValue);
    }

    /**
     * Wrapper for getting shared preference integer value by key.
     *
     * @param key      The key of the preference to get.
     * @param defValue The default value if not found.
     * @return The value of the preference, or defValue if not found.
     */
    public static int getInt(String key, int defValue) {
        return getSharedPreferences().getInt(key, defValue);
    }

    public static void putString(String key, String value) {
        sharedPreferenceEditorApplyOrCommit(getEditor().putString(key, value));
    }

    public static void putStringArray(String key, String[] values, String delimiter) {
    	String value = "";
    	if(values!=null && values.length>0){
    		value = TextUtils.join(delimiter, values); 
    	}
        sharedPreferenceEditorApplyOrCommit(getEditor().putString(key, value));
    }
    
    /**
     * Wrapper for getting shared preference String Array value by key.
     *
     * @param key      The key of the preference to get.
     * @param defValue The default value if not found.
     * @param delimiter The delimiter used to convert the string to array.
     * @return The value of the preference, or defValue if not found.
     */
    @SuppressWarnings("null")
	public static String[] getStringArray(String key, String[] defValue, String delimiter){
    	String[] result = null;
    	String value = getSharedPreferences().getString(key, "");
    	if(value.indexOf(delimiter)>= 0){
    		result = value.split(delimiter);
    	}else{
    		if(value.length() > 0){
    			result[0] = value;
    		}
    	}
		return result;
    	
    }

    public static void putBoolean(String key, boolean value) {
        sharedPreferenceEditorApplyOrCommit(getEditor().putBoolean(key, value));
    }

    public static void putInt(String key, int value) {
        sharedPreferenceEditorApplyOrCommit(getEditor().putInt(key, value));
    }

    public static void putLong(String key, long value) {
        sharedPreferenceEditorApplyOrCommit(getEditor().putLong(key, value));
    }

    public static long getLong(String key, int defaultValue) {
        return getSharedPreferences().getLong(key, defaultValue);
    }

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(JREngage.getApplicationContext());
    }

    private static SharedPreferences.Editor getEditor() {
        return getSharedPreferences().edit();
    }

    public static void remove(String key) {
        sharedPreferenceEditorApplyOrCommit(getEditor().remove(key));
    }

    private static void sharedPreferenceEditorApplyOrCommit(SharedPreferences.Editor editor) {
        try {
            editor.getClass().getMethod("apply").invoke(editor);
        } catch (NoSuchMethodException ignore) {
        } catch (InvocationTargetException ignore) {
        } catch (IllegalAccessException ignore) {
        }

        editor.commit();
    }
}