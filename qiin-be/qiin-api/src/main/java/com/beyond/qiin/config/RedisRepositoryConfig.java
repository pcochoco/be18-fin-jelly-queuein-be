package com.beyond.qiin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(basePackages = "com.beyond.qiin.infra.redis")
public class RedisRepositoryConfig {}
