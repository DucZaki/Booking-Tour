package edu.bookingtour.service;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.repo.DatChoRepository;
import edu.bookingtour.repo.NgayKhoiHanhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TourCapacityService {

    public static final List<String> HOLD_STATUSES = List.of("PENDING", "PAID");
    public static final int DEFAULT_CAPACITY = 50;
    public static final int MIN_CAPACITY = 1;
    public static final int MAX_CAPACITY = 500;

    @Autowired
    private NgayKhoiHanhRepository ngayKhoiHanhRepository;

    @Autowired
    private DatChoRepository datChoRepository;

  /** Dùng class (không dùng record) để Thymeleaf đọc được remaining/capacity/booked. */
    public static final class CapacitySnapshot {
        private final int capacity;
        private final int booked;
        private final int remaining;

        public CapacitySnapshot(int capacity, int booked, int remaining) {
            this.capacity = capacity;
            this.booked = booked;
            this.remaining = remaining;
        }

        public int getCapacity() {
            return capacity;
        }

        public int getBooked() {
            return booked;
        }

        public int getRemaining() {
            return remaining;
        }

        public boolean isSoldOut() {
            return remaining <= 0;
        }
    }

    public static class CapacityExceededException extends RuntimeException {
        private final int remaining;

        public CapacityExceededException(String message, int remaining) {
            super(message);
            this.remaining = remaining;
        }

        public int getRemaining() {
            return remaining;
        }
    }

    public static String formatRemainingMessage(int remaining) {
        if (remaining <= 0) {
            return "Rất tiếc Tour hiện tại đã hết chỗ.";
        }
        return "Rất tiếc Tour hiện tại số chỗ còn nhận chỉ còn: " + remaining + " chỗ";
    }

    public int resolveCapacity(NgayKhoiHanh nkh) {
        if (nkh == null) {
            return DEFAULT_CAPACITY;
        }
        if (nkh.getSucChua() != null && nkh.getSucChua() >= MIN_CAPACITY) {
            return Math.min(nkh.getSucChua(), MAX_CAPACITY);
        }
        if (nkh.getChuyenDi() != null
                && nkh.getChuyenDi().getSucChuaMacDinh() != null
                && nkh.getChuyenDi().getSucChuaMacDinh() >= MIN_CAPACITY) {
            return Math.min(nkh.getChuyenDi().getSucChuaMacDinh(), MAX_CAPACITY);
        }
        return DEFAULT_CAPACITY;
    }

    public int getBookedGuests(Integer nkhId) {
        if (nkhId == null) {
            return 0;
        }
        Integer sum = datChoRepository.sumGuestsByNgayKhoiHanhAndStatuses(nkhId, HOLD_STATUSES);
        return sum == null ? 0 : sum;
    }

    @Transactional(readOnly = true)
    public CapacitySnapshot getSnapshot(Integer nkhId) {
        return ngayKhoiHanhRepository.findByIdWithChuyenDi(nkhId)
                .map(this::buildSnapshot)
                .orElse(new CapacitySnapshot(DEFAULT_CAPACITY, 0, DEFAULT_CAPACITY));
    }

    @Transactional(readOnly = true)
    public CapacitySnapshot buildSnapshot(NgayKhoiHanh nkh) {
        if (nkh == null || nkh.getId() == null) {
            return new CapacitySnapshot(DEFAULT_CAPACITY, 0, DEFAULT_CAPACITY);
        }
        int capacity = resolveCapacity(nkh);
        int booked = getBookedGuests(nkh.getId());
        int remaining = Math.max(0, capacity - booked);
        return new CapacitySnapshot(capacity, booked, remaining);
    }

    @Transactional(readOnly = true)
    public Map<Integer, CapacitySnapshot> snapshotsForDepartures(List<NgayKhoiHanh> departures) {
        if (departures == null || departures.isEmpty()) {
            return Map.of();
        }
        return departures.stream()
                .filter(n -> n.getId() != null)
                .collect(Collectors.toMap(NgayKhoiHanh::getId, this::buildSnapshot, (a, b) -> a));
    }

    @Transactional
    public void assertCanBook(Integer nkhId, int requestedGuests) {
        if (requestedGuests <= 0) {
            throw new CapacityExceededException("Vui lòng chọn số lượng hành khách hợp lệ.", 0);
        }
        NgayKhoiHanh nkh = ngayKhoiHanhRepository.findByIdForUpdate(nkhId)
                .orElseThrow(() -> new CapacityExceededException("Ngày khởi hành không tồn tại.", 0));
        CapacitySnapshot snap = buildSnapshot(nkh);
        if (requestedGuests > snap.getRemaining()) {
            throw new CapacityExceededException(formatRemainingMessage(snap.getRemaining()), snap.getRemaining());
        }
    }

    @Transactional
    public void updateDepartureCapacity(Integer nkhId, Integer newCapacity) {
        if (newCapacity == null || newCapacity < MIN_CAPACITY || newCapacity > MAX_CAPACITY) {
            throw new IllegalArgumentException("Sức chứa phải từ " + MIN_CAPACITY + " đến " + MAX_CAPACITY + ".");
        }
        NgayKhoiHanh nkh = ngayKhoiHanhRepository.findById(nkhId)
                .orElseThrow(() -> new RuntimeException("Ngày khởi hành không tồn tại"));
        int booked = getBookedGuests(nkhId);
        if (newCapacity < booked) {
            throw new IllegalArgumentException(
                    "Không thể đặt sức chứa " + newCapacity + " vì đã có " + booked + " khách đặt/giữ chỗ.");
        }
        nkh.setSucChua(newCapacity);
        ngayKhoiHanhRepository.save(nkh);
    }

    public void applyDefaultCapacity(NgayKhoiHanh nkh, ChuyenDi tour) {
        int cap = DEFAULT_CAPACITY;
        if (tour != null && tour.getSucChuaMacDinh() != null && tour.getSucChuaMacDinh() >= MIN_CAPACITY) {
            cap = Math.min(tour.getSucChuaMacDinh(), MAX_CAPACITY);
        }
        nkh.setSucChua(cap);
    }
}
