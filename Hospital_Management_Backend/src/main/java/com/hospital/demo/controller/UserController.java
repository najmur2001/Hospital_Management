package com.hospital.demo.controller;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.demo.dto.CommanApiResponse;
import com.hospital.demo.dto.UserLoginRequest;
import com.hospital.demo.dto.UserLoginResponse;
import com.hospital.demo.dto.UserRoleResponse;
import com.hospital.demo.dto.UsersResponseDto;
import com.hospital.demo.entity.User;
import com.hospital.demo.exception.UserNotFoundException;
import com.hospital.demo.service.CustomUserDetailsService;
import com.hospital.demo.service.UserService;
import com.hospital.demo.utility.Constants.ResponseCode;
import com.hospital.demo.utility.Constants.Sex;
import com.hospital.demo.utility.Constants.UserRole;
import com.hospital.demo.utility.Constants.UserStatus;
import com.hospital.demo.utility.JwtUtil;





@RestController
@RequestMapping("api/user/")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

	Logger LOG = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	private JwtUtil jwtUtil;

	@GetMapping("roles")
	
	public ResponseEntity<?> getAllUsers() {
		
		UserRoleResponse response = new UserRoleResponse();
		List<String> roles = new ArrayList<>();
		
		for(UserRole role : UserRole.values() ) {
			roles.add(role.value());
		}
		
		if(roles.isEmpty()) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to Fetch User Roles");
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		else {
		    response.setRoles(roles);
			response.setResponseCode(ResponseCode.SUCCESS.value());
			response.setResponseMessage("User Roles Fetched success");
			return new ResponseEntity(response, HttpStatus.OK);
		}
		
	}
	
	@GetMapping("gender")
	
	public ResponseEntity<?> getAllUserGender() {
		
		UserRoleResponse response = new UserRoleResponse();
		List<String> genders = new ArrayList<>();
		
		for(Sex gender : Sex.values() ) {
			genders.add(gender.value());
		}
		
		if(genders.isEmpty()) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to Fetch User Genders");
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		else {
			response.setGenders(genders);
			response.setResponseCode(ResponseCode.SUCCESS.value());
			response.setResponseMessage("User Genders Fetched success");
			return new ResponseEntity(response, HttpStatus.OK);
		}
		
	}
	
	@PostMapping("register")
	
	public ResponseEntity<?> register(@RequestBody User user) {
		LOG.info("Recieved request for User  register");

		CommanApiResponse response = new CommanApiResponse();
		String encodedPassword = passwordEncoder.encode(user.getPassword());

		user.setPassword(encodedPassword);
		user.setStatus(UserStatus.ACTIVE.value());

		User registerUser = userService.registerUser(user);

		if (registerUser != null) {
			response.setResponseCode(ResponseCode.SUCCESS.value());
			response.setResponseMessage(user.getRole() + " User Registered Successfully");
			return new ResponseEntity(response, HttpStatus.OK);
		}

		else {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to Register " + user.getRole() + " User");
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("login")
	
	public ResponseEntity<?> login(@RequestBody UserLoginRequest userLoginRequest) {
		LOG.info("Recieved request for User Login");

		String jwtToken = null;
		UserLoginResponse useLoginResponse = new UserLoginResponse();
        User user = null;
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(userLoginRequest.getEmailId(), userLoginRequest.getPassword()));
		} catch (Exception ex) {
			LOG.error("Autthentication Failed!!!");
			useLoginResponse.setResponseCode(ResponseCode.FAILED.value());
			useLoginResponse.setResponseMessage("Failed to Login as " + userLoginRequest.getEmailId());
			return new ResponseEntity(useLoginResponse, HttpStatus.BAD_REQUEST);
		}

		UserDetails userDetails = customUserDetailsService.loadUserByUsername(userLoginRequest.getEmailId());

		user = userService.getUserByEmailId(userLoginRequest.getEmailId());
		
		if(user.getStatus() != UserStatus.ACTIVE.value()) {
			useLoginResponse.setResponseCode(ResponseCode.FAILED.value());
			useLoginResponse.setResponseMessage("User is Inactive");
			return new ResponseEntity(useLoginResponse, HttpStatus.BAD_REQUEST);
		}
		
		for (GrantedAuthority grantedAuthory : userDetails.getAuthorities()) {
			if (grantedAuthory.getAuthority().equals(userLoginRequest.getRole())) {
				jwtToken = jwtUtil.generateToken(userDetails.getUsername());
			}
		}

		// user is authenticated
		if (jwtToken != null) {
			useLoginResponse = User.toUserLoginResponse(user);
			
			useLoginResponse.setResponseCode(ResponseCode.SUCCESS.value());
			useLoginResponse.setResponseMessage(user.getFirstName() + " logged in Successful");
			useLoginResponse.setJwtToken(jwtToken);
			return new ResponseEntity(useLoginResponse, HttpStatus.OK);
		
		}

		else {

			useLoginResponse.setResponseCode(ResponseCode.FAILED.value());
			useLoginResponse.setResponseMessage("Failed to Login as " + userLoginRequest.getEmailId());
			return new ResponseEntity(useLoginResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("id")
	
	public ResponseEntity<?> fetchUser(@RequestParam("userId") int userId) {
		
		UsersResponseDto response = new UsersResponseDto();
		
		User user = userService.getUserById(userId);
		
		if(user == null) {
			throw new UserNotFoundException();
		}
		
		response.setUser(user);
		response.setResponseCode(ResponseCode.SUCCESS.value());
		response.setResponseMessage("User Fetched Successfully");
		
		return new ResponseEntity(response, HttpStatus.OK);
	}
	
	@GetMapping("delete/id")
	
	public ResponseEntity<?> deleteUser(@RequestParam("userId") int userId) {
		
		System.out.println("request came for USER DELETE By ID");
		
		CommanApiResponse response = new CommanApiResponse();
		
		User user = null;
		user = userService.getUserById(userId);
		
		if(user == null) {
			throw new UserNotFoundException();
		}
		
		user.setStatus(UserStatus.DELETED.value());
		
		userService.registerUser(user);
		
		response.setResponseCode(ResponseCode.SUCCESS.value());
		response.setResponseMessage("User Deleted Successfully");
		
		return new ResponseEntity(response, HttpStatus.OK);
	}
	
}

