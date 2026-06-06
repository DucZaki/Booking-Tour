package edu.bookingtour.repo;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DatChoRepository extends JpaRepository<DatCho, Integer> {

    @org.springframework.data.jpa.repository.Query("""
            SELECT COALESCE(SUM(d.soLuong), 0) FROM DatCho d
            WHERE d.idNgayKhoiHanh.id = :nkhId AND d.trangThai IN :statuses
            """)
    Integer sumGuestsByNgayKhoiHanhAndStatuses(
            @org.springframework.data.repository.query.Param("nkhId") Integer nkhId,
            @org.springframework.data.repository.query.Param("statuses") Collection<String> statuses);
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

    List<DatCho> findByEmailOrderByNgayDatDesc(String email);

    @Query("""
            SELECT d FROM DatCho d
            LEFT JOIN FETCH d.idChuyenDi t
            LEFT JOIN FETCH t.idDiemDen
            LEFT JOIN FETCH t.idPhuongTien
            LEFT JOIN FETCH d.idDiemDon
            LEFT JOIN FETCH d.idNgayKhoiHanh
            WHERE d.id = :id
            """)
    java.util.Optional<DatCho> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT d FROM DatCho d WHERE d.maCheckIn = :maCheckIn")
    java.util.Optional<DatCho> findByMaCheckIn(@Param("maCheckIn") String maCheckIn);

    @Query("""
            SELECT d FROM DatCho d
            LEFT JOIN FETCH d.idChuyenDi t
            LEFT JOIN FETCH t.idDiemDen
            LEFT JOIN FETCH t.idPhuongTien
            LEFT JOIN FETCH d.idDiemDon
            LEFT JOIN FETCH d.idNgayKhoiHanh
            WHERE d.maCheckIn = :token
            """)
    java.util.Optional<DatCho> findByMaCheckInWithDetails(@Param("token") String token);

    long countByIdChuyenDi_IdAndTrangThaiIn(Integer tourId, Collection<String> statuses);

    @Query("""
            select count(d) from DatCho d
             where d.idNguoiDung.id = :userId
               and d.idMaGiamGia.id = :promoId
               and d.trangThai <> 'CANCELLED'
            """)
    long countPromoUsageByUser(@Param("userId") Integer userId, @Param("promoId") Integer promoId);

    @Query("""
            SELECT d FROM DatCho d
            LEFT JOIN FETCH d.idDiemDon
            LEFT JOIN FETCH d.idNgayKhoiHanh nkh
            LEFT JOIN FETCH nkh.chuyenDi t
            LEFT JOIN FETCH t.idNoiLuuTru
            WHERE d.idNgayKhoiHanh.id = :nkhId AND d.trangThai = 'PAID'
            ORDER BY d.hoTen ASC
            """)
    List<DatCho> findPaidManifestByNgayKhoiHanh(@Param("nkhId") Integer nkhId);

    @Query("""
            SELECT d FROM DatCho d
            LEFT JOIN FETCH d.idDiemDon
            LEFT JOIN FETCH d.idNgayKhoiHanh nkh
            LEFT JOIN FETCH nkh.chuyenDi t
            LEFT JOIN FETCH t.idNoiLuuTru
            WHERE d.idNgayKhoiHanh.id = :nkhId AND d.trangThai = 'PAID'
              AND (LOWER(d.hoTen) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR d.soDienThoai LIKE CONCAT('%', :q, '%')
                   OR CAST(d.id AS string) LIKE CONCAT('%', :q, '%'))
            ORDER BY d.hoTen ASC
            """)
    List<DatCho> searchPaidManifestByNgayKhoiHanh(@Param("nkhId") Integer nkhId, @Param("q") String q);
}
