package com.cloud.azure.service;

import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cloud.azure.feignClient.GatewayServiceFeignController;
import com.cloud.azure.util.GenericUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.rest.LogLevel;

@Service
public class AzureCredentialService {
	
	private static final String AUTHORIZATION_PREFIX = "Bearer ";

	@Autowired
	private GatewayServiceFeignController gatewayFeign;

	@Autowired
	private ObjectMapper objectMapper;

	public Azure getAzure() throws CloudException, IOException {
		Map<String, String> userDetail = getUserCredentials();
		if (userDetail.isEmpty()) {
			return null;
		}
		ApplicationTokenCredentials tokenCredentials = new ApplicationTokenCredentials(userDetail.get("azureAccessKey"),
				userDetail.get("azureTenantId"), userDetail.get("azureSecretKey"), AzureEnvironment.AZURE);
		Azure azure = Azure.configure().withLogLevel(LogLevel.NONE).authenticate(tokenCredentials)
				.withDefaultSubscription();
		return azure;
	}

	private Map<String, String> getUserCredentials() {
		Long userId = Long.valueOf(GenericUtils.getLoggedInUser().getUserId());
		ResponseEntity<Object> getUserDetail = gatewayFeign.getUserDetail(userId);
		JSONObject object = new JSONObject(getUserDetail).getJSONObject("body");
		if (!object.getBoolean("isSuccess")) {
			return null;
		}
		JSONObject dataFetch = new JSONObject(object.toString()).getJSONObject("data");
		try {
			Map<String, String> userDetail = objectMapper.readValue(dataFetch.toString(), Map.class);
			return userDetail;
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return null;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String azureAuthToken(String resource) throws IOException {
		Map<String, String> userDetail = getUserCredentials();
		if (userDetail.isEmpty()) {
			return null;
		}
		ApplicationTokenCredentials tokenCredentials = new ApplicationTokenCredentials(userDetail.get("azureAccessKey"),
				userDetail.get("azureTenantId"), userDetail.get("azureSecretKey"), AzureEnvironment.AZURE);
		return AUTHORIZATION_PREFIX + tokenCredentials.getToken(resource);
	}

}
