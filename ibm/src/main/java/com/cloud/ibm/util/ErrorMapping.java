package com.cloud.ibm.util;

import com.cloud.ibm.constant.ErrorCode;
import com.cloud.ibm.constant.ErrorDescription;

import java.util.HashMap;
import java.util.Map;

public class ErrorMapping {

    private ErrorMapping() { throw new IllegalStateException("ErrorMapper is a Utility class"); }

    private static Map<String, String> errorMapping;

    static {
        errorMapping = new HashMap<>();
        errorMapping.put(ErrorCode.DEFAULT_ERROR, ErrorDescription.DEFAULT_ERROR);
        errorMapping.put(ErrorCode.MISSION_LOCALE, ErrorDescription.MISSION_LOCALE);
        errorMapping.put(ErrorCode.UPDATE_CLIENT_ID, ErrorDescription.UPDATE_CLIENT_ID);
        errorMapping.put(ErrorCode.UPDATE_CLIENT_SECRET, ErrorDescription.UPDATE_CLIENT_SECRET);
        errorMapping.put(ErrorCode.UPDATE_IBM_USER, ErrorDescription.UPDATE_IBM_USER);
        errorMapping.put(ErrorCode.UPDATE_IBM_USER_API_KEY, ErrorDescription.UPDATE_IBM_USER_API_KEY);
        errorMapping.put(ErrorCode.VERSION, ErrorDescription.VERSION);
        errorMapping.put(ErrorCode.GENERATION, ErrorDescription.GENERATION);
        errorMapping.put(ErrorCode.DATE_FORMAT, ErrorDescription.DATE_FORMAT);
        errorMapping.put(ErrorCode.GENERATION_VALIDATION, ErrorDescription.GENERATION_VALIDATION);
        errorMapping.put(ErrorCode.INVALID_PARAM, ErrorDescription.INVALID_PARAM);
        errorMapping.put(ErrorCode.NETWORK_TYPE_VALIDATION, ErrorDescription.NETWORK_TYPE_VALIDATION);
        errorMapping.put(ErrorCode.MISSING_USER, ErrorDescription.MISSING_USER);
        errorMapping.put(ErrorCode.MISSING_PASSWORD, ErrorDescription.MISSING_PASSWORD);
        errorMapping.put(ErrorCode.MISSING_HOST, ErrorDescription.MISSING_HOST);
        errorMapping.put(ErrorCode.MISSING_PACKAGE_ID, ErrorDescription.MISSING_PACKAGE_ID);
    }

    public static String getErrorDescription(String key){
        return errorMapping.getOrDefault(key, ErrorDescription.DEFAULT_ERROR);
    }

}
