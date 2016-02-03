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
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.janrain.android.R;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.session.JRSession;
import com.janrain.android.utils.LogUtils;

import java.io.Serializable;
import java.util.HashMap;

import static com.janrain.android.R.string.jr_dialog_ok;
import static com.janrain.android.utils.AndroidUtils.actionBarSetDisplayHomeAsUpEnabled;
import static com.janrain.android.utils.AndroidUtils.activityGetActionBar;

/**
 * @internal
 */
public abstract class JRUiFragment extends Fragment {
    private static final String KEY_MANAGED_DIALOGS = "jr_managed_dialogs";
    private static final String KEY_DIALOG_ID = "jr_dialog_id";
    private static final String KEY_MANAGED_DIALOG_OPTIONS = "jr_dialog_options";
    private static final String KEY_DIALOG_PROGRESS_TEXT = "jr_progress_dialog_text";
    private static final String KEY_DIALOG_PROGRESS_CANCELABLE = "jr_progress_dialog_cancelable";
    protected static final String PARENT_FRAGMENT_EMBEDDED = "jr_parent_fragment_embedded";
    
    public static final String JR_FRAGMENT_FLOW_MODE = "jr_fragment_flow_mode";
    public static final int JR_FRAGMENT_FLOW_AUTH = 0;
    public static final int JR_FRAGMENT_FLOW_SHARING = 1;
    /**
     * deprecated
     */
    public static final int JR_FRAGMENT_FLOW_BETA_DIRECT_SHARE = 2;

    public static final int REQUEST_LANDING = 1;
    public static final int REQUEST_WEBVIEW = 2;
    public static final int REQUEST_NATIVE = 3;
    public static final int DIALOG_ABOUT = 1000;
    public static final int DIALOG_PROGRESS = 1001;
    public static final String JR_ACTIVITY_JSON = "JRActivityJson";

    private FinishReceiver mFinishReceiver;
    private HashMap<Integer, ManagedDialog> mManagedDialogs = new HashMap<Integer, ManagedDialog>();
    private JRCustomInterfaceConfiguration mCustomInterfaceConfiguration;
    private Integer mFragmentResult;

    /*package*/ JRSession mSession;
    /*package*/ final String TAG = getLogTag();
    /*package*/ String getLogTag() { return getClass().getSimpleName(); }

    /**
     * @internal
     *
     * @class FinishReceiver
     * Used to listen to "Finish" broadcast messages sent by JREngage.cancel*
     **/
    private class FinishReceiver extends BroadcastReceiver {
        private String TAG = JRUiFragment.this.TAG + "-" + FinishReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String target = intent.getStringExtra(JRFragmentHostActivity.EXTRA_FINISH_FRAGMENT_TARGET);

