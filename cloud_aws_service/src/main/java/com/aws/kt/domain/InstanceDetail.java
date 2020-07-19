package com.aws.kt.domain;

public class InstanceDetail {

	private String instanceId;
	private String imageId;
	private String instanceType;
	private String instanceName;
	private String monitoringState;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getMonitoringState() {
		return monitoringState;
	}

	public void setMonitoringState(String monitoringState) {
		this.monitoringState = monitoringState;
	}

	@Override
	public String toString() {
		return "InstanceDeyail " + 
	           "[instanceId=" + instanceId + "," +
			   "imageId=" + imageId + ", " + 
	           "instanceType="+ instanceType + ","
	         + "instanceName=" + instanceName + ","+
	           "monitoringState=" + monitoringState + 
	             "]";
	}

}
