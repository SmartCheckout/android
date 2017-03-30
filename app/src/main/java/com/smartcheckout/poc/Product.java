package com.example.swetha_swaminathan.smartshopper;

/**
 * Created by Swetha_Swaminathan on 2/28/2017.
 */

public class Product {

    private String productName;
    private int quatity;
    private String barcodeValue;

    @Override
    public boolean equals(Object o) {


        String product = (String) o;

        if (productName != null && productName.equals(o))
            return true;
        return  false;

    }

    @Override
    public int hashCode() {

        return 1;
    }

    public String getBarcodeValue() {
        return barcodeValue;
    }

    public void setBarcodeValue(String barcodeValue) {
        this.barcodeValue = barcodeValue;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuatity() {
        return quatity;
    }

    public void setQuatity(int quatity) {
        this.quatity = quatity;
    }
}
