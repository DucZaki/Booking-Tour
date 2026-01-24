package edu.bookingtour.service;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.entity.YeuThich;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.repo.FavoriteRepository;
import edu.bookingtour.repo.NguoiDungRepository;
import edu.bookingtour.service.NguoiDungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FavoriteService {
    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private NguoiDungService nguoiDungservice;

    @Autowired
    private TourService TourService;

    @Autowired
    private FavoriteRepository yeuThichRepository;

    public void saveFavorite(Integer tourid, String username){
        NguoiDung user = nguoiDungservice.findByTenDangNhap(username).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: "));
        ChuyenDi tour = TourService.findByIdd(tourid);
        YeuThich favorite = new  YeuThich();
        favorite.setIdChuyenDi(tour);
        favorite.setIdNguoiDung(user);
        yeuThichRepository.save(favorite);
    }
    public void deleteFavorite(Integer id){
       yeuThichRepository.deleteById(id);
    }
}
