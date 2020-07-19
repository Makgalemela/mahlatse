package com.cloud.ibm.service;

import org.springframework.http.ResponseEntity;

import javax.validation.Valid;

public interface IbmService {

    ResponseEntity<Object> testApiRegister();

    ResponseEntity<Object> getCatalogProduct(String locale, int page, int PageSize);

    ResponseEntity<Object> getVirtualGuest();

    ResponseEntity<Object> getDedicatedVirtualGuest();

    ResponseEntity<Object> getInvoiceDetails();

    ResponseEntity<Object> getAllObjectDetails();

    ResponseEntity<Object> getAccountDetails();

    ResponseEntity<Object> getDatabaseDetails();

    ResponseEntity<Object> getInstanceDetails(String version, String generation);

    ResponseEntity<Object> getCpuAndMemoryUsage(Long serverId, String startDate, String endDate);

    ResponseEntity<Object> getProductPackage();

    ResponseEntity<Object> getCustomerUserDetails();

    ResponseEntity<Object> getBandwidthMonitoring(Long serverId, String networkType, String startDate, String endDate);

    ResponseEntity<Object> getItemPriceDetails(Long packageId);

    ResponseEntity<Object> getSoftwareDetails(String user, String host, String password);











}
