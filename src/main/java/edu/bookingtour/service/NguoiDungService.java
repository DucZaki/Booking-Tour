package edu.bookingtour.service;

import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.repo.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NguoiDungService {
    @Autowired
    private NguoiDungRepository nguoiDungRepository;
    public List<String> findhotenbinhluan() {
        return nguoiDungRepository.findTatCaHoTen();
    }
    public Optional<NguoiDung> findById(Integer id) {
        return nguoiDungRepository.findById(id);
    }
    public List<NguoiDung> findAll() {
        return nguoiDungRepository.findAll();
    }
    public NguoiDung save(NguoiDung nguoiDung) {
        NguoiDung user = new NguoiDung();
        user.setTenDangNhap(nguoiDung.getTenDangNhap());
        user.setEmail(nguoiDung.getEmail());
        user.setMatKhau(nguoiDung.getMatKhau());
        user.setVaiTro(nguoiDung.getVaiTro());
        user.setHoTen(nguoiDung.getHoTen());
        user.setNumber(nguoiDung.getNumber());
        return nguoiDungRepository.save(nguoiDung);
    }
    public NguoiDung update(Integer id,NguoiDung nguoiDung) {
        NguoiDung user = nguoiDungRepository.findById(id).orElseThrow(()->new RuntimeException("Nguoi Dung id not found"));
        user.setTenDangNhap(nguoiDung.getTenDangNhap());
        user.setEmail(nguoiDung.getEmail());
        if(nguoiDung.getMatKhau() != null && !nguoiDung.getMatKhau().isEmpty()){
            user.setMatKhau(nguoiDung.getMatKhau());
        }
        user.setVaiTro(nguoiDung.getVaiTro());
        user.setHoTen(nguoiDung.getHoTen());
        user.setNumber(nguoiDung.getNumber());
        return nguoiDungRepository.save(user);
    }

    public void deleteById(int id) {
        nguoiDungRepository.deleteById(id);
    }
}
