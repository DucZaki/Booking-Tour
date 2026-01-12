package edu.bookingtour.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
public class SecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/**").permitAll() // Cho phép truy cập TẤT CẢ các trang
                            // Hoặc bạn có thể chỉ định cụ thể: .requestMatchers("/", "/home", "/css/**").permitAll()
                            .anyRequest().authenticated()
                    )
                    .formLogin(login -> login.disable()) // Tắt trang login mặc định
                    .csrf(csrf -> csrf.disable()); // Tắt bảo vệ CSRF (để test API dễ hơn)

            return http.build();
        }

}