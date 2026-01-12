package edu.bookingtour.repo;

import edu.bookingtour.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {
    Optional<NguoiDung> findByTenDangNhap(String tenDangNhap);
    Optional<NguoiDung> findByEmail(String email);
    boolean existsByTenDangNhap(String tenDangNhap);
    boolean existsByEmail(String email);
    List<NguoiDung> findAll();
    @Query("SELECT nd.hoTen FROM NguoiDung nd JOIN DanhGia dg ON nd = dg.idNguoiDung")
    List<String> findTatCaHoTen();
}