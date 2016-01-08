package sharecrew.net.fragpanel.reports;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class HTTPFetchSteam {

    private String steam_id;

    public HTTPFetchSteam(String steam_id){
        this.steam_id = steam_id;
    }

    private String connect(){
        String link = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=F9270B3CA08D28AB8D7A6F60163EA527&steamids=";
        String format = "&format=xml";

        String newLink = link + steam_id + format;
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

    public String fetch_steam_avatar(){
        String xml = connect();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;

        try{
            db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            try {
                Document doc = db.parse(is);
                return doc.getElementsByTagName("avatarfull").item(0).getTextContent();
            }catch(Exception e){
                e.getStackTrace();
            }
        }catch(ParserConfigurationException e){
            e.getStackTrace();
        }
        return "ERROR";
    }
}
