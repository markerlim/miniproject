package com.project.mini_ssf.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.project.mini_ssf.model.PreOrderListing;
import com.project.mini_ssf.service.CartService;
import com.project.mini_ssf.service.StripeService;
import com.stripe.Stripe;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
public class PaymentController {

    @Autowired
    private CartService cartService;

    @Autowired
    private StripeService stripeService;

    @Value("${stripe.api.key}")
    private String stripePublicKey;

    @GetMapping("/payment-link")
    public RedirectView generatePaymentLink(HttpSession session) {
        Stripe.apiKey = stripePublicKey;

        try {
            List<PreOrderListing> cartItems = cartService.getCartItems(); 
            if (cartItems.isEmpty()) {
                return new RedirectView("/cart-empty");
            }
            String paymentUrl = stripeService.createPaymentLink(cartItems);
            return new RedirectView(paymentUrl); 
        } catch (Exception e) {
            RedirectView redirectView = new RedirectView("/error");
            redirectView.addStaticAttribute("message", "Error creating payment link: " + e.getMessage());
            return redirectView;
        }
    }
}
