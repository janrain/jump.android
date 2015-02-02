**NOTE:** The documentation on this site is no longer being updated. For current Mobile documentation,
refer to http://developers.janrain.com/.

Welcome to the JUMP platform libary for Android.  This library is available open-source under a Berkeley
license, as found in the LICENSE file.

Getting Started:
    Docs/Jump_Integration_Guide.md

Phonegap Plugin:
    http://developers.janrain.com/documentation/mobile-libraries/phonegapcordova/

Report bugs or ask questions:
    https://support.janrain.com/forums/20122381-android-library-q-a

Old git repo: https://github.com/janrain/engage.android
New git repo: https://github.com/janrain/jump.android
(Both are exactly the same, for now.)

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

