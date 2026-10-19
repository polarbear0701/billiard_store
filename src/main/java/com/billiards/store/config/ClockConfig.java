package com.billiards.store.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The store's clock. Business dates ("today", payment time) are computed in the store's timezone
 * rather than whatever the host machine uses.
 */
@Configuration
public class ClockConfig {

	@Bean
	public Clock clock(
		@Value("${app.timezone:Asia/Ho_Chi_Minh}") String timezone
	) {
		return Clock.system(ZoneId.of(timezone));
	}
}
