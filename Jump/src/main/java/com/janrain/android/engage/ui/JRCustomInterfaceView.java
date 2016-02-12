/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2012, Janrain, Inc.
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @brief The abstract superclass of classes which define custom View hierarchies
 *
 * Extend this class and implement onCreateView to return a custom View hierarchy.
 *
 * This class also supplies several utility methods for controlling the behavior of the UI of the Engage
 * library.
 */
public abstract class JRCustomInterfaceView extends JRCustomInterface {
    private static final String TAG = JRCustomInterfaceView.class.getSimpleName();
    private View mView;
    private JRUiFragment mUiFragment;
    private Context mContext;

    /*package*/ boolean mViewCreated = false;

    /**
     * Implement this method to return your custom view hierarchy for embedding in the Engage library UI.
     *
     * This method should have no side effects as it may be called more than once per activity.
     * @param context The Context
     * @param inflater A LayoutInflater which can be used to inflate your layout file
     * @param container The ViewGroup to which your view will be attached
     * @param savedInstanceState Any state saved
     * @return Your custom sign-in view
     */
    public abstract View onCreateView(Context context,
                             LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState);

    /**
     * Retrieves the Activity object this custom view is a member of
     * @return
     *  The Activity this custom view is a member of
     */
    public Activity getActivity() {
        return mUiFragment.getActivity();
    }

    /**
     * A utility method to retrieve a Resources reference from the Application Context
     * @return
     *  A resources reference from the Application Context
     */
    public Resources getResources() {
        return mContext.getResources();
    }

    /**
     * A utility method to fetch a resource string by id
     * @param id
     *  The id of the string to fetch
     * @return
     *  The fetched string
     */
    public final String getString(int id) {
        return getResources().getString(id);
    }

    /**
     * A utility method to fetch a resource string by id, formatted with arguments
     * @param id
     *  The id of the string to fetch
     * @param formatArgs
     *  The arguments used to format the string with
     * @return
     *  The fetched and formatted string
     */
    public final String getString(int id, Object... formatArgs) {
        return getResources().getString(id, formatArgs);
    }

    /**
     * Gets the created View hierarchy
     * @return the created View hierarchy
     */
    public final View getView() {
        return mView;
    }

    /**
     * Called when the activity displaying the custom view is resuming
     */
    public void onResume() {}

    /**
     * Called when the activity displaying the custom view is pausing
     */
    public void onPause() {}

    /**
     * Called when the activity displaying the custom view is saving its instance state
     * @param outState
     *  The Bundle to write this custom views saved state to
     */
    public void onSaveInstanceState(Bundle outState) {}

    /**
     * Called when the activity displaying the custom view is destroyed
     */
    public void onDestroy() {}

    /**
     * Invoke this utility method to close the provider list
     */
    public final void finishJrSignin() {
        if (mUiFragment instanceof JRProviderListFragment) {
            ((JRProviderListFragment) mUiFragment).finishJrSignin();
        } else {
            Log.e(TAG, "Can't call finishJrSignin from JRCustomUiViews not displayed in JRProviderList");
        }
    }

    /**
     * Utility method to show a progress indicator, convenient for usage during custom username/password
     * authentication. This is not the only way to display a progress dialog, it is also possible to
     * use getActivity to have the Activity object to use with the Android Dialog classes.
     *
     * @param cancelable
     *  Whether the progress dialog is cancelable by the user, for example by pressing the back button
     *
     * @param cancelListener
     *  The listener notified when the dialog is canceled (e.g. by the back button). Null for no listener
     */
    public void showProgressIndicator(boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        mUiFragment.showProgressDialogForCustomView(cancelable, cancelListener);
    }

    /**
     * Dismiss the progress dialog displayed by showProgressIndicator
     */
    public void dismissProgressIndicator() {
        mUiFragment.dismissProgressDialogForCustomView();
    }

    /**
     * @internal
     * @hide
     */
    /*package*/ final View doOnCreateView(JRUiFragment fragment,
                                          Context context,
                                          LayoutInflater inflater,
                                          ViewGroup container,
                                          Bundle savedInstanceState) {
        mContext = context;
        mUiFragment = fragment;
        mViewCreated = true;
        mView = onCreateView(context, inflater, container, savedInstanceState);
        return mView;
    }
}
