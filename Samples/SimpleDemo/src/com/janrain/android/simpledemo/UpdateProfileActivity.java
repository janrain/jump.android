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
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.janrain.android.Jump;
import com.janrain.android.capture.Capture;
import com.janrain.android.capture.CaptureApiError;
import com.janrain.android.capture.CaptureRecord;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.janrain.android.simpledemo.R.id.update_profile_addressCity;
import static com.janrain.android.simpledemo.R.id.update_profile_addressCountry;
import static com.janrain.android.simpledemo.R.id.update_profile_addressPostalCode;
import static com.janrain.android.simpledemo.R.id.update_profile_addressState;
import static com.janrain.android.simpledemo.R.id.update_profile_addressStreet1;
import static com.janrain.android.simpledemo.R.id.update_profile_addressStreet2;
import static com.janrain.android.simpledemo.R.id.update_profile_birthdate;
import static com.janrain.android.simpledemo.R.id.update_profile_display_name;
import static com.janrain.android.simpledemo.R.id.update_profile_email;
import static com.janrain.android.simpledemo.R.id.update_profile_first_name;
import static com.janrain.android.simpledemo.R.id.update_profile_gender;
import static com.janrain.android.simpledemo.R.id.update_profile_last_name;
import static com.janrain.android.simpledemo.R.id.update_profile_about;
import static com.janrain.android.simpledemo.R.id.update_profile_middle_name;
import static com.janrain.android.simpledemo.R.id.update_profile_mobile;
import static com.janrain.android.simpledemo.R.id.update_profile_optIn;
import static com.janrain.android.simpledemo.R.id.update_profile_phone;

public class UpdateProfileActivity extends Activity {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private FieldOptionsHolder genderOptions;
    private FieldOptionsHolder stateOptions;
    private FieldOptionsHolder countryOptions;
    private Date birthDate;

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
        setEditTextString(update_profile_phone, getStringOrNullFromUser(user, "primaryAddress.phone"));
        setEditTextString(update_profile_mobile, getStringOrNullFromUser(user, "primaryAddress.mobile"));
        setEditTextString(update_profile_addressStreet1, getStringOrNullFromUser(user, "primaryAddress.address1"));
        setEditTextString(update_profile_addressStreet2, getStringOrNullFromUser(user, "primaryAddress.address2"));
        setEditTextString(update_profile_addressCity, getStringOrNullFromUser(user, "primaryAddress.city"));
        setEditTextString(update_profile_addressPostalCode, getStringOrNullFromUser(user, "primaryAddress.zip"));
        setCheckBoxBoolean(update_profile_optIn, getBooleanFromUser(user, "optIn.status", false));
        setEditTextString(update_profile_about, getStringOrNullFromUser(user, "aboutMe"));

        updateBirthDate(getDateOrNullFromUser(user, "birthday"));

        genderOptions = getFieldOptions("gender");
        stateOptions = getFieldOptions("addressState");
        countryOptions = getFieldOptions("addressCountry");

        initSpinner(update_profile_gender, getStringOrNullFromUser(user, "gender"), genderOptions);
        initSpinner(R.id.update_profile_addressState, getStringOrNullFromUser(user, "primaryAddress.stateAbbreviation"), stateOptions);
        initSpinner(R.id.update_profile_addressCountry, getStringOrNullFromUser(user, "primaryAddress.country"), countryOptions);

