package com.cloud.ibm.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class RestCallService {

    private static final Logger logger = LoggerFactory.getLogger(IbmServiceImpl.class);

    public <T, V> V publishRequest(String url, T t, HttpMethod httpMethod, Class className) {
        logger.info("RestCallService::publishRequest::send request to url: {}, request payload: {}",url,t);
        RestTemplate restTemplate = new RestTemplate();
        V responseData = null;
        try {
            ResponseEntity<V> responseEntity = restTemplate.exchange(url, httpMethod, new HttpEntity<>(t, getHeaders()), className);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                responseData = responseEntity.getBody();
            }
        } catch (Exception e) {
            logger.error("RestCallService::publishRequest::EXCEPTION IN PUBLISH REQUEST:: {}, FOR URL:: {}, AND REQUEST:: {}",e.getMessage(),url,t);
        }
        return responseData;
    }

    public ResponseEntity<String> sendRequest(String url, HttpMethod methodType, JSONObject requestPayload){

        logger.info("RestCallService::sendRequest:: URL:: {}, REQUEST PAYLOAD:: {}",url);
        ResponseEntity<String> responseEntity = null;
        try {
            HttpEntity<String> requestEntity = new HttpEntity<>(requestPayload.toString(), getHeaders());
            RestTemplate restTemplate=new RestTemplate();
            responseEntity = restTemplate.exchange(url, methodType, requestEntity, String.class);
            logger.info("RestCallService::sendRequest:: RESPONSE :: {}",responseEntity);
        }
        catch (RestClientException ex){
            logger.error("RestCallService::sendRequest::RESTCLIENTEXCEPTION OCCURRED FOR THE REQUEST URL:: {}, AND REQUEST PAYLOAD:: {}, RESPONSE:: {}",url,requestPayload,responseEntity,ex);
        }
        catch (Exception ex){
            logger.error("RestCallService::sendRequest::EXCEPTION OCCURRED FOR THE REQUEST URL:: {}, AND REQUEST PAYLOAD:: {}, RESPONSE:: {}",url,requestPayload,responseEntity,ex);
        }

        return responseEntity;
    }

    public <T, V> V publishRequestWithHeader(String url, T t, HttpMethod httpMethod, Class className, HttpHeaders headers) {
        logger.info("RestCallService::publishRequestWithHeader::SEND REQUEST TO URL: {}, REQUEST PAYLOAD: {}");
        ResponseEntity<V> responseEntity = null;
        RestTemplate restTemplate = new RestTemplate();
        V responseData = null;
        try {
            responseEntity = restTemplate.exchange(url, httpMethod, new HttpEntity<>(t, headers), className);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                responseData = responseEntity.getBody();
            }
            logger.info("RestCallService::publishRequestWithHeader:: RESPONSE :: {}",responseEntity);
        } catch (Exception e) {
            logger.error("RestCallService::publishRequestWithHeader::EXCEPTION IN PUBLISH REQUEST WITH HEADER:: {}, URL:: {}, REQUEST JSON:: {}");
        }
        return responseData;
    }

    private HttpHeaders getHeaders(){
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return requestHeaders;
    }

    public <T, V> V sendRequestWithHeader(String url, HttpMethod httpMethod, HttpEntity headers, Class className) {
        logger.info("RestCallService::sendRequestWithHeader::SEND REQUEST TO URL: {}, REQUEST PAYLOAD: {}");
        ResponseEntity<V> responseEntity = null;
        RestTemplate restTemplate = new RestTemplate();
        V responseData = null;
        try {
            responseEntity = restTemplate.exchange(url, httpMethod, headers, className);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                responseData = responseEntity.getBody();
            }
            logger.info("RestCallService::sendRequestWithHeader:: RESPONSE :: {}",responseEntity);
        } catch (Exception e) {
            logger.error("RestCallService::sendRequestWithHeader::EXCEPTION IN PUBLISH REQUEST WITH HEADER:: {}, URL:: {}, REQUEST JSON:: {}");
        }
        return responseData;
    }
}
