package edu.bookingtour.service;

import edu.bookingtour.entity.DanhGia;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.repo.DanhGiaRepository;
import edu.bookingtour.repo.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class DanhGiaService {
    @Autowired
    ChuyenDiRepository chuyenDiRepository;
    @Autowired
    NguoiDungRepository nguoiDungRepository;
    @Autowired
    DanhGiaRepository danhGiaRepository;

    public Page<DanhGia> filter(Integer diem, String ten, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
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
