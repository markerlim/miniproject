package com.project.mini_ssf.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RedisUserDetailsService implements UserDetailsService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUserDetailsService(@Qualifier("object") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String userUuid = (String) redisTemplate.opsForValue().get("user:uuid:" + username);
        if (userUuid == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        Map<Object, Object> userMap = redisTemplate.opsForHash().entries("user:" + userUuid);
        Map<String, String> user = new HashMap<>();
        for (Map.Entry<Object, Object> entry : userMap.entrySet()) {
            user.put((String) entry.getKey(), (String) entry.getValue());
        }        
        String role = user.get("role");

        return User.withUsername(username)
                .password("")  
                .roles(role)  
                .build();
    }

    public void storeOAuth2UserDetails(OAuth2User oauth2User) {
        String username = oauth2User.getName();  
        String userUuid = (String) redisTemplate.opsForValue().get("user:uuid:" + username);
        
        if (userUuid == null) {
            userUuid = UUID.randomUUID().toString();

            redisTemplate.opsForValue().set("user:uuid:" + username, userUuid);
        }

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        Map<String, String> userDetails = new HashMap<>();
        userDetails.put("email", email);
        userDetails.put("name", name);
        userDetails.put("role", "USER");
        
        redisTemplate.opsForHash().putAll("user:" + userUuid, userDetails);
    }
}
