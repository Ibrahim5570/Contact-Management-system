package com.cms.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contact_emails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "label", nullable = false, length = 50)
    private String label;
}