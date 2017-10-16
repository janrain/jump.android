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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    private String[] genderNames;
    private String[] genderValues;
    private boolean[] genderSelectables;

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
        setEditTextString(update_profile_phone, getStringOrNullFromUser(user, "primaryAddress.phone"));
        setEditTextString(update_profile_mobile, getStringOrNullFromUser(user, "primaryAddress.mobile"));
        setEditTextString(update_profile_addressStreet1, getStringOrNullFromUser(user, "primaryAddress.address1"));
        setEditTextString(update_profile_addressStreet2, getStringOrNullFromUser(user, "primaryAddress.address2"));
        setEditTextString(update_profile_addressCity, getStringOrNullFromUser(user, "primaryAddress.city"));
        setEditTextString(update_profile_addressPostalCode, getStringOrNullFromUser(user, "primaryAddress.zip"));
        setEditTextString(update_profile_addressState, getStringOrNullFromUser(user, "primaryAddress.stateAbbreviation"));
        setEditTextString(update_profile_addressCountry, getStringOrNullFromUser(user, "primaryAddress.country"));
        setCheckBoxBoolean(update_profile_optIn, getBooleanFromUser(user, "optIn.status", false));
        setEditTextString(update_profile_about, getStringOrNullFromUser(user, "aboutMe"));

        initGenderValues();
        initGenderAdapter(user);
    }

    public void update(View view) {
        CaptureRecord user = Jump.getSignedInUser();

        String email = getEditTextString(update_profile_email);
        String firstName = getEditTextString(update_profile_first_name);
        String middleName = getEditTextString(update_profile_middle_name);
        String lastName = getEditTextString(update_profile_last_name);
        String displayName = getEditTextString(update_profile_display_name);
        String gender = getSpinnerSelectedValue(R.id.update_profile_gender, genderValues);
        String phone = getEditTextString(update_profile_phone);
        String mobile = getEditTextString(update_profile_mobile);
        String addressStreet1 = getEditTextString(update_profile_addressStreet1);
        String addressStreet2 = getEditTextString(update_profile_addressStreet2);
        String addressCity = getEditTextString(update_profile_addressCity);
        String addressPostalCode = getEditTextString(update_profile_addressPostalCode);
        String addressState = getEditTextString(update_profile_addressState);
        String addressCountry = getEditTextString(update_profile_addressCountry);

        String about = getEditTextString(update_profile_about);

        try {
            user.put("email", email);
            user.put("displayName", displayName);
            user.put("givenName", firstName);
            user.put("middleName", middleName);
            user.put("familyName", lastName);
            user.put("gender", gender);

            final JSONObject primaryAddressObj = !user.isNull("primaryAddress") ?
                    user.optJSONObject("primaryAddress") : new JSONObject();
            primaryAddressObj.put("phone", phone);
            primaryAddressObj.put("mobile", mobile);
            primaryAddressObj.put("address1", addressStreet1);
            primaryAddressObj.put("address2", addressStreet2);
            primaryAddressObj.put("city", addressCity);
            primaryAddressObj.put("zip", addressPostalCode);
            primaryAddressObj.put("stateAbbreviation", addressState);
            primaryAddressObj.put("country", addressCountry);
            user.put("primaryAddress", primaryAddressObj);

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
                adb.setMessage(errorMsg);
                adb.show();
            }
        });
    }

    private void initGenderValues() {
        Map<String, Object> fields = (Map<String, Object>) Jump.getCaptureFlow().get("fields");
        Map<String, Object> fieldGender = (Map<String, Object>) fields.get("gender");
        List<Map<String, Object>> fieldGenderOptions = (List<Map<String, Object>>) fieldGender.get("options");

        int valuesCount = fieldGenderOptions.size();
        genderNames = new String[valuesCount];
        genderValues = new String[valuesCount];
        genderSelectables = new boolean[valuesCount];
        int i = 0;
        for (Map<String, Object> values : fieldGenderOptions) {
            String value = (String) values.get("value");
            String text = (String) values.get("text");
            Boolean disabled = (Boolean) values.get("disabled");

            genderNames[i] = text != null ? text : "";
            genderValues[i] = value != null ? value : "";
            genderSelectables[i] = disabled == null || !disabled;

            i++;
        }
    }

    private void initGenderAdapter(CaptureRecord user) {
        final String gender = getStringOrNullFromUser(user, "gender");
        ArrayAdapter<String> genderAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, genderNames) {
                    @Override
                    public boolean isEnabled(int position) {
                        return genderSelectables[position];
                    }
                };
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner genderSpinner = (Spinner) findViewById(R.id.update_profile_gender);
        genderSpinner.setAdapter(genderAdapter);
        int genderSelection = 0;
        for (int i = 0; i < genderValues.length; i++) {
            if (genderValues[i].equals(gender)) {
                genderSelection = i;
                break;
            }
        }
        genderSpinner.setSelection(genderSelection);
    }

    private String getStringOrNullFromUser(CaptureRecord user, String key) {
        Object result = getObjectOrNullFromUser(user, key);
        if (result == null) {
            return null;
        }

        return String.valueOf(result);
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

    private Object getObjectOrNullFromUser(CaptureRecord user, String key) {
        if (key == null || key.isEmpty() || key.endsWith(".")) {
            return null;
        }

        if (!key.contains(".")) {
            return !user.isNull(key) ? user.optString(key) : null;
        }

        LinkedList<String> segments = new LinkedList<>(Arrays.asList(key.split("\\.")));
        String last = segments.removeLast();
        JSONObject currentObject = user;
        for (String segment : segments) {
            currentObject = currentObject.optJSONObject(segment);
            if (currentObject == null) {
                return null;
            }
        }

        return !currentObject.isNull(last) ? currentObject.opt(last) : null;
    }

    public String getSpinnerSelectedValue(int layoutId, String[] values) {
        int position = ((Spinner) findViewById(layoutId)).getSelectedItemPosition();
        return values[position];
    }
}
