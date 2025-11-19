package edu.bookingtour.repo;
import edu.bookingtour.entity.DiemDen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DiemDenRepository extends JpaRepository<DiemDen, Integer> {
    List<DiemDen> findByNoiBat(Boolean noiBat);
}
