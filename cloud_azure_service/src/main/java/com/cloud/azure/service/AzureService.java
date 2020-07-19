package com.cloud.azure.service;

import java.io.IOException;
import java.net.MalformedURLException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.cloud.azure.domain.AzureCredentials;
import com.cloud.azure.dto.CostManagementFilter;
import com.cloud.azure.dto.RateCardFilterDto;
import com.microsoft.azure.CloudException;

public interface AzureService {

	public ResponseEntity<Object> getAllResources() throws CloudException, IOException;

	public ResponseEntity<Object> getBudgetDetail()
			throws MalformedURLException, IOException;

	public ResponseEntity<Object> getSqlDatabasesDetail() throws CloudException, IOException;

	public ResponseEntity<Object> getLocationDetail() throws CloudException, IOException;

	public ResponseEntity<Object> getUserDetail() throws CloudException, IOException;

	public ResponseEntity<Object> getAzureRateCardDetail(RateCardFilterDto dto) throws IOException;

	public ResponseEntity<Object> getCostDetail(CostManagementFilter costManagementFilter) throws IOException;

	public ResponseEntity<Object> getSoftwareDetail(MultipartFile file)  throws IOException, InterruptedException, Exception;
}
