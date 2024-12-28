package com.project.mini_ssf.model;

import java.time.LocalDate;
import java.util.List;

public class Orders {
    
    private String orderId;

    private List<PreOrderListing> listing;

    private LocalDate orderDate;

    public Orders() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<PreOrderListing> getListing() {
        return listing;
    }

    public void setListing(List<PreOrderListing> listing) {
        this.listing = listing;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
    
}
