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
    /*private ImageView locationButton;
    private LinearLayout confirmationBox;*/
   // private ProgressBar progressBar;
    //private TextView status;

    private static final int RC_SCAN_BARCODE = 0;
    private static final int RC_LOCATION_PERMISSION = 1;
    private static final int RC_CHECK_SETTING = 2;

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
        ((TextView)findViewById(R.id.no_loc_message)).setText(R.string.no_loc_scan_qr_store);
        setContentView(R.layout.no_loc_store_selection);
    }

    @Override
    public void onConnectionSuspended(int i) {
        ((TextView)findViewById(R.id.no_loc_message)).setText(R.string.no_loc_scan_qr_store);
        setContentView(R.layout.no_loc_store_selection);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        //Connect the google API client
        mGoogleApiClient.connect();




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
            case RC_SCAN_BARCODE:
                if (bundle.containsKey("Barcode")) {
                    Barcode barcode = bundle.getParcelable("Barcode");
                    System.out.println("=====> Control returned from Scan Barcode Activity. Barcode : " + barcode.displayValue);
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
                    ((TextView)findViewById(R.id.no_loc_message)).setText(R.string.no_loc_perm_denied);
                    setContentView(R.layout.no_loc_store_selection);
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
                            ((TextView)findViewById(R.id.no_loc_message)).setText(R.string.no_loc_scan_qr_store);
                            setContentView(R.layout.no_loc_store_selection);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            ((TextView)findViewById(R.id.no_loc_message)).setText(R.string.no_loc_scan_qr_store);
                            setContentView(R.layout.no_loc_store_selection);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        System.out.println("Resolution not possible");
                        ((TextView)findViewById(R.id.no_loc_message)).setText(R.string.no_loc_scan_qr_store);
                        setContentView(R.layout.no_loc_store_selection);
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
              if (locationRetryCount < locationRetryLimit) {
                  Log.d(TAG,"Location Update Received : "+ location.getLatitude() + " : " + location.getLongitude());
                  String locSearchEP = getString(R.string.storeSearchURL);
                  RequestParams params = new RequestParams();
                  // ****Needs to be uncommented for getting the actual location
                  params.put("lattitude", location.getLatitude());
                  params.put("longitude", location.getLongitude());
                  // ****Hard coding latitude and longitude for proceeding with dev
                 // params.put("lattitude", 25);
                  //params.put("longitude", -25);
                  params.put("context", "STORE_IN_CURRENT_LOC");
                  System.out.println("Sending request to store location");
                 // progressBar.setVisibility(View.VISIBLE);
                 // confirmationBox.setVisibility(View.GONE);

                  ahttpClient.get(locSearchEP, params, new JsonHttpResponseHandler() {

                      @Override
                      public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                          System.out.println("Before try block");
                          try {
                              // Unique store found
                              if (response.length() == 1) {
                                  selectedStore = new Store();
                                  JSONObject store = response.getJSONObject(0);
                                  System.out.println("Display address -->"+store.getString("displayAddress"));
                                  System.out.println("Store ID -->"+store.getString("id"));
                                  System.out.println("Setting display address and store Id");
                                  selectedStore.setDisplayAddress(store.getString("displayAddress"));
                                  selectedStore.setId(store.getString("id"));
                                 // status.setText(selectedStore.getDisplayAddress() + "\nCurrent Loc :" + location.getLatitude() + " : " + location.getLongitude());
                                  System.out.println("Before launching cart activity");
                                  launchCartActivity();
                                  //confirmationBox.setVisibility(View.VISIBLE);
                                  //progressBar.setVisibility(View.GONE);
                              }
                          } catch (JSONException je) {
                              je.printStackTrace();
                          } catch (Exception e) {
                              e.printStackTrace();
                          }


                      }
                  });
              }else{
                  LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, StoreSelectionActivity.this);
                  locationRetryCount = 0;
                  ((TextView)findViewById(R.id.no_loc_message)).setText(R.string.no_loc_scan_qr_store);
                  setContentView(R.layout.no_loc_store_selection);

                  //progressBar.setVisibility(View.GONE);
                  //confirmationBox.setVisibility(View.VISIBLE);
                  //status.setText("Unable to find store with location. Try with QR code option");
                  // Need to add code for QR code

              }
          }

    }

    public void findStoreByBarcode(String barcode){
        //Get Product Details

        String barcodeSearchEP = "http://5b33f7c6.ngrok.io/store/barcodesearch/%s";
        barcodeSearchEP = String.format(barcodeSearchEP, barcode);
        System.out.println(barcodeSearchEP);

        //progressBar.setVisibility(View.VISIBLE);
        //confirmationBox.setVisibility(View.GONE);

        ahttpClient.get(barcodeSearchEP, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                selectedStore = new Store();
                try{
                    selectedStore.setDisplayAddress(response.getString("displayAddress"));
                    selectedStore.setId(response.getString("id"));
                    //status.setText(selectedStore.getDisplayAddress());
                    launchScanBarcode();
                    //progressBar.setVisibility(View.GONE);
                    //confirmationBox.setVisibility(View.VISIBLE);
                }catch(JSONException je ){
                    je.printStackTrace();
                }catch(Exception e){
                    e.printStackTrace();
                }



            }
        });

    }


    //Methods to launch applications activities
    public void launchScanBarcode(){
        Intent barcodeScanIntent = new Intent(this,ScanBarcodeActivity.class);
        startActivityForResult(barcodeScanIntent,RC_SCAN_BARCODE);
    }

    public void launchCartActivity(){
        System.out.println("Launching cart activity");
        if(selectedStore!=null){
            System.out.println("Launching cart activity");
            Intent cartActivityIntent = new Intent(this,CartActivity.class);
            cartActivityIntent.putExtra("StoreDisplay", selectedStore.getDisplayAddress());
            cartActivityIntent.putExtra("StoreId",selectedStore.getId());
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


}
