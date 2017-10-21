package com.smartcheckout.poc.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.smartcheckout.poc.util.CommonUtils;
import com.smartcheckout.poc.util.SharedPreferrencesUtil;
import com.smartcheckout.poc.util.StateData;
import com.smartcheckout.poc.util.Currency;
import com.smartcheckout.poc.util.TransactionStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

import static com.smartcheckout.poc.constants.constants.RC_SCAN_BARCODE_ITEM;

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
    private Button fabCheckOut;
    private Button payButton;



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
    private  Context ctx= this.getApplication();


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
            payButton = (Button)findViewById(R.id.payButton);
            payButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try
                    {
                        persistTransactionData(true,TransactionStatus.CHECKOUT);
                    }
                    catch (Exception e)
                    {
                        // TODO: handle
                    }
                }
            });

            payButton.setVisibility(View.INVISIBLE);

            //Display details of the store
            System.out.println("CartActivity --> Store title -->" + storeTitle);
            ((TextView) findViewById(R.id.storeTitle)).setText(storeTitle);
            ((TextView) findViewById(R.id.storeAddress)).setText(storeDisplayAddress);

            boolean newTransaction = false;
            // coming back from payment view
            if(inputBundle.containsKey("TransactionId") && inputBundle.get("TransactionId") != null)
            {
                StateData.transactionId = inputBundle.getString("TransactionId");
                Log.d(TAG, "Retrieve Existing transaction " + inputBundle.get("TransactionId") );

            }
            // retrieving from saved instance
