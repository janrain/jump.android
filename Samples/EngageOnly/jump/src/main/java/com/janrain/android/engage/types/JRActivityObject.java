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
import android.util.Log;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.session.JRSession;
import com.janrain.android.utils.AndroidUtils;
import com.janrain.android.utils.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.janrain.android.utils.CollectionUtils.Function;
import static com.janrain.android.utils.CollectionUtils.map;

/*
 * @file
 * @brief Interface for creating and populating activities that you wish to publish.
 *
 * Interface for creating and populating activities that you wish to publish
 * to your user's social networks.  Create an activity object, fill in the
 * object's fields, and pass the object to the JREngage library when you
 * are ready to share.
 */

/**
 * @brief An activity object you create, populate, and post to the user's activity stream.
 *
 * Create an activity object, fill in the object's fields, and pass the object to
 * the JREngage library when you are ready to publish. Currently supported providers are:
 *   - Facebook
 *   - LinkedIn
 *   - Twitter
 *   - MySpace
 *   - Yahoo!
 *
 * Janrain Engage will make a best effort to use all of the fields submitted in the activity
 * request, but note that how they get presented (and which ones are used) ultimately depends on
 * the provider.
 *
 * This API will work if and only if:
 *   - Your Janrain Engage application has been configured with the given provider
 *   - The user has already authenticated and has given consent to publish activity
 *
 * Otherwise, you will be given an error response indicating what was wrong. Detailed error
 * responses will also be given if the activity parameter does not meet the formatting requirements
 * described below.
 *
 * @sa For more information of Janrain Engage's activity api, see <a
 * href="http://documentation.janrain.com/activity">the activity section</a> of our API Documentation.
 *
 * @nosubgrouping
 **/
public class JRActivityObject implements Serializable, JRJsonifiable {
    private static final String TAG = JRActivityObject.class.getSimpleName();

/**
 * @name Private Attributes
 * The various properties of the JRActivityObject that you can access and configure through the
 * object's constructor, getters, and setters
 **/
/*@{*/
    /**
     * A string describing what the user did, written in the third person (e.g.,
     * "wrote a restaurant review", "posted a comment", "took a quiz").
     *
     * {@link #getAction() Getter:}
     **/
    private String mAction;

    /**
     * The URL of the resource being mentioned in the activity update.
     *
     * @par Getter:
     *      #getUrl()
     **/
    private String mUrl;

    /**
     * A string containing user-supplied content, such as a comment or the first paragraph of
     * an article that the user wrote.
     *
     * @note
     *      Some providers (Twitter in particular) may truncate this value.
     *
     * @par Getter/Setter:
     *      #getUserGeneratedContent(), #setUserGeneratedContent()
     **/
    private String mUserGeneratedContent = "";

    /**
     * The title of the resource being mentioned in the activity update.
     *
     * @note No length restriction on the status is imposed by Janrain Engage, however Yahoo
     * truncates this value to 256 characters.
     *
     * @par Getter/Setter:
     *      #getTitle(), #setTitle()
     **/
    private String mTitle = "";

    /**
     * A description of the resource mentioned in the activity update.
     *
     * @par Getter/Setter:
     *      #getDescription(), #setDescription()
     **/
    private String mDescription = "";

    /**
     * An array of JRActionLink objects, each having two attributes: \e text and \e href.
     * An action link is a link a user can use to take action on an activity update on the provider.
     *
     * @par Example:
     * @code
     * action_links:
     * [
     *   {
     *     "text": "Rate this quiz result",
     *     "href": "http://example.com/quiz/12345/result/6789/rate"
     *   },
     *   {
     *     "text": "Take this quiz",
     *     "href": "http://example.com/quiz/12345/take"
     *   }
     * ]
     * @endcode
     *
     * @note
     *      Any objects added to this array that are not of type JRActionLink will
     *      be ignored.
     *
     * @par Getter/Setter:
     *      #getActionLinks(), #setActionLinks(), #addActionLink()
     **/
    private List<JRActionLink> mActionLinks = new ArrayList<JRActionLink>();

