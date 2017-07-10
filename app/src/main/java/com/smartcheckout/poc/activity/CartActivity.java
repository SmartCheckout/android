package com.smartcheckout.poc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.smartcheckout.poc.R;
import com.smartcheckout.poc.adapters.CartListViewAdapter;
import com.smartcheckout.poc.models.Bill;
import com.smartcheckout.poc.models.CartItem;
import com.smartcheckout.poc.models.Product;
import com.smartcheckout.poc.util.PropertiesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class CartActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private ListView cartListView;

    private String storeId;
    private String storeDisplay;
    private static final int RC_SCAN_BARCODE = 0;
    private AsyncHttpClient ahttpClient = new AsyncHttpClient();
    private CartListViewAdapter cartAdapter;
    private double totalBill;
    private double totalSavings;
    private View transactionView;
    private View paymentView;
    private int mShortAnimationDuration;
    private BottomNavigationView bottomNavigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("Creating Cart activity");
        super.onCreate(savedInstanceState);
        Intent initiatingIntent = getIntent();
        Bundle inputBundle = initiatingIntent.getExtras();
        if(inputBundle.containsKey("StoreId") && inputBundle.containsKey("StoreDisplay")){
            storeId = inputBundle.getString("StoreId");
            storeDisplay = inputBundle.getString("StoreDisplay");

            //Set the cart layout & hide the payment view
            setContentView(R.layout.activity_cart);
            transactionView = findViewById(R.id.transactionContainer);
            paymentView = findViewById(R.id.paymentContainer);
            mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

            //Intitially hide the payment view
            paymentView.setVisibility(View.GONE);

            //Display the address of the store
            ((TextView)findViewById(R.id.storeDetails)).setText(storeDisplay);
            CartListViewAdapter cartListViewAdapter = new CartListViewAdapter(this);

            //Link the cartList and the adapter
            cartListView = (ListView)findViewById(R.id.cartList);
            cartListView.setAdapter(cartListViewAdapter);




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


            // Set the bottom navigation view
            /*bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_search:
                            mTextMessage.setText(R.string.title_home);
                            return true;
                        case R.id.navigation_cart:
                            mTextMessage.setText(R.string.title_dashboard);
                            return true;
                        case R.id.navigation_accountSettings:
                            mTextMessage.setText(R.string.title_notifications);
                            return true;
                    }
                    return false;
                }
            });*/


        }else{
            startActivity(new Intent(this,StoreSelectionActivity.class));
        }
        cartAdapter = (CartListViewAdapter) cartListView.getAdapter();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bundle = data.getExtras();
        switch(requestCode){
            case RC_SCAN_BARCODE:
                if(bundle.containsKey("Barcode")){
                    Barcode barcode = bundle.getParcelable("Barcode");
                    System.out.println("=====> Control returned from Scan Barcode Activity. Barcode : "+barcode.displayValue);
                    handleBarcode(barcode.displayValue);
                }
                break;
        }
    }

    public void handleBarcode(String barcode){
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

                        System.out.println("Product unique id -->"+response.getString("uniqueId"));
                        System.out.println("Product barcode -->"+response.getString("barcode"));
                        System.out.println("Product title -->"+response.getString("title"));
                        System.out.println("Product description -->"+response.getString("description"));
                        System.out.println("Product category -->"+response.getString("category"));
                        System.out.println("Product retailPrice -->"+response.getDouble("retailPrice"));
                        System.out.println("Product retailPrice -->"+response.getDouble("discount"));
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
                        CartItem cartItem = new CartItem(product,1,"");
                        cartAdapter.addItem(cartItem);

                } catch (JSONException je) {
                    je.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });


    }

    public Bill createBill() {

        Bill bill = new Bill();
        float taxRate = 0;
        try {
            taxRate = Float.parseFloat(PropertiesUtil.getProperty("taxRate",getApplicationContext()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<CartItem> cartList  = cartAdapter.getCartItemList();
        for (CartItem cartItem : cartList) {
            bill.totalAmount += cartItem.getQuantity()*cartItem.getProduct().getRetailPrice();
            bill.savings += cartItem.getProduct().getSavings();
        }
        bill.taxes = bill.totalAmount*(1-taxRate);
        bill.amountPaid = bill.getTotalAmount() - bill.getSavings() - bill.getTaxes();
        return bill;

    }
    public void launchBarcodeScanner(){

        //Launch the bar scanner activity
        /*Intent barcodeScanIntent = new Intent(this,ScanBarcodeActivity.class);
        startActivityForResult(barcodeScanIntent,RC_SCAN_BARCODE);*/

        System.out.println("In launchBarcodeScanner");

        //Bypassing scan activity to directly hit the service and get dummy data. Should remove this portion in actual app
        handleBarcode("5790");
    }

    public void launchPayment() {

        // Set the content payment view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        //Calcualte the total bill
        Bill bill = createBill();
        ((TextView)paymentView.findViewById(R.id.totalAmount)).setText(""+bill.getAmountPaid());
        ((TextView)paymentView.findViewById(R.id.savingLabel)).setText(""+bill.getSavings());
        paymentView.setAlpha(0f);
        paymentView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        paymentView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);
    }

}