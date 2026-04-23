package com.cms.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactEmailDto
{
    private Long id;

    @NotBlank(message = "Email required!")
    @Email(message = "Invalid Email format")
    private String email;

    @NotBlank(message = "Label required!")
    private String label;
}