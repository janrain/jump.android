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
package com.janrain.android.engage.net;

import android.os.Handler;
import android.os.Looper;
import com.janrain.android.utils.ApacheSetFromMap;
import com.janrain.android.utils.ThreadUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.ByteArrayEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @internal
 *
 * @class JRConnectionManager
 **/
public class JRConnectionManager {
    private static JRConnectionManager sInstance;

    // createConnection can be called from a BG thread so this is wrapped in a synced map for thread
    // safety
    private static final Map<JRConnectionManagerDelegate, Set<ManagedConnection>> sDelegateConnections =
            Collections.synchronizedMap(
                    new WeakHashMap<JRConnectionManagerDelegate, Set<ManagedConnection>>());

    private JRConnectionManager() {}

    public static synchronized JRConnectionManager getInstance() {
        if (sInstance == null) sInstance = new JRConnectionManager();
        return sInstance;
    }

    /**
     * Creates a managed HTTP connection. If called from the UI thread (or any thread with a Looper) then
     * all IO is performed on a background thread, callbacks are posted back to the Looper thread. Otherwise
     * IO and callbacks are performed synchronously.
     *
     * @param requestUrl
     *      The URL to be executed. May not be null.
     * @param delegate
     *      The delegate (listener) class instance. May be null. Callback methods will be invoked on the UI
     *      thread.
     * @param tag
     *      Optional tag for the connection, later passed to the delegate for the purpose of distinguishing
     *      multiple connections handled by a single delegate.
     * @param requestHeaders extra custom HTTP headers
     * @param postData if non-null will perform a POST, if null a GET
     * @param followRedirects true to follow HTTP 302 redirects, necessary for Facebook profile pics
     */
    public static void createConnection(String requestUrl,
                                        JRConnectionManagerDelegate delegate,
                                        Object tag,
                                        List<NameValuePair> requestHeaders,
                                        byte[] postData,
                                        boolean followRedirects) {
        if (requestHeaders == null) requestHeaders = new ArrayList<NameValuePair>();

        ManagedConnection managedConnection =
                new ManagedConnection(delegate, tag, requestUrl, postData, requestHeaders, followRedirects);

        trackAndStartConnection(delegate, managedConnection);
    }

    private static void trackAndStartConnection(JRConnectionManagerDelegate delegate,
                                                ManagedConnection managedConnection) {
        HttpUriRequest request;
        if (managedConnection.mPostData != null) {
            request = new HttpPost(managedConnection.mRequestUrl);
            ((HttpPost) request).setEntity(new ByteArrayEntity(managedConnection.mPostData));
            request.addHeader("Content-Type", "application/x-www-form-urlencoded");
            request.addHeader("Content-Language", "en-US");
        } else {
            request = new HttpGet(managedConnection.mRequestUrl);
        }

        managedConnection.mHttpRequest = request;

        if (managedConnection.getFollowRedirects()) {
            HttpClientParams.setRedirecting(request.getParams(), true);
        }

        synchronized (sDelegateConnections) {
            Set<ManagedConnection> connections = sDelegateConnections.get(delegate);
            if (connections == null) {
                connections = new ApacheSetFromMap<ManagedConnection>(
                        new WeakHashMap<ManagedConnection, Boolean>());
                sDelegateConnections.put(delegate, connections);
            }
            connections.add(managedConnection);
        }

        if (Looper.myLooper() != null) {
            // if we're on a Looper thread then operate asynchronously, and post a message back to the Looper
            // later
            ThreadUtils.executeInBg(new AsyncHttpClient.HttpExecutor(new Handler(), managedConnection));
        } else {
            // no Looper -> operate synchronously
            new AsyncHttpClient.HttpExecutor(null, managedConnection).run();
        }
    }

    public static void stopConnectionsForDelegate(JRConnectionManagerDelegate delegate) {
        synchronized (sDelegateConnections) {
            Set<ManagedConnection> connections = sDelegateConnections.get(delegate);
            if (connections != null) for (ManagedConnection c : connections) c.mDelegate = null;
        }
    }

    /*package*/ static class ManagedConnection {
        final private Object mTag;
        final private String mRequestUrl;
        final private byte[] mPostData;
        final private List<NameValuePair> mRequestHeaders;
        final private boolean mFollowRedirects;

        private HttpUriRequest mHttpRequest;
        private AsyncHttpClient.AsyncHttpResponse mResponse;
        private JRConnectionManagerDelegate mDelegate;

        public ManagedConnection(JRConnectionManagerDelegate delegate,
                                 Object tag,
                                 String requestUrl,
                                 byte[] postData,
                                 List<NameValuePair> requestHeaders, boolean followRedirects) {
            mDelegate = delegate;
            mTag = tag;
            mRequestHeaders = requestHeaders == null ? new ArrayList<NameValuePair>() : requestHeaders;
            mRequestUrl = requestUrl;
            mPostData = postData;
            mFollowRedirects = followRedirects;
        }

        /*package*/ String getRequestUrl() {
            return mRequestUrl;
        }

        /*package*/ byte[] getPostData() {
            return mPostData;
        }

        /*package*/ List<NameValuePair> getRequestHeaders() {
            return mRequestHeaders;
        }

        /*package*/ HttpUriRequest getHttpRequest() {
            return mHttpRequest;
        }

        /*package*/ boolean getFollowRedirects() {
            return mFollowRedirects;
        }

        /*package*/ void setResponse(AsyncHttpClient.AsyncHttpResponse response) {
            mResponse = response;
        }
    }

    /*package*/ static class HttpCallback implements Runnable {
        private final ManagedConnection mConn;

        /*package*/ HttpCallback(ManagedConnection cd) {
            mConn = cd;
        }

        public void run() {
            synchronized (sDelegateConnections) {
                Set<ManagedConnection> managedConnections = sDelegateConnections.get(mConn.mDelegate);
                if (managedConnections != null) managedConnections.remove(mConn);
            }

            if (mConn.mDelegate == null || mConn.mHttpRequest.isAborted()) return;

            if (mConn.mResponse.hasException()) {
                mConn.mDelegate.connectionDidFail(mConn.mResponse.getException(),
                        mConn.mResponse.getHeaders(),
                        mConn.mResponse.getPayload(),
                        mConn.mRequestUrl,
                        mConn.mTag);
            } else {
                mConn.mDelegate.connectionDidFinishLoading(mConn.mResponse.getHeaders(),
                        mConn.mResponse.getPayload(),
                        mConn.mRequestUrl,
                        mConn.mTag);
            }
        }
    }

    public static void setCustomUserAgent(String customUserAgent) {
        AsyncHttpClient.setCustomUserAgent(customUserAgent + "" + System.getProperty("http.agent"));
    }
}
