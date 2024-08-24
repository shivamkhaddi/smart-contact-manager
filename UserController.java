package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.Entities.Contact;
import com.smart.Entities.MyOrder;
import com.smart.Entities.User;
import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private MyOrderRepository myOrderRepository;
	
	@ModelAttribute     // now this data will be common for all the handlers
	public void addCommonData(Model model, Principal principal) {
		
		String userName =principal.getName();
		System.out.println("UserName:"+userName);
		
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("User: "+user);
		
		model.addAttribute("user",user);
	}

	// Dash_board home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		
		model.addAttribute("title","User Dashboard");
		
		
		return "General/user_dashboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact", new Contact());
		
		return "General/add_contact_form";
	}
	
	
	//processing add contact from
	
	//@PostMapping("/process-contact")
	@RequestMapping(value="/process-contact", method = RequestMethod.POST)
	public String processContact(@Valid @ModelAttribute Contact contact,BindingResult result, @RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session) {
		
		if(result.hasErrors()) {
			System.out.println("problem");
			return "General/add_contact_form";
		}
		
		try {
						

			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
		 
			
		
		//process and uploading file
		
		if(file.isEmpty()) {
			//if the file is empty then try our message
			
			System.out.println("File is Empty");
			contact.setImage("default.png");
			
		}else {
			//file the file to folder and update name to contact
			contact.setImage(file.getOriginalFilename());
			
			File saveFile =new ClassPathResource("static/images").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("image uploaded");
		}
		
		//setting user in contact
		contact.setUser(user);
		
		//setting contact in user
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("Data "+contact);
		
		System.out.println("Added to data base");
		
		//message success
		
		session.setAttribute("message",new Message("your contact is added , Add more !!","success"));
		  
		
		}catch(Exception e) {
			System.out.println("ERROR :"+e.getMessage());
			e.printStackTrace();
			
		//error message	
			
			session.setAttribute("message",new Message("Something went wrong !!","danger"));	
		}
		return "General/add_contact_form";
	}
	
	// show contacts handler
	//show 5 contacts per page = 5[n]
	//current page =0 [page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal ) {
		
		m.addAttribute("title","show user contacts");
		
		//send contact list to show contacts 
		
		//method-1 to get contacts
		//String userName = principal.getName();		
		//User user = this.userRepository.getUserByUserName(userName);		
		//List<Contact> contacts = user.getContacts();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		PageRequest pageable = PageRequest.of(page, 3);
		
		Page<Contact> contacts =this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage", page);
		
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "General/show_contacts";
	}
	
	//show particular contact details
	@RequestMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		
		System.out.println(cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);	
		Contact contact =contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId()) {
			 model.addAttribute("contact",contact);
			 model.addAttribute("title",contact.getName());
		
		}
		
		
		
		return "General/contact_detail";
	}
	
	// delete contact handler
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model model,Principal principal,HttpSession session) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		
		//check
		 String userName = principal.getName();
		 User user = this.userRepository.getUserByUserName(userName);
		
		 if(user.getId() == contact.getUser().getId()) {
			 //contact.setUser(null);
			 
			 //remove img  contact.getImage()
			 
			 user.getContacts().remove(contact);
			 
			 this.userRepository.save(user);
		 }
		     
		 
		 
		 session.setAttribute("message", new Message("Contact deleted successfully...","success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
	
	//update from handler
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m) {
		
		m.addAttribute("title","update contact");
		
		Contact contact =this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		
		return "General/update_form";
	}
	
	//update contact handler
	@RequestMapping(value="/process-update", method= RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal principal) {
		
		try {
			
			//old contact details
			Contact oldContactDetail =this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty()) {
				
				//file work.. update new file
				//delete old photo
				File deleteFile = new ClassPathResource("static/images").getFile();
				File file1= new File(deleteFile,oldContactDetail.getImage());
				file1.delete();
				
				
				//update new contact
				
				File saveFile = new ClassPathResource("static/images").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator + file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			}else {
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user =this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact is Updated...","success"));
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/contact/"+contact.getcId();
	}
	
	
	// your profile handler
	
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		
		m.addAttribute("title","Profile Page");
		return "General/profile";
	}
	
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings() {
		
		return "General/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		
		String userName = principal.getName();
		
		User currentUser = this.userRepository.getUserByUserName(userName);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is successfully changed...", "success"));
		}else {
			//error
			session.setAttribute("message", new Message("please Enter correct old password!!", "danger"));
			return "redirect:/user/settings";
		}
		
		
		return "redirect:/user/index";
	}
	
	
	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal )throws RazorpayException {
		
		int amt = Integer.parseInt(data.get("amount").toString());
		
		var client =new RazorpayClient("rzp_test_xD7CjqrC6Eewrw", "R65KONzhJ436al4j61yyKj7i");
		
		JSONObject ob = new JSONObject();
		ob.put("amount", amt*100);
		ob.put("currency", "INR");
		ob.put("receipt", "txn_246105");
		
		Order order = client.orders.create(ob);
		System.out.println(order);
		
		//save order in database
		
		MyOrder myOrder = new MyOrder();
		
		
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));
		
		this.myOrderRepository.save(myOrder);
		//if you want you can save to your data base..
		
		
		return order.toString();
	}
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
		
		MyOrder myorder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		
		myorder.setPaymentId(data.get("payment_id").toString());
		myorder.setStatus(data.get("status").toString());
		
		this.myOrderRepository.save(myorder);
		
		return ResponseEntity.ok(Map.of("msg", "updated"));
	}
}




//with the help of "Principal principal" we get unique identifier of user Entity
