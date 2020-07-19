package com.cloud.gateway.config;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisDataConfig {
	
	@Autowired
	private EnvironmentConfig environmentConfig;

	public static final Logger logger = LoggerFactory.getLogger(RedisDataConfig.class);

	@Bean
	//@ConditionalOnMissingBean
	public JedisConnectionFactory jedisConnectionFactory() throws UnknownHostException {

		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(environmentConfig.getRedisMaxConn());
		poolConfig.setMinIdle(environmentConfig.getRedisMinIdleConn());
		poolConfig.setMaxIdle(environmentConfig.getRedisMaxIdleConn());
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);

		JedisConnectionFactory factory = new JedisConnectionFactory();
		factory.setPoolConfig(poolConfig);
		factory.setHostName(environmentConfig.getRedisHost());
		factory.setUsePool(true);
		factory.setPort(environmentConfig.getRedisPort());

		logger.info("---------jedis----------" + factory.getHostName() + " -- " + factory.getPort() + " ---------"
				+ factory.getPassword());
		return factory;
	}
    @Bean
    @ConditionalOnMissingBean
    public RedisConnectionFactory redisConnectionFactory() throws UnknownHostException {

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(environmentConfig.getRedisMaxConn());
        poolConfig.setMinIdle(environmentConfig.getRedisMinIdleConn());
        poolConfig.setMaxIdle(environmentConfig.getRedisMaxIdleConn());
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);

        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setPoolConfig(poolConfig);
        factory.setHostName(environmentConfig.getRedisHost());
        factory.setUsePool(true);
        factory.setPort(environmentConfig.getRedisPort());

        logger.info("---------REDIS----------" + factory.getHostName() + " -- " + factory.getPort() + " ---------"
                + factory.getPassword());
        return factory;
    }

	@Bean(name = "redisUserTemplate")
	public RedisTemplate<String, String> redisTemplateUser(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new GenericJackson2JsonRedisSerializer());
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		return template;
	}
}
