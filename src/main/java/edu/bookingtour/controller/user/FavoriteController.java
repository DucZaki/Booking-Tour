package edu.bookingtour.controller.user;

//import ch.qos.logback.core.model.Model;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.entity.YeuThich;
import edu.bookingtour.repo.FavoriteRepository;
import edu.bookingtour.service.FavoriteService;
import edu.bookingtour.service.NguoiDungService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private NguoiDungService nguoiDungService;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @PostMapping("/add")
    public String addFavorite(@RequestParam("tourId") Integer tourId,
                              Principal principal,
                              HttpServletRequest request) {

        // principal.getName() chính là username (tên đăng nhập) của người dùng hiện tại
        if (principal != null) {
            String username = principal.getName();
            favoriteService.saveFavorite(tourId, username);
        }


        // Quay lại trang chi tiết tour vừa xem
        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }
    @GetMapping("/my-favorites")
    public String showFavorites(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        // Tìm người dùng hiện tại
        NguoiDung user = nguoiDungService.findByTenDangNhap(principal.getName()).get();

        // Lấy danh sách yêu thích của người dùng đó
        // (Bạn cần viết hàm này trong YeuThichRepository)
        List<YeuThich> list = favoriteRepository.findByIdNguoiDung(user);

        model.addAttribute("listYeuThich", list);
        return "user/favorite";
    }
    @GetMapping("/delete/{id}")
    public String removeFavorite(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        favoriteService.deleteFavorite(id);
        redirectAttributes.addFlashAttribute("message", "Đã xóa yêu thích");
        return "redirect:/favorites/my-favorites";
    }
}