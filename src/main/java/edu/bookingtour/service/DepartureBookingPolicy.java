package edu.bookingtour.service;

import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.util.DepartureTimeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DepartureBookingPolicy {

    private static final DateTimeFormatter DEADLINE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Value("${app.booking.cutoff-hours-before-departure:24}")
    private int cutoffHours;

    public boolean isBookingOpen(NgayKhoiHanh nkh) {
        return nkh != null && nkh.getNgay() != null && LocalDateTime.now().isBefore(bookingDeadline(nkh));
    }

    public LocalDateTime gatheringDateTime(NgayKhoiHanh nkh) {
        if (nkh == null || nkh.getNgay() == null) {
            return LocalDateTime.now();
        }
        return DepartureTimeUtil.gatheringDateTime(nkh.getNgay(), nkh.getGioTapTrung());
    }

    public LocalDateTime bookingDeadline(NgayKhoiHanh nkh) {
        return gatheringDateTime(nkh).minusHours(cutoffHours);
    }

    public void assertBookingAllowed(NgayKhoiHanh nkh) {
        if (nkh == null || nkh.getNgay() == null) {
            throw new IllegalArgumentException("Ngày khởi hành không hợp lệ.");
        }
        if (!isBookingOpen(nkh)) {
            throw new IllegalArgumentException(closureMessage(nkh));
        }
    }

    public String closureMessage(NgayKhoiHanh nkh) {
        LocalDateTime deadline = bookingDeadline(nkh);
        LocalDateTime gather = gatheringDateTime(nkh);
        return "Chuyến đi đã đóng đặt vé. Vui lòng đặt trước ít nhất "
                + cutoffHours + " giờ so với giờ tập trung ("
                + DepartureTimeUtil.formatTime(gather.toLocalTime()) + " ngày "
                + gather.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + "). Hạn đặt vé: " + deadline.format(DEADLINE_FMT) + ".";
    }

    /** Sau giờ tập trung → muộn; trước hoặc đúng giờ → đúng giờ. */
    public boolean isLateArrival(NgayKhoiHanh nkh, LocalDateTime checkInAt) {
        if (nkh == null || checkInAt == null) {
            return false;
        }
        return checkInAt.isAfter(gatheringDateTime(nkh));
    }

    public int getCutoffHours() {
        return cutoffHours;
    }
}
