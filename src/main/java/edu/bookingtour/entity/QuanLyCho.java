package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "quan_ly_cho")
public class QuanLyCho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chuyen_di")
    private ChuyenDi idChuyenDi;

    @Column(name = "tong_so_cho")
    private Integer tongSoCho;

    @Column(name = "da_dat")
    private Integer daDat;

    @Column(name = "con_lai")
    private Integer conLai;

}