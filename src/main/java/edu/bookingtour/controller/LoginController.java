//package edu.bookingtour.controller;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.ui.Model;
//@Controller
//public class LoginController {
//
//    @GetMapping("/login")
//    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
//                                @RequestParam(value = "logout", required = false) String logout,
//                                Model model) {
//        if (error != null) {
//            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
//        }
//        if (logout != null) {
//            model.addAttribute("message", "Bạn đã đăng xuất thành công.");
//        }
//        return "login/login"; // Trả về file login.html
//    }
//    @GetMapping("/register")
//    public String showRegisterPage() {
//        return "login/register"; // Đường dẫn tới templates/login/register.html
//    }
//}