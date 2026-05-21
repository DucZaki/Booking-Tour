package edu.bookingtour.service;

import edu.bookingtour.entity.DatCho;
import edu.bookingtour.util.BookingEmailTemplate;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BRAND_NAME = "ZakiBooking";

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String from;

    @Value("${app.base-url:http://localhost:8080}")
    private String mailBaseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /** Email ngắn khi vừa tạo đơn — chờ thanh toán VNPay. */
    public void sendBookingCreated(DatCho datCho) {
        if (!hasRecipient(datCho)) {
            return;
        }
        sendHtml(
                datCho.getEmail(),
                BookingEmailTemplate.pendingSubject(datCho),
                BookingEmailTemplate.pendingHtml(datCho),
                BookingEmailTemplate.pendingText(datCho));
    }

    /** Email xác nhận đầy đủ sau khi thanh toán thành công. */
    public void sendPaymentSuccess(DatCho datCho) {
        if (!hasRecipient(datCho)) {
            return;
        }
        sendHtml(
                datCho.getEmail(),
                BookingEmailTemplate.successSubject(datCho),
                BookingEmailTemplate.successHtml(datCho, mailBaseUrl),
                BookingEmailTemplate.successText(datCho));
    }

    private void sendHtml(String to, String subject, String html, String plainText) {
        if (from == null || from.isBlank()) {
            log.warn("Skip email to {} — app.mail.from chưa cấu hình", to);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(from, BRAND_NAME, "UTF-8"));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainText, html);
            mailSender.send(message);
            log.info("Sent {} email to {}", BRAND_NAME, to);
        } catch (Exception e) {
            log.warn("Send mail failed to {}: {}", to, e.getMessage());
        }
    }

    private static boolean hasRecipient(DatCho datCho) {
        return datCho != null && datCho.getEmail() != null && !datCho.getEmail().isBlank();
    }
}
