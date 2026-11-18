package com.company.project.product.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Kept out of the application class so web-layer slice tests don't need JPA. */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing
public class JpaConfig {
}
