# Janrain Android Mobile Libraries Integration Guide

## Upgrading to v7.1.1 from v7.1.0

* NOTE: The library now forces HTTP 1.1 or 2.0 connections using TLS version 1.2 only when communicating to all Janrain Servers
* Library dependencies updated:
  * 'com.android.support:support-v4:27.1.0'
  * files('libs/org.apache.http.legacy.jar')
  * 'com.squareup.okhttp3:okhttp:3.10.0'
  * 'com.squareup.okhttp3:okhttp-apache:3.10.0'
  * 'com.squareup.okio:okio:1.14.0'
  * 'net.openid:appauth:0.7.0'

## Upgrading to v7.1 from v7.0.X

This version now allows the use of the `res/raw/janrain_config.json` file which would be placed in
the application's `res` folder. This file includes the settings for the OpenID AppAuth library that were previously covered in the `/jump.android/Jump/src/res/values/openid_appauth_idp_configs.xml` file.  Please follow these steps to use this file:

* Make sure there is a `res/raw` folder in your app, this may need to be created if it doesn't exist.
* Copy the `/Samples/SimpleDemo/res/raw/janrain_config.json` or the `/Samples/SimpleDemoNative/res/raw/janrain_config.json`
file to the `res/raw` folder.
* Update your janrain credentials and configuration settings in the `janrain_config.json` file.
* Use the new `JumpConfig(Context)` constructor. This constructor loads the configurations from the json file.
* You can now remove all the assignations you had to the JumpConfig object's fields.

At this point you can override the config object's values by assigning them values programmatically.

The `/Samples/SimpleDemo/res/raw/janrain_config.json` file is intended to be used as the template for your own application's configuration file.  Please update the values in this file to reflect your Janrain configuration and settings.

NOTE:  These changes are not mandatory, the libraries should still work using your existing configuration methods as long as they are compatible with previous versions.

## Upgrading to v7.0
* In order to support Google's deprecation of the use of Webviews for web based authentication the mobile libraries have been updated to use Google's preferred OpenID AppAuth for Android Libraries (version 0.5.1 tested) for web based Google authentication (this is different from the native Google Android SDK based authentication that the Mobile Libraries also support).  Other than the required code and configuration changes the end-user experience should not appear to be any different than in previous versions of the mobile libraries.

* Please read the "Docs/Upgrade_Guide.md" for general upgrade guidance.

* Please read the Samples/README.md and the respective sample folder's README.md files for specific upgrade and configuration details as there have been changes since the last release.

- Update the module settings for your project to use the latest Jump sdk files you may need to remove any existing "jump" modules and re-add the latest module in order to ensure your project files are updated.
- Open the `/jump.android/Jump/src/res/values/openid_appauth_idp_configs.xml` file and update the `google_client_id` and `google_auth_redirect_uri` with the appropriate Google application client id that correlates to the Google app that is used in your Engage application.
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

## Upgrading to v6.0

* The only IDE that this release supports and has been tested with is the Android Studio IDE.
* The Android Mobile Libraries have removed all inter-dependencies on the Google, Facebook, and Twitter SDK's and Libraries.  The SimpleDemoNative app has been created to demonstrate how to integrate native provider logon for these providers using their SDK's and Libraries. Please refer to the "Native Authentication Guide" for more information.
* **NOTE:**  Google Play/Sign-On libraries newer than version 8.1 are NOT supported.  Google has changed the oAuth access token provisioning as of version 8.3 and it is no longer compatible with Janrain's API's at the time of this release.  Janrain will be updating their API's to support Google's re-architecture in the future.
* If your previous project had implemented native provider authentication you will have to re-implement this as outlined in the Native Authentication Guide and the "SimpleDemoNative" sample application.
* If you want to use the Janrain Mobile Libraries and Sample Code with the latest Android API levels there is now has dependencies on the deprecated org.apache.http.legacy.jar.  This file is included in the Github repo in the libs folder.  Additional information on this can be found in the build.gradle file.

This guide describes integrating the Janrain User Management Platform into your Android app. This includes
the Capture user registration system. For Engage-only (i.e. social-authentication-only) integrations see
`Engage_Only_Integration_Guide.md`

