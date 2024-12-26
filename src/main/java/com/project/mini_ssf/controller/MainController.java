package com.project.mini_ssf.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.mini_ssf.model.PreOrderListing;
import com.project.mini_ssf.service.AcraService;
import com.project.mini_ssf.service.CartService;
import com.project.mini_ssf.service.ListingService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("")
public class MainController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private CartService cartService;

    @Autowired
    private AcraService acraService;

    @GetMapping(path = { "" })
    public ModelAndView HomePage(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        List<PreOrderListing> list = listingService.getAllListing();

        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            mav.addObject("item", list);
            mav.addObject("loginMessage", "Successfully logged in!");
        } else {
            mav.addObject("loginMessage", "You are not logged in.");
        }
        String role = (String) session.getAttribute("role");
        if ("buyer".equals(role)) {
            mav.setViewName("home-logged-in");
            return mav;
        } else if ("seller".equals(role)) {
            mav.setViewName("home-logged-in-seller");
            return mav;
        }
        mav.setViewName("home");
        return mav;
    }

    @GetMapping("/login")
    public ModelAndView LoginPage() {
        ModelAndView mav = new ModelAndView("login");
        mav.addObject("loginMessage", "Please log in.");
        return mav;
    }

    @PostMapping("/set-role")
    public String setRole(HttpSession session, @RequestParam("role") String role) {
        System.out.println(role);
        List<PreOrderListing> cart = new ArrayList<>();
        if (role != null && (role.equals("buyer") || role.equals("seller"))) {
            session.setAttribute("role", role);
            session.setAttribute("cart", cart.toString());
            return "redirect:/oauth2/authorization/google";
        }
        return "redirect:/login?error=invalid_role";
    }

    @GetMapping("/buyer/home")
    public ModelAndView LoggedInHomePage(@AuthenticationPrincipal OAuth2User oAuth2User,
            @ModelAttribute("message") String message) {
        ModelAndView mav = new ModelAndView("home-logged-in");
        List<PreOrderListing> list = listingService.getAllListing();
        mav.addObject("item", list);

        if (oAuth2User != null) {
            mav.addObject("userName", oAuth2User.getName());
        }
        if (message != null) {
            mav.addObject("message", message);
        }
        return mav;
    }

    @GetMapping("/buyer/cart")
    public ModelAndView CartPage(@AuthenticationPrincipal OAuth2User oAuth2User,
            @ModelAttribute("message") String message) {
        ModelAndView mav = new ModelAndView("cart-page");
        List<PreOrderListing> list = cartService.getCartItems();
        mav.addObject("item", list);

        if (oAuth2User != null) {
            mav.addObject("userName", oAuth2User.getName());
        }
        if (message != null) {
            mav.addObject("message", message);
        }
        return mav;
    }

    @GetMapping("/buyer/clear-cart")
    public String clearCart (){
        cartService.clearCart();
        return "redirect:/buyer/cart";
    }

    @PostMapping("/add-to-cart")
    public String addToCart(@RequestParam String id, @RequestParam String title,
            @RequestParam String content, @RequestParam String category,
            @RequestParam String sellerId, @RequestParam LocalDate date,
            @RequestParam String image, @RequestParam Long price,
            @RequestParam String stripeProductId,
            @RequestParam Integer qty, RedirectAttributes redirectAttributes,
            HttpSession session) {

        PreOrderListing cartItem = new PreOrderListing();
        cartItem.setId(id);
        cartItem.setTitle(title);
        cartItem.setContent(content);
        cartItem.setImage(image);
        cartItem.setCategory(category);
        cartItem.setSellerId(sellerId);
        cartItem.setPrice(price);
        cartItem.setStripeProductId(stripeProductId);
        cartItem.setDeadline(date);
        cartItem.setQty(qty);

        boolean success = cartService.addItemToCart(cartItem);

        if (success) {
            redirectAttributes.addFlashAttribute("message", "Item added to cart");
        } else {
            redirectAttributes.addFlashAttribute("message", "Failed to add item to cart");
        }

        return "redirect:/buyer/home";
    }

    @GetMapping("/seller/home")
    public ModelAndView LoggedInHomePageSeller(@AuthenticationPrincipal OAuth2User oAuth2User, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        String sellerId = session.getAttribute("userId").toString();
        if(!acraService.checkIfUserAddedUEN(sellerId)){
            mav.addObject("uenEntry",true);
        }
        mav.setViewName("home-logged-in-seller");
        List<PreOrderListing> list = listingService.getSellerPostings(sellerId);
        mav.addObject("item", list);

        if (oAuth2User != null) {
            mav.addObject("userName", oAuth2User.getName());
        }

        return mav;
    }

    @GetMapping("/seller/newposting")
    public ModelAndView newPostingAsSeller() {
        ModelAndView mav = new ModelAndView("form");
        PreOrderListing preOrderListing = new PreOrderListing();
        mav.addObject("preOrderListing", preOrderListing);
        return mav;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.invalidate();
        return "redirect:/";
    }
}
