package com.smartContact.dao;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartContact.entities.Contact;
import com.smartContact.entities.User;

public interface ContactRepository extends JpaRepository <Contact, Integer> {

	//pagination...
	
	//pageable object has current page and contacts per page
	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);

	//search
 
}
