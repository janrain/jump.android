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

package com.janrain.android.simpledemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.janrain.android.Jump;
import com.janrain.android.capture.Capture;
import com.janrain.android.capture.CaptureApiError;
import com.janrain.android.capture.CaptureRecord;

import org.json.JSONException;

import static com.janrain.android.simpledemo.R.id.update_profile_addressCity;
import static com.janrain.android.simpledemo.R.id.update_profile_addressCountry;
import static com.janrain.android.simpledemo.R.id.update_profile_addressPostalCode;
import static com.janrain.android.simpledemo.R.id.update_profile_addressState;
import static com.janrain.android.simpledemo.R.id.update_profile_addressStreet1;
import static com.janrain.android.simpledemo.R.id.update_profile_addressStreet2;
import static com.janrain.android.simpledemo.R.id.update_profile_display_name;
import static com.janrain.android.simpledemo.R.id.update_profile_email;
import static com.janrain.android.simpledemo.R.id.update_profile_first_name;
import static com.janrain.android.simpledemo.R.id.update_profile_last_name;
import static com.janrain.android.simpledemo.R.id.update_profile_about;
import static com.janrain.android.simpledemo.R.id.update_profile_middle_name;
import static com.janrain.android.simpledemo.R.id.update_profile_mobile;
import static com.janrain.android.simpledemo.R.id.update_profile_optIn;
import static com.janrain.android.simpledemo.R.id.update_profile_phone;

public class UpdateProfileActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_profile_activity);

        setTitle("Update Profile");

        CaptureRecord user = Jump.getSignedInUser();

        setEditTextString(update_profile_email, getStringOrNullFromUser(user, "email"));
        setEditTextString(update_profile_display_name, getStringOrNullFromUser(user, "displayName"));
        setEditTextString(update_profile_first_name, getStringOrNullFromUser(user, "givenName"));
        setEditTextString(update_profile_middle_name, getStringOrNullFromUser(user, "middleName"));
        setEditTextString(update_profile_last_name, getStringOrNullFromUser(user, "familyName"));
//        setEditTextString(update_profile_birthdate, getStringOrNullFromUser(user, "birthdate"));
        setEditTextString(update_profile_phone, getStringOrNullFromUser(user, "phone"));
        setEditTextString(update_profile_mobile, getStringOrNullFromUser(user, "mobile"));
        setEditTextString(update_profile_addressStreet1, getStringOrNullFromUser(user, "addressStreetAddress1"));
        setEditTextString(update_profile_addressStreet2, getStringOrNullFromUser(user, "addressStreetAddress2"));
        setEditTextString(update_profile_addressCity, getStringOrNullFromUser(user, "addressCity"));
        setEditTextString(update_profile_addressPostalCode, getStringOrNullFromUser(user, "addressPostalCode"));
        setEditTextString(update_profile_addressState, getStringOrNullFromUser(user, "addressState"));
        setEditTextString(update_profile_addressCountry, getStringOrNullFromUser(user, "addressCountry"));
        setCheckBoxBoolean(update_profile_optIn, getBooleanFromUser(user, "optIn", false));
        setEditTextString(update_profile_about, getStringOrNullFromUser(user, "aboutMe"));

        final String gender = getStringOrNullFromUser(user, "gender");
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[] {
                        "--",
                        "Not Specified",
                        "Male",
                        "Female",
                        "Other"
                }
        );

        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)findViewById(R.id.update_profile_gender)).setAdapter(genderAdapter);
    }

    public void update(View view) {
        CaptureRecord user = Jump.getSignedInUser();

        String email = getEditTextString(update_profile_email);
        String firstName = getEditTextString(update_profile_first_name);
        String middleName = getEditTextString(update_profile_middle_name);
        String lastName = getEditTextString(update_profile_last_name);
        String displayName = getEditTextString(update_profile_display_name);
        String about = getEditTextString(update_profile_about);

        try {
            user.put("email", email);
            user.put("displayName", displayName);
            user.put("givenName", firstName);
            user.put("middleName", middleName);
            user.put("familyName", lastName);
            user.put("aboutMe", about);
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected ", e);
        }

        Capture.updateUserProfile(user, new Capture.CaptureApiRequestCallback() {

            public void onSuccess() {
                Toast.makeText(UpdateProfileActivity.this, "Profile Updated", Toast.LENGTH_LONG).show();
                finish();
            }

            public void onFailure(CaptureApiError error) {
                String errorMsg = (error.error_message == null || error.error_message.isEmpty()) ? error.error_description : error.error_message;
                AlertDialog.Builder adb = new AlertDialog.Builder(UpdateProfileActivity.this);
                adb.setTitle("Error");
                adb.setMessage(errorMsg.toString());
                adb.show();
            }
        });
    }

    private String getStringOrNullFromUser(CaptureRecord user, String key) {
        if (user.isNull(key)) {
            return null;
        }
        return user.optString(key);
    }

    private boolean getBooleanFromUser(CaptureRecord user, String key, boolean defaultValue) {
        if (user.isNull(key)) {
            return defaultValue;
        }
        return user.optBoolean(key, defaultValue);
    }

    private String getEditTextString(int layoutId) {
        return ((EditText) findViewById(layoutId)).getText().toString();
    }

    private void setEditTextString(int layoutId, String value) {
        ((EditText) findViewById(layoutId)).setText(value);
    }

    private boolean getCheckBoxBoolean(int layoutId) {
        return ((CheckBox) findViewById(layoutId)).isChecked();
    }

    private void setCheckBoxBoolean(int layoutId, boolean value) {
        ((CheckBox) findViewById(layoutId)).setChecked(value);
    }
}
