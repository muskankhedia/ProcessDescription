package data_gathering;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;

public class Description_Process {


    private String searchProcess(String searchURL)  {

        try {
            Document doc = Jsoup.connect(searchURL).userAgent("Mozilla/5.0").get();
            String text = doc.toString();
            String subStr = "";
            String str1 = "<h2>Description</h2>";
            String str2 = "<h2>";
            int lastIndex = text.indexOf(str1) + str1.length() + 1;
            if (text.indexOf(str1) >= 1) {
                subStr = text.substring(lastIndex);
                int firstIndex = subStr.indexOf(str2);
                if (firstIndex > 1){
                    subStr = subStr.substring(0,firstIndex);
                }
            }

            String result =  Jsoup.parse(subStr).text();
            return result;
        }
        catch (Exception e) {
            return null;
        }
    }

    private void iterateAllLinks() throws IOException {

        JSONArray processList = new JSONArray();
        char initialLetter = '0';
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

            for (Element result : results) {
                Elements link = result.getElementsByTag("a");
                String processName = link.text();
                if (!processName.equals("")) {
                    if (processName.charAt(0) == initialLetter) {
                        JSONObject processData = new JSONObject();
                        JSONObject processObject = new JSONObject();
                        String searchURL = baseURL + "/" + processName;
                        System.out.println("searchURL:: " + searchURL);
                        String data = searchProcess(searchURL);
                        System.out.println("processname: " + processName);
                        System.out.println("data: " + data);
                        processData.put("Process Name", processName);
                        processData.put("Link", searchURL);
                        processData.put("Description", data);
                        processObject.put("Process", processData);
                        processList.add(processObject);
                    } else {
                        //Write JSON file
                        String fileName  = "processdesc" + i + initialLetter + ".json";
                        try (FileWriter file = new FileWriter(fileName)) {
//                            initialLetter += 1 ;
                            file.write(processList.toJSONString());
                            file.flush();
                            processList = new JSONArray();
                            while (!(initialLetter == processName.charAt(0))) {
                                initialLetter += 1;
                            }
                            if (processName.charAt(0) == initialLetter){
                                JSONObject processData = new JSONObject();
                                JSONObject processObject = new JSONObject();
                                String searchURL = baseURL + "/" + processName;
                                System.out.println("searchURL:: " + searchURL);
                                String data = searchProcess(searchURL);
                                System.out.println("processname: " + processName);
                                System.out.println("data: " + data);
                                processData.put("Process Name", processName);
                                processData.put("Link", searchURL);
                                processData.put("Description", data);
                                processObject.put("Process", processData);
                                processList.add(processObject);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }



    public static void main(String[] args) throws IOException {
        Description_Process p = new Description_Process();
//        System.out.println(p.searchProcess("https://linux.die.net/man/1/cheatmake"));
        p.iterateAllLinks();
    }

}