    /**
     * An array of objects with base class \e JRMediaObject (i.e. JRImageMediaObject,
     * JRFlashMediaObject, JRMp3MediaObject).
     *
     * To publish attached media objects with your activity, create the preferred
     * object, populate the object's fields, then add the object to the #mMedia array.
     * You can attach pictures, videos, and mp3s to your activity, although how the
     * media objects get presented and whether or not they are used, depend on the provider.
     *
     * If you include more than one media type in the array, JREngage will
     * choose only one of these types, in this order:
     *   -# image
     *   -# flash
     *   -# mp3
     *
     * Also, any objects added to this array that do not inherit from \e JRMediaObject
     * will be ignored.
     *
     * @sa Media object format and rules are identical to those described on the <a
     * href="http://developers.facebook.com/docs/guides/attachments">Facebook Developer page
     * on Attachments</a>.
     *
     * @par Getter/Setter:
     *      #getMedia(), #setMedia(List<JRMediaObject>), #setMedia(JRMediaObject)
     **/
    private List<JRMediaObject> mMedia = new ArrayList<JRMediaObject>();

    /**
     * An object with attributes describing properties of the update. An attribute \e value can be
     * a string or an object with two attributes, \e text and \e href.
     *
     * @par Example:
     * @code
     *   properties:
     *   {
     *       "Time": "05:00",
     *       "Location":
     *       {
     *           "text": "Portland",
     *           "href": "http://en.wikipedia.org/wiki/Portland,_Oregon"
     *       }
     *   }
     * @endcode
     *
     * @par Getter/Setter:
     *      #getProperties(), #setProperties()
     **/
    private JRDictionary mProperties = new JRDictionary();

    /**
     * An JREmailObject containing the subject and message body of an email, if the user wishes to
     * share via email.
     *
     * @par Getter/Setter:
     *      #getEmail(), #setEmail()
     **/
    private JREmailObject mEmail;

    /**
     * A JRSmsObject containing the message body of an SMS, if the user wishes to
     * share via SMS.
     *
     * @par Getter/Setter:
     *      #getSms(), #setSms()
     **/
    private JRSmsObject mSms;
/*@}*/

    private transient boolean mIsShortening = false;
    private String mShortenedUrl;

/**
 * @name Constructors
 * Constructor for JRActivityObject
 **/
/*@{*/
    /**
     * Returns a JRActivityObject initialized with the given action and URL.
     *
     * @param action
     *   A string describing what the user did, written in the third person.  This value cannot
     *   be null
     *
     * @param url
     *   The URL of the resource being mentioned in the activity update. Null for no resource link.
     *
     * @throws IllegalArgumentException
     *   if action is null
     **/
    public JRActivityObject(String action, String url) {
        init(action, url);
    }

    /**
     * Returns a JRActivityObject initialized with the given action.
     *
     * @param action
     *   A string describing what the user did, written in the third person.  This value cannot
     *   be null
     *
     * @throws IllegalArgumentException
     *   if action is null
     **/
    public JRActivityObject(String action) {
        init(action, "");
    }
/*@}*/

    /**
     * Returns a JRActivityObject initialized with the given dictionary.
     *
     * @param activity
     *   A dictionary containing the properties of an activity object.
     *
     * @throws IllegalArgumentException
     *   If activity is null
     **/
    public JRActivityObject(JRDictionary activity) {
        if (activity == null) throw new IllegalArgumentException("illegal null action");

        init(activity.getAsString("action"), activity.getAsString("url", ""));

        mTitle       = activity.getAsString("resourceTitle");
        mDescription = activity.getAsString("resourceDescription");

        List<JRDictionary> actionLinks = activity.getAsListOfDictionaries("actionLinks", true);

        mActionLinks.addAll(map(actionLinks, new Function<JRActionLink, JRDictionary>() {
            public JRActionLink operate(JRDictionary val) { return new JRActionLink(val); }
        }));

        List<JRDictionary> medias;
        medias = activity.getAsListOfDictionaries("media", true);

        for (JRDictionary mediaObject : medias) {
            if ((mediaObject).getAsString("type").equals("image")) {
                mMedia.add(new JRImageMediaObject(mediaObject));
            } else if ((mediaObject).getAsString("type").equals("flash")) {
                mMedia.add(new JRFlashMediaObject(mediaObject));
            } else if ((mediaObject).getAsString("type").equals("mp3")) {
                mMedia.add(new JRMp3MediaObject(mediaObject));
            }
        }

        mProperties = activity.getAsDictionary("properties", true);

        if (activity.containsKey("email")) mEmail = new JREmailObject(activity.getAsDictionary("email"));
        if (activity.containsKey("sms")) mSms = new JRSmsObject(activity.getAsDictionary("sms"));
    }


