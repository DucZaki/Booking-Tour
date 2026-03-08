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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import edu.bookingtour.service.NguoiDungService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private NguoiDungService nguoiDungService;

    @Autowired
    private DatChoRepository datChoRepository;

    @Value("${avatar.path}")
    private String avatarPath;

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
        LocalDateTime currentTime = LocalDateTime.now();

        // Pre-filter bookings for tabs
        List<DatCho> pendingBookings = bookings.stream()
                .filter(b -> "PENDING".equals(b.getTrangThai())
                        && (b.getCreatedAt() == null || !b.getCreatedAt().plusMinutes(15).isBefore(currentTime)))
                .collect(java.util.stream.Collectors.toList());

        List<DatCho> paidBookings = bookings.stream()
                .filter(b -> "PAID".equals(b.getTrangThai()))
                .collect(java.util.stream.Collectors.toList());

        List<DatCho> failedBookings = bookings.stream()
                .filter(b -> "FAILED".equals(b.getTrangThai())
                        || ("PENDING".equals(b.getTrangThai())
                                && b.getCreatedAt() != null
                                && b.getCreatedAt().plusMinutes(15).isBefore(currentTime)))
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("paidBookings", paidBookings);
        model.addAttribute("failedBookings", failedBookings);
        model.addAttribute("currentTime", currentTime);
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

    @GetMapping("/change-password")
    public String changePassword(Model model, Authentication authentication) {
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
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/user/change-password";
        }

        NguoiDung user = resolveUser(authentication);
        try {
            nguoiDungService.changePassword(user.getId(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công!");
            return "redirect:/user/profile";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/change-password";
        }
    }

    @PostMapping("/update-avatar")
    public String updateAvatar(@RequestParam("avatar") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một tập tin ảnh!");
            return "redirect:/user/profile";
        }

        try {
            NguoiDung user = resolveUser(authentication);

            // Tạo thư mục nếu chưa tồn tại
            File directory = new File(avatarPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Lưu file
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            File dest = new File(directory.getAbsolutePath() + File.separator + fileName);
            file.transferTo(dest);

            // Cập nhật đường dẫn trong DB
            String dbPath = "/anh/user/" + fileName;
            nguoiDungService.updateAvatar(user.getId(), dbPath);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ảnh đại diện thành công!");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi lưu file: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/user/profile";
    }
}
