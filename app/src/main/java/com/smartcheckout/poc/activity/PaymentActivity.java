package com.smartcheckout.poc.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.smartcheckout.poc.R;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    private static String TAG = "PaymentActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Intent initiatingIntent = getIntent();
        Bundle inputBundle = initiatingIntent.getExtras();
        if(inputBundle!= null && inputBundle.containsKey("amount") && inputBundle.containsKey("name")){
            final Activity srcActivity = this;
            try{
                Checkout checkout = new Checkout();
                Float amount = (Float)inputBundle.get("amount") *100;
                checkout.setImage(R.drawable.cart_launch_icon);
                checkout.setKeyID("rzp_test_wnre6SUsbTyIJO");
                checkout.setFullScreenDisable(true);

                JSONObject options = new JSONObject();
                options.put("key", "rzp_test_wnre6SUsbTyIJO");
                options.put("name",inputBundle.get("name"));
                options.put("description", "Order #123456");
                options.put("amount", amount.intValue());
                options.put("currency", "USD");

                checkout.open(srcActivity, options);
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
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, code, Toast.LENGTH_SHORT).show();
        Log.e(TAG,response);
    }
}
