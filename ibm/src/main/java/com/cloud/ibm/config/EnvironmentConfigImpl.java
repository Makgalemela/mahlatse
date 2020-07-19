package com.cloud.ibm.config;

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

    @Value("${authorizationTokenPrefix}")
    private String proxyName;

    @Value("${tokenSecret}")
    private String tokenSecret;

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

    @Override
    public String getRedisPwd() {
        return redisPwd;
    }

    @Override
    public String getProxyName() {
        return proxyName;
    }

    @Override
    public String getTokenSecret() {
        return tokenSecret;
    }
}
