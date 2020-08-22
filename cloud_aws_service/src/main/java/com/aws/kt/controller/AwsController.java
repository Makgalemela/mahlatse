package com.aws.kt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.aws.kt.domain.AwsCredential;
import com.aws.kt.domain.AwsSoftwareDetail;
import com.aws.kt.domain.UserCredtentials;
import com.aws.kt.service.AwsService;

import io.swagger.annotations.Api;

import static com.aws.kt.config.UrlConstant.*;

import java.io.IOException;

@RestController
@Api
@CrossOrigin("*")
public class AwsController {

	
	@Autowired
	private AwsService awsService;

	@GetMapping(value =INSTANCE_DETAIL,consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getInstanceDetail(@RequestParam("regionName")String regionName) throws Exception {
		return awsService.getInstanceDetail(regionName);
	}
	
	@GetMapping(value=DATABASE_DETAIL,consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getDatabaseDetail(@RequestParam("regionName")String regionName) throws Exception {
		return awsService.getDatabaseDetail(regionName);
	}
	
	@GetMapping(value=ACCOUNT_SUMMARY,consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getAccountSummary(@RequestParam("regionName")String regionName) throws Exception {
		return awsService.getAccountSummary(regionName);
	}
	
	@PostMapping(value= SOFTWARE_DETAIL,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> getSoftwareDetail(@RequestBody MultipartFile file ,AwsSoftwareDetail awsSoftwareDetail) throws Exception {
		return awsService.getSoftwareDetail(file,awsSoftwareDetail);
	}

	@GetMapping(value=USER_DETAIL,consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getUserDetail(@RequestParam("regionName")String regionName) throws Exception {
		return awsService.getUserDetail(regionName);
	}
	
	@GetMapping(value=AWS_BILLING)
	public ResponseEntity<Object> getAwsBilling(@RequestParam("regionName")String regionName) throws Exception {
		return awsService.getAwsBilling(regionName);
	}
	
	@GetMapping(value=AWS_RUNNING_INSTANCES)
	public ResponseEntity<Object> getRunningInstances(@RequestParam("regionName")String regionName) throws Exception {
		return awsService.getRunningInstances(regionName);
	}
	
	@GetMapping(value=AWS_PRICE_CATALOG)
	public ResponseEntity<Object> getPriceCatalog(@RequestParam("regionName")String regionName) throws Exception {
		return awsService.getPriceCatalog(regionName);
	}
	
	@GetMapping(value=AWS_BUDGET)
	public ResponseEntity<Object> getBudget(@RequestParam("regionName")String regionName) throws Exception {
		return awsService.getBudget(regionName);
	}
	
	@GetMapping(value=AWS_COST_FORECAST)
	public ResponseEntity<Object> getAwsCostForecast(@RequestParam("regionName")String regionName) throws Exception {
		return awsService.getAwsCostForecast(regionName);
	}
}
