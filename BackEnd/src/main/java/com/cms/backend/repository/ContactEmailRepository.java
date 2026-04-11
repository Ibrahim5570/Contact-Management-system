package com.cms.backend.repository;

import com.cms.backend.entity.ContactEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactEmailRepository extends JpaRepository<ContactEmail, Long>{

}
