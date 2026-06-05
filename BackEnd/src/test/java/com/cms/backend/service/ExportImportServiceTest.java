package com.cms.backend.service;

import com.cms.backend.entity.*;
import com.cms.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportImportServiceTest {

    @Mock
    private ContactRepository contactRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private ExportImportService exportImportService;

    private User testUser;
    private Contact testContact;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("ibrahim@test.com")
                .firstName("Ibrahim")
                .lastName("Test")
                .build();

        testContact = Contact.builder()
                .id(1L)
                .user(testUser)
                .firstName("John")
                .lastName("Doe")
                .title("Mr")
                .build();
    }

    @Test
    void exportContactsToCsv_ShouldReturnCsvBytes_WhenContactsExist() throws IOException {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.findAll()).thenReturn(List.of(testContact));

        byte[] result = exportImportService.exportContactsToCsv("ibrahim@test.com");

        assertNotNull(result);
        assertTrue(result.length > 0);
        String csv = new String(result);
        assertTrue(csv.contains("First Name"));
        assertTrue(csv.contains("John"));
        assertTrue(csv.contains("Doe"));
    }

    @Test
    void exportContactsToCsv_ShouldReturnHeaderOnly_WhenNoContacts() throws IOException {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.findAll()).thenReturn(List.of());

        byte[] result = exportImportService.exportContactsToCsv("ibrahim@test.com");

        assertNotNull(result);
        String csv = new String(result);
        assertTrue(csv.contains("First Name"));
        assertTrue(csv.contains("Last Name"));
    }

    @Test
    void exportContactsToCsv_ShouldOnlyExportCurrentUserContacts() throws IOException {
        User otherUser = User.builder().id(2L).email("other@test.com").build();
        Contact otherContact = Contact.builder()
                .id(2L).user(otherUser)
                .firstName("Jane").lastName("Smith").build();

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.findAll()).thenReturn(List.of(testContact, otherContact));

        byte[] result = exportImportService.exportContactsToCsv("ibrahim@test.com");
        String csv = new String(result);

        assertTrue(csv.contains("John"));
        assertFalse(csv.contains("Jane"));
    }

    @Test
    void importContactsFromCsv_ShouldImportValidRows() throws IOException {
        String csvContent = "First Name,Last Name,Title,Email 1,Email 1 Label," +
                "Email 2,Email 2 Label,Phone 1,Phone 1 Label,Phone 2,Phone 2 Label\n" +
                "John,Doe,Mr,john@test.com,work,,, 03001234567,mobile,,\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "contacts.csv",
                "text/csv", csvContent.getBytes());

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.save(any(Contact.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Integer> result =
                exportImportService.importContactsFromCsv("ibrahim@test.com", file);

        assertEquals(1, result.get("imported"));
        assertEquals(0, result.get("skipped"));
        verify(contactRepo, times(1)).save(any(Contact.class));
    }

    @Test
    void importContactsFromCsv_ShouldSkipRowsWithMissingFirstName() throws IOException {
        String csvContent = "First Name,Last Name,Title,Email 1,Email 1 Label," +
                "Email 2,Email 2 Label,Phone 1,Phone 1 Label,Phone 2,Phone 2 Label\n" +
                ",Doe,Mr,john@test.com,work,,,,,, \n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "contacts.csv",
                "text/csv", csvContent.getBytes());

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        Map<String, Integer> result =
                exportImportService.importContactsFromCsv("ibrahim@test.com", file);

        assertEquals(0, result.get("imported"));
        assertEquals(1, result.get("skipped"));
        verify(contactRepo, never()).save(any(Contact.class));
    }

    @Test
    void importContactsFromCsv_ShouldSkipRowsWithMissingLastName() throws IOException {
        String csvContent = "First Name,Last Name,Title,Email 1,Email 1 Label," +
                "Email 2,Email 2 Label,Phone 1,Phone 1 Label,Phone 2,Phone 2 Label\n" +
                "John,,Mr,john@test.com,work,,,,,, \n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "contacts.csv",
                "text/csv", csvContent.getBytes());

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        Map<String, Integer> result =
                exportImportService.importContactsFromCsv("ibrahim@test.com", file);

        assertEquals(0, result.get("imported"));
        assertEquals(1, result.get("skipped"));
    }

    @Test
    void importContactsFromCsv_ShouldHandleMultipleRows() throws IOException {
        String csvContent = "First Name,Last Name,Title,Email 1,Email 1 Label," +
                "Email 2,Email 2 Label,Phone 1,Phone 1 Label,Phone 2,Phone 2 Label\n" +
                "John,Doe,Mr,john@test.com,work,,,,,, \n" +
                "Jane,Smith,,jane@test.com,personal,,,,,, \n" +
                ",Missing,,,,,,,,, \n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "contacts.csv",
                "text/csv", csvContent.getBytes());

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.save(any(Contact.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Integer> result =
                exportImportService.importContactsFromCsv("ibrahim@test.com", file);

        assertEquals(2, result.get("imported"));
        assertEquals(1, result.get("skipped"));
    }
}