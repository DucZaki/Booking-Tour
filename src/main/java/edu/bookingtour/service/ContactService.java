package edu.bookingtour.service;

import edu.bookingtour.entity.Contact;
import edu.bookingtour.repo.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {
    @Autowired
    private ContactRepository contactRepository;

    public void save(Contact contact) {
        contactRepository.save(contact);
    }

    public Page<Contact> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return contactRepository.findAll(pageable);
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
