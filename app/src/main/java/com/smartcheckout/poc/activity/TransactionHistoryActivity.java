package com.smartcheckout.poc.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.smartcheckout.poc.R;
import com.smartcheckout.poc.adapters.BillListViewAdapter;
import com.smartcheckout.poc.adapters.TransactionListViewAdapter;
import com.smartcheckout.poc.models.CartItem;
import com.smartcheckout.poc.models.Transaction;
import com.smartcheckout.poc.util.StateData;
import com.smartcheckout.poc.util.TransactionStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.smartcheckout.poc.constants.constants.TRANSACTION_SEARCH_EP;
import static com.smartcheckout.poc.constants.constants.TRANSACTION_URL;

/**
 * Created by Swetha_Swaminathan on 11/13/2017.
 */

public class TransactionHistoryActivity extends Activity {

    private AsyncHttpClient ahttpClient = new AsyncHttpClient();
    private ListView mListPastTransactionView;
    private ListView mListCurrentTransactionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_view);
        findViewById(R.id.transaction_history_view).setVisibility(View.GONE);

        mListPastTransactionView = (ListView) findViewById(R.id.transaction_list);

        final ProgressDialog nDialog = new ProgressDialog(TransactionHistoryActivity.this);
        nDialog.setMessage("Loading..");
        nDialog.setIndeterminate(true);
        nDialog.setCancelable(false);
        nDialog.show();

        RequestParams rqstparams = new RequestParams();
        rqstparams.put("status",TransactionStatus.APPROVED);
        rqstparams.put("userId", StateData.userId);


        ahttpClient.get(TRANSACTION_SEARCH_EP, rqstparams, new JsonHttpResponseHandler() {
            @SuppressLint("NewApi")

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                Type listType = new TypeToken<ArrayList<Transaction>>() {}.getType();
                List<Transaction> transactionList = new Gson().fromJson(response.toString(), listType);

                Log.d("Shwe",transactionList.size()+" ");
                TransactionListViewAdapter transactionListViewAdapter = new TransactionListViewAdapter(TransactionHistoryActivity.this,transactionList);
                mListPastTransactionView.setAdapter(transactionListViewAdapter);
                nDialog.dismiss();
                findViewById(R.id.transaction_history_view).setVisibility(View.VISIBLE);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("Shwe",errorResponse.toString());
                nDialog.dismiss();
            }
        });

        mListCurrentTransactionView = (ListView) findViewById(R.id.pending_transaction_list);

        RequestParams rqstparams1 = new RequestParams();
        rqstparams1.put("status",TransactionStatus.PAYMENT_SUCCESSFUL);
        rqstparams1.put("userId", StateData.userId);


        ahttpClient.get(TRANSACTION_SEARCH_EP, rqstparams1, new JsonHttpResponseHandler() {
            @SuppressLint("NewApi")

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                Type listType = new TypeToken<ArrayList<Transaction>>() {}.getType();
                List<Transaction> transactionList = new Gson().fromJson(response.toString(), listType);

                TransactionListViewAdapter transactionListViewAdapter = new TransactionListViewAdapter(TransactionHistoryActivity.this,transactionList);
                mListCurrentTransactionView.setAdapter(transactionListViewAdapter);
                nDialog.dismiss();
                findViewById(R.id.transaction_history_view).setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("Shwe",errorResponse.toString());
                nDialog.dismiss();
            }
        });
    }
}
