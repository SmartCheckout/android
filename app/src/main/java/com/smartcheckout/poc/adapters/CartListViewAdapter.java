package com.smartcheckout.poc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smartcheckout.poc.R;
import com.smartcheckout.poc.models.CartItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by yeshwanth on 4/4/2017.
 */

public class CartListViewAdapter extends BaseAdapter {

    // View lookup cache
    private static class ViewHolder {
        ImageView productImg;
        TextView productTitle;
        TextView productDesc;
        TextView sellingPrice;
        NumberPicker quantity;
    }

    private Context context;
    private List<CartItem> cartItemList;
    private HashMap<String, CartItem> itemTracker;

    //Creates an adapter from an already defined list
    public CartListViewAdapter(Context context, List<CartItem> cartItemList) {
        this.cartItemList = cartItemList;
        this.context = context;
        itemTracker = new HashMap<>();
        for (CartItem item : cartItemList) {
            itemTracker.put(item.getProduct().getBarcode(), item);
        }
    }

    //Creates an adapter with a new list
    public CartListViewAdapter(Context context) {
        this.context = context;
        this.cartItemList = new ArrayList<CartItem>();
        itemTracker = new HashMap<>();
    }

    public CartItem findItemInCart(CartItem cartItem) {
        return itemTracker.get(cartItem.getProduct().getBarcode());
    }

    public void addItem(CartItem cartItem) {
        CartItem iteminCart = findItemInCart(cartItem);
        if (iteminCart == null) {
            cartItemList.add(cartItem);
            itemTracker.put(cartItem.getProduct().getBarcode(), cartItem);
        } else {
            int currentQty = iteminCart.getQuantity();
            iteminCart.setQuantity(currentQty + cartItem.getQuantity());
        }

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cartItemList.size();
    }

    @Override
    public Object getItem(int i) {
        if (i < cartItemList.size())
            return cartItemList.get(i);
        else
            return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;

        //Get the cart item for this position
        final CartItem item = (CartItem) getItem(position);

        if (item != null) {

        if (view == null) {
            // If there's no view to re-use, inflate a brand new view for row

            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.cart_item, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.productImg = (ImageView) view.findViewById(R.id.productImg);
            viewHolder.productTitle = (TextView) view.findViewById(R.id.productTitle);
            viewHolder.productDesc = (TextView) view.findViewById(R.id.productDesc);
            //Set attributes for quantity here so that they are also cached by viewHolder
            viewHolder.quantity = (NumberPicker) view.findViewById(R.id.quantity);
            viewHolder.quantity.setMinValue(1);
            viewHolder.quantity.setMaxValue(25);
            viewHolder.quantity.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                    item.setQuantity(newVal);
                    notifyDataSetChanged();

                }
            });
            viewHolder.sellingPrice = (TextView) view.findViewById(R.id.sellingPrice);
            // Cache the viewHolder object inside the fresh view
            view.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) view.getTag();
        }


            DecimalFormat df = new DecimalFormat("#.00");

            //Populate product details ** Need to set image...discuss with Yesh
            viewHolder.productTitle.setText(item.getProduct().getTitle());
            viewHolder.productDesc.setText(item.getProduct().getDescription());
            viewHolder.sellingPrice.setText(df.format(item.getQuantity() * item.getProduct().getSellingPrice()));
            viewHolder.quantity.setValue(item.getQuantity());
            System.out.println("Product image url -->"+item.getProduct().getImagePath());
            loadProductImage(item.getProduct().getImagePath(), viewHolder.productImg);

            // Check with Yesh whether savings need to be shown at item level
            /*if(savings >0){
                ((TextView)view.findViewById(R.id.itemSavings)).setText("Saved : $" + df.format(savings));
                ((TextView)view.findViewById(R.id.itemSavings)).setVisibility(View.VISIBLE);
            }else{
                ((TextView)view.findViewById(R.id.itemSavings)).setVisibility(View.GONE);
            }*/


        }


        return view;
    }

    public List<CartItem> getCartItemList() {
        return cartItemList;
    }

    //Load product image from firebase using glide for caching
    public void loadProductImage(String url, ImageView prodImageView) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageReference = storage.getReferenceFromUrl(url);
        // Load the image using Glide and Firebase UI as the model
        Glide.with(getApplicationContext()).using(new FirebaseImageLoader()).load(imageReference).into(prodImageView);

    }
}
