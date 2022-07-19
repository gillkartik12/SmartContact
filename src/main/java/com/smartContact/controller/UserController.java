package com.smartContact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smartContact.dao.ContactRepository;
import com.smartContact.dao.UserRepository;
import com.smartContact.entities.Contact;
import com.smartContact.entities.User;
import com.smartContact.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		String userName= principal.getName();
		System.out.println("Username: "+userName);
		
		User user = userRepository.getUserByUserName(userName);
		System.out.println("User: "+user);
		//get the user by username
		m.addAttribute("user",user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model m, Principal principal) {
		
		m.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	//open add contact form
	@GetMapping("/addContact")
	public String openAddContactForm(Model m) {
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
	return "normal/addContact";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file, 
			Principal p,
			HttpSession session) {
	 
		try {
		String name= p.getName();
	   User user= this.userRepository.getUserByUserName(name);
		
	   //processing and uploading file
	   if(file.isEmpty()) {
		   System.out.println("file is empty");
		   contact.setImage("photo.png");
	   }
	   else {
		   //will set name of img in db
		   
		   contact.setImage(file.getOriginalFilename());
		   File saveFile=new ClassPathResource("static/img").getFile();
	 
		   Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
		   Files.copy(file.getInputStream(), path , StandardCopyOption.REPLACE_EXISTING);
	   System.out.println("Image is uploaded");
	   }
	   
	   
	   contact.setUser(user);
	   
	   user.getContacts().add(contact);

	   this.userRepository.save(user);
		
	   System.out.println("DATA "+contact);
	   
	   System.out.println("Added to database");
	   
	   //message success....
	   session.setAttribute("message", new Message("Your contact is added!!", "success"));
	   
		}
		
		catch (Exception e) {
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();
			//error message....
			   session.setAttribute("message", new Message("Something went wrong!!", "danger"));

		}
		return "normal/addContact";
	}
	
	//show contacts handler
	//per page 5 contacts
	@GetMapping("/showContacts/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model m, Principal p) {
		m.addAttribute("title", "Show Contacts");
		//contact list showing
	    String username= p.getName();
	    User user= this.userRepository.getUserByUserName(username);
		
		//pageable object has current page and contacts per page
         Pageable pageable=PageRequest.of(page, 5);
	    Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(), pageable);
		
		m.addAttribute("contacts" , contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		
		
		return "normal/showContacts";
	}
	
	
	//showing particular contact details
	@RequestMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model m, Principal p) {
		
		Optional<Contact> contactOptional =this.contactRepository.findById(cId);
		Contact contact= contactOptional.get();
		
		//
		String username= p.getName();
		User user=this.userRepository.getUserByUserName(username);
		
		if(user.getId()==contact.getUser().getId()) {
			m.addAttribute("contact", contact);
			m.addAttribute("title", contact.getName());
		}
		
		
		
		return "normal/contactDetail";
	}
	
	
	//delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model m, HttpSession session, Principal p) {
		
		Optional<Contact> oContact=this.contactRepository.findById(cId);
		Contact contact= oContact.get();
		
		//contact.setUser(null);
		//this.contactRepository.delete(contact);
		User user = this.userRepository.getUserByUserName(p.getName());
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message("Contact Deleted Successfully","success"));
		return "redirect:/user/showContacts/0";
	}
	
	
	//update contact handler
	@PostMapping("/updateContact/{cId}")
	public String updateContact(@PathVariable("cId") Integer cId,Model m) {
	
		m.addAttribute("title", "Update Contact");
		Contact contact=this.contactRepository.findById(cId).get();
		m.addAttribute("contact",contact);
		return "normal/updateContact";
	}
	
	//save update contact handler
	@RequestMapping(value="/process-update", method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model m, HttpSession session,Principal p ) {
		
		System.out.println("Contact Name: " + contact.getName());
		try {
			
			//old contact details
			Contact oldContact=this.contactRepository.findById(contact.getcId()).get();
			
		//image	
		if(!file.isEmpty()) {
			
			//delete old picture
			File deleteFile=new ClassPathResource("static/img").getFile();
File file1=new File(deleteFile, oldContact.getImage());
file1.delete();
			
			//update new picture
			File saveFile=new ClassPathResource("static/img").getFile();
			 
			   Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			   Files.copy(file.getInputStream(), path , StandardCopyOption.REPLACE_EXISTING);
			
			contact.setImage(file.getOriginalFilename());
			
			
		}
		else {
			contact.setImage(oldContact.getImage());
		}
		
		User user= this.userRepository.getUserByUserName(p.getName());
		contact.setUser(user);
		
		this.contactRepository.save(contact);
		
		session.setAttribute("message",new Message("Your contact is updated..","success"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/contact/"+contact.getcId();
				}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		m.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	}
	
	

