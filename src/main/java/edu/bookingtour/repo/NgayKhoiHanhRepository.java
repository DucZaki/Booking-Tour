package edu.bookingtour.repo;

import edu.bookingtour.entity.NgayKhoiHanh;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NgayKhoiHanhRepository extends JpaRepository<NgayKhoiHanh, Integer> {

    List<NgayKhoiHanh> findByChuyenDiIdAndThangAndNam(Integer chuyenDiId, Integer thang, Integer nam);

    Optional<NgayKhoiHanh> findByChuyenDiIdAndNgay(Integer chuyenDiId, LocalDate ngay);

    List<NgayKhoiHanh> findByChuyenDiId(Integer chuyenDiId);

    @Query("""
            SELECT n FROM NgayKhoiHanh n
            JOIN FETCH n.chuyenDi c
            LEFT JOIN FETCH n.guide
            WHERE c.id = :tourId AND n.ngay >= :from AND n.ngay <= :to
            ORDER BY n.ngay ASC
            """)
    List<NgayKhoiHanh> findByTourAndDateRange(
            @Param("tourId") Integer tourId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT n FROM NgayKhoiHanh n
            JOIN FETCH n.chuyenDi c
            LEFT JOIN FETCH n.guide
            WHERE n.ngay = :date
              AND (c.ngayKetThuc IS NULL OR c.ngayKetThuc >= :date)
            ORDER BY n.gioTapTrung ASC, c.tieuDe ASC
            """)
    List<NgayKhoiHanh> findAllDeparturesOnDate(@Param("date") LocalDate date);

    @Query("""
            SELECT n FROM NgayKhoiHanh n
            JOIN FETCH n.chuyenDi c
            LEFT JOIN FETCH n.guide
            WHERE n.ngay >= :from AND n.ngay <= :to
              AND (c.ngayKetThuc IS NULL OR c.ngayKetThuc >= :from)
            ORDER BY n.ngay ASC, n.gioTapTrung ASC, c.tieuDe ASC
            """)
    List<NgayKhoiHanh> findActiveDeparturesInRange(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    void deleteByChuyenDiIdAndThangAndNam(Integer chuyenDiId, Integer thang, Integer nam);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM NgayKhoiHanh n JOIN FETCH n.chuyenDi WHERE n.id = :id")
    Optional<NgayKhoiHanh> findByIdForUpdate(@Param("id") Integer id);

    @Query("SELECT n FROM NgayKhoiHanh n JOIN FETCH n.chuyenDi WHERE n.id = :id")
    Optional<NgayKhoiHanh> findByIdWithChuyenDi(@Param("id") Integer id);

    @Query("""
            SELECT n FROM NgayKhoiHanh n
            JOIN FETCH n.chuyenDi c
            LEFT JOIN FETCH n.guide
            WHERE n.id = :id
            """)
    Optional<NgayKhoiHanh> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
            SELECT n FROM NgayKhoiHanh n
            JOIN FETCH n.chuyenDi c
            LEFT JOIN FETCH n.guide g
            WHERE n.ngay >= :from AND n.ngay <= :to
              AND (:guideId IS NULL OR g.id = :guideId)
            ORDER BY n.ngay ASC, c.tieuDe ASC
            """)
    List<NgayKhoiHanh> findDeparturesInRange(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("guideId") Integer guideId);

    @Query("""
            SELECT n FROM NgayKhoiHanh n
            JOIN FETCH n.chuyenDi c
            LEFT JOIN FETCH n.guide
            WHERE n.trangThaiDoan = 'IN_PROGRESS'
              AND COALESCE(n.ngayVe, n.ngay) <= :date
            ORDER BY COALESCE(n.ngayVe, n.ngay) ASC, c.tieuDe ASC
            """)
    List<NgayKhoiHanh> findInProgressDueForCompletion(@Param("date") LocalDate date);

    @Query("""
            SELECT n FROM NgayKhoiHanh n
            JOIN FETCH n.chuyenDi c
            LEFT JOIN FETCH n.guide
            WHERE n.trangThaiDoan = 'SCHEDULED'
              AND n.ngay <= :date
            ORDER BY n.ngay ASC, c.tieuDe ASC
            """)
    List<NgayKhoiHanh> findScheduledReadyForAutoStart(@Param("date") LocalDate date);
}
