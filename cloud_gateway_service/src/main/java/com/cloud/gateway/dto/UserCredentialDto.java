package com.cloud.gateway.dto;

import io.swagger.annotations.ApiModel;

@ApiModel
public class UserCredentialDto {

	private String awsAccessKey;
	private String awsSecretKey;
	private String azureAccessKey;
	private String azureSecretkey;
	private String azureTenantId;
	private String ibmUser;
	private String ibmUserApiKey;
	private String ibmClientId;
	private String ibmClientSecret;

	public String getIbmUser() {
		return ibmUser;
	}

	public void setIbmUser(String ibmUser) {
		this.ibmUser = ibmUser;
	}

	public String getIbmUserApiKey() {
		return ibmUserApiKey;
	}

	public void setIbmUserApiKey(String ibmUserApiKey) {
		this.ibmUserApiKey = ibmUserApiKey;
	}

	public String getIbmClientId() {
		return ibmClientId;
	}

	public void setIbmClientId(String ibmClientId) {
		this.ibmClientId = ibmClientId;
	}

	public String getIbmClientSecret() {
		return ibmClientSecret;
	}

	public void setIbmClientSecret(String ibmClientSecret) {
		this.ibmClientSecret = ibmClientSecret;
	}

	public String getAzureTenantId() {
		return azureTenantId;
	}

	public void setAzureTenantId(String azureTenantId) {
		this.azureTenantId = azureTenantId;
	}

	public String getAwsAccessKey() {
		return awsAccessKey;
	}

	public void setAwsAccessKey(String awsAccessKey) {
		this.awsAccessKey = awsAccessKey;
	}

	public String getAwsSecretKey() {
		return awsSecretKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public String getAzureAccessKey() {
		return azureAccessKey;
	}

	public void setAzureAccessKey(String azureAccessKey) {
		this.azureAccessKey = azureAccessKey;
	}

	public String getAzureSecretkey() {
		return azureSecretkey;
	}

	public void setAzureSecretkey(String azureSecretkey) {
		this.azureSecretkey = azureSecretkey;
	}

}
