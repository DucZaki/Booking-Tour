package edu.bookingtour.config;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

@Component
public class VNPayConfig {
    private static final Logger log = LoggerFactory.getLogger(VNPayConfig.class);

    @Value("${vnp.pay-url}")
    public String vnp_PayUrl;

    @Value("${vnp.return-url}")
    public String vnp_Returnurl;

    @Value("${vnp.tmn-code}")
    public String vnp_TmnCode;

    @Value("${vnp.hash-secret}")
    public String vnp_HashSecret;

    @Value("${vnp.api-url}")
    public String vnp_ApiUrl;

    @Value("${vnp.ipn-url:${vnp.return-url}}")
    public String vnp_IpnUrl;

    @PostConstruct
    void logConfig() {
        log.info("VNPay TMN={}, payUrl={}, returnUrl={}, ipnUrl={}", vnp_TmnCode, vnp_PayUrl, vnp_Returnurl, vnp_IpnUrl);
        if (vnp_Returnurl != null && vnp_Returnurl.contains("YOUR_DOMAIN")) {
            log.error("VNPay returnUrl vẫn là placeholder YOUR_DOMAIN — đặt APP_BASE_URL đúng domain Railway "
                    + "(Variables) rồi redeploy, và đăng ký URL callback trên cổng VNPay Sandbox.");
        }
        if (vnp_TmnCode == null || vnp_TmnCode.isBlank() || vnp_HashSecret == null || vnp_HashSecret.isBlank()) {
            log.error("Thiếu VNP_TMN_CODE hoặc VNP_HASH_SECRET — không tạo được link thanh toán.");
        }
    }

    public String getReturnUrl(HttpServletRequest request) {
        if (vnp_Returnurl != null && !vnp_Returnurl.isBlank()) {
            return vnp_Returnurl;
        }
        // Fallback: derive from incoming request when not configured.
        // NOTE: VNPay sandbox/live thường yêu cầu ReturnUrl/IPNUrl thuộc domain đã được phê duyệt.
        if (request != null) {
            String forwardedHost = request.getHeader("X-Forwarded-Host");
            if (forwardedHost != null && !forwardedHost.isBlank()) {
                String proto = request.getHeader("X-Forwarded-Proto");
                if (proto == null || proto.isBlank()) {
                    proto = "https";
                }
                return proto + "://" + forwardedHost.split(",")[0].trim() + "/payment/vnpay-callback";
            }
            String host = request.getHeader("Host");
            if (host != null && !host.isBlank()) {
                return "http://" + host.split(",")[0].trim() + "/payment/vnpay-callback";
            }
        }
        return "http://localhost:8080/payment/vnpay-callback";
    }

    public boolean verifyCallbackSignature(HttpServletRequest request, String vnpSecureHash)
            throws UnsupportedEncodingException {
        if (vnpSecureHash == null || vnpSecureHash.isBlank()) {
            return false;
        }
        Map<String, String> raw = extractRawFields(request);
        raw.remove("vnp_SecureHashType");
        raw.remove("vnp_SecureHash");
        if (hashRawFields(raw).equals(vnpSecureHash)) {
            return true;
        }
        // Fallback theo vnpay_return.jsp (encode trước rồi hash)
        Map<String, String> encoded = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String rawName = params.nextElement();
            if ("vnp_SecureHash".equals(rawName) || "vnp_SecureHashType".equals(rawName)) {
                continue;
            }
            String rawValue = request.getParameter(rawName);
            if (rawValue != null && !rawValue.isEmpty()) {
                encoded.put(URLEncoder.encode(rawName, StandardCharsets.US_ASCII),
                        URLEncoder.encode(rawValue, StandardCharsets.US_ASCII));
            }
        }
        return hashSignedFields(encoded).equals(vnpSecureHash);
    }

    private String hashSignedFields(Map<String, String> encodedFields) {
        List<String> fieldNames = new ArrayList<>(encodedFields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = encodedFields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                sb.append(fieldName).append('=').append(fieldValue);
            }
            if (itr.hasNext()) {
                sb.append('&');
            }
        }
        return hmacSHA512(vnp_HashSecret, sb.toString());
    }

    public String md5(String message) {
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
        } catch (UnsupportedEncodingException ex) {
            digest = "";
        } catch (NoSuchAlgorithmException ex) {
            digest = "";
        }
        return digest;
    }

    public String Sha256(String message) {
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
        } catch (UnsupportedEncodingException ex) {
            digest = "";
        } catch (NoSuchAlgorithmException ex) {
            digest = "";
        }
        return digest;
    }

    /** Verify callback/IPN — encode giá trị raw một lần (chuẩn VNPay). */
    public String hashRawFields(Map<String, String> rawFields) throws UnsupportedEncodingException {
        List<String> fieldNames = new ArrayList<>(rawFields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldValue = rawFields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (!first) {
                    hashData.append('&');
                }
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                first = false;
            }
        }
        return hmacSHA512(vnp_HashSecret, hashData.toString());
    }

    public Map<String, String> extractRawFields(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String name = params.nextElement();
            String value = request.getParameter(name);
            if (value != null && !value.isEmpty()) {
                fields.put(name, value);
            }
        }
        return fields;
    }

    // Util verify callback/IPN — giống Config.hashAllFields() trong code mẫu VNPay JSP
    /** Tạo URL thanh toán — giống ajaxServlet.java trong code mẫu VNPay JSP */
    public String buildPaymentUrl(Map<String, String> params) throws UnsupportedEncodingException {
        if (vnp_TmnCode == null || vnp_TmnCode.isBlank() || vnp_HashSecret == null || vnp_HashSecret.isBlank()) {
            throw new IllegalStateException("VNPay chưa được cấu hình (vnp.tmn-code / vnp.hash-secret)");
        }

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        return vnp_PayUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    public Map<String, String> createPayParams(HttpServletRequest request, String txnRef, long amountVnd,
            String orderInfo) {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnp_TmnCode);
        // VNPay yêu cầu số tiền theo đơn vị nhỏ nhất (VND * 100)
        params.put("vnp_Amount", String.valueOf(amountVnd * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        String returnUrl = getReturnUrl(request);
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", getIpAddress(request));
        log.info("VNPay payment: txnRef={}, amountVnd={}, amountVnp={}, returnUrl={}", txnRef, amountVnd,
                amountVnd * 100, returnUrl);

        // Etc/GMT+7 là GMT-7 (đảo dấu), dùng timezone VN chuẩn để tránh CreateDate/ExpireDate sai.
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        params.put("vnp_CreateDate", formatter.format(cld.getTime()));
        cld.add(Calendar.MINUTE, 15);
        params.put("vnp_ExpireDate", formatter.format(cld.getTime()));
        return params;
    }

    public String hmacSHA512(final String key, final String data) {
        try {

            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }

    public String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
            if ("0:0:0:0:0:0:0:1".equals(ipAdress) || "::1".equals(ipAdress)) {
                ipAdress = "127.0.0.1";
            }
        } catch (Exception e) {
            ipAdress = "127.0.0.1";
        }
        return ipAdress;
    }

    public String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
