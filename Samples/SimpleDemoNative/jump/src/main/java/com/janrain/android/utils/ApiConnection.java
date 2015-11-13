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

import android.util.Pair;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static android.text.TextUtils.join;
import static com.janrain.android.utils.AndroidUtils.urlEncode;
import static com.janrain.android.utils.LogUtils.throwDebugException;

public class ApiConnection {
    private final String url;
    private  Set<Pair<String,String>> params = new HashSet<Pair<String, String>>();
    public Method method = Method.POST;
    private JRConnectionManagerDelegate connectionManagerDelegate;

    public ApiConnection(String url) {
        this.url = url;
    }

    public static byte[] paramsGetBytes(Set<Pair<String, String>> bodyParams) {
        try {
            return paramsToString(bodyParams).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected", e);
        }
    }

    public static String paramsToString(Set<Pair<String, String>> bodyParams) {
        Collection<String> paramPairs = CollectionUtils.map(bodyParams,
                new CollectionUtils.Function<String, Pair<String, String>>() {
                    public String operate(Pair<String, String> val) {
                        return val.first.concat("=").concat(urlEncode(val.second));
                    }
                });

        return join("&", paramPairs);
    }

    public static Object connectionManagerGetJsonContent(HttpResponseHeaders headers, byte[] payload) {
        String json = null;
        try {
            json = new String(payload, "UTF-8");
            if (headers.getContentType().toLowerCase().startsWith("application/json")) {
                return new JSONTokener(json).nextValue();
            }
            LogUtils.logd("unrecognized content type: " + headers.getContentType());
            LogUtils.logd(json);
            return json;
        } catch (NullPointerException ignore) {
            return json;
        } catch (JSONException ignore) {
            return json;
        } catch (UnsupportedEncodingException e) {
            throwDebugException(new RuntimeException(e));
            return json;
        }
    }

    public void stopConnection() {
        JRConnectionManager.stopConnectionsForDelegate(connectionManagerDelegate);
    }

    public void maybeAddParam(String key, String value) {
        if (key != null && value != null) params.add(new Pair<String, String>(key, value));
    }

    public void addAllToParams(String... params) {
        for (int i = 0; i < params.length - 1; i += 2) {
            String key = params[i];
            String value = params[i + 1];
            if (value != null) {
                this.params.add(new Pair<String, String>(key, value));
            } else {
                throwDebugException(new RuntimeException("null value in params"));
            }
        }

        if (params.length % 2 == 1) LogUtils.loge("error: odd number of param strings");
    }

    public void addAllToParams(Set<Pair<String, String>> params) {
        this.params.addAll(params);
    }

    public void fetchResponseMaybeJson(final FetchCallback callback) {
        JRConnectionManagerDelegate.SimpleJRConnectionManagerDelegate connectionCallback =
                new JRConnectionManagerDelegate.SimpleJRConnectionManagerDelegate() {
                    @Override
                    public void connectionDidFinishLoading(HttpResponseHeaders headers,
                                                           byte[] payload,
                                                           String requestUrl,
                                                           Object tag) {
                        Object response = connectionManagerGetJsonContent(headers, payload);
                        callback.run(response);
                    }

                    @Override
                    public void connectionDidFail(Exception ex,
                                                  HttpResponseHeaders responseHeaders,
                                                  byte[] payload,
                                                  String requestUrl,
                                                  Object tag) {
                        int responseCode = responseHeaders == null ? -1 : responseHeaders.getResponseCode();
                        LogUtils.loge("failed request (" + responseCode + "): " + requestUrl, ex);
                        Object response = connectionManagerGetJsonContent(responseHeaders, payload);
                        callback.run(response);
                    }
                };

        if (method == Method.POST) {
            byte[] postData = paramsGetBytes(params);
            JRConnectionManager.createConnection(url, connectionCallback, null, null, postData, false);
        } else {
            String urlWithParams = url + "?" + paramsToString(params);
            JRConnectionManager.createConnection(urlWithParams, connectionCallback, null, null, null, false);
        }

        connectionManagerDelegate = connectionCallback;
    }

    public void fetchResponseAsJson(final FetchJsonCallback callback) {
        fetchResponseMaybeJson(new FetchCallback() {
            public void run(Object response) {
                if (response instanceof JSONObject) {
                    callback.run(((JSONObject) response));
                } else {
                    LogUtils.loge("bad response: " + response);
                    callback.run(null);
                }
            }
        });
    }

    public enum Method {POST, GET}

    public interface FetchJsonCallback {
        void run(JSONObject jsonObject);
    }

    public interface FetchCallback {
        void run(Object response);
    }

    public void addCustomUserAgent(String customUserAgent) {
         JRConnectionManager.setCustomUserAgent(customUserAgent);
    }

}
