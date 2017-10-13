/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2013, Janrain, Inc.
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

package com.janrain.android;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

/**
 * A 'POJO' -- plain old java object, used to contain your configuration for the JUMP library, to be passed
 * to Jump.init()
 */
public final class JumpConfig {
    /**
     * A context to perform IO with
     */
    public Context context;

    /**
     * The application ID of your Engage app, from the Engage Dashboard
     */
    public String engageAppId;

    /**
     * The application ID of your Capture app. Found on the Capture app's dashboard, at the top of the
     * API clients page.
     */
    public String captureAppId;

    /**
     * The domain of your Capture app, contact your deployment engineer for this
     */
    public String captureDomain;

    /**
     * The Capture API client ID for use with this mobile app.
     */
    public String captureClientId;

    /**
     * The name of the Capture flow to operate with. The flow defines the end-user experience, including
     * defining forms and error messages, validation, state transition, etc.
     */
    public String captureFlowName;

    /**
     * Used to specify an explicit flow version. Use null normally, and Jump will fetch the latest ("HEAD")
     * version of the flow.
     */
    public String captureFlowVersion;

    /**
     * The name of the locale to use in the Capture flow
     */
    public String captureLocale;

    /**
     * Controls whether thin registration is enabled. See Jump_Registration_Guide.md.
     */
    public boolean captureEnableThinRegistration;

    /**
     * The name of the Capture sign-in form in the flow
     */
    public String captureTraditionalSignInFormName;

    /**
     * The name of the social registration form in the flow
     */
    public String captureSocialRegistrationFormName;

    /**
     * The name of the traditional (i.e. username and password) registration form in the flow
     */
    public String captureTraditionalRegistrationFormName;

    /**
     * The name of the edit user profile form
     */
    public String captureEditUserProfileFormName;

    /**
     * The type of traditional sign-in. I.e. username or email-address based
     */
    public Jump.TraditionalSignInType traditionalSignInType;


    /**
     * Set this to "true" if you want the Janrain SDK to silently fail, then attempt WebView authentication
     * when the Google+ SDK is integrated but Google Play Services is unavailable.
     *
     * When false, if the Google Play error is SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRE, or
     * SERVICE_DISABLED, then the SDK will present Google's dialog suggesting that the user install or update
     * Google Play Services. After the dialog is dismissed it will call your onFailure method. If the Google
     * Play error is something else, then the SDK will silently fail and attempt WebView authentication.
     *
     * Reference: https://developer.android.com/google/play-services/setup.html#ensure
     *
     * This defaults to true.
     */
    public Boolean tryWebViewAuthenticationWhenGooglePlayIsUnavailable = true;

    /**
     * A list of custom identity providers. See `Engage_Custom_Provider_Guide.md` for details on
     * configuring custom providers
     */
    public final Map<String, JRDictionary> customProviders = new HashMap<String, JRDictionary>();

    private JSONObject configJson;

    /**
     * This constructor is deprecated, please use new constructor {@link JumpConfig#JumpConfig(Context)}
     */
    @Deprecated
    public JumpConfig() {
    }

