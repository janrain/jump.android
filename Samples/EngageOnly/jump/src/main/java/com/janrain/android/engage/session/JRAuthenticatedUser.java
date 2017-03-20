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
package com.janrain.android.engage.session;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.janrain.android.R;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.utils.AndroidUtils;
import com.janrain.android.utils.ThreadUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * @internal
 *
 * @class JRAuthenticatedUser
 **/
public class JRAuthenticatedUser implements Serializable {
    public static final String TAG = JRAuthenticatedUser.class.getSimpleName();
    public static final String KEY_DEVICE_TOKEN = "device_token";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_PREFERRED_USERNAME = "preferred_username";
    public static final String KEY_IDENTIFIER = "identifier";
    public static final String KEY_AUTH_INFO = "auth_info";
    public static final String KEY_PROFILE = "profile";
    public static final String KEY_DISPLAY_NAME = "displayName";

    private String mPhoto;
    private String mPreferredUsername;
    private String mDeviceToken;
    private String mProviderName;
    private String mIdentifier;
    private String mWelcomeMessage;
    private String mDisplayName;

    public JRAuthenticatedUser(JRDictionary mobileEndPointResponse,
                               String providerName,
                               String welcomeMessage) {
        mProviderName = providerName;
        mDeviceToken = mobileEndPointResponse.getAsString(KEY_DEVICE_TOKEN);
        mPhoto = mobileEndPointResponse.getAsString(KEY_PHOTO);
        mPreferredUsername = mobileEndPointResponse.getAsString(KEY_PREFERRED_USERNAME);
        mIdentifier = mobileEndPointResponse.getAsDictionary(KEY_AUTH_INFO).getAsDictionary(KEY_PROFILE)
                .getAsString(KEY_IDENTIFIER);
        mWelcomeMessage = welcomeMessage == null ?
                getApplicationContext().getResources().getString(R.string.jr_welcome_back_message)
                        + mPreferredUsername
                : welcomeMessage;
        mDisplayName = mobileEndPointResponse.getAsDictionary(KEY_AUTH_INFO).getAsDictionary(KEY_PROFILE)
                .getAsString(KEY_DISPLAY_NAME);
    }

    public String getPhoto() { /* (readonly) */
        return mPhoto;
    }

    public String getWelcomeMessage() {
        return mWelcomeMessage;
    }

    public String getPreferredUsername() {  /* (readonly) */
        return mPreferredUsername;
    }

    public String getDeviceToken() {        /* (readonly) */
        return mDeviceToken;
    }

    public String getProviderName() {       /* (readonly) */
        return mProviderName;
    }

    public String getIdentifier() {
        return mIdentifier;
    }
    
    public String getDisplayName() {
        return mDisplayName;
    }
    
    public static class ProfilePicMissingException extends Exception {}

    private String getCachedProfilePicKey() throws ProfilePicMissingException {
        if (TextUtils.isEmpty(mPhoto)) throw new ProfilePicMissingException();
        return AndroidUtils.urlEncode(mPhoto);
    }

    private Context getApplicationContext() {
        return JREngage.getApplicationContext();
    }

    @SuppressWarnings("unchecked")
    public void asyncDownloadProfilePic(final ProfilePicAvailableListener callback) {
        final Handler uiThread = new Handler();

        ThreadUtils.executeInBg(new Runnable() {
            public void run() {
                profilePicBackgroundThread(uiThread, callback);
            }
        });
    }

    private void profilePicBackgroundThread(final Handler uiThread,
                                            final ProfilePicAvailableListener callback) {
        FileInputStream fis = null;
        Bitmap cachedProfilePic = null;
        try {
            fis = getApplicationContext().openFileInput("userpic~" + getCachedProfilePicKey());
            cachedProfilePic = BitmapFactory.decodeStream(fis);
        } catch (ProfilePicMissingException e) {
            return;
        } catch (FileNotFoundException ignore) {
        } catch (UnsupportedOperationException ignore) {
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ignore) {}
        }

        if (cachedProfilePic != null) {
            final Bitmap cachedProfilePic_ = cachedProfilePic;
            uiThread.post(new Runnable() {
                public void run() {
                    callback.onProfilePicAvailable(cachedProfilePic_);
                }
            });
        } else if (!TextUtils.isEmpty(getPhoto())) {
            JRConnectionManager.createConnection(getPhoto(),
                    new JRConnectionManagerDelegate.SimpleJRConnectionManagerDelegate() {
                        @Override
                        public void connectionDidFinishLoading(HttpResponseHeaders headers,
                                                               byte[] payload,
                                                               String requestUrl,
                                                               Object tag) {
                            FileOutputStream fos = null;
                            try {
                                fos = getApplicationContext().openFileOutput("userpic~" +
                                        getCachedProfilePicKey(), Activity.MODE_PRIVATE);
                                fos.write(payload);
                                final Bitmap bitmap =
                                        BitmapFactory.decodeByteArray(payload, 0, payload.length);
                                if (bitmap != null) {
                                    uiThread.post(new Runnable() {
                                        public void run() {
                                            callback.onProfilePicAvailable(bitmap);
                                        }
                                    });
                                }
                            } catch (ProfilePicMissingException ignore) {
                                // Can't happen, would be caught above.
                            } catch (IOException e) {
                                Log.e(TAG, "profile pic image caching exception.", e);
                            } finally {
                                if (fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException ignore) {
                                    }
                                }
                            }
                        }
                    }, null, null, null, true);
        }
    }

    @SuppressWarnings("unchecked")
    public void deleteCachedProfilePic() {
        ThreadUtils.executeInBg(new Runnable() {
            public void run() {
                try {
                    getApplicationContext().deleteFile("userpic~" + getCachedProfilePicKey());
                } catch (ProfilePicMissingException ignore) {
                }
            }
        });
    }

    public interface ProfilePicAvailableListener {
        public void onProfilePicAvailable(Bitmap b);
    }
}