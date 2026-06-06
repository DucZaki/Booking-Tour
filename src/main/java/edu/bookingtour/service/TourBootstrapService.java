package edu.bookingtour.service;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.repo.NgayKhoiHanhRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Kích hoạt hàng loạt tour (dev): gia hạn ngày tour + tối đa 3 ngày KH/tháng
 * cho tour chưa có ngày KH nào trong tháng hiện tại.
 */
@Service
public class TourBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(TourBootstrapService.class);
    private static final int DEPARTURES_PER_TOUR = 3;

    private final ChuyenDiRepository chuyenDiRepository;
    private final NgayKhoiHanhRepository ngayKhoiHanhRepository;
    private final NgayKhoiHanhService ngayKhoiHanhService;

    public TourBootstrapService(ChuyenDiRepository chuyenDiRepository,
            NgayKhoiHanhRepository ngayKhoiHanhRepository,
            NgayKhoiHanhService ngayKhoiHanhService) {
        this.chuyenDiRepository = chuyenDiRepository;
        this.ngayKhoiHanhRepository = ngayKhoiHanhRepository;
        this.ngayKhoiHanhService = ngayKhoiHanhService;
    }

    @Transactional
    public BootstrapResult activateAllToursForCurrentMonth() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        int toursExtended = 0;
        int departuresAdded = 0;

        List<ChuyenDi> tours = chuyenDiRepository.findAll();
        for (ChuyenDi tour : tours) {
            long tripDays = resolveTripDays(tour);

            Set<LocalDate> existingDates = ngayKhoiHanhRepository
                    .findByChuyenDiIdAndThangAndNam(tour.getId(), month, year)
                    .stream()
                    .map(NgayKhoiHanh::getNgay)
                    .collect(Collectors.toSet());

            // Đã có ngày KH trong tháng → không seed thêm (tránh cộng dồn mỗi lần restart app)
            if (!existingDates.isEmpty()) {
                continue;
            }

            extendTourLikeAdmin(tour, today, monthEnd);
            toursExtended++;

            List<LocalDate> targetDates = pickDepartureDates(today, monthEnd, DEPARTURES_PER_TOUR);

            for (LocalDate ngayDi : targetDates) {
                if (existingDates.contains(ngayDi)) {
                    continue;
                }
                LocalDate ngayVe = computeReturnDate(ngayDi, monthEnd, tripDays);
                ngayKhoiHanhService.addDepartureDateWithDefaults(tour.getId(), ngayDi, ngayVe);
                departuresAdded++;
            }
        }

        BootstrapResult result = new BootstrapResult(tours.size(), toursExtended, departuresAdded, month, year);
        log.info("Tour bootstrap: {} tour, gia hạn {}, thêm {} ngày KH (T{}/{})",
                result.totalTours(), result.toursExtended(), result.departuresAdded(), month, year);
        return result;
    }

    /** Giống POST /admin/tour/extend — kéo tour về còn hiệu lực trong tháng này. */
    private void extendTourLikeAdmin(ChuyenDi tour, LocalDate today, LocalDate monthEnd) {
        tour.setNgayKhoiHanh(today);
        tour.setNgayKetThuc(monthEnd);
        chuyenDiRepository.save(tour);
    }

    private long resolveTripDays(ChuyenDi tour) {
        if (tour.getNgayKhoiHanh() != null && tour.getNgayKetThuc() != null) {
            long span = ChronoUnit.DAYS.between(tour.getNgayKhoiHanh(), tour.getNgayKetThuc());
            if (span >= 1) {
                return span;
            }
        }
        return 2;
    }

    private LocalDate computeReturnDate(LocalDate ngayDi, LocalDate monthEnd, long tripDays) {
        LocalDate ngayVe = ngayDi.plusDays(tripDays);
        return ngayVe.isAfter(monthEnd) ? monthEnd : ngayVe;
    }

    /** 3 ngày cách đều trong phần còn lại của tháng (từ hôm nay trở đi). */
    static List<LocalDate> pickDepartureDates(LocalDate today, LocalDate monthEnd, int count) {
        List<LocalDate> dates = new ArrayList<>();
        if (today.isAfter(monthEnd)) {
            return dates;
        }
        long remaining = ChronoUnit.DAYS.between(today, monthEnd);
        if (remaining <= 0) {
            dates.add(today);
            return dates;
        }
        long step = Math.max(1, remaining / Math.max(1, count));
        LocalDate cursor = today;
        while (dates.size() < count && !cursor.isAfter(monthEnd)) {
            dates.add(cursor);
            cursor = cursor.plusDays(step);
        }
        while (dates.size() < count) {
            LocalDate last = dates.get(dates.size() - 1);
            LocalDate next = last.plusDays(1);
            if (next.isAfter(monthEnd)) {
                break;
            }
            dates.add(next);
        }
        return dates;
    }

    public record BootstrapResult(int totalTours, int toursExtended, int departuresAdded, int month, int year) {
    }
}
