package edu.bookingtour.service;

import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.repo.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NguoiDungService {
    @Autowired
    private NguoiDungRepository nguoiDungRepository;
    public List<String> findhotenbinhluan() {
      return nguoiDungRepository.findTatCaHoTen();
    }
}
