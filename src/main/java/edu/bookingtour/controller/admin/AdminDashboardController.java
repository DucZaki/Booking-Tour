package edu.bookingtour.controller.admin;

import edu.bookingtour.repo.DashboardRepository;
import edu.bookingtour.repo.NguoiDungRepository;
import edu.bookingtour.repo.ChuyenDiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private ChuyenDiRepository chuyenDiRepository;

    @GetMapping
    public String index(Model model) {
        // KPI Cards
        model.addAttribute("totalBookings", dashboardRepository.countTotalBookings());
        model.addAttribute("successBookings", dashboardRepository.countSuccessfulBookings());
        model.addAttribute("failedBookings", dashboardRepository.countFailedBookings());
        Double totalRevenue = dashboardRepository.calculateTotalRevenue();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        model.addAttribute("totalUsers", nguoiDungRepository.count());
        model.addAttribute("totalTours", chuyenDiRepository.count());

        // Top Selling Tours
        List<Object[]> topTours = dashboardRepository.findTopSellingTours();
        model.addAttribute("topTours", topTours);

        // Chart Data: Monthly Revenue (Current Year)
        int currentYear = LocalDate.now().getYear();
        List<Object[]> monthlyData = dashboardRepository.findMonthlyRevenueByYear(currentYear);
        Map<Integer, Double> revenueMap = new HashMap<>();
        IntStream.rangeClosed(1, 12).forEach(i -> revenueMap.put(i, 0.0));
        for (Object[] row : monthlyData) {
            revenueMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }
        
        List<Integer> months = new ArrayList<>();
        List<Double> revenues = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(i);
            revenues.add(revenueMap.get(i));
        }
        model.addAttribute("chartMonths", months);
        model.addAttribute("chartRevenues", revenues);

        // Chart Data: Booking Status
        List<Object[]> statusData = dashboardRepository.findBookingStatusDistribution();
        List<String> statusLabels = new ArrayList<>();
        List<Long> statusCounts = new ArrayList<>();
        for (Object[] row : statusData) {
            statusLabels.add((String) row[0]);
            statusCounts.add(((Number) row[1]).longValue());
        }
        model.addAttribute("statusLabels", statusLabels);
        model.addAttribute("statusCounts", statusCounts);

        // User Spending Stats
        List<Object[]> userSpendingData = dashboardRepository.findUserSpendingStats();
        model.addAttribute("userSpendingStats", userSpendingData);

        return "admin/dashboard";
    }

    // REST endpoint: lấy danh sách khách hàng đã mua theo tourId
    @GetMapping("/tour-bookings/{tourId}")
    @ResponseBody
    public List<Object[]> getTourBookings(@PathVariable Integer tourId) {
        return dashboardRepository.findBookingDetailsByTourId(tourId);
    }

    // REST endpoint: lấy dữ liệu doanh thu theo tháng của một năm cụ thể
    @GetMapping("/api/revenue/monthly")
    @ResponseBody
    public Map<String, Object> getMonthlyRevenueApi(@RequestParam int year) {
        List<Object[]> monthlyData = dashboardRepository.findMonthlyRevenueByYear(year);
        Map<Integer, Double> revenueMap = new HashMap<>();
        IntStream.rangeClosed(1, 12).forEach(i -> revenueMap.put(i, 0.0));
        for (Object[] row : monthlyData) {
            revenueMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }

        List<String> months = new ArrayList<>();
        List<Double> revenues = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add("Tháng " + i);
            revenues.add(revenueMap.get(i));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("labels", months);
        response.put("data", revenues);
        response.put("year", year);
        return response;
    }

    @GetMapping("/revenue")
    public String revenueDetail(Model model, @RequestParam(required = false) Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        
        List<Object[]> weeklyRaw = dashboardRepository.findWeeklyRevenueByYear(targetYear);
        List<Object[]> monthlyRaw = dashboardRepository.findMonthlyRevenueByYear(targetYear);
        List<Object[]> yearlyRaw = dashboardRepository.findYearlyRevenue();

        model.addAttribute("selectedYear", targetYear);
        model.addAttribute("weeklyRevenue", weeklyRaw);
        model.addAttribute("monthlyRevenue", monthlyRaw);
        model.addAttribute("yearlyRevenue", yearlyRaw);

        // Ensure 12 months for Monthly Chart
        Map<Integer, Double> monthlyMap = new HashMap<>();
        IntStream.rangeClosed(1, 12).forEach(i -> monthlyMap.put(i, 0.0));
        for (Object[] row : monthlyRaw) {
            monthlyMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }

        List<String> monthlyLabels = new ArrayList<>();
        List<Double> monthlyData = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            monthlyLabels.add("Tháng " + i);
            monthlyData.add(monthlyMap.get(i));
        }

        // Map for JS Charts
        model.addAttribute("weeklyLabels", weeklyRaw.stream().map(r -> "Tuần " + r[0]).toList());
        model.addAttribute("weeklyData", weeklyRaw.stream().map(r -> r[1]).toList());
        model.addAttribute("monthlyLabels", monthlyLabels);
        model.addAttribute("monthlyData", monthlyData);
        model.addAttribute("yearlyLabels", yearlyRaw.stream().map(r -> "Năm " + r[0]).toList());
        model.addAttribute("yearlyData", yearlyRaw.stream().map(r -> r[1]).toList());

        model.addAttribute("bookings", dashboardRepository.findAllBookingDetails());
        Double totalRevenue = dashboardRepository.calculateTotalRevenue();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        return "admin/revenue";
    }

    @GetMapping("/bookings")
    public String bookingsDetail(Model model, 
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> bookingsPage = dashboardRepository.findAllBookingDetails(pageable);
        
        model.addAttribute("bookings", bookingsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookingsPage.getTotalPages());
        model.addAttribute("pageSize", size);
        
        return "admin/bookings";
    }

    @GetMapping("/tour-performance")
    public String tourPerformance(Model model) {
        List<Object[]> tours = dashboardRepository.findTopSellingTours();
        model.addAttribute("tours", tours);
        
        List<String> tourLabels = new ArrayList<>();
        List<Long> tourData = new ArrayList<>();
        for (Object[] row : tours) {
            tourLabels.add((String) row[1]);
            tourData.add(((Number) row[2]).longValue());
        }
        model.addAttribute("tourLabels", tourLabels);
        model.addAttribute("tourData", tourData);

        Long totalBookings = dashboardRepository.countTotalBookings();
        model.addAttribute("totalBookings", totalBookings != null ? totalBookings : 0L);
        model.addAttribute("successBookings", dashboardRepository.countSuccessfulBookings());
        return "admin/tour-performance";
    }

    @GetMapping("/users-detail")
    public String usersDetail(Model model) {
        model.addAttribute("users", dashboardRepository.findAllUserDetails());
        return "admin/users-detail";
    }
}
