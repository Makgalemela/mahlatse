package com.cloud.ibm.service;

import com.cloud.ibm.constant.Params;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.softlayer.api.ApiClient;
import com.softlayer.api.RestApiClient;
import com.softlayer.api.service.Location;
import com.softlayer.api.service.location.Region;
import com.softlayer.api.service.product.Item;
import com.softlayer.api.service.product.Package;
import com.softlayer.api.service.product.item.Category;
import com.softlayer.api.service.product.item.Price;
import com.softlayer.api.service.product.pkg.Attribute;
import com.softlayer.api.service.product.pkg.Locations;
import com.softlayer.api.service.product.pkg.Preset;
import com.softlayer.api.service.product.pkg.Type;
import com.softlayer.api.service.product.pkg.order.Configuration;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductPackage {


    ApiClient client;
    Package.Service service;

    ProductPackage(){

    }
    public ProductPackage(String user, String apiKey) {
        client = (new RestApiClient()).withCredentials(user, apiKey);
        service = Package.service(client);
    }

    /**
     * method constructor sent the package Id and get the package items
     * @param user user name credential.
     * @param apiKey api key credential.
     * @param packageId package identifier.
     */
    public ProductPackage(String user, String apiKey, long packageId) {
        client = (new RestApiClient()).withCredentials(user, apiKey);
        service = Package.service(client, packageId);
    }

    public List<Preset> getAccountRestrictedActivePresets() {
        return service.getAccountRestrictedActivePresets();
    }

    public List<Category> getAccountRestrictedCategories() {
        return service.getAccountRestrictedCategories();
    }

    public boolean getAccountRestrictedPricesFlag() {
        return service.getAccountRestrictedPricesFlag();
    }

    public List<Item> getActiveItems() {
        return service.getActiveItems();
    }

    public List<Preset> getActivePreset() {
        return service.getActivePresets();
    }

    public List<Item> getActiveRamItems() {
        return service.getActiveRamItems();
    }

    public List<Item> getActiveServerItems() {
        return service.getActiveServerItems();
    }

    public List<Item> getActiveSoftwaerItems() {
        return service.getActiveServerItems();
    }

    public List<Price> getActiveUsageItems() {
        return service.getActiveUsagePrices();
    }

    public Boolean getAdditionalServiceFlag() {
        return service.getAdditionalServiceFlag();
    }

    public List<Attribute> getAttributes() {
        return service.getAttributes();
    }

    public List<Locations> getAvaibleLocation() {
        return service.getAvailableLocations();
    }

    public Long getAvaibleStorageUnits() {
        return service.getAvailableStorageUnits();
    }

    public List<Category> getCategories() {
        return service.getCategories();
    }

    public List<Item> getCdnItems() {
        return service.getCdnItems();
    }

    public List<Configuration> getConfiguration() {
        return service.getConfiguration();
    }

    public String getDefaultBootCategoryCode() {
        return service.getDefaultBootCategoryCode();
    }

    public List<Item> getDefaultRamItems() {
        return service.getDefaultRamItems();
    }

    public String getDeploymentNodeType() {
        return service.getDeploymentNodeType();
    }

    public String getDeploymentType() {
        return service.getDeploymentNodeType();
    }

    public Boolean getDeisallowCustomDiskPartitions() {
        return service.getDisallowCustomDiskPartitions();
    }

    public List<Package> getAllObjects() {
        return service.getAllObjects();
    }

    public List<Price> getItemsPrices() {
        return service.getItemPrices();
    }

    public Package getObject() {
        return service.getObject();
    }

    public List<Location> getLocations() {
        return service.getLocations();
    }

    public List<Region> getRegions() {
        return service.getRegions();
    }

    public Type getType() {
        return service.getType();
    }

    public String getProductPackageDetails(String user, String apiKey){

        String namePackage = Params.PACKAGE_METAL_SERVER;

        Long packageId;
        ProductPackage productPackage = new ProductPackage(user, apiKey);
        for (Package pack : productPackage.getAllObjects()) {
            if (pack.getKeyName().equals(namePackage)) {
                packageId = pack.getId();
                productPackage= new ProductPackage(user,apiKey,packageId);
                break;
            }
        }
        List<Price> allItems = productPackage.getItemsPrices();
        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();

        String response = gson.toJson(allItems);

        return response;
    }
}
