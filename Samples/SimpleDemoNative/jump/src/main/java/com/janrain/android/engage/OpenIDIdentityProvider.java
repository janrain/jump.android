/*
 * Copyright 2015 The AppAuth for Android Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.janrain.android.engage;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.BoolRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.janrain.android.R;
import com.janrain.android.engage.session.JRSession;

import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.AuthorizationServiceConfiguration.RetrieveConfigurationCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

/**
 * An abstraction of identity providers, containing all necessary info for the demo app.
 */
class OpenIDIdentityProvider {

    /**
     * Value used to indicate that a configured property is not specified or required.
     */
    public static final int NOT_SPECIFIED = -1;

    /**
     * This constant is deprecated, use it if and only if you're still
     * using the configuration through string resources.
     * <br />
     * <br />
     * Otherwise please use the
     * {@link OpenIDIdentityProvider#getAllProviders(Context)}
     * or
     * {@link OpenIDIdentityProvider#getAllProviders(Context)}
     * methods.
     */
    @Deprecated
    public static final OpenIDIdentityProvider GOOGLE = new OldProvider(
            "Google",
            R.bool.google_enabled,
            R.string.google_discovery_uri,
            NOT_SPECIFIED, // auth endpoint is discovered
            NOT_SPECIFIED, // token endpoint is discovered
            NOT_SPECIFIED, // dynamic registration not supported
            R.string.google_client_id,
            R.string.google_auth_redirect_uri,
            R.string.google_scope_string,
            R.drawable.btn_google,
            R.string.google_name,
            android.R.color.white
    );

    /**
     * This constant is deprecated, use it if and only if you're still
     * using the configuration through string resources.
     * <br />
     * <br />
     * Otherwise please use the
     * {@link OpenIDIdentityProvider#getAllProviders(Context)}
     * or
     * {@link OpenIDIdentityProvider#getAllProviders(Context)}
     * methods.
     */
    @Deprecated
    public static final List<OpenIDIdentityProvider> PROVIDERS = Arrays.asList(GOOGLE);

    private static List<OpenIDIdentityProvider> sProviders;

    /**
     * Gets all the configured OpenID Identity providers
     * @param context Context used to read the configuration file.
     * @return
     */
    public static List<OpenIDIdentityProvider> getAllProviders(Context context) {
        if (sProviders == null) {
            sProviders = new ArrayList<>();
            BufferedSource configSource = Okio.buffer(
                    Okio.source(context.getResources().openRawResource(R.raw.janrain_config))
            );

            JSONArray jsonArray;
            try {
                Buffer configData = new Buffer();
                configSource.readAll(configData);
                JSONObject jsonObject = new JSONObject(configData.readString(Charset.forName("UTF-8")));
                jsonArray = jsonObject.getJSONArray("openIDIdentityProviders");
            } catch (IOException ex) {
                throw new RuntimeException("Failed to read configuration: " + ex.getMessage(), ex);
            } catch (JSONException ex) {
                throw new RuntimeException("Unable to parse configuration: " + ex.getMessage(), ex);
            }

            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                try {
                    final JSONObject jsonObject = jsonArray.getJSONObject(i);
                    sProviders.add(new OpenIDIdentityProvider(jsonObject));
                } catch (JSONException e) {
                    throw new RuntimeException("Failed to read configuration: " + e.getMessage(), e);
                }
            }
        }

