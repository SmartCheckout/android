package com.smartcheckout.poc;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import static com.smartcheckout.poc.BuildConfig.DEBUG;


public class MainActivity extends AppCompatActivity {
    TextView mainArea;
    // Firebase UI authentication instance variables
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;

    public static final int RC_BARCODE_CAPTURE = 0;
    public static final int RC_SIGN_IN = 100;

    private String userDisplayName;
    private static final String ANONYMOUS = "";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

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
        //Login code
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                System.out.println(" ------------->Starting on create");
                if (user != null) {
                    // User is signed in
                    //Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    System.out.println("--------------> In on create --User is logged in");
                    onSignedInInitialize();
                } else {
                    // User is signed out
                    // Log.d(TAG, "onAuthStateChanged:signed_out");
                    //Firebase UI dropin to take care of login flow
                    System.out.println("---------------> In on create -- User is logged out");
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(!DEBUG)
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
                // ...
            }
        };

    }

    //Added for login
    @Override
    public void onStart() {
        super.onStart();
    }

    //Added for login
    @Override
    public void onStop() {
        super.onStop();
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
        System.out.println("In on resume");
        // Attach authorization state listener
        if (mAuthListener != null)
            mAuth.addAuthStateListener(mAuthListener);
    }

    // Added for scanning barcode
    public void scanBarCode(View view) {
        Intent intent = new Intent(this, ScanBarcodeActivity.class);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result from ScanBarcodeActivity
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra("barcode");
                    mainArea.setText("Barcode value" + barcode.displayValue);
                } else {
                    mainArea.setText("Oops! We could not scan the barcode");
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        // Result from Firebase Auth UI signin
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                onSignedInInitialize();
            } else {
                // Sign in failed
                final View coordinatorLayoutView = findViewById(R.id.snackbarPosition);
                if (response == null) {
                    // User pressed back button
                    Snackbar.make(coordinatorLayoutView, R.string.sign_in_cancelled, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Snackbar.make(coordinatorLayoutView, R.string.no_internet_connection, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Snackbar.make(coordinatorLayoutView, R.string.unknown_error, Snackbar.LENGTH_LONG).show();
                    return;
                }
            }

            //showSnackbar(R.string.unknown_sign_in_response);
        }
    }

    //Initialize the home screen ehen the user is signed in
    private void onSignedInInitialize() {
        setContentView(R.layout.activity_main);
        mainArea = (TextView) findViewById(R.id.main_area);
    }

    private void onSignedOutCleanUp() {
        userDisplayName = ANONYMOUS;
    }

    // Initialize the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    // Take action based on selection in the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                //    showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}