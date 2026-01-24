package edu.bookingtour.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class DiemDenController {

@GetMapping("/destination/{id}")
public String viewdetail(){

    return "0";
}
}
