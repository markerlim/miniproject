package com.project.mini_ssf.model;

import java.time.LocalDate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;

public class PreOrderListing {

    private String id;

    private String stripeProductId;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 40, message = "Title should be between 5 and 40 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 50, max = 600, message = "Content should be between 50 and 600 characters")
    private String content;

    @NotBlank(message = "Image URL is required")
    @Pattern(regexp = "^(https?|ftp)://[^\s/$.?#].[^\s]*$", message = "Invalid URL format")
    private String image;
    
    @NotBlank(message = "Category is required")
    private String category;

    private String sellerId;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price should be greater than or equal to 0")
    @Max(value = 999999,message = "Price should be lesser than or equal to 999999" )
    private Long price;

    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    private LocalDate deadline;

    private Integer qty;

    private Boolean sellerConfirmSalesComplete;

    private Boolean buyerConfirmSalesComplete;

    public PreOrderListing() {}

    public PreOrderListing(String id, String title, String content, String image, String category, String sellerId, String stripeProductId,
    Long price, LocalDate deadline) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.image = image;
        this.category = category;
        this.sellerId = sellerId;
        this.price = price;
        this.stripeProductId = stripeProductId;
        this.deadline = deadline;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public String getStripeProductId() {
        return stripeProductId;
    }

    public void setStripeProductId(String stripeProductId) {
        this.stripeProductId = stripeProductId;
    }

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


}
