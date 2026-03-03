package edu.bookingtour.repo;

import edu.bookingtour.entity.NgayKhoiHanh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NgayKhoiHanhRepository extends JpaRepository<NgayKhoiHanh, Integer> {

    List<NgayKhoiHanh> findByChuyenDiIdAndThangAndNam(Integer chuyenDiId, Integer thang, Integer nam);

    Optional<NgayKhoiHanh> findByChuyenDiIdAndNgay(Integer chuyenDiId, LocalDate ngay);

    List<NgayKhoiHanh> findByChuyenDiId(Integer chuyenDiId);

    void deleteByChuyenDiIdAndThangAndNam(Integer chuyenDiId, Integer thang, Integer nam);
}
