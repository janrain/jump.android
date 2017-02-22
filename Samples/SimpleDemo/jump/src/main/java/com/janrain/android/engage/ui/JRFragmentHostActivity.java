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
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.janrain.android.R;
import com.janrain.android.engage.session.JRSession;
import com.janrain.android.utils.AndroidUtils;
import com.janrain.android.utils.LogUtils;

public class JRFragmentHostActivity extends FragmentActivity {
    private static final String TAG = JRFragmentHostActivity.class.getSimpleName();

    public static final String JR_UI_CUSTOMIZATION_CLASS = "jr_ui_customization_class";
    public static final String JR_FRAGMENT_ID = "com.janrain.android.engage.JR_FRAGMENT_ID";
    public static final String JR_PROVIDER = "JR_PROVIDER";
    public static final String JR_SIGN_OUT_PROVIDER = "JR_SIGN_OUT_PROVIDER";
    public static final String JR_REVOKE_PROVIDER = "JR_REVOKE_PROVIDER";
    public static final int JR_LANDING = 1;
    public static final int JR_WEBVIEW = 2;
    public static final int JR_PUBLISH = 3;
    public static final int JR_PROVIDER_LIST = 4;
    public static final int JR_OPENID_APPAUTH = 5;
    private static final String JR_OPERATION_MODE = "JR_OPERATION_MODE";
    private static final int JR_DIALOG = 0;
    private static final int JR_FULLSCREEN = 1;
    private static final int JR_FULLSCREEN_NO_TITLE = 2;

    public static final String ACTION_FINISH_FRAGMENT = "com.janrain.android.engage.ACTION_FINISH_FRAGMENT";
    public static final String EXTRA_FINISH_FRAGMENT_TARGET =
            "com.janrain.android.engage.EXTRA_FINISH_FRAGMENT_TARGET";
    public static final String FINISH_TARGET_ALL = "JR_FINISH_ALL";

    public static final IntentFilter FINISH_INTENT_FILTER = new IntentFilter(ACTION_FINISH_FRAGMENT);

    private JRUiFragment mUiFragment;
    private Integer m_Result;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.logd(TAG, "[onCreate]: " + getFragmentId());

        JRSession session = JRSession.getInstance();

        if (session == null || savedInstanceState != null) {
            /* This flow control path is reached when there's process death and restart */
            Log.e(TAG, "bailing out after a process kill/restart. mSession: " + session);

            // prevent fragment recreation error -- the system needs the fragment's container to exist
            // even if the Activity is finishing right away
            setContentView(R.layout.jr_fragment_host_activity);
            super.finish();
            return;
        }

        switch (getFragmentId()) {
            case JR_PROVIDER_LIST:
                mUiFragment = new JRProviderListFragment();
                break;
            case JR_LANDING:
                mUiFragment = new JRLandingFragment();
                break;
            case JR_WEBVIEW:
                mUiFragment = new JRWebViewFragment();
                break;
            case JR_PUBLISH:
                mUiFragment = new JRPublishFragment();
                break;
            default:
                throw new IllegalFragmentIdException(getFragmentId());
        }
        
        Bundle fragArgs = new Bundle();
        fragArgs.putInt(JRUiFragment.JR_FRAGMENT_FLOW_MODE, getFlowMode());
        fragArgs.putAll(getIntent().getExtras());
        mUiFragment.setArguments(fragArgs);

        mUiFragment.onFragmentHostActivityCreate(this, session);

        if (shouldBeDialog()) {
            AndroidUtils.activitySetFinishOnTouchOutside(this, true);

            if (shouldBePhoneSizedDialog()) {
                getTheme().applyStyle(R.style.jr_dialog_phone_sized, true);
            } else {
                getTheme().applyStyle(R.style.jr_dialog_71_percent, true);
            }

            if (!mUiFragment.shouldShowTitleWhenDialog()) {
                getTheme().applyStyle(R.style.jr_disable_title_and_action_bar_style, true);
            }
        } else if (getOperationMode() == JR_FULLSCREEN_NO_TITLE) {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getTheme().applyStyle(R.style.jr_disable_title_and_action_bar_style, true);
        } else if (getOperationMode() == JR_FULLSCREEN) {
            // noop
        }

        setContentView(R.layout.jr_fragment_host_activity);

