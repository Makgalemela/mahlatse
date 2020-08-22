package com.cloud.azure.domain;

import io.swagger.annotations.ApiModel;

@ApiModel
public class AzureCredentials {

	private String clientId;
	private String tenantId;
	private String clientSecretId;
	//private String subscriptionId;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getClientSecretId() {
		return clientSecretId;
	}

	public void setClientSecretId(String clientSecretId) {
		this.clientSecretId = clientSecretId;
	}

	//public String getSubscriptionId() {
//		return subscriptionId;
//	}

//	public void setSubscriptionId(String subscriptionId) {
//		this.subscriptionId = subscriptionId;
//	}

}
