package edu.bookingtour.util;

/**
 * Mã tour hiển thị: ZAKI + id (vd. ZAKI3).
 */
public final class TourCodeUtil {

    private static final String PREFIX = "ZAKI";

    private TourCodeUtil() {
    }

    public static String format(Integer id) {
        return id != null ? PREFIX + id : PREFIX;
    }
}
