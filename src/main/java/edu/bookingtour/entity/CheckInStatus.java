package edu.bookingtour.entity;

public enum CheckInStatus {
    PENDING,
    CHECKED_IN,
    LATE,
    NO_SHOW,
    CANCELLED_LAST_MINUTE;

    public String getLabel() {
        return switch (this) {
            case PENDING -> "Chưa đến";
            case CHECKED_IN -> "Đã check-in";
            case LATE -> "Đến muộn";
            case NO_SHOW -> "Vắng mặt";
            case CANCELLED_LAST_MINUTE -> "Hủy phút chót";
        };
    }

    public static CheckInStatus fromDb(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }
        try {
            return CheckInStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return PENDING;
        }
    }
}
