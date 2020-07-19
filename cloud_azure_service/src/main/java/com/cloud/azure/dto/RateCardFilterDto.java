package com.cloud.azure.dto;

public class RateCardFilterDto {

	private String offerDurableId;
	private String currency;
	private String locale;
	private String regionInfo;

	public String getOfferDurableId() {
		return offerDurableId;
	}

	public void setOfferDurableId(String offerDurableId) {
		this.offerDurableId = offerDurableId;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getRegionInfo() {
		return regionInfo;
	}

	public void setRegionInfo(String regionInfo) {
		this.regionInfo = regionInfo;
	}

}
