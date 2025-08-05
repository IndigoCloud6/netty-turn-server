package com.example.turnserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for the TURN server
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    /**
     * Configure HTTP security
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .antMatchers("/", "/index.html", "/static/**").permitAll()
                .antMatchers("/actuator/health", "/actuator/info").permitAll()
                .antMatchers("/api/turn/ping", "/api/turn/info").permitAll()
                .antMatchers("/api/statistics/health").permitAll()
                
                // Admin endpoints - require authentication
                .antMatchers("/api/users/**").hasRole("ADMIN")
                .antMatchers("/api/turn/cleanup").hasRole("ADMIN")
                
                // Statistics endpoints - require authentication but allow USER role
                .antMatchers("/api/statistics/**").hasAnyRole("USER", "ADMIN")
                .antMatchers("/api/turn/status", "/api/turn/sessions/**", "/api/turn/allocations/**").hasAnyRole("USER", "ADMIN")
                
                // Default deny
                .anyRequest().authenticated()
            )
            .httpBasic();
        
        return http.build();
    }
    
    /**
     * Password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}