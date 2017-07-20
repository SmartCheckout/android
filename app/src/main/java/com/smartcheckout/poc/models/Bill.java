package com.smartcheckout.poc.models;

import com.smartcheckout.poc.util.PropertiesUtil;

import java.io.IOException;
import java.math.BigDecimal;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by rahul on 7/3/2017.
 */

public class Bill {

    private float sumSellPrice; // Total amount by adding MRP of all itens in the cart
    private float savings;     // Total savings by calculating the disounts and offers
    private float tax;       // T
   private float totalAmountPaid; //Final amount to be paid

    public Bill(float sumSellPrice, float savings) {
        this.sumSellPrice = sumSellPrice;
        this.savings = savings;
    }

    /*public float getTotalAmount() {
        return round(this.totalAmount,2);
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }
*/
    public float getSumSellPrice() {
        return round(this.sumSellPrice,2);
    }

    public void setSumSellPrice(float sumSellPrice) {
        this.sumSellPrice = sumSellPrice;
    }

    public float getSavings() {
        return round(this.savings,2);
    }

    public void setSavings(float savings) {
        this.savings = savings;
    }



    private void calTax() {

        float taxRate = 0;
        try {
            taxRate = Float.parseFloat(PropertiesUtil.getProperty("taxRate",getApplicationContext()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Tax rate -->"+ taxRate);
        this.tax = this.sumSellPrice*taxRate;
        System.out.println("Tax-->"+tax);
    }

    public float calTotalAMountPaid() {
        calTax();
        this.totalAmountPaid = this.sumSellPrice + this.tax;
        return round(totalAmountPaid, 2);

    }







    private float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
