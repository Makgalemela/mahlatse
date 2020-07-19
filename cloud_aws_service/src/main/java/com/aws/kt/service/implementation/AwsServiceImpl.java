package com.aws.kt.service.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.budgets.AWSBudgets;
import com.amazonaws.services.budgets.AWSBudgetsClientBuilder;
import com.amazonaws.services.budgets.model.Budget;
import com.amazonaws.services.budgets.model.DescribeBudgetsRequest;
import com.amazonaws.services.budgets.model.DescribeBudgetsResult;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.costexplorer.AWSCostExplorer;
import com.amazonaws.services.costexplorer.AWSCostExplorerClient;
import com.amazonaws.services.costexplorer.AWSCostExplorerClientBuilder;
import com.amazonaws.services.costexplorer.model.DateInterval;
import com.amazonaws.services.costexplorer.model.Expression;
import com.amazonaws.services.costexplorer.model.GetCostAndUsageRequest;
import com.amazonaws.services.costexplorer.model.GetCostAndUsageResult;
import com.amazonaws.services.costexplorer.model.Granularity;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.AttachedPolicy;
import com.amazonaws.services.identitymanagement.model.GetAccountSummaryRequest;
import com.amazonaws.services.identitymanagement.model.GetAccountSummaryResult;
import com.amazonaws.services.identitymanagement.model.GetUserRequest;
import com.amazonaws.services.identitymanagement.model.GetUserResult;
import com.amazonaws.services.identitymanagement.model.Group;
import com.amazonaws.services.identitymanagement.model.ListAttachedUserPoliciesRequest;
import com.amazonaws.services.identitymanagement.model.ListAttachedUserPoliciesResult;
import com.amazonaws.services.identitymanagement.model.ListGroupsForUserRequest;
import com.amazonaws.services.identitymanagement.model.ListGroupsForUserResult;
import com.amazonaws.services.identitymanagement.model.ListUsersRequest;
import com.amazonaws.services.identitymanagement.model.ListUsersResult;
import com.amazonaws.services.identitymanagement.model.User;
import com.amazonaws.services.pricing.AWSPricing;
import com.amazonaws.services.pricing.AWSPricingClientBuilder;
import com.amazonaws.services.pricing.model.Filter;
import com.amazonaws.services.pricing.model.GetProductsRequest;
import com.amazonaws.services.pricing.model.GetProductsResult;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.aws.kt.domain.AwsSoftwareDetail;
import com.aws.kt.feignClient.GatewayServiceFeignController;
import com.aws.kt.service.AwsService;
import com.aws.kt.service.FileStorageService;
import com.aws.kt.util.GenericUtils;
import com.aws.kt.util.ResponseHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

@Service
public class AwsServiceImpl implements AwsService {

	@Value("${file.path}")
	private String filePath;

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private GatewayServiceFeignController gatewayFeign;

	@Autowired
	private ObjectMapper objectMapper;

	private static final Logger LOGGER = LoggerFactory.getLogger(AwsServiceImpl.class);
	private static final String offerTermCode = ".JRTCKXETXF";
	private static final String rateCode = ".6YS6EN2CT7";

