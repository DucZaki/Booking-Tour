package edu.bookingtour.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out!");
        }
        return "login"; // template login.html
    }

    @GetMapping("/default")
    public String defaultAfterLogin(Authentication authentication) {
        // Kiểm tra role và redirect
        var authorities = authentication.getAuthorities();

        for (var authority : authorities) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                return "redirect:/admin/dashboard";
            } else if (authority.getAuthority().equals("ROLE_USER")) {
                return "redirect:/user/dashboard";
            }
        }

        return "redirect:/";
    }
}