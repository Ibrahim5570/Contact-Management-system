package com.cms.backend.controller;

import com.cms.backend.dto.*;
import com.cms.backend.service.ContactService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacts")
public class ContactController
{

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    private final ContactService contactService;

    public ContactController(ContactService contactService)
    {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<Page<ContactResponse>> getContacts(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)
    {
        logger.info("GET /api/contacts");
        return ResponseEntity.ok(contactService.getContacts(userDetails.getUsername(), search, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> getContact(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id)
    {
        logger.info("GET /api/contacts/{}", id);
        return ResponseEntity.ok(contactService.getContactById(userDetails.getUsername(), id));
    }

    @PostMapping
    public ResponseEntity<ContactResponse> createContact(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ContactRequest request)
    {
        logger.info("POST /api/contacts");
        return ResponseEntity.ok(contactService.createContact(userDetails.getUsername(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> updateContact(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, @Valid @RequestBody ContactRequest request)
    {
        logger.info("PUT /api/contacts/{}", id);
        return ResponseEntity.ok(contactService.updateContact(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContact(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id)
    {
        logger.info("DELETE /api/contacts/{}", id);
        contactService.deleteContact(userDetails.getUsername(), id);
        return ResponseEntity.ok("Contact deleted successfully!");
    }
}