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

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.graphics.drawable.*;
import android.graphics.*;
import android.view.*;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import com.janrain.android.Jump;
import com.janrain.android.capture.CaptureApiError;
import com.janrain.android.capture.CaptureRecord;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.utils.LogUtils;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.janrain.android.capture.Capture.CaptureApiRequestCallback;

import com.janrain.android.multidex.simpledemonative.NativeConfig;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;


public class MainActivity extends FragmentActivity {

    private NativeConfig nativeConfig;

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private String TWITTER_KEY;
    private String TWITTER_SECRET;
    private String GOOGLE_SIGN_IN_ENGAGE_API_KEY;

    //Facebook SDK
    private CallbackManager facebookCallbackManager;
    private AccessTokenTracker facebookAccessTokenTracker;
    private AccessToken facebookToken;
    private static String facebookEmail;

    private boolean flowDownloaded = false;

    /* Google Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 9001;

    /* Client used to interact with Google APIs. */
    private GoogleSignInClient mGoogleSignInClient;

    /* Facebook Request code used to invoke sign in user interactions. */
    private static final int FACEBOOK_REQUEST_CODE_SIGN_IN = 64206;

    /* Twitter Request code used to invoke sign in user interactions. */
    private static final int TWITTER_REQUEST_CODE_SIGN_IN = 140;

    //Twitter
    private TwitterAuthClient twitterAuthClient;
    private TwitterAuthToken twitterToken;
    private static String twitterEmail;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    private class MySignInResultHandler implements Jump.SignInResultHandler, Jump.SignInCodeHandler {
        public void onSuccess() {
            AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setMessage("Sign-in complete.");
            b.setNeutralButton("Dismiss", null);
            b.show();
        }

        public void onCode(String code) {
            Toast.makeText(MainActivity.this, "Authorization Code: " + code, Toast.LENGTH_LONG).show();
        }

        public void onFailure(SignInError error) {
            if (error.reason == SignInError.FailureReason.CAPTURE_API_ERROR &&
                    error.captureApiError.isMergeFlowError()) {
                // Called below is the default merge-flow handler. Merge behavior may also be implemented by
                // headless-native-API for more control over the user experience.
                //
                // To do so, call Jump.showSignInDialog or Jump.performTraditionalSignIn directly, and
                // pass in the merge-token and existing-provider-name retrieved from `error`.
                //
                // String mergeToken = error.captureApiError.getMergeToken();
                // String existingProvider = error.captureApiError.getExistingAccountIdentityProvider()
                //
                // (An existing-provider-name of "capture" indicates a conflict with a traditional-sign-in
                // account. You can handle this case yourself, by displaying a dialog and calling
                // Jump.performTraditionalSignIn, or you can call Jump.showSignInDialog(..., "capture") and
                // a library-provided dialog will be provided.)

                Jump.startDefaultMergeFlowUi(MainActivity.this, error, signInResultHandler);
            } else if (error.reason == SignInError.FailureReason.CAPTURE_API_ERROR &&
                    error.captureApiError.isTwoStepRegFlowError()) {
                // Called when a user cannot sign in because they have no record, but a two-step social
                // registration is possible. (Which means that the error contains pre-filled form fields
                // for the registration form.
                Intent i = new Intent(MainActivity.this, com.janrain.android.multidex.simpledemonative.RegistrationActivity.class);
                JSONObject prefilledRecord = error.captureApiError.getPreregistrationRecord();
                i.putExtra("preregistrationRecord", prefilledRecord.toString());
                i.putExtra("socialRegistrationToken", error.captureApiError.getSocialRegistrationToken());
                MainActivity.this.startActivity(i);
            } else {
                String errorString = error.reason.toString().isEmpty() ? error.toString() : error.reason.toString();
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setMessage("Sign-in failure:" + errorString);
                b.setNeutralButton("Dismiss", null);
                b.show();
            }
        }
    }



    private final MySignInResultHandler signInResultHandler = new MySignInResultHandler();

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setMessage("Could not download flow.");
            b.setNeutralButton("Dismiss", null);
            b.show();
        }
    };

    private final BroadcastReceiver flowMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            flowDownloaded = true;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String state = extras.getString("message");
                Toast.makeText(MainActivity.this, state, Toast.LENGTH_LONG).show();
            }

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //enableStrictMode();
        nativeConfig = new NativeConfig(this.getApplicationContext());
        TWITTER_KEY = nativeConfig.twitterKey;
        TWITTER_SECRET = nativeConfig.twitterSecret;
        // Configure sign-in to request the user's ID, email address, and basic
