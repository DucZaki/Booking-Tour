package edu.bookingtour.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class User {
    @Id
    private Integer id;
    private String email;
    private String password;
    private String ten_dang_nhap;
    @Column(name = "ngay_tao")
    private LocalDateTime ngay_tao;
    @Column(name = "vai_tro", length = 20, nullable = false)
    private String vai_tro;
    private String ho_ten;

}
