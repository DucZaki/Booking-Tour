package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.Contact;
import edu.bookingtour.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/contact")
public class AdminContactController {
    @Autowired
    private ContactService contactService;
    @GetMapping
    public String list(Model model) {
        model.addAttribute("contacts", contactService.findAll());
        return "admin/contact/contact-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Contact contact = contactService.findById(id);
        if (contact != null && "NEW".equals(contact.getStatus())) {
            contactService.markAsRead(id);
            contact.setStatus("READ");
        }
        model.addAttribute("contact", contact);
        return "admin/contact/contact-detail";
    }
}
