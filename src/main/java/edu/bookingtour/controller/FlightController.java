package edu.bookingtour.controller;

import edu.bookingtour.client.AmadeusClient;
import edu.bookingtour.entity.ChuyenDi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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