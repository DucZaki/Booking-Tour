package edu.bookingtour.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class TravelPayoutsClient {

    private final String TOKEN = "f2f7610a909d5c25b8440e1e89e23148";
    private final String API_URL = "https://api.travelpayouts.com/aviasales/v3/prices_for_dates";
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Lấy giá vé rẻ nhất và định dạng sang VND
     */
    public double getCheapestPrice(String origin, String destination, String departureDate) {
        try {
            List<Map<String, Object>> flights = getFlightsFromApi(origin, destination, departureDate);

            if (!flights.isEmpty()) {
                // API đã sắp xếp giá rẻ nhất lên đầu, lấy phần tử số 0
                Object priceObj = flights.get(0).get("price");
                return Double.parseDouble(priceObj.toString());
            }
        } catch (Exception e) {
            System.err.println("Lỗi TravelPayouts API: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Lấy mã hãng hàng không (Airline Code)
     */
    public String getCarrierCode(String origin, String destination, String departureDate) {
        try {
            List<Map<String, Object>> flights = getFlightsFromApi(origin, destination, departureDate);
            if (!flights.isEmpty()) {
                return (String) flights.get(0).get("airline");
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy thông tin hãng bay: " + e.getMessage());
        }
        return "N/A";
    }

    /**
     * Hàm dùng chung để gọi API và trả về danh sách "data"
     */
    private List<Map<String, Object>> getFlightsFromApi(String origin, String destination, String date) throws Exception {
        // Xây dựng URL với các tham số cần thiết
        String url = String.format(
                "%s?origin=%s&destination=%s&departure_at=%s&one_way=true&currency=vnd&token=%s",
                API_URL, origin, destination, date, TOKEN);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Map<String, Object> json = mapper.readValue(response.body(), Map.class);

        if (json.containsKey("data") && json.get("data") instanceof List) {
            return (List<Map<String, Object>>) json.get("data");
        }

        return Collections.emptyList();
    }

    public String formatPriceToK(double price) {
        if (price == 0) return "N/A";
        double kValue = price / 1000;
        return String.format("%,.0fK", kValue);
    }

}