/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2013, Janrain, Inc.
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.janrain.android.Jump;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_addressCity;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_addressCountry;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_addressPostalCode;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_addressState;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_addressStreet1;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_addressStreet2;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_birthdate;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_display_name;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_email;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_first_name;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_gender;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_last_name;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_middle_name;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_mobile;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_optIn;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_password;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_password_confirm;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_password_label;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_password_confirm_label;
import static com.janrain.android.multidex.simpledemonative.R.id.trad_reg_phone;
import static com.janrain.android.utils.CollectionUtils.collectionToHumanReadableString;

public class RegistrationActivity extends Activity {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private FieldOptionsHolder genderOptions;
    private FieldOptionsHolder stateOptions;
    private FieldOptionsHolder countryOptions;
    private Date birthDate;

    private View registerButton;
    private JSONObject newUser = new JSONObject();
    private String socialRegistrationToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity);
        registerButton = findViewById(R.id.trad_reg_button);

        String preregJson = getIntent().getStringExtra("preregistrationRecord");
        String socialRegToken = getIntent().getStringExtra("socialRegistrationToken");

        if (preregJson != null) {
            JSONObject preregistrationRecord;
            try {
                preregistrationRecord = new JSONObject(preregJson);
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }

            newUser = preregistrationRecord;
            socialRegistrationToken = socialRegToken;
            setTitle("Almost Done!");
            setEditTextString(trad_reg_email, getStringOrNullFromUser(newUser, "email"));
            setEditTextString(trad_reg_display_name, getStringOrNullFromUser(newUser, "displayName"));
            setEditTextString(trad_reg_first_name, getStringOrNullFromUser(newUser, "givenName"));
            setEditTextString(trad_reg_middle_name, getStringOrNullFromUser(newUser, "middleName"));
            setEditTextString(trad_reg_last_name, getStringOrNullFromUser(newUser, "familyName"));
            setEditTextString(trad_reg_phone, getStringOrNullFromUser(newUser, "primaryAddress.phone"));
            setEditTextString(trad_reg_mobile, getStringOrNullFromUser(newUser, "primaryAddress.mobile"));
            setEditTextString(trad_reg_addressStreet1, getStringOrNullFromUser(newUser, "primaryAddress.address1"));
            setEditTextString(trad_reg_addressStreet2, getStringOrNullFromUser(newUser, "primaryAddress.address2"));
            setEditTextString(trad_reg_addressCity, getStringOrNullFromUser(newUser, "primaryAddress.city"));
            setEditTextString(trad_reg_addressPostalCode, getStringOrNullFromUser(newUser, "primaryAddress.zip"));
            setCheckBoxBoolean(trad_reg_optIn, getBooleanFromUser(newUser, "optIn.status", false));
            setEditTextString(trad_reg_password, getStringOrNullFromUser(newUser, "password"));
            setEditTextString(trad_reg_password_confirm, getStringOrNullFromUser(newUser, ""));
        } else {
            setTitle("Sign Up");
        }

        if (socialRegToken != null){
            findViewById(trad_reg_password).setVisibility(View.GONE);
            findViewById(trad_reg_password_confirm).setVisibility(View.GONE);
            findViewById(trad_reg_password_label).setVisibility(View.GONE);
            findViewById(trad_reg_password_confirm_label).setVisibility(View.GONE);
        } else {
            findViewById(trad_reg_password).setVisibility(View.VISIBLE);
            findViewById(trad_reg_password_confirm).setVisibility(View.VISIBLE);
            findViewById(trad_reg_password_label).setVisibility(View.VISIBLE);
            findViewById(trad_reg_password_confirm_label).setVisibility(View.VISIBLE);
        }

        updateBirthDate(getDateOrNullFromUser(newUser, "birthday"));

        genderOptions = getFieldOptions("gender");
        stateOptions = getFieldOptions("addressState");
        countryOptions = getFieldOptions("addressCountry");

        initSpinner(trad_reg_gender, getStringOrNullFromUser(newUser, "gender"), genderOptions);
        initSpinner(trad_reg_addressState, getStringOrNullFromUser(newUser, "primaryAddress.stateAbbreviation"), stateOptions);
        initSpinner(trad_reg_addressCountry, getStringOrNullFromUser(newUser, "primaryAddress.country"), countryOptions);

        findViewById(trad_reg_birthdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBirthDateClick();
            }
        });
    }

    public void register(View view) {
        String email = getEditTextString(trad_reg_email);
        String displayName = getEditTextString(trad_reg_display_name);
        String firstName = getEditTextString(trad_reg_first_name);
        String middleName = getEditTextString(trad_reg_middle_name);
        String lastName = getEditTextString(trad_reg_last_name);
        String phone = getEditTextString(trad_reg_phone);
        String mobile = getEditTextString(trad_reg_mobile);
        String addressStreet1 = getEditTextString(trad_reg_addressStreet1);
        String addressStreet2 = getEditTextString(trad_reg_addressStreet2);
        String addressCity = getEditTextString(trad_reg_addressCity);
        String addressPostalCode = getEditTextString(trad_reg_addressPostalCode);

        String gender = getSpinnerSelectedValue(trad_reg_gender, genderOptions);
        String addressState = getSpinnerSelectedValue(trad_reg_addressState, stateOptions);
        String addressCountry = getSpinnerSelectedValue(trad_reg_addressCountry, countryOptions);

        boolean shouldOptIn = getCheckBoxBoolean(R.id.trad_reg_optIn);

        String password = getEditTextString(trad_reg_password);
        String passwordConfirm = getEditTextString(trad_reg_password_confirm);

        if (password.equals(passwordConfirm)){
            try {
                newUser.put("email", email)
                        .put("displayName", displayName)
                        .put("givenName", firstName)
                        .put("middleName", middleName)
                        .put("familyName", lastName)
                        .put("gender", gender)
                        .put("password", password)
                        .put("primaryAddress", new JSONObject()
                                .put("phone", phone)
                                .put("mobile", mobile)
                                .put("address1", addressStreet1)
                                .put("address2", addressStreet2)
                                .put("city", addressCity)
                                .put("zip", addressPostalCode)
                                .put("stateAbbreviation", addressState)
                                .put("country", addressCountry));

                if (shouldOptIn) {
                    newUser.put("optIn", new JSONObject().put("status", true));
                }

                if (birthDate != null) {
                    newUser.put("birthday", DATE_FORMAT.format(birthDate));
                }
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }

            Jump.registerNewUser(newUser, socialRegistrationToken, new MySignInResultHandler());
            registerButton.setEnabled(false);
        }else{
            Toast.makeText(RegistrationActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
        }

    }

    private String getEditTextString(int layoutId) {
        return ((TextView) findViewById(layoutId)).getText().toString();
    }

    private void setEditTextString(int layoutId, String value) {
        ((TextView) findViewById(layoutId)).setText(value);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't need to call Jump.saveToDisk here, there's no state since the user isn't signed in until
        // after they are registered.
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
            setEditTextString(trad_reg_birthdate, DATE_FORMAT.format(birthDate));
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

    private String getStringOrNullFromUser(JSONObject user, String key) {
        Object result = getObjectOrNullFromUser(user, key);
        if (result == null) {
            return null;
        }

        return String.valueOf(result);
    }

    private Date getDateOrNullFromUser(JSONObject user, String key) {
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

    private boolean getBooleanFromUser(JSONObject user, String key, boolean defaultValue) {
        if (user.isNull(key)) {
            return defaultValue;
        }
        return user.optBoolean(key, defaultValue);
    }

    private boolean getCheckBoxBoolean(int layoutId) {
        return ((CheckBox) findViewById(layoutId)).isChecked();
    }

    private void setCheckBoxBoolean(int layoutId, boolean value) {
        ((CheckBox) findViewById(layoutId)).setChecked(value);
    }

    private Object getObjectOrNullFromUser(JSONObject user, String key) {
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

    private class MySignInResultHandler implements Jump.SignInResultHandler, Jump.SignInCodeHandler {
        public void onSuccess() {
            Toast.makeText(RegistrationActivity.this, "Registration Complete", Toast.LENGTH_LONG).show();
            finish();
        }

        public void onCode(String code) {
            Toast.makeText(RegistrationActivity.this, "Authorization Code: " + code, Toast.LENGTH_LONG).show();
        }

        public void onFailure(SignInError error) {
            AlertDialog.Builder adb = new AlertDialog.Builder(RegistrationActivity.this);
            if (error.captureApiError.isFormValidationError()) {
                adb.setTitle("Invalid Fields");
                Map<String, Object> messages =
                        (Map) error.captureApiError.getLocalizedValidationErrorMessages();
                String message = collectionToHumanReadableString(messages);
                adb.setMessage(message);
            } else {
                adb.setTitle("Unrecognized error");
                adb.setMessage(error.toString());
            }
            adb.show();
            registerButton.setEnabled(true);
        }
    }
}
