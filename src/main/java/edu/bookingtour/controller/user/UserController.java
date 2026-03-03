package edu.bookingtour.controller.user;

import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.repo.DatChoRepository;
import edu.bookingtour.repo.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DatChoRepository datChoRepository;

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String name = authentication.getName();
        NguoiDung user = nguoiDungRepository
                .findByTenDangNhap(name)
                .or(() -> nguoiDungRepository.findByEmail(name))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        model.addAttribute("user", user);
        return "user/profile";
    }

    @GetMapping("/bookings")
    public String bookings(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String name = authentication.getName();
        NguoiDung user = nguoiDungRepository
                .findByTenDangNhap(name)
                .or(() -> nguoiDungRepository.findByEmail(name))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        List<DatCho> bookings = datChoRepository.findByIdNguoiDungOrderByIdDesc(user);
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("currentTime", LocalDateTime.now());
        return "user/bookings";
    }
}
