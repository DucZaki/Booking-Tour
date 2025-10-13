package edu.bookingtour.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String adminHome() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String manageUsers() {
        return "admin/users";
    }

    @GetMapping("/admin/travels")
    public String manageTravels() {
        return "admin/travels";
    }

    @GetMapping("/admin/destinations")
    public String manageDestinations() {
        return "admin/destinations";
    }

}
