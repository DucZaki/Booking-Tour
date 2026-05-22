package edu.bookingtour.config;

import edu.bookingtour.entity.DiemDon;
import edu.bookingtour.repo.DiemDonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.runners.init-data", havingValue = "true", matchIfMissing = false)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private DiemDonRepository diemDonRepository;

    @Override
    public void run(String... args) {
        try {
            if (diemDonRepository.findByTen("Hà Nội").isEmpty()) {
                diemDonRepository.save(new DiemDon("Hà Nội"));
            }
        } catch (Exception e) {
            log.error("DataInitializer thất bại, bỏ qua để không ảnh hưởng khởi động.", e);
        }
    }
}
