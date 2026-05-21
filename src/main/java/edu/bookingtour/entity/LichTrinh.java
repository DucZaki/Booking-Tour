package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lich_trinh")
public class LichTrinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tour_id", nullable = false)
    private Integer tourId;

    @Column(name = "ngay_thu")
    private Integer ngayThu;

    /** Tuyến / điểm đến trong ngày, VD: Tp.HCM – Nha Trang */
    @Column(name = "tieu_de")
    private String tieuDe;

    @Column(name = "so_bua_an")
    private String soBuaAn;

    /** Mỗi dòng = một hoạt động (hiển thị bullet trên UI) */
    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "nghi_dem")
    private String nghiDem;

    /** Tóm tắt hoạt động chính trong ngày */
    @Column(name = "hoat_dong_chinh", length = 500)
    private String hoatDongChinh;

    /** Ảnh minh họa cho ngày (URL) */
    @Column(name = "hinh_anh", length = 500)
    private String hinhAnh;

    @Transient
    public List<String> getNoiDungLines() {
        if (noiDung == null || noiDung.isBlank()) {
            return List.of();
        }
        return Arrays.stream(noiDung.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Transient
    public String resolvedHoatDongChinh() {
        if (hoatDongChinh != null && !hoatDongChinh.isBlank()) {
            return hoatDongChinh.trim();
        }
        List<String> lines = getNoiDungLines();
        return lines.isEmpty() ? null : lines.get(0);
    }
}