//            else if (SharedPreferrencesUtil.getStringPreference(this,"TransactionId") != null)
//            {
//                StateData.transactionId =  SharedPreferrencesUtil.getStringPreference(this,"TransactionId");
//                Log.d(TAG, "Retrieve Existing transaction " + SharedPreferrencesUtil.getStringPreference(this,"TransactionId")  );
//            }

            // first time activity is created
            else
            {
                newTransaction =  true;
                Log.d(TAG, "creating transcation for first time"   );

                try {
                    persistTransactionData(false,TransactionStatus.INITIATED);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(!newTransaction) {

                String getTrnsEP = "http://ec2-54-191-68-157.us-west-2.compute.amazonaws.com:8080/transaction";
                RequestParams params = new RequestParams();
                params.put("trnsId", StateData.transactionId);

                ahttpClient.get(getTrnsEP, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                        Log.d(TAG, "Retrieved Cart " + response.toString() );
                        Type listType = new TypeToken<ArrayList<CartItem>>(){}.getType();
                        List<CartItem> cartList = null;
                        try {
                            cartList = new Gson().fromJson(response.getJSONArray("cart").toString(), listType);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        cartAdapter = new CartListViewAdapter(CartActivity.this.getApplicationContext(),cartList);
                        //Link the cartList and the adapter
                        cartListView = (ListView) findViewById(R.id.cartList);
                        cartListView.setAdapter(cartAdapter);
                        cartAdapter.registerDataSetObserver(new DataSetObserver() {
                            @Override
                            public void onChanged() {
                                updateAndShowBill();
                            }
                        });
                        //Set swipe to delete functionlaity
                        setSwipeDelItem();
                        if(cartList!= null && !cartList.isEmpty()){
                            updateAndShowBill();
                        }
                    }
                });

            }
            else{
                cartAdapter = new CartListViewAdapter(this);
                //Link the cartList and the adapter
                cartListView = (ListView) findViewById(R.id.cartList);
                cartListView.setAdapter(cartAdapter);
                cartAdapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        updateAndShowBill();
                    }
                });
                //Set swipe to delete functionlaity
                setSwipeDelItem();
            }

            //Initialize the scan button and its clickListener
            fabScan = (FloatingActionButton) findViewById(R.id.fabScan);
            fabScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchBarcodeScanner();

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
    public void onStop() {

        super.onStop();
        try {
            persistTransactionData(false,TransactionStatus.SUSPENDED);
            // if the cart is empty dont remember this transaction
            if(cartAdapter != null && cartAdapter.getCartItemList() != null && cartAdapter.getCount() > 0)
            {
                SharedPreferrencesUtil.setStringPreference(this,"TransactionId",StateData.transactionId);
                SharedPreferrencesUtil.setDatePreference(this,"TransactionUpdatedDate", CommonUtils.getCurrentDate());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bundle = data.getExtras();
        switch (requestCode) {
            case RC_SCAN_BARCODE_ITEM:
                if (resultCode == RESULT_OK) {
                    Barcode barcode = bundle.getParcelable("Barcode");
                    if(barcode != null) {
                        System.out.println("=====> Control returned from Scan Barcode Activity. Barcode : " + barcode.displayValue);
                        handleBarcode(barcode.displayValue);
                    }
                }
                else if(resultCode == RESULT_CANCELED )
                {
                    String reason = bundle.getString("Reason");
                    if(reason != null && reason.equalsIgnoreCase("Timeout"))
                        Toast.makeText(this,getResources().getString(R.string.toast_scan_timedout),Toast.LENGTH_LONG).show();
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

    public void updateAndShowBill(){
        calculateBill();
        payButton.setText("PAY $"+bill.getTotalAmount());
        if(payButton.getVisibility() == View.INVISIBLE)
            payButton.setVisibility(View.VISIBLE);

    }

    public void launchBarcodeScanner() {
        emulatorCounter++;
        System.out.println("In launchBarcodeScanner");
        //Launch the bar scanner activity
        /*Intent barcodeScanIntent = new Intent(this,ScanBarcodeActivity.class);
        barcodeScanIntent.putExtra("requestCode",RC_SCAN_BARCODE_ITEM);
        startActivityForResult(barcodeScanIntent,RC_SCAN_BARCODE_ITEM);*/

        //Bypassing scan activity to directly hit the service and get dummy data. Should remove this portion in actual app
        populateDummyScanProd();
    }

    public JSONArray getCart() throws JSONException {
        JSONObject cartDetailsJson = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        Log.d(TAG, "getcart " );


        if(cartAdapter == null || cartAdapter.getCartItemList() == null || cartAdapter.getCartItemList().isEmpty())
            return null;
        for(CartItem item : cartAdapter.getCartItemList()) {

            Product product = item.getProduct();
            JSONObject productObj = new JSONObject();
            productObj.put("uniqueId",product.getUniqueId());
            productObj.put("barcode",product.getBarcode());
            productObj.put("title",product.getTitle());
            productObj.put("description",product.getDescription());
            productObj.put("category",product.getCategory());
            productObj.put("retailPrice",product.getRetailPrice());
            productObj.put("discount",product.getDiscount());

            JSONObject cartObj = new JSONObject();
            cartObj.put("product",productObj);
            cartObj.put("quantity",item.getQuantity());


            jsonArray.put(cartObj);
        }

        return jsonArray;

    }

    public void persistTransactionData(final boolean launchPayment,TransactionStatus status) throws JSONException {
        String currentTS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date());
        //Store object preparation
        JSONObject store = new JSONObject();
        store.put("id", StateData.storeId);
        JSONObject jBill = null;
        if(bill != null) {
            //Bill object preparation
            jBill = new JSONObject();
            jBill.put("subTotal", bill.getSubTotal());
            jBill.put("tax", bill.getTax());
            jBill.put("currency", bill.getCurrency().toString());
            jBill.put("total", bill.getTotalAmount());
            jBill.put("savings", bill.getSavings());

            StateData.billAmount = bill.getTotalAmount();

        }
        JSONArray cart = getCart();


        if (StateData.transactionId == null) {
            // Persist transaction to
            String createTrnsEP = "http://ec2-54-191-68-157.us-west-2.compute.amazonaws.com:8080/transaction/create";
            JSONObject createTrnsReq = new JSONObject();

            createTrnsReq.put("trnsDate", currentTS);
            createTrnsReq.put("status", status);
            createTrnsReq.put("createTS", currentTS);
            createTrnsReq.put("updateTS", currentTS);
            createTrnsReq.put("store", store);
            createTrnsReq.put("cart",cart);
            createTrnsReq.put("bill", jBill);

            StateData.status = TransactionStatus.CHECKOUT;
            // Invoking create transaction
            StringEntity requestEntity = new StringEntity(createTrnsReq.toString(), ContentType.APPLICATION_JSON);
            Log.d(TAG, "Invoking create transaction. Request : " + createTrnsReq.toString());
            ahttpClient.post(this, createTrnsEP, requestEntity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        //Setting transaction id into state data
                        Log.d(TAG, "Create Transaction Successful");
                        StateData.transactionId = response.getString("trnsId");
                        Log.d(TAG, "Generated transaction id : " + StateData.transactionId);
                        if(launchPayment) {
                            Intent paymentIntent = new Intent(CartActivity.this, PaymentActivity.class);
                            startActivity(paymentIntent);
                        }
                    } catch (Exception e) {
                        // TODO: throw custom exception
                    }

                }

            });
        } else {
            JSONObject updateTransReq = new JSONObject();
            updateTransReq.put("trnsId", StateData.transactionId);
            updateTransReq.put("status", status);
            updateTransReq.put("trnsDate", currentTS);
            updateTransReq.put("updateTS", currentTS);
            updateTransReq.put("store", store);
            updateTransReq.put("bill", jBill);
            updateTransReq.put("cart",cart);

            HttpEntity requestEntity = new StringEntity(updateTransReq.toString(), ContentType.APPLICATION_JSON);
            Log.d(TAG, "Update transaction status triggered. " + updateTransReq.toString());

            String updateTrnsEP = "http://ec2-54-191-68-157.us-west-2.compute.amazonaws.com:8080/transaction/update";
            ahttpClient.post(this, updateTrnsEP, requestEntity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        //Setting transaction id into state data
                        Log.d(TAG, "Update Transaction Successful");
                        StateData.transactionId = response.getString("trnsId");
                        Log.d(TAG, "Updated transaction id : " + StateData.transactionId);

                        if(launchPayment) {
                            Intent paymentIntent = new Intent(CartActivity.this, PaymentActivity.class);
                            startActivity(paymentIntent);
                        }
                    } catch (Exception e) {
                        // TODO: throw custom exception

                    }
                }
            });
        }

    }
    
    public void populateDummyScanProd() {

        if ((emulatorCounter % 10) == 0)
            handleBarcode("8901725133979");
        else if ((emulatorCounter % 10) == 1)
            handleBarcode("8901719255144");
        else if ((emulatorCounter % 10) == 2)
            handleBarcode("8901063093416");
        else if ((emulatorCounter % 10) == 3)
            handleBarcode("8904004400946");
        else if ((emulatorCounter % 10) == 4)
            handleBarcode("02289902");
        else if ((emulatorCounter % 10) == 5)
            handleBarcode("022000005120");
        else if ((emulatorCounter % 10) == 6)
            handleBarcode("762111962614");
        else if ((emulatorCounter % 3) == 7)
            handleBarcode("05929031187");
        else if ((emulatorCounter % 3) == 8)
            handleBarcode("833091700241");
        else if ((emulatorCounter % 3) == 9)
            handleBarcode("8904208600913");
        else
            handleBarcode("83309170036");
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

    @Override
    public void onBackPressed() {
    }
}
