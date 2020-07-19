package com.cloud.azure.dto;

public class CostManagementFilter {

	private String type;
	private String timeFrame;
	private String granularity;
	private String totalCostName;
	private String totalCostFunction;
	private String groupingType;
	private String groupingName;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTimeFrame() {
		return timeFrame;
	}

	public void setTimeFrame(String timeFrame) {
		this.timeFrame = timeFrame;
	}

	public String getTotalCostName() {
		return totalCostName;
	}

	public void setTotalCostName(String totalCostName) {
		this.totalCostName = totalCostName;
	}

	public String getTotalCostFunction() {
		return totalCostFunction;
	}

	public void setTotalCostFunction(String totalCostFunction) {
		this.totalCostFunction = totalCostFunction;
	}

	public String getGroupingType() {
		return groupingType;
	}

	public void setGroupingType(String groupingType) {
		this.groupingType = groupingType;
	}

	public String getGroupingName() {
		return groupingName;
	}

	public void setGroupingName(String groupingName) {
		this.groupingName = groupingName;
	}

	public String getGranularity() {
		return granularity;
	}

	public void setGranularity(String granularity) {
		this.granularity = granularity;
	}

}
