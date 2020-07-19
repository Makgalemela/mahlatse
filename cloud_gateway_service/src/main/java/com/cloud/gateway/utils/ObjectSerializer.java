package com.cloud.gateway.utils;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ObjectSerializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(ObjectSerializer.class);

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
			LOGGER.info("value :: {} ",jsonObj);
			userDto = (T) objectMapper.readValue(jsonObj, class1);
			LOGGER.info("read value :: {} ",userDto);
		} catch (JsonProcessingException jpe) {
			LOGGER.error("JsonProcessingException Occured {} ",jpe);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("JsonProcessingException Occured {} ",e);;
		}
		return userDto;
	}
}
