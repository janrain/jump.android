/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2012, Janrain, Inc.
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

package com.janrain.android.capture;

import com.janrain.android.Jump;
import com.janrain.android.utils.ApiConnection;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.Set;

import static com.janrain.android.Jump.TraditionalSignInType;
import static com.janrain.android.Jump.getCaptureClientId;
import static com.janrain.android.utils.LogUtils.throwDebugException;

/**
 * This class implements Capture operations
 * It's not meant to be used directly, but rather through com.janrain.android.Jump
 */
public class Capture {

    final public static String JR_REFRESH_SECRET = "jr_capture_refresh_secret";

    private Capture() {}

    /**
     * @param username The username (or email address).
     * @param password The password
     * @param handler a call-back handler.
     * @return a connection handle
     */
    public static CaptureApiConnection performTraditionalSignIn(String username,
                                                                String password,
                                                                SignInResultHandler handler,
                                                                String mergeToken) {
        CaptureApiConnection connection = new CaptureApiConnection("/oauth/auth_native_traditional");
        String refreshSecret = generateAndStoreRefreshSecret();

        if (refreshSecret == null) {
            handler.onFailure(new CaptureApiError("Unable to generate secure random refresh secret"));
            return null;
        }

        Set flowCreds = CaptureFlowUtils.getTraditionalSignInCredentials(username, password);
        if (flowCreds != null) {
            connection.addAllToParams(flowCreds);
        } else {
            connection.addAllToParams("user", username, "password", password);
        }

        connection.addAllToParams("client_id", getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "response_type", Jump.getResponseType(),
                "redirect_uri", Jump.getRedirectUri(),
                "form", Jump.getCaptureTraditionalSignInFormName(),
                "refresh_secret", refreshSecret,
                "flow", Jump.getCaptureFlowName(),
                "flow_version", Jump.getCaptureFlowVersion());
        connection.maybeAddParam("merge_token", mergeToken);
        connection.fetchResponseAsJson(handler);
        return connection;
    }

    /**
     * @deprecated
     */
    public static CaptureApiConnection performTraditionalSignIn(String username,
                                                                String password,
                                                                TraditionalSignInType type,
                                                                SignInResultHandler handler,
                                                                String mergeToken) {
        return performTraditionalSignIn(username, password, handler, mergeToken);
    }

    /**
     * @deprecated
     */
    public static CaptureApiConnection performTraditionalSignIn(String username,
                                                                String password,
                                                                TraditionalSignInType type,
                                                                SignInResultHandler handler) {
        return performTraditionalSignIn(username, password, handler, null);
    }

    /**
     * Indicates a change was made to the JSON record which cannot be effected on Capture.
     * Currently, this is always the result of changes which violate the record schema.
     */
    public static class InvalidApidChangeException extends Exception {
        /*package*/ InvalidApidChangeException(String description) {
            super(description);
        }
    }

    /**
     * An interface used to communicate Capture API request results, or errors.
     */
    public static interface CaptureApiRequestCallback {
        /**
         * Called on successful API request
         */
        public void onSuccess();

        /**
         * Called on occurrence of error
         * @param e the error which occurred
         */
        public void onFailure(CaptureApiError e);
    }

    /**
     * Performs a social sign-in to Capture with the given Engage auth_info token
     *
     * @param authInfoToken an Engage auth_info token (the Engage instance for which the auth_info token is
     *                      value must be properly configured with the Capture instance which the JUMP library
     *                      has been configured with.
     * @param handler a sign-in result handler.
     * @param identityProvider the identity provider that the authInfoToken is valid for
     * @param mergeToken the merge token for this sign-in, if any
     */
    public static CaptureApiConnection performSocialSignIn(String authInfoToken,
                                                           final SignInResultHandler handler,
                                                           String identityProvider, String mergeToken) {
        handler.authenticationToken = authInfoToken;
        handler.identityProvider = identityProvider;

        String refreshSecret = generateAndStoreRefreshSecret();

        if (refreshSecret == null) {
            handler.onFailure(new CaptureApiError("Unable to generate secure random refresh secret"));
            return null;
        }

        CaptureApiConnection c = new CaptureApiConnection("/oauth/auth_native");
        c.addAllToParams("client_id", getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "response_type", Jump.getResponseType(),
                "redirect_uri", Jump.getRedirectUri(),
                "token", authInfoToken,
                "thin_registration", String.valueOf(Jump.getCaptureEnableThinRegistration()),
                "refresh_secret", refreshSecret
        );

        c.maybeAddParam("flow_version", Jump.getCaptureFlowVersion());
        c.maybeAddParam("flow", Jump.getCaptureFlowName());
        c.maybeAddParam("registration_form", Jump.getCaptureSocialRegistrationFormName());
        c.maybeAddParam("merge_token", mergeToken);
        c.fetchResponseAsJson(handler);
        return c;
    }

