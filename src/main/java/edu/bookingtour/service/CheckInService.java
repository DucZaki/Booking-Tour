package edu.bookingtour.service;

import edu.bookingtour.entity.CheckInStatus;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.repo.DatChoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CheckInService {

    private final DatChoRepository datChoRepository;
    private final TourManifestService tourManifestService;

    public CheckInService(DatChoRepository datChoRepository, TourManifestService tourManifestService) {
        this.datChoRepository = datChoRepository;
        this.tourManifestService = tourManifestService;
    }

    public Optional<DatCho> findByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return datChoRepository.findByMaCheckInWithDetails(token.trim());
    }

    public Optional<DatCho> findById(Integer id) {
        return datChoRepository.findByIdWithDetails(id);
    }

    /** Gán token nếu đơn chưa có (đơn cũ trước migration). */
    @Transactional
    public String ensureCheckInToken(DatCho booking) {
        if (booking.getMaCheckIn() != null && !booking.getMaCheckIn().isBlank()) {
            return booking.getMaCheckIn();
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        booking.setMaCheckIn(token);
        datChoRepository.save(booking);
        return token;
    }

    @Transactional
    public CheckInResult confirmCheckIn(String token) {
        return updateStatusByToken(token, CheckInStatus.CHECKED_IN, null);
    }

    @Transactional
    public CheckInResult updateStatusByToken(String token, CheckInStatus status, NguoiDung actor) {
        DatCho booking = datChoRepository.findByMaCheckInWithDetails(token.trim()).orElse(null);
        if (booking == null) {
            return CheckInResult.ofNotFound();
        }
        return applyStatus(booking, status, actor);
    }

    @Transactional
    public CheckInResult updateStatusByBookingId(Integer bookingId, CheckInStatus status, NguoiDung actor) {
        DatCho booking = datChoRepository.findByIdWithDetails(bookingId).orElse(null);
        if (booking == null) {
            return CheckInResult.ofNotFound();
        }
        if (booking.getIdNgayKhoiHanh() != null
                && actor != null
                && !tourManifestService.canAccessDeparture(actor, booking.getIdNgayKhoiHanh())) {
            return CheckInResult.invalid("Bạn không được phân công đoàn này.");
        }
        return applyStatus(booking, status, actor);
    }

    private CheckInResult applyStatus(DatCho booking, CheckInStatus status, NguoiDung actor) {
        if (!"PAID".equals(booking.getTrangThai())) {
            return CheckInResult.invalid("Đơn chưa thanh toán — không thể check-in.");
        }
        if (status == CheckInStatus.CHECKED_IN || status == CheckInStatus.LATE) {
            if (booking.getCheckinStatusEnum() == CheckInStatus.CHECKED_IN
                    && status == CheckInStatus.CHECKED_IN) {
                return CheckInResult.ofAlreadyCheckedIn(booking);
            }
            booking.setCheckinStatusEnum(status);
            if (booking.getCheckedInAt() == null) {
                booking.setCheckedInAt(LocalDateTime.now());
            }
            datChoRepository.save(booking);
            String msg = status == CheckInStatus.LATE ? "Đã ghi nhận khách đến muộn." : "Check-in thành công!";
            return CheckInResult.ofSuccess(booking, msg);
        }
        if (status == CheckInStatus.NO_SHOW || status == CheckInStatus.CANCELLED_LAST_MINUTE) {
            booking.setCheckinStatusEnum(status);
            datChoRepository.save(booking);
            return CheckInResult.ofSuccess(booking, status.getLabel() + " — đã cập nhật.");
        }
        if (status == CheckInStatus.PENDING) {
            booking.setCheckinStatusEnum(CheckInStatus.PENDING);
            booking.setCheckedInAt(null);
            datChoRepository.save(booking);
            return CheckInResult.ofSuccess(booking, "Đã đặt lại trạng thái chưa đến.");
        }
        return CheckInResult.invalid("Trạng thái không hợp lệ.");
    }

    public record CheckInResult(
            boolean ok,
            boolean notFound,
            boolean alreadyCheckedIn,
            String message,
            DatCho booking) {

        static CheckInResult ofNotFound() {
            return new CheckInResult(false, true, false, "Không tìm thấy vé.", null);
        }

        static CheckInResult invalid(String msg) {
            return new CheckInResult(false, false, false, msg, null);
        }

        static CheckInResult ofAlreadyCheckedIn(DatCho b) {
            return new CheckInResult(false, false, true, "Khách đã check-in trước đó.", b);
        }

        static CheckInResult ofSuccess(DatCho b, String msg) {
            return new CheckInResult(true, false, false, msg, b);
        }

        static CheckInResult ofSuccess(DatCho b) {
            return ofSuccess(b, "Check-in thành công!");
        }
    }
}
