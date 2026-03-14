package edu.bookingtour.service;

import edu.bookingtour.entity.LichTrinh;
import edu.bookingtour.repo.LichTrinhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LichTrinhService {
    @Autowired
    private LichTrinhRepository repository;
    @Autowired
    private LichTrinhRepository lichTrinhRepository;

    public LichTrinh create(LichTrinh lichTrinh) {
        return repository.save(lichTrinh);
    }

    public LichTrinh update(Integer id, LichTrinh lichTrinh) {
        LichTrinh lt = repository.findById(id).orElseThrow(()->new RuntimeException("Lịch trình không tồn tại"));
        lt.setNgayThu(lichTrinh.getNgayThu());
        lt.setTieuDe(lichTrinh.getTieuDe());
        lt.setSoBuaAn(lichTrinh.getSoBuaAn());
        lt.setNoiDung(lichTrinh.getNoiDung());
        lt.setNghiDem(lichTrinh.getNghiDem());
        return repository.save(lt);
    }

    public List<LichTrinh> getByTour(Integer tourId) {
        return repository.findByTourIdOrderByNgayThuAsc(tourId);
    }

    public LichTrinh getDetail(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Lịch trình không tồn tại"));
    }

    public int getNextDayByTour(Integer tourId) {
        Integer max = lichTrinhRepository.findMaxNgayThu(tourId);
        return (max == null) ? 1 : max + 1;
    }

    public void delete(Integer id) {
        LichTrinh lt = repository.findById(id).orElseThrow();
        Integer tourId = lt.getTourId();
        int ngayThu = lt.getNgayThu();
        repository.deleteById(id);
        List<LichTrinh> list = repository.findByTourIdOrderByNgayThuAsc(tourId);
        for (LichTrinh l : list) {
            if (l.getNgayThu() > ngayThu) {
                l.setNgayThu(l.getNgayThu() - 1);
                repository.save(l);
            }
        }
    }
}
