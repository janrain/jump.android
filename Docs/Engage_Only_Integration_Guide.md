# Engage-Only Integration Guide

This guide describes integrating Engage-only into your android App. For a description of integration steps
for the JUMP platform see `Jump_Integration_Guide.md`.

## 10,000' View

1. Gather configuration details
2. Add the necessary library dependency to your project
3. Declare the library project dependency
4. Add library required activities permissions and to your `AndroidManifest.xml` file.
5. Import and initialize the library.
6. Begin authentication by calling one of the two `show...Dialog methods`.
7. Receive your authentication token URL's response in JREngageDelegate#jrAuthenticationDidReachTokenUrl()

## Gather Configuration Details

Configure your desired set of social identity providers in the Engage Dashboard. There is a provider
configuration guide here:

    http://developers.janrain.com/overview/social-login/implementing-social-login/

### Configure the Providers Used in the Android Library

Once the providers themselves are configured they need to be enabled explicitly for the the native Android
Engage Library.

While signed in to the Engage dashboard go to the 'Engage for Android' configuration wizard (in the drop-down
menus, under Deployment -> 'Engage for Android'). Follow the wizard to configure the providers used
for authentication and social sharing from the Android library.

### Retrieve your Engage Application ID

You will also need your 20-character Application ID from the Engage Dashboard. Click the `Home` link int the Engage
dashboard and you will find your app ID in the right-most column towards the bottom of the colum under the "Application
Info" header.

## Declare the Library Dependency

If you are using Eclipse, see the Eclipse_Import_Guide.md. If you are using IntelliJ or Android Studio it's
way easier (import the Jump module and add a module dependency to it.)

If you are using the ant build tool chain the library just use
`android update project -p path/to/your/prj -l path/to/jump.android/Jump`.

## Declare the JUMP Activities

Ensure the declaration of the `android.permission.INTERNET` permission in your `<uses-permission>` element,
and copy from `.../Jump/AndroidManifest.xml`, adding the following two `<activity>` XML elements, and to your
project's `AndroidManifest.xml` file:

    <manifest xmlns:android="http://schemas.android.com/apk/res/android" ... >

      <uses-permission android:name="android.permission.INTERNET" />
      <uses-sdk android:minSdkVersion="4" android:targetSdkVersion="13" />

      ...

      <application ... >

      ...


        <!-- The following activities are for the Janrain Engage for Android library -->
        <!-- This activity must have a dialog theme such as Theme.Holo.Dialog, or
            Theme.Dialog, etc.

            Using android:theme="@style/Theme.Janrain.Dialog" will result in Theme.Dialog on API 4-10 and
            Theme.Holo.DialogWhenLarge.NoActionBar on API 11+
        -->
        <activity
            android:name="com.janrain.android.engage.ui.JRFragmentHostActivity"
            android:configChanges="orientation|screenSize"
            android:theme="@style/Theme.Janrain.Dialog.Light"
            android:windowSoftInputMode="adjustResize|stateHidden"
            />

        <!-- This activity must have a normal (non-dialog) theme such as Theme, Theme.Light, Theme.Holo, etc.

            Using android:theme="@style/Theme.Janrain" or "@style/Theme.Janrain.Light" will result in
            Theme (or Theme.Light) on API 4-10 and
            Theme.Holo (or Theme.Holo.Light) on API 11+
        -->
        <activity
            android:name="com.janrain.android.engage.ui.JRFragmentHostActivity$Fullscreen"
            android:configChanges="orientation|screenSize"
            android:theme="@style/Theme.Janrain.Light"
            android:windowSoftInputMode="adjustResize|stateHidden"
            />
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
        <activity
            android:name="com.janrain.android.engage.OpenIDAppAuthCancelledActivity"
            android:label="REPLACE_WITH_YOUR_APPNAME_AS_NEEDED"
            android:theme="@style/Theme.Janrain.Dialog.Light"
            android:windowSoftInputMode="stateHidden">
        </activity>

      ...

      </application>

    </manifest>

Note: If you wish to target a version of Android lower than 13 (which is 3.2) you *can*. To do so, change the
`android:targetSdkVersion`, to your desired deployment target. _You must still build against API 13+
even when targeting a lower API level._ The build SDK used when compiling your project is defined by your
project's local.properties. `android list target` to get a list of targets available in your installation of
the Android SDK. `android update project -p path/to/project -t target_name_or_target_installation_id` to
update the build target SDK for your project. (Note that this does *not* affect your project's
`minSdkVersion` or `targetSdkVersion`.)

