package com.smartcheckout.poc.models;

import com.smartcheckout.poc.util.PropertiesUtil;

import java.io.IOException;
import java.math.BigDecimal;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by rahul on 7/3/2017.
 */

public class Bill {

    private float totalAmount; // Total amount by adding MRP of all itens in the cart
    private float savings;     // Total savings by calculating the disounts and offers
    private float tax;       // T
    private float amountPaid; //Final amount to be paid

    public Bill(float totalAmount, float savings) {
        this.totalAmount = totalAmount;
        this.savings = savings;
        calTax();
        calAmountPaid();
    }

    public float getTotalAmount() {
        return round(this.totalAmount,2);
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public float getSavings() {
        return round(this.savings,2);
    }

    public void setSavings(float savings) {
        this.savings = savings;
    }

    public float getTax() {
       return round(this.tax,2);
    }

    private void calTax() {

        float taxRate = 0;
        try {
            taxRate = Float.parseFloat(PropertiesUtil.getProperty("taxRate",getApplicationContext()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.tax = this.totalAmount*(1-taxRate);

    }

    public float getAmountPaid() {
        return round(this.amountPaid,2);
    }

    public void calAmountPaid() {
        calTax();
        this.amountPaid = this.getTotalAmount() - this.getSavings() - this.getTax();
    }

    private float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
