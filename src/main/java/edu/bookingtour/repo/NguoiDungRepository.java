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
            "(SELECT COUNT(d) FROM DatCho d WHERE d.idNguoiDung = u AND d.trangThai = 'PAID'), " +
            "(SELECT COALESCE(SUM(d.tongGia), 0) FROM DatCho d WHERE d.idNguoiDung = u AND d.trangThai = 'PAID') " +
            "FROM NguoiDung u " +
            "ORDER BY u.ngayTao DESC")
    Page<Object[]> findAllUserDetails(Pageable pageable);

    @Query(value = """
            SELECT u.id, u.ho_ten, u.email, u.number, u.vai_tro, u.ngay_tao,
                   COUNT(CASE WHEN d.trang_thai = 'PAID' THEN 1 END) AS paid_bookings,
                   COALESCE(SUM(CASE WHEN d.trang_thai = 'PAID' THEN d.tong_gia ELSE 0 END), 0) AS total_spending
            FROM nguoi_dung u
            LEFT JOIN dat_cho d ON d.id_nguoi_dung = u.id
            WHERE (:kw IS NULL OR :kw = ''
                   OR LOWER(u.ho_ten) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.ten_dang_nhap) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR CAST(u.id AS CHAR) LIKE CONCAT('%', :kw, '%'))
            GROUP BY u.id, u.ho_ten, u.email, u.number, u.vai_tro, u.ngay_tao
            ORDER BY total_spending DESC, paid_bookings DESC, u.ho_ten ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT u.id)
            FROM nguoi_dung u
            WHERE (:kw IS NULL OR :kw = ''
                   OR LOWER(u.ho_ten) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.ten_dang_nhap) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR CAST(u.id AS CHAR) LIKE CONCAT('%', :kw, '%'))
            """,
            nativeQuery = true)
    Page<Object[]> searchUsersBySpendingDesc(@org.springframework.data.repository.query.Param("kw") String keyword,
            Pageable pageable);

    @Query(value = """
            SELECT u.id, u.ho_ten, u.email, u.number, u.vai_tro, u.ngay_tao,
                   COUNT(CASE WHEN d.trang_thai = 'PAID' THEN 1 END) AS paid_bookings,
                   COALESCE(SUM(CASE WHEN d.trang_thai = 'PAID' THEN d.tong_gia ELSE 0 END), 0) AS total_spending
            FROM nguoi_dung u
            LEFT JOIN dat_cho d ON d.id_nguoi_dung = u.id
            WHERE (:kw IS NULL OR :kw = ''
                   OR LOWER(u.ho_ten) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.ten_dang_nhap) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR CAST(u.id AS CHAR) LIKE CONCAT('%', :kw, '%'))
            GROUP BY u.id, u.ho_ten, u.email, u.number, u.vai_tro, u.ngay_tao
            ORDER BY total_spending ASC, u.ho_ten ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT u.id)
            FROM nguoi_dung u
            WHERE (:kw IS NULL OR :kw = ''
                   OR LOWER(u.ho_ten) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.ten_dang_nhap) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR CAST(u.id AS CHAR) LIKE CONCAT('%', :kw, '%'))
            """,
            nativeQuery = true)
    Page<Object[]> searchUsersBySpendingAsc(@org.springframework.data.repository.query.Param("kw") String keyword,
            Pageable pageable);

    @Query(value = """
            SELECT u.id, u.ho_ten, u.email, u.number, u.vai_tro, u.ngay_tao,
                   COUNT(CASE WHEN d.trang_thai = 'PAID' THEN 1 END) AS paid_bookings,
                   COALESCE(SUM(CASE WHEN d.trang_thai = 'PAID' THEN d.tong_gia ELSE 0 END), 0) AS total_spending
            FROM nguoi_dung u
            LEFT JOIN dat_cho d ON d.id_nguoi_dung = u.id
            WHERE (:kw IS NULL OR :kw = ''
                   OR LOWER(u.ho_ten) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.ten_dang_nhap) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR CAST(u.id AS CHAR) LIKE CONCAT('%', :kw, '%'))
            GROUP BY u.id, u.ho_ten, u.email, u.number, u.vai_tro, u.ngay_tao
            ORDER BY paid_bookings DESC, total_spending DESC, u.ho_ten ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT u.id)
            FROM nguoi_dung u
            WHERE (:kw IS NULL OR :kw = ''
                   OR LOWER(u.ho_ten) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.ten_dang_nhap) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR CAST(u.id AS CHAR) LIKE CONCAT('%', :kw, '%'))
            """,
            nativeQuery = true)
    Page<Object[]> searchUsersByBookingsDesc(@org.springframework.data.repository.query.Param("kw") String keyword,
            Pageable pageable);

    @Query(value = """
            SELECT u.id, u.ho_ten, u.email, u.number, u.vai_tro, u.ngay_tao,
                   COUNT(CASE WHEN d.trang_thai = 'PAID' THEN 1 END) AS paid_bookings,
                   COALESCE(SUM(CASE WHEN d.trang_thai = 'PAID' THEN d.tong_gia ELSE 0 END), 0) AS total_spending
            FROM nguoi_dung u
            LEFT JOIN dat_cho d ON d.id_nguoi_dung = u.id
            WHERE (:kw IS NULL OR :kw = ''
                   OR LOWER(u.ho_ten) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.ten_dang_nhap) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR CAST(u.id AS CHAR) LIKE CONCAT('%', :kw, '%'))
            GROUP BY u.id, u.ho_ten, u.email, u.number, u.vai_tro, u.ngay_tao
            ORDER BY u.ho_ten ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT u.id)
            FROM nguoi_dung u
            WHERE (:kw IS NULL OR :kw = ''
                   OR LOWER(u.ho_ten) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(u.ten_dang_nhap) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR CAST(u.id AS CHAR) LIKE CONCAT('%', :kw, '%'))
            """,
            nativeQuery = true)
    Page<Object[]> searchUsersByNameAsc(@org.springframework.data.repository.query.Param("kw") String keyword,
            Pageable pageable);
}