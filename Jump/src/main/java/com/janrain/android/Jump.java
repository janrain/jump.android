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
package com.janrain.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import com.janrain.android.capture.Capture;
import com.janrain.android.capture.CaptureApiError;
import com.janrain.android.capture.CaptureFlowUtils;
import com.janrain.android.capture.CaptureRecord;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.types.JRDictionary;

import com.janrain.android.engage.ui.JRCustomInterface;
import com.janrain.android.utils.AndroidUtils;
import com.janrain.android.utils.ApiConnection;
import com.janrain.android.utils.JsonUtils;
import com.janrain.android.utils.LogUtils;
import com.janrain.android.utils.ThreadUtils;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Map;

import static com.janrain.android.Jump.SignInResultHandler.SignInError;
import static com.janrain.android.Jump.SignInResultHandler.SignInError.FailureReason.AUTHENTICATION_CANCELED_BY_USER;
import static com.janrain.android.Jump.SignInResultHandler.SignInError.FailureReason.CAPTURE_API_ERROR;
import static com.janrain.android.Jump.SignInResultHandler.SignInError.FailureReason.ENGAGE_ERROR;
import static com.janrain.android.Jump.SignInResultHandler.SignInError.FailureReason.JUMP_NOT_INITIALIZED;
import static com.janrain.android.Jump.ForgotPasswordResultHandler.ForgetPasswordError;
import static com.janrain.android.Jump.ForgotPasswordResultHandler.ForgetPasswordError.FailureReason.
        FORGOTPASSWORD_JUMP_NOT_INITIALIZED;
import static com.janrain.android.Jump.CaptureApiResultHandler.CaptureAPIError;
import static com.janrain.android.Jump.CaptureApiResultHandler.CaptureAPIError.FailureReason.CAPTURE_API_FORMAT_ERROR;
import static com.janrain.android.utils.LogUtils.throwDebugException;

/**
 * See jump.android/Jump_Integration_Guide.md for a developer's integration guide.
 */
public class Jump {
    public static final String JR_FAILED_TO_DOWNLOAD_FLOW = "com.janrain.android.Jump.FAILED_TO_DOWNLOAD_FLOW";

    private static final String JR_CAPTURE_FLOW = "jr_capture_flow";

    public static final String JR_DOWNLOAD_FLOW_SUCCESS = "com.janrain.android.Jump.DOWNLOAD_FLOW_SUCCESS";

    /*package*/ enum State {
        STATE;

        // Computed values:
        /*package*/ CaptureRecord signedInUser;
        /*package*/ JREngage jrEngage;
        /*package*/ Map<String, Object> captureFlow;
        /*package*/ String refreshSecret;

        // Configured values:
        /*package*/ Context context;
        /*package*/ String captureAppId;
        /*package*/ String captureClientId;
        /*package*/ String captureDomain;
        /*package*/ boolean flowUsesTestingCdn;
        /*package*/ String captureFlowName;
        /*package*/ String captureFlowVersion;
        /*package*/ String captureLocale;
        /*package*/ boolean captureEnableThinRegistration;
        /*package*/ String captureTraditionalSignInFormName;
        /*package*/ String captureSocialRegistrationFormName;
        /*package*/ String captureTraditionalRegistrationFormName;
        /*package*/ String captureEditUserProfileFormName;
        /*package*/ TraditionalSignInType traditionalSignInType;
        /*package*/ String captureForgotPasswordFormName;
        /*package*/ String captureResendEmailVerificationFormName;
        /*package*/ String userAgent;
        /*package*/ String accessToken;
        /*package*/ String captureRedirectUri;

        // Transient state values:
        /*
         * Every method that performs a sign-in or registration must set signInHandler and call
         * fireHandlerOnFailure and fireHandlerOnSuccess when the operation has completed.
         */
        /*package*/ SignInResultHandler signInHandler;
        /*package*/ CaptureApiResultHandler captureAPIHandler;
        public boolean initCalled;
    }

    /*package*/ static final State state = State.STATE;

    private Jump() {}

    /**
     * @deprecated
     * Initialize the Jump library with you configuration data
     */
    public static void init(Context context,
                            String engageAppId,
                            String captureDomain,
                            String captureClientId,
                            String captureLocale,
                            String captureTraditionalSignInFormName,
                            TraditionalSignInType traditionalSignInType) {
        //j.showAuthenticationDialog();
        //j.addDelegate(); remove
        //j.cancelAuthentication();
        //j.createSocialPublishingFragment();
        //j.setAlwaysForceReauthentication();
        //j.setEnabledAuthenticationProviders();
        //j.setTokenUrl();
        //j.signoutUserForAllProviders();
        JumpConfig jumpConfig = new JumpConfig();
        jumpConfig.engageAppId = engageAppId;
        jumpConfig.captureDomain = captureDomain;
        jumpConfig.captureClientId = captureClientId;
        jumpConfig.captureLocale = captureLocale;
        jumpConfig.captureTraditionalSignInFormName = captureTraditionalSignInFormName;
        jumpConfig.traditionalSignInType = traditionalSignInType;
        init(context, jumpConfig);
    }