        View fragmentContainer = findViewById(R.id.jr_fragment_container);
        if (fragmentContainer instanceof CustomMeasuringFrameLayout) {
            // CMFL -> dialog mode on a tablet
            if (shouldBePhoneSizedDialog()) {
                // Do the actual setting of the target size to achieve phone sized dialog.
                ((CustomMeasuringFrameLayout) fragmentContainer).setTargetSizeDip(320, 480);
                getWindow().makeActive();
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(getWindow().getAttributes());
                lp.width = AndroidUtils.scaleDipToPixels(320);
                // After discussing it with Lilli we think it makes sense to let the height of the window
                // grow if the title is enabled
                //int targetHeight = mUiFragment.getCustomTitle() == null ? 480 : 560;
                //lp.height = AndroidUtils.scaleDipToPixels(targetHeight);
                getWindow().setAttributes(lp);
            }
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.jr_fragment_container, mUiFragment)
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .commit();
    }

    private int getOperationMode() {
        return getIntent().getExtras().getInt(JR_OPERATION_MODE);
    }

    private boolean shouldBePhoneSizedDialog() {
        return shouldBeDialog() && !(mUiFragment instanceof JRPublishFragment);
    }

    private boolean shouldBeDialog() {
        return AndroidUtils.isXlarge();
    }

    private int getFragmentId() {
        return getIntent().getExtras().getInt(JR_FRAGMENT_ID);
    }

    private int getFlowMode() {
        return getIntent().getExtras().getInt(JRUiFragment.JR_FRAGMENT_FLOW_MODE);
    }

    private String getSpecificProvider() {
        return getIntent().getExtras().getString(JR_PROVIDER);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.logd(TAG, "requestCode: " + requestCode + " resultCode: " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        /* Sometimes this activity starts an activity by proxy for its fragment, in that case we
         * delegate the result to the fragment here.
         */
        if (requestCode <= 1<<16) mUiFragment.onActivityResult(requestCode, resultCode, data);
        /* However, the Fragment API munges activityForResult invocations from fragments by bitshifting
         * the request code up two bytes. This method doesn't handle such request codes; they dispatch
         * by the Fragment API path.
         */
        JRSession session = JRSession.getInstance();
        if (session != null && session.getCurrentOpenIDAppAuthProvider() != null) {
            session.getCurrentOpenIDAppAuthProvider().onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (AndroidUtils.isCupcake() && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // Take care of calling this method on earlier versions of
            // the platform where it doesn't exist.
            onBackPressed();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // Unfortunately setResult is final so it can't be overridden to track the result-definedness state of
    // the activity, which is used to track unplanned #finish()es and to fire onBackPressed at that time.
    //@Override
    //public void setResult() { }
    /*package*/ void _setResult(int resultCode) {
        setResult(resultCode);
        m_Result = resultCode;
    }

    /**
     * @internal
     * Intercepts finish calls triggered by activitySetFinishOnTouchOutside by detecting whether a result has been
     * specified yet.
     */
    @Override
    public void finish() {
        if (m_Result == null) {
            onBackPressed();
        } else {
            super.finish();
        }
    }

    @Override
    public void onBackPressed() {
        LogUtils.logd(TAG, "[onBackPressed]");
        mUiFragment.onBackPressed();
    }

    public static class IllegalFragmentIdException extends RuntimeException {
        int mFragId;

        public IllegalFragmentIdException(int fragId) {
            mFragId = fragId;
        }

        public String toString() {
            return "Bad fragment ID: " + mFragId;
        }
    }

    public static Intent createIntentForCurrentScreen(Activity activity, boolean showTitleBar) {
        Intent intent;
        if (AndroidUtils.isSmallNormalOrLargeScreen()) {
            intent = new Intent(activity, Fullscreen.class);
            if (showTitleBar) {
                intent.putExtra(JR_OPERATION_MODE, JR_FULLSCREEN);
            } else {
                intent.putExtra(JR_OPERATION_MODE, JR_FULLSCREEN_NO_TITLE);
            }
        } else { // Honeycomb (because the screen is large+)
            intent = new Intent(activity, JRFragmentHostActivity.class);
            intent.putExtra(JR_OPERATION_MODE, JR_DIALOG);
        }
        return intent;
    }

    public static Intent createProviderListIntent(Activity activity) {
        Intent i = createIntentForCurrentScreen(activity, true);
        i.putExtra(JR_FRAGMENT_ID, JR_PROVIDER_LIST);
        return i;
    }

    public static Intent createUserLandingIntent(Activity activity) {
        Intent i = createIntentForCurrentScreen(activity, true);
        i.putExtra(JR_FRAGMENT_ID, JR_LANDING);
        return i;
    }

    public static Intent createWebViewIntent(Activity activity) {
        Intent i = createIntentForCurrentScreen(activity, false);
        i.putExtra(JR_FRAGMENT_ID, JR_WEBVIEW);
        return i;
    }

    public static Intent createOpenIDAppAuthIntent(Activity activity) {
        Intent i = createIntentForCurrentScreen(activity, false);
        i.putExtra(JR_FRAGMENT_ID, JR_OPENID_APPAUTH);
        return i;
    }


    /* ~aliases for alternative activity declarations for this activity */
    public static class Fullscreen extends JRFragmentHostActivity {}
}