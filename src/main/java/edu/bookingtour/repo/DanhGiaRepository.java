package edu.bookingtour.repo;

import edu.bookingtour.entity.DanhGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {
    Page<DanhGia> findByDiem(Integer diem, Pageable pageable);

    Page<DanhGia> findByDiemAndIdNguoiDung_HoTenContainingIgnoreCase(Integer diem, String hoTen, Pageable pageable);

    Page<DanhGia> findByIdNguoiDung_HoTenContainingIgnoreCase(String hoTen, Pageable pageable);

    List<DanhGia> findByIdChuyenDi_Id(Integer id);

    Optional<DanhGia> findByIdChuyenDi_IdAndIdNguoiDung_TenDangNhap(Integer tourId, String username);
}