            if (JRUiFragment.this.getClass().toString().equals(target) ||
                    target.equals(JRFragmentHostActivity.FINISH_TARGET_ALL)) {
                if (!isEmbeddedMode()) tryToFinishFragment();
                LogUtils.logd(TAG, "[onReceive] handled");
            } else {
                LogUtils.logd(TAG, "[onReceive] ignored");
            }
        }
    }

    private static class ManagedDialog implements Serializable {
        int mId;
        transient Dialog mDialog;
        transient Bundle mOptions;
        boolean mShowing;
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);

        if (JRSession.getInstance() == null) {
            throw new IllegalStateException("You must call JREngage.initInstance before inflating " +
                    "JREngage fragments.");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mFinishReceiver == null) mFinishReceiver = new FinishReceiver();
        getActivity().registerReceiver(mFinishReceiver, JRFragmentHostActivity.FINISH_INTENT_FILTER);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.logd(TAG, "[onCreate]");

        mSession = JRSession.getInstance();
        if (mSession != null) mSession.setUiIsShowing(true);
        /* Embedded fragments aren't compatible with setRetainInstance, because the parent Activity may
         * not handle config changes */
        if (!isEmbeddedMode()) setRetainInstance(true);
        setHasOptionsMenu(true);

        if (getActivity() instanceof JRFragmentHostActivity) {
            if (getCustomTitle() != null) getActivity().setTitle(getCustomTitle());
            actionBarSetDisplayHomeAsUpEnabled(activityGetActionBar(getActivity()), true);
        }
    }

    @Override
    public abstract View onCreateView(LayoutInflater inflater,
                                      ViewGroup container,
                                      Bundle savedInstanceState);

    @Override
    @SuppressWarnings("unchecked")
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtils.logd(TAG, "[onActivityCreated]");

        mSession = JRSession.getInstance();

        if (savedInstanceState != null) {
            mManagedDialogs = (HashMap) savedInstanceState.get(KEY_MANAGED_DIALOGS);
            Parcelable[] p = savedInstanceState.getParcelableArray(KEY_MANAGED_DIALOG_OPTIONS);
            if (mManagedDialogs != null && p != null) {
                for (Parcelable p_ : p) {
                    Bundle b = (Bundle) p_;
                    mManagedDialogs.get(b.getInt(KEY_DIALOG_ID)).mOptions = b;
                }
            } else {
                mManagedDialogs = new HashMap<Integer, ManagedDialog>();
            }
        }

        for (ManagedDialog d : mManagedDialogs.values()) {
            d.mDialog = onCreateDialog(d.mId, d.mOptions);
            if (d.mShowing) d.mDialog.show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.logd(TAG, "[onResume]");
        maybeShowHideTaglines();
        if (mCustomInterfaceConfiguration != null) mCustomInterfaceConfiguration.onResume();
    }

    @Override
    public void onPause() {
        if (mCustomInterfaceConfiguration != null) mCustomInterfaceConfiguration.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        LogUtils.logd(TAG, "[onDestroyView]");

        for (ManagedDialog d : mManagedDialogs.values()) d.mDialog.dismiss();

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        LogUtils.logd(TAG, "[onDestroy]");
        if (mFragmentResult != null) {
            if (getActivity() instanceof JRFragmentHostActivity) {
                ((JRFragmentHostActivity) getActivity())._setResult(mFragmentResult);
            } else if (getTargetFragment() != null) {
                getTargetFragment().onActivityResult(getTargetRequestCode(), mFragmentResult, null);
            }
        }

        if (mSession != null) mSession.setUiIsShowing(false);

        if (mCustomInterfaceConfiguration != null) mCustomInterfaceConfiguration.onDestroy();

        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (mFinishReceiver != null) getActivity().unregisterReceiver(mFinishReceiver);

        super.onDetach();
    }

    /* May be called at any time before onDestroy() */
    @Override
    public void onSaveInstanceState(Bundle outState) {

        Bundle[] dialogOptions = new Bundle[mManagedDialogs.size()];
        int x = 0;
        for (ManagedDialog d : mManagedDialogs.values()) {
            d.mShowing = d.mDialog.isShowing();
            dialogOptions[x++] = d.mOptions;
            d.mOptions.putInt(KEY_DIALOG_ID, d.mId);
        }
        outState.putSerializable(KEY_MANAGED_DIALOGS, mManagedDialogs);
        outState.putParcelableArray(KEY_MANAGED_DIALOG_OPTIONS, dialogOptions);

        if (mCustomInterfaceConfiguration != null) {
            mCustomInterfaceConfiguration.onSaveInstanceState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }
    //--

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        //menu.add("test");

        if (mSession == null || mSession.getHidePoweredBy()) {
            Log.e(TAG, "Bailing out of onCreateOptionsMenu");
            return;
        }

        inflater.inflate(R.menu.jr_about_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.jr_menu_about) {
            showDialog(DIALOG_ABOUT);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*package*/ void onFragmentHostActivityCreate(JRFragmentHostActivity jrfh, JRSession session) {
        String uiCustomizationName =
                getArguments().getString(JRFragmentHostActivity.JR_UI_CUSTOMIZATION_CLASS);

        if (uiCustomizationName != null) {
            try {
                Class classRef = Class.forName(uiCustomizationName);
                final JRCustomInterface customInterface = (JRCustomInterface) classRef.newInstance();
                if (customInterface instanceof JRCustomInterfaceConfiguration) {
                    mCustomInterfaceConfiguration = (JRCustomInterfaceConfiguration) customInterface;
                    if (mCustomInterfaceConfiguration.mColorButtons != null) {
                        ColorButton.sEnabled = mCustomInterfaceConfiguration.mColorButtons;
                    }
                } else if (customInterface instanceof JRCustomInterfaceView) {
                    mCustomInterfaceConfiguration = new JRCustomInterfaceConfiguration() {
                        {
                            //Type safe because the type of the instantiated instance from the
                            //class ref is run time type checked
                            mProviderListHeader = (JRCustomInterfaceView) customInterface;
                        }
                    };
                } else {
                    Log.e(TAG, "Unexpected class from: " + uiCustomizationName);
                    //Not possible at the moment, there are only two subclasses of abstract JRCustomInterface
                }
            } catch (ClassNotFoundException e) {
                customSigninReflectionError(uiCustomizationName, e);
            } catch (ClassCastException e) {
                customSigninReflectionError(uiCustomizationName, e);
            } catch (java.lang.InstantiationException e) {
                customSigninReflectionError(uiCustomizationName, e);
            } catch (IllegalAccessException e) {
                customSigninReflectionError(uiCustomizationName, e);
            }
        }
    }

    /*package*/ void maybeShowHideTaglines() {
        if (mSession == null || getView() == null) {
            Log.e(TAG, "Bailing out of maybeShowHideTaglines: mSession: " + mSession + " getView(): "
                    + getView());
            return;
        }

        boolean hideTagline = mSession.getHidePoweredBy();
        int visibility = hideTagline ? View.GONE : View.VISIBLE;

        View tagline = getView().findViewById(R.id.jr_tagline);
        if (tagline != null) tagline.setVisibility(visibility);

        View bonusTagline = getView().findViewById(R.id.jr_email_sms_powered_by_text);
        if (bonusTagline != null) bonusTagline.setVisibility(visibility);
    }

    /*package*/ ProgressDialog createProgressDialog(Bundle options) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(options.getString(KEY_DIALOG_PROGRESS_TEXT));
        return progressDialog;
    }

    /*package*/ void showProgressDialog(String displayText) {
        showProgressDialog(displayText, true);
    }

    /*package*/ ManagedDialog showProgressDialog(String displayText, boolean cancelable) {
        Bundle opts = new Bundle();
        opts.putString(KEY_DIALOG_PROGRESS_TEXT, displayText);
        opts.putBoolean(KEY_DIALOG_PROGRESS_CANCELABLE, cancelable);
        return showDialog(DIALOG_PROGRESS, opts);
    }

    /*package*/ void showProgressDialog() {
        showProgressDialog(getString(R.string.jr_progress_loading));
    }

    /*package*/ void dismissProgressDialog() {
        dismissDialog(DIALOG_PROGRESS);
    }

    private AlertDialog createAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(jr_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog retval = builder.create();
        View l = retval.getWindow().getLayoutInflater().inflate(R.layout.jr_about_dialog, null);
        retval.setView(l);

        return retval;
    }

    /*package*/ final boolean isEmbeddedMode() {
        FragmentActivity a = getActivity();
        return a != null && !(a instanceof JRFragmentHostActivity);
    }

    /*package*/ Dialog onCreateDialog(int id, Bundle options) {
        Dialog dialog;
        switch (id) {
            case DIALOG_ABOUT:
                dialog = createAboutDialog();
                break;
            case DIALOG_PROGRESS:
                dialog = createProgressDialog(options);
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    /*package*/ void onPrepareDialog(int id, Dialog d, Bundle options) {
        switch (id) {
            case DIALOG_PROGRESS:
                d.setCancelable(options.getBoolean(KEY_DIALOG_PROGRESS_CANCELABLE, true));
                d.setOnCancelListener(null);
        }
    }

    /*package*/ void showDialog(int dialogId) {
        showDialog(dialogId, new Bundle());
    }

    /*package*/ ManagedDialog showDialog(int dialogId, Bundle options) {
        ManagedDialog d = mManagedDialogs.get(dialogId);
        if (d == null) {
            d = new ManagedDialog();
            d.mDialog = onCreateDialog(dialogId, options);
            d.mId = dialogId;
            mManagedDialogs.put(dialogId, d);
        }

        d.mOptions = options;
        onPrepareDialog(dialogId, d.mDialog, options);
        d.mDialog.show();
        //d.mShowing = true; // See also dismissDialog comment

        return d;
    }

    /*package*/ void dismissDialog(int dialogId) {
        ManagedDialog d = mManagedDialogs.get(dialogId);
        if (d != null) d.mDialog.dismiss();
    }

    private void startActivityForFragId(int fragId, int requestCode) {
        startActivityForFragId(fragId, requestCode, null);
    }

    /*package*/ int getColor(int colorId) {
        return getResources().getColor(colorId);
    }

    /*package*/ void startActivityForFragId(int fragId, int requestCode, Bundle opts) {
        boolean showTitle;
        switch (fragId) {
            case JRFragmentHostActivity.JR_LANDING:
                showTitle = true;
                break;
            case JRFragmentHostActivity.JR_WEBVIEW:
                showTitle = false;
                break;
            default: throw new JRFragmentHostActivity.IllegalFragmentIdException(fragId);
        }

        Intent i = JRFragmentHostActivity.createIntentForCurrentScreen(getActivity(), showTitle);
        i.putExtra(JRFragmentHostActivity.JR_FRAGMENT_ID, fragId);
        i.putExtra(JRUiFragment.PARENT_FRAGMENT_EMBEDDED, isEmbeddedMode());
        i.putExtra(JR_FRAGMENT_FLOW_MODE, getFragmentFlowMode());
        if (opts != null) i.putExtras(opts);
        if (isSpecificProviderFlow()) {
            i.putExtra(JRFragmentHostActivity.JR_PROVIDER, getSpecificProvider());
        }
        startActivityForResult(i, requestCode);
    }
    
    protected int getFragmentFlowMode() {
        return getArguments().getInt(JR_FRAGMENT_FLOW_MODE);
    }
    
    private void showFragment(Class<? extends JRUiFragment> fragClass, int requestCode) {
        JRUiFragment f;
        try {
            f = fragClass.newInstance();
        } catch (java.lang.InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        Bundle args = new Bundle();
        args.putInt(JR_FRAGMENT_FLOW_MODE, getFragmentFlowMode());
        f.setArguments(args);
        f.setTargetFragment(this, requestCode);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(((ViewGroup) getView().getParent()).getId(), f)
                .addToBackStack(fragClass.getSimpleName())
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .commit();
    }

    /*package*/ void setFragmentResult(int result) {
        mFragmentResult = result;
    }
    
    /*package*/ Integer getFragmentResult() {
        return mFragmentResult;
    }

    /*package*/ void finishFragmentWithResult(int result) {
        setFragmentResult(result);
        finishFragment();
    }

    /*package*/ void finishFragment() {
        FragmentActivity activity = getActivity();
        if (activity instanceof JRFragmentHostActivity) {
            if (mFragmentResult != null) ((JRFragmentHostActivity) activity)._setResult(mFragmentResult);
            activity.finish();
        } else if (null != activity) {
            FragmentManager fm = activity.getSupportFragmentManager();
            int bsec = fm.getBackStackEntryCount();
            if (bsec > 0 && fm.getBackStackEntryAt(bsec - 1).getName().equals(getLogTag())) {
                fm.popBackStack();
            } else if (bsec > 0) {
                Log.e(TAG, "Error trying to finish fragment not on top of back stack");
                fm.popBackStack(getLogTag(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else { // bsec == 0
                // Root fragment, if it's finishing it's because authentication finished?
                fm.beginTransaction()
                        .remove(this)
                        .setTransition(FragmentTransaction.TRANSIT_NONE)
                        .commit();
            }
        }
    }

    /*package*/ void startWebViewAuthForProvider(JRProvider provider) {
        if (provider.requiresInput() ||
                (mSession.getAuthenticatedUserForProvider(provider) != null &&
                        !provider.getForceReauthUrlFlag()) && !mSession.getAlwaysForceReauth()) {
            showUserLanding();
        } else {
            showWebView();
        }
    }


    /*package*/ void showUserLanding() {
        if (getActivity() instanceof JRFragmentHostActivity || isSharingFlow()) {
            startActivityForFragId(JRFragmentHostActivity.JR_LANDING, REQUEST_LANDING);
        } else {
            showFragment(JRLandingFragment.class, REQUEST_LANDING);
        }
    }

    /*package*/ void showWebView(boolean forSharingFlow) {
        if (getActivity() instanceof JRFragmentHostActivity || forSharingFlow) {
            Bundle opts = new Bundle();
            opts.putInt(JR_FRAGMENT_FLOW_MODE,
                    forSharingFlow ? JR_FRAGMENT_FLOW_SHARING : JR_FRAGMENT_FLOW_AUTH);
            startActivityForFragId(JRFragmentHostActivity.JR_WEBVIEW, REQUEST_WEBVIEW, opts);
        } else {
            showFragment(JRWebViewFragment.class, REQUEST_WEBVIEW);
        }
    }

    /*package*/ void showWebView() {
        showWebView(false);
    }

    /*package*/ boolean isSharingFlow() {
        return getArguments().getInt(JR_FRAGMENT_FLOW_MODE) == JR_FRAGMENT_FLOW_SHARING;
    }

    /*package*/ void tryToFinishFragment() {
        LogUtils.logd(TAG, "[tryToFinishFragment]");
        finishFragment();
    }

    /*package*/ boolean hasView() {
        return getView() != null;
    }

    /*package*/ boolean isSpecificProviderFlow() {
        return getArguments().getString(JRFragmentHostActivity.JR_PROVIDER) != null;
    }

    /*package*/ String getSpecificProvider() {
        return getArguments().getString(JRFragmentHostActivity.JR_PROVIDER);
    }

    /**
     * @internal
     * Delegated to from JRFragmentHostActivity
     */
    /*package*/ void onBackPressed() {}

    private void customSigninReflectionError(String customName, Exception e) {
        Log.e(TAG, "Can't load custom signin class: " + customName + "\n", e);
    }

    /*package*/ JRCustomInterfaceConfiguration getCustomUiConfiguration() {
        return mCustomInterfaceConfiguration;
    }

    /*package*/ void doCustomViewCreate(JRCustomInterfaceView view,
                                    LayoutInflater inflater,
                                    Bundle savedInstanceState,
                                    ViewGroup container) {
        view.doOnCreateView(this,
                getActivity().getApplicationContext(),
                inflater,
                container,
                savedInstanceState);
    }

    public void showProgressDialogForCustomView(boolean cancelable,
                                                DialogInterface.OnCancelListener cancelListener) {
        ManagedDialog md = showProgressDialog(getString(R.string.jr_progress_loading), cancelable);
        md.mDialog.setOnCancelListener(cancelListener);
    }

    public void dismissProgressDialogForCustomView() {
        dismissProgressDialog();
    }

    /*package*/ String getCustomTitle() {
        return null;
    }

    /*package*/ boolean shouldShowTitleWhenDialog() {
        return false;
    }
}
