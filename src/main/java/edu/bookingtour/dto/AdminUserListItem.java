package edu.bookingtour.dto;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Dòng hiển thị trong admin danh sách người dùng (từ native query).
 */
public record AdminUserListItem(
        Integer id,
        String hoTen,
        String email,
        String phone,
        String vaiTro,
        String ngayThamGia,
        long paidBookings,
        double totalSpending) {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static AdminUserListItem fromRow(Object[] row) {
        return new AdminUserListItem(
                row[0] != null ? ((Number) row[0]).intValue() : null,
                row[1] != null ? row[1].toString() : "",
                row[2] != null ? row[2].toString() : "",
                row[3] != null ? row[3].toString() : null,
                row[4] != null ? row[4].toString() : "",
                formatDate(row[5]),
                row[6] != null ? ((Number) row[6]).longValue() : 0L,
                row[7] != null ? ((Number) row[7]).doubleValue() : 0.0);
    }

    private static String formatDate(Object value) {
        if (value == null) {
            return "-";
        }
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime().format(FMT);
        }
        if (value instanceof Date d) {
            return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(FMT);
        }
        if (value instanceof Instant i) {
            return i.atZone(ZoneId.systemDefault()).toLocalDate().format(FMT);
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt.format(FMT);
        }
        return value.toString();
    }
}
