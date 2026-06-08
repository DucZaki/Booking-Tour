package edu.bookingtour.entity;

public enum TrangThaiDoan {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED;

    public String getLabel() {
        return switch (this) {
            case SCHEDULED -> "Sắp diễn ra";
            case IN_PROGRESS -> "Đang diễn ra";
            case COMPLETED -> "Đã hoàn thành";
        };
    }

    public static TrangThaiDoan fromDb(String value) {
        if (value == null || value.isBlank()) {
            return SCHEDULED;
        }
        try {
            return TrangThaiDoan.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return SCHEDULED;
        }
    }
}
