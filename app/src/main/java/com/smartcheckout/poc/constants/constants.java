package com.smartcheckout.poc.constants;

/**
 * Created by Swetha_Swaminathan on 10/20/2017.
 */

public class constants {

    // Response Codes
    public static final int RC_SCAN_BARCODE_STORE = 200;
    public static final int RC_SCAN_BARCODE_ITEM = 0;

    public static final int RC_LOCATION_PERMISSION = 1;
    public static final int RC_CHECK_SETTING = 2;

    // Endpoints
    public static String STORE_SEARCH_URL= "http://ec2-54-191-68-157.us-west-2.compute.amazonaws.com:8080/store/";
    public static String PRODUCT_SEARCH_URL="http://ec2-54-191-68-157.us-west-2.compute.amazonaws.com:8080/product/";
    public static String TRANSACTION_URL = "http://ec2-54-191-68-157.us-west-2.compute.amazonaws.com:8080/transaction/";


    public static String TRANSACTION_CREATE_EP = TRANSACTION_URL + "create/";
    public static String TRANSACTION_UPDATE_EP = TRANSACTION_URL + "update/";

    public static String LOCATION_SEARCH_EP = STORE_SEARCH_URL + "locationsearch/";
    public static String BARCODE_SEARCH_EP = STORE_SEARCH_URL + "barcodesearch/";



    public static int TIMEOUT_TRANSACTION_MINS = 100;
    public static int TIMEOUT_SCAN_MILLISECS = 30000;

    public static int SPINNER_MAX_VALUE = 5;

    public static String SP_TRANSACTION_ID="TransactionId";
    public static String SP_TRANSACTION_UPDATED_TS="TransactionUpdatedDate";


}
