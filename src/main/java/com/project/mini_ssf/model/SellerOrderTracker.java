package com.project.mini_ssf.model;

import java.time.LocalDate;
import java.util.List;

public class SellerOrderTracker {
    private String id; 
    private String buyerId;
    private String buyerEmail;
    private String buyerName;
    private String listingId;
    private String orderId;
    private int quantity; 
    private LocalDate purchaseDate;
    private List<PreOrderListing> listing;
    private Boolean sellerConfirmSalesComplete;
    private Boolean buyerConfirmSalesComplete;


    public Boolean getSellerConfirmSalesComplete() {
        return sellerConfirmSalesComplete;
    }

    public void setSellerConfirmSalesComplete(Boolean sellerConfirmSalesComplete) {
        this.sellerConfirmSalesComplete = sellerConfirmSalesComplete;
    }

    public Boolean getBuyerConfirmSalesComplete() {
        return buyerConfirmSalesComplete;
    }

    public void setBuyerConfirmSalesComplete(Boolean buyerConfirmSalesComplete) {
        this.buyerConfirmSalesComplete = buyerConfirmSalesComplete;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public List<PreOrderListing> getListing() {
        return listing;
    }

    public void setListing(List<PreOrderListing> listing) {
        this.listing = listing;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    
}

