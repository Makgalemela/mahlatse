package com.cloud.azure.service;

import java.io.IOException;

import org.springframework.http.ResponseEntity;

import com.microsoft.azure.CloudException;

public interface AzureMonitorService {

	public ResponseEntity<Object> getNetworkMonitoringDetail() throws CloudException, IOException;

	public ResponseEntity<Object> getMonitoringDetail() throws CloudException, IOException;

}
