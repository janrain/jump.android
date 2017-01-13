#SimpleDemoNative Sample Application#

Tested withe the following provider SDK versions:

* Facebook Android SDK 4.18.0
* Google Play/Sign-In 8.1
* Fabric.io Twitter 2.3.1

This application is NOT configured with any useful Application or SDK credentials.

Please refer to the Jump Integration Guide and Native Authentication Guide for instructions on configuring the necessary credentials for your Social Login, Registration, and Native identity provider SDK's.

##IMPORTANT
Please read the Docs/Upgrade Guide.md and RELEASE_NOTES before attempting to run these applications.  There are important configuration steps that must be taken before these apps will run.

####To run this demo with your own configuration:

1. Find /jump.android/Samples/SimpleDemo/src/com/janrain/android/simpledemo/SimpleDemoApplication.java
2. Edit the settings in the jumpconfig object to reflect your Social Login and Registration Settings.
3. Edit the settings to include the correct form names as found in your flow file.
4. Update the fabric.properties file with the correct Fabric.io apiSecret.
5. Update the following key in the AndroidManifest.xml file:
```xml
<meta-data android:name="io.fabric.ApiKey" android:value="c62a7de78181c7889143e54a7a33e92adfd1fa63" />
```
6.  Find and update the following values in the MainActivity.java file:
```java
// Note: Your consumer key and secret should be obfuscated in your source code before shipping.
private static final String TWITTER_KEY = "UPDATE";
private static final String TWITTER_SECRET = "UPDATE";
```
7. Update the google-services.json file with the correct Google application settings that correlates to the Google application used by the configured Engage application.
8. Find this file: /jump.android/Samples/SimpleDemoNative/res/values/strings.xml and update the following value with the Facebook App ID that corresponds to the Facebook application used by the configured Engage application:
```xml
<!-- Facebook SDK https://developers.facebook.com/docs/android/getting-started -->
    <string name="facebook_app_id">UPDATE</string>
```


###Typical Misconfiguration Errors###

*Error*:
```
10:54:59.243 [ERROR] [org.gradle.BuildExceptionReporter]
10:54:59.243 [ERROR] [org.gradle.BuildExceptionReporter] FAILURE: Build failed with an exception.
10:54:59.243 [ERROR] [org.gradle.BuildExceptionReporter]
10:54:59.243 [ERROR] [org.gradle.BuildExceptionReporter] * What went wrong:
10:54:59.243 [ERROR] [org.gradle.BuildExceptionReporter] Execution failed for task ':fabricGenerateResourcesDebug'.
10:54:59.243 [ERROR] [org.gradle.BuildExceptionReporter] > Crashlytics Developer Tools error.
10:54:59.243 [ERROR] [org.gradle.BuildExceptionReporter]
10:54:59.243 [ERROR] [org.gradle.BuildExceptionReporter] * Exception is:
10:54:59.243 [ERROR] [org.gradle.BuildExceptionReporter] org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':fabricGenerateResourcesDebug'.
10:54:59.248 [ERROR] [org.gradle.BuildExceptionReporter] Caused by: java.lang.IllegalArgumentException: Crashlytics found an invalid API key: UPDATE.
10:54:59.248 [ERROR] [org.gradle.BuildExceptionReporter] Check the Crashlytics plugin to make sure that the application has been added successfully!
```

*Resolution*: Update the fabric.properties file with the correct Fabric.io apiSecret.
And/Or: Update the following key in the AndroidManifest.xml file:
```xml
<meta-data android:name="io.fabric.ApiKey" android:value="FABRIC_IO_API_KEY" />
```

*Error*:
```
E/Twitter: Failed to get request token
com.twitter.sdk.android.core.TwitterApiException: 401 Authorization Required
at retrofit.RestAdapter$RestHandler.invokeRequest(RestAdapter.java:383)
at retrofit.RestAdapter$RestHandler.access$100(RestAdapter.java:220)
at retrofit.RestAdapter$RestHandler$2.obtainResponse(RestAdapter.java:278)
at retrofit.CallbackRunnable.run(CallbackRunnable.java:42)
at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1133)
at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:607)
at retrofit.Platform$Android$2$1.run(Platform.java:142)
at java.lang.Thread.run(Thread.java:761)
```

*Resolution*: Find and update the following values in the MainActivity.java file:
```java
// Note: Your consumer key and secret should be obfuscated in your source code before shipping.
private static final String TWITTER_KEY = "UPDATE";
private static final String TWITTER_SECRET = "UPDATE";
```

*Error*:
```
E/[com.janrain.android.engage.net.AsyncHttpClient$HttpExecutor.run:193]: HTTP/1.1 400 Bad Request
{"stat": "fail", "err": {"code": 1, "msg": "token does not match the googleplus application registered with Janrain"}}
```

*Resolution*: Update the google-services.json file with the correct Google application settings that correlates to the Google application used by the configured Engage application.

*Error*: Initiating Facebook Native login results in the Facebook interface erroring with `Invalid App ID: UPDATE`

*Resolution*:  Find this file: /jump.android/Samples/SimpleDemoNative/res/values/strings.xml and update the following value with the Facebook App ID that corresponds to the Facebook application used by the configured Engage application:
```xml
<!-- Facebook SDK https://developers.facebook.com/docs/android/getting-started -->
    <string name="facebook_app_id">UPDATE</string>
```


