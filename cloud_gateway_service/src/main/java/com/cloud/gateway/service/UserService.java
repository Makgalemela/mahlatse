package com.cloud.gateway.service;

import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import com.cloud.gateway.dto.LoginDTO;
import com.cloud.gateway.dto.UserCredentialDto;
import com.cloud.gateway.dto.UserDetails;
import com.cloud.gateway.dto.UserRegistrationDto;

public interface UserService {

	ResponseEntity<Object> register(@Valid UserRegistrationDto registrationForm);

	ResponseEntity<Object> login(@Valid LoginDTO loginDTO);

	ResponseEntity<Object> logout(UserDetails loggedInUser);

	ResponseEntity<Object> userCredentials(UserCredentialDto userCredentialDto);

	ResponseEntity<Object> getUserDetail(Long userId);

	ResponseEntity<Object> updateCredentials(UserCredentialDto userCredentialDto, Long id);

}
