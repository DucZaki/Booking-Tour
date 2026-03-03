package edu.bookingtour.config;

import edu.bookingtour.entity.DiemDon;
import edu.bookingtour.repo.DiemDonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DiemDonRepository diemDonRepository;

    @Override
    public void run(String... args) throws Exception {
        if (diemDonRepository.findByTen("Hà Nội").isEmpty()) {
            diemDonRepository.save(new DiemDon("Hà Nội"));
        }
    }
}
