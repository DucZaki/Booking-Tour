package edu.bookingtour.service;

import edu.bookingtour.dto.PromoApplyResult;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.MaGiamGia;
import edu.bookingtour.repo.MaGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaGiamGiaService {

    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;

    @Autowired
    private TourService tourService;

    public Page<MaGiamGia> findAll(int page, int size) {
        return maGiamGiaRepository.findAll(PageRequest.of(page, size));
    }

    public List<MaGiamGia> findAllActive(LocalDate today) {
        return maGiamGiaRepository.findAll().stream()
                .filter(m -> m.getMa() != null && !m.getMa().isBlank())
                .filter(m -> isActive(m, today))
                .sorted(Comparator.comparing(MaGiamGia::getMa))
                .toList();
    }

    public Optional<MaGiamGia> findById(Integer id) {
        return maGiamGiaRepository.findById(id);
    }

    @Transactional
    public MaGiamGia save(MaGiamGia entity, List<Integer> selectedTourIds, boolean applyAllTours) {
        normalize(entity, selectedTourIds, applyAllTours);
        if (entity.getId() == null && maGiamGiaRepository.existsByMaIgnoreCase(entity.getMa())) {
            throw new IllegalArgumentException("Mã giảm giá đã tồn tại");
        }
        if (entity.getId() != null && maGiamGiaRepository.existsByMaIgnoreCaseAndIdNot(entity.getMa(), entity.getId())) {
            throw new IllegalArgumentException("Mã giảm giá đã tồn tại");
        }
        return maGiamGiaRepository.save(entity);
    }

    @Transactional
    public void delete(Integer id) {
        maGiamGiaRepository.deleteById(id);
    }

    public PromoApplyResult validateAndApply(String maCode, int tourId, double unitPricePerGuest, int soLuong) {
        if (maCode == null || maCode.isBlank()) {
            return PromoApplyResult.invalid("Vui lòng nhập mã giảm giá");
        }
        double subtotal = unitPricePerGuest * soLuong;
        MaGiamGia promo = maGiamGiaRepository.findByMaIgnoreCase(maCode.trim()).orElse(null);
        if (promo == null) {
            return PromoApplyResult.invalid("Mã giảm giá không tồn tại");
        }
        if (!isActive(promo, LocalDate.now())) {
            return PromoApplyResult.invalid("Mã giảm giá đã hết hạn hoặc chưa có hiệu lực");
        }
        if (!appliesToTour(promo, tourId)) {
            return PromoApplyResult.invalid("Mã không áp dụng cho tour này");
        }
        if (promo.getGiaToiThieu() != null && BigDecimal.valueOf(unitPricePerGuest).compareTo(promo.getGiaToiThieu()) < 0) {
            return PromoApplyResult.invalid("Tour chưa đủ điều kiện giá tối thiểu (" +
                    formatMoney(promo.getGiaToiThieu().doubleValue()) + " VND/khách)");
        }
        double discount = calculateDiscount(promo, subtotal);
        if (discount <= 0) {
            return PromoApplyResult.invalid("Mã giảm giá không áp dụng được cho đơn này");
        }
        double total = Math.max(0, subtotal - discount);
        return PromoApplyResult.ok(promo, subtotal, discount, total);
    }

    public boolean isActive(MaGiamGia m, LocalDate today) {
        if (m.getNgayBatDau() != null && today.isBefore(m.getNgayBatDau())) {
            return false;
        }
        if (m.getNgayKetThuc() != null && today.isAfter(m.getNgayKetThuc())) {
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
        if (MaGiamGia.LOAI_SO_TIEN.equals(m.resolvedLoai())) {
            return Math.min(subtotal, val.doubleValue());
        }
        double percent = val.doubleValue();
        return BigDecimal.valueOf(subtotal * percent / 100.0)
                .setScale(0, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /** Tour đang có ít nhất một mã giảm hiệu lực */
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
        String loai = MaGiamGia.LOAI_SO_TIEN.equals(m.resolvedLoai())
                ? "giảm " + formatMoney(m.resolvedGiaTri().doubleValue()) + " VND"
                : "giảm " + m.resolvedGiaTri().intValue() + "%";
        String scope = Boolean.TRUE.equals(m.getApDungTatCa()) ? "tất cả tour" : "một số tour chỉ định";
        String min = m.getGiaToiThieu() != null
                ? " | đơn từ " + formatMoney(m.getGiaToiThieu().doubleValue()) + " VND/khách"
                : "";
        return String.format("**%s**: %s (%s)%s", m.getMa(), loai, scope, min);
    }

    private void normalize(MaGiamGia entity, List<Integer> selectedTourIds, boolean applyAllTours) {
        entity.setMa(entity.getMa() != null ? entity.getMa().trim().toUpperCase() : null);
        entity.setApDungTatCa(applyAllTours);
        if (applyAllTours) {
            entity.setTourIds(new HashSet<>());
        } else {
            entity.setTourIds(selectedTourIds != null
                    ? new HashSet<>(selectedTourIds)
                    : new HashSet<>());
        }
        if (MaGiamGia.LOAI_PHAN_TRAM.equals(entity.getLoaiGiam())) {
            entity.setPhanTramGiam(entity.getGiaTriGiam() != null ? entity.getGiaTriGiam().intValue() : null);
        }
    }

    private static String formatMoney(double v) {
        return String.format("%,.0f", v);
    }
}
