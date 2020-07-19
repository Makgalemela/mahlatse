package com.cloud.gateway.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import com.cloud.gateway.dto.UserDetails;

public class GenericUtils {

	private GenericUtils() {
		// DC
	}

	public static UserDetails getLoggedInUser() {
		return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
}
