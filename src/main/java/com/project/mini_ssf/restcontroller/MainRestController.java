package com.project.mini_ssf.restcontroller;

import java.io.Reader;
import java.io.StringReader;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.project.mini_ssf.model.EntityDetails;
import com.project.mini_ssf.model.PreOrderListing;
import com.project.mini_ssf.service.AcraService;
import com.project.mini_ssf.service.ListingService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductCreateParams.DefaultPriceData;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class MainRestController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private AcraService acraService;

    @Value("${stripe.api.key}")
    private String stripeApi;

    @PostMapping(path = "/posting", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView postNotice(@Valid @ModelAttribute PreOrderListing preOrderListing, HttpSession session,
            BindingResult result) throws StripeException {

        Stripe.apiKey = stripeApi;
        ModelAndView mav = new ModelAndView();
        if (result.hasErrors()) {
            mav.addObject("preOrderListing", preOrderListing);
            mav.setViewName("form");
            mav.setStatus(HttpStatusCode.valueOf(200));
            return mav;
        }

        Instant instant = preOrderListing.getDeadline().atStartOfDay(ZoneId.systemDefault()).toInstant();
        String id = UUID.randomUUID().toString();
        String sellerId = (String) session.getAttribute("userId");


        DefaultPriceData defaultPriceData = DefaultPriceData.builder()
                .setUnitAmount(preOrderListing.getPrice() * 100)
                .setCurrency("SGD")
                .build();

        ProductCreateParams productParams = ProductCreateParams.builder()
                .setName(preOrderListing.getTitle())
                .setDescription(preOrderListing.getContent())
                .setDefaultPriceData(defaultPriceData)
                .addImage(preOrderListing.getImage())
                .build();

        Product product = Product.create(productParams);
        
        preOrderListing.setStripeProductId(product.getId());

        JsonObjectBuilder jsonObject = Json.createObjectBuilder();
        jsonObject.add("id", id);
        jsonObject.add("title", preOrderListing.getTitle());
        jsonObject.add("content", preOrderListing.getContent());
        jsonObject.add("image", preOrderListing.getImage());
        jsonObject.add("category", preOrderListing.getCategory());
        jsonObject.add("sellerId", sellerId);
        jsonObject.add("price", preOrderListing.getPrice());
        jsonObject.add("stripeProductId", preOrderListing.getStripeProductId());
        jsonObject.add("deadline", instant.toEpochMilli());
        JsonObject Jobject = jsonObject.build();


        listingService.postToListingServer(Jobject, id);
        listingService.saveSellerPosting(sellerId);

        List<PreOrderListing> list = listingService.getSellerPostings(sellerId);
        mav.addObject("item", list);

        mav.setViewName("home-logged-in-seller");
        return mav;
    }

    @PostMapping(path = "/regAcra")
    public ModelAndView SellerDetails(@RequestParam("uen") String uen, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("home-logged-in-seller");
        EntityDetails ent = acraService.getEntityByUen(uen);
        if (ent == null || acraService.checkIfUENregistered(uen)) {
            mav.addObject("uenEntry", true);
            return mav;
        }
        String sellerId = (String) session.getAttribute("userId");
        acraService.saveAcraToSeller(sellerId, ent);
        return mav;
    }
}
