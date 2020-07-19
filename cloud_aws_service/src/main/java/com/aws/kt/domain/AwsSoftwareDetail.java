package com.aws.kt.domain;

import javax.validation.constraints.NotNull;

public class AwsSoftwareDetail {

	@NotNull
	private String regionName;
	@NotNull
	private String userIp;
	@NotNull
	private String userName;

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

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

}
