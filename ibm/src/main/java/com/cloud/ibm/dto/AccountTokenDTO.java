package com.cloud.ibm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountTokenDTO {
    private String access_token;
    private String refresh_token;
    private Long ims_user_id;
    private String token_type;
    private Long expires_in;
    private Long expiration;
    private String scope;

}
