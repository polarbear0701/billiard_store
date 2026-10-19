package com.billiards.store.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.billiards.store.common.enumerated.Role;
import com.billiards.store.common.util.AuthTokenFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /** Roles allowed to run day-to-day operations (sessions, orders, invoices). */
    private static final String[] OPERATORS = {"ADMIN", "STAFF"};

    @Value("${app.username}")
    private String username;

    @Value("${app.password}")
    private String password;

    /** Extra accounts, see {@link AccountParser}. Optional. */
    @Value("${app.accounts:}")
    private String accounts;

    /**
     * Accounts live in memory and come from configuration: the app does not store user data.
     * USER_ACCOUNT / USER_PASSWORD is always an admin; APP_ACCOUNTS adds more (admin or staff).
     * Changing accounts means changing the environment and restarting.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        List<UserDetails> users = new ArrayList<>();
        Set<String> names = new HashSet<>();

        users.add(build(username, password, Role.ADMIN, passwordEncoder));
        names.add(username);
        for (AccountParser.Account account : AccountParser.parse(accounts)) {
            if (!names.add(account.username())) {
                throw new IllegalStateException(
                    "Duplicate account '" + account.username() + "' (already defined by USER_ACCOUNT)"
                );
            }
            users.add(build(account.username(), account.password(), account.role(), passwordEncoder));
        }
        return new InMemoryUserDetailsManager(users);
    }

    private static UserDetails build(String name, String rawPassword, Role role, PasswordEncoder encoder) {
        return User.withUsername(name)
            .password(encoder.encode(rawPassword))
            .roles(role.name())
            .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean 
    public SecurityFilterChain filterChain(
        HttpSecurity httpSecurity, 
        AuthTokenFilter authTokenFilter, 
        CustomAuthenticationEntryPoint unauthorizedHandler, 
        CustomAccessDeniedHandler accessDeniedHandler,
        CorsConfigurationSource corsConfigurationSource) throws Exception {
        
        
        httpSecurity
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedHandler)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                // operations: read everything needed to run the floor
                .requestMatchers(HttpMethod.GET,
                    "/table/**", "/product/**", "/play-session/**", "/order-item/**", "/invoice/**")
                    .hasAnyRole(OPERATORS)
                // operations: sessions, orders, payments
                .requestMatchers(HttpMethod.POST,
                    "/play-session/create", "/order-item/create", "/order-item/cancel/*", "/invoice/create")
                    .hasAnyRole(OPERATORS)
                .requestMatchers(HttpMethod.PUT,
                    "/play-session/start-time/*", "/play-session/close/*",
                    "/order-item/update/quantity", "/invoice/complete", "/table/occupy/*/*")
                    .hasAnyRole(OPERATORS)
                // everything else (product/table management, deletes, audit logs, ...) is admin only
                .anyRequest().hasRole("ADMIN")
            );
        httpSecurity.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }
}
