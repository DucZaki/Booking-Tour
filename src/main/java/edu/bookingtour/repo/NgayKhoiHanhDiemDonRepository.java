package edu.bookingtour.repo;

import edu.bookingtour.entity.NgayKhoiHanhDiemDon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NgayKhoiHanhDiemDonRepository extends JpaRepository<NgayKhoiHanhDiemDon, Integer> {

    List<NgayKhoiHanhDiemDon> findByNgayKhoiHanhIdOrderByDiemDonIdAsc(Integer ngayKhoiHanhId);

    Optional<NgayKhoiHanhDiemDon> findByNgayKhoiHanhIdAndDiemDonId(Integer ngayKhoiHanhId, Integer diemDonId);

    void deleteByNgayKhoiHanhId(Integer ngayKhoiHanhId);
}
