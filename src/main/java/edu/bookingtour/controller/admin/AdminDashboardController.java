package edu.bookingtour.controller.admin;

import edu.bookingtour.repo.DashboardRepository;
import edu.bookingtour.repo.NguoiDungRepository;
import edu.bookingtour.repo.ChuyenDiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // Chart Data: Monthly Revenue
        List<Object[]> monthlyData = dashboardRepository.findMonthlyRevenue();
        List<Integer> months = new ArrayList<>();
        List<Double> revenues = new ArrayList<>();
        // Initialize with zeros for all 12 months
        Map<Integer, Double> revenueMap = new HashMap<>();
        for (int i = 1; i <= 12; i++)
            revenueMap.put(i, 0.0);
        for (Object[] row : monthlyData) {
            revenueMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }
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

        return "admin/dashboard";
    }
}
