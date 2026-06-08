package edu.bookingtour.util;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.NgayKhoiHanh;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class DepartureTimeUtil {

    private static final Pattern HH_MM = Pattern.compile("^\\d{1,2}:\\d{2}$");
    private static final DateTimeFormatter HH_MM_FMT = DateTimeFormatter.ofPattern("H:mm");
    public static final LocalTime DEFAULT_GATHERING_TIME = LocalTime.of(6, 0);
    public static final int HOURS_BEFORE_BUS = 1;
    public static final int HOURS_BEFORE_FLIGHT = 2;

    private DepartureTimeUtil() {
    }

    /** Giờ tập trung = giờ khởi hành (bay/xe) trừ 1h (bus) hoặc 2h (máy bay). */
    public static String computeGatheringTime(String departureTime, boolean isBus) {
        LocalTime dep = parseTime(departureTime);
        int hoursBefore = isBus ? HOURS_BEFORE_BUS : HOURS_BEFORE_FLIGHT;
        return formatTime(dep.minusHours(hoursBefore));
    }

    public static boolean isBusTour(ChuyenDi chuyenDi) {
        return chuyenDi != null
                && chuyenDi.getIdPhuongTien() != null
                && "Bus".equalsIgnoreCase(chuyenDi.getIdPhuongTien().getLoai());
    }

    public static void syncGatheringTime(NgayKhoiHanh nkh, ChuyenDi chuyenDi) {
        if (nkh == null) {
            return;
        }
        nkh.setGioTapTrung(computeGatheringTime(nkh.getGioBayDi(), isBusTour(chuyenDi)));
    }

    public static LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_GATHERING_TIME;
        }
        String trimmed = raw.trim();
        if ("N/A".equalsIgnoreCase(trimmed)) {
            return DEFAULT_GATHERING_TIME;
        }
        try {
            if (HH_MM.matcher(trimmed).matches()) {
                return LocalTime.parse(trimmed, HH_MM_FMT);
            }
            if (trimmed.length() >= 5 && trimmed.charAt(2) == ':') {
                return LocalTime.parse(trimmed.substring(0, 5));
            }
        } catch (DateTimeParseException ignored) {
            // fallback below
        }
        return DEFAULT_GATHERING_TIME;
    }

    public static String formatTime(LocalTime time) {
        if (time == null) {
            return DEFAULT_GATHERING_TIME.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /** Chuẩn hóa HH:mm từ form admin; ném lỗi nếu sai định dạng. */
    public static String normalizeGatheringTime(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Thời gian xuất phát không được để trống");
        }
        String trimmed = raw.trim();
        if (!HH_MM.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Thời gian xuất phát phải theo định dạng HH:mm (ví dụ 07:00)");
        }
        return formatTime(LocalTime.parse(trimmed, HH_MM_FMT));
    }

    public static LocalDateTime gatheringDateTime(LocalDate departureDate, String gatheringTime) {
        LocalDate date = departureDate != null ? departureDate : LocalDate.now();
        return LocalDateTime.of(date, parseTime(gatheringTime));
    }

    /** Parse HH:mm; trả null nếu N/A hoặc không hợp lệ (dùng hiển thị, không fallback 06:00). */
    public static LocalTime parseTimeOrNull(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        if ("N/A".equalsIgnoreCase(trimmed)) {
            return null;
        }
        try {
            if (HH_MM.matcher(trimmed).matches()) {
                return LocalTime.parse(trimmed, HH_MM_FMT);
            }
            if (trimmed.length() >= 5 && trimmed.charAt(2) == ':') {
                return LocalTime.parse(trimmed.substring(0, 5));
            }
        } catch (DateTimeParseException ignored) {
            return null;
        }
        return null;
    }

    /** Thời lượng bay/xe giữa hai mốc HH:mm — ví dụ "2g 10p". */
    public static String formatFlightDuration(String depart, String arrive) {
        LocalTime d = parseTimeOrNull(depart);
        LocalTime a = parseTimeOrNull(arrive);
        if (d == null || a == null) {
            return "";
        }
        int mins = a.toSecondOfDay() / 60 - d.toSecondOfDay() / 60;
        if (mins < 0) {
            mins += 24 * 60;
        }
        if (mins <= 0) {
            return "";
        }
        int h = mins / 60;
        int m = mins % 60;
        if (h > 0 && m > 0) {
            return h + "g " + m + "p";
        }
        if (h > 0) {
            return h + "g";
        }
        return m + "p";
    }
}
