package edu.bookingtour.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.MaGiamGia;
import edu.bookingtour.repo.DatChoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final List<String> GEMINI_MODELS = List.of(
            "gemini-2.0-flash",
            "gemini-2.0-flash-lite",
            "gemini-1.5-flash-8b"
    );

    private static final String CHAT_ERROR_USER =
            "Xin lỗi, tôi đang gặp sự cố tạm thời. Bạn thử lại sau hoặc liên hệ hotline **+84 866147595** — [form liên hệ](/contact).";

    private static final String CHAT_MAINTENANCE =
            "Chatbot đang bảo trì. Vui lòng thử lại sau hoặc liên hệ hotline **+84 866147595**.";

    private static final String CHAT_CONTENT_FILTER =
            "Không thể trả lời do nội dung bị lọc. Hãy thử câu hỏi khác.";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Value("${groq.api.key:${GROQ_API_KEY:}}")
    private String groqApiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    @Value("${groq.api.model:llama-3.1-8b-instant}")
    private String groqModel;

    @Value("${openrouter.api.key:${OPENROUTER_API_KEY:}}")
    private String openRouterApiKey;

    @Value("${openrouter.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String openRouterApiUrl;

    @Value("${openrouter.api.model:google/gemini-2.0-flash-exp:free}")
    private String openRouterModel;

    @Value("${gemini.api.url:}")
    private String configuredGeminiApiUrl;

    @Value("${GEMINI_API_KEY:${gemini.api.key:}}")
    private String geminiApiKey;

    @Autowired
    private TourService tourService;

    @Autowired
    private DatChoRepository datChoRepository;

    @Autowired
    private MaGiamGiaService maGiamGiaService;

    public String ask(String message) {
        if (message == null || message.isBlank()) {
            return "Vui lòng nhập câu hỏi của bạn.";
        }

        String systemPrompt = constructSystemPrompt(message);
        String userText = message.trim();

        if (groqApiKey != null && !groqApiKey.isBlank()) {
            try {
                String reply = callOpenAiChat(groqApiUrl, groqApiKey, groqModel, systemPrompt, userText, "Groq");
                if (reply != null && !reply.isBlank()) {
                    return reply;
                }
            } catch (ChatApiException ex) {
                log.warn("Groq failed: {} {}", ex.getStatusCode(), ex.getMessage());
                return ex.getUserMessage();
            } catch (Exception ex) {
                log.error("Groq unexpected error", ex);
                return CHAT_ERROR_USER;
            }
        }

        if (openRouterApiKey != null && !openRouterApiKey.isBlank()) {
            try {
                String reply = callOpenAiChat(openRouterApiUrl, openRouterApiKey, openRouterModel, systemPrompt, userText, "OpenRouter");
                if (reply != null && !reply.isBlank()) {
                    return reply;
                }
            } catch (ChatApiException ex) {
                log.warn("OpenRouter failed: {} {}", ex.getStatusCode(), ex.getMessage());
                return ex.getUserMessage();
            } catch (Exception ex) {
                log.error("OpenRouter unexpected error", ex);
                return CHAT_ERROR_USER;
            }
        }

        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return CHAT_MAINTENANCE;
        }

        for (String model : GEMINI_MODELS) {
            try {
                String reply = callGemini(model, systemPrompt, userText);
                if (reply != null && !reply.isBlank()) {
                    return reply;
                }
            } catch (ChatApiException ex) {
                log.warn("Gemini model {} failed: {} {}", model, ex.getStatusCode(), ex.getMessage());
                if (!ex.isRetryable()) {
                    return ex.getUserMessage();
                }
            } catch (Exception ex) {
                log.error("Gemini unexpected error for model {}", model, ex);
            }
        }

        return CHAT_ERROR_USER;
    }

    /** Groq & OpenRouter — OpenAI-compatible chat/completions */
    private String callOpenAiChat(String apiUrl, String apiKey, String model,
                                  String systemPrompt, String userText, String provider) throws Exception {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("model", model);

        ArrayNode messages = mapper.createArrayNode();
        messages.add(mapper.createObjectNode()
                .put("role", "system")
                .put("content", systemPrompt));
        messages.add(mapper.createObjectNode()
                .put("role", "user")
                .put("content", userText));
        payload.set("messages", messages);
        payload.put("temperature", 0.3);
        payload.put("max_tokens", 1024);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey.trim());

        if ("OpenRouter".equals(provider)) {
            builder.header("HTTP-Referer", "http://localhost:8080")
                    .header("X-Title", "ZakiBooking");
        }

        HttpRequest request = builder
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseOpenAiChatResponse(response.statusCode(), response.body(), provider);
    }

    private String parseOpenAiChatResponse(int statusCode, String body, String provider)
            throws ChatApiException, com.fasterxml.jackson.core.JsonProcessingException {
        JsonNode root = mapper.readTree(body != null && !body.isBlank() ? body : "{}");

        if (statusCode == 429) {
            throw new ChatApiException(429, provider + " rate limit: " + body, true);
        }
        if (statusCode == 401 || statusCode == 403) {
            throw new ChatApiException(statusCode, provider + " auth error: " + body, false);
        }
        if (statusCode >= 400) {
            String apiMsg = root.path("error").path("message").asText("");
            throw new ChatApiException(statusCode, provider + " HTTP " + statusCode + ": " + apiMsg, statusCode >= 500);
        }

        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return null;
        }
        return choices.get(0).path("message").path("content").asText("").trim();
    }

    private String callGemini(String model, String systemPrompt, String userText) throws Exception {
        String url = resolveGeminiApiUrl(model);
        ObjectNode payload = mapper.createObjectNode();

        ObjectNode systemInstruction = mapper.createObjectNode();
        ArrayNode systemParts = mapper.createArrayNode();
        systemParts.add(mapper.createObjectNode().put("text", systemPrompt));
        systemInstruction.set("parts", systemParts);
        payload.set("systemInstruction", systemInstruction);

        ObjectNode userContent = mapper.createObjectNode();
        userContent.put("role", "user");
        ArrayNode userParts = mapper.createArrayNode();
        userParts.add(mapper.createObjectNode().put("text", userText));
        userContent.set("parts", userParts);

        ArrayNode contents = mapper.createArrayNode();
        contents.add(userContent);
        payload.set("contents", contents);

        ObjectNode generationConfig = mapper.createObjectNode();
        generationConfig.put("temperature", 0.3);
        generationConfig.put("maxOutputTokens", 1024);
        payload.set("generationConfig", generationConfig);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseGeminiResponse(response.statusCode(), response.body());
    }

    private String resolveGeminiApiUrl(String model) {
        String base = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent";
        if (configuredGeminiApiUrl != null && !configuredGeminiApiUrl.isBlank()) {
            base = configuredGeminiApiUrl.replaceAll("/models/[^/:]+:generateContent",
                    "/models/" + model + ":generateContent");
            if (!base.contains(":generateContent")) {
                base = "https://generativelanguage.googleapis.com/v1beta/models/"
                        + model + ":generateContent";
            }
        }
        return base + "?key=" + geminiApiKey.trim();
    }

    private String parseGeminiResponse(int statusCode, String body) throws ChatApiException, com.fasterxml.jackson.core.JsonProcessingException {
        JsonNode root = mapper.readTree(body != null && !body.isBlank() ? body : "{}");

        if (statusCode == 429) {
            throw new ChatApiException(429, "Gemini rate limit: " + body, true);
        }
        if (statusCode == 401 || statusCode == 403) {
            throw new ChatApiException(statusCode, "Gemini auth error: " + body, false);
        }
        if (statusCode >= 400) {
            String apiMsg = root.path("error").path("message").asText("");
            throw new ChatApiException(statusCode, "Gemini HTTP " + statusCode + ": " + apiMsg,
                    statusCode == 404 || statusCode >= 500);
        }

        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            String blockReason = root.path("promptFeedback").path("blockReason").asText("");
            if (!blockReason.isBlank()) {
                throw new ChatApiException(400, "Gemini content filter: " + blockReason, false, CHAT_CONTENT_FILTER);
            }
            return null;
        }

        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            return null;
        }
        return parts.get(0).path("text").asText("").trim();
    }

    private String constructSystemPrompt(String userMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là 'Zaki AI' - tư vấn viên ZakiBooking.\n\n");

        sb.append("CÁCH TRẢ LỜI CHO KHÁCH (hiển thị trên web):\n");
        sb.append("- Ngắn gọn, thân thiện, Markdown.\n");
        sb.append("- **CẤM** paste nguyên danh mục tour, **CẤM** lộ chuỗi nội bộ (ID thô, Điểm nổi bật dài, trạng thái [HẾT HẠN]).\n");
        sb.append("- Gợi ý tối đa 1–3 tour: **tên tour** + giá + link [Xem chi tiết](/tour/ID).\n");
        sb.append("- **CẤM** bịa mã giảm giá hoặc gán số tiền giảm cố định cho từng tour.\n");
        sb.append("- Hỏi mã giảm: chỉ dùng mục MÃ GIẢM GIÁ. Nếu không có mã hiệu lực → trả lời: ");
        sb.append("\"Hiện chưa có mã giảm giá. Vui lòng theo dõi trang web thường xuyên để đón nhận ưu đãi mới.\" ");
        sb.append("Hotline +84 866147595, /contact.\n");
        sb.append("- Hỏi \"tour đang giảm giá\": chỉ liệt kê tour trong TOUR ĐANG CÓ KHUYẾN MÃI (nếu có).\n\n");

        appendFullTourCatalog(sb);
        appendPromoCodes(sb);
        appendToursOnSale(sb);

        if (userMessage.contains("@")) {
            String email = extractEmail(userMessage);
            if (email != null) {
                sb.append("\n--- ĐƠN HÀNG (email ").append(email).append(") ---\n");
                datChoRepository.findByEmailOrderByNgayDatDesc(email).stream().limit(3).forEach(d -> {
                    sb.append(String.format("- Đơn #%d: %s | %s | %s | %s VND\n",
                            d.getId(), d.getIdChuyenDi().getTieuDe(), d.getNgayDat(), d.getTrangThai(), d.getTongGia()));
                });
            }
        }

        sb.append("\nHotline: **+84 866147595** | Form liên hệ: **/contact**");
        return sb.toString();
    }

    /** Đưa toàn bộ tour trong DB vào prompt để AI tư vấn theo danh mục thật. */
    private void appendFullTourCatalog(StringBuilder sb) {
        List<ChuyenDi> tours = tourService.findAll().stream()
                .sorted(Comparator.comparing(ChuyenDi::getId, Comparator.nullsLast(Integer::compareTo)))
                .toList();
        LocalDate today = LocalDate.now();
        long bookable = tours.stream()
                .filter(t -> t.getNgayKetThuc() == null || !t.getNgayKetThuc().isBefore(today))
                .count();

        sb.append("NHIỆM VỤ TƯ VẤN:\n");
        sb.append("- Bạn có danh mục **đầy đủ ").append(tours.size()).append(" tour** (");
        sb.append(bookable).append(" tour còn trong hạn / có thể đặt).\n");
        sb.append("- Khi khách hỏi (điểm đến, giá, ngày, phương tiện, gia đình, biển, núi...), ");
        sb.append("hãy **so sánh và gợi ý 1–3 tour phù hợp nhất** từ danh sách, nêu lý do ngắn (giá, lịch, điểm đến).\n");
        sb.append("- **Không** bịa tour ngoài danh sách. Mỗi tour gợi ý kèm link: [Xem chi tiết](/tour/ID).\n");
        sb.append("- Tour đánh dấu [HẾT HẠN] chỉ nhắc khi khách hỏi lịch sử, ưu tiên tour còn đặt được.\n\n");

        sb.append("--- DANH MỤC TOUR (TOÀN BỘ) ---\n");
        if (tours.isEmpty()) {
            sb.append("(Hiện chưa có tour trong hệ thống.)\n");
            return;
        }
        for (ChuyenDi t : tours) {
            appendTourCatalogLine(sb, t, today);
        }
        sb.append("--- HẾT DANH MỤC ---\n\n");
    }

    private void appendPromoCodes(StringBuilder sb) {
        LocalDate today = LocalDate.now();
        List<MaGiamGia> codes = maGiamGiaService.findAllActive(today);

        sb.append("--- MÃ GIẢM GIÁ (CHỈ ĐƯỢC NHẮC CÁC MÃ SAU) ---\n");
        if (codes.isEmpty()) {
            sb.append("(KHÔNG có mã hiệu lực — báo khách theo dõi website.)\n");
        } else {
            for (MaGiamGia m : codes) {
                sb.append("- ").append(maGiamGiaService.formatPromoForChat(m)).append("\n");
            }
        }
        sb.append("--- HẾT MÃ GIẢM GIÁ ---\n\n");
    }

    private void appendToursOnSale(StringBuilder sb) {
        LocalDate today = LocalDate.now();
        List<ChuyenDi> saleTours = maGiamGiaService.findToursWithActivePromo(today);
        sb.append("--- TOUR ĐANG CÓ KHUYẾN MÃI (mã còn hiệu lực) ---\n");
        if (saleTours.isEmpty()) {
            sb.append("(Không có tour khuyến mãi riêng — chỉ mã chung nếu có.)\n");
        } else {
            for (ChuyenDi t : saleTours) {
                sb.append(String.format("- ID %d: %s | %s VND | /tour/%d\n",
                        t.getId(), nullSafe(t.getTieuDe()),
                        t.getGia() != null ? t.getGia().toPlainString() : "?",
                        t.getId()));
            }
        }
        sb.append("--- HẾT TOUR KHUYẾN MÃI ---\n\n");
    }

    private void appendTourCatalogLine(StringBuilder sb, ChuyenDi t, LocalDate today) {
        String destination = "Nhiều nơi";
        if (t.getIdDiemDen() != null) {
            String tp = t.getIdDiemDen().getThanhPho();
            String qg = t.getIdDiemDen().getQuocGia();
            destination = (tp != null ? tp : "") + (qg != null && !qg.isBlank() ? ", " + qg : "");
            if (destination.isBlank()) {
                destination = "Nhiều nơi";
            }
        }

        String transport = "—";
        if (t.getIdPhuongTien() != null && t.getIdPhuongTien().getLoai() != null) {
            transport = t.getIdPhuongTien().getLoai();
        }

        boolean bookable = t.getNgayKetThuc() == null || !t.getNgayKetThuc().isBefore(today);
        String status = bookable ? "ĐANG MỞ" : "HẾT HẠN";
        String dates = formatTourDates(t.getNgayKhoiHanh(), t.getNgayKetThuc());
        String gia = t.getGia() != null ? t.getGia().toPlainString() : "Liên hệ";

        sb.append(String.format("- **ID %d** [%s] %s | %s VND | %s | PT: %s | %s",
                t.getId(), status, nullSafe(t.getTieuDe()), gia, destination, transport, dates));
        if (Boolean.TRUE.equals(t.getNoiBat())) {
            sb.append(" | ⭐Nổi bật");
        }
        sb.append("\n");

        sb.append(" | /tour/").append(t.getId()).append("\n");
    }

    private static String formatTourDates(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return "Ngày: linh hoạt";
        }
        if (start != null && end != null) {
            return "Ngày: " + start + " → " + end;
        }
        if (start != null) {
            return "Từ ngày: " + start;
        }
        return "Đến ngày: " + end;
    }

    private static String truncate(String text, int maxLen) {
        String cleaned = text.replaceAll("\\s+", " ").trim();
        if (cleaned.length() <= maxLen) {
            return cleaned;
        }
        return cleaned.substring(0, maxLen) + "...";
    }

    private static String nullSafe(String s) {
        return s != null ? s : "Tour";
    }

    private String extractEmail(String message) {
        var m = java.util.regex.Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(message);
        return m.find() ? m.group() : null;
    }

    private static final class ChatApiException extends Exception {
        private final int statusCode;
        private final boolean retryable;
        private final String userReply;

        ChatApiException(int statusCode, String technicalMessage, boolean retryable) {
            this(statusCode, technicalMessage, retryable, null);
        }

        ChatApiException(int statusCode, String technicalMessage, boolean retryable, String userReply) {
            super(technicalMessage);
            this.statusCode = statusCode;
            this.retryable = retryable;
            this.userReply = userReply;
        }

        int getStatusCode() {
            return statusCode;
        }

        String getUserMessage() {
            return userReply != null ? userReply : CHAT_ERROR_USER;
        }

        boolean isRetryable() {
            return retryable;
        }
    }
}
