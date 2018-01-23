package com.tus.oss.server.impl;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ckaratza
 * The redis client configuration provider.
 */
@Configuration
public class RedisConfig {

    @Bean
    RedisClient redisClient(@Value("${redis.host}") String host, @Value("${redis.port}") Integer port) {
        return RedisClient.create(RedisURI.create(host, port));
    }
}
