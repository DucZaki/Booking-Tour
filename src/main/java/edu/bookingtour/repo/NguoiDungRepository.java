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
    List<NguoiDung> findByVaiTro(String vaiTro);
    @Query("SELECT n.hoTen FROM NguoiDung n")
    List<String> findTatCaHoTen();
    long countByVaiTro(String vaiTro);
}