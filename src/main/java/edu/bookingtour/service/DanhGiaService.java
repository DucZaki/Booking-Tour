package edu.bookingtour.service;

import edu.bookingtour.entity.DanhGia;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.repo.DanhGiaRepository;
import edu.bookingtour.repo.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class DanhGiaService {
    @Autowired
    ChuyenDiRepository chuyenDiRepository;
    @Autowired
    NguoiDungRepository nguoiDungRepository;
    @Autowired
    DanhGiaRepository danhGiaRepository;

    public Page<DanhGia> filter(Integer diem, String ten, Integer tourId, String sort, int page, int size) {
        Sort sortOption = Sort.by("ngayDanhGia").descending();
        if ("scoreAsc".equals(sort)) {
            sortOption = Sort.by("diem").ascending();
        } else if ("scoreDesc".equals(sort)) {
            sortOption = Sort.by("diem").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sortOption);

        if (tourId != null) {
            if (diem != null) {
                return danhGiaRepository.findByIdChuyenDi_IdAndDiem(tourId, diem, pageable);
            }
            return danhGiaRepository.findByIdChuyenDi_Id(tourId, pageable);
        }

        if (diem != null && ten != null && !ten.isEmpty()) {
            return danhGiaRepository.findByDiemAndIdNguoiDung_HoTenContainingIgnoreCase(diem, ten, pageable);
        }
        if (diem != null) {
            return danhGiaRepository.findByDiem(diem, pageable);
        }
        if (ten != null && !ten.isEmpty()) {
            return danhGiaRepository.findByIdNguoiDung_HoTenContainingIgnoreCase(ten, pageable);
        }
        return danhGiaRepository.findAll(pageable);
    }

    public List<edu.bookingtour.dto.TourReviewDTO> getToursWithReviews(String sort) {
        java.util.stream.Stream<edu.bookingtour.dto.TourReviewDTO> stream = chuyenDiRepository.findAll().stream()
                .filter(tour -> tour.getDanhGias() != null && !tour.getDanhGias().isEmpty())
                .map(tour -> {
                    double avg = tour.getDanhGias().stream()
                            .mapToInt(DanhGia::getDiem)
                            .average()
                            .orElse(0.0);
                    long total = tour.getDanhGias().size();
                    long positiveCount = tour.getDanhGias().stream()
                            .filter(dg -> dg.getDiem() >= 4)
                            .count();
                    long percentage = (total > 0) ? (positiveCount * 100 / total) : 0;

                    return edu.bookingtour.dto.TourReviewDTO.builder()
                            .id(tour.getId())
                            .tieuDe(tour.getTieuDe())
                            .hinhAnh(tour.getHinhAnh())
                            .avgRating(avg)
                            .totalReviews(total)
                            .positivePercentage(percentage)
                            .build();
                });

        if ("ratingDesc".equals(sort)) {
            stream = stream.sorted(Comparator.comparingDouble(edu.bookingtour.dto.TourReviewDTO::getAvgRating).reversed());
        } else if ("ratingAsc".equals(sort)) {
            stream = stream.sorted(Comparator.comparingDouble(edu.bookingtour.dto.TourReviewDTO::getAvgRating));
        }

        return stream.toList();
    }

    public List<DanhGia> findByTourId(Integer tourId) {
        return danhGiaRepository.findByIdChuyenDi_Id(tourId);
    }

    public DanhGia findUserReview(Integer tourId, String username) {
        return danhGiaRepository.findByIdChuyenDi_IdAndIdNguoiDung_TenDangNhap(tourId, username).orElse(null);
    }

    public void save(Integer tourId, Integer diem, String binhLuan, String username) {
        DanhGia existing = danhGiaRepository.findByIdChuyenDi_IdAndIdNguoiDung_TenDangNhap(tourId, username)
                .orElse(null);
        if (existing != null) {
            existing.setDiem(diem);
            existing.setBinhLuan(binhLuan);
            existing.setNgayDanhGia(Instant.now());
            danhGiaRepository.save(existing);
        } else {
            DanhGia dg = new DanhGia();
            dg.setDiem(diem);
            dg.setBinhLuan(binhLuan);
            dg.setNgayDanhGia(Instant.now());
            dg.setIdChuyenDi(
                    chuyenDiRepository.findById(tourId).orElseThrow(() -> new RuntimeException("Tour không tồn tại")));
            dg.setIdNguoiDung(nguoiDungRepository.findByTenDangNhap(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại")));
            danhGiaRepository.save(dg);
        }
    }

    public void delete(Integer id) {
        danhGiaRepository.deleteById(id);
    }
}