    /**
     * Initializes the Jump library. It is recommended to call this method from your Application object.
     * Initialization will cause some network and disk IO to be performed (on a background thread) so it
     * is recommended that the library be initialized before it is used.
     * @param context a context to perform IO from
     * @param jumpConfig an instance of JumpConfig which contains your configuration values. These values
     */
    public static synchronized void init(Context context, JumpConfig jumpConfig) {
        if (state.initCalled) throwDebugException(new IllegalStateException("Multiple Jump.init() calls"));
        state.initCalled = true;

        state.context = context;
        state.jrEngage = JREngage.initInstance(context.getApplicationContext(), jumpConfig.engageAppId,
                null, null, jumpConfig.customProviders);
        state.captureSocialRegistrationFormName = jumpConfig.captureSocialRegistrationFormName;
        state.captureTraditionalRegistrationFormName = jumpConfig.captureTraditionalRegistrationFormName;
        state.captureEditUserProfileFormName = jumpConfig.captureEditUserProfileFormName;
        state.captureEnableThinRegistration = jumpConfig.captureEnableThinRegistration;
        state.captureFlowName = jumpConfig.captureFlowName;
        state.captureFlowVersion = jumpConfig.captureFlowVersion;
        state.captureDomain = jumpConfig.captureDomain;
        state.captureAppId = jumpConfig.captureAppId;
        state.captureClientId = jumpConfig.captureClientId;
        state.traditionalSignInType = jumpConfig.traditionalSignInType;
        state.captureLocale = jumpConfig.captureLocale;
        state.captureTraditionalSignInFormName = jumpConfig.captureTraditionalSignInFormName;
        state.captureForgotPasswordFormName = jumpConfig.captureForgotPasswordFormName;
        state.captureResendEmailVerificationFormName = jumpConfig.captureResendEmailVerificationFormName;
        if(jumpConfig.captureRedirectUri == null){
            state.captureRedirectUri = "http://android.library";
        }else{
            state.captureRedirectUri = jumpConfig.captureRedirectUri;
        }



        final Context tempContext = context;
        ThreadUtils.executeInBg(new Runnable() {
            public void run() {
                loadUserFromDiskInternal(tempContext);
                loadRefreshSecretFromDiskInternal(tempContext);

                if (state.captureLocale != null && state.captureFlowName != null &&
                        state.captureAppId != null) {
                    loadFlow();
                    downloadFlow();
                }
            }
        });
    }


    public static String getCaptureDomain() {
        return state.captureDomain;
    }

    /**
     * Change the Capture Domain that will be used as the base URL for Capture request
     * @param domain
     *   The new Capture domain
     */
    public static void setCaptureDomain(String domain) {
        state.captureDomain = domain;
    }

    public static String getCaptureClientId() {
        return state.captureClientId;
    }

    /**
     * Change the Capture Client ID that will be used in requests to Capture
     * @param clientId
     *   The new Capture Client ID
     */
    public static void setCaptureClientId(String clientId) {
        state.captureClientId = clientId;
    }

    public static String getCaptureLocale() {
        return state.captureLocale;
    }

    public static String getCaptureTraditionalSignInFormName() {
        return state.captureTraditionalSignInFormName;
    }

    /**
     * Change the engage app ID and reload the Engage configuration data
     * @param engageAppId
     *   The new Engage app ID
     */
    public static void reconfigureWithNewEngageAppId(String engageAppId) {
        state.jrEngage.changeEngageAppId(engageAppId);
    }

    /**
     * @deprecated use com.janrain.android.Jump#getCaptureTraditionalSignInFormName
     */
    public static String getCaptureFormName() {
        return getCaptureTraditionalSignInFormName();
    }

    public static String getCaptureSocialRegistrationFormName() {
        return state.captureSocialRegistrationFormName;
    }

    public static String getCaptureTraditionalRegistrationFormName() {
        return state.captureTraditionalRegistrationFormName;
    }

    public static String getCaptureEditUserProfileFormName() {
        return state.captureEditUserProfileFormName;
    }

    public static String getCaptureForgotPasswordFormName() {
        return state.captureForgotPasswordFormName;
    }

    public static String getCaptureResendEmailVerificationFormName() {
        return state.captureResendEmailVerificationFormName;
    }

    public static String getResponseType() {
        SignInResultHandler handler = state.signInHandler;

        if (handler instanceof SignInCodeHandler) {
            return "code_and_token";
        }
        return "token";
    }

    public static String getCaptureAppId() {
        return state.captureAppId;
    }

    public static String getCaptureFlowName() {
        return state.captureFlowName;
    }

    public static Map<String, Object> getCaptureFlow() {
        return state.captureFlow;
    }

    public static String getRefreshSecret() {
        return state.refreshSecret;
    }

    public static void setRefreshSecret(String secret) {
        state.refreshSecret = secret;
        saveToken(secret, Capture.JR_REFRESH_SECRET);
    }

    public static String getRedirectUri() {
        return state.captureRedirectUri;
    }

    public static void setRedirectUri(String redirectUri) {
        state.captureRedirectUri = redirectUri;
    }

    public static boolean getCaptureEnableThinRegistration() {
        return state.captureEnableThinRegistration;
    }

    /**
     * @return the currently signed-in user, or null
     */
    public static CaptureRecord getSignedInUser() {
        return state.signedInUser;
    }

