package com.cloud.gateway.service.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.cloud.gateway.domain.User;
import com.cloud.gateway.domain.UserCredentials;
import com.cloud.gateway.dto.LoginDTO;
import com.cloud.gateway.dto.LoginResponseDTO;
import com.cloud.gateway.dto.UserCredentialDto;
import com.cloud.gateway.dto.UserDetails;
import com.cloud.gateway.dto.UserRegistrationDto;
import com.cloud.gateway.repository.UserCredentialRepository;
import com.cloud.gateway.repository.UserRepository;
import com.cloud.gateway.security.JwtService;
import com.cloud.gateway.service.RedisService;
import com.cloud.gateway.service.UserService;
import com.cloud.gateway.utils.GenericUtils;
import com.cloud.gateway.utils.ResponseHandler;

import io.micrometer.core.instrument.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private RedisService redisService;

	@Autowired
	private UserCredentialRepository userCredentialRepository;

	@Override
	public ResponseEntity<Object> register(@Valid UserRegistrationDto registrationForm) {
		User user = new User();
		User fetchUser;
		try {
			fetchUser = userRepo.findByEmail(registrationForm.getEmail());
			if (Objects.nonNull(fetchUser)) {
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
						"user already register with email id");
			}
			user.setEmail(registrationForm.getEmail());
			user.setPassword(bCryptPasswordEncoder.encode(registrationForm.getPassword()));
			User saveDetail = userRepo.save(user);
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "user registration successfully", saveDetail);
		} catch (Exception e) {
			return ResponseHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, false,
					"something wrong ! try again aater");
		}
	}

	@Override
	public ResponseEntity<Object> login(@Valid LoginDTO loginDTO) {
		try {
			User fetchUser = userRepo.findByEmail(loginDTO.getEmail());
			if (Objects.isNull(fetchUser)) {
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user not exists");
			}
			if (bCryptPasswordEncoder.matches(loginDTO.getPassword(), fetchUser.getPassword())) {
				LoginResponseDTO loginResponseDTO = createResponse(fetchUser);
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "user login successfully",
						loginResponseDTO);
			}
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user not login successfully");
		} catch (Exception e) {
			return ResponseHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, false,
					"something wrong ! try again aater");
		}
	}

	private LoginResponseDTO createResponse(User fetchUser) {
		LoginResponseDTO loginResponseDTO = new LoginResponseDTO(fetchUser.getId(), fetchUser.getEmail());
		loginResponseDTO.setAuthorization(jwtService.getToken(fetchUser));
		return loginResponseDTO;
	}

	@Override
	public ResponseEntity<Object> logout(UserDetails loggedInUser) {
		List<String> sessions = redisService.getDataFromRedis(loggedInUser.getEmail());
		if (sessions.contains(loggedInUser.getAuthorization())) {
			sessions.remove(loggedInUser.getAuthorization());
			redisService.saveDataInRedis(loggedInUser.getEmail(), sessions);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
					"Your session has been expired. Please login again");
		}
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "user logoutsuccessfully", loggedInUser);
	}

	@Override
	public ResponseEntity<Object> userCredentials(UserCredentialDto userCredentialDto) {
		UserCredentials userCredentials = new UserCredentials();
		Long userId = Long.valueOf(GenericUtils.getLoggedInUser().getUserId());
		UserCredentials fetchUser;
		try {
			User user = userRepo.findById(userId).orElse(null);
			if (Objects.isNull(user)) {
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user not exists");
			}
			UserCredentials fetchuserCedCredentials = userCredentialRepository.findByUserId(userId);
			if (Objects.nonNull(fetchuserCedCredentials)) {
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
						"user credential alredy exist please update the credentials", fetchuserCedCredentials);
			}
			if (Objects.nonNull(userCredentialDto.getAwsAccessKey())) {
				fetchUser = userCredentialRepository.findByAwsAccessKey(userCredentialDto.getAwsAccessKey());
				if (Objects.nonNull(fetchUser)) {
					return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
							"user already register with aws access key");
				}
				userCredentials.setAwsAccessKey(userCredentialDto.getAwsAccessKey());
			}
			if (Objects.nonNull(userCredentialDto.getAwsSecretKey())) {
				fetchUser = userCredentialRepository.findByAwsSecretKey(userCredentialDto.getAwsSecretKey());
				if (Objects.nonNull(fetchUser)) {
					return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
							"user already register with aws secret key");
				}
				userCredentials.setAwsSecretKey(userCredentialDto.getAwsSecretKey());
			}
			if (Objects.nonNull(userCredentialDto.getAzureAccessKey())) {
				fetchUser = userCredentialRepository.findByAzureAccessKey(userCredentialDto.getAzureAccessKey());
				if (Objects.nonNull(fetchUser)) {
					return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
							"user already register with azure access key");
				}
				userCredentials.setAzureAccessKey(userCredentialDto.getAzureAccessKey());
			}
			if (Objects.nonNull(userCredentialDto.getAzureSecretkey())) {
				fetchUser = userCredentialRepository.findByAzureSecretkey(userCredentialDto.getAzureSecretkey());
				if (Objects.nonNull(fetchUser)) {
					return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
							"user already register with azure secret key");
				}
				userCredentials.setAzureSecretkey(userCredentialDto.getAzureSecretkey());
			}
			if (Objects.nonNull(userCredentialDto.getAzureTenantId())) {
				fetchUser = userCredentialRepository.findByAzureSecretkey(userCredentialDto.getAzureTenantId());
				if (Objects.nonNull(fetchUser)) {
					return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
							"user already register with azure tenant id");
				}
				userCredentials.setAzureTenantId(userCredentialDto.getAzureTenantId());
			}
			if(Objects.nonNull(userCredentialDto.getIbmUser())){
				fetchUser = userCredentialRepository.findByIbmUser(userCredentialDto.getIbmUser());
				if(Objects.nonNull(fetchUser)){
					return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
							"user already register with IBM user");
				}
				fetchuserCedCredentials.setIbmUser(userCredentialDto.getIbmUser());
			}
			if(Objects.nonNull(userCredentialDto.getIbmUserApiKey())){
				fetchUser = userCredentialRepository.findByIbmUserApiKey(userCredentialDto.getIbmUserApiKey());
				if(Objects.nonNull(fetchUser)){
					return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
							"user already register with IBM user Api key");
				}
				fetchuserCedCredentials.setIbmUserApiKey(userCredentialDto.getIbmUserApiKey());
			}
			if(Objects.nonNull(userCredentialDto.getIbmClientId())){
				fetchUser = userCredentialRepository.findByIbmClientId(userCredentialDto.getIbmClientId());
				if(Objects.nonNull(fetchUser)){
					return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
							"user already register with IBM CLIENT ID");
				}
				fetchuserCedCredentials.setIbmClientId(userCredentialDto.getIbmClientId());
			}
			if(Objects.nonNull(userCredentialDto.getIbmClientSecret())){
				fetchUser = userCredentialRepository.findByIbmClientSecret(userCredentialDto.getIbmClientSecret());
				if(Objects.nonNull(fetchUser)){
					return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
							"user already register with IBM IBM CLIENT SECRET KEY");
				}
				fetchuserCedCredentials.setIbmClientSecret(userCredentialDto.getIbmClientSecret());
			}

			userCredentials.setUser(user);
			UserCredentials save = userCredentialRepository.save(userCredentials);
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "user credentials save successfully", save);
		} catch (Exception e) {
			return ResponseHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, false,
					"something wrong ! try again aater");
		}
	}

	@Override
	public ResponseEntity<Object> getUserDetail(Long userId) {
		HashMap<String, String> map = new HashMap<>();
		try {
			User user = userRepo.findById(userId).orElse(null);
			if (Objects.isNull(user)) {
				return ResponseHandler.generateResponse(HttpStatus.OK, false, "user not exists", null);
			}
			UserCredentials fetchuserCedCredentials = userCredentialRepository.findByUserId(user.getId());
			if (Objects.isNull(fetchuserCedCredentials)) {
				return ResponseHandler.generateResponse(HttpStatus.OK, false, "user credential not exists", null);
			}
			map.put("userId", "" + user.getId());
			map.put("email", "" + user.getEmail());
			map.put("awsAccessKey", Objects.isNull(fetchuserCedCredentials.getAwsAccessKey()) ? ""
					: fetchuserCedCredentials.getAwsAccessKey());
			map.put("awsSecretKey", Objects.isNull(fetchuserCedCredentials.getAwsSecretKey()) ? ""
					: fetchuserCedCredentials.getAwsSecretKey());
			map.put("azureAccessKey", Objects.isNull(fetchuserCedCredentials.getAzureAccessKey()) ? ""
					: fetchuserCedCredentials.getAzureAccessKey());
			map.put("azureSecretKey", Objects.isNull(fetchuserCedCredentials.getAzureSecretkey()) ? ""
					: fetchuserCedCredentials.getAzureSecretkey());
			map.put("azureTenantId", Objects.isNull(fetchuserCedCredentials.getAzureTenantId()) ? ""
					: fetchuserCedCredentials.getAzureTenantId());
			map.put("ibmUser", Objects.isNull(fetchuserCedCredentials.getIbmUser()) ? ""
					: fetchuserCedCredentials.getIbmUser());
			map.put("ibmUserApiKey", Objects.isNull(fetchuserCedCredentials.getIbmUserApiKey()) ? ""
					: fetchuserCedCredentials.getIbmUserApiKey());
			map.put("ibmClientId", Objects.isNull(fetchuserCedCredentials.getIbmClientId()) ? ""
					: fetchuserCedCredentials.getIbmClientId());
			map.put("ibmClientSecret", Objects.isNull(fetchuserCedCredentials.getIbmClientSecret()) ? ""
					: fetchuserCedCredentials.getIbmClientSecret());
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "detail fetch successfully", map);
		} catch (Exception e) {
			return ResponseHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, false,
					"something wrong ! try again aater", null);
		}
	}

	@Override
	public ResponseEntity<Object> updateCredentials(UserCredentialDto userCredentialDto, Long id) {
		UserCredentials fetchuserCedCredentials = userCredentialRepository.findById(id).orElse(null);
		UserCredentials getAllDetail;
		if (Objects.isNull(fetchuserCedCredentials)) {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credential not exists");
		}
		if (!StringUtils.isEmpty(userCredentialDto.getAwsAccessKey())) {
			getAllDetail = userCredentialRepository.findByAwsAccessKey(userCredentialDto.getAwsAccessKey());
			if (Objects.nonNull(getAllDetail)) {
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
						"user already register with aws access key");
			}
			fetchuserCedCredentials.setAwsAccessKey(userCredentialDto.getAwsAccessKey());
		}
		if (!StringUtils.isEmpty(userCredentialDto.getAwsSecretKey())) {
			getAllDetail = userCredentialRepository.findByAwsSecretKey(userCredentialDto.getAwsSecretKey());
			if (Objects.nonNull(getAllDetail)) {
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
						"user already register with aws secret key");
			}
			fetchuserCedCredentials.setAwsSecretKey(userCredentialDto.getAwsSecretKey());
		}
		if (Objects.nonNull(userCredentialDto.getAzureAccessKey())) {
			getAllDetail = userCredentialRepository.findByAzureAccessKey(userCredentialDto.getAzureAccessKey());
			if (Objects.nonNull(getAllDetail)) {
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
						"user already register with azure access key");
			}
			fetchuserCedCredentials.setAzureAccessKey(userCredentialDto.getAzureAccessKey());
		}
		if (Objects.nonNull(userCredentialDto.getAzureSecretkey())) {
			getAllDetail = userCredentialRepository.findByAzureSecretkey(userCredentialDto.getAzureSecretkey());
			if (Objects.nonNull(getAllDetail)) {
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
						"user already register with azure secret key");
			}
			fetchuserCedCredentials.setAzureSecretkey(userCredentialDto.getAzureSecretkey());
		}
		if (Objects.nonNull(userCredentialDto.getAzureTenantId())) {
			getAllDetail = userCredentialRepository.findByAzureSecretkey(userCredentialDto.getAzureTenantId());
			if (Objects.nonNull(getAllDetail)) {
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
						"user already register with azure tenant id");
			}
			fetchuserCedCredentials.setAzureTenantId(userCredentialDto.getAzureTenantId());
		}
		if(Objects.nonNull(userCredentialDto.getIbmUser())){
			if(!StringUtils.isEmpty(userCredentialDto.getIbmUser())) {
				getAllDetail = userCredentialRepository.findByIbmUser(userCredentialDto.getIbmUser());
				if (Objects.nonNull(getAllDetail)) {
					return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
							"user already register with IBM USER");
				}
				fetchuserCedCredentials.setIbmUser(userCredentialDto.getIbmUser());
			}else{
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
						"IBM USER IS EMPTY");
			}
		}
		if(Objects.nonNull(userCredentialDto.getIbmUserApiKey())){
			if(!StringUtils.isEmpty(userCredentialDto.getIbmUserApiKey())) {
				getAllDetail = userCredentialRepository.findByIbmUserApiKey(userCredentialDto.getIbmUserApiKey());
				if (Objects.nonNull(getAllDetail)) {
					return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
							"user already register with IBM USER API KEY");
				}
				fetchuserCedCredentials.setIbmUserApiKey(userCredentialDto.getIbmUserApiKey());
			}else{
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
						" IBM USER API KEY IS EMPTY");
			}
		}
		if(Objects.nonNull(userCredentialDto.getIbmClientId())){
			if(!StringUtils.isEmpty(userCredentialDto.getIbmClientId())) {
				getAllDetail = userCredentialRepository.findByIbmClientId(userCredentialDto.getIbmClientId());
				if (Objects.nonNull(getAllDetail)) {
					return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
							"user already register with IBM CLIENT ID");
				}
				fetchuserCedCredentials.setIbmClientId(userCredentialDto.getIbmClientId());
			}else{
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
						"IBM CLIENT ID IS EMPTY");
			}
		}
		if(Objects.nonNull(userCredentialDto.getIbmClientSecret())){
			if(!StringUtils.isEmpty(userCredentialDto.getIbmClientSecret())) {
				getAllDetail = userCredentialRepository.findByIbmClientSecret(userCredentialDto.getIbmClientSecret());
				if (Objects.nonNull(getAllDetail)) {
					return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
							"user already register with  IBM CLIENT SECRET KEY");
				}
				fetchuserCedCredentials.setIbmClientSecret(userCredentialDto.getIbmClientSecret());
			}else{
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
						"IBM CLIENT SECRET KEY IS EMPTY");
			}
		}
		UserCredentials save = userCredentialRepository.saveAndFlush(fetchuserCedCredentials);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "user credentials update successfully", save);
	}

}
