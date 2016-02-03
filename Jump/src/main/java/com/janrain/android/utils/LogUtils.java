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

package com.janrain.android.utils;

import android.util.Log;
import com.janrain.android.engage.JREngage;

public class LogUtils {
    private LogUtils() {}

    public static void logd(String tag, String msg, Throwable tr) {
        if (JREngage.sLoggingEnabled == null || JREngage.sLoggingEnabled) Log.d(tag, msg, tr);
    }

    public static void logd(String tag, String msg) {
        if (JREngage.sLoggingEnabled == null || JREngage.sLoggingEnabled) Log.d(tag, msg);
    }

    private static void logd(String msg, Throwable t, int stackDepth) {
        if (!(JREngage.sLoggingEnabled == null || JREngage.sLoggingEnabled)) return;
        if (t != null) {
            Log.d("[" + getLogTag(stackDepth) + "]", msg, t);
        } else {
            Log.d("[" + getLogTag(stackDepth) + "]", msg);
        }
    }

    public static void logd(String msg, Throwable t) {
        logd(msg, t, 2);
    }

    public static void logd(String msg) {
        logd(msg, null, 2);
    }

    public static void logd() {
        logd("", null, 2);
    }

    public static void loge(String msg) {
        loge(msg, null, 2);
    }

    public static void loge(String msg, Throwable t) {
        loge(msg, t, 2);
    }

    private static void loge(String msg, Throwable t, int stackDepth) {
        if (t != null) {
            Log.e("[" + getLogTag(stackDepth) + "]", msg, t);
        } else {
            Log.e("[" + getLogTag(stackDepth) + "]", msg);
        }
    }

    private static String getLogTag(int stackDepth) {
        String method;
        try {
            throw new Exception();
        } catch (Exception e) {
            e.fillInStackTrace();
            StackTraceElement stackTraceElement = e.getStackTrace()[stackDepth + 1];
            method = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + ":"
                    + stackTraceElement.getLineNumber();
        }
        return method;
    }

    public static void throwDebugException(RuntimeException debugException) {
        if (AndroidUtils.isApplicationDebuggable(null)) {
            throw debugException;
        } else {
            LogUtils.loge("Unexpected exception", debugException);
        }
    }
}
