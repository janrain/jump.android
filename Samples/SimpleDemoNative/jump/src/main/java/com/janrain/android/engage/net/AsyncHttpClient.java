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

 /*
  * The class InflatingEntity and the method setupGoogleInflater are:
  * Copyright 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package com.janrain.android.engage.net;

import android.os.Handler;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.utils.IoUtils;
import com.janrain.android.utils.LogUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.apache.OkApacheClient;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import static com.janrain.android.engage.net.JRConnectionManager.ManagedConnection;

/**
 * @internal
 *
 * @class AsyncHttpClient
 */
/*package*/ class AsyncHttpClient {
    private static String USER_AGENT;
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";

    private AsyncHttpClient() {}

    /*package*/ static class HttpExecutor implements Runnable {
        private static final HttpClient mHttpClient = setupHttpClient();
        private final Handler mHandler;
        private final ManagedConnection mConn;
        private final JRConnectionManager.HttpCallback callBack;

        /*package*/ HttpExecutor(Handler handler, ManagedConnection managedConnection) {
            mConn = managedConnection;
            mHandler = handler;
            callBack = new JRConnectionManager.HttpCallback(mConn);
        }

        static private HttpClient setupHttpClient() {
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(30, TimeUnit.SECONDS);
            client.setFollowRedirects(false);
            OkApacheClient apacheClient = new OkApacheClient(client);

            return apacheClient;
        }

        public void run() {
            try {
                HttpUriRequest request = mConn.getHttpRequest();
                InetAddress ia = InetAddress.getByName(request.getURI().getHost());
                LogUtils.logd("Requesting: " + mConn.getRequestUrl() + " (" + ia.getHostAddress() + ")");

                if (USER_AGENT != null) {
                    request.addHeader("User-Agent", USER_AGENT);
                }
                for (NameValuePair header : mConn.getRequestHeaders()) {
                    request.addHeader(header.getName(), header.getValue());
                }

                LogUtils.logd("Headers: " + Arrays.asList(mConn.getHttpRequest().getAllHeaders()).toString());
                if (mConn.getHttpRequest() instanceof HttpPost) {
                    String postBody = EntityUtils.toString(((HttpPost) mConn.getHttpRequest()).getEntity());
                    LogUtils.logd("POST to " + mConn.getRequestUrl() + ": " + postBody);
                }

                HttpResponse response;
                try {
                    response = mHttpClient.execute(request);
                } catch (IOException e) {
                    // XXX Mediocre way to match exceptions from aborted requests:
                    if (request.isAborted() && e.getMessage().contains("abort")) {
                        throw new AbortedRequestException();
                    } else {
                        throw e;
                    }
                }

                if (request.isAborted()) throw new AbortedRequestException();

                // Fetching the status code allows the response interceptor to have a chance to un-gzip the
                // entity before we fetch it.
                response.getStatusLine().getStatusCode();

                HttpResponseHeaders headers = HttpResponseHeaders.fromResponse(response, request);

                HttpEntity entity = response.getEntity();
                byte[] responseBody;
                if (entity == null) {
                    responseBody = new byte[0];
                } else {
                    responseBody = EntityUtils.toByteArray(entity);
                    entity.consumeContent();

                    final Header encoding = entity.getContentEncoding();
                    if (encoding != null) {
                        for (HeaderElement element : encoding.getElements()) {
                            if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                                GZIPInputStream gis =
                                        new GZIPInputStream(new ByteArrayInputStream(responseBody));
                                responseBody = IoUtils.readAndClose(gis, true);
                                break;
                            }
                        }
                    }
                }

                AsyncHttpResponse ahr;
                String statusLine = response.getStatusLine().toString();
                String bodyStr = new String(responseBody);
                int bodySubStrLen = bodyStr.length() > 300 ? 300 : bodyStr.length();

                switch (response.getStatusLine().getStatusCode()) {
                    case HttpStatus.SC_OK:
                        // Normal success
                    case HttpStatus.SC_NOT_MODIFIED:
                        // From mobile_config_and_baseurl called with an Etag
                    case HttpStatus.SC_MOVED_PERMANENTLY:
                    case HttpStatus.SC_SEE_OTHER:
                    case HttpStatus.SC_TEMPORARY_REDIRECT:
                    case HttpStatus.SC_MOVED_TEMPORARILY:
                        // for UPS-1390 - don't error on 302s from token URL
                    case HttpStatus.SC_CREATED:
                        // Response from the Engage trail creation and maybe URL shortening calls
                        LogUtils.logd(statusLine + ": " + bodyStr.substring(0, bodySubStrLen));
                        ahr = new AsyncHttpResponse(mConn, null, headers, responseBody);
                        break;
                    default:
                        LogUtils.loge(statusLine + "\n" + bodyStr.substring(0, bodySubStrLen));
                        ahr = new AsyncHttpResponse(mConn, new Exception(statusLine), headers, responseBody);
                }

                mConn.setResponse(ahr);
                invokeCallback(callBack);
            } catch (IOException e) {
                LogUtils.loge(this.toString());
                LogUtils.loge("IOException while executing HTTP request.", e);
                mConn.setResponse(new AsyncHttpResponse(mConn, e, null, null));
                invokeCallback(callBack);
            } catch (AbortedRequestException e) {
                LogUtils.loge("Aborted request: " + mConn.getRequestUrl());
                mConn.setResponse(new AsyncHttpResponse(mConn, null, null, null));
                invokeCallback(callBack);
            }
        }

        private void invokeCallback(JRConnectionManager.HttpCallback callBack) {
            if (mHandler != null) {
                mHandler.post(callBack);
            } else {
                callBack.run();
            }
        }

        public String toString() {
            String postData = mConn.getPostData() == null ? "null" : new String(mConn.getPostData());
            return "url: " + mConn.getRequestUrl() + "\nheaders: " + mConn.getRequestHeaders()
                    + "\npostData: " + postData;
        }

        private static class AbortedRequestException extends Exception {}
    }

    /**
     * @internal
     *
     * @class AsyncHttpResponseHolder
     * Wraps the possible data returned from a full asynchronous HTTP request.  This object will
     * contain either headers and data (if successful) or an Exception object (if failed).
     */
    /*package*/ static class AsyncHttpResponse {
        final private HttpResponseHeaders mHeaders;
        final private byte[] mPayload;
        final private Exception mException;
        final private ManagedConnection mManagedConnection;

        private AsyncHttpResponse(ManagedConnection conn,
                                  Exception exception,
                                  HttpResponseHeaders headers,
                                  byte[] payload) {
            mManagedConnection = conn;
            mException = exception;
            mHeaders = headers;
            mPayload = payload;
        }

        /**
         * Gets the URL that this HTTP response (or error) corresponds to.
         *
         * @return
         *      The URL that this HTTP response (or error) corresponds to.
         */
        /*package*/ String getUrl() {
            return mManagedConnection.getRequestUrl();
        }

        /**
         * Gets the headers object.  If the operation failed, it will return null.
         *
         * @return
         *         The HttpResponseHeaders object synthesized from the HTTP response if
         *         the operation was successful, null otherwise.
         */
        /*package*/ HttpResponseHeaders getHeaders() {
            return mHeaders;
        }

        /**
         * Gets the payload array.  If the operation failed, it will return null.
         *
         * @return
         *         The byte array containing the data (payload) from the HTTP response if
         *         the operation was successful, null otherwise.
         */
        /*package*/ byte[] getPayload() {
            return mPayload;
        }

        /**
         * Gets the exception object.  If the operation succeeded, it will return null.
         *
         * @return
         *         The Exception that occurred as a result of the asynchronous HTTP request if
         *         the operation failed, null otherwise.
         */
        /*package*/ Exception getException() {
            return mException;
        }

        /**
         * Checks to see if the holder contains a valid (non-null) headers object.
         *
         * @return
         *         <code>true</code> if the headers object is valid, <code>false</code> otherwise.
         */
        /*package*/ boolean hasHeaders() {
            return (mHeaders != null);
        }

        /**
         * Checks to see if the holder contains a valid (non-null) payload array.
         *
         * @return
         *         <code>true</code> if the payload array is valid, <code>false</code> otherwise.
         */
        /*package*/ boolean hasPayload() {
            return (mPayload != null);
        }

        /**
         * Checks to see if the holder contains a valid (non-null) exception object.
         *
         * @return
         *         <code>true</code> if the exception object is valid, <code>false</code> otherwise.
         */
        /*package*/ boolean hasException() {
            return (mException != null);
        }
    }

    public static void setCustomUserAgent(String customUserAgent) {
        USER_AGENT = customUserAgent;
    }
}