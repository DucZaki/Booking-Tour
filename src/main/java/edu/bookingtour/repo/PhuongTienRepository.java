package edu.bookingtour.repo;

import edu.bookingtour.entity.PhuongTien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhuongTienRepository extends JpaRepository<PhuongTien, Integer> {
    @Query("SELECT p FROM PhuongTien p WHERE p.id IN ( SELECT MIN(p2.id) FROM PhuongTien p2 GROUP BY p2.loai)")
    List<PhuongTien> findDistinctLoai();

}
