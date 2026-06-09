package edu.bookingtour.service;

import edu.bookingtour.entity.CheckInStatus;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.entity.TrangThaiDoan;
import edu.bookingtour.repo.DatChoRepository;
import edu.bookingtour.util.DepartureStatusUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
public class CheckInService {

    private static final DateTimeFormatter GATHER_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final DatChoRepository datChoRepository;
    private final TourManifestService tourManifestService;
    private final EmailService emailService;
    private final DepartureBookingPolicy departureBookingPolicy;

    public CheckInService(DatChoRepository datChoRepository, TourManifestService tourManifestService,
            EmailService emailService, DepartureBookingPolicy departureBookingPolicy) {
        this.datChoRepository = datChoRepository;
        this.tourManifestService = tourManifestService;
        this.emailService = emailService;
        this.departureBookingPolicy = departureBookingPolicy;
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
    public CheckInResult confirmCheckIn(String token, NguoiDung actor) {
        return updateStatusByToken(token, CheckInStatus.CHECKED_IN, actor);
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
        boolean arrival = status == CheckInStatus.CHECKED_IN || status == CheckInStatus.LATE;
        if (arrival) {
            CheckInResult guard = guardArrivalCheckIn(booking, actor);
            if (guard != null) {
                return guard;
            }
        }
        CheckInStatus previousStatus = booking.getCheckinStatusEnum();
        if (status == CheckInStatus.CHECKED_IN || status == CheckInStatus.LATE) {
            if (booking.getCheckinStatusEnum() == CheckInStatus.CHECKED_IN
                    && status == CheckInStatus.CHECKED_IN) {
                return CheckInResult.ofAlreadyCheckedIn(booking);
            }
            LocalDateTime checkInAt = booking.getCheckedInAt() != null ? booking.getCheckedInAt() : LocalDateTime.now();
            CheckInStatus resolved = resolveArrivalStatus(booking, status, checkInAt);
            booking.setCheckinStatusEnum(resolved);
            if (booking.getCheckedInAt() == null) {
                booking.setCheckedInAt(checkInAt);
            }
            datChoRepository.save(booking);
            if (previousStatus != CheckInStatus.CHECKED_IN && previousStatus != CheckInStatus.LATE) {
                emailService.sendCheckInSuccess(booking);
            }
            String msg = resolved == CheckInStatus.LATE ? "Đã ghi nhận khách đến muộn." : "Check-in thành công!";
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

    /**
     * Ràng buộc quét QR check-in khách:
     *  - Guide chỉ được check-in đoàn mình phụ trách.
     *  - Đoàn chưa đến giờ tập trung (còn "Sắp diễn ra") thì chưa được check-in.
     * Admin được bỏ qua để xử lý/sửa thủ công. Trả về null nếu hợp lệ.
     */
    private CheckInResult guardArrivalCheckIn(DatCho booking, NguoiDung actor) {
        if (actor == null || TourManifestService.isAdmin(actor)) {
            return null;
        }
        NgayKhoiHanh nkh = booking.getIdNgayKhoiHanh();
        if (nkh == null) {
            return null;
        }
        if (!tourManifestService.canAccessDeparture(actor, nkh)) {
            return CheckInResult.invalid("Vé thuộc đoàn bạn không phụ trách — không thể check-in.");
        }
        TrangThaiDoan effective = DepartureStatusUtil.effectiveStatus(nkh, LocalDateTime.now());
        if (effective == TrangThaiDoan.CANCELLED) {
            return CheckInResult.invalid("Đoàn đã bị hủy — không thể check-in.");
        }
        if (effective == TrangThaiDoan.SCHEDULED) {
            LocalDateTime start = DepartureStatusUtil.departureStartDateTime(nkh);
            String when = start != null ? " (" + GATHER_FMT.format(start) + ")" : "";
            return CheckInResult.invalid(
                    "Chưa đến giờ tập trung" + when + " — đoàn chưa diễn ra, chưa thể check-in.");
        }
        return null;
    }

    private CheckInStatus resolveArrivalStatus(DatCho booking, CheckInStatus requested, LocalDateTime checkInAt) {
        if (requested == CheckInStatus.LATE) {
            return CheckInStatus.LATE;
        }
        if (requested == CheckInStatus.CHECKED_IN && booking.getIdNgayKhoiHanh() != null
                && departureBookingPolicy.isLateArrival(booking.getIdNgayKhoiHanh(), checkInAt)) {
            return CheckInStatus.LATE;
        }
        return requested;
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
