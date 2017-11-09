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
import android.widget.EditText;
import android.widget.Toast;

import com.janrain.android.Jump;
import com.janrain.android.capture.Capture;
import com.janrain.android.capture.CaptureApiError;
import com.janrain.android.capture.CaptureRecord;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import static com.janrain.android.simpledemo.R.id.change_password_old;
import static com.janrain.android.simpledemo.R.id.change_password_new;
import static com.janrain.android.simpledemo.R.id.change_password_confirm;

public class ChangePasswordActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password_activity);

        setTitle("Change Password");

        CaptureRecord user = Jump.getSignedInUser();

    }

    public void changePassword(View view) {

        CaptureRecord user = Jump.getSignedInUser();

        String oldPassword = getEditTextString(change_password_old);
        String newPassword = getEditTextString(change_password_new);
        String confirmPassword = getEditTextString(change_password_confirm);

        Map<String, String> fieldMap = new HashMap<String, String>();
        fieldMap.put("oldpassword", currentPassword);
        fieldMap.put("newpassword", newPassword);
        fieldMap.put("newpasswordConfirm", confirmPassword);

        Capture.updateUserProfileWithFormFieldsProvided(fieldMap,
                "/oauth/update_profile_native",
                "newPasswordFormProfile",
                user.getAccessToken(),
                new Capture.CaptureApiRequestCallback() {

            public void onSuccess() {
                Toast.makeText(ChangePasswordActivity.this, "Password Changed", Toast.LENGTH_LONG).show();
                finish();
            }

            public void onFailure(CaptureApiError error) {
                AlertDialog.Builder adb = new AlertDialog.Builder(ChangePasswordActivity.this);
                adb.setTitle("Error");
                adb.setMessage(error.toString());
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

    private String getEditTextString(int layoutId) {
        return ((EditText) findViewById(layoutId)).getText().toString();
    }

    private void setEditTextString(int layoutId, String value) {
        ((EditText) findViewById(layoutId)).setText(value);
    }
}
