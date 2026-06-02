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

    void deleteByChuyenDiIdAndThangAndNam(Integer chuyenDiId, Integer thang, Integer nam);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM NgayKhoiHanh n JOIN FETCH n.chuyenDi WHERE n.id = :id")
    Optional<NgayKhoiHanh> findByIdForUpdate(@Param("id") Integer id);

    @Query("SELECT n FROM NgayKhoiHanh n JOIN FETCH n.chuyenDi WHERE n.id = :id")
    Optional<NgayKhoiHanh> findByIdWithChuyenDi(@Param("id") Integer id);
}
