v7.1.1

* Google Sign In (Native sample app tested with Sign-In SDK Services version 3.1.2 and play-services-auth version 11.8.0).  NOTE: The Native sample app has been modified to work with the newer Google Sign-In Android SDK's.  It is no longer compatible with the Google+ Android SDK's.  If you want to use the older Google+ Android SDK's for native authentication please refer to a previous release.  The supporting methods for the older SDK's are still in place and should work.
  1. For Google Sign-In use: `Jump.startCodeAuthForNativeProvider(activity, "googleplus", authCode,"",activity.signInResultHandler,"");`
  1. For Google+ use: `Jump.startTokenAuthForNativeProvider(activity, "googleplus", googleToken,"",activity.signInResultHandler,"");`
* Facebook (Native sample app tested with Facebook Android SDK version 4.31.1 as per facebook's own documentation)
* Twitter (Native sample app tested Twitter SDK version 3.1.1)

v7.1

* Project files upgraded to Android Studio 3 (Also tested with Android Studio 2.3)
* Facebook (Native sample app tested with Facebook Android SDK version 4.+ as per facebook's own documentation)
* Google+ (Native sample app tested with Play/Sign-In SDK Services version 3.1.0 and Play Auth version 11.8.0)
* Twitter (Native sample app tested Twitter SDK version 3.1.1)
* Bug fixes to several library files to address crashes from null values.
* The sample apps have been heavily refactored.  They now load the library files from the top-level "Jump" folder in the repo instead of copying them to the app folder.
* The sample apps now optionally use a json configuration file loaded from the application's `res\raw\janrain_config.json` file.  The original method for intiallizing the libraries is still supported.
* The sample apps have been updated to use the latest version of the Janrain "standard" flow file.
* The sample app registration and profile forms have been updated to include all the form fields from the "standard" flow.  This includes methods for populating date-pickers and selection spinners/pickers.


v7.0.5

* Potentially BREAKING CHANGE: If you are using the "capture/CaptureRecord.java" file's "hasPassowrd()" method to determine if a user record is social only then the logic has been corrected.  "hasPassword()" now returns a Boolean true if the user record contains a password value other than null.
* Added code and examples to support the change password functionality of flow forms.  This includes a new method that allow the developer to build up a simple Map of flow form names and values and submit the form to any Janrain "oauth/" compatible endpoint.  This method does no validation that the submitted data is accurate for the flow form configuration.  All validation of the submitted data and error reporting will be done server-side.
```
public static CaptureApiConnection updateUserProfileWithFormFieldsProvided(Map<String, String> fieldMap,
                                                                               String oauthEndpoint,
                                                                               String captureFlowFormName,
                                                                               String accessToken,
                                                                               final CaptureApiRequestCallback handler)
```
* Improved LinkAccount click behavior in the sample apps.
* Improved error message parsing in several areas.
* Resolved Twitter cancel-return webview display bug.
* Added code to avoid null pointer exceptions with certain Samsung browsers during the OpenID AppAuth process.

v7.0.4

* Resolved notification issue where the user cancelled the OpenID AppAuth process by using the back button or closing the browser and the mobile libraries weren't notified.  NOTE:  This change requires adding an activity to your AndroidManifest.xml.  Please refer to the Docs/Upgrade_Guide.md for more assistance.
* Added extra verification for null/empty engageAppUrl settings.
* Updated sample applications to use the 0.5.1 version of the OpenID AppAuth Android libraries

v7.0.3

* OpenID AppAuth library activity and service connections are now bound to the activity from which the main Jump libraries are invoked from (MainActivity in the SimpleDemo application).
* Removed JROpenIDAppAuthFragment.java class.
* Resolved issues around back button or OpenID AppAuth cancellation behavior.
* Improved error handling when the user denies access to the OpenID AppAuth permissions/scopes request.
* Added an example of invoking direct Engage authentication (Google) through the SimpleDemo main UI without needing to invoke the Engage Provider List Fragment.
* There should be no code changes required for the integrating developer/engineer from the 7.0.2 release (unless they have made other library modifications that relate to the above behaviors)

v7.0.2

* Bug fixes around Activity creation for the App Auth module.
* Documentation update
* Catch error when signInForm is not defined or spelled incorrectly.

v7.0.1

* Resolved compatibility issues with certain Android versions and configurations.

v7.0.0

* Implemented OpenID AppAuth Libraries to support Google WebView Deprecation.
* Added support for non-standard Engage server url's
* Added support for custom flow download location url's
* Added support for Native Authentication using the WeChat application
* Updated sample applications as needed to implement new changes.
* Updated SimpleDemoNative sample app to use Facebook SDK version 4.18.0 and Twitter SDK version 2.3.1

v6.0.1

* Added missing credentials to sample app

v6.0.0

* The only IDE that this release supports and has been tested with is the Android Studio IDE.
* The Android Mobile Libraries have removed all inter-dependencies on the Google, Facebook, and Twitter SDK's and Libraries.  The SimpleDemoNative app has been created to demonstrate how to integrate native provider logon for these providers using their SDK's and Libraries.  NOTE:  Google Play/Sign-On libraries newer than version 8.1 are NOT supported.  Google has changed the oAuth access token provisioning as of version 8.3 and it is no longer compatible with Janrain's API's at the time of this release.  Janrain will be updating their API's to support Google's re-architecture in the future.
* Resolved an issue with the CaptureJsonUtils.java file where customers with large amounts of user records would overflow the Integer data type.
* If you want to use the Janrain Mobile Libraries and Sample Code with the latest Android API levels there is now has dependencies on the deprecated org.apache.http.legacy.jar.  This file is included in the Github repo in the libs folder.  Additional information on this can be found in the build.gradle file.
* Added support for native Twitter authentication
* Resolved an issue with Facebook Sharing previews not updating properly in the sharing dialog.
* Updated logos for PayPal, Yahoo, and Instagram
* Demonstrated how to included required legacy apache libraries for use in newer versions of Android.
* Implemented the ability to set the RedirectUri value for API calls.
* Added Auth_info data to the merge account callback mechanism
* Implemented the return of the original user info during a failed synchronization of a user record.

v5.0.1

* The Janrain Mobile Libraries and Sample Code now has dependencies on OKHttp. Follow the steps in Eclipse_Import_Guide.md or IntelliJ_Import_Guide.md no add the necessary Jars to your project.

v5.0.0

 * Updated Mobile Libraries and Sample Code for Android 5.0 compatability
 * Fixed issue with Twitter login leading to blank screen
 * Added method to refresh capture record with what is on the server
 * Known issue with Atom x64 (in emulation and potentially with devices utilizing this processor which is currently only the Lenovo P90), with a potential crash during Facebook share attempts

v4.7.1

 * Fixed issue with Native Google+ where Google+ UI would be invoked multiple times
   upon leaving and entering the app
 * Fixed issue with Native Google+ where back button would not back out of the
   "Add a Google Account" screen.
 * Fixed crash when immediately pressing back after invoking Native Google+

v4.7.0

 * User landing screen now uses square provider icons
 * Added broadcasts for when the flow succeeds or fails to downlaod
 * Removed unused Backplane support
 * Fixed crash when signing in with a native provider and no network connection


