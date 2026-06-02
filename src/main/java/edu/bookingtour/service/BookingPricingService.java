package edu.bookingtour.service;

import edu.bookingtour.dto.FlightQuoteResponse;
import edu.bookingtour.entity.ChuyenDi;
import org.springframework.stereotype.Service;

@Service
public class BookingPricingService {

    private static final double ADULT_RATIO = 1.0;
    private static final double CHILD_RATIO = 0.75; // 5-11
    private static final double SMALL_CHILD_RATIO = 0.5; // 2-4
    private static final double BABY_RATIO = 0.0; // <2
    private static final double DEFAULT_SINGLE_ROOM_SURCHARGE = 500000.0;

    public double getSingleRoomSurcharge(ChuyenDi tour) {
        if (tour == null || tour.getIdNoiLuuTru() == null || tour.getIdNoiLuuTru().getGiaPhongDon() == null) {
            return DEFAULT_SINGLE_ROOM_SURCHARGE;
        }
        return tour.getIdNoiLuuTru().getGiaPhongDon().doubleValue();
    }

    public BookingPriceBreakdown calculate(ChuyenDi tour,
                                           FlightQuoteResponse quote,
                                           int adults,
                                           int children,
                                           int smallChildren,
                                           int babies,
                                           int singleRoomCount) {
        int normalizedAdults = Math.max(0, adults);
        int normalizedChildren = Math.max(0, children);
        int normalizedSmallChildren = Math.max(0, smallChildren);
        int normalizedBabies = Math.max(0, babies);
        int totalGuests = normalizedAdults + normalizedChildren + normalizedSmallChildren + normalizedBabies;
        int maxSingleRoom = normalizedAdults;
        int normalizedSingleRoom = Math.max(0, Math.min(singleRoomCount, maxSingleRoom));
        double singleRoomSurcharge = getSingleRoomSurcharge(tour);

        double tourPrice = quote != null ? quote.getGiaTour() : 0;
        double adultTicket = quote != null ? quote.getTongGiaVe() : 0;
        double outboundAdultTicket = quote != null ? quote.getGiaVeDi() : 0;
        double returnAdultTicket = quote != null ? quote.getGiaVeVe() : 0;
        boolean isAirline = isAirlineTour(tour);
        boolean domestic = isDomesticTour(tour);
        String airline = resolveAirline(tour);

        double adultPrice = (tourPrice * ADULT_RATIO) + adultTicket;
        double childTourPrice = tourPrice * CHILD_RATIO;
        double smallChildTourPrice = tourPrice * SMALL_CHILD_RATIO;
        double babyTourPrice = tourPrice * BABY_RATIO;

        double childTicket = isAirline
                ? calculateAirlineTicketForChild(airline, domestic, outboundAdultTicket, returnAdultTicket)
                : adultTicket;
        double smallChildTicket = isAirline
                ? calculateAirlineTicketForChild(airline, domestic, outboundAdultTicket, returnAdultTicket)
                : adultTicket;
        double babyTicket = isAirline
                ? calculateAirlineTicketForInfant(airline, domestic, outboundAdultTicket, returnAdultTicket)
                : 0;

        double childPrice = childTourPrice + childTicket;
        double smallChildPrice = smallChildTourPrice + smallChildTicket;
        double babyPrice = babyTourPrice + babyTicket;

        double ageSubtotal = (normalizedAdults * adultPrice)
                + (normalizedChildren * childPrice)
                + (normalizedSmallChildren * smallChildPrice)
                + (normalizedBabies * babyPrice);
        double singleRoomTotal = normalizedSingleRoom * singleRoomSurcharge;
        double subtotal = ageSubtotal + singleRoomTotal;

        return new BookingPriceBreakdown(
                normalizedAdults,
                normalizedChildren,
                normalizedSmallChildren,
                normalizedBabies,
                totalGuests,
                normalizedSingleRoom,
                tourPrice,
                adultTicket,
                adultPrice,
                childPrice,
                smallChildPrice,
                babyPrice,
                singleRoomSurcharge,
                ageSubtotal,
                singleRoomTotal,
                subtotal,
                airline,
                domestic
        );
    }

    private boolean isAirlineTour(ChuyenDi tour) {
        if (tour == null || tour.getIdPhuongTien() == null || tour.getIdPhuongTien().getLoai() == null) {
            return true;
        }
        String loai = tour.getIdPhuongTien().getLoai().toLowerCase();
        return !loai.contains("bus") && !loai.contains("xe");
    }

    private String resolveAirline(ChuyenDi tour) {
        if (tour == null || tour.getIdPhuongTien() == null || tour.getIdPhuongTien().getHang() == null) {
            return "";
        }
        return tour.getIdPhuongTien().getHang().trim().toLowerCase();
    }

