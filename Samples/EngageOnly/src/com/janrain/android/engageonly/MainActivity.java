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
package com.janrain.android.engageonly;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
//import com.janrain.android.engage.session.JRSession;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;

import net.openid.appauth.AuthorizationService;

public class MainActivity extends AppCompatActivity implements JREngageDelegate {

    private static final String ENGAGE_APP_ID = "appcfamhnpkagijaeinl";
    private static final String ENGAGE_TOKEN_URL = "";
    private static final String TAG = "MainActivity";
    private JREngage mEngage;
    //private JREngageDelegate mEngageDelegate = ...;

    private static final int DIALOG_JRENGAGE_ERROR = 1;

    private Button mAuthButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEngage = JREngage.initInstance(this.getApplicationContext(), ENGAGE_APP_ID, ENGAGE_TOKEN_URL, this);
        JREngage.blockOnInitialization();

        this.setContentView(R.layout.activity_main);
        this.mAuthButton = (Button)this.findViewById(R.id.mAuthButton);
        this.mAuthButton.setOnClickListener(mAuthButtonClickListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    View.OnClickListener mAuthButtonClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            // This button/listener becomes a "done editing" button when the user enters edit mode
            /* To see an example of how you can force the user to always reauthenticate and skip the
                /* returning user landing page, uncomment the following two lines, and comment-out the
                /* third */
                /* mEngage.setAlwaysForceReauthentication(true); */
                /* mEngage.showAuthenticationDialog(ProfilesActivity.this, true); */
            AuthorizationService authorizationService = new AuthorizationService(MainActivity.this);
            mEngage.setAuthorizationService(authorizationService);
            mEngage.setAuthorizationActivity(MainActivity.this);
            mEngage.showAuthenticationDialog(MainActivity.this);

        }
    };

    public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
        Log.d(TAG, "[jrEngageDialogDidFailToShowWithError]");

        String mDialogErrorMessage = "Authentication Error: " +
                ((error == null) ? "unknown" : error.getMessage());

        Toast.makeText(this, mDialogErrorMessage, Toast.LENGTH_SHORT).show();
    }

    public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {

        JRDictionary profile = (auth_info == null) ? null : auth_info.getAsDictionary("profile");
        String displayName = (profile == null) ? null : profile.getAsString("displayName");
        String message = "Authentication successful" + ((TextUtils.isEmpty(displayName))
                ? "" : (" for user: " + displayName));
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void jrAuthenticationDidReachTokenUrl(String tokenUrl,
                                                 HttpResponseHeaders response,
                                                 String tokenUrlPayload,
                                                 String provider) {
        Toast.makeText(this, "Authentication did reach token url", Toast.LENGTH_SHORT).show();
    }

    public void jrAuthenticationDidNotComplete() {
        Toast.makeText(this, "Authentication did not complete", Toast.LENGTH_SHORT).show();
    }

    public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
        String message = "Authentication failed, error: " +
                ((error == null) ? "unknown" : error.getMessage());

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {
        Toast.makeText(this, "Authentication failed to reach token url", Toast.LENGTH_SHORT).show();
    }

    public void jrSocialDidNotCompletePublishing() { }

    public void jrSocialDidCompletePublishing() { }

    public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
    }
    public void jrSocialPublishJRActivityDidFail(JRActivityObject activity,
                                                 JREngageError error,
                                                 String provider) {
    }
}