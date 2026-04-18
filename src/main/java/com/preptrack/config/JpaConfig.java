package com.preptrack.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @EnableJpaAuditing makes @CreatedDate and @LastModifiedDate annotations work
// It hooks into JPA lifecycle events (prePersist, preUpdate) to set timestamps automatically
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
