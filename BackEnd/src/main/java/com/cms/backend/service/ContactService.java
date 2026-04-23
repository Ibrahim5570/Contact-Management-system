package com.cms.backend.service;

import com.cms.backend.dto.*;
import com.cms.backend.entity.*;
import com.cms.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactService
{

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ContactService(ContactRepository contactRepository, UserRepository userRepository)
    {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    public Page<ContactResponse> getContacts(String email, String search, int page, int size)
    {
        logger.info("Getting contacts for: {}", email);
        User user = getUser(email);
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());

        Page<Contact> contacts;
        if (search != null && !search.trim().isEmpty())
        {
            contacts = contactRepository.searchByUserIdAndName(user.getId(), search.trim(), pageable);
        }
        else
        {
            contacts = contactRepository.findByUserId(user.getId(), pageable);
        }

        return contacts.map(this::mapToResponse);
    }

    public ContactResponse getContactById(String email, Long contactId)
    {
        logger.info("Getting contact {} for: {}", contactId, email);
        User user = getUser(email);
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        if (!contact.getUser().getId().equals(user.getId()))
        {
            throw new RuntimeException("Access denied!");
        }

        return mapToResponse(contact);
    }

    @Transactional
    public ContactResponse createContact(String email, ContactRequest request)
    {
        logger.info("Creating contact for: {}", email);
        User user = getUser(email);

        Contact contact = Contact.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .title(request.getTitle())
                .build();

        if (request.getEmails() != null)
        {
            List<ContactEmail> emails = request.getEmails().stream()
                    .map(dto -> ContactEmail.builder()
                            .contact(contact)
                            .email(dto.getEmail())
                            .label(dto.getLabel())
                            .build())
                    .collect(Collectors.toList());
            contact.getEmails().addAll(emails);
        }

        if (request.getPhones() != null)
        {
            List<ContactPhone> phones = request.getPhones().stream()
                    .map(dto -> ContactPhone.builder()
                            .contact(contact)
                            .phoneNumber(dto.getPhoneNumber())
                            .label(dto.getLabel())
                            .build())
                    .collect(Collectors.toList());
            contact.getPhones().addAll(phones);
        }

        Contact saved = contactRepository.save(contact);
        logger.info("Contact created with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public ContactResponse updateContact(String email, Long contactId, ContactRequest request)
    {
        logger.info("Updating contact {} for: {}", contactId, email);
        User user = getUser(email);
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found!"));

        if (!contact.getUser().getId().equals(user.getId()))
        {
            throw new RuntimeException("Access denied!");
        }

        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());

        contact.getEmails().clear();
        if (request.getEmails() != null)
        {
            request.getEmails().forEach(dto -> contact.getEmails().add(
                    ContactEmail.builder()
                            .contact(contact)
                            .email(dto.getEmail())
                            .label(dto.getLabel())
                            .build()));
        }

        contact.getPhones().clear();
        if (request.getPhones() != null)
        {
            request.getPhones().forEach(dto -> contact.getPhones().add(
                    ContactPhone.builder()
                            .contact(contact)
                            .phoneNumber(dto.getPhoneNumber())
                            .label(dto.getLabel())
                            .build()));
        }

        Contact saved = contactRepository.save(contact);
        logger.info("Contact updated: {}", contactId);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteContact(String email, Long contactId)
    {
        logger.info("Deleting contact {} for: {}", contactId, email);
        User user = getUser(email);
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found!"));

        if (!contact.getUser().getId().equals(user.getId()))
        {
            throw new RuntimeException("Access denied!");
        }

        contactRepository.delete(contact);
        logger.info("Contact deleted: {}", contactId);
    }

    private User getUser(String email)
    {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private ContactResponse mapToResponse(Contact contact)
    {
        ContactResponse response = new ContactResponse();
        response.setId(contact.getId());
        response.setFirstName(contact.getFirstName());
        response.setLastName(contact.getLastName());
        response.setTitle(contact.getTitle());
        response.setCreatedAt(contact.getCreatedAt());
        response.setUpdatedAt(contact.getUpdatedAt());

        response.setEmails(contact.getEmails().stream()
                .map(e -> {
                    ContactEmailDto dto = new ContactEmailDto();
                    dto.setId(e.getId());
                    dto.setEmail(e.getEmail());
                    dto.setLabel(e.getLabel());
                    return dto;
                }).collect(Collectors.toList()));

        response.setPhones(contact.getPhones().stream()
                .map(p -> {
                    ContactPhoneDto dto = new ContactPhoneDto();
                    dto.setId(p.getId());
                    dto.setPhoneNumber(p.getPhoneNumber());
                    dto.setLabel(p.getLabel());
                    return dto;
                }).collect(Collectors.toList()));

        return response;
    }
}