package edu.bookingtour.controller;

import edu.bookingtour.client.newsapi;
import edu.bookingtour.dto.ArticleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller

public class NewsController {

    @Autowired
    private newsapi newsService; // Class gọi API

    @GetMapping("/tin-tuc") // <--- Đường dẫn con
    public String showNewsPage(Model model) {
        // Lấy dữ liệu từ API thông qua NewsClient đã viết
        List<ArticleDTO> articles = newsService.getLatestNews();
        model.addAttribute("articles", articles);
        return "tintuc/tintuc"; // Trả về file tintuc.html
    }
}