        return sProviders;
    }

    public static List<OpenIDIdentityProvider> getProviders(Context context) {
        final ArrayList<OpenIDIdentityProvider> providers = new ArrayList<>();
        for (OpenIDIdentityProvider provider : getAllProviders(context)) {
            provider.readConfiguration(context);
            if (provider.isEnabled()) {
                providers.add(provider);
            }
        }

        return providers;
    }

    /**
     * This method is deprecated, use it if and only if you're still
     * using the configuration through string resources.
     * <br />
     * <br />
     * Otherwise please use the
     * {@link OpenIDIdentityProvider#getAllProviders(Context)}
     * or
     * {@link OpenIDIdentityProvider#getAllProviders(Context)}
     * methods.
     */
    @Deprecated
    public static List<OpenIDIdentityProvider> getEnabledProviders(Context context) {
        ArrayList<OpenIDIdentityProvider> providers = new ArrayList<>();
        for (OpenIDIdentityProvider provider : PROVIDERS) {
            provider.readConfiguration(context);
            if (provider.isEnabled()) {
                providers.add(provider);
            }
        }
        return providers;
    }

    private static boolean isResSpecified(int value) {
        return value != NOT_SPECIFIED;
    }

    private static int checkResSpecified(int value, String valueName) {
        if (!isResSpecified(value)) {
            throw new IllegalArgumentException(valueName + " must be specified");
        }
        return value;
    }

    @NonNull
    public String name;

    /**
     * Configuration through json config file doesn't support this resource. Use only if you're
     * still using configuration through resources
     */
    @Deprecated
    @DrawableRes
    public final int buttonImageRes;

    /**
     * Configuration through json config file doesn't support this resource. Use only if you're
     * still using configuration through resources
     */
    @Deprecated
    @StringRes
    public final int buttonContentDescriptionRes;

    /**
     * Configuration through json config file doesn't support this resource. Use only if you're
     * still using configuration through resources
     */
    @Deprecated
    public final int buttonTextColorRes;

    private final JSONObject configJson;

    protected boolean mConfigurationRead = false;
    protected boolean mEnabled;
    protected Uri mDiscoveryEndpoint;
    protected Uri mAuthEndpoint;
    protected Uri mTokenEndpoint;
    protected Uri mRegistrationEndpoint;
    protected String mClientId;
    protected Uri mRedirectUri;
    protected String mScope;

    /*package*/ JRSession mSession;
    /*package*/ final String TAG = getLogTag();
    /*package*/ String getLogTag() { return getClass().getSimpleName(); }

    public OpenIDIdentityProvider(JSONObject configJson) {
        this(configJson, NOT_SPECIFIED, NOT_SPECIFIED, NOT_SPECIFIED);
    }

    @Deprecated
    protected OpenIDIdentityProvider(
            JSONObject configJson,
            @DrawableRes int buttonImageRes,
            @StringRes int buttonContentDescriptionRes,
            @ColorRes int buttonTextColorRes) {
        this.buttonImageRes = checkResSpecified(buttonImageRes, "buttonImageRes");
        this.buttonContentDescriptionRes =
                checkResSpecified(buttonContentDescriptionRes, "buttonContentDescriptionRes");
        this.buttonTextColorRes = checkResSpecified(buttonTextColorRes, "buttonTextColorRes");
        this.configJson = configJson;
    }

    /**
     * This must be called before any of the getters will function.
     */
    public void readConfiguration(Context context) {
        if (mConfigurationRead) {
            return;
        }

        this.name = getConfigStringMandatory("name");
        this.mEnabled = getConfigBoolean("enabled", true);
        this.mRedirectUri = getConfigUriMandatory("redirect_uri");
        this.mScope = getConfigStringMandatory("authorization_scope");

        // TODO read and validate configuration for the following fields:
//        mDiscoveryEndpointRes;
//        mAuthEndpointRes;
//        mTokenEndpointRes;
//        mRegistrationEndpointRes;
//        mClientIdRes;
//        mRedirectUriRes;
//        mScopeRes;

        mConfigurationRead = true;
    }

    private String getConfigString(String key, String defaultValue) {
        try {
            return configJson.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return defaultValue;
    }

    private String getConfigStringMandatory(String key) {
        try {
            return configJson.getString(key);
        } catch (JSONException e) {
            throw new IllegalArgumentException(key + " must be specified", e);
        }
    }

    private boolean getConfigBoolean(String key, boolean defaultValue) {
        final String value = getConfigString(key, null);
        if (value == null) {
            return defaultValue;
        }

        return value.equals("1") || value.toLowerCase().equals("true");
    }

    private Uri getConfigUriMandatory(String key) {
        final String value = getConfigStringMandatory(key);
        return Uri.parse(value);
    }

    private void checkConfigurationRead() {
        if (!mConfigurationRead) {
            throw new IllegalStateException("Configuration not read");
        }
    }

    public boolean isEnabled() {
        checkConfigurationRead();
        return mEnabled;
    }

    @Nullable
    public Uri getDiscoveryEndpoint() {
        checkConfigurationRead();
        return mDiscoveryEndpoint;
    }

    @Nullable
    public Uri getAuthEndpoint() {
        checkConfigurationRead();
        return mAuthEndpoint;
    }

    @Nullable
    public Uri getTokenEndpoint() {
        checkConfigurationRead();
        return mTokenEndpoint;
    }

    public String getClientId() {
        checkConfigurationRead();
        return mClientId;
    }


    public void setClientId(String clientId) {
        mClientId = clientId;
    }

    @NonNull
    public Uri getRedirectUri() {
        checkConfigurationRead();
        return mRedirectUri;
    }

    @NonNull
    public String getScope() {
        checkConfigurationRead();
        return mScope;
    }

    public void retrieveConfig(Context context,
                               RetrieveConfigurationCallback callback) {
        readConfiguration(context);
        if (getDiscoveryEndpoint() != null) {
            AuthorizationServiceConfiguration.fetchFromUrl(mDiscoveryEndpoint, callback);
        } else {
            AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(
                    mAuthEndpoint,
                    mTokenEndpoint,
                    mRegistrationEndpoint
            );

            callback.onFetchConfigurationCompleted(config, null);
        }
    }

    private static Uri getUriResource(Resources res, @StringRes int resId, String resName) {
        return Uri.parse(res.getString(resId));
    }

    /**
     * Configuration through resources is deprecated, please use the new json config file
     */
    @Deprecated
    private static class OldProvider extends OpenIDIdentityProvider {

        @BoolRes
        private final int mEnabledRes;

        @StringRes
        private final int mDiscoveryEndpointRes;

        @StringRes
        private final int mAuthEndpointRes;

        @StringRes
        private final int mTokenEndpointRes;

        @StringRes
        private final int mRegistrationEndpointRes;

        @StringRes
        private final int mClientIdRes;

        @StringRes
        private final int mRedirectUriRes;

        @StringRes
        private final int mScopeRes;

        OldProvider(
                @NonNull String name,
                @BoolRes int enabledRes,
                @StringRes int discoveryEndpointRes,
                @StringRes int authEndpointRes,
                @StringRes int tokenEndpointRes,
                @StringRes int registrationEndpointRes,
                @StringRes int clientIdRes,
                @StringRes int redirectUriRes,
                @StringRes int scopeRes,
                @DrawableRes int buttonImageRes,
                @StringRes int buttonContentDescriptionRes,
                @ColorRes int buttonTextColorRes) {
            super(null, buttonImageRes, buttonContentDescriptionRes, buttonTextColorRes);
            if (!isResSpecified(discoveryEndpointRes)
                    && !isResSpecified(authEndpointRes)
                    && !isResSpecified(tokenEndpointRes)) {
                throw new IllegalArgumentException(
                        "the discovery endpoint or the auth and token endpoints must be specified");
            }

            this.name = name;
            this.mEnabledRes = checkResSpecified(enabledRes, "enabledRes");
            this.mDiscoveryEndpointRes = discoveryEndpointRes;
            this.mAuthEndpointRes = authEndpointRes;
            this.mTokenEndpointRes = tokenEndpointRes;
            this.mRegistrationEndpointRes = registrationEndpointRes;
            this.mClientIdRes = clientIdRes;
            this.mRedirectUriRes = checkResSpecified(redirectUriRes, "redirectUriRes");
            this.mScopeRes = checkResSpecified(scopeRes, "scopeRes");
        }


        /**
         * This must be called before any of the getters will function.
         */
        public void readConfiguration(Context context) {
            if (mConfigurationRead) {
                return;
            }

            Resources res = context.getResources();
            mEnabled = res.getBoolean(mEnabledRes);

            mDiscoveryEndpoint = isResSpecified(mDiscoveryEndpointRes)
                    ? getUriResource(res, mDiscoveryEndpointRes, "discoveryEndpointRes")
                    : null;
            mAuthEndpoint = isResSpecified(mAuthEndpointRes)
                    ? getUriResource(res, mAuthEndpointRes, "authEndpointRes")
                    : null;
            mTokenEndpoint = isResSpecified(mTokenEndpointRes)
                    ? getUriResource(res, mTokenEndpointRes, "tokenEndpointRes")
                    : null;
            mRegistrationEndpoint = isResSpecified(mRegistrationEndpointRes)
                    ? getUriResource(res, mRegistrationEndpointRes, "registrationEndpointRes")
                    : null;
            mClientId = isResSpecified(mClientIdRes)
                    ? res.getString(mClientIdRes)
                    : null;
            mRedirectUri = getUriResource(res, mRedirectUriRes, "mRedirectUriRes");
            mScope = res.getString(mScopeRes);

            mConfigurationRead = true;
        }
    }
}
