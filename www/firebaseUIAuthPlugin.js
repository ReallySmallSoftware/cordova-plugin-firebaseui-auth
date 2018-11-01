/*global dispatchEvent: false, Promise: false, CustomEvent: false */
/*jshint esversion: 6 */
var exec = require('cordova/exec');
const PLUGIN_NAME = 'FirebaseUIAuth';

function FirebaseUIAuth(options) {

  function dispatchEvent(event) {
    window.dispatchEvent(new CustomEvent(event.type, {
      detail: event.data
    }));
  }

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

  this.signInAnonymously = function() {
    return exec(dispatchEvent, null, PLUGIN_NAME, 'signInAnonymously', []);
  };

  this.signOut = function() {
    return exec(dispatchEvent, null, PLUGIN_NAME, 'signOut', []);
  };

  this.delete = function() {
    return exec(dispatchEvent, null, PLUGIN_NAME, 'deleteUser', []);
  };

  this.sendEmailVerification = function() {
    return exec(dispatchEvent, null, PLUGIN_NAME, 'sendEmailVerification', []);
  };

  this.reloadUser = function() {
    return exec(dispatchEvent, null, PLUGIN_NAME, 'reloadUser', []);
  };
}

module.exports = {
  initialise: function(options) {
    return new Promise(function(resolve, reject) {
      resolve(new FirebaseUIAuth(options));
    });
  }
};
