package edu.bookingtour;

import edu.bookingtour.config.EnvBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookingTourApplication {

    public static void main(String[] args) {
        EnvBootstrap.loadDotEnv();
        SpringApplication.run(BookingTourApplication.class, args);
    }

}