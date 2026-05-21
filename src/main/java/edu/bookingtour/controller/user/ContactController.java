package edu.bookingtour.controller.user;

import edu.bookingtour.entity.Contact;
import edu.bookingtour.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping("/contact")
    public String contactForm(Model model) {
        if (!model.containsAttribute("contact")) {
            model.addAttribute("contact", new Contact());
        }
        return "user/contact";
    }

    @PostMapping("/contact")
    public String submitContact(@Valid @ModelAttribute("contact") Contact contact,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "user/contact";
        }
        contactService.save(contact);
        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/contact";
    }
}
