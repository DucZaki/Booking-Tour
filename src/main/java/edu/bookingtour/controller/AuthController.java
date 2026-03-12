package edu.bookingtour.controller;

import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.service.NguoiDungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private NguoiDungService nguoiDungService;

    /**
     * Hiển thị trang đăng nhập
     */
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            jakarta.servlet.http.HttpSession session,
            Model model) {
        if (error != null) {
            String sessionError = (String) session.getAttribute("error.message");
            if (sessionError != null) {
                model.addAttribute("errorMessage", sessionError);
                session.removeAttribute("error.message");
            } else {
                model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không đúng!");
            }
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Đăng xuất thành công!");
        }
        return "login/login";
    }

    /**
     * Hiển thị trang đăng ký
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("nguoiDung", new NguoiDung());
        return "login/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("nguoiDung") NguoiDung nguoiDung,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (nguoiDung.getTenDangNhap() == null || nguoiDung.getTenDangNhap().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Tên đăng nhập không được để trống!");
            return "login/register";
        }
        if (nguoiDung.getMatKhau() == null || nguoiDung.getMatKhau().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Mật khẩu không được để trống!");
            return "login/register";
        }
        if (!nguoiDung.getMatKhau().equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "login/register";
        }
        if (nguoiDung.getEmail() == null || nguoiDung.getEmail().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Email không được để trống!");
            return "login/register";
        }
        if (nguoiDung.getHoTen() == null || nguoiDung.getHoTen().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Họ tên không được để trống!");
            return "login/register";
        }
        try {
            nguoiDungService.registerNewUser(nguoiDung);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Chào mừng bạn tham gia ZakiBooking.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "login/register";
        }
    }

    /**
     * Chuyển hướng sau khi đăng nhập thành công
     */
    @GetMapping("/redirect-after-login")
    public String redirectAfterLogin(Authentication authentication) {

        // Kiểm tra vai trò và chuyển hướng tương ứng
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/admin";
        } else {
            return "redirect:/";
        }
    }

    /**
     * Trang lỗi khi không có quyền truy cập
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("errorMessage", "Bạn không có quyền truy cập trang này!");
        return "error/403";
    }
}