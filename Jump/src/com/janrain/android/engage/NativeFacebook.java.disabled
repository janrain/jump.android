package com.janrain.android.engage;
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

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.utils.LogUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.janrain.android.engage.JRNativeAuth.NativeProvider;
import static com.janrain.android.engage.JRNativeAuth.NativeAuthError;

public class NativeFacebook extends NativeProvider {
    private static Class<?> fbSessionClass = null;
    private static Class fbCallbackClass = null;
    private static Class fbCanceledExceptionClass = null;

    private Object mFacebookCallback = null;

    static {
        ClassLoader classLoader = JRNativeAuth.class.getClassLoader();
        try {
            fbSessionClass = classLoader.loadClass("com.facebook.Session");
            fbCallbackClass = classLoader.loadClass("com.facebook.Session$StatusCallback");
            fbCanceledExceptionClass =
                    classLoader.loadClass("com.facebook.FacebookOperationCanceledException");
        } catch (ClassNotFoundException e) {
            LogUtils.logd("Could not load Native Facebook SDK: " + e);
        }
    }

    /*package*/ static boolean canHandleAuthentication() {
        return fbSessionClass != null && fbCallbackClass != null && fbCanceledExceptionClass != null;
    }

    /*package*/ NativeFacebook(FragmentActivity activity, JRNativeAuth.NativeAuthCallback callback) {
        super(activity, callback);
    }

    @Override
    public String provider() {
        return "facebook";
    }

    @Override
    public void startAuthentication()  {
        mFacebookCallback = getFacebookCallBack();
        NativeAuthError authError = NativeAuthError.CANNOT_INVOKE_FACEBOOK_OPEN_SESSION_METHODS;

        try {
            Method getActiveSession = fbSessionClass.getMethod("getActiveSession");
            Object session = getActiveSession.invoke(fbSessionClass);

            if (session != null && isFacebookSessionOpened(session)) {
                String accessToken = getFacebookAccessToken(session);
                getAuthInfoTokenForAccessToken(accessToken);
            } else {
                Method openActiveSession = fbSessionClass.getMethod("openActiveSession",
                        Activity.class, boolean.class, fbCallbackClass);

                openActiveSession.invoke(fbSessionClass, fromActivity, true, mFacebookCallback);
            }
        } catch (NoSuchMethodException e) {
            triggerOnFailure("Could not open Facebook Session", authError, e);
        } catch (InvocationTargetException e) {
            triggerOnFailure("Could not open Facebook Session", authError, e);
        } catch (IllegalAccessException e) {
            triggerOnFailure("Could not open Facebook Session", authError, e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent data) {
        if (fbSessionClass != null) {
            try {
                Method getActiveSession = fbSessionClass.getMethod("getActiveSession");
                Object session = getActiveSession.invoke(fbSessionClass);

                Method onActivityResult = fbSessionClass.getMethod(
                        "onActivityResult", Activity.class, int.class, int.class, Intent.class);

                onActivityResult.invoke(session, fromActivity, requestCode, responseCode, data);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    /*package*/ void triggerOnSuccess(JRDictionary payload) {
        removeFacebookCallback();
        super.triggerOnSuccess(payload);
    }

    @Override
    /*package*/ void triggerOnFailure(final String message, NativeAuthError errorCode, Exception exception,
                                      boolean shouldTryWebViewAuthentication) {
        removeFacebookCallback();
        super.triggerOnFailure(message, errorCode, exception, shouldTryWebViewAuthentication);
    }

    private void removeFacebookCallback() {
        if (fbSessionClass != null && mFacebookCallback != null) {
            try {
                Method getActiveSession = fbSessionClass.getMethod("getActiveSession");
                Object session = getActiveSession.invoke(fbSessionClass);

                Method removeCallback = fbSessionClass.getMethod("removeCallback", fbCallbackClass);
                removeCallback.invoke(session, mFacebookCallback);
                mFacebookCallback = null;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Object getFacebookCallBack() {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] objects) throws Throwable {
                if (method.getName().equals("call")) {
                    Object fbSession = objects[0],
                            sessionState = objects[1];
                    Exception exception = (Exception) objects[2];

                    if (isFacebookSessionOpened(fbSession)) {
                        String accessToken = getFacebookAccessToken(fbSession);
                        getAuthInfoTokenForAccessToken(accessToken);
                    } else if (isFacebookSessionClosed(fbSession)){
                        if (fbCanceledExceptionClass.isInstance(exception)) {
                            triggerOnFailure(
                                    "Facebook login canceled",
                                    NativeAuthError.LOGIN_CANCELED,
                                    exception);
                        } else {
                            triggerOnFailure(
                                    "Could not open Facebook Session",
                                    NativeAuthError.FACEBOOK_SESSION_IS_CLOSED,
                                    exception);
                        }
                    }
                } else if (method.getName().equals("equals")) {
                    return (proxy == objects[0]);
                }
                return null;
            }
        };
        return Proxy.newProxyInstance(
                fbCallbackClass.getClassLoader(), new Class[]{fbCallbackClass}, handler);
    }

    private boolean isFacebookSessionOpened(Object session) {
        return getBoolForFbSessionAndMethod(session, "isOpened");
    }

    private boolean isFacebookSessionClosed(Object session) {
        return getBoolForFbSessionAndMethod(session, "isClosed");
    }

    private boolean getBoolForFbSessionAndMethod(Object session, String methodName) {
        boolean out = false;

        try {
            Method isOpened = session.getClass().getMethod(methodName);
            Boolean result = (Boolean) isOpened.invoke(session.getClass().cast(session));
            out = result;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return out;
    }

    private String getFacebookAccessToken(Object session) {
        String out = null;

        try {
            Method getAccessToken = session.getClass().getMethod("getAccessToken");
            out = (String) getAccessToken.invoke(session.getClass().cast(session));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return out;
    }
}
