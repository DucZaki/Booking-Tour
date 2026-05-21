package edu.bookingtour.service;

import edu.bookingtour.entity.DatCho;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendBookingCreated(DatCho datCho) {
        if (datCho == null || datCho.getEmail() == null || datCho.getEmail().isBlank()) {
            return;
        }
        String subject = "ZakiBooking - Da nhan don dat cho #" + datCho.getId();
        StringBuilder body = new StringBuilder();
        body.append("Xin chao ").append(nz(datCho.getHoTen(), "Quy khach")).append("\n\n");
        body.append("ZakiBooking da nhan don dat cho cua ban.\n");
        body.append("Ma don hang: #").append(datCho.getId()).append("\n");
        if (datCho.getIdChuyenDi() != null) {
            body.append("Chuyen di: ").append(nz(datCho.getIdChuyenDi().getTieuDe(), "")).append("\n");
        }
        if (datCho.getIdDiemDon() != null) {
            body.append("Diem xuat phat: ").append(nz(datCho.getIdDiemDon().getTen(), "")).append("\n");
        }
        if (datCho.getSoLuong() != null) {
            body.append("So luong khach: ").append(datCho.getSoLuong()).append("\n");
        }
        if (datCho.getTongGia() != null) {
            body.append("Tong tien (tam tinh): ").append(String.format("%,.0f", datCho.getTongGia())).append(" VND\n");
        }
        body.append("\nTrang thai: ").append(nz(datCho.getTrangThai(), "PENDING")).append("\n");
        body.append("\nCam on ban da su dung ZakiBooking.\n");

        send(datCho.getEmail(), subject, body.toString());
    }

    public void sendPaymentSuccess(DatCho datCho) {
        if (datCho == null || datCho.getEmail() == null || datCho.getEmail().isBlank()) {
            return;
        }
        String subject = "ZakiBooking - Thanh toan thanh cong #" + datCho.getId();
        StringBuilder body = new StringBuilder();
        body.append("Xin chao ").append(nz(datCho.getHoTen(), "Quy khach")).append("\n\n");
        body.append("Thanh toan cua ban da duoc xac nhan thanh cong.\n");
        body.append("Ma don hang: #").append(datCho.getId()).append("\n");
        if (datCho.getIdChuyenDi() != null) {
            body.append("Chuyen di: ").append(nz(datCho.getIdChuyenDi().getTieuDe(), "")).append("\n");
        }
        if (datCho.getIdDiemDon() != null) {
            body.append("Diem xuat phat: ").append(nz(datCho.getIdDiemDon().getTen(), "")).append("\n");
        }
        if (datCho.getTongGia() != null) {
            body.append("So tien: ").append(String.format("%,.0f", datCho.getTongGia())).append(" VND\n");
        }
        body.append("\nHen gap ban tren hanh trinh sap toi.\n");

        send(datCho.getEmail(), subject, body.toString());
    }

    private void send(String to, String subject, String text) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            if (from != null && !from.isBlank()) {
                msg.setFrom(from);
            }
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
        } catch (Exception e) {
            // Never fail checkout/payment because of email.
            log.warn("Send mail failed to {}: {}", to, e.getMessage());
        }
    }

    private static String nz(String v, String fallback) {
        return (v == null) ? fallback : v;
    }
}
