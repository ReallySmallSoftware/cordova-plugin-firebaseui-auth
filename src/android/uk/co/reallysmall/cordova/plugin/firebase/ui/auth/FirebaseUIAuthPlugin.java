package uk.co.reallysmall.cordova.plugin.firebase.ui.auth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FirebaseUIAuthPlugin extends CordovaPlugin implements OnCompleteListener<AuthResult> {
    private static final int RC_SIGN_IN = 123;
    private final String TAG = "FirebaseUIAuthPlugin";
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build());
    private CallbackContext callbackContext;
    private FirebaseAuth firebaseAuth;
    private JSONObject options;
    private boolean anonymous;

    @Override
    protected void pluginInitialize() {
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "action : " + action);

        if ("initialise".equals(action)) {
            return initialise(args, callbackContext);
        } else if ("signIn".equals(action)) {
            return signIn(callbackContext);
        } else if ("signInAnonymously".equals(action)) {
            return signInAnonymously(callbackContext);
        } else if ("signOut".equals(action)) {
            return signOut(callbackContext);
        } else if ("deleteUser".equals(action)) {
            return deleteUser(callbackContext);
        } else if ("getToken".equals(action)) {
            return getToken(callbackContext);
        } else if ("sendEmailVerification".equals(action)) {
            return sendEmailVerification(callbackContext);
        } else if ("reloadUser".equals(action)) {
            return reloadUser(callbackContext);
        } else {
            Log.w(TAG, "Unknown action : " + action);
            return false;
        }
    }

    private boolean reloadUser(CallbackContext callbackContext) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            user.reload().addOnSuccessListener(new OnSuccessListener<Void>() {

                @Override
                public void onSuccess(Void aVoid) {
                   FirebaseUser user = firebaseAuth.getCurrentUser();
                   raiseEventForUser(user);
                }
             });
        }

        return true;
    }

    private boolean signInAnonymously(CallbackContext callbackContext) {

        signInAnonymous(firebaseAuth);

        return true;
    }

    private boolean sendEmailVerification(final CallbackContext callbackContext) {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null && !anonymous) {

            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        raiseEvent(callbackContext, "emailverificationsent", null);
                    } else {
                        raiseEvent(callbackContext, "emailverificationnotsent", null);
                    }
                }
            });
        }

        return true;
    }

    private boolean initialise(JSONArray args, final CallbackContext callbackContext) {

        Log.d(TAG, "initialise");

        options = new JSONObject();

        anonymous = false;

        try {
            options = args.getJSONObject(0);
            createProviderList();
            if (options.has("anonymous") && options.getBoolean("anonymous")) {
                anonymous = true;
            }
        } catch (JSONException ex) {
            Log.e(TAG, "initialise: error getting options: " + ex.getMessage());
        }

        this.callbackContext = callbackContext;

        firebaseAuth = FirebaseAuth.getInstance();

        return true;
    }

    private void signInAnonymous(final FirebaseAuth firebaseAuth) {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null && anonymous) {
            firebaseAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInAnonymously:success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        raiseEventForUser(user);
                    } else {
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        raiseErrorEvent("signinfailure", 1, "Anonymous sign in failed");
                    }
                }
            });
        }
    }

    private void createProviderList() throws JSONException {

        if (options.has("providers")) {

            Log.d(TAG, "createProviderList: parsing providers");

            providers = new ArrayList<AuthUI.IdpConfig>();

            JSONArray providerList = options.getJSONArray("providers");

            int length = providerList.length();

            for (int i = 0; i < length; i++) {
                Log.d(TAG, "createProviderList: parsing provider " + providerList.getString(i));

                if ("EMAIL".equals(providerList.getString(i))) {
                    providers.add(new AuthUI.IdpConfig.EmailBuilder().build());
                }
                if ("FACEBOOK".equals(providerList.getString(i))) {
                    providers.add(new AuthUI.IdpConfig.FacebookBuilder().build());
                }
                if ("GOOGLE".equals(providerList.getString(i))) {
                    providers.add(new AuthUI.IdpConfig.GoogleBuilder().build());
                }
                if ("PHONE".equals(providerList.getString(i))) {
                    providers.add(new AuthUI.IdpConfig.PhoneBuilder().build());
                }
                if ("TWITTER".equals(providerList.getString(i))) {
                    providers.add(new AuthUI.IdpConfig.TwitterBuilder().build());
                }
                if ("GITHUB".equals(providerList.getString(i))) {
                    providers.add(new AuthUI.IdpConfig.GitHubBuilder().build());
                }
                if ("ANONYMOUS".equals(providerList.getString(i))) {
                    providers.add(new AuthUI.IdpConfig.AnonymousBuilder().build());
                }
            }
        }
    }

    private boolean signIn(CallbackContext callbackContext) {

        Log.d(TAG, "signIn");

        this.callbackContext = callbackContext;

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null && !user.isAnonymous()) {
            Log.d(TAG, "signIn: already have user");
            raiseEventForUser(user);
        } else {
            Log.d(TAG, "signIn: sign in");

            cordova.setActivityResultCallback(this);

            cordova.getActivity().startActivityForResult(
                    this.buildCustomInstance()
                            .enableAnonymousUsersAutoUpgrade()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }

        return true;
    }

    private AuthUI.SignInIntentBuilder buildCustomInstance() {
        AuthUI.SignInIntentBuilder instance = AuthUI.getInstance().createSignInIntentBuilder();

        Log.d(TAG, "buildCustomInstance");

        boolean smartLockEnabled = false;
        boolean smartLockHints = false;

        try {
            if (options.has("logo")) {
                int id = getIdentifier(options.getString("logo"), "drawable");
            //    instance = instance.setLogo(id);
            }
            if (options.has("theme")) {
                int id = getIdentifier(options.getString("theme"), "style");
                instance = instance.setTheme(id);
            } else {
                int id = getIdentifier("FirebaseUILogonTheme", "style");
                instance = instance.setTheme(id);
            }
            if (options.has("tosUrl") && options.has("privacyPolicyUrl")) {
                instance = instance.setTosAndPrivacyPolicyUrls(options.getString("tosUrl"),options.getString("privacyPolicyUrl"));
            } else {
                if (options.has("tosUrl")) {
                    instance = instance.setTosUrl(options.getString("tosUrl"));
                }
                if (options.has("privacyPolicyUrl")) {
                    instance = instance.setPrivacyPolicyUrl(options.getString("privacyPolicyUrl"));
                }
            }
            if (options.has("smartLockEnabled")) {
                smartLockEnabled = options.getBoolean("smartLockEnabled");
            }
            if (options.has("smartLockHints")) {
                smartLockHints = options.getBoolean("smartLockHints");
            }
            instance = instance.setIsSmartLockEnabled(smartLockEnabled, smartLockHints);
        } catch (JSONException ex) {
            Log.e(TAG, "Error in buildCustomInstance ", ex);
        }

        return instance;
    }

    private int getIdentifier(String name, String type) {
        return cordova.getActivity().getApplicationContext().getResources().getIdentifier(name,
                type,
                cordova.getActivity().getApplicationContext().getPackageName());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult");
        Log.d(TAG, "onActivityResult:requestCode:" + requestCode);

        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult:resultCode:" + resultCode);

            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                raiseEventForUser(user);
            } else {
                if (response == null) {
                    signInAnonymous(firebaseAuth);
                    raiseEvent(callbackContext, "signinaborted", null);
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT) {
                    handleAnonymousUpgradeMergeConflict(response);
                } else {

                    if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                        raiseErrorEvent("signinfailure", response.getError().getErrorCode(), "No network so unable to sign in.");

                        return;
                    }

                    Log.e(TAG, "Sign-in error: ", response.getError());
                    raiseErrorEvent("signinfailure", response.getError().getErrorCode(), "There was a problem signing in.");
                }
            }
        }
    }

    private void handleAnonymousUpgradeMergeConflict(IdpResponse response) {
        AuthCredential nonAnonymousCredential = response.getCredentialForLinking();

        FirebaseAuth.getInstance().signInWithCredential(nonAnonymousCredential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult result) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        raiseEventForUser(user);
                    }
                });
    }

    private boolean signOut(CallbackContext callbackContext) {

        Log.d(TAG, "signOut");

        AuthUI.getInstance().signOut((FragmentActivity) cordova.getActivity()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Log.d(TAG, "signOut: success");

                    raiseEvent(callbackContext, "signoutsuccess", null);
                    signInAnonymous(firebaseAuth);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "signOut: failure", e);
                raiseErrorEvent("signoutfailure", 1, "There was a problem signing out.");
            }
        });

        return true;
    }

    private boolean deleteUser(final CallbackContext callbackContext) {

        Log.d(TAG, "deleteUser");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final FirebaseUIAuthPlugin plugin = this;

        if (user != null) {
            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        Log.d(TAG, "deleteUser: success");

                        AuthUI.getInstance().delete((FragmentActivity) cordova.getActivity());
                        raiseEvent(callbackContext, "deleteusersuccess", null);

                        plugin.signInAnonymous(firebaseAuth);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "deleteUser: failure", e);
                    raiseErrorEvent("deleteuserfailure", 1, "This operation requires you to have recently authenticated. Please log out and back in and try again.");
                }
            });
        }

        return true;
    }


    private boolean getToken(final CallbackContext callbackContext) {

        Log.d(TAG, "getToken");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {

                    callbackContext.success(task.getResult().getToken());
                }
            });
        } else {
            callbackContext.error("no_user_found");
        }

        return true;
    }

    private void raiseErrorEvent(String event, int code, String message) {
        JSONObject data = new JSONObject();
        try {
            data.put("code", code);
            data.put("message", message);
        } catch (JSONException e) {
            Log.e(TAG, "Error in raiseErrorEvent ", e);
        }
        raiseEvent(callbackContext, event, data);
    }

    private void raiseEventForUser(FirebaseUser user) {
        final JSONObject resultData = new JSONObject();

        Log.d(TAG, "raiseEventForUser");

        try {

            anonymous = false;

            resultData.put("name", user.getDisplayName());
            resultData.put("email", user.getEmail());
            resultData.put("emailVerified", user.isEmailVerified());
            resultData.put("id", user.getUid());
            if (user.getMetadata().getCreationTimestamp() == user.getMetadata().getLastSignInTimestamp()) {
                resultData.put("newUser", true);
            } else {
                resultData.put("newUser", false);
            }
            if (user.getPhotoUrl() != null) {
                resultData.put("photoUrl", user.getPhotoUrl().toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error in raiseEventForUser ", e);
        }

        raiseEvent(callbackContext, "signinsuccess", resultData);

        Log.d(TAG, "raiseEventForUser: raised");

    }

    private void raiseEvent(CallbackContext callbackContext, String type, Object data) {

        Log.d(TAG, "raiseEvent: " + type);

        JSONObject event = new JSONObject();
        try {
            event.put("type", type);
            if (data != null) {
                event.put("data", data);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error in raiseEvent ", e);
        }

        PluginResult result = new PluginResult(PluginResult.Status.OK, event);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        Log.d(TAG, "onComplete");

        if (!task.isSuccessful()) {
            Exception err = task.getException();
            JSONObject data = new JSONObject();
            try {
                data.put("code", err.getClass().getSimpleName());
                data.put("message", err.getMessage());
            } catch (JSONException e) {
                Log.e(TAG, "Error in onComplete ", e);
            }
            raiseEvent(callbackContext, "signinfailure", data);
        }
    }
}