    /**
     * @deprecated
     */
    public static CaptureApiConnection performSocialSignIn(String authInfoToken,
                                                           final SignInResultHandler handler) {
        return performSocialSignIn(authInfoToken, handler, null, null);
    }

    /**
     * Registers a new user
     *
     * @param newUser the user record to fill the form fields for the registration form with.
     * @param socialRegistrationToken the social registration token, or null to perform a tradtional
     *                                registration
     * @param handler a sign-in result handler. (Successful registrations are also sign-ins.)
     *
     */
    public static CaptureApiConnection performRegistration(JSONObject newUser,
                                                           String socialRegistrationToken,
                                                           final SignInResultHandler handler) {
        if (newUser == null) throwDebugException(new IllegalArgumentException("null newUser"));

        // need to download flow
        // then translate object to form fields
        // then submit form

        String registrationForm = socialRegistrationToken != null ?
                Jump.getCaptureSocialRegistrationFormName() :
                Jump.getCaptureTraditionalRegistrationFormName();

        String url = socialRegistrationToken != null ? "/oauth/register_native" :
                "/oauth/register_native_traditional";

        CaptureApiConnection c = new CaptureApiConnection(url);

        c.addAllToParams(CaptureFlowUtils.getFormFields(newUser, registrationForm, Jump.getCaptureFlow()));

        String refreshSecret = generateAndStoreRefreshSecret();

        if (refreshSecret == null) {
            handler.onFailure(new CaptureApiError("Unable to generate secure random refresh secret"));
            return null;
        }

        c.addAllToParams(
                "client_id", Jump.getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "response_type", Jump.getResponseType(),
                "redirect_uri", Jump.getRedirectUri(),
                "flow", Jump.getCaptureFlowName(),
                "form", registrationForm,
                "refresh_secret", refreshSecret
        );

        c.maybeAddParam("flow_version", CaptureFlowUtils.getFlowVersion(Jump.getCaptureFlow()));
        c.maybeAddParam("token", socialRegistrationToken);

        c.fetchResponseAsJson(handler);
        return c;
    }
    /**
     * Capture Forgot password performer
     */
    public static CaptureApiConnection performForgotPassword(String emailAddress,
                                                             final ForgotPasswordResultHandler handler) {
        handler.authenticationToken = Jump.getResponseType();
        CaptureApiConnection c = new CaptureApiConnection("/oauth/forgot_password_native");
        c.addAllToParams("client_id", getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "response_type", Jump.getResponseType(),
                "redirect_uri", Jump.getRedirectUri(),
                CaptureFlowUtils.getUserIdFieldName(Jump.getCaptureForgotPasswordFormName(),
                        Jump.getCaptureFlow()),  emailAddress

        );

        c.maybeAddParam("flow_version", Jump.getCaptureFlowVersion());
        c.maybeAddParam("flow", Jump.getCaptureFlowName());
        c.maybeAddParam("form", Jump.getCaptureForgotPasswordFormName());
        c.fetchResponseAsJson(handler);
        return c;
    }

