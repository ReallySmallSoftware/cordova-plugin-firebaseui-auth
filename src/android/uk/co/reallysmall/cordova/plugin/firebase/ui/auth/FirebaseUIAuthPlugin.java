package uk.co.reallysmall.cordova.plugin.firebase.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FirebaseUIAuthPlugin extends CordovaPlugin implements OnCompleteListener<AuthResult>, FirebaseAuth.AuthStateListener {
    private static final int RC_SIGN_IN = 123;
    private final String TAG = "FirebaseUIAuthPlugin";
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());
    private CallbackContext signInCallbackContext;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void pluginInitialize() {
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "action : " + action);

        switch (action) {
            case "initialise":
                return initialise(callbackContext);
            case "signIn":
                return signIn(callbackContext);
            case "signOut":
                return signOut(callbackContext);
            default:
                return false;
        }

    }

    private boolean initialise(CallbackContext callbackContext) {

        Log.d(TAG, "initialise");
        firebaseAuth = FirebaseAuth.getInstance();

        signInCallbackContext = callbackContext;

        firebaseAuth.addAuthStateListener(this);
        return true;
    }

    private boolean signIn(CallbackContext callbackContext) {

        Log.d(TAG, "signIn");

        signInCallbackContext = callbackContext;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Log.d(TAG, "signIn: already have user");

            raiseEventForUser(user);
        } else {
            Log.d(TAG, "signIn: sign in");

            cordova.getActivity().startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }

        return true;
    }

    private boolean signOut(CallbackContext callbackContext) {

        Log.d(TAG, "signOut");

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signOut();

        raiseEvent(callbackContext, "signoutsuccess", null);

        return true;
    }

    private void raiseEventForUser(FirebaseUser user) {
        final JSONObject resultData = new JSONObject();

        Log.d(TAG, "raiseEventForUser");

        try {
            resultData.put("token", "token");
            resultData.put("name", user.getDisplayName());
            resultData.put("email", user.getEmail());
            resultData.put("id", user.getUid());
            if (user.getPhotoUrl() != null) {
                resultData.put("photoUrl", user.getPhotoUrl().toString());
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());

        }
        Log.d(TAG, "raiseEventForUser: raising");
        Log.d(TAG, "raiseEventForUser: raising " + user.getDisplayName());
        Log.d(TAG, "raiseEventForUser: raising " + user.getEmail());

        raiseEvent(signInCallbackContext, "signinsuccess", resultData);

        Log.d(TAG, "raiseEventForUser: raised");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult");

        Log.d(TAG, "onActivityResult:requestCode:" + requestCode);

        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult:resultCode:" + resultCode);

            if (resultCode == RESULT_OK) {
                IdpResponse response = IdpResponse.fromResultIntent(data);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                IdpResponse idpResponse = IdpResponse.fromResultIntent(data);

                raiseEventForUser(user);

            } else {
            }
        }
    }


    private void raiseEvent(CallbackContext callbackContext, String type, Object data) {


        JSONObject event = new JSONObject();
        try {
            event.put("type", type);
            event.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
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
            }
            raiseEvent(signInCallbackContext, "signinfailure", data);
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

        Log.d(TAG, "onAuthStateChanged");

        final FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "onAuthStateChanged: signed in");

            raiseEventForUser(user);

        } else {
            Log.d(TAG, "onAuthStateChanged: signed out");

            raiseEvent(signInCallbackContext,"signoutsuccess",null);
        }
    }
}