    public static String getCustomUserAgentContext() {
        ApplicationInfo ai = AndroidUtils.getApplicationInfo();
        if (state.context == null) return null;
        String packageName = state.context.getApplicationContext().getPackageName();
        PackageInfo info = null;
        try {
            info = state.context.getApplicationContext().getPackageManager().getPackageInfo(packageName, 0);
            state.userAgent = state.context.getApplicationContext().getPackageManager().getApplicationLabel(ai).toString();
            state.userAgent += "/" + info.versionCode + " ";
        } catch (PackageManager.NameNotFoundException e) {
            throwDebugException(new RuntimeException("User agent create failed : ", e));
        }
        return state.userAgent;
    }

    public static String getAccessToken() {
        return state.signedInUser.getAccessToken();
    }

    /**
     * Starts the Capture sign-in flow with Permissions/Scopes from JumpConfig.
     *
     * If the providerName parameter is not null and is a valid provider name string then authentication
     * begins directly with that provider.
     *
     * If providerName is null than a list of available providers is displayed first.
     *
     * @param fromActivity the activity from which to start the dialog activity
     * @param providerName the name of the provider to show the sign-in flow for. May be null.
     *                     If null, a list of providers (and a traditional sign-in form) is displayed to the
     *                     end-user.
     * @param permissions  the Permissions/Scopes from the JumpConfig 
     *                     Used for Native Authentication of Facebook and Google+
     * @param handler your result handler, called upon completion on the UI thread
     * @param mergeToken an Engage auth_info token retrieved from an EMAIL_ADDRESS_IN_USE Capture API error,
     *                   or null for none.
     */
    public static void showSignInDialog(Activity fromActivity, String providerName, String[] permissions, 
                                        SignInResultHandler handler, final String mergeToken) {
        if (state.jrEngage == null || state.captureDomain == null) {
            handler.onFailure(new SignInError(JUMP_NOT_INITIALIZED, null, null));
            return;
        }

        state.signInHandler = handler;
        if ("capture".equals(providerName)) {
            TradSignInUi.showStandAloneDialog(fromActivity, mergeToken);
        } else {
            showSocialSignInDialog(fromActivity, providerName, mergeToken);
        }
    }

    /**
     * Starts the Capture sign-in flow.
     *
     * If the providerName parameter is not null and is a valid provider name string then authentication
     * begins directly with that provider.
     *
     * If providerName is null than a list of available providers is displayed first.
     *
     * @param fromActivity the activity from which to start the dialog activity
     * @param providerName the name of the provider to show the sign-in flow for. May be null.
     *                     If null, a list of providers (and a traditional sign-in form) is displayed to the
     *                     end-user.
     * @param handler your result handler, called upon completion on the UI thread
     * @param mergeToken an Engage auth_info token retrieved from an EMAIL_ADDRESS_IN_USE Capture API error,
     *                   or null for none.
     */
    public static void showSignInDialog(Activity fromActivity, String providerName,
                                        SignInResultHandler handler, final String mergeToken) {
        if (state.jrEngage == null || state.captureDomain == null) {
            handler.onFailure(new SignInError(JUMP_NOT_INITIALIZED, null, null));
            return;
        }

        state.signInHandler = handler;
        if ("capture".equals(providerName)) {
            TradSignInUi.showStandAloneDialog(fromActivity, mergeToken);
        } else {
            showSocialSignInDialog(fromActivity, providerName, mergeToken);
        }
    }

    private static void showSocialSignInDialog(Activity fromActivity, String providerName, final String mergeToken) {
        state.jrEngage.addDelegate(new JREngageDelegate.SimpleJREngageDelegate() {
            @Override
            public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
                handleEngageAuthenticationSuccess(auth_info, provider, mergeToken);
                state.jrEngage.removeDelegate(this);
            }

            @Override
            public void jrAuthenticationDidNotComplete() {
                fireHandlerFailure(new SignInError(AUTHENTICATION_CANCELED_BY_USER, null, null));
            }

            @Override
            public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
                fireHandlerFailure(new SignInError(ENGAGE_ERROR, null, error));
            }

            @Override
            public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
                fireHandlerFailure(new SignInError(ENGAGE_ERROR, null, error));
            }

