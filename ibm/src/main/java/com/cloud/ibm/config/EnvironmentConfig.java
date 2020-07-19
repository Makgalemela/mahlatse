package com.cloud.ibm.config;

public interface EnvironmentConfig {
    int getRedisMaxConn();

    int getRedisMinIdleConn();

    int getRedisMaxIdleConn();

    String getRedisHost();

    int getRedisPort();

    String getRedisPwd();

    String getProxyName();

    String getTokenSecret();
}
