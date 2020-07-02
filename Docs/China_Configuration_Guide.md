# Engage configuration guide for China
Social login servers deployed in China are restricted in various ways,
leading restricting the behavior of this library too.

This document will explain the differences between the standard  
configuration and the one needed for China.

## The social providers
Standard engage servers depend on the 'realm', which is a subdomain  
established for each Engage property. This process is started by  
retrieving the social providers' configurations through a fixed engage
server url.
China social login servers don't support engage's realm and are hosted  
in different servers with different hostnames.
Some configuration parameters may be provided through the
`janrain_config.json` file to overcome this situation:
* **engageAppUrl** - This is the base social login server url, which
will override the standard `https://rpxnow.com` url.
**applicationId** - This parameter was already mandatory, but it's worth
mentioning that the library is now prepared to attach both when
requesting configurations and when calling the social provider's
start url. Attaching this parameter to the start url overcomes the
lacking of the engage realm.

## Getting the token
Engage supports retrieving both the token and the user profile through  
the same sign-in mechanism which starts by calling the start url. This  
is achieved by using the `signin/device` endpoint as intermediary. This  
workflow is started by adding a `device=android` parameter to the start  
url.
This workflow isn't supported by China social login servers; the  
mechanism supported is passing a `token_url` parameter through the start  
url. After the sign-in is completed the social login server will send a  
redirect to the provided url, which will be handled by the library.

In order to configure these two mechanisms, the library provides two  
parameters which need to be set in the `janrain_config.json` file:
* **engageResponseType** - This parameter will select which workflow will
be used depending on the following values:
  * **token_profile**: This is the default value if **response_type** is
omitted. This value triggers the default workflow which will retrieve
both the token and the user profile.
  * **token**: Triggers the alternate workflow which only retrieves the
token. This is supported both in China and any other country. When using
this response type, the **engageWhitelistedDomain** configuration will
be mandatory.

* **engageResponseType** - This will be mandatory when using the
**token** response type, and it will be ignored otherwise. After a
successful sign in, the social login server will redirect to this url,
so make sure to whitelist the url in the social login application.
As the redirect will be received inside the application, not through a
web browser, this token url will **not** be able to act as a deeplink.

# Capture Configuration Guide
Capture usually provides the flow configuration file through a CDN which
url is handled inside the library, but for China a different CDN may be
used.
This CDN can be configured through the `janrain_config.json` file with
the `downloadFlowUrl`. This is just the base url without any paths
referring to the flow file, usually it's just the schema and the hostname.

# Configuring the same app for both environments
The recommended way to attain this configuration is by providing a  
localized `/res/raw` folder for china, additionally to the default one.
This will lead to having these two configuration files:
* `/res/raw/janrain_config.json`
* `/res/raw-cn/janrain_config.json`

A different way would be to build different flavors, leading to a  
similar behavior by overriding the default configuration file with a
different one for china.
