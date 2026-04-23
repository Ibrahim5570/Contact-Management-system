package com.cms.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactPhoneDto
{
    private Long id;

    @NotBlank(message = "Phone number required!")
    private String phoneNumber;

    @NotBlank(message = "Label required!")
    private String label;
}