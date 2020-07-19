package com.cloud.azure.service.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import com.cloud.azure.dto.CostManagementFilter;
import com.cloud.azure.dto.RateCardFilterDto;
import com.cloud.azure.service.AzureCredentialService;
import com.cloud.azure.service.AzureService;
import com.cloud.azure.service.FileStorageService;
import com.cloud.azure.util.ResponseHandler;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.VaultCertificate;
import com.microsoft.azure.management.compute.VaultSecretGroup;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineUnmanagedDataDisk;
import com.microsoft.azure.management.compute.implementation.SshPublicKeyInner;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlDatabaseUsageMetric;
import com.microsoft.azure.management.sql.SqlServer;

import io.micrometer.core.instrument.util.StringUtils;

@Service
public class AzureServiceImplementation implements AzureService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceImplementation.class);

	@Value("${azure.management.resource}")
	private String azureManagementResource;

	@Value("${azure.graph.resource}")
	private String azureGraphResource;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private AzureCredentialService azureCredentialService;
	
	@Override
	public ResponseEntity<Object> getAllResources() throws CloudException, IOException {
		JSONObject root = new JSONObject();
		JSONArray virtualMachineArray = new JSONArray();
		Azure azure = azureCredentialService.getAzure();
		List<VirtualMachine> virtualMachineList = azure.virtualMachines().list().stream().collect(Collectors.toList());
		if (virtualMachineList.isEmpty()) {
			return null;
		}
		for (VirtualMachine machine : virtualMachineList) {
			JSONObject virtualMachineDetail = new JSONObject();
			virtualMachineDetail.put("availabilitySetId", machine.availabilitySetId());
			virtualMachineDetail.put("vmId", machine.vmId());
			virtualMachineDetail.put("primaryPublicIPAddress", machine.getPrimaryPublicIPAddress().ipAddress());
			virtualMachineDetail.put("type", machine.type());
			virtualMachineDetail.put("primaryPrivateIp", machine.getPrimaryNetworkInterface().primaryPrivateIP());
			JSONObject object = new JSONObject();
			object.put("vmSize", machine.size().toString());
			virtualMachineDetail.put("hardwareProfile", object);
			JSONArray unamanagedDisk = new JSONArray();
			for (Entry<Integer, VirtualMachineUnmanagedDataDisk> unmanaged : machine.unmanagedDataDisks().entrySet()) {
				JSONObject unmanagedDataDisk = new JSONObject();
				unmanagedDataDisk.put("cachingType", unmanaged.getValue().cachingType());
				unmanagedDataDisk.put("vhdUri", unmanaged.getValue().vhdUri());
				unmanagedDataDisk.put("sourceImageUri", unmanaged.getValue().sourceImageUri());
				unmanagedDataDisk.put("size", unmanaged.getValue().size());
				unmanagedDataDisk.put("name", unmanaged.getValue().name());
				unmanagedDataDisk.put("diskSizeGB", unmanaged.getValue().inner().diskSizeGB() + " GB");
				unmanagedDataDisk.put("imageUri", unmanaged.getValue().inner().image().uri());
				unamanagedDisk.put(unmanagedDataDisk);
			}
			virtualMachineDetail.put("unmanagedDisk", unamanagedDisk);
			JSONObject storageProfileObject = new JSONObject();
			JSONObject imageReference = new JSONObject();
			imageReference.put("sku", machine.storageProfile().imageReference().sku());
			imageReference.put("version", machine.storageProfile().imageReference().version());
			imageReference.put("offer", machine.storageProfile().imageReference().offer());
			imageReference.put("id", machine.storageProfile().imageReference().id());
			imageReference.put("publisher", machine.storageProfile().imageReference().publisher());
			storageProfileObject.put("imageReference", imageReference);
			JSONObject onDiskObject = new JSONObject();
			onDiskObject.put("osType", machine.storageProfile().osDisk().osType());
			onDiskObject.put("name", machine.storageProfile().osDisk().name());
			onDiskObject.put("cachinType", machine.storageProfile().osDisk().caching());
			onDiskObject.put("createOption", machine.storageProfile().osDisk().createOption().toString());
			onDiskObject.put("diskSizeGB", machine.storageProfile().osDisk().diskSizeGB());
			JSONObject onDiskManagedDisk = new JSONObject();
			onDiskManagedDisk.put("storageAccountType",
					machine.storageProfile().osDisk().managedDisk().storageAccountType().toString());
			onDiskManagedDisk.put("id", machine.storageProfile().osDisk().managedDisk().id().toString());
			onDiskObject.put("managedDisk", onDiskManagedDisk);
			storageProfileObject.put("onDisk", onDiskObject);
			JSONArray dataDiskArray = new JSONArray();
			for (DataDisk dataDisk : machine.storageProfile().dataDisks()) {
				JSONObject dataDiskObject = new JSONObject();
				dataDiskObject.put("caching", dataDisk.caching().toString());
				dataDiskObject.put("createOption", dataDisk.createOption().toString());
				dataDiskObject.put("diskSizeGB", dataDisk.diskSizeGB());
				dataDiskObject.put("lun", dataDisk.lun());
				dataDiskObject.put("name", dataDisk.name().toString());
				JSONObject managedDisk = new JSONObject();
				managedDisk.put("storageAccountType",
						machine.storageProfile().osDisk().managedDisk().storageAccountType().toString());
				managedDisk.put("id", machine.storageProfile().osDisk().managedDisk().id().toString());
				dataDiskObject.put("managedDisk", managedDisk);
				dataDiskArray.put(dataDiskObject);
			}
			storageProfileObject.put("dataDisk", dataDiskArray);
			virtualMachineDetail.put("storageProfile", storageProfileObject);
			JSONObject osProfileObject = new JSONObject();
			osProfileObject.put("adminUserName", machine.osProfile().adminUsername());
			osProfileObject.put("computerName", machine.osProfile().computerName());
			osProfileObject.put("customeData", machine.osProfile().customData());
			JSONObject linuxConfigurationObject = new JSONObject();
			linuxConfigurationObject.put("disablePasswordAuthentication",
					machine.osProfile().linuxConfiguration().disablePasswordAuthentication());
			JSONArray sshPublicKeyArray = new JSONArray();
			for (SshPublicKeyInner sshKey : machine.osProfile().linuxConfiguration().ssh().publicKeys()) {
				JSONObject sshPublicKeyObject = new JSONObject();
				sshPublicKeyObject.put("path", sshKey.path().toString());
				sshPublicKeyObject.put("keyData", sshKey.keyData().toString());
				sshPublicKeyArray.put(sshPublicKeyObject);
			}
			linuxConfigurationObject.put("ssh", sshPublicKeyArray);
			osProfileObject.put("linuxConfiguration", linuxConfigurationObject);
			JSONArray secretArray = new JSONArray();
			JSONArray certificateArray = new JSONArray();
			for (VaultSecretGroup secretGroup : machine.osProfile().secrets()) {
				JSONObject secretObject = new JSONObject();
				secretObject.put("sourceVault", secretGroup.sourceVault());
				for (VaultCertificate certificate : secretGroup.vaultCertificates()) {
					JSONObject certificateObject = new JSONObject();
					certificateObject.put("certificateStore", certificate.certificateStore());
					certificateObject.put("certificateUrl", certificate.certificateUrl());
					certificateArray.put(certificateObject);
				}
				secretObject.put("certificate", certificateArray);
				secretArray.put(secretObject);
			}
			osProfileObject.put("secret", secretArray);
			virtualMachineDetail.put("osProfile", osProfileObject);
			JSONArray array = new JSONArray();
			JSONObject networkProfile = new JSONObject();
			for (String value : machine.networkInterfaceIds()) {
				JSONObject networkId = new JSONObject();
				networkId.put("ids", value);
				array.put(networkId);
			}
			networkProfile.put("networkInterfaces", array);
			virtualMachineDetail.put("networkProfile", networkProfile);
			JSONObject diagnosticsProfile = new JSONObject();
			JSONObject bootDiagnostics = new JSONObject();
			bootDiagnostics.put("enabled", machine.diagnosticsProfile().bootDiagnostics().enabled());
			bootDiagnostics.put("storageUri", machine.diagnosticsProfile().bootDiagnostics().storageUri());
			diagnosticsProfile.put("bootDiagnostics", bootDiagnostics);
			virtualMachineDetail.put("diagnosticsProfile", diagnosticsProfile);
			virtualMachineDetail.put("provisioningState", machine.provisioningState());
			virtualMachineArray.put(virtualMachineDetail);
		}
		root.put("virtualMachineDetail", virtualMachineArray);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "azure virtual machine data ", root.toString());
	}

	@Override
	public ResponseEntity<Object> getBudgetDetail() throws MalformedURLException, IOException {
		String authToken = azureCredentialService.azureAuthToken(azureManagementResource);	
		Azure azure = azureCredentialService.getAzure();
		String virtualMachineListUrl = "https://management.azure.com/subscriptions/" + azure.subscriptionId()
				+ "/providers/Microsoft.Consumption/budgets?api-version=2019-10-01";
		HttpHeaders http = new HttpHeaders();
		http.setContentType(MediaType.APPLICATION_JSON);
		http.set("Authorization", authToken);
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(virtualMachineListUrl);
		final HttpEntity<Object> requestEntity = new HttpEntity<>(http);
		try {
			ResponseEntity<String> output = restTemplate.exchange(builder.build().toUri(), HttpMethod.GET,
					requestEntity, String.class);
			JSONObject json = new JSONObject(output.getBody());
			if (output.getStatusCodeValue() == HttpStatus.OK.value()) {
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "azure budget detail", output.getBody());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, false,
					"something wrong please check again", null);
		}
		return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "try again later", null);
	}

	@Override
	public ResponseEntity<Object> getSqlDatabasesDetail() throws CloudException, IOException {
		JSONObject object = new JSONObject();
		JSONArray array = new JSONArray();
		Azure azure = azureCredentialService.getAzure();
		List<SqlServer> sqlServerList = azure.sqlServers().list().stream().collect(Collectors.toList());
		if (sqlServerList.isEmpty()) {
			return null;
		}
		JSONArray sqlArray = new JSONArray();
		for (SqlServer server : sqlServerList) {
			JSONObject sqlObject = new JSONObject();
			sqlObject.put("adminLogin", server.administratorLogin());
			sqlObject.put("serverId", server.id());
			sqlObject.put("serverName", server.name());
			sqlObject.put("type", server.type());
			sqlObject.put("serverRegioName", server.regionName());
			sqlObject.put("serverResourceGroupName", server.resourceGroupName());
			for (SqlDatabase database : server.databases().list()) {
				JSONObject databaseObject = new JSONObject();
				JSONArray matricArray = new JSONArray();
				databaseObject.put("databaseName", database.name());
				databaseObject.put("collation", database.collation());
				databaseObject.put("creationDate", database.creationDate());
				databaseObject.put("databaseId", database.databaseId());
				databaseObject.put("defaultSecondayLocation", database.defaultSecondaryLocation());
				databaseObject.put("id", database.id());
				databaseObject.put("status", database.status());
				databaseObject.put("region", database.regionName());
				databaseObject.put("maxSizeByte", database.maxSizeBytes());
				databaseObject.put("requestedServiceObjectiveName",
						database.requestedServiceObjectiveName().toString());
				List<SqlDatabaseUsageMetric> value = database.listUsageMetrics().stream()
						.filter((x) -> x.resourceName().equalsIgnoreCase(database.name())).collect(Collectors.toList());
				for (SqlDatabaseUsageMetric matric : value) {
					JSONObject matricData = new JSONObject();
					matricData.put("matricCurrentValue", matric.currentValue());
					matricData.put("matricDisplayName", matric.displayName());
					matricData.put("matricLimit", matric.limit());
					matricData.put("matricUnit", matric.unit());
					matricData.put("matricNextResetTime", matric.nextResetTime());
					matricData.put("matricResourceName", matric.resourceName());
					matricArray.put(matricData);
				}
				databaseObject.put("matricData", matricArray);
				sqlArray.put(databaseObject);
			}
			JSONArray tagArray = new JSONArray();
			for (Entry<String, String> map : server.tags().entrySet()) {
				JSONObject tags = new JSONObject();
				tags.put("key", map.getKey());
				tags.put("key", map.getValue());
				tagArray.put(tags);
			}
			sqlObject.put("tags", tagArray);
			sqlObject.put("databse", sqlArray);
			sqlObject.put("version", server.version());
			array.put(sqlObject);
		}
		object.put("databaseDetail", array);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "azure sql server detail", object.toString());
	}
	
	@Override
	public ResponseEntity<Object> getLocationDetail() throws CloudException, IOException {
		String authToken = azureCredentialService.azureAuthToken(azureManagementResource);
		Azure azure = azureCredentialService.getAzure();
		String url = "https://management.azure.com/subscriptions/" + azure.subscriptionId()
				+ "/locations?api-version=2020-01-01";
		HttpHeaders http = new HttpHeaders();
		http.setContentType(MediaType.APPLICATION_JSON);
		http.set("Authorization", authToken);
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
		final HttpEntity<Object> requestEntity = new HttpEntity<>(http);
		JSONObject json = null;
		try {
			ResponseEntity<String> output = restTemplate.exchange(builder.build().toUri(), HttpMethod.GET,
					requestEntity, String.class);
			json = new JSONObject(output.getBody());
			if (output.getStatusCodeValue() == 200) {
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "azure list location", json.toString());
			}
		} catch (Exception e) {
			return ResponseHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, false,
					"something wrong please check !", null);
		}
		return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "please try again later", null);
	}

	@Override
	public ResponseEntity<Object> getUserDetail() throws CloudException, IOException {
		String authToken = azureCredentialService.azureAuthToken(azureGraphResource);
		String url = "https://graph.microsoft.com/v1.0/users";
		HttpHeaders http = new HttpHeaders();
		http.setContentType(MediaType.APPLICATION_JSON);
		http.set("Authorization", "Bearer " + authToken);
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
		final HttpEntity<Object> requestEntity = new HttpEntity<>(http);
		JSONObject json = null;
		try {
			ResponseEntity<String> output = restTemplate.exchange(builder.build().toUri(), HttpMethod.GET,
					requestEntity, String.class);
			json = new JSONObject(output.getBody());
			if (output.getStatusCodeValue() == 200) {
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "user detail fetch", json.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, false,
					"something wrong please check !", null);
		}
		return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "please try again", null);
	}

	@Override
	public ResponseEntity<Object> getAzureRateCardDetail(RateCardFilterDto dto) throws IOException {
		String authToken = azureCredentialService.azureAuthToken(azureManagementResource);
		HttpHeaders http = new HttpHeaders();
		http.setContentType(MediaType.APPLICATION_JSON);
		http.set("Authorization", authToken);
		String url = "https://management.azure.com/subscriptions/8f2e59f7-a40e-4694-9eba-ebd6d25f1efa/providers/"
				+ "Microsoft.Commerce/RateCard?api-version=2015-06-01-preview&$filter=OfferDurableId eq" + "'"
				+ dto.getOfferDurableId() + "'" + "and Currency eq " + "'" + dto.getCurrency() + "'" + " and Locale eq "
				+ "'" + dto.getLocale() + "'" + " and RegionInfo eq " + "'" + dto.getRegionInfo() + "'";
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
		final HttpEntity<Object> requestEntity = new HttpEntity<>(http);
		JSONObject json = null;
		try {
			ResponseEntity<String> output = restTemplate.exchange(builder.build().toUri(), HttpMethod.GET,
					requestEntity, String.class);
			json = new JSONObject(output.getBody());
			if (output.getStatusCodeValue() == 200) {
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "user detail fetch", json.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, false,
					"something wrong please check !", null);
		}
		return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "please try again", null);
	}

	@Override
	public ResponseEntity<Object> getCostDetail(CostManagementFilter costManagementFilter) throws IOException {
		String authToken = azureCredentialService.azureAuthToken(azureManagementResource);
		Azure azure = azureCredentialService.getAzure();
		HttpHeaders http = new HttpHeaders();
		http.setContentType(MediaType.APPLICATION_JSON);
		http.set("Authorization", authToken);
		String url = "https://management.azure.com/subscriptions/" + azure.subscriptionId()
				+ "/providers/Microsoft.CostManagement/query?api-version=2019-11-01";
		JSONObject jsonBody = costJSONBody(costManagementFilter);
		LOGGER.info("result :: {} ", jsonBody);
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
		final HttpEntity<Object> requestEntity = new HttpEntity<>(jsonBody.toString(), http);
		JSONObject json = null;
		try {
			ResponseEntity<String> output = restTemplate.exchange(builder.build().toUri(), HttpMethod.POST,
					requestEntity, String.class);
			json = new JSONObject(output.getBody());
			LOGGER.info("result :::: {}", json);
			if (output.getStatusCodeValue() == 200) {
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "list of billing accounts",
						json.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, false,
					"something wrong please check !", null);
		}
		return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "please try again later", null);
	}

	@Override
	public ResponseEntity<Object> getSoftwareDetail(MultipartFile file) throws Exception {
		String fileName = fileStorageService.storeFile(file);
		JSONObject object = new JSONObject();
		JSch jsch = new JSch();
		jsch.addIdentity(fileName);
		jsch.setConfig("StrictHostKeyChecking", "no");
		Session session = jsch.getSession("AzureUser", "104.211.28.29", 22);
		session.connect();
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand("ps axco command");
		channel.setErrStream(System.err);
		channel.connect();
		InputStream input = channel.getInputStream();
		byte[] tmp = new byte[1024];
		StringBuffer buffer = new StringBuffer();
		while (true) {
			while (input.available() > 0) {
				int i = input.read(tmp, 0, 1024);
				if (i < 0)
					break;
				buffer.append(new String(tmp, 0, i));
			}
			if (channel.isClosed()) {
				break;
			}
			Thread.sleep(1000);
		}
		channel.disconnect();
		session.disconnect();
		JSONArray array = new JSONArray();
		String[] split = buffer.toString().split("\n");
		for (int i = 0; i < split.length; i++) {
			array.put(split[i]);
		}
		object.put("software", array);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "fetch software install list", object.toString());
	}


	private JSONObject costJSONBody(CostManagementFilter costManagementFilter) {
		JSONObject object = new JSONObject();
		object.put("type",
				StringUtils.isEmpty(costManagementFilter.getType()) ? "Usage" : costManagementFilter.getType());
		object.put("timeframe", StringUtils.isEmpty(costManagementFilter.getTimeFrame()) ? "TheLastMonth"
				: costManagementFilter.getTimeFrame());
		JSONObject dataSetObject = new JSONObject();
		dataSetObject.put("granularity", StringUtils.isEmpty(costManagementFilter.getGranularity()) ? "None"
				: costManagementFilter.getGranularity());
		JSONObject aggregationObject = new JSONObject();
		JSONObject totalCostObject = new JSONObject();
		totalCostObject.put("name", StringUtils.isEmpty(costManagementFilter.getTotalCostName()) ? "PreTaxCost"
				: costManagementFilter.getTotalCostName());
		totalCostObject.put("function", StringUtils.isEmpty(costManagementFilter.getTotalCostFunction()) ? "Sum"
				: costManagementFilter.getTotalCostFunction());
		aggregationObject.put("totalCost", totalCostObject);
		dataSetObject.put("aggregation", aggregationObject);
		JSONArray array = new JSONArray();
		JSONObject grouping = new JSONObject();
		grouping.put("type", "Dimension");
		grouping.put("name", "ResourceGroup");
		array.put(grouping);
		dataSetObject.put("grouping", array);
		object.put("dataSet", dataSetObject);
		return object;
	}

}
