package edu.bookingtour.service;

import edu.bookingtour.entity.Contact;
import edu.bookingtour.repo.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {
    @Autowired
    private ContactRepository contactRepository;

    public void save(Contact contact) {
        contactRepository.save(contact);
    }

    public List<Contact> findAll() {
        return contactRepository.findAll();
    }

    public Contact findById(Long id) {
        return contactRepository.findById(id).orElse(null);
    }

    public void markAsRead(Long id) {
        Contact contact = findById(id);
        if (contact != null) {
            contact.setStatus("READ");
            contactRepository.save(contact);
        }
    }
}
