package edu.bookingtour.controller.admin;

import edu.bookingtour.dto.AdminUserListItem;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.repo.DatChoRepository;
import edu.bookingtour.service.NguoiDungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/user")
public class AdminUserController {
    @Autowired
    private NguoiDungService nguoiDungService;

    @Autowired
    private DatChoRepository datChoRepository;

    @GetMapping
    public String listUsers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int perPage,
                            @RequestParam(required = false) String q,
                            @RequestParam(defaultValue = "spending_desc") String sort,
                            Model model) {
        Page<Object[]> nguoiDung = nguoiDungService.findAllUserDetail(page, perPage, q, sort);
        model.addAttribute("users", nguoiDung.getContent().stream().map(AdminUserListItem::fromRow).toList());
        model.addAttribute("page", page);
        model.addAttribute("perPage", perPage);
        model.addAttribute("totalPage", nguoiDung.getTotalPages());
        model.addAttribute("keyword", q != null ? q : "");
        model.addAttribute("sort", sort);
        return "admin/user/user-list";
    }
    @GetMapping("/create")
    public String createUser(Model model) {
        model.addAttribute("users", new NguoiDung());
        return "admin/user/user-create";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("users") NguoiDung user, Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            nguoiDungService.createUserByAdmin(user);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm người dùng thành công.");
            return "redirect:/admin/user";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("users", user);
            return "admin/user/user-create";
        }
    }
    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Integer id, Model model) {
        NguoiDung nguoiDung = nguoiDungService.findById(id).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        model.addAttribute("user", nguoiDung);
        return "admin/user/user-update";
    }
    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Integer id, @ModelAttribute("user") NguoiDung user, Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            nguoiDungService.update(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật người dùng thành công.");
            return "redirect:/admin/user/" + id;
        } catch (IllegalArgumentException ex) {
            user.setId(id);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("user", user);
            return "admin/user/user-update";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        nguoiDungService.deleteById(id);
        return "redirect:/admin/user";
    }

    @GetMapping("/{id}")
    public String showDetailUser(@PathVariable Integer id, 
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int perPage,
                                 Model model) {
        NguoiDung user = nguoiDungService.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));
        model.addAttribute("user", user);
        
        Page<edu.bookingtour.entity.DatCho> bookingPage = datChoRepository.findByIdNguoiDung(user, PageRequest.of(page, perPage));
        
        model.addAttribute("bookings", bookingPage.getContent());
        model.addAttribute("bookingPage", bookingPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        
        model.addAttribute("totalBookings", bookingPage.getTotalElements());
        model.addAttribute("totalSpending", datChoRepository.sumTongGiaByUser(user));
        
        List<DatCho> recentList = datChoRepository.findRecentBookingsByUser(user);
        if (!recentList.isEmpty()) {
            model.addAttribute("lastBooking", recentList.get(0));
        }
        
        return "admin/user/user-detail";
    }

    @GetMapping("/api/{id}/spending")
    @ResponseBody
    public Map<String, Object> getUserSpendingChart(@PathVariable Integer id) {
        NguoiDung user = nguoiDungService.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));
        List<Object[]> spendingData = datChoRepository.findMonthlySpendingByUser(user);
        
        Map<Integer, Double> spendingMap = new HashMap<>();
        java.util.stream.IntStream.rangeClosed(1, 12).forEach(i -> spendingMap.put(i, 0.0));
        for (Object[] row : spendingData) {
            spendingMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }

        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            labels.add("Tháng " + i);
            data.add(spendingMap.get(i));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("data", data);
        response.put("userId", id);
        return response;
    }
}
