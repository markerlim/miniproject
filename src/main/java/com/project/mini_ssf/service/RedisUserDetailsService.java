package com.project.mini_ssf.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RedisUserDetailsService implements UserDetailsService {

    @Qualifier("object")
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String[] parts = username.split(":");
        if (parts.length != 2) {
            throw new UsernameNotFoundException("Invalid username format. Expected 'username:role'.");
        }
    
        String actualUsername = parts[0];
        String role = parts[1];
        String userKey = "user:uuid:" + actualUsername + ":" + role;
    
        String userUuid = (String) redisTemplate.opsForValue().get(userKey);
        if (userUuid == null) {
            throw new UsernameNotFoundException("User not found with username and role: " + username);
        }
    
        Map<Object, Object> userMap = redisTemplate.opsForHash().entries("user:" + userUuid);
        Map<String, String> user = new HashMap<>();
        for (Map.Entry<Object, Object> entry : userMap.entrySet()) {
            user.put((String) entry.getKey(), (String) entry.getValue());
        }
    
        String storedRole = user.get("role");
        if (!role.equals(storedRole)) {
            throw new UsernameNotFoundException("Role mismatch for user: " + actualUsername);
        }
    
        return User.withUsername(username)
                .password("")
                .roles(storedRole)
                .build();
    }
    

    public void storeOAuth2UserDetails(HttpSession session, OAuth2User oauth2User, String role) {
        String username = oauth2User.getName();
        String userKey = "user:uuid:" + username + ":" + role;
        String userUuid = (String) redisTemplate.opsForValue().get(userKey);
    
        
        if (userUuid == null) {
            userUuid = UUID.randomUUID().toString();

            redisTemplate.opsForValue().set(userKey, userUuid);
        }
    
        if(role.equals("SELLER")){
            session.setAttribute("userId", "user:" + userUuid);
        }
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
    
        Map<String, String> userDetails = new HashMap<>();
        userDetails.put("email", email);
        userDetails.put("name", name);
        userDetails.put("role", role);
        redisTemplate.opsForHash().putAll("user:" + userUuid, userDetails);
    }
    
}
