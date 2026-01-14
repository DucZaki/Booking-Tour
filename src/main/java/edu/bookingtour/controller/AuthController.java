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
            Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không đúng!");
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

    /**
     * Xử lý đăng ký người dùng mới
     */
    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("nguoiDung") NguoiDung nguoiDung,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Kiểm tra các trường bắt buộc
        if (nguoiDung.getTenDangNhap() == null || nguoiDung.getTenDangNhap().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Tên đăng nhập không được để trống!");
            return "login/register";
        }

        if (nguoiDung.getMatKhau() == null || nguoiDung.getMatKhau().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Mật khẩu không được để trống!");
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
            // Đăng ký người dùng mới
            nguoiDungService.registerNewUser(nguoiDung);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login/login";

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
            return "redirect:/admin/user";
        } else {
            return "redirect:/user/profile";
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

    /**
     * Trang profile người dùng
     */
    @GetMapping("/user/profile")
    public String userProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        NguoiDung nguoiDung = nguoiDungService.findByTenDangNhap(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        model.addAttribute("nguoiDung", nguoiDung);
        return "user/profile";
    }

    /**
     * Cập nhật thông tin profile
     */
    @PostMapping("/user/profile/update")
    public String updateProfile(
            @ModelAttribute NguoiDung nguoiDung,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            NguoiDung currentUser = nguoiDungService.findByTenDangNhap(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            // Cập nhật thông tin (không cho phép thay đổi username và vai trò)
            currentUser.setHoTen(nguoiDung.getHoTen());
            currentUser.setEmail(nguoiDung.getEmail());
            currentUser.setNumber(nguoiDung.getNumber());

            nguoiDungService.save(currentUser);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật thông tin thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/user/profile";
    }

    /**
     * Hiển thị form đổi mật khẩu
     */
    @GetMapping("/user/change-password")
    public String showChangePasswordPage() {
        return "user/change-password";
    }

    /**
     * Xử lý đổi mật khẩu
     */
    @PostMapping("/user/change-password")
    public String changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return "redirect:/user/change-password";
        }

        try {
            String username = authentication.getName();
            NguoiDung user = nguoiDungService.findByTenDangNhap(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            nguoiDungService.changePassword(user.getId(), oldPassword, newPassword);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đổi mật khẩu thành công!");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/user/change-password";
    }
}