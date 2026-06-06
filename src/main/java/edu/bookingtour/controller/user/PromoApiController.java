package edu.bookingtour.controller.user;

import edu.bookingtour.dto.FlightQuoteResponse;
import edu.bookingtour.dto.PromoApplyResult;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.service.MaGiamGiaService;
import edu.bookingtour.service.BookingPricingService;
import edu.bookingtour.service.NgayKhoiHanhDiemDonService;
import edu.bookingtour.service.NgayKhoiHanhService;
import edu.bookingtour.service.NguoiDungService;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/promo")
public class PromoApiController {

    @Autowired
    private MaGiamGiaService maGiamGiaService;

    @Autowired
    private TourService tourService;

    @Autowired
    private NgayKhoiHanhService ngayKhoiHanhService;

    @Autowired
    private NgayKhoiHanhDiemDonService ngayKhoiHanhDiemDonService;

    @Autowired
    private BookingPricingService bookingPricingService;

    @Autowired
    private NguoiDungService nguoiDungService;

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody Map<String, Object> body) {
        String ma = body.get("ma") != null ? body.get("ma").toString() : "";
        int tourId = parseInt(body.get("tourId"), 0);
        int nkhId = parseInt(body.get("nkhId"), 0);
        int diemDonId = parseInt(body.get("diemDonId"), 0);
        int soNguoiLon = Math.max(1, parseInt(body.get("soNguoiLon"), 1));
        int soTreEm = Math.max(0, parseInt(body.get("soTreEm"), 0));
        int soTreNho = Math.max(0, parseInt(body.get("soTreNho"), 0));
        int soEmBe = Math.max(0, parseInt(body.get("soEmBe"), 0));
        int soPhongDon = Math.max(0, parseInt(body.get("soPhongDon"), 0));

        ChuyenDi tour = tourService.findByIdd(tourId);
        NgayKhoiHanh nkh = ngayKhoiHanhService.findById(nkhId);
        if (tour == null || nkh == null) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Thông tin tour không hợp lệ"));
        }
        if (diemDonId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Vui lòng chọn điểm đón"));
        }

        FlightQuoteResponse quote = ngayKhoiHanhDiemDonService.getQuote(nkhId, diemDonId, false);
        if (!quote.isAvailable()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", quote.getMessage()));
        }

        double unitPrice = quote.getUnitPrice();
        BookingPricingService.BookingPriceBreakdown pricing = bookingPricingService.calculate(
                tour, quote, soNguoiLon, soTreEm, soTreNho, soEmBe, soPhongDon
        );
        PromoApplyResult result = maGiamGiaService.validateAndApplyOnSubtotal(
                ma, tourId, unitPrice, pricing.getSubtotal(), resolveUserId(), nkh.getNgay()
        );

        Map<String, Object> resp = new HashMap<>();
        resp.put("valid", result.isValid());
        resp.put("message", result.getMessage());
        resp.put("subtotal", pricing.getSubtotal());
        resp.put("singleRoomTotal", pricing.getSingleRoomTotal());
        resp.put("singleRoomCount", pricing.getSingleRoomCount());
        if (result.isValid()) {
            resp.put("discount", result.getDiscount());
            resp.put("total", result.getTotal());
        }
        return ResponseEntity.ok(resp);
    }

    private Integer resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return nguoiDungService.findByTenDangNhap(auth.getName()).map(u -> u.getId()).orElse(null);
    }

    private int parseInt(Object o, int def) {
        if (o == null) return def;
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
