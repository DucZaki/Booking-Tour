package edu.bookingtour.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {

    @GetMapping("/ve-chung-toi")
    public String aboutPage() {
        return "pages/ve-chung-toi";
    }
}
