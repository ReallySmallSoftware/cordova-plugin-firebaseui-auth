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
  var uiElement;

  var initialise = function() {
    if (firebase.apps.length === 0) {
      firebase.initializeApp(options.browser);
    }
    resolve(self);
  };

  loadJS('https://cdn.firebase.com/libs/firebaseui/2.5.1/firebaseui.js', function() {
    loadCSS('https://cdn.firebase.com/libs/firebaseui/2.5.1/firebaseui.css', initialise, document.body);
  }, document.body);

  this.uiElement = options.uiElement;

  this.signInSuccess = function(user) {

    $(this.uiElement).hide();

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
      var customEvent = new CustomEvent("signoutsuccess", {});
      window.dispatchEvent(customEvent);
    }

    return false;
  };

  var providers = [];

  $.each(options.providers, function(i, v) {
    if (v === "GOOGLE") {
      providers.push(firebase.auth.GoogleAuthProvider.PROVIDER_ID);
    } else
    if (v === "FACEBOOK") {
      providers.push(firebase.auth.FacebookAuthProvider.PROVIDER_ID);
    } else
    if (v === "EMAIL") {
      providers.push(firebase.auth.EmailAuthProvider.PROVIDER_ID);
    }
  });

  this.uiConfig = {
    callbacks: {
      signInSuccess: $.proxy(this.signInSuccess, this),
      uiShown: function() {}
    },
    signInOptions: providers
  };

  if (options.tosUrl !== undefined) {
    this.uiConfig.tosUrl = options.tosUrl;
  }

  this.getToken = function(success, failure) {
    user.getIdToken().then(success);
  };

  this.signIn = function() {
    var ui = firebaseui.auth.AuthUI.getInstance();

    if (ui === null) {
      ui = new firebaseui.auth.AuthUI(firebase.auth());
    }

    $(this.uiElement).show();
    $(this.uiElement).css("position", "fixed");
    $(this.uiElement).css("top", "0");
    $(this.uiElement).css("padding-top", "80px");
    $(this.uiElement).css("left", "0");
    $(this.uiElement).css("right", "0");
    $(this.uiElement).css("bottom", "0");
    $(this.uiElement).css("background-color", "white");

    ui.start(this.uiElement, this.uiConfig);
  };

  this.signOut = function() {
    firebase.auth().signOut();
    var customEvent = new CustomEvent("signoutsuccess", {});
    window.dispatchEvent(customEvent);
  };
}

module.exports = {
  initialise: function(options) {
    return new Promise(function(resolve, reject) {
      var db = new FirebaseUIAuth(options, resolve);
    });
  }
};
