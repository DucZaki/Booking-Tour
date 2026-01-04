package edu.bookingtour.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Component
public class AmadeusClient {
    private static final String TOKEN_URL = "https://test.api.amadeus.com/v1/security/oauth2/token";
    private static final String FLIGHT_URL = "https://test.api.amadeus.com/v2/shopping/flight-offers";

    private final String clientId = "wr7EGAmSiRQG2wMZPGNAf9RbAKbiNBoS";
    private final String clientSecret = "qMVXA4W9eqIXTcus";
    private String accessToken;

    public String login() throws Exception {
        if (accessToken != null) return accessToken;
        HttpClient client = HttpClient.newHttpClient();
        String body = "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(response.body(), Map.class);
        accessToken = (String) data.get("access_token");
        return accessToken;
    }

    public String getCheapestPriceFormatted(String origin, String destination, String date) {
        try {
            String token = login();
            HttpClient client = HttpClient.newHttpClient();
            String url = String.format("%s?originLocationCode=%s&destinationLocationCode=%s&departureDate=%s&adults=1&max=1",
                    FLIGHT_URL, origin, destination, date);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            JsonNode flightOffers = root.path("data");

            if (flightOffers.isArray() && !flightOffers.isEmpty()) {
                double totalPrice = flightOffers.get(0).path("price").path("total").asDouble();
                return formatPriceToK(totalPrice);
            }
        } catch (Exception e) {
            System.err.println("Lỗi API Amadeus: " + e.getMessage());
        }
        return null;
    }

    private String formatPriceToK(double price) {
        double kValue = price / 1000;
        return String.format("%,.0fK", kValue);
    }
}