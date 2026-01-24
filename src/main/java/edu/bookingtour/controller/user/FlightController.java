package edu.bookingtour.controller.user;

import edu.bookingtour.client.TravelPayoutsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights")
public class FlightController {
    @Autowired
    private TravelPayoutsClient travelPayoutsClient;

    @GetMapping("/price")
    public double getPrice(@RequestParam String from, @RequestParam String to, @RequestParam String date) {
        double price = travelPayoutsClient.getCheapestPrice(from, to, date);
        return price;
    }
}