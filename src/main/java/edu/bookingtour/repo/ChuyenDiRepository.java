package edu.bookingtour.repo;

import edu.bookingtour.entity.ChuyenDi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChuyenDiRepository extends JpaRepository<ChuyenDi, Integer>,
                                            JpaSpecificationExecutor<ChuyenDi> {
    List<ChuyenDi> findByNoiBat (boolean noiBat);
    List<ChuyenDi> findAll();
    long count();

    @Query("""
            SELECT cd FROM ChuyenDi cd
            JOIN cd.idDiemDen dd
            WHERE (:thanhPho IS NULL OR LOWER(dd.thanhPho) = LOWER(:thanhPho))
            AND (:quocGia IS NULL OR LOWER(dd.quocGia) = LOWER(:quocGia))
            AND (:diemDen IS NULL OR LOWER(dd.thanhPho) = LOWER(:diemDen) OR LOWER(dd.quocGia) = LOWER(:diemDen))
            AND (:ngayDi IS NULL OR cd.ngayKhoiHanh >= :ngayDi)
            AND (:minGia IS NULL OR :maxGia IS NULL OR cd.gia BETWEEN :minGia AND :maxGia)
            """)
    Page<ChuyenDi> filterTour(String thanhPho, String quocGia, String diemDen, LocalDate ngayDi, BigDecimal minGia, BigDecimal maxGia, Pageable pageable);

    Page<ChuyenDi> findByNgayKetThucAfter(LocalDate today, Pageable pageable);
    Page<ChuyenDi> findByNgayKetThucBefore(LocalDate today, Pageable pageable);
}
