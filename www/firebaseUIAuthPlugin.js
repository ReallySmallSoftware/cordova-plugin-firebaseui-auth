var exec = require('cordova/exec');
const PLUGIN_NAME = 'FirebaseUIAuth';

function FirebaseUIAuth(options) {

    options = options || {};
    exec(dispatchEvent, null, PLUGIN_NAME, 'initialise', [options]);

    this.getToken = function(success, failure) {

        if(window.Promise) {
            return new Promise(function (resolve, reject) {
                exec(resolve, reject, PLUGIN_NAME, 'getToken', []);
            });
        } else {
            return exec(success, failure, PLUGIN_NAME, 'getToken', []);
        }
    };

    this.signIn = function (silent) {

        return exec(dispatchEvent, null, PLUGIN_NAME, 'signIn', [silent === true]);
    };

    this.signOut = function () {

        return exec(dispatchEvent, null, PLUGIN_NAME, 'signOut', []);
    };

    function dispatchEvent(event) {

        window.dispatchEvent(new CustomEvent(event.type, {detail: event.data}));
    }
}

if (typeof module !== undefined && module.exports) {
    module.exports = FirebaseUIAuth;
}
