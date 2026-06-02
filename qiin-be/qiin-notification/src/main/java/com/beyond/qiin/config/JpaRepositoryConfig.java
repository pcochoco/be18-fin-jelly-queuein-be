package com.beyond.qiin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.beyond.qiin.domain")
public class JpaRepositoryConfig {}
