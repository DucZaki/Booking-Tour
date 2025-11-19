package edu.bookingtour.repo;

import edu.bookingtour.entity.ChuyenDi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChuyenDiRepository extends JpaRepository<ChuyenDi, Integer> {
    List<ChuyenDi> findByNoiBat (boolean noiBat);
}
