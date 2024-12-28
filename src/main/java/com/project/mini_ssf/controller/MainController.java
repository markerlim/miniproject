package com.project.mini_ssf.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.mini_ssf.model.EntityDetails;
import com.project.mini_ssf.model.Orders;
import com.project.mini_ssf.model.PreOrderListing;
import com.project.mini_ssf.model.SellerOrderTracker;
import com.project.mini_ssf.service.AcraService;
import com.project.mini_ssf.service.CartService;
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

@Controller
@RequestMapping("")
public class MainController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private CartService cartService;

    @Autowired
    private AcraService acraService;

    @Value("${stripe.api.key}")
    private String stripeApi;

    @GetMapping(path = { "" })
    public ModelAndView HomePage(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        List<PreOrderListing> list = listingService.getAllListing();

        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            mav.addObject("item", list);
        } else {
        }
        String role = (String) session.getAttribute("role");
        if ("buyer".equals(role)) {
            mav.setViewName("redirect:/buyer/home");
            return mav;
        } else if ("seller".equals(role)) {
            mav.setViewName("redirect:/seller/home");
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

    @GetMapping("/success")
    public String paymentSuccess(HttpSession session) {
        listingService.saveBuyerPurchases(session.getAttribute("userId").toString());
        cartService.clearCart();
        return "redirect:/buyer/purchases";
    }

    @GetMapping("/error")
    public String errorMessage(){
        return "error";
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

    @PostMapping("/api/posting")
    public ModelAndView postListing(@Valid @ModelAttribute PreOrderListing preOrderListing, BindingResult result,
            HttpSession session) throws StripeException {

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
        jsonObject.add("buyerConfirmSalesComplete", false);
        jsonObject.add("sellerConfirmSalesComplete", false);

        JsonObject Jobject = jsonObject.build();

        listingService.postToListingServer(Jobject, id);
        listingService.saveSellerPosting(sellerId);

        List<PreOrderListing> list = listingService.getSellerPostings(sellerId);
        mav.addObject("item", list);

        mav.setViewName("home-logged-in-seller");
        return mav;
    }

    @GetMapping("/entities")
    public ModelAndView getAllEntities() {
        ModelAndView mav = new ModelAndView();
        List<EntityDetails> entities = listingService.getAllSellers();
        System.out.println(entities);
        mav.addObject("entities", entities);
        mav.setViewName("seller-page");
        return mav;
    }

    @GetMapping("/entities/{id}")
    public ModelAndView getEntityListing(@PathVariable String id) {
        ModelAndView mav = new ModelAndView();
        List<PreOrderListing> list = listingService.getSellerPostings(id);
        mav.addObject("item", list);
        mav.setViewName("oneSellerListings");
        return mav;
    }

    @GetMapping("/buyer/home")
    public ModelAndView LoggedInHomePage(@AuthenticationPrincipal OAuth2User oAuth2User,
            @ModelAttribute("message") String message,
            HttpSession session) {
        ModelAndView mav = new ModelAndView();
        String role = (String) session.getAttribute("role");
        if ("seller".equals(role)) {
            mav.setViewName("redirect:/");
            return mav;
        }

        List<PreOrderListing> list = listingService.getAllListing();
        mav.addObject("item", list);

        if (oAuth2User != null) {
            mav.addObject("userName", oAuth2User.getName());
        }
        if (message != null) {
            mav.addObject("message", message);
        }

        mav.setViewName("home-logged-in");
        return mav;
    }

    @GetMapping("/buyer/home/{category}")
    public ModelAndView FilterFromHome(@AuthenticationPrincipal OAuth2User oAuth2User,
            @ModelAttribute("message") String message,
            @PathVariable String category,
            HttpSession session) {
        ModelAndView mav = new ModelAndView();
        String role = (String) session.getAttribute("role");
        if ("seller".equals(role)) {
            mav.setViewName("redirect:/");
            return mav;
        }
        List<PreOrderListing> list = listingService.getAllListing();
        list = list.stream() 
                    .filter(listing -> category.equalsIgnoreCase(listing.getCategory()))
                    .collect(Collectors.toList());
        mav.addObject("item", list);
        
        if (oAuth2User != null) {
            mav.addObject("userName", oAuth2User.getName());
        }
        if (message != null) {
            mav.addObject("message", message);
        }

        mav.setViewName("home-logged-in");
        return mav;
    }

    @GetMapping("/buyer/purchases")
    public ModelAndView PurchasedPage(HttpSession session) {
        ModelAndView mav = new ModelAndView("success");
        String buyerId = session.getAttribute("userId").toString();
        List<Orders> list = listingService.getBuyerPurchases(buyerId);
        mav.addObject("item", list);
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

    @GetMapping("/clear-cart")
    public String clearCart() {
        cartService.clearCart();
        return "redirect:/buyer/cart";
    }

    @PostMapping("/cart-item-del")
    public String deleteCart(@RequestParam String id) {
        cartService.removeItemFromCart(id);
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
        cartItem.setBuyerConfirmSalesComplete(false);
        cartItem.setSellerConfirmSalesComplete(false);

        boolean success = cartService.addItemToCart(cartItem);

        if (success) {
            redirectAttributes.addFlashAttribute("message", "Item added to cart");
        } else {
            redirectAttributes.addFlashAttribute("message", "Failed to add item to cart");
        }

        return "redirect:/buyer/home";
    }

    @PostMapping("/confirm-sale")
    public String confirmSales(@RequestParam String listingId, @RequestParam String buyerId, @RequestParam String orderId){
        listingService.setSellerConfirmSalesComplete(buyerId, orderId, listingId);
        return "redirect:/seller/sales";
    }

    @GetMapping("/seller/home")
    public ModelAndView LoggedInHomePageSeller(@AuthenticationPrincipal OAuth2User oAuth2User, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        String sellerId = session.getAttribute("userId").toString();
        String role = (String) session.getAttribute("role");
        if ("buyer".equals(role)) {
            mav.setViewName("redirect:/");
            return mav;
        }
        if (!acraService.checkIfUserAddedUEN(sellerId)) {
            mav.addObject("uenEntry", true);
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

    @GetMapping("/seller/sales")
    public ModelAndView getSalesOrderPage(HttpSession session){
        ModelAndView mav = new ModelAndView("sellerOrderManagement");
        String sellerId = session.getAttribute("userId").toString();
        List<SellerOrderTracker> list = listingService.getSellerOrderTracker(sellerId);
        mav.addObject("item", list);
        return mav;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.invalidate();
        return "redirect:/";
    }
}
