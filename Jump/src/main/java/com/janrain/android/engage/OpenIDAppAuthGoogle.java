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

import android.app.Application;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.content.Context;
import android.content.Intent;
import com.janrain.android.utils.LogUtils;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.AuthorizationServiceConfiguration.RetrieveConfigurationCallback;
import net.openid.appauth.ClientSecretBasic;
import net.openid.appauth.RegistrationRequest;
import net.openid.appauth.RegistrationResponse;
import net.openid.appauth.ResponseTypeValues;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.janrain.android.engage.session.JRSession;
import static com.janrain.android.engage.JROpenIDAppAuth.OpenIDAppAuthProvider;


public class OpenIDAppAuthGoogle extends OpenIDAppAuthProvider {

    private String[] scopes;
    private boolean isConnecting = false;
    private static final String TAG = "OpenIDAppAuthGoogle";


    /*package*/ static boolean canHandleAuthentication(Context context) {
        List<OpenIDIdentityProvider> providers = OpenIDIdentityProvider.getEnabledProviders(context);
        //API Level 24:
        //return providers.stream().filter(o -> o.name.equals("googleplus")).findFirst().isPresent();
        for(OpenIDIdentityProvider idp : providers) {
            if(idp.name.equals("Google")) return true;
        }
        return false;
    }

    /*package*/ OpenIDAppAuthGoogle(FragmentActivity activity, JROpenIDAppAuth.OpenIDAppAuthCallback callback, Context parentContext, AuthorizationService authorizationService) {
        super(activity, callback, parentContext, authorizationService);
        scopes = new String[] {"https://www.googleapis.com/auth/plus.login"};
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        LogUtils.logd(TAG, Integer.toString(resultCode));

    }

    @Override
    public String provider() {
        return "googleplus";
    }

    @Override
    public void startAuthentication() {
        LogUtils.logd(TAG, "[startAuthentication]");
        final Context appContext = fromParentContext;

        List<OpenIDIdentityProvider> providers = OpenIDIdentityProvider.getEnabledProviders(appContext);

        for (final OpenIDIdentityProvider idp : providers) {
            if(idp.name.equals("Google")){
                Uri issuerUri = Uri.parse("https://accounts.google.com");
                AuthorizationServiceConfiguration.fetchFromIssuer(
                    issuerUri,
                    new RetrieveConfigurationCallback() {
                        @Override public void onFetchConfigurationCompleted(
                                @Nullable AuthorizationServiceConfiguration serviceConfiguration,
                                @Nullable AuthorizationException ex) {
                            if (ex != null) {
                                LogUtils.logd(TAG, "Failed to retrieve configuration for " + idp.name, ex);
                            } else {
                                if (idp.getClientId() == null) {
                                    // Do dynamic client registration if no client_id
                                    makeRegistrationRequest(serviceConfiguration, idp);
                                } else {
                                    makeAuthRequest(serviceConfiguration, idp, new AuthState());
                                }
                            }
                        }
                    });


            }
        }


    }

    @Nullable
    private Context getApplicationContext() {
        try {
            final Class<?> activityThreadClass =
                    Class.forName("android.app.ActivityThread");
            final Method method = activityThreadClass.getMethod("currentApplication");
            return (Application) method.invoke(null, (Object[]) null);
        } catch (final ClassNotFoundException e) {
            // handle exception
            LogUtils.loge(TAG, e);
        } catch (final NoSuchMethodException e) {
            // handle exception
            LogUtils.loge(TAG, e);
        } catch (final IllegalArgumentException e) {
            // handle exception
            LogUtils.loge(TAG, e);
        } catch (final IllegalAccessException e) {
            // handle exception
            LogUtils.loge(TAG, e);
        } catch (final InvocationTargetException e) {
            // handle exception
            LogUtils.loge(TAG, e);
        }
        return null;
    }


    private void makeAuthRequest(
            @NonNull AuthorizationServiceConfiguration serviceConfig,
            @NonNull OpenIDIdentityProvider idp,
            @NonNull AuthState authState) {


        AuthorizationRequest authRequest = new AuthorizationRequest.Builder(
                serviceConfig,
                idp.getClientId(),
                ResponseTypeValues.CODE,
                idp.getRedirectUri())
                .setScope(idp.getScope())
                .setLoginHint(null)
                .build();


        //Intent postAuthIntent = new Intent(context, MyAuthResultHandlerActivity.class);
        //Intent authCanceledIntent = new Intent(context, MyAuthCanceledHandlerActivity.class);
        final JRSession session = JRSession.getInstance();
        session.setCurrentlyAuthenticatingOpenIDAppAuthProvider(this);
        OpenIDAppAuthTokenActivity ta = new OpenIDAppAuthTokenActivity();
        LogUtils.logd(TAG, "Making auth request to " + serviceConfig.authorizationEndpoint);

        mAuthService.performAuthorizationRequest(
                authRequest,
                ta.createPostAuthorizationIntent(
                        this.getApplicationContext(),
                        authRequest,
                        serviceConfig.discoveryDoc,
                        authState));

    }

    private void makeRegistrationRequest(
            @NonNull AuthorizationServiceConfiguration serviceConfig,
            @NonNull final OpenIDIdentityProvider idp) {

        LogUtils.logd(TAG, "Making registration request to " + serviceConfig.registrationEndpoint);
        final RegistrationRequest registrationRequest = new RegistrationRequest.Builder(
                serviceConfig,
                Arrays.asList(idp.getRedirectUri()))
                .setTokenEndpointAuthenticationMethod(ClientSecretBasic.NAME)
                .build();

        mAuthService.performRegistrationRequest(
                registrationRequest,
                new AuthorizationService.RegistrationResponseCallback() {
                    @Override
                    public void onRegistrationRequestCompleted(
                            @Nullable RegistrationResponse registrationResponse,
                            @Nullable AuthorizationException ex) {
                        LogUtils.logd(TAG, "Registration request complete");
                        if (registrationResponse != null) {
                            idp.setClientId(registrationResponse.clientId);
                            LogUtils.logd(TAG, "Registration request complete successfully");
                            // Continue with the authentication
                            makeAuthRequest(registrationResponse.request.configuration, idp,
                                    new AuthState((registrationResponse)));
                        }
                    }
                });
    }

    @Override
    public void signOut() {

    }

    @Override
    public void revoke() {

    }



}
