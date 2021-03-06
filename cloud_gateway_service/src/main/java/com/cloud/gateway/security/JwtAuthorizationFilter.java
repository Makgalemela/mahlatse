package com.cloud.gateway.security;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.cloud.gateway.dto.UserDetails;
import com.cloud.gateway.service.RedisService;
import com.cloud.gateway.utils.ObjectSerializer;

import static com.cloud.gateway.config.AppConstants.*;

import io.jsonwebtoken.Claims;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

	public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			FilterChain filterChain) throws IOException, ServletException {
		if (("OPTIONS").equals(httpServletRequest.getMethod())) {
			httpServletResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
			return;
		}
		try {
			Authentication resultOfAuthentication = authenticateRequest(httpServletRequest, httpServletResponse);
			SecurityContextHolder.getContext().setAuthentication(resultOfAuthentication);
			LOGGER.info("request :: {} ",httpServletRequest);
			filterChain.doFilter(httpServletRequest, httpServletResponse);
		} catch (InternalAuthenticationServiceException internalAuthenticationServiceException) {
			SecurityContextHolder.clearContext();
			logger.error("Internal authentication service exception", internalAuthenticationServiceException);
			httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (AuthenticationException authenticationException) {
			SecurityContextHolder.clearContext();
			JSONObject json = new JSONObject();
			try {
				responseObject(authenticationException.getMessage(), HttpServletResponse.SC_UNAUTHORIZED, false, json,
						httpServletResponse, httpServletRequest);
			} catch (JSONException e) {
				logger.error("Error Occured {} ", e);
			}
		} catch (Exception e) {
			e.printStackTrace();
			SecurityContextHolder.clearContext();
			JSONObject json = new JSONObject();
			try {
				responseObject(e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED, false, json, httpServletResponse,
						httpServletRequest);
			} catch (JSONException je) {
				logger.error("Error Occured {} ", je);
			}
		}
	}

	boolean responseObject(String message, Integer status, boolean isSuccess, JSONObject json,
			HttpServletResponse httpResponse, HttpServletRequest httpRequest) throws IOException, JSONException {
		json.put(TIMESTAMP, new Date().getTime());
		json.put(STATUS, status);
		json.put(IS_SUCCESS, isSuccess);
		json.put(MESSAGE, message);
		httpResponse.setStatus(status);
		return setHttpRequestResponse(httpRequest, httpResponse, json);
	}

	private Authentication authenticateRequest(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		UsernamePasswordAuthenticationToken authentication = null;
		try {
			authentication = getAuthentication(request, response);
		} catch (InvalidDataAccessApiUsageException e) {
			logger.info("Sorry, Invalid token*************");
			throw new InvalidDataAccessApiUsageException("Invalid token");
		}
		if (authentication == null || !authentication.isAuthenticated()) {
			logger.info("Sorry, unable to authenticate for user*************");
			throw new InternalAuthenticationServiceException("Unable to authenticate for user");
		}
		return authentication;
	}

	boolean setHttpRequestResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse, JSONObject json)
			throws IOException {
		httpResponse.addHeader("Content-Type", "application/json");
		httpResponse.getWriter().write(json.toString());
		httpResponse.getWriter().flush();
		httpResponse.getWriter().close();
		return false;
	}

	public UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request,
			HttpServletResponse response)
			throws AuthenticationException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		String authorization = request.getHeader(AUTHORIZATION);
		UserDetails userDetails;
		if (authorization == null || authorization.equals("")) {
			logger.info("Sorry, you are not authorized*********");
			throw new BadCredentialsException("Sorry, you are not authorized");
		} else {
			RedisService redisService = WebApplicationContextUtils
					.getRequiredWebApplicationContext(request.getServletContext()).getBean(RedisService.class);
			JwtService jwtService = WebApplicationContextUtils
					.getRequiredWebApplicationContext(request.getServletContext()).getBean(JwtService.class);
			Claims claims = jwtService.parseJwtToken(authorization);
			if (claims == null) {
				throw new BadCredentialsException("Your session has been expired. Please login again");
			}
			userDetails = ObjectSerializer.getObject(claims, UserDetails.class);
			userDetails.setAuthorization(authorization);
			List<String> sessions = redisService.getDataFromRedis(userDetails.getEmail());
			if (!sessions.contains(authorization)) {
				throw new BadCredentialsException("Your session has been expired. Please login again");
			}
			LOGGER.info("user detail :: {} ",userDetails);
			return new UsernamePasswordAuthenticationToken(userDetails, null, new ArrayList<>());
		}
	}

}
