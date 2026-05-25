package com.cms.backend.controller;

import com.cms.backend.service.ExportImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ExportImportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportImportController.class);

    private final ExportImportService exportImportService;

    public ExportImportController(ExportImportService exportImportService) {
        this.exportImportService = exportImportService;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportContacts(
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("GET /api/contacts/export");
        try {
            byte[] csvData = exportImportService.exportContactsToCsv(
                    userDetails.getUsername());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=contacts.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csvData);
        } catch (Exception e) {
            logger.error("Export failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Integer>> importContacts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        logger.info("POST /api/contacts/import");
        try {
            Map<String, Integer> result = exportImportService.importContactsFromCsv(
                    userDetails.getUsername(), file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Import failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}