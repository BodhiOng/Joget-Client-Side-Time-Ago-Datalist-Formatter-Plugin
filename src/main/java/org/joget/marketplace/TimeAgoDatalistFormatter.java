package org.joget.marketplace;

import java.text.SimpleDateFormat;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormatDefault;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import org.joget.apps.datalist.service.DataListService;
import org.joget.commons.util.LogUtil;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeAgoDatalistFormatter extends DataListColumnFormatDefault {

    private final static String MESSAGE_PATH = "messages/TimeAgoDatalistFormatter";

    public String getName() {
        return "Time Ago Datalist Formatter";
    }

    public String getVersion() {
        return "7.0.0";
    }

    public String getDescription() {
        // support i18n
        return AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.pluginDesc", getClassName(), MESSAGE_PATH);
    }

    public String getYear() {
        // support i18n
        return " " + AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.year(s)", getClassName(), MESSAGE_PATH) + " ";
    }

    public String getMonth() {
        // support i18n
        return " " + AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.month(s)", getClassName(), MESSAGE_PATH) + " ";
    }

    public String getDay() {
        // support i18n
        return " " + AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.day(s)", getClassName(), MESSAGE_PATH) + " ";
    }
    
    public String getHour() {
        // support i18n
        return " " + AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.hour(s)", getClassName(), MESSAGE_PATH) + " ";
    }

    public String getMinute() {
        // support i18n
        return " " + AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.minute(s)", getClassName(), MESSAGE_PATH) + " ";
    }  
    
    public String getTryDefaultFormatErrorMsg() {
        return AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.tryDefaultFormatErrorMsg", getClassName(), MESSAGE_PATH);
    } 
    
    public String getLabel() {
        // support i18n
        return AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.pluginLabel", getClassName(), MESSAGE_PATH);
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/TimeAgoDatalistFormatter.json", null, true, MESSAGE_PATH);
    }

    // Check different Date Formats
    public String checkDateFormat(String date) {
        
        // Store different Date Formats
        String[] dateFormats = { "yyyy-MM-dd", "yyyy-MM-dd hh:mm a", "MMMMMMMMM dd, yyyy" };
        
        // Store formatted date
        String formattedDate = "";
                   
        // Loop through different Date Formats to find which matches the input date format
        for (String dateFormat : dateFormats) {

            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat); // Take in the current dateFormat to be checked
            sdf.setLenient(false); // Set a strict format checking
            SimpleDateFormat finalDateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Format date to yyyy-MM-dd format

            try { // If can parse, the input format is the same as the current 
                
                // Break the loop once found matching dateFormat
                Date unformattedDate = sdf.parse(date);
                formattedDate = finalDateFormat.format(unformattedDate);
                break;

            } catch (ParseException e) {
                // Continue to check for other formats if
                // input format does not match current format
            }
        }
        return formattedDate;
    }
    
    // Check different Time Formats
    public String checkTimeFormat(String time) {
        
        // Store different Time Formats
        String[] timeFormats = { "hh:mm a", "hh:mma", "h:mm a", "h:mma" };

        // Store formatted time
        String formattedTime = "";
        
        if (!"".equals(checkDateFormat(time))) {
            formattedTime = "";
            
        } else {

            // Loop through different Time Formats to find which matches the input time format
            for (String timeFormat : timeFormats) {

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(timeFormat); // Take in the current timeFormat to be checked

                try { // If can parse, the input format is the same as the current timeFormat

                    // Break the loop once found matching timeFormat
                    LocalTime unformattedTime = LocalTime.parse(time.toUpperCase(), dtf);
                    DateTimeFormatter finalTimeFormat = DateTimeFormatter.ofPattern("hh:mm a");
                    formattedTime = unformattedTime.format(finalTimeFormat);
                    break;

                } catch (DateTimeParseException e) {
                    // Continue to check for other formats if
                    // input format does not match current format
                }
            }
        } 
        return formattedTime;
    }
    
    // Split the time and reform into "hh:mm" format
    public String splitTime(String time) {
        
        // Split time into hours and minutes
        String[] hour_minute = time.split(":");
        String hour = hour_minute[0];

        if (hour_minute[1].toUpperCase().contains("AM") || hour_minute[1].toUpperCase().contains("PM")) {
            
            if (hour_minute[1].contains(" ")) {
                String[] minute_indicator = hour_minute[1].split(" ");
                String minute = minute_indicator[0];
                time = hour + ":" + minute;
                
            } else {
                String[] minute_indicator = hour_minute[1].split("[aApP]");
                String minute = minute_indicator[0];
                time = hour + ":" + minute;
            }
            
        }
        return time;
    }
    
    // Check Time Validity
    public String checkTimeValidity(String formattedTime) {
       
        String time = "";
        
        // Check validity
        if (!"".equals(checkDateFormat(formattedTime))) {
            time = "";
            
        } else {
            
            time = splitTime(formattedTime);
            
            // Conditions
            Boolean is12Hours = LocalTime.parse(time).getHour() >= 0 && LocalTime.parse(time).getHour() <= 12;
            Boolean is24Hours = LocalTime.parse(time).getHour() > 12 && LocalTime.parse(time).getHour() < 24;
            Boolean isWithin60Minutes = LocalTime.parse(time).getMinute() >= 0 && LocalTime.parse(time).getMinute() < 60;
            Boolean hasTimeIndicator = formattedTime.toUpperCase().contains("AM") || formattedTime.toUpperCase().contains("PM");
            
            if (is12Hours && hasTimeIndicator)  { // Valid

                if (isWithin60Minutes) { // Valid
                    time = "validTimeInputs"; // Valid time
                }

            } else if (is24Hours && !hasTimeIndicator) { // Valid

                if (isWithin60Minutes) { // Valid
                    time = "validTimeInputs"; // Valid time
                }
            }
        }
        return time;
    }
    
    // Check if input is a date or time
    public String checkDateOrTime(String input1, String input2) {
        
        String diff = "";
        
        // Conditions
        Boolean equalDateInputs = !"".equals(checkDateFormat(input1)) && !"".equals(checkDateFormat(input2));
        Boolean validTimeInputs = ("validTimeInputs".equals(checkTimeValidity(input1)) && "validTimeInputs".equals(checkTimeValidity(input2)));

        if (equalDateInputs) {
            
            // If parsing succeeded in checkDateFormat(), the input is a date
            String formattedColumnDate = checkDateFormat(input1); // Format date to yyyy-MM-dd format
            String formattedTargetDate = checkDateFormat(input2); // Format date to yyyy-MM-dd format

            // Parse
            LocalDate date1 = LocalDate.parse(formattedColumnDate); // First input date
            LocalDate date2 = LocalDate.parse(formattedTargetDate); // Second input date

            // Find difference between date1 and date2
            diff = getDateDiff(date1, date2);
            
        } else if (validTimeInputs) {
            
            // If parsing succeeded in checkTimeFormat(), the input is a time
            String formattedColumnTime = checkTimeFormat(input1); // Format date to hh:mm a format
            String formattedTargetTime = checkTimeFormat(input2); // Format date to hh:mm a format

            // Parse
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a");
            LocalTime time1 = LocalTime.parse(formattedColumnTime, dtf); // First input time
            LocalTime time2 = LocalTime.parse(formattedTargetTime, dtf); // Second input time

            // Find difference between time1 and time2
            diff = getTimeDiff(time1, time2);
            
        } else if (!validTimeInputs) {
            diff = "invalidTimeInputs";
            
        }
        return diff;
    }
    
    // Calculate difference between date1 and date2
    public String getDateDiff(LocalDate date1, LocalDate date2) {
        
        Period dateDiff = Period.between(date1, date2);
        String diff;
        
        if (Math.abs(dateDiff.getYears()) > 0) {
            diff = Math.abs(dateDiff.getYears()) + getYear() + Math.abs(dateDiff.getMonths()) +
                    getMonth() + Math.abs(dateDiff.getDays()) + getDay();
        } else if (Math.abs(dateDiff.getMonths()) > 0) {
            diff = Math.abs(dateDiff.getMonths()) + getMonth() + Math.abs(dateDiff.getDays()) + getDay();
        } else {
            diff = Math.abs(dateDiff.getDays()) + getDay();
        }
        return diff;
    }
    
    // Calculate difference between time1 and time2
    public String getTimeDiff(LocalTime time1, LocalTime time2) {
        
        Duration timeDiff = Duration.between(time1, time2);
        String diff;

        if (Math.abs(timeDiff.toHours()) > 0) {
            diff = Math.abs(timeDiff.toHours()) + getHour() + Math.abs(timeDiff.toMinutes() % 60) + getMinute();
        } else {
            diff = Math.abs(timeDiff.toMinutes() % 60) + getMinute();
        }
        return diff;
    }
    
    @Override
    public String format(DataList dataList, DataListColumn column, Object row, Object value) {

        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String result = (String) value;
        
        String duration = getPropertyString("duration");
        
        if (duration.equals("today")) {
            
            // Get input Column Date and Today Date
            String columnStr = result;
            String todayStr = LocalDate.now().toString();

            // Outputs
            if (!"invalidTimeInputs".equals(checkDateOrTime(columnStr, todayStr))) {
                return checkDateOrTime(columnStr, todayStr);

            } else if ("invalidTimeInputs".equals(checkDateOrTime(columnStr, todayStr))) {
                System.out.println(getTryDefaultFormatErrorMsg() + columnStr + ", " + todayStr);
                return columnStr;
            }

        } else if (duration.equals("anotherDate")) {
            
            // Get input Column Date
            String columnStr = result;

            // Get input Target Date
            String targetStr = getPropertyString("targetDate");
            targetStr = (String) DataListService.evaluateColumnValueFromRow(row, targetStr);
                
            // Outputs
            if (!"invalidTimeInputs".equals(checkDateOrTime(columnStr, targetStr))) {
                return checkDateOrTime(columnStr, targetStr);

            } else if ("invalidTimeInputs".equals(checkDateOrTime(columnStr, targetStr))) {
                System.out.println(getTryDefaultFormatErrorMsg() + columnStr + ", " + targetStr);
                return columnStr;
            }

        } else if (duration.equals("twoDates")) {
            
            // Get input From Date
            String fromStr = getPropertyString("fromDate");
            fromStr = (String) DataListService.evaluateColumnValueFromRow(row, fromStr);
            
            // Get input To Date
            String toStr = getPropertyString("toDate");
            toStr = (String) DataListService.evaluateColumnValueFromRow(row, toStr);
            
            // Outputs
            if (!"invalidTimeInputs".equals(checkDateOrTime(fromStr, toStr))) {
                return checkDateOrTime(fromStr, toStr);

            } else if ("invalidTimeInputs".equals(checkDateOrTime(fromStr, toStr))) {
                System.out.println(getTryDefaultFormatErrorMsg() + fromStr + ", " + toStr);
                return fromStr + "\n" + toStr;
            }
        }
        return result;
    }
}