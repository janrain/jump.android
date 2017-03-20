# Configuring the SimpleCaptureDemo application.

##IMPORTANT
Please read the Docs/Upgrade Guide.md and RELEASE_NOTES before attempting to run these applications.  There are important configuration steps that must be taken before these apps will run.

####To run this demo with your own configuration:

1. Find /jump.android/Samples/EngageOnly/src/com/janrain/android/engageonly/EngageOnlyApplication.java
2. Edit the settings in the jumpconfig object to reflect your Social Login and Registration Settings.
3. Edit the settings to include the correct form names as found in your flow file.



###Typical Misconfiguration Errors###

*Error*:
```
Error:15:38:35.643 [ERROR] [org.gradle.api.Project] [net.openid:appauth:0.4.1] /jump.android.internal/Samples/EngageOnly/build/intermediates/exploded-aar/net.openid/appauth/0.4.1/AndroidManifest.xml:41:23-64 Error:
	Attribute data@scheme at [net.openid:appauth:0.4.1] AndroidManifest.xml:41:23-64 requires a placeholder substitution but no value for <appAuthRedirectScheme> is provided.
15:38:35.650 [ERROR] [org.gradle.api.Project] /jump.android.internal/Samples/EngageOnly/AndroidManifest.xml Error:
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



