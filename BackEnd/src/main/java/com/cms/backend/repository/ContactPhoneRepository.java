package com.cms.backend.repository;

import com.cms.backend.entity.ContactPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactPhoneRepository extends JpaRepository<ContactPhone, Long>{

}
