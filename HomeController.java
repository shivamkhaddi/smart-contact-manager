package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.Entities.User;
import com.smart.dao.UserRepository;
import com.smart.helper.Message;
import com.smart.service.EmailService;
import com.smart.service.OTPService;

import jakarta.servlet.http.HttpSession;

import jakarta.validation.Valid;




@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
   @Autowired
   private OTPService otpService;
	
	
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home-Smart Contact Manager");
		return "home";
	}
	
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About-Smart Contact Manager");
		return "about";
	}
	
	//sign up handler
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","Register-Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	//handler for registration
	//step 1: handle user information submission and send otp

	@Transactional
	@RequestMapping(value="/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1, @RequestParam(value="agreement",defaultValue="false") boolean agreement, Model model,HttpSession session) {
		
		
		try {
			if(!agreement) {
				System.out.println("you have not agreed the terms and conditions");
				throw new Exception("you have not agreed the terms and conditions");
			}
			
			if(result1.hasErrors()) {
				
				System.out.println("ERROR" + result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			// store user information in session
			session.setAttribute("tempUser", user);
			
			// Generate and send Otp
			
			String  otp = otpService.generateOTP(user.getEmail());
			emailService.sendEmail(user.getEmail(), "Your OTP Code","Your OTP Code is:" +otp);
			session.setAttribute("otp_email", user.getEmail());
			session.setAttribute("otp", otp);
			
			return "verify_otp1";
			
			
			/*user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageurl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement "+agreement);
			System.out.println("user "+user);
			
			
			User result =this.userRepository.save(user);
			
			model.addAttribute("user",new User());
			
			session.setAttribute("message",new Message("Successfully Registered!","alert-success"));
			
			
			
			return "signup";*/
			
		}catch(Exception e) {
			
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new Message("Something Went Wrong !!"+e.getMessage(),"alert-danger"));
			
			
			
			return "signup";
		}
		
	}
	
	//step 2: handle OTP verification
	
	@RequestMapping(value = "/verify_otp1", method = RequestMethod.POST)
	public String verifyOtp1(@RequestParam("otp") String otp, HttpSession session, Model model) {
		String email = (String) session.getAttribute("otp_email");
		
		if(otpService.verifyOTP(email, otp)) {
			User tempUser = (User) session.getAttribute("tempUser");
			
			if(tempUser != null) {
				// Save user information after successful OTP verification
                tempUser.setRole("ROLE_USER");
                tempUser.setEnabled(true);
                tempUser.setImageurl("default.png");
                tempUser.setPassword(passwordEncoder.encode(tempUser.getPassword())); // Encode password

                userRepository.save(tempUser);

                // Clear OTP from session
                otpService.clearOTP(email);
                session.removeAttribute("tempUser");
                session.removeAttribute("otp_email");

                model.addAttribute("user", new User());
                session.setAttribute("message", new Message("Successfully Registered!", "alert-success"));
                return "signup";
            } else {
                model.addAttribute("message", new Message("User data not found", "alert-danger"));
                return "verify_otp1";
            }
        } else {
            model.addAttribute("message", new Message("Invalid OTP", "alert-danger"));
            return "verify_otp1";
        }
	}
	
	//Handler for custom login
	
	@GetMapping("/signin")
	public String customLogin(Model model) {
		
		model.addAttribute("title","Login Page");
		return "login";
	}
	
}
