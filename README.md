cordova-plugin-firebaseui-auth
==
A FirebaseUI Auth plugin to enable easy authentication using a number of different providers.

What is FirebaseUI?
--

Installation
--
From the documentation (https://opensource.google.com/projects/firebaseui):

> A UI library for Firebase, including binding for the realtime database, authentication and storage.

Supported platforms
--
This plugin supports the following platforms:

- Android
- iOS
- Browser

Support providers
--
The following providers are currently supported:

- Google
- Facebook
- Email

Installation
--

```
cordova plugin add cordova-plugin-firebaseui-auth --variable ANDROID_FIREBASE_VERSION=11.6.0
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

Any variables that are not supplied will use default values. For credential based variables (FACEBOOK_APPLICATION_ID, REVERSED_CLIENT_ID and REVERSED_CLIENT_ID) this will result in the provider not working.

Dependencies
--
In order for FirebaseUI to work on Android the default Cordova MainActivity needs to be replaced with a class that inherites from a FragmentActivity.

This plugin therefore depends on the `cordova-plugin-android-fragmentactivity` plugin to enable this.

It also depends on `cordova-plugin-firebase-hooks` to enable the necessary config files to be copied to the correct platform locations.

Getting started
--
This guide will assume familiarity with Firebase and FirebaseUI.

Create a new FirebaseAuthUI instance:

```
   firebaseUIAuth = FirebaseUIAuth.initialise({
      "providers": ["GOOGLE", "FACEBOOK", "EMAIL"],
      "tosUrl" : "http://www.myapp.co.uk/terms.html",
      "privacyPolicyUrl" : "http://www.myapp.co.uk/privacy.html",
      "theme" : "themeName",
      "logo" : "logoName"
    }).then(<do my stuff>);
```

This is initialised as a promise to allow the Browser implementation to dynamically add a reference to the FirestoreUI Javascript SDK.

Not all of the above options will function on all platforms:

- providers: a list of authentication providers to use
- tosUrl: a terms of services URL when signing up via email
- privacyPolicyUrl: a privacy policy URL - Android only
- theme: a theme identifier for styling - Android only
- logo: a logo to display - Android only

How do I use it?
--
- Download the google-services.json and/or GoogleService-Info.plist for you application from the Firebase console.
- If using Facebook as a provider make sure you set up an app and record the necessary credentials
- Copy the files from previous step to root of your project.  If you don't do this plugin's hook won't be able to copy
them to the appropriate locations and bad things will happen at runtime.
- Now you're ready to write some code.

History
==
0.0.1
--
- Initial release
