package com.project.mini_ssf.service;

import java.io.Reader;
import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.mini_ssf.model.EntityDetails;
import com.project.mini_ssf.model.Orders;
import com.project.mini_ssf.model.PreOrderListing;
import com.project.mini_ssf.model.SellerOrderTracker;
import com.project.mini_ssf.repo.ListingRepo;
import com.stripe.exception.StripeException;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
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
        listingRepo.saveToListingHistory(jsonObject, id);
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
            listing.setDeadline(
                    Instant.ofEpochMilli(jsonMsgObj.getJsonNumber("deadline").longValue())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
            listing.setBuyerConfirmSalesComplete(jsonMsgObj.getBoolean("buyerConfirmSalesComplete"));
            listing.setSellerConfirmSalesComplete(jsonMsgObj.getBoolean("sellerConfirmSalesComplete"));
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
        pol.setDeadline(
                Instant.ofEpochMilli(jsonMsgObj.getJsonNumber("deadline").longValue())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
        pol.setBuyerConfirmSalesComplete(jsonMsgObj.getBoolean("buyerConfirmSalesComplete"));
        pol.setSellerConfirmSalesComplete(jsonMsgObj.getBoolean("sellerConfirmSalesComplete"));
        return pol;
    }

    public PreOrderListing getOneListingFromHistory(String id) {
        String stringC = listingRepo.getOneListingFromHistory(id);

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
        pol.setDeadline(
                Instant.ofEpochMilli(jsonMsgObj.getJsonNumber("deadline").longValue())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
        pol.setBuyerConfirmSalesComplete(jsonMsgObj.getBoolean("buyerConfirmSalesComplete"));
        pol.setSellerConfirmSalesComplete(jsonMsgObj.getBoolean("sellerConfirmSalesComplete"));
        return pol;
    }

    public void saveSellerPosting(String userUuid) throws StripeException {

        List<PreOrderListing> allListings = getAllListing();
        List<PreOrderListing> filteredListings = allListings.stream()
                .filter(listing -> userUuid.equals(listing.getSellerId()))
                .collect(Collectors.toList());

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

        for (PreOrderListing listing : filteredListings) {

            Instant instant = listing.getDeadline().atStartOfDay(ZoneId.systemDefault()).toInstant();

            JsonObject listingJson = Json.createObjectBuilder()
                    .add("id", listing.getId())
                    .add("title", listing.getTitle())
                    .add("content", listing.getContent())
                    .add("image", listing.getImage())
                    .add("category", listing.getCategory())
                    .add("sellerId", listing.getSellerId())
                    .add("price", listing.getPrice())
                    .add("stripeProductId", listing.getStripeProductId())
                    .add("deadline", instant.toEpochMilli())
                    .add("buyerConfirmSalesComplete", listing.getBuyerConfirmSalesComplete())
                    .add("sellerConfirmSalesComplete", listing.getSellerConfirmSalesComplete())
                    .build();

            jsonObjectBuilder.add(listing.getId(), listingJson);
        }

        JsonObject finalJsonObject = jsonObjectBuilder.build();

        listingRepo.saveUserListing(userUuid, finalJsonObject);
        listingRepo.saveUserListingHistory(userUuid, finalJsonObject);
    }

    public void updateSellerOrder(String userUuid, PreOrderListing listing, int quantity, String orderId,
            Boolean sellerConfirmSalesComplete, Boolean buyerConfirmSalesComplete) {
        List<SellerOrderTracker> listOfOrders = getSellerOrderTracker(listing.getSellerId());

        if (listOfOrders == null) {
            listOfOrders = new ArrayList<>();
        }

        SellerOrderTracker purchaseTracking = new SellerOrderTracker();
        purchaseTracking.setBuyerId(userUuid);
        purchaseTracking.setListingId(listing.getId());
        purchaseTracking.setQuantity(quantity);
        purchaseTracking.setPurchaseDate(LocalDate.now());
        purchaseTracking.setBuyerEmail(listingRepo.getUserEmail(userUuid));
        purchaseTracking.setBuyerName(listingRepo.getUserName(userUuid));
        purchaseTracking.setOrderId(orderId);
        purchaseTracking.setSellerConfirmSalesComplete(sellerConfirmSalesComplete);
        purchaseTracking.setBuyerConfirmSalesComplete(buyerConfirmSalesComplete);
        listOfOrders.add(purchaseTracking);

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        for (SellerOrderTracker order : listOfOrders) {
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("buyerId", order.getBuyerId())
                    .add("listingId", order.getListingId())
                    .add("quantity", order.getQuantity())
                    .add("purchaseDate", order.getPurchaseDate().toString())
                    .add("buyerEmail", order.getBuyerEmail())
                    .add("buyerName", order.getBuyerName())
                    .add("orderId", order.getOrderId())
                    .add("buyerConfirmSalesComplete", order.getBuyerConfirmSalesComplete())
                    .add("sellerConfirmSalesComplete", order.getSellerConfirmSalesComplete())
                    .build();
            jsonObjectBuilder.add(order.getOrderId(), jsonObject);
        }

        listingRepo.saveSellerSales(listing.getSellerId(), jsonObjectBuilder.build());
    }

    public void saveBuyerPurchases(String userUuid) {
        List<PreOrderListing> list = cartService.getCartItems();

        List<Orders> existingOrders = getBuyerPurchases(userUuid);

        Orders newOrder = new Orders();
        newOrder.setOrderId(UUID.randomUUID().toString());
        newOrder.setListing(list);
        newOrder.setOrderDate(LocalDate.now());

        if (existingOrders == null) {
            existingOrders = new ArrayList<>();
        }
        existingOrders.add(newOrder);

        JsonArrayBuilder ordersArrayBuilder = Json.createArrayBuilder();

        for (Orders order : existingOrders) {

            Instant instant2 = order.getOrderDate().atStartOfDay(ZoneId.systemDefault()).toInstant();

            JsonObjectBuilder orderJsonBuilder = Json.createObjectBuilder();
            orderJsonBuilder.add("orderId", order.getOrderId());
            orderJsonBuilder.add("orderDate", instant2.toEpochMilli());
            JsonArrayBuilder listingsArrayBuilder = Json.createArrayBuilder();
            for (PreOrderListing listing : order.getListing()) {

                Instant instant = listing.getDeadline().atStartOfDay(ZoneId.systemDefault()).toInstant();

                JsonObject listingJson = Json.createObjectBuilder()
                        .add("id", listing.getId())
                        .add("title", listing.getTitle())
                        .add("content", listing.getContent())
                        .add("image", listing.getImage())
                        .add("category", listing.getCategory())
                        .add("sellerId", listing.getSellerId())
                        .add("price", listing.getPrice())
                        .add("qty", listing.getQty())
                        .add("stripeProductId", listing.getStripeProductId())
                        .add("deadline", instant.toEpochMilli())
                        .add("buyerConfirmSalesComplete", listing.getBuyerConfirmSalesComplete())
                        .add("sellerConfirmSalesComplete", listing.getSellerConfirmSalesComplete())
                        .build();

                listingsArrayBuilder.add(listingJson);
                updateSellerOrder(userUuid, listing, listing.getQty(), order.getOrderId(),
                        listing.getSellerConfirmSalesComplete(), listing.getBuyerConfirmSalesComplete());
            }

            orderJsonBuilder.add("listings", listingsArrayBuilder);
            ordersArrayBuilder.add(orderJsonBuilder.build());

        }

        JsonObjectBuilder finalJsonObjectBuilder = Json.createObjectBuilder();
        finalJsonObjectBuilder.add("orders", ordersArrayBuilder);

        JsonObject finalJsonObject = finalJsonObjectBuilder.build();

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
            preOrderListing.setDeadline(
                    Instant.ofEpochMilli(listingJson.getJsonNumber("deadline").longValue())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
            preOrderListing.setBuyerConfirmSalesComplete(listingJson.getBoolean("buyerConfirmSalesComplete"));
            preOrderListing.setSellerConfirmSalesComplete(listingJson.getBoolean("sellerConfirmSalesComplete"));
            listings.add(preOrderListing);
        }
        return listings;
    }

    public void deleteListingFromSellerPostings(String userUuid, String listingIdToDelete) {
        List<PreOrderListing> list = getSellerPostings(userUuid);  // Get all the listings for the seller
        
        if (list == null || list.isEmpty()) {
            System.out.println("No listings found for this user.");
            return;
        }
        
        PreOrderListing listingToRemove = null;
        for (PreOrderListing listing : list) {
            if (listing.getId().equals(listingIdToDelete)) {
                listingToRemove = listing;
                break;
            }
        }
    
        if (listingToRemove != null) {
            list.remove(listingToRemove);
            System.out.println("Listing removed: " + listingIdToDelete);
    
            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

            for (PreOrderListing listing : list) {
                jsonBuilder.add(listing.getId(), Json.createObjectBuilder()
                        .add("id", listing.getId())
                        .add("title", listing.getTitle())
                        .add("content", listing.getContent())
                        .add("image", listing.getImage())
                        .add("category", listing.getCategory())
                        .add("sellerId", listing.getSellerId())
                        .add("stripeProductId", listing.getStripeProductId())
                        .add("price", listing.getPrice())
                        .add("deadline", listing.getDeadline().toString())
                        .add("buyerConfirmSalesComplete", listing.getBuyerConfirmSalesComplete())
                        .add("sellerConfirmSalesComplete", listing.getSellerConfirmSalesComplete())
                        .build());
            }

            listingRepo.saveUserListing(userUuid, jsonBuilder.build());
        } else {
            System.out.println("Listing with ID " + listingIdToDelete + " not found.");
        }
    }
    
    public List<Orders> getBuyerPurchases(String userUuid) {
        String list = listingRepo.getuserPurchases(userUuid);

        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        Reader reader = new StringReader(list);
        JsonReader jsonReader = Json.createReader(reader);
        JsonObject jsonMsgObj = jsonReader.readObject();

        JsonArray ordersArray = jsonMsgObj.getJsonArray("orders");

        if (ordersArray == null || ordersArray.isEmpty()) {
            return new ArrayList<>();
        }

        List<Orders> ordersList = new ArrayList<>();

        for (JsonObject orderJson : ordersArray.getValuesAs(JsonObject.class)) {
            Orders orders = new Orders();
            orders.setOrderId(orderJson.getString("orderId"));
            orders.setOrderDate(
                    Instant.ofEpochMilli(orderJson.getJsonNumber("orderDate").longValue())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
            List<PreOrderListing> listings = new ArrayList<>();

            JsonArray listingsArray = orderJson.getJsonArray("listings");

            for (JsonObject listingJson : listingsArray.getValuesAs(JsonObject.class)) {
                PreOrderListing preOrderListing = new PreOrderListing();
                preOrderListing.setId(listingJson.getString("id"));
                preOrderListing.setTitle(listingJson.getString("title"));
                preOrderListing.setContent(listingJson.getString("content"));
                preOrderListing.setImage(listingJson.getString("image"));
                preOrderListing.setCategory(listingJson.getString("category"));
                preOrderListing.setSellerId(listingJson.getString("sellerId"));
                preOrderListing.setQty(listingJson.getJsonNumber("qty").intValue());
                preOrderListing.setStripeProductId(listingJson.getString("stripeProductId"));
                preOrderListing.setPrice(listingJson.getJsonNumber("price").longValue());
                preOrderListing.setDeadline(
                        Instant.ofEpochMilli(listingJson.getJsonNumber("deadline").longValue())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate());
                preOrderListing.setBuyerConfirmSalesComplete(listingJson.getBoolean("buyerConfirmSalesComplete"));
                preOrderListing.setSellerConfirmSalesComplete(listingJson.getBoolean("sellerConfirmSalesComplete"));
                listings.add(preOrderListing);
            }

            orders.setListing(listings);
            ordersList.add(orders);
        }

        return ordersList;
    }

    public List<SellerOrderTracker> getSellerOrderTracker(String userUuid) {
        String sellerOrdersJson = listingRepo.getSellerOrders(userUuid);

        List<SellerOrderTracker> sellerOrderTrackers = new ArrayList<>();

        if (sellerOrdersJson != null && !sellerOrdersJson.isEmpty()) {
            JsonReader jsonReader = Json.createReader(new StringReader(sellerOrdersJson));
            JsonObject jsonObject = jsonReader.readObject();

            for (String key : jsonObject.keySet()) {
                JsonObject orderJson = jsonObject.getJsonObject(key);

                SellerOrderTracker orderTracker = new SellerOrderTracker();
                orderTracker.setBuyerId(orderJson.getString("buyerId"));
                orderTracker.setListingId(orderJson.getString("listingId"));
                orderTracker.setQuantity(orderJson.getInt("quantity"));
                orderTracker.setPurchaseDate(LocalDate.parse(orderJson.getString("purchaseDate")));
                orderTracker.setBuyerEmail(orderJson.getString("buyerEmail"));
                orderTracker.setBuyerName(orderJson.getString("buyerName"));
                orderTracker.setOrderId(orderJson.getString("orderId"));
                orderTracker.setSellerConfirmSalesComplete(orderJson.getBoolean("sellerConfirmSalesComplete"));
                orderTracker.setBuyerConfirmSalesComplete(orderJson.getBoolean("buyerConfirmSalesComplete"));

                PreOrderListing listing = getOneListingFromHistory(orderTracker.getListingId());

                List<PreOrderListing> listings = new ArrayList<>();
                listings.add(listing);
                orderTracker.setListing(listings);

                sellerOrderTrackers.add(orderTracker);
            }
        }

        return sellerOrderTrackers;
    }

    public List<EntityDetails> getAllSellers() {
        Map<String, String> hashMap = listingRepo.getAllSellersFromRedis();
        List<EntityDetails> listDetails = new ArrayList<>();

        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            Reader reader = new StringReader(entry.getValue());
            JsonReader jsonReader = Json.createReader(reader);
            JsonObject jsonMsgObj = jsonReader.readObject();

            EntityDetails ent = new EntityDetails();
            ent.setUen(jsonMsgObj.getString("uen", null)); // Use default null for missing keys
            ent.setIssuanceAgencyId(jsonMsgObj.getString("issuanceAgencyId", null));
            ent.setUenStatus(jsonMsgObj.getString("uenStatus", null));
            ent.setEntityName(jsonMsgObj.getString("entityName", null));
            ent.setEntityType(jsonMsgObj.getString("entityType", null));
            ent.setUenIssueDate(jsonMsgObj.containsKey("uenIssueDate")
                    ? LocalDate.parse(jsonMsgObj.getString("uenIssueDate"))
                    : null);
            ent.setRegStreetName(jsonMsgObj.getString("regStreetName", null));
            ent.setRegPostalCode(jsonMsgObj.getString("regPostalCode", null));
            ent.setSellerId(jsonMsgObj.getString("sellerId", null));
            ent.setSellerEmail(jsonMsgObj.getString("sellerEmail", null));
            ent.setSellerName(jsonMsgObj.getString("sellerName", null));
            listDetails.add(ent);
        }

        return listDetails;
    }

    public void setSellerConfirmSalesComplete(String userId, String orderId, String listingId) {
        List<Orders> buyerlist = getBuyerPurchases(userId);
        for (Orders order : buyerlist) {
            if (order.getOrderId().equals(orderId)) {
                List<PreOrderListing> pol = order.getListing();
                for (PreOrderListing preOrderListing : pol) {
                    if (preOrderListing.getId().equals(listingId)) {
                        preOrderListing.setSellerConfirmSalesComplete(true);
                    }
                    continue;
                }
            }
            continue;
        }

        JsonArrayBuilder ordersArrayBuilder = Json.createArrayBuilder();

        for (Orders order : buyerlist) {

            Instant instant2 = order.getOrderDate().atStartOfDay(ZoneId.systemDefault()).toInstant();

            JsonObjectBuilder orderJsonBuilder = Json.createObjectBuilder();
            orderJsonBuilder.add("orderId", order.getOrderId());
            orderJsonBuilder.add("orderDate", instant2.toEpochMilli());
            JsonArrayBuilder listingsArrayBuilder = Json.createArrayBuilder();
            for (PreOrderListing listing : order.getListing()) {

                Instant instant = listing.getDeadline().atStartOfDay(ZoneId.systemDefault()).toInstant();

                JsonObject listingJson = Json.createObjectBuilder()
                        .add("id", listing.getId())
                        .add("title", listing.getTitle())
                        .add("content", listing.getContent())
                        .add("image", listing.getImage())
                        .add("category", listing.getCategory())
                        .add("sellerId", listing.getSellerId())
                        .add("price", listing.getPrice())
                        .add("qty", listing.getQty())
                        .add("stripeProductId", listing.getStripeProductId())
                        .add("deadline", instant.toEpochMilli())
                        .add("buyerConfirmSalesComplete", listing.getBuyerConfirmSalesComplete())
                        .add("sellerConfirmSalesComplete", listing.getSellerConfirmSalesComplete())
                        .build();

                listingsArrayBuilder.add(listingJson);
                updateSellerOrder(userId, listing, listing.getQty(), order.getOrderId(),
                        listing.getSellerConfirmSalesComplete(), listing.getBuyerConfirmSalesComplete());
            }

            orderJsonBuilder.add("listings", listingsArrayBuilder);
            ordersArrayBuilder.add(orderJsonBuilder.build());

        }

        JsonObjectBuilder finalJsonObjectBuilder = Json.createObjectBuilder();
        finalJsonObjectBuilder.add("orders", ordersArrayBuilder);

        JsonObject finalJsonObject = finalJsonObjectBuilder.build();

        listingRepo.saveUserPurchases(userId, finalJsonObject);

    }

    public void setBuyerConfirmSalesComplete(String userId, String orderId, String listingId) {
        List<Orders> buyerlist = getBuyerPurchases(userId);
        for (Orders order : buyerlist) {
            if (order.getOrderId().equals(orderId)) {
                List<PreOrderListing> pol = order.getListing();
                for (PreOrderListing preOrderListing : pol) {
                    if (preOrderListing.getId().equals(listingId)) {
                        preOrderListing.setBuyerConfirmSalesComplete(true);
                    }
                    continue;
                }
            }
            continue;
        }

        JsonArrayBuilder ordersArrayBuilder = Json.createArrayBuilder();

        for (Orders order : buyerlist) {

            Instant instant2 = order.getOrderDate().atStartOfDay(ZoneId.systemDefault()).toInstant();

            JsonObjectBuilder orderJsonBuilder = Json.createObjectBuilder();
            orderJsonBuilder.add("orderId", order.getOrderId());
            orderJsonBuilder.add("orderDate", instant2.toEpochMilli());
            JsonArrayBuilder listingsArrayBuilder = Json.createArrayBuilder();
            for (PreOrderListing listing : order.getListing()) {

                Instant instant = listing.getDeadline().atStartOfDay(ZoneId.systemDefault()).toInstant();

                JsonObject listingJson = Json.createObjectBuilder()
                        .add("id", listing.getId())
                        .add("title", listing.getTitle())
                        .add("content", listing.getContent())
                        .add("image", listing.getImage())
                        .add("category", listing.getCategory())
                        .add("sellerId", listing.getSellerId())
                        .add("price", listing.getPrice())
                        .add("qty", listing.getQty())
                        .add("stripeProductId", listing.getStripeProductId())
                        .add("deadline", instant.toEpochMilli())
                        .add("buyerConfirmSalesComplete", listing.getBuyerConfirmSalesComplete())
                        .add("sellerConfirmSalesComplete", listing.getSellerConfirmSalesComplete())
                        .build();

                listingsArrayBuilder.add(listingJson);
                updateSellerOrder(userId, listing, listing.getQty(), order.getOrderId(),
                        listing.getSellerConfirmSalesComplete(), listing.getBuyerConfirmSalesComplete());
            }

            orderJsonBuilder.add("listings", listingsArrayBuilder);
            ordersArrayBuilder.add(orderJsonBuilder.build());

        }

        JsonObjectBuilder finalJsonObjectBuilder = Json.createObjectBuilder();
        finalJsonObjectBuilder.add("orders", ordersArrayBuilder);

        JsonObject finalJsonObject = finalJsonObjectBuilder.build();

        listingRepo.saveUserPurchases(userId, finalJsonObject);

    }

    public void deleteListing(String id) {
        listingRepo.deleteListing(id);

        List<PreOrderListing> list = getAllListing();

        list.removeIf(listing -> listing.getId().equals(id));

        for (PreOrderListing listing : list) {

            Instant instant = listing.getDeadline().atStartOfDay(ZoneId.systemDefault()).toInstant();

            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
                    .add("title", listing.getTitle())
                    .add("content", listing.getContent())
                    .add("image", listing.getImage())
                    .add("category", listing.getCategory())
                    .add("sellerId", listing.getSellerId())
                    .add("stripeProductId", listing.getStripeProductId())
                    .add("price", listing.getPrice())
                    .add("deadline", instant.toEpochMilli())
                    .add("buyerConfirmSalesComplete", listing.getBuyerConfirmSalesComplete())
                    .add("sellerConfirmSalesComplete", listing.getSellerConfirmSalesComplete());

            listingRepo.saveToRedis(jsonObjectBuilder.build(), listing.getId());
        }
    }
}
