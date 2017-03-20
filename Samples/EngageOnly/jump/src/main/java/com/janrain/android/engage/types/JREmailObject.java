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

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @brief Object containing content to be shared by email.
 *
 * JREmailObject is a simple container object for a pre-composed email to be attached to a
 * {@link com.janrain.android.engage.types.JRActivityObject JRActivityObject}.
 *
 * Instantiate a new JREmailObject with a subject and a body.  Optionally, add a list of URLs
 * from the body and Engage will automatically shorten them, track click-throughs, and provide
 * analytics on your dashboard.
 *
 * @nosubgrouping
 **/
public class JREmailObject implements Serializable {
/**
 * @name Private Attributes
 * The various properties of the JREmailObject that you can access and configure through the object's
 * constructor and getters
 **/
/*@{*/
    /**
     * The pre-composed subject of the email.
     *
     * @par Getter/Setter:
     *      #getSubject(), #setSubject()
     **/
    private String mSubject;

    /**
     * The pre-composed body of the email.
     *
     * @par Getter/Setter:
     *      #getBody(), #setBody()
     **/
    private String mBody;

    /**
     * The list of URLs found in the body that are to be shortened.
     *
     * @par Getter/Setter:
     *      #getUrls(), #setUrls(), #addUrl()
     **/
    private List<String> mUrls;

/*@}*/

    /**
     * @internal
     * The list of shortened URLs.
     **/
    private List<String> mShortUrls;

/**
 * @name Constructors
 * Constructors for the JREmailObject
 **/
/*@{*/
    /**
     * Create an empty email object.
     */
    public JREmailObject() {
        this("", "");
    }

    /**
     * Create an email object with a given subject and body.
     *
     * @param subject
     *      The email's subject line, passed to the device's email application
     *
     * @param body
     *      The email's body, which will be appended to the user's comment and passed to the
     *      device's email application.
     **/
    public JREmailObject(String subject, String body) {
        if (subject == null) subject = "";
        if (body == null) body = "";

        mSubject = subject;
        mBody = body;
        mUrls = new ArrayList<String>();
    }
/*@}*/

    /**
     * @internal
     * Returns a JREmailObject initialized with the given dictionary. Used for Phonegap plugin.
     *
     * @param dictionary
     *   A dictionary containing the properties of an activity object.
     *
     * @throws IllegalArgumentException
     *   if dictionary is null
     *
     * NOTE: This function should not be used directly.  It is intended only for use by the
     * JREngage library.
     **/
    public JREmailObject(JRDictionary dictionary) {
        if (dictionary == null) throw new IllegalArgumentException("illegal null action");

        mSubject = dictionary.getAsString("subject", "");
        mBody    = dictionary.getAsString("messageBody", "");
        mUrls    = dictionary.getAsListOfStrings("urls", true);
    }

    /**
     * @internal
     *
     * @param shortUrls
     */
    /* package*/ void setShortUrls(List<String> shortUrls) {
        mShortUrls = shortUrls;

        for (String longUrl : mUrls) {
            String shortUrl = mShortUrls.get(mUrls.indexOf(longUrl));
            mBody = mBody.replace(longUrl, shortUrl);
        }
    }

/**
 * @name Getters/Setters
 * Getters for the JREmailObject's private properties
 **/
/*@{*/
    /**
     * Getter for the email object's #mSubject property.
     * 
     * @return
     *      The subject of the email
     **/
    public String getSubject() {
        return mSubject;
    }

    /**
     * Setter for the email object's #mSubject property.
     *
     * @param subject
     *      The subject of the email
     **/
    public void setSubject(String subject) {
        mSubject = subject;
    }

    /**
     * Getter for the email object's #mBody property.
     *
     * @return
     *      The body of the email
     **/
    public String getBody() {
        return mBody;
    }

    /**
     * Setter for the email object's #mBody property.
     *
     * @param body
     *      The body of the email
     **/
    public void setBody(String body) {
        mBody = body;
    }

    /**
     * Getter for the email object's #mUrls property, a list of URLs to be shortened.
     *
     * @return
     *      An immutable list of URLs to be shortened
     **/
    public List<String> getUrls() {
        return Collections.unmodifiableList(mUrls);
    }

    /**
     * Setter for the email object's #mUrls property, a list of URLs to be shortened.  Each of these URLs will
     * be shortened to an rpx.me URL, which tracks click-throughs, and provides analytics.  Once shortened,
     * the Engage for Android library will substitute the shortened version for the original long version for
     * each URL found in the body of the email.
     *
     * @param urls
     *      The email object's list of URLs to be shortened, not more than five URLs
     **/
    public void setUrls(List<String> urls) {
        if (urls == null) urls = new ArrayList<String>();
        mUrls = new ArrayList<String>();

        for (String url : urls) addUrl(url);
    }

    /**
     * Add a single URL to the list of URLs to shorten.
     *
     * @param url
     *      The URL to add to the list of URLs to shorten
     **/
    public void addUrl(String url) {
        if (TextUtils.isEmpty(url)) throw new IllegalArgumentException("illegal empty string parameter");
        if (mUrls.size() >= 5) {
            throw new IllegalArgumentException("JREmailObject supports a maximum of five URLs");
        }

        mUrls.add(url);
    }
/*@}*/
}
