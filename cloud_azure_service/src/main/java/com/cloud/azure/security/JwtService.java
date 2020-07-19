package com.cloud.azure.security;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.azure.config.EnvironmentConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Service
public class JwtService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);

	@Autowired
	private EnvironmentConfig config;

	public Claims parseJwtToken(String token) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		return parseJwtToken(token, true);
	}

	public Claims parseJwtToken(String token, boolean suppressExpiredTokenExceptions)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		if (token == null || token.isEmpty()) {
			return null;
		}
		LOGGER.info("nfo ::: {} ,{}", config.getProxyName(), config.getTokenSecret());
		token = token.replace(config.getProxyName(), "");
		LOGGER.info("token ::: {} ", token);
		try {
			return Jwts.parser().setSigningKey(config.getTokenSecret()).parseClaimsJws(token).getBody();
		} catch (SignatureException e) {
			LOGGER.error("Invalid JWT signature: {}", e.getMessage());
			return null;
		} catch (MalformedJwtException e) {
			LOGGER.error("Invalid JWT token: {}", e.getMessage());
			return null;
		} catch (ExpiredJwtException e) {
			LOGGER.error("JWT token is expired: {}", e.getMessage());
			return null;
		} catch (UnsupportedJwtException e) {
			LOGGER.error("JWT token is unsupported: {}", e.getMessage());
			return null;
		} catch (IllegalArgumentException e) {
			LOGGER.error("JWT claims string is empty: {}", e.getMessage());
			return null;
		}
	}

}
