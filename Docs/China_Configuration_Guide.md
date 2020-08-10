# Social login configuration guide for China
Social login servers deployed in China are restricted in various ways,
which means the behavior of this library is restricted as well.

This document explains the differences between the standard
configuration and the configuration needed for China.

## The social providers
Standard social login servers depend on the "realm", a subdomain
established for each social login property. Typically, the social login
process is started by retrieving the social provider configurations
using a fixed server URL. However, China social login servers don't
support the Social Login realm and, instead, are hosted on different
servers with different hostnames.  
However, a configuration configuration parameter may be provided
through the `janrain_config.json` file to overcome this situation:
* **engageDomain** - This is the base social login server URL, which
overrides the standard `https://rpxnow.com` URL.

## Getting the token
Social login supports retrieving both the token and the user profile
through the same sign-in mechanism (which starts by calling the start
URL). This workflow isn't supported by China social login servers, which
can provide the token but not the user profile data.

In order to configure these two mechanisms, the library provides two
parameters which must be set in the `janrain_config.json` file:
* **engageResponseType** - This parameter selects the workflow to
be used depending on the following values:
  * **token_profile**: The default value if **response_type** is
omitted. This value triggers the default workflow, which retrieves
both the token and the user profile.
  * **token**: Triggers the alternate workflow, which only retrieves the
token. This is supported in China and in all other regions. When
using this response type, the **engageWhitelistedDomain** configuration
is mandatory.

* **engageWhitelistedDomain** - This is mandatory when using the
**token** response type, and is otherwise ignored. After a successful
sign in the social login server redirects to this URL, so be sure
to allow-list the URL in the social login application.
As the redirect is received inside the application and not through a
web browser, this token URL **cannot** act as a deeplink.

# Capture configuration guide
Capture usually provides the flow configuration file through a CDN whose
URL is handled inside the library, but for China a different CDN can be
used.
This CDN can be configured through the `janrain_config.json` file with
the `captureFlowDomain` parameter. This is just the base URL without
any paths referring to the flow file; instead, it's usually just the
schema and the hostname.

# Configuring the same app for both environments
There are three recommended ways to obtain this configuration:

1. Provide a localized `/res/raw` folder for China (ie. `/res/raw-zh`)
in addition to the default `/res/raw` folder. This results in two
configuration files:
    * `/res/raw/janrain_config.json`
    * `/res/raw-zh/janrain_config.json`

2. Build with different flavors. This leads to a similar behavior by
overriding the default configuration file with a different one for
China.

3. Override the configuration file in the JanrainConfig constructor:
    ```
    JumpConfig jumpConfig = new JumpConfig(this, R.raw.custom_janrain_config);
    Jump.init(this, jumpConfig); 
    ```

