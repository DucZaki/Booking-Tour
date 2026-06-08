package edu.bookingtour.service;

import edu.bookingtour.entity.CheckInStatus;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.entity.TrangThaiDoan;
import edu.bookingtour.repo.DatChoRepository;
import edu.bookingtour.repo.NgayKhoiHanhRepository;
import edu.bookingtour.repo.NguoiDungRepository;
import edu.bookingtour.util.DepartureStatusUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TourManifestService {

    private final NgayKhoiHanhRepository ngayKhoiHanhRepository;
    private final DatChoRepository datChoRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final EmailService emailService;

    public TourManifestService(NgayKhoiHanhRepository ngayKhoiHanhRepository,
            DatChoRepository datChoRepository,
            NguoiDungRepository nguoiDungRepository,
            EmailService emailService) {
        this.ngayKhoiHanhRepository = ngayKhoiHanhRepository;
        this.datChoRepository = datChoRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.emailService = emailService;
    }

    public Optional<NguoiDung> findGuideUser(String username) {
        return nguoiDungRepository.findByTenDangNhap(username);
    }

    public List<NguoiDung> listGuides() {
        return nguoiDungRepository.findByVaiTroOrderByHoTenAsc("GUIDE");
    }

    public List<NgayKhoiHanh> listDeparturesForGuide(NguoiDung guide, LocalDate from, LocalDate to) {
        Integer guideId = isAdmin(guide) ? null : guide.getId();
        return ngayKhoiHanhRepository.findDeparturesInRange(from, to, guideId);
    }

    public Optional<NgayKhoiHanh> getDeparture(Integer nkhId) {
        return ngayKhoiHanhRepository.findByIdWithDetails(nkhId);
    }

    public boolean canAccessDeparture(NguoiDung actor, NgayKhoiHanh nkh) {
        if (actor == null || nkh == null) {
            return false;
        }
        if (isAdmin(actor)) {
            return true;
        }
        if (!isGuide(actor)) {
            return false;
        }
        return nkh.getGuide() != null && actor.getId().equals(nkh.getGuide().getId());
    }

    public List<DatCho> manifest(NgayKhoiHanh nkh, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return datChoRepository.findPaidManifestByNgayKhoiHanh(nkh.getId());
        }
        return datChoRepository.searchPaidManifestByNgayKhoiHanh(nkh.getId(), keyword.trim());
    }

    public ManifestStats stats(List<DatCho> bookings) {
        int totalGuests = bookings.stream().mapToInt(b -> b.getSoLuong() != null ? b.getSoLuong() : 0).sum();
        int checkedIn = bookings.stream()
                .filter(b -> b.getCheckinStatusEnum() == CheckInStatus.CHECKED_IN
                        || b.getCheckinStatusEnum() == CheckInStatus.LATE)
                .mapToInt(b -> b.getSoLuong() != null ? b.getSoLuong() : 0)
                .sum();
        int pending = bookings.stream()
                .filter(b -> b.getCheckinStatusEnum() == CheckInStatus.PENDING)
                .mapToInt(b -> b.getSoLuong() != null ? b.getSoLuong() : 0)
                .sum();
        int absent = bookings.stream()
                .filter(b -> b.getCheckinStatusEnum() == CheckInStatus.NO_SHOW
                        || b.getCheckinStatusEnum() == CheckInStatus.CANCELLED_LAST_MINUTE)
                .mapToInt(b -> b.getSoLuong() != null ? b.getSoLuong() : 0)
                .sum();
        return new ManifestStats(bookings.size(), totalGuests, checkedIn, pending, absent);
    }

    @Transactional
    public void assignGuide(Integer nkhId, Integer guideId) {
        NgayKhoiHanh nkh = ngayKhoiHanhRepository.findById(nkhId)
                .orElseThrow(() -> new IllegalArgumentException("Ngày khởi hành không tồn tại"));
        if (guideId == null) {
            nkh.setGuide(null);
        } else {
            NguoiDung guide = nguoiDungRepository.findById(guideId)
                    .orElseThrow(() -> new IllegalArgumentException("HDV không tồn tại"));
            if (!"GUIDE".equals(guide.getVaiTro())) {
                throw new IllegalArgumentException("Tài khoản không phải HDV");
            }
            nkh.setGuide(guide);
        }
        ngayKhoiHanhRepository.save(nkh);
    }

    @Transactional
    public NgayKhoiHanh startDeparture(NguoiDung actor, Integer nkhId) {
        return updateDepartureStatus(actor, nkhId, TrangThaiDoan.IN_PROGRESS, false);
    }

    @Transactional
    public NgayKhoiHanh completeDeparture(NguoiDung actor, Integer nkhId, boolean confirmEarlyEnd) {
        return updateDepartureStatus(actor, nkhId, TrangThaiDoan.COMPLETED, confirmEarlyEnd);
    }

    @Transactional
    public NgayKhoiHanh updateDepartureStatus(NguoiDung actor, Integer nkhId, TrangThaiDoan status) {
        return updateDepartureStatus(actor, nkhId, status, false);
    }

    @Transactional
    public NgayKhoiHanh updateDepartureStatus(NguoiDung actor, Integer nkhId, TrangThaiDoan status,
            boolean confirmEarlyEnd) {
        NgayKhoiHanh nkh = ngayKhoiHanhRepository.findByIdWithDetails(nkhId)
                .orElseThrow(() -> new IllegalArgumentException("Ngày khởi hành không tồn tại"));
        if (!canAccessDeparture(actor, nkh)) {
            throw new IllegalArgumentException("Bạn không được phân công đoàn này");
        }
        TrangThaiDoan previous = nkh.getTrangThaiDoanEnum();
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        }
        DepartureStatusUtil.assertForwardTransition(previous, status);

        LocalDateTime now = LocalDateTime.now();
        if (status == TrangThaiDoan.COMPLETED && !DepartureStatusUtil.isPastEnd(nkh, now) && !confirmEarlyEnd) {
            throw new IllegalArgumentException(
                    "Theo lịch trình tour vẫn đang diễn ra. Vui lòng xác nhận lần nữa nếu bạn chắc chắn muốn kết thúc sớm.");
        }

        nkh.setTrangThaiDoanEnum(status);
        if (status == TrangThaiDoan.IN_PROGRESS && nkh.getThoiDiemBatDau() == null) {
            nkh.setThoiDiemBatDau(now);
        }
        NgayKhoiHanh saved = ngayKhoiHanhRepository.save(nkh);
        handleLifecycleNotices(saved, previous, status);
        return saved;
    }

    @Transactional
    public int autoStartDueDepartures() {
        LocalDateTime now = LocalDateTime.now();
        int started = 0;
        for (NgayKhoiHanh nkh : ngayKhoiHanhRepository.findScheduledReadyForAutoStart(now.toLocalDate())) {
            if (DepartureStatusUtil.isStartTimeReached(nkh, now)) {
                TrangThaiDoan previous = nkh.getTrangThaiDoanEnum();
                nkh.setTrangThaiDoanEnum(TrangThaiDoan.IN_PROGRESS);
                nkh.setThoiDiemBatDau(now);
                NgayKhoiHanh saved = ngayKhoiHanhRepository.save(nkh);
                handleLifecycleNotices(saved, previous, TrangThaiDoan.IN_PROGRESS);
                started++;
            }
        }
        return started;
    }

    @Transactional
    public int completeDueDepartures() {
        LocalDateTime now = LocalDateTime.now();
        int completed = 0;
        for (NgayKhoiHanh nkh : ngayKhoiHanhRepository.findInProgressDueForCompletion(now.toLocalDate())) {
            if (DepartureStatusUtil.isPastEnd(nkh, now)) {
                TrangThaiDoan previous = nkh.getTrangThaiDoanEnum();
                nkh.setTrangThaiDoanEnum(TrangThaiDoan.COMPLETED);
                NgayKhoiHanh saved = ngayKhoiHanhRepository.save(nkh);
                handleLifecycleNotices(saved, previous, TrangThaiDoan.COMPLETED);
                completed++;
            }
        }
        return completed;
    }

    private void handleLifecycleNotices(NgayKhoiHanh nkh, TrangThaiDoan previous, TrangThaiDoan current) {
        if (previous == current) {
            return;
        }
        if (current == TrangThaiDoan.IN_PROGRESS) {
            notifyTripStarted(nkh);
        } else if (current == TrangThaiDoan.COMPLETED) {
            notifyTripCompleted(nkh);
        }
    }

    private void notifyTripStarted(NgayKhoiHanh nkh) {
        for (DatCho booking : datChoRepository.findPaidManifestByNgayKhoiHanh(nkh.getId())) {
            CheckInStatus status = booking.getCheckinStatusEnum();
            boolean notCheckedIn = status != CheckInStatus.CHECKED_IN && status != CheckInStatus.LATE;
            if (notCheckedIn && booking.getTripStartedNoticeSentAt() == null) {
                if (emailService.sendTripStartedNotice(booking)) {
                    booking.setTripStartedNoticeSentAt(LocalDateTime.now());
                    datChoRepository.save(booking);
                }
            }
        }
    }

    private void notifyTripCompleted(NgayKhoiHanh nkh) {
        for (DatCho booking : datChoRepository.findPaidManifestByNgayKhoiHanh(nkh.getId())) {
            if (booking.getTripCompletedNoticeSentAt() == null) {
                if (emailService.sendTripCompletedNotice(booking)) {
                    booking.setTripCompletedNoticeSentAt(LocalDateTime.now());
                    datChoRepository.save(booking);
                }
            }
        }
    }

    @Transactional
    public void updateBookingNote(NguoiDung actor, Integer bookingId, String note) {
        DatCho booking = datChoRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking"));
        if (!canAccessDeparture(actor, booking.getIdNgayKhoiHanh())) {
            throw new IllegalArgumentException("Bạn không được phân công booking này");
        }
        booking.setGhiChu(note != null && !note.isBlank() ? note.trim() : null);
        datChoRepository.save(booking);
    }

    public static boolean isAdmin(NguoiDung user) {
        return user != null && "ADMIN".equals(user.getVaiTro());
    }

    public static boolean isGuide(NguoiDung user) {
        return user != null && "GUIDE".equals(user.getVaiTro());
    }

    public record ManifestStats(int bookingCount, int guestCount, int checkedIn, int pending, int absent) {
    }
}
