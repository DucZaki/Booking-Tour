package edu.bookingtour.controller;

import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.service.CheckInService;
import edu.bookingtour.service.QrCodeService;
import edu.bookingtour.service.TourManifestService;
import edu.bookingtour.util.TourCodeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;

@Controller
public class CheckInController {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CheckInService checkInService;
    private final QrCodeService qrCodeService;
    private final TourManifestService tourManifestService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public CheckInController(CheckInService checkInService, QrCodeService qrCodeService,
            TourManifestService tourManifestService) {
        this.checkInService = checkInService;
        this.qrCodeService = qrCodeService;
        this.tourManifestService = tourManifestService;
    }

    /** Trang mở ra khi quét QR — xem thông tin vé & admin check-in. */
    @GetMapping("/check-in/{token}")
    public String checkInPage(@PathVariable String token, Model model) {
        return checkInService.findByToken(token)
                .map(booking -> populateModel(model, booking, token, null))
                .orElseGet(() -> {
                    model.addAttribute("valid", false);
                    model.addAttribute("message", "Mã QR không hợp lệ hoặc đã hết hạn.");
                    return "checkin/verify";
                });
    }

    @PostMapping("/check-in/{token}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'GUIDE')")
    public String confirmCheckIn(@PathVariable String token,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        NguoiDung actor = userDetails != null
                ? tourManifestService.findGuideUser(userDetails.getUsername()).orElse(null)
                : null;
        CheckInService.CheckInResult result = checkInService.confirmCheckIn(token, actor);
        redirectAttributes.addFlashAttribute("flashMessage", result.message());
        redirectAttributes.addFlashAttribute("flashSuccess", result.ok());
        return "redirect:/check-in/" + token;
    }

    /** Ảnh QR encode URL trang check-in. */
    @GetMapping(value = "/check-in/{token}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrImage(@PathVariable String token) {
        if (checkInService.findByToken(token).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String url = buildCheckInUrl(token);
        byte[] png = qrCodeService.generatePng(url, 320);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }

    private String populateModel(Model model, DatCho booking, String token, String flashMsg) {
        boolean paid = "PAID".equals(booking.getTrangThai());
        boolean checkedIn = booking.getCheckinStatusEnum() == edu.bookingtour.entity.CheckInStatus.CHECKED_IN
                || booking.getCheckinStatusEnum() == edu.bookingtour.entity.CheckInStatus.LATE
                || booking.getCheckedInAt() != null;

        model.addAttribute("valid", true);
        model.addAttribute("paid", paid);
        model.addAttribute("checkedIn", checkedIn);
        model.addAttribute("booking", booking);
        model.addAttribute("token", token);
        model.addAttribute("orderId", booking.getId());
        model.addAttribute("tourCode", booking.getIdChuyenDi() != null
                ? TourCodeUtil.format(booking.getIdChuyenDi().getId()) : "—");
        model.addAttribute("customerName", booking.getHoTen());
        model.addAttribute("guestCount", booking.getSoLuong());
        model.addAttribute("totalPrice", booking.getTongGia());
        model.addAttribute("departurePoint",
                booking.getIdDiemDon() != null ? booking.getIdDiemDon().getTen() : "—");
        model.addAttribute("tourTitle",
                booking.getIdChuyenDi() != null ? booking.getIdChuyenDi().getTieuDe() : "—");
        model.addAttribute("departureDate",
                booking.getIdNgayKhoiHanh() != null && booking.getIdNgayKhoiHanh().getNgay() != null
                        ? booking.getIdNgayKhoiHanh().getNgay().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "—");
        model.addAttribute("checkedInAt",
                checkedIn ? booking.getCheckedInAt().format(DT_FMT) : null);
        model.addAttribute("qrUrl", "/check-in/" + token + "/qr.png");
        return "checkin/verify";
    }

    public String buildCheckInUrl(String token) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + "/check-in/" + token;
    }
}
