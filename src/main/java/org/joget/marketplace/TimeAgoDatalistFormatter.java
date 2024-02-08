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
    
    public static String checkDateFormats(String date) {

        // Store different Date Formats
        String[] dateFormats = { "yyyy-MM-dd", "yyyy-MM-dd hh:mm a", "MMMMMMMMM dd, yyyy" };
        
        // Store formatted date
        String formattedDate = "";
        
        // Loop through different Date Formats to find which matches the input date format
        for (String dateFormat : dateFormats) {

            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat); // Take in the current dateFormat
            sdf.setLenient(false); // Set a strict format checking
            SimpleDateFormat finalFormat = new SimpleDateFormat("yyyy-MM-dd"); // Format date to yyyy-MM-dd format

            try { // If can parse the input format is the same as the current format taken in from dateFormats

                Date unformattedDate = sdf.parse(date);
                formattedDate = finalFormat.format(unformattedDate);
                break; // Break the loop once input format is the same as the current dateFormat

            } catch (ParseException e) {
                // Continue to check for other formats if
                // input format does not match current format
            }

        }
        return formattedDate;

    }

    @Override
    public String format(DataList dataList, DataListColumn column, Object row, Object value) {

        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String result = (String) value;
        
        String duration = getPropertyString("duration");
        Period dateDiff;
        
        if (duration.equals("today")) {
            
            // Obtain input Column Date
            String columnDateStr = result;
            String formattedColumnDate = checkDateFormats(columnDateStr); // Format date to yyyy-MM-dd format

            // Parse
            LocalDate columnDate = LocalDate.parse(formattedColumnDate);
            LocalDate currentDate = LocalDate.now(); // Current Date      

            // Duration From Column Date To Today
            try {

                dateDiff = Period.between(columnDate, currentDate);

                if (Math.abs(dateDiff.getYears()) > 0) {
                    return Math.abs(dateDiff.getYears()) + getYear() + Math.abs(dateDiff.getMonths()) +
                            getMonth() + Math.abs(dateDiff.getDays()) + getDay();
                } else if (Math.abs(dateDiff.getMonths()) > 0) {
                    return Math.abs(dateDiff.getMonths()) + getMonth() + Math.abs(dateDiff.getDays()) + getDay();
                } else {
                    return Math.abs(dateDiff.getDays()) + getDay();
                }

            } catch (Exception e) {
               LogUtil.error("org.sample.TimeAgoDatalistFormatter", e, "Error!!");
            }

        } else if (duration.equals("anotherDate")) {
            
             // Obtain input Column Date
            String columnDateStr = result;
            String formattedColumnDate = checkDateFormats(columnDateStr); // Format date to yyyy-MM-dd format
            
            // Obtain input Target Date
            String targetDateStr = getPropertyString("targetDate");
            targetDateStr = (String) DataListService.evaluateColumnValueFromRow(row, targetDateStr);
            String formattedTargetDate = checkDateFormats(targetDateStr); // Format date to yyyy-MM-dd format
            
            // Parse
            LocalDate columnDate = LocalDate.parse(formattedColumnDate); // Column Date
            LocalDate anotherDate = LocalDate.parse(formattedTargetDate); // Target Date

            // From Column Date To Another Date
            try {

                dateDiff = Period.between(columnDate, anotherDate);

                if (Math.abs(dateDiff.getYears()) > 0) {
                    return Math.abs(dateDiff.getYears()) + getYear() + Math.abs(dateDiff.getMonths()) +
                            getMonth() + Math.abs(dateDiff.getDays()) + getDay();
                } else if (Math.abs(dateDiff.getMonths()) > 0) {
                    return Math.abs(dateDiff.getMonths()) + getMonth() + Math.abs(dateDiff.getDays()) + getDay();
                } else {
                    return Math.abs(dateDiff.getDays()) + getDay();
                }

            } catch (Exception e) {
               LogUtil.error("org.sample.TimeAgoDatalistFormatter", e, "Error!!");
            }

        } else if (duration.equals("twoDates")) {
            
            // Obtain input From Date
            String fromDateStr = getPropertyString("fromDate");
            fromDateStr = (String) DataListService.evaluateColumnValueFromRow(row, fromDateStr);
            String formattedFromDate = checkDateFormats(fromDateStr); // Format date to yyyy-MM-dd format
            
            // Obtain input To Date
            String toDateStr = getPropertyString("toDate");
            toDateStr = (String) DataListService.evaluateColumnValueFromRow(row, toDateStr);
            String formattedToDate = checkDateFormats(toDateStr); // Format date to yyyy-MM-dd format

            // Parse
            LocalDate fromDate = LocalDate.parse(formattedFromDate); // From Date
            LocalDate toDate = LocalDate.parse(formattedToDate); // To Date

            // Duration Between Two Dates
            try {

                dateDiff = Period.between(fromDate, toDate);

                if (Math.abs(dateDiff.getYears()) > 0) {
                    return Math.abs(dateDiff.getYears()) + getYear() + Math.abs(dateDiff.getMonths()) +
                            getMonth() + Math.abs(dateDiff.getDays()) + getDay();
                } else if (Math.abs(dateDiff.getMonths()) > 0) {
                    return Math.abs(dateDiff.getMonths()) + getMonth() + Math.abs(dateDiff.getDays()) + getDay();
                } else {
                    return Math.abs(dateDiff.getDays()) + getDay();
                }

            } catch (Exception e) {
               LogUtil.error(TimeAgoDatalistFormatter.class.getName(), e, "Not able to compute duration");
            }

        }
             
        return result;
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
}
