Welcome to the JUMP platform libary for Android.  This library is available open-source under a Berkeley
license, as found in the LICENSE file.

Getting Started:
    Docs/Jump_Integration_Guide.md

Report bugs or ask questions:
    https://support.janrain.com

### Upgrading to v7.0.4 or v7.0.5
- Add the following activity to your AndroidManifest.xml file (maybe below the OpenIDAppAuthTokenActivity):
```xml
<activity
    android:name="com.janrain.android.engage.OpenIDAppAuthCancelledActivity"
    android:label="REPLACE_WITH_YOUR_APPNAME_AS_NEEDED"
    android:theme="@style/Theme.Janrain.Dialog.Light"
    android:windowSoftInputMode="stateHidden">
</activity>
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

### Upgrading to v7.0.3

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

### Upgrading to v6.0.0


- The only IDE that this release supports and has been tested with is the Android Studio IDE.
- The Android Mobile Libraries have removed all inter-dependencies on the Google, Facebook, and Twitter SDK's and Libraries.  The SimpleDemoNative app has been created to demonstrate how to integrate native provider logon for these providers using their SDK's and Libraries.  NOTE:  Google Play/Sign-On libraries newer than version 8.1 are NOT supported.  Google has changed the oAuth access token provisioning as of version 8.3 and it is no longer compatible with Janrain's API's at the time of this release.  Janrain will be updating their API's to support Google's re-architecture in the future.
- If your previous project had implemented native provider authentication you will have to re-implement this as outlined in the Native Authentication Guide and the "SimpleDemoNative" sample application.
- If you want to use the Janrain Mobile Libraries and Sample Code with the latest Android API levels there is now has dependencies on the deprecated org.apache.http.legacy.jar.  This file is included in the Github repo in the libs folder.  Additional information on this can be found in the build.gradle file.
-


### Upgrading to v5.0.1

- The Janrain Mobile Libraries and Sample Code now has dependencies on OKHttp. Follow the steps in **Eclipse_Import_Guide.md** or
**IntelliJ_Import_Guide.md** to add the necessary Jars to your project.

Updating from 3.x to 4.0:
- 4.0 adds and focuses on Capture support, renames the library project / IDE metadata to "Jump"
- See the developers integration guide, `Jump_Integration_Guide.md`
- There's a new initializer signature

Updating from 2.x to 3.0:
- See the documentation on developers.janrain.com

Updating from 1.x to 2.0:
- Replace the activity declarations in your AndroidManifest.xml with fresh copies from
  JREngage/AndroidManifest.xml
- Update your Android target to Android 13 / Honeycomb 3.2 (still deployable to Android 4+ / Donut / 1.6)
  (Alternately, remove the "screenSize" configuration change handler from the "configChanges"
  portion of each JREngage Activity declaration in your AndroidManifest.xml and then target your app at
  lower versions of Android.)
- Ensure that the JREngage/libs/android-support-v4.jar is added to your IDE's project (ant builds work
  without updating)
- Ensure that the jackson-core-lgpl-1.6.4.jar and jackson-mapper-lgpl-1.6.4.jar libraries are removed from
  your IDE's project.

Using tablet support:
- Tablet support works in two modes, either embedded as a UI Fragment or as a modal dialog.
- To show a modal dialog simply continue to call JREngage#showAuthenticationDialog() or
  JREngage#showSocialPublishingDialog(...)
- To start a Fragment in a specific container call JREngage#showSocialPublishingFragment(...)
- Or, if you wish to manage the Fragment yourself (e.g. to add it to the back stack), use
  JREngage#createSocialPublishingFragment(...)
- Embedded mode requires a host activity sub-classed from android.support.v4.app.FragmentActivity,
  android.app.FragmentActivity is incompatible.
