package com.smart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender javaMailSender;
	
	@Value("$(spring.mail.username)")
	private String fromEmailId;
	
	public boolean sendEmail(String to,String body,String subject ) throws MessagingException {
		
		//SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
		
		
		//simpleMailMessage.setFrom(fromEmailId);
		//simpleMailMessage.setTo(to);
		//simpleMailMessage.setText(body);
		//simpleMailMessage.setSubject(subject);
		
		//javaMailSender.send(simpleMailMessage);
		
		 MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		 MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

		    try {
		        helper.setTo(to);
		        helper.setSubject(subject);
		        helper.setText(body, true); // Set true to enable HTML content
		        
		        
		        javaMailSender.send(mimeMessage);
		    } catch (MessagingException e) {
		        // Handle exception
		        e.printStackTrace();
		    }
		
		return true;
	}
}
