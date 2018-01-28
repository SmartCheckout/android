package com.smartcheckout.poc.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartcheckout.poc.R;
import com.smartcheckout.poc.activity.BillViewActivity;
import com.smartcheckout.poc.activity.PaymentActivity;
import com.smartcheckout.poc.activity.TransactionHistoryActivity;
import com.smartcheckout.poc.models.CartItem;
import com.smartcheckout.poc.models.Transaction;
import com.smartcheckout.poc.util.StateData;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Swetha_Swaminathan on 11/13/2017.
 */

public class TransactionListViewAdapter extends BaseAdapter {

    List<Transaction> mDataSource = null;
    private Context mContext;
    private LayoutInflater mInflater;

    public TransactionListViewAdapter(Context context, List<Transaction> transactionList)
    {
        mContext = context;
        mDataSource = transactionList;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = mInflater.inflate(R.layout.transaction_item, parent, false);


        final Transaction transaction = (Transaction) getItem(position);

        TextView storeName =  (TextView)rowView.findViewById(R.id.storeName);
        storeName.setText(transaction.getStore().getTitle());

        TextView transactionDate = (TextView)rowView.findViewById(R.id.transactionDate);
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy");
        transactionDate.setText(formatter.format(transaction.getTrnsDate()));

        TextView billAmount = (TextView)rowView.findViewById(R.id.billAmount);
        billAmount.setText(String.valueOf(transaction.getBill().getTotal()));

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent billViewIntent = new Intent(mContext, BillViewActivity.class);
                billViewIntent.putExtra("TransactionId", transaction.getTrnsId());
                StateData.transactionReceipt = transaction;
                mContext.startActivity(billViewIntent);
            }
        });

        return rowView;
    }
}
