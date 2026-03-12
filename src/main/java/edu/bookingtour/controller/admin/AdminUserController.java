package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.service.NguoiDungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/user")
public class AdminUserController {
    @Autowired
    private NguoiDungService nguoiDungService;

    @Autowired
    private edu.bookingtour.repo.DatChoRepository datChoRepository;

    @GetMapping
    public String listUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int perPage, Model model) {
        Page<NguoiDung> nguoiDung = nguoiDungService.findAllUser(page, perPage);
        model.addAttribute("users", nguoiDung);
        model.addAttribute("page", page);
        model.addAttribute("perPage", perPage);
        model.addAttribute("totalPage", nguoiDung.getTotalPages());
        return "admin/user/user-list";
    }
    @GetMapping("/create")
    public String createUser(Model model) {
        model.addAttribute("users", new NguoiDung());
        return "admin/user/user-create";
    }

    @PostMapping("/save")
    public String saveUser(NguoiDung user) {
        nguoiDungService.save(user);
        return "redirect:/admin/user";
    }
    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Integer id, Model model) {
        NguoiDung nguoiDung = nguoiDungService.findById(id).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        model.addAttribute("user", nguoiDung);
        return "admin/user/user-update";
    }
    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Integer id, NguoiDung user) {
        nguoiDungService.update(id, user);
        return "redirect:/admin/user";
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
        
        // Paginated bookings
        org.springframework.data.domain.Page<edu.bookingtour.entity.DatCho> bookingPage = 
            datChoRepository.findByIdNguoiDung(user, org.springframework.data.domain.PageRequest.of(page, perPage));
        
        model.addAttribute("bookings", bookingPage.getContent());
        model.addAttribute("bookingPage", bookingPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        
        // Stats
        model.addAttribute("totalBookings", bookingPage.getTotalElements());
        model.addAttribute("totalSpending", datChoRepository.sumTongGiaByUser(user));
        
        // Last booking for summary card (not paginated)
        java.util.List<edu.bookingtour.entity.DatCho> recentList = datChoRepository.findRecentBookingsByUser(user);
        if (!recentList.isEmpty()) {
            model.addAttribute("lastBooking", recentList.get(0));
        }
        
        return "admin/user/user-detail";
    }

    // REST endpoint: Lấy chi tiêu theo tháng của một user cụ thể trong năm hiện tại
    @GetMapping("/api/{id}/spending")
    @ResponseBody
    public java.util.Map<String, Object> getUserSpendingChart(@PathVariable Integer id) {
        NguoiDung user = nguoiDungService.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));
        java.util.List<Object[]> spendingData = datChoRepository.findMonthlySpendingByUser(user);
        
        java.util.Map<Integer, Double> spendingMap = new java.util.HashMap<>();
        java.util.stream.IntStream.rangeClosed(1, 12).forEach(i -> spendingMap.put(i, 0.0));
        for (Object[] row : spendingData) {
            spendingMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }

        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<Double> data = new java.util.ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            labels.add("Tháng " + i);
            data.add(spendingMap.get(i));
        }

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("labels", labels);
        response.put("data", data);
        response.put("userId", id);
        return response;
    }
}
