const PLUGIN_NAME = 'FirebaseUIAuthPlugin';

function FirebaseUIAuth(options) {

    options = options || {};
    var allowDomains = options.allowDomains ? [].concat(options.allowDomains) : null;

    this.getToken = function(success, failure) {
    };

    this.signIn = function (silent) {
    };

    this.signOut = function () {
    };

    function dispatchEvent(event) {
        window.dispatchEvent(new CustomEvent(event.type, {detail: event.data}));
    }
}

if (typeof module !== undefined && module.exports) {
    module.exports = FirebaseUIAuth;
}
