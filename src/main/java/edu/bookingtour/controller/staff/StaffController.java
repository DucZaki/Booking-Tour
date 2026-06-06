package edu.bookingtour.controller.staff;

import edu.bookingtour.entity.CheckInStatus;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.entity.TrangThaiDoan;
import edu.bookingtour.service.CheckInService;
import edu.bookingtour.service.TourManifestService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/staff")
@PreAuthorize("hasAnyRole('GUIDE', 'ADMIN')")
public class StaffController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TourManifestService tourManifestService;
    private final CheckInService checkInService;

    public StaffController(TourManifestService tourManifestService, CheckInService checkInService) {
        this.tourManifestService = tourManifestService;
        this.checkInService = checkInService;
    }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        NguoiDung guide = requireGuide(userDetails);
        LocalDate today = LocalDate.now();
        List<NgayKhoiHanh> departures = tourManifestService.listDeparturesForGuide(guide, today, today.plusDays(14));
        model.addAttribute("guide", guide);
        model.addAttribute("departures", departures);
        model.addAttribute("isAdminView", TourManifestService.isAdmin(guide));
        return "staff/dashboard";
    }

    @GetMapping("/departures/{nkhId}/manifest")
    public String manifest(@PathVariable Integer nkhId,
            @RequestParam(required = false) String q,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        NguoiDung guide = requireGuide(userDetails);
        NgayKhoiHanh nkh = tourManifestService.getDeparture(nkhId).orElse(null);
        if (nkh == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chuyến khởi hành.");
            return "redirect:/staff";
        }
        if (!tourManifestService.canAccessDeparture(guide, nkh)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không được phân công đoàn này.");
            return "redirect:/staff";
        }
        List<DatCho> bookings = tourManifestService.manifest(nkh, q);
        model.addAttribute("nkh", nkh);
        model.addAttribute("bookings", bookings);
        model.addAttribute("stats", tourManifestService.stats(bookings));
        model.addAttribute("keyword", q != null ? q : "");
        model.addAttribute("statuses", CheckInStatus.values());
        model.addAttribute("groupStatuses", TrangThaiDoan.values());
        return "staff/manifest";
    }

    @PostMapping("/departures/{nkhId}/status")
    public String updateGroupStatus(@PathVariable Integer nkhId,
            @RequestParam TrangThaiDoan status,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        NguoiDung guide = requireGuide(userDetails);
        try {
            tourManifestService.updateDepartureStatus(guide, nkhId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đoàn: " + status.getLabel());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/departures/" + nkhId + "/manifest";
    }

    @PostMapping("/bookings/{id}/checkin-status")
    public String updateBookingStatus(@PathVariable Integer id,
            @RequestParam CheckInStatus status,
            @RequestParam Integer nkhId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        NguoiDung guide = requireGuide(userDetails);
        CheckInService.CheckInResult result = checkInService.updateStatusByBookingId(id, status, guide);
        redirectAttributes.addFlashAttribute(result.ok() ? "successMessage" : "errorMessage", result.message());
        return "redirect:/staff/departures/" + nkhId + "/manifest";
    }

    @GetMapping("/scan")
    public String scanPage() {
        return "staff/scan";
    }

    @GetMapping("/check-in/manual")
    public String manualCheckIn(@RequestParam(required = false) String token, RedirectAttributes redirectAttributes) {
        if (token == null || token.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập mã check-in.");
            return "redirect:/staff/scan";
        }
        String cleaned = token.trim();
        var m = java.util.regex.Pattern.compile("/check-in/([a-f0-9]+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(cleaned);
        if (m.find()) {
            cleaned = m.group(1);
        }
        return "redirect:/staff/check-in/" + cleaned;
    }

    @GetMapping("/check-in/{token}")
    public String staffCheckInPage(@PathVariable String token,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        NguoiDung guide = requireGuide(userDetails);
        Optional<DatCho> bookingOpt = checkInService.findByToken(token);
        if (bookingOpt.isEmpty()) {
            model.addAttribute("valid", false);
            model.addAttribute("message", "Mã QR không hợp lệ.");
            return "staff/check-in";
        }
        DatCho booking = bookingOpt.get();
        NgayKhoiHanh nkh = booking.getIdNgayKhoiHanh();
        if (nkh != null && !tourManifestService.canAccessDeparture(guide, nkh)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vé thuộc đoàn bạn không phụ trách.");
            return "redirect:/staff";
        }
        populateCheckInModel(model, booking, token);
        return "staff/check-in";
    }

    @PostMapping("/check-in/{token}/confirm")
    public String staffConfirm(@PathVariable String token,
            @RequestParam(defaultValue = "CHECKED_IN") CheckInStatus status,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        NguoiDung guide = requireGuide(userDetails);
        Optional<DatCho> bookingOpt = checkInService.findByToken(token);
        if (bookingOpt.isPresent()) {
            NgayKhoiHanh nkh = bookingOpt.get().getIdNgayKhoiHanh();
            if (nkh != null && !tourManifestService.canAccessDeparture(guide, nkh)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vé thuộc đoàn bạn không phụ trách.");
                return "redirect:/staff";
            }
        }
        CheckInService.CheckInResult result = checkInService.updateStatusByToken(token, status, guide);
        redirectAttributes.addFlashAttribute(result.ok() ? "successMessage" : "errorMessage", result.message());
        return "redirect:/staff/check-in/" + token;
    }

    private NguoiDung requireGuide(UserDetails userDetails) {
        return tourManifestService.findGuideUser(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản"));
    }

    private void populateCheckInModel(Model model, DatCho booking, String token) {
        boolean paid = "PAID".equals(booking.getTrangThai());
        CheckInStatus st = booking.getCheckinStatusEnum();
        boolean checkedIn = st == CheckInStatus.CHECKED_IN || st == CheckInStatus.LATE;

        model.addAttribute("valid", true);
        model.addAttribute("paid", paid);
        model.addAttribute("checkedIn", checkedIn);
        model.addAttribute("checkinStatus", st);
        model.addAttribute("booking", booking);
        model.addAttribute("token", token);
        model.addAttribute("orderId", booking.getId());
        model.addAttribute("customerName", booking.getHoTen());
        model.addAttribute("phone", booking.getSoDienThoai());
        model.addAttribute("guestCount", booking.getSoLuong());
        model.addAttribute("departurePoint",
                booking.getIdDiemDon() != null ? booking.getIdDiemDon().getTen() : "—");
        model.addAttribute("tourTitle",
                booking.getIdChuyenDi() != null ? booking.getIdChuyenDi().getTieuDe() : "—");
        model.addAttribute("departureDate",
                booking.getIdNgayKhoiHanh() != null && booking.getIdNgayKhoiHanh().getNgay() != null
                        ? booking.getIdNgayKhoiHanh().getNgay().format(DATE_FMT)
                        : "—");
        model.addAttribute("hotelName",
                booking.getIdChuyenDi() != null && booking.getIdChuyenDi().getIdNoiLuuTru() != null
                        ? booking.getIdChuyenDi().getIdNoiLuuTru().getTen()
                        : "—");
        model.addAttribute("roomInfo", booking.getSoPhong() != null ? booking.getSoPhong()
                : (booking.getSoPhongDon() != null && booking.getSoPhongDon() > 0 ? "Phòng đơn x" + booking.getSoPhongDon() : "—"));
        model.addAttribute("seatInfo", booking.getSoGhe() != null ? booking.getSoGhe()
                : (booking.getIdNgayKhoiHanh() != null ? booking.getIdNgayKhoiHanh().getMaChuyenBayDi() : "—"));
    }
}