**Warning** You must have a flow configured with your Capture instance in order to use the Capture library.

## Features

* Engage social sign-in (includes OpenID, and many OAuth identity providers, e.g. Google, Facebook, etc.)
* Sign-in to Capture accounts
    * Either via Engage social sign-in or via traditional username/password sign-in
    * Including the Capture "merge account flow" (which links two social accounts by verified email address
      at sign-in time)
* Capture account record updates
* Capture Account "thin" social registration -- automatic account creation for social sign-in users.

### In the Pipeline

* Profile updates (including password and email address updates)
* In app forgot-password-flow initiation
* Social account linking (like the "merge account" sign-in flow, but after a user is signed in.)
* Session refreshing (sessions currently last one hour)

## 10,000' View

Basic use flow:

1. Gather your configuration details
2. Declare the library project dependency, and add the required elements to your `AndroidManifest.xml` file.
3. Initialize the library
4. Start a sign-in
5. Read and modify the record
6. Send record updates
7. Persist the local user object

## Gather your Configuration Details

Before you begin integrating you will need an array of configuration details:

1. Sign in to your Engage Dashboard - https://rpxnow.com
    1. Configure the social providers you wish to use for authentication ("Deployment" drop down menu ->
       "Engage for Android").
    2. Retrieve your 20-character Engage application ID from the Engage Dashboard (In the right column of
       the "Home" page on the Engage dashboard.)
2. Ask your deployment engineer or account manager for your Capture domain.
3. Create a new Capture API client for your mobile app:
    1. Sign in to the Capture dashboard and provision a new API client for your mobile app

       **Warning** `login_client` is mutually exclusive with all other API client features, which means only
       login clients can be used to sign users in, and only non-login-clients can perform client_id and
       client_secret authenticated Capture API calls. This means that you cannot use the owner client as a
       login client.
       **Warning** If you do not set the write_with_token access schema for your API client to include the
       attributes your client will write to in the its write access schema you will receive
       `missing attribute` errors when attempting to update attributes.
