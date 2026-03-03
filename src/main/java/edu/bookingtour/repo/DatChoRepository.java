package edu.bookingtour.repo;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatChoRepository extends JpaRepository<DatCho, Integer> {
    List<DatCho> findByIdNguoiDungOrderByIdDesc(NguoiDung user);

    List<DatCho> findByIdNguoiDungAndIdChuyenDiAndTrangThai(NguoiDung user, ChuyenDi tour, String status);
}
