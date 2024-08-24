package com.smart.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.Entities.User;
import com.smart.dao.UserRepository;

public class UserDetailServiceImpl implements UserDetailsService{

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		//fetching user from database
		
		User user = userRepository.getUserByUserName(username);
		
		if(user == null) {
			throw new UsernameNotFoundException("could not found user!!");	
		}
		
		CustomUserDetails customUserDetails = new CustomUserDetails(user);
		
		
		return customUserDetails;
	}

}
