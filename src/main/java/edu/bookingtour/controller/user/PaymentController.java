package edu.bookingtour.controller.user;

import edu.bookingtour.config.VNPayConfig;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.service.DatChoService;
import edu.bookingtour.service.NgayKhoiHanhService;
import edu.bookingtour.service.NguoiDungService;
import edu.bookingtour.service.TourService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.SimpleDateFormat;
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

    @PostMapping("/booking/submit")
    public String submitBooking(
            @RequestParam Integer tourId,
            @RequestParam Integer nkhId,
            @RequestParam String hoTen,
            @RequestParam String email,
            @RequestParam String soDienThoai,
            @RequestParam Integer soLuong,
            @RequestParam(required = false) String diaChi,
            @RequestParam(required = false) String ghiChu,
            Principal principal,
            HttpServletRequest request) throws UnsupportedEncodingException {

        ChuyenDi tour = tourService.findByIdd(tourId);
        NgayKhoiHanh nkh = ngayKhoiHanhService.findById(nkhId);

        if (tour == null || nkh == null) {
            return "redirect:/tour";
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

        // Calculate total amount including transport fees
        double totalAmount = (tour.getGia().doubleValue() + nkh.getTongGiaVe()) * soLuong;
        datCho.setTongGia(totalAmount);

        datCho = datChoService.save(datCho);

        // Prepare VNPay parameters
        long amount = (long) (totalAmount * 100);
        String vnp_TxnRef = String.valueOf(datCho.getId());
        String vnp_IpAddr = vnPayConfig.getIpAddress(request);
        String vnp_TmnCode = vnPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan tour: " + tour.getTieuDe());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.vnp_Returnurl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build query string and hash data
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                if (!first) {
                    query.append('&');
                }
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                first = false;
            }
        }

        String vnp_SecureHash = vnPayConfig.hashAllFields(vnp_Params);
        String paymentUrl = vnPayConfig.vnp_PayUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;

        return "redirect:" + paymentUrl;
    }

    @GetMapping("/payment/vnpay-callback")
    public String vnpayCallback(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }

        // Check hash
        try {
            String signValue = vnPayConfig.hashAllFields(fields);
            if (signValue.equals(vnp_SecureHash)) {
                String orderId = request.getParameter("vnp_TxnRef");
                String responseCode = request.getParameter("vnp_ResponseCode");

                if ("00".equals(responseCode)) {
                    // Payment success
                    datChoService.updateStatus(Integer.parseInt(orderId), "PAID");
                } else {
                    // Payment failed
                    datChoService.updateStatus(Integer.parseInt(orderId), "FAILED");
                }

                // Redirect to a dedicated result page with all info from VNPay
                String resultUrl = "/payment/vnpay-result?" + request.getQueryString();
                return "redirect:" + resultUrl;
            } else {
                redirectAttributes.addFlashAttribute("error", "Chữ ký không hợp lệ!");
                return "redirect:/tour";
            }
        } catch (UnsupportedEncodingException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi mã hóa: " + e.getMessage());
            return "redirect:/tour";
        }
    }

    @GetMapping("/payment/vnpay-result")
    public String vnpayResult(HttpServletRequest request, Model model) {
        model.addAttribute("responseCode", request.getParameter("vnp_ResponseCode"));
        model.addAttribute("orderId", request.getParameter("vnp_TxnRef"));
        String amountStr = request.getParameter("vnp_Amount");
        model.addAttribute("amount", amountStr != null ? Long.parseLong(amountStr) : 0L);
        model.addAttribute("orderInfo", request.getParameter("vnp_OrderInfo"));
        model.addAttribute("bankCode", request.getParameter("vnp_BankCode"));
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

        ChuyenDi tour = datCho.getIdChuyenDi();
        long amount = (long) (datCho.getTongGia() * 100);

        String vnp_TxnRef = String.valueOf(datCho.getId());
        String vnp_IpAddr = vnPayConfig.getIpAddress(request);
        String vnp_TmnCode = vnPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan lai tour: " + tour.getTieuDe());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.vnp_Returnurl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                if (!first)
                    query.append('&');
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                first = false;
            }
        }

        String vnp_SecureHash = vnPayConfig.hashAllFields(vnp_Params);
        String paymentUrl = vnPayConfig.vnp_PayUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;

        return "redirect:" + paymentUrl;
    }
}
