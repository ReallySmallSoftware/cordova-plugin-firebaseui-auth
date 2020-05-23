/*global firebase, alert:false, firebaseui: false, Promise: false, CustomEvent: false */
/*jshint esversion: 6 */
const PLUGIN_NAME = 'FirebaseUIAuth';

var loadJS = function (url, loaded, implementationCode, location) {

  if (!loaded) {
    var scriptTag = document.createElement('script');
    scriptTag.src = url;

    scriptTag.onload = implementationCode;
    scriptTag.onreadystatechange = implementationCode;

    location.appendChild(scriptTag);
  } else {
    implementationCode();
  }
};

var loadCSS = function (url, loaded, implementationCode, location) {
  if (!loaded) {
    var linkTag = document.createElement('link');
    linkTag.href = url;
    linkTag.type = "text/css";
    linkTag.rel = "stylesheet";

    linkTag.onload = implementationCode;
    linkTag.onreadystatechange = implementationCode;

    location.appendChild(linkTag);
  } else {
    implementationCode();
  }
};

function FirebaseUIAuth(options, resolve) {

  var self = this;

  var initialise = function () {
    if (firebase.apps.length === 0) {
      firebase.initializeApp(options.browser);
    }

    firebase.auth().onAuthStateChanged(function (user) {
      self.signInSuccess(user);
    });

    self.anonymous = options.anonymous ? true : false;

    var providers = self.buildProviders(options.providers);
    self.buildUIConfig(options, providers);

    self.uiElement = options.uiElement;

    resolve(self);
  };

  var firebaseLoaded = "firebase" in window;
  var firebaseAuthLoaded = false;
  var firebaseUILoaded = false;
  if (firebaseLoaded) {
    firebaseAuthLoaded = "auth" in firebase;

    if (firebaseAuthLoaded) {
      firebaseUILoaded = "AuthUI" in firebase.auth;
    }
  }

  loadJS('https://www.gstatic.com/firebasejs/7.14.0/firebase-app.js', firebaseLoaded, function () {
    loadJS('https://www.gstatic.com/firebasejs/7.14.0/firebase-auth.js', firebaseAuthLoaded, function () {
      loadJS('https://www.gstatic.com/firebasejs/ui/4.5.0/firebase-ui-auth.js', firebaseUILoaded, function () {
        loadCSS('https://www.gstatic.com/firebasejs/ui/4.5.0/firebase-ui-auth.css', firebaseUILoaded, initialise, document.body);
      }, document.body);
    }, document.body);
  }, document.body);

  this.buildProviders = function (optionProviders) {

    var providers = [];
    var i;

    for (i = 0; i < optionProviders.length; i++) {
      if (optionProviders[i] === "GOOGLE") {
        providers.push(firebase.auth.GoogleAuthProvider.PROVIDER_ID);
      } else if (optionProviders[i] === "FACEBOOK") {
        providers.push(firebase.auth.FacebookAuthProvider.PROVIDER_ID);
      } else if (optionProviders[i] === "EMAIL") {
        providers.push(firebase.auth.EmailAuthProvider.PROVIDER_ID);
      } else if (optionProviders[i] === "apple.com") {
        providers.push("apple.com");
      }
    }

    return providers;
  };

  this.buildUIConfig = function (options, providers) {
    this.uiConfig = {
      callbacks: {
        uiShown: function () { }
      },
      signInOptions: providers
    };

    if (options.tosUrl !== undefined) {
      this.uiConfig.tosUrl = options.tosUrl;
    }
  };

  this.signInAnonymously = function () {
    if (this.anonymous === true) {
      firebase.auth().signInAnonymously().catch(function (error) {
        alert(error.code + ":" + error.message);
      });
    }
  };

  this.signInSuccess = function (user) {

    if (user) {
      document.getElementById(this.uiElement).style.display = 'none';
      this.raiseEventForUser(user);
    } else {
      var customEvent = new CustomEvent("signoutsuccess", {});
      window.dispatchEvent(customEvent);
    }

    return false;
  };

  this.reloadUser = function () {

    var self = this;

    firebase.auth().currentUser.reload().then(function () {
      self.raiseEventForUser(firebase.auth().currentUser);
    });
  };

  this.raiseEventForUser = function (user) {
    var customEvent = new CustomEvent("signinsuccess", this._formatUser(user));
    window.dispatchEvent(customEvent);
  };

  this._formatUser = function (user) {
    var newUser = false;

    if (user.metadata.creationTime === user.metadata.lastSignInTime) {
      newUser = true;
    }

    var detail = {
      "detail": {
        "name": user.displayName,
        "email": user.email,
        "emailVerified": user.emailVerified,
        "id": user.uid,
        "newUser": newUser
      }
    };

    if (user.photoURL !== undefined) {
      detail.detail.photoUrl = user.photoURL;
    }

    return detail;
  };


  this._formatEmptyUser = function (user) {

    var detail = {
      "detail": {
        "name": null,
        "email": null,
        "emailVerified": false,
        "id": null,
        "newUser": false
      }
    };

    return detail;
  };

  this.getToken = function () {
    var currentUser = firebase.auth().currentUser;
    return currentUser.getIdToken();
  };

  this.getCurrentUser = function () {
    var currentUser = firebase.auth().currentUser;
    var formattedUser;
    if (currentUser !== null) {
      formattedUser = this._formatUser(currentUser);
    } else {
      formattedUser = this._formatEmptyUser();
    }

    return new Promise(function (resolve, reject) {
      resolve(formattedUser);
    })
  };

  this.signIn = function () {

    var currentUser = firebase.auth().currentUser;

    if (currentUser !== null && !currentUser.isAnonymous) {
      this.raiseEventForUser(currentUser);
    } else {
      var ui = firebaseui.auth.AuthUI.getInstance();

      if (ui === null) {
        ui = new firebaseui.auth.AuthUI(firebase.auth());
      }

      var element = document.getElementById(this.uiElement);

      element.style.display = "block";
      element.style.position = "fixed";
      element.style.top = "0";
      element.style.paddingTop = "80px";
      element.style.left = "0";
      element.style.right = "0";
      element.style.bottom = "0";
      element.style.zIndex = "10000";
      element.style.backgroundColor = "white";

      ui.start('#' + this.uiElement, this.uiConfig);
    }
  };

  this.sendEmailVerification = function () {

    var currentUser = firebase.auth().currentUser;
    var customEvent;

    currentUser.sendEmailVerification().then(function () {
      customEvent = new CustomEvent("emailverificationsent", {});
      window.dispatchEvent(customEvent);
    }, function (error) {
      customEvent = new CustomEvent("emailverificationnotsent", {});
      window.dispatchEvent(customEvent);
    });
  };

  this.signOut = function () {
    firebase.auth().signOut();
    var customEvent = new CustomEvent("signoutsuccess", {});
    window.dispatchEvent(customEvent);
    this.signInAnonymously();
  };

  this.delete = function () {
    var ui = firebaseui.auth.AuthUI.getInstance();

    if (ui === null) {
      ui = new firebaseui.auth.AuthUI(firebase.auth());
    }

    var currentUser = firebase.auth().currentUser; 0
    currentUser.delete().then(function () {
      ui.delete();

      var customEvent = new CustomEvent("deleteusersuccess", {});
      window.dispatchEvent(customEvent);
      self.signInAnonymously();

    }).catch(function (err) {
      var customEvent = new CustomEvent("deleteuserfailure", {
        detail: {
          message: err.message
        }
      });
      window.dispatchEvent(customEvent);
    });
  };
}

module.exports = {
  initialise: function (options) {
    return new Promise(function (resolve, reject) {
      var db = new FirebaseUIAuth(options, resolve);
    });
  }
};
