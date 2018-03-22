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

package com.janrain.android.multidex.simpledemonative;

import android.content.Context;
import android.support.annotation.Nullable;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;

import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

public final class NativeConfig {
    public Context context;
    public String twitterKey;
    public String twitterSecret;
    public String googleSignInEngageClientId;
    private JSONObject configJson;

    public NativeConfig(Context context) {
        this.context = context;

        BufferedSource configSource =
                Okio.buffer(Okio.source(context.getResources().openRawResource(R.raw.simple_demo_native_config)));
        Buffer configData = new Buffer();
        try {
            configSource.readAll(configData);
            configJson = new JSONObject(configData.readString(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read configuration: " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw new RuntimeException("Unable to parse configuration: " + ex.getMessage(), ex);
        }

        twitterKey = getConfigString("twitter_key");
        twitterSecret = getConfigString("twitter_secret");
        googleSignInEngageClientId = getConfigString("google_signin_engage_app_client_id");
    }

    @Nullable
    private String getConfigString(String propName) {
        String value = configJson.optString(propName);
        if (value == null) {
            return null;
        }

        value = value.trim();
        if (TextUtils.isEmpty(value)) {
            return null;
        }

        return value;
    }
}