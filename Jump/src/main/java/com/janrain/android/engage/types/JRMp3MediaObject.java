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


/**
 * @brief Mp3 object to be included in a post to a user's stream.
 *
 * Create an mp3 media object, fill in the object's fields, and add the object to the
 * JRActivityObject#mMedia array in your JRActivityObject.  How the mp3s get presented
 * and whether or not they are used, depend on the provider.
 *
 * Each mp3 must contain a \e src url, which is the URL of the MP3 file to be rendered.
 * The mp3 can also include a \e title, \e artist, and \e album.
 *
 * @note You can only include one JRMp3MediaObject in the media array.  Any others
 * will be ignored.
 *
 * * @ Format and rules are identical to those described on the
 * <a href="http://developers.facebook.com/docs/guides/attachments">
 * Facebook Developer page on Attachments</a>.
 *
 * @nosubgrouping
 **/
 public class JRMp3MediaObject extends JRMediaObject {
/**
 * @name Private Attributes
 * The various properties of the mp3 media object that you can access and configure through the
 * object's constructor, getters, and setters
 **/
/*@{*/
    /**
     * The URL of the mp3 file to be rendered.
     *
     * @par Getter:
     *      #getSrc()
     **/
    private String mSrc;

    /**
     * The title of the song.
     *
     * @par Getter/Setter:
     *      #getTitle(), #setTitle()
     **/
    private String mTitle;

    /**
     * The artist.
     *
     * @par Getter/Setter:
     *      #getArtist(), #setArtist()
     **/
    private String mArtist;

    /**
     * The album.
     *
     * @par Getter/Setter:
     *      #getAlbum(), #setAlbum()
     **/
    private String mAlbum;
/*@}*/

/**
 * @name Constructors
 * Constructor for an mp3 media object
 **/
/*@{*/
    /**
     * Returns a JRMp3MediaObject initialized with the given src.
     *
     * @param src
     *   The URL of the mp3 file to be rendered.  This value cannot be null
     *
     * @throws IllegalArgumentException
     *   if src is null
     **/
    public JRMp3MediaObject(String src) {
        if (src == null) throw new IllegalArgumentException("illegal null src");
        mSrc = src; // TODO: Check that these are valid urls!
    }
/*@}*/

    /**
     * @internal
     * Returns a JRMp3MediaObject initialized with the given dictionary.
     *
     * @param dictionary
     *   A dictionary containing the properties of an activity object.
     *
     * @throws IllegalArgumentException
     *   if dictionary is null or if the values for the key \c src is null
     *
     * NOTE: This function should not be used directly.  It is intended only for use by the
     * JREngage library.
     **/
    public JRMp3MediaObject(JRDictionary dictionary) {
        if (dictionary == null) throw new IllegalArgumentException("illegal null action");

        mSrc    = dictionary.getAsString("src");
        mTitle  = dictionary.getAsString("title");
        mArtist = dictionary.getAsString("artist");
        mAlbum  = dictionary.getAsString("album");

        if (mSrc == null) throw new IllegalArgumentException("illegal null src");
    }

/**
 * @name Getters/Setters
 * Getters and setters for the mp3 media object's private properties
 **/
/*@{*/
    /**
     * Getter for the mp3 object's #mSrc property.
     *
     * @return
     *      The URL of the MP3 file to be rendered
     **/
    public String getSrc() {  /* (readonly) */
        return mSrc;
    }

    /**
     * Getter for the mp3 object's #mTitle property.
     *
     * @return
     *      The title of the song
     **/
    public String getTitle() {
        return mTitle;
    }

    /**
     * Setter for the mp3 object's #mTitle property.
     *
     * @param title
     *      The title of the song
     **/
    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * Getter for the mp3 object's #mArtist property.
     *
     * @return
     *      The artist
     **/
    public String getArtist() {
        return mArtist;
    }

    /**
     * Setter for the mp3 object's #mArtist property.
     *
     * @param artist
     *      The artist
     **/
    public void setArtist(String artist) {
        mArtist = artist;
    }

    /**
     * Getter for the mp3 object's #mAlbum property.
     *
     * @return
     *      The album
     **/
    public String getAlbum() {
        return mAlbum;
    }

    /**
     * Setter for the mp3 object's #mAlbum property.
     *
     * @param album
     *      The album
     **/
    public void setAlbum(String album) {
        mAlbum = album;
    }
/*@}*/

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public JRDictionary toJRDictionary() {
        JRDictionary retval = new JRDictionary();

        retval.put("src", getSrc());
        retval.put("type", getType());
        if (getAlbum() != null) retval.put("album", getAlbum());
        if (getArtist() != null) retval.put("artist", getArtist());
        if (getTitle() != null) retval.put("title", getTitle());

        return retval;
    }
}
