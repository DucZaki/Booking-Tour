package edu.bookingtour.util;

import edu.bookingtour.entity.PhuongTien;

/**
 * Nhãn & icon hiển thị theo loại phương tiện (máy bay / xe / tàu).
 */
public final class TransportUi {

    public static final String PLANE = "plane";
    public static final String BUS = "bus";
    public static final String SHIP = "ship";

    private TransportUi() {
    }

    public static String kind(PhuongTien pt) {
        if (pt == null) {
            return PLANE;
        }
        return kind(pt.getLoai(), pt.getHang());
    }

    public static String label(String kind) {
        return switch (kind) {
            case BUS -> "Xe khách";
            case SHIP -> "Tàu thủy";
            default -> "Máy bay";
        };
    }

    public static String label(PhuongTien pt) {
        return label(kind(pt));
    }

    public static String kind(String loai, String hang) {
        String combined = ((loai != null ? loai : "") + " " + (hang != null ? hang : "")).toLowerCase();
        if (combined.contains("bus") || combined.contains("xe khách") || combined.contains("xe ")) {
            return BUS;
        }
        if (combined.contains("ship") || combined.contains("ferry") || combined.contains("phà")
                || combined.contains("tàu th") || combined.contains("thủy") || combined.contains("boat")) {
            return SHIP;
        }
        return PLANE;
    }

    public static String icon(String kind) {
        return switch (kind) {
            case BUS -> "bi-bus-front";
            case SHIP -> "bi-tsunami";
            default -> "bi-airplane";
        };
    }

    /** Icon giữa timeline (chiều đi/về). */
    public static String iconTimeline(String kind) {
        return switch (kind) {
            case BUS -> "bi-bus-front";
            case SHIP -> "bi-tsunami";
            default -> "bi-airplane-fill";
        };
    }

    public static String infoSectionTitle(String kind) {
        return switch (kind) {
            case BUS -> "THÔNG TIN CHUYẾN XE";
            case SHIP -> "THÔNG TIN CHUYẾN TÀU";
            default -> "THÔNG TIN CHUYẾN BAY";
        };
    }

    public static String infoSectionSubtitle(String kind) {
        return switch (kind) {
            case BUS -> "Giờ khởi hành và mã chuyến xe theo ngày bạn chọn";
            case SHIP -> "Giờ khởi hành và mã chuyến tàu theo ngày bạn chọn";
            default -> "Giờ bay và mã chuyến theo ngày bạn chọn";
        };
    }

    public static String tripCodeLabel(String kind) {
        return switch (kind) {
            case BUS -> "Mã chuyến xe";
            case SHIP -> "Mã chuyến tàu";
            default -> "Mã chuyến bay";
        };
    }

    public static String ticketLabel(String kind) {
        return switch (kind) {
            case BUS -> "Vé xe";
            case SHIP -> "Vé tàu";
            default -> "Vé máy bay";
        };
    }

    public static String ticketLabelOutbound(String kind) {
        return ticketLabel(kind) + " (đi)";
    }

    public static String ticketLabelReturn(String kind) {
        return ticketLabel(kind) + " (về)";
    }

    public static String totalTicketPriceLabel(String kind) {
        return switch (kind) {
            case BUS -> "TỔNG GIÁ VÉ XE";
            case SHIP -> "TỔNG GIÁ VÉ TÀU";
            default -> "TỔNG GIÁ VÉ MÁY BAY";
        };
    }

    public static String outboundTripLabel(String kind) {
        return switch (kind) {
            case BUS -> "Chuyến xe đi";
            case SHIP -> "Chuyến tàu đi";
            default -> "CB đi";
        };
    }

    public static String returnTripLabel(String kind) {
        return switch (kind) {
            case BUS -> "Chuyến xe về";
            case SHIP -> "Chuyến tàu về";
            default -> "CB về";
        };
    }

    public static boolean showAirportCodes(String kind) {
        return PLANE.equals(kind);
    }
}
