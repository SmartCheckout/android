package com.smartshopper.androidpoc.models;

import java.util.List;

/**
 * Created by yeshwanth on 4/7/2017.
 */

public class StoreSearchResponse {

    List<Store> stores;

    public StoreSearchResponse(List<Store> stores) {
        this.stores = stores;
    }

    public List<Store> getStores() {
        return stores;
    }

    public void setStores(List<Store> stores) {
        this.stores = stores;
    }
}
