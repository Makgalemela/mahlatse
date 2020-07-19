package com.cloud.ibm.mapping;

public class IBMUrl {

    private IBMUrl(){
        throw new IllegalStateException("IBM Utility class");
    }

    public static final String CATALOG_PRODUCT_API = "https://api.ibm.com/marketplace/run/v2/catalog/products";
    public static final String ACCOUNT_TOKEN_API = "https://iam.cloud.ibm.com/identity/token";
    public static final String ACCOUNT_SOFT_LAYER = "https://api.softlayer.com/rest/v3/SoftLayer_Account";
    public static final String DATABASE_DETAILS_API = "https://resource-controller.cloud.ibm.com/v2/resource_instances";
    public static final String INSTANCE_DETAILS_API = "https://us-south.iaas.cloud.ibm.com/v1/instances";
    public static final String SOFTWARE_DETAILS_COMMAND = "ps axco command";
}
