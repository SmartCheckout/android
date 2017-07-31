package com.smartcheckout.poc.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.smartcheckout.poc.R;
import com.smartcheckout.poc.models.Store;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * Created by yeshwanth on 4/5/2017.
 */

public class StoreSelectionActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener  {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
     private ProgressBar progressBar;


    private static final int RC_LOCATION_PERMISSION = 1;
    private static final int RC_CHECK_SETTING = 2;

   // private static final int RC_SCAN_BARCODE_PROD = 100;
    private static final int RC_SCAN_BARCODE_STORE = 200;

    private AsyncHttpClient ahttpClient = new AsyncHttpClient();
    private Store selectedStore;
    private boolean storeMatched = false;
    private boolean locationEnabled = false;
    private int locationRetryCount = 0;
    private int locationRetryLimit = 5;
    private static final String TAG = "StoreSelectionActivity";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("In on Connected() --> Google APi client");
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000);
        startLocationUpdates();
    }

    public void startLocationUpdates(){

        if(locationEnabled){
            if (ActivityCompat.checkSelfPermission(StoreSelectionActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                String[] requiredPermission = {ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(StoreSelectionActivity.this,requiredPermission,RC_LOCATION_PERMISSION);
            }else {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }

        }else{
            enableLocationSettings();
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("In on ConnectionFailed() --> Google APi client");
        createNoLocView(getResources().getString(R.string.no_connection_message));
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("In on ConnectionSuspended() --> Google APi client");
        createNoLocView(getResources().getString(R.string.no_connection_message));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        setTheme(R.style.SplashTheme);
        super.onCreate(savedInstanceState);
        //Conenct Google API client. Will receive call back. See appropriate method for success or faliure
        connectGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationRetryCount = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, StoreSelectionActivity.this);
            mGoogleApiClient.disconnect();
        }
        ahttpClient.cancelAllRequests(true);

    }
    // Need to add code for on Pause and on Resume
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        switch (requestCode) {
            case RC_SCAN_BARCODE_STORE:
                if (bundle.containsKey("Barcode")) {
                    Barcode barcode = bundle.getParcelable("Barcode");
                    Log.d(TAG,"=====> Control returned from Scan Barcode Activity. Barcode : " + barcode.displayValue);
                    findStoreByBarcode(barcode.displayValue);
                }
                break;
            case RC_CHECK_SETTING: // Response from location enabled
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        locationEnabled = true;
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RC_LOCATION_PERMISSION:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startLocationUpdates();
                }
                else  {
                    createNoLocView(getResources().getString(R.string.no_loc_perm_denied));
                }
        }
    }

    /**
     * Utility Method to enable location settings
     *
     * */
    public void enableLocationSettings(){

        LocationRequest request = new LocationRequest().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(request);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            //Location Setting Result Handler
            @Override
            public void onResult(LocationSettingsResult result) {
                System.out.println("Location Setting result");
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        locationEnabled = true;
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(StoreSelectionActivity.this,RC_CHECK_SETTING);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                          Log.d(TAG,e.getLocalizedMessage());
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        System.out.println("Resolution not possible");
                        createNoLocView(getResources().getString(R.string.no_loc_message));
                        break;
                }
            }
        });

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void findStoreByLocation(final Location location){
          if(location!=null && !storeMatched) {
                  Log.d(TAG,"Location Update Received : "+ location.getLatitude() + " : " + location.getLongitude());
                  String locSearchEP = getString(R.string.storeSearchURL);
                  RequestParams params = new RequestParams();
                  params.put("lattitude", location.getLatitude());
                  params.put("longitude", location.getLongitude());
                  params.put("context", "STORE_IN_CURRENT_LOC");
                  System.out.println("Sending request to store location");
                  ahttpClient.setMaxRetriesAndTimeout(3,1000);
                  ahttpClient.get(locSearchEP, params, new JsonHttpResponseHandler() {

                      @Override
                      public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                          System.out.println("In onSuccess SelectStore");
                          try {
                              // Unique store found
                              if (response.length() == 1) {
                                  selectedStore = new Store();
                                  JSONObject store = response.getJSONObject(0);
                                  System.out.println("Store ID -->"+store.getString("id"));
                                  System.out.println("Store title -->"+store.getString("title"));
                                  System.out.println("Display address -->"+store.getString("displayAddress"));
                                  System.out.println("Setting store details");
                                  selectedStore.setId(store.getString("id"));
                                  selectedStore.setTitle(store.getString("title"));
                                  selectedStore.setDisplayAddress(store.getString("displayAddress"));
                                  System.out.println("Before launching cart activity");
                                  launchCartActivity();
                              }
                          } catch (JSONException je) {
                              je.printStackTrace();
                          } catch (Exception e) {
                              e.printStackTrace();
                          }


                      }

                      public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errResponse) {
                          System.out.println("In onFaliure SelectStore");
                          Log.d(TAG,statusCode+" "+errResponse,throwable);
                          LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, StoreSelectionActivity.this);
                          createNoLocView(getResources().getString(R.string.no_loc_message));

                      }
                  });

          }

    }

    public void findStoreByBarcode(String barcode){
        //Get Product Details

        String barcodeSearchEP = "http://5b33f7c6.ngrok.io/store/barcodesearch/%s";
        barcodeSearchEP = String.format(barcodeSearchEP, barcode);
        System.out.println(barcodeSearchEP);
        progressBar = (ProgressBar) findViewById(R.id.progressBarStoreScan);

        progressBar.setVisibility(View.VISIBLE);

        ahttpClient.get(barcodeSearchEP, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                selectedStore = new Store();
                try{
                    selectedStore.setDisplayAddress(response.getString("displayAddress"));
                    selectedStore.setId(response.getString("id"));
                    progressBar.setVisibility(View.GONE);
                    launchCartActivity();
                }catch(JSONException je ){
                    je.printStackTrace();
                }catch(Exception e){
                    e.printStackTrace();
                }



            }
        });

    }


    //Methods to launch applications activities. scanType should be a predefined constant for store or product(i.e.RC_SCAN_BARCODE_STORE etc.)
    public void launchScanBarcode(int scanType){
        Intent barcodeScanIntent = new Intent(this,ScanBarcodeActivity.class);
        startActivityForResult(barcodeScanIntent, scanType);
    }

    public void launchCartActivity(){
        System.out.println("Launching cart activity");
        if(selectedStore!=null){
            System.out.println("Launching cart activity");
            Intent cartActivityIntent = new Intent(this,CartActivity.class);
            cartActivityIntent.putExtra("StoreId",selectedStore.getId());
            cartActivityIntent.putExtra("StoreTitle",selectedStore.getTitle());
            cartActivityIntent.putExtra("StoreDisplayAddress", selectedStore.getDisplayAddress());

            startActivity(cartActivityIntent);
        }

    }

    // Call back handler for receiving location updates
    @Override
    public void onLocationChanged(Location location) {

        System.out.println("Location Update received. Accuracy : "+ location.getAccuracy());
        locationRetryCount++;
        findStoreByLocation(location);
    }

    //Create the no location view
    public void createNoLocView(String message) {
        setTheme(R.style.AppTheme);
        setContentView(R.layout.no_loc_store_selection);
        ((TextView)findViewById(R.id.noLocMessage)).setText(message);
        //Set listener for the on store listener
        Button scanQRStore = (Button) findViewById(R.id.scanQrStore);
        //Need to add code to find locaiton from the QR code from the service
        /*scanQRStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchScanBarcode(RC_SCAN_BARCODE_STORE);

            }
        });*/

        Button findLocation = (Button) findViewById(R.id.findLocation);
        findLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Clicked find location");
                connectGoogleApiClient();
            }
        });
/*
        findViewById(R.id.findLocation).setOnClickListener();
        if(viewId ==  R.layout.no_loc_store_selection) {

        }*/
    }

    public void connectGoogleApiClient() {
        System.out.println("In connectGoogleApiClient()");

        if (mGoogleApiClient == null) {
            System.out.println("GoogleApiClient null....Creating client");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        //Connect the google API client
        System.out.println("GoogleApiClient not null");
        mGoogleApiClient.connect();
        System.out.println("Connection done");

    }


}
