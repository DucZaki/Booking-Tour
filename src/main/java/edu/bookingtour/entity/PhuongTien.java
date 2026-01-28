package edu.bookingtour.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "phuong_tien")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_diem_den")
    private DiemDen idDiemDen;

}