    private void init(String action, String url) {
        if (action == null) throw new IllegalArgumentException("illegal null action");
        if (url == null) url = "";

        LogUtils.logd(TAG, "created with action: " + action + " url: " + url);
        mAction = action;
        mUrl = url;
    }

/**
 * @name Getters/Setters
 * Getters and setters for the JRActivityObject's private properties
 **/
/*@{*/

    /**
     * Getter for the activity object's #mAction property.
     *
     * @return
     *      A string describing what the user did, written in the third person (e.g.,
     *      "wrote a restaurant review", "posted a comment", "took a quiz")
     **/
    public String getAction() {  /* (readonly) */
        return mAction;
    }

    /**
     * Getter for the activity object's #mUrl property.
     *
     * @return
     *       The URL of the resource being mentioned in the activity update
     **/
    public String getUrl() {  /* (readonly) */
        return mUrl;
    }

    /**
     * Getter for the activity object's #mUserGeneratedContent property.
     *
     * @return
     *      A string containing user-supplied content, such as a comment or the first paragraph of
     *      an article that the user wrote
     **/
    public String getUserGeneratedContent() {
        return mUserGeneratedContent;
    }

    /**
     * Setter for the activity object's #mUserGeneratedContent property.
     *
     * @param userGeneratedContent
     *      A string containing user-supplied content, such as a comment or the first paragraph of
     *      an article that the user wrote
     **/
    public void setUserGeneratedContent(String userGeneratedContent) {
        mUserGeneratedContent = userGeneratedContent;
    }

    /**
     * Getter for the activity object's #mTitle property.
     *
     * @return
     *      The title of the resource being mentioned in the activity update
     **/
    public String getTitle() {
        return mTitle;
    }

    /**
     * Setter for the activity object's #mTitle property.
     *
     * @param title
     *      The title of the resource being mentioned in the activity update
     **/
    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * Getter for the activity object's #mDescription property.
     *
     * @return
     *      A description of the resource mentioned in the activity update
     **/
    public String getDescription() {
        return mDescription;
    }

    /**
     * Setter for the activity object's #mDescription property.
     *
     * @param description
     *      A description of the resource mentioned in the activity update
     **/
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * Getter for the activity object's #mActionLinks property.
     *
     * @return
     *      An array of JRActionLink objects, each having two attributes: \e text and \e href.
     *      An action link is a link a user can use to take action on an activity update on the provider
     **/
    public List<JRActionLink> getActionLinks() {
        return mActionLinks;
    }

    /**
     * Setter for the activity object's #mActionLinks property.
     *
     * @param actionLinks
     *      An array of JRActionLink objects, each having two attributes: \e text and \e href.
     *      An action link is a link a user can use to take action on an activity update on the provider
     **/
    public void setActionLinks(List<JRActionLink> actionLinks) {
        mActionLinks = actionLinks;
    }

    /**
     * Add to the activity object's #mActionLinks property.
     *
     * @param actionLink
     *      A single JRActionLink to be added to the array of action links, creating the array if
     *      it hasn't already been created
     **/
    public void addActionLink(JRActionLink actionLink) {
        if (mActionLinks == null) mActionLinks = new ArrayList<JRActionLink>();
        mActionLinks.add(actionLink);
    }

    /**
     * Add to the activity object's #mActionLinks property.
     *
     * @param displayText
     *      The text displayed to the user for the action link, e.g. "Download the single now", or "Sign up
     *      to bring a dish to the potluck".
     * @param link
     *      The URL to the link where the user can carry out the action.
     **/
    public void addActionLink(String displayText, String link) {
        addActionLink(new JRActionLink(displayText, link));
    }

