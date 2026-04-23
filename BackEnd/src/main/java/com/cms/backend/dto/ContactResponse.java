package com.cms.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContactResponse
{
    private Long id;
    private String firstName;
    private String lastName;
    private String title;
    private List<ContactEmailDto> emails;
    private List<ContactPhoneDto> phones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}