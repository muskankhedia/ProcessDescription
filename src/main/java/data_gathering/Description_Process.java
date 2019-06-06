package data_gathering;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Description_Process {

    private LinkedHashMap<String, String> searchProcess(String searchURL)  {

        try {
            Document doc = Jsoup.connect(searchURL).userAgent("Mozilla/5.0").get();
            String text = doc.toString();
//            System.out.println("text:: "+ text);
            LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
            String subStr = "";
            String subStr2 = "";
            for (int i = 0; i < text.length(); ++i ) {
                String str1 = "<h2>";
                String str2 = "</h2>";
                int index1 = text.indexOf(str1) + str1.length() ;
                int index2 = text.indexOf(str2) + str2.length() + 1;
                String heading = text.substring(index1, text.indexOf(str2));
//                System.out.println("heading:: "+ heading);
                int firstIndex = 0;
                if (text.indexOf(str2) >= 0) {
                    subStr = text.substring(index2);
                    firstIndex = subStr.indexOf(str1);
                    if (firstIndex > 0){
                        i = 0;
                        text = subStr;
                        subStr2 = subStr.substring(0,firstIndex);
                    } else {
                        subStr2 = subStr;
                        String desc =  Jsoup.parse(subStr2).text();
//                        System.out.println("desc:: "+ desc);
                        result.put(heading,desc);
                        break;
                    }
                }
                String desc =  Jsoup.parse(subStr2).text();
//                System.out.println("desc:: "+ desc);
                result.put(heading,desc);
            }
//            System.out.println(result);
            return result;
        }
        catch (Exception e) {
            return null;
        }
    }

    private void iterateAllLinks() throws IOException {

        JSONArray processList = new JSONArray();
        for (int i = 1; i <= 10; i++) {
            String baseURL = "https://linux.die.net/man/";

            if (i < 9) {
                baseURL = baseURL + i;
            } else if (i == 9) {
                baseURL = baseURL + "l";
            } else {
                baseURL = baseURL + "n";
            }

            System.out.println("baseURL: " + baseURL);
            Document doc = Jsoup.connect(baseURL).userAgent("Mozilla/5.0").get();
            Elements results = doc.select("dl > dt");
            iterateElements(baseURL, results, i);
        }
    }

    public void iterateElements (String baseURL, Elements results, int i) {
        char initialLetter = '0';
        JSONArray processList = new JSONArray();
        for (Element result : results) {
            Elements link = result.getElementsByTag("a");
            String processName = link.text();
            if (!processName.equals("")) {
                if (processName.charAt(0) == initialLetter) {
                    String searchURL = baseURL + "/" + processName;
                    JSONObject processObject = storeData(searchURL, processName);
                    processList.add(processObject);

                } else {
                    //Write JSON file
                    String fileName  = "processdesc" + i + initialLetter + ".json";
                    try  {
                        FileWriter file = new FileWriter(fileName);
//                            initialLetter += 1 ;
                        file.write(processList.toJSONString());
                        file.flush();
                        processList = new JSONArray();
                        while (!(initialLetter == processName.charAt(0))) {
                            initialLetter += 1;
                        }
                        if (processName.charAt(0) == initialLetter){
                            String searchURL = baseURL + "/" + processName;
                            JSONObject processObject = storeData(searchURL, processName);
                            processList.add(processObject);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public JSONObject storeData (String url, String processName) {
        JSONObject processData = new JSONObject();
        JSONObject processObject = new JSONObject();
        System.out.println("searchURL:: " + url);
        LinkedHashMap<String, String> data = searchProcess(url);
        System.out.println("processname: " + processName);
        System.out.println("data: " + data);
        processData.put("Process Name", processName);
        processData.put("Link", url);
        try {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String heading = entry.getKey();
                String desc = entry.getValue();
                processData.put(heading, desc);
            }
            processObject.put("Process", processData);
            return processObject;
        } catch (Exception e){
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        Description_Process p = new Description_Process();
//        System.out.println(p.searchProcess("https://linux.die.net/man/1/cheatmake"));
//        p.searchProcess("https://linux.die.net/man/1/cheatmake");
        p.iterateAllLinks();
    }

}