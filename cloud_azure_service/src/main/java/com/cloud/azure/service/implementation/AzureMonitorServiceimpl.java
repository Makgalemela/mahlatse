package com.cloud.azure.service.implementation;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.cloud.azure.service.AzureCredentialService;
import com.cloud.azure.service.AzureMonitorService;
import com.cloud.azure.util.ResponseHandler;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.monitor.Metric;
import com.microsoft.azure.management.monitor.MetricCollection;
import com.microsoft.azure.management.monitor.MetricDefinition;
import com.microsoft.azure.management.monitor.MetricValue;
import com.microsoft.azure.management.monitor.TimeSeriesElement;
import com.microsoft.azure.management.network.NetworkInterface;


@Service
public class AzureMonitorServiceimpl implements AzureMonitorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AzureMonitorServiceimpl.class);

	@Autowired
	private AzureCredentialService azureCredentialService;
	
	@Override
	public ResponseEntity<Object> getNetworkMonitoringDetail() throws CloudException, IOException {
		JSONObject networkMonitorData = new JSONObject();
		Azure azure = azureCredentialService.getAzure();
		DateTime recordDateTime = DateTime.now();
		List<NetworkInterface> networkInterface = azure.networkInterfaces().list().stream()
				.collect(Collectors.toList());
		JSONArray networkArray = new JSONArray();
		for (NetworkInterface network : networkInterface) {
			JSONObject object = new JSONObject();
			object.put("networkId", network.id());
			object.put("networkType", network.type());
			object.put("networkRegionName", network.regionName());
			object.put("networkResourceGroupName", network.resourceGroupName());
			List<MetricDefinition> metricDefincation = azure.metricDefinitions().listByResource(network.id()).stream()
					.filter((x) -> x.name().localizedValue().equalsIgnoreCase("Bytes Sent")
							|| x.name().localizedValue().equalsIgnoreCase("Bytes Received"))
					.collect(Collectors.toList());
			JSONArray array = new JSONArray();
			for (MetricDefinition metrics : metricDefincation) {
				JSONObject metricsObject = new JSONObject();
					MetricCollection metricCollection = metrics.defineQuery().startingFrom(recordDateTime.minusHours(2))
							.endsBefore(recordDateTime).withAggregation("Average").withInterval(Period.hours(1))
							.execute();
					metricsObject.put("metricsName", metrics.name().localizedValue());
					metricsObject.put("metricsId", metrics.id());
					metricsObject.put("unit", metrics.unit());
					metricsObject.put("nameSpaces", metricCollection.namespace());
					metricsObject.put("queryTime", metricCollection.timespan());
					metricsObject.put("timeGrain", metricCollection.interval());
					metricsObject.put("cost", metricCollection.cost());
					JSONArray metricArray = new JSONArray();
					for (Metric metric : metricCollection.metrics()) {
						JSONObject metricObject = new JSONObject();
						metricObject.put("metricName", metric.name().localizedValue());
						metricObject.put("type", metric.type());
						metricObject.put("unit", metric.unit());
						JSONArray timeSeriesArray = new JSONArray();
						for (TimeSeriesElement timeElement : metric.timeseries()) {
							JSONObject timeElementObject = new JSONObject();
							JSONArray dataArray = new JSONArray();
							for (MetricValue data : timeElement.data()) {
								JSONObject dataObject = new JSONObject();
								dataObject.put("timeStamp", data.timeStamp());
								dataObject.put("minimum", data.minimum());
								dataObject.put("maximum", data.maximum());
								dataObject.put("average", data.average());
								dataObject.put("total", data.total());
								dataObject.put("count", data.count());
								dataArray.put(dataObject);
							}
							timeElementObject.put("data", dataArray);
							timeSeriesArray.put(timeElementObject);
						}
						metricsObject.put("matric", timeSeriesArray);
						metricArray.put(metricObject);
					}
				array.put(metricsObject);
			}
			object.put("metricDetail", array);
			networkArray.put(object);
		}
		networkMonitorData.put("networkMonitoring", networkArray);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "network monitor detail",
				networkMonitorData.toString());
	}

	@Override
	public ResponseEntity<Object> getMonitoringDetail() throws CloudException, IOException {
		JSONObject virtualMonitorData = new JSONObject();
		Azure azure = azureCredentialService.getAzure();
		DateTime recordDateTime = DateTime.now();
		List<VirtualMachine> virtualMachine = azure.virtualMachines().list().stream().collect(Collectors.toList());
		JSONArray virtualArray = new JSONArray();
		for (VirtualMachine virtual : virtualMachine) {
			JSONObject object = new JSONObject();
			object.put("virtualMachineId", virtual.id());
			object.put("virtualMachineType", virtual.type());
			object.put("virtualMachineRegionName", virtual.regionName());
			object.put("virtualMachineResourceGroupName", virtual.resourceGroupName());
			List<MetricDefinition> metricDefincation = azure.metricDefinitions().listByResource(virtual.id()).stream()
					.filter((x) -> x.name().localizedValue().equalsIgnoreCase("Percentage CPU")
							|| x.name().localizedValue().equalsIgnoreCase("Network In Billable (Deprecated)")
							|| x.name().localizedValue().equalsIgnoreCase("Network Out Billable (Deprecated)"))
					.collect(Collectors.toList());
			JSONArray array = new JSONArray();
			for (MetricDefinition metrics : metricDefincation) {
				JSONObject metricsObject = new JSONObject();
					MetricCollection metricCollection = metrics.defineQuery().startingFrom(recordDateTime.minusHours(2))
							.endsBefore(recordDateTime).withAggregation("Average").withInterval(Period.hours(1))
							.execute();
					metricsObject.put("metricsName", metrics.name().localizedValue());
					metricsObject.put("metricsId", metrics.id());
					metricsObject.put("unit", metrics.unit());
					metricsObject.put("nameSpaces", metricCollection.namespace());
					metricsObject.put("queryTime", metricCollection.timespan());
					metricsObject.put("timeGrain", metricCollection.interval());
					metricsObject.put("cost", metricCollection.cost());
					JSONArray metricArray = new JSONArray();
					for (Metric metric : metricCollection.metrics()) {
						JSONObject metricObject = new JSONObject();
						metricObject.put("metricName", metric.name().localizedValue().toString());
						metricObject.put("type", metric.type());
						metricObject.put("unit", metric.unit());
						JSONArray timeSeriesArray = new JSONArray();
						JSONObject timeElementObject = new JSONObject();
						for (TimeSeriesElement timeElement : metric.timeseries()) {
							JSONArray dataArray = new JSONArray();
							for (MetricValue data : timeElement.data()) {
								JSONObject dataObject = new JSONObject();
								dataObject.put("timeStamp", data.timeStamp());
								dataObject.put("average", data.average());
								dataArray.put(dataObject);
							}
							timeElementObject.put("data", dataArray);
							timeSeriesArray.put(timeElementObject);
						}
						metricsObject.put("matric", timeSeriesArray);
						metricArray.put(metricObject);
					}
				array.put(metricsObject);
			}
			object.put("metricDetail", array);
			virtualArray.put(object);
		}
		virtualMonitorData.put("virtualMachineMonitoring", virtualArray);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "virtual monitor detail",
				virtualMonitorData.toString());
	}

}