    /**
     * Alias for addMedia(JRMediaObject)
     *
     * @param media
     *        A single JRImageMediaObject, JRFlashMediaObject, or JRMp3MediaObject to be added to
     *        the array of media objects, creating the array if it hasn't already been created
     **/
    public void addMedia(JRMediaObject media) {
        if (mMedia == null) mMedia = new ArrayList<JRMediaObject>();
        mMedia.add(media);
    }

    /**
     * Getter for the activity object's #mMedia property.
     *
     * @return
     *      An array of objects with base class \e JRMediaObject (i.e. JRImageMediaObject,
     *      JRFlashMediaObject, JRMp3MediaObject)
     **/
    public List<JRMediaObject> getMedia() {
        return mMedia;
    }

    /**
     * Setter for the activity object's #mMedia property.
     *
     * @param media
     *      An array of objects with base class \e JRMediaObject (i.e. JRImageMediaObject,
     *      JRFlashMediaObject, JRMp3MediaObject)
     **/
    public void setMedia(List<JRMediaObject> media) {
        mMedia = media;
    }

    /**
     * Setter for the activity object's #mMedia property.
     *
     * @deprecated use #addMedia(JRMediaObject)
     *
     * @param mediaObject
     *        A single JRImageMediaObject, JRFlashMediaObject, or JRMp3MediaObject to be added to
     *        the array of media objects, creating the array if it hasn't already been created
     **/
    public void setMedia(JRMediaObject mediaObject) {
        if (mMedia == null) mMedia = new ArrayList<JRMediaObject>();
        mMedia.add(mediaObject);
    }

    /**
     * Getter for the activity object's #mProperties property.
     *
     * @return
     *      An object with attributes describing properties of the update. An attribute value can be
     *      a string or an object with two attributes, \e text and \e href.
     **/
    public Map<String, Object> getProperties() {
        return mProperties;
    }

    /**
     * Setter for the activity object's #mProperties property.
     *
     * @param properties
     *      An object with attributes describing properties of the update. An attribute value can be
     *      a string or an object with two attributes, \e text and \e href.
     **/
    public void setProperties(JRDictionary properties) {
        mProperties = properties;
    }

    /**
     * Getter for the activity object's #mEmail property.
     *
     * @return
     *      An JREmailObject containing the content that a user can send via SMS to share an activity
     **/
    public JREmailObject getEmail() {
        return mEmail;
    }

    /**
     * Setter for the activity object's #mEmail property.
     *
     * @param email
     *      An JREmailObject containing the content that a user can send via email to share an activity
     **/
    public void setEmail(JREmailObject email) {
        mEmail = email;
    }

    /**
     * Getter for the activity object's #mSms property.
     *
     * @return
     *      An JRSmsObject containing the content that a user can send via SMS to share an activity
     **/
    public JRSmsObject getSms() {
        return mSms;
    }

    /**
     * Setter for the activity object's #mSms property.
     *
     * @param sms
     *      An JRSmsObject containing the content that a user can send via SMS to share an activity
     **/
    public void setSms(JRSmsObject sms) {
        mSms = sms;
    }

/*@}*/

    /**
     * @internal
     * Returns a HashMap (Dictionary) representing the JRActivityObject.
     *
     * @return
     *   An HashMap (Dictionary) of String objects representing the JRActivityObject.
     *
     * NOTE: This function should not be used directly.  It is intended only for use by the
     * JREngage library.
     *
     */
    public JRDictionary toJRDictionary() {
        JRDictionary map = new JRDictionary();
        // XXX these work by json-ifying the action links, media, and properties automatically by
        // their exposed getter methods.
        map.put("url", mUrl);
        map.put("action", mAction);
        map.put("user_generated_content", mUserGeneratedContent);
        map.put("title", mTitle);
        map.put("description", mDescription);
        map.put("action_links", mActionLinks);
        map.put("media", mMedia);
        map.put("properties", mProperties);
        return map;
    }

    public interface ShortenedUrlCallback {
        public void setShortenedUrl(String shortenedUrl);
    }

