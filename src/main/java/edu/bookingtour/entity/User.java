package edu.bookingtour.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "ten_dang_nhap")
    private String tenDangNhap;

    @Column(name = "ngay_tao")
    private Instant ngayTao;

    @Column(name = "vai_tro", nullable = false, length = 20)
    private String vaiTro;

    @Column(name = "ho_ten")
    private String hoTen;

}