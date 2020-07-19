package com.cloud.gateway.security;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.cloud.gateway.config.EnvironmentConfig;
import com.cloud.gateway.domain.User;
import com.cloud.gateway.service.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Service
public class JwtService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);

	@Autowired
	private EnvironmentConfig config;
	
	@Autowired
	private RedisService redisService;

	public static UserDetails getLoggedInUser() {
		return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public String getToken(User user) {
		Claims claims = Jwts.claims().setSubject(user.getEmail());
		claims.put("userId", user.getId());
		claims.put("email", user.getEmail());
		String authorization = Jwts.builder().setSubject(""+user.getId()).setClaims(claims)
				.setExpiration(new Date(System.currentTimeMillis() + config.getExpirationTime()))
				.signWith(SignatureAlgorithm.HS512, config.getTokenSecret()).compact();
		List<String> sessions = redisService.getDataFromRedis(user.getEmail());
		sessions.add(config.getProxyName() + " "+authorization);
		redisService.saveDataInRedis(user.getEmail(), sessions);
		return authorization;
	}

	public Claims parseJwtToken(String token) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		return parseJwtToken(token, true);
	}

	public Claims parseJwtToken(String token, boolean suppressExpiredTokenExceptions)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		if (token == null || token.isEmpty()) {
			return null;
		}
		LOGGER.info("nfo ::: {} ,{}",config.getProxyName(),config.getTokenSecret());
		token = token.replace(config.getProxyName(), "");
		LOGGER.info("token ::: {} ",token);
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
