package com.smartcheckout.poc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.smartcheckout.poc.R;
import com.smartcheckout.poc.adapters.CartListViewAdapter;
import com.smartcheckout.poc.adapters.SwipeDismissListViewTouchListener;
import com.smartcheckout.poc.models.Bill;
import com.smartcheckout.poc.models.CartItem;
import com.smartcheckout.poc.models.Product;
import com.smartcheckout.poc.util.StateData;
import com.smartcheckout.poc.util.Currency;
import com.smartcheckout.poc.util.TransactionStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

public class CartActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private ListView cartListView;
    //Store details
    private String storeId;
    private String storeTitle;
    private String storeDisplayAddress;
    private Bill bill = null;

    //Floating action buttons
    private FloatingActionButton fabScan;
    private FloatingActionButton fabCheckOut;


    private static final int RC_SCAN_BARCODE = 0;
    private AsyncHttpClient ahttpClient = new AsyncHttpClient();
    private CartListViewAdapter cartAdapter;
    private double totalBill;
    private double totalSavings;
    private View transactionView;
    private View paymentView;
    private View mainContainerView;
    private int mShortAnimationDuration;
    private BottomNavigationView bottomNavigationView;
    private int emulatorCounter = 0;
    private String TAG = "CartActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("Creating Cart activity");
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        Intent initiatingIntent = getIntent();
        Bundle inputBundle = initiatingIntent.getExtras();
        if (inputBundle.containsKey("StoreId") && inputBundle.containsKey("StoreDisplayAddress") && inputBundle.containsKey("StoreTitle")) {
            storeId = inputBundle.getString("StoreId");
            storeTitle = inputBundle.getString("StoreTitle");
            storeDisplayAddress = inputBundle.getString("StoreDisplayAddress");

            //Set the cart layout & hide the payment view
            setContentView(R.layout.activity_cart);
            transactionView = findViewById(R.id.transactionContainer);
            paymentView = findViewById(R.id.paymentContainer);
            mainContainerView = findViewById(R.id.mainContainer);
            mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

            //Intitially hide the payment view
            paymentView.setVisibility(View.GONE);
            findViewById(R.id.payButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkoutAndPay();
                }
            });

            //Close payment view when user clicks back on the main cart screen
            mainContainerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closePaymentView();

                }
            });

            //Display details of the store
            System.out.println("CartActivity --> Store title -->" + storeTitle);
            ((TextView) findViewById(R.id.storeTitle)).setText(storeTitle);
            ((TextView) findViewById(R.id.storeAddress)).setText(storeDisplayAddress);

            //Link the cartList and the adapter
            cartAdapter = new CartListViewAdapter(this);
            cartListView = (ListView) findViewById(R.id.cartList);
            cartListView.setAdapter(cartAdapter);

            //Set swipe to delete functionlaity
            setSwipeDelItem();

            //Initialize the scan button and its clickListener
            fabScan = (FloatingActionButton) findViewById(R.id.fabScan);
            fabScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchBarcodeScanner();

                }
            });

            //Intiiate the cart checkout floating action and listener
            fabCheckOut = (FloatingActionButton) findViewById(R.id.fabCheckOut);
            fabCheckOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchBillView();

                }
            });

            //Create the Bottom Navigation View
            createBottomNavView();


        } else {
            startActivity(new Intent(this, StoreSelectionActivity.class));
        }
        //cartAdapter = (CartListViewAdapter) cartListView.getAdapter();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bundle = data.getExtras();
        switch (requestCode) {
            case RC_SCAN_BARCODE:
                if (bundle.containsKey("Barcode")) {
                    Barcode barcode = bundle.getParcelable("Barcode");
                    System.out.println("=====> Control returned from Scan Barcode Activity. Barcode : " + barcode.displayValue);
                    handleBarcode(barcode.displayValue);
                }
                break;
        }
    }

    public void handleBarcode(String barcode) {
        //Get Product Details
        String productSearchURL = getString(R.string.productSearchURL);
        RequestParams params = new RequestParams();

        params.put("id", barcode);
        System.out.println("Sending request to search product");
        //progressBar.setVisibility(View.VISIBLE);

        ahttpClient.get(productSearchURL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                System.out.println("In onSuccess and Before try block");

                try {
                    // Unique product found

                        /*System.out.println("Product unique id -->"+response.getString("uniqueId"));
                        System.out.println("Product barcode -->"+response.getString("barcode"));
                        System.out.println("Product title -->"+response.getString("title"));
                        System.out.println("Product description -->"+response.getString("description"));
                        System.out.println("Product category -->"+response.getString("category"));
                        System.out.println("Product retailPrice -->"+response.getDouble("retailPrice"));
                        System.out.println("Product retailPrice -->"+response.getDouble("discount"));*/

                    //Hardcoding image url for testing....need to change it to load dynamically
                    //String imagePath = "gs://smartcheckout-2846e.appspot.com/product_icons/item1.jpg";
                    Product product = new Product(response.getString("uniqueId"),
                            response.getString("barcode"),
                            response.getString("title"),
                            response.getString("description"),
                            response.getString("category"),
                            response.getDouble("retailPrice"),
                            Float.valueOf(response.getString("discount")));
                    System.out.println("Created product");
                    //progressBar.setVisibility(View.GONE);
                    // Add the product to the Cart
                    CartItem cartItem = new CartItem(product, 1);
                    System.out.println("Created cart item");
                    cartAdapter.addItem(cartItem);
                    System.out.println("Added cart item to adapter");

                } catch (JSONException je) {
                    je.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });


    }

    public void calculateBill() {
        if(this.bill == null){
            this.bill = new Bill(cartAdapter.getTotalAmount(), cartAdapter.getTotalSavings(), 0, Currency.USD);
            this.bill.notifyChanges();
        }else {
            this.bill.setSubTotal(cartAdapter.getTotalAmount());
            this.bill.setSavings(cartAdapter.getTotalSavings());
            this.bill.notifyChanges();
        }
    }

    public void launchBarcodeScanner() {
        emulatorCounter++;
        System.out.println("In launchBarcodeScanner");
        //Launch the bar scanner activity
        Intent barcodeScanIntent = new Intent(this,ScanBarcodeActivity.class);
        startActivityForResult(barcodeScanIntent,RC_SCAN_BARCODE);

        //Bypassing scan activity to directly hit the service and get dummy data. Should remove this portion in actual app
        //populateDummyScanProd();
    }

    public void launchBillView() {


        // Set the content payment view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        //Calcualte the total bill
        calculateBill();
        ((TextView)paymentView.findViewById(R.id.totalAmount)).setText(""+bill.getTotalAmount());
        ((TextView)paymentView.findViewById(R.id.saving)).setText(""+bill.getSavings());

        //make the floating aciton buttons disappear
        fabScan.setVisibility(View.GONE);
        fabCheckOut.setVisibility(View.GONE);

        // Set the content payment view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        paymentView.setAlpha(0f);
        paymentView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        paymentView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);
    }

    public void closePaymentView() {
        paymentView.setVisibility(View.GONE);
        //Make the floating action buttons visible
        //make the floating aciton buttons disappear
        fabScan.setVisibility(View.VISIBLE);
        fabCheckOut.setVisibility(View.VISIBLE);
    }

    /*
    * Triggered when the user clicks the pay button from bill view
    * Prerequisite : bill is calculated.
    * Actions : persists transaction into db and initiates payment activity
    * */
    public void checkoutAndPay(){
        if(bill != null){
            // Persist transaction to
            String createTrnsEP = "http://ec2-54-191-68-157.us-west-2.compute.amazonaws.com:8080/transaction/create";
            JSONObject createTrnsReq = new JSONObject();
            StateData.billAmount = bill.getTotalAmount();
            try{
                String currentTS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date());
                //Store object preparation
                JSONObject store = new JSONObject();
                store.put("id",StateData.storeId);

                //Bill object preparation
                JSONObject jBill = new JSONObject();
                jBill.put("subTotal",bill.getSubTotal());
                jBill.put("tax", bill.getTax());
                jBill.put("currency", bill.getCurrency().toString());
                jBill.put("total",bill.getTotalAmount());
                jBill.put("savings",bill.getSavings());

                createTrnsReq.put("trnsDate", currentTS );
                createTrnsReq.put("status", TransactionStatus.CHECKOUT);
                createTrnsReq.put("createTS", currentTS);
                createTrnsReq.put("updateTS", currentTS);
                createTrnsReq.put("store", store);
                createTrnsReq.put("bill", jBill);

            }catch(JSONException je){
                je.printStackTrace();
            }



            StateData.status = TransactionStatus.CHECKOUT;
            // Invoking create transaction
            StringEntity requestEntity = new StringEntity(createTrnsReq.toString(), ContentType.APPLICATION_JSON);
            Log.d(TAG,"Invoking create transaction. Request : "+ createTrnsReq.toString());
            ahttpClient.post(this, createTrnsEP, requestEntity, "application/json" , new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                   try{
                       //Setting transaction id into state data
                       Log.d(TAG, "Create Transaction Successful");
                       StateData.transactionId = response.getString("trnsId");
                       Log.d(TAG, "Generated transaction id : "+  StateData.transactionId);

                       Intent paymentIntent = new Intent(CartActivity.this, PaymentActivity.class);
                       startActivity(paymentIntent);
                   }catch(Exception e){

                   }

                }

            });
        }

    }

    public void persistTransactionData(){

    }
    
    public void populateDummyScanProd() {

        if ((emulatorCounter % 3) == 0)
            handleBarcode("5790");
        else if ((emulatorCounter % 3) == 1)
            handleBarcode("022000005120");
        else
            handleBarcode("02289902");
    }

    public void createBottomNavView() {

        // Set the bottom navigation view
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        //Set on click listener for the post sign out screen
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_search:
                        return true;
                    case R.id.navigation_cart:
                        return true;
                    case R.id.navigation_accountSettings:
                        System.out.println("Bottom navigation --> settings case");
                        //setContentView(R.layout.settings);
                        startActivity(new Intent(CartActivity.this, SettingsActivity.class));
                        //Set the on click listener for sign out
                        return true;
                }
                return false;
            }
        });
    }

    public void setSwipeDelItem() {
        // Create a ListView-specific touch listener. ListViews are given special treatment because
        // by default they handle touches for their list items... i.e. they're in charge of drawing
        // the pressed state (the list selector), handling list item clicks, etc.
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        cartListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView cartListView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    cartAdapter.remove((CartItem)cartAdapter.getItem(position));
                                }
                                cartAdapter.notifyDataSetChanged();
                            }
                        });
        cartListView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        cartListView.setOnScrollListener(touchListener.makeScrollListener());


    }

}
