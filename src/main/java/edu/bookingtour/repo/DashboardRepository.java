package edu.bookingtour.repo;

import edu.bookingtour.entity.DatCho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DashboardRepository extends JpaRepository<DatCho, Integer> {

        @Query("SELECT COUNT(d) FROM DatCho d")
        long countTotalBookings();

        @Query("SELECT COUNT(d) FROM DatCho d WHERE d.trangThai = 'PAID'")
        long countSuccessfulBookings();

        @Query("SELECT COUNT(d) FROM DatCho d WHERE d.trangThai = 'FAILED'")
        long countFailedBookings();

        @Query("SELECT SUM(d.tongGia) FROM DatCho d WHERE d.trangThai = 'PAID'")
        Double calculateTotalRevenue();

        @Query("SELECT d.idChuyenDi.id as id, d.idChuyenDi.tieuDe as tieuDe, COUNT(d) as totalBookings, SUM(d.tongGia) as revenue "
                        +
                        "FROM DatCho d " +
                        "WHERE d.trangThai = 'PAID' " +
                        "GROUP BY d.idChuyenDi.id, d.idChuyenDi.tieuDe " +
                        "ORDER BY COUNT(d) DESC")
        List<Object[]> findTopSellingTours();

        @Query("SELECT FUNCTION('MONTH', d.ngayDat) as month, SUM(d.tongGia) as revenue " +
                        "FROM DatCho d " +
                        "WHERE d.trangThai = 'PAID' AND FUNCTION('YEAR', d.ngayDat) = FUNCTION('YEAR', CURRENT_DATE) " +
                        "GROUP BY FUNCTION('MONTH', d.ngayDat) " +
                        "ORDER BY FUNCTION('MONTH', d.ngayDat)")
        List<Object[]> findMonthlyRevenue();

        @Query("SELECT d.trangThai as status, COUNT(d) as count FROM DatCho d GROUP BY d.trangThai")
        List<Object[]> findBookingStatusDistribution();

        // Thống kê chi tiêu của từng người dùng (đã thanh toán)
        @Query("SELECT COALESCE(d.idNguoiDung.hoTen, d.hoTen) as tenKhach, " +
                        "COALESCE(d.idNguoiDung.email, d.email) as email, " +
                        "COUNT(d) as soLuotMua, " +
                        "SUM(d.tongGia) as tongChiTieu " +
                        "FROM DatCho d " +
                        "WHERE d.trangThai = 'PAID' " +
                        "GROUP BY d.idNguoiDung.id, d.idNguoiDung.hoTen, d.idNguoiDung.email, d.hoTen, d.email " +
                        "ORDER BY SUM(d.tongGia) DESC")
        List<Object[]> findUserSpendingStats();

        // Lấy danh sách booking chi tiết (khách hàng + số lượng) cho một tour cụ thể
        @Query("SELECT d.hoTen as tenKhach, d.email as email, d.soDienThoai as sdt, " +
                        "d.soLuong as soLuong, d.tongGia as tongGia, d.ngayDat as ngayDat " +
                        "FROM DatCho d " +
                        "WHERE d.idChuyenDi.id = :tourId AND d.trangThai = 'PAID' " +
                        "ORDER BY d.ngayDat DESC")
        List<Object[]> findBookingDetailsByTourId(
                        @org.springframework.data.repository.query.Param("tourId") Integer tourId);
}
