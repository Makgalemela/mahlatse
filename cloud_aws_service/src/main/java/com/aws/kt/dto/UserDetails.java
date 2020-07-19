package com.aws.kt.dto;

public class UserDetails {

	private String userId;
	private String email;
	private String authorization;

	public String getEmail() {
		return email;
	}

	public String getAuthorization() {
		return authorization;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

}
