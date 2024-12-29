package com.project.mini_ssf.config;

import com.project.mini_ssf.service.RedisUserDetailsService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private RedisUserDetailsService redisUserDetailsService;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/set-role", "/styles.css", "/login", "/filter/**", "/product/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .successHandler(authenticationSuccessHandler()) 
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")  
            )
            .csrf(csrf -> csrf
            .ignoringRequestMatchers("/set-role") 
        );

        return http.build();
    }

    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2AuthenticationToken oauth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2AuthenticationToken.getPrincipal();
    
            HttpSession session = request.getSession();

            String role = (String) request.getSession().getAttribute("role");
            System.out.println(role);

            if (role == null || (!role.equals("buyer") && !role.equals("seller"))) {
                response.sendRedirect("/login");
                return;
            }
    
            redisUserDetailsService.storeOAuth2UserDetails(session, oauth2User, role.toUpperCase());
            
            response.sendRedirect("/" + role + "/home");
        };
    }
}
