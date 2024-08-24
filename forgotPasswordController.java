package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.Entities.User;
import com.smart.dao.UserRepository;
import com.smart.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Controller
public class forgotPasswordController {
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	boolean flag= true;
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;

	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email,HttpSession session) throws MessagingException {
		
		
		//generating otp of 4 digit
		
				
		int otp = random.nextInt(99999);
		
		//write code for send otp
		String to= email;
		String body= ""
				+ "<div style='border:1px solid #e2e2e2; padding:20px;'> "
				+ "<h1>"
				+ "OTP is :"
				+ "<b>"+otp
				+ "</b>"
				+ "</h1>"
				+ "</div>";
				
		String subject="OTP From SCM";
		
		boolean flag = this.emailService.sendEmail(to,body,subject);
		
		if(flag) {
			
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}else {
			
			session.setAttribute("message","check your email id !!" );
			
			return "forgot_email_form";
		}
		
		
		
	}
	
	//verify otp
	@PostMapping("/verify-otp")
	public String  verifyOtp(@RequestParam("otp") int otp,HttpSession session) {
		
		int myOtp= (int)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		
		if(myOtp == otp) {
			
			//password change form
			User user =this.userRepository.getUserByUserName(email);
			
			if(user == null) {
				//send error message
				
				session.setAttribute("message", "No user with this email exist !!");
				
				return "forgot_email_form";
			}else {
				//
				
			}
			
			return "password_change_form";
		}
		else {
			
			session.setAttribute("message", "You have entered wrong OTP !!");
			return "verify_otp";
		}
		
	
		
	}
	
	// change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword,HttpSession session) {
		
		String email =(String) session.getAttribute("email");
		User user =this.userRepository.getUserByUserName(email);
		user.setPassword(this.bcrypt.encode(newPassword));
		this.userRepository.save(user);
		
		
		
		return "redirect:/signin?change=password changed successfully..";
	}
}
