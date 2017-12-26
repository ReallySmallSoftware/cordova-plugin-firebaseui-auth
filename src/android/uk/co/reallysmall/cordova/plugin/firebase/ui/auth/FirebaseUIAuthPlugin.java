package uk.co.reallysmall.cordova.plugin.firebase.ui.auth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserInfo;

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

public class FirebaseUIAuthPlugin extends CordovaPlugin implements OnCompleteListener<AuthResult>, FirebaseAuth.AuthStateListener {
    private static final int RC_SIGN_IN = 123;
    private final String TAG = "FirebaseUIAuthPlugin";
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
    private CallbackContext callbackContext;
    private FirebaseAuth firebaseAuth;
    private JSONObject options;

    @Override
    protected void pluginInitialize() {
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "action : " + action);

        switch (action) {
            case "initialise":
                return initialise(args, callbackContext);
            case "signIn":
                return signIn(callbackContext);
            case "signOut":
                return signOut(callbackContext);
            case "getToken":
                return getToken(callbackContext);
            default:
                return false;
        }

    }

    private boolean initialise(JSONArray args, CallbackContext callbackContext) {

        Log.d(TAG, "initialise");

        options = new JSONObject();

        try {
            options = args.getJSONObject(0);
            createProviderList();
        } catch (JSONException ex) {
            Log.d(TAG, "initialise: error getting options: " + ex.getMessage());
        }

        firebaseAuth = FirebaseAuth.getInstance();

        this.callbackContext = callbackContext;

        firebaseAuth.addAuthStateListener(this);

        return true;
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
                    providers.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
                }
                if ("FACEBOOK".equals(providerList.getString(i))) {
                    providers.add(new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());
                }
                if ("GOOGLE".equals(providerList.getString(i))) {
                    providers.add(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
                }
                if ("PHONE".equals(providerList.getString(i))) {
                    providers.add(new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build());
                }
                if ("TWITTER".equals(providerList.getString(i))) {
                    providers.add(new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());
                }
            }

        }
    }

    private boolean signIn(CallbackContext callbackContext) {

        Log.d(TAG, "signIn");

        this.callbackContext = callbackContext;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Log.d(TAG, "signIn: already have user");
            raiseEventForUser(user);
        } else {
            Log.d(TAG, "signIn: sign in");

            cordova.getActivity().startActivityForResult(
                    this.buildCustomInstance()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }

        return true;
    }

    private AuthUI.SignInIntentBuilder buildCustomInstance() {
        AuthUI.SignInIntentBuilder instance = AuthUI.getInstance().createSignInIntentBuilder();

        try {
            if (options.has("logo")) {
                instance = instance.setLogo(options.getInt("logo"));
            }
            if (options.has("theme")) {
                instance = instance.setTheme(options.getInt("theme"));
            }
            if (options.has("tosUrl")) {
                instance = instance.setTosUrl(options.getString("tosUrl"));
            }
            if (options.has("privacyPolicyUrl")) {
                instance = instance.setPrivacyPolicyUrl(options.getString("privacyPolicyUrl"));
            }
        } catch (JSONException ex) {
        }

        return instance;
    }

    private boolean signOut(CallbackContext callbackContext) {

        Log.d(TAG, "signOut");

        AuthUI.getInstance().signOut((FragmentActivity) cordova.getActivity());

        raiseEvent(callbackContext, "signoutsuccess", null);

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

    private void raiseEventForUser(FirebaseUser user) {
        final JSONObject resultData = new JSONObject();

        Log.d(TAG, "raiseEventForUser");

        List<? extends UserInfo> userInfoList = user.getProviderData();

        for (UserInfo userInfo : userInfoList) {

            if (user.getProviderId().equals(userInfo.getProviderId())) {

                try {
                    resultData.put("token", "token");
                    resultData.put("name", userInfo.getDisplayName());
                    resultData.put("email", userInfo.getEmail());
                    resultData.put("id", userInfo.getUid());
                    if (userInfo.getPhotoUrl() != null) {
                        resultData.put("photoUrl", userInfo.getPhotoUrl().toString());
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }

        raiseEvent(callbackContext, "signinsuccess", resultData);

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

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                raiseEventForUser(user);
            }
        }
    }


    private void raiseEvent(CallbackContext callbackContext, String type, Object data) {

        Log.d(TAG, "raiseEvent: " + type);

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
            raiseEvent(callbackContext, "signinfailure", data);
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

            raiseEvent(callbackContext, "signoutsuccess", null);
        }
    }
}
