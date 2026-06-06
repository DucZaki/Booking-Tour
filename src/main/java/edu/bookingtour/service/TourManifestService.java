package edu.bookingtour.service;

import edu.bookingtour.entity.CheckInStatus;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.entity.TrangThaiDoan;
import edu.bookingtour.repo.DatChoRepository;
import edu.bookingtour.repo.NgayKhoiHanhRepository;
import edu.bookingtour.repo.NguoiDungRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TourManifestService {

    private final NgayKhoiHanhRepository ngayKhoiHanhRepository;
    private final DatChoRepository datChoRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public TourManifestService(NgayKhoiHanhRepository ngayKhoiHanhRepository,
            DatChoRepository datChoRepository,
            NguoiDungRepository nguoiDungRepository) {
        this.ngayKhoiHanhRepository = ngayKhoiHanhRepository;
        this.datChoRepository = datChoRepository;
        this.nguoiDungRepository = nguoiDungRepository;
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
        long checkedIn = bookings.stream()
                .filter(b -> b.getCheckinStatusEnum() == CheckInStatus.CHECKED_IN
                        || b.getCheckinStatusEnum() == CheckInStatus.LATE)
                .count();
        long pending = bookings.stream().filter(b -> b.getCheckinStatusEnum() == CheckInStatus.PENDING).count();
        long absent = bookings.stream()
                .filter(b -> b.getCheckinStatusEnum() == CheckInStatus.NO_SHOW
                        || b.getCheckinStatusEnum() == CheckInStatus.CANCELLED_LAST_MINUTE)
                .count();
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
    public NgayKhoiHanh updateDepartureStatus(NguoiDung actor, Integer nkhId, TrangThaiDoan status) {
        NgayKhoiHanh nkh = ngayKhoiHanhRepository.findByIdWithDetails(nkhId)
                .orElseThrow(() -> new IllegalArgumentException("Ngày khởi hành không tồn tại"));
        if (!canAccessDeparture(actor, nkh)) {
            throw new IllegalArgumentException("Bạn không được phân công đoàn này");
        }
        nkh.setTrangThaiDoanEnum(status);
        return ngayKhoiHanhRepository.save(nkh);
    }

    public static boolean isAdmin(NguoiDung user) {
        return user != null && "ADMIN".equals(user.getVaiTro());
    }

    public static boolean isGuide(NguoiDung user) {
        return user != null && "GUIDE".equals(user.getVaiTro());
    }

    public record ManifestStats(int bookingCount, int guestCount, long checkedIn, long pending, long absent) {
    }
}
