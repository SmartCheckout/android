package com.smartcheckout.poc.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.smartcheckout.poc.R;
import com.smartcheckout.poc.models.Store;
import com.smartcheckout.poc.util.SharedPreferrencesUtil;
import com.smartcheckout.poc.util.StateData;
import com.smartcheckout.poc.util.TransactionStatus;

import net.glxn.qrgen.android.QRCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

import static com.smartcheckout.poc.constants.constants.TRANSACTION_UPDATE_EP;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    private static String TAG = "PaymentActivity";

    private AsyncHttpClient ahttpClient = new AsyncHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_success);


        if(preRequisiteCheck()){
            Log.d(TAG,"Payment pre-requisites met. Initiating payment");
            launchRazorPay(this);
        }else{
            // Pre-requsites not met. returning to cart activity.
            Toast.makeText(this, "Not eligible for payment", Toast.LENGTH_SHORT).show();
            launchCartActivity();
        }

    }

    public boolean preRequisiteCheck(){
        return StateData.billAmount != 0.0f
                && StateData.storeName != null
                && StateData.transactionId != null;
    }

    public void launchRazorPay(Activity srcActivity){
        try{
            //Razor pay checkout object preparation
            Checkout checkout = new Checkout();
            JSONObject updateTransReq = new JSONObject();

            Float amount = StateData.billAmount *100;
            checkout.setImage(R.drawable.cart_launch_icon);
            checkout.setKeyID("rzp_test_wnre6SUsbTyIJO");
            checkout.setFullScreenDisable(true);

            // Razor pay options
            JSONObject options = new JSONObject();
            options.put("key", "rzp_test_wnre6SUsbTyIJO");
            options.put("name",StateData.storeName);
            //options.put("description", StateData.transactionId);
            options.put("amount", amount.intValue());
            options.put("currency", "INR");

            // Update transaction status
            updateTransReq.put("trnsId", StateData.transactionId);
            updateTransReq.put("status", TransactionStatus.PAYMENT_INITIATED);
            updateTransaction(new StringEntity(updateTransReq.toString(), ContentType.APPLICATION_JSON));
            Log.d(TAG,"Update transaction status triggered. " + updateTransReq.toString());

            // Trigger Razor Pay
            checkout.open(srcActivity, options);
            Log.d(TAG,"Payment request initiated");


        }catch(JSONException je){
            Log.e(TAG, je.getMessage());
        }
        catch(Exception e){
            Log.e(TAG, ": " + e.getMessage());
        }
    }


    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Payment successful. Gateway payment ref : " + razorpayPaymentID);

        JSONObject updateTransReq = new JSONObject();
        try{

            // Updating payment success view
            setContentView(R.layout.payment_success);

            // Display transaction id QR code
            Bitmap myBitmap = QRCode.from(StateData.transactionId).withColor(0xFF000000,0x00FFFFFF).bitmap();

            ImageView myImage = (ImageView) findViewById(R.id.trnsQRCode);
            TextView amountView = ((TextView) findViewById(R.id.amountMessage1));
            if(amountView != null && amountView.getText() != null)
            {
                String updatedText = amountView.getText().toString().concat(" "+StateData.billAmount.toString());
                amountView.setText(updatedText);
            }

            myImage.setImageBitmap(myBitmap);
            Log.d(TAG,"Transaction bitmap generated");

            // Updating transaction status and payment reference
            JSONObject payment = new JSONObject();
            payment.put("paymentGateway","RAZOR_PAY");
            payment.put("paymentRef",razorpayPaymentID);
            payment.put("paymentStatus","SUCCESS");

            updateTransReq.put("trnsId", StateData.transactionId);
            updateTransReq.put("status", TransactionStatus.PAYMENT_SUCCESSFUL);
            updateTransReq.put("payment", new JSONArray().put(payment));
            updateTransaction(new StringEntity(updateTransReq.toString(), ContentType.APPLICATION_JSON));

            //Flushing transaction from shared preference
            SharedPreferrencesUtil.setStringPreference(this, "TransactionId",null);
            SharedPreferrencesUtil.setDatePreference(this, "TransactionUpdatedDate",null);

            Log.d(TAG,"Update transaction status triggered. " + updateTransReq.toString());

        }catch(Exception e){
            //Todo
        }
    }

    @Override
    public void onPaymentError(int code, String response) {

        switch (code)
        {
            case Checkout.NETWORK_ERROR:
                Log.i("tag","Network error from Razor Pay");
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.toast_not_connected_internet),Toast.LENGTH_SHORT).show();
                break;
            case Checkout.INVALID_OPTIONS:
                Log.i("tag","Invalid Options from Razor Pay");
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.toast_payment_exited),Toast.LENGTH_SHORT).show();
                break;
            case Activity.RESULT_CANCELED:
                Log.i("tag","Payment cancelled from Razor Pay");
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.toast_payment_exited),Toast.LENGTH_SHORT).show();
                break;
        }

        JSONObject updateTransReq = new JSONObject();

        try {
            JSONObject payment = new JSONObject();
            updateTransReq.put("trnsId", StateData.transactionId);
            updateTransReq.put("status", TransactionStatus.PAYMENT_FAILURE);
            updateTransReq.put("payment", new JSONArray().put(payment));
            Log.d(TAG,"Update transaction status triggered. " + updateTransReq.toString());

            updateTransaction(new StringEntity(updateTransReq.toString(), ContentType.APPLICATION_JSON));

        }catch(Exception e){
            //Todo
        }
        Log.e(TAG,response);

        launchCartActivity();
    }

    public void updateTransaction(HttpEntity requestEntity){
        ahttpClient.post(this, TRANSACTION_UPDATE_EP, requestEntity, "application/json" , new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG,String.format("Update transaction returned success. Response code : %d", statusCode));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errResponse) {
                Log.e(TAG,String.format("Update transaction returned failure. Status Code : %d. Message : %s", statusCode, errResponse.toString()));

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
    }

}
