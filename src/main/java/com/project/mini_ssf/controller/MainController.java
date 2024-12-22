package com.project.mini_ssf.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("")
public class MainController {

    @GetMapping("")
    public ModelAndView HomePage() {
        ModelAndView modelAndView = new ModelAndView("home");  

        if (SecurityContextHolder.getContext().getAuthentication() != null &&
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            modelAndView.addObject("loginMessage", "Successfully logged in!");
        } else {
            modelAndView.addObject("loginMessage", "You are not logged in.");
        }

        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView LoginPage() {
        ModelAndView modelAndView = new ModelAndView("login");
        modelAndView.addObject("loginMessage", "Please log in.");
        return modelAndView;
    }

    @GetMapping("/home")
    public ModelAndView LoggedInHomePage(@AuthenticationPrincipal OAuth2User oAuth2User) {
        ModelAndView modelAndView = new ModelAndView("home-logged-in");

        if (oAuth2User != null) {
            modelAndView.addObject("userName", oAuth2User.getName());
        }

        return modelAndView;
    }

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/";
    }
}
