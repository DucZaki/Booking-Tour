package edu.bookingtour.util;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.DiemDen;
import edu.bookingtour.entity.NgayKhoiHanh;

import java.time.format.DateTimeFormatter;

/**
 * HTML/text email templates — thương hiệu ZakiBooking.
 */
public final class BookingEmailTemplate {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String BRAND = "ZakiBooking";
    private static final String ACCENT = "#ffc107";
    private static final String DARK = "#212529";

    private BookingEmailTemplate() {
    }

    public static String successSubject(DatCho booking) {
        return BRAND + " — Xác nhận đặt tour thành công #" + booking.getId();
    }

    public static String pendingSubject(DatCho booking) {
        return BRAND + " — Đơn đặt tour #" + booking.getId() + " đang chờ thanh toán";
    }

    public static String successHtml(DatCho booking) {
        return successHtml(booking, "http://localhost:8080");
    }

    public static String successHtml(DatCho booking, String baseUrl) {
        BookingInfo info = BookingInfo.from(booking, baseUrl);
        StringBuilder rows = new StringBuilder();
        row(rows, "Mã đơn hàng", "#" + info.orderId);
        row(rows, "Mã tour", info.tourCode);
        row(rows, "Chuyến đi", info.tourTitle);
        row(rows, "Điểm khởi hành", info.departurePoint);
        row(rows, "Điểm đến", info.destination);
        row(rows, "Phương tiện", info.transport);
        row(rows, "Ngày đi", info.departureDate);
        row(rows, "Ngày về", info.returnDate);
        if (info.outboundTrip != null) {
            row(rows, "Chiều đi", info.outboundTrip);
        }
        if (info.returnTrip != null) {
            row(rows, "Chiều về", info.returnTrip);
        }
        row(rows, "Số khách", info.guestCount);
        row(rows, "Tổng thanh toán", "<strong style=\"color:#dc3545;font-size:18px;\">" + info.totalPrice + "</strong>");
        row(rows, "Liên hệ", info.phone);

        String qrBlock = "";
        if (info.checkInQrUrl != null) {
            qrBlock = """
                    <tr><td colspan="2" style="padding:20px 16px;text-align:center;background:#fff8e1;">
                      <p style="margin:0 0 12px;font-weight:700;color:#856404;">Mã QR check-in</p>
                      <img src="%s" alt="QR check-in" width="160" height="160" style="border:1px solid #ffc107;border-radius:8px;padding:8px;background:#fff;">
                      <p style="margin:12px 0 0;font-size:12px;color:#6c757d;">Quét khi lên tour · <a href="%s" style="color:#856404;">Mở vé online</a></p>
                    </td></tr>
                    """.formatted(info.checkInQrUrl, esc(info.checkInPageUrl));
        }

        return wrap(
                "Đặt tour thành công!",
                "Xin chào <strong>" + esc(info.customerName) + "</strong>, cảm ơn bạn đã tin tưởng <strong>"
                        + BRAND + "</strong>. Thanh toán đã được xác nhận. Dưới đây là thông tin chuyến đi của bạn:",
                rows.toString() + qrBlock,
                "Hẹn gặp bạn trên hành trình sắp tới! Đưa mã QR cho nhân viên ZakiBooking khi check-in.",
                "#28a745",
                "✓ Thanh toán thành công");
    }

    public static String successText(DatCho booking) {
        BookingInfo info = BookingInfo.from(booking);
        StringBuilder b = new StringBuilder();
        b.append(BRAND).append(" — Xác nhận đặt tour thành công\n\n");
        b.append("Xin chao ").append(info.customerName).append(",\n\n");
        b.append("Ma don: #").append(info.orderId).append("\n");
        b.append("Ma tour: ").append(info.tourCode).append("\n");
        b.append("Chuyen di: ").append(info.tourTitle).append("\n");
        b.append("Diem khoi hanh: ").append(info.departurePoint).append("\n");
        b.append("Diem den: ").append(info.destination).append("\n");
        b.append("Phuong tien: ").append(info.transport).append("\n");
        b.append("Ngay di: ").append(info.departureDate).append("\n");
        b.append("Ngay ve: ").append(info.returnDate).append("\n");
        b.append("So khach: ").append(info.guestCount).append("\n");
        b.append("Tong thanh toan: ").append(info.totalPrice).append("\n\n");
        b.append("Cam on ban da su dung ").append(BRAND).append("!\n");
        return b.toString();
    }

