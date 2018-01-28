package com.smartcheckout.poc.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.smartcheckout.poc.models.Transaction;
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

public class BillViewActivity extends Activity {

    private AsyncHttpClient ahttpClient = new AsyncHttpClient();

    private ListView mListView;
    private TextView storeName ;
     private TextView addressLine1 ;
    private TextView addressLine2 ;
    private TextView addressLine3;
    private TextView phoneNo ;
    private ImageView myImage;
    private TextView totalAmount ;
    private TextView tvTranscationDate ;
    private TextView totalSavings ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE | Window.FEATURE_SWIPE_TO_DISMISS);
        setContentView(R.layout.bill_view);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = CommonUtils.getScreenHeight(this) - 100;
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        mListView = (ListView) findViewById(R.id.cart_list);
        storeName = (TextView) findViewById(R.id.storeName);
        addressLine1 = (TextView) findViewById(R.id.addressLine1);
        addressLine2 = (TextView) findViewById(R.id.addressLine2);
        addressLine3 = (TextView) findViewById(R.id.addressLine3);
        phoneNo = (TextView) findViewById(R.id.phone);
        myImage = (ImageView) findViewById(R.id.trnsQRCode);
        totalAmount = (TextView) findViewById(R.id.totalAmount);
        tvTranscationDate = (TextView) findViewById(R.id.trnsDate);
        totalSavings = (TextView) findViewById(R.id.totalSavings);

        Intent initiatingIntent = getIntent();
        Bundle inputBundle = initiatingIntent.getExtras();

        final String transactionId = inputBundle.getString("TransactionId");

        // check if the transaction is cached in StateData

        if(StateData.transactionReceipt != null && StateData.transactionReceipt.getTrnsId().equalsIgnoreCase(transactionId))
        {
            Transaction transaction = StateData.transactionReceipt;

            if(transaction.getCart() != null && transaction.getStore() != null && transaction.getBill() != null)
                restoreView(transaction.getTrnsId(),transaction.getCart(),transaction.getStore(),transaction.getBill(),new Date(transaction.getTrnsDate()));

            return;
        }

        // if there is some issues with the cached receipt, retrieve from the backend

        findViewById(R.id.billlayout).setVisibility(View.GONE);

        RequestParams rqstparams = new RequestParams();
        rqstparams.put("trnsId", transactionId);

        final ProgressDialog nDialog = new ProgressDialog(BillViewActivity.this);
        nDialog.setMessage("Loading..");
        nDialog.setIndeterminate(true);
        nDialog.setCancelable(false);
        nDialog.show();

        ahttpClient.get(TRANSACTION_URL, rqstparams, new JsonHttpResponseHandler() {
            @SuppressLint("NewApi")
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Type listType = new TypeToken<ArrayList<CartItem>>() {}.getType();
                List<CartItem> cartList = null;
                Bill bill = null;
                Store store = null;
                try {
                    cartList = new Gson().fromJson(response.getJSONArray("cart").toString(), listType);
                    store = new Gson().fromJson(response.getJSONObject("store").toString(), Store.class);
                    bill = new Gson().fromJson(response.getJSONObject("bill").toString(), Bill.class);
                    Date transcationDate = new Date(response.getLong("trnsDate"));

                    nDialog.dismiss();
                    findViewById(R.id.billlayout).setVisibility(View.VISIBLE);
                    restoreView(transactionId,cartList,store,bill,transcationDate);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public void restoreView(String transactionId, List<CartItem> cartList,Store store,Bill bill,Date transcationDate )
    {

        Bitmap myBitmap = QRCode.from(transactionId).withColor(0xFF000000,0x00FFFFFF).bitmap();
        myImage.setImageBitmap(myBitmap);

        final View headerView = getLayoutInflater().inflate(R.layout.bill_item,null);

        ((TextView)headerView.findViewById(R.id.product)).setTypeface(null,Typeface.BOLD);
        ((TextView)headerView.findViewById(R.id.price)).setTypeface(null,Typeface.BOLD);
        ((TextView)headerView.findViewById(R.id.quantity)).setTypeface(null,Typeface.BOLD);
        ((TextView)headerView.findViewById(R.id.amount)).setTypeface(null,Typeface.BOLD);

        ((TextView)headerView.findViewById(R.id.product)).setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        ((TextView)headerView.findViewById(R.id.quantity)).setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        mListView.addHeaderView(headerView);


        BillListViewAdapter billViewAdapter = new BillListViewAdapter(BillViewActivity.this,cartList);
        mListView.setAdapter(billViewAdapter);

        // Set store details
        storeName.setText(store.getTitle());
        addressLine1.setText(store.getAddress().getAddressLine1());

        if(store.getAddress().getAddressLine2() != null && !store.getAddress().getAddressLine2().isEmpty())
        {
            addressLine2.setText(store.getAddress().getAddressLine2());
            addressLine3.setText(store.getAddress().getCity()+","+store.getAddress().getState()+","+store.getAddress().getPostalCode());

        }
        else
        {
            addressLine2.setText(store.getAddress().getCity()+","+store.getAddress().getState()+","+store.getAddress().getPostalCode());
        }

        phoneNo.setText("Phone: "+store.getPhone());


        totalAmount.setText("\u20B9 "+String.valueOf(bill.getTotal()));
        totalSavings.setText("\u20B9 " +String.valueOf(bill.getSavings()));


        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy");
        tvTranscationDate.setText(formatter.format(transcationDate));
    }

}
