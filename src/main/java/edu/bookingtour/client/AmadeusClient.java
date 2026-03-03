package edu.bookingtour.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class AmadeusClient {

    @Value("${amadeus.api.key}")
    private String apiKey;

    @Value("${amadeus.api.secret}")
    private String apiSecret;

    @Value("${amadeus.api.base-url}")
    private String baseUrl;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    private String accessToken;
    private Instant tokenExpiry;

    /**
     * Lấy access token từ Amadeus OAuth2
     */
    private synchronized String getAccessToken() throws Exception {
        if (accessToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            return accessToken;
        }

        String body = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(apiSecret, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/security/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Amadeus OAuth2 failed: " + response.body());
        }

        JsonNode json = mapper.readTree(response.body());
        accessToken = json.get("access_token").asText();
        int expiresIn = json.get("expires_in").asInt();
        tokenExpiry = Instant.now().plusSeconds(expiresIn - 60); // refresh 1 phút trước khi hết hạn

        return accessToken;
    }

    /**
     * Tìm vé máy bay rẻ nhất cho ngày cụ thể
     * Trả về Map chứa: price, airline, flightNumber, departureTime, arrivalTime
     */
    public Map<String, Object> getCheapestFlight(String origin, String destination, String departureDate) {
        Map<String, Object> result = new HashMap<>();
        try {
            String token = getAccessToken();

            String url = String.format(
                    "%s/v2/shopping/flight-offers?originLocationCode=%s&destinationLocationCode=%s&departureDate=%s&adults=1&currencyCode=VND&max=1",
                    baseUrl, origin, destination, departureDate);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Amadeus API error: " + response.statusCode() + " - " + response.body());
                return result;
            }

            JsonNode json = mapper.readTree(response.body());
            JsonNode data = json.get("data");

            if (data != null && data.isArray() && data.size() > 0) {
                JsonNode offer = data.get(0);

                // Giá vé
                double price = offer.get("price").get("grandTotal").asDouble();
                result.put("price", price);

                // Thông tin chuyến bay đầu tiên (segment đầu tiên)
                JsonNode segments = offer.get("itineraries").get(0).get("segments");
                if (segments != null && segments.size() > 0) {
                    JsonNode segment = segments.get(0);
                    String airline = segment.get("carrierCode").asText();
                    String flightNumber = segment.get("number").asText();
                    String departureTime = segment.get("departure").get("at").asText();
                    String arrivalTime = segment.get("arrival").get("at").asText();

                    result.put("airline", airline);
                    result.put("flightNumber", airline + flightNumber);
                    result.put("departureTime", departureTime);
                    result.put("arrivalTime", arrivalTime);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi Amadeus API: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Lấy giá vé rẻ nhất
     */
    public double getCheapestPrice(String origin, String destination, String departureDate) {
        Map<String, Object> flight = getCheapestFlight(origin, destination, departureDate);
        if (flight.containsKey("price")) {
            return (double) flight.get("price");
        }
        return 0;
    }

    /**
     * Lấy mã chuyến bay
     */
    public String getFlightNumber(String origin, String destination, String departureDate) {
        Map<String, Object> flight = getCheapestFlight(origin, destination, departureDate);
        return (String) flight.getOrDefault("flightNumber", "N/A");
    }

    /**
     * Format giá tiền sang dạng K
     */
    public String formatPriceToK(double price) {
        if (price == 0)
            return "N/A";
        double kValue = price / 1000;
        return String.format("%,.0fK", kValue);
    }
}