    public static String pendingHtml(DatCho booking) {
        BookingInfo info = BookingInfo.from(booking);
        StringBuilder rows = new StringBuilder();
        row(rows, "Mã đơn hàng", "#" + info.orderId);
        row(rows, "Chuyến đi", info.tourTitle);
        row(rows, "Ngày đi", info.departureDate);
        row(rows, "Tổng tiền", info.totalPrice);

        return wrap(
                "Đơn đang chờ thanh toán",
                "Xin chào <strong>" + esc(info.customerName) + "</strong>, "
                        + BRAND + " đã ghi nhận đơn đặt tour của bạn. Vui lòng hoàn tất thanh toán VNPay trong <strong>15 phút</strong>.",
                rows.toString(),
                "Sau khi thanh toán thành công, bạn sẽ nhận email xác nhận đầy đủ thông tin chuyến đi.",
                ACCENT,
                "⏳ Chờ thanh toán");
    }

    public static String pendingText(DatCho booking) {
        BookingInfo info = BookingInfo.from(booking);
        return BRAND + " — Don #" + info.orderId + " dang cho thanh toan.\n"
                + "Chuyen di: " + info.tourTitle + "\n"
                + "Tong tien: " + info.totalPrice + "\n";
    }

    private static String wrap(String title, String intro, String tableRows, String footer, String badgeColor,
            String badgeText) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#f4f4f5;font-family:Segoe UI,Roboto,Arial,sans-serif;">
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f4f4f5;padding:24px 12px;">
                <tr><td align="center">
                <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="max-width:600px;width:100%%;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.08);">
                  <tr>
                    <td style="background:linear-gradient(135deg,%s 0%%,#343a40 100%%);padding:28px 32px;text-align:center;">
                      <div style="font-size:32px;font-weight:800;letter-spacing:1px;color:%s;">Zaki<span style="color:#fff;">Booking</span></div>
                      <div style="color:#adb5bd;font-size:13px;margin-top:6px;">Đặt tour — Trải nghiệm — Khám phá</div>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:28px 32px 8px;">
                      <span style="display:inline-block;background:%s;color:#fff;font-size:12px;font-weight:700;padding:6px 14px;border-radius:20px;">%s</span>
                      <h1 style="margin:16px 0 12px;font-size:22px;color:%s;">%s</h1>
                      <p style="margin:0 0 20px;color:#495057;line-height:1.6;font-size:15px;">%s</p>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:0 32px 24px;">
                      <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border:1px solid #e9ecef;border-radius:8px;overflow:hidden;">
                        %s
                      </table>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:0 32px 28px;">
                      <p style="margin:0;color:#6c757d;font-size:14px;line-height:1.6;">%s</p>
                    </td>
                  </tr>
                  <tr>
                    <td style="background:#f8f9fa;padding:20px 32px;text-align:center;border-top:1px solid #e9ecef;">
                      <p style="margin:0 0 4px;font-weight:700;color:%s;">%s</p>
                      <p style="margin:0;font-size:12px;color:#adb5bd;">Email tự động — vui lòng không trả lời trực tiếp.</p>
                    </td>
                  </tr>
                </table>
                </td></tr></table>
                </body></html>
                """.formatted(DARK, ACCENT, badgeColor, badgeText, DARK, esc(title), intro, tableRows, footer, DARK,
                BRAND);
    }

    private static void row(StringBuilder sb, String label, String value) {
        sb.append("""
                <tr>
                  <td style="padding:12px 16px;border-bottom:1px solid #e9ecef;background:#fafafa;width:38%%;font-size:13px;color:#6c757d;font-weight:600;">%s</td>
                  <td style="padding:12px 16px;border-bottom:1px solid #e9ecef;font-size:14px;color:#212529;">%s</td>
                </tr>
                """.formatted(esc(label), value));
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String fmtDate(java.time.LocalDate d) {
        return d != null ? d.format(DATE_FMT) : "—";
    }

    private static String fmtMoney(Double amount) {
        if (amount == null) {
            return "—";
        }
        return String.format("%,.0f VND", amount);
    }

    private record BookingInfo(
            int orderId,
            String customerName,
            String tourCode,
            String tourTitle,
            String departurePoint,
            String destination,
            String transport,
            String departureDate,
            String returnDate,
            String outboundTrip,
            String returnTrip,
            String guestCount,
            String totalPrice,
            String phone,
            String checkInQrUrl,
            String checkInPageUrl) {

        static BookingInfo from(DatCho b) {
            return from(b, "http://localhost:8080");
        }

        static BookingInfo from(DatCho b, String baseUrl) {
            ChuyenDi tour = b.getIdChuyenDi();
            NgayKhoiHanh nkh = b.getIdNgayKhoiHanh();
            DiemDen den = tour != null ? tour.getIdDiemDen() : null;

            String transport = "—";
            if (tour != null && tour.getIdPhuongTien() != null) {
                String kind = TransportUi.kind(tour.getIdPhuongTien());
                transport = switch (kind) {
                    case TransportUi.BUS -> "Xe khách (Bus)";
                    case TransportUi.SHIP -> "Tàu thủy / Phà";
                    default -> "Máy bay";
                };
            }

            String outbound = null;
            String inbound = null;
            if (nkh != null) {
                if (nkh.getMaChuyenBayDi() != null) {
                    outbound = nkh.getMaChuyenBayDi() + " · "
                            + nz(nkh.getGioBayDi()) + " → " + nz(nkh.getGioDenDi());
                }
                if (nkh.getMaChuyenBayVe() != null) {
                    inbound = nkh.getMaChuyenBayVe() + " · "
                            + nz(nkh.getGioBayVe()) + " → " + nz(nkh.getGioDenVe());
                }
            }

            String dest = "—";
            if (den != null) {
                dest = den.getThanhPho() != null ? den.getThanhPho() : "—";
                if (den.getQuocGia() != null && !den.getQuocGia().isBlank()) {
                    dest += ", " + den.getQuocGia();
                }
            }

            String token = b.getMaCheckIn();
            String qrUrl = null;
            String pageUrl = null;
            if (token != null && !token.isBlank()) {
                String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
                pageUrl = base + "/check-in/" + token;
                qrUrl = pageUrl + "/qr.png";
            }

            return new BookingInfo(
                    b.getId(),
                    b.getHoTen() != null ? b.getHoTen() : "Quý khách",
                    tour != null ? TourCodeUtil.format(tour.getId()) : "—",
                    tour != null ? nz(tour.getTieuDe()) : "—",
                    b.getIdDiemDon() != null ? nz(b.getIdDiemDon().getTen()) : "—",
                    dest,
                    transport,
                    nkh != null ? fmtDate(nkh.getNgay()) : "—",
                    nkh != null ? fmtDate(nkh.getNgayVe()) : "—",
                    outbound,
                    inbound,
                    b.getSoLuong() != null ? String.valueOf(b.getSoLuong()) : "—",
                    fmtMoney(b.getTongGia()),
                    nz(b.getSoDienThoai()),
                    qrUrl,
                    pageUrl);
        }

        private static String nz(String v) {
            return v != null && !v.isBlank() ? v : "—";
        }
    }
}