    /**
     * Resend email verification
     *
     * @param emailAddress the email address to verify
     * @param callback a Capture Api Request Callback
     * @return
     */
    public static CaptureApiConnection resendEmailVerification(String emailAddress,
                                                               final CaptureApiRequestCallback callback) {
        if (Jump.getCaptureResendEmailVerificationFormName() == null) {
            throwDebugException(new IllegalArgumentException("null captureResendEmailVerificationFormName"));
        }

        CaptureApiConnection c = getResendEmailVerificationConnection(emailAddress);
        c.fetchResponseAsJson(new ApiConnection.FetchJsonCallback() {
            public void run(JSONObject response) {
                if (response == null) {
                    callback.onFailure(CaptureApiError.INVALID_API_RESPONSE);
                } else if ("ok".equals(response.opt("stat"))) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(new CaptureApiError(response, null, null));
                }
            }
        });
        return c;
    }

    private static CaptureApiConnection getResendEmailVerificationConnection(String emailAddress) {
        String fieldName = CaptureFlowUtils.getUserIdFieldName(
                Jump.getCaptureResendEmailVerificationFormName(), Jump.getCaptureFlow());

        CaptureApiConnection c = new CaptureApiConnection("/oauth/verify_email_native");
        c.addAllToParams(
                "client_id", getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "response_type", "token",
                "redirect_uri", Jump.getRedirectUri(),
                "form", Jump.getCaptureResendEmailVerificationFormName(),
                fieldName, emailAddress);

        c.maybeAddParam("flow_version", Jump.getCaptureFlowVersion());
        c.maybeAddParam("flow", Jump.getCaptureFlowName());
        return c;
    }

    private static String generateAndStoreRefreshSecret() {
        final int SECRET_LENGTH = 40;

        SecureRandom random = new SecureRandom();
        StringBuilder buffer = new StringBuilder();

        while (buffer.length() < SECRET_LENGTH) {
            buffer.append(Integer.toHexString(random.nextInt()));
        }

        String refreshSecret = buffer.toString().substring(0, SECRET_LENGTH);

        Jump.setRefreshSecret(refreshSecret);

        return refreshSecret;
    }

    public static CaptureApiConnection updateUserProfile(CaptureRecord user,
                                                         final CaptureApiRequestCallback handler) {

        if (user == null) {
            throwDebugException(new IllegalArgumentException("null user"));
        }

        CaptureApiConnection c = getUpdateUserProfileConnection(user);

        c.fetchResponseAsJson(new ApiConnection.FetchJsonCallback() {
            public void run(JSONObject response) {
                if (response == null) {
                    handler.onFailure(CaptureApiError.INVALID_API_RESPONSE);
                } else if ("ok".equals(response.opt("stat"))) {
                    handler.onSuccess();
                } else {
                    handler.onFailure(new CaptureApiError(response, null, null));
                }
            }
        });

        return c;
    }

    private static CaptureApiConnection getUpdateUserProfileConnection(CaptureRecord user) {
        String editProfileForm = Jump.getCaptureEditUserProfileFormName();

        if (editProfileForm == null) {
            throwDebugException(new IllegalArgumentException("You must set captureEditUserProfileFormName"));
        }

        CaptureApiConnection c = new CaptureApiConnection("/oauth/update_profile_native");

        c.addAllToParams(CaptureFlowUtils.getFormFields(user, editProfileForm, Jump.getCaptureFlow()));

        c.addAllToParams(
                "client_id", Jump.getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "flow", Jump.getCaptureFlowName(),
                "flow_version", Jump.getCaptureFlowVersion(),
                "form", Jump.getCaptureEditUserProfileFormName(),
                "access_token", user.accessToken
        );

        return c;
    }

    public static CaptureApiConnection updateUserProfile(CaptureRecord user,
                                                         String editProfileFormName,
                                                         final CaptureApiRequestCallback handler) {

        if (user == null) {
            throwDebugException(new IllegalArgumentException("null user"));
        }

        CaptureApiConnection c = getUpdateUserProfileConnection(user, editProfileFormName);

        c.fetchResponseAsJson(new ApiConnection.FetchJsonCallback() {
            public void run(JSONObject response) {
                if (response == null) {
                    handler.onFailure(CaptureApiError.INVALID_API_RESPONSE);
                } else if ("ok".equals(response.opt("stat"))) {
                    handler.onSuccess();
                } else {
                    handler.onFailure(new CaptureApiError(response, null, null));
                }
            }
        });

        return c;
    }

    private static CaptureApiConnection getUpdateUserProfileConnection(CaptureRecord user, String editProfileForm) {

        if (editProfileForm == null) {
            throwDebugException(new IllegalArgumentException("You must set captureEditUserProfileFormName"));
        }

        CaptureApiConnection c = new CaptureApiConnection("/oauth/update_profile_native");

        c.addAllToParams(CaptureFlowUtils.getFormFields(user, editProfileForm, Jump.getCaptureFlow()));

        c.addAllToParams(
                "client_id", Jump.getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "flow", Jump.getCaptureFlowName(),
                "flow_version", Jump.getCaptureFlowVersion(),
                "form", Jump.getCaptureEditUserProfileFormName(),
                "access_token", user.accessToken
        );

        return c;
    }

    /**
     * @internal
     */
    public static abstract class SignInResultHandler implements ApiConnection.FetchJsonCallback {
        private boolean canceled = false;
        private String authenticationToken;
        private String identityProvider;

        public void cancel() {
            canceled = true;
        }

        public final void run(JSONObject response) {
            if (canceled) return;
            if (response == null) {
                onFailure(CaptureApiError.INVALID_API_RESPONSE);
            } else if ("ok".equals(response.opt("stat"))) {
                Object user = response.opt("capture_user");
                if (user instanceof JSONObject) {
                    String accessToken = response.optString("access_token");
                    //String refreshSecret = response.optString("refresh_secret");
                    CaptureRecord record = new CaptureRecord(((JSONObject) user), accessToken);
                    onSuccess(record, response);
                } else {
                    onFailure(CaptureApiError.INVALID_API_RESPONSE);
                }
            } else {
                onFailure(new CaptureApiError(response, authenticationToken, identityProvider));
            }
        }

        public abstract void onSuccess(CaptureRecord record, JSONObject response);

        public abstract void onFailure(CaptureApiError error);
    }

    /**
     * @internal
     */
    public static abstract class ForgotPasswordResultHandler implements ApiConnection.FetchJsonCallback {
        private boolean canceled = false;
        private String authenticationToken;
        private String identityProvider;

        public void cancel() {
            canceled = true;
        }

        public final void run(JSONObject response) {


            if (canceled) return;
            if (response == null) {
                onFailure(CaptureApiError.INVALID_API_RESPONSE);
            } else if ("ok".equals(response.opt("stat"))) {
                onSuccess();
            } else {
                onFailure(new CaptureApiError(response, authenticationToken, identityProvider));
            }
        }

        public abstract void onSuccess();

        public abstract void onFailure(CaptureApiError error);
    }


    /**
     * @internal
     */
    public static abstract class CaptureApiResultHandler implements ApiConnection.FetchJsonCallback {
        private boolean canceled = false;
        private String authenticationToken;
        private String identityProvider;

        public void cancel() {
            canceled = true;
        }

        public final void run(JSONObject response) {
            if (canceled) return;
            if (response == null) {
                onFailure(CaptureApiError.INVALID_API_RESPONSE);
            } else if ("ok".equals(response.opt("stat"))) {
                onSuccess(response);
            } else if ((response.opt("result"))!= null && String.valueOf(response.opt("result")).length()>0 ) {
                onSuccess(response);
            } else {
                onFailure(CaptureApiError.INVALID_API_RESPONSE);
            }
        }

        public abstract void onSuccess(JSONObject response);

        public abstract void onFailure(CaptureApiError error);
    }

    /**
     * Link new account
     *
     * @param token   token of the account that user wants to link
     * @param handler the generic capture API result handler.
     */
    public static CaptureApiConnection performLinkAccount(String token,
                                                          final CaptureApiResultHandler handler) {
        if (token == null) {
            handler.onFailure(new CaptureApiError(
                    "Unable to perform link account : link account token is null"));
            return null;
        }
        if (Jump.getAccessToken() == null) {
            handler.onFailure(new CaptureApiError(
                    "Unable to perform link account : capture account access token is null"));
            return null;
        }
        CaptureApiConnection c = new CaptureApiConnection("/oauth/link_account_native");
        c.addAllToParams(
                "client_id", Jump.getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "response_type", Jump.getResponseType(),
                "redirect_uri", Jump.getRedirectUri(),
                "access_token", Jump.getAccessToken(),
                "token", token,
                "flow", Jump.getCaptureFlowName(),
                "flow_version", CaptureFlowUtils.getFlowVersion(Jump.getCaptureFlow())
        );
        c.fetchResponseAsJson(handler);
        return c;
    }

    /**
     * Link new account
     *
     * @param identifier identifier of the account that user wants to unlink
     * @param handler    the generic capture API result handler.
     */
    public static CaptureApiConnection performUnlinkAccount(String identifier,
                                                            final CaptureApiResultHandler handler) {
        if (identifier == null) {
            handler.onFailure(new CaptureApiError("Unable to perform unlink account"));
            return null;
        }
        CaptureApiConnection c = new CaptureApiConnection("/oauth/unlink_account_native");
        c.addAllToParams(
                "client_id", Jump.getCaptureClientId(),
                "locale", Jump.getCaptureLocale(),
                "identifier_to_remove", identifier,
                "access_token", Jump.getAccessToken(),
                "flow", Jump.getCaptureFlowName(),
                "flow_version", CaptureFlowUtils.getFlowVersion(Jump.getCaptureFlow())
        );
        c.fetchResponseAsJson(handler);
        return c;
    }

    /**
     * Fetch signed user Data
     *
     * @param handler the generic capture API result handler.
     */
    public static CaptureApiConnection performUpdateSignedUserData(CaptureApiResultHandler handler) {
        CaptureApiConnection c = new CaptureApiConnection("/entity");
        c.addAllToParams("access_token", Jump.getAccessToken());
        c.fetchResponseAsJson(handler);
        return c;
    }
}

