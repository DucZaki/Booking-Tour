package edu.bookingtour.util;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.entity.TrangThaiDoan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Trạng thái vận hành đoàn: chỉ tiến (SCHEDULED → IN_PROGRESS → COMPLETED).
 */
public final class DepartureStatusUtil {

    private DepartureStatusUtil() {
    }

    /** Giờ tập trung ngày khởi hành — mốc bắt đầu tự động. */
    public static LocalDateTime departureStartDateTime(NgayKhoiHanh nkh) {
        if (nkh == null || nkh.getNgay() == null) {
            return null;
        }
        return nkh.getGatheringDateTime();
    }

    /** Ngày/giờ kết thúc dự kiến (ngày về + giờ đến, hoặc ngày KH tour). */
    public static LocalDateTime departureEndDateTime(NgayKhoiHanh nkh) {
        if (nkh == null) {
            return null;
        }
        LocalDate endDate = nkh.getNgayVe();
        if (endDate == null) {
            ChuyenDi tour = nkh.getChuyenDi();
            if (tour != null && tour.getNgayKetThuc() != null) {
                endDate = tour.getNgayKetThuc();
            }
        }
        if (endDate == null) {
            endDate = nkh.getNgay();
        }
        if (endDate == null) {
            return null;
        }
        LocalTime endTime = DepartureTimeUtil.parseTimeOrNull(nkh.getGioDenVe());
        if (endTime == null) {
            endTime = LocalTime.of(23, 59);
        }
        return LocalDateTime.of(endDate, endTime);
    }

    public static boolean isPastEnd(NgayKhoiHanh nkh, LocalDateTime now) {
        LocalDateTime end = departureEndDateTime(nkh);
        return end != null && !end.isAfter(now);
    }

    public static boolean isStartTimeReached(NgayKhoiHanh nkh, LocalDateTime now) {
        LocalDateTime start = departureStartDateTime(nkh);
        return start != null && !now.isBefore(start);
    }

    /** Trạng thái hiển thị — đồng bộ DB và mốc thời gian. */
    public static TrangThaiDoan effectiveStatus(NgayKhoiHanh nkh, LocalDateTime now) {
        if (nkh == null) {
            return TrangThaiDoan.SCHEDULED;
        }
        TrangThaiDoan stored = nkh.getTrangThaiDoanEnum();
        if (stored == TrangThaiDoan.COMPLETED || isPastEnd(nkh, now)) {
            return TrangThaiDoan.COMPLETED;
        }
        if (stored == TrangThaiDoan.IN_PROGRESS) {
            return TrangThaiDoan.IN_PROGRESS;
        }
        if (isStartTimeReached(nkh, now)) {
            return TrangThaiDoan.IN_PROGRESS;
        }
        return TrangThaiDoan.SCHEDULED;
    }

    public static String badgeClass(TrangThaiDoan status) {
        if (status == null) {
            return "bg-warning text-dark";
        }
        return switch (status) {
            case COMPLETED -> "bg-secondary";
            case IN_PROGRESS -> "bg-success";
            case SCHEDULED -> "bg-warning text-dark";
        };
    }

    public static void assertForwardTransition(TrangThaiDoan from, TrangThaiDoan to) {
        if (from == to) {
            return;
        }
        if (from == TrangThaiDoan.SCHEDULED && to == TrangThaiDoan.IN_PROGRESS) {
            return;
        }
        if (from == TrangThaiDoan.IN_PROGRESS && to == TrangThaiDoan.COMPLETED) {
            return;
        }
        throw new IllegalArgumentException("Không thể chuyển từ \"" + from.getLabel() + "\" sang \"" + to.getLabel()
                + "\". Trạng thái đoàn chỉ được cập nhật theo chiều tiến.");
    }
}
