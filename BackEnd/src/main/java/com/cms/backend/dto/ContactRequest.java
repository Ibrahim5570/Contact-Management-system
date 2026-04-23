package com.cms.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ContactRequest
{
    @NotBlank(message = "First name required!")
    private String firstName;

    @NotBlank(message = "Last name required!")
    private String lastName;
    private String title;

    private List<ContactEmailDto> emails = new ArrayList<>();
    private List<ContactPhoneDto> phones = new ArrayList<>();
}