    public synchronized void shortenUrls(final ShortenedUrlCallback callback) {
        if (mShortenedUrl != null) {
            callback.setShortenedUrl(mShortenedUrl);
            return;
        }

        if (mIsShortening) return; // todo there's a bug here, this code branch doesn't invoke its callback
        mIsShortening = true;

        // Don't shorten empty URLs
        JRSession session = JRSession.getInstance();
        try {
            // todo invoke when the activity object is created (or maybe when publish is called?)
            List<String> emptyList = new ArrayList<String>();
            final List<String> emailUrls = mEmail == null ? emptyList : mEmail.getUrls();
            final List<String> smsUrls = mSms == null ? emptyList : mSms.getUrls();

            // Make the JSON string
            JSONStringer jss = (new JSONStringer())
                    .object()
                        .key(JRSession.USERDATA_ACTIVITY_KEY)
                        .array()
                            .value(getUrl())
                        .endArray()

                        .key("email")
                        .array();
                            for (String url : emailUrls) jss.value(url);
                        jss.endArray()
                                
                        .key("sms")
                        .array();
                            for (String url : smsUrls) jss.value(url);
                        jss.endArray()
                    .endObject();

            // Make the URL
            final String jsonEncodedUrlsMap = jss.toString();
            String urlEncodedJson = AndroidUtils.urlEncode(jsonEncodedUrlsMap);
            final String getUrlsUrl =
                    session.getRpBaseUrl() + "/openid/get_urls?"
                    + "urls=" + urlEncodedJson
                    + "&app_name=" + session.getUrlEncodedAppName()
                    + "&device=android";

            // Define the network callback
            JRConnectionManagerDelegate jrcmd =
                    new JRConnectionManagerDelegate.SimpleJRConnectionManagerDelegate() {
                @Override
                public void connectionDidFinishLoading(HttpResponseHeaders headers,
                                                       byte[] payload,
                                                       String requestUrl,
                                                       Object tag) {
                    String shortUrl = getUrl();
                    String payloadString = new String(payload);

                    try {
                        LogUtils.logd(TAG, "fetchShortenedURLs connectionDidFinishLoading: " + payloadString);
                        JSONObject jso = (JSONObject) (new JSONTokener(payloadString)).nextValue();
                        jso = jso.getJSONObject("urls");
                        JSONObject jsonActivityUrls = jso.getJSONObject(JRSession.USERDATA_ACTIVITY_KEY);
                        JSONObject jsonSmsUrls = jso.getJSONObject("sms");
                        JSONObject jsonEmailUrls = jso.getJSONObject("email");

                        shortUrl = jsonActivityUrls.getString(getUrl());
                        if (mEmail != null) {
                            List<String> shortEmailUrls = new ArrayList<String>();
                            for (String longUrl : emailUrls)
                                shortEmailUrls.add(jsonEmailUrls.getString(longUrl));
                            mEmail.setShortUrls(shortEmailUrls);
                        }
                        if (mSms != null) {
                            List<String> shortSmsUrls = new ArrayList<String>();
                            for (String longUrl : smsUrls)
                                shortSmsUrls.add(jsonSmsUrls.getString(longUrl));
                            mSms.setShortUrls(shortSmsUrls);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "URL shortening JSON parse error", e);
                    } catch (ClassCastException e) {
                        Log.e(TAG, "URL shortening JSON parse error", e);
                    }

                    mIsShortening = false;
                    mShortenedUrl = shortUrl;
                    if (!TextUtils.isEmpty(getUrl())) updateUI(shortUrl); // Empty URLs a
                }

                void updateUI(String shortenedUrl) {
                    callback.setShortenedUrl(shortenedUrl);
                }

                @Override
                public void connectionDidFail(Exception ex,
                                              HttpResponseHeaders responseHeaders,
                                              byte[] payload, String requestUrl,
                                              Object tag) {
                    mIsShortening = false;
                    updateUI(getUrl());
                }
            };

            // Invoke the network call
            JRConnectionManager.createConnection(getUrlsUrl, jrcmd, null, null, null, false);

            // If the activity resource URL is empty invoke the callback immediately
            if (TextUtils.isEmpty(getUrl())) {
                mShortenedUrl = "";
                callback.setShortenedUrl("");
            }

        } catch (JSONException e) {
            Log.e(TAG, "URL shortening JSON error", e);
        }
    }
}
