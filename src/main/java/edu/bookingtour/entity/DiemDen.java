package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "diem_den")
public class DiemDen {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String thanh_pho;
    private String chau_luc;
    private String mo_ta;
    private String hinh_anh;
    @Column(name="noi_bat")
    private boolean noibat;

}
