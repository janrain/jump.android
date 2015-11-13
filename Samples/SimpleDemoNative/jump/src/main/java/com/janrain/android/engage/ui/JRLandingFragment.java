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
package com.janrain.android.engage.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.janrain.android.R;
import com.janrain.android.engage.session.JRAuthenticatedUser;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.utils.AndroidUtils;
import com.janrain.android.utils.LogUtils;

/**
 * @internal
 *
 * @class JRLandingFragment
 **/
public class JRLandingFragment extends JRUiFragment {
    public static final String KEY_ALERT_DIALOG_TITLE = "jr_alert_dialog_title";
    public static final String KEY_ALERT_DIALOG_MESSAGE = "jr_alert_dialog_message";
    public static final int DIALOG_GENERIC_ALERT = 1;
    
    public static final int RESULT_SWITCH_ACCOUNTS = Activity.RESULT_FIRST_USER;
    public static final int RESULT_RESTART = Activity.RESULT_FIRST_USER + 1;
    public static final int RESULT_FAIL = Activity.RESULT_FIRST_USER + 2;

    private View.OnClickListener mButtonListener = new View.OnClickListener() {
        public void onClick(View view) {
            LogUtils.logd(TAG, "[onClick] handled");

            if (view.equals(mSignInButton)) {
                onSignInClick();
            } else if (view.equals(mSwitchAccountButton)) {
                onSwitchAccountsClick();
            }
        }
    };

    private boolean mIsAlertShowing = false;
    private boolean mIsFinishPending = false;

    private ImageView mLogo;
    private EditText mUserInput;
    private TextView mWelcomeLabel;
    private TextView mProviderName;

    private Button mSwitchAccountButton;
    private ColorButton mSignInButton;
    private JRProvider mProvider;

