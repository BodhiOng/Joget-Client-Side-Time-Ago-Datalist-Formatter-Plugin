package org.joget.marketplace;

import java.util.UUID;

import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormatDefault;
import org.joget.apps.datalist.service.DataListService;

public class TimeAgoDatalistFormatter extends DataListColumnFormatDefault {

        private final static String MESSAGE_PATH = "messages/TimeAgoDatalistFormatter";

        public String getName() {
                return "Time Ago Datalist Formatter";
        }

        public String getVersion() {
                return "7.0.3";
        }

        public String getDescription() {
                // support i18n
                return AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.pluginDesc",
                                getClassName(),
                                MESSAGE_PATH);
        }

        public String getYear() {
                // support i18n
                return " " + AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.year(s)",
                                getClassName(),
                                MESSAGE_PATH) + " ";
        }

        public String getMonth() {
                // support i18n
                return " " + AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.month(s)",
                                getClassName(),
                                MESSAGE_PATH) + " ";
        }

        public String getDay() {
                // support i18n
                return " " + AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.day(s)",
                                getClassName(),
                                MESSAGE_PATH) + " ";
        }

        public String getHour() {
                // support i18n
                return " " + AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.hour(s)",
                                getClassName(),
                                MESSAGE_PATH) + " ";
        }

        public String getMinute() {
                // support i18n
                return " " + AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.minute(s)",
                                getClassName(), MESSAGE_PATH) + " ";
        }

        public String getTryDefaultFormatErrorMsg() {
                return AppPluginUtil.getMessage(
                                "org.joget.marketplace.TimeAgoDatalistFormatter.tryDefaultFormatErrorMsg",
                                getClassName(), MESSAGE_PATH);
        }

        public String getLabel() {
                // support i18n
                return AppPluginUtil.getMessage("org.joget.marketplace.TimeAgoDatalistFormatter.pluginLabel",
                                getClassName(),
                                MESSAGE_PATH);
        }

        public String getClassName() {
                return getClass().getName();
        }

        public String getPropertyOptions() {
                return AppUtil.readPluginResource(getClassName(), "/properties/TimeAgoDatalistFormatter.json", null,
                                true,
                                MESSAGE_PATH);
        }

        @Override
        public String format(DataList dataList, DataListColumn column, Object row, Object value) {
                String result = (String) value;
                String duration = getPropertyString("duration");
                String uniqueId = "timeago_" + UUID.randomUUID().toString().replace("-", "");
                String dateOutputFormat = getPropertyString("dateOutputFormat");
                String inclDateOutputFormat = getPropertyString("inclDateOutputFormat");

                StringBuilder script = new StringBuilder();
                script.append("<span id=\"" + uniqueId + "\" class=\"time-ago-formatter\" data-value=\""
                                + escapeHtml(result)
                                + "\">");
                script.append(result); // Default value until JavaScript runs
                script.append("</span>\n");

                script.append("<script>\n");
                script.append("(function() {\n");
                script.append("  const element = document.getElementById('" + uniqueId + "');\n");
                script.append("  const value = element.getAttribute('data-value');\n");

                // Add the duration type
                script.append("  const durationType = '" + duration + "';\n");

                // Add the date output format preferences
                script.append("  const dateOutputFormat = '" + dateOutputFormat + "';\n");
                script.append("  const inclDateOutputFormat = '" + inclDateOutputFormat + "';\n");

                // Add internationalization strings
                script.append("  const i18n = {\n");
                script.append("    year: '" + getYear().trim() + "',\n");
                script.append("    month: '" + getMonth().trim() + "',\n");
                script.append("    day: '" + getDay().trim() + "',\n");
                script.append("    hour: '" + getHour().trim() + "',\n");
                script.append("    minute: '" + getMinute().trim() + "'\n");
                script.append("  };\n");

                // Add target date for "anotherDate" mode
                if (duration.equals("anotherDate")) {
                        String targetStr = getPropertyString("targetDate");
                        targetStr = (String) DataListService.evaluateColumnValueFromRow(row, targetStr);
                        script.append("  const targetDate = '" + escapeHtml(targetStr) + "';\n");
                }

                // Add from and to dates for "twoDates" mode
                if (duration.equals("twoDates")) {
                        String fromStr = getPropertyString("fromDate");
                        fromStr = (String) DataListService.evaluateColumnValueFromRow(row, fromStr);
                        String toStr = getPropertyString("toDate");
                        toStr = (String) DataListService.evaluateColumnValueFromRow(row, toStr);
                        script.append("  const fromDate = '" + escapeHtml(fromStr) + "';\n");
                        script.append("  const toDate = '" + escapeHtml(toStr) + "';\n");
                }

                // Include the client-side time-ago calculation function
                script.append(getClientSideTimeAgoScript());

                // Call the function based on duration type
                if (duration.equals("today")) {
                        script.append(
                                        "  const formattedValue = calculateTimeAgo(value, new Date().toISOString(), i18n, dateOutputFormat, inclDateOutputFormat);\n");
                } else if (duration.equals("anotherDate")) {
                        script.append(
                                        "  const formattedValue = calculateTimeAgo(value, targetDate, i18n, dateOutputFormat, inclDateOutputFormat);\n");
                } else if (duration.equals("twoDates")) {
                        script.append(
                                        "  const formattedValue = calculateTimeAgo(fromDate, toDate, i18n, dateOutputFormat, inclDateOutputFormat);\n");
                }

                script.append("  if (formattedValue) {\n");
                script.append("    element.textContent = formattedValue;\n");
                script.append("  }\n");
                script.append("})();\n");
                script.append("</script>");

                return script.toString();
        }

        /**
         * Escape HTML special characters to prevent XSS
         */
        private String escapeHtml(String input) {
                if (input == null) {
                        return "";
                }
                return input.replace("&", "&amp;")
                                .replace("<", "&lt;")
                                .replace(">", "&gt;")
                                .replace("\"", "&quot;")
                                .replace("'", "&#39;");
        }

        /**
         * Returns the JavaScript code for client-side time-ago calculation
         */
        private String getClientSideTimeAgoScript() {
                StringBuilder script = new StringBuilder();

                script.append("  function calculateTimeAgo(date1, date2, i18n, dateOutputFormat, inclDateOutputFormat) {\n");
                script.append("    try {\n");
                script.append("      // Parse dates\n");
                script.append("      const parsedDate1 = parseMultiFormatDate(date1);\n");
                script.append("      const parsedDate2 = parseMultiFormatDate(date2);\n");
                script.append("      \n");
                script.append("      if (!parsedDate1 || !parsedDate2) {\n");
                script.append("        return null;\n");
                script.append("      }\n");
                script.append("      \n");
                script.append("      // Calculate difference\n");
                script.append("      const diff = calculateDateTimeDifference(parsedDate1, parsedDate2, i18n);\n");
                script.append("      \n");
                script.append("      // Format output if needed\n");
                script.append("      if (dateOutputFormat && dateOutputFormat !== '') {\n");
                script.append(
                                "        return formatDateTimeOutput(diff, dateOutputFormat, inclDateOutputFormat === 'true', i18n);\n");
                script.append("      }\n");
                script.append("      \n");
                script.append("      return diff;\n");
                script.append("    } catch (e) {\n");
                script.append("      console.error('Time-ago calculation error:', e);\n");
                script.append("      return null;\n");
                script.append("    }\n");
                script.append("  }\n");

                // Add date parsing function
                script.append("  function parseMultiFormatDate(dateString) {\n");
                script.append("    if (!dateString) return null;\n");
                script.append("    \n");
                script.append("    // Try standard ISO format first\n");
                script.append("    let date = new Date(dateString);\n");
                script.append("    if (!isNaN(date.getTime())) {\n");
                script.append("      return date;\n");
                script.append("    }\n");
                script.append("    \n");
                script.append("    // Try common date formats\n");
                script.append("    const formats = [\n");
                script.append("      // Date only formats\n");
                script.append(
                                "      { regex: /^(\\d{4})-(\\d{1,2})-(\\d{1,2})$/, fn: (m) => new Date(parseInt(m[1]), parseInt(m[2])-1, parseInt(m[3])) },\n");
                script.append(
                                "      { regex: /^(\\d{1,2})-(\\d{1,2})-(\\d{4})$/, fn: (m) => new Date(parseInt(m[3]), parseInt(m[2])-1, parseInt(m[1])) },\n");
                script.append("      \n");
                script.append("      // Date time formats\n");
                script.append(
                                "      { regex: /^(\\d{4})-(\\d{1,2})-(\\d{1,2})[ T](\\d{1,2}):(\\d{1,2})(?::(\\d{1,2}))?(?:\\s*([aApP][mM]))?$/, \n");
                script.append("        fn: (m) => {\n");
                script.append("          let hours = parseInt(m[4]);\n");
                script.append("          const ampm = m[7] ? m[7].toLowerCase() : null;\n");
                script.append("          if (ampm === 'pm' && hours < 12) hours += 12;\n");
                script.append("          if (ampm === 'am' && hours === 12) hours = 0;\n");
                script.append(
                                "          return new Date(parseInt(m[1]), parseInt(m[2])-1, parseInt(m[3]), hours, parseInt(m[5]), m[6] ? parseInt(m[6]) : 0);\n");
                script.append("        }\n");
                script.append("      },\n");
                script.append("      \n");
                script.append("      // Time only formats (assuming today's date)\n");
                script.append("      { regex: /^(\\d{1,2}):(\\d{1,2})(?:\\s*([aApP][mM]))?$/, \n");
                script.append("        fn: (m) => {\n");
                script.append("          let hours = parseInt(m[1]);\n");
                script.append("          const ampm = m[3] ? m[3].toLowerCase() : null;\n");
                script.append("          if (ampm === 'pm' && hours < 12) hours += 12;\n");
                script.append("          if (ampm === 'am' && hours === 12) hours = 0;\n");
                script.append("          const today = new Date();\n");
                script.append(
                                "          return new Date(today.getFullYear(), today.getMonth(), today.getDate(), hours, parseInt(m[2]), 0);\n");
                script.append("        }\n");
                script.append("      }\n");
                script.append("    ];\n");
                script.append("    \n");
                script.append("    // Try each format\n");
                script.append("    for (const format of formats) {\n");
                script.append("      const match = dateString.match(format.regex);\n");
                script.append("      if (match) {\n");
                script.append("        const parsedDate = format.fn(match);\n");
                script.append("        if (!isNaN(parsedDate.getTime())) {\n");
                script.append("          return parsedDate;\n");
                script.append("        }\n");
                script.append("      }\n");
                script.append("    }\n");
                script.append("    \n");
                script.append("    return null;\n");
                script.append("  }\n");

                // Add date difference calculation function
                script.append("  function calculateDateTimeDifference(date1, date2, i18n) {\n");
                script.append("    // Ensure date2 is greater than date1\n");
                script.append("    if (date1 > date2) {\n");
                script.append("      [date1, date2] = [date2, date1];\n");
                script.append("    }\n");
                script.append("    \n");
                script.append("    const diffMs = date2 - date1;\n");
                script.append("    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));\n");
                script.append("    \n");
                script.append("    // Calculate years, months and days properly\n");
                script.append("    let years = date2.getFullYear() - date1.getFullYear();\n");
                script.append("    let months = date2.getMonth() - date1.getMonth();\n");
                script.append("    let days = date2.getDate() - date1.getDate();\n");
                script.append("    \n");
                script.append("    // Adjust for negative days\n");
                script.append("    if (days < 0) {\n");
                script.append("      // Get the number of days in the previous month\n");
                script.append("      const lastMonth = new Date(date2.getFullYear(), date2.getMonth(), 0);\n");
                script.append("      days += lastMonth.getDate();\n");
                script.append("      months--;\n");
                script.append("    }\n");
                script.append("    \n");
                script.append("    // Adjust for negative months\n");
                script.append("    if (months < 0) {\n");
                script.append("      months += 12;\n");
                script.append("      years--;\n");
                script.append("    }\n");
                script.append("    \n");
                script.append("    // Calculate hours and minutes\n");
                script.append("    const diffHours = Math.floor((diffMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));\n");
                script.append("    const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));\n");
                script.append("    \n");
                script.append("    // Format the result\n");
                script.append("    let result = '';\n");
                script.append("    \n");
                script.append("    if (years > 0) {\n");
                script.append("      result += years + ' ' + i18n.year.trim() + ' ' + months + ' ' + i18n.month.trim() + ' ' + days + ' ' + i18n.day.trim();\n");
                script.append("    } else if (months > 0) {\n");
                script.append("      result += months + ' ' + i18n.month.trim() + ' ' + days + ' ' + i18n.day.trim();\n");
                script.append("    } else if (diffDays > 0) {\n");
                script.append("      result += diffDays + ' ' + i18n.day.trim();\n");
                script.append("    }\n");
                script.append("    \n");
                script.append("    // Add time difference if there are hours or minutes\n");
                script.append("    if (diffHours > 0 || (diffDays === 0 && diffMinutes > 0)) {\n");
                script.append("      if (diffHours > 0) {\n");
                script.append("        result += (result ? ' ' : '') + diffHours + ' ' + i18n.hour.trim();\n");
                script.append("      }\n");
                script.append("      if (diffMinutes > 0) {\n");
                script.append("        result += (result && diffHours > 0 ? ' ' : '') + diffMinutes + ' ' + i18n.minute.trim();\n");
                script.append("      }\n");
                script.append("    }\n");
                script.append("    \n");
                script.append("    return result;\n");
                script.append("  }\n");

                // Add output formatting function
                script.append("  function formatDateTimeOutput(time, dateOutputFormat, inclDateOutputFormat, i18n) {\n");
                script.append("    const parts = dateOutputFormat.split(';');\n");
                script.append("    let finalOutput = '';\n");
                script.append("    \n");
                script.append("    for (const part of parts) {\n");
                script.append("      let regex;\n");
                script.append("      switch (part) {\n");
                script.append("        case 'year':\n");
                script.append("          regex = inclDateOutputFormat ? \n");
                script.append(
                                "            new RegExp('\\\\b(\\\\d+\\\\s+' + i18n.year.trim().replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&') + ')') : \n");
                script.append(
                                "            new RegExp('\\\\b(\\\\d+)\\\\s+' + i18n.year.trim().replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&'));\n");
                script.append("          break;\n");
                script.append("        case 'month':\n");
                script.append("          regex = inclDateOutputFormat ? \n");
                script.append(
                                "            new RegExp('\\\\b(\\\\d+\\\\s+' + i18n.month.trim().replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&') + ')') : \n");
                script.append(
                                "            new RegExp('\\\\b(\\\\d+)\\\\s+' + i18n.month.trim().replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&'));\n");
                script.append("          break;\n");
                script.append("        case 'day':\n");
                script.append("          regex = inclDateOutputFormat ? \n");
                script.append(
                                "            new RegExp('\\\\b(\\\\d+\\\\s+' + i18n.day.trim().replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&') + ')') : \n");
                script.append(
                                "            new RegExp('\\\\b(\\\\d+)\\\\s+' + i18n.day.trim().replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&'));\n");
                script.append("          break;\n");
                script.append("        case 'hour':\n");
                script.append("          regex = inclDateOutputFormat ? \n");
                script.append(
                                "            new RegExp('\\\\b(\\\\d+\\\\s+' + i18n.hour.trim().replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&') + ')') : \n");
                script.append(
                                "            new RegExp('\\\\b(\\\\d+)\\\\s+' + i18n.hour.trim().replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&'));\n");
                script.append("          break;\n");
                script.append("        case 'minute':\n");
                script.append("          regex = inclDateOutputFormat ? \n");
                script.append(
                                "            new RegExp('\\\\b(\\\\d+\\\\s+' + i18n.minute.trim().replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&') + ')') : \n");
                script.append(
                                "            new RegExp('\\\\b(\\\\d+)\\\\s+' + i18n.minute.trim().replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&'));\n");
                script.append("          break;\n");
                script.append("        default:\n");
                script.append("          continue;\n");
                script.append("      }\n");
                script.append("      \n");
                script.append("      const match = time.match(regex);\n");
                script.append("      if (match) {\n");
                script.append("        finalOutput += match[1] + ' ';\n");
                script.append("      }\n");
                script.append("    }\n");
                script.append("    \n");
                script.append("    return finalOutput.trim();\n");
                script.append("  }\n");

                return script.toString();
        }
}