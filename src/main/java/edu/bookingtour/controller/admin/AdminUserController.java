package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.service.NguoiDungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/user")
public class AdminUserController {
    @Autowired
    private NguoiDungService nguoiDungService;
    @GetMapping
    public String listUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int perPage, Model model) {
        Page<NguoiDung> nguoiDung = nguoiDungService.findAllUser(page, perPage);
        model.addAttribute("users", nguoiDung);
        model.addAttribute("page", page);
        model.addAttribute("perPage", perPage);
        model.addAttribute("totalPage", nguoiDung.getTotalPages());
        return "admin/user/user-list";
    }
    @GetMapping("/create")
    public String createUser(Model model) {
        model.addAttribute("users", new NguoiDung());
        return "admin/user/user-create";
    }

    @PostMapping("/save")
    public String saveUser(NguoiDung user) {
        nguoiDungService.save(user);
        return "redirect:/admin/user";
    }
    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Integer id, Model model) {
        NguoiDung nguoiDung = nguoiDungService.findById(id).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        model.addAttribute("user", nguoiDung);
        return "admin/user/user-update";
    }
    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Integer id, NguoiDung user) {
        nguoiDungService.update(id, user);
        return "redirect:/admin/user";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        nguoiDungService.deleteById(id);
        return "redirect:/admin/user";
    }

    @GetMapping("/{id}")
    public String showDetailUser(@PathVariable Integer id, Model model) {
        NguoiDung user = nguoiDungService.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));
        model.addAttribute("user", user);
        return "admin/user/user-detail";
    }
}
