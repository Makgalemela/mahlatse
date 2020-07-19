package com.cloud.azure.config;

public class UrlConstant {

	public UrlConstant() {

	}

	public static final String API_VERSION = "v1";
	public static final String GATEWAY_SERVICE = "api-gateway/" + API_VERSION;
	public static final String FETCH_VRITUAL_DETAIL = API_VERSION + "/azure/virtual";
	public static final String FETCH_BUDGET_DETAIL = API_VERSION + "/azure/budgets";
	public static final String FETCH_SQL_DATABASE_DETAIL = API_VERSION + "/azure/sql/databases";
	public static final String FETCH_SUBSCRIPTION_LOCATION = API_VERSION + "/azure/subscription/locations";
	public static final String FETCH_USER = API_VERSION + "/azure/user";
	public static final String FETCH_RATE_CARD = API_VERSION + "/azure/rate/card";
	public static final String FETCH_COST = API_VERSION + "/azure/cost";

	public static final String FETCH_BILLING = API_VERSION + "/azure/accounts/detail";
	public static final String FETCH_SOFTWARE = API_VERSION + "/azure/software";
	public static final String FETCH_NETWORK_MONITOR = API_VERSION + "/azure/monitor/network";
	public static final String FETCH_VITUAL_MACHINE_MONTIORING = API_VERSION + "/azure/monitor";

	public static final String GET_USER_DETAIL = GATEWAY_SERVICE + "/user";

}