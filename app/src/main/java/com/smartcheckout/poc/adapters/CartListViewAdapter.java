package com.smartcheckout.poc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
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
        Spinner quantity;
        ArrayAdapter<Integer> quantityAdapter;
    }

    private Context context;
    private List<CartItem> cartItemList;
    private HashMap<String, CartItem> itemTracker;
    private ArrayList<Integer> quantList;
    private float totalAmount = 0;
    private float totalSavings = 0;

    //Creates an adapter from an already defined list
    public CartListViewAdapter(Context context, List<CartItem> cartItemList) {
        super();
        this.context = context;
        this.quantList = new ArrayList<Integer>();
        this.cartItemList = new ArrayList<CartItem>();
        this.itemTracker = new HashMap<>();

        for (int i=1; i <= 5; i++) {
            this.quantList.add(i);
        }
        for (CartItem item : cartItemList) {
            addItem(item);
        }
    }

    //Creates an adapter with a new list
    public CartListViewAdapter(Context context) {
        this.context = context;
        this.cartItemList = new ArrayList<CartItem>();
        this.itemTracker = new HashMap<>();
        this.quantList = new ArrayList<Integer>();
        for (int i=1; i <= 5; i++) {
            this.quantList.add(i);
        }
    }

    public CartItem findItemInCart(CartItem cartItem) {
        return itemTracker.get(cartItem.getProduct().getBarcode());
    }

    public void addItem(CartItem cartItem) {
        CartItem iteminCart = findItemInCart(cartItem);
        if (iteminCart == null) {
            // Item not in cart
            cartItemList.add(cartItem);
            itemTracker.put(cartItem.getProduct().getBarcode(), cartItem);

            // Updating the total amount and total savings of the contents of the cart
            totalAmount += cartItem.getQuantity() * cartItem.getProduct().getSellingPrice();
            totalSavings += cartItem.getQuantity()* cartItem.getProduct().getSavings();

        } else {
            // Item alreay in cart
            // Updating the total amount and total savings of the contents of the cart based on the increase in quantity
            int currentQty = iteminCart.getQuantity();
            int qtyIncrease = cartItem.getQuantity();
            iteminCart.setQuantity(currentQty + qtyIncrease);
            totalAmount += qtyIncrease * cartItem.getProduct().getSellingPrice();
            totalSavings += qtyIncrease* cartItem.getProduct().getSavings();
        }

        notifyDataSetChanged();
    }

    public float getTotalAmount(){return totalAmount;}
    public float getTotalSavings(){return totalSavings;}

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

        System.out.println("-----In getView----");

        //Get the cart item for this position
        final CartItem item = (CartItem) getItem(position);
        System.out.println("Item ----> "+item.getProduct().getTitle());

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
            viewHolder.quantity = (Spinner) view.findViewById(R.id.quantity);
            viewHolder.quantityAdapter = new ArrayAdapter<Integer>(getApplicationContext(), R.layout.spinner_item, quantList);
            // Specify the layout to use when the list of choices appears
            viewHolder.quantityAdapter.setDropDownViewResource(R.layout.spinner_drop_down);
            viewHolder.quantity.setAdapter(viewHolder.quantityAdapter);
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
            //viewHolder.quantity.setValue(item.getQuantity());
            //Add listener and update quantity
            viewHolder.quantity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> spinnerParent, View spinnerView, int spinnerPosition, long id) {
                    //System.out.println("-----In Spinner onItemSelected-----");
                    //System.out.println("Item ----->" + item.getProduct().getTitle());
                    //System.out.println("Spinner position ----->" + spinnerPosition);

                    int newQuantity = (Integer)spinnerParent.getItemAtPosition(spinnerPosition);
                    int qtyDifference = newQuantity - item.getQuantity();
                    totalAmount += qtyDifference* item.getProduct().getSellingPrice();
                    totalSavings += qtyDifference* item.getProduct().getSavings();

                    item.setQuantity(newQuantity);
                    notifyDataSetChanged();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            //Set the item quantity in spinner
            viewHolder.quantity.setSelection(viewHolder.quantityAdapter.getPosition(item.getQuantity()));

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

    //Removes the specified item
    public void remove(CartItem cartItem) {
      // Updating the total amount and savings based on the item being removed
        totalSavings -= cartItem.getQuantity()*cartItem.getProduct().getSavings();
        totalAmount -= cartItem.getQuantity()*cartItem.getProduct().getSellingPrice();
        cartItemList.remove(cartItem);
        itemTracker.remove(cartItem.getProduct().getBarcode());
    }
}
