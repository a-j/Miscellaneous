package aj.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class CSVToJSON {
    public static void main(String[] args) throws Exception {
        List<URLData> taggedData = new ArrayList<>();
        List<URLData> taggedNoData = new ArrayList<>();
        List<URLData> notTagged = new ArrayList<>();
        List<URLData> other = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader("data/weburls.csv"))) {
            List<String[]> lines = reader.readAll();
            for(String[] nextLine : lines) {
                URLData urlData = new URLData(nextLine);
                if (isTagged(urlData.getAnalyticsTools())) {
                    if (urlData.getVisits() > 0) {
                        taggedData.add(urlData);
                    } else {
                        urlData.setVisits(10000);   // just for it to show up in the bubble chart
                        taggedNoData.add(urlData);
                    }
                } else {
                    urlData.setVisits(10000);   // just for it to show up in the bubble chart
                    notTagged.add(urlData);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        System.out.println("Tagged Data: " + taggedData.size());
        System.out.println("Tagged NoData: " + taggedNoData.size());
        System.out.println("Not Tagged: " + notTagged.size());

        URLData urlData = new URLData();
        urlData.setDomain("Acquired Entities (96 Sites)");
        urlData.setType("XXX");
        urlData.setVisits(300000);
        urlData.setAnalyticsTools(Arrays.asList("No Tagging"));
        other.add(urlData);

        urlData = new URLData();
        urlData.setDomain("Other UHG URL's (87 Sites)");
        urlData.setType("XXX");
        urlData.setVisits(300000);
        urlData.setAnalyticsTools(Arrays.asList("No Tagging"));
        other.add(urlData);

        Map<String, Object> otherMap = new HashMap<>();
        otherMap.put("name", "other");
        otherMap.put("children", other);

        Map<String, Object> taggedDataMap = new HashMap<>();
        taggedDataMap.put("name", "tagged-data");
        taggedDataMap.put("children", taggedData);

        Map<String, Object> taggedNoDataMap = new HashMap<>();
        taggedNoDataMap.put("name", "tagged-no-data");
//        taggedNoDataMap.put("children", taggedNoData);
        if (taggedNoData.size() > 0) {
            List<URLData> tmpTaggedNoData = new ArrayList<>();

            urlData = new URLData();
            urlData.setDomain("Tagged, but no data yet (" + taggedNoData.size() + " Sites)");
            urlData.setType("XXX");
            urlData.setVisits(300000);
            urlData.setAnalyticsTools(Arrays.asList("No Tagging"));
            tmpTaggedNoData.add(urlData);

            taggedNoDataMap.put("children", tmpTaggedNoData);
        }

        Map<String, Object> notTaggedMap = new HashMap<>();
        notTaggedMap.put("name", "not-tagged");
//        notTaggedMap.put("children", notTagged);
        if (notTagged.size() > 0) {
            List<URLData> tmpNotTagged = new ArrayList<>();

            urlData = new URLData();
            urlData.setDomain("Not Tagged (" + notTagged.size() + " Sites)");
            urlData.setType("XXX");
            urlData.setVisits(300000);
            urlData.setAnalyticsTools(Arrays.asList("No Tagging"));
            tmpNotTagged.add(urlData);

            notTaggedMap.put("children", tmpNotTagged);
        }

        List<Map<String, Object>> maps = new ArrayList<>();
        maps.add(taggedDataMap);
        maps.add(taggedNoDataMap);
        maps.add(notTaggedMap);
        maps.add(otherMap);

        Map<String, Object> map = new HashMap<>();
        map.put("name", "weburls");
        map.put("children", maps);

        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(map));
    }

    private static boolean isTagged(List<String> tools) {
        if (tools.contains("Adobe") || tools.contains("GA") || tools.contains("Webtrends") || tools.contains("WebTrends")
                || tools.contains("Yahoo Analytics") || tools.contains("Wordpress Stats")) {
            return true;
        }
        return false;
    }
}

class URLData {
    private String domain;
    private String type;
    private int visits;
    private String displayValue;
    private List<String> analyticsTools;

    public URLData() {}

    public URLData(String[] columns) {
        if (columns.length >= 4) {
            this.domain = columns[0];
            this.type = columns[1];
            try {
                String num = columns[2].replace(",", "");
                this.visits = (int) Double.parseDouble(num);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            DecimalFormat df = new DecimalFormat("#.0");
            if (this.visits >= 1000 && this.visits < 1000000) {
                this.displayValue = df.format(this.visits/1000.0);
                this.displayValue += "K";
            } else if (this.visits >= 1000000) {
                this.displayValue = df.format(this.visits/1000000.0);
                this.displayValue += "M";
            } else {
                this.displayValue = String.valueOf(this.visits);
            }

            this.analyticsTools = Arrays.asList(columns[3].trim().split("\\s*,\\s*"));
        }
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getVisits() {
        return visits;
    }

    public void setVisits(int visits) {
        this.visits = visits;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public List<String> getAnalyticsTools() {
        return analyticsTools;
    }

    public void setAnalyticsTools(List<String> analyticsTools) {
        this.analyticsTools = analyticsTools;
    }

    public String toString() {
        return domain + "|" + type + "|" + visits + "|" + analyticsTools;
    }
}