package edu.bookingtour.controller.user;

import edu.bookingtour.config.VNPayConfig;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.dto.FlightQuoteResponse;
import edu.bookingtour.dto.PromoApplyResult;
import edu.bookingtour.service.DatChoService;
import edu.bookingtour.service.EmailService;
import edu.bookingtour.service.MaGiamGiaService;
import edu.bookingtour.service.BookingPricingService;
import edu.bookingtour.service.NgayKhoiHanhDiemDonService;
import edu.bookingtour.service.NgayKhoiHanhService;
import edu.bookingtour.service.NguoiDungService;
import edu.bookingtour.service.TourCapacityService;
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
import java.time.Period;
import java.util.*;
import java.util.Objects;

@Controller
public class PaymentController {

    @Autowired
    private DatChoService datChoService;

    @Autowired
    private TourService tourService;

    @Autowired
    private NgayKhoiHanhService ngayKhoiHanhService;

    @Autowired
    private NgayKhoiHanhDiemDonService ngayKhoiHanhDiemDonService;

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

    @Autowired
    private BookingPricingService bookingPricingService;

    @Autowired
    private TourCapacityService tourCapacityService;

    @PostMapping("/booking/submit")
    public String submitBooking(
            @RequestParam Integer tourId,
            @RequestParam Integer nkhId,
            @RequestParam Integer departureId,
            @RequestParam String hoTen,
            @RequestParam String email,
            @RequestParam String soDienThoai,
            @RequestParam(defaultValue = "1") Integer soNguoiLon,
            @RequestParam(defaultValue = "0") Integer soTreEm,
            @RequestParam(defaultValue = "0") Integer soTreNho,
            @RequestParam(defaultValue = "0") Integer soEmBe,
            @RequestParam(defaultValue = "0") Integer soPhongDon,
            @RequestParam(required = false) String diaChi,
            @RequestParam(required = false) String ghiChu,
            @RequestParam(required = false) String maGiamGia,
            @RequestParam(required = false, name = "adultNames") List<String> adultNames,
            @RequestParam(required = false, name = "adultDob") List<String> adultDob,
            @RequestParam(required = false, name = "adultGender") List<String> adultGender,
            @RequestParam(required = false, name = "adultPhone") List<String> adultPhone,
            @RequestParam(required = false, name = "childNames") List<String> childNames,
            @RequestParam(required = false, name = "childDob") List<String> childDob,
            @RequestParam(required = false, name = "childGender") List<String> childGender,
            @RequestParam(required = false, name = "smallChildNames") List<String> smallChildNames,
            @RequestParam(required = false, name = "smallChildDob") List<String> smallChildDob,
            @RequestParam(required = false, name = "smallChildGender") List<String> smallChildGender,
            @RequestParam(required = false, name = "babyNames") List<String> babyNames,
            @RequestParam(required = false, name = "babyDob") List<String> babyDob,
            @RequestParam(required = false, name = "babyGender") List<String> babyGender,
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
        datCho.setIdDiemDon(diemDonRepository.getReferenceById(Objects.requireNonNull(departureId)));
        datCho.setIdNgayKhoiHanh(nkh);

        FlightQuoteResponse quote = ngayKhoiHanhDiemDonService.getQuote(nkhId, departureId, false);
        if (!quote.isAvailable()) {
            redirectAttributes.addFlashAttribute("promoError", quote.getMessage());
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }

        BookingPricingService.BookingPriceBreakdown pricing = bookingPricingService.calculate(
                tour,
                quote,
                soNguoiLon,
                soTreEm,
                soTreNho,
                soEmBe,
                soPhongDon
        );
        if (pricing.getAdults() <= 0) {
            redirectAttributes.addFlashAttribute("promoError", "Cần tối thiểu 1 người lớn cho mỗi booking.");
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }
        if (pricing.getTotalGuests() <= 0) {
            redirectAttributes.addFlashAttribute("promoError", "Vui lòng chọn số lượng hành khách hợp lệ.");
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }
        try {
            tourCapacityService.assertCanBook(nkhId, pricing.getTotalGuests());
        } catch (TourCapacityService.CapacityExceededException ex) {
            redirectAttributes.addFlashAttribute("capacityRemaining", ex.getRemaining());
            redirectAttributes.addFlashAttribute("showCapacityToast", true);
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("promoError",
                    "Không kiểm tra được sức chứa. Vui lòng thử lại hoặc liên hệ hỗ trợ.");
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }
        List<String> normalizedAdultNames = adultNames == null ? List.of()
                : adultNames.stream().map(s -> s == null ? "" : s.trim()).filter(s -> !s.isEmpty()).toList();
        if (normalizedAdultNames.size() < pricing.getAdults()) {
            redirectAttributes.addFlashAttribute("promoError", "Vui lòng nhập đầy đủ thông tin người lớn.");
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }
        if (!validatePassengerGroup(pricing.getAdults(), adultNames, adultDob, adultGender, 12, 200, nkh.getNgay(), "Người lớn")) {
            redirectAttributes.addFlashAttribute("promoError", "Thông tin người lớn không hợp lệ hoặc sai độ tuổi (>=12).");
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }
        if (!validateRequiredPhones(pricing.getAdults(), adultPhone)) {
            redirectAttributes.addFlashAttribute("promoError", "Vui lòng nhập số điện thoại cho toàn bộ người lớn.");
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }
        if (!validatePassengerGroup(pricing.getChildren(), childNames, childDob, childGender, 5, 11, nkh.getNgay(), "Trẻ em")) {
            redirectAttributes.addFlashAttribute("promoError", "Thông tin trẻ em không hợp lệ hoặc sai độ tuổi (5-11).");
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }
        if (!validatePassengerGroup(pricing.getSmallChildren(), smallChildNames, smallChildDob, smallChildGender, 2, 4, nkh.getNgay(), "Trẻ nhỏ")) {
            redirectAttributes.addFlashAttribute("promoError", "Thông tin trẻ nhỏ không hợp lệ hoặc sai độ tuổi (2-4).");
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }
        if (!validatePassengerGroup(pricing.getBabies(), babyNames, babyDob, babyGender, 0, 1, nkh.getNgay(), "Em bé")) {
            redirectAttributes.addFlashAttribute("promoError", "Thông tin em bé không hợp lệ hoặc sai độ tuổi (<2).");
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }

        double unitPrice = quote.getUnitPrice();
        PromoApplyResult promoResult = maGiamGiaService.validateAndApplyOnSubtotal(
                maGiamGia,
                tourId,
                unitPrice,
                pricing.getSubtotal()
        );
        if (maGiamGia != null && !maGiamGia.isBlank() && !promoResult.isValid()) {
            redirectAttributes.addFlashAttribute("promoError", promoResult.getMessage());
            return "redirect:/tour/" + tourId + "/dat-tour?nkhId=" + nkhId;
        }

        double totalAmount = promoResult.isValid() ? promoResult.getTotal() : pricing.getSubtotal();
        if (promoResult.isValid() && promoResult.getMaGiamGia() != null) {
            datCho.setIdMaGiamGia(promoResult.getMaGiamGia());
        }
        datCho.setSoNguoiLon(pricing.getAdults());
        datCho.setSoTreEm(pricing.getChildren());
        datCho.setSoTreNho(pricing.getSmallChildren());
        datCho.setSoEmBe(pricing.getBabies());
        datCho.setSoLuong(pricing.getTotalGuests());
        datCho.setSoPhongDon(pricing.getSingleRoomCount());
        datCho.setPhuThuPhongDon(pricing.getSingleRoomTotal());
        datCho.setTongGia(totalAmount);
        datCho.setGhiChu(appendBookingBreakdownNote(ghiChu, pricing, normalizedAdultNames));

        datCho = datChoService.save(datCho);
        try {
            emailService.sendBookingCreated(datCho);
        } catch (Exception mailEx) {
            // Không chặn thanh toán nếu gửi mail lỗi
        }

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

    private String appendBookingBreakdownNote(String rawNote,
                                              BookingPricingService.BookingPriceBreakdown pricing,
                                              List<String> adultNames) {
        String userNote = rawNote == null ? "" : rawNote.trim();
        String breakdown = String.format(
                "Co cau hanh khach: NL=%d, TE(5-11)=%d, TN(2-4)=%d, EB(<2)=%d, phong don=%d, phu thu phong don=%.0f",
                pricing.getAdults(),
                pricing.getChildren(),
                pricing.getSmallChildren(),
                pricing.getBabies(),
                pricing.getSingleRoomCount(),
                pricing.getSingleRoomTotal()
        );
        String adultInfo = adultNames == null || adultNames.isEmpty()
                ? ""
                : " | Nguoi lon: " + String.join(", ", adultNames);
        if (userNote.isBlank()) {
            return breakdown + adultInfo;
        }
        return userNote + " | " + breakdown + adultInfo;
    }

    private boolean validatePassengerGroup(int expectedCount,
                                           List<String> names,
                                           List<String> dobs,
                                           List<String> genders,
                                           int minAge,
                                           int maxAge,
                                           LocalDate departureDate,
                                           String label) {
        if (expectedCount == 0) {
            return true;
        }
        if (names == null || dobs == null || genders == null) {
            return false;
        }
        if (names.size() < expectedCount || dobs.size() < expectedCount || genders.size() < expectedCount) {
            return false;
        }
        for (int i = 0; i < expectedCount; i++) {
            String name = names.get(i) == null ? "" : names.get(i).trim();
            String dobText = dobs.get(i) == null ? "" : dobs.get(i).trim();
            String gender = genders.get(i) == null ? "" : genders.get(i).trim();
            if (name.isBlank() || dobText.isBlank() || gender.isBlank()) {
                return false;
            }
            LocalDate dob = parseDateSafe(dobText);
            if (dob == null) {
                return false;
            }
            int age = Period.between(dob, departureDate).getYears();
            if (age < minAge || age > maxAge) {
                return false;
            }
        }
        return true;
    }

    private boolean validateRequiredPhones(int expectedCount, List<String> phones) {
        if (expectedCount == 0) {
            return true;
        }
        if (phones == null || phones.size() < expectedCount) {
            return false;
        }
        for (int i = 0; i < expectedCount; i++) {
            String phone = phones.get(i) == null ? "" : phones.get(i).trim();
            if (phone.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private LocalDate parseDateSafe(String value) {
        try {
            return LocalDate.parse(value);
        } catch (Exception ignored) {
            return null;
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
