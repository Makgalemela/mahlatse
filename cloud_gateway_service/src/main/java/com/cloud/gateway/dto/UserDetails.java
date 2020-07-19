package com.cloud.gateway.dto;

public class UserDetails {

	private String email;
	private String userId;
	private String authorization;

	public String getAuthorization() {
		return authorization;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "UserDetails [email=" + email + ", userId=" + userId + ", authorization=" + authorization + "]";
	}
	
	

}
