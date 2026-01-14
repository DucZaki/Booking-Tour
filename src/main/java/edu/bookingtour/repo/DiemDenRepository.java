package edu.bookingtour.repo;
import edu.bookingtour.entity.DiemDen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DiemDenRepository extends JpaRepository<DiemDen, Integer> {
    List<DiemDen> findByNoiBat(Boolean noiBat);

    @Query("SELECT DISTINCT d.chauLuc FROM DiemDen d")
    List<String> findDistinctChauLuc();

    @Query("SELECT DISTINCT d.quocGia FROM DiemDen d WHERE d.chauLuc = :chauLuc")
    List<String> findDistinctQuocGiaByChauLuc(String chauLuc);

    @Query("SELECT d FROM DiemDen d WHERE d.quocGia = :quocGia")
    List<DiemDen> findByQuocGia(String quocGia);
}
