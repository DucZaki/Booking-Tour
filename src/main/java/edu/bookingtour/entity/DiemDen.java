package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "diem_den")
public class DiemDen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "thanh_pho")
    private String thanhPho;

    @Column(name = "quoc_gia")
    private String quocGia;

    @Column(name = "chau_luc")
    private String chauLuc;

    @Column(name = "hinh_anh")
    private String hinhAnh;

    @ColumnDefault("0")
    @Column(name = "noi_bat")
    private Boolean noiBat;

    @Column(name = "mo_ta")
    private String moTa;

}