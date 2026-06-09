package edu.bookingtour.repo;

import edu.bookingtour.entity.NgayKhoiHanhDiemDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NgayKhoiHanhDiemDonRepository extends JpaRepository<NgayKhoiHanhDiemDon, Integer> {

    List<NgayKhoiHanhDiemDon> findByNgayKhoiHanhIdOrderByDiemDonIdAsc(Integer ngayKhoiHanhId);

    Optional<NgayKhoiHanhDiemDon> findByNgayKhoiHanhIdAndDiemDonId(Integer ngayKhoiHanhId, Integer diemDonId);

    void deleteByNgayKhoiHanhId(Integer ngayKhoiHanhId);

    boolean existsByNgayKhoiHanhIdAndGuideId(Integer ngayKhoiHanhId, Integer guideId);

    boolean existsByNgayKhoiHanhIdAndDiemDonIdAndGuideId(Integer ngayKhoiHanhId, Integer diemDonId, Integer guideId);

    @Query("""
            SELECT dd FROM NgayKhoiHanhDiemDon dd
            JOIN FETCH dd.ngayKhoiHanh nkh
            JOIN FETCH nkh.chuyenDi c
            JOIN FETCH dd.diemDon
            LEFT JOIN FETCH dd.guide
            WHERE dd.guide.id = :guideId
              AND nkh.ngay >= :from AND nkh.ngay <= :to
            ORDER BY nkh.ngay ASC, c.tieuDe ASC
            """)
    List<NgayKhoiHanhDiemDon> findAssignmentsForGuideInRange(
            @Param("guideId") Integer guideId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT dd.diemDon.id FROM NgayKhoiHanhDiemDon dd
            WHERE dd.ngayKhoiHanh.id = :nkhId AND dd.guide.id = :guideId
            """)
    List<Integer> findDiemDonIdsByNkhAndGuide(@Param("nkhId") Integer nkhId, @Param("guideId") Integer guideId);
}
