package com.smartcheckout.poc.models;

/**
 * Created by rahul on 7/3/2017.
 */

public class Bill {

    public float totalAmount; // Total amount by adding MRP of all itens in the cart
    public float savings;     // Total savings by calculating the disounts and offers
    public float taxes;       //
    public float amountPaid; //Final amount to be paid

    public Bill(float totalAmount, float savings, float taxes, float amountPaid) {
        this.totalAmount = totalAmount;
        this.savings = savings;
        this.taxes = taxes;
        this.amountPaid = amountPaid;
    }

    public Bill () {
        super();
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public float getSavings() {
        return savings;
    }

    public void setSavings(float savings) {
        this.savings = savings;
    }

    public float getTaxes() {
        return taxes;
    }

    public void setTaxes(float taxes) {
        this.taxes = taxes;
    }

    public float getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(float amountPaid) {
        this.amountPaid = amountPaid;
    }
}
