package edu.bookingtour.repo;

import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {

    @Query("SELECT nd.hoTen FROM NguoiDung nd JOIN DanhGia dg ON nd = dg.idNguoiDung")
    List<String> findTatCaHoTen();
}
