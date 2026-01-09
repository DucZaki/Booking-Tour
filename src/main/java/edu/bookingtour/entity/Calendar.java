package edu.bookingtour.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class Calendar {
    private LocalDate date;
    private boolean isCurrentMonth;
    private double flightPrice;
    private boolean isSelected;

}