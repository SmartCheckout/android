package com.smartcheckout.poc.util;

import com.smartcheckout.poc.models.Store;
import com.smartcheckout.poc.models.Transaction;

/**
 * Created by yeshwanth on 8/17/2017.
 */

public class StateData {
    public static String userId = null;
    public static String storeId = null;
    public static String storeName = null;
    public static String transactionId = null;
    public static TransactionStatus status = null;
    public static Transaction transactionReceipt = null;
    public static Store store;
    public static Float billAmount = 0.0f;
}
