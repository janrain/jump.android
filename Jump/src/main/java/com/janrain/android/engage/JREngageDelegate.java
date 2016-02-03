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
package com.janrain.android.engage;

import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;

/**
 * @brief
 * The interface implemented by an object in order to receive notifications when a user
 * authenticates or publishes an activity to their social networks.
 *
 * The methods of this interface are invoked upon the success or failure of the Janrain Engage for
 * Android Activities. They provide a conduit for the authenticated user's profile data, and if
 * server-side authentication is configured, the data payload returned by your server's token URL.
 *
 * @nosubgrouping
 **/
public interface JREngageDelegate {

/**
 * @name Success notifications
 * Messages sent by JREngage notifying success
 **/
/*@{*/

    /**
     * Notifies the delegate that the user has successfully authenticated with the given provider,
     * passing to the delegate a JRDictionary object with the user's profile data.
     *
     * @param auth_info
     *   A JRDictionary of fields containing all the information that Janrain Engage knows about
     *   the user signing in to your application.  Includes the field \e "profile" which contains the
     *   user's profile information.
     *
     * @param provider
     *   The name of the provider on which the user authenticated.
     *   For a list of possible strings, please see the
     *   <a href="http://documentation.janrain.com/engage/sdks/ios/mobile-providers#basicProviders">
     *   List of Providers</a>
     *
     * @note
     *   The structure of the \e auth_info JRDictionary (represented here in JSON) will be like the
     *   following:
     * @code
     "auth_info":
     {
       "profile":
       {
         "displayName": "brian",
         "preferredUsername": "brian",
         "url": "http:\/\/brian.myopenid.com\/",
         "providerName": "Other",
         "identifier": "http:\/\/brian.myopenid.com\/"
       }
     }
     * @endcode
     *
     * @sa For a full description of the dictionary and its fields,
     * please see the <a href="http://documentation.janrain.com/engage/api/auth_info">auth_info
     * response</a> section of the Janrain Engage API documentation.
     **/
    void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider);

    /**
     * Notifies the delegate after the library has successfully posted the Engage auth_info token to
     * your server application's token URL, passing to the delegate the body and headers of the HTTP
     * response from the token URL.
     *
     * @param tokenUrl
     *   The URL on the server where the token was posted and server-side authentication was completed
     *
     * @param response
     *   The response headers returned from the server
     *
     * @param tokenUrlPayload
     *   The response from the server
     *
     * @param provider
     *   The name of the provider on which the user authenticated.
     *   For a list of possible strings, please see the
     *   <a href="http://documentation.janrain.com/engage/sdks/ios/mobile-providers#basicProviders">
     *   List of Providers</a>
     **/
    void jrAuthenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response,
                                          String tokenUrlPayload, String provider);

    /**
     * \anchor didPublish
     * Notifies the delegate after the user successfully shares an activity on the given provider.
     *
     * @param activity
     *   The shared activity
     *
     * @param provider
     *   The name of the provider on which the user published the activity.
     *   For a list of possible strings, please see the
     *   For a list of possible strings, please see the
     *   <a href="http://documentation.janrain.com/engage/sdks/ios/mobile-providers#socialProviders">
     *   List of Social Providers</a>
     **/
    void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider);

    /**
     * Notifies the delegate after the social publishing dialog is closed (e.g., the user presses
     * the back button) and publishing is complete. You may receive multiple \ref didPublish
     * "void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider)"
     * messages before the dialog is closed and publishing is complete.
     **/
    void jrSocialDidCompletePublishing();
/*@}*/


/**
 * @name Failure notifications
 * Messages sent by JREngage notifying failure
 **/
/*@{*/
    /**
     * Notifies the delegate when the application invokes the display of a library Activity, but the
     * Activity fails to start. May occur if the library failed to connect to the Engage server, or
     * if the JRActivityObject was null, etc.
     *
     * @param error
     *   The error that occurred during configuration
     *
     * @note
     * This message is only sent if your application tries to show a JREngage dialog, and not
     * necessarily when the error occurred. For example, if the error occurred during the library's
     * configuration with the Engage server, it will not be sent through this interface until the
     * application attempts to display a library Activity.
     *
     * The raison d'etre for this delayed delegate delivery is to allow for the possibility that an
     * application may speculatively configure the library, but never actually invoke any library
     * Activies.  In that case, no error is delivered to the application.
     **/
    void jrEngageDialogDidFailToShowWithError(JREngageError error);

    /**
     * Notifies the delegate that authorization was canceled for any reason other than an error.
     * For example: The user pressed the back button, the cancelAuthentication method was called,
     * or configuration of the library timed out.
     **/
    void jrAuthenticationDidNotComplete();

    /**
     * @name Success notifications
     * Messages sent by JREngage notifying success
     **/
    /*@{*/

    /**
     * Notifies the delegate when authentication has failed and could not be recovered by the
     * library.
     *
     * @param error
     *   The error that occurred during authentication
     *
     * @param provider
     *   The name of the provider on which the user tried to authenticate.
     *   For a list of possible strings, please see the
     *   <a href="http://documentation.janrain.com/engage/sdks/ios/mobile-providers#basicProviders">
     *   List of Providers</a>
     *
     * @note
     * This message is not sent if authentication was canceled.  To be notified of a canceled
     * authentication, see jrAuthenticationDidNotComplete().
     **/
    void jrAuthenticationDidFailWithError(JREngageError error, String provider);

    /**
     * Notifies the delegate when the call to the token URL has failed.
     *
     * @param tokenUrl
     *   The URL on the server where the token was posted and server-side authentication was
     *   completed
     *
     * @param error
     *   The error that occurred during server-side authentication
     *
     * @param provider
     *   The name of the provider on which the user authenticated.
     *   For a list of possible strings, please see the
     *   <a href="http://documentation.janrain.com/engage/sdks/ios/mobile-providers#basicProviders">
     *   List of Providers</a>
     **/
    void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider);

    /**
     * Notifies the delegate if social publishing was canceled for any reason other than an error.
     * For example, the user presses the back button, any class (e.g., the JREngage delegate) calls
     * the cancelPublishing method, or if configuration of the library times out.
     **/
    void jrSocialDidNotCompletePublishing();

    /**
     * Notifies the delegate when publishing an activity failed and could not be recovered by the
     * library.
     *
     * @param activity
     *   The activity the user was trying to share
     *
     * @param error
     *   The error that occurred during publishing
     *
     * @param provider
     *   The name of the provider on which the user attempted to publish the activity.
     *   For a list of possible strings, please see the
     *   <a href="http://documentation.janrain.com/engage/sdks/ios/mobile-providers#socialProviders">
     *   List of Social Providers</a>
     **/
    void jrSocialPublishJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider);
/*@}*/

    /**
     * An empty implementation suitable for subclassing and selective method implementation.
     */
    abstract class SimpleJREngageDelegate implements JREngageDelegate {
        public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
        }

        public void jrAuthenticationDidReachTokenUrl(String tokenUrl,
                                                     HttpResponseHeaders response,
                                                     String tokenUrlPayload,
                                                     String provider) {
        }

        public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
        }

        public void jrSocialDidCompletePublishing() {
        }

        public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
        }

        public void jrAuthenticationDidNotComplete() {
        }

        public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
        }

        public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl,
                                                          JREngageError error,
                                                          String provider) {
        }

        public void jrSocialDidNotCompletePublishing() {
        }

        public void jrSocialPublishJRActivityDidFail(JRActivityObject activity,
                                                     JREngageError error,
                                                     String provider) {
        }
    }
}
