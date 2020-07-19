package com.cloud.ibm.util;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ObjectSerializer {
    private static ObjectMapper objectMapper;

    @Autowired
    public ObjectSerializer(ObjectMapper objectMapper) {
        ObjectSerializer.objectMapper = objectMapper;
    }

    public static <T> T getObject(Object obj, Class<T> class1) {
        String jsonObj = "";
        T userDto = null;
        try {
            jsonObj = objectMapper.writeValueAsString(obj);
            userDto = objectMapper.readValue(jsonObj, class1);
            System.out.println(jsonObj);
        } catch (JsonProcessingException jpe) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userDto;
    }
}