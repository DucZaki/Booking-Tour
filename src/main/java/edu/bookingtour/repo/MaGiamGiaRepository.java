package edu.bookingtour.repo;

import edu.bookingtour.entity.MaGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Integer> {
    Optional<MaGiamGia> findByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, Integer id);

    @Modifying
    @Query("""
            update MaGiamGia m
               set m.soLanDaDung = m.soLanDaDung + 1
             where m.id = :id
               and m.active = true
               and (m.soLanDungToiDa is null or m.soLanDaDung < m.soLanDungToiDa)
            """)
    int incrementUsage(@Param("id") Integer id);

    @Query("""
            select m from MaGiamGia m
             where (m.active is null or m.active = true)
               and (m.ngayBatDau is null or m.ngayBatDau <= :today)
               and (m.ngayKetThuc is null or m.ngayKetThuc >= :today)
               and (m.soLanDungToiDa is null or m.soLanDaDung is null or m.soLanDaDung < m.soLanDungToiDa)
             order by m.id desc
            """)
    List<MaGiamGia> findActivePromos(@Param("today") LocalDate today);
}
