package com.aws.kt.config;

public class UrlConstant {

	public UrlConstant() {

	}

	public static final String API_VERSION = "v1";
	public static final String FETCH_AWS_DATA = API_VERSION + "/aws/data";
	public static final String FETCH_AWS_SOFTWARE = API_VERSION + "/aws/file";
	public static final String GATEWAY_SERVICE = "api-gateway/" + API_VERSION;
	public static final String INSTANCE_DETAIL=API_VERSION+"/aws/instance";
	public static final String DATABASE_DETAIL=API_VERSION+"/aws/database";
	public static final String ACCOUNT_SUMMARY=API_VERSION+"/aws/account/summary";
	public static final String SOFTWARE_DETAIL=API_VERSION+"/aws/software";
	public static final String USER_DETAIL=API_VERSION+"/aws/user";
	public static final String AWS_BILLING=API_VERSION+"/aws/billing";
	public static final String AWS_RUNNING_INSTANCES=API_VERSION+"/aws/running/instances";
	public static final String AWS_PRICE_CATALOG=API_VERSION+"/aws/price/catalog";
	public static final String AWS_BUDGET=API_VERSION+"/aws/budget";
	public static final String AWS_COST_FORECAST=API_VERSION+"/aws/cost/forecast";
	
	
	public static final String GET_USER_DETAIL = GATEWAY_SERVICE + "/user";
	
}