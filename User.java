package com.smart.Entities;


import java.util.ArrayList;
import java.util.List;



import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;




@Entity
@Table(name="USER")
public class User {
   
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	@NotBlank(message = "This area can't be empty")
	@Size(min=2, max=20, message="min 2 and max 20 characters are allowed")
	private String name;
	
	@Column(unique=true)
	@Pattern(regexp = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$",message="Invalid Email!!")
	private String email;
	
	//getting error here so improve it after wards
	//@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{4,12}$",
	//		message = "Password must be 4 to 12 characters long and include at least one uppercase letter, one lowercase letter, one special character, and one digit")
	private String password;
	private String role;
	private boolean enabled;
	private String imageurl;
	
	@Column(length = 500)
	private String about;
	
	@OneToMany(cascade = CascadeType.ALL,orphanRemoval = true, fetch =FetchType.LAZY, mappedBy="user")  //orphanRemoval =true means if child entity is unlinked from parent then it will be removed
	private List<Contact> contacts = new ArrayList<>();
	
	



	public User() {
		super();
	}
	
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getImageurl() {
		return imageurl;
	}

	public void setImageurl(String imageurl) {
		this.imageurl = imageurl;
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}
	
	public List<Contact> getContacts() {
		return contacts;
	}



	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}



	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", email=" + email + ", password=" + password + ", role=" + role
				+ ", enabled=" + enabled + ", imageurl=" + imageurl + ", about=" + about + ", contacts=" + contacts
				+ "]";
	}
	
	
	
}
