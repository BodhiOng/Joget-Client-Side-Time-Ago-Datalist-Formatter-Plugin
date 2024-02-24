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

            try { // If can parse, the input format is the same as the current dateFormat

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
        
        // Loop through different Time Formats to find which matches the input time format
        for (String timeFormat : timeFormats) {
                
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(timeFormat); // Take in the current timeFormat to be checked

            try { // If can parse, the input format is the same as the current timeFormat

                // Break the loop once found matching timeFormat
                LocalTime unformattedDate = LocalTime.parse(time.toUpperCase(), dtf);
                DateTimeFormatter finalTimeFormat = DateTimeFormatter.ofPattern("hh:mm a");
                formattedTime = unformattedDate.format(finalTimeFormat);
                break;

            } catch (DateTimeParseException e) {
                // Continue to check for other formats if
                // input format does not match current format
            }
        }
        return formattedTime;
    }
    
    // Check if input is a date or time
    public String checkDateOrTime(String input1, String input2 ) {
        
        String diff = "";
        
        if (!"".equals(checkDateFormat(input1)) && !"".equals(checkDateFormat(input2))) {
            
            // If parsing succeeded in checkDateFormat(), the input is a date

            String formattedColumnDate = checkDateFormat(input1); // Format date to yyyy-MM-dd format
            String formattedTargetDate = checkDateFormat(input2); // Format date to yyyy-MM-dd format

            // Parse
            LocalDate date1 = LocalDate.parse(formattedColumnDate); // First input date
            LocalDate date2 = LocalDate.parse(formattedTargetDate); // Second input date

            // Find difference between date1 and date2
            try {
                diff = getDateDiff(date1, date2);
            } catch (Exception e) {
               LogUtil.error("org.sample.TimeAgoDatalistFormatter", e, "Error!!");
            }
            
        } else if (!"".equals(checkTimeFormat(input1)) && !"".equals(checkTimeFormat(input2))) {
            
            // If parsing succeeded in checkTimeFormat(), the input is a time

            String formattedColumnTime = checkTimeFormat(input1); // Format date to hh:mm a format
            String formattedTargetTime = checkTimeFormat(input2); // Format date to hh:mm a format

            // Parse
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a");
            LocalTime time1 = LocalTime.parse(formattedColumnTime, dtf); // First input time
            LocalTime time2 = LocalTime.parse(formattedTargetTime, dtf); // Second input time

            // Find difference between time1 and time2
            try {
                diff = getTimeDiff(time1, time2);
            } catch (Exception e) {
               LogUtil.error("org.sample.TimeAgoDatalistFormatter", e, "Error!!");
            }
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
            
            // Obtain input Column Date
            String columnDateStr = result;
            String formattedColumnDate = checkDateFormat(columnDateStr); // Format date to yyyy-MM-dd format

            // Parse
            LocalDate columnDate = LocalDate.parse(formattedColumnDate);
            LocalDate currentDate = LocalDate.now(); // Current Date      

            // Duration From Column Date To Today
            try {
                return getDateDiff(columnDate, currentDate); // Get date difference
            } catch (Exception e) {
               LogUtil.error("org.sample.TimeAgoDatalistFormatter", e, "Error!!");
            }

        } else if (duration.equals("anotherDate")) {
            
            // Obtain input Column Date
            String columnStr = result;

            // Obtain input Target Date
            String targetStr = getPropertyString("targetDate");
            targetStr = (String) DataListService.evaluateColumnValueFromRow(row, targetStr);
            
            return checkDateOrTime(columnStr, targetStr); // Check if input is a date or time

        } else if (duration.equals("twoDates")) {
            
            // Obtain input From Date
            String fromStr = getPropertyString("fromDate");
            fromStr = (String) DataListService.evaluateColumnValueFromRow(row, fromStr);
            
            // Obtain input To Date
            String toStr = getPropertyString("toDate");
            toStr = (String) DataListService.evaluateColumnValueFromRow(row, toStr);
            
            return checkDateOrTime(fromStr, toStr); // Check if input is a date or time
        }
        return result;
    }
}