    /**
     * Creates and initializes a new JumpConfir object to be
     * passed to {@link Jump#init(Context, JumpConfig)}.
     * <br />
     * The fields are initialized using the <b>res/raw/janrain_config.json</b> file
     * which you should provide in your app.
     * @param context
     */
    public JumpConfig(Context context) {
        this.context = context;

        BufferedSource configSource =
                Okio.buffer(Okio.source(context.getResources().openRawResource(R.raw.janrain_config)));
        Buffer configData = new Buffer();
        try {
            configSource.readAll(configData);
            configJson = new JSONObject(configData.readString(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read configuration: " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw new RuntimeException("Unable to parse configuration: " + ex.getMessage(), ex);
        }

        engageAppId = getConfigString("engageAppId");
        captureDomain = getConfigString("captureDomain");
        captureClientId = getConfigString("captureClientId");
        captureLocale = getConfigString("captureLocale");
        captureTraditionalSignInFormName = getConfigString("captureTraditionalSignInFormName");
        traditionalSignInType = getConfigEnum("traditionalSignInType", Jump.TraditionalSignInType.class, null);
        captureAppId = getConfigString("captureAppId");
        captureFlowName = getConfigString("captureFlowName");
        captureFlowVersion = getConfigString("captureFlowVersion");
        captureSocialRegistrationFormName = getConfigString("captureSocialRegistrationFormName");
        captureTraditionalRegistrationFormName = getConfigString("captureTraditionalRegistrationFormName");
        captureEditUserProfileFormName = getConfigString("captureEditUserProfileFormName");
        captureEnableThinRegistration = getConfigBoolean("captureEnableThinRegistration", false);
        captureForgotPasswordFormName = getConfigString("captureForgotPasswordFormName");
    }

    /**
     * Adds a custom SAML provider for Engage authentication
     * @param providerId A short string which will be used to refer to the custom provider
     * @param friendlyName A string representing the user-facing name of the provider
     * @param samlProvider The name of the SAML implementation in Engage for your custom SAML provider
     * @param iconResourceId an optional Resource ID of a 30x30 icon for your custom provider
     */
    public void addCustomSamlProvider(String providerId, String friendlyName, String samlProvider,
                                      int iconResourceId) {
        JRDictionary providerMap = new JRDictionary();
        providerMap.put(JRProvider.KEY_FRIENDLY_NAME, friendlyName);
        providerMap.put(JRProvider.KEY_SAML_PROVIDER, samlProvider);

        if (iconResourceId != 0) {
            providerMap.put(JRProvider.KEY_ICON_RESOURCE_ID, new Integer(iconResourceId));
        }

        customProviders.put(providerId, providerMap);
    }

    /**
     * Adds a custom OpenID provider for Engage authentication
     * @param providerId A short string which will be used to refer to the custom provider
     * @param friendlyName A string representing the user-facing name of the provider
     * @param openIdIdentifier The OpenID identifier of your for your custom OpenID provider
     * @param opxBlob An optional custom "opx_blob" parameter for use with Janrain Identity Service's OpenID
    providers
     * @param iconResourceId an optional Resource ID of a 30x30 icon for your custom provider
     */
    public void addCustomOpenIdProvider(String providerId, String friendlyName,
                                        String openIdIdentifier, String opxBlob,
                                        int iconResourceId) {
        JRDictionary providerMap = new JRDictionary();
        providerMap.put(JRProvider.KEY_FRIENDLY_NAME, friendlyName);
        providerMap.put(JRProvider.KEY_OPENID_IDENTIFIER, openIdIdentifier);

        if (iconResourceId != 0) {
            providerMap.put(JRProvider.KEY_ICON_RESOURCE_ID, new Integer(iconResourceId));
        }
        if (opxBlob != null) {
            providerMap.put(JRProvider.KEY_OPX_BLOB, opxBlob);
        }

        customProviders.put(providerId, providerMap);
    }

    /**
     * The name of the form used to start the reset password flow
     */
    public String captureForgotPasswordFormName;

    /**
     * The name of the form used for resending the verification email
     */
    public String captureResendEmailVerificationFormName;

    /**
     * Allows for customization of the redirectUri
     */
    public String captureRedirectUri;

    /**
     * Used for non-rpx.now Engage app url's
     */
    public String engageAppUrl;
    /**
     * Used for custom Flow endpoints (i.e. China)
     */
    public String downloadFlowUrl;


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

    @Nullable
    private boolean getConfigBoolean(String propName, boolean defaultValue) {
        final String stringValue = getConfigString(propName);
        if (stringValue == null) {
            return defaultValue;
        }

        return stringValue.equals("1") || stringValue.toLowerCase().equals("true");
    }

    @Nullable
    private <T extends Enum<T>> T getConfigEnum(String propName, Class<T> type, T defaultValue) {
        final String stringValue = getConfigString(propName);
        if (stringValue == null) {
            return defaultValue;
        }

        try {
            return Enum.valueOf(type, stringValue);
        } catch (Exception e) {
            String message = String.format(
                    Locale.US,
                    "Failed parse property: %s with value '%s' to enum type %s",
                    propName,
                    stringValue,
                    type.getSimpleName()
            );

            LogUtils.loge(message, e);
        }

        return defaultValue;
    }

}
