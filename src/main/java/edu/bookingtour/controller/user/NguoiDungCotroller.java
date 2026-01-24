package edu.bookingtour.controller.user;

import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.repo.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
@Controller
@RequestMapping("/user")
public class NguoiDungCotroller {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login/login";
        }

        String username = authentication.getName();
        NguoiDung user = nguoiDungRepository
                .findByTenDangNhap(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        model.addAttribute("user", user);
        return "user/profile";
    }


}
