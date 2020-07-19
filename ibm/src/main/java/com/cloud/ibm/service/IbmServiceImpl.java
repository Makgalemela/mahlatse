package com.cloud.ibm.service;

import com.cloud.ibm.constant.ErrorCode;
import com.cloud.ibm.constant.ErrorDescription;
import com.cloud.ibm.constant.Params;
import com.cloud.ibm.dto.AccountTokenDTO;
import com.cloud.ibm.dto.ErrorResponseDTO;
import com.cloud.ibm.feignClient.CloudGatwayFeignController;
import com.cloud.ibm.mapping.IBMUrl;
import com.cloud.ibm.util.GenericUtil;
import com.cloud.ibm.util.ResponseHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.softlayer.api.ApiClient;
import com.softlayer.api.RestApiClient;
import com.softlayer.api.service.Account;
import com.softlayer.api.service.network.Pod;
import com.softlayer.api.service.product.Package;
import com.softlayer.api.service.product.item.Price;
import com.softlayer.api.service.user.Customer;
import com.softlayer.api.service.virtual.Guest;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.util.*;

@Service
public class IbmServiceImpl implements IbmService{

    private static final Logger logger = LoggerFactory.getLogger(IbmServiceImpl.class);

    @Autowired
    private CloudGatwayFeignController cloudGatewayFeign;

    @Autowired
    private RestCallService restCallService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CpuAndMemoryUsageService cpuAndMemoryUsageService;

    @Autowired
    private ProductPackage productPackage;

