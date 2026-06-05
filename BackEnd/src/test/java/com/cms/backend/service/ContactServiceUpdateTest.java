package com.cms.backend.service;

import com.cms.backend.dto.*;
import com.cms.backend.entity.*;
import com.cms.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceUpdateTest {

    @Mock private ContactRepository contactRepo;
    @Mock private UserRepository userRepo;

    @InjectMocks
    private ContactService contactService;

    private User testUser;
    private User otherUser;
    private Contact testContact;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).email("ibrahim@test.com")
                .firstName("Ibrahim").lastName("Test").build();

        otherUser = User.builder()
                .id(2L).email("other@test.com")
                .firstName("Other").lastName("User").build();

        testContact = Contact.builder()
                .id(1L).user(testUser)
                .firstName("John").lastName("Doe").title("Mr").build();
    }

    @Test
    void updateContact_ShouldUpdateFields_WhenContactBelongsToUser() {
        ContactRequest request = new ContactRequest();
        request.setFirstName("Johnny");
        request.setLastName("Doe");
        request.setTitle("Dr");

        Contact updatedContact = Contact.builder()
                .id(1L).user(testUser)
                .firstName("Johnny").lastName("Doe").title("Dr").build();

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.findById(anyLong())).thenReturn(Optional.of(testContact));
        when(contactRepo.save(any(Contact.class))).thenReturn(updatedContact);

        ContactResponse response = contactService.updateContact(
                "ibrahim@test.com", 1L, request);

        assertNotNull(response);
        assertEquals("Johnny", response.getFirstName());
        assertEquals("Dr", response.getTitle());
        verify(contactRepo, times(1)).save(any(Contact.class));
    }

    @Test
    void updateContact_ShouldThrowException_WhenContactNotFound() {
        ContactRequest request = new ContactRequest();
        request.setFirstName("Johnny");
        request.setLastName("Doe");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                contactService.updateContact("ibrahim@test.com", 99L, request));

        verify(contactRepo, never()).save(any(Contact.class));
    }

    @Test
    void updateContact_ShouldThrowException_WhenContactBelongsToOtherUser() {
        Contact otherContact = Contact.builder()
                .id(1L).user(otherUser)
                .firstName("John").lastName("Doe").build();

        ContactRequest request = new ContactRequest();
        request.setFirstName("Johnny");
        request.setLastName("Doe");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.findById(anyLong())).thenReturn(Optional.of(otherContact));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                contactService.updateContact("ibrahim@test.com", 1L, request));

        assertEquals("Access denied!", exception.getMessage());
        verify(contactRepo, never()).save(any(Contact.class));
    }

    @Test
    void deleteContact_ShouldThrowException_WhenContactBelongsToOtherUser() {
        Contact otherContact = Contact.builder()
                .id(1L).user(otherUser)
                .firstName("John").lastName("Doe").build();

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.findById(anyLong())).thenReturn(Optional.of(otherContact));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                contactService.deleteContact("ibrahim@test.com", 1L));

        assertEquals("Access denied!", exception.getMessage());
        verify(contactRepo, never()).delete(any(Contact.class));
    }

    @Test
    void getContactById_ShouldThrowException_WhenContactBelongsToOtherUser() {
        Contact otherContact = Contact.builder()
                .id(1L).user(otherUser)
                .firstName("John").lastName("Doe").build();

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.findById(anyLong())).thenReturn(Optional.of(otherContact));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                contactService.getContactById("ibrahim@test.com", 1L));

        assertEquals("Access denied!", exception.getMessage());
    }

    @Test
    void createContact_ShouldHandleEmailsAndPhones() {
        ContactEmailDto emailDto = new ContactEmailDto();
        emailDto.setEmail("john@work.com");
        emailDto.setLabel("work");

        ContactPhoneDto phoneDto = new ContactPhoneDto();
        phoneDto.setPhoneNumber("03001234567");
        phoneDto.setLabel("mobile");

        ContactRequest request = new ContactRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmails(List.of(emailDto));
        request.setPhones(List.of(phoneDto));

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.save(any(Contact.class))).thenReturn(testContact);

        ContactResponse response = contactService.createContact(
                "ibrahim@test.com", request);

        assertNotNull(response);
        verify(contactRepo, times(1)).save(any(Contact.class));
    }
}