4. Discover your flow settings:
    Ask your deployment engineer for:
        * The name of the Capture "flow" you should use
        * The name of the flow's traditional sign-in form
        * The name of the "locale" in the flow your app will use
          The value for US English is "en-US".
        * The appropriate values for the settings `default_flow_name` and `default_flow_version`.
          (Set these settings for your new API client in the "Settings" section of the Janrain Capture
          dashboard (https://janraincapture.com) )
5. Determine whether your app should use "Thin" social registration, or "two-step" social registration.
6. Determine the name of the traditional sign-in key-attribute (e.g. `email` or `username`)

**Warning** You _must_ create a new API client with the correct `login_client` feature for operation of the
JUMP native mobile libraries.

## Declare and Import

### Declare the Android Library Project Dependency

Using the Android command line tool, from the directory of your project's AndroidManifest.xml:

    android update project -p . -l ../path/to/jump.android/Jump

### Declare the JUMP Activities

Ensure the presence of the `android.permission.INTERNET` permission in your `<uses-permission>` element, and
copy from `.../Jump/AndroidManifest.xml`, adding the following two `<activity>` XML elements, and to your
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

      ...

      </application>

    </manifest>

**Note**: If you wish to target a version of Android lower than 17 you may but this is not supported by Janrain.
To do so, change the `android:targetSdkVersion`, to your desired deployment target. _You must still build against
API 17+ even when targeting a lower API level._ The build SDK used when compiling your project is defined by your
project's local.properties. `android list target` to get a list of targets available in your installation of
the Android SDK. `android update project -p . -t target_name_or_target_installation_id` to update the build
SDK for your project. (Note that this does *not* affect your project's `minSdkVersion` or `targetSdkVersion`.

### Import the Library

Import the following classes:

    import com.janrain.android.Jump;
    import com.janrain.android.capture.Capture;
    import com.janrain.android.capture.CaptureApiError;

## Initialize

Initialize the library by calling `Jump#init` method. For example:

    String engageAppId = "your Engage App ID";
    String captureDomain = "your Capture domain";
    String captureClientId = "your Capture Client ID";
    String captureLocale = "your Capture flow locale";
    String captureSignInFormName = "your Capture sign-in form's name";
    String forgottenPasswordFormName = "your Capture forgot password form name";
    Jump.TraditionalSignInType signInType = Jump.TraditionalSignInType.EMAIL; // or USERNAME
    Jump.init(this, engageAppId, captureDomain, captureClientId, captureLocale, captureSignInFormName,
            signInType);

## Start Sign-In

Once the Jump library has been initialized, your application can start the sign-in flow by calling the
sign-in dialog display method, `com.janrain.android.Jump#showSignInDialog`. You will need to define a
callback handler which implements the `com.janrain.android.Jump.SignInResultHandler` interface. For example:

    private class MySignInResultHandler implements Jump.SignInResultHandler, Jump.SignInCodeHandler {
        public void onSuccess() {
            AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setMessage("Sign-in complete.");
            b.setNeutralButton("Dismiss", null);
            b.show();
        }

        // Part of the com.janrain.android.Jump.SignInCodeHandler interface
        public void onCode(String code) {
            // Do something with the Access Code
        }

        public void onFailure(SignInError error) {
            AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setMessage("error:" + error);
            b.setNeutralButton("Dismiss", null);
            b.show();
        }
    }

Then call `com.janrain.android.Jump#showSignInDialog` with an instance of your callback handler class.

    Jump.showSignInDialog(MainActivity.this, null, new MySignInResultHandler());

Implementing the `com.janrain.android.Jump.SignInCodeHandler` interface is optional. `onCode(String code)` is
called, when the sign in has succeeded, with a Capture OAuth Authorization Code that can be used by a server
side application (e.g. The Capture Drupal Plugin) to retrieve an Access Token.

### Traditional Sign-In and Social Sign-In

The Capture libraries supports both social sign-in via Engage (e.g. Facebook) as well as traditional
sign-in (i.e. username and password or email and password sign-in.) There are three main ways to start
sign-in:

- `Jump.showSignInDialog(Activity, String, SignInResultHandler, String)`: Starts the Engage social sign-in
  process. If the provider argument (the second parameter) is null it sign-in begins by displaying a list of
  all currently configured social sign-in providers, and guiding the user through the authentication.
- `Jump.showSignInDialog(Activity, String, SignInResultHandler, String)`: If called with a non-null string
  for the second argument it proceeds directly to the provider. So, e.g. "facebook" will start a sign-in
  directly with Facebook.
- `Jump.performTraditionalSignIn(String, String, SignInResultHandler, String)`: Starts the traditional
  sign-in flow headlessly (with no user-experience).

The fourth String parameter is the merge token paremeter. It is used in the second step of the "Merge Account
Flow", described below.

### Handling the Merge Account Sign-In Flow

Sometimes a user will have created a record with one means of sign-in (e.g. a traditional username and
password record) and will later attempt to sign-in with a different means (e.g. with Facebook.)

When this happens the sign-in cannot succeed, because there is no Capture record associated with the social
sign-in identity, and the email address from the identity is already in use.

Before being able to sign-in with the social identity, the user must merge the identity into their existing
record. This is called the "Merge Account Flow."

The merge is achieved at the conclusion of a second sign-in flow authenticated by the record's existing
associated identity. The second sign-in is initiated upon the failure of the first sign-in flow, and also
includes a merge token which Capture uses to merge the identity from the first (failed) sign-in into the
record.

Capture libraries event sequence for Merge Account Flow:

 1. User attempts to sign-in with a social identity, "identity F".
 2. Capture sign-in fails because there is an existing Capture record connected to "identity G", which shares
    some constrained attributes with "identity F". E.g. the two identities have the same email address.
 3. The SignInResultHandler has its failure callback invoked, with an error representing this state. This
    case is to be discerned via the isMergeFlowError() method of the contained captureApiError. E.g. like
    `error.reason == SignInError.FailureReason.CAPTURE_API_ERROR &&
    error.captureApiError.isMergeFlowError())`.
 4. The host application (your mobile app) notifies the user of the conflict and advises the user to merge the
    accounts
 5. The user elects to take action
 6. The merge sign-in is started by invoking `com.janrain.android.Jump#showSignInDialog` with a merge token,
    or by invoking `com.janrain.android.capture.Capture.performTraditionalSignIn` with a merge token.
    The existing identity provider of the record is retrieved with
    `error.captureApiError.getExistingAccountIdentityProvider()`, and the merge token with
    `error.captureApiError.getMergeToken()`.

Example:

    public void onFailure(SignInError error) {
        if (error.reason == SignInError.FailureReason.CAPTURE_API_ERROR &&
                error.captureApiError.isMergeFlowError()) {
            final String mergeToken = error.captureApiError.getMergeToken();
            final String existingProvider = error.captureApiError.getExistingAccountIdentityProvider();
            String conflictingIdentityProvider = error.captureApiError.getConflictingIdentityProvider();
            String conflictingIdpNameLocalized = JRProvider.getLocalizedName(conflictingIdentityProvider);
            String existingIdpNameLocalized = JRProvider.getLocalizedName(conflictingIdentityProvider);

            AlertDialog alertDialog = new AlertDialog.Builder(fromActivity)
                    .setTitle("Email Address Conflict")
                    .setCancelable(false)
                    .setMessage("The " + conflictingIdpNameLocalized + " account that you signed in with has"
                            + " the same email address as an existing user. Sign in with " +
                            existingIdpNameLocalized + " to continue.")
                    .setPositiveButton(fromActivity.getString("Merge"),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // When existingProvider == "capture" you can also call ...
                                    //
                                    //     Jump.performTraditionalSignIn(String signInName, String password,
                                    //         final SignInResultHandler handler, final String mergeToken);
                                    //
                                    // ... instead of showSignInDialog if you wish to create the dialog
                                    // and then use the headless API to perform the traditional sign-in.
                                    Jump.showSignInDialog(fromActivity,
                                            existingProvider,
                                            signInResultHandler,
                                            mergeToken);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

This example checks for the merge-flow error, it prompts the user to merge, and it start authentication.

**Note** That the "existing provider" of the merge flow can be "capture" which represents Capture itself.
This case occurs when the merge-failure was a conflict with an existing record created with traditional
sign-in. This case is handled can be handled separately if you wish to create the traditional-sign in UI, or
you can call
`Jump.showSignInDialog(yourActivity, "capture", yourSignInResultHandler, yourMergeTokenFromTheError)`.

### Baseline Merge User-Interface

In order to lower the JUMP Mobile Libraries and Sample Code integration overhead the Mobile Libraries and Sample Code provides a baseline user experience for the
merge account flow. You can use the baseline UX to get started exploring the feature. Later on, you can get
more control over the UX by implementing your own UX, and using the headless APIs provided to interact with
Capture.

To invoke the baseline UX call
`Jump.startDefaultMergeFlowUi(yourActivity, theOriginalSignInError, yourSignInResultHandler)`

## Read and Modify the Account Record

### Capture Schema Basics

Capture user records are defined by the Capture schema, which defines the attributes of the record. An attribute is
either a primitive value (a number, a string, a date, or similar) an object, or a plural.

Primitive attribute values are the actual data that make up your user record. For example, they are your user's
identifier, or their email address, or birthday.

Objects and plurals make up the structure of your user record. For example, in the default Capture schema, the user's
name is represented by an object with six primitive values (strings) used to contain the different parts of the name.
(The six values are `familyName`, `formatted`, `givenName`, `honorificPrefix`, `honorificSuffix`, `middleName`.)
Objects can contain primitive values, sub-objects, or plurals, and those attributes are defined in the schema.

Plurals contain collections of objects. Each element in a plural is an object or another plural. Every element in a
plural has the same set of attributes, which are defined in the schema. Think of a plural as an object that may have
zero-or-more instances.

### Updating the User Profile

Update the properties of your `CaptureRecord` that correspond to the fields of the `editProfileForm` in your
flow. For example: `givenName`, `familyName`, `birthdate`, `aboutMe`, etc.

Call `Capture.updateUserProfile` with the `CaptureRecord` and a `CaptureApiRequestCallback` to update the
user's profile.

Upon a successful update the callback's `onSuccess` method will be called. If the update fails for any reason
the callback's `onFailure` method will be called with an error.

Below is how the SimpleDemo application updates a user's profile.

        try {
            user.put("email", email);
            user.put("displayName", displayName);
            user.put("givenName", firstName);
            user.put("familyName", lastName);
            user.put("aboutMe", about);
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected ", e);
        }

        Capture.updateUserProfile(user, new Capture.CaptureApiRequestCallback() {

            public void onSuccess() {
                Toast.makeText(UpdateProfileActivity.this, "Profile Updated", Toast.LENGTH_LONG).show();
                finish();
            }

            public void onFailure(CaptureApiError error) {
                AlertDialog.Builder adb = new AlertDialog.Builder(UpdateProfileActivity.this);
                adb.setTitle("Error");
                adb.setMessage(error.toString());
                adb.show();
            }
        });

### Read and Modify the Capture Record Model

You can retrieve the signed-in Capture user's account record via `Jump.getSignedInUser()`. The record is
an instance of `org.json.JSONObject`, with some additional methods defined by the `CaptureRecord` subclass.
You can read and write to the record via the usual `JSONObject` methods.

For example, to read the aboutMe attribute in the record:

    Jump.getSignedInUser().optString("aboutMe")

You can make changes too, for example to write to the aboutMe attribute in the record:

    try {
        Jump.getSignedInUser().put("aboutMe", "Bacon, the healthiest of all vegetables.");
    } catch (JSONException e) {
        throw new RuntimeException("Unexpected", e);
    }

Any changes made to the record must still obey the entity type schema from Capture. So, e.g. you cannot add
dynamic new attributes, but you can add additional elements to plurals in the schema.

### Send Record Updates to Capture

To push local changes to the Capture server call `com.janrain.android.capture.CaptureRecord#synchronize`.

## Call the Load and Store Hooks

Because Android can garbage collect processes at its discretion every app must persist its state when it may
leave the foreground, and restore the persisted state when it restarts.

When your Android application starts you must call `com.janrain.android.Jump#loadFromDisk`.

For example, from the SimpleDemo Application object:

    public class SimpleDemoApplication extends Application {
        @Override
        public void onCreate() {
            super.onCreate();

            Jump.loadFromDisk(this);
        }
    }

Whenever your Android application pauses you must call `com.janrain.android.Jump.saveToDisk`.

For example, from MainActivity in SimpleDemo:

    @Override
    protected void onPause() {
        Jump.saveToDisk(this);
        super.onPause();
    }

## Sign the User Out

Call `com.janrain.android.Jump.signOutCaptureUser` to sign the user out.

## Next: Registration

Once you have sign-in and record updates working, see the `User_Registration_Guide.md` for a guide to new user
registration.

## Appearance Customization

### Creating Your Own User Interface

You can create your own traditional sign-in user interface and use
`com.janrain.android.Jump.performTraditionalSignIn` to sign users in.
`com.janrain.android.Jump.showSignInDialog` also takes a provider name parameter which, if supplied, will
direct the library to begin the sign-in flow directly with that provider, skipping the stock list of
sign-in providers user interface.

### Customizing the Stock User Interface

The JUMP for Android Mobile Libraries and Sample Code has an API for appearance customization, and allows for customization through the
Android Theme system as well.

For customizing the look and feel of the sign-in experience, please see
[Custom UI for Android](http://developers.janrain.com/documentation/mobile-libraries/advanced-topics/custom-ui-for-android/).

## Troubleshooting

### Attribute Does Not Exist

    <CaptureApiError code: 223 error: unknown_attribute description: attribute does not exist: /your_attr_name>`

Use [entityType.setAccessSchema](http://developers.janrain.com/documentation/api-methods/capture/entitytype/setaccessschema)
to add write-access to this attribute to your native API client.
