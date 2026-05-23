package edu.bookingtour.controller.user;

import edu.bookingtour.config.VNPayConfig;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.dto.PromoApplyResult;
import edu.bookingtour.service.DatChoService;
import edu.bookingtour.service.EmailService;
import edu.bookingtour.service.MaGiamGiaService;
import edu.bookingtour.service.NgayKhoiHanhService;
import edu.bookingtour.service.NguoiDungService;
import edu.bookingtour.service.TourService;
import edu.bookingtour.repo.DiemDonRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class PaymentController {

    @Autowired
    private DatChoService datChoService;

    @Autowired
    private TourService tourService;

    @Autowired
    private NgayKhoiHanhService ngayKhoiHanhService;

    @Autowired
    private NguoiDungService nguoiDungService;

    @Autowired
    private VNPayConfig vnPayConfig;

    @Autowired
    private MaGiamGiaService maGiamGiaService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DiemDonRepository diemDonRepository;

    @PostMapping("/booking/submit")
    public String submitBooking(
            @RequestParam Integer tourId,
            @RequestParam Integer nkhId,
            @RequestParam Integer departureId,
            @RequestParam String hoTen,
            @RequestParam String email,
            @RequestParam String soDienThoai,
            @RequestParam Integer soLuong,
            @RequestParam(required = false) String diaChi,
            @RequestParam(required = false) String ghiChu,
            @RequestParam(required = false) String maGiamGia,
            Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {

        ChuyenDi tour = tourService.findByIdd(tourId);
        NgayKhoiHanh nkh = ngayKhoiHanhService.findById(nkhId);

        if (tour == null || nkh == null) {
            return "redirect:/tour";
        }

        // Require Gmail for booking notifications
        if (email == null || email.isBlank() || !email.trim().toLowerCase().endsWith("@gmail.com")) {
            redirectAttributes.addFlashAttribute("promoError", "Vui lòng nhập Gmail hợp lệ (@gmail.com) để nhận thông báo.");
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }

        // Create booking entry
        DatCho datCho = new DatCho();
        if (principal != null) {
            NguoiDung user = nguoiDungService.findByTenDangNhap(principal.getName()).orElse(null);
            datCho.setIdNguoiDung(user);
        }
        datCho.setIdChuyenDi(tour);
        datCho.setSoLuong(soLuong);
        datCho.setNgayDat(LocalDate.now());
        datCho.setCreatedAt(LocalDateTime.now());
        datCho.setTrangThai("PENDING");
        datCho.setHoTen(hoTen);
        datCho.setEmail(email);
        datCho.setSoDienThoai(soDienThoai);
        datCho.setDiaChi(diaChi);
        datCho.setGhiChu(ghiChu);

        // Validate selected departure point belongs to tour options
        tourService.normalizeTourDepartureOptions(tour);
        boolean okDeparture = tour.getDiemDons() != null && tour.getDiemDons().stream()
                .anyMatch(d -> d.getId() != null && d.getId().equals(departureId));
        if (!okDeparture) {
            redirectAttributes.addFlashAttribute("promoError", "Điểm xuất phát không hợp lệ.");
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }
        // Persist chosen departure on booking
        datCho.setIdDiemDon(diemDonRepository.getReferenceById(departureId));
        datCho.setIdNgayKhoiHanh(nkh);

        double unitPrice = tour.getGia().doubleValue() + nkh.getTongGiaVe();
        PromoApplyResult promoResult = maGiamGiaService.validateAndApply(maGiamGia, tourId, unitPrice, soLuong);
        if (maGiamGia != null && !maGiamGia.isBlank() && !promoResult.isValid()) {
            redirectAttributes.addFlashAttribute("promoError", promoResult.getMessage());
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }

        double totalAmount = promoResult.isValid() ? promoResult.getTotal()
                : unitPrice * soLuong;
        if (promoResult.isValid() && promoResult.getMaGiamGia() != null) {
            datCho.setIdMaGiamGia(promoResult.getMaGiamGia());
        }
        datCho.setTongGia(totalAmount);

        datCho = datChoService.save(datCho);
        emailService.sendBookingCreated(datCho);

        long amountVnd = (long) Math.round(totalAmount);
        if (amountVnd < 5000L) {
            redirectAttributes.addFlashAttribute("promoError", "Số tiền tối thiểu thanh toán VNPay là 5.000₫");
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }
        String orderInfo = "Donhang" + datCho.getId();
        // VNPay yêu cầu vnp_TxnRef unique; nếu reuse (vd: repay nhiều lần) sandbox thường báo
        // "Giao dịch đã quá thời gian chờ" ngay lập tức. Dùng suffix timestamp để luôn unique,
        // callback/IPN sẽ parse lại ID đơn từ phần trước dấu '_'.
        String txnRef = datCho.getId() + "_" + System.currentTimeMillis();
        try {
            Map<String, String> vnpParams = vnPayConfig.createPayParams(request, txnRef, amountVnd,
                    orderInfo);
            String paymentUrl = vnPayConfig.buildPaymentUrl(vnpParams);
            return "redirect:" + paymentUrl;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("promoError", "Không tạo được link VNPay: " + e.getMessage());
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }
    }

    @GetMapping("/payment/vnpay-callback")
    public String vnpayCallback(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, String> ipnResult = processVnpayNotification(request);
        if ("00".equals(ipnResult.get("RspCode")) || "02".equals(ipnResult.get("RspCode"))) {
            String resultUrl = "/payment/vnpay-result?" + request.getQueryString();
            return "redirect:" + resultUrl;
        }
        redirectAttributes.addFlashAttribute("error", ipnResult.get("Message"));
        return "redirect:/tour";
    }

    @RequestMapping(value = "/payment/vnpay-ipn", method = { org.springframework.web.bind.annotation.RequestMethod.GET,
            org.springframework.web.bind.annotation.RequestMethod.POST })
    @ResponseBody
    public Map<String, String> vnpayIpn(HttpServletRequest request) {
        return processVnpayNotification(request);
    }

    private Map<String, String> processVnpayNotification(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            Map<String, String> fields = vnPayConfig.extractRawFields(request);
            String vnpSecureHash = request.getParameter("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            if (!vnPayConfig.verifyCallbackSignature(request, vnpSecureHash)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid Checksum");
                return response;
            }

            String txnRef = request.getParameter("vnp_TxnRef");
            String responseCode = request.getParameter("vnp_ResponseCode");
            String amountStr = request.getParameter("vnp_Amount");
            if (txnRef == null || txnRef.isBlank()) {
                response.put("RspCode", "01");
                response.put("Message", "Order not Found");
                return response;
            }
            // txnRef có dạng "<datChoId>_<timestamp>"; fallback: nếu không có '_' thì coi như id.
            String orderIdStr = txnRef.split("_", 2)[0];
            DatCho datCho = datChoService.findById(Integer.parseInt(orderIdStr)).orElse(null);

            if (datCho == null) {
                response.put("RspCode", "01");
                response.put("Message", "Order not Found");
                return response;
            }

            if (amountStr != null) {
                long paidAmountCents = Long.parseLong(amountStr);
                long orderAmountCents = (long) Math.round(datCho.getTongGia() * 100);
                if (paidAmountCents != orderAmountCents) {
                    response.put("RspCode", "04");
                    response.put("Message", "Invalid Amount");
                    return response;
                }
            }

            if ("PAID".equals(datCho.getTrangThai())) {
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
                return response;
            }

            if ("00".equals(responseCode)) {
                datChoService.updateStatus(Integer.parseInt(orderIdStr), "PAID");
                try {
                    DatCho updated = datChoService.findByIdWithDetails(Integer.parseInt(orderIdStr)).orElse(null);
                    if (updated != null) {
                        emailService.sendPaymentSuccess(updated);
                    }
                } catch (Exception ignored) {
                }
            } else {
                datChoService.updateStatus(Integer.parseInt(orderIdStr), "FAILED");
            }
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return response;
        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
            return response;
        }
    }

    @GetMapping("/payment/vnpay-result")
    public String vnpayResult(HttpServletRequest request, Model model) {
        String responseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");
        String orderIdStr = txnRef != null ? txnRef.split("_", 2)[0] : null;
        model.addAttribute("responseCode", responseCode);
        model.addAttribute("orderId", orderIdStr);
        String amountStr = request.getParameter("vnp_Amount");
        model.addAttribute("amount", amountStr != null ? Long.parseLong(amountStr) : 0L);
        model.addAttribute("orderInfo", request.getParameter("vnp_OrderInfo"));
        model.addAttribute("bankCode", request.getParameter("vnp_BankCode"));
        model.addAttribute("payDate", request.getParameter("vnp_PayDate"));
        model.addAttribute("message", request.getParameter("vnp_Message"));
        if ("00".equals(responseCode) && orderIdStr != null) {
            try {
                datChoService.findById(Integer.parseInt(orderIdStr))
                        .ifPresent(b -> model.addAttribute("checkInToken", b.getMaCheckIn()));
            } catch (NumberFormatException ignored) {
            }
        }
        return "chuyendi/vnpay-result";
    }

    @GetMapping("/booking/repay/{id}")
    public String repay(@PathVariable Integer id, HttpServletRequest request, RedirectAttributes redirectAttributes)
            throws UnsupportedEncodingException {
        DatCho datCho = datChoService.findById(id).orElse(null);
        if (datCho == null || !"PENDING".equals(datCho.getTrangThai())) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không hợp lệ hoặc đã thanh toán!");
            return "redirect:/user/bookings";
        }

        // Check 15 mins expiration
        if (datCho.getCreatedAt() != null && datCho.getCreatedAt().plusMinutes(15).isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng đã hết hạn thanh toán (15 phút)!");
            return "redirect:/user/bookings";
        }

        long amountVnd = (long) Math.round(datCho.getTongGia());
        String orderInfo = "Thanh toan lai don hang " + datCho.getId();
        String txnRef = datCho.getId() + "_" + System.currentTimeMillis();
        Map<String, String> vnpParams = vnPayConfig.createPayParams(request, txnRef, amountVnd,
                orderInfo);
        String paymentUrl = vnPayConfig.buildPaymentUrl(vnpParams);

        return "redirect:" + paymentUrl;
    }
}
