# Eclipse Import Guide

This will guide you through:

* [Adding the Library to Your Android Eclipse Project](#add-the-library-to-your-android-eclipse-project)
* [Phonegap Integration](#phonegap-integration)
* [Troubleshooting Build and Runtime Errors](#troubleshooting-build-and-runtime-errors)

## Clone the SDK Repository

Clone the Janrain Jump for Android library from GitHub:

    git clone git://github.com/janrain/jump.android.git

## Add the Library to Your Android Eclipse Project

1. Ensure that the [Android ADT](http://developer.android.com/sdk/eclipse-adt.html#installing) plug-in is
   installed.
2. In Eclipse, select `File` -> `Import`.
3. Select `Android` -> `Existing Android Code Into Workspace`, click `Next`.
4. Click `Browse`, browse to `.../jump.android/Jump`, click `Open`.
   Do *not* check `Copy projects into workspace`.
5. Click `Finish`.
6. Select your project in the Package Explorer.
7. From the `Project` menu, select `Properties`.
8. Select `Android` in the left list.
9. In the `Library` section click `Add`.
10. Select `Jump`, click `OK`.
11. Click `Apply`.
12. If your project already includes a copy of the Android Support Library (`android-support-v4.jar`)
    then browse to `.../jump.android/Jump/libs/` and delete `android-support-v4.jar` and skip to step 17
13. Using the already open Properties window for your project, select `Java Build Path` in the left list.
14. Select the `Libraries` tab.
15. Click `Add JARs`.
16. In `Jump/libs/`, select `android-support-v4.jar`, `okhttp-2.2.0.jar`, `okhttp-apache-2.2.0.jar`,
    and `okio-1.2.0.jar` then click `OK`.
17. Open the "Jump" project's Properties window.
17. Set the `Project Build Target` of the Jump Eclipse project to API level 15 or above. (Eclipse may have
    reset the Jump project build target to a lower level.)
18. Your project should now build and run.

**Warning:** Because the Jump for Android library provides Android resources it is not sufficient to add
`jump.jar` to your Eclipse project build path, you must declare the Android library project dependency
as detailed.

**Warning:** There can only be one version of `android-support-v4.jar` in the accumulated set of `Java Build
Path` values for your workspace (although there may be more than one copy of the same version). If you have
a conflict you will need to delete one of the versions from the libs subdirectory of either the Jump SDK or
from your project.

### Phonegap Integration

1.  First, follow steps 1-5 above.
2.  Then, repeat steps 1-5 above for the `.../jump.android/JREngagePhonegapPlugin` folder.
3.  Do as in steps 9 and 10 above, except instead of adding an Android library dependency to `Jump` in steps
    add a library dependency to `JREngagePhonegapPlugin`.
4.  Open the project properties for the JREngagePhonegapPlugin project.
5.  Select `Java Build Path` in the left list.
6.  Select the `Libraries` tab.
7.  Click `Add JARs`.
8.  In `your_project/libs/`, select `cordova-2.3.0.jar`, then click `OK`.
9.  Add the `<activity>` tags as documented in Jump_Integration_Guide.md
10. Add this to the `<plugins>` tag in `your_project/res/config.xml`:
    `<plugin name="JREngagePlugin" value="com.janrain.android.engage.JREngagePhonegapPlugin"/>`

Further Eclipse support is available through the
[Jump for Android support forum](https://support.janrain.com/forums/20122381-android-library-q-a).

## Troubleshooting Build and Runtime errors

* `Error retrieving parent for item`:

    jump.android/Jump/res/values-v11/styles.xml:3: error: Error retrieving parent for item: No
    resource found that matches the given name '@android:style/Theme.Holo.Light.DialogWhenLarge.NoActionBar'.

   Ensure that the project build SDK is Android API level 13 or higher. Note that this setting is different
   than the targeted API level. See the IDE setup instructions, or message us on the
   [Jump for Android support forum](https://support.janrain.com/forums/20122381-android-library-q-a) for
   more help with this.

* `package android.support.v4.app does not exist`

    jump.android/Jump/src/com/janrain/android/engage/ui/JRUiFragment.java
    package android.support.v4.app does not exist

   Ensure that the "android-support-v4" library is defined in your IDE and that it references the
   `jump.android/Jump/libs/android-support-v4.jar` file.

* `package com.janrain.android.engage.net.async does not exist`

  Try cleaning and rebuilding your project. If you are compiling with Eclipse close and restart Eclipse.

## Next

Follow the [JUMP for Android Guide](Jump_Integration_Guide.md)
