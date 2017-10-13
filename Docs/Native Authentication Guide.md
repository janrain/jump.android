# Native Authentication Guide

This guide describes the process of integrating with native Android authentication systems. The Social Sign-in library has historically supported authentication by means of a WebView running a traditional web OAuth flow. Support is now introduced for authentication by means of native identity-provider libraries.

## Supported Providers

* Facebook (Tested with Facebook Android SDK version 4.+ as per facebook's own documentation)
* Google+ (Tested with Play/Sign-In SDK Services version 3.1.0 and Play Auth version 9.8.0)
* Twitter (Tested Twitter SDK version 3.3.1)

## Native Authentication for Janrain Mobile Libraries for Android version 6.0.0 or newer.

There are potentially *breaking* changes to the Janrain Mobile Libraries for Android with version 6.0.0.  All dependencies on third-party SDK's and libraries around native Social Provider support (Google+ and Facebook) have been removed.

The Janrain Mobile Libraries for Android no longer integrates third-party Social Provider SDK's or libraries.  The SimpleCaptureNative app has been created to demonstrate how to retrieve a native provider's oAuth access token using the current(at the time of this release) native provider's tools in as close a format to the native provider's sample code on their website.  The developer will now retrieve the native provider's oAuth access token using their preferred method and initiate the Janrain authentication process using `Jump.startTokenAuthForNativeProvider(final Activity fromActivity, final String providerName, final String accessToken, final String tokenSecret, SignInResultHandler handler, final String mergeToken)`

### 10,000′ View
1. Configure the native authentication framework for the providers you want to support (Google+, Facebook, or Twitter)
2. Provide the user the option to sign in with a Native Provider or through a UIWebview Dialog for non-native Providers
3. If the user selects to login with a Native Provider, initiate the Native Provider's SDK and retrieve a properly scoped oAuth Access Token (and Token Secret for Twitter).
4. Pass the Native Provider's oAuth access token to the Janrain Mobile Libraries where it will be posted to the Social Login server for verification and a Social Login access token will be returned.

### Facebook

As of release 7.0.0 the following Facebook SDK version 4.+ implementation steps were implemented in the SimpleCaptureDemo sample application in order to retrieve the Facebook oAuth access token from the Android device:

1. Follow *ALL* of the steps on this page *EXCEPT* for Step 2 (Create a Facebook App): https://developers.facebook.com/docs/android/getting-started/  In order for the Janrain Social Login Server to validate the provided Facebook oAuth token, the token must be provisioned from the same Facebook application that is configured for the Janrain Social Login application.  In most cases, the developer would simply add an Android App Settings configuration to the existing Facebook App.
2. Use this page as a starting point for implementing native Facebook Login:  https://developers.facebook.com/docs/facebook-login/android
3. Make sure that the Permissions requested in the `LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "email"));` method include the required permissions.  In most cases these permissions need to mirror the Facebook app permissions configuration of the Engage Social Login application that is configured in the Janrain Social Login Dashboard.
4. In your application's '/res/values/strings.xml add the following value with the Facebook App ID that corresponds to the Facebook application used by the configured Engage application:
```xml
<!-- Facebook SDK https://developers.facebook.com/docs/android/getting-started -->
    <string name="facebook_app_id">UPDATE</string>
```
5. Refer to the `MainActivity.java` file for an example of how this was done with the SimpleDemoNative application using the Facebook SDK version 4.9.0.
6. Once you have retrieved the oAuth access token from the Facebook SDK you can initiate the Janrain authentication process with `Jump.startTokenAuthForNativeProvider(final Activity fromActivity, final String providerName, final String accessToken, final String tokenSecret, SignInResultHandler handler, final String mergeToken)`

### Google+

As of release 7.0.6 the following Google SDK Play/Sign-In SDK Services version 3.1.0 and Play Auth version 9.8.0 implementation steps were implemented in the SimpleDemoNative sample application in order to retrieve the Google+ oAuth access token from the Android device:

1. Implement the following Gradle dependencies (see the build.gradle file in the SimpleDemoNative sample app for a full implementation):
    classpath 'com.google.gms:google-services:3.1.0'
    and
    compile 'com.google.android.gms:play-services:9.8.0'
    compile 'com.google.android.gms:play-services-plus:9.8.0'
    compile 'com.google.android.gms:play-services-auth:9.8.0'
2. Add the following buidscript repository and dependency value to the bottom (NOTE: this location appears to be important) your application's build.gradle file:
```gradle
apply plugin: 'com.google.gms.google-services'
```
3. Follow *ALL* of the steps on this page that involve the project configuration and Google+ app configuration: https://developers.google.com/identity/sign-in/android/start  In order for the Janrain Social Login Server to validate the provided Google+ oAuth token, the token must be provisioned from the same Google+ application that is configured for the Janrain Social Login application.  In most cases, the developer would simply add an Android App Client ID configuration to the existing Google+ App.

The google-services.json file can be complicated to fill in, it may be easier to go to this url and download a new one: https://developers.google.com/mobile/add

4. In the case of the SimpleDemoNative application the integration steps were implemented in the `MainActivity.java` file with minimal changes from the examples provided by Google at this link: https://developers.google.com/identity/sign-in/android/sign-in.
5. Make sure that the Scopes requested when configuring the Google API Client includes the required scopes.  In most cases these scopes need to mirror the Google+ app permissions configuration of the Engage Social Login application that is configured in the Janrain Social Login Dashboard. Example: `mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Plus.API).addScope(new Scope(Scopes.PROFILE)).addScope(new Scope(Scopes.EMAIL)).build();`
6. Refer to the `MainActivity.m` file for an example of how this was done with the SimpleDemoNative application.
7. If using the google-services.json file, update it with the correct Google application settings that correlates to the Google application used by the configured Engage application.
8. Once you have retrieved the oAuth access token from the Google+ SDK you can initiate the Janrain authentication process with `Jump.startTokenAuthForNativeProvider(final Activity fromActivity, final String providerName, final String accessToken, final String tokenSecret, SignInResultHandler handler, final String mergeToken)`

### Twitter

As of release 7.0.6 the following TwitterKit SDK (tested with version 3.3.1) implementation steps were implemented in the SimpleDemoNative sample application in order to retrieve the Twitter oAuth access token from the Android device:

1. Review the TwitterKit SDK Installation instructions from this link: https://dev.twitter.com/twitterkit/android/installation
2. Configure your Twitter App.  In order for the Janrain Social Login Server to validate the provided Twitter oAuth token, the token must be provisioned from the same Twitter application that is configured for the Janrain Social Login application.  In most cases, the developer would simply add an Android App Client ID configuration to the existing Twitter App.
3. Add the following dependency value to your application's build.gradle file:
```gradle
dependencies {
    compile 'com.twitter.sdk.android:twitter:3.1.1'
}

```
4. In the case of the SimpleDemoNative application the integration steps were implemented in the `MainActivity.java` file with minimal changes from the examples provided by Twitter at these links: https://dev.twitter.com/twitterkit/android/installation  and https://dev.twitter.com/twitterkit/android/log-in-with-twitter
5. NOTE: In most default cases Twitter will not return an email address for an end user. This may cause account merging or linking issues if your Registration user experience relies heavily on merged social profiles.  This use-case is typically addressed by forcing Twitter account holders to use the "Account Linking" functionality of the SDK.  Customer's may choose to work with Twitter to get their application white-listed so it will attempt to return an email address from a user profile.  However, email addresses are not "required" for Twitter accounts, subsequently there is still no guarantee that an email address will be returned.
6. Refer to the `MainActivity.java` file for an example of how this was done with the SimpleDemoNative application.
7. Once you have retrieved the oAuth *access token AND token secret* from the TwitterKit SDK you can initiate the Janrain authentication process with `Jump.startTokenAuthForNativeProvider(final Activity fromActivity, final String providerName, final String accessToken, final String tokenSecret, SignInResultHandler handler, final String mergeToken)`


## DEPRECATED: Native Authentication implementation for Janrain Mobile Libraries for Android versions prior to version 6.0.0

Prior versions (before version 6.0) of the Janrain Mobile Libraries for Android attempted to use reflection to call the native provider SDK's and retrieve the oAuth access token.  This presented a maintenance and compatibility issue with the Janrain Mobile Libraries for Android only able to support specific versions of the native provider sdk's.  The following documentation will be removed in a subsequent release but is preserved for customer's using older versions of the Janrain Mobile Libraries for Android.

Native authentication is supported by the Social Sign-in library, and is compatible with both the Sign-in only and User Registration deployments.

## Supported Providers

- Facebook
- Google+

At this time native authentication is available for authentication only, and not for social-identity-resource authorization (e.g. sharing.)

The Mobile Libraries and Sample Code is not currently able to request the same scopes that are configured in the Engage dashboard when using Native Authentication. This will be available in a future release. For the time being Facebook requests basic_info and Google+ requests plus.login.

## 10,000′ View

1. Configure the native authentication framework.
2. Start authentication.
3. The library will delegate the authentication to the native authentication framework.
4. The library delegate message will fire when native authentication completes.

## Facebook

### Configure the Native Authentication Framework

Follow the steps for creating a new Android project in the Getting Started with the Facebook SDK for Android guide at developers.facebook.com. Stop before the “A minimum viable social application” section.

Make sure that both your Android application and Social Sign-in app are configured to use the same Facebook Application App ID.

### Begin Sign-In or Authentication

Start authentication or sign-in as normal. If the Facebook Android Mobile Libraries and Sample Code is compiled into your app, it will be used to perform all Facebook authentication.

### Signing Out

Following Facebook’s documentation we’ll use `closeAndClearTokenInformation` to close the in-memory Facebook
session.

Import the Facebook Session class

    import com.facebook.Session;

Call `closeAndClearTokenInformation` when your sign-out button is pressed. For example, in SimpleDemo, we add
the following to the `onClickListener` for the `signOut` button

        signOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ...
                // Sign out of the Facebook SDK
                Session session = Session.getActiveSession();
                if (session == null) {
                    session = new Session(MainActivity.this);
                    Session.setActiveSession(session);
                }
                session.closeAndClearTokenInformation();
            }
        });

