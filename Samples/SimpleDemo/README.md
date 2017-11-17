# Configuring the SimpleCaptureDemo application.

## IMPORTANT
Please read the Docs/Upgrade Guide.md and RELEASE_NOTES before attempting to run these applications.  There are important configuration steps that must be taken before these apps will run.

#### To run this demo with your own configuration:

*NOTE:* There have been significant updates to the code base in this release.  While over all functionality should not have changed it is highly recommended that thorough testing be performed when upgrading to this version from previous versions.

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

*NOTE:*  These changes are not mandatory, the libraries should still work using your existing configuration methods as long as they are compatible with previous versions.


### Typical Misconfiguration Errors

*Error*:
```
Error:15:38:35.643 [ERROR] [org.gradle.api.Project] [net.openid:appauth:0.4.1] /jump.android.internal/Samples/SimpleDemo/build/intermediates/exploded-aar/net.openid/appauth/0.4.1/AndroidManifest.xml:41:23-64 Error:
	Attribute data@scheme at [net.openid:appauth:0.4.1] AndroidManifest.xml:41:23-64 requires a placeholder substitution but no value for <appAuthRedirectScheme> is provided.
15:38:35.650 [ERROR] [org.gradle.api.Project] /jump.android.internal/Samples/SimpleDemo/AndroidManifest.xml Error:
	Attribute data@scheme at AndroidManifest.xml requires a placeholder substitution but no value for <appAuthRedirectScheme> is provided.
```
*Resolution*:  Add the following to the application's build.gradle file's `defaultConfig` section (make sure to update the scheme to the correct scheme for your Google app's client id:
```json
manifestPlaceholders = [
            'appAuthRedirectScheme': 'com.googleusercontent.apps.YOUR_GOOGLE_APP_CLIENT_ID'
    ]
}
```

*Error*:

```
Error:Execution failed for task ':processDebugManifest'.
> Manifest merger failed : Attribute data@scheme at AndroidManifest.xml requires a placeholder substitution but no value for <appAuthRedirectScheme> is provided.
```

*Resolution*: You need to add the manifest placeholder to your app's build.gradle defaultConfig section.

    // If using web-based (not native) Google authentication.
    // Replace the below string with your own Google client ID. Make sure this is consistent
    // with the values used in openid_appauth_idp_configs.xml
    manifestPlaceholders = [
            'appAuthRedirectScheme': 'com.googleusercontent.apps.UPDATE_WITH_GOOGLE_CLIENT_ID'
    ]



