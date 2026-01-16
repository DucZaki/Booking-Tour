package edu.bookingtour.repo;

import edu.bookingtour.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DanhGiaRepository extends JpaRepository<DanhGia,Integer>
{
    List<DanhGia> findAll();
//    List<Integer> findByIdChuyenDi();
}
