package com.cloud.ibm.feignClient;

import com.cloud.ibm.mapping.URLMapping;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "cloudgateway", url = "${cloud.service.url}")
public interface CloudGatwayFeignController {

    @GetMapping(value = URLMapping.GET_USER_DETAIL)
    public ResponseEntity<Object> getUserDetail(@RequestParam("userId") Long userId);
}
