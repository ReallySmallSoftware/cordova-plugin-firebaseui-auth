var exec = require('cordova/exec');
const PLUGIN_NAME = 'FirebaseUIAuth';

function FirebaseUIAuth(options) {

  options = options || {};
  exec(dispatchEvent, null, PLUGIN_NAME, 'initialise', [options]);

  this.getToken = function() {
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'getToken', []);
    });
  };

  this.signIn = function() {
    return exec(dispatchEvent, null, PLUGIN_NAME, 'signIn', []);
  };

  this.signOut = function() {
    return exec(dispatchEvent, null, PLUGIN_NAME, 'signOut', []);
  };

  this.delete = function() {
    return exec(dispatchEvent, null, PLUGIN_NAME, 'deleteUser', []);
  };

  function dispatchEvent(event) {
    window.dispatchEvent(new CustomEvent(event.type, {
      detail: event.data
    }));
  }
}

module.exports = {
  initialise: function(options) {
    return new Promise(function(resolve, reject) {
      resolve(new FirebaseUIAuth(options));
    });
  }
};
