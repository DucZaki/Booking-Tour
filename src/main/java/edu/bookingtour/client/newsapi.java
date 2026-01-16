package edu.bookingtour.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.bookingtour.dto.ArticleDTO;
import edu.bookingtour.dto.NewsResponseDTO;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
public class newsapi {
    private final String TOKEN = "f4576b6c4b744b5bbb4306309ade8ff1";
    private final String API_URL = "https://newsapi.org/v2/everything";
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public List<ArticleDTO> getLatestNews() {
        try {
            // Từ khóa: Địa điểm nổi tiếng thế giới và Thời tiết
            // Dùng tiếng Anh để ĐẢM BẢO có dữ liệu đổ về
            String query = "travel destinations OR world places OR weather forecast";
            return getNews(query);
        } catch (Exception e) {
            System.err.println("Lỗi thực thi lấy tin: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<ArticleDTO> getNews(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        // Bỏ language=vi, đổi thành language=en để lấy tin quốc tế
        // Sắp xếp theo relevancy (liên quan nhất)
        String url = API_URL + "?q=" + encodedQuery
                + "&language=en"
                + "&sortBy=relevancy"
                + "&pageSize=12" // Lấy 12 bài cho đẹp grid 4x3
                + "&apiKey=" + TOKEN;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Dòng này cực kỳ quan trọng để debug:
        System.out.println("API Status: " + response.statusCode());

        if (response.statusCode() == 200) {
            NewsResponseDTO newsResponse = mapper.readValue(response.body(), NewsResponseDTO.class);

            if (newsResponse.getArticles() == null || newsResponse.getArticles().isEmpty()) {
                System.out.println("API trả về thành công nhưng không có bài báo nào khớp từ khóa.");
                return Collections.emptyList();
            }
            return newsResponse.getArticles();
        } else {
            System.out.println("Lỗi từ NewsAPI: " + response.body());
            return Collections.emptyList();
        }
    }
}