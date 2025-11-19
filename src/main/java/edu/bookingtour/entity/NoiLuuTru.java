package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "noi_luu_tru")
public class NoiLuuTru {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ten")
    private String ten;

    @Column(name = "loai", length = 100)
    private String loai;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "gia", precision = 10, scale = 2)
    private BigDecimal gia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_diem_den")
    private DiemDen idDiemDen;

}