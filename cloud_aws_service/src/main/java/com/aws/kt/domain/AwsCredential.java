package com.aws.kt.domain;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;

@ApiModel
public class AwsCredential {

	@NotNull
	private String awsSecretKey;
	@NotNull
	private String awsAccessKey;
	@NotNull
	private String regionName;
	@NotNull
	private String userIp;
	@NotNull
	private String userName;

	public String getUserIp() {
		return userIp;
	}

	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAwsSecretKey() {
		return awsSecretKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public String getAwsAccessKey() {
		return awsAccessKey;
	}

	public void setAwsAccessKey(String awsAccessKey) {
		this.awsAccessKey = awsAccessKey;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

}
