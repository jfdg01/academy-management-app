package app.rest;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;

import app.servicios.ServicioJwt;

/**
 * Base test configuration for all REST tests
 * Provides common security setup and required mocks
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("test")
@TestPropertySource(properties = {
    "spring.security.csrf.enabled=false"
})
@EnableAutoConfiguration(exclude = {
    app.config.WebConfig.class,
    app.config.ApiLoggingInterceptor.class,
    app.config.RequestResponseLoggingFilter.class
})
public class BaseRestTestConfig {
    
    /**
     * Mock for ServicioJwt which is required by the security configuration and ApiLoggingInterceptor
     */
    @MockBean
    private ServicioJwt servicioJwt;
    
    /**
     * Mock for ApiLoggingInterceptor to prevent autowiring issues
     */
    @MockBean
    private app.config.ApiLoggingInterceptor apiLoggingInterceptor;
    
    /**
     * Security filter chain for testing that disables CSRF and requires authentication
     * This configuration is only used in tests and doesn't conflict with the main security config
     */
    @Bean
    @Profile("test")
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
            );
        
        return http.build();
    }
}


