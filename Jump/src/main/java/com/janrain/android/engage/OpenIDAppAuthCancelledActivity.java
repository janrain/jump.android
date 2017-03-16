package com.janrain.android.engage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.janrain.android.R;
import com.janrain.android.engage.session.JRSession;
import com.janrain.android.utils.LogUtils;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.RegistrationResponse;

import java.util.concurrent.Executors;

import static android.R.attr.data;


public class OpenIDAppAuthCancelledActivity extends Activity {

    private static final String TAG = "OpenIDAppAuthCancelledActivity";
    private static final String EXTRA_FAILED = "failed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra(EXTRA_FAILED, false)) {
            LogUtils.logd(TAG, "OpenID Authorization cancelled by user");
            final JRSession session = JRSession.getInstance();
            JROpenIDAppAuth.OpenIDAppAuthProvider mOpenIDProvider = session.getCurrentOpenIDAppAuthProvider();
            mOpenIDProvider.triggerOnFailure("OpenID Authorization cancelled by user", JROpenIDAppAuth.OpenIDAppAuthError.LOGIN_CANCELED);
            this.finish();
        }

    }

}
