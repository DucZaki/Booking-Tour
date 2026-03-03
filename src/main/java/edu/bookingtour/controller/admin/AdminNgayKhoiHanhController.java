package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.service.NgayKhoiHanhService;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/tour/{tourId}/ngay-khoi-hanh")
public class AdminNgayKhoiHanhController {

    @Autowired
    private NgayKhoiHanhService ngayKhoiHanhService;

    @Autowired
    private TourService tourService;

    /**
     * Danh sách ngày khởi hành của tour
     */
    @GetMapping
    public String list(@PathVariable Integer tourId, Model model) {
        ChuyenDi tour = tourService.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại"));
        List<NgayKhoiHanh> danhSach = ngayKhoiHanhService.findByChuyenDiId(tourId);

        model.addAttribute("tour", tour);
        model.addAttribute("danhSach", danhSach);
        return "admin/tour/ngay-khoi-hanh-list";
    }

    /**
     * Thêm ngày khởi hành mới (hỗ trợ nhiều ngày)
     */
    @PostMapping("/add")
    public String add(@PathVariable Integer tourId,
            @RequestParam("ngayDi") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> danhSachNgayDi,
            @RequestParam("ngayVe") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> danhSachNgayVe) {
        for (int i = 0; i < danhSachNgayDi.size(); i++) {
            LocalDate ngayDi = danhSachNgayDi.get(i);
            LocalDate ngayVe = (i < danhSachNgayVe.size()) ? danhSachNgayVe.get(i) : null;
            if (ngayDi != null) {
                ngayKhoiHanhService.addDepartureDate(tourId, ngayDi, ngayVe);
            }
        }
        return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh";
    }

    /**
     * Form chỉnh sửa ngày khởi hành
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer tourId, @PathVariable Integer id, Model model) {
        ChuyenDi tour = tourService.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại"));
        NgayKhoiHanh nkh = ngayKhoiHanhService.findById(id);
        if (nkh == null) {
            return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh";
        }

        model.addAttribute("tour", tour);
        model.addAttribute("nkh", nkh);
        return "admin/tour/ngay-khoi-hanh-edit";
    }

    /**
     * Cập nhật ngày đi/ngày về
     */
    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer tourId,
            @PathVariable Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayDi,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayVe) {
        ngayKhoiHanhService.updateDepartureDate(id, ngayDi, ngayVe);
        return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh";
    }

    /**
     * Xoá ngày khởi hành
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer tourId, @PathVariable Integer id) {
        ngayKhoiHanhService.deleteDepartureDate(id);
        return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh";
    }
}
