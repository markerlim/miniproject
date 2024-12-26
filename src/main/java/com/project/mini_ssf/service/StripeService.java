package com.project.mini_ssf.service;
import com.project.mini_ssf.model.PreOrderListing;
import com.stripe.Stripe;
import com.stripe.model.PaymentLink;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.PaymentLinkCreateParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class StripeService {

    @Value("${stripe.api.key}")
    private String stripeApi;

    public String createPaymentLink(List<PreOrderListing> cartItems) throws Exception {

        Stripe.apiKey = stripeApi;

        // Group items by price ID
        Map<String, Long> priceToQuantityMap = new HashMap<>();

        for (PreOrderListing item : cartItems) {
            String stripeProductId = item.getStripeProductId();
            System.out.println(item.toString());
            Product product = Product.retrieve(stripeProductId);

            Price stripePrice = Price.retrieve(product.getDefaultPrice());
            String priceId = stripePrice.getId();

            // Aggregate quantities
            priceToQuantityMap.put(priceId, 
                priceToQuantityMap.getOrDefault(priceId, 0L) + 
                (item.getQty() != null ? item.getQty().longValue() : 1L));
        }

        // Build payment link params
        PaymentLinkCreateParams.Builder paramsBuilder = PaymentLinkCreateParams.builder();

        for (Map.Entry<String, Long> entry : priceToQuantityMap.entrySet()) {
            paramsBuilder.addLineItem(
                PaymentLinkCreateParams.LineItem.builder()
                    .setPrice(entry.getKey()) // Price ID
                    .setQuantity(entry.getValue()) // Aggregated quantity
                    .build()
            );
        }

        PaymentLink paymentLink = PaymentLink.create(paramsBuilder.build());

        return paymentLink.getUrl(); 
    }

}
