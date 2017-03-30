package com.example.swetha_swaminathan.smartshopper;

import com.google.android.gms.location.places.Place;

/**
 * Created by Swetha_Swaminathan on 2/21/2017.
 */

public class AppState {

    private boolean storeSelected = false;
    private String storeName;
    private boolean locationSelected = false;
    private Place storeDetails;

    public Place getStoreDetails() {
        return storeDetails;
    }

    public void setStoreDetails(Place storeDetails) {
        this.storeDetails = storeDetails;
    }

    public boolean isLocationSelected() {
        return locationSelected;
    }

    public void setLocationSelected(boolean locationSelected) {
        this.locationSelected = locationSelected;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public boolean isStoreSelected() {
        return storeSelected;
    }

    public void setStoreSelected(boolean storeSelected) {
        this.storeSelected = storeSelected;
    }

}
