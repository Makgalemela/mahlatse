package com.cloud.ibm.service;

import com.cloud.ibm.constant.Params;
import com.cloud.ibm.util.GenericUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softlayer.api.ApiClient;
import com.softlayer.api.RestApiClient;
import com.softlayer.api.service.container.bandwidth.GraphOutputs;
import com.softlayer.api.service.container.metric.data.Type;
import com.softlayer.api.service.metric.tracking.Object;
import com.softlayer.api.service.metric.tracking.object.Data;
import com.softlayer.api.service.virtual.Guest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CpuAndMemoryUsageService {

    private static final Logger logger = LoggerFactory.getLogger(CpuAndMemoryUsageService.class);

    @Autowired
    private ObjectMapper objectMapper;

    private Guest.Service guestService;
    private Object.Service trakingService;

    public  CpuAndMemoryUsageService(){

    }

    public CpuAndMemoryUsageService(Guest.Service guestService, Object.Service trakingService) {
        this.guestService = guestService;
        this.trakingService = trakingService;
    }

    public Map<String, String> getCpuAndMemoryDetails(String username, String apiKey, Long serverIdValue, String sDate, String eDate) throws ParseException, JsonProcessingException {
        //Long serverId = 105314598L;

        Long serverId = new Long(serverIdValue);
        JSONObject jsonObject = new JSONObject();

        String startDate = sDate.concat(Params.START_DATE);
        String endDate = eDate.concat(Params.END_DATE);

        String filePathCpu = "C:\\file\\cpufile.jpg";
        String filePathMemory = "C:\\file\\memoryfile.jpg";

        // Get Api Client.
        ApiClient client = new RestApiClient().withCredentials(username, apiKey);
        Guest.Service guestService = Guest.service(client, serverId);

        Long trakingId = guestService.getMetricTrackingObjectId();
        Object.Service trakingService = Object.service(client, trakingId);

        CpuAndMemoryUsageService usage = new CpuAndMemoryUsageService(guestService, trakingService);
        List<Data> cpuRecord = usage.getCpuUsage(startDate, endDate);
        Map<String, Double> cpuAverage = usage.calculateAverages(cpuRecord);
        List<Data> memoryRecord = usage.getMemoryUsage(startDate, endDate);
        Map<String, Double> memoryAverage = usage.calculateAverages(memoryRecord);

        // print records and cpu usage
        if(Objects.nonNull(cpuRecord)) {
            int count = 0;
            for (Data record : cpuRecord) {
                SimpleDateFormat formattedDate = new SimpleDateFormat("dd-MMM-yyyy");
                String dateFormatted = formattedDate.format(record.getDateTime().getTime());
                JSONObject object = new JSONObject();
                object.put(Params.DATE, dateFormatted);
                object.put(Params.COUNT, record.getCounter());
                object.put(Params.TYPE, record.getType());
                jsonObject.put("Cpu" + count, object);
                count++;
                /*System.out.println(record.getType() + " - " + dateFormatted + " - " + record.getCounter());*/
            }
        }

        for (Map.Entry<String, Double> average : cpuAverage.entrySet()) {
            jsonObject.put(average.getKey(), average.getValue());
            //System.out.println(average.getKey() + ": " + average.getValue() + "\n");
        }

        // cpu graph
        List<String> cpuTypes = new ArrayList<>();
        for (Map.Entry<String, Double> average : cpuAverage.entrySet()) {
            cpuTypes.add(average.getKey());
        }
        usage.getCpuGraph(startDate, endDate, filePathCpu, cpuTypes);

        if(Objects.nonNull(memoryRecord)) {
            int count1 = 0;
            for (Data record : memoryRecord) {
                SimpleDateFormat formattedDate = new SimpleDateFormat("dd-MMM-yyyy");
                String dateFormatted = formattedDate.format(record.getDateTime().getTime());
                JSONObject object1 = new JSONObject();
                object1.put(Params.DATE, dateFormatted);
                object1.put(Params.COUNT, record.getCounter());
                object1.put(Params.TYPE, record.getType());
                jsonObject.put("memory"+count1, object1);
                count1++;
                /*System.out.println(record.getType() + " - " + dateFormatted + " - " + record.getCounter());*/
            }
        }

        // there is only 1 memory and its value must be divided by 2^30 to convert it to GB
        String memoryUsage = memoryAverage.get(Params.MEMORY_USAGE) / Math.pow(2, 30)+" GB";
        jsonObject.put(Params.MEMORY_AVERAGE, memoryUsage);

        // memory graph
        usage.getMemoryGraph(startDate, endDate, filePathMemory);

        Map<String, String> mapUser = objectMapper.readValue(jsonObject.toString(), Map.class);

        return mapUser;
    }

    public Map<String, Double> calculateAverages(List<Data> records) {
        Map<String, Double> total = new HashMap<>();
        Map<String, Integer> totalCounter = new HashMap<>();
        for (Data item : records) {
            if (!total.containsKey(item.getType())) {
                total.put(item.getType(), item.getCounter().doubleValue());
                totalCounter.put(item.getType(), 1);
            } else {
                double itemCounterTotal = total.get(item.getType()) + item.getCounter().doubleValue();
                total.put(item.getType(), itemCounterTotal);
                totalCounter.put(item.getType(), totalCounter.get(item.getType()) + 1);
            }
        }

        Map<String, Double> average = new HashMap<>();
        for (Map.Entry<String, Double> entry : total.entrySet()) {
            double usageCounter = entry.getValue() / totalCounter.get(entry.getKey());
            average.put(entry.getKey(), usageCounter);
        }
        return average;
    }

    public List<Data> getCpuUsage(String startDate, String endDate) throws ParseException {

        GregorianCalendar start = GenericUtil.getGregorianDate(startDate);
        GregorianCalendar end = GenericUtil.getGregorianDate(endDate);

        Guest guestResponse = this.guestService.getObject();
        BigDecimal guestRespo = this.guestService.getAverageDailyPublicBandwidthUsage();
        /*System.out.println(guestRespo+" Average Bandwidth usage");*/
        logger.info("CpuAndMemoryUsageService :: getCpuUsage :: Average Bandwidth usage :: {} ", guestRespo);

        List<Type> types = new ArrayList<>();
        for (int i = 0; i < guestResponse.getStartCpus(); i++) {
            Type type = new Type();
            type.setKeyName("CPU" + String.valueOf(i));
            type.setName("cpu" + String.valueOf(i));
            type.setSummaryType("max");
            types.add(type);
        }

        return this.trakingService.getSummaryData(start, end, types, 3600L);
    }

    public List<Data> getMemoryUsage(String startDate, String endDate) throws ParseException {

        GregorianCalendar start = GenericUtil.getGregorianDate(startDate);
        GregorianCalendar end = GenericUtil.getGregorianDate(endDate);

        //build the SoftLayer_Container_Metric_Data_Type array
        List<Type> types = new ArrayList<>();

        Type type = new Type();
        type.setKeyName("MEMORY_USAGE");
        type.setSummaryType("max");
        type.setUnit("GB");
        types.add(type);

        return this.trakingService.getSummaryData(start, end, types, 3600L);
    }

    public void getCpuGraph(String startDate, String endDate, String filePath, List<String> cpuType) throws ParseException {

        GregorianCalendar start = GenericUtil.getGregorianDate(startDate);
        GregorianCalendar end = GenericUtil.getGregorianDate(endDate);

        GraphOutputs cpuGraph = this.trakingService.getGraph(start, end, cpuType);
        byte[] cpuImae = cpuGraph.getGraphImage();

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(cpuImae);
            BufferedImage image = ImageIO.read(inputStream);
            ImageIO.write(image, "jpg", new File(filePath));
            logger.info("CpuAndMemoryUsageService :: getCpuGraph :: CPU Graph Image created");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getMemoryGraph(String startDate, String endDate, String filePath) throws ParseException {

        GregorianCalendar start = GenericUtil.getGregorianDate(startDate);
        GregorianCalendar end = GenericUtil.getGregorianDate(endDate);

        GraphOutputs cpuGraph = this.guestService.getMemoryMetricImageByDate(start, end);
        byte[] cpuImage = cpuGraph.getGraphImage();

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(cpuImage);
            BufferedImage image = ImageIO.read(inputStream);
            ImageIO.write(image, "jpg", new File(filePath));
            logger.info("CpuAndMemoryUsageService :: getMemoryGraph :: Memory Graph Image created");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
