package com.smartcheckout.poc.models;

import java.math.BigDecimal;
import com.smartcheckout.poc.util.Currency;

/**
 * Created by rahul on 7/3/2017.
 */

public class Bill {

    private float subTotal; // Total amount by adding MRP of all itens in the cart
    private float savings;     // Total savings by calculating the disounts and offers
    private float taxPercent;
    private float tax;       // T
    private Currency currency;
    private float totalAmount; //Final amount to be paid

    public Bill(float subTotal, float savings, float taxPercent, Currency currency) {
        this.subTotal = subTotal;
        this.savings = savings;
        this.taxPercent = taxPercent;
        this.currency = currency;
        notifyChanges();
    }

    public float getSubTotal() {
        return round(this.subTotal, 2);
    }

    public void setSubTotal(float subTotal) {
        this.subTotal = subTotal;
    }

    public float getSavings() {
        return round(this.savings, 2);
    }

    public void setSavings(float savings) {
        this.savings = savings;
    }

    public float getTax(){
        return round(this.tax, 2);
    }

    public float getTotalAmount(){
        return round(this.totalAmount, 2);
    }

    public Currency getCurrency(){
        return this.currency;
    }

    /*
    * Recalculates the tax and total amount of the bill.
    * Should be called by the client every time the subTotal amount or tax is changed.
    * */
    public void notifyChanges(){
        this.tax = this.subTotal * this.taxPercent;
        this.totalAmount =  this.subTotal + this.tax;
    }


    private float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
