package com.cloud.ibm.util;

import com.cloud.ibm.config.AppConstant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ResponseHandler {

    private ResponseHandler() {

    }

    public static ResponseEntity<Object> generateResponse(HttpStatus status, boolean isSuccess, String message,
                                                          Object data) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put(AppConstant.TIMESTAMP, new Date());
            map.put(AppConstant.STATUS, status.value());
            map.put(AppConstant.IS_SUCCESS, isSuccess);
            map.put(AppConstant.MESSAGE, message);
            map.put(AppConstant.DATA, data);

            return new ResponseEntity<>(map, status);
        } catch (Exception e) {
            map.clear();
            map.put(AppConstant.TIMESTAMP, new Date());
            map.put(AppConstant.STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
            map.put(AppConstant.IS_SUCCESS, false);
            map.put(AppConstant.MESSAGE, e.getMessage());
            map.put(AppConstant.DATA, null);
            return new ResponseEntity<>(map, status);
        }
    }

    public static ResponseEntity<Object> generateResponse(HttpStatus status, boolean isSuccess, String message) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put(AppConstant.TIMESTAMP, new Date());
            map.put(AppConstant.STATUS, status.value());
            map.put(AppConstant.IS_SUCCESS, isSuccess);
            map.put(AppConstant.MESSAGE, message);
            return new ResponseEntity<>(map, status);
        } catch (Exception e) {
            map.clear();
            map.put(AppConstant.TIMESTAMP, new Date());
            map.put(AppConstant.STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
            map.put(AppConstant.IS_SUCCESS, false);
            map.put(AppConstant.MESSAGE, e.getMessage());
            map.put(AppConstant.DATA, null);
            return new ResponseEntity<>(map, status);
        }
    }
}
