package com.cloud.gateway.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RedisService {

	private static Logger logger = LoggerFactory.getLogger(RedisService.class);
	
	@Autowired
	private ObjectMapper objectMapper;

	@Resource(name = "redisUserTemplate")
	private ValueOperations<String, String> valOps;
	
	public Map<String, Object> saveDataInRedis(String id, Object obj) {
		Map<String, Object> result = new HashMap<>();
		String jsonObj = "";
		try {
			jsonObj = objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException jpe) {

			logger.info("error in Redis save");
		}
		valOps.set(id, jsonObj);
		result.put("isSuccess", true);
		result.put("massage", "Data saved Successfully in redis");
		return result;
	}

	public List<String> getDataFromRedis(String id) {
		String jsonObj = valOps.get(id);

		List<String> session = new ArrayList<>();
		try {
			session = objectMapper.readValue(jsonObj, new TypeReference<List<String>>() {
			});
		} catch (Exception e) {

			logger.info("Data from redis is deleted for id {}", id);
		}

		return session;
	}
	
	public Boolean tokenExpire(String id, Long seconds) {
		return valOps.getOperations().expire(id, seconds, TimeUnit.MILLISECONDS);
	}
}
