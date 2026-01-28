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
    private static final String TOKEN = "f4576b6c4b744b5bbb4306309ade8ff1";
    private static final String API_URL = "https://newsapi.org/v2/everything";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public List<ArticleDTO> getLatestNews() {
        try {
            String query = "travel OR tourism OR destination";
            return getNews(query);
        } catch (Exception e) {
            System.err.println("Lỗi lấy tin: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<ArticleDTO> getNews(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = API_URL
                + "?q=" + encodedQuery
                + "&language=en"
                + "&sortBy=publishedAt"
                + "&pageSize=12"
                + "&apiKey=" + TOKEN;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("API Status: " + response.statusCode());

        if (response.statusCode() == 200) {
            NewsResponseDTO newsResponse =
                    mapper.readValue(response.body(), NewsResponseDTO.class);

            return newsResponse.getArticles() != null
                    ? newsResponse.getArticles()
                    : Collections.emptyList();
        }

        System.out.println("API Error: " + response.body());
        return Collections.emptyList();
    }
}
