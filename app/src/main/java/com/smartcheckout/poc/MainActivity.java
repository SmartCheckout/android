package com.smartcheckout.poc;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.smartcheckout.poc.BuildConfig.DEBUG;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static AppState appState = new AppState();
    public static List<Product> scannedItems = new ArrayList<Product>();
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    TextView storeName;
    TextView storeDesc;
    RatingBar storeRatings;
    FloatingActionButton fabAdd;

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

        //Create the main screen
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        storeName = (TextView)findViewById(R.id.storeName);
        storeDesc = (TextView)findViewById(R.id.storeDesc);
        storeRatings =(RatingBar)findViewById(R.id.ratings);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        /*
        * The following code example shows setting an AutocompleteFilter on a PlaceAutocompleteFragment to
        * set a filter returning only results with a precise address.
        */

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                .build();
        autocompleteFragment.setFilter(typeFilter);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("Swetha", "Place: " + place.getName());//get place details here
                Log.i("Swetha", "Place: " + place.getLatLng());//get place details here
                appState.setLocationSelected(true);
                appState.setStoreDetails(place);
                appState.setStoreSelected(true);
                fabAdd.setVisibility(View.VISIBLE);
                findViewById(R.id.place_autocomplete_fragment).setVisibility(View.GONE);
                storeName.setVisibility(View.VISIBLE);
                storeDesc.setVisibility(View.VISIBLE);
                storeRatings.setVisibility(View.VISIBLE);
                storeRatings.setRating(place.getRating());
                storeName.setText(place.getName());
                storeDesc.setText(place.getAddress()+"\nPh: "+place.getPhoneNumber());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Swetha", "An error occurred: " + status);
            }
        });

        // Set the drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
       // drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
                    onSignInInitialize();
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
       /* if (requestCode == RC_BARCODE_CAPTURE) {
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
*/
        // Result from Firebase Auth UI signin
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                onSignInInitialize();
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
    private void onSignInInitialize() {
        System.out.println("------->In sign in initialize");
        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("Barcode"))
        {
            Barcode barcode = (Barcode) intent.getParcelableExtra("Barcode");
            if(!scannedItems.contains((barcode.displayValue)))
            {
                Product prod = new Product();
                prod.setProductName(barcode.displayValue);
                prod.setBarcodeValue(barcode.rawValue);
                prod.setQuatity(1);
                scannedItems.add(prod);

            }

        }


        /*final FloatingActionButton fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(appState.isLocationSelected())
                {
                    final String stores[] = new String[] {"Walmart, Story road", "Walmart, Willow Glen", "Walmart, Milpitas"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Nearby Stores");
                    builder.setItems(stores, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            appState.setStoreName(stores[which]);
                            appState.setStoreSelected(true);
                            fabSearch.setVisibility(View.GONE);
                            fabAdd.setVisibility(View.VISIBLE);
                            findViewById(R.id.place_autocomplete_fragment).setVisibility(View.GONE);
                            storeName.setText(stores[which]);
                            storeName.setVisibility(View.VISIBLE);
                        }
                    });
                    builder.show();
                }


            }
        }); */

        if(appState.isStoreSelected())
        {
            fabAdd.setVisibility(View.VISIBLE);
            findViewById(R.id.place_autocomplete_fragment).setVisibility(View.GONE);
            storeName.setText(appState.getStoreName());
            storeName.setVisibility(View.VISIBLE);
            storeDesc.setVisibility(View.VISIBLE);
            storeRatings.setVisibility(View.VISIBLE);
            storeRatings.setRating(appState.getStoreDetails().getRating());
            storeName.setText(appState.getStoreDetails().getName());
            storeDesc.setText(appState.getStoreDetails().getAddress()+"\nPh: "+appState.getStoreDetails().getPhoneNumber());
        }

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, scannedItems, prepareListData());

        // setting list adapter
        expListView.setAdapter(listAdapter);


    }

    private void onSignedOutCleanUp() {
        userDisplayName = ANONYMOUS;
    }

    // Initialize the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
                //    showHelp
                return true;
            case R.id.action_settings:
                // showSettings
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private HashMap<String, List<String>> prepareListData() {
        HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();


        // Adding child data
        List<String> prodDetails = new ArrayList<String>();
        prodDetails.add("Manufacturer: Unilever");
        prodDetails.add("Size: 200g");
        prodDetails.add("Refund: 15 days");


        for(Product item:scannedItems)
        {
            listDataChild.put(item.getProductName(),prodDetails);
        }

        return listDataChild;
    }


    /** Called when the user clicks the Send button */
    public void sendMessage() {
        Intent intent = new Intent(this, ScanBarcodeActivity.class);
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

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
            case R.id.nav_camera:
                // Handle the camera action
            case R.id.nav_gallery:
                //Handle gallery action here
            case R.id.nav_slideshow:
                //Handle slideshow action here
            case R.id.nav_manage:
                //Handle tools action here
                return true;
            case R.id.nav_share:
                //Handle share action here
            case R.id.nav_send:
                //Handle send aciton here
            default:
                //Default case
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}