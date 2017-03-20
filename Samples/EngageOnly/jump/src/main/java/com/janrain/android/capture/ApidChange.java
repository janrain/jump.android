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

package com.janrain.android.capture;

import android.util.Pair;

import java.util.Set;
import java.util.regex.Pattern;

/*package*/ abstract class ApidChange {
    /*package*/ String attrPath;
    /*package*/ Object newVal;

    /*package*/ String findClosestParentSubentity() {
        int n = attrPath.lastIndexOf("#");
        if (n == -1) return "/";
        String number = Pattern.compile("#([0-9])*").matcher(attrPath.substring(n)).group();
        return attrPath.substring(0, n) + number;
    }

    @Override
    public String toString() {
        return "<" + getClass().getSimpleName() + " attrPath: " + attrPath + " newVal: " + newVal + ">";
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this instanceof ApidUpdate) return super.equals(obj);
        return toString().equals(obj.toString()) && obj instanceof ApidChange;
    }

    /*package*/ abstract String getUrlFor();

    ///*package*/ void writeConnectionBody(URLConnection urlConnection, String accessToken) throws IOException {
    //    Set<Pair<String, String>> params = getBodyParams();
    //    params.add(new Pair<String, String>("access_token", accessToken));
    //
    //    Capture.writePostParams(urlConnection, params);
    //}

    /*package*/ abstract Set<Pair<String, String>> getBodyParams();
}
