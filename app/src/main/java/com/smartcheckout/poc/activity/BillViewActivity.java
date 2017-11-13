package com.smartcheckout.poc.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.smartcheckout.poc.R;
import com.smartcheckout.poc.adapters.BillListViewAdapter;
import com.smartcheckout.poc.models.Bill;
import com.smartcheckout.poc.models.CartItem;
import com.smartcheckout.poc.models.Store;
import com.smartcheckout.poc.util.CommonUtils;
import com.smartcheckout.poc.util.StateData;

import net.glxn.qrgen.android.QRCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.smartcheckout.poc.constants.constants.TRANSACTION_URL;

/**
 * Created by Swetha_Swaminathan on 11/12/2017.
 */

public class BillViewActivity extends AppCompatActivity {

    private AsyncHttpClient ahttpClient = new AsyncHttpClient();

    private ListView mListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bill_view);
        mListView = (ListView) findViewById(R.id.cart_list);
        Intent initiatingIntent = getIntent();
        Bundle inputBundle = initiatingIntent.getExtras();

        final String transactionId = inputBundle.getString("TransactionId");


        RequestParams params = new RequestParams();
        params.put("trnsId", transactionId);

        ahttpClient.get(TRANSACTION_URL, params, new JsonHttpResponseHandler() {
            @SuppressLint("NewApi")
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Type listType = new TypeToken<ArrayList<CartItem>>() {
                }.getType();
                List<CartItem> cartList = null;
                Bill bill = null;
                Store store = null;
                try {
                    cartList = new Gson().fromJson(response.getJSONArray("cart").toString(), listType);
                    bill = new Gson().fromJson(response.getJSONObject("bill").toString(), Bill.class);
                    store = new Gson().fromJson(response.getJSONObject("store").toString(), Store.class);
                    Date transcationDate = new Date(response.getLong("trnsDate"));

                    // Set store details
                    TextView storeName = (TextView) findViewById(R.id.storeName);
                    storeName.setText(store.getTitle());

                    TextView addressLine1 = (TextView) findViewById(R.id.addressLine1);
                    addressLine1.setText(store.getAddress().getAddressLine1());

                    TextView addressLine2 = (TextView) findViewById(R.id.addressLine2);
                    TextView addressLine3 = (TextView) findViewById(R.id.addressLine3);

                    if(store.getAddress().getAddressLine2() != null && !store.getAddress().getAddressLine2().isEmpty())
                    {
                        addressLine2.setText(store.getAddress().getAddressLine2());
                        addressLine3.setText(store.getAddress().getCity()+","+store.getAddress().getState()+","+store.getAddress().getPostalCode());

                    }
                    else
                    {
                        addressLine2.setText(store.getAddress().getCity()+","+store.getAddress().getState()+","+store.getAddress().getPostalCode());
                    }


                    TextView phoneNo = (TextView) findViewById(R.id.phone);
                    phoneNo.setText("Phone: "+store.getPhone());

                    // Set Transaction details

                    Bitmap myBitmap = QRCode.from(transactionId).withColor(0xFF000000,0x00FFFFFF).bitmap();
                    ImageView myImage = (ImageView) findViewById(R.id.trnsQRCode);
                    myImage.setImageBitmap(myBitmap);

                    TextView tvTranscationDate = (TextView) findViewById(R.id.trnsDate);
                    SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy");
                    tvTranscationDate.setText(formatter.format(transcationDate));

                    View headerView = getLayoutInflater().inflate(R.layout.bill_item,null);

                    ((TextView)headerView.findViewById(R.id.product)).setTypeface(null,Typeface.BOLD);
                    ((TextView)headerView.findViewById(R.id.price)).setTypeface(null,Typeface.BOLD);
                    ((TextView)headerView.findViewById(R.id.quantity)).setTypeface(null,Typeface.BOLD);
                    ((TextView)headerView.findViewById(R.id.amount)).setTypeface(null,Typeface.BOLD);

                    ((TextView)headerView.findViewById(R.id.product)).setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                    ((TextView)headerView.findViewById(R.id.price)).setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                    ((TextView)headerView.findViewById(R.id.quantity)).setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                    ((TextView)headerView.findViewById(R.id.amount)).setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);


                    mListView.addHeaderView(headerView);
                    BillListViewAdapter billViewAdapter = new BillListViewAdapter(BillViewActivity.this,cartList);
                    mListView.setAdapter(billViewAdapter);

                    TextView totalAmount = (TextView) findViewById(R.id.totalAmount);
                    totalAmount.setText("\u20B9 "+String.valueOf(bill.getTotal()));

                    TextView totalSavings = (TextView) findViewById(R.id.totalSavings);
                    totalSavings.setText("\u20B9 " +String.valueOf(bill.getSavings()));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void launchCartActivity()
    {
        Intent cartActivityIntent = new Intent(this,CartActivity.class);

        Store selectedStore = StateData.store;
        cartActivityIntent.putExtra("StoreId",selectedStore.getId());
        cartActivityIntent.putExtra("StoreTitle",selectedStore.getTitle());
        cartActivityIntent.putExtra("StoreDisplayAddress", selectedStore.getDisplayAddress());
        cartActivityIntent.putExtra("TransactionId", StateData.transactionId);

        startActivity(cartActivityIntent);
    }
    @Override
    public void onBackPressed() {
        //launchCartActivity();
    }
}
