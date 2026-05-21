package edu.bookingtour.dto;

import edu.bookingtour.entity.MaGiamGia;
import lombok.Getter;

@Getter
public class PromoApplyResult {
    private final boolean valid;
    private final String message;
    private final MaGiamGia maGiamGia;
    private final double subtotal;
    private final double discount;
    private final double total;

    private PromoApplyResult(boolean valid, String message, MaGiamGia maGiamGia,
                             double subtotal, double discount, double total) {
        this.valid = valid;
        this.message = message;
        this.maGiamGia = maGiamGia;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
    }

    public static PromoApplyResult invalid(String message) {
        return new PromoApplyResult(false, message, null, 0, 0, 0);
    }

    public static PromoApplyResult ok(MaGiamGia m, double subtotal, double discount, double total) {
        return new PromoApplyResult(true, "Áp dụng mã thành công", m, subtotal, discount, total);
    }
}