    private boolean isDomesticTour(ChuyenDi tour) {
        if (tour == null || tour.getIdDiemDen() == null || tour.getIdDiemDen().getQuocGia() == null) {
            return true;
        }
        String quocGia = tour.getIdDiemDen().getQuocGia().toLowerCase();
        return quocGia.contains("việt") || quocGia.contains("viet") || quocGia.contains("vn");
    }

    private double calculateAirlineTicketForChild(String airline, boolean domestic, double outboundAdultTicket, double returnAdultTicket) {
        if (isVietnamAirlines(airline)) {
            double ratio = domestic ? 0.9 : 0.75;
            return (outboundAdultTicket + returnAdultTicket) * ratio;
        }
        if (isVietjet(airline)) {
            return outboundAdultTicket + returnAdultTicket;
        }
        if (isBamboo(airline)) {
            return (outboundAdultTicket + returnAdultTicket) * 0.75;
        }
        // fallback: áp mức trung bình 75%
        return (outboundAdultTicket + returnAdultTicket) * 0.75;
    }

    private double calculateAirlineTicketForInfant(String airline, boolean domestic, double outboundAdultTicket, double returnAdultTicket) {
        if (isVietnamAirlines(airline)) {
            return (outboundAdultTicket + returnAdultTicket) * 0.1;
        }
        if (isVietjet(airline)) {
            return domestic ? 100_000 * 2.0 : 200_000 * 2.0;
        }
        if (isBamboo(airline)) {
            return domestic ? 100_000 * 2.0 : 250_000 * 2.0;
        }
        return domestic ? 100_000 * 2.0 : 200_000 * 2.0;
    }

    private boolean isVietnamAirlines(String airline) {
        return airline.contains("vietnam airlines") || airline.contains("vna");
    }

    private boolean isVietjet(String airline) {
        return airline.contains("vietjet");
    }

    private boolean isBamboo(String airline) {
        return airline.contains("bamboo");
    }

    public static class BookingPriceBreakdown {
        private final int adults;
        private final int children;
        private final int smallChildren;
        private final int babies;
        private final int totalGuests;
        private final int singleRoomCount;
        private final double tourPrice;
        private final double adultTicket;
        private final double adultPrice;
        private final double childPrice;
        private final double smallChildPrice;
        private final double babyPrice;
        private final double singleRoomSurcharge;
        private final double ageSubtotal;
        private final double singleRoomTotal;
        private final double subtotal;
        private final String airline;
        private final boolean domestic;

        public BookingPriceBreakdown(int adults, int children, int smallChildren, int babies, int totalGuests,
                                     int singleRoomCount, double tourPrice, double adultTicket,
                                     double adultPrice, double childPrice, double smallChildPrice,
                                     double babyPrice, double singleRoomSurcharge, double ageSubtotal,
                                     double singleRoomTotal, double subtotal, String airline, boolean domestic) {
            this.adults = adults;
            this.children = children;
            this.smallChildren = smallChildren;
            this.babies = babies;
            this.totalGuests = totalGuests;
            this.singleRoomCount = singleRoomCount;
            this.tourPrice = tourPrice;
            this.adultTicket = adultTicket;
            this.adultPrice = adultPrice;
            this.childPrice = childPrice;
            this.smallChildPrice = smallChildPrice;
            this.babyPrice = babyPrice;
            this.singleRoomSurcharge = singleRoomSurcharge;
            this.ageSubtotal = ageSubtotal;
            this.singleRoomTotal = singleRoomTotal;
            this.subtotal = subtotal;
            this.airline = airline;
            this.domestic = domestic;
        }

        public int getAdults() {
            return adults;
        }

        public int getChildren() {
            return children;
        }

        public int getSmallChildren() {
            return smallChildren;
        }

        public int getBabies() {
            return babies;
        }

        public int getTotalGuests() {
            return totalGuests;
        }

        public int getSingleRoomCount() {
            return singleRoomCount;
        }

        public double getTourPrice() {
            return tourPrice;
        }

        public double getAdultTicket() {
            return adultTicket;
        }

        public double getAdultPrice() {
            return adultPrice;
        }

        public double getChildPrice() {
            return childPrice;
        }

        public double getSmallChildPrice() {
            return smallChildPrice;
        }

        public double getBabyPrice() {
            return babyPrice;
        }

        public double getSingleRoomSurcharge() {
            return singleRoomSurcharge;
        }

        public double getAgeSubtotal() {
            return ageSubtotal;
        }

        public double getSingleRoomTotal() {
            return singleRoomTotal;
        }

        public double getSubtotal() {
            return subtotal;
        }

        public String getAirline() {
            return airline;
        }

        public boolean isDomestic() {
            return domestic;
        }
    }
}
