package edu.bookingtour.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TourCardStatsView {
    private final double averageRating;
    private final long ratingCount;
    private final long bookingCount;

    public String formattedRating() {
        return String.format(java.util.Locale.ROOT, "%.1f", averageRating);
    }

    public String getFormattedRating() {
        return formattedRating();
    }

    public String formattedRatingCount() {
        return formatCountCap99(ratingCount);
    }

    public String getFormattedRatingCount() {
        return formattedRatingCount();
    }

    public String formattedBookingCount() {
        return formatCountCap99(bookingCount);
    }

    public String getFormattedBookingCount() {
        return formattedBookingCount();
    }

    private static String formatCountCap99(long n) {
        if (n >= 99) {
            return "99+";
        }
        return String.valueOf(n);
    }
}
