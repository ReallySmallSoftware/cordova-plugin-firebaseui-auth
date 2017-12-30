const PLUGIN_NAME = 'FirebaseUIAuth';

var loadJS = function(url, implementationCode, location) {
  var scriptTag = document.createElement('script');
  scriptTag.src = url;

  scriptTag.onload = implementationCode;
  scriptTag.onreadystatechange = implementationCode;

  location.appendChild(scriptTag);
};

var loadCSS = function(url, implementationCode, location) {
  var linkTag = document.createElement('link');
  linkTag.href = url;
  linkTag.type = "text/css";
  linkTag.rel = "stylesheet";

  linkTag.onload = implementationCode;
  linkTag.onreadystatechange = implementationCode;

  location.appendChild(linkTag);
};

function FirebaseUIAuth(options, resolve) {

  var self = this;
  var uiConfig;

  var initialise = function() {
    if (firebase.apps.length === 0) {
      firebase.initializeApp(options.browser);
    }
    resolve(self);
  };

  loadJS('https://cdn.firebase.com/libs/firebaseui/2.5.1/firebaseui.js', function() {
    loadCSS('https://cdn.firebase.com/libs/firebaseui/2.5.1/firebaseui.css', initialise, document.body);
  }, document.body);

  this.signInSuccess = function(user) {
    if (user) {
      var detail = {
        "detail": {
          "name": user.displayName,
          "email": user.email,
          "emailVerified": user.emailVerified,
          "id": user.uid
        }
      };

      if (user.photoURL !== undefined) {
        detail.detail.photoUrl = user.photoURL;
      }

      var customEvent = new CustomEvent("signinsuccess", detail);
      window.dispatchEvent(customEvent);
    } else {
      var customEvent = new CustomEvent("signoutsuccess", detail);
      window.dispatchEvent(customEvent);
    }

    return false;
  };

  this.uiConfig = {
    callbacks: {
      signInSuccess: $.proxy(this.signInSuccess, this),
      uiShown: function() {}
    },
    signInOptions: [
      firebase.auth.GoogleAuthProvider.PROVIDER_ID,
      firebase.auth.FacebookAuthProvider.PROVIDER_ID,
      firebase.auth.EmailAuthProvider.PROVIDER_ID
    ],
    tosUrl: '<your-tos-url>'
  };

  this.getToken = function(success, failure) {
    user.getIdToken().then(success);
  };

  this.signIn = function(silent) {
    var ui = firebaseui.auth.AuthUI.getInstance();

    if (ui === null) {
      ui = new firebaseui.auth.AuthUI(firebase.auth());
    }

    ui.start('#firebaseui-auth-container', this.uiConfig);
  };

  this.signOut = function() {
    firebase.auth().signOut();
  };
}

module.exports = {
  initialise: function(options) {
    return new Promise(function(resolve, reject) {
      var db = new FirebaseUIAuth(options, resolve);
    });
  }
};
