package edu.bookingtour.repo;

import edu.bookingtour.entity.DiemDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiemDonRepository extends JpaRepository<DiemDon, Integer> {
    Optional<DiemDon> findByTen(String ten);
}
