package edu.bookingtour.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("12345"));
        System.out.println(encoder.encode("pass123"));
    }
}