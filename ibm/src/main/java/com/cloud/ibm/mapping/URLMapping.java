package com.cloud.ibm.mapping;

public class URLMapping {

    private URLMapping(){
        throw new IllegalStateException("URLMapping is a Utility class");
    }

    public static final String API_VERSION = "v1";
    public static final String GATEWAY_SERVICE = "api-gateway/" + API_VERSION;
    public static final String TEST_SERVICE = API_VERSION + "/test";
    public static final String GET_USER_DETAIL = GATEWAY_SERVICE + "/user";
    public static final String GET_CATALOG_PRODUCT = API_VERSION + "/catalogProduct";
    public static final String GET_VIRTUAL_GUEST_ITEMS = API_VERSION + "/virtualGuestItems";
    public static final String GET_DEDICATED_VIRTUAL_GUEST = API_VERSION + "/virtualDedicatedHost";
    public static final String GET_INVOICE = API_VERSION + "/getInvoiceDetails";
    public static final String GET_ALL_OBJECT_DETAILS = API_VERSION + "/getAllObjectDetails";
    public static final String GET_ACCOUNT_DETAILS = API_VERSION + "/getAccountDetails";
    public static final String GET_DATABASE_DETAILS = API_VERSION + "/getDatabaseDetails";
    public static final String GET_INSTANCE_DETAILS = API_VERSION + "/getInstanceDetails";
    public static final String GET_CPU_AND_MEMORY_USAGE = API_VERSION + "/usage/getCpuAndMemory";
    public static final String GET_PRODUCT_PACKAGE = API_VERSION + "/getProductPackage";
    public static final String GET_CUSTOMER_USER_DETAILS = API_VERSION + "/getUserDetails";
    public static final String GET_BANDWIDTH_MONITORING = API_VERSION + "/getBandwidth/monitoring";
    public static final String GET_ITEM_PRICES = API_VERSION + "/getItem/priceDetails";
    public static final String GET_SOFTWARE_DETAILS = API_VERSION + "/getSoftwareDetails";
}
