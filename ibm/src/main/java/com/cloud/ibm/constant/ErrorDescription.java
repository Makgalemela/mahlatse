package com.cloud.ibm.constant;

public class ErrorDescription {

    private ErrorDescription() { throw new IllegalStateException("ErrorDescription is a Utility class"); }

    public static final String DEFAULT_ERROR = "UNKNOWN ERROR";
    public static final String INVALID_PARAM = "INVALID";
    public static final String MISSION_LOCALE = "Missing locale";
    public static final String UPDATE_CLIENT_ID = "Missing IBM Client ID Please Update";
    public static final String UPDATE_CLIENT_SECRET = "Missing IBM Client Secret Please Update";
    public static final String UPDATE_IBM_USER = "Missing IBM User Please Update";
    public static final String UPDATE_IBM_USER_API_KEY = "Missing IBM USER API KEY Please Update";
    public static final String VERSION = "Missing version";
    public static final String GENERATION = "Missing generation";
    public static final String DATE_FORMAT = "Date format like YYYY-MM-DD";
    public static final String GENERATION_VALIDATION = "Accept generation only 1 Or 2";
    public static final String NETWORK_TYPE_VALIDATION = "Accept Network Type only public or private";
    public static final String MISSING_USER = "missing user";
    public static final String MISSING_HOST = "missing host";
    public static final String MISSING_PASSWORD = "missing password";
    public static final String MISSING_PACKAGE_ID = "missing Item package Id";


}