    public JRLandingFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mSession == null) {
            finishFragment();
            return;
        }

        mProvider = mSession.getCurrentlyAuthenticatingProvider();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mSession == null) return null;
        View view = inflater.inflate(R.layout.jr_provider_landing, container, false);

        mLogo = (ImageView)view.findViewById(R.id.jr_landing_logo);
        mUserInput = (EditText)view.findViewById(R.id.jr_landing_edit);
        mWelcomeLabel = (TextView)view.findViewById(R.id.jr_landing_welcome_label);
        mSwitchAccountButton = (Button)view.findViewById(R.id.jr_landing_switch_account_button);
        mSignInButton = (ColorButton)view.findViewById(R.id.jr_landing_small_signin_button);
        mProviderName = (TextView)view.findViewById(R.id.jr_row_provider_label);

        mSwitchAccountButton.setOnClickListener(mButtonListener);
        mSignInButton.setOnClickListener(mButtonListener);
        mSignInButton.setColor(getColor(R.color.jr_janrain_darkblue_lightened));
        if (AndroidUtils.SDK_INT <= 10) {
            /* Todo this should really test for Theme.Holo or it's descendants */
            mSignInButton.setTextColor(getColor(android.R.color.white));
        }

        mLogo.setImageDrawable(mProvider.getProviderIcon(getActivity()));
        mProviderName.setText(mProvider.getFriendlyName());

        if (mProvider.getName().equals("openid")) {
            mUserInput.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_URI);
        }

        if (mProvider.requiresInput()) {
            LogUtils.logd(TAG, "[prepareUserInterface] current provider requires input");
            configureButtonVisibility(true); // one button UI

            mWelcomeLabel.setVisibility(View.GONE);

            mUserInput.setVisibility(View.VISIBLE);
            mUserInput.setText(mProvider.getUserInput());
            mUserInput.setHint(mProvider.getUserInputHint());
        } else { // doesn't require input
            configureButtonVisibility(false); // = two button UI -> Switch Accounts is showing
            LogUtils.logd(TAG, "[prepareUserInterface] current provider doesn't require input");

            mUserInput.setVisibility(View.GONE);

            mWelcomeLabel.setVisibility(View.VISIBLE);
            JRAuthenticatedUser user = mSession.getAuthenticatedUserForProvider(mProvider);
            if (user == null) {
                finishFragmentWithResult(RESULT_RESTART);
                return view;
            }
            mWelcomeLabel.setText(user.getWelcomeMessage());
        }

        return view;
    }

    private void onSignInClick() {
        if (mSession.getCurrentlyAuthenticatingProvider().requiresInput()) {
            //TODO validate OpenID URLs so they don't hang the WebView
            String text = mUserInput.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                String title = getString(R.string.jr_landing_bad_user_input);
                String message = getString(R.string.jr_landing_bad_input_long);
                Bundle options = new Bundle();
                options.putString(KEY_ALERT_DIALOG_TITLE, title);
                options.putString(KEY_ALERT_DIALOG_MESSAGE, message);
                showDialog(DIALOG_GENERIC_ALERT, options);
                mIsAlertShowing = true;
            } else {
                mSession.getCurrentlyAuthenticatingProvider().setUserInput(text);
                showWebView();
            }
        } else {
            showWebView();
        }
    }

    private void onSwitchAccountsClick() {
        LogUtils.logd(TAG, "[onSwitchAccountsClick]");

        //mSession.getCurrentlyAuthenticatingProvider().setForceReauth(true);
        mSession.signOutUserForProvider(mSession.getCurrentlyAuthenticatingProvider().getName());
        mSession.setReturningAuthProvider("");
        mSession.setCurrentlyAuthenticatingProviderPermissions(null);
        mSession.triggerAuthenticationDidRestart();
        finishFragmentWithResult(RESULT_SWITCH_ACCOUNTS);
    }

    @Override
    /*package*/ void onPrepareDialog(int id, Dialog d, Bundle options) {
        if (id == DIALOG_GENERIC_ALERT) {
            AlertDialog d_ = (AlertDialog) d;
            d_.setTitle(options.getString(KEY_ALERT_DIALOG_TITLE));
            d_.setMessage(options.getString(KEY_ALERT_DIALOG_MESSAGE));
            return;
        }

        super.onPrepareDialog(id, d, options);
    }

    @Override
    /*package*/ Dialog onCreateDialog(int id, Bundle options) {
        if (id == DIALOG_GENERIC_ALERT) {
            return new AlertDialog.Builder(getActivity())
                .setTitle(options.getString(KEY_ALERT_DIALOG_TITLE))
                .setMessage(options.getString(KEY_ALERT_DIALOG_MESSAGE))
                .setPositiveButton(getString(R.string.jr_dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mIsAlertShowing = false;
                        if (mIsFinishPending) {
                            mIsFinishPending = false;
                            /* If there is a pending Finish that we deferred because the dialog was displayed
                             * it is called here, now. */
                            tryToFinishFragment();
                        }
                    }
                }).create();
        }

        return super.onCreateDialog(id, options);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_WEBVIEW) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    finishFragmentWithResult(Activity.RESULT_OK);
                    break;
                case JRWebViewFragment.RESULT_FAIL_AND_STOP:
                    finishFragmentWithResult(RESULT_FAIL);
                case JRWebViewFragment.RESULT_RESTART:
                    finishFragmentWithResult(RESULT_RESTART);
                    break;
                case JRWebViewFragment.RESULT_BAD_OPENID_URL:
                    break;
                default:
                    Log.e(TAG, "Unrecognized request/result code " + requestCode + "/" + resultCode);
            }
        } else {
            Log.e(TAG, "Unrecognized request/result code " + requestCode + "/" + resultCode);
        }
    }

    @Override
    /*package*/ void tryToFinishFragment() {
        LogUtils.logd(TAG, "[tryToFinishFragment]");
        if (mIsAlertShowing) {
            mIsFinishPending = true;
        } else {
            finishFragment();
        }
    }

    private void configureButtonVisibility(boolean isSingleButtonLayout) {
        if (isSingleButtonLayout) {
            mSwitchAccountButton.setVisibility(View.INVISIBLE);
            mSignInButton.setVisibility(View.VISIBLE);
        } else {
            mSwitchAccountButton.setVisibility(View.VISIBLE);
            mSignInButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    /*package*/ String getCustomTitle() {
        JRProvider provider = mSession.getCurrentlyAuthenticatingProvider();
        if (provider.requiresInput()) {
            return provider.getUserInputDescriptor();
        } else if (getCustomUiConfiguration() != null &&
                getCustomUiConfiguration().mLandingTitle != null) {
            return getCustomUiConfiguration().mLandingTitle;
        } else {
            return getString(R.string.jr_landing_default_custom_title);
        }
    }

    @Override
    /*package*/ void onBackPressed() {
        LogUtils.logd(TAG, "[onBackPressed]");

        if (mSession == null) {
            finishFragmentWithResult(RESULT_RESTART);
            return;
        }

        if (isSpecificProviderFlow()) {
            mSession.triggerAuthenticationDidCancel();
        } else {
            mSession.triggerAuthenticationDidRestart();
        }

        finishFragmentWithResult(RESULT_RESTART);
    }

    @Override
    /*package*/ boolean shouldShowTitleWhenDialog() {
        return getCustomUiConfiguration() != null &&
                getCustomUiConfiguration().mShowLandingTitleWhenDialog != null &&
                getCustomUiConfiguration().mShowLandingTitleWhenDialog;
    }
}