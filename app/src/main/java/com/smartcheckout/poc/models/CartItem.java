package com.smartshopper.androidpoc.models;

/**
 * Created by yeshwanth on 4/4/2017.
 */

public class CartItem {

    private Product product;
    private Integer quantity;
    private String imagePath;

    public CartItem(Product product, Integer quantity, String imagePath) {
        this.product = product;
        this.quantity = quantity;
        this.imagePath = imagePath;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
