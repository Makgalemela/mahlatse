package com.cloud.azure.util;

import org.springframework.security.core.context.SecurityContextHolder;

import com.cloud.azure.dto.UserDetails;


public class GenericUtils {

	private GenericUtils() {
		// DC
	}

	public static UserDetails getLoggedInUser() {
		return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
}
