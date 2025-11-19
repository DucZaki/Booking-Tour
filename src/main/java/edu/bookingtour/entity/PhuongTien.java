package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "phuong_tien")
public class PhuongTien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Lob
    @Column(name = "loai")
    private String loai;

    @Column(name = "hang")
    private String hang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_diem_den")
    private DiemDen idDiemDen;

}