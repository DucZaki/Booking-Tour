package edu.bookingtour.repo;

import edu.bookingtour.entity.MaGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Integer> {
    Optional<MaGiamGia> findByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, Integer id);
}
