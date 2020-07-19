package com.aws.kt.util;

import org.springframework.security.core.context.SecurityContextHolder;

import com.aws.kt.dto.UserDetails;


public class GenericUtils {

	private GenericUtils() {
		// DC
	}

	public static UserDetails getLoggedInUser() {
		return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
}
