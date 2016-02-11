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
import com.janrain.android.Jump;
import com.janrain.android.utils.JsonUtils;
import com.janrain.android.utils.LogUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Set;

import static com.janrain.android.capture.Capture.CaptureApiRequestCallback;
import static com.janrain.android.capture.CaptureStringUtils.readAndClose;

public class CaptureDebugUtils {
    private static JSONObject getEntity(int id) throws IOException, JSONException {
        //URLConnection entityConn = new URL("https://" + CAPTURE_DOMAIN + "/entity?" +
        //        "type_name=" + ENTITY_TYPE_NAME +
        //        "&client_id=" + CLIENT_ID +
        //        "&client_secret=" + CLIENT_SECRET +
        //        "&id=" + id).openConnection();
        URLConnection entityConn = new URL("https://" + Jump.getCaptureDomain() + "/entity?" +
                "access_token=6vxjc6xg88g2q5ht").openConnection();
        entityConn.connect();

        String response = readAndClose(entityConn.getInputStream());

        JSONObject jo = new JSONObject(new JSONTokener(response));
        if ("ok".equals(jo.optString("stat"))) {
            //CaptureStringUtils.log("response: " + response);
            return jo.getJSONObject("result");
        } else {
            throw new IOException("failed to get entity, bad JSON response: " + jo);
        }
    }

    public static void writePostParams(URLConnection connection, Set<Pair<String, String>> params)
            throws IOException {
        connection.getOutputStream().write(CaptureApiConnection.paramsGetBytes(params));
    }

    public static void main(String[] args) throws IOException, JSONException {
        //JRCaptureEntity.inflate(getEntity(159)); // for entity type "user"
        CaptureRecord record = new CaptureRecord(getEntity(14474), null, null);
        LogUtils.logd("Capture", record.toString(2));

        deeplyRandomizeArrayElementOrder(record);
        record.put("email", "nathan+androidtest2@janrain.com");
        ((JSONArray) record.opt("pinapinapL1Plural")).put(new JSONObject("{\"string1\":\"poit\"}"));
        ((JSONObject) ((JSONObject) record.opt("oinoL1Object")).opt("oinoL2Object")).put("string1", "zot");
        ((JSONObject) ((JSONObject) record.opt("oinoL1Object")).opt("oinoL2Object")).put("string2", "narf");

        record.refreshAccessToken(null);

        try {
            record.synchronize(new CaptureApiRequestCallback() {
                public void onSuccess() {
                    LogUtils.logd("Capture", "success");
                }

                public void onFailure(CaptureApiError e) {
                    LogUtils.logd("Capture", ("failure: " + e));
                }
            },null);
        } catch (Capture.InvalidApidChangeException e) {
            e.printStackTrace();
        }
    }

    public static Object urlConnectionGetJsonContent(URLConnection uConn) {
        String json = null;
        try {
            if (uConn.getContentType().toLowerCase().equals("application/json")) {
                json = CaptureStringUtils.readAndClose(uConn.getInputStream());
                return new JSONTokener(json).nextValue();
            }
            LogUtils.logd("content type: " + uConn.getContentType());
            LogUtils.logd(CaptureStringUtils.readAndClose(uConn.getInputStream()));
            return null;
        } catch (IOException e) {
            LogUtils.logd(e.toString());
            return null;
        } catch (JSONException ignore) {
            return json;
        }
    }

    public static void deepArraySort(JSONArray original) {
        for (int i=0; i < original.length(); i++) {
            try {
                Object val = original.get(i);
                if (val instanceof  JSONObject) {
                    deepArraySort(((JSONObject) val));
                } else if (val instanceof JSONArray) {
                    deepArraySort(((JSONArray) val));
                }
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }

        try {
            mergeSort(original, 0, original.length());
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected", e);
        }
    }

    private static void mergeSort(JSONArray original, int start, int len) throws JSONException {
        if (len <= 1) return;

        int halfLen = len / 2;
        int left = start;
        int right = start + halfLen;

        mergeSort(original, left, halfLen);
        mergeSort(original, right, len - halfLen);

        Object[] temp = new Object[len];
        int tempIndex = 0;

        while (left < start + halfLen || right < start + len) {
            Object leftVal = original.opt(left);
            Object rightVal = original.opt(right);
            if (left < start + halfLen && right < start + len) {
                if (JsonUtils.compareJsonVals(leftVal, rightVal) < 0) {
                    temp[tempIndex++] = leftVal;
                    left++;
                } else  {
                    temp[tempIndex++] = rightVal;
                    right++;
                }
            } else if (left < start + halfLen) {
                temp[tempIndex++] = leftVal;
                left++;
            } else {
                temp[tempIndex++] = rightVal;
                right++;
            }
        }

        for (int i=0; i < temp.length; i++) original.put(start + i, temp[i]);
    }

    public static void deepArraySort(JSONObject original) {
        Iterator<String> keys = original.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                Object val = original.get(key);
                if (val instanceof JSONArray) {
                    deepArraySort(((JSONArray) val));
                } else if (val instanceof JSONObject) {
                    deepArraySort(((JSONObject) val));
                }
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }
    }

    public static void deeplyRandomizeArrayElementOrder(JSONArray original) {
        for (int i=0; i < original.length(); i++) {
            try {
                Object val = original.get(i);
                if (val instanceof  JSONObject) {
                    deeplyRandomizeArrayElementOrder(((JSONObject) val));
                } else if (val instanceof JSONArray) {
                    deeplyRandomizeArrayElementOrder(((JSONArray) val));
                }
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }

        for (int i = 0; i < original.length(); i++) {
            int j = ((int) (Math.random() * original.length())) % original.length();
            try {
                Object iVal = original.get(i);
                original.put(i, original.get(j));
                original.put(j, iVal);
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }
    }

    public static void deeplyRandomizeArrayElementOrder(JSONObject original) {
        Iterator<String> keys = original.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                Object val = original.get(key);
                if (val instanceof JSONArray) {
                    deeplyRandomizeArrayElementOrder(((JSONArray) val));
                } else if (val instanceof JSONObject) {
                    deeplyRandomizeArrayElementOrder(((JSONObject) val));
                }
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }
    }
}
