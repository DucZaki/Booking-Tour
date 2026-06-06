package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.entity.NgayKhoiHanhDiemDon;
import edu.bookingtour.service.NgayKhoiHanhDiemDonService;
import edu.bookingtour.service.NgayKhoiHanhService;
import edu.bookingtour.service.TourCapacityService;
import edu.bookingtour.service.TourManifestService;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/tour/{tourId}/ngay-khoi-hanh")
public class AdminNgayKhoiHanhController {

    @Autowired
    private NgayKhoiHanhService ngayKhoiHanhService;

    @Autowired
    private NgayKhoiHanhDiemDonService ngayKhoiHanhDiemDonService;

    @Autowired
    private TourService tourService;

    @Autowired
    private TourCapacityService tourCapacityService;

    @Autowired
    private TourManifestService tourManifestService;

    /**
     * Danh sách ngày khởi hành của tour
     */
    @GetMapping
    public String list(@PathVariable Integer tourId, Model model) {
        ChuyenDi tour = tourService.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại"));
        List<NgayKhoiHanh> danhSach = ngayKhoiHanhService.findByChuyenDiId(tourId);

        Map<Integer, List<NgayKhoiHanhDiemDon>> diemDonByNkh = new HashMap<>();
        for (NgayKhoiHanh nkh : danhSach) {
            List<NgayKhoiHanhDiemDon> rows = ngayKhoiHanhDiemDonService.findByNgayKhoiHanhId(nkh.getId());
            if (rows.isEmpty()) {
                ngayKhoiHanhDiemDonService.syncForNgayKhoiHanh(nkh, false);
                rows = ngayKhoiHanhDiemDonService.findByNgayKhoiHanhId(nkh.getId());
            }
            diemDonByNkh.put(nkh.getId(), rows);
        }

        model.addAttribute("tour", tour);
        model.addAttribute("danhSach", danhSach);
        model.addAttribute("diemDonByNkh", diemDonByNkh);
        model.addAttribute("capacityByNkh", tourCapacityService.snapshotsForDepartures(danhSach));
        model.addAttribute("guides", tourManifestService.listGuides());
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
        model.addAttribute("capacity", tourCapacityService.getSnapshot(nkh.getId()));
        return "admin/tour/ngay-khoi-hanh-edit";
    }

    /**
     * Cập nhật ngày đi/ngày về
     */
    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer tourId,
            @PathVariable Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayDi,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayVe,
            @RequestParam(required = false) Integer sucChua,
            RedirectAttributes redirectAttributes) {
        try {
            ngayKhoiHanhService.updateDepartureDate(id, ngayDi, ngayVe);
            if (sucChua != null) {
                ngayKhoiHanhService.updateCapacity(id, sucChua);
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh/edit/" + id;
        }
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật ngày khởi hành.");
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

    @PostMapping("/diem-don/{rowId}/toggle")
    public String toggleDiemDon(@PathVariable Integer tourId,
            @PathVariable Integer rowId,
            @RequestParam(defaultValue = "true") boolean active) {
        ngayKhoiHanhDiemDonService.toggleActive(rowId, active);
        return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh";
    }

    @PostMapping("/diem-don/{rowId}/refresh")
    public String refreshDiemDon(@PathVariable Integer tourId,
            @PathVariable Integer rowId,
            RedirectAttributes redirectAttributes) {
        try {
            ngayKhoiHanhDiemDonService.refreshFlight(rowId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã làm mới giá vé từ Amadeus.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi làm mới vé: " + e.getMessage());
        }
        return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh";
    }

    @PostMapping("/diem-don/{rowId}/schedule")
    public String updateDiemDonSchedule(@PathVariable Integer tourId,
            @PathVariable Integer rowId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime activeFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime activeTo) {
        ngayKhoiHanhDiemDonService.updateSchedule(rowId, activeFrom, activeTo);
        return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh";
    }

    @PostMapping("/{nkhId}/assign-guide")
    public String assignGuide(@PathVariable Integer tourId,
            @PathVariable Integer nkhId,
            @RequestParam(required = false) Integer guideId,
            RedirectAttributes redirectAttributes) {
        try {
            tourManifestService.assignGuide(nkhId, guideId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật HDV phụ trách.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh";
    }

    @PostMapping("/sync-all")
    public String syncAll(@PathVariable Integer tourId, RedirectAttributes redirectAttributes) {
        List<NgayKhoiHanh> danhSach = ngayKhoiHanhService.findByChuyenDiId(tourId);
        for (NgayKhoiHanh nkh : danhSach) {
            ngayKhoiHanhDiemDonService.syncForNgayKhoiHanh(nkh, true);
        }
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã đồng bộ " + danhSach.size() + " ngày khởi hành theo điểm đón.");
        return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh";
    }
}
