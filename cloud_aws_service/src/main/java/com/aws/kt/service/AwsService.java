package com.aws.kt.service;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.aws.kt.domain.AwsCredential;
import com.aws.kt.domain.AwsSoftwareDetail;
import com.aws.kt.domain.UserCredtentials;

public interface AwsService {

	public ResponseEntity<Object> getInstanceDetail(String regionName) throws IOException;

	public ResponseEntity<Object> getDatabaseDetail(String regionName) throws IOException;

	public ResponseEntity<Object> getAccountSummary(String regionName) throws IOException;

	public ResponseEntity<Object> getSoftwareDetail(MultipartFile file, AwsSoftwareDetail awsSoftwareDetail) throws Exception;

	public ResponseEntity<Object> getUserDetail(String regionName) throws IOException;

	public ResponseEntity<Object> getAwsBilling(String regionName) throws IOException;

	public ResponseEntity<Object> getRunningInstances(String regionName) throws IOException;

	public ResponseEntity<Object> getPriceCatalog(String regionName) throws IOException;

	public ResponseEntity<Object> getBudget(String regionName) throws IOException;

	public ResponseEntity<Object> getAwsCostForecast(String regionName);

}
