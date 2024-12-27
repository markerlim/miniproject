package com.project.mini_ssf.service;

import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.mini_ssf.model.PreOrderListing;
import com.project.mini_ssf.repo.ListingRepo;
import com.stripe.exception.StripeException;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

@Service
public class ListingService {

    @Autowired
    private ListingRepo listingRepo;

    @Autowired
    private CartService cartService;

    public void postToListingServer(JsonObject jsonObject, String id) {
        listingRepo.saveToRedis(jsonObject, id);
    }

    public List<PreOrderListing> getAllListing() {
        Map<String, String> hashMap = listingRepo.getAllListingFromRedis();
        List<PreOrderListing> preOrderListings = new ArrayList<>();

        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            Reader reader = new StringReader(entry.getValue());
            JsonReader jsonReader = Json.createReader(reader);
            JsonObject jsonMsgObj = jsonReader.readObject();

            PreOrderListing listing = new PreOrderListing();
            listing.setId(entry.getKey());
            listing.setTitle(jsonMsgObj.getString("title"));
            listing.setContent(jsonMsgObj.getString("content"));
            listing.setImage(jsonMsgObj.getString("image"));
            listing.setCategory(jsonMsgObj.getString("category"));
            listing.setSellerId(jsonMsgObj.getString("sellerId"));
            listing.setStripeProductId(jsonMsgObj.getString("stripeProductId"));
            listing.setPrice(jsonMsgObj.getJsonNumber("price").longValue());
            listing.setDeadline(LocalDate.parse(jsonMsgObj.getString("deadline", "1970-01-01")));
            System.out.println(listing.toString());
            preOrderListings.add(listing);
        }

        return preOrderListings;
    }

    public PreOrderListing getOneListing(String id) {
        String stringC = listingRepo.getOneListingFromRedis(id);

        Reader reader = new StringReader(stringC);
        JsonReader jsonReader = Json.createReader(reader);
        JsonObject jsonMsgObj = jsonReader.readObject();

        PreOrderListing pol = new PreOrderListing();
        pol.setId(id);
        pol.setTitle(jsonMsgObj.getString("title"));
        pol.setContent(jsonMsgObj.getString("content"));
        pol.setImage(jsonMsgObj.getString("image"));
        pol.setCategory(jsonMsgObj.getString("category"));
        pol.setSellerId(jsonMsgObj.getString("sellerId"));
        pol.setStripeProductId(jsonMsgObj.getString("stripeProductId"));
        pol.setPrice(jsonMsgObj.getJsonNumber("price").longValue());
        pol.setDeadline(LocalDate.parse(jsonMsgObj.getString("deadline", "1970-01-01")));

        return pol;
    }

    public void saveSellerPosting(String userUuid) throws StripeException {

        List<PreOrderListing> allListings = getAllListing();
        List<PreOrderListing> filteredListings = allListings.stream()
                .filter(listing -> userUuid.equals(listing.getSellerId()))
                .collect(Collectors.toList());

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

        for (PreOrderListing listing : filteredListings) {

            JsonObject listingJson = Json.createObjectBuilder()
                    .add("id", listing.getId())
                    .add("title", listing.getTitle())
                    .add("content", listing.getContent())
                    .add("image", listing.getImage())
                    .add("category", listing.getCategory())
                    .add("sellerId", listing.getSellerId())
                    .add("price", listing.getPrice())
                    .add("stripeProductId", listing.getStripeProductId())
                    .add("deadline", listing.getDeadline().toString())
                    .build();

            jsonObjectBuilder.add(listing.getId(), listingJson);
        }

        JsonObject finalJsonObject = jsonObjectBuilder.build();

        listingRepo.saveUserListing(userUuid, finalJsonObject);
    }

    public void saveBuyerPurchases(String userUuid) throws StripeException {
        List<PreOrderListing> list = cartService.getCartItems();
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

        for (PreOrderListing listing : list) {

            JsonObject listingJson = Json.createObjectBuilder()
                    .add("id", listing.getId())
                    .add("title", listing.getTitle())
                    .add("content", listing.getContent())
                    .add("image", listing.getImage())
                    .add("category", listing.getCategory())
                    .add("sellerId", listing.getSellerId())
                    .add("price", listing.getPrice())
                    .add("stripeProductId", listing.getStripeProductId())
                    .add("deadline", listing.getDeadline().toString())
                    .build();

            jsonObjectBuilder.add(listing.getId(), listingJson);
        }

        JsonObject finalJsonObject = jsonObjectBuilder.build();

        listingRepo.saveUserPurchases(userUuid, finalJsonObject);
    }

    public List<PreOrderListing> getSellerPostings(String userUuid) {
        String list = listingRepo.getuserPosting(userUuid);
        if (list == null || list.isEmpty()) {
            return null;
        }
        Reader reader = new StringReader(list);
        JsonReader jsonReader = Json.createReader(reader);
        JsonObject jsonMsgObj = jsonReader.readObject();

        List<PreOrderListing> listings = new ArrayList<>();

        for (String listingId : jsonMsgObj.keySet()) {
            JsonObject listingJson = jsonMsgObj.getJsonObject(listingId);

            PreOrderListing preOrderListing = new PreOrderListing();
            preOrderListing.setId(listingJson.getString("id"));
            preOrderListing.setTitle(listingJson.getString("title"));
            preOrderListing.setContent(listingJson.getString("content"));
            preOrderListing.setImage(listingJson.getString("image"));
            preOrderListing.setCategory(listingJson.getString("category"));
            preOrderListing.setSellerId(listingJson.getString("sellerId"));
            preOrderListing.setStripeProductId(listingJson.getString("stripeProductId"));
            preOrderListing.setPrice(listingJson.getJsonNumber("price").longValue());
            preOrderListing.setDeadline(LocalDate.parse(jsonMsgObj.getString("deadline", "1970-01-01")));

            listings.add(preOrderListing);
        }
        return listings;
    }

    public List<PreOrderListing> getBuyerPurchases(String userUuid) {
        String list = listingRepo.getuserPurchases(userUuid);
        if (list == null || list.isEmpty()) {
            return null;
        }
        Reader reader = new StringReader(list);
        JsonReader jsonReader = Json.createReader(reader);
        JsonObject jsonMsgObj = jsonReader.readObject();

        List<PreOrderListing> listings = new ArrayList<>();

        for (String listingId : jsonMsgObj.keySet()) {
            JsonObject listingJson = jsonMsgObj.getJsonObject(listingId);

            PreOrderListing preOrderListing = new PreOrderListing();
            preOrderListing.setId(listingJson.getString("id"));
            preOrderListing.setTitle(listingJson.getString("title"));
            preOrderListing.setContent(listingJson.getString("content"));
            preOrderListing.setImage(listingJson.getString("image"));
            preOrderListing.setCategory(listingJson.getString("category"));
            preOrderListing.setSellerId(listingJson.getString("sellerId"));
            preOrderListing.setStripeProductId(listingJson.getString("stripeProductId"));
            preOrderListing.setPrice(listingJson.getJsonNumber("price").longValue());
            preOrderListing.setDeadline(LocalDate.parse(jsonMsgObj.getString("deadline", "1970-01-01")));

            listings.add(preOrderListing);
        }
        return listings;

    }
}
