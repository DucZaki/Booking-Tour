package edu.bookingtour.util;

import edu.bookingtour.entity.CheckInStatus;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.entity.TrangThaiDoan;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component("bookingView")
public class BookingViewHelper {

    public boolean hasTripStatus(DatCho booking) {
        return resolveTripOutcome(booking).isPresent();
    }

    public String tripStatusLabel(DatCho booking) {
        return resolveTripOutcome(booking).map(TripOutcome::label).orElse(null);
    }

    public String tripStatusBadgeClass(DatCho booking) {
        return resolveTripOutcome(booking).map(TripOutcome::badgeClass).orElse(null);
    }

    public static Optional<TripOutcome> resolveTripOutcome(DatCho booking) {
        if (booking == null || !"PAID".equals(booking.getTrangThai())) {
            return Optional.empty();
        }
        NgayKhoiHanh nkh = booking.getIdNgayKhoiHanh();
        if (nkh == null || nkh.getNgay() == null) {
            return Optional.empty();
        }
        CheckInStatus checkIn = booking.getCheckinStatusEnum();
        TrangThaiDoan tripStatus = nkh.getTrangThaiDoanEnum();
        if (tripStatus == TrangThaiDoan.COMPLETED) {
            return Optional.of(TripOutcome.COMPLETED);
        }
        if (tripStatus == TrangThaiDoan.IN_PROGRESS) {
            if (checkIn == CheckInStatus.CHECKED_IN || checkIn == CheckInStatus.LATE) {
                return Optional.of(TripOutcome.DEPARTED);
            }
            return Optional.of(TripOutcome.OVERDUE);
        }
        LocalDate today = LocalDate.now();
        if (!nkh.getNgay().isBefore(today)) {
            return Optional.empty();
        }
        if (checkIn == CheckInStatus.CHECKED_IN || checkIn == CheckInStatus.LATE) {
            LocalDate tripEnd = resolveTripEndDate(booking, nkh);
            if (tripEnd != null && !tripEnd.isAfter(today)) {
                return Optional.of(TripOutcome.COMPLETED);
            }
            return Optional.of(TripOutcome.DEPARTED);
        }
        return Optional.of(TripOutcome.OVERDUE);
    }

    /** Ngày kết thúc chuyến: ưu tiên ngày về trên ngày KH, rồi ngày kết thúc tour. */
    static LocalDate resolveTripEndDate(DatCho booking, NgayKhoiHanh nkh) {
        if (nkh.getNgayVe() != null) {
            return nkh.getNgayVe();
        }
        ChuyenDi tour = booking.getIdChuyenDi();
        if (tour != null && tour.getNgayKetThuc() != null) {
            return tour.getNgayKetThuc();
        }
        return nkh.getNgay();
    }

    public enum TripOutcome {
        OVERDUE("Đã quá hạn", "bg-secondary"),
        DEPARTED("Đã đi rồi", "bg-info text-dark"),
        COMPLETED("Đã hoàn thành", "bg-success");

        private final String label;
        private final String badgeClass;

        TripOutcome(String label, String badgeClass) {
            this.label = label;
            this.badgeClass = badgeClass;
        }

        public String label() {
            return label;
        }

        public String badgeClass() {
            return badgeClass;
        }
    }
}
