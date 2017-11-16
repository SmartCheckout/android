package com.smartcheckout.poc.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartcheckout.poc.R;
import com.smartcheckout.poc.models.CartItem;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Swetha_Swaminathan on 11/12/2017.
 */

public class BillListViewAdapter extends BaseAdapter {

    List<CartItem> mDataSource = null;
    private Context mContext;
    private LayoutInflater mInflater;
    public BillListViewAdapter(Context context, List<CartItem> cartItemList)
    {
        mContext = context;
        mDataSource = cartItemList;
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

        DecimalFormat df = new DecimalFormat("#.00");

        View rowView = mInflater.inflate(R.layout.bill_item, parent, false);

        if(position%2 == 0)
            rowView.setBackgroundColor(Color.LTGRAY);
        CartItem item = (CartItem) getItem(position);

        TextView product = (TextView) rowView.findViewById(R.id.product);
        product.setText(item.getProduct().getTitle());

        TextView price = (TextView) rowView.findViewById(R.id.price);
        price.setText("₹ "+df.format(item.getProduct().getSellingPrice()));

        TextView quantity = (TextView) rowView.findViewById(R.id.quantity);
        quantity.setText(String.valueOf(item.getQuantity()));

        TextView amount = (TextView) rowView.findViewById(R.id.amount);
        amount.setText("₹ "+df.format(item.getQuantity() * item.getProduct().getSellingPrice()));

        return rowView;
    }
}
