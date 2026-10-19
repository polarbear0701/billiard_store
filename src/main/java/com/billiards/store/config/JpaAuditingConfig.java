package com.billiards.store.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {
		private static final String SYSTEM_USER = "SYSTEM";
	/**
     * This bean tells Spring Data JPA who the current user is.
     * It automatically fills in your @CreatedBy and @LastModifiedBy fields.
     */
	@Bean
	public AuditorAware<String> auditorProvider () {
		return () -> {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
				return Optional.of(SYSTEM_USER);
			} 

			return Optional.of(authentication.getName());
		};
	}
	
	/* * PRO TIP: When you add Spring Security later, you will change this 
         * return statement to fetch the actual logged-in user's username:
         * * return () -> Optional.ofNullable(SecurityContextHolder.getContext())
         * .map(SecurityContext::getAuthentication)
         * .filter(Authentication::isAuthenticated)
         * .map(Authentication::getName);
         */
}