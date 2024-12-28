package com.project.mini_ssf.service;
import org.springframework.stereotype.Service;

import com.project.mini_ssf.model.PreOrderListing;

import java.util.List;
import java.util.ArrayList;

@Service
public class CartService {

    private List<PreOrderListing> cartItems = new ArrayList<>();

    public boolean addItemToCart(PreOrderListing item) {
        if(item.equals(null)){
            return false;
        }
        cartItems.add(item);
        return true;
    }

    public void removeItemFromCart(String id) {
        for(int i = 0; i < cartItems.size(); i++){
            if(cartItems.get(i).getId().equals(id)){
                cartItems.remove(i);
            }
        }
    }

    public List<PreOrderListing> getCartItems() {
        return cartItems;
    }

    public Integer getCartQty(){
        return cartItems.size();
    }

    public void clearCart(){
        this.cartItems = new ArrayList<>();
    }
}

