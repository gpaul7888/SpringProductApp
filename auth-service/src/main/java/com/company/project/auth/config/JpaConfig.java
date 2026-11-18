package com.company.project.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Fills {@code @CreatedDate} / {@code @LastModifiedDate} fields automatically. */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing
public class JpaConfig {
}
