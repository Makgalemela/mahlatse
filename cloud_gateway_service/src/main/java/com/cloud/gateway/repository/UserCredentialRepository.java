package com.cloud.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud.gateway.domain.UserCredentials;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredentials, Long> {

	UserCredentials findByUserId(Long userId);

	UserCredentials findByAwsAccessKey(String awsAccessKey);

	UserCredentials findByAwsSecretKey(String awsSecretKey);

	UserCredentials findByAzureAccessKey(String azureAccessKey);

	UserCredentials findByAzureSecretkey(String azureSecretkey);

	UserCredentials findByIbmUser(String ibmUser);

	UserCredentials findByIbmUserApiKey(String ibmUserApiKey);

	UserCredentials findByIbmClientId(String ibmClientId);

	UserCredentials findByIbmClientSecret(String ibmClientSecret);

}
