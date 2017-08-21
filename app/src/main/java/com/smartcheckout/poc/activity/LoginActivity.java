package com.smartcheckout.poc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smartcheckout.poc.R;
import com.smartcheckout.poc.util.StateData;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    // Login variables
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    public static final int RC_SIGN_IN = 101;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    // Code for finding out the development hash key for facebook login.
        // Need to be used only once
       /* try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.smartcheckout.poc",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }*/

        //Initialize Firebase componets
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                System.out.println(" ------------->Starting on create");
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    onSignInInitialize();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    //Firebase UI dropin to take care of login flow
                    System.out.println("---------------> In on create -- User is logged out");
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build()))
                                    .setTheme(R.style.AppTheme)
                                    .build(),
                            RC_SIGN_IN);
                }
                // ...
            }
        };

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove authorization state listener
        if (mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Attach authorization state listener
        if (mAuthListener != null)
            mAuth.addAuthStateListener(mAuthListener);
    }
    public void onSignInInitialize() {
        /** TBD : Set the logged in user id to StateData **/
        Intent storeSelectionIntent = new Intent(this, StoreSelectionActivity.class);
        startActivity(storeSelectionIntent);

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
           IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                onSignInInitialize();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar(R.string.login_failed);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar(R.string.unknown_error);
                    return;
                }
            }

        }
    }
    /*public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            //Sign Out selected
            case R.id.sign_out_menu:
                System.out.println("In sign out case");
                System.out.println(user.getDisplayName());
                AuthUI.getInstance().signOut(this); // Add listener
                System.out.println("Notified auth ui");
                System.out.println(user.getDisplayName());
                return true;
            case R.id.help_menu:
                //    showHelp
                return true;
            case R.id.action_settings:
                // showSettings
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    public void showSnackbar(int message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .show();

    }
}