*Note:* This does not revoke the applications access to Facebook. So if a user has a Facebook account set up
in the Facebook app it will continue to be used to sign in without asking to be reauthorized.

*Note:* The context that is passed into the Session constructor should match the context that you passed into
Jump when showing the sign in dialog.

### Release Builds

If you plan to use ProGuard on your release builds add the following to your ProGuard configuration file

    -keep class com.facebook.** {*;}
    -keepattributes Signature

## Google+

### Configure the Native Authentication Framework

Follow the Google+ Platform getting started directions up to “Initalize the Plus Client”.For native Google+ authentication to work via Social Sign-in both Janrain and the Google+ Android SDK must be configured to use the same Google+ project in the Google Cloud Console.

### Configure JUMP

By default the Jump library will silently fail when Google Play Services is unavailable then attempt to sign in using a WebView. If you would like the SDK to present Google’s dialog suggesting that the user install or update or configure Google Play Services when the error is one of `SERVICE_MISSING`, `SERVICE_VERSION_UPDATE_REQUIRED`, or `SERVICE_DISABLED`, then set

    jumpConfig.tryWebViewAuthenticationWhenGooglePlayIsUnavailable = false;

or for an Engage Only integration

	engage.setTryWebViewAuthenticationWhenGooglePlayIsUnavailable(false);

After the dialog is dismissed the library will call your `onFailure` method. If the error is something else, then the SDK will fail silently and attempt sign in using a WebView.

### Signing out of the Google+ SDK

You should provide your users with the ability to sign out of the Google+ SDK. This will allow them to sign in again with a different account if they have multiple Google+ accounts on their Android device. To do that call

	Jump.signOutNativeGooglePlus(MainActivity.this);

or for an Engage Only integration

	engage.signOutNativeGooglePlus(MainActivity.this);

### Revoking the access token and disconnecting the app

To revoke the access token and disconnect the app from a user’s Google+ account call

    Jump.revokeAndDisconnectNativeGooglePlus(MainActivity.this);

or for an Engage Only integration

    engage.revokeAndDisconnectNativeGooglePlus(MainActivity.this);
