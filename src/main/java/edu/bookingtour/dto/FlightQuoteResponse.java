package edu.bookingtour.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FlightQuoteResponse {
    private final boolean available;
    private final String message;
    private final Integer diemDonId;
    private final String diemDonTen;
    private final String diemDenTen;
    private final double giaVeDi;
    private final double giaVeVe;
    private final double tongGiaVe;
    private final double giaTour;
    private final double unitPrice;
    private final String maChuyenBayDi;
    private final String gioBayDi;
    private final String gioDenDi;
    private final String maChuyenBayVe;
    private final String gioBayVe;
    private final String gioDenVe;
    private final String ngayDi;
    private final String ngayVe;

    public static FlightQuoteResponse unavailable(String message) {
        return FlightQuoteResponse.builder()
                .available(false)
                .message(message)
                .build();
    }
}
