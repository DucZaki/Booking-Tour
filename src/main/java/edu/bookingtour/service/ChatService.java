package edu.bookingtour.service;

import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import edu.bookingtour.repo.DatChoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class ChatService {

    @Value("${gemini.api.url:}")
    private String apiUrl;

    @Autowired
    private TourService tourService;

    @Autowired
    private DatChoRepository datChoRepository;

    @Value("${GEMINI_API_KEY:${gemini.api.key:}}")
    private String apiKey;

    public String ask(String message) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "Xin lỗi, Chatbot hiện đang bảo trì (thiếu API Key).";
        }

        try {
            String systemPrompt = constructSystemPrompt(message);
            
            RestTemplate rt = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Google Gemini API
            String url = (apiUrl != null && !apiUrl.isEmpty()) ? apiUrl + "?key=" + apiKey 
                         : "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=" + apiKey;

            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> parts = new HashMap<>();
            parts.put("text", systemPrompt + "\n\nUser: " + message);
            contents.put("parts", new Object[]{parts});
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("contents", new Object[]{contents});

            HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);
            ResponseEntity<String> resp = rt.postForEntity(url, req, String.class);

            if (resp.getStatusCode().is2xxSuccessful()) {
                ObjectMapper om = new ObjectMapper();
                JsonNode root = om.readTree(resp.getBody());
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            } else {
                return "Tôi gặp sự cố khi kết nối với AI. Vui lòng thử lại sau.";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Đã có lỗi xảy ra: " + ex.getMessage();
        }
    }

    private String constructSystemPrompt(String userMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là 'Zaki AI' - Chuyên gia tư vấn du lịch nhiệt tình của ZakiBooking.\n");
        sb.append("PHONG CÁCH:\n");
        sb.append("- Thân thiện, chuyên nghiệp, sử dụng biểu tượng cảm xúc (emoji) phù hợp.\n");
        sb.append("- Trả lời ngắn gọn, súc tích, trình bày bằng Markdown (dùng bullet points, chữ đậm).\n\n");
        
        sb.append("NHIỆM VỤ:\n");
        sb.append("1. Dựa vào danh sách tour bên dưới để tư vấn. LUÔN cung cấp link xem chi tiết dạng: [Xem chi tiết tại đây](/tour/ID).\n");
        sb.append("2. Nếu khách hỏi về đơn hàng, hãy nhắc họ cung cấp email để bạn tra cứu.\n");
        sb.append("3. Nếu không có tour nào khớp hoàn toàn, hãy gợi ý các tour gần giống nhất.\n\n");

        // Thêm dữ liệu tour đang hoạt động
        sb.append("--- CƠ SỞ DỮ LIỆU TOUR (DÙNG ĐỂ TƯ VẤN) ---\n");
        tourService.findAll().stream().limit(15).forEach(t -> {
            sb.append(String.format("- **ID %d**: %s | Giá: %s VND | Điểm đến: %s\n", 
                t.getId(), t.getTieuDe(), t.getGia(), t.getIdDiemDen() != null ? t.getIdDiemDen().getThanhPho() : "Nhiều nơi"));
            sb.append(String.format("  Mô tả ngắn: %s\n", t.getMoTa() != null && t.getMoTa().length() > 100 ? t.getMoTa().substring(0, 100) + "..." : t.getMoTa()));
            sb.append(String.format("  Link: /tour/%d\n", t.getId()));
        });
        sb.append("\n");

        // Tra cứu đơn hàng nếu user message có chứa email
        if (userMessage.contains("@")) {
            String email = extractEmail(userMessage);
            if (email != null) {
                sb.append("--- THÔNG TIN ĐƠN HÀNG CỦA KHÁCH (DÙNG ĐỂ PHẢN HỒI) ---\n");
                datChoRepository.findByEmailOrderByNgayDatDesc(email).stream().limit(3).forEach(d -> {
                    sb.append(String.format("- Đơn #%d: Tour '%s', Ngày đặt: %s, Trạng thái: %s, Tổng tiền: %s VND\n", 
                        d.getId(), d.getIdChuyenDi().getTieuDe(), d.getNgayDat(), d.getTrangThai(), d.getTongGia()));
                });
                sb.append("\n");
                sb.append("Hãy xác nhận thông tin đơn hàng trên với khách một cách tế nhị.\n");
            }
        }

        sb.append("\nLƯU Ý: Nếu không tìm thấy tour phù hợp hoặc khách cần hỗ trợ gấp, hãy bảo khách gọi Hotline: **+84 866147595**.");
        return sb.toString();
    }

    private String extractEmail(String message) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(message);
        return m.find() ? m.group() : null;
    }
}
