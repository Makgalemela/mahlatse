package com.aws.kt.util;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static com.aws.kt.config.AppConstants.*;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHandler {

    private ResponseHandler() {
       
    }

    public static ResponseEntity<Object> generateResponse(HttpStatus status, boolean isSuccess, String message,
                                                          Object data) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put(TIMESTAMP, new Date());
            map.put(STATUS, status.value());
            map.put(IS_SUCCESS, isSuccess);
            map.put(MESSAGE, message);
            map.put(DATA, data);

            return new ResponseEntity<>(map, status);
        } catch (Exception e) {
            map.clear();
            map.put(TIMESTAMP, new Date());
            map.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
            map.put(IS_SUCCESS, false);
            map.put(MESSAGE, e.getMessage());
            map.put(DATA, null);
            return new ResponseEntity<>(map, status);
        }
    }

    public static ResponseEntity<Object> generateResponse(HttpStatus status, boolean isSuccess, String message) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put(STATUS, status.value());
            map.put(IS_SUCCESS, isSuccess);
            map.put(MESSAGE, message);
            map.put(TIMESTAMP, new Date());
            return new ResponseEntity<>(map, status);
        } catch (Exception e) {
            map.clear();
            map.put(TIMESTAMP, new Date());
            map.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
            map.put(IS_SUCCESS, false);
            map.put(MESSAGE, e.getMessage());
            map.put(DATA, null);
            return new ResponseEntity<>(map, status);
        }
    }
}
