package com.janrain.android.engage;
/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2016, Janrain, Inc.
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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.janrain.android.engage.session.JRSession;
import com.janrain.android.utils.LogUtils;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceDiscovery;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class OpenIDAppAuthTokenActivity extends FragmentActivity  {
    private static final String TAG = "OpenIDAppAuthTokenActivity";

    private static final String KEY_AUTH_STATE = "authState";
    private static final String KEY_USER_INFO = "userInfo";

    private static final String EXTRA_AUTH_SERVICE_DISCOVERY = "authServiceDiscovery";
    private static final String EXTRA_AUTH_STATE = "authState";

    private static final int BUFFER_SIZE = 1024;

    private AuthState mAuthState;
    private AuthorizationService mAuthService;
    private JSONObject mUserInfoJson;
    private JROpenIDAppAuth.OpenIDAppAuthProvider mOpenIDProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        mAuthService = new AuthorizationService(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_AUTH_STATE)) {
                try {
                    mAuthState = AuthState.jsonDeserialize(
                            savedInstanceState.getString(KEY_AUTH_STATE));
                } catch (JSONException ex) {
                    LogUtils.logd(TAG, "Malformed authorization JSON saved", ex);
                }
            }

            if (savedInstanceState.containsKey(KEY_USER_INFO)) {
                try {
                    mUserInfoJson = new JSONObject(savedInstanceState.getString(KEY_USER_INFO));
                } catch (JSONException ex) {
                    LogUtils.logd(TAG, "Failed to parse saved user info JSON", ex);
                }
            }
        }

        if (mAuthState == null) {
            mAuthState = getAuthStateFromIntent(getIntent());
            AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
            AuthorizationException ex = AuthorizationException.fromIntent(getIntent());
            mAuthState.update(response, ex);

            if (response != null) {
                LogUtils.logd(TAG, "Received AuthorizationResponse.");
                exchangeAuthorizationCode(response);
            } else {
                LogUtils.logd(TAG, "Authorization failed: " + ex);
                
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        if (mAuthState != null) {
            state.putString(KEY_AUTH_STATE, mAuthState.jsonSerializeString());
        }

        if (mUserInfoJson != null) {
            state.putString(KEY_USER_INFO, mUserInfoJson.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuthService.dispose();
    }

    private void receivedTokenResponse(
            @Nullable TokenResponse tokenResponse,
            @Nullable AuthorizationException authException) {
        LogUtils.logd(TAG, "Token request complete");
        LogUtils.logd(TAG, "Token: " + tokenResponse.accessToken);

        mAuthState.update(tokenResponse, authException);
        final JRSession session = JRSession.getInstance();
        JROpenIDAppAuth.OpenIDAppAuthProvider mOpenIDProvider = session.getCurrentOpenIDAppAuthProvider();
        mOpenIDProvider.getAuthInfoTokenForAccessToken(tokenResponse.accessToken);
        this.finish();
    }

    private void refreshAccessToken() {
        performTokenRequest(mAuthState.createTokenRefreshRequest());
    }

    private void exchangeAuthorizationCode(AuthorizationResponse authorizationResponse) {
        performTokenRequest(authorizationResponse.createTokenExchangeRequest());
    }

    private void performTokenRequest(TokenRequest request) {
        ClientAuthentication clientAuthentication;
        try {
            clientAuthentication = mAuthState.getClientAuthentication();
        } catch (ClientAuthentication.UnsupportedAuthenticationMethod ex) {
            LogUtils.logd(TAG, "Token request cannot be made, client authentication for the token "
                    + "endpoint could not be constructed (%s)", ex);
            return;
        }

        mAuthService.performTokenRequest(
                request,
                clientAuthentication,
                new AuthorizationService.TokenResponseCallback() {
                    @Override
                    public void onTokenRequestCompleted(
                            @Nullable TokenResponse tokenResponse,
                            @Nullable AuthorizationException ex) {
                        receivedTokenResponse(tokenResponse, ex);
                    }
                });
    }

    private void fetchUserInfo() {
        if (mAuthState.getAuthorizationServiceConfiguration() == null) {
            LogUtils.logd(TAG, "Cannot make userInfo request without service configuration");
        }

        mAuthState.performActionWithFreshTokens(mAuthService, new AuthState.AuthStateAction() {
            @Override
            public void execute(String accessToken, String idToken, AuthorizationException ex) {
                if (ex != null) {
                    LogUtils.logd(TAG, "Token refresh failed when fetching user info");
                    return;
                }

                AuthorizationServiceDiscovery discoveryDoc = getDiscoveryDocFromIntent(getIntent());
                if (discoveryDoc == null) {
                    throw new IllegalStateException("no available discovery doc");
                }

                URL userInfoEndpoint;
                try {
                    userInfoEndpoint = new URL(discoveryDoc.getUserinfoEndpoint().toString());
                } catch (MalformedURLException urlEx) {
                    LogUtils.logd(TAG, "Failed to construct user info endpoint URL", urlEx);
                    return;
                }

                InputStream userInfoResponse = null;
                try {
                    HttpURLConnection conn = (HttpURLConnection) userInfoEndpoint.openConnection();
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                    conn.setInstanceFollowRedirects(false);
                    userInfoResponse = conn.getInputStream();
                    String response = readStream(userInfoResponse);
                    updateUserInfo(new JSONObject(response));
                } catch (IOException ioEx) {
                    LogUtils.logd(TAG, "Network error when querying userinfo endpoint", ioEx);
                } catch (JSONException jsonEx) {
                    LogUtils.logd(TAG, "Failed to parse userinfo response");
                } finally {
                    if (userInfoResponse != null) {
                        try {
                            userInfoResponse.close();
                        } catch (IOException ioEx) {
                            LogUtils.logd(TAG, "Failed to close userinfo response stream", ioEx);
                        }
                    }
                }
            }
        });
    }

    private void updateUserInfo(final JSONObject jsonObject) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mUserInfoJson = jsonObject;
            }
        });
    }

    private static String readStream(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        char[] buffer = new char[BUFFER_SIZE];
        StringBuilder sb = new StringBuilder();
        int readCount;
        while ((readCount = br.read(buffer)) != -1) {
            sb.append(buffer, 0, readCount);
        }
        return sb.toString();
    }

    static PendingIntent createPostAuthorizationIntent(
            @NonNull Context context,
            @NonNull AuthorizationRequest request,
            @Nullable AuthorizationServiceDiscovery discoveryDoc,
            @NonNull AuthState authState) {
        Intent intent = new Intent(context, OpenIDAppAuthTokenActivity.class);
        intent.putExtra(EXTRA_AUTH_STATE, authState.jsonSerializeString());
        if (discoveryDoc != null) {
            intent.putExtra(EXTRA_AUTH_SERVICE_DISCOVERY, discoveryDoc.docJson.toString());
        }

        return PendingIntent.getActivity(context, request.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static AuthorizationServiceDiscovery getDiscoveryDocFromIntent(Intent intent) {
        if (!intent.hasExtra(EXTRA_AUTH_SERVICE_DISCOVERY)) {
            return null;
        }
        String discoveryJson = intent.getStringExtra(EXTRA_AUTH_SERVICE_DISCOVERY);
        try {
            return new AuthorizationServiceDiscovery(new JSONObject(discoveryJson));
        } catch (JSONException | AuthorizationServiceDiscovery.MissingArgumentException  ex) {
            throw new IllegalStateException("Malformed JSON in discovery doc");
        }
    }

    static AuthState getAuthStateFromIntent(Intent intent) {
        if (!intent.hasExtra(EXTRA_AUTH_STATE)) {
            throw new IllegalArgumentException("The AuthState instance is missing in the intent.");
        }
        try {
            return AuthState.jsonDeserialize(intent.getStringExtra(EXTRA_AUTH_STATE));
        } catch (JSONException ex) {
            LogUtils.logd(TAG, "Malformed AuthState JSON saved", ex);
            throw new IllegalArgumentException("The AuthState instance is missing in the intent.");
        }
    }
}
