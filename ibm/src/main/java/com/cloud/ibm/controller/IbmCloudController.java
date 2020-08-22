package com.cloud.ibm.controller;

import com.cloud.ibm.constant.ErrorCode;
import com.cloud.ibm.constant.ErrorDescription;
import com.cloud.ibm.constant.Params;
import com.cloud.ibm.dto.ErrorResponseDTO;
import com.cloud.ibm.mapping.URLMapping;
import com.cloud.ibm.service.IbmService;
import com.cloud.ibm.util.ResponseHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@CrossOrigin("*")

public class IbmCloudController {

    private static final Logger logger = LoggerFactory.getLogger(IbmCloudController.class);

    @Autowired
    private IbmService ibmService;

    @GetMapping(value =URLMapping.TEST_SERVICE,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getTestApi() throws Exception {
        logger.info("IbmCloudController :: getTestApi ::PROCESSING METHOD");
        ResponseEntity status = ibmService.testApiRegister();
        return ResponseHandler.generateResponse(HttpStatus.OK, true, "user registration successfully", status);
    }

    @GetMapping(value = URLMapping.GET_CATALOG_PRODUCT)
    public ResponseEntity<Object> getCatalogProduct(@RequestParam("locale")String locale, @RequestParam("page") int page, @RequestParam("pageSize") int pageSize ){
        logger.info("IbmCloudController :: getCatalogProduct ::PROCESSING METHOD");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        if (StringUtils.isBlank(locale)){
            errors.add(new ErrorResponseDTO(ErrorCode.MISSION_LOCALE, ErrorDescription.MISSION_LOCALE));
            return new ResponseEntity<>(errors, HttpStatus.OK);
        }
        return ibmService.getCatalogProduct(locale, page, pageSize);
    }

    @GetMapping(value = URLMapping.GET_VIRTUAL_GUEST_ITEMS)
    public ResponseEntity<Object> getVirtualGuest(){
        logger.info("IbmCloudController :: getVirtualGuest ::PROCESSING METHOD");
        return ibmService.getVirtualGuest();
    }

    @GetMapping(value = URLMapping.GET_DEDICATED_VIRTUAL_GUEST)
    public ResponseEntity<Object> getDedicatedVirtualGuest(){
        logger.info("IbmCloudController :: getDedicatedVirtualGuest ::PROCESSING METHOD");
        return ibmService.getDedicatedVirtualGuest();
    }

    @GetMapping(value = URLMapping.GET_INVOICE)
    public ResponseEntity<Object> getInvoiceDetails(){
        logger.info("IbmCloudController :: getInvoiceDetails ::PROCESSING METHOD");
        return ibmService.getInvoiceDetails();
    }

    @GetMapping(value = URLMapping.GET_ALL_OBJECT_DETAILS)
    public ResponseEntity<Object> getAllObject(){
        logger.info("IbmCloudController :: getAllObject ::PROCESSING METHOD");
        return ibmService.getAllObjectDetails();
    }

    @GetMapping(value = URLMapping.GET_ACCOUNT_DETAILS)
    public ResponseEntity<Object> getAccountDetails(){
        logger.info("IbmCloudController :: getAccountDetails ::PROCESSING METHOD");
        return ibmService.getAccountDetails();
    }

    @GetMapping(value = URLMapping.GET_DATABASE_DETAILS)
    public ResponseEntity<Object> getDatabaseDetails(){
        logger.info("IbmCloudController :: getDatabaseDetails ::PROCESSING METHOD");
        return ibmService.getDatabaseDetails();
    }

    @GetMapping(value = URLMapping.GET_INSTANCE_DETAILS)
    public ResponseEntity<Object> getInstanceDetails(@RequestParam("versionDate") String versionDate, @RequestParam("generation") String generation){
        logger.info("IbmCloudController :: getInstanceDetails ::PROCESSING METHOD");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        if(!(Params.ONE.equalsIgnoreCase(generation)) && !(Params.TWO.equalsIgnoreCase(generation))){
            errors.add(new ErrorResponseDTO(ErrorCode.GENERATION_VALIDATION, ErrorDescription.GENERATION_VALIDATION));
            return new ResponseEntity<>(errors, HttpStatus.OK);
        }else if(!versionDate.matches(Params.DATE_PATTERN)){
            errors.add(new ErrorResponseDTO(ErrorCode.DATE_FORMAT, ErrorDescription.DATE_FORMAT));
            return new ResponseEntity<>(errors, HttpStatus.OK);
        } else if(StringUtils.isEmpty(versionDate)){
            errors.add(new ErrorResponseDTO(ErrorCode.VERSION, ErrorDescription.VERSION));
            return new ResponseEntity<>(errors, HttpStatus.OK);
        }else if(StringUtils.isEmpty(generation)){
            errors.add(new ErrorResponseDTO(ErrorCode.GENERATION, ErrorDescription.GENERATION));
            return new ResponseEntity<>(errors, HttpStatus.OK);
        }
        return ibmService.getInstanceDetails(versionDate, generation);
    }

    @GetMapping(value = URLMapping.GET_CPU_AND_MEMORY_USAGE)
    public ResponseEntity<Object> getCpuAndMemoryUsage(@RequestParam("serverId") Long serverId, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate){
        logger.info("IbmCloudController :: getCpuAndMemoryUsage ::PROCESSING METHOD");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        if((!startDate.matches(Params.DATE_PATTERN)) || (!endDate.matches(Params.DATE_PATTERN))) {
            errors.add(new ErrorResponseDTO(ErrorCode.DATE_FORMAT, ErrorDescription.DATE_FORMAT));
            return new ResponseEntity<>(errors, HttpStatus.OK);
        }
        return ibmService.getCpuAndMemoryUsage(serverId, startDate, endDate);
    }

    @GetMapping(value=URLMapping.GET_PRODUCT_PACKAGE)
    public ResponseEntity<Object> getProductPackage(){
        logger.info("IbmCloudController :: getProductPackage ::PROCESSING METHOD");
        return ibmService.getProductPackage();
    }

    @GetMapping(value = URLMapping.GET_CUSTOMER_USER_DETAILS)
    public ResponseEntity<Object> getCustomerUser(){
        logger.info("IbmCloudController :: getCustomerUser ::PROCESSING METHOD");
        return ibmService.getCustomerUserDetails();
    }

    @GetMapping(value = URLMapping.GET_BANDWIDTH_MONITORING)
    public ResponseEntity<Object> getBandwidthMonitoring(@RequestParam("serverId") Long serverId, @RequestParam("networkType") String networkType, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate){
        logger.info("IbmCloudController :: getBandwidthMonitoring ::PROCESSING METHOD");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        if((!startDate.matches(Params.DATE_PATTERN)) || (!endDate.matches(Params.DATE_PATTERN))) {
            errors.add(new ErrorResponseDTO(ErrorCode.DATE_FORMAT, ErrorDescription.DATE_FORMAT));
            return new ResponseEntity<>(errors, HttpStatus.OK);
        }else if((!networkType.equalsIgnoreCase(Params.PUBLIC)) && (!networkType.equalsIgnoreCase(Params.PRIVATE))){
            errors.add(new ErrorResponseDTO(ErrorCode.NETWORK_TYPE_VALIDATION, ErrorDescription.NETWORK_TYPE_VALIDATION));
            return new ResponseEntity<>(errors, HttpStatus.OK);

        }
        return ibmService.getBandwidthMonitoring(serverId, networkType, startDate, endDate);
    }

    @GetMapping(value = URLMapping.GET_ITEM_PRICES)
    public ResponseEntity<Object> getItemPriceDetails(@RequestParam("packageId") Long packageId){
        logger.info("IbmCloudController :: getItemPriceDetails ::PROCESSING METHOD");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        if(Objects.isNull(packageId)){
            errors.add(new ErrorResponseDTO(ErrorCode.MISSING_PACKAGE_ID, ErrorDescription.MISSING_PACKAGE_ID));
            return new ResponseEntity<>(errors, HttpStatus.OK);
        }
        return ibmService.getItemPriceDetails(packageId);
    }

    @GetMapping(value = URLMapping.GET_SOFTWARE_DETAILS)
    public ResponseEntity<Object> getSoftwareDetails(@RequestParam("user") String user, @RequestParam("host") String host, @RequestParam("password") String password){
        logger.info("IbmCloudController :: getSoftwareDetails ::PROCESSING METHOD");
        List<ErrorResponseDTO> errors = new ArrayList<>();
        if(StringUtils.isEmpty(user) || Objects.isNull(user)){
            errors.add(new ErrorResponseDTO(ErrorCode.MISSING_USER, ErrorDescription.MISSING_USER));
            return new ResponseEntity<>(errors, HttpStatus.OK);
        }else if(StringUtils.isEmpty(host) || Objects.isNull(host)){
            errors.add(new ErrorResponseDTO(ErrorCode.MISSING_HOST, ErrorDescription.MISSING_HOST));
            return new ResponseEntity<>(errors, HttpStatus.OK);
        }else if(StringUtils.isEmpty(password) || Objects.isNull(password) ){
            errors.add(new ErrorResponseDTO(ErrorCode.MISSING_PASSWORD, ErrorDescription.MISSING_PASSWORD));
            return new ResponseEntity<>(errors, HttpStatus.OK);
        }
        return ibmService.getSoftwareDetails(user, host, password);
    }
}
