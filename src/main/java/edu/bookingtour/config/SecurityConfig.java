package edu.bookingtour.config;

import edu.bookingtour.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/anh/**",
                                "/images/**",
                                "/",
                                "/tour/**",
                                "/api/public/**",
                                "/tintuc",
                                "/tin-tuc",
                                "slogan.png"
                        ).permitAll()

                        // Admin only endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // User and Admin endpoints
                        .requestMatchers("/user/**", "/booking/**").hasAnyRole("USER", "ADMIN")

                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform-login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/redirect-after-login", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
//                        .defaultSuccessUrl("/")
                )
//                .rememberMe(remember -> remember
//                        .key("zaki-booking-remember-me")
//                        .rememberMeParameter("remember-me")
//                        .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 ngày
//                        .userDetailsService(userDetailsService)
//                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                )

                .sessionManagement(session -> session
                        .sessionFixation().changeSessionId()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                );

        return http.build();
    }
}