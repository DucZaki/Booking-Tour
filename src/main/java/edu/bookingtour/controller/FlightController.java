package edu.bookingtour.controller;

import edu.bookingtour.client.AmadeusClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights")
public class FlightController {
    @Autowired
    private AmadeusClient amadeusClient;

    @GetMapping("/price")
    public String getPrice(@RequestParam String from, @RequestParam String to, @RequestParam String date) {
        String price = amadeusClient.getCheapestPriceFormatted(from, to, date);
        return (price != null) ? price : "N/A";
    }
}