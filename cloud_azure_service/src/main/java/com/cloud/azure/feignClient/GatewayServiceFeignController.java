package com.cloud.azure.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import static com.cloud.azure.config.UrlConstant.*;

@FeignClient(name = "cloudgateway", url = "${cloud.service.url}")
public interface GatewayServiceFeignController {

	@GetMapping(GET_USER_DETAIL)
	public ResponseEntity<Object> getUserDetail(@RequestParam("userId") Long userId);

}
