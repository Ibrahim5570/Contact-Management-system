package com.cms.backend.service;

import com.cms.backend.dto.ContactRequest;
import com.cms.backend.dto.ContactResponse;
import com.cms.backend.entity.Contact;
import com.cms.backend.entity.User;
import com.cms.backend.repository.ContactRepository;
import com.cms.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private ContactService contactService;

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
    void getContacts_ShouldReturnPageOfContacts() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("firstName").ascending());
        Page<Contact> contactPage = new PageImpl<>(List.of(testContact), pageable, 1);

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.findByUserId(anyLong(), any(Pageable.class)))
                .thenReturn(contactPage);

        Page<ContactResponse> result = contactService.getContacts(
                "ibrahim@test.com", null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getFirstName());
    }

    @Test
    void getContacts_ShouldSearchByName_WhenSearchProvided() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("firstName").ascending());
        Page<Contact> contactPage = new PageImpl<>(List.of(testContact), pageable, 1);

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.searchByUserIdAndName(anyLong(), anyString(),
                any(Pageable.class))).thenReturn(contactPage);

        Page<ContactResponse> result = contactService.getContacts(
                "ibrahim@test.com", "John", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(contactRepo).searchByUserIdAndName(anyLong(), eq("John"),
                any(Pageable.class));
    }

    @Test
    void createContact_ShouldReturnContactResponse() {
        ContactRequest request = new ContactRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setTitle("Mr");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.save(any(Contact.class))).thenReturn(testContact);

        ContactResponse response = contactService.createContact(
                "ibrahim@test.com", request);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        verify(contactRepo, times(1)).save(any(Contact.class));
    }

    @Test
    void getContactById_ShouldReturnContact_WhenExists() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.findById(anyLong())).thenReturn(Optional.of(testContact));

        ContactResponse response = contactService.getContactById(
                "ibrahim@test.com", 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John", response.getFirstName());
    }

    @Test
    void getContactById_ShouldThrowException_WhenNotFound() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                contactService.getContactById("ibrahim@test.com", 99L));
    }

    @Test
    void deleteContact_ShouldDelete_WhenContactBelongsToUser() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.findById(anyLong())).thenReturn(Optional.of(testContact));

        assertDoesNotThrow(() ->
                contactService.deleteContact("ibrahim@test.com", 1L));

        verify(contactRepo, times(1)).delete(any(Contact.class));
    }

    @Test
    void deleteContact_ShouldThrowException_WhenContactNotFound() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(contactRepo.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                contactService.deleteContact("ibrahim@test.com", 99L));

        verify(contactRepo, never()).delete(any(Contact.class));
    }
}