package com.cloud.gateway.dto;

public class LoginResponseDTO {
	private Long userId;
	private String email;
	private String authorization;

	public LoginResponseDTO() {
		// DC
	}

	public LoginResponseDTO(Long userId, String email) {
		super();
		this.userId = userId;
		this.email = email;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public LoginResponseDTO(String authorization) {
		this.authorization = authorization;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAuthorization() {
		return authorization;
	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

}
