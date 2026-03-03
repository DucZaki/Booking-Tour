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

        @Query("SELECT SUM(d.soLuong * d.idChuyenDi.gia) FROM DatCho d WHERE d.trangThai = 'PAID'")
        Double calculateTotalRevenue();

        @Query("SELECT d.idChuyenDi.id as id, d.idChuyenDi.tieuDe as tieuDe, COUNT(d) as totalBookings, SUM(d.soLuong * d.idChuyenDi.gia) as revenue "
                        +
                        "FROM DatCho d " +
                        "WHERE d.trangThai = 'PAID' " +
                        "GROUP BY d.idChuyenDi.id, d.idChuyenDi.tieuDe " +
                        "ORDER BY COUNT(d) DESC")
        List<Object[]> findTopSellingTours();

        @Query("SELECT FUNCTION('MONTH', d.ngayDat) as month, SUM(d.soLuong * d.idChuyenDi.gia) as revenue " +
                        "FROM DatCho d " +
                        "WHERE d.trangThai = 'PAID' AND FUNCTION('YEAR', d.ngayDat) = FUNCTION('YEAR', CURRENT_DATE) " +
                        "GROUP BY FUNCTION('MONTH', d.ngayDat) " +
                        "ORDER BY FUNCTION('MONTH', d.ngayDat)")
        List<Object[]> findMonthlyRevenue();

        @Query("SELECT d.trangThai as status, COUNT(d) as count FROM DatCho d GROUP BY d.trangThai")
        List<Object[]> findBookingStatusDistribution();
}
