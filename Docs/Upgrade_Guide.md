# JUMP Android Upgrade Guide

This guide describes the steps required to upgrade from different versions of the library.

Please note that many of these update steps are cumulative.  Please start from your current version and step through each section in ascending order.

## Generalized Upgrade Process

1. Remove **Jump** or **JREngage** modules from your project.
2. Follow the steps in **Jump_Integration_Guide.md**

       **Note:** Be sure to update your `AndroidManifest.xml`.

### Generalized Solutions

* **java: package com.janrain.android.engage.R does not exist**

    Use `R` instead.

* **java: package com.janrain.android.engage.utils does not exist**

    Use `com.janrain.android.utils` instead.

* **java: method createConnection in class com.janrain.android.engage.net.JRConnectionManager cannot be applied to given types;
      required: java.lang.String,com.janrain.android.engage.net.JRConnectionManagerDelegate,java.lang.Object,java.util.List<org.apache.http.NameValuePair>,byte[],boolean
      found: java.lang.String,<anonymous com.janrain.android.engage.net.JRConnectionManagerDelegate.SimpleJRConnectionManagerDelegate>,<nulltype>
      reason: actual and formal argument lists differ in length**

    The signature of `createConnection` has changed to `createConnection(String requestUrl,
    JRConnectionManagerDelegate delegate, Object tag, List<NameValuePair> requestHeaders, byte[] postData,
    boolean followRedirects)`. `requestHeaders` and `postData` are both optional (can be `null).

* **java: cannot find symbol
      symbol:   method logd(java.lang.String,java.lang.String)
      location: class com.janrain.android.engage.JREngage**

    Use `com.janrain.android.utils.LogUtils` instead.

### Upgrading `TwitterKit` library if using TwitterKit for Native Authentication
Since the `TwitterKit` is now out of the fabric bundle, it needs to be configured independently and depending on the case you might also need to remove the `io.fabric` plugin.

* Update Twitter Kit library to version 3.1.1 in your `build.gradle` file:
```
dependencies {
...
	compile 'com.twitter.sdk.android:twitter:3.1.1'
...
}
```

* Remove the `io.fabric` plugin from your `build.gradle` file **unless you're using any other fabric products**:
```
dependencies {
...
	classpath 'io.fabric.tools:gradle:1.+' // REMOVE THIS ONE
...
}

apply plugin: 'io.fabric' // REMOVE THIS ONE TOO
```

After the version changes it's very likely you'll have various build errors because the Twitter Kit library also changed some of its classes. Review your code is compilant with the installation instructions on this link:
https://dev.twitter.com/twitterkit/android/installation

### Upgrading to v7.1 from v7.0.5

NOTE: There have been significant updates to the code base in this release.  While over all functionality should not have changed it is highly recommended that thorough testing be performed when upgrading to this version from previous versions.

This version now allows the use of the `res/raw/janrain_config.json` file which would be placed in
the application's `res` folder.  This file includes the settings for the OpenID AppAuth library that were previously covered in the `/jump.android/Jump/src/res/values/openid_appauth_idp_configs.xml` file.  Please follow these steps to use this file:

* Make sure there is a `res/raw` folder in your app, this may need to be created if it doesn't exist.
* Copy the `/Samples/SimpleDemo/res/raw/janrain_config.json` or the `/Samples/SimpleDemoNative/res/raw/janrain_config.json`
file to the `res/raw` folder.
* Update your janrain credentials and configuration settings in the `janrain_config.json` file.
* Use the new `JumpConfig(Context)` constructor. This constructor loads the configurations from the json file.
* You can now remove all the assignations you had to the JumpConfig object's fields.

At this point you can override the config object's values by assigning them values programmatically.

The `/Samples/SimpleDemo/res/raw/janrain_config.json` file is intended to be used as the template for your own application's configuration file.  Please update the values in this file to reflect your Janrain configuration and settings.

NOTE:  These changes are not mandatory, the libraries should still work using your existing configuration methods as long as they are compatible with previous versions.

If you are leveraging TwitterKit for Native Authentication, you may need to remove the Fabric.io dependencies as Fabric.io has been sold to Google and the TwitterKit functionality is now a stand alone library maintained by Twitter.

If you are leveraging the Google Sign-In libraries for Native Authentication, updating the Google Play Sign-In dependencies may require moving the following line to the bottom of your application's build.gradle file:
```
apply plugin: 'com.google.gms.google-services'
```

### Upgrading to v7.0.5 from v7.0.4

In general there should be no changes required for customers that have successfully upgraded to v7.0.4.  There were some changes to the Simple Demo application to demonstrate the Change Password functionality and improvements to the Alert Dialogs used to show errors.


### Upgrading to v7.0.4 from previous versions.

* Add the following activity to your AndroidManifest.xml file (maybe below the OpenIDAppAuthTokenActivity):
```xml
<activity
    android:name="com.janrain.android.engage.OpenIDAppAuthCancelledActivity"
    android:label="REPLACE_WITH_YOUR_APPNAME_AS_NEEDED"
    android:theme="@style/Theme.Janrain.Dialog.Light"
    android:windowSoftInputMode="stateHidden">
</activity>

* Optional:  Update build.gradle files to use the OpenID AppAuth libraries version 0.5.1 as needed

  compile 'net.openid:appauth:0.5.1'

```

Please refer to the Simple Demo app to see additional code modifications that may be required.

If you are invoking the Engage/Social Login authentication dialogs directly from within your application then you may need to bind the OpenID AppAuth Authorization Service to your main activity (or the activity you have configured to receive the redirect from Google).

The mobile libraries are expecting the Authorization service that is persisted in the Engage state/session to be the same as what will need to be referenced later in the authentication process (see JROpenIDAppAuth.java).

NOTE:  Do not bind the Authorization service to a Fragment Activity or activity that might be destroyed before the Authorization process is complete otherwise a "leaked service connection" error will occur.

The following code is extracted from the Jump.java file and demonstrates how to do this:

```
//Required Includes:
import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.browser.BrowserBlacklist;
import net.openid.appauth.browser.Browsers;
import net.openid.appauth.browser.VersionRange;
import net.openid.appauth.browser.VersionedBrowserMatcher;

//Example code:
BrowserBlacklist blacklist = new BrowserBlacklist(
                new VersionedBrowserMatcher(
                        Browsers.SBrowser.PACKAGE_NAME,
                        Browsers.SBrowser.SIGNATURE_SET,
                        true, // custom tab
                        VersionRange.ANY_VERSION));
      AuthorizationService authorizationService = new AuthorizationService(fromActivity,
              new AppAuthConfiguration.Builder().setBrowserMatcher(blacklist).build());
      state.jrEngage.setAuthorizationService(authorizationService);
      state.jrEngage.setAuthorizationActivity(fromActivity);
      if (providerName != null) {
          state.jrEngage.showAuthenticationDialog(fromActivity, providerName);
      } else {
          state.jrEngage.showAuthenticationDialog(fromActivity, TradSignInUi.class);
      }
```

###  Upgrading to v7.0.3 from previous versions.

* In order to support Google's deprecation of the use of Webviews for web based authentication the mobile libraries have been updated to use Google's preferred OpenID AppAuth for Android Libraries (version 0.4.1 tested) for web based Google authentication (this is different from the native Google Android SDK based authentication that the Mobile Libraries also support).  Other than the required code and configuration changes the end-user experience should not appear to be any different than in previous versions of the mobile libraries.

* Please read the "Docs/Upgrade_Guide.md" for general upgrade guidance.

* Please read the Samples/README.md and the respective sample folder's README.md files for specific upgrade and configuration details as there have been changes since the last release.

- Update the module settings for your project to use the latest Jump sdk files you may need to remove any existing "jump" modules and re-add the latest module in order to ensure your project files are updated.
- Open the '/jump.android/Jump/src/res/values/openid_appauth_idp_configs.xml' file and update the `google_client_id` and `google_auth_redirect_uri` with the appropriate Google application client id that correlates to the Google app that is used in your Engage application.
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!--
    This contains the authorization service configuration details that are used to demonstrate
    authentication. By default, all authorization services are disabled until you modify this file
    to provide your own configuration details.
    -->
    <eat-comment/>
    <bool name="google_enabled">true</bool>
    <string name="google_client_id" translatable="false">UPDATE_WITH_GOOGLE_CLIENT_ID.apps.googleusercontent.com</string>
    <!--
    NOTE: This scheme is automatically provisioned by Google for Android OAuth2 clients, and is
    the reverse form of the client ID registered above. Handling of this scheme is registered in an
    intent filter in the app's manifest.
    -->
    <string name="google_auth_redirect_uri" translatable="false">com.googleusercontent.apps.UPDATE_WITH_GOOGLE_CLIENT_ID:/oauth2redirect</string>
</resources>
```

- Open your application's AndroidManifest.xml and add the following activities (modify as needed):
```xml
<activity android:name="net.openid.appauth.RedirectUriReceiverActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
        <data android:scheme="https"
            android:host="com.googleusercontent.apps.UPDATE_WITH_GOOGLE_CLIENT_ID:"
            android:path="/oauth2redirect"/>
    </intent-filter>
</activity>
<activity
    android:name="com.janrain.android.engage.OpenIDAppAuthTokenActivity"
    android:label="SimpleDemoApplication"
    android:theme="@style/Theme.Janrain.Dialog.Light"
    android:windowSoftInputMode="stateHidden" >
</activity>
```

Add the manifest placeholder to your app's build.gradle defaultConfig section.

    // If using web-based (not native) Google authentication.
    // Replace the below string with your own Google client ID. Make sure this is consistent
    // with the values used in openid_appauth_idp_configs.xml
    manifestPlaceholders = [
            'appAuthRedirectScheme': 'com.googleusercontent.apps.UPDATE_WITH_GOOGLE_CLIENT_ID'
    ]

Include jcenter with your list of repositories.

    repositories {
        jcenter ()
        maven { url 'https://maven.fabric.io/public' }
    }


###  Upgrading to v6.0.0
* The only IDE that this release supports and has been tested with is the Android Studio IDE.
* The Android Mobile Libraries have removed all inter-dependencies on the Google, Facebook, and Twitter SDK's and Libraries.  The SimpleDemoNative app has been created to demonstrate how to integrate native provider logon for these providers using their SDK's and Libraries.  Please refer to the "Native Authentication Guide" for more information.
* **NOTE:**  Google Play/Sign-On libraries newer than version 8.1 are NOT supported.  Google has changed the oAuth access token provisioning as of version 8.3 and it is no longer compatible with Janrain's API's at the time of this release.  Janrain will be updating their API's to support Google's re-architecture in the future.
* Resolved an issue with the CaptureJsonUtils.java file where customers with large amounts of user records would overflow the Integer data type.
* If you want to use the Janrain Mobile Libraries and Sample Code with the latest Android API levels there is now has dependencies on the deprecated org.apache.http.legacy.jar.  This file is included in the Github repo in the libs folder.  Additional information on this can be found in the build.gradle file.

### Upgrading to v5.0.1

The Janrain Mobile Libraries and Sample Code now has dependencies on OKHttp. Follow the steps in **Eclipse_Import_Guide.md** or
**IntelliJ_Import_Guide.md** no add the necessary Jars to your project.

### Solutions when upgrading to v4.7.0

* **When linking accounts my implementation of `jrAuthenticationDidSucceedForLinkAccount` is never called**

    `jrAuthenticationDidSucceedForLinkAccount` was moved from the `JREngageDelegate` to the
    `Jump.CaptureLinkAccountHandler` to prevent Social Sign-in only applications from having to implement
    unnecessary methods. To fix this add `Jump.CaptureLinkAccountHandler` to the list of interfaces that your
    delegate implements. For example:

        private class MyEngageDelegate implements JREngageDelegate, Jump.CaptureLinkAccountHandler {


## Upgrading v2.0.1-v3.1.0 to v4.2.0

1. Remove the **JREngage** module from your project.
2. Follow the steps in **Engage_Only_Integration_Guide.md** (v2.0.1 through v3.1.0 did not support Capture.)

       **Note:** Be sure to update your `AndroidManifest.xml`.


### Solutions for upgrading from v2.0.1-v2.0.12

* **java: package com.janrain.android.engage.utils does not exist**

    Import `com.janrain.android.utils.PrefUtils` and change references to `Prefs` to `PrefUtils`.

* **java: reference to showAuthenticationDialog is ambiguous, both method
    showAuthenticationDialog(android.app.Activity,java.lang.String) in com.janrain.android.engage.JREngage and
    method showAuthenticationDialog(java.lang.Boolean,java.lang.String) in com.janrain.android.engage.JREngage
    match**

    `showAuthenticationDialg(Boolean, String)` has been deprecated, use
    `showAuthenticationDialog(Activity, String)` instead and pass in the Activity that is launching the
     authentication as the Activity.

     For example in the old version of our SimpleDemo we had
     `mEngage.showAuthenticationDialog(null, "facebook")` in `MainActivity`'s `onCreate`. To upgrade we
     replaced it with `mEngage.showAuthenticationDialog(MainActivity.this, "facebook")`.


### Solutions for upgrading from v3.0.0-v3.1.0

* **java: cannot find symbol
      symbol:   class JRCustomUiConfiguration
      location: package com.janrain.android.engage.ui**

    Use `com.janrain.android.engage.ui.JRCustomInterfaceConfiguration` instead.

* **java: cannot find symbol
      symbol:   class JRCustomUiView
      location: package com.janrain.android.engage.ui**

    Use `com.janrain.android.engage.ui.JRCustomInterfaceView` instead.

* **java: cannot find symbol
      symbol:   method showBetaDirectShareDialog(com.janrain.android.simpledemo.MainActivity,com.janrain.android.engage.types.JRActivityObject)
      location: variable mEngage of type com.janrain.android.engage.JREngage**

    `showBetaDirectShareDialog` has been removed.

* **java: method does not override or implement a method from a supertype** in reference to a subclass of
    SimpleJRConnectionManagerDelegate

    The signature of `connectionDidFail` has changed to `connectionDidFail(Exception ex,
    HttpResponseHeaders responseHeaders, byte[] payload, String requestUrl, Object tag)`.
