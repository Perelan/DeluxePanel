package sharecrew.net.fragpanel.reports;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import sharecrew.net.fragpanel.extra.Utility;

public class HTTPReportRequest {

    // 10.0.3.2 <-- GenyMotion

    public String connect(){
        String link = "fetch_report.php";
        String newLink = Utility.WEBSITE + link + "?key=" + Utility.KEY;
        URL url;
        HttpURLConnection urlConnection = null;

        System.out.println(newLink);
        try{
            url = new URL(newLink);
            urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(in);

            int data = isr.read();
            StringBuilder sb = new StringBuilder();

            while (data != -1) {
                char current = (char) data;
                data = isr.read();
                sb.append(current);
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert urlConnection != null;
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
