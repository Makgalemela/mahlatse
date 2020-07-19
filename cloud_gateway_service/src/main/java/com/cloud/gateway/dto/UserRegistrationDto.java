package com.cloud.gateway.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class UserRegistrationDto {

	@Pattern(regexp = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-[$&+,:;=?@#|'<>-^*()%!]]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", message = "Please enter a valid email address")
	@NotBlank(message = "Please enter the email")
	private String email;
	@Pattern(regexp = "(?=.*[A-Z])(?=.*[$@$#!()^*%*?&])(?=.*[0-9])(?=.*[a-z]).{6,20}", message = "Please enter a valid password")
	@NotNull(message = "Please enter the password")
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
