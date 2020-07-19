package com.cloud.gateway.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentConfigImpl implements EnvironmentConfig {


	@Value("${spring.redis.password}")
	private String redisPwd;

	@Value("${spring.redis.jedis.pool.max-active}")
	private int redisMaxConn;

	@Value("${spring.redis.jedis.pool.min-idle}")
	private int redisMinIdleConn;

	@Value("${spring.redis.jedis.pool.max-idle}")
	private int redisMaxIdleConn;

	@Value("${spring.redis.port}")
	private int redisPort;

	@Value("${spring.redis.host}")
	private String redisHost;
	
	@Value("${authorizationTokenHeaderName}")
	private String headerName;
	
	@Value("${authorizationTokenPrefix}")
	private String proxyName;
	
	@Value("${expirationTime}")
	private Long expirationTime;
	
	@Value("${tokenSecret}")
	private String tokenSecret;
	
	@Override
	public String getEnvironment() {
		return "Environment";
	}

	@Override
	public int getRedisMaxConn() {
		return redisMaxConn;
	}

	@Override
	public int getRedisMinIdleConn() {
		return redisMinIdleConn;
	}

	@Override
	public int getRedisMaxIdleConn() {
		return redisMaxIdleConn;
	}

	@Override
	public String getRedisHost() {
		return redisHost;
	}

	@Override
	public int getRedisPort() {
		return redisPort;
	}

	/**
	 * @return the redisPwd
	 */
	public String getRedisPwd() {
		return redisPwd;
	}

	/**
	 * @param redisPwd
	 *            the redisPwd to set
	 */
	public void setRedisPwd(String redisPwd) {
		this.redisPwd = redisPwd;
	}

	@Override
	public String getHeaderName() {
		// TODO Auto-generated method stub
		return headerName;
	}

	@Override
	public String getProxyName() {
		// TODO Auto-generated method stub
		return proxyName;
	}

	@Override
	public Long getExpirationTime() {
		// TODO Auto-generated method stub
		return expirationTime;
	}

	@Override
	public String getTokenSecret() {
		// TODO Auto-generated method stub
		return tokenSecret;
	}

}
