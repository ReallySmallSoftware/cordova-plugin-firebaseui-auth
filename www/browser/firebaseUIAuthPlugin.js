const PLUGIN_NAME = 'FirebaseUIAuth';

var loadJS = function(url, loaded, implementationCode, location) {

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

var loadCSS = function(url, loaded, implementationCode, location) {
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
  var uiConfig;
  var uiElement;
  var anonymous;

  var initialise = function() {
    if (firebase.apps.length === 0) {
      firebase.initializeApp(options.browser);
    }

    firebase.auth().onAuthStateChanged(function(user) {
      self.signInSuccess(user);
    });

    self.anonymous = options.anonymous ? true : false;
    self.signInAnonymously();

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
  };

  loadJS('https://www.gstatic.com/firebasejs/4.8.1/firebase.js', firebaseLoaded, function() {
    loadJS('https://www.gstatic.com/firebasejs/4.8.1/firebase-auth.js', firebaseAuthLoaded, function() {
      loadJS('https://cdn.firebase.com/libs/firebaseui/2.5.1/firebaseui.js', firebaseUILoaded, function() {
        loadCSS('https://cdn.firebase.com/libs/firebaseui/2.5.1/firebaseui.css', firebaseUILoaded, initialise, document.body);
      }, document.body)
    }, document.body)
  }, document.body);

  this.buildProviders = function(optionProviders) {

    var providers = [];

    for (i = 0; i < optionProviders.length; i++) {
      if (optionProviders[i] === "GOOGLE") {
        providers.push(firebase.auth.GoogleAuthProvider.PROVIDER_ID);
      } else
      if (optionProviders[i] === "FACEBOOK") {
        providers.push(firebase.auth.FacebookAuthProvider.PROVIDER_ID);
      } else
      if (optionProviders[i] === "EMAIL") {
        providers.push(firebase.auth.EmailAuthProvider.PROVIDER_ID);
      }
    }

    return providers;
  };

  this.buildUIConfig = function(options, providers) {
    this.uiConfig = {
      callbacks: {
        uiShown: function() {}
      },
      signInOptions: providers
    };

    if (options.tosUrl !== undefined) {
      this.uiConfig.tosUrl = options.tosUrl;
    }
  };

  this.signInAnonymously = function() {
    if (this.anonymous === true) {
      firebase.auth().signInAnonymously().catch(function(error) {
        alert(error.code + ":" + error.message);
      });
    }
  };

  this.signInSuccess = function(user) {

    if (user) {
      document.getElementById(this.uiElement).style.display = 'none';
      this.raiseEventForUser(user);
    } else {
      var customEvent = new CustomEvent("signoutsuccess", {});
      window.dispatchEvent(customEvent);
    }

    return false;
  };

  this.raiseEventForUser = function(user) {
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
  };

  this.getToken = function(success, failure) {
    var currentUser = firebase.auth().currentUser;
    currentUser.getIdToken().then(success);
  };

  this.signIn = function() {

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

  this.signOut = function() {
    firebase.auth().signOut();
    var customEvent = new CustomEvent("signoutsuccess", {});
    window.dispatchEvent(customEvent);
    this.signInAnonymously();
  };
}

module.exports = {
  initialise: function(options) {
    return new Promise(function(resolve, reject) {
      var db = new FirebaseUIAuth(options, resolve);
    });
  }
};
