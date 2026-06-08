package edu.bookingtour.service;

import edu.bookingtour.dto.PromoApplyResult;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.MaGiamGia;
import edu.bookingtour.repo.DatChoRepository;
import edu.bookingtour.repo.MaGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaGiamGiaService {

    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;

    @Autowired
    private DatChoRepository datChoRepository;

    @Autowired
    private TourService tourService;

    public Page<MaGiamGia> findAll(int page, int size) {
        return maGiamGiaRepository.findAll(PageRequest.of(page, size));
    }

    public List<MaGiamGia> findAllActive(LocalDate today) {
        return maGiamGiaRepository.findActivePromos(today);
    }

    public Optional<MaGiamGia> findById(Integer id) {
        return maGiamGiaRepository.findById(id);
    }

    @Transactional
    public MaGiamGia save(MaGiamGia entity, List<Integer> selectedTourIds) {
        normalize(entity, selectedTourIds);
        if (entity.getId() == null && maGiamGiaRepository.existsByMaIgnoreCase(entity.getMa())) {
            throw new IllegalArgumentException("Mã giảm giá đã tồn tại");
        }
        if (entity.getId() != null && maGiamGiaRepository.existsByMaIgnoreCaseAndIdNot(entity.getMa(), entity.getId())) {
            throw new IllegalArgumentException("Mã giảm giá đã tồn tại");
        }
        if (entity.getSoLanDaDung() == null) {
            entity.setSoLanDaDung(0);
        }
        return maGiamGiaRepository.save(entity);
    }

    @Transactional
    public void delete(Integer id) {
        maGiamGiaRepository.deleteById(id);
    }

    @Transactional
    public boolean consumeUsage(Integer promoId) {
        return maGiamGiaRepository.incrementUsage(promoId) > 0;
    }

    public PromoApplyResult validateAndApply(String maCode, int tourId, double unitPricePerGuest, int soLuong) {
        double subtotal = unitPricePerGuest * soLuong;
        return validateAndApplyOnSubtotal(maCode, tourId, unitPricePerGuest, subtotal, null, null);
    }

    public PromoApplyResult validateAndApplyOnSubtotal(String maCode,
                                                       int tourId,
                                                       double referenceUnitPrice,
                                                       double subtotal) {
        return validateAndApplyOnSubtotal(maCode, tourId, referenceUnitPrice, subtotal, null, null);
    }

    public PromoApplyResult validateAndApplyOnSubtotal(String maCode,
                                                       int tourId,
                                                       double referenceUnitPrice,
                                                       double subtotal,
                                                       Integer userId,
                                                       LocalDate ngayKhoiHanh) {
        if (maCode == null || maCode.isBlank()) {
            return PromoApplyResult.invalid("Vui lòng nhập mã giảm giá");
        }
        MaGiamGia promo = maGiamGiaRepository.findByMaIgnoreCase(maCode.trim()).orElse(null);
        if (promo == null) {
            return PromoApplyResult.invalid("Mã giảm giá không tồn tại");
        }
        String ruleError = validateRules(promo, tourId, referenceUnitPrice, subtotal, userId, ngayKhoiHanh);
        if (ruleError != null) {
            return PromoApplyResult.invalid(ruleError);
        }
        double discount = calculateDiscount(promo, subtotal);
        if (discount <= 0) {
            return PromoApplyResult.invalid("Mã giảm giá không áp dụng được cho đơn này");
        }
        double total = Math.max(0, subtotal - discount);
        return PromoApplyResult.ok(promo, subtotal, discount, total);
    }

    public boolean isActive(MaGiamGia m, LocalDate today) {
        if (!Boolean.TRUE.equals(m.getActive())) {
            return false;
        }
        if (m.getNgayBatDau() != null && today.isBefore(m.getNgayBatDau())) {
            return false;
        }
        if (m.getNgayKetThuc() != null && today.isAfter(m.getNgayKetThuc())) {
            return false;
        }
        if (m.getSoLanDungToiDa() != null && m.getSoLanDaDung() != null
                && m.getSoLanDaDung() >= m.getSoLanDungToiDa()) {
            return false;
        }
        return true;
    }

    public boolean appliesToTour(MaGiamGia m, int tourId) {
        if (Boolean.TRUE.equals(m.getApDungTatCa())) {
            return true;
        }
        return m.getTourIds() != null && m.getTourIds().contains(tourId);
    }

    public double calculateDiscount(MaGiamGia m, double subtotal) {
        BigDecimal val = m.resolvedGiaTri();
        BigDecimal subtotalBd = BigDecimal.valueOf(subtotal);
        BigDecimal raw;
        if (m.isAmountType()) {
            raw = val;
        } else {
            raw = subtotalBd.multiply(val)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (m.getGiamToiDa() != null && raw.compareTo(m.getGiamToiDa()) > 0) {
                raw = m.getGiamToiDa();
            }
        }
        return raw.min(subtotalBd).max(BigDecimal.ZERO)
                .setScale(0, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public List<ChuyenDi> findToursWithActivePromo(LocalDate today) {
        List<MaGiamGia> active = findAllActive(today);
        if (active.isEmpty()) {
            return List.of();
        }
        boolean hasGlobal = active.stream().anyMatch(m -> Boolean.TRUE.equals(m.getApDungTatCa()));
        if (hasGlobal) {
            return tourService.findAll();
        }
        Set<Integer> ids = active.stream()
                .flatMap(m -> m.getTourIds() != null ? m.getTourIds().stream() : java.util.stream.Stream.empty())
                .collect(Collectors.toSet());
        return tourService.findAll().stream()
                .filter(t -> ids.contains(t.getId()))
                .toList();
    }

    public String formatPromoForChat(MaGiamGia m) {
        String loai = m.isAmountType()
                ? "giảm " + formatMoney(m.resolvedGiaTri().doubleValue()) + " VND"
                : "giảm " + m.resolvedGiaTri().intValue() + "%";
        String scope = Boolean.TRUE.equals(m.getApDungTatCa()) ? "tất cả tour" : "một số tour chỉ định";
        String min = m.getDonToiThieu() != null
                ? " | đơn từ " + formatMoney(m.getDonToiThieu().doubleValue()) + " VND"
                : (m.getGiaToiThieu() != null
                ? " | giá/khách từ " + formatMoney(m.getGiaToiThieu().doubleValue()) + " VND"
                : "");
        return String.format("**%s**: %s (%s)%s", m.getMa(), loai, scope, min);
    }

    private String validateRules(MaGiamGia promo,
                                 int tourId,
                                 double referenceUnitPrice,
                                 double subtotal,
                                 Integer userId,
                                 LocalDate ngayKhoiHanh) {
        if (!isActive(promo, LocalDate.now())) {
            return "Mã giảm giá đã hết hạn hoặc chưa có hiệu lực";
        }
        if (!appliesToTour(promo, tourId)) {
            return "Mã không áp dụng cho tour này";
        }
        if (promo.getDonToiThieu() != null
                && BigDecimal.valueOf(subtotal).compareTo(promo.getDonToiThieu()) < 0) {
            return "Mã này chỉ áp dụng cho đơn hàng từ " + formatMoney(promo.getDonToiThieu().doubleValue()) + " VND";
        }
        if (promo.getGiaToiThieu() != null
                && BigDecimal.valueOf(referenceUnitPrice).compareTo(promo.getGiaToiThieu()) < 0) {
            return "Tour chưa đủ điều kiện giá tối thiểu ("
                    + formatMoney(promo.getGiaToiThieu().doubleValue()) + " VND/khách)";
        }
        String campaignErr = validateCampaignType(promo, ngayKhoiHanh);
        if (campaignErr != null) {
            return campaignErr;
        }
        if (userId != null && promo.getGioiHanMoiUser() != null && promo.getGioiHanMoiUser() > 0) {
            long used = datChoRepository.countPromoUsageByUser(userId, promo.getId());
            if (used >= promo.getGioiHanMoiUser()) {
                if (promo.getGioiHanMoiUser() == 1) {
                    return "Bạn đã sử dụng mã này rồi";
                }
                return "Mỗi tài khoản chỉ được dùng mã này tối đa " + promo.getGioiHanMoiUser() + " lần";
            }
        }
        return null;
    }

    private String validateCampaignType(MaGiamGia promo, LocalDate ngayKhoiHanh) {
        String kieu = promo.getKieuChienDich() != null ? promo.getKieuChienDich() : MaGiamGia.KIEU_STANDARD;
        if (MaGiamGia.KIEU_STANDARD.equalsIgnoreCase(kieu)) {
            return null;
        }
        if (ngayKhoiHanh == null) {
            return "Thiếu thông tin ngày khởi hành để kiểm tra mã";
        }
        if (MaGiamGia.KIEU_EARLY_BIRD.equalsIgnoreCase(kieu)) {
            if (promo.getSoNgayDatTruoc() == null || promo.getSoNgayDatTruoc() <= 0) {
                return "Mã Early Bird chưa được cấu hình đúng";
            }
            long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), ngayKhoiHanh);
            if (daysUntil < promo.getSoNgayDatTruoc()) {
                return "Mã Early Bird chỉ áp dụng khi đặt trước ít nhất "
                        + promo.getSoNgayDatTruoc() + " ngày so với ngày khởi hành";
            }
            return null;
        }
        if (MaGiamGia.KIEU_LAST_MINUTE.equalsIgnoreCase(kieu)) {
            int hoursLimit = promo.getSoGioLastMinute() != null ? promo.getSoGioLastMinute() : 48;
            LocalDateTime departure = ngayKhoiHanh.atStartOfDay();
            long hoursUntil = ChronoUnit.HOURS.between(LocalDateTime.now(), departure);
            if (hoursUntil < 0) {
                return "Tour đã khởi hành, không thể dùng mã Last-minute";
            }
            if (hoursUntil > hoursLimit) {
                return "Mã Last-minute chỉ áp dụng cho tour khởi hành trong vòng " + hoursLimit + " giờ tới";
            }
            return null;
        }
        return null;
    }

    private void normalize(MaGiamGia entity, List<Integer> selectedTourIds) {
        entity.setMa(entity.getMa() != null ? entity.getMa().trim().toUpperCase() : null);
        if (entity.getLoaiGiam() != null && MaGiamGia.LOAI_SO_TIEN.equalsIgnoreCase(entity.getLoaiGiam())) {
            entity.setLoaiGiam(MaGiamGia.LOAI_AMOUNT);
        }
        boolean applyAll = selectedTourIds == null || selectedTourIds.isEmpty();
        entity.setApDungTatCa(applyAll);
        if (applyAll) {
            entity.setTourIds(new HashSet<>());
        } else {
            entity.setTourIds(new HashSet<>(selectedTourIds));
        }
        if (MaGiamGia.LOAI_PHAN_TRAM.equals(entity.getLoaiGiam())) {
            entity.setPhanTramGiam(entity.getGiaTriGiam() != null ? entity.getGiaTriGiam().intValue() : null);
        }
        if (entity.getKieuChienDich() == null || entity.getKieuChienDich().isBlank()) {
            entity.setKieuChienDich(MaGiamGia.KIEU_STANDARD);
        }
        if (!MaGiamGia.KIEU_EARLY_BIRD.equalsIgnoreCase(entity.getKieuChienDich())) {
            entity.setSoNgayDatTruoc(null);
        }
        if (!MaGiamGia.KIEU_LAST_MINUTE.equalsIgnoreCase(entity.getKieuChienDich())) {
            entity.setSoGioLastMinute(null);
        }
        if (entity.getActive() == null) {
            entity.setActive(true);
        }
        if (entity.getNgayBatDau() == null) {
            entity.setNgayBatDau(LocalDate.now());
        }
        if (entity.getNgayKetThuc() == null) {
            entity.setNgayKetThuc(LocalDate.now().plusMonths(3));
        }
    }

    private static String formatMoney(double v) {
        return String.format("%,.0f", v);
    }
}
