package edu.bookingtour.service;

import edu.bookingtour.entity.DatCho;
import edu.bookingtour.repo.DatChoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DatChoService {
    @Autowired
    private DatChoRepository datChoRepository;

    public DatCho save(DatCho datCho) {
        return datChoRepository.save(datCho);
    }

    public Optional<DatCho> findById(Integer id) {
        return datChoRepository.findById(id);
    }

    public void updateStatus(Integer id, String status) {
        datChoRepository.findById(id).ifPresent(datCho -> {
            datCho.setTrangThai(status);
            datChoRepository.save(datCho);
        });
    }
}
