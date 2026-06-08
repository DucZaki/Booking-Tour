package edu.bookingtour.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Calendar {
    private LocalDate date;
    private boolean currentMonth;
    private double flightPrice;
    private boolean selected;
    private boolean pastDay;
    private boolean hasDeparture;
    private Integer ngayKhoiHanhId;
    private int remainingGuests;
    private boolean soldOut;
    /** Đã qua hạn đặt vé (trước giờ tập trung 24h). */
    private boolean bookingClosed;
}