Welcome to the JUMP platform libary for Android.  This library is available open-source under a Berkeley
license, as found in the LICENSE file.

Getting Started:
    Docs/Jump_Integration_Guide.md

Report bugs or ask questions:
    https://support.janrain.com

### Upgrading to v6.0

- IMPORTANT: This is the last release of this form of the Janrain Android Mobile Libraries.  Other than major bug fixes or compatibility updates no further implementations will be released.  A new Android Sample Application will be written using more modern Android tools and libraries.
- The only IDE that this release supports and has been tested with is the Android Studio IDE.
- The Android Mobile Libraries have removed all inter-dependencies on the Google, Facebook, and Twitter SDK's and Libraries.  The SimpleDemoNative app has been created to demonstrate how to integrate native provider logon for these providers using their SDK's and Libraries.  NOTE:  Google Play/Sign-On libraries newer than version 8.1 are NOT supported.  Google has changed the oAuth access token provisioning as of version 8.3 and it is no longer compatible with Janrain's API's at the time of this release.  Janrain will be updating their API's to support Google's re-architecture in the future.
- If your previous project had implemented native provider authentication you will have to re-implement this as outlined in the Native Authentication Guide and the "SimpleDemoNative" sample application.
- If you want to use the Janrain Mobile Libraries and Sample Code with the latest Android API levels there is now has dependencies on the deprecated org.apache.http.legacy.jar.  This file is included in the Github repo in the libs folder.  Additional information on this can be found in the build.gradle file.


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