        findViewById(update_profile_birthdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBirthDateClick();
            }
        });
    }

    public void update(View view) {
        CaptureRecord user = Jump.getSignedInUser();

        String email = getEditTextString(update_profile_email);
        String firstName = getEditTextString(update_profile_first_name);
        String middleName = getEditTextString(update_profile_middle_name);
        String lastName = getEditTextString(update_profile_last_name);
        String displayName = getEditTextString(update_profile_display_name);
        String phone = getEditTextString(update_profile_phone);
        String mobile = getEditTextString(update_profile_mobile);
        String addressStreet1 = getEditTextString(update_profile_addressStreet1);
        String addressStreet2 = getEditTextString(update_profile_addressStreet2);
        String addressCity = getEditTextString(update_profile_addressCity);
        String addressPostalCode = getEditTextString(update_profile_addressPostalCode);

        String gender = getSpinnerSelectedValue(update_profile_gender, genderOptions);
        String addressState = getSpinnerSelectedValue(update_profile_addressState, stateOptions);
        String addressCountry = getSpinnerSelectedValue(update_profile_addressCountry, countryOptions);

        String about = getEditTextString(update_profile_about);

        try {
            user.put("email", email);
            user.put("displayName", displayName);
            user.put("givenName", firstName);
            user.put("middleName", middleName);
            user.put("familyName", lastName);
            user.put("gender", gender);
            if (birthDate != null) {
                user.put("birthday", DATE_FORMAT.format(birthDate));
            }

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

    private void onBirthDateClick() {
        final DatePickerFragment fragment = DatePickerFragment.getInstance(birthDate);
        final DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                final Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);

                updateBirthDate(calendar.getTime());
            }
        };

        fragment.setOnDateSetListener(listener);
        fragment.show(getFragmentManager(), "datePicker");
    }

    private void updateBirthDate(Date date) {
        birthDate = date;
        if (birthDate != null) {
            setEditTextString(update_profile_birthdate, DATE_FORMAT.format(birthDate));
        }
    }

    @SuppressWarnings("unchecked")
    private FieldOptionsHolder getFieldOptions(String fieldName) {
        Object objFields = Jump.getCaptureFlow().get("fields");
        if (objFields == null || !(objFields instanceof Map)) {
            return null;
        }

        Map<String, Object> fields = (Map<String, Object>) objFields;
        Object objFieldData = fields.get(fieldName);
        if (objFieldData == null || !(objFieldData instanceof Map)) {
            return null;
        }

        Map<String, Object> fieldData = (Map<String, Object>) objFieldData;
        Object objFieldOptions = fieldData.get("options");
        if (objFieldOptions == null || !(objFieldOptions instanceof List)) {
            return null;
        }

        List<Map<String, Object>> fieldOptions = (List<Map<String, Object>>) objFieldOptions;
        int valuesCount = fieldOptions.size();
        final FieldOptionsHolder holder = new FieldOptionsHolder();
        holder.names = new String[valuesCount];
        holder.values = new String[valuesCount];
        holder.disabledPositions = new boolean[valuesCount];
        int i = 0;
        for (Map<String, Object> values : fieldOptions) {
            String value = (String) values.get("value");
            String text = (String) values.get("text");
            Boolean disabled = (Boolean) values.get("disabled");

            holder.names[i] = text != null ? text : "";
            holder.values[i] = value != null ? value : "";
            holder.disabledPositions[i] = disabled != null && disabled;

            i++;
        }

        return holder;
    }

    private void initSpinner(int viewId, String fieldValue, final FieldOptionsHolder options) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options.names) {
                    @Override
                    public boolean isEnabled(int position) {
                        return !options.disabledPositions[position];
                    }
                };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(viewId);
        spinner.setAdapter(adapter);
        int genderSelection = 0;
        for (int i = 0; i < options.values.length; i++) {
            if (options.values[i].equals(fieldValue)) {
                genderSelection = i;
                break;
            }
        }
        spinner.setSelection(genderSelection);
    }

    private String getStringOrNullFromUser(CaptureRecord user, String key) {
        Object result = getObjectOrNullFromUser(user, key);
        if (result == null) {
            return null;
        }

        return String.valueOf(result);
    }

    private Date getDateOrNullFromUser(CaptureRecord user, String key) {
        String strDate = getStringOrNullFromUser(user, key);
        if (strDate == null) {
            return null;
        }

        try {
            return DATE_FORMAT.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean getBooleanFromUser(CaptureRecord user, String key, boolean defaultValue) {
        if (user.isNull(key)) {
            return defaultValue;
        }
        return user.optBoolean(key, defaultValue);
    }

    private String getEditTextString(int layoutId) {
        return ((TextView) findViewById(layoutId)).getText().toString();
    }

    private void setEditTextString(int layoutId, String value) {
        ((TextView) findViewById(layoutId)).setText(value);
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

    public String getSpinnerSelectedValue(int layoutId, FieldOptionsHolder optionsHolder) {
        if (optionsHolder == null) {
            return null;
        }

        int position = ((Spinner) findViewById(layoutId)).getSelectedItemPosition();
        return optionsHolder.values[position];
    }

    private final class FieldOptionsHolder {
        public String[] names;
        public String[] values;
        public boolean[] disabledPositions;
    }
}
