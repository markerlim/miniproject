package com.project.mini_ssf.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.project.mini_ssf.model.EntityDetails;
import com.project.mini_ssf.service.AcraService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class MainRestController {

    @Autowired
    private AcraService acraService;

    @Value("${stripe.api.key}")
    private String stripeApi;

    @PostMapping("/regAcra")
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
