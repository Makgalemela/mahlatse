package com.cloud.azure.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cloud.azure.service.AzureMonitorService;
import static com.cloud.azure.config.UrlConstant.*;

@RestController
public class AzureMonitorController {

	@Autowired
	private AzureMonitorService azureMonitorService;

	@GetMapping(FETCH_NETWORK_MONITOR)
	public ResponseEntity<Object> getNetworkMonitoringDetail() throws MalformedURLException, IOException {
		return azureMonitorService.getNetworkMonitoringDetail();
	}

	@GetMapping(FETCH_VITUAL_MACHINE_MONTIORING)
	public ResponseEntity<Object> getMonitoringDetail() throws MalformedURLException, IOException {
		return azureMonitorService.getMonitoringDetail();
	}

}