            private void fireHandlerFailure(SignInError err) {
                state.jrEngage.removeDelegate(this);
                Jump.fireHandlerOnFailure(err);
            }
        });

        if (providerName != null) {
            state.jrEngage.showAuthenticationDialog(fromActivity, providerName);
        } else {
            state.jrEngage.showAuthenticationDialog(fromActivity, TradSignInUi.class);
        }
    }

    public static void startTokenAuthForNativeProvider(final Activity fromActivity,
                                                       final String providerName,
                                                       final String accessToken,
                                                       final String tokenSecret,
                                                       SignInResultHandler handler,
                                                       final String mergeToken) {
        if (state.jrEngage == null || state.captureDomain == null) {
            handler.onFailure(new SignInError(JUMP_NOT_INITIALIZED, null, null));
            return;
        }

        state.signInHandler = handler;
        nextTokenAuthForNativeProvider(fromActivity,providerName,accessToken,tokenSecret,mergeToken);

    }

    private static void nextTokenAuthForNativeProvider(Activity fromActivity,
                                                       String providerName,
                                                       final String accessToken,
                                                       final String tokenSecret,
                                                       final String mergeToken) {
        state.jrEngage.addDelegate(new JREngageDelegate.SimpleJREngageDelegate() {
            @Override
            public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
                handleEngageAuthenticationSuccess(auth_info, provider, mergeToken);
                state.jrEngage.removeDelegate(this);
            }

            @Override
            public void jrAuthenticationDidNotComplete() {
                fireHandlerFailure(new SignInError(AUTHENTICATION_CANCELED_BY_USER, null, null));
            }

            @Override
            public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
                fireHandlerFailure(new SignInError(ENGAGE_ERROR, null, error));
            }

            private void fireHandlerFailure(SignInError err) {
                state.jrEngage.removeDelegate(this);
                Jump.fireHandlerOnFailure(err);
            }
        });


        if (providerName != null && accessToken != null) {
            state.jrEngage.getAuthInfoTokenForNativeProvider(fromActivity, providerName, accessToken, tokenSecret);
        }else{
            LogUtils.logd("Provider Name or Access Token can not be null");
        }
    }

    private static void handleEngageAuthenticationSuccess(final JRDictionary auth_info, String provider,
                                                          String mergeToken) {
        String authInfoToken = auth_info.getAsString("token");

        Capture.performSocialSignIn(authInfoToken, new Capture.SignInResultHandler() {
            public void onSuccess(CaptureRecord record, JSONObject response) {
                state.signedInUser = record;
                Jump.fireHandlerOnSuccess(response);
            }

            public void onFailure(CaptureApiError error) {
                Jump.fireHandlerOnFailure(new SignInError(CAPTURE_API_ERROR, error, null, auth_info));
            }
        }, provider, mergeToken);
    }

    /**
     * @deprecated
     */
    public static void showSignInDialog(Activity fromActivity, String providerName,
                                        SignInResultHandler handler) {
        showSignInDialog(fromActivity, providerName, handler, null);
    }

    /**
     * Signs the signed-in user out, and removes their record from disk.
     * @param applicationContext the application context used to interact with the disk
     */
    public static void signOutCaptureUser(Context applicationContext) {
        state.signedInUser = null;
        state.refreshSecret = null;
        CaptureRecord.deleteFromDisk(applicationContext);
    }



    /*package*/ static void fireHandlerOnFailure(SignInError failureParam) {
        SignInResultHandler handler_ = state.signInHandler;
        state.signInHandler = null;
        if (handler_ != null) handler_.onFailure(failureParam);
    }

    // Package level because of use from TradSignInUi:
    /*package*/ static void fireHandlerOnSuccess(JSONObject response) {
        SignInResultHandler handler_ = state.signInHandler;
        state.signInHandler = null;
        if (handler_ != null) {
            handler_.onSuccess();

            if (handler_ instanceof SignInCodeHandler) {
                String code = response.optString("authorization_code");
                ((SignInCodeHandler)handler_).onCode(code);
            }
        }
    }


    public enum TraditionalSignInType { EMAIL, USERNAME }

    /**
     * Headless API for Capture traditional account sign-in
     * @param signInName the end user's user name or email address
     * @param password the end user's password
     * @param handler your callback handler, invoked upon completion in the UI thread
     * @param mergeToken an Engage auth_info token retrieved from an EMAIL_ADDRESS_IN_USE Capture API error,
     *                   or null for none.
     */
    public static void performTraditionalSignIn(String signInName, String password,
                                                final SignInResultHandler handler, final String mergeToken) {
        if (state.jrEngage == null || state.captureDomain == null) {
            handler.onFailure(new SignInError(JUMP_NOT_INITIALIZED, null, null));
            return;
        }

        state.signInHandler = handler;

        Capture.performTraditionalSignIn(signInName, password,
                new Capture.SignInResultHandler() {
                    @Override
                    public void onSuccess(CaptureRecord record, JSONObject response) {
                        state.signedInUser = record;
                        fireHandlerOnSuccess(response);
                    }

                    @Override
                    public void onFailure(CaptureApiError error) {
                        fireHandlerOnFailure(new SignInError(CAPTURE_API_ERROR, error, null));
                    }
                }, mergeToken);
    }

    /**
     * Registers a new user record with Capture. Used for both traditional registrations and social two-step
     * registrations.
     *
     * Requires:
     *  - a flow name be configured when calling Jump.init
     *  - a social registration form be configured
     *  - a traditional registration form be configured
     *  - the Capture app ID be configured
     *
     * @param newUser A JSON object (which matches the record schema) used to populate the fields of the
     *                registration form.
     * @param socialRegistrationToken A social registration token, or null to perform a traditional
     *                                registration
     * @param registrationResultHandler A handler for the registration result
     */
    public static void registerNewUser(JSONObject newUser,
                                       String socialRegistrationToken,
                                       final SignInResultHandler registrationResultHandler) {
        if (state.jrEngage == null || state.captureDomain == null || state.captureFlowName == null ||
                state.captureSocialRegistrationFormName == null ||
                state.captureTraditionalRegistrationFormName == null || state.captureAppId == null) {
            registrationResultHandler.onFailure(new SignInError(JUMP_NOT_INITIALIZED, null, null));
            return;
        }

        state.signInHandler = registrationResultHandler;

        Capture.performRegistration(newUser, socialRegistrationToken, new Capture.SignInResultHandler(){
            public void onSuccess(CaptureRecord registeredUser, JSONObject result) {
                state.signedInUser = registeredUser;
                fireHandlerOnSuccess(result);
            }

            public void onFailure(CaptureApiError error) {
                fireHandlerOnFailure(new SignInError(CAPTURE_API_ERROR, error, null));
            }
        });
    }

    /**
     * An interface to receive callbacks notifying the completion of a sign-in flow.
     */
    public interface SignInResultHandler {
        /**
         * Errors that may be sent upon failure of the sign-in flow
         */
        public static class SignInError {
            public enum FailureReason {

                /**
                 * A well formed response could not be retrieved from the Capture server
                 */
                INVALID_CAPTURE_API_RESPONSE,

                /**
                 * The Jump library has not been initialized
                 */
                JUMP_NOT_INITIALIZED,

                /**
                 * The user canceled sign-in the sign-in flow during authentication
                 */
                AUTHENTICATION_CANCELED_BY_USER,

                /**
                 * The password provided was invalid. Only generated by #performTraditionalSignIn(...)
                 */
                INVALID_PASSWORD,

                /**
                 * The sign-in failed with a well-formed Capture sign-in API error
                 */
                CAPTURE_API_ERROR,

                /**
                 * The sign-in failed with a JREngageError
                 */
                ENGAGE_ERROR
            }

            public JRDictionary auth_info;
            public final FailureReason reason;
            public final CaptureApiError captureApiError;
            public final JREngageError engageError;

            /*package*/ SignInError(FailureReason reason, CaptureApiError captureApiError,
                                    JREngageError engageError) {
                this.reason = reason;
                this.captureApiError = captureApiError;
                this.engageError = engageError;
            }

            SignInError(FailureReason reason, CaptureApiError captureApiError,
                        JREngageError engageError,JRDictionary auth_info) {
                this.auth_info = auth_info;
                this.reason = reason;
                this.captureApiError = captureApiError;
                this.engageError = engageError;
            }

            public String toString() {
                return "<" + super.toString() + " reason: " + reason + " captureApiError: " + captureApiError
                        + " engageError: " + engageError + ">";
            }
        }

        /**
         * Called when Capture sign-in has succeeded. At this point Jump.getCaptureUser will return the
         * CaptureRecord instance for the user.
         */
        void onSuccess();

        /**
         * Called when Capture sign-in has failed.
         * @param error the error which caused the failure
         */
        void onFailure(SignInError error);
    }

    /**
     * An interface to receive a callback which handles the Capture OAuth Access Code that is generated on the
     * completion of the sign-in flow. Implement this interface in your sign in result handler if you would
     * like to receive the code. See the Start Sign-in section of the jump.android/Docs/Jump_Integration_Guide.md
     * for more information.
     */
    public interface SignInCodeHandler {
        /**
         * Called when Capture sign-in has succeeded.
         *
         * @param code An OAuth Authorization Code, this short lived code can be used to get an Access Token
         *             for use with a server side application like the Capture Drupal Plugin.
         */
        void onCode(String code);
    }

    /**
     * An interface to receive a callback which handles the Facebook closeAndClearTokenInformation call.
     */
    public interface FacebookRevokedHandler {
        /**
         * Called when Facebook closeAndClearTokenInformation has succeeded. 
         */
        void onSuccess();

        /**
         * Called when Facebook closeAndClearTokenInformation has failed.
         * Should be enhanced with some error messaging
         */
        void onFailure();
    }

    /**
     * An interface to receive callbacks notifying the completion of a sign-in flow.
     */
    public interface CaptureApiResultHandler {
        /**
         * Called when Capture sign-in has succeeded. At this point Jump.getCaptureUser will return the
         * CaptureRecord instance for the user.
         */
        void onSuccess(JSONObject response);

        /**
         * Called when Capture sign-in has failed.
         *
         * @param error the error which caused the failure
         */
        void onFailure(CaptureAPIError error);

        /**
         * Errors that may be sent upon failure of the sign-in flow
         */
        public static class CaptureAPIError {
            public final FailureReason reason;
            public final CaptureApiError captureApiError;
            public final JREngageError engageError;

            /*package*/ CaptureAPIError(FailureReason reason, CaptureApiError captureApiError,
                                        JREngageError engageError) {
                this.reason = reason;
                this.captureApiError = captureApiError;
                this.engageError = engageError;
            }

            public String toString() {
                return "<" + super.toString() + " reason: " + reason + " captureApiError: " + captureApiError
                        + " engageError: " + engageError + ">";
            }

            public enum FailureReason {
                /**
                 * The capture api request failed with invalid fields format
                 */
                CAPTURE_API_FORMAT_ERROR
            }
        }
    }

    /**
     * An interface to receive a callback when authorization completes for account linking.
     * Implement this interface in addition to te JREngageDelegate interface when you are linking accounts
     */
    public interface CaptureLinkAccountHandler {
        /**
         * Notifies the delegate that the user has successfully authenticated with the given provider,
         * passing to the delegate a JRDictionary object with the user's profile data.
         *
         * This will be called instead of jrAuthenticationDidSucceedForUser if you are linking accounts.
         *
         * @param auth_info
         *   A JRDictionary of fields containing all the information that Janrain Engage knows about
         *   the user signing in to your application.  Includes the field \e "profile" which contains the
         *   user's profile information.
         *
         * @param provider
         *   The name of the provider on which the user authenticated.
         *   For a list of possible strings, please see the
         *   <a href="http://documentation.janrain.com/engage/sdks/ios/mobile-providers#basicProviders">
         *   List of Providers</a>
         *
         * @sa For a full description of the dictionary and its fields,
         * please see the <a href="http://documentation.janrain.com/engage/api/auth_info">auth_info
         * response</a> section of the Janrain Engage API documentation.
         **/
        void jrAuthenticationDidSucceedForLinkAccount(JRDictionary auth_info, String provider);
    }

    /**
     * @deprecated Loading state from disk is now done automatically from Jump.init
     */
    public static void loadFromDisk(Context context) {
        loadUserFromDiskInternal(context);
    }

    /*package*/ static void loadUserFromDiskInternal(Context context) {
        state.signedInUser = CaptureRecord.loadFromDisk(context);
    }

    private static void loadRefreshSecretFromDiskInternal(Context context) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = state.context.openFileInput(Capture.JR_REFRESH_SECRET);
            ois = new ObjectInputStream(fis);
            state.refreshSecret = (String) ois.readObject();
        } catch (ClassCastException e) {
            throwDebugException(e);
        } catch (FileNotFoundException ignore) {
        } catch (StreamCorruptedException e) {
            throwDebugException(new RuntimeException(e));
        } catch (IOException e) {
            throwDebugException(new RuntimeException(e));
        } catch (ClassNotFoundException e) {
            throwDebugException(new RuntimeException(e));
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException ignore) {
            }

            try {
                if (ois != null) ois.close();
            } catch (IOException ignore) {
            }
        }
    }

    private static void loadFlow() {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = state.context.openFileInput(JR_CAPTURE_FLOW);
            ois = new ObjectInputStream(fis);
            state.captureFlow = (Map<String, Object>) ois.readObject();
        } catch (ClassCastException e) {
            throwDebugException(e);
        } catch (FileNotFoundException ignore) {
        } catch (StreamCorruptedException e) {
            throwDebugException(new RuntimeException(e));
        } catch (IOException e) {
            throwDebugException(new RuntimeException(e));
        } catch (ClassNotFoundException e) {
            throwDebugException(new RuntimeException(e));
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException ignore) {
            }

            try {
                if (ois != null) ois.close();
            } catch (IOException ignore) {
            }
        }
    }

    private static void downloadFlow() {
        String flowVersion = state.captureFlowVersion != null ? state.captureFlowVersion : "HEAD";

        String flowUrlString =
                String.format("https://%s.cloudfront.net/widget_data/flows/%s/%s/%s/%s.json",
                        state.flowUsesTestingCdn ? "dlzjvycct5xka" : "d1lqe9temigv1p",
                        state.captureAppId, state.captureFlowName, flowVersion,
                        state.captureLocale);

        ApiConnection c = new ApiConnection(flowUrlString);
        c.method = ApiConnection.Method.GET;
        c.fetchResponseAsJson(new ApiConnection.FetchJsonCallback() {
            public void run(JSONObject jsonObject) {
                if (jsonObject == null) {
                    LogUtils.logd("Error downloading flow");
                    Intent intent = new Intent(JR_FAILED_TO_DOWNLOAD_FLOW);
                    intent.putExtra("message", "Error downloading flow");
                    LocalBroadcastManager.getInstance(state.context).sendBroadcast(intent);
                } else {
                    state.captureFlow = JsonUtils.jsonToCollection(jsonObject);
                    LogUtils.logd("Parsed flow, version: " + CaptureFlowUtils.getFlowVersion(state.captureFlow));
                    Intent intent = new Intent(JR_DOWNLOAD_FLOW_SUCCESS);
                    intent.putExtra("message", "Download flow Success!!");
                    LocalBroadcastManager.getInstance(state.context).sendBroadcast(intent);
                    storeCaptureFlow();
                }
            }
        });
    }

    private static void storeCaptureFlow() {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = state.context.openFileOutput(JR_CAPTURE_FLOW, 0);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(state.captureFlow);
        } catch (FileNotFoundException e) {
            throwDebugException(new RuntimeException(e));
        } catch (IOException e) {
            throwDebugException(new RuntimeException(e));
        } finally {
            try {
                if (oos != null) oos.close();
            } catch (IOException ignore) {
            }

            try {
                if (fos != null) fos.close();
            } catch (IOException ignore) {
            }
        }
    }

    private static void saveToken(final String token, final String tokenType) {
        ThreadUtils.executeInBg(new Runnable() {
            public void run() {
                FileOutputStream fos = null;
                ObjectOutputStream oos = null;

                try {
                    fos = state.context.openFileOutput(tokenType, Context.MODE_PRIVATE);
                    oos = new ObjectOutputStream(fos);
                    oos.writeObject(token);
                } catch (FileNotFoundException e) {
                    throwDebugException(new RuntimeException(e));
                } catch (IOException e) {
                    throwDebugException(new RuntimeException(e));
                } finally {
                    try {
                        if (oos != null) oos.close();
                    } catch (IOException ignore) {
                    }

                    try {
                        if (fos != null) fos.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        });
    }

    /**
     * To be called from Activity#onPause
     * @param context the application context, used to interact with the disk
     */
    public static void saveToDisk(Context context) {
        if (state.signedInUser != null) state.signedInUser.saveToDisk(context);
    }

    /**
     * @return the downloaded flow's version, if any
     */
    public static String getCaptureFlowVersion() {
        Map<String, Object> captureFlow = getCaptureFlow();
        if (captureFlow == null) return null;
        return CaptureFlowUtils.getFlowVersion(captureFlow);
    }

    /**
     * The default merge-flow handler. Provides a baseline implementation of the merge-account flow UI
     *
     * @param fromActivity the Activity from which to launch subsequent Activities and Dialogs.
     * @param error the error received by your
     * @param signInResultHandler your sign-in result handler.
     */
    public static void startDefaultMergeFlowUi(final Activity fromActivity,
                                               SignInError error,
                                               final SignInResultHandler signInResultHandler) {
        if (state.jrEngage == null || state.captureDomain == null) {
            signInResultHandler.onFailure(new SignInError(JUMP_NOT_INITIALIZED, null, null));
            return;
        }

        final String mergeToken = error.captureApiError.getMergeToken();
        String tempExistingProvider = error.captureApiError.getExistingAccountIdentityProvider();
        /**
         * Work around to address how engage returns the provider for both older Google
         * and newer Google+ as just "google".
         * If you are using the older Google IDP configuration remove the following three
         * lines of code.
         */
        if(tempExistingProvider.equals("google")){
            tempExistingProvider = "googleplus";
        }
        final String existingProvider = tempExistingProvider;
        
        String conflictingIdentityProvider = error.captureApiError.getConflictingIdentityProvider();
        String conflictingIdpNameLocalized = JRProvider.getLocalizedName(conflictingIdentityProvider);
        String existingIdpNameLocalized = JRProvider.getLocalizedName(existingProvider);

        AlertDialog alertDialog = new AlertDialog.Builder(fromActivity)
                .setTitle(fromActivity.getString(R.string.jr_merge_flow_default_dialog_title))
                .setCancelable(false)
                .setMessage(fromActivity.getString(R.string.jr_merge_flow_default_dialog_message,
                        conflictingIdpNameLocalized,
                        existingIdpNameLocalized))
                .setPositiveButton(fromActivity.getString(R.string.jr_merge_flow_default_merge_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // When existingProvider == "capture" you can also call ...
                                //
                                //     Jump.performTraditionalSignIn(String signInName, String password,
                                //         final SignInResultHandler handler, final String mergeToken);
                                //
                                // ... instead of showSignInDialog if you wish to present your own dialog
                                // and then use the headless API to perform the traditional sign-in.
                                
                                // For the Merge Account workflow it is recommended to use the standard 
                                // web based (non-native) authentication dialog.  This allows the end 
                                // user to manually enter the social account that "owns" the Janrain user 
                                // record.  If the user did not have this account stored on their 
                                // phone or had multiple accounts stored the user interface could be overly 
                                // complicated. The standard web based sign in dialog can be forced by 
                                // passing a null permissions parameter in the method call below
                                Jump.showSignInDialog(fromActivity,
                                        existingProvider,
                                        null,
                                        signInResultHandler,
                                        mergeToken);

                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * An interface to receive a callback which handles the Capture recoverPassword  that is generated on the
     * forgot password flow. Implement this interface in your forgot password result handler if you would like
     * to receive the code.See the Start Sign-in section of the jump.android/Docs/Jump_Integration_Guide.md
     * for more information.
     */
    public interface ForgotPasswordResultHandler {
        /**
         * Called when Capture forgot password has succeeded.
         */
        void onSuccess();

        /**
         * Called when Capture forgot password has failed.
         *
         * @param error the error which caused the failure
         */
        void onFailure(ForgetPasswordError error);

        /**
         * Errors that may be sent upon failure of the forgot password flow
         */
        public static class ForgetPasswordError {
            public final FailureReason reason;
            public final CaptureApiError captureApiError;

            /*package*/ ForgetPasswordError(FailureReason reason, CaptureApiError captureApiError) {
                this.reason = reason;
                this.captureApiError = captureApiError;
            }

            public String toString() {
                return "<" + super.toString() + " reason: " + reason + " captureApiError: " + captureApiError
                        + ">";
            }

            public enum FailureReason {
                /**
                 * A well formed response could not be retrieved from the Capture server
                 */
                INVALID_CAPTURE_API_RESPONSE,

                /**
                 * The Jump library has not been initialized
                 */
                FORGOTPASSWORD_JUMP_NOT_INITIALIZED,

                /**
                 * The email provided was invalid. Only generated by #performTraditionalSignIn(...)
                 */
                FORGOTPASSWORD_INVALID_EMAILID,

                /**
                 * The forgot password failed with a well-formed Capture forgot-password API error
                 */
                FORGOTPASSWORD_CAPTURE_API_ERROR,

                /**
                 * The forgot password failed when forgot-password form name is null
                 */
                FORGOTPASSWORD_FORM_NAME_NOT_INITIALIZED,


            }
        }
    }

    /**
     * Headless API for Capture forgot password
     *
     * @param emailAddress the end user's user name or email address
     * @param handler     your callback handler, invoked upon completion in the UI thread
     */
    public static void performForgotPassword(String emailAddress, final ForgotPasswordResultHandler handler) {

        {
            if (state.jrEngage == null || state.captureDomain == null ||
                    Jump.getCaptureForgotPasswordFormName() == null) {

                handler.onFailure(new ForgetPasswordError(FORGOTPASSWORD_JUMP_NOT_INITIALIZED, null));
                return;
            }
            Capture.performForgotPassword(emailAddress, new Capture.ForgotPasswordResultHandler() {

                @Override
                public void onSuccess() {
                    handler.onSuccess();
                }

                @Override
                public void onFailure(CaptureApiError error) {
                    handler.onFailure(new ForgetPasswordError(
                            ForgetPasswordError.FailureReason.FORGOTPASSWORD_CAPTURE_API_ERROR,
                            error));
                }
            });
        }
    }


   /**
    * Resend email verification
    *
    * @param emailAddress the email address to verify
    * @param callback a Capture Api Request Callback
    */
    public static void resendEmailVerification(String emailAddress,
                                               final Capture.CaptureApiRequestCallback callback) {
        Capture.resendEmailVerification(emailAddress, callback);
    }

    /**
     * Starts the Engage account linking flow. <p/> If the providerName parameter is not null and is a valid
     * provider name string then authentication begins directly with that provider. <p/> If providerName is
     * null than a list of available providers is displayed first.
     *
     * @param fromActivity the activity from which to start the dialog activity
     * @param providerName the name of the provider to show the sign-in flow for. May be null. If null, a list
     *                     of providers (and a traditional sign-in form) is displayed to the end-user.
     * @param linkAccount  the boolean set to true for account linking
     * @param mDelegate    an Engage Delegate to handle the JRSession response.
     */

    public static void showSocialSignInDialog(Activity fromActivity, String providerName, boolean linkAccount, JREngageDelegate mDelegate) {
        state.jrEngage.showAuthenticationDialog(fromActivity, null, providerName, null, linkAccount);
        state.jrEngage.addDelegate(mDelegate);
    }
    
    /**
     * Starts the Engage account linking flow. <p/> If the providerName parameter is not null and is a valid
     * provider name string then authentication begins directly with that provider. <p/> If providerName is
     * null than a list of available providers is displayed first.
     *
     * @param fromActivity the activity from which to start the dialog activity
     * @param providerName the name of the provider to show the sign-in flow for. May be null. If null, a list
     *                     of providers (and a traditional sign-in form) is displayed to the end-user.
     * @param linkAccount  the boolean set to true for account linking
     * @param mDelegate    an Engage Delegate to handle the JRSession response.
     */

    public static void showSocialSignInDialog(Activity fromActivity, String providerName, String[] permissions, 
                                              boolean linkAccount, JREngageDelegate mDelegate) {

        state.jrEngage.showAuthenticationDialog(fromActivity, null, providerName, null, linkAccount);
        state.jrEngage.addDelegate(mDelegate);
    }

    /**
     * Headless API for Capture link account
     *
     * @param token   token of the account that user wants to link
     * @param handler your callback handler, invoked upon completion in the UI thread
     */
    public static void performLinkAccount(final String token, final CaptureApiResultHandler handler) {
        state.captureAPIHandler = handler;
        Capture.performLinkAccount(token, new Capture.CaptureApiResultHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                fireHandlerOnCaptureAPISuccess(response);
            }

            @Override
            public void onFailure(CaptureApiError error) {
                Jump.fireHandlerOnCaptureAPIFailure(new CaptureAPIError(CAPTURE_API_FORMAT_ERROR,
                        error,
                        null));
            }
        });
    }

    /**
     * Headless API for Capture unlink account
     *
     * @param identifier the identifier of the account that user wants to unlink
     * @param handler    your result handler, called upon completion on the UI thread
     */
    public static void performUnlinkAccount(String identifier, final CaptureApiResultHandler handler) {
        state.captureAPIHandler = handler;
        Capture.performUnlinkAccount(identifier, new Capture.CaptureApiResultHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                fireHandlerOnCaptureAPISuccess(response);
            }

            @Override
            public void onFailure(CaptureApiError error) {
                Jump.fireHandlerOnCaptureAPIFailure(new CaptureAPIError(CAPTURE_API_FORMAT_ERROR,
                        error,
                        null));
            }
        });
    }

    /**
     * Headless API for fetching Capture Signed user data
     *
     * @param handler your result handler, called upon completion on the UI thread
     */
    public static void performFetchCaptureData(final CaptureApiResultHandler handler) {
        state.captureAPIHandler = handler;
        Capture.performUpdateSignedUserData(new Capture.CaptureApiResultHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                fireHandlerOnCaptureAPISuccess(response);
            }

            @Override
            public void onFailure(CaptureApiError error) {
                Jump.fireHandlerOnCaptureAPIFailure(new CaptureAPIError(CAPTURE_API_FORMAT_ERROR,
                        error,
                        null));
            }
        });
    }

    /*package*/
    static void fireHandlerOnCaptureAPIFailure(CaptureAPIError failureParam) {
        CaptureApiResultHandler handler_ = state.captureAPIHandler;
        state.captureAPIHandler = null;
        if (handler_ != null) handler_.onFailure(failureParam);
    }

    /*package*/
    static void fireHandlerOnCaptureAPISuccess(JSONObject response) {
        CaptureApiResultHandler handler_ = state.captureAPIHandler;
        state.captureAPIHandler = null;
        if (handler_ != null) {
            handler_.onSuccess(response);
        }
    }
}