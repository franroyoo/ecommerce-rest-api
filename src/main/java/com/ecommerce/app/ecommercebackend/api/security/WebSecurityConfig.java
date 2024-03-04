package com.ecommerce.app.ecommercebackend.api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationFilter;


@Configuration
public class WebSecurityConfig {

    private JWTRequestFilter jwtRequestFilter;

    @Autowired
    public WebSecurityConfig(JWTRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf().disable().cors().disable();
        http.addFilterBefore(jwtRequestFilter, AuthorizationFilter.class);
        http.authorizeHttpRequests()
                .requestMatchers("/product/**","/auth/register","/auth/login","/auth/verify","/v3/**","/swagger-ui/**","/error").permitAll()
                .anyRequest().authenticated();
        return http.build();
    }
}
