package sharecrew.net.fragpanel.login;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sharecrew.net.fragpanel.extra.Utility;
import sharecrew.net.fragpanel.reports.HTTPReportRequest;

public class HTTPLoginRequest {

   private String username, password;

    // 10.0.3.2 <-- GenyMotion

    private final String LINK = "fetch_admin.php";

    public HTTPLoginRequest(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String connect(){
        String newLink = Utility.WEBSITE + LINK + "?admin_username=" + username +
                        "&admin_password=" + decode_password(password);
        URL url;
        HttpURLConnection urlConnection = null;

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

    private String decode_password(final String password){
        try{
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(password.getBytes());
            byte msgDigest[] = digest.digest();

            StringBuilder sb = new StringBuilder();

            for(byte a : msgDigest){
                String h = Integer.toHexString(0xFF & a);
                while (h.length() < 2)
                    h = "0" + h;
                sb.append(h);
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }
}