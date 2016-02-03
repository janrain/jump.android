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

import android.content.Context;
import android.text.TextUtils;
import com.janrain.android.engage.JREngage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @internal
 * @class Archiver Utility class used to archive/unarchive objects.  This implementation serializes the object
 * to a binary format and saves to the protected storage area (disk).
 */
public final class Archiver {
    private static final String TAG = Archiver.class.getSimpleName();
    // token used to separate parts of file name
    private static final String DICTIONARY_FILENAME_SEPARATOR = "~";
    // prefix + dictionary name
    private static final String DICTIONARY_FILENAME_BASE_FORMAT = "dict" + DICTIONARY_FILENAME_SEPARATOR +
            "%s";

    private Archiver() {
    }

    /**
     * Saves (archives) the specified object to the local (protected) file system.
     *
     * @param name   The name the object will be saved as on disk.  This parameter cannot be null.
     * @param object The object to be saved.
     * @throws IllegalArgumentException if the name parameter is null.
     * @throws IllegalStateException    if JREngage.getContext() returns null.
     */
    public static void asyncSave(String name, Object object) {
        Context context = JREngage.getApplicationContext();
        if (context == null) throw new IllegalStateException("Illegal null Context");
        asyncSave(name, object, context);
    }

    /**
     * Saves (archives) the specified object to the local (protected) file system.
     *
     * @param name    The name the object will be saved as on disk.  This parameter cannot be null.
     * @param object  The object to be saved.
     * @param context A context to access the filesystem from.
     * @throws IllegalArgumentException if the name parameter is null.
     */
    public static void asyncSave(final String name, final Object object, final Context context) {
        if (TextUtils.isEmpty(name)) throw new IllegalArgumentException("name parameter cannot be null");

        ThreadUtils.executeInBg(new Runnable() {
            public void run() {
                String fileName = String.format(DICTIONARY_FILENAME_BASE_FORMAT, name);

                // purge existing file
                context.deleteFile(fileName);

                FileOutputStream fos = null;
                ObjectOutputStream oos = null;
                try {
                    fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                    oos = new ObjectOutputStream(fos);
                    oos.writeObject(object);
                    oos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        if (oos != null) {
                            oos.close();
                        }
                    } catch (IOException ignore) {
                    }

                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException ignore) {
                    }
                }
            }
        });
    }

    /**
     * Loads the specified object from the local (protected) file system.
     *
     * @param name The name of the object to be loaded from disk.  This parameter cannot be null.
     * @return The object if found and loaded, null otherwise.
     * @throws IllegalArgumentException if the name parameter is null.
     * @throws IllegalStateException    if JREngage.getContext() returns null.
     * @throws LoadException            if there was any problem loading the object. (Including SUID
     *                                  mismatches, IO exceptions, de-serialization exceptions, cosmic rays,
     *                                  et cetera.)
     */
    public static <T> T load(String name) throws LoadException {
        Context context = JREngage.getApplicationContext();
        if (context == null) throw new IllegalStateException("[loadObject] JREngage.getContext() is null.");
        if (TextUtils.isEmpty(name)) throw new IllegalArgumentException("name parameter cannot be null");

        return load(name, context);
    }

    /**
     * Loads (unarchives) the specified object from the local (protected) file system.
     *
     * @param name    The name of the object to be loaded from disk.  This parameter cannot be null.
     * @param context The Context used to access the file system.
     * @return The object if found and loaded, null otherwise.
     * @throws IllegalArgumentException if the name parameter is null.
     * @throws IllegalStateException    if JREngage.getContext() returns null.
     * @throws LoadException            if there was any problem loading the object. (Including SUID
     *                                  mismatches, IO exceptions, de-serialization exceptions, cosmic rays,
     *                                  et cetera.)
     */
    @SuppressWarnings("unchecked")
    public static <T> T load(String name, Context context) throws LoadException {
        String fileName = String.format(DICTIONARY_FILENAME_BASE_FORMAT, name);

        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = context.openFileInput(fileName);
            ois = new ObjectInputStream(fis);
            return (T) ois.readObject();
        } catch (IOException e) {
            throw new LoadException(e);
        } catch (ClassNotFoundException e) {
            throw new LoadException(e);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException ignore) {
                }
            }

            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public static void delete(String name) {
        Context context = JREngage.getApplicationContext();
        if (context == null) throw new IllegalStateException("[loadObject] JREngage.getContext() is null.");
        if (TextUtils.isEmpty(name)) throw new IllegalArgumentException("name parameter cannot be null");

        delete(context, name);
    }

    public static void delete(Context context, String name) {
        String fileName = String.format(DICTIONARY_FILENAME_BASE_FORMAT, name);
        context.deleteFile(fileName);

    }

    public static class LoadException extends Exception {
        public LoadException(String detailMessage) {
            super(detailMessage);
        }

        public LoadException(Throwable throwable) {
            super(throwable);
        }
    }
}