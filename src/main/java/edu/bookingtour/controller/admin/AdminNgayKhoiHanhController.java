package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.CheckInStatus;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.LichTrinh;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.entity.NgayKhoiHanhDiemDon;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.entity.TrangThaiDoan;
import edu.bookingtour.repo.LichTrinhRepository;
import edu.bookingtour.repo.NgayKhoiHanhRepository;
import edu.bookingtour.repo.NguoiDungRepository;
import edu.bookingtour.service.CheckInService;
import edu.bookingtour.service.LichTrinhService;
import edu.bookingtour.service.NgayKhoiHanhDiemDonService;
import edu.bookingtour.service.NgayKhoiHanhService;
import edu.bookingtour.service.TourCapacityService;
import edu.bookingtour.service.TourManifestService;
import edu.bookingtour.service.TourService;
import edu.bookingtour.util.DepartureStatusUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
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

    @Autowired
    private NgayKhoiHanhRepository ngayKhoiHanhRepository;

    @Autowired
    private LichTrinhService lichTrinhService;

    @Autowired
    private LichTrinhRepository lichTrinhRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private CheckInService checkInService;

    /**
     * Danh sách ngày khởi hành của tour (lọc theo tháng / trạng thái)
     */
    @GetMapping
    public String list(@PathVariable Integer tourId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false, defaultValue = "in_progress") String phase,
            Model model) {
        ChuyenDi tour = tourService.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại"));

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        int viewMonth = month != null ? month : today.getMonthValue();
        int viewYear = year != null ? year : today.getYear();
        YearMonth ym = YearMonth.of(viewYear, viewMonth);

        List<NgayKhoiHanh> allInMonth = ngayKhoiHanhRepository.findByTourAndDateRange(
                tourId, ym.atDay(1), ym.atEndOfMonth());

        List<NgayKhoiHanh> danhSach;
        List<NgayKhoiHanh> inProgressList = List.of();
        List<NgayKhoiHanh> upcomingList = List.of();
        List<NgayKhoiHanh> completedList = List.of();
        List<NgayKhoiHanh> cancelledList = List.of();
        boolean showGrouped = "all".equals(phase);

        if (showGrouped) {
            inProgressList = filterByEffectiveStatus(allInMonth, TrangThaiDoan.IN_PROGRESS, now);
            upcomingList = filterByEffectiveStatus(allInMonth, TrangThaiDoan.SCHEDULED, now);
            completedList = filterByEffectiveStatus(allInMonth, TrangThaiDoan.COMPLETED, now);
            cancelledList = filterByEffectiveStatus(allInMonth, TrangThaiDoan.CANCELLED, now);
            danhSach = new ArrayList<>();
            danhSach.addAll(inProgressList);
            danhSach.addAll(upcomingList);
            danhSach.addAll(completedList);
            danhSach.addAll(cancelledList);
        } else {
            danhSach = filterByPhase(allInMonth, phase, now);
        }

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
        model.addAttribute("showGrouped", showGrouped);
        model.addAttribute("inProgressList", inProgressList);
        model.addAttribute("upcomingList", upcomingList);
        model.addAttribute("completedList", completedList);
        model.addAttribute("cancelledList", cancelledList);
        model.addAttribute("diemDonByNkh", diemDonByNkh);
        model.addAttribute("capacityByNkh", tourCapacityService.snapshotsForDepartures(danhSach));
        model.addAttribute("guides", tourManifestService.listGuides());
        model.addAttribute("viewMonth", viewMonth);
        model.addAttribute("viewYear", viewYear);
        model.addAttribute("phase", phase);
        model.addAttribute("today", today);
        return "admin/tour/ngay-khoi-hanh-list";
    }

    private static List<NgayKhoiHanh> filterByPhase(List<NgayKhoiHanh> departures, String phase, LocalDateTime now) {
        TrangThaiDoan target = switch (phase) {
            case "upcoming" -> TrangThaiDoan.SCHEDULED;
            case "completed" -> TrangThaiDoan.COMPLETED;
            case "all" -> null;
            default -> TrangThaiDoan.IN_PROGRESS;
        };
        if (target == null) {
            return departures;
        }
        return filterByEffectiveStatus(departures, target, now);
    }

    private static List<NgayKhoiHanh> filterByEffectiveStatus(
            List<NgayKhoiHanh> departures, TrangThaiDoan target, LocalDateTime now) {
        return departures.stream()
                .filter(n -> DepartureStatusUtil.effectiveStatus(n, now) == target)
                .toList();
    }

    /**
     * Trung tâm quản lý một ngày khởi hành — khách, tour, HDV, vận hành.
     */
    @GetMapping("/{nkhId}/quan-ly")
    public String manageDeparture(@PathVariable Integer tourId,
            @PathVariable Integer nkhId,
            @RequestParam(required = false, defaultValue = "overview") String tab,
            @RequestParam(required = false) String q,
            Model model) {
        ChuyenDi tour = tourService.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại"));
        NgayKhoiHanh nkh = tourManifestService.getDeparture(nkhId)
                .orElse(null);
        if (nkh == null || nkh.getChuyenDi() == null || !tourId.equals(nkh.getChuyenDi().getId())) {
            return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh";
        }

        List<NgayKhoiHanh> siblings = ngayKhoiHanhRepository.findByChuyenDiId(tourId);
        Integer prevId = null;
        Integer nextId = null;
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getId().equals(nkhId)) {
                if (i > 0) {
                    prevId = siblings.get(i - 1).getId();
                }
                if (i < siblings.size() - 1) {
                    nextId = siblings.get(i + 1).getId();
                }
                break;
            }
        }

        List<DatCho> bookings = tourManifestService.manifest(nkh, q);
        List<LichTrinh> itinerary = lichTrinhService.getByTour(tourId);
        List<NgayKhoiHanhDiemDon> diemDonRows = ngayKhoiHanhDiemDonService.findByNgayKhoiHanhId(nkhId);
        if (diemDonRows.isEmpty()) {
            ngayKhoiHanhDiemDonService.syncForNgayKhoiHanh(nkh, false);
            diemDonRows = ngayKhoiHanhDiemDonService.findByNgayKhoiHanhId(nkhId);
        }

        model.addAttribute("tour", tour);
        model.addAttribute("nkh", nkh);
        model.addAttribute("tab", tab);
        model.addAttribute("bookings", bookings);
        model.addAttribute("stats", tourManifestService.stats(bookings));
        model.addAttribute("itinerary", itinerary);
        model.addAttribute("diemDonRows", diemDonRows);
        model.addAttribute("capacity", tourCapacityService.getSnapshot(nkhId));
        model.addAttribute("guides", tourManifestService.listGuides());
        model.addAttribute("groupStatuses", TrangThaiDoan.values());
        model.addAttribute("checkinStatuses", CheckInStatus.values());
        model.addAttribute("keyword", q != null ? q : "");
        model.addAttribute("prevNkhId", prevId);
        model.addAttribute("nextNkhId", nextId);
        return "admin/tour/ngay-khoi-hanh-hub";
    }

    @GetMapping("/{nkhId}/manifest.csv")
    public void exportManifest(@PathVariable Integer tourId,
            @PathVariable Integer nkhId,
            HttpServletResponse response) throws IOException {
        NgayKhoiHanh nkh = tourManifestService.getDeparture(nkhId).orElse(null);
        if (nkh == null || nkh.getChuyenDi() == null || !tourId.equals(nkh.getChuyenDi().getId())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        List<DatCho> bookings = tourManifestService.manifest(nkh, null);
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=manifest-" + nkhId + ".csv");
        response.getWriter().println("\uFEFFMa don,Ho ten,So dien thoai,So khach,Diem don,Phong,Ghe,Thanh toan,Check-in,Ghi chu");
        for (DatCho b : bookings) {
            response.getWriter().println(String.join(",",
                    csv("#" + b.getId()),
                    csv(b.getHoTen()),
                    csv(b.getSoDienThoai()),
                    csv(String.valueOf(b.getSoLuong())),
                    csv(b.getIdDiemDon() != null ? b.getIdDiemDon().getTen() : ""),
                    csv(b.getSoPhong()),
                    csv(b.getSoGhe()),
                    csv(b.getTrangThai()),
                    csv(b.getCheckinStatusEnum().getLabel()),
                    csv(b.getGhiChu())));
        }
    }

    @PostMapping("/{nkhId}/status")
    public String updateGroupStatus(@PathVariable Integer tourId,
            @PathVariable Integer nkhId,
            @RequestParam TrangThaiDoan status,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        NguoiDung admin = resolveAdmin(userDetails);
        try {
            tourManifestService.adminSetDepartureStatus(admin, nkhId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đoàn: " + status.getLabel());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh/" + nkhId + "/quan-ly?tab=overview";
    }

    @PostMapping("/{nkhId}/bookings/{bookingId}/checkin")
    public String updateBookingCheckin(@PathVariable Integer tourId,
            @PathVariable Integer nkhId,
            @PathVariable Integer bookingId,
            @RequestParam CheckInStatus status,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        NguoiDung admin = resolveAdmin(userDetails);
        CheckInService.CheckInResult result = checkInService.updateStatusByBookingId(bookingId, status, admin);
        redirectAttributes.addFlashAttribute(result.ok() ? "successMessage" : "errorMessage", result.message());
        return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh/" + nkhId + "/quan-ly?tab=khach";
    }

    @PostMapping("/{nkhId}/bookings/{bookingId}/note")
    public String updateBookingNote(@PathVariable Integer tourId,
            @PathVariable Integer nkhId,
            @PathVariable Integer bookingId,
            @RequestParam(required = false) String note,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        NguoiDung admin = resolveAdmin(userDetails);
        try {
            tourManifestService.updateBookingNote(admin, bookingId, note);
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu ghi chú.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh/" + nkhId + "/quan-ly?tab=khach";
    }

    private NguoiDung resolveAdmin(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalStateException("Chưa đăng nhập");
        }
        return nguoiDungRepository.findByTenDangNhap(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy admin"));
    }

    private static String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
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
            @RequestParam(required = false) String gioTapTrung,
            RedirectAttributes redirectAttributes) {
        try {
            ngayKhoiHanhService.updateDepartureDate(id, ngayDi, ngayVe);
            if (sucChua != null) {
                ngayKhoiHanhService.updateCapacity(id, sucChua);
            }
            if (gioTapTrung != null && !gioTapTrung.isBlank()) {
                ngayKhoiHanhService.updateGatheringTime(id, gioTapTrung);
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh/edit/" + id;
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh/edit/" + id;
        }
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật ngày khởi hành.");
        return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh";
    }

    @PostMapping("/{nkhId}/gathering-time")
    public String updateGatheringTime(@PathVariable Integer tourId,
            @PathVariable Integer nkhId,
            @RequestParam String gioTapTrung,
            RedirectAttributes redirectAttributes) {
        try {
            ngayKhoiHanhService.updateGatheringTime(nkhId, gioTapTrung);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật thời gian xuất phát.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
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
        return "redirect:/admin/tour/" + tourId + "/ngay-khoi-hanh/" + nkhId + "/quan-ly?tab=staff";
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
