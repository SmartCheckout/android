package com.smartcheckout.poc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;
import com.smartcheckout.poc.R;
import com.smartcheckout.poc.adapters.CartListViewAdapter;
import com.smartcheckout.poc.models.CartItem;
import com.smartcheckout.poc.models.Product;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private ListView cartListView;

    private String storeId;
    private String storeDisplay;
    private static final int RC_SCAN_BARCODE = 0;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

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

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("Creating Cart activity");
        super.onCreate(savedInstanceState);
        Intent initiatingIntent = getIntent();
        Bundle inputBundle = initiatingIntent.getExtras();
        if(inputBundle.containsKey("StoreId") && inputBundle.containsKey("StoreDisplay")){
            storeId = inputBundle.getString("StoreId");
            storeDisplay = inputBundle.getString("StoreDisplay");
            //Set the cart layout
            setContentView(R.layout.activity_cart);
            //Display the address of the store
            ((TextView)findViewById(R.id.storeDetails)).setText(storeDisplay);
            CartListViewAdapter cartListViewAdapter = new CartListViewAdapter(this,new ArrayList<CartItem>());

            cartListView = (ListView)findViewById(R.id.cartList);
            cartListView.setAdapter(cartListViewAdapter);
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAdd);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchBarcodeScanner();

                }
            });

        }else{
            startActivity(new Intent(this,StoreSelectionActivity.class));
        }


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
        CartListViewAdapter cartAdapter = (CartListViewAdapter) cartListView.getAdapter();

        Product product = new Product("100",barcode,"Daily Pure","2% Fat reduced milk, 1 Galon","Poultry",2.99,10.0f);
        CartItem cartItem1 = new CartItem(product,1,"");
        cartAdapter.addItem(0,cartItem1);
    }

    public void launchBarcodeScanner(){
        Intent barcodeScanIntent = new Intent(this,ScanBarcodeActivity.class);
        startActivityForResult(barcodeScanIntent,RC_SCAN_BARCODE);
    }

}