	private JSONObject awsPriceCatalog(AWSCredentials credentials, String regionName) throws IOException {
		AWSPricing awsPricing = AWSPricingClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.fromName(regionName))
				.build();
		Filter filter1 = new Filter();
		filter1.setType("TERM_MATCH");
		filter1.setField("location");
		filter1.setValue(Regions.fromName(regionName).getDescription());
		Filter[] filters = { filter1 };
		GetProductsRequest request = new GetProductsRequest().withFilters(filters).withServiceCode("AmazonEC2");
		GetProductsResult result = awsPricing.getProducts(request);
		JSONObject object = new JSONObject();
		JSONArray arrays = new JSONArray();
		for (String price : result.getPriceList()) {
			JSONObject array = new JSONObject(price);
			arrays.put(array);
		}
		object.put("Format Version", result.getFormatVersion());
		object.put("products", arrays);
		return object;
	}

	private JSONArray fetchCloudWatch(AWSCredentials credentials, String regionName) {
		JSONArray array = new JSONArray();
		final AmazonCloudWatch cw = AmazonCloudWatchClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.fromName(regionName))
				.build();
		final long twoWeeks = 1000 * 60 * 60;
        final int twelveHours = 60 * 60 ;
		GetMetricStatisticsRequest getMetricStatisticsRequest = new GetMetricStatisticsRequest()
				.withStartTime(new Date(new Date().getTime() - twoWeeks)).withNamespace("AWS/Billing")
				.withPeriod(twelveHours).withDimensions(new Dimension().withName("Currency").withValue("USD"))
				.withMetricName("EstimatedCharges").withStatistics("Average", "Maximum", "Sum").withEndTime(new Date());
		GetMetricStatisticsResult result = cw.getMetricStatistics(getMetricStatisticsRequest);
		JSONObject object = new JSONObject();
		for (Datapoint data : result.getDatapoints()) {
			object.put("MAXIMUM", "$" + data.getMaximum());
			object.put("AVERAGE", "$" + data.getAverage());
			object.put("SUM", "$" + data.getSum());
			object.put("TIMESTAMP", data.getTimestamp());
			array.put(object);
			break;
		}
		return array;
	}

	private JSONArray fetchInstanceDetail(AWSCredentials credentials, String regionName, AmazonEC2 ec2,
			DescribeInstancesResult response) {
		boolean done = false;
		JSONArray array = new JSONArray();
		AWSPricing awsPricing = AWSPricingClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.US_EAST_1).build();
		while (!done) {
			for (Reservation reservation : response.getReservations()) {
				List<Instance> instance = reservation.getInstances().stream()
						.filter((x) -> x.getState().getName().equals("running")).collect(Collectors.toList());
				if (!instance.isEmpty()) {
					for (Instance instances : instance) {
						array.put(fetchValue(ec2, instances, credentials, regionName, awsPricing));
					}
				}
			}

			if (response.getNextToken() == null) {
				done = true;
			}
		}
		return array;
	}

	private JSONObject fetchValue(AmazonEC2 ec2, Instance instances, AWSCredentials credentials, String regionName,
			AWSPricing awsPricing) {
		Filter filter = new Filter();
		filter.setType("TERM_MATCH");
		filter.setField("instanceType");
		filter.setValue(instances.getInstanceType());
		Filter filter1 = new Filter();
		filter1.setType("TERM_MATCH");
		filter1.setField("location");
		filter1.setValue(Regions.fromName(regionName).getDescription());
		Filter[] filters = { filter, filter1 };
		GetProductsRequest request = new GetProductsRequest().withFilters(filters).withServiceCode("AmazonEC2");
		GetProductsResult result = awsPricing.getProducts(request);
		return getJonObject(result, instances);
	}

	private JSONArray fetchUserDetails(AWSCredentials credentials, String regionName) {
		boolean done = false;
		JSONArray Object = new JSONArray();
		ListUsersRequest request = new ListUsersRequest();
		final AmazonIdentityManagement iam = AmazonIdentityManagementClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.fromName(regionName))
				.build();
		while (!done) {
			ListUsersResult response = iam.listUsers(request);
			for (User user : response.getUsers()) {
				List<Object> addPolicyList = new ArrayList<>();
				List<Object> groupList = new ArrayList<>();
				JSONObject jsonObject1 = new JSONObject();
				GetUserResult userDetail = iam.getUser(new GetUserRequest().withUserName(user.getUserName()));
				jsonObject1.put("CREATED DATE", userDetail.getUser().getCreateDate());
				jsonObject1.put("USER NAME", user.getUserName());
				jsonObject1.put("USER ID", user.getUserId());
				jsonObject1.put("USER ARN", user.getArn());
				ListAttachedUserPoliciesResult result = iam.listAttachedUserPolicies(
						new ListAttachedUserPoliciesRequest().withUserName(user.getUserName()));
				for (AttachedPolicy policy : result.getAttachedPolicies()) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("POLICY NAME", policy.getPolicyName());
					map.put("POLICY ARN ", policy.getPolicyArn());
					addPolicyList.add(map);
				}
				ListGroupsForUserResult groupDetail = iam
						.listGroupsForUser(new ListGroupsForUserRequest().withUserName(user.getUserName()));
				for (Group group : groupDetail.getGroups()) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("GROUP NAME ", group.getGroupName());
					map.put("GROUP ARN ", group.getArn());
					map.put("GROUP ID ", group.getGroupId());
					map.put("GROUP PATH ", group.getPath());
					groupList.add(map);
				}
				jsonObject1.put("POLICIES", addPolicyList);
				jsonObject1.put("GROUPS", groupList);
				Object.put(jsonObject1);
			}
			request.setMarker(response.getMarker());
			if (!response.getIsTruncated()) {
				done = true;
			}
		}
		return Object;
	}

	private JSONObject fetchVolumes(AmazonEC2 ec2, Instance instance) {
		JSONObject Object = new JSONObject();
		for (InstanceBlockDeviceMapping instances : instance.getBlockDeviceMappings()) {
			DescribeVolumesResult describeVolumesResult = ec2
					.describeVolumes(new DescribeVolumesRequest().withVolumeIds(instances.getEbs().getVolumeId()));
			for (Volume volumes : describeVolumesResult.getVolumes()) {
				JSONObject jsonObject1 = new JSONObject();
				jsonObject1.put("size", volumes.getSize());
				jsonObject1.put("state", volumes.getState());
				Object.put(volumes.getVolumeId(), jsonObject1);
			}
		}
		return Object;
	}

	private JSONObject getJonObject(GetProductsResult result, Instance instances) {
		JSONObject object1 = new JSONObject(result.getPriceList().get(0)).getJSONObject("product");
		JSONObject priceJsonObject = new JSONObject(result.getPriceList().get(0)).getJSONObject("terms")
				.getJSONObject("OnDemand").getJSONObject(object1.getString("sku") + offerTermCode)
				.getJSONObject("priceDimensions").getJSONObject(object1.getString("sku") + offerTermCode + rateCode)
				.getJSONObject("pricePerUnit");
		JSONObject object = new JSONObject();
		JSONObject cpuObject = new JSONObject();
		for (Tag tag : instances.getTags()) {
			object.put("INSTANCE NAME", tag.getValue());
		}
		object.put("INSTANCE TYPE", instances.getInstanceType());
		object.put("INSTANCE STATE", instances.getState().getName());
		object.put("PRIVATE IP", instances.getPrivateIpAddress());
		object.put("PUBLIC IP", instances.getPublicIpAddress());
		cpuObject.put("CORECOUNT", instances.getCpuOptions().getCoreCount());
		cpuObject.put("THREADPERCORE", instances.getCpuOptions().getThreadsPerCore());
		object.put("CPU", cpuObject);
		object.put("LAUNCH TIME", instances.getLaunchTime());
		object.put("INSTANCE MEMORY", object1.getJSONObject("attributes").getString("memory"));
		object.put("OPERATING SYSTEM", object1.getJSONObject("attributes").getString("operatingSystem"));
		object.put("INSTANCE VALUE", priceJsonObject.getBigDecimal("USD").setScale(4, RoundingMode.HALF_DOWN));
		object.put("INSTANCE MONTHLY",
				priceJsonObject.getBigDecimal("USD").multiply(new BigDecimal(30)).setScale(4, RoundingMode.HALF_DOWN));
		return object;
	}

	@Override
	public ResponseEntity<Object> getInstanceDetail(String regionName) throws IOException {
		boolean done = false;
		Map<String, String> userDetail = getUserCredentials();
		if (userDetail.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
		}
		AmazonEC2 ec2 = getConnection(userDetail.get("awsAccessKey"), userDetail.get("awsSecretKey"), regionName);
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		JSONArray array = new JSONArray();
		JSONObject root = new JSONObject();
		while (!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);
			for (Reservation reservation : response.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					JSONObject jsonObject1 = new JSONObject();
					for (Tag tag : instance.getTags()) {
						jsonObject1.put("INSTANCE NAME", tag.getValue());
					}
					jsonObject1.put("INSTANCE ID", instance.getInstanceId());
					jsonObject1.put("AMI IMAGE", instance.getImageId());
					jsonObject1.put("INSTANCE TYPE", instance.getInstanceType());
					jsonObject1.put("INSTANCE STATUS", instance.getState().getName());
					jsonObject1.put("MONITORING", instance.getMonitoring().getState());
					jsonObject1.put("PULIC IP ADDRESS", instance.getPublicIpAddress());
					jsonObject1.put("PRIVATE IP ADDRESS", instance.getPrivateIpAddress());
					JSONObject volume = fetchVolumes(ec2, instance);
					jsonObject1.put("VOLUMES", volume);
					JSONArray fetchCloudWatchDetail = fetchCloudWatchDetah(userDetail.get("awsAccessKey"),
							userDetail.get("awsSecretKey"), regionName, instance.getInstanceId());
					jsonObject1.put("CLOUD WATCH", fetchCloudWatchDetail);
					array.put(jsonObject1);
				}
			}
			request.setNextToken(response.getNextToken());
			if (response.getNextToken() == null) {
				done = true;
			}
		}
		root.put("Instance", array);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "Ec2 Instance Detail",
				JSONValue.toJSONString(root));
	}

	private JSONArray fetchCloudWatchDetah(String awsAccessKey, String awsSecretKey, String regionName,
			String instanceId) {
		JSONArray array = new JSONArray();
		AmazonCloudWatch cw = AmazonCloudWatchClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(getCredentials(awsAccessKey, awsSecretKey)))
				.withRegion(Regions.fromName(regionName)).build();
		long offsetInMilliseconds = 1000 * 60 * 60;
		GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
				.withStartTime(new Date(new Date().getTime() - offsetInMilliseconds)).withNamespace("AWS/EC2")
				.withPeriod(60 * 60).withDimensions(new Dimension().withName("InstanceId").withValue(instanceId))
				.withMetricName("NetworkOut").withStatistics("Average", "Sum", "Maximum").withEndTime(new Date());
		GetMetricStatisticsResult getMetricStatisticsResult = cw.getMetricStatistics(request);

		for (Datapoint point : getMetricStatisticsResult.getDatapoints()) {
			JSONObject object = new JSONObject();
			object.put("MATRIC NAME", getMetricStatisticsResult.getLabel());
			object.put("DESCIPTION", "network out for the instance");
			object.put("INSTANCE ID", instanceId);
			object.put("TIMESTAMP", point.getTimestamp());
			object.put("AVERAGE", point.getAverage());
			object.put("MAXIMUM", point.getMaximum());
			object.put("SUM", point.getSum());
			object.put("UNIT", point.getUnit());
			array.put(object);
		}
		request = new GetMetricStatisticsRequest().withStartTime(new Date(new Date().getTime() - offsetInMilliseconds))
				.withNamespace("AWS/EC2").withPeriod(60 * 60)
				.withDimensions(new Dimension().withName("InstanceId").withValue(instanceId))
				.withMetricName("NetworkIn").withStatistics("Average", "Sum", "Maximum").withEndTime(new Date());
		GetMetricStatisticsResult networkIn = cw.getMetricStatistics(request);
		for (Datapoint point : networkIn.getDatapoints()) {
			JSONObject object = new JSONObject();
			object.put("MATRIC NAME", networkIn.getLabel());
			object.put("INSTANCE ID", instanceId);
			object.put("DESCIPTION", "network in for the instance");
			object.put("TIMESTAMP", point.getTimestamp());
			object.put("AVERAGE", point.getAverage());
			object.put("MAXIMUM", point.getMaximum());
			object.put("SUM", point.getSum());
			object.put("UNIT", point.getUnit());
			array.put(object);
		}
		request = new GetMetricStatisticsRequest().withStartTime(new Date(new Date().getTime() - offsetInMilliseconds))
				.withNamespace("AWS/EC2").withPeriod(60 * 60)
				.withDimensions(new Dimension().withName("InstanceId").withValue(instanceId))
				.withMetricName("CPUUtilization").withStatistics("Average", "Sum", "Maximum").withEndTime(new Date());
		GetMetricStatisticsResult CPUUtilization = cw.getMetricStatistics(request);
		for (Datapoint point : CPUUtilization.getDatapoints()) {
			JSONObject object = new JSONObject();
			object.put("MATRIC NAME", CPUUtilization.getLabel());
			object.put("INSTANCE ID", instanceId);
			object.put("DESCIPTION", "CPUUtilization for the instance");
			object.put("TIMESTAMP", point.getTimestamp());
			object.put("AVERAGE", point.getAverage());
			object.put("MAXIMUM", point.getMaximum());
			object.put("SUM", point.getSum());
			object.put("UNIT", point.getUnit());
			array.put(object);
		}
		return array;
	}

	private Map<String, String> getUserCredentials() {
		Long userId = Long.valueOf(GenericUtils.getLoggedInUser().getUserId());
		ResponseEntity<Object> getUserDetail = gatewayFeign.getUserDetail(userId);
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

	private AmazonEC2 getConnection(String awsAccessKey, String awsSecretKey, String regionName) {
		AWSCredentials credentials = getCredentials(awsAccessKey, awsSecretKey);
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.fromName(regionName))
				.build();
		return ec2;
	}

	private AWSCredentials getCredentials(String awsAccessKey, String awsSecretKey) {
		return new BasicAWSCredentials(awsAccessKey, awsSecretKey);
	}

	@Override
	public ResponseEntity<Object> getDatabaseDetail(String regionName) throws IOException {
		JSONObject object = new JSONObject();
		Map<String, String> userDetail = getUserCredentials();
		if (userDetail.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
		}
		JSONArray rdsDetailFetch = fetchRdDetail(userDetail.get("awsAccessKey"), userDetail.get("awsSecretKey"),
				regionName);
		object.put("DATABASE", rdsDetailFetch);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "Ec2 Database Detail",
				JSONValue.toJSONString(object));
	}

	private JSONArray fetchRdDetail(String awsAccessKey, String awsSecretKey, String regionName) {
		JSONArray array = new JSONArray();
		AmazonRDS client = AmazonRDSClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(getCredentials(awsAccessKey, awsSecretKey)))
				.withRegion(Regions.fromName(regionName)).build();
		DescribeDBInstancesRequest request = new DescribeDBInstancesRequest();
		DescribeDBInstancesResult response = client.describeDBInstances(request);
		for (DBInstance instance : response.getDBInstances()) {
			JSONObject object = new JSONObject();
			object.put("MASTER DB NAME", instance.getMasterUsername());
			object.put("DATABASE NAME", instance.getDBName());
			object.put("ADDRESS", instance.getEndpoint().getAddress());
			object.put("ENGINE", instance.getEngine());
			object.put("ENGINE VERSION", instance.getEngineVersion());
			object.put("INSTANCE CLASS", instance.getDBInstanceClass());
			object.put("PORT", instance.getEndpoint().getPort());
			object.put("ALLOCATE STORAGE", instance.getAllocatedStorage() + " GB");
			object.put("AVAILABALITY ZONE", instance.getAvailabilityZone());
			object.put("LICENCE MODEL", instance.getLicenseModel());
			array.put(object);
		}
		return array;
	}

	@Override
	public ResponseEntity<Object> getAccountSummary(String regionName) throws IOException {
		JSONObject root = new JSONObject();
		Map<String, String> userDetail = getUserCredentials();
		if (userDetail.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
		}
		JSONArray array = new JSONArray();
		final AmazonIdentityManagement iam = AmazonIdentityManagementClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(
						getCredentials(userDetail.get("awsAccessKey"), userDetail.get("awsSecretKey"))))
				.withRegion(Regions.fromName(regionName)).build();
		GetAccountSummaryResult summary = iam.getAccountSummary(new GetAccountSummaryRequest());
		JSONObject accountSummary = new JSONObject();
		for (Entry<String, Integer> map : summary.getSummaryMap().entrySet()) {
			accountSummary.put(map.getKey(), map.getValue());
		}
		array.put(accountSummary);
		root.put("ACCOUNT DETAIL", array);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "aws account summary",
				JSONValue.toJSONString(root));
	}

	@Override
	public ResponseEntity<Object> getSoftwareDetail(MultipartFile file, AwsSoftwareDetail awsSoftwareDetail)
			throws Exception {
		JSONObject root = new JSONObject();
		Map<String, String> userDetail = getUserCredentials();
		if (userDetail.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
		}
		JSONArray softwareInstallList = fetchSoftwareInstallList(file, awsSoftwareDetail);
		root.put("SOFTWARE", softwareInstallList);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "aws software install",
				JSONValue.toJSONString(root));
	}

	private JSONArray fetchSoftwareInstallList(MultipartFile file, AwsSoftwareDetail awsSoftwareDetail)
			throws Exception {
		String command = "ps axco command";
		JSONArray array = new JSONArray();
		StringBuffer buffer = getCommandDetail(file, awsSoftwareDetail, command);
		String[] split = buffer.toString().split("\n");
		for (int i = 0; i < split.length; i++) {
			array.put(split[i]);
		}
		return array;
	}

	private StringBuffer getCommandDetail(MultipartFile file, AwsSoftwareDetail awsSoftwareDetail, String command)
			throws Exception {
		String fileName = fileStorageService.storeFile(file);
		JSch jsch = new JSch();
		jsch.addIdentity(fileName);
		jsch.setConfig("StrictHostKeyChecking", "no");
		Session session = jsch.getSession(awsSoftwareDetail.getUserName(), awsSoftwareDetail.getUserIp(), 22);
		session.connect();
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(command);
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
		return buffer;
	}

	@Override
	public ResponseEntity<Object> getUserDetail(String regionName) throws IOException {
		JSONObject root = new JSONObject();
		Map<String, String> userDetail = getUserCredentials();
		if (userDetail.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
		}
		JSONArray userProfile = fetchUserDetails(
				getCredentials(userDetail.get("awsAccessKey"), userDetail.get("awsSecretKey")), regionName);
		root.put("USER DETAIL", userProfile);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "aws user detail", JSONValue.toJSONString(root));
	}

	@Override
	public ResponseEntity<Object> getAwsBilling(String regionName) throws IOException {
		JSONObject root = new JSONObject();
		Map<String, String> userDetail = getUserCredentials();
		if (userDetail.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
		}
		JSONArray cloudWatch = fetchCloudWatch(
				getCredentials(userDetail.get("awsAccessKey"), userDetail.get("awsSecretKey")), regionName);
		root.put("AWS BILLING", cloudWatch);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "aws billing detail",
				JSONValue.toJSONString(root));
	}

	@Override
	public ResponseEntity<Object> getRunningInstances(String regionName) throws IOException {
		boolean done = false;
		JSONObject root = new JSONObject();
		JSONArray instanceDetail = new JSONArray();
		Map<String, String> userDetail = getUserCredentials();
		if (userDetail.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
		}
		AmazonEC2 ec2 = getConnection(userDetail.get("awsAccessKey"), userDetail.get("awsSecretKey"), regionName);
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		while (!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);
			instanceDetail = fetchInstanceDetail(
					getCredentials(userDetail.get("awsAccessKey"), userDetail.get("awsSecretKey")), regionName, ec2,
					response);
			request.setNextToken(response.getNextToken());
			if (response.getNextToken() == null) {
				done = true;
			}
		}
		root.put("RUNNING INSTANCES", instanceDetail);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "aws running instances",
				JSONValue.toJSONString(root));
	}

	@Override
	public ResponseEntity<Object> getPriceCatalog(String regionName) throws IOException {
		Map<String, String> userDetail = getUserCredentials();
		if (userDetail.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
		}
		JSONObject object = awsPriceCatalog(
				getCredentials(userDetail.get("awsAccessKey"), userDetail.get("awsSecretKey")), regionName);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "price catalog", JSONValue.toJSONString(object));
	}

	@Override
	public ResponseEntity<Object> getBudget(String regionName) throws IOException {
		boolean done = false;
		JSONArray array = new JSONArray();
		JSONObject root = new JSONObject();
		Map<String, String> userDetail = getUserCredentials();
		if (userDetail.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
		}
		final AmazonIdentityManagement iam = AmazonIdentityManagementClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(
						getCredentials(userDetail.get("awsAccessKey"), userDetail.get("awsSecretKey"))))
				.withRegion(Regions.fromName(regionName)).build();
		GetUserResult user = iam.getUser();
		String accountId = user.getUser().getArn().split(":")[4];
		AWSBudgets client = AWSBudgetsClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(
						getCredentials(userDetail.get("awsAccessKey"), userDetail.get("awsSecretKey"))))
				.withRegion(Regions.fromName(regionName)).build();
		DescribeBudgetsRequest request = new DescribeBudgetsRequest().withAccountId(accountId);
		while (!done) {
			DescribeBudgetsResult result = client.describeBudgets(request);
			if (result.getBudgets().isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, true, "aws budget not found", null);
			for (Budget budget : result.getBudgets()) {
				JSONObject object = fetchJSOnOBject(budget);
				array.put(object);
			}
			request.setNextToken(result.getNextToken());
			if (result.getNextToken() == null) {
				done = true;
			}
		}
		root.put("BUDGET", array);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "aws budget", JSONValue.toJSONString(root));
	}

	private JSONObject fetchJSOnOBject(Budget budget) {
		JSONObject object = new JSONObject();
		object.put("BUDGET NAME", budget.getBudgetName());
		object.put("BUDGET TYPE", budget.getBudgetType());
		JSONObject calculatedSpend = new JSONObject();
		JSONObject actualSpend = new JSONObject();
		JSONObject forecastSpend = new JSONObject();
		actualSpend.put("AMOUNT", budget.getCalculatedSpend().getActualSpend().getAmount());
		actualSpend.put("CURRENCY", budget.getCalculatedSpend().getActualSpend().getUnit());
		calculatedSpend.put("ACTUAL SPEND", actualSpend);
		forecastSpend.put("AMOUNT", budget.getCalculatedSpend().getForecastedSpend().getAmount());
		forecastSpend.put("AMOUNT", budget.getCalculatedSpend().getForecastedSpend().getUnit());
		calculatedSpend.put("FORECAST SPEND", forecastSpend);
		object.put("CALCULATED SPEND", calculatedSpend);
		JSONObject budgetLimit = new JSONObject();
		budgetLimit.put("AMOUNT", budget.getBudgetLimit().getAmount());
		budgetLimit.put("CURRENCY", budget.getBudgetLimit().getUnit());
		object.put("BUDGET LIMIT", budgetLimit);
		JSONObject costFilter = new JSONObject();
		for (Entry<String, List<String>> map : budget.getCostFilters().entrySet()) {
			costFilter.put(map.getKey(), map.getValue());
		}
		object.put("COST FILTER", costFilter);
		JSONObject costType = new JSONObject();
		costType.put("INCLUDE CREDIT", budget.getCostTypes().getIncludeCredit());
		costType.put("INCLUDE DISCOUNT", budget.getCostTypes().getIncludeDiscount());
		costType.put("INCLUDE OTHER SUBSCRIPTION", budget.getCostTypes().getIncludeOtherSubscription());
		costType.put("INCLUDE RECURRING", budget.getCostTypes().getIncludeRecurring());
		costType.put("INCLUDE REFUND", budget.getCostTypes().getIncludeRefund());
		costType.put("INCLUDE TAX", budget.getCostTypes().getIncludeTax());
		costType.put("INCLUDE SUPPORT", budget.getCostTypes().getIncludeSupport());
		costType.put("INCLUDE UPFRONT", budget.getCostTypes().getIncludeUpfront());
		costType.put("USE BLENDED", budget.getCostTypes().getUseBlended());
		costType.put("USE AMORTIZED", budget.getCostTypes().getUseAmortized());
		object.put("COST TYPES", costType);
		JSONObject timePeriod = new JSONObject();
		timePeriod.put("START DATE", budget.getTimePeriod().getStart());
		timePeriod.put("END DATE", budget.getTimePeriod().getEnd());
		object.put("TIME UNIT", budget.getTimeUnit());
		return object;
	}

	@Override
	public ResponseEntity<Object> getAwsCostForecast(String regionName) {
		Map<String, String> userDetail = getUserCredentials();
		if (userDetail.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, "user credentials not found", null);
		}
		AWSCostExplorer client = AWSCostExplorerClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(
						getCredentials(userDetail.get("awsAccessKey"), userDetail.get("awsSecretKey"))))
				.withRegion(Regions.fromName(regionName)).build();
		DateInterval interval = new DateInterval();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		cal.add(Calendar.DATE, new Date().getDate());
		String pattern = "yyyy-MM-dd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String startDate = simpleDateFormat.format(cal.getTime());
		String endDate = simpleDateFormat.format(new Date());
		interval.setStart(startDate);
		interval.setEnd(endDate);
		ArrayList<String> getmetrics = new ArrayList<>();
		getmetrics.add("BlendedCost");
		getmetrics.add("AmortizedCost");
		getmetrics.add("NetAmortizedCost");
		getmetrics.add("NetUnblendedCost");
		getmetrics.add("NormalizedUsageAmount");
		getmetrics.add("UnblendedCost");
		getmetrics.add("UsageQuantity");
		GetCostAndUsageResult response = client.getCostAndUsage(new GetCostAndUsageRequest()
				.withGranularity(Granularity.MONTHLY).withTimePeriod(interval).withMetrics(getmetrics));
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "aws cost forecast", response.getResultsByTime());
	}
}