//      // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestServerAuthCode(nativeConfig.googleSignInEngageClientId)
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        facebookCallbackManager = CallbackManager.Factory.create();

        facebookAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
                facebookToken = currentAccessToken;
                if(facebookToken != null) {
                    LogUtils.logd("Facebook Access Token:" + facebookToken.getToken());
                    LogUtils.logd("Facebook User ID:" + facebookToken.getUserId());
                }else{
                    LogUtils.logd("Facebook Access Token Cleared");
                }
                GraphRequest.newMeRequest(
                        currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject me, GraphResponse response) {
                                if (response.getError() != null) {
                                    LogUtils.loge("Unable to retrieve Facebook Email: " + response.getError().getErrorMessage());
                                } else {
                                    facebookEmail = me.optString("email");
                                }
                            }
                        }).executeAsync();
            }
        };

        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        facebookToken = AccessToken.getCurrentAccessToken();
                        LogUtils.logd("Facebook Login Success: " + facebookToken.getToken());
                        Jump.startTokenAuthForNativeProvider(MainActivity.this, "facebook", facebookToken.getToken(), "", MainActivity.this.signInResultHandler, "");
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        LogUtils.logd("Facebook Login Cancelled");
                        Toast.makeText(MainActivity.this, "Facebook Login was cancelled by user",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        LogUtils.loge("Facebook Login Error: " + exception.getLocalizedMessage());
                        Toast.makeText(MainActivity.this, "An error occurred during Facebook Login",
                                Toast.LENGTH_LONG).show();
                    }
                });


        //Initialize Twitter SDK
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);


        IntentFilter filter = new IntentFilter(Jump.JR_FAILED_TO_DOWNLOAD_FLOW);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);

        IntentFilter flowFilter = new IntentFilter(Jump.JR_DOWNLOAD_FLOW_SUCCESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(flowMessageReceiver, flowFilter);

        ScrollView sv = new ScrollView(this);
        sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        Button webviewAuth = addButton(linearLayout, "Sign-In");
        Button googleAuth = addButton(linearLayout, "Google Sign-in");
        Button facebookAuth = addButton(linearLayout, "Facebook Login");
        Button twitterAuth = addButton(linearLayout, "Twitter Login");
        Button dumpRecord = addButton(linearLayout, "Dump Record to Log");
        Button editProfile = addButton(linearLayout, "Edit Profile");
        Button changePassword = addButton(linearLayout, "Change Password");
        Button refreshToken = addButton(linearLayout, "Refresh Access Token");
        Button refreshSignedInUser = addButton(linearLayout, "Refresh SignedIn User");
        Button resendVerificationButton = addButton(linearLayout, "Resend Email Verification");
        Button link_unlinkAccount = addButton(linearLayout, "Link & Unlink Account");
        addButton(linearLayout, "Share").setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (flowDownloaded)
                    JREngage.getInstance().showSocialPublishingDialog(MainActivity.this,
                            new JRActivityObject("", null));
            }
        });

        addButton(linearLayout, "Traditional Registration").setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (flowDownloaded)
                    MainActivity.this.startActivity(new Intent(MainActivity.this, com.janrain.android.multidex.simpledemonative.RegistrationActivity.class));
            }
        });

        Button signOut = addButton(linearLayout, "Sign Out of Capture Only");
        Button nativeSignOut = addButton(linearLayout, "Sign Out of Native Providers + Capture");

        sv.addView(linearLayout);
        setContentView(sv);

        webviewAuth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (flowDownloaded) {
                    Jump.showSignInDialog(MainActivity.this, null, signInResultHandler, null);
                } else {
                    Toast.makeText(MainActivity.this, "Flow Configuration not downloaded yet",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        googleAuth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (flowDownloaded) {
                    // This task is always completed immediately, there is no need to attach an
                    // asynchronous listener.
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);

                }else{
                    Toast.makeText(MainActivity.this, "Flow Configuration not downloaded yet",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        facebookAuth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (flowDownloaded) {
                    //Add Facebook Native
                    // If the access token is available already assign it.
                    facebookToken = AccessToken.getCurrentAccessToken();
                    if (facebookToken == null ) {
                        LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "email"));
                    }else{
                        Set<String> facebookPermissions = facebookToken.getPermissions();
                        if(!facebookPermissions.contains("public_profile")|| !facebookPermissions.contains("email")) {
                            String tokenTest = facebookToken.getToken();
                            Jump.startTokenAuthForNativeProvider(MainActivity.this, "facebook", facebookToken.getToken(), "", MainActivity.this.signInResultHandler, "");
                        }else{
                            LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "email"));
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Flow Configuration not downloaded yet",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        twitterAuth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (flowDownloaded) {

                    TwitterSession twitterSession = TwitterCore
                            .getInstance()
                            .getSessionManager()
                            .getActiveSession();

                    if (twitterSession != null) {
                        twitterToken = twitterSession.getAuthToken();
                    }else{
                        twitterToken = null;
                    }
                    if(twitterToken == null){

                        //Activity mActivity = MainActivity.this; //(Activity) v.getContext()
                        twitterAuthClient = new TwitterAuthClient();
                        twitterAuthClient.authorize(MainActivity.this, new Callback<TwitterSession>(){
                            @Override
                            public void success(Result<TwitterSession> twitterSessionResult) {
                                TwitterSession twitterSession = twitterSessionResult.data;
                                twitterToken = twitterSession.getAuthToken();
                                LogUtils.logd("Logged with twitter:" + twitterToken.token + " " + twitterToken.secret);
                                //Optional if your Twitter app supports the retrieval of email addresses.
                                // https://docs.fabric.io/android/twitter/request-user-email-address.html
                                TwitterAuthClient twitterAuthClient = new TwitterAuthClient();
                                twitterAuthClient.requestEmail(twitterSession, new Callback<String>() {
                                    @Override
                                    public void success(Result<String> result) {
                                        if (result.data != null) {
                                            MainActivity.twitterEmail = result.data;
                                            LogUtils.logd("Retrieved Twitter Email: " + result.data);
                                        } else {
                                            LogUtils.logd("Twitter Email is null");
                                        }
                                        Jump.startTokenAuthForNativeProvider(MainActivity.this, "twitter", twitterToken.token, twitterToken.secret, MainActivity.this.signInResultHandler, "");
                                    }

                                    @Override
                                    public void failure(TwitterException exception) {
                                        // Do something on failure
                                        LogUtils.loge("Error getting Twitter Email", exception);
                                        Jump.startTokenAuthForNativeProvider(MainActivity.this, "twitter", twitterToken.token, twitterToken.secret, MainActivity.this.signInResultHandler, "");
                                    }
                                });
                            }

                            @Override
                            public void failure(TwitterException e) {
                                LogUtils.logd("Failed login with twitter: " + e.getLocalizedMessage());
                                e.printStackTrace();
                            }
                        });
                    }else{
                        Jump.startTokenAuthForNativeProvider(MainActivity.this, "twitter", twitterToken.token, twitterToken.secret, MainActivity.this.signInResultHandler, "");
                    }

                }else{
                    Toast.makeText(MainActivity.this, "Flow Configuration not downloaded yet",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        dumpRecord.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (flowDownloaded) {
                    LogUtils.logd(String.valueOf(Jump.getSignedInUser()));
                }else{
                    Toast.makeText(MainActivity.this, "Flow Configuration not downloaded yet",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Jump.getSignedInUser() == null) {
                    Toast.makeText(MainActivity.this, "Can't edit without record instance.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Intent i = new Intent(MainActivity.this, com.janrain.android.multidex.simpledemonative.UpdateProfileActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CaptureRecord user = Jump.getSignedInUser();
                if (user == null) {
                    Toast.makeText(MainActivity.this, "Can't change password without record instance.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if(!user.hasPassword()){
                    Toast.makeText(MainActivity.this, "Can't change password on social only accounts.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Intent i = new Intent(MainActivity.this, com.janrain.android.multidex.simpledemonative.ChangePasswordActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        refreshToken.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Jump.getSignedInUser() == null) {
                    Toast.makeText(MainActivity.this, "Cannot refresh token without signed in user",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Jump.getSignedInUser().refreshAccessToken(new CaptureApiRequestCallback() {
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Access Token Refreshed",
                                Toast.LENGTH_LONG).show();
                    }

                    public void onFailure(CaptureApiError e) {
                        Toast.makeText(MainActivity.this, "Failed to refresh access token",
                                Toast.LENGTH_LONG).show();
                        LogUtils.loge(e.toString());
                    }
                });
            }
        });

        refreshSignedInUser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Jump.getSignedInUser() == null) {
                    Toast.makeText(MainActivity.this, "Cannot refresh signed in user, there was no one present",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Jump.CaptureApiResultHandler handler = new Jump.CaptureApiResultHandler() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Toast.makeText(MainActivity.this, "SignedIn User Refreshed",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(CaptureAPIError error) {
                        Toast.makeText(MainActivity.this, "Failed to refresh SignedIn User",
                                Toast.LENGTH_LONG).show();
                        LogUtils.loge(error.toString());
                    }
                };

                Jump.performFetchCaptureData(handler, true);
            }
        });

        resendVerificationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CaptureRecord user = Jump.getSignedInUser();
                if (user == null) {
                    promptForResendVerificationEmailAddress("");
                }else{
                    promptForResendVerificationEmailAddress(user.getEmail());
                }
            }
        });

        link_unlinkAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Jump.getSignedInUser() != null && Jump.getAccessToken() != null) {
                    MainActivity.this.startActivity(new Intent(MainActivity.this, com.janrain.android.multidex.simpledemonative.LinkListActivity.class));
                } else {
                    Toast.makeText(MainActivity.this, "Please Login to Link Account",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Jump.signOutCaptureUser(MainActivity.this);
                Toast.makeText(MainActivity.this, "Signed out of Capture",
                        Toast.LENGTH_LONG).show();
            }
        });

        nativeSignOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (facebookToken != null){
                    LoginManager.getInstance().logOut();
                    LogUtils.logd("Logged out of Facebook");
                }
                try {
                    mGoogleSignInClient.signOut();
                } catch(Exception ex){}//Do nothing

                if(twitterToken != null){
                    MainActivity.ClearCookies(MainActivity.this);
                    TwitterCore
                            .getInstance()
                            .getSessionManager()
                            .clearActiveSession();

                    LogUtils.logd("Logged out of Twitter");
                }
                Jump.signOutCaptureUser(MainActivity.this);
                Toast.makeText(MainActivity.this, "Signed out of Capture and all Native Providers",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    //From: http://stackoverflow.com/questions/28998241/how-to-clear-cookies-and-cache-of-webview-on-android-when-not-in-webview
    @SuppressWarnings("deprecation")
    private static void ClearCookies(Context context)
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LogUtils.logd("Using ClearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else
        {
            LogUtils.logd("Using ClearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP));
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    private void promptForResendVerificationEmailAddress(String emailAddress) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if(!emailAddress.isEmpty()){
            input.setText(emailAddress);
        }
        AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
        b.setView(input);
        b.setTitle("Please confirm your email address");
        b.setMessage("We'll resend your verification email.");
        b.setNegativeButton("Cancel", null);
        b.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String emailAddress = input.getText().toString();
                sendVerificationEmail(emailAddress);
            }
        });
        b.show();
    }

    private void sendVerificationEmail(String emailAddress) {
        Jump.resendEmailVerification(emailAddress, new CaptureApiRequestCallback() {

            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Verification email sent.", Toast.LENGTH_LONG).show();
            }

            public void onFailure(CaptureApiError e) {
                String error = (e.error_message == null || e.error_message.isEmpty()) ? e.error_description : e.error_message;
                Toast.makeText(MainActivity.this, "Failed to send verification email: " + error,
                         Toast.LENGTH_LONG).show();
            }
        });
    }

    private Button addButton(LinearLayout linearLayout, String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        linearLayout.addView(button);
        return button;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        // Activity being restarted from stopped state
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mGoogleApiClient.disconnect();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            String authCode = account.getServerAuthCode();
            // Signed in successfully, show authenticated UI.
            LogUtils.logd("Google Server AuthCode: " + authCode);
            Jump.startCodeAuthForNativeProvider(MainActivity.this, "googleplus", authCode,"",MainActivity.this.signInResultHandler,"");
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            LogUtils.loge("Google Sign-in failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.logd("onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else if (requestCode == FACEBOOK_REQUEST_CODE_SIGN_IN){
            LogUtils.logd("requestCode: FACEBOOK_REQUEST_CODE_SIGN_IN");
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == TWITTER_REQUEST_CODE_SIGN_IN){
            LogUtils.logd("requestCode: TWITTER_REQUEST_CODE_SIGN_IN");
            twitterAuthClient.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        Jump.saveToDisk(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Jump.saveToDisk(this);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if(Jump.getCaptureFlowName() != "") flowDownloaded = true;
    }

    //Left in for testing and debugging use
    private static void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                        //        .detectDiskReads()
                        //        .detectDiskWrites()
                        //        .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                        //        .penaltyDeath()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                //.detectAll()
                //.detectActivityLeaks()
                //.detectLeakedSqlLiteObjects()
                //.detectLeakedClosableObjects()
                .penaltyLog()
                        //.penaltyDeath()
                .build());
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(flowMessageReceiver);
        super.onDestroy();
        //Facebook SDK
        facebookAccessTokenTracker.stopTracking();
    }


}
