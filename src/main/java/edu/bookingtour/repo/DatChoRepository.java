package edu.bookingtour.repo;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatChoRepository extends JpaRepository<DatCho, Integer> {
    List<DatCho> findByIdNguoiDungOrderByIdDesc(NguoiDung user);

    List<DatCho> findByIdNguoiDungAndIdChuyenDiAndTrangThai(NguoiDung user, ChuyenDi tour, String status);

    @Query("SELECT FUNCTION('MONTH', d.ngayDat) as month, SUM(d.tongGia) as revenue " +
           "FROM DatCho d " +
           "WHERE d.idNguoiDung = :user AND d.trangThai = 'PAID' AND FUNCTION('YEAR', d.ngayDat) = FUNCTION('YEAR', CURRENT_DATE) " +
           "GROUP BY FUNCTION('MONTH', d.ngayDat) " +
           "ORDER BY FUNCTION('MONTH', d.ngayDat)")
    List<Object[]> findMonthlySpendingByUser(@Param("user") NguoiDung user);

    @Query("SELECT d FROM DatCho d WHERE d.idNguoiDung = :user ORDER BY d.ngayDat DESC")
    List<DatCho> findRecentBookingsByUser(@Param("user") NguoiDung user);

    org.springframework.data.domain.Page<DatCho> findByIdNguoiDung(NguoiDung user, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COALESCE(SUM(d.tongGia), 0.0) FROM DatCho d WHERE d.idNguoiDung = :user AND d.trangThai = 'PAID'")
    Double sumTongGiaByUser(@Param("user") NguoiDung user);
}
