package com.project.mini_ssf.config;

import com.project.mini_ssf.service.RedisUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RedisUserDetailsService redisUserDetailsService;

    public SecurityConfig(RedisUserDetailsService redisUserDetailsService) {
        this.redisUserDetailsService = redisUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .defaultSuccessUrl("/home", true) 
                .failureUrl("/home")
                .successHandler(authenticationSuccessHandler())  
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/home")  
            )
            .rememberMe(rememberMe -> rememberMe
                .userDetailsService(redisUserDetailsService)  
                .key("REMEMBERME")  
            );

        return http.build();
    }

    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2AuthenticationToken oauth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2AuthenticationToken.getPrincipal();
            
            redisUserDetailsService.storeOAuth2UserDetails(oauth2User);  

            response.sendRedirect("/home");
        };
    }
}