Open the '/jump.android/Jump/src/res/values/openid_appauth_idp_configs.xml' file and update the `google_client_id` and `google_auth_redirect_uri` with the appropriate Google application client id that correlates to the Google app that is used in your Engage application.
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

## Import and Initialize

Import the following classes:

    import com.janrain.android.engage.JREngage;
    import com.janrain.android.engage.JREngageDelegate;
    import com.janrain.android.engage.JREngageError;
    import com.janrain.android.engage.net.async.HttpResponseHeaders;
    import com.janrain.android.engage.types.JRActivityObject;
    import com.janrain.android.engage.types.JRDictionary;
    import net.openid.appauth.AppAuthConfiguration;
    import net.openid.appauth.AuthorizationService;
    import net.openid.appauth.browser.BrowserBlacklist;
    import net.openid.appauth.browser.Browsers;
    import net.openid.appauth.browser.VersionRange;
    import net.openid.appauth.browser.VersionedBrowserMatcher;



Interaction begins by calling the `JREngage.initInstance` method, which returns the `JREngage` object:

    private static final String ENGAGE_APP_ID = "";
    private static final String ENGAGE_TOKEN_URL = "";
    private JREngage mEngage;
    private JREngageDelegate mEngageDelegate = ...;

    ...

    mEngage = JREngage.initInstance(this.getApplicationContext(), ENGAGE_APP_ID, ENGAGE_TOKEN_URL, this);

[initInstance]
takes four arguments, `context`, `appId`, `tokenUrl`, and `delegate`:

- `context` — Your Android application Context.
- `appId` — Your Engage application ID (found on the Engage Dashboard).
- `tokenUrl` — Your web server's authentication token URL.
- `delegate` — An implementation of the `JREngageDelegate` interface through which you will receive callbacks
  from the library.

### Choosing your Engage Delegate Class

Select a class you will use to receive callbacks from the Engage library. This is called your Engage
delegate. The delegate should be a singleton object. A good place to start is your app's Android Application
class, if you have one. Activities which are always at the root of the back stack can be acceptable choices.
Avoid using Activities which will short lived.

## Social Sign-In

An Engage authentication is meaningful in the context of authenticating your mobile app *to* something.
If you are unsure of what your users should be authenticating to, then Janrain Capture may be a great choice.
(Capture signs authenticating users into your copy of their social profile, affording your mobile app a
place to store and retrieve data from, and a pier from which to build out a service.)

Once the `JREngage` object has been initialized, start authentication by calling
[showAuthenticationDialog] after setting the AuthorizationService and AuthorizationActivity.
method:

```
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
      mEngage.showAuthenticationDialog(fromActivity, TradSignInUi.class);

```

You will receive your authentication token URL's response in the jrAuthenticationDidReachTokenUrl method.
When received you will have access to the body of the response, as well as the headers, which frequently
contain session cookies used to coordinate the app's session with your web server. Parsing your
authentication token URL's response for session establishing information, or retrieving session cookies from
the header, is your app's responsibility.

For guidance implementing your web-server's authentication token URL, see `Authentication-Token-URL.md`.

## Social Sharing

If you want to share an activity, first create an instance of the
[`JRActivityObject`]:

    String activityText = "added JREngage to her Android application!";
    String activityLink = "http://janrain.com";

    JRActivityObject jrActivity = new JRActivityObject(activityText, activityLink);

Populate the new object with information about the activity being shared by. Here, an exciting Facebook
"action link" is added:

    activityObject.addActionLink(new JRActionLink("Download the Quick Share demo!",
          "https://market.android.com/details?id=com.janrain.android.quickshare");

Then pass the activity to the
[`showSocialPublishingDialogWithActivity`]:

    mEngage.showSocialPublishingDialogWithActivity(jrActivity);

### Upgrading to v7.0 or later

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
 <activity
    android:name="com.janrain.android.engage.OpenIDAppAuthCancelledActivity"
    android:label="REPLACE_WITH_YOUR_APPNAME_AS_NEEDED"
    android:theme="@style/Theme.Janrain.Dialog.Light"
    android:windowSoftInputMode="stateHidden">
</activity>
```

- Add the following initialization tasks to set the AuthorizationService and AuthorizationActivity before calling the [showAuthenticationDialog] method.
method:

    AuthorizationService authorizationService = new AuthorizationService(MainActivity.this);
    mEngage.setAuthorizationService(authorizationService);
    mEngage.setAuthorizationActivity(MainActivity.this);
    mEngage.showAuthenticationDialog();


