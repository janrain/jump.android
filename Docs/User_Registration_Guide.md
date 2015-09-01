# JUMP User Registration Guide

This guide describes use of the user registration feature in the JUMP Mobile Libraries and Sample Code. This guide is a follow-on to
`Jump_Integration_Guide.md`, which describes the fundamentals of the integration process.

## Registration Types

There are three types of user registration:

* "Traditional" registration. This is registration as traditionally implemented. The user fills out a form,
  and the fields of the form are submitted via a JUMP platform API, which registers the user.
* "Thin" social registration. This is automatic registration based on social identities. It is only used in
  the context of a authentication with a social identity. No form is presented to the user. The user's social
  identity is used to populate the fields of a new user record if one does not already exist for their social
  identity identifier URL.
* "Two-step" social registration. This is a registration form with pre-populated values from the user's social
  identity. The first step is "the user authenticates with a social identity, but no user record is found so a
  pre-populated form is returned to the user." The second step is "the user submits the registration form."

## Thin Registration

Thin registration is enabled or disabled at the time that you configure the Capture library, via the
`captureEnableThinRegistration` field in the `JumpConfig` instance used to initialize the JUMP library.
Set to `true` to enable thin registration. No further configuration is required. When thin registration is
possible it will be performed by Capture automatically and the user will be signed in simultaneously.

### Thin Registration Requirements

For thin registration to succeed all Capture schema constraints and rules must be fulfillable with the social
profile. (See
http://developers.janrain.com/documentation/api-methods/capture/entitytype/setattributeconstraints/ and
http://developers.janrain.com/documentation/api-methods/capture/entitytype/rules/ ) If a constraint or rule
cannot be met then thin registration will not occur, and the JUMP library will return an instance of
`com.janrain.android.Jump.SignInResultHandler.SignInError#SignInError` with an included
`com.janrain.android.capture.CaptureApiError`, which will have an error code of `310`, (==
`com.janrain.android.capture.CaptureApiError#RECORD_NOT_FOUND`) For example, if there is the schema
constraint of `['required']` on the `/email` attribute of your schema, then users attempting to sign-in with
Twitter (which does not profile an email address in it's social profiles) will not be able to thin-register.

## Traditional Registration

To perform traditional registration first instantiate a new `JSONObject`:

    JSONObject newUser = new JSONObject();

Then, present a registration form of your own creation to the user, allow them to fill in values. There
should be a field in your form for each of the fields in your traditional registration form in your flow.
(You can look in your flow directly, or ask your deployment engineer for a list of these fields.)

For example, for the default Capture schema and the standard user registration flow, display 5 text fields,
one each, for the following attributes in the default Capture schema:

* /email
* /displayName
* /firstName
* /lastName
* /password

When the user submits the form then copy the text field values into corresponding attributes in `newUser`.
Once the user object is populated with values then call `com.janrain.android.Jump#registerNewUser`. Use
`null` for the social registration token.

Upon completion a method will be invoked on your `com.janrain.android.Jump.SignInResultHandler`.
`onSuccess()` is invoked for successful registrations, `onFailure(SignInError)` is invoked for failures.
Form validation failures are communicated to your application as failures.

### Handling Form Validation Failures

Capture performs server side form validation on all forms. If there is s validation error then your result
handler receive a failure method invocation. The error received in that method can be inspected, and form
validation errors can be differentiated from other (e.g. networking) errors and presented to the user.

To detect a form validation error use `com.janrain.android.capture.CaptureApiError#isFormValidationError`.
A validation error, once detected, can be further inspected by calling
`com.janrain.android.capture.CaptureApiError.getLocalizedValidationErrorMessages`. This method returns a
map of form-field-names to lists-of-localized-error-messages-for-that-field. For example, it might return a
map like:

    {
        "displayName": ["Display name is required."],
        "email": ["Email address is not formatted correctly."],
        "password": ["Password is required."]
    }

The localized validation failure messages will be localized in accordance with the locale used to configure
the JUMP library and with the translations defined in your flow.

## "Two-Step" Social Registration

Two-step social registration is the composition of a failed social sign-in and a registration. It is started
via the social sign-in API first, with a follow-on call to the registration API after the sign-in fails.

To perform a social registration first the user is run through the social sign-in API. Inspect the error
received in `SignInResultHandler#onFailure(SignInError error)` with
`error.captureApiError.isTwoStepRegFlowError().`. If `true`, then the sign-in error can be recovered by
performing a social registration. The error received will also provide a pre-populated user object which
should be used to pre-populate the social registration form.

### Two-Step Implementation

Two-step social registration is performed similarly to traditional registration, as described above, but with the
addition of a social registration token in the call to `com.janrain.android.Jump#registerNewUser`.

The social registration token is retrieved from the sign-in error received in your
`SignInResultHandler#onFailure` method, with the `CaptureApiError#getSocialRegistrationToken` method, and the
pre-populated user object can be retrieved with `CaptureApiError#getPreregistrationRecord`.

With the pre-populated user record display a form with fields pre-populated by the properties of the
pre-registration record, and pass the social registration token in with the registration message.

Social registration form validation errors are handled in the same way as traditional registration form
validation errors.

## Forgotten Passwords

When a user submits an incorrect email address or password in a traditional registration sign-in form, they
are given the option to create new password. The AlertDialog that informs the user of the failed sign-in has
a button to begin the forgotten password flow. Once the user taps "Forgot Password," another AlertDialog is
presented asking the user to confirm their email address. If the user taps "Send," then an API call will be
made to capture that will trigger an email with instructions on how the user can reset their password.

If you would like to trigger the forgotten password flow directly call -

    Capture.performForgotPassword(emailAddress, new Capture.ForgotPasswordResultHandler() {..});


## Example

In `SimpleDemo` see the `MainActivity` for an example of the `SignInResultHandler`, and see
`RegistrationActivity` for an example of a registration form (used for both traditional and social
registration.)
