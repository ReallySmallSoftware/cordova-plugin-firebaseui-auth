cordova-plugin-firebaseui-auth
==
A FirebaseUI Auth plugin to enable easy authentication using a number of different providers.

What is FirebaseUI?
==
From the documentation (https://opensource.google.com/projects/firebaseui):

> A UI library for Firebase, including binding for the realtime database, authentication and storage.

Supported platforms
==
This plugin supports the following platforms:

- Android
- iOS
- Browser

Support providers
==
The following providers are currently supported:

- Google
- Facebook
- Email

Follow the instructions for each of the platforms and providers you wish you use in order to add firebase to your app:

- https://firebase.google.com/docs/android/setup?authuser=0#manually_add_firebase
- https://firebase.google.com/docs/ios/setup?authuser=0#manually_add_firebase
- https://developers.facebook.com/

Installation
==

```
cordova plugin add cordova-plugin-firebaseui-auth --variable ANDROID_FIREBASE_VERSION=11.6.0
    --variable FACEBOOK_APPLICATION_ID=12345678
    --variable FACEBOOK_DISPLAY_NAME="My application"
    --variable REVERSED_CLIENT_ID="com.googleusercontent.apps.262373026581-6jso41abne2jnlqhho4861hk41nsxvbe"
    --variable COLOR_PRIMARY="#ffffff"usercontent.apps.262373026581-6jso41abne2jnlqhho4861hk41nsxvbe"
    --variable COLOR_PRIMARY="#ffffff"
    --variable COLOR_DARK_PRIMARY="#555555"
    --variable COLOR_LIGHT_PRIMARY="#aaaaaa"
    --variable COLOR_ACCENT="#7C4DFF"
    --variable COLOR_SECONDARY="#FFC107"
    --variable COLOR_CONTROL="#ffffff"
    --variable COLOR_BACKGROUND="#000000"
```

or

```
phonegap plugin add cordova-plugin-firebaseui-auth  --variable ANDROID_FIREBASE_VERSION=11.6.0
    --variable FACEBOOK_APPLICATION_ID=12345678
    --variable FACEBOOK_DISPLAY_NAME="My application"
    --variable REVERSED_CLIENT_ID="com.googleusercontent.apps.262373026581-6jso41abne2jnlqhho4861hk41nsxvbe"
    --variable COLOR_PRIMARY="#ffffff"
    --variable COLOR_DARK_PRIMARY="#555555"
    --variable COLOR_LIGHT_PRIMARY="#aaaaaa"
    --variable COLOR_ACCENT="#7C4DFF"
    --variable COLOR_SECONDARY="#FFC107"
    --variable COLOR_CONTROL="#ffffff"
    --variable COLOR_BACKGROUND="#000000"
```

Any variables that are not supplied will use default values. For credential based variables (FACEBOOK_APPLICATION_ID, REVERSED_CLIENT_ID and REVERSED_CLIENT_ID) this will result in the provider not working until you supply real values.

Not all variables are relevant to all providers:

- ANDROID_FIREBASE_VERSION: the version of Firebase to use. This changes regularly and mismatching the version used by different plugins can cause build issues, so be aware
- FACEBOOK_APPLICATION_ID: Facebook only - the application id if your application
- FACEBOOK_DISPLAY_NAME: Facebook only - the display name of your application - only used by iOS
- REVERSED_CLIENT_ID: Google only - the reversed client Id which can be found in your GoogleService-Info.plist file - only used by iOS
- COLOR_PRIMARY: Used to style the UI - only used by Android
- COLOR_DARK_PRIMARY: Used to style the UI - only used by Android
- COLOR_LIGHT_PRIMARY: Used to style the UI - only used by Android
- COLOR_ACCENT: Used to style the UI - only used by Android
- COLOR_SECONDARY: Used to style the UI - only used by Android
- COLOR_CONTROL: Used to style the UI - only used by Android
- COLOR_BACKGROUND: Used to style the UI - only used by Android

Firebase configuration
--
Android
--
You must ensure that `google-services.json` is put in the correct location. This can be achieved using the following in your `config.xml`:

```
<platform name="android">
    <resource-file src="google-services.json" target="google-services.json" />
</platform>
```
iOS
--
iOS requires `GoogleService-Info.plist` is put in the correct location. Similarly this can be done as follws:
```
<platform name="ios">
    <resource-file src="GoogleService-Info.plist" />
</platform>
```

Dependencies
==
In order for FirebaseUI to work on Android the default Cordova MainActivity needs to be replaced with a class that inherits from a FragmentActivity.

This plugin therefore depends on the `cordova-plugin-android-fragmentactivity` plugin to enable this.

Getting started
==
This guide will assume familiarity with Firebase and FirebaseUI.

Create a new FirebaseAuthUI instance:

```
   FirebaseUIAuth.initialise({
      "providers": ["GOOGLE", "FACEBOOK", "EMAIL"],
      "tosUrl" : "http://www.myapp.co.uk/terms.html",
      "privacyPolicyUrl" : "http://www.myapp.co.uk/privacy.html",
      "theme" : "themeName",
      "logo" : "logoName",
      "uiElement" : "#mywebelement",
      "anonymous" : true|false
    }).then(function(firebaseUIAuth) {
      myfirebaseUIAuthInstance = firebaseUIAuth;
    });
```

This is initialised as a promise to allow the Browser implementation to dynamically add a reference to the FirestoreUI Javascript SDK.

Not all of the above options will function on all platforms:

- providers: a list of authentication providers to use
- tosUrl: a terms of services URL when signing up via email
- privacyPolicyUrl: a privacy policy URL - Android only
- theme: a theme identifier for styling - Android only
- logo: a logo to display - Android only
- uiElement: a jQuery selector for web login - Web only
- anonymous : if true log in an an anonymous user upon initialisation

Methods
==

signIn()
--
Call this to start the sign in process based on the above configuration. This can raise the following events:

signinsuccess
--
The user has signed in successfully. The following data is returned:

```
{
  name: 'user display name',
  email: 'user email',
  emailVerified: true | false,
  photoUrl: 'url of user image if available',
  id: <user id>
}
```

signinfailure
--
Sign in failed for some reason. The following is returned:

```
{
  code: <failure code>,
  message: 'failure message'
}
```

signOut()
--
Sign the current user out of the application. This can raise the following events:

signoutsuccess
--
The user has signed out successfully.

signoutfailure
--
Sign out failed for some reason.


getToken(success, failure)
--
Get an access token. `success` will be invoked with the returned token if it was found.

What platform configuration is carried out?
==

For iOS and Android a number of platform files are added or updated based on the supplied configuration.

Android
--

`res/values/strings.xml`
This has the following added:

- facebook_application_id
- facebook_login_protocol_scheme

`res/values/color.xml`
This is either created or updated with the colour definitions supplied.

`res/values/styles.xml`
This is either created or updated with a style that uses the above colour definitions.

iOS
--

`*-Info.plist`

The following keys are added:

```
<key>CFBundleURLTypes</key>
<array>
  <dict>
    <key>CFBundleTypeRole</key>
    <string>Editor</string>
    <key>CFBundleURLName</key>
    <string>REVERSED_CLIENT_ID</string>
    <key>CFBundleURLSchemes</key>
    <array>
      <string>$REVERSED_CLIENT_ID</string>
    </array>
    <key>CFBundleTypeRole</key>
    <string>Editor</string>
    <key>CFBundleURLName</key>
    <string></string>
    <key>CFBundleURLSchemes</key>
    <array>
      <string>fb$FACEBOOK_APPLICATION_ID</string>
    </array>
  </dict>
</array>
<key>FacebookAppID</key>
<string>$FACEBOOK_APPLICATION_ID</string>
<key>FacebookDisplayName</key>
<string>$FACEBOOK_DISPLAY_NAME</string>
<key>LSApplicationQueriesSchemes</key>
<array>
  <string>fbapi</string>
  <string>fb-messenger-api</string>
  <string>fbauth2</string>
  <string>fbshareextensions</string>
</array>
```

Browser security
==
In order to ensure the browser implementation works, it will be necessary to configure the Content-Security-Policy meta tag with something similar to the following:

```
<meta http-equiv="Content-Security-Policy" content="default-src 'self' gap://ready file://* *;
                  style-src 'self' 'unsafe-inline'
                        https://*.gstatic.com
                        https://*.googleapis.com
                        https://*.firebase.com
                        https://*.firebaseio.com;
                  script-src 'self' 'unsafe-inline' 'unsafe-eval'
                        https://*.gstatic.com
                        https://*.googleapis.com
                        https://*.firebase.com
                        https://*.firebaseio.com">
```

History
==
0.0.1
--
- Initial release
