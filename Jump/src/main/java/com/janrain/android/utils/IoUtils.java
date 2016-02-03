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
package com.janrain.android.utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.janrain.android.utils.LogUtils.throwDebugException;

/**
 * @internal
 * @class IoUtils
 */
public final class IoUtils {
    private static final String TAG = IoUtils.class.getSimpleName();

    private IoUtils() { }

    /**
     * Reads the entire contents of the specified stream to a byte array, then closes the stream.
     *
     * @param in                 The input stream to read the contents of.
     * @param shouldThrowOnError Flag indicating whether or not the user wants to handle exceptions that are
     *                           thrown during this operation.
     * @return A byte array representing the full contents of the stream, null if stream is null.
     * @throws IOException If the user passed <code>true</code> for 'shouldThrowOnError' and an IOException
     *                     has occurred.
     */
    public static byte[] readAndClose(InputStream in, boolean shouldThrowOnError) throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;

            // XXX
            // This loop results in the HttpClient EofSensorInputStream detecting EOF and delegating its
            // wrapped InputStream's closure to the associated BasicManagedEntity, which only closes its
            // stream if it's using a managed connection, and always tells the EofSensorInputStream not to
            // close the stream. The EofSensorInputStream also tosses the reference to its wrapped stream,
            // which orphans the stream and it becomes uncloseable.
            // None of this makes sense to me, but I am working around the behavior I don't understand by
            // rewriting the loop to not read past EOF.
            //while ((len = in.read(buffer)) != -1) baos.write(buffer, 0, len);

            // Rewritten loop:
            //while ((len = in.read(buffer, 0, in.available())) > 0) baos.write(buffer, 0, len);

            // Time passes ...
            //
            // Rewrote the gzip handling to drain the HTTP response stream into a byte array first,
            // which alleviates the need to rewrite the loop (letting the more efficient original
            // version be used.)
            while ((len = in.read(buffer)) != -1) baos.write(buffer, 0, len);

            return baos.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "[readAndClose] problem reading from input stream.", e);
            if (shouldThrowOnError) throw e;
        } finally {
            try {
                if (baos != null) baos.close();
            } catch (IOException e) {
                throwDebugException(new RuntimeException(e));
            }

            try {
                in.close();
            } catch (IOException e) {
                throwDebugException(new RuntimeException(e));
            }
        }

        return null;
    }
}
