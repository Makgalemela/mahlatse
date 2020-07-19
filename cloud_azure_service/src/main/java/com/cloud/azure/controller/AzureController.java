package com.cloud.azure.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import static com.cloud.azure.config.UrlConstant.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.cloud.azure.domain.AzureCredentials;
import com.cloud.azure.dto.CostManagementFilter;
import com.cloud.azure.dto.RateCardFilterDto;
import com.cloud.azure.service.AzureService;
import com.microsoft.azure.CloudException;
import io.swagger.annotations.Api;

@RestController
@Api
public class AzureController {

	@Autowired
	private AzureService azureService;

	@GetMapping(FETCH_VRITUAL_DETAIL)
	public ResponseEntity<Object> getAllResources() throws CloudException, IOException {
		return azureService.getAllResources();
	}

	@GetMapping(FETCH_BUDGET_DETAIL)
	public ResponseEntity<Object> getBudgetDetail()
			throws CloudException, IOException {
		return azureService.getBudgetDetail();
	}

	@GetMapping(FETCH_SQL_DATABASE_DETAIL)
	public ResponseEntity<Object> getSqlDatabasesDetail() throws MalformedURLException, IOException {
		return azureService.getSqlDatabasesDetail();
	}

	@GetMapping(FETCH_SUBSCRIPTION_LOCATION)
	public ResponseEntity<Object> getSubscriptionLocationDetail() throws MalformedURLException, IOException {
		return azureService.getLocationDetail();
	}

	@GetMapping(FETCH_USER)
	public ResponseEntity<Object> getUserDetail() throws MalformedURLException, IOException {
		return azureService.getUserDetail();
	}

	@GetMapping(FETCH_RATE_CARD)
	public ResponseEntity<Object> getAzureRateCardDetail(@RequestBody RateCardFilterDto dto)
			throws MalformedURLException, IOException {
		return azureService.getAzureRateCardDetail(dto);
	}

	@PostMapping(FETCH_COST)
	public ResponseEntity<Object> getCostDetail(@RequestBody CostManagementFilter costManagementFilter) throws MalformedURLException, IOException {
		return azureService.getCostDetail(costManagementFilter);
	}

	@PostMapping(value = FETCH_SOFTWARE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> getSoftwareDetail(@RequestBody MultipartFile file) throws Exception {
		return azureService.getSoftwareDetail(file);
	}
}
