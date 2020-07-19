package com.cloud.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.cloud.gateway.dto.LoginDTO;
import com.cloud.gateway.dto.UserCredentialDto;
import com.cloud.gateway.dto.UserRegistrationDto;
import com.cloud.gateway.service.UserService;
import com.cloud.gateway.utils.GenericUtils;
import static com.cloud.gateway.config.UrlConstant.*;
import javax.validation.Valid;
import io.swagger.annotations.Api;

@RestController
@Api(value = "Api used for User Controller")
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping(REGISTRATION)
	public ResponseEntity<Object> userRegistration(@RequestBody @Valid UserRegistrationDto registrationForm) {
		return userService.register(registrationForm);
	}

	@PostMapping(LOGIN)
	public ResponseEntity<Object> userLogin(@RequestBody @Valid LoginDTO loginDTO) {
		return userService.login(loginDTO);
	}

	@DeleteMapping(LOGOUT)
	public ResponseEntity<Object> userLogout() {
		return userService.logout(GenericUtils.getLoggedInUser());
	}
	
	@PostMapping(CREDENTIALS)
	public ResponseEntity<Object> userCredentials(@RequestBody UserCredentialDto userCredentialDto) {
		return userService.userCredentials(userCredentialDto);
	}
	
	@PutMapping(CREDENTIALS)
	public ResponseEntity<Object> updateCredentials(@RequestBody UserCredentialDto userCredentialDto ,@RequestParam Long id) {
		return userService.updateCredentials(userCredentialDto,id);
	}
	
	@GetMapping(DETAIL)
	public ResponseEntity<Object> getUserDetail(@RequestParam("userId")Long userId) {
		return userService.getUserDetail(userId);
	}

}
