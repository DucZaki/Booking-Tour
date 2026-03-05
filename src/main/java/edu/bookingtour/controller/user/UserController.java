package edu.bookingtour.controller.user;

import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.repo.DatChoRepository;
import edu.bookingtour.repo.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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

    // -----------------------------------------------
    // Tính hạng thành viên theo tổng chi tiêu (PAID)
    // -----------------------------------------------
    private String getMemberTier(double totalSpending) {
        if (totalSpending >= 100_000_000)
            return "Kim Cương";
        if (totalSpending >= 50_000_000)
            return "Bạch Kim";
        if (totalSpending >= 20_000_000)
            return "Vàng";
        if (totalSpending >= 10_000_000)
            return "Bạc";
        return "Đồng";
    }

    private String getMemberTierIcon(double totalSpending) {
        if (totalSpending >= 100_000_000)
            return "👑";
        if (totalSpending >= 50_000_000)
            return "💎";
        if (totalSpending >= 20_000_000)
            return "🥇";
        if (totalSpending >= 10_000_000)
            return "🥈";
        return "🥉";
    }

    private String getMemberTierColor(double totalSpending) {
        if (totalSpending >= 100_000_000)
            return "tier-diamond";
        if (totalSpending >= 50_000_000)
            return "tier-platinum";
        if (totalSpending >= 20_000_000)
            return "tier-gold";
        if (totalSpending >= 10_000_000)
            return "tier-silver";
        return "tier-bronze";
    }

    private double getTierProgress(double totalSpending) {
        if (totalSpending >= 100_000_000)
            return 100.0;
        if (totalSpending >= 50_000_000)
            return (totalSpending - 50_000_000) / 500_000; // to 100tr
        if (totalSpending >= 20_000_000)
            return (totalSpending - 20_000_000) / 300_000; // to 50tr
        if (totalSpending >= 10_000_000)
            return (totalSpending - 10_000_000) / 100_000; // to 20tr
        return totalSpending / 100_000; // to 10tr
    }

    private NguoiDung resolveUser(Authentication authentication) {
        String name = authentication.getName();
        return nguoiDungRepository
                .findByTenDangNhap(name)
                .or(() -> nguoiDungRepository.findByEmail(name))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    // -----------------------------------------------
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        NguoiDung user = resolveUser(authentication);
        Double total = datChoRepository.sumTongGiaByUser(user);
        double totalSpending = (total != null) ? total : 0.0;

        model.addAttribute("user", user);
        model.addAttribute("totalSpending", totalSpending);
        model.addAttribute("memberTier", getMemberTier(totalSpending));
        model.addAttribute("memberTierIcon", getMemberTierIcon(totalSpending));
        model.addAttribute("memberTierColor", getMemberTierColor(totalSpending));
        model.addAttribute("tierProgress", getTierProgress(totalSpending));
        return "user/profile";
    }

    @GetMapping("/bookings")
    public String bookings(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        NguoiDung user = resolveUser(authentication);
        List<DatCho> bookings = datChoRepository.findByIdNguoiDungOrderByIdDesc(user);
        Double total = datChoRepository.sumTongGiaByUser(user);
        double totalSpending = (total != null) ? total : 0.0;

        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("currentTime", LocalDateTime.now());
        model.addAttribute("totalSpending", totalSpending);
        model.addAttribute("memberTier", getMemberTier(totalSpending));
        model.addAttribute("memberTierIcon", getMemberTierIcon(totalSpending));
        model.addAttribute("memberTierColor", getMemberTierColor(totalSpending));
        model.addAttribute("tierProgress", getTierProgress(totalSpending));
        return "user/bookings";
    }

    @GetMapping("/edit-profile")
    public String editProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        NguoiDung user = resolveUser(authentication);
        Double total = datChoRepository.sumTongGiaByUser(user);
        double totalSpending = (total != null) ? total : 0.0;

        model.addAttribute("user", user);
        model.addAttribute("totalSpending", totalSpending);
        model.addAttribute("memberTier", getMemberTier(totalSpending));
        model.addAttribute("memberTierIcon", getMemberTierIcon(totalSpending));
        model.addAttribute("memberTierColor", getMemberTierColor(totalSpending));
        model.addAttribute("tierProgress", getTierProgress(totalSpending));
        return "user/edit-profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute NguoiDung updatedUser, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        NguoiDung currentUser = resolveUser(authentication);

        // Cập nhật thông tin được phép sửa
        currentUser.setHoTen(updatedUser.getHoTen());
        currentUser.setNumber(updatedUser.getNumber());

        nguoiDungRepository.save(currentUser);

        redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin cá nhân thành công!");
        return "redirect:/user/profile";
    }
}
