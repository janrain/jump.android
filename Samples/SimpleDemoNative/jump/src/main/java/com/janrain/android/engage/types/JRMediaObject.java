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
package com.janrain.android.engage.types;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.net.async.HttpResponseHeaders;

import java.io.Serializable;

/**
 * @brief Super type for all activity media objects.
 *
 * @class JRMediaObject
 * Super class for JRImageMediaObject, JRFlashMediaObject, and JRMp3MediaObject -- the media objects that
 * are shareable in JRActivityObjects. Do not subclass this object, instead instantiate one of the supplied
 * subclasses.
 **/
public abstract class JRMediaObject implements Serializable, JRJsonifiable {
    private transient Bitmap mThumbnailBitmap;

    public boolean hasThumbnail() { return false; }
    public String getThumbnail() {return null; }
    public abstract String getType();

    /**
     * @internal
     * @param tal
     */
    public void downloadThumbnail(final ThumbnailAvailableListener tal) {
        if (mThumbnailBitmap != null) {
            tal.onThumbnailAvailable(mThumbnailBitmap);
            return;
        }

        if (!TextUtils.isEmpty(getThumbnail())) {
            JRConnectionManager.createConnection(getThumbnail(),
                    new JRConnectionManagerDelegate.SimpleJRConnectionManagerDelegate() {
                        @Override
                        public void connectionDidFinishLoading(HttpResponseHeaders headers,
                                                               byte[] payload,
                                                               String requestUrl,
                                                               Object tag) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(payload, 0, payload.length);
                            mThumbnailBitmap = bitmap;

                            if (bitmap != null) tal.onThumbnailAvailable(bitmap);
                        }
                    }, null, null, null, false);
        }
    }

    /**
     * @internal
     */
    public interface ThumbnailAvailableListener {
        public void onThumbnailAvailable(Bitmap b);
    }

    /**
     * @internal
     * @return
     */
    public abstract JRDictionary toJRDictionary();
}
