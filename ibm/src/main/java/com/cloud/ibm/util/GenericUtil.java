package com.cloud.ibm.util;

import com.cloud.ibm.dto.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class GenericUtil {

    private GenericUtil() {
        throw new IllegalStateException("GenericUtil is a Utility class");
    }

    public static UserDetails getLoggedInUser() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static GregorianCalendar getGregorianDate(String date) throws ParseException {

        ZoneId defaultZoneId = ZoneId.systemDefault();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        LocalDate localDate = LocalDate.parse(date, formatter);
        Date date1 = Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
        GregorianCalendar dateValue = new GregorianCalendar();
        dateValue.setTime(date1);
        return dateValue;
    }
}
