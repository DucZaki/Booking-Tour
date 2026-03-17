package edu.bookingtour.repo;

import edu.bookingtour.entity.NguoiDung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {
    Optional<NguoiDung> findByTenDangNhap(String tenDangNhap);
    Optional<NguoiDung> findByEmail(String email);
    @Query("SELECT n.hoTen FROM NguoiDung n")
    List<String> findTatCaHoTen();
    List<NguoiDung> findAll();
    @Query("SELECT u.id, u.hoTen, u.email, u.number, u.vaiTro, u.ngayTao, " +
            "(SELECT COUNT(d) FROM DatCho d WHERE d.idNguoiDung = u), " +
            "(SELECT SUM(d.tongGia) FROM DatCho d WHERE d.idNguoiDung = u AND d.trangThai = 'PAID') " +
            "FROM NguoiDung u " +
            "ORDER BY u.ngayTao DESC")
    Page<Object[]> findAllUserDetails(Pageable pageable);
}