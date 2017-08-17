package com.smartcheckout.poc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.smartcheckout.poc.R;
import com.smartcheckout.poc.adapters.CartListViewAdapter;
import com.smartcheckout.poc.models.Bill;
import com.smartcheckout.poc.models.CartItem;
import com.smartcheckout.poc.models.Product;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

public class CartActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private ListView cartListView;
    //Store details
    private String storeId;
    private String storeTitle;
    private String storeDisplayAddress;
    private Bill bill = null;


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
                    closePayment();

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


            //Initialize the scan button and its clickListener
            FloatingActionButton fabScan = (FloatingActionButton) findViewById(R.id.fabScan);
            fabScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchBarcodeScanner();

                }
            });

            //Intiiate the cart checkout floating action and listener
            FloatingActionButton fabCheckOut = (FloatingActionButton) findViewById(R.id.fabCheckOut);
            fabCheckOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchPayment();

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

    public Bill createBill() {

        List<CartItem> cartList = cartAdapter.getCartItemList();
        float totalAmount = 0, savings = 0;
        for (CartItem cartItem : cartList) {
            System.out.println("cart item price -->" + cartItem.getProduct().getSellingPrice());
            System.out.println("cart item quantity -->" + cartItem.getQuantity());
            System.out.println("cart item savings -->" + cartItem.getProduct().getSavings());
            totalAmount += (cartItem.getQuantity() * cartItem.getProduct().getSellingPrice());
            savings += (cartItem.getQuantity() * cartItem.getProduct().getSavings());
        }
        System.out.println("Total amount -->" + totalAmount);
        System.out.println("cart item quantity -->" + savings);
        return new Bill(totalAmount, savings);

    }

    public void launchBarcodeScanner() {
        emulatorCounter++;
        System.out.println("In launchBarcodeScanner");
        //Launch the bar scanner activity
       /* Intent barcodeScanIntent = new Intent(this,ScanBarcodeActivity.class);
        startActivityForResult(barcodeScanIntent,RC_SCAN_BARCODE);*/

        //Bypassing scan activity to directly hit the service and get dummy data. Should remove this portion in actual app
        populateDummyScanProd();
    }

    public void launchPayment() {

        // Set the content payment view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        //Calcualte the total bill
        bill = createBill();
        ((TextView)paymentView.findViewById(R.id.totalAmount)).setText(""+bill.calTotalAMountPaid());
        ((TextView)paymentView.findViewById(R.id.saving)).setText(""+bill.getSavings());
        paymentView.setAlpha(0f);
        paymentView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        paymentView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);
    }

    public void closePayment() {
        paymentView.setVisibility(View.GONE);
    }

    public void checkoutAndPay(){
        if(bill != null){
            Intent paymentIntent = new Intent(this, PaymentActivity.class);
            paymentIntent.putExtra("amount",bill.calTotalAMountPaid());
            paymentIntent.putExtra("name", storeTitle);
            paymentIntent.putExtra("currency","USD");
            startActivity(paymentIntent);
        }

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

}
