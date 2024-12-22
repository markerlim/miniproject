package com.project.mini_ssf;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventListener {

    @EventListener
    public void handleLoginSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        System.out.println("User logged in successfully: " + username);
    }

    @EventListener
    public void handleLoginFailure(AuthenticationFailureBadCredentialsEvent event) {
        System.out.println("Failed login attempt: " + event.getException().getMessage());
    }

    @EventListener
    public void handleLogout(LogoutSuccessEvent event) {
        String username = event.getAuthentication().getName();
        System.out.println("User logged out successfully: " + username);
    }
}
