package com.cms.backend.service;

import com.cms.backend.entity.*;
import com.cms.backend.repository.*;
import org.apache.commons.csv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ExportImportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportImportService.class);

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ExportImportService(ContactRepository contactRepository,
                               UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public byte[] exportContactsToCsv(String identifier) throws IOException {
        logger.info("Exporting contacts for: {}", identifier);
        User user = getUser(identifier);

        List<Contact> contacts = contactRepository.findAll().stream()
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .toList();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withHeader(
                        "First Name", "Last Name", "Title",
                        "Email 1", "Email 1 Label",
                        "Email 2", "Email 2 Label",
                        "Phone 1", "Phone 1 Label",
                        "Phone 2", "Phone 2 Label"
                ))) {

            for (Contact contact : contacts) {
                List<ContactEmail> emails = new ArrayList<>(contact.getEmails());
                List<ContactPhone> phones = new ArrayList<>(contact.getPhones());

                printer.printRecord(
                        contact.getFirstName(),
                        contact.getLastName(),
                        contact.getTitle() != null ? contact.getTitle() : "",
                        emails.size() > 0 ? emails.get(0).getEmail() : "",
                        emails.size() > 0 ? emails.get(0).getLabel() : "",
                        emails.size() > 1 ? emails.get(1).getEmail() : "",
                        emails.size() > 1 ? emails.get(1).getLabel() : "",
                        phones.size() > 0 ? phones.get(0).getPhoneNumber() : "",
                        phones.size() > 0 ? phones.get(0).getLabel() : "",
                        phones.size() > 1 ? phones.get(1).getPhoneNumber() : "",
                        phones.size() > 1 ? phones.get(1).getLabel() : ""
                );
            }
        }

        logger.info("Exported {} contacts for: {}", contacts.size(), identifier);
        return out.toByteArray();
    }

    @Transactional
    public Map<String, Integer> importContactsFromCsv(String identifier,
                                                      MultipartFile file) throws IOException {
        logger.info("Importing contacts for: {}", identifier);
        User user = getUser(identifier);

        int imported = 0;
        int skipped = 0;

        try (CSVParser parser = CSVParser.parse(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

            for (CSVRecord record : parser) {
                try {
                    String firstName = record.get("First Name");
                    String lastName = record.get("Last Name");

                    if (firstName == null || firstName.isBlank() ||
                            lastName == null || lastName.isBlank()) {
                        skipped++;
                        continue;
                    }

                    Contact contact = Contact.builder()
                            .user(user)
                            .firstName(firstName)
                            .lastName(lastName)
                            .title(getOrEmpty(record, "Title"))
                            .build();

                    // Add emails
                    String email1 = getOrEmpty(record, "Email 1");
                    String email1Label = getOrEmpty(record, "Email 1 Label");
                    if (!email1.isBlank()) {
                        contact.getEmails().add(ContactEmail.builder()
                                .contact(contact)
                                .email(email1)
                                .label(email1Label.isBlank() ? "work" : email1Label)
                                .build());
                    }

                    String email2 = getOrEmpty(record, "Email 2");
                    String email2Label = getOrEmpty(record, "Email 2 Label");
                    if (!email2.isBlank()) {
                        contact.getEmails().add(ContactEmail.builder()
                                .contact(contact)
                                .email(email2)
                                .label(email2Label.isBlank() ? "personal" : email2Label)
                                .build());
                    }

                    // Add phones
                    String phone1 = getOrEmpty(record, "Phone 1");
                    String phone1Label = getOrEmpty(record, "Phone 1 Label");
                    if (!phone1.isBlank()) {
                        contact.getPhones().add(ContactPhone.builder()
                                .contact(contact)
                                .phoneNumber(phone1)
                                .label(phone1Label.isBlank() ? "mobile" : phone1Label)
                                .build());
                    }

                    String phone2 = getOrEmpty(record, "Phone 2");
                    String phone2Label = getOrEmpty(record, "Phone 2 Label");
                    if (!phone2.isBlank()) {
                        contact.getPhones().add(ContactPhone.builder()
                                .contact(contact)
                                .phoneNumber(phone2)
                                .label(phone2Label.isBlank() ? "home" : phone2Label)
                                .build());
                    }

                    contactRepository.save(contact);
                    imported++;

                } catch (Exception e) {
                    logger.warn("Skipped row due to error: {}", e.getMessage());
                    skipped++;
                }
            }
        }

        logger.info("Import complete - imported: {}, skipped: {}", imported, skipped);
        return Map.of("imported", imported, "skipped", skipped);
    }

    private String getOrEmpty(CSVRecord record, String column) {
        try {
            return record.get(column);
        } catch (Exception e) {
            return "";
        }
    }

    private User getUser(String identifier) {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhoneNumber(identifier))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + identifier));
    }
}