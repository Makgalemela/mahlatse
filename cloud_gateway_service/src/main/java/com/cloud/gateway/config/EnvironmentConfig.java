package com.cloud.gateway.config;

public interface EnvironmentConfig {
	String getEnvironment();

	int getRedisMaxConn();

	int getRedisMinIdleConn();

	int getRedisMaxIdleConn();

	String getRedisHost();

	int getRedisPort();

	String getRedisPwd();

	String getHeaderName();

	String getProxyName();

	Long getExpirationTime();

	String getTokenSecret();
}
