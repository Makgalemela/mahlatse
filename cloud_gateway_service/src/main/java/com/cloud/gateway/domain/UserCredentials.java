package com.cloud.gateway.domain;

import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

@Entity
public class UserCredentials {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	private String awsAccessKey;
	private String awsSecretKey;
	private String azureAccessKey;
	private String azureSecretkey;
	private String azureTenantId;
	private String ibmUser;
	private String ibmUserApiKey;
	private String ibmClientId;
	private String ibmClientSecret;
	@ApiModelProperty(hidden = true)
	@CreationTimestamp
	private Date createdOn;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	@ApiModelProperty(hidden = true)
	private Date updatedOn = new Date();
	@JoinColumn
	@JsonIgnore
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	private User user;

	public String getIbmUser() {
		return ibmUser;
	}

	public void setIbmUser(String ibmUser) {
		this.ibmUser = ibmUser;
	}

	public String getIbmUserApiKey() {
		return ibmUserApiKey;
	}

	public void setIbmUserApiKey(String ibmUserApiKey) {
		this.ibmUserApiKey = ibmUserApiKey;
	}

	public String getIbmClientId() {
		return ibmClientId;
	}

	public void setIbmClientId(String ibmClientId) {
		this.ibmClientId = ibmClientId;
	}

	public String getIbmClientSecret() {
		return ibmClientSecret;
	}

	public void setIbmClientSecret(String ibmClientSecret) {
		this.ibmClientSecret = ibmClientSecret;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAwsAccessKey() {
		return awsAccessKey;
	}

	public void setAwsAccessKey(String awsAccessKey) {
		this.awsAccessKey = awsAccessKey;
	}

	public String getAwsSecretKey() {
		return awsSecretKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public String getAzureAccessKey() {
		return azureAccessKey;
	}

	public void setAzureAccessKey(String azureAccessKey) {
		this.azureAccessKey = azureAccessKey;
	}

	public String getAzureSecretkey() {
		return azureSecretkey;
	}

	public void setAzureSecretkey(String azureSecretkey) {
		this.azureSecretkey = azureSecretkey;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public Long getUser() {
		return user.getId();
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getAzureTenantId() {
		return azureTenantId;
	}

	public void setAzureTenantId(String azureTenantId) {
		this.azureTenantId = azureTenantId;
	}

}
