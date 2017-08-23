package com.smartcheckout.poc.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.smartcheckout.poc.R;
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

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    private static String TAG = "PaymentActivity";
    private AsyncHttpClient ahttpClient = new AsyncHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        if(StateData.billAmount != 0.0f && StateData.storeName != null && StateData.transactionId != null){
            //Update transaction status
            Log.d(TAG,"Payment pre-requisites met. Initiating payment");
            JSONObject updateTransReq = new JSONObject();
            try{
                updateTransReq.put("trnsId", StateData.transactionId);
                updateTransReq.put("status", TransactionStatus.PAYMENT_INITIATED);
                updateTransaction(new StringEntity(updateTransReq.toString(), ContentType.APPLICATION_JSON));
                Log.d(TAG,"Update transaction status triggered. " + updateTransReq.toString());
            }catch(Exception e){
                //Todo
            }

            final Activity srcActivity = this;
            try{
                Checkout checkout = new Checkout();
                Float amount = StateData.billAmount *100;
                checkout.setImage(R.drawable.cart_launch_icon);
                checkout.setKeyID("rzp_test_wnre6SUsbTyIJO");
                checkout.setFullScreenDisable(true);

                JSONObject options = new JSONObject();
                options.put("key", "rzp_test_wnre6SUsbTyIJO");
                options.put("name",StateData.storeName);
                options.put("description", StateData.transactionId);
                options.put("amount", amount.intValue());
                options.put("currency", "INR");

                checkout.open(srcActivity, options);
                Log.d(TAG,"Payment request initiated");
            }catch(JSONException je){
                Log.e(TAG, je.getMessage());
            }

        }else{
            Toast.makeText(this, "Amount not available in request", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Payment successful. Gateway payment ref : " + razorpayPaymentID);

        JSONObject updateTransReq = new JSONObject();
        JSONArray payments = new JSONArray();
        try{
            JSONObject payment = new JSONObject();
            payment.put("paymentGateway","RAZOR_PAY");
            payment.put("paymentRef",razorpayPaymentID);
            payment.put("paymentStatus","SUCCESS");

            updateTransReq.put("trnsId", StateData.transactionId);
            updateTransReq.put("status", TransactionStatus.PAYMENT_SUCCESSFUL);
            updateTransReq.put("payment", new JSONArray().put(payment));
            updateTransaction(new StringEntity(updateTransReq.toString(), ContentType.APPLICATION_JSON));
            Log.d(TAG,"Update transaction status triggered. " + updateTransReq.toString());

            Bitmap myBitmap = QRCode.from(StateData.transactionId).bitmap();
            ImageView myImage = (ImageView) findViewById(R.id.trnsQRCode);
            myImage.setImageBitmap(myBitmap);

            Log.d(TAG,"Transaction bitmap generated");
        }catch(Exception e){
            //Todo
        }
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, code, Toast.LENGTH_SHORT).show();
        Log.e(TAG,"Payment failure reqtured from gateway. " + response);

        JSONObject updateTransReq = new JSONObject();
        try{
            JSONObject payment = new JSONObject();
            payment.put("paymentGateway","RAZOR_PAY");;
            payment.put("paymentStatus",response);

            updateTransReq.put("trnsId", StateData.transactionId);
            updateTransReq.put("status", TransactionStatus.PAYMENT_FAILURE);
            updateTransReq.put("payment", new JSONArray().put(payment));

            updateTransaction(new StringEntity(updateTransReq.toString(), ContentType.APPLICATION_JSON));
            Log.d(TAG,"Update transaction status triggered. " + updateTransReq.toString());
        }catch(Exception e){
            //Todo
        }
        Log.e(TAG,response);
    }

    public void updateTransaction(HttpEntity requestEntity){
        String updateTrnsEP = "http://ec2-54-191-68-157.us-west-2.compute.amazonaws.com:8080/transaction/update";
        ahttpClient.post(this, updateTrnsEP, requestEntity, "application/json" , new JsonHttpResponseHandler(){
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
}
