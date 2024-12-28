package com.project.mini_ssf.config;

import com.stripe.Stripe;

public class StripeConfig {
    public static void init() {
        Stripe.apiKey = System.getenv("STRIPE_API_KEY");
    }
}

