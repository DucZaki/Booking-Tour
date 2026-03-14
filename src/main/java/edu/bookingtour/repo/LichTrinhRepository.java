package edu.bookingtour.repo;

import edu.bookingtour.entity.LichTrinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LichTrinhRepository extends JpaRepository<LichTrinh, Integer> {
    List<LichTrinh> findByTourIdOrderByNgayThuAsc(Integer tourId);
    @Query("select max(l.ngayThu) from LichTrinh l where l.tourId = :tourId")
    Integer findMaxNgayThu(Integer tourId);
}
