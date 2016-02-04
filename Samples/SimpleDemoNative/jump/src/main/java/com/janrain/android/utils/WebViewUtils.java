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

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.util.Collection;

public class WebViewUtils {
    public static void deleteWebViewCookiesForDomains(Context context, Collection<String> domains) {
        for (String d : domains) {
            deleteWebViewCookiesForDomain(context, d, false);
            deleteWebViewCookiesForDomain(context, d, true);
        }
    }

    private static void deleteWebViewCookiesForDomain(Context context, String domain, boolean secure) {
        CookieSyncManager csm = CookieSyncManager.createInstance(context);
        CookieManager cm = CookieManager.getInstance();

        /* http://code.google.com/p/android/issues/detail?id=19294 */
        if (AndroidUtils.SDK_INT >= 11) {
            // don't trim leading '.'s
        } else {
            /* Trim leading '.'s */
            if (domain.startsWith(".")) domain = domain.substring(1);
        }

        /* Cookies are stored by domain, and are not different for different schemes (i.e. http vs
         * https) (although they do have an optional 'secure' flag.) */
        domain = "http" + (secure ? "s" : "") + "://" + domain;
        String cookieGlob = cm.getCookie(domain);
        if (cookieGlob != null) {
            String[] cookies = cookieGlob.split(";");
            for (String cookieTuple : cookies) {
                String[] cookieParts = cookieTuple.split("=");

                /* setCookie has changed a lot between different versions of Android with respect to
                 * how it handles cookies like these, which are set in order to clear an existing
                 * cookie.  This way of invoking it seems to work on all versions. */
                cm.setCookie(domain, cookieParts[0] + "=;");

                /* These calls have worked for some subset of the the set of all versions of
                 * Android:
                 * cm.setCookie(domain, cookieParts[0] + "=");
                 * cm.setCookie(domain, cookieParts[0]); */
            }
            csm.sync();
        }
    }
}
