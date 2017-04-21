package com.janrain.android.multidex.simpledemonative;

/*
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* Copyright (c) 2011, Janrain, Inc.
*
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without modification,
* are permitted provided that the following conditions are met:
*
* * Redistributions of source code must retain the above copyright notice, this
* list of conditions and the following disclaimer.
* * Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation and/or
* other materials provided with the distribution.
* * Neither the name of the Janrain, Inc. nor the names of its
* contributors may be used to endorse or promote products derived from this
* software without specific prior written permission.
*
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
* ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
* ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
*/

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.janrain.android.Jump;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;
-import com.janrain.android.multidex.simpledemonative.R;
import com.janrain.android.utils.LogUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class LinkListActivity extends ListActivity {
    private static final String TAG = ListActivity.class.getSimpleName();
    private static com.janrain.android.simpledemo.LinkAccountsAdapter mAdapter;
    private final MyCaptureApiResultHandler captureApiResultHandler = new MyCaptureApiResultHandler();
    ListView link_account;
    TextView mIdentifier;
    ImageView unlinkAccount;
    private Button mLinkAccount;
    private int position;
    private boolean link_unlink = false;
    private class MyEngageDelegate implements JREngageDelegate, Jump.CaptureLinkAccountHandler {
        public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
            String message = "Simpledemo:\nJREngage dialog failed to show.\nError: " +
                    ((error == null) ? "unknown" : error.getMessage());
            Toast.makeText(LinkListActivity.this, message, Toast.LENGTH_LONG).show();
        }

        public void jrAuthenticationDidSucceedForUser(JRDictionary authInfo, String provider) {
            String deviceToken = authInfo.getAsString("device_token");
            JRDictionary profile = (authInfo == null) ? null : authInfo.getAsDictionary("profile");
            String identifier = profile.getAsString("identifier");
            String displayName = (profile == null) ? null : profile.getAsString("displayName");
            String message = "Authentication successful" + ((TextUtils.isEmpty(displayName))
                    ? "" : (" for user: " + displayName));
            Toast.makeText(LinkListActivity.this, message, Toast.LENGTH_LONG).show();
        }

        @Override
        public void jrAuthenticationDidSucceedForLinkAccount(JRDictionary auth_info, String provider) {
            String token = auth_info.getAsString("token");
            if(link_unlink == true){
            Jump.performLinkAccount(token, captureApiResultHandler);
            }
        }

        public void jrAuthenticationDidReachTokenUrl(String tokenUrl,
                                                     HttpResponseHeaders response,
                                                     String tokenUrlPayload,
                                                     String provider) {
            org.apache.http.Header[] headers = response.getHeaders();
            org.apache.http.cookie.Cookie[] cookies = response.getCookies();
            String firstCookieValue = response.getHeaderField("set-cookie");
            Toast.makeText(LinkListActivity.this,
                    "Token URL response " + tokenUrlPayload,
                    Toast.LENGTH_LONG).show();
        }

        private void showResultDialog(String title, String message) {
            // This shouldn't be done here because LinkListActivity isn't displayed (resumed?) when this is
            // called but it works most of the time.
            (new AlertDialog.Builder(LinkListActivity.this)).setTitle(title)
                    .setMessage(message)
                    .setNeutralButton("OK", null)
                    .show();
        }

        public void jrAuthenticationDidNotComplete() {
            Toast.makeText(LinkListActivity.this,
                    "Authentication did not complete",
                    Toast.LENGTH_LONG).show();
        }

        public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
            String message = ((error == null) ? "unknown" : error.getMessage());
            Toast.makeText(LinkListActivity.this,
                    "Authentication Failed : " + message,
                    Toast.LENGTH_LONG).show();
        }

        public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl,
                                                          JREngageError error,
                                                          String provider) {
            Toast.makeText(LinkListActivity.this, "Failed to reach token URL", Toast.LENGTH_LONG).show();
        }

        public void jrSocialDidNotCompletePublishing() {
            Toast.makeText(LinkListActivity.this, "Sharing did not complete", Toast.LENGTH_LONG).show();
        }

        public void jrSocialDidCompletePublishing() {
            Toast.makeText(LinkListActivity.this, "Sharing did complete", Toast.LENGTH_LONG).show();
        }

        public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
            Toast.makeText(LinkListActivity.this, "Activity shared", Toast.LENGTH_LONG).show();
        }

        public void jrSocialPublishJRActivityDidFail(JRActivityObject activity,
                                                     JREngageError error,
                                                     String provider) {
            Toast.makeText(LinkListActivity.this, "Activity failed to share", Toast.LENGTH_LONG).show();
        }
    };

    private JREngageDelegate mJREngageDelegate = new MyEngageDelegate();

    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linked_account_listview);
        mLinkAccount = (Button) findViewById(R.id.btn_link_account);
        mLinkAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Jump.getSignedInUser() != null && Jump.getAccessToken() != null) {
                    link_unlink = true;
                    Jump.showSocialSignInDialog(LinkListActivity.this, null, true, mJREngageDelegate);

                } else {
                    LinkListActivity.this.startActivity(new Intent(LinkListActivity.this,
                            com.janrain.android.multidex.simpledemonative.MainActivity.class));
                }
            }
        });
        link_account = (ListView) findViewById(android.R.id.list);
        validateSignedInUser();
        link_account.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                setPosition(position);

                mIdentifier = (TextView) v.findViewById(R.id.row_profile_linkaccount_label);
                unlinkAccount = (ImageView) v.findViewById(R.id.row_unlink_btn);

                AlertDialog.Builder b = new AlertDialog.Builder(LinkListActivity.this);
                b.setTitle("Unlink Account");
                b.setMessage("Do you want to unlink the account?");
                b.setPositiveButton("Unlink", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        link_unlink = false;
                        if (!Jump.getSignedInUser().hasPassword()) {
                            if (link_account.getChildCount() > 1) {
                                Jump.performUnlinkAccount(String.valueOf(mIdentifier.getText()),
                                        captureApiResultHandler);
                            } else {
                                Toast.makeText(LinkListActivity.this,
                                        "Cannot unlink this account",
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        } else {
                            Jump.performUnlinkAccount(String.valueOf(mIdentifier.getText()),
                                    captureApiResultHandler);
                        }
                        dialog.dismiss();
                    }
                });
                b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                b.show();
            }
        });
    }

    public void loadLinkedUnlinkedAccounts() throws JSONException {
        Jump.performFetchCaptureData(new Jump.CaptureApiResultHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                ArrayList<com.janrain.android.multidex.simpledemonative.LinkData> linkUnlinkResults = new ArrayList<com.janrain.android.multidex.simpledemonative.LinkData>();
                JSONObject json = response;
                try {
                    JSONArray profiles = json.getJSONObject("result").getJSONArray("profiles");
                    for (int i = 0; i < profiles.length(); i++) {
                        JSONObject profileData = profiles.getJSONObject(i);
                        LogUtils.loge(profileData.getString("domain"));
                        JSONArray profileEmails = profileData.getJSONObject("profile").getJSONArray("emails");
                        StringBuilder profileEmailsCombined = new StringBuilder();
                        if(profileEmails.length()>0){
                            for(int ii=0; ii<profileEmails.length();ii++){
                                if(profileEmailsCombined.toString() != ""){
                                    profileEmailsCombined.append(", ");
                                }
                                profileEmailsCombined.append(profileEmails.getJSONObject(ii).getString("value"));
                            }
                        }
                        LogUtils.loge(profileEmailsCombined.toString());
                        com.janrain.android.multidex.simpledemonative.LinkData linkedRecords = new com.janrain.android.multidex.simpledemonative.LinkData(profileData.getString("identifier"),
                                profileData.getString("domain"), profileEmailsCombined.toString());
                        linkUnlinkResults.add(linkedRecords);
                        LogUtils.loge(profileData.getString("identifier"));
                    }
                    mAdapter = new com.janrain.android.simpledemo.LinkAccountsAdapter(LinkListActivity.this, linkUnlinkResults);
                    link_account.setAdapter(mAdapter);
                } catch (JSONException e) {
                    LogUtils.loge("Error parsing data " + e.toString());
                }
            }

            @Override
            public void onFailure(CaptureAPIError error) {

                Toast.makeText(LinkListActivity.this,
                        "Account LinkUnlink Failed.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void validateSignedInUser() {
        if (Jump.getSignedInUser() != null && Jump.getAccessToken() != null) {
            try {
                loadLinkedUnlinkedAccounts();
            } catch (JSONException e) {
                Toast.makeText(LinkListActivity.this,
                        "Account LinkUnlink Failed.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private class MyCaptureApiResultHandler implements Jump.CaptureApiResultHandler {
        public void onSuccess(JSONObject response) {
            link_unlink = false;
            validateSignedInUser();
        }

        public void onFailure(CaptureAPIError error) {
            link_unlink = false;
            Toast.makeText(LinkListActivity.this,
                    "Account LinkUnlink Failed.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
