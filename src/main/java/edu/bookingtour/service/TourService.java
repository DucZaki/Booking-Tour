package edu.bookingtour.service;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.repo.ChuyenDiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TourService {
    @Autowired
    private ChuyenDiRepository repo;

    public List<ChuyenDi> findAll() {
        return repo.findAll();
    }

    public long count() {
        return repo.count();
    }

    public ChuyenDi findById(Integer id) {
        return repo.findById(id).orElse(null);
    }
}