    @Override
    public ResponseEntity<Object> testApiRegister() {
        List<ErrorResponseDTO> errors = new ArrayList<>();
        String success = "Success";
        Map<String, String> map = getUserCredentials();
        logger.info("IbmServiceImpl :: testApiRegister :: STATUS {} ",success);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> getCatalogProduct(String locale, int page, int PageSize) {
        logger.info("IbmServiceImpl :: getCatalogProduct :: PROCESSING CATALOG PRODUCT ");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        String response = null;
        Map<String, String> mapUser = new HashMap<>();
        try{
            Map<String, String> userDetail = getUserCredentials();
            if (userDetail.isEmpty()) {
                return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
            }
            logger.info("IbmServiceImpl :: getCatalogProduct :: Found User Details");

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add(Params.LOCALE, locale);
            params.add(Params.PAGE, String.valueOf(page));
            params.add(Params.PAGE_SIZE, String.valueOf(PageSize));

            String ibmClientId = userDetail.get(Params.IBM_CLIENT_ID);
            String ibmClientSecret = userDetail.get(Params.IBM_CLIENT_SECRET);
            if(StringUtils.isEmpty(ibmClientId) || Objects.isNull(ibmClientId)){
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_CLIENT_ID, ErrorDescription.UPDATE_CLIENT_ID));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            }else if(StringUtils.isEmpty(ibmClientSecret) || Objects.isNull(ibmClientSecret)){
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_CLIENT_SECRET, ErrorDescription.UPDATE_CLIENT_SECRET));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            }
            String url = UriComponentsBuilder.fromUriString(IBMUrl.CATALOG_PRODUCT_API).queryParams(params).build().toUri().toString().trim();

            response = restCallService.publishRequestWithHeader(url, new JSONObject(), HttpMethod.GET, String.class, getCatalogHeader(ibmClientId, ibmClientSecret));

            mapUser = objectMapper.readValue(response, Map.class);
        }catch (Exception e){
            logger.error("IbmServiceImpl :: getCatalogProduct :: Error {} ", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "CATALOG Product", mapUser);
    }

    @Override
    public ResponseEntity<Object> getVirtualGuest() {
        logger.info("IbmServiceImpl :: getVirtualGuest :: PROCESSING VIRTUAL GUEST ITEM ");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        Gson gson = new Gson();
        List<Map<String, Object>> guestMap = new ArrayList<Map<String, Object>>();
        try{
            Map<String, String> userDetail = getUserCredentials();
            if (userDetail.isEmpty()) {
                return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
            }
            logger.info("IbmServiceImpl :: getVirtualGuest :: Found User Details");
            String ibmUser = userDetail.get(Params.IBM_USER);
            String ibmUserApiKey = userDetail.get(Params.IBM_USER_API_KEY);
            if (StringUtils.isEmpty(ibmUser) || Objects.isNull(ibmUser)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER, ErrorDescription.UPDATE_IBM_USER));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            } else if (StringUtils.isEmpty(ibmUserApiKey) || Objects.isNull(ibmUserApiKey)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER_API_KEY, ErrorDescription.UPDATE_IBM_USER_API_KEY));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            }
            // Declare the API client
            ApiClient client = new RestApiClient().withCredentials(ibmUser, ibmUserApiKey);
            Account.Service accountService = Account.service(client);

            accountService.setMask("mask[id, hostname, domain, datacenter[longName], billingItem[item]]");
            String virtualGuest = gson.toJson(accountService.getVirtualGuests());
            if(Objects.nonNull(virtualGuest)){
                guestMap = objectMapper.readValue(virtualGuest, new TypeReference<List<Map<String, Object>>>(){});
            }else {
                guestMap = objectMapper.readValue("Error", new TypeReference<List<Map<String, Object>>>(){});
            }
        }catch(Exception e){
            logger.error("IbmServiceImpl :: getVirtualGuestItem :: Unable to retrieve and Error {}", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "Billing Virtual Guest", guestMap);
    }

    @Override
    public ResponseEntity<Object> getDedicatedVirtualGuest() {
        logger.info("IbmServiceImpl :: getDedicatedVirtualGuest :: PROCESSING DEDICATED VIRTUAL GUEST ITEM ");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        Map<String, String> mapUser = new HashMap<>();
        String response= null;
        try {
            Map<String, String> userDetail = getUserCredentials();
            if (userDetail.isEmpty()) {
                return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
            }
            logger.info("IbmServiceImpl :: getDedicatedVirtualGuest :: Found User Details");
            String ibmUser = userDetail.get(Params.IBM_USER);
            String ibmUserApiKey = userDetail.get(Params.IBM_USER_API_KEY);
            if (StringUtils.isEmpty(ibmUser) || Objects.isNull(ibmUser)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER, ErrorDescription.UPDATE_IBM_USER));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            } else if (StringUtils.isEmpty(ibmUserApiKey) || Objects.isNull(ibmUserApiKey)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER_API_KEY, ErrorDescription.UPDATE_IBM_USER_API_KEY));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            }
            // Declare the API client
            ApiClient client = new RestApiClient().withCredentials(ibmUser, ibmUserApiKey);

            for (Guest guest : Account.service(client).getVirtualGuests()) {
                /*System.out.println("HOSTNAME :: "+guest.getHostname());
                System.out.println("DOMAIN :: "+guest.getDomain());*/

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                response = gson.toJson(guest);
                /*System.out.println(response);*/
            }
            if(Objects.nonNull(response)){
                mapUser = objectMapper.readValue(response, Map.class);
                logger.info("IbmServiceImpl :: getDedicatedVirtualGuest :: SUCCESS");
            }
        }catch (Exception e){
            logger.error("IbmServiceImpl :: getDedicatedVirtualGuest :: Unable to retrieve and Error {}", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "DEDICATED VIRTUAL GUEST", mapUser);
    }

    @Override
    public ResponseEntity<Object> getInvoiceDetails() {
        logger.info("IbmServiceImpl :: getInvoiceDetails :: PROCESSING INVOICE  DETAILS");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        List<Map<String, Object>> mapUser = new ArrayList<Map<String, Object>>();
        Gson gson = new Gson();
        String response= null;
        try{
            Map<String, String> userDetail = getUserCredentials();
            if (userDetail.isEmpty()) {
                return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
            }
            logger.info("IbmServiceImpl :: getInvoiceDetails :: Found User Details");
            String ibmUser = userDetail.get(Params.IBM_USER);
            String ibmUserApiKey = userDetail.get(Params.IBM_USER_API_KEY);
            if (StringUtils.isEmpty(ibmUser) || Objects.isNull(ibmUser)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER, ErrorDescription.UPDATE_IBM_USER));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            } else if (StringUtils.isEmpty(ibmUserApiKey) || Objects.isNull(ibmUserApiKey)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER_API_KEY, ErrorDescription.UPDATE_IBM_USER_API_KEY));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            }
            // Declare the API client
            ApiClient client = new RestApiClient().withCredentials(ibmUser, ibmUserApiKey);

            Account.Service accountService = Account.service(client);

            // Declaring the object mask to get information about the billing item.
            accountService.setMask("mask[payment,amount,invoiceTotalOneTimeAmount,invoiceTotalRecurringAmount,invoiceTotalOneTimeTaxAmount,invoiceTotalRecurringTaxAmount]");

            response = gson.toJson(accountService.getInvoices());
            if(Objects.nonNull(response)){
                mapUser  = objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>(){});
                logger.info("IbmServiceImpl :: getInvoiceDetails :: SUCCESS");
            }
        }catch (Exception e){
            logger.error("IbmServiceImpl :: getInvoiceDetails :: Unable to retrieve and Error {}", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "GET INVOICE DETAILS", mapUser);
    }

    @Override
    public ResponseEntity<Object> getAllObjectDetails() {
        logger.info("IbmServiceImpl :: getAllObjectDetails :: PROCESSING ALL OBJECT DETAILS  ");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        List<Map<String, Object>> mapUser = new ArrayList<Map<String, Object>>();
        Gson gson = new Gson();
        String response= null;
        try{
            Map<String, String> userDetail = getUserCredentials();
            if (userDetail.isEmpty()) {
                return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
            }
            logger.info("IbmServiceImpl :: getAllObjectDetails :: Found User Details");
            String ibmUser = userDetail.get(Params.IBM_USER);
            String ibmUserApiKey = userDetail.get(Params.IBM_USER_API_KEY);
            if (StringUtils.isEmpty(ibmUser) || Objects.isNull(ibmUser)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER, ErrorDescription.UPDATE_IBM_USER));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            } else if (StringUtils.isEmpty(ibmUserApiKey) || Objects.isNull(ibmUserApiKey)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER_API_KEY, ErrorDescription.UPDATE_IBM_USER_API_KEY));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            }
            // Declare the API client
            ApiClient client = new RestApiClient().withCredentials(ibmUser, ibmUserApiKey);

            Pod.Service service = Pod.service(client);

            List<Pod> allHW = service.getAllObjects();
            gson = (new GsonBuilder()).setPrettyPrinting().create();
            response = gson.toJson(allHW);
            if(Objects.nonNull(response)){
                mapUser  = objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>(){});
                logger.info("IbmServiceImpl :: getAllObjectDetails :: SUCCESS");
            }else{
                mapUser = objectMapper.readValue("ERROR", new TypeReference<List<Map<String, Object>>>(){});
            }
        }catch (Exception e){
            logger.error("IbmServiceImpl :: getAllObjectDetails :: Unable to retrieve and Error {}", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "GET All OBJECT DETAILS", mapUser);
    }

    @Override
    public ResponseEntity<Object> getAccountDetails() {
        logger.info("IbmServiceImpl :: getAccountDetails :: PROCESSING ACCOUNT DETAILS  ");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        Map<String, String> mapUser = new HashMap<>();
        try {
            Map<String, String> userDetail = getUserCredentials();
            if (userDetail.isEmpty()) {
                return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
            }
            logger.info("IbmServiceImpl :: getAccountDetails :: Found User Details");
            String ibmUser = userDetail.get(Params.IBM_USER);
            String ibmUserApiKey = userDetail.get(Params.IBM_USER_API_KEY);
            if (StringUtils.isEmpty(ibmUser) || Objects.isNull(ibmUser)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER, ErrorDescription.UPDATE_IBM_USER));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            } else if (StringUtils.isEmpty(ibmUserApiKey) || Objects.isNull(ibmUserApiKey)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER_API_KEY, ErrorDescription.UPDATE_IBM_USER_API_KEY));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            }
            Map<String, String> tokenDetails = getAccountTokenAPI(ibmUser, ibmUserApiKey);
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add(Params.OBJECT_MASK, Params.NETWORK_LAN);
            String url = UriComponentsBuilder.fromUriString(IBMUrl.ACCOUNT_SOFT_LAYER).queryParams(params).build().toUri().toString().trim();

            String response = restCallService.publishRequestWithHeader(url, new JSONObject(), HttpMethod.GET, String.class, getAuthAccount(tokenDetails.get(Params.ACCESS_TOKEN)));
            JSONObject jsonData = XML.toJSONObject(response);
            mapUser  = objectMapper.readValue(jsonData.toString(), Map.class);
        }catch (Exception e){
            logger.error("IbmServiceImpl :: getAccountDetails :: Unable to retrieve and Error {}", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "GET ACCOUNT DETAILS", mapUser);
    }

    @Override
    public ResponseEntity<Object> getDatabaseDetails() {
        List<ErrorResponseDTO> errors = new ArrayList<>();
        logger.info("IbmServiceImpl :: getDatabaseDetails :: PROCESSING DATABASE DETAILS  ");
        Map<String, String> mapUser = new HashMap<>();
        try {
            Map<String, String> userDetail = getUserCredentials();
            if (userDetail.isEmpty()) {
                return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
            }
            logger.info("IbmServiceImpl :: getDatabaseDetails :: Found User Details");
            String ibmUser = userDetail.get(Params.IBM_USER);
            String ibmUserApiKey = userDetail.get(Params.IBM_USER_API_KEY);
            if (StringUtils.isEmpty(ibmUser) || Objects.isNull(ibmUser)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER, ErrorDescription.UPDATE_IBM_USER));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            } else if (StringUtils.isEmpty(ibmUserApiKey) || Objects.isNull(ibmUserApiKey)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER_API_KEY, ErrorDescription.UPDATE_IBM_USER_API_KEY));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            }
            Map<String, String> tokenDetails = getAccountTokenAPI(ibmUser, ibmUserApiKey);

            String response = restCallService.publishRequestWithHeader(IBMUrl.DATABASE_DETAILS_API, new JSONObject(), HttpMethod.GET, String.class, getAuthAccount(tokenDetails.get(Params.ACCESS_TOKEN)));
            mapUser  = objectMapper.readValue(response, Map.class);
        }catch (Exception e){
            logger.error("IbmServiceImpl :: getDatabaseDetails :: Unable to retrieve and Error {}", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "GET DATABASE DETAILS", mapUser);
    }

    @Override
    public ResponseEntity<Object> getInstanceDetails(String version, String generation) {
        logger.info("IbmServiceImpl :: getInstanceDetails :: PROCESSING INSTANCE DETAILS  ");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        Map<String, String> mapUser = new HashMap<>();
        try {
            Map<String, String> userDetail = getUserCredentials();
            if (userDetail.isEmpty()) {
                return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
            }
            logger.info("IbmServiceImpl :: getInstanceDetails :: Found User Details");
            String ibmUser = userDetail.get(Params.IBM_USER);
            String ibmUserApiKey = userDetail.get(Params.IBM_USER_API_KEY);
            if (StringUtils.isEmpty(ibmUser) || Objects.isNull(ibmUser)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER, ErrorDescription.UPDATE_IBM_USER));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            } else if (StringUtils.isEmpty(ibmUserApiKey) || Objects.isNull(ibmUserApiKey)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER_API_KEY, ErrorDescription.UPDATE_IBM_USER_API_KEY));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            }
            Map<String, String> tokenDetails = getAccountTokenAPI(ibmUser, ibmUserApiKey);
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add(Params.VERSION, version);
            params.add(Params.GENERATION, generation);

            String url = UriComponentsBuilder.fromUriString(IBMUrl.INSTANCE_DETAILS_API).queryParams(params).build().toUri().toString().trim();

            String response = restCallService.publishRequestWithHeader(url, new JSONObject(), HttpMethod.GET, String.class, getInstanceToken(tokenDetails.get(Params.ACCESS_TOKEN)));
            mapUser  = objectMapper.readValue(response, Map.class);
        }catch (Exception e){
            logger.error("IbmServiceImpl :: getInstanceDetails :: Unable to retrieve and Error {}", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "GET INSTANCE DETAILS", mapUser);
    }

    @Override
    public ResponseEntity<Object> getCpuAndMemoryUsage(Long serverId, String startDate, String endDate) {
        logger.info("IbmServiceImpl :: getCpuAndMemoryUsage :: PROCESSING CPU AND MEMORY DETAILS  ");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        Map<String, String> mapUser = new HashMap<>();
        try {
            Map<String, String> userDetail = getUserCredentials();
            if (userDetail.isEmpty()) {
                return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
            }
            logger.info("IbmServiceImpl :: getCpuAndMemoryUsage :: Found User Details");
            String ibmUser = userDetail.get(Params.IBM_USER);
            String ibmUserApiKey = userDetail.get(Params.IBM_USER_API_KEY);
            if (StringUtils.isEmpty(ibmUser) || Objects.isNull(ibmUser)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER, ErrorDescription.UPDATE_IBM_USER));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            } else if (StringUtils.isEmpty(ibmUserApiKey) || Objects.isNull(ibmUserApiKey)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER_API_KEY, ErrorDescription.UPDATE_IBM_USER_API_KEY));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            }
            mapUser = cpuAndMemoryUsageService.getCpuAndMemoryDetails(ibmUser, ibmUserApiKey, serverId, startDate, endDate);
        }catch (Exception e){
            logger.error("IbmServiceImpl :: getCpuAndMemoryUsage :: Unable to retrieve and Error {}", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "GET CPU AND MEMORY DETAILS", mapUser);
    }

    @Override
    public ResponseEntity<Object> getProductPackage() {
        logger.info("IbmServiceImpl :: getProductPackage :: PROCESSING PRODUCT PACKAGE DETAILS  ");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        List<Map<String, Object>> mapUser = new ArrayList<Map<String, Object>>();
        try {
            Map<String, String> userDetail = getUserCredentials();
            if (userDetail.isEmpty()) {
                return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
            }
            logger.info("IbmServiceImpl :: getProductPackage :: Found User Details");
            String ibmUser = userDetail.get(Params.IBM_USER);
            String ibmUserApiKey = userDetail.get(Params.IBM_USER_API_KEY);
            if (StringUtils.isEmpty(ibmUser) || Objects.isNull(ibmUser)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER, ErrorDescription.UPDATE_IBM_USER));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            } else if (StringUtils.isEmpty(ibmUserApiKey) || Objects.isNull(ibmUserApiKey)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER_API_KEY, ErrorDescription.UPDATE_IBM_USER_API_KEY));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            }
            String response = productPackage.getProductPackageDetails(ibmUser, ibmUserApiKey);
            mapUser  = objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>(){});
        }catch (Exception e){
            logger.error("IbmServiceImpl :: getProductPackage :: Unable to retrieve and Error {}", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "GET PRODUCT PACKAGE DETAILS", mapUser);
    }

    @Override
    public ResponseEntity<Object> getCustomerUserDetails() {
        logger.info("IbmServiceImpl :: getCustomerUserDetails :: PROCESSING USER DETAILS  ");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        List<Map<String, Object>> mapUser = new ArrayList<Map<String, Object>>();
        try {
            Map<String, String> userDetail = getUserCredentials();
            if (userDetail.isEmpty()) {
                return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
            }
            logger.info("IbmServiceImpl :: getCustomerUserDetails :: Found User Details");
            String ibmUser = userDetail.get(Params.IBM_USER);
            String ibmUserApiKey = userDetail.get(Params.IBM_USER_API_KEY);
            if (StringUtils.isEmpty(ibmUser) || Objects.isNull(ibmUser)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER, ErrorDescription.UPDATE_IBM_USER));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            } else if (StringUtils.isEmpty(ibmUserApiKey) || Objects.isNull(ibmUserApiKey)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER_API_KEY, ErrorDescription.UPDATE_IBM_USER_API_KEY));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            }

            ApiClient client = new RestApiClient().withCredentials(ibmUser, ibmUserApiKey);
            Account.Service accountService = Account.service(client);

            List<Customer> customer = accountService.getUsers();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String response = gson.toJson(customer);
            mapUser  = objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>(){});
        }catch (Exception e){
            logger.error("IbmServiceImpl :: getCustomerUserDetails :: Unable to retrieve and Error {}", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "GET USER DETAILS", mapUser);

    }

    @Override
    public ResponseEntity<Object> getBandwidthMonitoring(Long serverId, String networkType, String sDate, String eDate) {
        logger.info("IbmServiceImpl :: getBandwidthMonitoring :: PROCESSING BANDWIDTH MONITORING DETAILS  ");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        List<Map<String, Object>> mapUser = new ArrayList<Map<String, Object>>();
        try {
            Map<String, String> userDetail = getUserCredentials();
            if (userDetail.isEmpty()) {
                return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
            }
            logger.info("IbmServiceImpl :: getBandwidthMonitoring :: Found User Details");
            String ibmUser = userDetail.get(Params.IBM_USER);
            String ibmUserApiKey = userDetail.get(Params.IBM_USER_API_KEY);
            if (StringUtils.isEmpty(ibmUser) || Objects.isNull(ibmUser)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER, ErrorDescription.UPDATE_IBM_USER));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            } else if (StringUtils.isEmpty(ibmUserApiKey) || Objects.isNull(ibmUserApiKey)) {
                errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER_API_KEY, ErrorDescription.UPDATE_IBM_USER_API_KEY));
                return new ResponseEntity<>(errors, HttpStatus.OK);
            }
            Long virtualServerId = new Long(serverId);
            // Declare the API client
            ApiClient client = new RestApiClient().withCredentials(ibmUser, ibmUserApiKey);
            Guest.Service vsiService = Guest.service(client, virtualServerId);

            String startDate = sDate.concat(Params.START_DATE);
            String endDate = eDate.concat(Params.END_DATE);

            GregorianCalendar gregorianStartDate = GenericUtil.getGregorianDate(startDate);
            GregorianCalendar gregorianEndDate = GenericUtil.getGregorianDate(endDate);

            Gson gson = new Gson();

            String response = gson.toJson(vsiService.getBandwidthDataByDate(gregorianStartDate, gregorianEndDate, networkType));

            if(Objects.nonNull(response)) {
                mapUser = objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
                logger.info("IbmServiceImpl :: getBandwidthMonitoring :: SUCCESS");
            }
        }catch (Exception e){
            logger.error("IbmServiceImpl :: getBandwidthMonitoring :: Unable to retrieve and Error {}", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "GET BANDWIDTH MONITORING DETAILS", mapUser);
    }


    @Override
    public ResponseEntity<Object> getItemPriceDetails(Long id) {
            logger.info("IbmServiceImpl :: getItemPriceDetails :: PROCESSING ITEM PRICE DETAILS  ");
            List<ErrorResponseDTO> errors = new ArrayList<>();
            List<Map<String, Object>> mapUser = new ArrayList<Map<String, Object>>();
            try {
                Map<String, String> userDetail = getUserCredentials();
                if (userDetail.isEmpty()) {
                    return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
                }
                logger.info("IbmServiceImpl :: getItemPriceDetails :: Found User Details");
                String ibmUser = userDetail.get(Params.IBM_USER);
                String ibmUserApiKey = userDetail.get(Params.IBM_USER_API_KEY);
                if (StringUtils.isEmpty(ibmUser) || Objects.isNull(ibmUser)) {
                    errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER, ErrorDescription.UPDATE_IBM_USER));
                    return new ResponseEntity<>(errors, HttpStatus.OK);
                } else if (StringUtils.isEmpty(ibmUserApiKey) || Objects.isNull(ibmUserApiKey)) {
                    errors.add(new ErrorResponseDTO(ErrorCode.UPDATE_IBM_USER_API_KEY, ErrorDescription.UPDATE_IBM_USER_API_KEY));
                    return new ResponseEntity<>(errors, HttpStatus.OK);
                }

                Long packageId = new Long(id);

                ApiClient client = new RestApiClient().withCredentials(ibmUser, ibmUserApiKey);
                Package.Service accountService = Package.service(client, packageId);

                List<Price> price =  accountService.getItemPrices();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                String response = gson.toJson(price);
                if(Objects.nonNull(response)){
                    mapUser = objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
                    logger.info("IbmServiceImpl :: getItemPriceDetails :: SUCCESS");
                }
        }catch (Exception e){
            logger.error("IbmServiceImpl :: getItemPriceDetails :: Unable to retrieve and Error {}", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "GET ITEM PRICE DETAILS", mapUser);
    }

    @Override
    public ResponseEntity<Object> getSoftwareDetails(String user, String host, String password) {
        logger.info("IbmServiceImpl :: getSoftwareDetails :: PROCESSING ITEM PRICE DETAILS  ");
        StringBuilder stringBuilder = new StringBuilder();
        String response[] = null;
        try {
            String softwareCommand = IBMUrl.SOFTWARE_DETAILS_COMMAND;
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            Session session=jsch.getSession(user, host, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.connect();

            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(softwareCommand);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);

            InputStream in=channel.getInputStream();
            channel.connect();
            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    stringBuilder.append(new String(tmp, 0, i));
                    response = stringBuilder.toString().split("\n");
                    logger.info("IbmServiceImpl :: getSoftwareDetails :: SUCCESS  ");
                }
                if(channel.isClosed()){
                    logger.info("IbmServiceImpl :: getSoftwareDetails :: exit-status :: {} ", channel.getExitStatus());
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception ee){}
            }
            channel.disconnect();
            session.disconnect();
            logger.info("IbmServiceImpl :: getSoftwareDetails :: status :: DISCONNECT");

        }catch (Exception e){
            logger.error("IbmServiceImpl :: getSoftwareDetails :: Unable to retrieve and Error {}", e.getMessage());
        }
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "GET SOFTWARE DETAILS", response);
    }

    private HttpHeaders getAuthAccount(String token){
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add(Params.AUTHORIZATION, "Bearer "+token);
        return requestHeaders;
    }

    private Map<String, String> getAccountTokenAPI(String ibmUser, String ibmUserApiKey){
        logger.info("IbmServiceImpl :: getAccountTokenAPI :: PROCESSING Account Token API ");
        Map<String, String> tokenDetails = new HashMap<>();
        try{
            String accountToken = restCallService.sendRequestWithHeader(IBMUrl.ACCOUNT_TOKEN_API, HttpMethod.POST, getAccountToken(ibmUser, ibmUserApiKey), String.class);
            tokenDetails = objectMapper.readValue(accountToken, Map.class);
        }catch (Exception e){
            logger.error("IbmServiceImpl :: getAccountTokenAPI :: Unable to retrieve and Error {}", e.getMessage());
        }
        return tokenDetails;

    }
    private HttpHeaders getInstanceToken(String token){
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        requestHeaders.add("Accept", MediaType.APPLICATION_JSON.toString());
        requestHeaders.add(Params.AUTHORIZATION, "Bearer "+token);
        return requestHeaders;
    }
    private HttpEntity getAccountToken(String ibmUser, String ibmUserApiKey){
        HttpHeaders requestHeaders = new HttpHeaders();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(Params.GRANT_TYPE, Params.URN_IBM_PARAM_AUTH.concat(ibmUser));
        params.add(ibmUser, ibmUserApiKey);
        requestHeaders.add("Content-Type",MediaType.APPLICATION_FORM_URLENCODED.toString());
        requestHeaders.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity formEntity = new HttpEntity<MultiValueMap<String, String>>(params, requestHeaders);
        return formEntity;
    }

    private HttpHeaders getCatalogHeader(String clientId, String clientSecret){
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.add(Params.X_IBM_CLIENT, clientId);
        requestHeaders.add(Params.X_IBM_CLIENT_SECRET, clientSecret);
        return requestHeaders;
    }

    private Map<String, String> getUserCredentials() {
        Long userId = Long.valueOf(GenericUtil.getLoggedInUser().getUserId());
        ResponseEntity<Object> getUserDetail = cloudGatewayFeign.getUserDetail(userId);
        JSONObject object = new JSONObject(getUserDetail).getJSONObject("body");
        if (!object.getBoolean("isSuccess")) {
            return null;
        }
        JSONObject dataFetch = new JSONObject(object.toString()).getJSONObject("data");
        try {
            Map<String, String> userDetail = objectMapper.readValue(dataFetch.toString(), Map.class);
            return userDetail;
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return null;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
