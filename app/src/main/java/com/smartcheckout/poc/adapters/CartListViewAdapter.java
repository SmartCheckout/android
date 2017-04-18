package com.smartshopper.androidpoc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.smartshopper.androidpoc.R;
import com.smartshopper.androidpoc.models.CartItem;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.jar.Pack200;

/**
 * Created by yeshwanth on 4/4/2017.
 */

public class CartListViewAdapter extends BaseAdapter {

    private Context context;
    private List<CartItem> cartItemList;
    private LayoutInflater inflater;
    private HashMap<String,CartItem> itemTracker;

    public CartListViewAdapter(Context context, List<CartItem> cartItemList) {
        this.cartItemList = cartItemList;
        this.context = context;
        itemTracker = new HashMap<>();
        for(CartItem item : cartItemList){
            itemTracker.put(item.getProduct().getBarcode(),item);
        }
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public CartItem findItemInCart(CartItem cartItem){
        return itemTracker.get(cartItem.getProduct().getBarcode());
    }

    public void addItem(int position,CartItem cartItem){
        CartItem iteminCart = findItemInCart(cartItem);
        if(iteminCart == null){
            cartItemList.add(position,cartItem);
            itemTracker.put(cartItem.getProduct().getBarcode(),cartItem);
        }else{
            int currentQty = iteminCart.getQuantity();
            iteminCart.setQuantity(currentQty+cartItem.getQuantity());
        }

        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return cartItemList.size();
    }

    @Override
    public Object getItem(int i) {
        if(i < cartItemList.size())
            return cartItemList.get(i);
        else
            return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null)
           view = inflater.inflate(R.layout.cart_item,null);

        final CartItem item = (CartItem)getItem(i);
        if(item!=null){
            DecimalFormat df = new DecimalFormat("#.00");

            //Populate product details
            NumberPicker qtyPicker  = (NumberPicker)view.findViewById(R.id.quantityPicker);
            qtyPicker.setMinValue(1);
            qtyPicker.setMaxValue(25);
            qtyPicker.setValue(item.getQuantity());
            qtyPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                    item.setQuantity(newVal);
                    notifyDataSetChanged();

                }
            });

            ((TextView)view.findViewById(R.id.productTile)).setText(item.getProduct().getTitle());
            ((TextView)view.findViewById(R.id.productDesc)).setText(item.getProduct().getDescription());
            ((TextView)view.findViewById(R.id.sellingPrice)).setText(df.format(item.getQuantity() * item.getProduct().getSellingPrice()));

            Double savings = item.getQuantity() * item.getProduct().getSavings();
            if(savings >0){
                ((TextView)view.findViewById(R.id.itemSavings)).setText("Saved : $" + df.format(savings));
                ((TextView)view.findViewById(R.id.itemSavings)).setVisibility(View.VISIBLE);
            }else{
                ((TextView)view.findViewById(R.id.itemSavings)).setVisibility(View.GONE);
            }

        }

        return view;
    }
}
